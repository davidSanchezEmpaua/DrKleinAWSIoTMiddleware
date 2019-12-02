repositories {
    mavenCentral()
}

plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
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
