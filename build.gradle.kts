plugins {
    `java-library`
    maven
    idea
}

group = "se.davison.graal.autoreflection"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val graalVersion = "19.3.0"
val classGraphVersion = "4.8.43"

dependencies {

    api("org.graalvm.sdk:graal-sdk:$graalVersion")
    api("org.graalvm.nativeimage:svm:$graalVersion")
    api("io.github.classgraph:classgraph:$classGraphVersion")

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}