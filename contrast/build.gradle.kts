
plugins {
    id("java")
    id("application")
}

val runManual by tasks.registering(JavaExec::class) {
    dependsOn( "classes")
    mainClass = "io.opentelemetry.examples.FullyManual"
    classpath = sourceSets.main.get().runtimeClasspath
}

val libraryInstrumentation by tasks.registering(JavaExec::class) {
    dependsOn( "classes")
    mainClass = "io.opentelemetry.examples.LibraryInstrumentation"
    classpath = sourceSets.main.get().runtimeClasspath
}

val javaAgent by tasks.registering(JavaExec::class) {
    dependsOn( "classes")
    mainClass = "io.opentelemetry.examples.JavaAgentAutoInstrumentation"
    classpath = sourceSets.main.get().runtimeClasspath
    jvmArgs = listOf("-javaagent:opentelemetry-javaagent-2.3.0.jar")
    systemProperty("otel.service.name", "java.agent")
}

dependencies {
    implementation("com.linecorp.armeria:armeria:1.28.4")
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.opentelemetry.instrumentation:opentelemetry-armeria-1.3:2.3.0-alpha")
}