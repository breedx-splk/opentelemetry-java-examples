package io.opentelemetry.example.autoconfigure;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;

public class EmptyAttrTestMain {

  public static void main(String[] args) throws Exception {

    System.setProperty("otel.exporter.otlp.protocol", "http/protobuf");

    AutoConfiguredOpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.builder()
        .addResourceCustomizer((resource, config) -> resource.merge(Resource.builder()
                .put("deployment.environment.name", "jjp-test")
                .put("service.name", "jjp-test")
                .put("service.namespace", "skyzone3000")
            .build()))
        .build();

    OpenTelemetrySdk otel = sdk.getOpenTelemetrySdk();

    Span span = otel.getTracer("jjp")
        .spanBuilder("test.span")
        .setAttribute("foo", "bar")
        .setAttribute("foo", "jibro")
        .setAttribute("empty", "")
        .setAttribute(AttributeKey.stringArrayKey("somelist"), Arrays.asList("foo", "bar", "baz") )
        .startSpan();

    span.makeCurrent();
    Thread.sleep(500);
    span.end();
  }

}
