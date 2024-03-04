package io.opentelemetry.example.metrics;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.opentelemetry.sdk.metrics.data.AggregationTemporality.CUMULATIVE;
import static io.opentelemetry.sdk.metrics.data.AggregationTemporality.DELTA;

public class HistogramTemporalityExample {

    private final static int METRIC_EXPORT_INTERVAL_MS = 2000;

    // Each sub-list is recorded, and then we sleep for the logging reporter to show the summary
    private final static List<List<Integer>> values = List.of(
            List.of(1, 6, 2),
            List.of(9, 2, 1),
            List.of(3, 3, 3),
            List.of(0, 21, 1)
    );

    // For CUMULATIVE, this is the result:
    // getSum=9.0, getCount=3, getMin=1.0, getMax=6.0
    // getSum=21.0, getCount=6, getMin=1.0, getMax=9.0
    // getSum=30.0, getCount=9, getMin=1.0, getMax=9.0
    // getSum=52.0, getCount=12, getMin=0.0, getMax=21.0

    // For DELTA, this is the result:
    // getSum=9.0, getCount=3, getMin=1.0, getMax=6.0
    // getSum=12.0, getCount=3, getMin=1.0, getMax=9.0
    // getSum=9.0, getCount=3, getMin=3.0, getMax=3.0
    // getSum=22.0, getCount=3, getMin=0.0, getMax=21.0

    public static void main(String[] args) {
        // Comment out the one you want
         AggregationTemporality temporality = CUMULATIVE;
//        AggregationTemporality temporality = DELTA;
        OpenTelemetry otel = initOpenTelemetry(temporality);
        Meter meter = otel.getMeter("io.opentelemetry.example.metrics");
        LongHistogram histogram = meter.histogramBuilder("some.numbers")
                .ofLongs() // Required to get a LongHistogram, default is DoubleHistogram
                .setDescription("A bunch of numbers")
                .setUnit("units")
                .build();
        values.forEach(subList -> {
            subList.forEach(histogram::record);
            sleep();
        });
    }

    private static void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(METRIC_EXPORT_INTERVAL_MS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static OpenTelemetry initOpenTelemetry(AggregationTemporality temporality) {
        // Wire up a logging exporter for metrics
        MetricReader periodicReader =
                PeriodicMetricReader.builder(LoggingMetricExporter.create(temporality))
                        .setInterval(Duration.ofMillis(METRIC_EXPORT_INTERVAL_MS))
                        .build();

        SdkMeterProvider meterProvider =
                SdkMeterProvider.builder().registerMetricReader(periodicReader).build();

        return OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .build();
    }

}
