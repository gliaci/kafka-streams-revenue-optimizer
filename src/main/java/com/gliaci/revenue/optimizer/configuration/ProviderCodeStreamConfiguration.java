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
public class ProviderCodeStreamConfiguration
{
  public record ProviderCode(Long orderId, String providerCode, Double amount, String currency) {}

  @Bean
  public JSONSerde<ProviderCode> providerCodeSerde() {
    return new JSONSerde<>(ProviderCode.class);
  }

  @Bean
  public KStream<Long, ProviderCode> providerCodesKStream(
      StreamsBuilder builder,
      JSONSerde<ProviderCode> providerCodeSerde,
      @Value("${revenue-optimizer.kafka.topics.provider-codes}") String providerCodesTopic) {
    return builder.stream(providerCodesTopic, Consumed.with(Serdes.Long(), providerCodeSerde));
  }
}
