
# Creating reactive Java microservices

## What you'll learn

You will learn how to build reactive microservices that can send requests to other microservices, and asynchronously receive and process the responses. You will use an external messaging system to handle the asynchronous messages that are sent and received between the microservices as streams of events. MicroProfile Reactive Messaging makes it easy to write and configure your application to send, receive, and process the events efficiently.

### Asynchronous messaging between microservices

Asynchronous communication between microservices can be used to build reactive and responsive applications. By decoupling the requests sent by a microservice from the responses that it receives, the microservice is not blocked from performing other tasks while waiting for the requested data to become available. Imagine asynchronous communication as a restaurant. A waiter might come to your table and take your order. While you are waiting for your food to be prepared, that waiter serves other tables and takes their orders too. When your food is ready, the waiter brings your food to the table and then continues to serve the other tables. If the waiter were to operate synchronously, they must take your order and then wait until they deliver your food before serving any other tables. In microservices, a request call from a REST client to another microservice can be time-consuming because the network might be slow, or the other service might be overwhelmed with requests and can't respond quickly. But in an asynchronous system, the microservice sends a request to another microservice and continues to send other calls and to receive and process other responses until it receives a response to the original request.

### What is MicroProfile Reactive Messaging?

MicroProfile Reactive Messaging provides an easy way to asynchronously send, receive, and process messages that are received as continuous streams of events. You simply annotate application beans' methods and Open Liberty converts the annotated methods to reactive streams-compatible publishers, subscribers, and processors and connects them up to each other. MicroProfile Reactive Messaging provides a Connector API so that your methods can be connected to external messaging systems that produce and consume the streams of events, such as [Apache Kafka](https://kafka.apache.org/).

The application in this guide consists of two microservices, **system** and **inventory**. Every 15 seconds, the **system** microservice calculates and publishes an event that contains its current average system load. The **inventory** microservice subscribes to that information so that it can keep an updated list of all the systems and their current system loads. The current inventory of systems can be accessed via the **/systems** REST endpoint. You'll create the **system** and **inventory** microservices using MicroProfile Reactive Messaging.

## Getting Started

If a terminal window does not open navigate:

> Terminal -> New Terminal

Check you are in the **home/project** folder:

```
pwd
```
{: codeblock}

The fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-microprofile-reactive-messaging.git
cd guide-microprofile-reactive-messaging
```
{: codeblock}

The **start** directory contains the starting project that you will build upon.


## Creating the producer in the system microservice

Navigate to the **start** directory to begin. 

```
cd start
```
{: codeblock}

The **system** microservice is the producer of the messages that are published to the Kafka messaging system as a stream of events. Every 15 seconds, the **system** microservice publishes an event that contains its calculation of the average system load (its CPU usage) for the last minute.

Create the **SystemService** class.

```
touch system/src/main/java/io/openliberty/guides/system/SystemService.java
```
{: codeblock}


> [File -> Open]guide-microprofile-reactive-messaging/start/system/src/main/java/io/openliberty/guides/system/SystemService.java

```
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
{: codeblock}

The **SystemService** class contains a **Publisher** method that is called **Flowable.interval()** method from **rxJava** is used to set the frequency of how often the system service publishes the calculation to the event stream.

The messages are transported between the service and the Kafka messaging system through a channel called **systemLoadTopic**.

## Creating the consumer in the inventory microservice

The **inventory** microservice records in its inventory the average system load information that it received from potentially multiple instances of the **system** service.

Create the **InventoryResource** class.

```
touch inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java
```
{: codeblock}


> [File -> Open]guide-microprofile-reactive-messaging/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java

```

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
    @Path("/systems/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystem(@PathParam("hostname") String hostname) {
        Optional<Properties> system = manager.getSystem(hostname);
        if (system.isPresent()) {
            return Response
                    .status(Response.Status.OK)
                    .entity(system)
                    .build();
        }
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity("hostname does not exist.")
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
    public void updateStatus(SystemLoad sl)  {
        String hostname = sl.hostname;
        if (manager.getSystem(hostname).isPresent()) {
            manager.updateCpuStatus(hostname, sl.loadAverage);
            logger.info("Host " + hostname + " was updated: " + sl);
        } else {
            manager.addSystem(hostname, sl.loadAverage);
            logger.info("Host " + hostname + " was added: " + sl);
        }
    }
}
```
{: codeblock}

The **inventory** microservice receives the message from the **system** microservice over the **/inventory** endpoint.

The **InventoryResource** class contains a method called **systemLoadTopic**.

## Configuring the MicroProfile Reactive Messaging connectors for Kafka

The **system** and **inventory** services exchange messages with the external messaging system through a channel. The MicroProfile Reactive Messaging Connector API makes it easy to connect each service to the channel. You just need to add configuration keys in a properties file for each of the services. These configuration keys define properties such as the name of the channel and the topic in the Kafka messaging system. Open Liberty includes theÂ **liberty-kafka** connector for sending and receiving messages from Apache Kafka.

The system and inventory microservices each have a MicroProfile Config properties file to define the properties of their outgoing and incoming streams.

Create the system/microprofile-config.properties file.

```
touch system/src/main/resources/META-INF/microprofile-config.properties
```
{: codeblock}


> [File -> Open]guide-microprofile-reactive-messaging/start/system/src/main/resources/META-INF/microprofile-config.properties

```
mp.messaging.connector.liberty-kafka.bootstrap.servers=localhost:9093

mp.messaging.outgoing.systemLoad.connector=liberty-kafka
mp.messaging.outgoing.systemLoad.topic=systemLoadTopic
mp.messaging.outgoing.systemLoad.key.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.outgoing.systemLoad.value.serializer=io.openliberty.guides.models.SystemLoad$SystemLoadSerializer
```
{: codeblock}


The **value.serializer**.

The **inventory** microservice uses a similar **microprofile-config.properties** configuration to define its required incoming stream.

Create the inventory/microprofile-config.properties file.

```
touch inventory/src/main/resources/META-INF/microprofile-config.properties
```
{: codeblock}


> [File -> Open]guide-microprofile-reactive-messaging/start/inventory/src/main/resources/META-INF/microprofile-config.properties

```
mp.messaging.connector.liberty-kafka.bootstrap.servers=localhost:9093

mp.messaging.incoming.systemLoad.connector=liberty-kafka
mp.messaging.incoming.systemLoad.topic=systemLoadTopic
mp.messaging.incoming.systemLoad.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.systemLoad.value.deserializer=io.openliberty.guides.models.SystemLoad$SystemLoadDeserializer
mp.messaging.incoming.systemLoad.group.id=system-load-status
```
{: codeblock}

## Configuring the server

To run the services, the Open Liberty server on which each service runs needs to be correctly configured. Relevant features, including the [MicroProfile Reactive Messaging feature](https://openliberty.io/docs/ref/feature/#mpReactiveMessaging-1.0.html), must be enabled for the **system** and **inventory** services.

Create the system/server.xml configuration file.
```
touch system/src/main/liberty/config/server.xml
```
{: codeblock}


> [File -> Open]guide-microprofile-reactive-messaging/start/system/src/main/liberty/config/server.xml

```
<server description="System Service">

  <featureManager>
    <feature>cdi-2.0</feature>
    <feature>concurrent-1.0</feature>
    <feature>jsonb-1.0</feature>
    <feature>mpHealth-2.2</feature>
    <feature>mpConfig-1.4</feature>
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
{: codeblock}

The **server.xml** file is already configured for the **inventory** microservice.

## Building and running the application

Build the **system** and **inventory** microservices using Maven and then run them in Docker containers.

Create the Maven configuration file.

```
touch system/pom.xml
```
{: codeblock}


> [File -> Open]guide-microprofile-reactive-messaging/start/system/pom.xml

```
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
        <!-- Liberty configuration -->
        <liberty.var.default.http.port>9083</liberty.var.default.http.port>
        <liberty.var.default.https.port>9446</liberty.var.default.https.port>
    </properties>

    <dependencies>
        <!-- Provided dependencies -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>8.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>3.3</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        <!-- tag::reactiveMessaging[] -->
        <dependency>
            <groupId>org.eclipse.microprofile.reactive.messaging</groupId>
            <artifactId>microprofile-reactive-messaging-api</artifactId>
            <version>1.0</version>
            <scope>provided</scope>
        </dependency>
        <!-- end::reactiveMessaging[] -->
        <!-- Required dependencies -->
        <dependency>
            <groupId>io.openliberty.guides</groupId>
            <artifactId>models</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <!-- tag::kafka[] -->
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>2.4.0</version>
        </dependency>
        <!-- end::kafka[] -->
        <!-- tag::rxjava[] -->
        <dependency>
            <groupId>io.reactivex.rxjava3</groupId>
            <artifactId>rxjava</artifactId>
            <version>3.0.0</version>
        </dependency>
        <!-- end::rxjava[] -->
        <!-- For tests -->
        <dependency>
            <groupId>org.microshed</groupId>
            <artifactId>microshed-testing-liberty</artifactId>
            <version>0.9</version>
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
            <version>5.6.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.2.3</version>
                <configuration>
                    <packagingExcludes>pom.xml</packagingExcludes>
                </configuration>
            </plugin>

            <!-- Liberty plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.2.1</version>
            </plugin>

            <!-- Plugin to run unit tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.2</version>
            </plugin>

            <!-- Plugin to run integration tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>2.22.2</version>
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
{: codeblock}

The **rxjava** dependencies.

The **rxjava** dependency is used for creating events at regular intervals.

Start your Docker environment. Dockerfiles are provided for you to use.

To build the application, run the Maven **install** and **package** goals from the command line in the **start** directory:

```
mvn -pl models install
mvn package
```
{: codeblock}


Run the following command to download or update to the latest **openliberty/open-liberty:kernel-java8-openj9-ubi** Docker image:

```
docker pull openliberty/open-liberty:kernel-java8-openj9-ubi
```
{: codeblock}



Run the following commands to containerize the microservices:

```
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
```
{: codeblock}

Next, use the provided script to start the application in Docker containers. The script creates a network for the containers to communicate with each other. It also creates containers for Kafka, Zookeeper, and the microservices in the project. For simplicity, the script starts one instance of the system service.



```
./scripts/startContainers.sh
```
{: codeblock}

## Testing the application

After the application is up and running, you can access the application by making a GET request to the **/systems** endpoint of the **inventory** service. 

Go to the **inventory/systems**  URL to access the inventory microservice: 

```
curl http://localhost:9085/inventory/systems
```
{: codeblock}

You see the CPU **systemLoad** property for all the systems:

```
{
   "hostname":"30bec2b63a96",
   "systemLoad":2.25927734375
}
```

You can revisit the http://localhost:9085/inventory/systems URL after a while, and you will notice the CPU **systemLoad** property for the systems changed.

You can use the **\http://localhost:9085/inventory/systems/{hostname}** URL to see the CPU **systemLoad** property for one particular system.

```
curl http://localhost:9085/inventory/systems/{hostname}
```
{: codeblock}

In the following example, the **30bec2b63a96** value is the **hostname**. If you go to the **\http://localhost:9085/inventory/systems/30bec2b63a96** URL, you can see the CPU **systemLoad** property only for the **30bec2b63a96** **hostname**:

```
{
   "hostname":"30bec2b63a96",
   "systemLoad":2.25927734375
}
```

## Tearing down the environment

Run the following script to stop the application:


```
./scripts/stopContainers.sh
```
{: codeblock}

# Summary

## Clean up your environment

Delete the **guide-microprofile-reactive-messaging** project by navigating to the **/home/project/** directory

```
cd ../..
rm -r -f guide-microprofile-reactive-messaging
rmdir guide-microprofile-reactive-messaging
```
{: codeblock}


## Great work! You're done!

You just developed a reactive Java application using MicroProfile Reactive Messaging, Open Liberty, and Kakfa.
