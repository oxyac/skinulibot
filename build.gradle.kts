plugins {
    java
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
}

group = "dev.oxyac"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation:2.7.5")
    implementation("org.telegram:telegrambots-client:7.5.0")
    implementation("org.telegram:telegrambots-springboot-webhook-starter:7.5.0")
    implementation("org.telegram:telegrambots-meta:7.4.2")
//    implementation("org.telegram:telegrambots-springboot-longpolling-starter:7.5.0")
    implementation("org.jsoup:jsoup:1.15.3")
    compileOnly("org.projectlombok:lombok")
    compileOnly("javax.transaction:transaction-api:1.1-rev-1")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2:2.1.214")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
