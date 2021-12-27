repositories {
    mavenCentral()
}

plugins {
  kotlin("jvm") version "1.6.10"
  id("org.graalvm.buildtools.native") version "0.9.9"
}

graalvmNative { 
  binaries {
    named("main") {
      imageName.set("vm8")
      mainClass.set("vm8.MainKt")
    }
  }
}