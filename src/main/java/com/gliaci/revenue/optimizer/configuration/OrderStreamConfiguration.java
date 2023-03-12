package com.gliaci.revenue.optimizer.configuration;

import com.gliaci.revenue.optimizer.serde.JSONSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderStreamConfiguration {

  public record Order(Long orderId, String providerCode, Double amount, String currency) {}

  @Bean
  public JSONSerde<Order> orderSerde() {
    return new JSONSerde<>(Order.class);
  }

  @Bean
  public KStream<Long, Order> ordersKStream(
      StreamsBuilder builder,
      JSONSerde<Order> orderSerde,
      @Value("${revenue-optimizer.kafka.topics.orders}") String ordersTopic) {
    return builder.stream(ordersTopic, Consumed.with(Serdes.Long(), orderSerde));
  }
}
