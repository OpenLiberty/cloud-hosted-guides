---
markdown-version: v1
title: instructions
branch: lab-5932-instruction
version-history-start-date: 2023-04-14T18:24:15Z
tool-type: theia
---
::page{title="Welcome to the Testing reactive Java microservices guide!"}

Learn how to test reactive Java microservices in true-to-production environments using Testcontainers.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You will learn how to write integration tests for reactive Java microservices and to run the tests in true-to-production environments by using containers with [Testcontainers](https://java.testcontainers.org/) and JUnit. Testcontainers tests your containerized application from outside the container so that you are testing the exact same image that runs in production. The reactive application in this guide sends and receives messages between services by using an external message broker, [Apache Kafka](https://kafka.apache.org/). Using an external message broker enables asynchronous communications between services so that requests are non-blocking and decoupled from responses. You can learn more about reactive Java services that use an external message broker to manage communications in the [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html) guide.

![Reactive system inventory application](https://raw.githubusercontent.com/OpenLiberty/guide-reactive-service-testing/prod/assets/reactive-messaging-system-inventory.png)


*True-to-production integration testing with Testcontainers*

Tests sometimes pass during the development and testing stages of an application's lifecycle but then fail in production because of differences between your development and production environments. While you can create mock objects and custom setups to minimize differences between environments, it is difficult to mimic a production system for an application that uses an external messaging system. Testcontainers addresses this problem by enabling the testing of applications in the same Docker containers that you’ll use in production. As a result, your environment remains the same throughout the application’s lifecycle – from development, through testing, and into production. You can learn more about Testcontainers in the [Building true-to-production integration tests with Testcontainers](https://openliberty.io/guides/testcontainers.html) guide.


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-reactive-service-testing.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-reactive-service-testing.git
cd guide-reactive-service-testing
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

In this IBM Cloud environment, you need to change the user home to ***/home/project*** by running the following command:
```bash
sudo usermod -d /home/project theia
```


### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the tests, go to the ***finish*** directory and run the following Maven goal to install the ***models*** artifact to the local Maven repository:

```bash
cd finish
mvn -pl models install
```



Next, navigate to the ***finish/system*** directory and run the following Maven goal to build the ***system*** microservice and run the integration tests on an Open Liberty server in a container:


```bash
export TESTCONTAINERS_RYUK_DISABLED=true
cd system
mvn verify
```

You will see the following output:

```
 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 52.46 s - in it.io.openliberty.guides.system.SystemServiceIT

 Results:

 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0


 --- failsafe:3.2.5:verify (verify) @ system ---
 ------------------------------------------------------------------------
 BUILD SUCCESS
 ------------------------------------------------------------------------
 Total time:  57.710 s
 Finished at: 2024-02-01T08:48:15-08:00
 ------------------------------------------------------------------------
```

This command might take some time to run the first time because the dependencies and the Docker image for Open Liberty must download. If you run the same command again, it will be faster.

You can also try out the ***inventory*** integration tests by repeating the same commands in the ***finish/inventory*** directory.


::page{title="Testing with the Kafka consumer client"}






Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-reactive-service-testing/start
```

The example reactive application consists of the ***system*** and ***inventory*** microservices. The ***system*** microservice produces messages to the Kafka message broker, and the ***inventory*** microservice consumes messages from the Kafka message broker. You will write integration tests to see how you can use the Kafka consumer and producer client APIs to test each service. Kafka Testcontainers and JUnit have already been included as required test dependencies in your Maven ***pom.xml*** files for the ***system*** and ***inventory*** microservices.

The ***start*** directory contains three directories: the ***system*** microservice directory, the ***inventory*** microservice directory, and the ***models*** directory. The ***models*** directory contains the model class that defines the structure of the system load data that is used in the application. Run the following Maven goal to install the packaged ***models*** artifact to the local Maven repository so it can be used later by the ***system*** and ***inventory*** microservices:

```bash
mvn -pl models install
```

### Launching the system microservice in dev mode with container

Initiate the microservices in dev mode by executing the following command to launch a Kafka instance replicating the production environment. The ***startKafka*** script will launch a local Kafka container and establish a ***reactive-app*** network that allows the ***system*** and ***inventory*** microservices to connect to the Kafka message broker.


```bash
./scripts/startKafka.sh
```


Navigate to the ***start/system*** directory.

```bash
cd /home/project/guide-reactive-service-testing/start/system
```

In this IBM Cloud environment, you need to pre-create the ***logs*** directory by running the following commands:
```bash
mkdir -p /home/project/guide-reactive-service-testing/start/system/target/liberty/wlp/usr/servers/defaultServer/logs
chmod 777 /home/project/guide-reactive-service-testing/start/system/target/liberty/wlp/usr/servers/defaultServer/logs
```

To launch the ***system*** microservice in dev mode with container, configure the container by specifying the options within ***\<containerRunOpts\>*** configuration for connecting to the ***reactive-app*** network and exposing the container port.

Run the following goal to start the ***system*** microservice in dev mode with container:


```bash
export TESTCONTAINERS_RYUK_DISABLED=true
mvn liberty:devc
```

Read the [Testcontainers custom configuration](https://java.testcontainers.org/features/configuration/#disabling-ryuk) document for disabling Ryuk.


After you see the following message, your Liberty instance is ready in dev mode:


```
**************************************************************
*    Liberty is running in dev mode.
*    ...    
*    Liberty container port information:
*        Internal container HTTP port [ 9083 ] is mapped to container host port [ 9083 ] <
*   ...     
```

[Dev mode](https://openliberty.io/docs/latest/development-mode.html) holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

The ***system*** microservice actively seeks a Kafka topic for message push operations. Upon the successful initialization of a Kafka service, the ***system*** microservice establishes connectivity to Kafka message broker by using the ***mp.messaging.connector.liberty-kafka.bootstrap.servers*** property. Additionally, the running ***system*** container exposes its service on the port ***9083*** for testing purposes in dev mode with container.

### Implementing tests for the system microservice

Now you can start writing the test by using Testcontainers.

Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.

Create the ***SystemServiceIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-reactive-service-testing/start/src/test/java/it/io/openliberty/guides/system/SystemServiceIT.java
```


> Then, to open the SystemServiceIT.java file in your IDE, select
> **File** > **Open** > guide-reactive-service-testing/start/src/test/java/it/io/openliberty/guides/system/SystemServiceIT.java, or click the following button

::openFile{path="/home/project/guide-reactive-service-testing/start/src/test/java/it/io/openliberty/guides/system/SystemServiceIT.java"}



```java
package it.io.openliberty.guides.system;

import java.net.Socket;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import io.openliberty.guides.models.SystemLoad;
import io.openliberty.guides.models.SystemLoad.SystemLoadDeserializer;

@Testcontainers
public class SystemServiceIT {

    private static Logger logger = LoggerFactory.getLogger(SystemServiceIT.class);
    private static Network network = Network.newNetwork();

    public static KafkaConsumer<String, SystemLoad> consumer;

    private static ImageFromDockerfile systemImage =
        new ImageFromDockerfile("system:1.0-SNAPSHOT")
            .withDockerfile(Paths.get("./Dockerfile"));

    private static KafkaContainer kafkaContainer = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withListener(() -> "kafka:19092")
            .withNetwork(network);

    private static GenericContainer<?> systemContainer =
        new GenericContainer(systemImage)
            .withNetwork(network)
            .withExposedPorts(9083)
            .waitingFor(Wait.forHttp("/health/ready").forPort(9083))
            .withStartupTimeout(Duration.ofMinutes(3))
            .withLogConsumer(new Slf4jLogConsumer(logger))
            .dependsOn(kafkaContainer);

    private static boolean isServiceRunning(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @BeforeAll
    public static void startContainers() {
        if (isServiceRunning("localhost", 9083)) {
            System.out.println("Testing with mvn liberty:devc");
        } else {
            kafkaContainer.start();
            systemContainer.withEnv(
                "mp.messaging.connector.liberty-kafka.bootstrap.servers",
                "kafka:19092");
            systemContainer.start();
            System.out.println("Testing with mvn verify");
        }
    }

    @BeforeEach
    public void createKafkaConsumer() {
        Properties consumerProps = new Properties();
        if (isServiceRunning("localhost", 9083)) {
            consumerProps.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9094");
        } else {
            consumerProps.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaContainer.getBootstrapServers());
        }
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "system-load-status");
        consumerProps.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class.getName());
        consumerProps.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            SystemLoadDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumer = new KafkaConsumer<String, SystemLoad>(consumerProps);
        consumer.subscribe(Collections.singletonList("system.load"));
    }

    @AfterAll
    public static void stopContainers() {
        systemContainer.stop();
        kafkaContainer.stop();
        if (network != null) {
            network.close();
        }
    }

    @AfterEach
    public void closeKafkaConsumer() {
        consumer.close();
    }

    @Test
    public void testCpuStatus() {
        ConsumerRecords<String, SystemLoad> records =
            consumer.poll(Duration.ofMillis(30 * 1000));
        System.out.println("Polled " + records.count() + " records from Kafka:");

        for (ConsumerRecord<String, SystemLoad> record : records) {
            SystemLoad sl = record.value();
            System.out.println(sl);
            assertNotNull(sl.hostname);
            assertNotNull(sl.loadAverage);
        }
        consumer.commitAsync();
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.





Construct the ***systemImage*** using the ***ImageFromDockerfile*** class, which allows Testcontainers to build the Docker image from a Dockerfile during the test runtime. For instance, the provided Dockerfile at the specified paths ***./Dockerfile*** is used to generate the ***system:1.0-SNAPSHOT*** image.

Use the ***kafkaContainer*** class to instantiate the ***kafkaContainer*** test container, initiating the ***confluentinc/cp-kafka:latest*** Docker image. Similarly, use the ***GenericContainer*** class to create the ***systemContainer*** test container, starting the ***system:1.0-SNAPSHOT*** Docker image.
 
It's important to note that ***withListener()*** has been configured to ***kafka:19092***, as the containerized ***system*** microservice will function as an additional producer. Therefore, the Kafka container needs to set up a listener to accommodate this requirement. For further details on using additional consumer or producer with Kafka container, please visit [official Kafka Test Container Documentation](https://java.testcontainers.org/modules/kafka/)

Given that containers are isolated by default, facilitating communication between the ***kafkaContainer*** and the ***systemContainer*** requires placing them on the same ***network***. The ***dependsOn()*** method is used to indicate that the ***system*** microservice container should commence only after ensuring the readiness of the kafka container. 

Prior to initiating the ***systemContainer***, it is imperative to override the ***mp.messaging.connector.liberty-kafka.bootstrap.servers*** property with ***kafka:19092*** using the ***withEnv()*** method. This step is necessary due to the establishment of a listener in the Kafka container, configured to handle an additional producer.

The test uses the ***KafkaConsumer*** client API, configuring the consumer to use the ***BOOTSTRAP_SERVERS_CONFIG*** property with the Kafka broker address if a local ***system*** microservice container is present. In the absence of a local service container, it uses the ***getBootstrapServers()*** method to obtain the broker address from the Kafka Testcontainer. Subsequently, the consumer is set up to consume messages from the ***system.load*** topic within the ***Kafka*** container.

To consume messages from a stream, the messages need to be deserialized from bytes. Kafka has its own default deserializer, but a custom deserializer is provided for you. The deserializer is configured by the ***VALUE_DESERIALIZER_CLASS_CONFIG*** property and is implemented in the ***SystemLoad*** class. To learn more about Kafka APIs and their usage, please refer to the [official Kafka Documentation](https://kafka.apache.org/documentation/#api).

The running ***system*** microservice container produces messages to the ***systemLoad*** Kafka topic, as denoted by the ***@Outgoing*** annotation. The ***testCpuStatus()*** test method uses the ***consumer.poll()*** method from the ***KafkaConsumer*** client API to retrieve a record from Kafka every 3 seconds within a specified timeout limit. This record is produced by the system service. Subsequently, the method uses ***Assertions*** to verify that the polled record aligns with the expected record.

### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

You will see the following output:

```
 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 25.674 s - in it.io.openliberty.guides.system.SystemServiceIT

 Results:

 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

 Integration tests finished.
```

After you are finished running tests, stop the Open Liberty server by pressing `Ctrl+C` in the command-line session where you ran the server.


If you aren't running in dev mode, you can run the tests by running the following command:


```bash
mvn verify
```


You will see the following output:

```
 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 50.63 s - in it.io.openliberty.guides.system.SystemServiceIT

 Results:

 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0


 --- failsafe:3.2.5:verify (verify) @ system ---
 ------------------------------------------------------------------------
 BUILD SUCCESS
 ------------------------------------------------------------------------
 Total time:  55.636 s
 Finished at: 2024-01-31T11:33:40-08:00
 ------------------------------------------------------------------------
```


::page{title="Testing with the Kafka producer client"}

The ***inventory*** microservice is tested in the same way as the ***system*** microservice. The only difference is that the ***inventory*** microservice consumes messages, which means that tests are written to use the Kafka producer client.

### Launching the inventory microservice in dev mode with container

Navigate to the ***start/inventory*** directory.

```bash
cd /home/project/guide-reactive-service-testing/start/inventory
```

Pre-create the ***logs*** directory by running the following commands:
```bash
mkdir -p /home/project/guide-reactive-service-testing/start/inventory/target/liberty/wlp/usr/servers/defaultServer/logs
chmod 777 /home/project/guide-reactive-service-testing/start/inventory/target/liberty/wlp/usr/servers/defaultServer/logs
```

Run the following goal to start the ***inventory*** microservice in dev mode with container:


```bash
mvn liberty:devc
```

### Building test REST client

Create a REST client interface to access the ***inventory*** microservice.

Create the ***InventoryResourceClient*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-reactive-service-testing/start/src/test/java/it/io/openliberty/guides/inventory/InventoryResourceClient.java
```


> Then, to open the InventoryResourceClient.java file in your IDE, select
> **File** > **Open** > guide-reactive-service-testing/start/src/test/java/it/io/openliberty/guides/inventory/InventoryResourceClient.java, or click the following button

::openFile{path="/home/project/guide-reactive-service-testing/start/src/test/java/it/io/openliberty/guides/inventory/InventoryResourceClient.java"}



```java
package it.io.openliberty.guides.inventory;

import java.util.List;
import java.net.Socket;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Properties;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.client.ClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import io.openliberty.guides.models.SystemLoad;
import io.openliberty.guides.models.SystemLoad.SystemLoadSerializer;


@Testcontainers
public class InventoryServiceIT {

    private static Logger logger = LoggerFactory.getLogger(InventoryServiceIT.class);

    public static InventoryResourceClient client;

    private static Network network = Network.newNetwork();
    public static KafkaProducer<String, SystemLoad> producer;
    private static ImageFromDockerfile inventoryImage =
        new ImageFromDockerfile("inventory:1.0-SNAPSHOT")
            .withDockerfile(Paths.get("./Dockerfile"));

    private static KafkaContainer kafkaContainer = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withListener(() -> "kafka:19092")
            .withNetwork(network);

    private static GenericContainer<?> inventoryContainer =
        new GenericContainer(inventoryImage)
            .withNetwork(network)
            .withExposedPorts(9085)
            .waitingFor(Wait.forHttp("/health/ready").forPort(9085))
            .withStartupTimeout(Duration.ofMinutes(3))
            .withLogConsumer(new Slf4jLogConsumer(logger))
            .dependsOn(kafkaContainer);

    private static InventoryResourceClient createRestClient(String urlPath) {
        ClientBuilder builder = ResteasyClientBuilder.newBuilder();
        ResteasyClient client = (ResteasyClient) builder.build();
        ResteasyWebTarget target = client.target(UriBuilder.fromPath(urlPath));
        return target.proxy(InventoryResourceClient.class);
    }

    private static boolean isServiceRunning(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @BeforeAll
    public static void startContainers() {

        String urlPath;
        if (isServiceRunning("localhost", 9085)) {
            System.out.println("Testing with mvn liberty:devc");
            urlPath = "http://localhost:9085";
        } else {
            System.out.println("Testing with mvn verify");
            kafkaContainer.start();
            inventoryContainer.withEnv(
                "mp.messaging.connector.liberty-kafka.bootstrap.servers",
                "kafka:19092");
            inventoryContainer.start();
            urlPath = "http://"
                + inventoryContainer.getHost()
                + ":" + inventoryContainer.getFirstMappedPort();
        }

        System.out.println("Creating REST client with: " + urlPath);
        client = createRestClient(urlPath);
    }

    @BeforeEach
    public void createKafkaProducer() {
        Properties producerProps = new Properties();
        if (isServiceRunning("localhost", 9085)) {
            producerProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9094");
        } else {
            producerProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaContainer.getBootstrapServers());
        }

        producerProps.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName());
        producerProps.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            SystemLoadSerializer.class.getName());

        producer = new KafkaProducer<String, SystemLoad>(producerProps);
    }

    @AfterAll
    public static void stopContainers() {
        client.resetSystems();
        inventoryContainer.stop();
        kafkaContainer.stop();
        if (network != null) {
            network.close();
        }
    }

    @AfterEach
    public void closeKafkaProducer() {
        producer.close();
    }

    @Test
    public void testCpuUsage() throws InterruptedException {
        SystemLoad sl = new SystemLoad("localhost", 1.1);
        producer.send(new ProducerRecord<String, SystemLoad>("system.load", sl));
        Thread.sleep(5000);
        Response response = client.getSystems();
        Assertions.assertEquals(200, response.getStatus(), "Response should be 200");
        List<Properties> systems =
            response.readEntity(new GenericType<List<Properties>>() { });
        assertEquals(systems.size(), 1);
        for (Properties system : systems) {
            assertEquals(sl.hostname, system.get("hostname"),
                "Hostname doesn't match!");
            BigDecimal systemLoad = (BigDecimal) system.get("systemLoad");
            assertEquals(sl.loadAverage, systemLoad.doubleValue(),
                "CPU load doesn't match!");
        }
    }
}
```


The ***InventoryResourceClient*** interface declares the ***getSystems()*** and ***resetSystems()*** methods for accessing the corresponding endpoints within the ***inventory*** microservice.





### Implementing tests for the inventory microservice

Now you can start writing the test by using Testcontainers.

Create the ***InventoryServiceIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-reactive-service-testing/start/src/test/java/it/io/openliberty/guides/inventory/InventoryServiceIT.java
```


> Then, to open the unknown file in your IDE, select
> **File** > **Open** > guide-reactive-service-testing/start/unknown, or click the following button

::openFile{path="/home/project/guide-reactive-service-testing/start/unknown"}


The ***InventoryServiceIT*** class uses the ***KafkaProducer*** client API to generate messages in the test environment, which are then consumed by the ***inventory*** microservice container.

Similar to ***system*** microservice testing, the configuration of the producer ***BOOTSTRAP_SERVERS_CONFIG*** property depends on whether a local ***inventory*** microservice container is detected.  In addition, the producer is configured with a custom serializer provided in the ***SystemLoad*** class.

The ***testCpuUsage*** test method uses the ***producer.send()*** method, using the ***KafkaProducer*** client API, to generate the ***Systemload*** message. Subsequently, it uses ***Assertions*** to verify that the response from the ***inventory*** microservice aligns with the expected outcome.

### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

You will see the following output:

```
 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 32.564 s - in it.io.openliberty.guides.inventory.InventoryServiceIT

 Results:

 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

 Integration tests finished.
```

After you are finished running tests, stop the Open Liberty server by pressing `Ctrl+C` in the command-line session where you ran the server.

If you aren't running in dev mode, you can run the tests by running the following command:


```bash
mvn verify
```

You will see the following output:

```
 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 53.22 s - in it.io.openliberty.guides.inventory.InventoryServiceIT

 Results:

 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0


 --- failsafe:3.2.5:verify (verify) @ inventory ---
 ------------------------------------------------------------------------
 BUILD SUCCESS
 ------------------------------------------------------------------------
 Total time:  58.789 s
 Finished at: 2024-01-31T11:40:43-08:00
 ------------------------------------------------------------------------
```


When you're finished trying out the microservice, you can stop the local Kafka container by running the following command in ***start*** directory:


```bash
./scripts/stopKafka.sh
```


::page{title="Summary"}

### Nice Work!

You just tested two reactive Java microservices using Testcontainers.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-reactive-service-testing*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-reactive-service-testing
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Testing%20reactive%20Java%20microservices&guide-id=cloud-hosted-guide-reactive-service-testing)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-reactive-service-testing/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-reactive-service-testing/pulls)



### Where to next?

* [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html)
* [Testing a MicroProfile or Jakarta EE application](https://openliberty.io/guides/microshed-testing.html)

**Learn more about Testcontainers**
* [Visit the official Testcontainers website](https://testcontainers.com/)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
