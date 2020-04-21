# Creating reactive microservices using MicroProfile Reactive Messaging

Learn how to use MicroProfile Reactive Messaging to implement a Java application with a reactive architecture.

### What you’ll learn

You will learn how to build reactive microservices using MicroProfile Reactive Messaging. You’ll also learn how to send messages between these microservices using Apache Kafka.

### What is asynchronous programming?
Imagine asynchronous programming as a restaurant. After you’re seated, a waitstaff takes your order. Then, you must wait a few minutes for your food to be prepared. While your food is being prepared, your waitstaff may take more orders or serve other tables. After your food is ready, your waitstaff brings out the food to your table. However, in a synchronous model, the waitstaff must wait for your food to be prepared before serving any other customers. This method blocks other customers from placing orders or receiving their food.

You can perform lengthy operations, such as input/output (I/O), without blocking with asynchronous methods. The I/O operation can occur in the background and a callback notifies the caller to continue its computation when the original request is complete. As a result, the original thread frees up so it can handle other work rather than wait for the I/O to complete. Revisiting the restaurant analogy, food is prepared asynchronously in the kitchen and your waitstaff is freed up to attend to other tables.

In the context of REST clients, HTTP request calls can be time consuming. The network might be slow, or maybe the upstream service is overwhelmed and can’t respond quickly. These lengthy operations can block the execution of your thread when it’s in use and prevent other work from being completed.

### What is reactive programming?
Reactive programming is a method for writing code based on react‐ing to changes. In technical terms, this is a paradigm in which declarative code is used to construct asynchronous processing pipelines. Translated, this is essentially the same process our minds perform when we try to multitask. Rather than true parallel tasking, we actually switch tasks and split those tasks during their duration. This enables us to use our time efficiently instead of having to wait for the previous task to complete. This is exactly what reactive programming was created to do. It is an event-based model in which data is pushed to a consumer as it becomes available, turning it into an asynchronous sequence of events. Reactive programming is a useful implementation technique for managing internal logic and data flow transformation locally within components (intercomponents), through asynchronous and non-blocking execution.

### What is MicroProfile Reactive Messaging?
The degree of association between the microservices within a system is called coupling. In order to ensure all microservices within the system are reactive, they must be decoupled from each other. This is important, because if services are not decoupled it can result in a fragile framework, resulting in potential flexibility, scaling and resilience problems. Decoupling of services can be accomplished by means of asynchronous communication. This is where MicroProfile Reactive Messaging can help.

### What is Kafka?
Apache Kafka is a stream-processing platform that manages communication in distributed systems. Communication is message-oriented, and follows the publish-subscribe model. Kafka allows for real-time stream processing and distributed, replicated storage of streams and messages. A Kafka producer is a client or a program, which produces the message and pushes them to a topic. A Kafka consumer is a client or a program, which consumes the published messages from a topic.

The application that you will be working with consists of two microservices, **system** and **inventory**. The system microservice sends the average **system** load data to the **inventory** microservice every 15 seconds. The **inventory** microservice keeps an updated list of all the **system** hostnames and their CPU data.

You’ll update the **system** and **inventory** microservices to use MicroProfile Reactive Messaging for message passing. These microservices run on Open Liberty.

### Getting started

The fastest way to work through this guide is to clone the Git repository and use the project that's inside:

`git clone https://github.com/openliberty/guide-microprofile-reactive-messaging.git`

Navigate into the guide

`cd guide-microprofile-reactive-messaging`

The **start** directory contains the starting project that you will build upon.

`cd start`

# MicroProfile Reactive Messaging Key Concepts

MicroProfile Reactive Messaging recommends a design to build reactive applications based on the following main concepts:

**Channel**

A **channel** is a bridge for transporting messages between different parts of the reactive system. This can be between the service and a messaging-broker, or between two components within the same service.

**@Outgoing**

**@Outgoing** is an annotation indicating that the method feeds a channel. The name of the channel is given as attribute. For example, **@Outgoing**("systemLoad").

**@Incoming**

**@Incoming** is an annotation indicating that the method consumes a channel. The name of the channel is given as attribute. For example, **@Incoming**("systemLoad").

**Connector**

MicroProfile Reactive Messaging uses connectors to attach one end of a channel to another messaging technology and are configured using MicroProfile Config. Open Liberty includes the liberty-kafka connector for sending and receiving messages from Apache Kafka.

# Building the Producer in the system microservice

The **system** microservice uses the MicroProfile Reactive Messaging to send CPU usage messages to the **inventory** microservice over Kafka.

Create the in the **SystemService** class.

Navigate to the **system** directory
> `cd system/src/main/java/io/openliberty/guides/system/`

Create the **SystemService.java**

`touch SystemService.java`

Open **SystemService.java** by navigating to 

>[File -> Open]draft-guide-microprofile-reactive-messaging/start/system/src/main/java/io/openliberty/guides/system/SystemService.java

Double click on SystemService.java to open and add the Java code.

```java
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.Publisher;

import io.openliberty.guides.models.SystemLoad;
import io.reactivex.rxjava3.core.Flowable;

@ApplicationScoped
public class SystemService {

    private static final OperatingSystemMXBean osMean =
            ManagementFactory.getOperatingSystemMXBean();
    private static String hostname = null;

    private static String getHostname() {
        if (hostname == null) {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                return System.getenv("HOSTNAME");
            }
        }
        return hostname;
    }

    @Outgoing("systemLoad")
    public Publisher<SystemLoad> sendSystemLoad() {
        return Flowable.interval(15, TimeUnit.SECONDS)
                .map((interval -> new SystemLoad(getHostname(),
                        new Double(osMean.getSystemLoadAverage()))));
    }

}
```

The **system** microservice calculates the average system load for the last minute every fifteen seconds. The average system load for a system is calculated by counting the number of runnable items that are queued to and run on the available processors, and then this count is averaged over a period of time.

The **sendSystemLoad()** method creates CPU system load statistics for a host system every fifteen seconds. The **rxJava** library is used to generate those CPU system load message events that are sent to Kafka every fifteen seconds. The **Flowable.interval()** from **rxJava** is used to create a **Publisher**. The **Publisher** is returned from the **@Outgoing("systemLoad")** channel, which will be configured in the **microprofile-config.properties** **systemLoad** stream in the below section. MicroProfile Reactive Messaging takes care of assigning the **Publisher** to the channel.

Change directories back to the top level **system** folder

`cd ../../../../../../..`

Create the **pom.xml** file

`touch pom.xml`

Open the **pom.xml** file 

>[File -> Open] guide-microprofile-reactive-messaging/start/system/pom.xml

Add the **maven** dependencies, **properties**, **plugins**, **packaging method**, war, **name** system, and the **version** 1.0-SNAPSHOT.

```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>system</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- Plugins -->
        <version.liberty-maven-plugin>3.2</version.liberty-maven-plugin>
        <version.maven-war-plugin>3.2.3</version.maven-war-plugin>
        <version.maven-surefire-plugin>2.22.2</version.maven-surefire-plugin>
        <version.maven-failsafe-plugin>2.22.2</version.maven-failsafe-plugin>
        <!-- Liberty configuration -->
        <liberty.var.default.http.port>9083</liberty.var.default.http.port>
        <liberty.var.default.https.port>9446</liberty.var.default.https.port>
    </properties>

    <dependencies>
        <!-- Provided dependencies -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-web-api</artifactId>
            <version>8.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>3.2</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile.reactive.messaging</groupId>
            <artifactId>microprofile-reactive-messaging-api</artifactId>
            <version>1.0</version>
            <scope>provided</scope>
        </dependency>
        <!-- Required dependencies -->
        <dependency>
            <groupId>io.openliberty.guides</groupId>
            <artifactId>models</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>2.4.0</version>
        </dependency>
        <dependency>
            <groupId>io.reactivex.rxjava3</groupId>
            <artifactId>rxjava</artifactId>
            <version>3.0.0</version>
        </dependency>
        <!-- For tests -->
        <dependency>
            <groupId>org.microshed</groupId>
            <artifactId>microshed-testing-liberty</artifactId>
            <version>0.8</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>kafka</artifactId>
            <version>1.12.5</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.5.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.30</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>${version.maven-war-plugin}</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                    <packagingExcludes>pom.xml</packagingExcludes>
                </configuration>
            </plugin>

            <!-- Liberty plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>${version.liberty-maven-plugin}</version>
            </plugin>

            <!-- Plugin to run unit tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>${version.maven-surefire-plugin}</version>
            </plugin>

            <!-- Plugin to run integration tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>${version.maven-failsafe-plugin}</version>
                <executions>
                    <execution>
                        <id>integration-test</id>
                        <goals>
                            <goal>integration-test</goal>
                        </goals>
                        <configuration>
                            <trimStackTrace>false</trimStackTrace>
                        </configuration>
                    </execution>
                    <execution>
                        <id>verify</id>
                        <goals>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

In order to develop the MicroProfile Reactive Messaging application using Maven, add the **microprofile-reactive-messaging-api**, **kafka-clients**, and **rxjava** dependencies to the **pom.xml**.

**microprofile-reactive-messaging-api** dependency is needed to enable the use of MicroProfile Reactive Messaging API. **kafka-clients** dependency is added since the application needs a Kafka client to connect to the Kafka broker. **rxjava** dependency is used for creating events at regular intervals.

# Building the Consumer in the inventory microservice

The **inventory** microservice consumes the events produced by the system microservice and stores the information about the CPU usage that runs on different systems.

Navigate to the bottom **inventory** directory

`cd ../inventory/src/main/java/io/openliberty/guides/inventory/`

Create **InventoryResource.java**

`touch InventoryResource.java`

[File -> Open]inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java

Add the Java code:

```java
package io.openliberty.guides.inventory;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.openliberty.guides.models.SystemLoad;

@ApplicationScoped
@Path("/inventory")
public class InventoryResource {

    private static Logger logger = Logger.getLogger(InventoryResource.class.getName());

    @Inject
    private InventoryManager manager;

    @GET
    @Path("/systems")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystems() {
        List<Properties> systems = manager.getSystems()
                .values()
                .stream()
                .collect(Collectors.toList());
        return Response
                .status(Response.Status.OK)
                .entity(systems)
                .build();
    }

    @GET
    @Path("/system/{hostId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystem(@PathParam("hostId") String hostId) {
        Optional<Properties> system = manager.getSystem(hostId);
        if (system.isPresent()) {
            return Response
                    .status(Response.Status.OK)
                    .entity(system)
                    .build();
        }
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity("hostId does not exist.")
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetSystems() {
        manager.resetSystems();
        return Response
                .status(Response.Status.OK)
                .build();
    }

    @Incoming("systemLoad")
    public void updateStatus(SystemLoad s)  {
        String hostId = s.hostId;
        if (manager.getSystem(hostId).isPresent()) {
            manager.updateCpuStatus(hostId, s.loadAverage);
            logger.info("Host " + hostId + " was updated: " + s);
        } else {
            manager.addSystem(hostId, s.loadAverage);
            logger.info("Host " + hostId + " was added: " + s);
        }
    }
}
```

The **inventory** microservice receives the message from the **system** microservice over the **@Incoming("systemLoad")** channel with its properties defined in the microprofile-config.properties. It’s also a RESTful service that is served at the **/inventory** endpoint.

# Configuring the MicroProfile Reactive Messaging connectors

MicroProfile Reactive Messaging uses connectors to send and receive messages from different systems. In order to configure the MicroProfile Reactive Messaging connectors, you need to format the configuration keys as follows :

1. mp.messaging.connector.liberty-kafka.<property name>: Applies to all channels using the liberty-kafka connector

2. mp.messaging.[incoming|outgoing].<channel name>.<property name> : Applies to a particular channel

3. Each channel which is to be connected using a connector must have the connector property defined to say which **connector** to use

Both the **system** and **inventory** microservices use connectors to connect certain channels to Apache Kafka and these connectors are configured by setting properties using MicroProfile Config. This is done by setting the required properties inside the **microprofile-config.properties** file included in each microservice.


Navigate to **META-INF** directory

`cd ../../../../../../../../system/src/main/resources/META-INF/`

Create the **system** **microprofile-config.properties**

`touch microprofile-config.properties`

Open **microprofile-config.properties**

[File -> Open]system/src/main/resources/META-INF/microprofile-config.properties

Add the **configuration properties**

```
# Liberty Kafka connector
mp.messaging.connector.liberty-kafka.bootstrap.servers=localhost:9093

# systemLoad stream
mp.messaging.outgoing.systemLoad.connector=liberty-kafka
mp.messaging.outgoing.systemLoad.topic=systemLoadTopic
mp.messaging.outgoing.systemLoad.key.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.outgoing.systemLoad.value.serializer=io.openliberty.guides.models.SystemLoad$SystemLoadSerializer
```

The **mp.messaging.connector.liberty-kafka.bootstrap.servers** property configures the hostname and port of the Kafka server for all channels which use the Kafka connector. 

The **system** microservice uses an outgoing connector to send messages from the **systemLoad** channel to the **systemLoadTopic** topic in the Kafka messaging-broker, so that the inventory microservices can consume the messages. The **key.serializer** and **value.serializer** properties characterize how to serialize the messages. The class **SystemLoadSerializer** implements the logic for turning a **SystemLoad** object into json and is configured as the **value.serializer**.

Navigate to the **inventory** directory to create the config properties for the **inventory**

`cd ../../../../../inventory/src/main/resources/META-INF/`

The **inventory** microservices uses a similar **microprofile-config.properties** configuration to define its required incoming stream.

Create the inventory **microprofile-config.properties** file.

`touch microprofile-config.properties`

Open **microprofile-config.properties**

[File -> Open]inventory/src/main/resources/META-INF/microprofile-config.properties

```
# Liberty Kafka connector
mp.messaging.connector.liberty-kafka.bootstrap.servers=localhost:9093

# systemLoad stream
mp.messaging.incoming.systemLoad.connector=liberty-kafka
mp.messaging.incoming.systemLoad.topic=systemLoadTopic
mp.messaging.incoming.systemLoad.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.systemLoad.value.deserializer=io.openliberty.guides.models.SystemLoad$SystemLoadDeserializer
mp.messaging.incoming.systemLoad.group.id=system-load-status
```

The **inventory** microservice uses an incoming connector to receive messages on the **systemLoad** channel which were sent by the **system*** microservice to the **systemLoadTopic** in the Kafka messaging-broker. 

Similarly the **key.deserializer** and **value.deserializer** properties define how to deserialize the messages. The class **SystemLoadDeserializer** implements the logic for turning json into a **SystemLoad** object and is configured as the **value.deserializer**. The **group.id** defines a unique name for the consumer group. Consumer group is a collection of consumers who share a common identifier for the group. This can also be seen as various machines ingesting from the Kafka topics. 

All these properties are required by the Apache Kafka Producer Configs and Apache Kafka Consumer Configs.

# Configuring the server

To use MicroProfile Reactive Messaging, you must enable the feature in the **server.xml** file for each service.

Create the **server.xml** file

Navigate back to **system** **config** directory to create the **server.xml**

` cd ../../../../../system/src/main/liberty/config/`

Open the **server.xml** file 

>[File -> Open] system/src/main/liberty/config/server.xml

Add the contents for the **server.xml** which contains the features and the **endpoint** configuring it to listen on **port 9083**

```xml
<server description="System Service">

  <featureManager>
    <feature>cdi-2.0</feature>
    <feature>concurrent-1.0</feature>
    <feature>jsonb-1.0</feature>
    <feature>mpHealth-2.1</feature>
    <feature>mpConfig-1.3</feature>
    <!-- tag::featureMP[] -->
    <feature>mpReactiveMessaging-1.0</feature>
    <!-- end::featureMP[] -->
  </featureManager>

  <variable name="default.http.port" defaultValue="9083"/>
  <variable name="default.https.port" defaultValue="9446"/>

  <httpEndpoint host="*" httpPort="${default.http.port}"
      httpsPort="${default.https.port}" id="defaultHttpEndpoint"/>

  <webApplication location="system.war" contextRoot="/"/>
</server>
```

The **inventory** microservice has **server.xml** already configured.

# Building the application

You will build and run the **system** and **inventory** microservices in Docker containers.

The **Dockerfiles** are already provided for use.

Navigate back to the **start** directory 

`cd ../../../../`

To build the application, run Maven install:


`mvn -pl models install`

Package the application 

`mvn package`

Update to the latest **open-liberty** Docker image.

`docker pull open-liberty`

### Containerize the microservices:

Build the **system** docker image

`docker build -t system:1.0-SNAPSHOT system/.`

Build the **inventory** docker image

docker build -t inventory:1.0-SNAPSHOT inventory/.

We have provided script to start the application in Docker containers. 
The script creates a network for the containers to communicate with each other. It also creates containers for **Kafka**, **Zookeeper**, and all of the microservices in the project.

Start the application 

`./scripts/startContainers.sh`

# Test the application

Once the application is up and running, you can access the application by making a **@GET** request to the **inventory** endpoint.

To access the **inventory** microservice, use the **inventory/systems** URL, and you see the CPU systemLoad property for all the systems.

`curl http://localhost:9085/inventory/systems`

The output should contain the **hostname** and **systemLoad**

```
{
   "hostname":"30bec2b63a96",
   "systemLoad":2.25927734375
}
```

Rerun the **/inventory/systems** URL after a while, and you will notice the **CPU systemLoad** property for all the systems have changed.

`curl http://localhost:9085/inventory/systems`

use the **hostId** URL to see the CPU systemLoad property for one particular system.

In this example the **hostId = 30bec2b63a96**

`curl @GET http://localhost:9085/inventory/system/`**hostId**

**Example** URL = curl http://localhost:9085/inventory/system/30bec2b63a96

### Tearing down the environment 

Finally, use the following script to stop the application:

`./scripts/stopContainers.sh`
