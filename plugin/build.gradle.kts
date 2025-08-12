import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.plugin.publish)
}


group = "com.wizy"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}


dependencies {
    implementation(libs.bundles.core.deps)
}


gradlePlugin {
    website.set("https://github.com/JustWizy/avro-gradle-plugin")
    vcsUrl.set("https://github.com/JustWizy/avro-gradle-plugin.git")

    plugins {
        create("avroIdlPlugin") {
            id = "com.wizy.avro-idl"
            implementationClass = "com.wizy.avro.AvroIdlToSchemaPlugin"
            displayName = "Avro IDL to Schema Plugin || Protocol"
            description = "Converts .avdl files to .avsc/pr with incremental build and caching"
            tags.set(listOf("avro", "idl", "schema", "protocol", "kotlin", "java"))
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}


kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}
