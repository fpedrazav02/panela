plugins {
    application
    id("org.graalvm.buildtools.native") version "0.10.2"
}

group = "io.github.fpedrazav02"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.7.7")
    implementation("org.luaj:luaj-jse:3.0.1")
}

application {
    mainClass.set("io.github.fpedrazav02.panela.Panela")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("panela")
            mainClass.set("io.github.fpedrazav02.panela.Panela")
            buildArgs.addAll(listOf(
                "-H:IncludeResources=templates/.*",
                "-H:-CheckToolchain",
                "--no-fallback"
            ))
        }
    }
}
