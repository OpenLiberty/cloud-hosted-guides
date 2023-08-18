---
markdown-version: v1
title: cloud-hosted-guide-jakarta-websocket
branch: lab-3446-instruction
version-history-start-date: 2023-01-05T10:56:36Z
tool-type: theia
---
::page{title="Welcome to the Bidirectional communication between services using Jakarta WebSocket guide!"}

Learn how to use Jakarta WebSocket to send and receive messages between services without closing the connection.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

Jakarta WebSocket enables two-way communication between client and server endpoints. First, each client makes an HTTP connection to a Jakarta WebSocket server. The server can then broadcast messages to the clients. link:[Server-Sent Events (SSE)](link:https://openliberty.io/guides/reactive-messaging-sse.html) also enables a client to receive automatic updates from a server via an HTTP connection however WebSocket differs from Server-Sent Events in that SSE is unidirectional from server to client, whereas WebSocket is bidirectional. WebSocket also enables real-time updates over a smaller bandwidth than SSE. The connection isn't closed meaning that the client can continue to send and receive messages with the server, without having to poll the server to receive any replies.

The application that you will build in this guide consists of the ***client*** service and the ***system*** server service. The following diagram depicts the application that is used in this guide. 

![Application architecture where system and client services use the Jakarta Websocket API to connect and communicate.](https://raw.githubusercontent.com/OpenLiberty/guide-jakarta-websocket/prod/assets/architecture.png)


You'll learn how to use the link:[Jakarta WebSocket API](link:https://openliberty.io/docs/latest/reference/javadoc/liberty-jakartaee9.1-javadoc.html?package=jakarta/websocket/package-frame.html&class=overview-summary.html) to build the ***system*** service and the scheduler in the ***client*** service. The scheduler pushes messages to the system service every 10 seconds, then the system service broadcasts the messages to any connected clients. You will also learn how to use a JavaScript ***WebSocket*** object in an HTML file to build a WebSocket connection, subscribe to different events, and display the broadcasting messages from the ***system*** service in a table.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-jakarta-websocket.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-jakarta-websocket.git
cd guide-jakarta-websocket
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed. 

To try out the application, go to the finish directory and run the following Maven goal to build the ***system*** service and deploy it to Open Liberty:

```bash
mvn -pl system liberty:run

```

Next, open another command-line session and run the following command to start the ***client*** service:

```bash
mvn -pl client liberty:run
```

After you see the following message in both command-line sessions, both your services are ready.

```
The defaultServer is ready to run a smarter planet. 
```

Check out the service at the ***http\://localhost:9080*** URL. See that the table is being updated for every 10 seconds. 

After you are finished checking out the application, stop both the ***system*** and ***client*** services by pressing `Ctrl+C` in the command-line sessions where you ran them. Alternatively, you can run the following goals from the ***finish*** directory in another command-line session:

```bash
mvn -pl system liberty:stop
mvn -pl client liberty:stop
```
 

::page{title="Creating the WebSocket server service"}

In this section, you will create the ***system*** WebSocket server service that broadcasts messages to clients.

Navigate to the ***start*** directory to begin.

```bash
cd /home/project/guide-jakarta-websocket/start
```

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), dev mode listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following command to start the ***system*** service in dev mode:

```bash
mvn -pl system liberty:dev
```

After you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************
*     Liberty is running in dev mode.
```

The ***system*** service is responsible for handling the messages produced by the ***client*** scheduler, building system load messages, and forwarding them to clients.

Create the SystemService class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jakarta-websocket/start/system/src/main/java/io/openliberty/guides/system/SystemService.java
```


> Then, to open the SystemService.java file in your IDE, select
> **File** > **Open** > guide-jakarta-websocket/start/system/src/main/java/io/openliberty/guides/system/SystemService.java, or click the following button

::openFile{path="/home/project/guide-jakarta-websocket/start/system/src/main/java/io/openliberty/guides/system/SystemService.java"}



```java
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/systemLoad",
                decoders = { SystemLoadDecoder.class },
                encoders = { SystemLoadEncoder.class })
public class SystemService {

    private static Logger logger = Logger.getLogger(SystemService.class.getName());

    private static Set<Session> sessions = new HashSet<>();

    private static final OperatingSystemMXBean OS =
        ManagementFactory.getOperatingSystemMXBean();

    private static final MemoryMXBean MEM =
        ManagementFactory.getMemoryMXBean();

    public static void sendToAllSessions(JsonObject systemLoad) {
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendObject(systemLoad);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        logger.info("Server connected to session: " + session.getId());
        sessions.add(session);
    }

    @OnMessage
    public void onMessage(String option, Session session) {
        logger.info("Server received message \"" + option + "\" "
                    + "from session: " + session.getId());
        try {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("time", Calendar.getInstance().getTime().toString());
            if (option.equalsIgnoreCase("loadAverage")
                || option.equalsIgnoreCase("both")) {
                builder.add("loadAverage", Double.valueOf(OS.getSystemLoadAverage()));
            }
            if (option.equalsIgnoreCase("memoryUsage")
                || option.equalsIgnoreCase("both")) {
                long heapMax = MEM.getHeapMemoryUsage().getMax();
                long heapUsed = MEM.getHeapMemoryUsage().getUsed();
                builder.add("memoryUsage", Double.valueOf(heapUsed * 100.0 / heapMax));
            }
            JsonObject systemLoad = builder.build();
            sendToAllSessions(systemLoad);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info("Session " + session.getId()
                    + " was closed with reason " + closeReason.getCloseCode());
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.info("WebSocket error for " + session.getId() + " "
                    + throwable.getMessage());
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


Annotate the ***SystemService*** class with a ***@ServerEndpoint*** annotation to make it a WebSocket server. The ***@ServerEndpoint***  ***value*** attribute specifies the URI where the endpoint will be deployed. The ***encoders*** attribute specifies the classes to encode messages and the ***decoders*** attribute specifies the classes to decode messages. Provide methods that define the parts of the WebSocket lifecycle like establishing a connection, receiving a message, and closing the connection by annotating them with the ***@OnOpen***, ***@OnMessage*** and ***@OnClose*** annotations respectively. The method that is annotated with the ***@OnError*** annotation is responsible for tackling errors.

The ***onOpen()*** method stores up the client sessions. The ***onClose()*** method displays the reason for closing the connection and removes the closing session from the client sessions.

The ***onMessage()*** method is called when receiving a message through the ***option*** parameter. The ***option*** parameter signifies which message to construct, either system load, memory usage data, or both, and sends out the ***JsonObject*** message. The ***sendToAllSessions()*** method uses the WebSocket API to broadcast the message to all client sessions.

Create the SystemLoadEncoder class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jakarta-websocket/start/system/src/main/java/io/openliberty/guides/system/SystemLoadEncoder.java
```


> Then, to open the SystemLoadEncoder.java file in your IDE, select
> **File** > **Open** > guide-jakarta-websocket/start/system/src/main/java/io/openliberty/guides/system/SystemLoadEncoder.java, or click the following button

::openFile{path="/home/project/guide-jakarta-websocket/start/system/src/main/java/io/openliberty/guides/system/SystemLoadEncoder.java"}



```java
package io.openliberty.guides.system;

import jakarta.json.JsonObject;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

public class SystemLoadEncoder implements Encoder.Text<JsonObject> {

    @Override
    public String encode(JsonObject object) throws EncodeException {
        return object.toString();
    }
}
```



The ***SystemLoadEncoder*** class implements the ***Encoder.Text*** interface. Override the ***encode()*** method that accepts the ***JsonObject*** message and converts the message to a string.

Create the SystemLoadDecoder class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jakarta-websocket/start/system/src/main/java/io/openliberty/guides/system/SystemLoadDecoder.java
```


> Then, to open the SystemLoadDecoder.java file in your IDE, select
> **File** > **Open** > guide-jakarta-websocket/start/system/src/main/java/io/openliberty/guides/system/SystemLoadDecoder.java, or click the following button

::openFile{path="/home/project/guide-jakarta-websocket/start/system/src/main/java/io/openliberty/guides/system/SystemLoadDecoder.java"}



```java
package io.openliberty.guides.system;

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;

public class SystemLoadDecoder implements Decoder.Text<JsonObject> {

    @Override
    public JsonObject decode(String s) throws DecodeException {
        try (JsonReader reader = Json.createReader(new StringReader(s))) {
            return reader.readObject();
        } catch (Exception e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("error", e.getMessage())
                    .build();
            return error;
        }
    }

    @Override
    public boolean willDecode(String s) {
        try (JsonReader reader = Json.createReader(new StringReader(s))) {
            reader.readObject();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
```



The ***SystemLoadDecoder*** class implements the ***Decoder.Text*** interface.
Override the ***decode()*** method that accepts string message and decodes the string back into a ***JsonObject***. The ***willDecode()*** override method checks out whether the string can be decoded into a JSON object and returns a Boolean value.


The required ***websocket*** and ***jsonb*** features for the ***system*** service have been enabled for you in the Liberty ***server.xml*** configuration file.


::page{title="Creating the client service"}

In this section, you will create the WebSocket client that communicates with the WebSocket server and the scheduler that uses the WebSocket client to send messages to the server. You'll also create an HTML file that uses a JavaScript ***WebSocket*** object to build a WebSocket connection, subscribe to different events, and display the broadcasting messages from the ***system*** service in a table.

On another command-line session, navigate to the ***start*** directory and run the following goal to start the ***client*** service in dev mode:

```bash
mvn -pl client liberty:dev
```

After you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************
*     Liberty is running in dev mode.
```

Create the SystemClient class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jakarta-websocket/start/client/src/main/java/io/openliberty/guides/client/scheduler/SystemClient.java
```


> Then, to open the SystemClient.java file in your IDE, select
> **File** > **Open** > guide-jakarta-websocket/start/client/src/main/java/io/openliberty/guides/client/scheduler/SystemClient.java, or click the following button

::openFile{path="/home/project/guide-jakarta-websocket/start/client/src/main/java/io/openliberty/guides/client/scheduler/SystemClient.java"}



```java
package io.openliberty.guides.client.scheduler;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

@ClientEndpoint()
public class SystemClient {

    private static Logger logger = Logger.getLogger(SystemClient.class.getName());

    private Session session;

    public SystemClient(URI endpoint) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpoint);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        logger.info("Scheduler connected to the server.");
    }

    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
        logger.info("Scheduler received message from the server: " + message);
    }

    public void sendMessage(String message) {
        session.getAsyncRemote().sendText(message);
        logger.info("Scheduler sent message \"" + message + "\" to the server.");
    }

    public void close() {
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Scheduler closed the session.");
    }

}
```



Annotate the ***SystemClient*** class with ***@ClientEndpoint*** annotation to make it as a WebSocket client. Create a constructor that uses the ***websocket*** APIs to establish connection with the server. Provide a method with the ***@OnOpen*** annotation that persists the client session when the connection is established. The ***onMessage()*** method that is annotated with the ***@OnMessage*** annotation handles messages from the server.

Create the SystemLoadScheduler class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jakarta-websocket/start/client/src/main/java/io/openliberty/guides/client/scheduler/SystemLoadScheduler.java
```


> Then, to open the SystemLoadScheduler.java file in your IDE, select
> **File** > **Open** > guide-jakarta-websocket/start/client/src/main/java/io/openliberty/guides/client/scheduler/SystemLoadScheduler.java, or click the following button

::openFile{path="/home/project/guide-jakarta-websocket/start/client/src/main/java/io/openliberty/guides/client/scheduler/SystemLoadScheduler.java"}



```java
package io.openliberty.guides.client.scheduler;

import java.net.URI;
import java.util.Random;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;

@Singleton
public class SystemLoadScheduler {

    private SystemClient client;
    private static final String[] MESSAGES = new String[] {
        "loadAverage", "memoryUsage", "both" };

    @PostConstruct
    public void init() {
        try {
            client = new SystemClient(new URI("ws://localhost:9081/systemLoad"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    public void sendSystemLoad() {
        Random r = new Random();
        client.sendMessage(MESSAGES[r.nextInt(MESSAGES.length)]);
    }

    @PreDestroy
    public void close() {
        client.close();
    }
}
```





Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.

The ***SystemLoadScheduler*** class uses the ***SystemClient*** class to establish a connection to the server by the ***ws://localhost:9081/systemLoad*** URI at the ***@PostConstruct*** annotated method. The ***sendSystemLoad()*** method calls the client to send a random string from either ***loadAverage***, ***memoryUsage***, or ***both*** to the ***system*** service. Using the link:[Jakarta Enterprise Beans Timer Service](link:https://openliberty.io/docs/latest/reference/javadoc/liberty-jakartaee9.1-javadoc.html?package=jakarta/ejb/package-frame.html&class=jakarta/ejb/TimerService.html), annotate the ***sendSystemLoad()*** method with the ***@Schedule*** annotation so that it sends out a message every 10 seconds.

Now, create the front-end UI. The images and styles for the UI are provided for you. 

Create the index.html file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jakarta-websocket/start/client/src/main/webapp/index.html
```


> Then, to open the index.html file in your IDE, select
> **File** > **Open** > guide-jakarta-websocket/start/client/src/main/webapp/index.html, or click the following button

::openFile{path="/home/project/guide-jakarta-websocket/start/client/src/main/webapp/index.html"}



```html
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Open Liberty System Load</title>
        <link href="https://fonts.googleapis.com/css?family=Asap" rel="stylesheet">
        <link rel="stylesheet" href="css/styles.css">
        <link href="favicon.ico" rel="icon" />
        <link href="favicon.ico" rel="shortcut icon" />
    </head>
    <body>
        <section id="appIntro">
            <div id="titleSection">
                <h1 id="appTitle">Open Liberty System Load</h1>
                <div class="line"></div>
                <div class="headerImage"></div>
            </div>

            <div class="msSection" id="systemLoads">
                <div class="headerRow">
                    <div class="headerIcon">
                      <img src="img/sysProps.svg"/>
                    </div>
                    <div class="headerTitle" id="sysPropTitle">
                      <h2>System Loads</h2>
                    </div>
                </div>
                <div class="sectionContent">
                    <table id="systemLoadsTable">
                        <tbody id="systemLoadsTableBody">
                            <tr>
                                <th>Time</th><th>System Load</th>
                                <th>Memory Usage (%)</th>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
        <footer class="bodyFooter">
            <div class="bodyFooterLink">
                <a id="licenseLink"
                   href="https://github.com/OpenLiberty/open-liberty/blob/release/LICENSE"
                >License</a>
                <a href="https://twitter.com/OpenLibertyIO">Twitter</a>
                <a href="https://github.com/OpenLiberty">GitHub</a>
                <a href="https://openliberty.io/">openliberty.io</a>
            </div>
            <p id="footer_text">an IBM open source project</p>
            <p id="footer_copyright">&copy;Copyright IBM Corp. 2022, 2023</p>
        </footer>
        <script>
    const webSocket = new WebSocket('ws://localhost:9081/systemLoad')

    webSocket.onopen = function (event) {
        console.log(event);
    };

    webSocket.onmessage = function (event) {
        var data = JSON.parse(event.data);
        var tableRow = document.createElement('tr');
        var loadAverage = data.loadAverage == null ? '-' : data.loadAverage.toFixed(2);
        var memoryUsage = data.memoryUsage == null ? '-' : data.memoryUsage.toFixed(2);
        tableRow.innerHTML = '<td>' + data.time + '</td>' +
                             '<td>' + loadAverage + '</td>' +
                             '<td>' + memoryUsage + '</td>';
        document.getElementById('systemLoadsTableBody').appendChild(tableRow);
    };
    
    webSocket.onerror = function (event) {
        console.log(event);
    };
        </script>
    </body>
</html>
```



The ***index.html*** front-end UI displays a table in which each row contains a time, system load, and the memory usage of the ***system*** service. Use a JavaScript ***WebSocket*** object to establish a connection to the server by the ***ws://localhost:9081/systemLoad*** URI. The ***webSocket.onopen*** event is triggered when the connection is established. The ***webSocket.onmessage*** event receives messages from the server and inserts a row with the data from the message into the table. The ***webSocket.onerror*** event defines how to tackle errors.


The required features for the ***client*** service are enabled for you in the Liberty ***server.xml*** configuration file.


::page{title="Running the application"}

Because you are running the ***system*** and ***client*** services in dev mode, the changes that you made are automatically picked up. You're now ready to check out your application in your browser.

Point your browser to the ***http\://localhost:9080*** URL to test out the ***client*** service. Notice that the table is updated every 10 seconds.

Visit the ***http\://localhost:9080*** URL again on a different tab or browser and verify that both sessions are updated every 10 seconds.


::page{title="Testing the application"}

Create the SystemClient class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jakarta-websocket/start/system/src/test/java/it/io/openliberty/guides/system/SystemClient.java
```


> Then, to open the SystemClient.java file in your IDE, select
> **File** > **Open** > guide-jakarta-websocket/start/system/src/test/java/it/io/openliberty/guides/system/SystemClient.java, or click the following button

::openFile{path="/home/project/guide-jakarta-websocket/start/system/src/test/java/it/io/openliberty/guides/system/SystemClient.java"}



```java
package it.io.openliberty.guides.system;

import java.net.URI;

import io.openliberty.guides.system.SystemLoadDecoder;
import jakarta.json.JsonObject;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

@ClientEndpoint()
public class SystemClient {

    private Session session;

    public SystemClient(URI endpoint) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpoint);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message, Session userSession) throws Exception {
        SystemLoadDecoder decoder = new SystemLoadDecoder();
        JsonObject systemLoad = decoder.decode(message);
        SystemServiceIT.verify(systemLoad);
    }

    public void sendMessage(String message) {
        session.getAsyncRemote().sendText(message);
    }

    public void close() throws Exception {
        session.close();
    }

}
```



The ***SystemClient*** class is used to communicate and test the ***system*** service. Its implementation is similar to the client class from the ***client*** service that you created in the previous section. At the ***onMessage()*** method, decode and verify the message. 

Create the SystemServiceIT class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jakarta-websocket/start/system/src/test/java/it/io/openliberty/guides/system/SystemServiceIT.java
```


> Then, to open the SystemServiceIT.java file in your IDE, select
> **File** > **Open** > guide-jakarta-websocket/start/system/src/test/java/it/io/openliberty/guides/system/SystemServiceIT.java, or click the following button

::openFile{path="/home/project/guide-jakarta-websocket/start/system/src/test/java/it/io/openliberty/guides/system/SystemServiceIT.java"}



```java
package it.io.openliberty.guides.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.json.JsonObject;

@TestMethodOrder(OrderAnnotation.class)
public class SystemServiceIT {

    private static CountDownLatch countDown;

    @Test
    @Order(1)
    public void testSystem() throws Exception {
        startCountDown(1);
        URI uri = new URI("ws://localhost:9081/systemLoad");
        SystemClient client = new SystemClient(uri);
        client.sendMessage("both");
        countDown.await(5, TimeUnit.SECONDS);
        client.close();
        assertEquals(0, countDown.getCount(),
                "The countDown was not 0.");
    }

    @Test
    @Order(2)
    public void testSystemMultipleSessions() throws Exception {
        startCountDown(3);
        URI uri = new URI("ws://localhost:9081/systemLoad");
        SystemClient client1 = new SystemClient(uri);
        SystemClient client2 = new SystemClient(uri);
        SystemClient client3 = new SystemClient(uri);
        client2.sendMessage("loadAverage");
        countDown.await(5, TimeUnit.SECONDS);
        client1.close();
        client2.close();
        client3.close();
        assertEquals(0, countDown.getCount(),
            "The countDown was not 0.");
    }

    private static void startCountDown(int count) {
        countDown = new CountDownLatch(count);
    }

    public static void verify(JsonObject systemLoad) {
        assertNotNull(systemLoad.getString("time"));
        assertTrue(
            systemLoad.getJsonNumber("loadAverage") != null
            || systemLoad.getJsonNumber("memoryUsage") != null
        );
        countDown.countDown();
    }
}
```



There are two test cases to ensure correct functionality of the ***system*** service. The ***testSystem()*** method verifies one client connection and the ***testSystemMultipleSessions()*** method verifies multiple client connections. 

### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started the ***system*** service.

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemServiceIT
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.247 s - in it.io.openliberty.guides.system.SystemServiceIT

Results:

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

When you are done checking out the services, exit dev mode by pressing `Ctrl+C` in the command-line sessions where you ran the ***system*** and ***client*** services, or by typing ***q*** and then pressing the ***enter/return*** key. Alternatively, you can run the ***liberty:stop*** goal from the ***start*** directory in another command-line session for the ***system*** and ***client*** services:
```bash
cd /home/project/guide-jakarta-websocket/start
mvn -pl system liberty:stop
mvn -pl client liberty:stop
```


::page{title="Summary"}

### Nice Work!

You developed an application that subscribes to real time updates by using Jakarta WebSocket and Open Liberty.




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-jakarta-websocket*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-jakarta-websocket
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Bidirectional%20communication%20between%20services%20using%20Jakarta%20WebSocket&guide-id=cloud-hosted-guide-jakarta-websocket)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-jakarta-websocket/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-jakarta-websocket/pulls)



### Where to next?

* [Streaming messages between client and server services using gRPC](https://openliberty.io/guides/grpc-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
