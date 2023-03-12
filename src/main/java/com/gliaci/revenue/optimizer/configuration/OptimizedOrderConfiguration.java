package com.gliaci.revenue.optimizer.configuration;

import com.gliaci.revenue.optimizer.configuration.ExchangeRatesTableConfiguration.ExchangeRateKey;
import com.gliaci.revenue.optimizer.configuration.ProviderCodeStreamConfiguration.ProviderCode;
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

  public record OptimizedOrder(Long orderId, String providerCode, BigDecimal savingAmount) {}

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
      KStream<Long, ProviderCode> providerCodesKStream,
      JSONSerde<ProviderCode> providerCodeSerde,
      @Value("${revenue-optimizer.kafka.topics.optimized-orders}")
          String optimizedBookingSavingTopic) {

    // ProviderCode, but in EUR
    final KStream<Long, ProviderCode> providerCodeInEurKStream =
        providerCodesKStream.join(
            exchangeRatesGlobalKTable,
            (key, value) -> new ExchangeRateKey(value.currency(), "EUR"),
            (fco, exchange) ->
                new ProviderCode(
                    fco.orderId(),
                    fco.providerCode(),
                    fco.amount() * Double.parseDouble(exchange.rate()),
                    "EUR"));

    final KStream<Long, OptimizedOrder> optimizedOrderStream =
        ordersKStream.join(
            providerCodeInEurKStream,
            (order, providerCode) ->
                new OptimizedOrder(
                    order.orderId(), order.providerCode(), BigDecimal.valueOf(order.amount() - providerCode.amount()).setScale(2, HALF_UP)),
            JoinWindows.ofTimeDifferenceWithNoGrace(OPTIMIZED_ORDER_JOIN_WINDOWS),
            StreamJoined.with(Serdes.Long(), orderSerde, providerCodeSerde));

    optimizedOrderStream.to(
        optimizedBookingSavingTopic, Produced.with(Serdes.Long(), optimizedOrderSerde));

    optimizedOrderStream.foreach(
        (idBooking, optimizedOrder) ->
            LOGGER.info("[{}] OptimizedOrder: {}", idBooking, optimizedOrder));

    return optimizedOrderStream;
  }
}
