package io.opentelemetry.example.autoconfigure;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;

public class EmptyAttrTestMain {

  public static void main(String[] args) throws Exception {

    System.setProperty("otel.exporter.otlp.protocol", "http/protobuf");

    AutoConfiguredOpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.builder()
        .addResourceCustomizer((resource, config) -> resource.merge(Resource.builder()
                .put("deployment.environment.name", "jjp-test")
                .put("service.name", "jjp-test")
            .build()))
        .build();

    OpenTelemetrySdk otel = sdk.getOpenTelemetrySdk();

    Span span = otel.getTracer("jjp")
        .spanBuilder("test.span")
        .setAttribute("foo", "bar")
        .setAttribute("empty", "")
        .startSpan();

    span.makeCurrent();
    Thread.sleep(500);
    span.end();
  }

}
