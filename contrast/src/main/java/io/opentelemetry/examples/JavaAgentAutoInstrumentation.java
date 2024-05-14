package io.opentelemetry.examples;

import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerPort;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class JavaAgentAutoInstrumentation {

    public static final int PORT = 8123;

    public static void main(String[] args) throws Exception {
        // This will not work, because the agent is started BEFORE the main method is invoked
        // See build.gradke.kts to see how it's set
        System.setProperty("otel.service.name", "with.javaagent");
        new JavaAgentAutoInstrumentation().runForever();
    }

    private void runForever() throws Exception {
        Server.builder()
                .port(new ServerPort(PORT, SessionProtocol.HTTP))
                .service("/javaAgent", buildHelloHandler())
                .build()
                .start();
        System.out.println("\n\033[0;36m*** JAVA AGENT example started on port " + PORT + " ***\033[0m");
        runClientForever();
    }

    private void runClientForever() throws Exception {
        WebClient webClient = WebClient.of("http://localhost:" + PORT);
        while (true) {
            System.out.print("\033[0;33mDoing a request from client to server..");
            String response = webClient.get("/javaAgent").aggregate().join()
                    .content()
                    .toString(StandardCharsets.UTF_8);
            System.out.println(".complete.\033[0m");
            System.out.println(response);
            TimeUnit.SECONDS.sleep(3);
        }
    }

    private HttpService buildHelloHandler() {
        return (ctx, req) -> {
            // Just get the current span context and add a custom attribute
            Span.current().setAttribute("my.custom.attr", "anything");
            return HttpResponse.of(calculateResponse());
        };
    }

    private String calculateResponse() {
        // Just to show how application code can re-use the SDK created by the javaagent
        Tracer tracer = GlobalOpenTelemetry.get().getTracer("helloHandler");
        Span span = tracer.spanBuilder("calculateResponse").startSpan();
        try (Scope scope = span.makeCurrent()) {
            return "This span was created the hard way!\n";
        } finally {
            span.end();
        }
    }

}