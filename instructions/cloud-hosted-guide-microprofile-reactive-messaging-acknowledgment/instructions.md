---
markdown-version: v1
title: instructions
branch: lab-499-instruction
version-history-start-date: 2020-09-16 15:14:06 UTC
tool-type: theia
---
::page{title="Welcome to the Acknowledging messages using MicroProfile Reactive Messaging guide!"}

Learn how to acknowledge messages by using MicroProfile Reactive Messaging.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

MicroProfile Reactive Messaging provides a reliable way to handle messages in reactive applications. MicroProfile Reactive Messaging ensures that messages aren't lost by requiring that messages that were delivered to the target server are acknowledged after they are processed. Every message that gets sent out must be acknowledged. This way, any messages that were delivered to the target service but not processed, for example, due to a system failure, can be identified and sent again.

The application in this guide consists of two microservices, ***system*** and ***inventory***. Every 15 seconds, the ***system*** microservice calculates and publishes events that contain its current average system load. The ***inventory*** microservice subscribes to that information so that it can keep an updated list of all the systems and their current system loads. You can get the current inventory of systems by accessing the ***/systems*** REST endpoint. The following diagram depicts the application that is used in this guide:

![Reactive system inventory](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-reactive-messaging-acknowledgment/prod/assets/reactive-messaging-system-inventory-rest.png)


You will explore the acknowledgment strategies that are available with MicroProfile Reactive Messaging, and you'll implement your own manual acknowledgment strategy. To learn more about how the reactive Java services used in this guide work, check out the [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html) guide.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-reactive-messaging-acknowledgment.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microprofile-reactive-messaging-acknowledgment.git
cd guide-microprofile-reactive-messaging-acknowledgment
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

::page{title="Choosing an acknowledgment strategy"}


Messages must be acknowledged in reactive applications. Messages are either acknowledged explicitly, or messages are acknowledged implicitly by MicroProfile Reactive Messaging. Acknowledgment for incoming messages is controlled by the ***@Acknowledgment*** annotation in MicroProfile Reactive Messaging. If the ***@Acknowledgment*** annotation isn't explicitly defined, then the default acknowledgment strategy applies, which depends on the method signature. Only methods that receive incoming messages and are annotated with the ***@Incoming*** annotation must acknowledge messages. Methods that are annotated only with the ***@Outgoing*** annotation don't need to acknowledge messages because messages aren't being received and MicroProfile Reactive Messaging requires only that _received_ messages are acknowledged.

Almost all of the methods in this application that require message acknowledgment are assigned the ***POST_PROCESSING*** strategy by default. If the acknowledgment strategy is set to ***POST_PROCESSING***, then MicroProfile Reactive Messaging acknowledges the message based on whether the annotated method emits data:

* If the method emits data, the incoming message is acknowledged after the outgoing message is acknowledged.
* If the method doesn't emit data, the incoming message is acknowledged after the method or processing completes.

It’s important that the methods use the ***POST_PROCESSING*** strategy because it fulfills the requirement that a message isn't acknowledged until after the message is fully processed. This processing strategy is beneficial in situations where messages must reliably not get lost. When the ***POST_PROCESSING*** acknowledgment strategy can’t be used, the ***MANUAL*** strategy can be used to fulfill the same requirement. In situations where message acknowledgment reliability isn't important and losing messages is acceptable, the ***PRE_PROCESSING*** strategy might be appropriate.

The only method in the guide that doesn't default to the ***POST_PROCESSING*** strategy is the ***sendProperty()*** method in the ***system*** service. The ***sendProperty()*** method receives property requests from the ***inventory*** service. For each property request, if the property that's being requested is valid, then the method creates and returns a ***PropertyMessage*** object with the value of the property. However, if the ***propertyName*** requested property doesn't exist, the request is ignored and no property response is returned.

A key difference exists between when a property response is returned and when a property response isn't returned. In the case where a property response is returned, the request doesn't finish processing until the response is sent and safely stored by the Kafka broker. Only then is the incoming message acknowledged. However, in the case where the requested property doesn’t exist and a property response isn't returned, the method finishes processing the request message so the message must be acknowledged immediately.

This case where a message either needs to be acknowledged immediately or some time later is one of the situations where the ***MANUAL*** acknowledgment strategy would be beneficial

::page{title="Implementing the MANUAL acknowledgment strategy"}


To begin, run the following command to navigate to the ***start*** directory:
```bash
cd /home/project/guide-microprofile-reactive-messaging-acknowledgment/start
```

Update the ***SystemService.sendProperty*** method to use the ***MANUAL*** acknowledgment strategy, which fits the method processing requirements better than the default ***PRE_PROCESSING*** strategy.

Replace the ***SystemService*** class.

> To open the SystemService.java file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging-acknowledgment/start/system/src/main/java/io/openliberty/guides/system/SystemService.java, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging-acknowledgment/start/system/src/main/java/io/openliberty/guides/system/SystemService.java"}



```java
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import io.openliberty.guides.models.PropertyMessage;
import io.openliberty.guides.models.SystemLoad;
import io.reactivex.rxjava3.core.Flowable;

@ApplicationScoped
public class SystemService {
    
    private static Logger logger = Logger.getLogger(SystemService.class.getName());

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
                        osMean.getSystemLoadAverage())));
    }

    @Incoming("propertyRequest")
    @Outgoing("propertyResponse")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public PublisherBuilder<Message<PropertyMessage>>
    sendProperty(Message<String> propertyMessage) {
        String propertyName = propertyMessage.getPayload();
        String propertyValue = System.getProperty(propertyName, "unknown");
        logger.info("sendProperty: " + propertyValue);
        if (propertyName == null || propertyName.isEmpty() || propertyValue == "unknown") {
            logger.warning("Provided property: " +
                    propertyName + " is not a system property");
            propertyMessage.ack();
            return ReactiveStreams.empty();
        }
        Message<PropertyMessage> message = Message.of(
                new PropertyMessage(getHostname(),
                        propertyName,
                        propertyValue),
                propertyMessage::ack
        );
        return ReactiveStreams.of(message);
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


The ***sendProperty()*** method needs to manually acknowledge the incoming messages, so it is annotated with the ***@Acknowledgment(Acknowledgment.Strategy.MANUAL)*** annotation. This annotation sets the method up to expect an incoming message. To meet the requirements of acknowledgment, the method parameter is updated to receive and return a ***Message*** of type ***String***, rather than just a ***String***. Then, the ***propertyName*** is extracted from the ***propertyMessage*** incoming message using the ***getPayload()*** method and checked for validity. One of the following outcomes occurs:

* If the ***propertyName*** system property isn't valid, the ***ack()*** method acknowledges the incoming message and returns an empty reactive stream using the ***empty()*** method. The processing is complete.
* If the system property is valid, the method creates a ***Message*** object with the value of the requested system property and sends it to the proper channel. The method acknowledges the incoming message only after the sent message is acknowledged.


::page{title="Waiting for a message to be acknowledged"}

The ***inventory*** service contains an endpoint that accepts ***PUT*** requests. When a ***PUT*** request that contains a system property is made to the ***inventory*** service, the ***inventory*** service sends a message to the ***system*** service. The message from the ***inventory*** service requests the value of the system property from the system service. Currently, a ***200*** response code is returned without confirming whether the sent message was acknowledged. Replace the ***inventory*** service to return a ***200*** response only after the outgoing message is acknowledged.

Replace the ***InventoryResource*** class.

> To open the InventoryResource.java file in your IDE, select
> **File** > **Open** > guide-microprofile-reactive-messaging-acknowledgment/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java, or click the following button

::openFile{path="/home/project/guide-microprofile-reactive-messaging-acknowledgment/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java"}



```java
package io.openliberty.guides.inventory;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.Publisher;

import io.openliberty.guides.models.PropertyMessage;
import io.openliberty.guides.models.SystemLoad;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;


@ApplicationScoped
@Path("/inventory")
public class InventoryResource {

    private static Logger logger = Logger.getLogger(InventoryResource.class.getName());
    private FlowableEmitter<Message<String>> propertyNameEmitter;

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

    @PUT
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    /* This method sends a message and returns a CompletionStage that doesn't
        complete until the message is acknowledged. */
    public CompletionStage<Response> updateSystemProperty(String propertyName) {
        logger.info("updateSystemProperty: " + propertyName);
        CompletableFuture<Void> result = new CompletableFuture<>();

        Message<String> message = Message.of(
                propertyName,
                () -> {
                    /* This is the ack callback, which runs when the outgoing
                        message is acknowledged. After the outgoing message is
                        acknowledged, complete the "result" CompletableFuture. */
                    result.complete(null);
                    /* An ack callback must return a CompletionStage that says
                        when it's complete. Asynchronous processing isn't necessary
                        so a completed CompletionStage is returned to indicate that
                        the work here is done. */
                    return CompletableFuture.completedFuture(null);
                }
        );

        propertyNameEmitter.onNext(message);
        /* Set up what happens when the message is acknowledged and the "result"
            CompletableFuture is completed. When "result" completes, the Response
            object is created with the status code and message. */
        return result.thenApply(a -> Response
                .status(Response.Status.OK)
                .entity("Request successful for the " + propertyName + " property\n")
                .build());
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

    @Incoming("addSystemProperty")
    public void getPropertyMessage(PropertyMessage pm)  {
        logger.info("getPropertyMessage: " + pm);
        String hostId = pm.hostname;
        if (manager.getSystem(hostId).isPresent()) {
            manager.updatePropertyMessage(hostId, pm.key, pm.value);
            logger.info("Host " + hostId + " was updated: " + pm);
        } else {
            manager.addSystem(hostId, pm.key, pm.value);
            logger.info("Host " + hostId + " was added: " + pm);
        }
    }

    @Outgoing("requestSystemProperty")
    public Publisher<Message<String>> sendPropertyName() {
        Flowable<Message<String>> flowable = Flowable.create(emitter ->
                this.propertyNameEmitter = emitter, BackpressureStrategy.BUFFER);
        return flowable;
    }
}
```



The ***sendPropertyName()*** method is updated to return a ***Message\<String\>*** instead of just a ***String***. This return type allows the method to set a callback that runs after the outgoing message is acknowledged. In addition to updating the ***sendPropertyName()*** method, the ***propertyNameEmitter*** variable is updated to send a ***Message\<String\>*** type.

The ***updateSystemProperty()*** method now returns a ***CompletionStage*** object wrapped around a Response type. This return type allows for a response object to be returned after the outgoing message is acknowledged. The outgoing ***message*** is created with the requested property name as the ***payload*** and an acknowledgment ***callback*** to execute an action after the message is acknowledged. The method creates a ***CompletableFuture*** variable that returns a ***200*** response code after the variable is completed in the ***callback*** function.

::page{title="Building and running the application"}

Build the ***system*** and ***inventory*** microservices using Maven and then run them in Docker containers.

Start your Docker environment. Dockerfiles are provided for you to use.

To build the application, run the Maven ***install*** and ***package*** goals from the command-line session in the ***start*** directory:

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

When both the liveness and readiness health checks are up, run the following curl command to access the ***inventory*** microservice:
```bash
curl -s http://localhost:9085/inventory/systems | jq
```

Look for the CPU ***systemLoad*** property for all the systems:

```
{
   "hostname":"30bec2b63a96",
   "systemLoad":1.44
}
```

The ***system*** service sends messages to the ***inventory*** service every 15 seconds. The ***inventory*** service processes and acknowledges each incoming message, ensuring that no ***system*** message is lost.


If you run the curl command again after a while, notice that the CPU ***systemLoad*** property for the systems changed.
```bash
curl -s http://localhost:9085/inventory/systems | jq
```

Make a ***PUT*** request to the ***http://localhost:9085/inventory/data*** URL to add the value of a particular system property to the set of existing properties. For example, run the following ***curl*** command:


```bash
curl -X PUT -d "os.name" http://localhost:9085/inventory/data --header "Content-Type:text/plain"
```

In this example, the ***PUT*** request with the ***os.name*** system property in the request body on the ***http://localhost:9085/inventory/data*** URL adds the ***os.name*** system property for your system. The ***inventory*** service sends a message that contains the requested system property to the ***system*** service. The ***inventory*** service then waits until the message is acknowledged before it sends a response back.

You see the following output:

```
Request successful for the os.name property
```

The previous example response is confirmation that the sent request message was acknowledged.


Run the following curl command again:
```bash
curl -s http://localhost:9085/inventory/systems | jq
```

The ***os.name*** system property value is now included with the previous values:

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
