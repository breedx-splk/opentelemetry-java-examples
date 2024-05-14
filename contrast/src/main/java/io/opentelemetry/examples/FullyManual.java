package io.opentelemetry.examples;

import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerPort;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class FullyManual {

    public static final int PORT = 8123;
    private final OpenTelemetry otel;

    public FullyManual(OpenTelemetry otel) {
        this.otel = otel;
    }

    public static void main(String[] args) throws Exception {
        // This would normally be done externally, via env vars etc.
        System.setProperty("otel.service.name", "manual.tracing");
        OpenTelemetry sdk = AutoConfiguredOpenTelemetrySdk.initialize()
                .getOpenTelemetrySdk();
        new FullyManual(sdk).runForever();
    }

    private void runForever() throws Exception {
        Server.builder()
                .port(new ServerPort(PORT, SessionProtocol.HTTP))
                .service("/manualTracing", buildHelloHandler())
                .build()
                .start();
        System.out.println("\n\033[0;36m*** MANUAL example started on port " + PORT + " ***\033[0m");
        runClientForever();
    }

    private void runClientForever() throws Exception {
        Tracer tracer = otel.getTracer("MyHttpClient");
        WebClient webClient = WebClient.of("http://localhost:" + PORT);
        while(true){
            System.out.print("\033[0;33mDoing a request from client to server....");
            Span span = tracer.spanBuilder("clientHttpRequest").startSpan();
            try(Scope scope = span.makeCurrent()) {
                String response = webClient.get("/manualTracing").aggregate().join()
                        .content()
                        .toString(StandardCharsets.UTF_8);
                System.out.println("...complete.\033[0m");
                System.out.println(response);
            }
            finally {
                span.end();
            }
            TimeUnit.SECONDS.sleep(3);
        }
    }

    private HttpService buildHelloHandler() {
        return (ctx, req) -> {
            Span span = spanBuilder("GET /hello")
                    // Not otel semconv, intentional for demonstration!
                    .setAttribute("httpRequestPath", req.path())
                    .setAttribute("requestUrl", req.uri().toString())
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                return HttpResponse.of(calculateResponse());
            } finally {
                span.end();
            }
        };
    }

    private String calculateResponse() {
        Span span = spanBuilder("calculateResponse").startSpan();
        try (Scope scope = span.makeCurrent()) {
            return "This span was created the hard way!\n";
        } finally {
            span.end();
        }
    }

    private SpanBuilder spanBuilder(String spanName) {
        Tracer tracer = otel.getTracer("helloHandler");
        return tracer.spanBuilder(spanName);
    }
}