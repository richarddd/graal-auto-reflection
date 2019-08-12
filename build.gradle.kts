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

val graalVersion = "19.1.1"
val classGraphVersion = "4.8.43"

dependencies {

    implementation("org.graalvm.sdk:graal-sdk:$graalVersion")
    implementation("com.oracle.substratevm:svm:$graalVersion")
    implementation("io.github.classgraph:classgraph:$classGraphVersion")

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}