package com.gliaci.revenue.optimizer.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gliaci.revenue.optimizer.serde.JSONSerde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The key of the table is the concatenation of the currency codes. For examples {@code EUR-USD}.
 */
@Configuration
public class ExchangeRatesTableConfiguration {

  public record ExchangeRateKey(
      @JsonProperty("FROM_CURRENCY") String fromCurrency,
      @JsonProperty("TO_CURRENCY") String toCurrency) {}

  public record ExchangeRate(
      @JsonProperty("FROM_CURRENCY") String fromCurrency,
      @JsonProperty("TO_CURRENCY") String toCurrency,
      @JsonProperty("RATE") String rate) {}

  @Bean
  public JSONSerde<ExchangeRateKey> exchangeKeySerdes() {
    return new JSONSerde<>(ExchangeRateKey.class);
  }

  @Bean
  public JSONSerde<ExchangeRate> exchangeSerdes() {
    return new JSONSerde<>(ExchangeRate.class);
  }

  /** The topic {@code exchangeRates} must be declared as "compacted". */
  @Bean
  public GlobalKTable<ExchangeRateKey, ExchangeRate> exchangeRatesGlobalKTable(
      @Value("${revenue-optimizer.kafka.topics.exchange-rates}") String exchangeRatesTopic,
      StreamsBuilder builder,
      JSONSerde<ExchangeRateKey> exchangeKeySerdes,
      JSONSerde<ExchangeRate> exchangeSerdes) {
    return builder.globalTable(
        exchangeRatesTopic,
        Consumed.with(exchangeKeySerdes, exchangeSerdes),
        Materialized.as("exchangeRates"));
  }
}
