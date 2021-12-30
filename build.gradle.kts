repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.graalvm.buildtools.native") version "0.9.9"
    application
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.0.3")
    testImplementation("io.kotest:kotest-property:5.0.3")
}

graalvmNative { 
    binaries {
        named("main") {
            imageName.set("vm8")
            mainClass.set("vm8.MainKt")
        }
    }
}

application {
    mainClass.set("vm8.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
