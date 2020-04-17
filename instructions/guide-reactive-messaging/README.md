# Creating asynchronous reactive microservices using MicroProfile Reactive Messaging

NOTE: This repository contains the guide documentation source. To view the guide in published form, view it on the https://openliberty.io/guides/{projectid}.html[Open Liberty website].   

Learn how to use MicroProfile Reactive Messaging to implement reactive architecture application.

## What you'll learn

You will learn how to build reactive microservices using MicroProfile Reactive Messaging. You'll also learn how to send messages between these microservices using Kafka broker. 

### What is MicroProfile Reactive Messaging?

Asynchronous communication allows temporal decoupling of services in a microservice-based architecture. MicroProfile Reactive Messaging delivers a way to build systems of microservices promoting responsive, non-blocking, location transparency, elastic, resiliency to failure and temporal decoupling, enforcing asynchronous message passing between the different parts of the system.
https://download.eclipse.org/microprofile/microprofile-reactive-messaging-1.0/microprofile-reactive-messaging-spec.html#_microprofile_reactive_messaging[View the MicroProfile Reactive Messaging Specification^]

### What is Kafka?

https://kafka.apache.org/[Apache Kafka^] is a stream-processing platform that manages communication in distributed systems. Communication is message-oriented, and follows the publish-subscribe model. Kafka allows for real-time stream processing and distributed, replicated storage of streams and messages. Kafka Producer is a client or a program, which produces the message and pushes it to the Topic. Whereas Kafka Consumer is a client or a program, which consumes the published messages from the Producer.

### What is asynchronous programming?
Imagine asynchronous programming as a restaurant. After you're seated, a waiter takes your Order. Then, you must wait a few minutes for your food to be prepared. While your food is being prepared, your waiter may take more orders or serve other tables. After your food is ready, your waiter brings out the food to your table. However, in a synchronous model, the waiter must wait for your food to be prepared before serving any other customers. This method blocks other customers from placing orders or receiving their food.

The Restaurant application is designed with Reactive Architecture. The application that you'll be working with consists of five microservices namely **order**, **kitchen**, **bar**, **servingWindow** and **restaurantBFF**. And uses Kafka broker to enable reactive messaging communication between the Producer and Consumer microservices over the messaging channels.

You'll update the **order**, **kitchen**, **bar**, and **servingWindow** microservices to use MicroProfile Reactive Messaging for message passing. These microservices run on Open Liberty.

image::reactive-messaging-restaurant.png[Reactive restaurant,align="center"]

The **restaurantBFF** microservice is a https://microservices.io/patterns/apigateway.html#variation-backends-for-frontends[backend for frontend^] service.
It communicates with the backend microservices on the caller's behalf.

The waiter places a request using the **restaurantBFF** microservice.

The **order** microservice consumes the request, produces Order messages, and sends them to Kafka on the **food** or **beverage** channel depending on the type.

An Order begins with a **NEW** status. The **kitchen** and **bar** microservices consume and process the Order and update the status to **IN_PROGRESS** and **READY** consecutively. Thereâ€™s a sleep operation in between each status to represent the Order processing time, and the status updates are reported back to the **order** microservice via reactive messages on Kafka.

The **servingWindow** microservice contains a list of all ready to serve food and beverages. It consumes these statuses from **kitchen** and **bar** microservices.
Once the Order is served, itâ€™s marked as **COMPLETED** and the status is sent back to the **order** microservice as a message.

## Additional prerequisites

You will build and run the  microservices in Docker containers. You can learn more about containerizing microservices with Docker in the https://openliberty.io/guides/containerize.html[Containerizing microservices^] guide.

Install Docker by following the instructions on the official https://docs.docker.com/engine/installation[Docker documentation^]. Start your Docker environment.


USE this Github Link for the time being:

**https://github.com/ankitagrawa/foodOrderRestaurantApp**

## Creating a Reactive Messaging application

### Key Concepts

#### @Outgoing
OutgoingÂ is an annotation indicating that the method feeds a channel. The name of the channel is given as attribute.

#### @Incoming
IncomingÂ is an annotation indicating that the method consumes a channel. The name of the channel is given as attribute.

#### Channel
A channel is a name indicating which source or destination of messages is used. Channels are opaque Strings.

#### Connector
Reactive messaging uses Connectors to attach one end of aÂ channelÂ  and are configured using MicroProfile Config.
Open Liberty includes theÂ liberty-kafkaÂ connector for sending and receiving messages from an Apache Kafka broker.

*Navigate to the **start** directory to begin. Most of the code is already provided for use.*


### Building the order microservice

The **order** microservice uses MicroProfile reactive messaging to send messages to **kitchen** and **bar** microservices over kafka.

```
#Replace the **OrderResource** class.#
**order/src/main/java/io/openliberty/guides/order/OrderResource.java**
```
OrderResource.java
```
```

**order** microservice creates an Order and sends it to the kafka topic on [hotspot=OutgoingFood file=0]**@Outgoing("food")** or [hotspot=OutgoingBev file=0]**@Outgoing("beverage")** channel.
The [hotspot=IncomingStatus file=0]**@Incoming("updateStatus")** channel receives an updated Order status from the kafka.

### Building the kitchen microservice

If the order is a food, **kitchen** service receives an Order, processes it and sends back all the statuses to kafka on MicroProfile Reactive Messaging channels.

```
#Replace the **KitchenResource** class.#
**kitchen/src/main/java/io/openliberty/guides/kitchen/KitchenResource.java**
```
KitchenResource.java
```
```

### Building the bar microservice

If the Order is a drink, **bar** service receives an Order, processes it and sends back all the statuses to kafka on MicroProfile Reactive Messaging channels.

```
#Replace the **BarResource** class.#
**bar/src/main/java/io/openliberty/guides/bar/BarResource.java**
```
BarResource.java
```
```

### Building the servingWindow microservice

**servingWindow** microservice receives the **Ready** Order from the **bar** and **kitchen** microservices over kafka topics.
Once the Order is served, it's marked as **COMPLETED** and the status is sent across to **order** microservice using the MicroProfile Reactive Messaging.

```
#Replace the **ServingWindowResource** class.#
**servingWindow/src/main/java/io/openliberty/guides/servingWindow/ServingWindowResource.java**
```
ServingWindowResource.java
```
```

### Configuring the MicroProfile Reactive Messaging

Each of the four microservices **order**, **kitchen**, **bar** and **servingWindow** contains **microprofile-config.properties**
file, which configures kafka connector with the MicroProfile Reactive Messaging channels.

```
#microprofile-config.properties example from the kitchen microservice.#
**finish/kitchen/src/main/resources/META-INF/microprofile-config.properties**
```
microprofile-config.properties
```
```

Rest all the microservices use similar **microprofile-config.properties** configuration.

## Configuring the server

To get the service running, the Open Liberty server needs to be correctly configured.

Replace the **bar** **server.xml** to include [hotspot=featureMP file=0]**mpReactiveMessaging-1.0** **feature** element.

```
 #Replace the bar server configuration file.#
 **bar/src/main/liberty/config/server.xml**
```
server.xml
```
```

Rest all the microservices have **server.xml** already configured.

.The configuration does the following actions:
- Configures the server to enable MicroProfile Reactive Messaging.
 
```
 #pom.xml configuration#
 **bar/pom.xml**
---- 
pom.xml
```
```

- MicroProfile Reactive Messaging dependency is included in the Maven **pom.xml** file. This is specified in the [hotspot=reactiveMessaging file=1]**<dependency>** element.

## Building the application

You will build and run the **order**, **kitchen**, **bar**, **servingWindow** and **restaurantBFF** microservices in Docker containers. You can learn more about containerizing microservices with Docker in the https://openliberty.io/guides/containerize.html[Containerizing microservices^] guide.

Start your Docker environment.

To build the application, run the Maven **install** goal from the command line in the **start** directory:

```
mvn -pl models install
mvn package
```

*Run the following commands to containerize the application:*

```
docker build -t order:1.0-SNAPSHOT order/.
docker build -t kitchen:1.0-SNAPSHOT kitchen/.
docker build -t bar:1.0-SNAPSHOT bar/.
docker build -t servingWindow:1.0-SNAPSHOT servingWindow/.
docker build -t restaurantBFF:1.0-SNAPSHOT restaurantBFF/.
```

Next, use the provided script to start the application in Docker containers. The script creates a network for the containers to communicate with each other. It also creates containers for Kafka, Zookeeper, and all of the microservices in the project.


--
```
./scripts/startContainers.sh
```
--

--
```
.\scripts\startContainers.bat
```
--

## Trying the application

You can access the application by making requests to the **restaurantBFF** endpoint using OpenAPI.

The services take some time to become available. Check out the service that you created at the
http://localhost:9080/openapi/ui[^] URL.

### Place Orders

Expand the **/api/orders** **POST** request to post an Order and click *Try it out*. Copy the following example input
into the text box:
 
```
{
  "tableID": "1",
  "foodList": [
    "burger"
  ],
  "beverageList": [
    "coke"
  ]
}
```

Click **Execute** and you will receive the 200 response.

### Check IN_PROGRESS Order Status

Now Expand the **/api/orders** **GET** request to get the Order status and click *Try it out*.

Click **Execute** and you will see the response with **IN_PROGRESS** status. 

```
[
  {
    "item": "burger",
    "orderID": "0001",
    "status": "IN_PROGRESS",
    "tableID": "1",
    "type": "FOOD"
  },
  {
    "item": "coke",
    "orderID": "0002",
    "status": "IN_PROGRESS",
    "tableID": "1",
    "type": "BEVERAGE"
  }
]
```

### Check READY Order Status

Click **Execute** again and you will see the response with **READY** status. 

```
[
  {
    "item": "burger",
    "orderID": "0001",
    "status": "READY",
    "tableID": "1",
    "type": "FOOD"
  },
  {
    "item": "coke",
    "orderID": "0002",
    "status": "READY",
    "tableID": "1",
    "type": "BEVERAGE"
  }
]
```

### Complete an Order

Expand the **/api/servingWindow/complete/{orderID}** **POST** request to complete an Order and click *Try it out*. Copy the following example input
into the text box:
 
```

0002
```

Click **Execute** and you will receive the 200 response.

Now Expand the **/api/orders** **GET** request to get the Order status and click *Try it out*.

Click **Execute** and you will see the response with **COMPLETED** status for **orderID 0002**. This updated status is cascaded to the **order** microservice.

```
[
  {
    "item": "burger",
    "orderID": "0001",
    "status": "IN_PROGRESS",
    "tableID": "1",
    "type": "FOOD"
  },
  {
    "item": "coke",
    "orderID": "0002",
    "status": "COMPLETED",
    "tableID": "1",
    "type": "BEVERAGE"
  }
]
```

## Testing the service

You will create an unit tests to test the basic functionality of the microservices. If a test failure occurs, then you may have introduced a bug into the code.














### Running the tests

Run tests

Navigate to the **restaurantBFF** directory, then verify that the tests pass by using the Maven **verify** goal:

```
mvn verify
```

When the tests succeed, you see output similar to the following example:






## Tearing down the environment

Navigate back to the **start** directory.

Finally, use the following script to stop the application:


--
```
./scripts/stopContainers.sh
```
--

--
```
.\scripts\stopContainers.sh
```
--

## Great work! You're done!

You have just developed an application using MicroProfile Reactive Messaging, Open Liberty and Kakfa.

## Related Links

Learn more about MicroProfile.

https://microprofile.io/[See the MicroProfile specs^]

https://openliberty.io/docs/ref/microprofile[View the MicroProfile API^]

https://download.eclipse.org/microprofile/microprofile-reactive-messaging-1.0/microprofile-reactive-messaging-spec.html#_microprofile_reactive_messaging[View the MicroProfile Reactive Messaging Specification^]
