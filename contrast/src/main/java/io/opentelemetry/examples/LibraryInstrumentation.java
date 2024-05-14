package io.opentelemetry.examples;

import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerPort;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.armeria.v1_3.ArmeriaTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class LibraryInstrumentation {

    public static final int PORT = 8123;
    private final OpenTelemetry otel;
    private final ArmeriaTelemetry armeriaTelemetry;

    public LibraryInstrumentation(OpenTelemetry otel, ArmeriaTelemetry armeriaTelemetry) {
        this.otel = otel;
        this.armeriaTelemetry = armeriaTelemetry;
    }

    public static void main(String[] args) throws Exception {
        // This would normally be done externally, via env vars etc.
        System.setProperty("otel.service.name", "library.instrumentation");
        OpenTelemetry sdk = AutoConfiguredOpenTelemetrySdk.initialize()
                .getOpenTelemetrySdk();
        ArmeriaTelemetry armeriaTelemetry = ArmeriaTelemetry.builder(sdk)
//                .addAttributeExtractor()
//                .addClientAttributeExtractor()
//                .setStatusExtractor()
                .build();
        new LibraryInstrumentation(sdk, armeriaTelemetry).runForever();
    }

    private void runForever() throws Exception {
        Server.builder()
                .port(new ServerPort(PORT, SessionProtocol.HTTP))
                .service("/libraryInstrumentation", buildInstrumentedHelloHandler())
                .build()
                .start();
        System.out.println("\n\033[0;36m*** LIBRARY INSTRUMENTATION example started on port " + PORT + " ***\033[0m");
        runClientForever();
    }

    private void runClientForever() throws Exception {
        WebClient webClient = WebClient.builder("http://localhost:" + PORT)
                .decorator(armeriaTelemetry.newClientDecorator())
                .build();
        while (true) {
            System.out.print("\033[0;33mDoing a request from client to server..");
            String response = webClient.get("/libraryInstrumentation").aggregate().join()
                    .content()
                    .toString(StandardCharsets.UTF_8);
            System.out.println(".complete.\033[0m");
            System.out.println(response);
            TimeUnit.SECONDS.sleep(3);
        }
    }

    private HttpService buildInstrumentedHelloHandler() {
        return armeriaTelemetry.newServiceDecorator().apply(buildHelloHandler());
    }

    private HttpService buildHelloHandler() {
        return (ctx, req) -> {
            Span.current().setAttribute("my.custom.attr", "anything");
            return HttpResponse.of(calculateResponse());
        };
    }

    private String calculateResponse() {
        // This is deep business logic or database calls that Ameria is unaware of, so
        // we keep it manually instrumented.
        Tracer tracer = otel.getTracer("helloHandler");
        Span span = tracer.spanBuilder("calculateResponse").startSpan();
        try (Scope scope = span.makeCurrent()) {
            return "This span was created the hard way!\n";
        } finally {
            span.end();
        }
    }

}