---
markdown-version: v1
title: instructions
branch: lab-204-instruction
version-history-start-date: 2022-02-09T14:19:17.000Z
---

::page{title="What you'll learn"}

MicroProfile Reactive Messaging provides a reliable way to handle messages in reactive applications. MicroProfile Reactive
Messaging ensures that messages aren't lost by requiring that messages that were delivered to the target server are acknowledged
after they are processed. Every message that gets sent out must be acknowledged. This way, any messages that were delivered
to the target service but not processed, for example, due to a system failure, can be identified and sent again.

The application in this guide consists of two microservices, ***system*** and ***inventory***. Every 15 seconds, the ***system***
microservice calculates and publishes events that contain its current average system load. The ***inventory*** microservice
subscribes to that information so that it can keep an updated list of all the systems and their current system loads.
You can get the current inventory of systems by accessing the ***/systems*** REST endpoint. The following diagram depicts
the application that is used in this guide:

![Reactive system inventory](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-reactive-messaging-acknowledgment/prod/assets/reactive-messaging-system-inventory-rest.png)


You will explore the acknowledgment strategies that are available with MicroProfile Reactive Messaging, and you'll implement
your own manual acknowledgment strategy. To learn more about how the reactive Java services used in this guide work, check
out the [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html) guide.


::page{title="Choosing an acknowledgment strategy"}


Messages must be acknowledged in reactive applications. Messages are either acknowledged explicitly, or messages are acknowledged
implicitly by MicroProfile Reactive Messaging. Acknowledgment for incoming messages is controlled by the ***@Acknowledgment***
annotation in MicroProfile Reactive Messaging. If the ***@Acknowledgment*** annotation isn't explicitly defined, then the
default acknowledgment strategy applies, which depends on the method signature. Only methods that receive incoming messages
and are annotated with the ***@Incoming*** annotation must acknowledge messages. Methods that are annotated only with the
***@Outgoing*** annotation don't need to acknowledge messages because messages aren't being received and MicroProfile Reactive
Messaging requires only that _received_ messages are acknowledged.

Almost all of the methods in this application that require message acknowledgment are assigned the ***POST_PROCESSING*** strategy
by default. If the acknowledgment strategy is set to ***POST_PROCESSING***, then MicroProfile Reactive Messaging acknowledges
the message based on whether the annotated method emits data:

* If the method emits data, the incoming message is acknowledged after the outgoing message is acknowledged.
* If the method doesn't emit data, the incoming message is acknowledged after the method or processing completes.

It’s important that the methods use the ***POST_PROCESSING*** strategy because it fulfills the requirement that a message isn't
acknowledged until after the message is fully processed. This processing strategy is beneficial in situations where messages
must reliably not get lost. When the ***POST_PROCESSING*** acknowledgment strategy can’t be used, the ***MANUAL*** strategy can
be used to fulfill the same requirement. In situations where message acknowledgment reliability isn't important and losing
messages is acceptable, the ***PRE_PROCESSING*** strategy might be appropriate.

The only method in the guide that doesn't default to the ***POST_PROCESSING*** strategy is the
***sendProperty()*** method in the ***system*** service. The ***sendProperty()***
method receives property requests from the ***inventory*** service. For each property request, if the property that's being
requested is valid, then the method ***returns*** a property response with the value of the property.
However, if the requested property ***doesn't exist***, the request is ignored and no property response
is ***returned***.

A key difference exists between when a property response is returned and when a property response isn't returned. In the
case where a property response is returned, the request doesn't finish processing until the response is sent and safely
stored by the Kafka broker. Only then is the incoming message acknowledged. However, in the case where the requested
property doesn’t exist and a property response isn't returned, the method finishes processing the request message so the
message must be acknowledged immediately.

This case where a message either needs to be acknowledged immediately or some time later is one of the situations where
the ***MANUAL*** acknowledgment strategy would be beneficial

::page{title="Implementing the MANUAL acknowledgment strategy"}



To begin, run the following command to navigate to the **start** directory:
```
cd /home/project/guide-microprofile-reactive-messaging-acknowledgment/start
```

Update the ***SystemService.sendProperty*** method to use the ***MANUAL*** acknowledgment strategy, which fits the method processing
requirements better than the default ***PRE_PROCESSING*** strategy.

Replace the ***SystemService*** class.

> To open the unknown file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging-acknowledgment/start/unknown, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging-acknowledgment/start/unknown"}


The ***sendProperty()*** method needs to manually acknowledge the incoming messages, so it is
annotated with the ***@Acknowledgment(Acknowledgment.Strategy.MANUAL)***
annotation. This annotation sets the method up to expect an incoming message. To meet the requirements of acknowledgment,
the method parameter is updated to receive and return a ***Message*** of type ***String***, rather
than just a ***String***. Then, the message ***payload*** is extracted and checked for validity.
One of the following outcomes occurs:

* If the system property ***isn't valid***, the method ***acknowledges***
  the incoming message and ***returns*** an empty reactive stream. 
  The processing is complete.
* If the system property is valid, the method creates a ***message*** with the value of the
  requested system property and sends it to the proper channel. The method acknowledges the incoming message only
  after the sent message is acknowledged.

::page{title="Waiting for a message to be acknowledged"}


The ***inventory*** service contains an endpoint that accepts ***PUT*** requests. When a ***PUT*** request that contains a system property
is made to the ***inventory*** service, the ***inventory*** service sends a message to the ***system*** service. The message from the
***inventory*** service requests the value of the system property from the system service. Currently, a ***200*** response code
is returned without confirming whether the sent message was acknowledged. Replace the ***inventory*** service to return a ***200***
response only after the outgoing message is acknowledged.

Replace the ***InventoryResource*** class.

> To open the unknown file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging-acknowledgment/start/unknown, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging-acknowledgment/start/unknown"}


The ***sendPropertyName()*** method is updated to return a
***Message\<String\>*** instead of just a ***String***. This return type allows the method to set a callback
that runs after the outgoing message is acknowledged. In addition to updating the ***sendPropertyName()***
method, the ***propertyNameEmitter*** variable is updated to send a ***Message\<String\>*** type.

The ***updateSystemProperty()*** method now returns a
***CompletionStage*** object wrapped around a Response type. This return type allows for a response
object to be returned after the outgoing message is acknowledged. The outgoing ***message*** is created
with the requested property name as the ***payload*** and an acknowledgment
***callback*** to execute an action after the message is acknowledged. The method creates a
***CompletableFuture*** variable that returns a ***200*** response
code after the variable is completed in the ***callback*** function.

::page{title="Building and running the application"}

Build the ***system*** and ***inventory*** microservices using Maven and then run them in Docker containers.

Start your Docker environment. Dockerfiles are provided for you to use.

To build the application, run the Maven ***install*** and ***package*** goals from the command-line session in the ***start*** directory:

```bash
mvn -pl models install
mvn package
```


Run the following commands to containerize the microservices:

```bash
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
```

Next, use the provided script to start the application in Docker containers. The script creates a network for the
containers to communicate with each other. It also creates containers for Kafka, Zookeeper, and the microservices 
in the project. For simplicity, the script starts one instance of the ***system*** service.


```bash
./scripts/startContainers.sh
```

::page{title="Testing the application"}

The application might take some time to become available. After the application is up and running, 
you can access it by making a GET request to the ***/systems*** endpoint of the ***inventory*** service.


Run the following curl command to confirm that the **inventory** microservice is up and running.
```
curl -s http://localhost:9085/health | jq
```

When both the liveness and readiness health checks are up, run the following curl command to access the **inventory** microservice:
```
curl -s http://localhost:9085/inventory/systems | jq
```

Look for the CPU **systemLoad** property for all the systems:

```
{
   "hostname":"30bec2b63a96",
   "systemLoad":1.44
}
```

The ***system*** service sends messages to the ***inventory*** service every 15 seconds. The ***inventory*** service processes and
acknowledges each incoming message, ensuring that no ***system*** message is lost.


If you run the curl command again after a while, notice that the CPU **systemLoad** property for the systems changed.
```
curl -s http://localhost:9085/inventory/systems | jq
```

Make a ***PUT*** request to the ***http://localhost:9085/inventory/data*** URL to add the value of a particular system property
to the set of existing properties. For example, run the following ***curl*** command:


```bash
curl -X PUT -d "os.name" http://localhost:9085/inventory/data --header "Content-Type:text/plain"
```

In this example, the ***PUT*** request with the ***os.name*** system property in the request body on the 
***http://localhost:9085/inventory/data*** URL adds the ***os.name*** system property for your system. 
The ***inventory*** service sends a message that contains the requested system property to the ***system*** service. 
The ***inventory*** service then waits until the message is acknowledged before it sends a response back.

You see the following output:

```
Request successful for the os.name property
```

The previous example response is confirmation that the sent request message was acknowledged.


Run the following curl command again:
```
curl -s http://localhost:9085/inventory/systems | jq
```

The **os.name** system property value is now included with the previous values:

```
{
   "hostname":"30bec2b63a96",
   "os.name":"Linux",
   "systemLoad":1.44
}
```

::page{title="Tearing down the environment"}

Finally, run the following script to stop the application:


```bash
./scripts/stopContainers.sh
```

::page{title="Summary"}

### Nice Work!

You developed an application by using MicroProfile Reactive Messaging, Open Liberty, and Kafka.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-reactive-messaging-acknowledgment*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-reactive-messaging-acknowledgment
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Acknowledging%20messages%20using%20MicroProfile%20Reactive%20Messaging&guide-id=cloud-hosted-guide-microprofile-reactive-messaging-acknowledgment)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-reactive-messaging-acknowledgment/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-reactive-messaging-acknowledgment/pulls)



### Where to next?

* [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html)
* [Integrating RESTful services with a reactive system](https://openliberty.io/guides/microprofile-reactive-messaging-rest.html)
* [Streaming updates to a client using Server-Sent Events](https://openliberty.io/guides/reactive-messaging-sse.html)
* [Testing reactive Java microservices](https://openliberty.io/guides/reactive-service-testing.html)
* [Consuming RESTful services asynchronously with template interfaces](https://openliberty.io/guides/microprofile-rest-client-async.html)

**Learn more about MicroProfile**
* [View the MicroProfile Reactive Messaging Specification](https://download.eclipse.org/microprofile/microprofile-reactive-messaging-1.0/microprofile-reactive-messaging-spec.html)
* [View the MicroProfile Reactive Messaging Javadoc](https://download.eclipse.org/microprofile/microprofile-reactive-messaging-1.0/apidocs/)
* [View the MicroProfile](https://openliberty.io/docs/latest/microprofile.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
