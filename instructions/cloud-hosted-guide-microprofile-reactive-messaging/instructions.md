---
markdown-version: v1
tool-type: theia
---
::page{title="Welcome to the Creating reactive Java microservices guide!"}

Learn how to write reactive Java microservices using MicroProfile Reactive Messaging.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You will learn how to build reactive microservices that can send requests to other microservices, and asynchronously receive and process the responses. You will use an external messaging system to handle the asynchronous messages that are sent and received between the microservices as streams of events. MicroProfile Reactive Messaging makes it easy to write and configure your application to send, receive, and process the events efficiently.

*Asynchronous messaging between microservices*

Asynchronous communication between microservices can be used to build reactive and responsive applications. By decoupling the requests sent by a microservice from the responses that it receives, the microservice is not blocked from performing other tasks while waiting for the requested data to become available. Imagine asynchronous communication as a restaurant. A waiter might come to your table and take your order. While you are waiting for your food to be prepared, that waiter serves other tables and takes their orders too. When your food is ready, the waiter brings your food to the table and then continues to serve the other tables. If the waiter were to operate synchronously, they must take your order and then wait until they deliver your food before serving any other tables. In microservices, a request call from a REST client to another microservice can be time-consuming because the network might be slow, or the other service might be overwhelmed with requests and can’t respond quickly. But in an asynchronous system, the microservice sends a request to another microservice and continues to send other calls and to receive and process other responses until it receives a response to the original request.

*What is MicroProfile Reactive Messaging?*

MicroProfile Reactive Messaging provides an easy way to asynchronously send, receive, and process messages that are received as continuous streams of events. You simply annotate application beans' methods and Open Liberty converts the annotated methods to reactive streams-compatible publishers, subscribers, and processors and connects them up to each other. MicroProfile Reactive Messaging provides a Connector API so that your methods can be connected to external messaging systems that produce and consume the streams of events, such as [Apache Kafka](https://kafka.apache.org/).

The application in this guide consists of two microservices, ***system*** and ***inventory***. Every 15 seconds, the ***system*** microservice calculates and publishes an event that contains its current average system load. The ***inventory*** microservice subscribes to that information so that it can keep an updated list of all the systems and their current system loads. The current inventory of systems can be accessed via the ***/systems*** REST endpoint. You'll create the ***system*** and ***inventory*** microservices using MicroProfile Reactive Messaging.

![Reactive system inventory](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-reactive-messaging/prod/assets/reactive-messaging-system-inventory.png)


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-reactive-messaging.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microprofile-reactive-messaging.git
cd guide-microprofile-reactive-messaging
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

::page{title="Creating the producer in the system microservice"}

Navigate to the ***start*** directory to begin. 
```bash
cd /home/project/guide-microprofile-reactive-messaging/start
```

The ***system*** microservice is the producer of the messages that are published to the Kafka messaging system as a stream of events. Every 15 seconds, the ***system*** microservice publishes an event that contains its calculation of the average system load (its CPU usage) for the last minute.

Create the ***SystemService*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-reactive-messaging/start/system/src/main/java/io/openliberty/guides/system/SystemService.java
```


> Then, to open the SystemService.java file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging/start/system/src/main/java/io/openliberty/guides/system/SystemService.java, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging/start/system/src/main/java/io/openliberty/guides/system/SystemService.java"}



```java
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.Publisher;

import io.openliberty.guides.models.SystemLoad;
import io.reactivex.rxjava3.core.Flowable;

@ApplicationScoped
public class SystemService {

    private static final OperatingSystemMXBean OS_MEAN =
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
                Double.valueOf(OS_MEAN.getSystemLoadAverage()))));
    }

}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.



The ***SystemService*** class contains a ***Publisher*** method that is called ***sendSystemLoad()***, which calculates and returns the average system load. The ***@Outgoing*** annotation on the ***sendSystemLoad()*** method indicates that the method publishes its calculation as a message on a topic in the Kafka messaging system. The ***Flowable.interval()*** method from ***rxJava*** is used to set the frequency of how often the system service publishes the calculation to the event stream.

The messages are transported between the service and the Kafka messaging system through a channel called ***systemLoad***. The name of the channel to use is set in the ***@Outgoing("systemLoad")*** annotation. Later in the guide, you will configure the service so that any messages sent by the ***system*** service through the ***systemLoad*** channel are published on a topic called ***system.load***, as shown in the following diagram:

![Reactive system publisher](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-reactive-messaging/prod/assets/reactive-messaging-system-inventory-publisher.png)


::page{title="Creating the consumer in the inventory microservice"}

The ***inventory*** microservice records in its inventory the average system load information that it received from potentially multiple instances of the ***system*** service.

Create the ***InventoryResource*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-reactive-messaging/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java
```


> Then, to open the InventoryResource.java file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java"}



```java
package io.openliberty.guides.inventory;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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




The ***inventory*** microservice receives the message from the ***system*** microservice over the ***@Incoming("systemLoad")*** channel. The properties of this channel are defined in the ***microprofile-config.properties*** file. The ***inventory*** microservice is also a RESTful service that is served at the ***/inventory*** endpoint.

The ***InventoryResource*** class contains a method called ***updateStatus()***, which receives the message that contains the average system load and updates its existing inventory of systems and their average system load. The ***@Incoming("systemLoad")*** annotation on the ***updateStatus()*** method indicates that the method retrieves the average system load information by connecting to the channel called ***systemLoad***. Later in the guide, you will configure the service so that any messages sent by the ***system*** service through the ***systemLoad*** channel are retrieved from a topic called ***system.load***, as shown in the following diagram:

![Reactive system inventory detail](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-reactive-messaging/prod/assets/reactive-messaging-system-inventory-detail.png)


::page{title="Configuring the MicroProfile Reactive Messaging connectors for Kafka"}

The ***system*** and ***inventory*** services exchange messages with the external messaging system through a channel. The MicroProfile Reactive Messaging Connector API makes it easy to connect each service to the channel. You just need to add configuration keys in a properties file for each of the services. These configuration keys define properties such as the name of the channel and the topic in the Kafka messaging system. Open Liberty includes the ***liberty-kafka*** connector for sending and receiving messages from Apache Kafka.

The system and inventory microservices each have a MicroProfile Config properties file to define the properties of their outgoing and incoming streams.

Create the system/microprofile-config.properties file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-reactive-messaging/start/system/src/main/resources/META-INF/microprofile-config.properties
```


> Then, to open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging/start/system/src/main/resources/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging/start/system/src/main/resources/META-INF/microprofile-config.properties"}



```
mp.messaging.connector.liberty-kafka.bootstrap.servers=kafka:9092

mp.messaging.outgoing.systemLoad.connector=liberty-kafka
mp.messaging.outgoing.systemLoad.topic=system.load
mp.messaging.outgoing.systemLoad.key.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.outgoing.systemLoad.value.serializer=io.openliberty.guides.models.SystemLoad$SystemLoadSerializer
```



The ***mp.messaging.connector.liberty-kafka.bootstrap.servers*** property configures the hostname and port for connecting to the Kafka server. The ***system*** microservice uses an outgoing connector to send messages through the ***systemLoad*** channel to the ***system.load*** topic in the Kafka message broker so that the ***inventory*** microservices can consume the messages. The ***key.serializer*** and ***value.serializer*** properties characterize how to serialize the messages. The ***SystemLoadSerializer*** class implements the logic for turning a ***SystemLoad*** object into JSON and is configured as the ***value.serializer***.

The ***inventory*** microservice uses a similar ***microprofile-config.properties*** configuration to define its required incoming stream.

Create the inventory/microprofile-config.properties file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-reactive-messaging/start/inventory/src/main/resources/META-INF/microprofile-config.properties
```


> Then, to open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging/start/inventory/src/main/resources/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging/start/inventory/src/main/resources/META-INF/microprofile-config.properties"}



```
mp.messaging.connector.liberty-kafka.bootstrap.servers=kafka:9092

mp.messaging.incoming.systemLoad.connector=liberty-kafka
mp.messaging.incoming.systemLoad.topic=system.load
mp.messaging.incoming.systemLoad.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.systemLoad.value.deserializer=io.openliberty.guides.models.SystemLoad$SystemLoadDeserializer
mp.messaging.incoming.systemLoad.group.id=system-load-status
```



The ***inventory*** microservice uses an incoming connector to receive messages through the ***systemLoad*** channel. The messages were published by the ***system*** microservice to the ***system.load*** topic in the Kafka message broker. The ***key.deserializer*** and ***value.deserializer*** properties define how to deserialize the messages. The ***SystemLoadDeserializer*** class implements the logic for turning JSON into a ***SystemLoad*** object and is configured as the ***value.deserializer***. The ***group.id*** property defines a unique name for the consumer group. A consumer group is a collection of consumers who share a common identifier for the group. You can also view a consumer group as the various machines that ingest from the Kafka topics. All of these properties are required by the [Apache Kafka Producer Configs](https://kafka.apache.org/documentation/#producerconfigs) and [Apache Kafka Consumer Configs](https://kafka.apache.org/documentation/#consumerconfigs).

::page{title="Configuring Liberty"}

To run the services, the Open Liberty on which each service runs needs to be correctly configured. Relevant features, including the [MicroProfile Reactive Messaging feature](https://openliberty.io/docs/ref/feature/#mpReactiveMessaging-3.0.html), must be enabled for the ***system*** and ***inventory*** services.

Create the system/server.xml configuration file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-reactive-messaging/start/system/src/main/liberty/config/server.xml
```


> Then, to open the server.xml file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging/start/system/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging/start/system/src/main/liberty/config/server.xml"}



```xml
<server description="System Service">

  <featureManager>
    <feature>cdi-4.0</feature>
    <feature>concurrent-3.0</feature>
    <feature>jsonb-3.0</feature>
    <feature>mpHealth-4.0</feature>
    <feature>mpConfig-3.1</feature>
    <feature>mpReactiveMessaging-3.0</feature>
  </featureManager>

  <variable name="http.port" defaultValue="9083"/>
  <variable name="https.port" defaultValue="9446"/>

  <httpEndpoint host="*" httpPort="${http.port}"
      httpsPort="${https.port}" id="defaultHttpEndpoint"/>

  <logging consoleLogLevel="INFO"/>
  <webApplication location="system.war" contextRoot="/"/>
</server>
```




The ***server.xml*** file is already configured for the ***inventory*** microservice.

::page{title="Building and running the application"}

Build the ***system*** and ***inventory*** microservices using Maven and then run them in Docker containers.

Create the Maven configuration file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-reactive-messaging/start/system/pom.xml
```


> Then, to open the pom.xml file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging/start/system/pom.xml, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging/start/system/pom.xml"}



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
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- Liberty configuration -->
        <liberty.var.http.port>9083</liberty.var.http.port>
        <liberty.var.https.port>9446</liberty.var.https.port>
    </properties>

    <dependencies>
        <!-- Provided dependencies -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>10.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>6.1</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile.reactive.messaging</groupId>
            <artifactId>microprofile-reactive-messaging-api</artifactId>
            <version>3.0</version>
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
            <version>3.8.0</version>
        </dependency>
        <dependency>
            <groupId>io.reactivex.rxjava3</groupId>
            <artifactId>rxjava</artifactId>
            <version>3.1.9</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.16</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.16</version>
        </dependency>
        <!-- For tests -->
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>kafka</artifactId>
            <version>1.20.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.11.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>1.20.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
                <configuration>
                    <packagingExcludes>pom.xml</packagingExcludes>
                </configuration>
            </plugin>

            <!-- Liberty plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.10.3</version>
                <configuration>
                    <!-- devc config -->
                    <containerRunOpts>
                        -p 9085:9085
                        --network=reactive-app
                    </containerRunOpts>
                </configuration>
            </plugin>

            <!-- Plugin to run unit tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.0</version>
            </plugin>

            <!-- Plugin to run integration tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.5.0</version>
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



The ***pom.xml*** file lists the ***microprofile-reactive-messaging-api***, ***kafka-clients***, and ***rxjava*** dependencies.

The ***microprofile-reactive-messaging-api*** dependency is needed to enable the use of MicroProfile Reactive Messaging API. The ***kafka-clients*** dependency is added because the application needs a Kafka client to connect to the Kafka broker. The ***rxjava*** dependency is used for creating events at regular intervals.

Start your Docker environment. Dockerfiles are provided for you to use.

To build the application, run the Maven ***install*** and ***package*** goals from the command line in the ***start*** directory:

```bash
mvn -pl models install
mvn package
```



Run the following commands to containerize the microservices:

```bash
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
```

Next, use the provided script to start the application in Docker containers. The script creates a network for the containers to communicate with each other. It also creates containers for Kafka and the microservices in the project. For simplicity, the script starts one instance of the system service.


```bash
./scripts/startContainers.sh
```

::page{title="Testing the application"}

The application might take some time to become available. After the application is up and running, you can access it by making a GET request to the ***/systems*** endpoint of the ***inventory*** service. 



Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.


Visit the ***http\://localhost:9085/health*** URL to confirm that the ***inventory*** microservice is up and running.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9085/health | jq
```




When both the liveness and readiness health checks are up, go to the ***http\://localhost:9085/inventory/systems*** URL to access the ***inventory*** microservice.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9085/inventory/systems | jq
```


You see the CPU ***systemLoad*** property for all the systems:

```
{
   "hostname":"30bec2b63a96",
   "systemLoad":2.25927734375
}
```


You can revisit the ***http\://localhost:9085/inventory/systems*** URL after a while, and you will notice the CPU ***systemLoad*** property for the systems changed.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9085/inventory/systems | jq
```



You can use the ***http://localhost:9085/inventory/systems/{hostname}*** URL to see the CPU ***systemLoad*** property for one particular system.

In the following example, the ***30bec2b63a96*** value is the ***hostname***. If you go to the ***http://localhost:9085/inventory/systems/30bec2b63a96*** URL, you can see the CPU ***systemLoad*** property only for the ***30bec2b63a96*** ***hostname***:

```
{
   "hostname":"30bec2b63a96",
   "systemLoad":2.25927734375
}
```

::page{title="Tearing down the environment"}

Run the following script to stop the application:


```bash
./scripts/stopContainers.sh
```

::page{title="Summary"}

### Nice Work!

You just developed a reactive Java application using MicroProfile Reactive Messaging, Open Liberty, and Kafka.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-reactive-messaging*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-reactive-messaging
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Creating%20reactive%20Java%20microservices&guide-id=cloud-hosted-guide-microprofile-reactive-messaging)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-reactive-messaging/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-reactive-messaging/pulls)



### Where to next?

* [Testing reactive Java microservices](https://openliberty.io/guides/reactive-service-testing.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
