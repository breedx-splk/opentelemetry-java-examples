
# About

Examples for comparing and contrasting

* fully manual instrumentation
* leveraging existing library instrumentation
* full auto-instrumentation

# Using

In one terminal, run jaeger via docker:
```bash
docker run -it --rm \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  jaegertracing/all-in-one:1.6
```

The UI will be at http://localhost:16686/


In other terminals:
```bash
../gradlew runManual
../gradlew libraryInstrumentation
```

In yet another terminal, send some data:
```bash
curl http://localhost:8123/manualTracing          # manual tracing 
curl http://localhost:8123/libraryInstrumentation # library instrumentation 
```

## Issues/questions

* Manual example doesn't do propagation. Guess that needs to be manual as well!