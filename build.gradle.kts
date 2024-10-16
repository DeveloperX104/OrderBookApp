plugins {
    kotlin("jvm") version "1.8.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-core:4.3.0")
    implementation("io.vertx:vertx-web:4.3.0")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.hibernate.validator:hibernate-validator:6.1.5.Final")
    implementation("org.glassfish:jakarta.el:3.0.3") 
    implementation("com.typesafe:config:1.4.1")
    
    // Jackson dependencies
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0") 
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.0") 
    implementation("com.fasterxml.jackson.core:jackson-core:2.14.0") 
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0") 

    // Vert.x Web Client
    implementation("io.vertx:vertx-web-client:4.3.0")

    // Test Dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.1")
    testImplementation("io.vertx:vertx-junit5:4.3.0") 
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.0") 
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.example.orderbook.app.MainVerticle")
}
