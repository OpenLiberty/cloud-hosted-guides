---
markdown-version: v1
tool-type: theiadocker
---
::page{title="Welcome to the Producing and consuming messages in Java microservices guide!"}

Learn how to produce and consume messages to communicate between Java microservices in a standard way by using the Jakarta Messaging API with the embedded Liberty Messaging Server or an external messaging server, IBM MQ.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You’ll learn how to communicate between Java web services when one service is producing a continuous stream of asynchronous messages or events to be consumed by other services, rather than just sending and receiving individual requests for data. You will also learn how to use a messaging server and client to manage the production and consumption of the messages by the services.

In this guide, you will first use the embedded Liberty Messaging Server to manage messages, then you will optionally switch to using an external messaging server to manage the messages, in this case, [IBM MQ](https://www.ibm.com/products/mq). You might use an external messaging server if it is critical that none of the messages is lost if there is a system overload or outage; for example during a bank transfer in a banking application.

You will learn how to write your Java application using the Jakarta Messaging API which provides a standard way to produce and consume messages in Java application, regardless of which messaging server your application will ultimately use.

The application in this guide consists of two microservices, ***system*** and ***inventory***. Every 15 seconds, the ***system*** microservice computes and publishes a message that contains the system’s current CPU and memory load usage. The ***inventory*** microservice subscribes to that information at the ***/systems*** REST endpoint so that it can keep an updated list of all the systems and their current system loads.

You’ll create the ***system*** and ***inventory*** microservices using the Jakarta Messaging API to produce and consume the messages using the embedded Liberty Messaging Server.

![Application architecture where system and inventory services use the Jakarta Messaging to communicate.](https://raw.githubusercontent.com/OpenLiberty/draft-guide-jms-intro/draft/assets/architecture.png)


You will then, optionally, reconfigure the application, without changing the application's Java code, to use an external IBM MQ messaging server instead.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/draft-guide-jms-intro.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/draft-guide-jms-intro.git
cd draft-guide-jms-intro
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the ***finish*** directory and run the following Maven goal to build and install the ***models*** module. The ***models*** module contains the ***SystemLoad*** data class for both the ***system*** and ***inventory*** microservices to use.
```
cd finish
mvn -pl models clean install
```

Start the ***inventory*** microservice by running the following command:
```
mvn -pl inventory liberty:run
```

Next, open another command-line session, navigate to the ***finish*** directory, and start the ***system*** microservice by using the following command:
```
mvn -pl system liberty:run
```

When you see the following message, your Liberty instances are ready:
```
The defaultServer server is ready to run a smarter planet.
```



Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.


Visit the ***http\://localhost:9081/health*** URL to confirm that the ***inventory*** microservice is up and running.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9081/health | jq
```




When both the liveness and readiness health checks are up, go to the ***http\://localhost:9081/inventory/systems*** URL to access the ***inventory*** microservice. You see the ***systemLoad*** property for all the systems:


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9081/inventory/systems | jq
```



```
{
   "hostname": <your hostname>,
   "systemLoad": 6.037155240703536E-9
}
```


You can revisit the ***http\://localhost:9081/inventory/systems*** URL after a while, and you will notice the ***systemLoad*** property for the systems changed.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9081/inventory/systems | jq
```




After you are finished checking out the application, stop the Liberty instances by pressing `Ctrl+C` in each command-line session where you ran Liberty. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:
```bash
mvn -pl inventory liberty:stop
mvn -pl system liberty:stop
```


::page{title="Creating the consumer in the inventory microservice"}

Navigate to the ***start*** directory to begin.

```bash
cd /home/project/guide-jms-intro/start
```

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), dev mode listens for file changes and automatically recompiles and deploys your updates whenever you save a new change.

Run the following goal to start the ***inventory*** microservice in dev mode:

```bash
mvn -pl inventory liberty:dev
```

When you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

The ***inventory*** microservice records in its inventory the recent system load information that it received from potentially multiple instances of the ***system*** microservice.

Create the ***InventoryQueueListener*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-jms-intro/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryQueueListener.java
```


> Then, to open the InventoryQueueListener.java file in your IDE, select
> **File** > **Open** > draft-guide-jms-intro/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryQueueListener.java, or click the following button

::openFile{path="/home/project/draft-guide-jms-intro/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryQueueListener.java"}



```java
package io.openliberty.guides.inventory;

import io.openliberty.guides.models.SystemLoad;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

import java.util.logging.Logger;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(
        propertyName = "destinationLookup", propertyValue = "jms/InventoryQueue"),
    @ActivationConfigProperty(
        propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class InventoryQueueListener implements MessageListener {

    private static Logger logger =
            Logger.getLogger(InventoryQueueListener.class.getName());

    @Inject
    private InventoryManager manager;

    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String json = textMessage.getText();
                SystemLoad systemLoad = SystemLoad.fromJson(json);

                String hostname = systemLoad.hostname;
                Double recentLoad = systemLoad.recentLoad;
                if (manager.getSystem(hostname).isPresent()) {
                    manager.updateCpuStatus(hostname, recentLoad);
                    logger.info("Host " + hostname + " was updated: " + recentLoad);
                } else {
                    manager.addSystem(hostname, recentLoad);
                    logger.info("Host " + hostname + " was added: " + recentLoad);
                }
            } else {
                logger.warning(
                    "Unsupported Message Type: " + message.getClass().getName());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***inventory*** microservice receives the messages from the ***system*** microservice. Implement the ***InventoryQueueListener*** class with the ***MessageListener*** interface and annotate with ***@MessageDriven*** to monitor the ***jms/InventoryQueue*** message queue. Implement the ***onMessage()*** method that processes the incoming messages, updates the inventory by using the ***InventoryManager*** bean, and logs the action. Use the ***SystemLoad.fromJson()*** method to convert the JSON message string to the ***SystemLoad*** object.

Next, configure the ***inventory*** microservice with an embedded messaging server and the [Messaging Server Client](https://openliberty.io/docs/latest/reference/feature/messagingClient-3.0.html) feature.

Replace the inventory's ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > draft-guide-jms-intro/start/inventory/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/draft-guide-jms-intro/start/inventory/src/main/liberty/config/server.xml"}



```xml
<server description="Inventory Service">

  <featureManager>
    <feature>restfulWS-3.1</feature>
    <feature>cdi-4.0</feature>
    <feature>jsonb-3.0</feature>
    <feature>mpHealth-4.0</feature>
    <feature>mpConfig-3.1</feature>
    <!--tag::messaging[]-->
    <feature>messaging-3.1</feature>
    <!--end::messaging[]-->
    <!--tag::messagingServer[]-->
    <feature>messagingServer-3.0</feature>
    <!--end::messagingServer[]-->
    <!--tag::messagingClient[]-->
    <feature>messagingClient-3.0</feature>
    <!--end::messagingClient[]-->
    <feature>enterpriseBeansLite-4.0</feature>
    <feature>mdb-4.0</feature>
  </featureManager>

  <variable name="http.port" defaultValue="9081"/>
  <variable name="https.port" defaultValue="9444"/>

  <httpEndpoint id="defaultHttpEndpoint" host="*"
                httpPort="${http.port}" httpsPort="${https.port}" />

  <wasJmsEndpoint id="InboundJmsCommsEndpoint"
                  host="*"
                  wasJmsPort="7277"
                  wasJmsSSLPort="9101"/>

  <connectionManager id="InventoryCM" maxPoolSize="400" minPoolSize="1"/>

  <messagingEngine id="InventoryME">
    <queue id="InventoryQueue"
           maxQueueDepth="5000"/>
  </messagingEngine>

  <jmsConnectionFactory connectionManagerRef="InventoryCM"
                        jndiName="InventoryConnectionFactory">
    <properties.wasJms/>
  </jmsConnectionFactory>

  <jmsQueue id="InventoryQueue" jndiName="jms/InventoryQueue">
    <properties.wasJms queueName="InventoryQueue"/>
  </jmsQueue>

  <jmsActivationSpec id="guide-jms-intro-inventory/InventoryQueueListener">
    <properties.wasJms maxConcurrency="200"/>
  </jmsActivationSpec>

  <logging consoleLogLevel="INFO"/>

  <webApplication location="guide-jms-intro-inventory.war" contextRoot="/"/>

</server>
```




The ***messagingServer*** feature enables a Liberty runtime to host an embedded messaging server to manage messaging destinations. The ***messagingClient*** feature enables applications to connect to a Liberty messaging server and access the messaging destinations hosted on that server through the Jakarta Messaging API that is enabled by the ***messaging*** feature.

Add the ***wasJmsEndpoint*** element to configure the Liberty runtime to monitor and manage incoming JMS connections from any hosts. Set up the ***messagingEngine*** configuration to ensure that the Liberty runtime can manage incoming message queues more effectively, assigning a reliable and persistent destination for the ***InventoryQueue***. Configure a ***jmsConnectionFactory*** element to use the ***InventoryCM*** connection manager and set properties for the JMS implementation. Define a ***jmsQueue*** element for the ***InventoryQueue*** message queue with its JNDI name and a ***jmsActivationSpec*** element to configure properties, including the queue listener class name and maximum concurrency.

To learn more about configuration for the ***jmsQueue*** element and ***jmsConnectionFactory*** element, see the [JMS Queue](https://openliberty.io/docs/latest/reference/config/jmsQueue.html) and [JMS Connection Factory](https://openliberty.io/docs/latest/reference/config/jmsConnectionFactory.html) documentation.


::page{title="Creating the message producer in the system service "}

Open another command-line session, navigate to the ***start*** directory, and run the following goal to start the ***system*** microservice in dev mode:

```bash
mvn -pl system liberty:dev
```

When you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

The ***system*** microservice is the producer of the messages that are published to the messaging server as a stream of events. Every 15 seconds, the ***system*** microservice triggers an event that calculates the recent CPU usage for the last minute.

Create the ***SystemService*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-jms-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java
```


> Then, to open the SystemService.java file in your IDE, select
> **File** > **Open** > draft-guide-jms-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java, or click the following button

::openFile{path="/home/project/draft-guide-jms-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java"}



```java
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import com.sun.management.OperatingSystemMXBean;

import io.openliberty.guides.models.SystemLoad;
import jakarta.annotation.Resource;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;

@Singleton
public class SystemService {

    private static final OperatingSystemMXBean OS_MEAN =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static String hostname = null;

    private static Logger logger = Logger.getLogger(SystemService.class.getName());

    @Inject
    @JMSConnectionFactory("InventoryConnectionFactory")
    private JMSContext context;

    @Resource(lookup = "jms/InventoryQueue")
    private Queue queue;

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

    @Schedule(second = "*/15", minute = "*", hour = "*", persistent = false)
    public void sendSystemLoad() {
        SystemLoad systemLoad = new SystemLoad(getHostname(),
                                    Double.valueOf(OS_MEAN.getCpuLoad()));
        context.createProducer().send(queue, systemLoad.toString());
        logger.info(systemLoad.toString());
    }
}
```


The ***SystemService*** class contains the ***sendSystemLoad()*** method that calculates the recent system load, creates a ***SystemLoad*** object, and publishes the object as a message to the ***jms/InventoryQueue*** message queue running in the messaging server by using the ***send()*** method. The ***@Schedule*** annotation on the ***sendSystemLoad()*** method sets the frequency at which the system service publishes the calculation to the event stream, ensuring it runs every 15 seconds.


Next, configure the ***system*** microservice to access the message queue.

Replace the system's ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > draft-guide-jms-intro/start/system/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/draft-guide-jms-intro/start/system/src/main/liberty/config/server.xml"}



```xml
<server description="System Service">

  <featureManager>
    <feature>cdi-4.0</feature>
    <feature>jsonb-3.0</feature>
    <feature>mpHealth-4.0</feature>
    <feature>mpConfig-3.1</feature>
    <!--tag::messaging[]-->
    <feature>messaging-3.1</feature>
    <!--end::messaging[]-->
    <!--tag::messagingClient[]-->
    <feature>messagingClient-3.0</feature>
    <!--end::messagingClient[]-->
    <feature>enterpriseBeansLite-4.0</feature>
    <feature>mdb-4.0</feature>
  </featureManager>

  <variable name="http.port" defaultValue="9082"/>
  <variable name="https.port" defaultValue="9445"/>
  <!--tag::jms[]-->
  <variable name="inventory.jms.host" defaultValue="localhost"/>
  <variable name="inventory.jms.port" defaultValue="7277"/>
  <!--end::jms[]-->

  <httpEndpoint id="defaultHttpEndpoint" host="*"
                httpPort="${http.port}" httpsPort="${https.port}"/>

  <connectionManager id="InventoryCM" maxPoolSize="400" minPoolSize="1"/>

  <jmsConnectionFactory
    connectionManagerRef="InventoryCM"
    jndiName="InventoryConnectionFactory">
    <properties.wasJms
      remoteServerAddress="${inventory.jms.host}:${inventory.jms.port}:BootstrapBasicMessaging"/>
  </jmsConnectionFactory>

  <jmsQueue id="InventoryQueue" jndiName="jms/InventoryQueue">
    <properties.wasJms queueName="InventoryQueue"/>
  </jmsQueue>

  <logging consoleLogLevel="INFO"/>

  <webApplication location="guide-jms-intro-system.war" contextRoot="/"/>

</server>
```




The ***messaging*** and ***messagingClient*** features enable the Liberty runtime to provide the required messaging services. Add a ***connectionManager*** element to handle connections for the messaging server running on the ***inventory*** microservice. Define the ***jmsConnectionFactory*** element to use the ***InventoryCM*** connection manager and set up the required ***remoteServerAddress*** properties. Use the ***jmsQueue*** element to define the inventory message queue.

In your dev mode console for the ***system*** microservice, type ***r*** and press ***enter/return*** key to restart the Liberty instance so that Liberty reads the configuration changes. When you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```


::page{title="Running the application"}

You started the Open Liberty in dev mode at the beginning of the guide, so all the changes were automatically picked up.

You can find the ***inventory*** microservice at the following URLs:


 ***http\://localhost:9081/inventory/systems***


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9081/inventory/systems | jq
```


You can also use ***curl*** command to retrieve the ***hostname*** and ***systemLoad*** information from the ***inventory/systems*** REST endpoint in another command line session:
```
curl http://localhost:9081/inventory/systems
```

::page{title="Testing the inventory application"}

While you can test your application manually, you should rely on automated tests because they trigger a failure whenever a code change introduces a defect. Because the application is a RESTful web service application, you can use JUnit and the RESTful web service Client API to write tests. In testing the functionality of the application, the scopes and dependencies are being tested.

Create the ***InventoryEndpointIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-jms-intro/start/inventory/src/test/java/it/io/openliberty/guides/inventory/InventoryEndpointIT.java
```


> Then, to open the InventoryEndpointIT.java file in your IDE, select
> **File** > **Open** > draft-guide-jms-intro/start/inventory/src/test/java/it/io/openliberty/guides/inventory/InventoryEndpointIT.java, or click the following button

::openFile{path="/home/project/draft-guide-jms-intro/start/inventory/src/test/java/it/io/openliberty/guides/inventory/InventoryEndpointIT.java"}



```java
package it.io.openliberty.guides.inventory;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryEndpointIT {

    private static String port;
    private static String baseUrl;
    private static String hostname;

    private Client client;

    private final String INVENTORY_SYSTEMS = "inventory/systems";

    @BeforeAll
    public static void oneTimeSetup() {
        port = System.getProperty("http.port");
        baseUrl = "http://localhost:" + port + "/";
    }

    @BeforeEach
    public void setup() {
        client = ClientBuilder.newClient();
    }

    @AfterEach
    public void teardown() {
        client.close();
    }


    @Test
    @Order(1)
    public void testGetSystems() {
        Response response = this.getResponse(baseUrl + INVENTORY_SYSTEMS);
        this.assertResponse(baseUrl, response);

        JsonArray systems = response.readEntity(JsonArray.class);

        boolean hostnameExists = false;
        boolean recentLoadExists = false;
        for (int n = 0; n < systems.size(); n++) {
            hostnameExists = systems.getJsonObject(n)
                                    .get("hostname").toString().isEmpty();
            recentLoadExists = systems.getJsonObject(n)
                                      .get("systemLoad").toString().isEmpty();

            assertFalse(hostnameExists, "A host was registered, but it was empty");
            assertFalse(recentLoadExists,
                "A recent system load was registered, but it was empty");
            if (!hostnameExists && !recentLoadExists) {
                String host = systems.getJsonObject(n).get("hostname").toString();
                hostname = host.substring(1, host.length() - 1);
                break;
            }
        }
        assertNotNull(hostname, "Hostname should be set by the first test. (1)");
        response.close();
    }

    @Test
    @Order(2)
    public void testGetSystemsWithHost() {
        assertNotNull(hostname, "Hostname should be set by the first test. (2)");

        Response response =
            this.getResponse(baseUrl + INVENTORY_SYSTEMS + "/" + hostname);
        this.assertResponse(baseUrl, response);

        JsonObject system = response.readEntity(JsonObject.class);

        String responseHostname = system.getString("hostname");
        Boolean recentLoadExists = system.get("systemLoad").toString().isEmpty();

        assertEquals(hostname, responseHostname,
            "Hostname should match the one from the TestNonEmpty");
        assertFalse(recentLoadExists, "A recent system load should not be empty");

        response.close();
    }

    @Test
    @Order(3)
    public void testUnknownHost() {
        Response badResponse =
            client.target(baseUrl + INVENTORY_SYSTEMS + "/" + "badhostname")
                  .request(MediaType.APPLICATION_JSON).get();

        assertEquals(404, badResponse.getStatus(),
            "BadResponse expected status: 404. Response code not as expected.");

        String stringObj = badResponse.readEntity(String.class);
        assertTrue(stringObj.contains("hostname does not exist."),
            "badhostname is not a valid host but it didn't raise an error");

        badResponse.close();
    }

    private Response getResponse(String url) {
        return client.target(url).request().get();
    }

    private void assertResponse(String url, Response response) {
        assertEquals(200, response.getStatus(), "Incorrect response code from " + url);
    }

}
```




See the following descriptions of the test cases:

* ***testGetSystems()*** verifies that the hostname and the system load for each system in the inventory are not empty.

* ***testGetSystemsWithHost()*** verifies that the hostname and system load returned by the ***system*** microservice match the ones stored in the ***inventory*** microservice and ensures they are not empty.

* ***testUnknownHost()*** verifies that an unknown host or a host that does not expose their JVM system properties is correctly handled as an error.


### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode for the ***inventory*** microservice.

If the tests pass, you see a similar output to the following example:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.325 sec - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results :

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

When you are done checking out the application, stop the Liberty instances by pressing `Ctrl+C` in each command-line session where you ran the ***system*** and ***inventory*** microservices.

::page{title="Using IBM MQ as the messaging server - Optional"}

The application has been built and tested. In this section, you'll learn how to configure Liberty to use [IBM MQ container](https://github.com/ibm-messaging/mq-container) as the messaging server instead of the embedded Liberty Messaging Server.



If you're an Intel-based Mac user, start IBM MQ by running the following command on the command-line session:


```bash
docker pull icr.io/ibm-messaging/mq:9.4.0.0-r3

docker volume create qm1data

docker run \
--env LICENSE=accept \
--env MQ_QMGR_NAME=QM1 \
--volume qm1data:/mnt/mqm \
--publish 1414:1414 --publish 9443:9443 \
--detach \
--env MQ_APP_PASSWORD=passw0rd \
--env MQ_ADMIN_PASSWORD=passw0rd \
--rm \
--platform linux/amd64 \
--name QM1 \
icr.io/ibm-messaging/mq:9.4.0.0-r3
```

If you're an ARM-based Mac user, check out the [How to build Mac IBM MQ container image](https://community.ibm.com/community/user/integration/blogs/richard-coppen/2023/06/30/ibm-mq-9330-container-image-now-available-for-appl) blog in the IBM TechXchange Community website for building IBM MQ container image.

Navigate to an empty directory for building the IBM MQ Docker container image and run the following commands:
```bash
git clone https://github.com/ibm-messaging/mq-container.git -b 9.4.0.0-r3
cd mq-container
make build-devserver COMMAND=docker
```

After building the container image, you can find the image version:
```bash
docker images | grep mq
```

When the container image is built, you see an image similar to the ***ibm-mqadvanced-server-dev:9.4.0.0-arm64***. Now, you can start IBM MQ by running the following command on the command-line session:

```bash
docker volume create qm1data

docker run \
--env LICENSE=accept \
--env MQ_QMGR_NAME=QM1 \
--volume docker:/mnt/mqm \
--publish 1414:1414 --publish 9443:9443 \
--detach \
--env MQ_APP_PASSWORD=passw0rd \
--env MQ_ADMIN_PASSWORD=passw0rd \
--name QM1 ibm-mqadvanced-server-dev:9.4.0.0-arm64
```


When the IBM MQ container is running, you can access the ***https\://localhost:9443/ibmmq/console*** URL.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl https://localhost:9443/ibmmq/console
```




Replace the ***pom.xml*** file of the inventory service.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > draft-guide-jms-intro/start/inventory/pom.xml, or click the following button

::openFile{path="/home/project/draft-guide-jms-intro/start/inventory/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-jms-intro-inventory</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- Liberty configuration -->
        <liberty.var.http.port>9081</liberty.var.http.port>
        <liberty.var.https.port>9444</liberty.var.https.port>
        <!-- IBM MQ -->
        <liberty.var.ibmmq-hostname>localhost</liberty.var.ibmmq-hostname>
        <liberty.var.ibmmq-port>1414</liberty.var.ibmmq-port>
        <liberty.var.ibmmq-channel>DEV.APP.SVRCONN</liberty.var.ibmmq-channel>
        <liberty.var.ibmmq-queue-manager>QM1</liberty.var.ibmmq-queue-manager>
        <liberty.var.ibmmq-username>app</liberty.var.ibmmq-username>
        <liberty.var.ibmmq-password>passw0rd</liberty.var.ibmmq-password>
        <liberty.var.ibmmq-inventory-queue-name>DEV.QUEUE.1</liberty.var.ibmmq-inventory-queue-name>
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
        
        <!--  Required dependencies -->
        <dependency>
           <groupId>io.openliberty.guides</groupId>
           <artifactId>guide-jms-intro-models</artifactId>
           <version>1.0-SNAPSHOT</version>
        </dependency>
        <!-- For tests -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-client</artifactId>
            <version>6.2.9.Final</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-json-binding-provider</artifactId>
            <version>6.2.9.Final</version>
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
            </plugin>

            <!-- Plugin to run unit tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.3.1</version>
            </plugin>

            <!-- Plugin to run integration tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.3.1</version>
                <configuration>
                    <systemPropertyVariables>
                        <http.port>${liberty.var.http.port}</http.port>
                        <https.port>${liberty.var.https.port}</https.port>
                    </systemPropertyVariables>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```




Add the ***liberty.var.ibmmq-**** properties for the IBM MQ container. You can change to different values when you deploy the application on a production environment without modifying the Liberty ***server.xml*** configuration file.


Replace the ***server.xml*** file of the inventory service.

> To open the server.xml file in your IDE, select
> **File** > **Open** > draft-guide-jms-intro/start/inventory/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/draft-guide-jms-intro/start/inventory/src/main/liberty/config/server.xml"}



```xml
<server description="Inventory Service">

  <featureManager>
    <feature>restfulWS-3.1</feature>
    <feature>cdi-4.0</feature>
    <feature>jsonb-3.0</feature>
    <feature>mpHealth-4.0</feature>
    <feature>mpConfig-3.1</feature>
    <feature>messaging-3.1</feature>
    <feature>messagingClient-3.0</feature>
    <feature>messagingServer-3.0</feature>
    <feature>enterpriseBeansLite-4.0</feature>
    <feature>mdb-4.0</feature>
  </featureManager>

  <variable name="http.port" defaultValue="9081"/>
  <variable name="https.port" defaultValue="9444"/>

  <httpEndpoint id="defaultHttpEndpoint" host="*"
                httpPort="${http.port}" httpsPort="${https.port}"/>

  <wasJmsEndpoint id="InboundJmsCommsEndpoint"
                  host="*"
                  wasJmsPort="7277"
                  wasJmsSSLPort="9101"/>

  <jmsQueue id="InventoryQueue" jndiName="jms/InventoryQueue">
    <properties.wmqjmsra baseQueueName="${ibmmq-inventory-queue-name}"/>
  </jmsQueue>

  <jmsActivationSpec id="guide-jms-intro-inventory/InventoryQueueListener">
    <properties.wmqjmsra
      hostName="${ibmmq-hostname}"
      port="${ibmmq-port}"
      channel="${ibmmq-channel}"
      queueManager="${ibmmq-queue-manager}"
      userName="${ibmmq-username}"
      password="${ibmmq-password}"
      transportType="CLIENT"/>
  </jmsActivationSpec>

  <resourceAdapter id="wmqjmsra"
    location="https://repo.maven.apache.org/maven2/com/ibm/mq/wmq.jakarta.jmsra/9.4.0.0/wmq.jakarta.jmsra-9.4.0.0.rar"/>
    
  <logging consoleLogLevel="INFO"/>

  <webApplication location="guide-jms-intro-inventory.war" contextRoot="/"/>

</server>
```




Refine the ***jmsQueue*** and ***jmsActivationSpec*** configurations with the variables for IBM MQ settings. Add the ***resourceAdapter*** element to define the RAR file that provides the IBM MQ classes for Java and JMS. Note that the ***messagingEngine*** and ***jmsConnectionFactory*** configurations are removed from the configuration because they are no longer required.

Replace the ***pom.xml*** file of the system service.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > draft-guide-jms-intro/start/system/pom.xml, or click the following button

::openFile{path="/home/project/draft-guide-jms-intro/start/system/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-jms-intro-system</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- Liberty configuration -->
        <liberty.var.http.port>9082</liberty.var.http.port>
        <liberty.var.https.port>9445</liberty.var.https.port>
        <liberty.var.inventory.jms.host>localhost</liberty.var.inventory.jms.host>
        <liberty.var.inventory.jms.port>7277</liberty.var.inventory.jms.port>
        <!-- IBM MQ -->
        <liberty.var.ibmmq-hostname>localhost</liberty.var.ibmmq-hostname>
        <liberty.var.ibmmq-port>1414</liberty.var.ibmmq-port>
        <liberty.var.ibmmq-channel>DEV.APP.SVRCONN</liberty.var.ibmmq-channel>
        <liberty.var.ibmmq-queue-manager>QM1</liberty.var.ibmmq-queue-manager>
        <liberty.var.ibmmq-username>app</liberty.var.ibmmq-username>
        <liberty.var.ibmmq-password>passw0rd</liberty.var.ibmmq-password>
        <liberty.var.ibmmq-inventory-queue-name>DEV.QUEUE.1</liberty.var.ibmmq-inventory-queue-name>
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
        <!-- Required dependencies -->
        <dependency>
            <groupId>io.openliberty.guides</groupId>
            <artifactId>guide-jms-intro-models</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.13</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.13</version>
        </dependency>
        <!-- For tests -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.3</version>
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
            </plugin>

            <!-- Plugin to run unit tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.3.1</version>
            </plugin>

            <!-- Plugin to run integration tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.3.1</version>
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




Add the ***liberty.var.ibmmq-**** properties for the IBM MQ container as you did for the ***inventory*** microservice previously.


Replace the ***server.xml*** file of the system service.

> To open the server.xml file in your IDE, select
> **File** > **Open** > draft-guide-jms-intro/start/system/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/draft-guide-jms-intro/start/system/src/main/liberty/config/server.xml"}



```xml
<server description="System Service">

  <featureManager>
    <feature>cdi-4.0</feature>
    <feature>jsonb-3.0</feature>
    <feature>mpHealth-4.0</feature>
    <feature>mpConfig-3.1</feature>
    <feature>messaging-3.1</feature>
    <feature>messagingClient-3.0</feature>
    <feature>enterpriseBeansLite-4.0</feature>
    <feature>mdb-4.0</feature>
  </featureManager>

  <variable name="http.port" defaultValue="9082"/>
  <variable name="https.port" defaultValue="9445"/>
  <variable name="inventory.jms.host" defaultValue="localhost"/>
  <variable name="inventory.jms.port" defaultValue="7277"/>

  <httpEndpoint id="defaultHttpEndpoint" host="*"
                httpPort="${http.port}" httpsPort="${https.port}" />

  <connectionManager id="InventoryCM" maxPoolSize="400" minPoolSize="1"/>

  <jmsConnectionFactory
    connectionManagerRef="InventoryCM"
    jndiName="InventoryConnectionFactory">
    <properties.wmqjmsra
      hostName="${ibmmq-hostname}"
      port="${ibmmq-port}"
      channel="${ibmmq-channel}"
      queueManager="${ibmmq-queue-manager}"
      userName="${ibmmq-username}"
      password="${ibmmq-password}"
      transportType="CLIENT" />
  </jmsConnectionFactory>

  <jmsQueue id="InventoryQueue" jndiName="jms/InventoryQueue">
    <properties.wmqjmsra baseQueueName="${ibmmq-inventory-queue-name}"/>
  </jmsQueue>

  <resourceAdapter id="wmqjmsra"
    location="https://repo.maven.apache.org/maven2/com/ibm/mq/wmq.jakarta.jmsra/9.4.0.0/wmq.jakarta.jmsra-9.4.0.0.rar"/>

  <logging consoleLogLevel="INFO"/>

  <webApplication location="guide-jms-intro-system.war" contextRoot="/"/>

</server>
```




Replace the ***properties.wasJms*** configuration by the ***properties.wmqjmsra*** configuration. All property values are defined in the ***pom.xml*** file that you replaced. Also, modify the ***jmsQueue*** property to set the ***baseQueueName*** value with the ***${ibmmq-inventory-queue-name}*** variable. Add the ***resourceAdapter*** element like you did for the ***inventory*** microservice.


Start the ***inventory*** microservice by running the following command in dev mode:
```bash
mvn -pl inventory liberty:dev
```

Next, open another command-line session, navigate to the ***start*** directory, and start the ***system*** microservice by using the following command:
```bash
mvn -pl system liberty:dev
```

When you see the following message, your Liberty instances are ready in dev mode:
```
The defaultServer server is ready to run a smarter planet.
```

You can access the ***inventory*** microservice by the ***http\://localhost:9081/inventory/systems*** URL.

In the command shell where ***inventory*** dev mode is running, press ***enter/return*** to run the tests. If the tests pass, you'll see output that is similar to the following example:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.325 sec - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results :

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

After you are finished checking out the application, stop the Liberty instances by pressing `Ctrl+C` in the command-line sessions where you ran the ***system*** and ***inventory*** microservices.

Run the following commands to stop the running IBM MQ container and clean up the ***qm1data*** volume:

```bash
docker stop QM1
docker rm QM1
docker volume remove qm1data
```

::page{title="Summary"}

### Nice Work!

You just developed a Java cloud-native application that uses Jakarta Messaging to produce and consume messages in Open Liberty.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***draft-guide-jms-intro*** project by running the following commands:

```bash
cd /home/project
rm -fr draft-guide-jms-intro
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Producing%20and%20consuming%20messages%20in%20Java%20microservices&guide-id=cloud-hosted-draft-guide-jms-intro)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/draft-guide-jms-intro/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/draft-guide-jms-intro/pulls)



### Where to next?

* [Bidirectional communication between services using Jakarta WebSocket](https://openliberty.io/guides/jakarta-websocket.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
