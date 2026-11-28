plugins {
    id("java")
    id("com.gradleup.shadow").version("9.6.1")
}

group = "net.approachcircle"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
}

tasks.shadowJar {
    destinationDirectory.set(File("${rootDir}/dev_server/plugins"))
    from(rootDir) {
        include("*.yml", "*.json", "LICENSE") // blehhhhhhh :p
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("io.sentry:sentry:8.66.0")
    implementation("com.datadoghq:java-dogstatsd-client:4.4.6")
    implementation("com.fasterxml.jackson.core:jackson-core:2.23.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.23.0")
    compileOnly("io.papermc.paper:paper-api:26.3.build.166-beta")
}

tasks.test {
    useJUnitPlatform()
}