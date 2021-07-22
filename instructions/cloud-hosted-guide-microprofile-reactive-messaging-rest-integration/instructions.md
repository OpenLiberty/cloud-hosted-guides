
# **Welcome to the Integrating RESTful services with a reactive system guide!**

Learn how to integrate RESTful Java microservices with a reactive system by using MicroProfile Reactive Messaging.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



# **What you'll learn**

You will learn how to integrate RESTful Java microservices with a reactive system by using MicroProfile Reactive
Messaging. RESTful Java microservices don't use reactive concepts, so you will learn how to bridge the gap between the
two using the RxJava library. In this guide, you will modify two microservices in an application so that when a user
hits the RESTful endpoint, the microservice generates producer events.

The application in this guide consists of two microservices, **system** and **inventory**. The following diagram illustrates
the application:

![Reactive system inventory](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-reactive-messaging-rest-integration/master/assets/reactive-messaging-system-inventory-rest.png)


Every 15 seconds, the **system** microservice calculates and publishes events that contain its current average system load.
The **inventory** microservice subscribes to that information so that it can keep an updated list of all the systems and
their current system loads. The current inventory of systems can be accessed via the **/systems** REST endpoint.

You will update the **inventory** microservice to subscribe to a **PUT** request response. This **PUT** request response
accepts a specific system property in the request body, queries that system property on the **system** microservice, and
provides the response. You will also update the **system** microservice to handle receiving and sending events that are
produced by the new endpoint. You will configure new channels to handle the events that are sent and received by the new endpoint.
To learn more about how the reactive Java services that are used in this guide work, check out the
[Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html) guide.

# **Getting started**

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-reactive-messaging-rest-integration.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-microprofile-reactive-messaging-rest-integration.git
cd guide-microprofile-reactive-messaging-rest-integration
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.

# **Adding a REST endpoint that produces events**



To begin, run the following command to navigate to the **start** directory:
```
cd /home/project/guide-microprofile-reactive-messaging-rest-integration/start
```
{: codeblock}


The **inventory** microservice records and stores the average system load information from all of the connected
system microservices. However, the **inventory** microservice does not contain an accessible REST endpoint to control the
sending or receiving of reactive messages. Add the **/data** RESTful endpoint to the **inventory** service by replacing the
**InventoryResource** class with an updated version of the class.

Replace the **InventoryResource** class.

> From the menu of the IDE, select   
> **File** > **Open** > guide-microprofile-reactive-messaging-rest-integration/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java




```
package io.openliberty.guides.inventory;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
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
    private FlowableEmitter<String> propertyNameEmitter;

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
    public Response updateSystemProperty(String propertyName) {
        logger.info("updateSystemProperty: " + propertyName);
        propertyNameEmitter.onNext(propertyName);
        return Response
                   .status(Response.Status.OK)
                   .entity("Request successful for the " + propertyName + " property\n")
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
    public Publisher<String> sendPropertyName() {
        Flowable<String> flowable = Flowable.<String>create(emitter -> 
            this.propertyNameEmitter = emitter, BackpressureStrategy.BUFFER);
        return flowable;
    }
}
```
{: codeblock}


The **updateSystemProperty()** method creates the **/data** endpoint that accepts
**PUT** requests with a system property name in the request body. The **propertyNameEmitter**
variable is an RxJava **Emitter** interface that sends the property name request to the event stream, which is Apache
Kafka in this case.

The **sendPropertyName()** method contains the
**Flowable.create()** RxJava method, which associates the emitter to a publisher
that is responsible for publishing events to the event stream. The publisher in this example is then connected to the
**@Outgoing("requestSystemProperty")** channel, which you will configure later in the
guide. MicroProfile Reactive Messaging takes care of assigning the publisher to the channel.

The **Flowable.create()** method also allows the configuration of a
**BackpressureStrategy** object, which controls what the publisher does if the emitted events
can't be consumed by the subscriber. In this example, the publisher used the **BackpressureStrategy.BUFFER** strategy. With
this strategy, the publisher can buffer events until the subscriber can consume them.

When the **inventory** service receives a request, it adds the system property name from the request body to the
**propertyNameEmitter** **FlowableEmitter** interface.
The property name sent to the emitter is then sent to the publisher. The publisher sends the event to the event channel
by using the configured **BackpressureStrategy** object when necessary.

# **Adding an event processor to a reactive service**


The **system** microservice is the producer of the messages that are published to the Kafka messaging system as a stream of
events. Every 15 seconds, the **system** microservice publishes events that contain its calculation of the average system
load, which is its CPU usage, for the last minute. Replace the **SystemService** class to add message processing of the
system property request from the **inventory** microservice and publish it to the Kafka messaging system.

Replace the **SystemService** class.

> From the menu of the IDE, select   
> **File** > **Open** > guide-microprofile-reactive-messaging-rest-integration/start/system/src/main/java/io/openliberty/guides/system/SystemService.java




```
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
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
    public PropertyMessage sendProperty(String propertyName) {
        logger.info("sendProperty: " + propertyName);
        if (propertyName == null || propertyName.isEmpty()) {
            logger.warning(propertyName == null ? "Null" : "An empty string"
                    + " is not System property.");
            return null;
        }
        return new PropertyMessage(getHostname(),
                propertyName,
                System.getProperty(propertyName, "unknown"));
    }
}
```
{: codeblock}


A new method that is named **sendProperty()** receives a
system property name from the **inventory** microservice over the **@Incoming("propertyRequest")**
channel. The method calculates the requested property in real time and publishes it back to Kafka over the
**@Outgoing("propertyResponse")** channel. In this scenario, the
**sendProperty()** method acts as a processor. Next, you'll configure the channels that you need.

# **Configuring the MicroProfile Reactive Messaging connectors for Kafka**



The **system** and **inventory** microservices each have a MicroProfile Config property file in which the properties of their
incoming and outgoing channels are defined. These properties include the names of channels, the topics in the Kafka
messaging system, and the associated message serializers and deserializers. To complete the message loop created in the previous sections, four channels
must be added and configured.

Replace the inventory/microprofile-config.properties file.

> From the menu of the IDE, select   
> **File** > **Open** > guide-microprofile-reactive-messaging-rest-integration/start/inventory/src/main/resources/META-INF/microprofile-config.properties




```
mp.messaging.connector.liberty-kafka.bootstrap.servers=localhost:9093

mp.messaging.incoming.systemLoad.connector=liberty-kafka
mp.messaging.incoming.systemLoad.topic=system.load
mp.messaging.incoming.systemLoad.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.systemLoad.value.deserializer=io.openliberty.guides.models.SystemLoad$SystemLoadDeserializer
mp.messaging.incoming.systemLoad.group.id=system-load-status

mp.messaging.incoming.addSystemProperty.connector=liberty-kafka
mp.messaging.incoming.addSystemProperty.topic=add.system.property
mp.messaging.incoming.addSystemProperty.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.addSystemProperty.value.deserializer=io.openliberty.guides.models.PropertyMessage$PropertyMessageDeserializer
mp.messaging.incoming.addSystemProperty.group.id=sys-property

mp.messaging.outgoing.requestSystemProperty.connector=liberty-kafka
mp.messaging.outgoing.requestSystemProperty.topic=request.system.property
mp.messaging.outgoing.requestSystemProperty.key.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.outgoing.requestSystemProperty.value.serializer=org.apache.kafka.common.serialization.StringSerializer
```
{: codeblock}


The newly created RESTful endpoint requires two new channels that move the requested messages between the **system**
and **inventory** microservices. The **inventory** microservice **microprofile-config.properties**
file now has two new channels, **requestSystemProperty** and
**addSystemProperty**. The **requestSystemProperty**
channel handles sending the system property request, and the **addSystemProperty** channel
handles receiving the system property response.

Replace the system/microprofile-config.properties file.

> From the menu of the IDE, select   
> **File** > **Open** > guide-microprofile-reactive-messaging-rest-integration/start/system/src/main/resources/META-INF/microprofile-config.properties




```
mp.messaging.connector.liberty-kafka.bootstrap.servers=localhost:9093

mp.messaging.outgoing.systemLoad.connector=liberty-kafka
mp.messaging.outgoing.systemLoad.topic=system.load
mp.messaging.outgoing.systemLoad.key.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.outgoing.systemLoad.value.serializer=io.openliberty.guides.models.SystemLoad$SystemLoadSerializer

mp.messaging.outgoing.propertyResponse.connector=liberty-kafka
mp.messaging.outgoing.propertyResponse.topic=add.system.property
mp.messaging.outgoing.propertyResponse.key.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.outgoing.propertyResponse.value.serializer=io.openliberty.guides.models.PropertyMessage$PropertyMessageSerializer

mp.messaging.incoming.propertyRequest.connector=liberty-kafka
mp.messaging.incoming.propertyRequest.topic=request.system.property
mp.messaging.incoming.propertyRequest.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.propertyRequest.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.propertyRequest.group.id=property-name
```
{: codeblock}


Replace the **system** microservice **microprofile-config.properties** file to add the two new
**propertyRequest** and **propertyResponse**
channels. The **propertyRequest** channel handles receiving the property request, and the
**propertyResponse** channel handles sending the property response.

# **Building and running the application**

Build the **system** and **inventory** microservices using Maven and then run them in Docker containers.

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


Next, use the provided script to start the application in Docker containers. The script creates a network for the
containers to communicate with each other. It also creates containers for Kafka, Zookeeper, and the microservices in the
project. For simplicity, the script starts one instance of the **system** service.


```
./scripts/startContainers.sh
```
{: codeblock}



# **Testing the application**

After the application is up and running, you can access the application by making a GET request to the **/systems** endpoint
of the **inventory** service.


Run the following curl command to access the  **inventory** microservice:
```
curl -s http://localhost:9085/inventory/systems | jq
```
{: codeblock}

You see the CPU **systemLoad** property for all the systems:

```
{
   "hostname":"30bec2b63a96",   
   "systemLoad":1.44
}
```


You can revisit the **inventory** service after a while by running the following curl command:
```
curl -s http://localhost:9085/inventory/systems | jq
```
{: codeblock}

Notice the value of the **systemLoad** property for the systems is changed.

Make a **PUT** request on the **http://localhost:9085/inventory/data** URL to add the value of a particular system
property to the set of existing properties. For example, run the following **curl** command:


```
curl -X PUT -d "os.name" http://localhost:9085/inventory/data --header "Content-Type:text/plain"
```
{: codeblock}



In this example, the **PUT** request with the **os.name** system property in the request body on the **http://localhost:9085/inventory/data**
URL adds the **os.name** system property for your system.

You see the following output:

```
Request successful for the os.name property
```

The **system** service is available so the request to the service is successful and returns a **200** response code.


You can revisit the **inventory** service by running the following curl command:
```
curl -s http://localhost:9085/inventory/systems | jq
```
{: codeblock}

Notice that the **os.name** system property value is now included with the previous values:

```
{
   "hostname":"30bec2b63a96",
   "os.name":"Linux",
   "systemLoad":1.44
}
```

# **Tearing down the environment**

Run the following script to stop the application:


```
./scripts/stopContainers.sh
```
{: codeblock}



# **Running multiple system instances**


This application has only one instance of the **system** service. The **inventory** service collects system properties of
all **system** services in the application. As an exercise, start multiple **system** services to see how the application
handles it. When you start the **system** instances, you must provide a unique **group.id**
through the **`MP_MESSAGING_INCOMING_PROPERTYREQUEST_GROUP_ID`** environment variable.

# **Summary**

## **Nice Work!**

You successfully integrated a RESTful microservice with a reactive system by using MicroProfile Reactive Messaging.



<br/>
## **Clean up your environment**


Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-microprofile-reactive-messaging-rest-integration** project by running the following commands:

```
cd /home/project
rm -fr guide-microprofile-reactive-messaging-rest-integration
```
{: codeblock}

<br/>
## **What did you think of this guide?**

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Integrating%20RESTful%20services%20with%20a%20reactive%20system&guide-id=cloud-hosted-guide-microprofile-reactive-messaging-rest-integration)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

<br/>
## **What could make this guide better?**

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-reactive-messaging-rest-integration/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-reactive-messaging-rest-integration/pulls)



<br/>
## **Where to next?**

* [Testing reactive Java microservices](https://openliberty.io/guides/reactive-service-testing.html)
* [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html)


<br/>
## **Log out of the session**

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.