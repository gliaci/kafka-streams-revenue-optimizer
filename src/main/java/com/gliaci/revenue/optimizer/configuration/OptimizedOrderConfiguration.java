package com.gliaci.revenue.optimizer.configuration;

import com.gliaci.revenue.optimizer.configuration.ExchangeRatesTableConfiguration.ExchangeRateKey;
import com.gliaci.revenue.optimizer.configuration.RetrievedPnrStreamConfiguration.RetrievedPnr;
import com.gliaci.revenue.optimizer.serde.JSONSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static com.gliaci.revenue.optimizer.configuration.ExchangeRatesTableConfiguration.ExchangeRate;
import static com.gliaci.revenue.optimizer.configuration.OrderStreamConfiguration.Order;
import static java.math.RoundingMode.HALF_UP;

@Configuration
public class OptimizedOrderConfiguration {
  private static final Logger LOGGER = LoggerFactory.getLogger(OptimizedOrderConfiguration.class);

  private static final Duration OPTIMIZED_ORDER_JOIN_WINDOWS = Duration.of(30, ChronoUnit.SECONDS);

  public record OptimizedOrder(Long orderId, String pnr, BigDecimal savingAmount) {}

  @Bean
  public JSONSerde<OptimizedOrder> optimizedOrderSerde() {
    return new JSONSerde<>(OptimizedOrder.class);
  }

  @Bean
  public KStream<Long, OptimizedOrder> optimizedOrdersKStream(
      JSONSerde<OptimizedOrder> optimizedOrderSerde,
      GlobalKTable<ExchangeRateKey, ExchangeRate> exchangeRatesGlobalKTable,
      KStream<Long, Order> ordersKStream,
      JSONSerde<Order> orderSerde,
      KStream<Long, RetrievedPnr> retrievedPnrsKStream,
      JSONSerde<RetrievedPnr> retrievedPnrSerde,
      @Value("${revenue-optimizer.kafka.topics.optimized-orders}")
          String optimizedBookingSavingTopic) {

    // RetrievedPnr, but in EUR
    final KStream<Long, RetrievedPnr> retrievedPnrInEurKStream =
        retrievedPnrsKStream.join(
            exchangeRatesGlobalKTable,
            (key, value) -> new ExchangeRateKey(value.currency(), "EUR"),
            (fco, exchange) ->
                new RetrievedPnr(
                    fco.orderId(),
                    fco.pnr(),
                    fco.amount() * Double.parseDouble(exchange.rate()),
                    "EUR"));

    final KStream<Long, OptimizedOrder> optimizedPnrStream =
        ordersKStream.join(
            retrievedPnrInEurKStream,
            (order, retrievedPnr) ->
                new OptimizedOrder(
                    order.orderId(), order.pnr(), BigDecimal.valueOf(order.amount() - retrievedPnr.amount()).setScale(2, HALF_UP)),
            JoinWindows.ofTimeDifferenceWithNoGrace(OPTIMIZED_ORDER_JOIN_WINDOWS),
            StreamJoined.with(Serdes.Long(), orderSerde, retrievedPnrSerde));

    optimizedPnrStream.to(
        optimizedBookingSavingTopic, Produced.with(Serdes.Long(), optimizedOrderSerde));

    optimizedPnrStream.foreach(
        (idBooking, optimizedPnr) ->
            LOGGER.info("[{}] OptimizedOrder: {}", idBooking, optimizedPnr));

    return optimizedPnrStream;
  }
}
