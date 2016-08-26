# Kafka Message Core Maven Repo
Usage: 
```
First add maven repo to your repository list:
<repositories>
    <repository>
        <id>kafka_message_core-mvn-repo</id>
        <url>https://raw.github.com/iswunistuttgart/kafka_message_core/mvn-repo/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>

Adding dependency:
<dependencies>
    <dependency>
        <groupId>de.unistuttgart.isw.serviceorchestration</groupId>
        <artifactId>kafka-message-core</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## Config
Set Env variable key: bootstrap.servers val:*yourhostip* to use this libary
