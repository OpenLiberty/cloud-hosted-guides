
# **Welcome to the Creating reactive Java microservices guide!**

Learn how to write reactive Java microservices using MicroProfile Reactive Messaging.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



# **What you'll learn**

You will learn how to build reactive microservices that can send requests to other microservices, and asynchronously receive and process the responses. You will use an external messaging system to handle the asynchronous messages that are sent and received between the microservices as streams of events. MicroProfile Reactive Messaging makes it easy to write and configure your application to send, receive, and process the events efficiently.

*Asynchronous messaging between microservices*

Asynchronous communication between microservices can be used to build reactive and responsive applications. By decoupling the requests sent by a microservice from the responses that it receives, the microservice is not blocked from performing other tasks while waiting for the requested data to become available. Imagine asynchronous communication as a restaurant. A waiter might come to your table and take your order. While you are waiting for your food to be prepared, that waiter serves other tables and takes their orders too. When your food is ready, the waiter brings your food to the table and then continues to serve the other tables. If the waiter were to operate synchronously, they must take your order and then wait until they deliver your food before serving any other tables. In microservices, a request call from a REST client to another microservice can be time-consuming because the network might be slow, or the other service might be overwhelmed with requests and can’t respond quickly. But in an asynchronous system, the microservice sends a request to another microservice and continues to send other calls and to receive and process other responses until it receives a response to the original request.

*What is MicroProfile Reactive Messaging?*

MicroProfile Reactive Messaging provides an easy way to asynchronously send, receive, and process messages that are received as continuous streams of events. You simply annotate application beans' methods and Open Liberty converts the annotated methods to reactive streams-compatible publishers, subscribers, and processors and connects them up to each other. MicroProfile Reactive Messaging provides a Connector API so that your methods can be connected to external messaging systems that produce and consume the streams of events, such as [Apache Kafka](https://kafka.apache.org/).

The application in this guide consists of two microservices, **system** and **inventory**. Every 15 seconds, the **system** microservice calculates and publishes an event that contains its current average system load. The **inventory** microservice subscribes to that information so that it can keep an updated list of all the systems and their current system loads. The current inventory of systems can be accessed via the **/systems** REST endpoint. You'll create the **system** and **inventory** microservices using MicroProfile Reactive Messaging.

![Reactive system inventory](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-reactive-messaging/master/assets/reactive-messaging-system-inventory.png)


# **Getting started**

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-reactive-messaging.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-microprofile-reactive-messaging.git
cd guide-microprofile-reactive-messaging
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.

# **Creating the producer in the system microservice**

Navigate to the **start** directory to begin. 

The **system** microservice is the producer of the messages that are published to the Kafka messaging system as a stream of events. Every 15 seconds, the **system** microservice publishes an event that contains its calculation of the average system load (its CPU usage) for the last minute.

Create the **SystemService** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-reactive-messaging/start/system/src/main/java/io/openliberty/guides/system/SystemService.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-reactive-messaging/start/system/src/main/java/io/openliberty/guides/system/SystemService.java





The **SystemService** class contains a **Publisher** method that is called **sendSystemLoad()**, which calculates and returns the average system load. The **@Outgoing** annotation on the **sendSystemLoad()** method indicates that the method publishes its calculation as a message on a topic in the Kafka messaging system. The **Flowable.interval()** method from **rxJava** is used to set the frequency of how often the system service publishes the calculation to the event stream.

The messages are transported between the service and the Kafka messaging system through a channel called **systemLoad**. The name of the channel to use is set in the **@Outgoing("systemLoad")** annotation. Later in the guide, you will configure the service so that any messages sent by the **system** service through the **systemLoad** channel are published on a topic called **system.load**, as shown in the following diagram:

![Reactive system publisher](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-reactive-messaging/master/assets/reactive-messaging-system-inventory-publisher.png)


# **Creating the consumer in the inventory microservice**

The **inventory** microservice records in its inventory the average system load information that it received from potentially multiple instances of the **system** service.

Create the **InventoryResource** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-reactive-messaging/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-reactive-messaging/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java





The **inventory** microservice receives the message from the **system** microservice over the **@Incoming("systemLoad")** channel. The properties of this channel are defined in the **microprofile-config.properties** file. The **inventory** microservice is also a RESTful service that is served at the **/inventory** endpoint.

The **InventoryResource** class contains a method called **updateStatus()**, which receives the message that contains the average system load and updates its existing inventory of systems and their average system load. The **@Incoming("systemLoad")** annotation on the **updateStatus()** method indicates that the method retrieves the average system load information by connecting to the channel called **systemLoad**. Later in the guide, you will configure the service so that any messages sent by the **system** service through the **systemLoad** channel are retrieved from a topic called **system.load**, as shown in the following diagram:

![Reactive system inventory detail](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-reactive-messaging/master/assets/reactive-messaging-system-inventory-detail.png)


# **Configuring the MicroProfile Reactive Messaging connectors for Kafka**

The **system** and **inventory** services exchange messages with the external messaging system through a channel. The MicroProfile Reactive Messaging Connector API makes it easy to connect each service to the channel. You just need to add configuration keys in a properties file for each of the services. These configuration keys define properties such as the name of the channel and the topic in the Kafka messaging system. Open Liberty includes the **liberty-kafka** connector for sending and receiving messages from Apache Kafka.

The system and inventory microservices each have a MicroProfile Config properties file to define the properties of their outgoing and incoming streams.

Create the system/microprofile-config.properties file.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-reactive-messaging/start/system/src/main/resources/META-INF/microprofile-config.properties
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-reactive-messaging/start/system/src/main/resources/META-INF/microprofile-config.properties





The **mp.messaging.connector.liberty-kafka.bootstrap.servers** property configures the hostname and port for connecting to the Kafka server. The **system** microservice uses an outgoing connector to send messages through the **systemLoad** channel to the **system.load** topic in the Kafka message broker so that the **inventory** microservices can consume the messages. The **key.serializer** and **value.serializer** properties characterize how to serialize the messages. The **SystemLoadSerializer** class implements the logic for turning a **SystemLoad** object into JSON and is configured as the **value.serializer**.

The **inventory** microservice uses a similar **microprofile-config.properties** configuration to define its required incoming stream.

Create the inventory/microprofile-config.properties file.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-reactive-messaging/start/inventory/src/main/resources/META-INF/microprofile-config.properties
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-reactive-messaging/start/inventory/src/main/resources/META-INF/microprofile-config.properties



The **inventory** microservice uses an incoming connector to receive messages through the **systemLoad** channel. The messages were published by the **system** microservice to the **system.load** topic in the Kafka message broker. The **key.deserializer** and **value.deserializer** properties define how to deserialize the messages. The **SystemLoadDeserializer** class implements the logic for turning JSON into a **SystemLoad** object and is configured as the **value.deserializer**. The **group.id** property defines a unique name for the consumer group. A consumer group is a collection of consumers who share a common identifier for the group. You can also view a consumer group as the various machines that ingest from the Kafka topics. All of these properties are required by the [Apache Kafka Producer Configs](https://kafka.apache.org/documentation/#producerconfigs) and [Apache Kafka Consumer Configs](https://kafka.apache.org/documentation/#consumerconfigs).

# **Configuring the server**

To run the services, the Open Liberty server on which each service runs needs to be correctly configured. Relevant features, including the [MicroProfile Reactive Messaging feature](https://openliberty.io/docs/ref/feature/#mpReactiveMessaging-1.0.html), must be enabled for the **system** and **inventory** services.

Create the system/server.xml configuration file.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-reactive-messaging/start/system/src/main/liberty/config/server.xml
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-reactive-messaging/start/system/src/main/liberty/config/server.xml





The **server.xml** file is already configured for the **inventory** microservice.

# **Building and running the application**

Build the **system** and **inventory** microservices using Maven and then run them in Docker containers.

Create the Maven configuration file.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-reactive-messaging/start/system/pom.xml
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-reactive-messaging/start/system/pom.xml




The **pom.xml** file lists the **microprofile-reactive-messaging-api**, **kafka-clients**, and **rxjava** dependencies.

The **microprofile-reactive-messaging-api** dependency is needed to enable the use of MicroProfile Reactive Messaging API. The **kafka-clients** dependency is added because the application needs a Kafka client to connect to the Kafka broker. The **rxjava** dependency is used for creating events at regular intervals.

Start your Docker environment. Dockerfiles are provided for you to use.

To build the application, run the Maven **install** and **package** goals from the command line in the **start** directory:

```
mvn -pl models install
mvn package
```
{: codeblock}


Run the following command to download or update to the latest Open Liberty Docker image:

```
docker pull openliberty/open-liberty:full-java11-openj9-ubi
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



# **Testing the application**

The application might take some time to become available. After the application is up and running, you can access it by making a GET request to the **/systems** endpoint of the **inventory** service. 



Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.


Visit the http://localhost:9085/health URL to confirm that the **inventory** microservice is up and running.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9085/health | jq
```
{: codeblock}




When both the liveness and readiness health checks are up, go to the http://localhost:9085/inventory/systems URL to access the **inventory** microservice.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9085/inventory/systems | jq
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


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9085/inventory/systems | jq
```
{: codeblock}



You can use the **http://localhost:9085/inventory/systems/{hostname}** URL to see the CPU **systemLoad** property for one particular system.

In the following example, the **30bec2b63a96** value is the **hostname**. If you go to the **http://localhost:9085/inventory/systems/30bec2b63a96** URL, you can see the CPU **systemLoad** property only for the **30bec2b63a96** **hostname**:

```
{
   "hostname":"30bec2b63a96",
   "systemLoad":2.25927734375
}
```

# **Tearing down the environment**

Run the following script to stop the application:


```
./scripts/stopContainers.sh
```
{: codeblock}



# **Summary**

## **Nice Work!**

You just developed a reactive Java application using MicroProfile Reactive Messaging, Open Liberty, and Kafka.



<br/>
## **Clean up your environment**


Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-microprofile-reactive-messaging** project by running the following commands:

```
cd /home/project
rm -fr guide-microprofile-reactive-messaging
```
{: codeblock}

<br/>
## **What did you think of this guide?**

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Creating%20reactive%20Java%20microservices&guide-id=cloud-hosted-guide-microprofile-reactive-messaging)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

<br/>
## **What could make this guide better?**

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-reactive-messaging/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-reactive-messaging/pulls)



<br/>
## **Where to next?**

* [Testing reactive Java microservices](https://openliberty.io/guides/reactive-service-testing.html)


<br/>
## **Log out of the session**

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.