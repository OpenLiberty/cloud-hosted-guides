
# Acknowledging messages using MicroProfile Reactive Messaging


Learn how to acknowledge messages by using MicroProfile Reactive Messaging.

## What you'll learn

THIS IS JAKUB'S MASTER TEST 123

MicroProfile Reactive Messaging provides a reliable way to handle messages in reactive applications. MicroProfile Reactive
Messaging ensures that messages aren't lost by requiring that messages that were delivered to the target server are acknowledged
after they are processed. Every message that gets sent out must be acknowledged. This way, any messages that were delivered
to the target service but not processed, for example, due to a system failure, can be identified and sent again.

The application in this guide consists of two microservices, `system` and `inventory`. Every 15 seconds, the `system`
microservice calculates and publishes events that contain its current average system load. The `inventory` microservice
subscribes to that information so that it can keep an updated list of all the systems and their current system loads.
You can get the current inventory of systems by accessing the `/systems` REST endpoint.

You will explore the acknowledgment strategies that are available with MicroProfile Reactive Messaging, and you'll implement
your own manual acknowledgment strategy. To learn more about how the reactive Java services used in this guide work, check
out the [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html) guide.

## Getting started

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-reactive-messaging-acknowledgment.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-microprofile-reactive-messaging-acknowledgment.git
cd guide-microprofile-reactive-messaging-acknowledgment
```
{: codeblock}


The `start` directory contains the starting project that you will build upon.

The `finish` directory contains the finished project that you will build.

## Choosing an acknowledgment strategy


Messages must be acknowledged in reactive applications. Messages are either acknowledged explicitly, or messages are acknowledged
implicitly by MicroProfile Reactive Messaging. Acknowledgment for incoming messages is controlled by the `@Acknowledgment`
annotation in MicroProfile Reactive Messaging. If the `@Acknowledgment` annotation isn't explicitly defined, then the
default acknowledgment strategy applies, which depends on the method signature. Only methods that receive incoming messages
and are annotated with the `@Incoming` annotation must acknowledge messages. Methods that are annotated only with the
`@Outgoing` annotation don't need to acknowledge messages because messages aren't being received and MicroProfile Reactive
Messaging requires only that _received_ messages are acknowledged.

Almost all of the methods in this application that require message acknowledgment are assigned the `POST_PROCESSING` strategy
by default. If the acknowledgment strategy is set to `POST_PROCESSING`, then MicroProfile Reactive Messaging acknowledges
the message based on whether the annotated method emits data:

    - If the method emits data, the incoming message is acknowledged after the outgoing message is acknowledged.
    - If the method doesn't emit data, the incoming message is acknowledged after the method or processing completes.

It’s important that the methods use the `POST_PROCESSING` strategy because it fulfills the requirement that a message isn't
acknowledged until after the message is fully processed. This processing strategy is beneficial in situations where messages
must reliably not get lost. When the `POST_PROCESSING` acknowledgment strategy can’t be used, the `MANUAL` strategy can
be used to fulfill the same requirement. In situations where message acknowledgment reliability isn't important and losing
messages is acceptable, the `PRE_PROCESSING` strategy might be appropriate.

The only method in the guide that doesn't default to the `POST_PROCESSING` strategy is the
`sendProperty()` method in the `system` service. The `sendProperty()`
method receives property requests from the `inventory` service. For each property request, if the property that's being
requested is valid, then the method `returns` a property response with the value of the property.
However, if the requested property `doesn't exist`, the request is ignored and no property response
is `returned`.

A key difference exists between when a property response is returned and when a property response isn't returned. In the
case where a property response is returned, the request doesn't finish processing until the response is sent and safely
stored by the Kafka broker. Only then is the incoming message acknowledged. However, in the case where the requested
property doesn’t exist and a property response isn't returned, the method finishes processing the request message so the
message must be acknowledged immediately.

This case where a message either needs to be acknowledged immediately or some time later is one of the situations where
the `MANUAL` acknowledgment strategy would be beneficial

## Implementing the MANUAL acknowledgment strategy


Navigate to the **start** directory to begin.

```
cd start
```
{: codeblock}


Update the `SystemService.sendProperty` method to use the `MANUAL` acknowledgment strategy, which fits the method processing
requirements better than the default `PRE_PROCESSING` strategy.

Replace the `SystemService` class.


> [File -> Open]guide-microprofile-reactive-messaging-acknowledgment/start/system/src/main/java/io/openliberty/guides/system/SystemService.java



```
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
{: codeblock}

The `sendProperty()` method needs to manually acknowledge the incoming messages, so it is
annotated with the `@Acknowledgment(Acknowledgment.Strategy.MANUAL)`
annotation. This annotation sets the method up to expect an incoming message. To meet the requirements of acknowledgment,
the method parameter is updated to receive and return a `Message` of type `String`, rather
than just a `String`. Then, the message `payload` is extracted and checked for validity.
One of the following outcomes occurs:

    - If the system property `isn't valid`, the method `acknowledges`
        the incoming message and `returns` an empty reactive stream. The processing is
        complete.
    - If the system property is valid, the method creates a `message` with the value of the
        requested system property and sends it to the proper channel. The method acknowledges the incoming message only
        after the sent message is acknowledged.

## Waiting for a message to be acknowledged


The `inventory` service contains an endpoint that accepts `PUT` requests. When a `PUT` request that contains a system property
is made to the `inventory` service, the `inventory` service sends a message to the `system` service. The message from the
`inventory` service requests the value of the system property from the system service. Currently, a `200` response code
is returned without confirming whether the sent message was acknowledged. Replace the `inventory` service to return a `200`
response only after the outgoing message is acknowledged.

Replace the `InventoryResource` class.


> [File -> Open]guide-microprofile-reactive-messaging-acknowledgment/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java



```
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
        complete until the message is acknowledged. */
    public CompletionStage<Response> updateSystemProperty(String propertyName) {
        logger.info("updateSystemProperty: " + propertyName);
        CompletableFuture<Void> result = new CompletableFuture<>();

        Message<String> message = Message.of(
                propertyName,
                () -> {
                        message is acknowledged. After the outgoing message is
                        acknowledged, complete the "result" CompletableFuture. */
                    result.complete(null);
                        when it's complete. Asynchronous processing isn't necessary 
                        so a completed CompletionStage is returned to indicate that 
                        the work here is done. */
                    return CompletableFuture.completedFuture(null);
                }
        );

        propertyNameEmitter.onNext(message);
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
{: codeblock}

The `sendPropertyName()` method is updated to return a
`Message<String>` instead of just a `String`. This return type allows the method to set a callback
that runs after the outgoing message is acknowledged. In addition to updating the `sendPropertyName()`
method, the `propertyNameEmitter` variable is updated to send a `Message<String>` type.

The `updateSystemProperty()` method now returns a
`CompletionStage` object wrapped around a Response type. This return type allows for a response
object to be returned after the outgoing message is acknowledged. The outgoing `message` is created
with the requested property name as the `payload` and an acknowledgment
`callback` to execute an action after the message is acknowledged. The method creates a
`CompletableFuture` variable that returns a `200` response
code after the variable is completed in the `callback` function.

## Building and running the application

Build the `system` and `inventory` microservices using Maven and then run them in Docker containers.

Start your Docker environment. Dockerfiles are provided for you to use.

To build the application, run the Maven `install` and `package` goals from the command-line session in the `start` directory:

```
mvn -pl models install
mvn package
```
{: codeblock}

Run the following command to download or update to the latest `openliberty/open-liberty:kernel-java8-openj9-ubi` Docker image:

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

Next, use the provided script to start the application in Docker containers. The script creates a network for the
containers to communicate with each other. It also creates containers for Kafka, Zookeeper, and the microservices in the
project. For simplicity, the script starts one instance of the `system` service.


```
./scripts/startContainers.sh
```
{: codeblock}



## Testing the application

After the application is up and running, you can access the application by making a GET request to the `/systems` endpoint
of the `inventory` service.

Go to the http://localhost:9085/inventory/systems[^] URL to access the inventory microservice You see the CPU `systemLoad`

```
curl http://localhost:9085/inventory/systems
```
{: codeblock}

property for all the systems:

```
{
   "hostname":"30bec2b63a96",
   "systemLoad":1.44
}
```

The `system` service sends messages to the `inventory` service every 15 seconds. The `inventory` service processes and
acknowledges each incoming message, ensuring that no `system` message is lost.

If you revisit the 
```
curl http://localhost:9085/inventory/systems
```
{: codeblock}

 URL after a while, you notice that the CPU `systemLoad`
property for the systems changed.

Make a `PUT` request to the `\http://localhost:9085/inventory/data` URL to add the value of a particular system property
to the set of existing properties. For example, run the following `curl` command:


```
curl -X PUT -d "os.name" http://localhost:9085/inventory/data --header "Content-Type:text/plain"
```
{: codeblock}


URL adds the `os.name` system property for your system. The `inventory` service sends a message that contains the requested
system property to the `system` service. The `inventory` service then waits until the message is acknowledged before it
sends a response back.

You see the following output:

```
Request successful for the os.name property
```

The previous example response is confirmation that the sent request message was acknowledged.

You can revisit the http://localhost:9085/inventory/systems[^] URL and see the `osname` system property value is now

```
curl http://localhost:9085/inventory/systems
```
{: codeblock}

included with the previous values:

```
{
   "hostname":"30bec2b63a96",
   "os.name":"Linux",
   "systemLoad":1.44
}
```

## Tearing down the environment

Finally, run the following script to stop the application:


```
./scripts/stopContainers.sh
```
{: codeblock}



# Summary

## Clean up your environment

Delete the **guide-microprofile-reactive-messaging-acknowledgment** project by navigating to the **/home/project/** directory

```
cd ../..
rm -r -f guide-microprofile-reactive-messaging-acknowledgment
rmdir guide-microprofile-reactive-messaging-acknowledgment
```
{: codeblock}

## Great work! You're done!

You developed an application by using MicroProfile Reactive Messaging, Open Liberty, and Kafka.

## Related Links

Learn more about MicroProfile.

[View the MicroProfile Reactive Messaging Specification](https://download.eclipse.org/microprofile/microprofile-reactive-messaging-1.0/microprofile-reactive-messaging-spec.html)

[View the MicroProfile Reactive Messaging Javadoc](https://download.eclipse.org/microprofile/microprofile-reactive-messaging-1.0/apidocs/)

[View the MicroProfile](https://openliberty.io/docs/latest/microprofile.html)

