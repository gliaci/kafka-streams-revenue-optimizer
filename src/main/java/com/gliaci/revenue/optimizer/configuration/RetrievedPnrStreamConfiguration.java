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
public class RetrievedPnrStreamConfiguration {
  public record RetrievedPnr(Long orderId, String pnr, Double amount, String currency) {}

  @Bean
  public JSONSerde<RetrievedPnr> retrievedPnrSerde() {
    return new JSONSerde<>(RetrievedPnr.class);
  }

  @Bean
  public KStream<Long, RetrievedPnr> retrievedPnrsKStream(
      StreamsBuilder builder,
      JSONSerde<RetrievedPnr> retrievedPnrSerde,
      @Value("${revenue-optimizer.kafka.topics.retrieved-pnrs}") String retrievedPnrsTopic) {
    return builder.stream(retrievedPnrsTopic, Consumed.with(Serdes.Long(), retrievedPnrSerde));
  }
}
