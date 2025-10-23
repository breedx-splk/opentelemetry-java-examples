package io.opentelemetry.example.autoconfigure;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.CodecConfiguration;
import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.propagation.TextMapCodec;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentracing.propagation.Format.Builtin;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EmptyAttrTestMain {

  public static void main(String[] args) throws Exception {
    Map<String, String> tracerTags = new HashMap<>();
    tracerTags.put("tfoo", "tbar");
    tracerTags.put("tboop", "tbeep");
    Configuration configuration = Configuration.fromEnv()
        .withServiceName("jjp-test")
        .withTracerTags(tracerTags)
        .withCodec();
//    CodecConfiguration codecConfig = new CodecConfiguration();
//    TextMapCodec codec = new TextMapCodec(true);
//    codec.inj
//    codecConfig.withCodec(Builtin.HTTP_HEADERS, codec);

//    Configuration configuration = new Configuration("jjp-test")
//        .withTracerTags(tracerTags)
//        .withCodec(codecConfig);

    JaegerTracer tracer = configuration.getTracer();

    JaegerSpan span = tracer.buildSpan("test.span")
        .withTag("somedupe", "foo")
        .withTag("somedupe", "bar")
        .withTag("somedupe", "baz")
        .start();
    Thread.sleep(500);
    span.finish();
    tracer.close();
  }

  public static void xmain(String[] args) throws Exception {

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
