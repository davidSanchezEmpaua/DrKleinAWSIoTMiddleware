repositories {
    mavenCentral()
}

plugins {
    java
    application
}

version = "1.0-SNAPSHOT"

application {
    mainClassName = "de.drklein.awsiot.middleware.AWSIoT2Salesforce"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compile("com.amazonaws:aws-java-sdk-secretsmanager:1.11.316")
    compile("com.amazonaws:aws-iot-device-sdk-java:1.3.4")
    compile("commons-codec:commons-codec:1.13")
    compile("commons-logging:commons-logging:1.2")
    compile("org.apache.httpcomponents:httpclient:4.5.10")
    compile("org.apache.httpcomponents:httpclient-cache:4.5.10")
    compile("org.apache.httpcomponents:httpcore:4.4.12")
    compile("com.fasterxml.jackson.core:jackson-core:2.10.1")
    compile("com.fasterxml.jackson.core:jackson-databind:2.10.1")
    compile("org.json:json:20190722")
}

tasks {
    wrapper {
        gradleVersion = "5.6.2"
        distributionType = Wrapper.DistributionType.ALL
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClassName
    }

    from(configurations.runtime.get().map {if (it.isDirectory) it else zipTree(it)}) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
}
