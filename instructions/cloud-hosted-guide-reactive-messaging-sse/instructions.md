---
markdown-version: v1
title: instructions
branch: lab-472-instruction
version-history-start-date: 2021-10-01 14:24:37 UTC
tool-type: theia
---
::page{title="Welcome to the Streaming updates to a client using Server-Sent Events guide!"}

Learn how to stream updates from a MicroProfile Reactive Messaging service to a front-end client by using Server-Sent Events (SSE).

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You will learn how to stream messages from a MicroProfile Reactive Messaging service to a front-end client by using Server-Sent Events (SSE).

MicroProfile Reactive Messaging provides an easy way for Java services to send requests to other Java services, and asynchronously receive and process the responses as a stream of events. SSE provides a framework to stream the data in these events to a browser client.

### What is SSE?

Server-Sent Events is an API that allows clients to subscribe to a stream of events that is pushed from a server. First, the client makes a connection with the server over HTTP. The server continuously pushes events to the client as long as the connection persists. SSE differs from traditional HTTP requests, which use one request for one response. SSE also differs from Web Sockets in that SSE is unidirectional from the server to the client, and Web Sockets allow for bidirectional communication.

For example, an application that provides real-time stock quotes might use SSE to push price updates from the server to the browser as soon as the server receives them. Such an application wouldn't need Web Sockets because the data travels in only one direction, and polling the server by using HTTP requests wouldn't provide real-time updates.

The application that you will build in this guide consists of a ***frontend*** service, a ***bff*** (backend for frontend) service, and three instances of a ***system*** service. The ***system*** services periodically publish messages that contain their hostname and current system load. The ***bff*** service receives the messages from the ***system*** services and pushes the contents as SSE to a JavaScript client in the ***frontend*** service. This client uses the events to update a table in the UI that displays each system's hostname and its periodically updating load. The following diagram depicts the application that is used in this guide:

![SSE Diagram](https://raw.githubusercontent.com/OpenLiberty/guide-reactive-messaging-sse/prod/assets/SSE_Diagram.png)


In this guide, you will set up the ***bff*** service by creating an endpoint that clients can use to subscribe to events. You will also enable the service to read from the reactive messaging channel and push the contents to subscribers via SSE. After that, you will configure the Kafka connectors to allow the ***bff*** service to receive messages from the ***system*** services. Finally, you will configure the client in the ***frontend*** service to subscribe to these events, consume them, and display them in the UI.

To learn more about the reactive Java services that are used in this guide, check out the [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html) guide.



::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-reactive-messaging-sse.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-reactive-messaging-sse.git
cd guide-reactive-messaging-sse
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.



::page{title="Setting up SSE in the bff service"}

In this section, you will create a REST API for SSE in the ***bff*** service. When a client makes a request to this endpoint, the initial connection between the client and server is established and the client is subscribed to receive events that are pushed from the server. Later in this guide, the client in the ***frontend*** service uses this endpoint to subscribe to the events that are pushed from the ***bff*** service.

Additionally, you will enable the ***bff*** service to read messages from the incoming stream and push the contents as events to subscribers via SSE.

Navigate to the ***start*** directory to begin.

Create the BFFResource class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-reactive-messaging-sse/start/bff/src/main/java/io/openliberty/guides/bff/BFFResource.java
```


> Then, to open the BFFResource.java file in your IDE, select
> **File** > **Open** > guide-reactive-messaging-sse/start/bff/src/main/java/io/openliberty/guides/bff/BFFResource.java, or click the following button

::openFile{path="/home/project/guide-reactive-messaging-sse/start/bff/src/main/java/io/openliberty/guides/bff/BFFResource.java"}



```java
package io.openliberty.guides.bff;

import io.openliberty.guides.models.SystemLoad;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/sse")
public class BFFResource {

    private Logger logger = Logger.getLogger(BFFResource.class.getName());

    private Sse sse;
    private SseBroadcaster broadcaster;

    @GET
    @Path("/")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void subscribeToSystem(
        @Context SseEventSink sink,
        @Context Sse sse
        ) {

        if (this.sse == null || this.broadcaster == null) { 
            this.sse = sse;
            this.broadcaster = sse.newBroadcaster();
        }
        
        this.broadcaster.register(sink);
        logger.info("New sink registered to broadcaster.");
    }

    private void broadcastData(String name, Object data) {
        if (broadcaster != null) {
            OutboundSseEvent event = sse.newEventBuilder()
                                        .name(name)
                                        .data(data.getClass(), data)
                                        .mediaType(MediaType.APPLICATION_JSON_TYPE)
                                        .build();
            broadcaster.broadcast(event);
        } else {
            logger.info("Unable to send SSE. Broadcaster context is not set up.");
        }
    }

    @Incoming("systemLoad")
    public void getSystemLoadMessage(SystemLoad sl)  {
        logger.info("Message received from system.load topic. " + sl.toString());
        broadcastData("systemLoad", sl);
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


### Creating the SSE API endpoint

The ***subscribeToSystem()*** method allows clients to subscribe to events via an HTTP ***GET*** request to the ***/bff/sse/*** endpoint. The ***@Produces(MediaType.SERVER_SENT_EVENTS)*** annotation sets the ***Content-Type*** in the response header to ***text/event-stream***. This content type indicates that client requests that are made to this endpoint are to receive Server-Sent Events. Additionally, the method parameters take in an instance of the ***SseEventSink*** class and the ***Sse*** class, both of which are injected using the ***@Context*** annotation. First, the method checks if the ***sse*** and ***broadcaster*** instance variables are assigned. If these variables aren't assigned, the ***sse*** variable is obtained from the ***@Context*** injection and the ***broadcaster*** variable is obtained by using the ***Sse.newBroadcaster()*** method. Then, the ***register()*** method is called to register the ***SseEventSink*** instance to the ***SseBroadcaster*** instance to subscribe to events.

For more information about these interfaces, see the Javadocs for [OutboundSseEvent](https://openliberty.io/docs/ref/javaee/8/#class=javax/ws/rs/sse/OutboundSseEvent.html&package=allclasses-frame.html) and [OutboundSseEvent.Builder](https://openliberty.io/docs/ref/javaee/8/#class=javax/ws/rs/sse/OutboundSseEvent.Builder.html&package=allclasses-frame.html).

### Reading from the reactive messaging channel

The ***getSystemLoadMessage()*** method receives the message that contains the hostname and the average system load. The ***@Incoming("systemLoad")*** annotation indicates that the method retrieves the message by connecting to the ***systemLoad*** channel in Kafka, which you configure in the next section.

Each time a message is received, the ***getSystemLoadMessage()*** method is called, and the hostname and system load contained in that message are broadcasted in an event to all subscribers.

### Broadcasting events

Broadcasting events is handled in the ***broadcastData()*** method. First, it checks whether the ***broadcaster*** value is ***null***. The ***broadcaster*** value must include at least one subscriber or there's no client to send the event to. If the ***broadcaster*** value is specified, the ***OutboundSseEvent*** interface is created by using the ***Sse.newEventBuilder()*** method, where the ***name*** of the event, the ***data*** it contains, and the ***mediaType*** are set. The ***OutboundSseEvent*** interface is then broadcasted, or sent to all registered sinks, by invoking the ***SseBroadcaster.broadcast()*** method.


You just set up an endpoint in the ***bff*** service that the client in the ***frontend*** service can use to subscribe to events. You also enabled the service to read from the reactive messaging channel and broadcast the information as events to subscribers via SSE.


::page{title="Configuring the Kafka connector for the bff service"}

A complete ***system*** service is provided for you in the ***start/system*** directory. The ***system*** service is the producer of the messages that are published to the Kafka messaging system. The periodically published messages contain the system's hostname and a calculation of the average system load (its CPU usage) for the last minute.

Configure the Kafka connector in the ***bff*** service to receive the messages from the ***system*** service.

Create the microprofile-config.properties file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-reactive-messaging-sse/start/bff/src/main/resources/META-INF/microprofile-config.properties
```


> Then, to open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > guide-reactive-messaging-sse/start/bff/src/main/resources/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/guide-reactive-messaging-sse/start/bff/src/main/resources/META-INF/microprofile-config.properties"}



```
mp.messaging.connector.liberty-kafka.bootstrap.servers=localhost:9093

mp.messaging.incoming.systemLoad.connector=liberty-kafka
mp.messaging.incoming.systemLoad.topic=system.load
mp.messaging.incoming.systemLoad.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.systemLoad.value.deserializer=io.openliberty.guides.models.SystemLoad$SystemLoadDeserializer
mp.messaging.incoming.systemLoad.group.id=bff
```



The ***bff*** service uses an incoming connector to receive messages through the ***systemLoad*** channel. The messages are then published by the ***system*** service to the ***system.load***  topic in the Kafka message broker. The ***key.deserializer*** and ***value.deserializer*** properties define how to deserialize the messages. The ***group.id*** property defines a unique name for the consumer group. All of these properties are required by the [Apache Kafka Consumer Configs](https://kafka.apache.org/documentation/#consumerconfigs) documentation.



::page{title="Configuring the frontend service to subscribe to and consume events"}

In this section, you will configure the client in the ***frontend*** service to subscribe to events and display their contents in a table in the UI.

The front-end UI is a table where each row contains the hostname and load of one of the three ***system*** services. The HTML and styling for the UI is provided for you but you must populate the table with information that is received from the Server-Sent Events.

Create the index.js file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-reactive-messaging-sse/start/frontend/src/main/webapp/js/index.js
```


> Then, to open the index.js file in your IDE, select
> **File** > **Open** > guide-reactive-messaging-sse/start/frontend/src/main/webapp/js/index.js, or click the following button

::openFile{path="/home/project/guide-reactive-messaging-sse/start/frontend/src/main/webapp/js/index.js"}



```javascript
function initSSE() {
    var source = new EventSource('http://localhost:9084/bff/sse', { withCredentials: true });
    source.addEventListener(
        'systemLoad',
        systemLoadHandler
    );
}

function systemLoadHandler(event) {
    var system = JSON.parse(event.data);
    if (document.getElementById(system.hostname)) {
        document.getElementById(system.hostname).cells[1].innerHTML =
                                        system.loadAverage.toFixed(2);
    } else {
        var tableRow = document.createElement('tr');
        tableRow.id = system.hostname;
        tableRow.innerHTML = '<td>' + system.hostname + '</td><td>'
                             + system.loadAverage.toFixed(2) + '</td>';
        document.getElementById('sysPropertiesTableBody').appendChild(tableRow);
    }
}


```



### Subscribing to SSE

The ***initSSE()*** method is called when the page first loads. This method subscribes the client to the SSE by creating a new instance of the ***EventSource*** interface and specifying the ***http://localhost:9084/bff/sse*** URL in the parameters. To connect to the server, the ***EventSource*** interface makes a ***GET*** request to this endpoint with a request header of ***Accept: text/event-stream***.

In this IBM cloud environment, you need to update the ***EventSource*** URL with the ***bff*** service domain instead of ***localhost***. Run the following command:
```bash
BFF_DOMAIN=${USERNAME}-9084.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')
sed -i 's=localhost:9084='"$BFF_DOMAIN"'=g' /home/project/guide-reactive-messaging-sse/start/frontend/src/main/webapp/js/index.js
```



Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.

Because this request comes from ***localhost:9080*** and is made to ***localhost:9084***, it must follow the Cross-Origin Resource Sharing (CORS) specification to avoid being blocked by the browser. To enable CORS for the client, set the ***withCredentials*** configuration element to true in the parameters of the ***EventSource*** interface. CORS is already enabled for you in the ***bff*** service. To learn more about CORS, check out the [CORS guide](https://openliberty.io/guides/cors.html).


### Consuming the SSE

The ***EventSource.addEventListener()*** method is called to add an event listener. This event listener listens for events with the name of ***systemLoad***. The ***systemLoadHandler()*** function is set as the handler function, and each time an event is received, this function is called. The ***systemLoadHandler()*** function will take the event object and parse the event's data property from a JSON string into a JavaScript object. The contents of this object are used to update the table with the system hostname and load. If a system is already present in the table, the load is updated, otherwise a new row is added for the system.


::page{title="Building and running the application"}

To build the application, navigate to the ***start*** directory and run the following Maven ***install*** and ***package*** goals from the command line:

```bash
cd /home/project/guide-reactive-messaging-sse/start
mvn -pl models install
mvn package
```


Run the following commands to containerize the ***frontend***, ***bff***, and ***system*** services:

```bash
docker build -t frontend:1.0-SNAPSHOT frontend/.
docker build -t bff:1.0-SNAPSHOT bff/.
docker build -t system:1.0-SNAPSHOT system/.
```

Next, use the following ***startContainers.sh*** script to start the application in Docker containers:



```bash
./scripts/startContainers.sh
```
This script creates a network for the containers to communicate with each other. It also creates containers for Kafka, Zookeeper, the ***frontend*** service, the ***bff*** service , and three instances of the ***system*** service.


The application might take some time to get ready. Run the following command to confirm that the ***bff*** microservice is up and running:
```bash
curl -s http://localhost:9084/health | jq
```

Once your application is up and running, use the following command to get the URL. Open your browser and check out your ***front*** service by going to the URL that the command returns.
```bash
echo http://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')
```

The latest version of most modern web browsers supports Server-Sent Events. The exception is Internet Explorer, which does not support SSE. When you visit the URL, look for a table similar to the following example:

![System table](https://raw.githubusercontent.com/OpenLiberty/guide-reactive-messaging-sse/prod/assets/system_table.png)


The table contains three rows, one for each of the running ***system*** containers. If you can see the loads updating, you know that your ***bff*** service is successfully receiving messages and broadcasting them as SSE to the client in the ***frontend*** service.


::page{title="Tearing down the environment"}

Run the following script to stop the application:


```bash
./scripts/stopContainers.sh
```

::page{title="Summary"}

### Nice Work!

You developed an application that subscribes to Server-Sent Events by using MicroProfile Reactive Messaging, Open Liberty, and Kafka.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-reactive-messaging-sse*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-reactive-messaging-sse
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Streaming%20updates%20to%20a%20client%20using%20Server-Sent%20Events&guide-id=cloud-hosted-guide-reactive-messaging-sse)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-reactive-messaging-sse/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-reactive-messaging-sse/pulls)



### Where to next?

* [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html)
* [Acknowledging messages using MicroProfile Reactive Messaging](https://openliberty.io/guides/microprofile-reactive-messaging-acknowledgment.html)
* [Integrating RESTful services with a reactive system](https://openliberty.io/guides/microprofile-reactive-messaging-rest-integration.html)
* [Testing reactive Java microservices](https://openliberty.io/guides/reactive-service-testing.html)
* [Containerizing microservices](https://openliberty.io/guides/containerize.html)

**Learn more about MicroProfile**
* [See the MicroProfile specs](https://microprofile.io/)
* [View the MicroProfile API](https://openliberty.io/docs/ref/microprofile)
* [View the MicroProfile Reactive Messaging Specification](https://download.eclipse.org/microprofile/microprofile-reactive-messaging-1.0/microprofile-reactive-messaging-spec.html#_microprofile_reactive_messaging)
* [View the JAX-RS Server-Sent Events API](https://openliberty.io/docs/ref/javaee/8/#package=javax/ws/rs/sse/package-frame.html&class=javax/ws/rs/sse/package-summary.html)
* [View the Server-Sent Events HTML Specification](https://html.spec.whatwg.org/multipage/server-sent-events.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
