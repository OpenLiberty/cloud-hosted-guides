---
markdown-version: v1
title: instructions
branch: lab-204-instruction
version-history-start-date: 2022-02-09T14:19:17.000Z
---
::page{title="Welcome to the Integrating RESTful services with a reactive system guide!"}

Learn how to integrate RESTful Java microservices with a reactive system by using MicroProfile Reactive Messaging.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You will learn how to integrate RESTful Java microservices with a reactive system by using MicroProfile Reactive Messaging. RESTful Java microservices don't use reactive concepts, so you will learn how to bridge the gap between the two using the RxJava library. In this guide, you will modify two microservices in an application so that when a user hits the RESTful endpoint, the microservice generates producer events.

The application in this guide consists of two microservices, ***system*** and ***inventory***. The following diagram illustrates the application:

![Reactive system inventory](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-reactive-messaging-rest-integration/prod/assets/reactive-messaging-system-inventory-rest.png)


Every 15 seconds, the ***system*** microservice calculates and publishes events that contain its current average system load. The ***inventory*** microservice subscribes to that information so that it can keep an updated list of all the systems and their current system loads. The current inventory of systems can be accessed via the ***/systems*** REST endpoint.

You will update the ***inventory*** microservice to subscribe to a ***PUT*** request response. This ***PUT*** request response accepts a specific system property in the request body, queries that system property on the ***system*** microservice, and provides the response. You will also update the ***system*** microservice to handle receiving and sending events that are produced by the new endpoint. You will configure new channels to handle the events that are sent and received by the new endpoint. To learn more about how the reactive Java services that are used in this guide work, check out the [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html) guide.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-reactive-messaging-rest-integration.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microprofile-reactive-messaging-rest-integration.git
cd guide-microprofile-reactive-messaging-rest-integration
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

::page{title="Adding a REST endpoint that produces events"}



To begin, run the following command to navigate to the ***start*** directory:
```bash
cd /home/project/guide-microprofile-reactive-messaging-rest-integration/start
```


The ***inventory*** microservice records and stores the average system load information from all of the connected system microservices. However, the ***inventory*** microservice does not contain an accessible REST endpoint to control the sending or receiving of reactive messages. Add the ***/data*** RESTful endpoint to the ***inventory*** service by replacing the ***InventoryResource*** class with an updated version of the class.

Replace the ***InventoryResource*** class.

> To open the unknown file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging-rest-integration/start/unknown, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging-rest-integration/start/unknown"}


The ***updateSystemProperty()*** method creates the ***/data*** endpoint that accepts ***PUT*** requests with a system property name in the request body. The ***propertyNameEmitter*** variable is an RxJava ***Emitter*** interface that sends the property name request to the event stream, which is Apache Kafka in this case.

The ***sendPropertyName()*** method contains the ***Flowable.create()*** RxJava method, which associates the emitter to a publisher that is responsible for publishing events to the event stream. The publisher in this example is then connected to the ***@Outgoing("requestSystemProperty")*** channel, which you will configure later in the guide. MicroProfile Reactive Messaging takes care of assigning the publisher to the channel.

The ***Flowable.create()*** method also allows the configuration of a ***BackpressureStrategy*** object, which controls what the publisher does if the emitted events can't be consumed by the subscriber. In this example, the publisher used the ***BackpressureStrategy.BUFFER*** strategy. With this strategy, the publisher can buffer events until the subscriber can consume them.

When the ***inventory*** service receives a request, it adds the system property name from the request body to the ***propertyNameEmitter*** ***FlowableEmitter*** interface. The property name sent to the emitter is then sent to the publisher. The publisher sends the event to the event channel by using the configured ***BackpressureStrategy*** object when necessary.

::page{title="Adding an event processor to a reactive service"}


The ***system*** microservice is the producer of the messages that are published to the Kafka messaging system as a stream of events. Every 15 seconds, the ***system*** microservice publishes events that contain its calculation of the average system load, which is its CPU usage, for the last minute. Replace the ***SystemService*** class to add message processing of the system property request from the ***inventory*** microservice and publish it to the Kafka messaging system.

Replace the ***SystemService*** class.

> To open the unknown file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging-rest-integration/start/unknown, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging-rest-integration/start/unknown"}


A new method that is named ***sendProperty()*** receives a system property name from the ***inventory*** microservice over the ***@Incoming("propertyRequest")*** channel. The method calculates the requested property in real time and publishes it back to Kafka over the ***@Outgoing("propertyResponse")*** channel. In this scenario, the ***sendProperty()*** method acts as a processor. Next, you'll configure the channels that you need.

::page{title="Configuring the MicroProfile Reactive Messaging connectors for Kafka"}



The ***system*** and ***inventory*** microservices each have a MicroProfile Config property file in which the properties of their incoming and outgoing channels are defined. These properties include the names of channels, the topics in the Kafka messaging system, and the associated message serializers and deserializers. To complete the message loop created in the previous sections, four channels must be added and configured.

Replace the inventory/microprofile-config.properties file.

> To open the unknown file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging-rest-integration/start/unknown, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging-rest-integration/start/unknown"}


The newly created RESTful endpoint requires two new channels that move the requested messages between the ***system*** and ***inventory*** microservices. The ***inventory*** microservice ***microprofile-config.properties*** file now has two new channels, ***requestSystemProperty*** and ***addSystemProperty***. The ***requestSystemProperty*** channel handles sending the system property request, and the ***addSystemProperty*** channel handles receiving the system property response.

Replace the system/microprofile-config.properties file.

> To open the unknown file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging-rest-integration/start/unknown, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging-rest-integration/start/unknown"}


Replace the ***system*** microservice ***microprofile-config.properties*** file to add the two new ***propertyRequest*** and ***propertyResponse*** channels. The ***propertyRequest*** channel handles receiving the property request, and the ***propertyResponse*** channel handles sending the property response.

::page{title="Building and running the application"}

Build the ***system*** and ***inventory*** microservices using Maven and then run them in Docker containers.

Start your Docker environment. Dockerfiles are provided for you to use.

To build the application, run the Maven ***install*** and ***package*** goals from the command line in the ***start*** directory:

```bash
mvn -pl models install
mvn package
```

Run the following command to download or update to the latest Open Liberty Docker image:

```bash
docker pull icr.io/appcafe/open-liberty:full-java11-openj9-ubi
```

Run the following commands to containerize the microservices:

```bash
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
```

Next, use the provided script to start the application in Docker containers. The script creates a network for the containers to communicate with each other. It also creates containers for Kafka, Zookeeper, and the microservices in the project. For simplicity, the script starts one instance of the ***system*** service.


```bash
./scripts/startContainers.sh
```

::page{title="Testing the application"}

The application might take some time to become available. After the application is up and running, you can access it by making a GET request to the ***/systems*** endpoint of the ***inventory*** service.


Run the following curl command to confirm that the ***inventory*** microservice is up and running.
```bash
curl -s http://localhost:9085/health | jq
```

When both the liveness and readiness health checks are up, run the following curl command to access the  ***inventory*** microservice:
```bash
curl -s http://localhost:9085/inventory/systems | jq
```

You see the CPU ***systemLoad*** property for all the systems:

```
{
   "hostname":"30bec2b63a96",   
   "systemLoad":1.44
}
```


You can revisit the ***inventory*** service after a while by running the following curl command:
```bash
curl -s http://localhost:9085/inventory/systems | jq
```

Notice the value of the ***systemLoad*** property for the systems is changed.

Make a ***PUT*** request on the ***http://localhost:9085/inventory/data*** URL to add the value of a particular system property to the set of existing properties. For example, run the following ***curl*** command:


```bash
curl -X PUT -d "os.name" http://localhost:9085/inventory/data --header "Content-Type:text/plain"
```

In this example, the ***PUT*** request with the ***os.name*** system property in the request body on the ***http://localhost:9085/inventory/data*** URL adds the ***os.name*** system property for your system.

You see the following output:

```
Request successful for the os.name property
```

The ***system*** service is available so the request to the service is successful and returns a ***200*** response code.


You can revisit the ***inventory*** service by running the following curl command:
```
curl -s http://localhost:9085/inventory/systems | jq
```

Notice that the ***os.name*** system property value is now included with the previous values:

```
{
   "hostname":"30bec2b63a96",
   "os.name":"Linux",
   "systemLoad":1.44
}
```

::page{title="Tearing down the environment"}

Run the following script to stop the application:


```bash
./scripts/stopContainers.sh
```

::page{title="Running multiple system instances"}


This application has only one instance of the ***system*** service. The ***inventory*** service collects system properties of all ***system*** services in the application. As an exercise, start multiple ***system*** services to see how the application handles it. When you start the ***system*** instances, you must provide a unique ***group.id*** through the ***MP_MESSAGING_INCOMING_PROPERTYREQUEST_GROUP_ID*** environment variable.

::page{title="Summary"}

### Nice Work!

You successfully integrated a RESTful microservice with a reactive system by using MicroProfile Reactive Messaging.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-reactive-messaging-rest-integration*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-reactive-messaging-rest-integration
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Integrating%20RESTful%20services%20with%20a%20reactive%20system&guide-id=cloud-hosted-guide-microprofile-reactive-messaging-rest-integration)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-reactive-messaging-rest-integration/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-reactive-messaging-rest-integration/pulls)



### Where to next?

* [Testing reactive Java microservices](https://openliberty.io/guides/reactive-service-testing.html)
* [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
