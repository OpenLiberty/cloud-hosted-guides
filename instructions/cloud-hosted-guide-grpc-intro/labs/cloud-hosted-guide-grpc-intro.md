---
markdown-version: v1
title: cloud-hosted-guide-grpc-intro
branch: lab-1145-instruction
version-history-start-date: 2022-08-11T09:54:57Z
tool-type: theia
---
::page{title="Welcome to the Streaming messages between client and server services using gRPC guide!"}

Learn how to use gRPC unary calls, server streaming, client streaming, and bidirectional streaming to communicate between Java client and server services with Open Liberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What is gRPC?"}

The [gRPC](https://grpc.io/) Remote Procedure Call is a technology that implements remote procedure call (RPC) style APIs with HTTP/2. Typically, gRPC uses [protocol buffers](https://developers.google.com/protocol-buffers/docs/reference/overview) to define the format of data to be transferred and the service interfaces to access it, which include service calls and expected messages. For each service defined in a ***.proto*** file, gRPC uses the definition to generate the skeleton code for users to implement and extend. Protocol buffers use a binary format to send and receive messages that is faster and more lightweight than the JSON that is typically used in RESTful APIs.

Protocol buffers allow cross-project support through the ***.proto*** file. As a result, gRPC clients and servers can run and communicate with each other from different environments. For example, a gRPC client running on a Java virtual machine can call a gRPC server developed in any other [supported language](https://grpc.io/docs/languages/). This feature of protocol buffers allows for easier integration between services.

::page{title="What you'll learn"}

You will learn how to create gRPC services and their clients by using protocol buffers and how to implement them with Open Liberty. You will use Maven to generate the gRPC stubs, deploy the services, and to interact with the running Liberty runtime.

The application that you will build in this guide consists of three projects: the ***systemproto*** model project, the ***query*** client service, and the ***system*** server service.

The ***query*** service implements four RESTful APIs by using four different gRPC streaming methods.

* Unary RPC: The client sends a single request and receives a single response.
* Server streaming RPC: The client sends a single request and the server returns a stream of messages.
* Client streaming RPC: The client sends a stream of messages and the server responds with a single message.
* Bidirectional RPC: Both client and server send a stream of messages. The client and server can read and write messages in any order.

![Application architecture of the gRPC application covered in guide](https://raw.githubusercontent.com/OpenLiberty/guide-grpc-intro/prod/assets/architecture.png)


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-grpc-intro.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-grpc-intro.git
cd guide-grpc-intro
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the ***finish*** directory and run the following Maven goal to generate all the gRPC abstract classes defined in the ***.proto*** file. 

```bash
cd finish
mvn -pl systemproto install
```

Start the ***system*** service by running the following command:
```bash
mvn -pl system liberty:run
```

Next, open another command-line session, navigate to the ***finish*** directory, and start the ***query*** service by using the following command:
```bash
mvn -pl query liberty:run
```


Click the following button to visit the ***/query/properties/os.name*** endpoint to test out basic unary call. You will see your operating system name.  

::startApplication{port="9081" display="external" name="/query/properties/os.name" route="/query/properties/os.name"}

Next, click the following button to visit the ***/query/properties/os*** endpoint to test out server streaming call. The details of your localhost operating system are displayed.

::startApplication{port="9081" display="external" name="/query/properties/os" route="/query/properties/os"}

Visit the ***/query/properties/user*** endpoint to test out client streaming call. The details of your localhost user properties are displayed.  

::startApplication{port="9081" display="external" name="/query/properties/user" route="/query/properties/user"}

Visit the ***/query/properties/java*** endpoint to test out bidirectional streaming. The details of your localhost Java properties are displayed.

::startApplication{port="9081" display="external" name="/query/properties/java" route="/query/properties/java"}

Observe the output from the consoles running the ***system*** and ***query*** services.

After you are finished checking out the application, stop both the ***query*** and ***system*** services by pressing `Ctrl+C` in the command-line sessions where you ran them. Alternatively, you can run the following goals from the ***finish*** directory in another command-line session:
```bash
mvn -pl system liberty:stop
mvn -pl query liberty:stop
```


::page{title="Creating and defining the gRPC server service"}

Navigate to the ***start*** directory to begin.

```bash
cd /home/project/guide-grpc-intro/start
```

First, create the ***.proto*** file and generate gRPC classes. You will implement the gRPC server service with the generated classes later. The ***.proto*** file defines all the service calls and message types. The message types are used in the service call definition for the parameters and returns.

Create the ***SystemService.proto*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-grpc-intro/start/systemproto/src/main/proto/SystemService.proto
```


> Then, to open the SystemService.proto file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/systemproto/src/main/proto/SystemService.proto, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/systemproto/src/main/proto/SystemService.proto"}



```

syntax = "proto3";
package io.openliberty.guides.systemproto;
option java_multiple_files = true;

service SystemService {
  rpc getProperty (SystemPropertyName) returns (SystemPropertyValue) {}

  rpc getServerStreamingProperties (SystemPropertyPrefix) returns (stream SystemProperty) {}

  rpc getClientStreamingProperties (stream SystemPropertyName) returns (SystemProperties) {}

  rpc getBidirectionalProperties (stream SystemPropertyName) returns (stream SystemProperty) {}
}

message SystemPropertyName {
    string propertyName = 1;
}

message SystemPropertyPrefix {
    string propertyPrefix = 1;
}

message SystemPropertyValue {
    string propertyValue = 1;
}

message SystemProperty {
    string propertyName = 1;
    string propertyValue = 2;
}

message SystemProperties {
    map<string, string> properties = 1;
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.



The first few lines define the ***syntax***, ***package***, and ***option*** basic configuration of the ***.proto*** file. The ***SystemService*** service contains the four service calls that you will implement in the coming sections.

The ***getProperty*** RPC defines the unary call. In this call, the client service sends a ***SystemPropertyName*** message to the server service, which returns a ***SystemPropertyValue*** message with the property value. The ***SystemPropertyName*** and ***SystemPropertyValue*** message types define that the ***propertyName*** and ***propertyValue*** fields must be string.

The ***getServerStreamingProperties*** RPC defines the server streaming call. The client service sends a ***SystemPropertyPrefix*** message to the server service. The server service returns a stream of ***SystemProperty*** messages. Each ***SystemProperty*** message contains ***propertyName*** and ***propertyValue*** strings.

The ***getClientStreamingProperties*** RPC defines the client streaming call. The client service streams ***SystemPropertyName*** messages to the server service. The server service returns a ***SystemProperties*** message that contains a map of the properties with their respective values.

The ***getBidirectionalProperties*** RPC defines the bidirectional streaming call. In this service, the client service streams ***SystemPropertyName*** messages to the server service. The server service returns a stream of ***SystemProperty*** messages.


To compile the ***.proto*** file, the ***pom.xml*** Maven configuration file needs the ***grpc-protobuf***, ***grpc-stub***, ***javax.annotation-api*** dependencies, and the ***protobuf-maven-plugin*** plugin. To install the correct version of the Protobuf compiler automatically, the ***os-maven-plugin*** extension is required in the ***build*** configuration.

Run the following command to generate the gRPC classes.
```bash
mvn -pl systemproto install
```


::page{title="Implementing the unary call"}

Navigate to the ***start*** directory.

```bash
cd /home/project/guide-grpc-intro/start
```

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), dev mode listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following command to start the ***system*** service in dev mode:

```bash
mvn -pl system liberty:dev
```

Open another command-line session, navigate to the ***start*** directory, and run the following command to start the ***query*** service in dev mode:

```bash
mvn -pl query liberty:dev
```

After you see the following message, your Liberty instances are ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session and navigate to the ***start*** directory to continue, or open the project in your editor.

Start by implementing the first service call, the unary call. In this service call, the ***query*** client service sends a property to the ***system*** server service, which returns the property value. This type of service call resembles a RESTful API. 

Create the ***SystemService*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java
```


> Then, to open the SystemService.java file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java"}



```java
package io.openliberty.guides.system;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyPrefix;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;

public class SystemService extends SystemServiceGrpc.SystemServiceImplBase {

    private static Logger logger = Logger.getLogger(SystemService.class.getName());

    public SystemService() {
    }

    @Override
    public void getProperty(
        SystemPropertyName request, StreamObserver<SystemPropertyValue> observer) {

        String pName = request.getPropertyName();
        String pValue = System.getProperty(pName);
        SystemPropertyValue value = SystemPropertyValue
                                        .newBuilder()
                                        .setPropertyValue(pValue)
                                        .build();

        observer.onNext(value);
        observer.onCompleted();

    }



}
```



The ***SystemService*** class extends the ***SystemServiceGrpc*** class that is generated by the ***.proto*** file. The four types of services defined in the proto file are implemented in this class.

The ***getProperty()*** method implements the unary RPC call defined in the ***.proto*** file. The ***getPropertyName()*** getter method that is generated by gRPC retrieves the property name from the client, and stores it into the ***pName*** variable. The System property value is stored into the ***pValue*** variable. The gRPC library will create a ***SystemPropertyValue*** message, with its type defined in the ***SystemService.proto*** file. Then, the message is sent to the client service through the ***StreamObserver*** by using its ***onNext()*** and ***onComplete()*** methods.

Replace the system's ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/system/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/system/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<server description="system service">

    <featureManager>
        <feature>restfulWS-3.1</feature>
        <feature>grpc-1.0</feature>
    </featureManager>

    <grpc target="*" maxInboundMessageSize="1024"/>

    <applicationManager autoExpand="true"/>

    <webApplication contextRoot="/" location="guide-grpc-intro-system.war"/>

    <logging consoleLogLevel="INFO"/>
</server>
```




Add the ***grpc*** feature to the Liberty ***server.xml*** configuration file. This feature enables applications running on Liberty to provide gRPC services. Configure the ***grpc*** element with the ***maxInboundMessageSize*** attribute to restrict inbound messages to 1024 bytes. This configuration applies universally to all gRPC services running on the server, as indicated by the wildcard (`*`) in the ***target*** attribute. If you want to learn more about configuration for the ***grpc*** element, see the [GRPC Server Properties](https://openliberty.io/docs/latest/reference/config/grpc.html).

Next, implement the corresponding REST endpoint in the ***query*** service.

Create the ***PropertiesResource*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java
```


> Then, to open the PropertiesResource.java file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java"}



```java
package io.openliberty.guides.query;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyPrefix;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceBlockingStub;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceStub;

@ApplicationScoped
@Path("/properties")
public class PropertiesResource {

    private static Logger logger = Logger.getLogger(PropertiesResource.class.getName());

    @Inject
    @ConfigProperty(name = "system.hostname", defaultValue = "localhost")
    String SYSTEM_HOST;

    @Inject
    @ConfigProperty(name = "system.port", defaultValue = "9080")
    int SYSTEM_PORT;

    @GET
    @Path("/{property}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPropertiesString(@PathParam("property") String property) {

        ManagedChannel channel = ManagedChannelBuilder
                                     .forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceBlockingStub client = SystemServiceGrpc.newBlockingStub(channel);
        SystemPropertyName request = SystemPropertyName.newBuilder()
                                             .setPropertyName(property).build();
        SystemPropertyValue response = client.getProperty(request);
        channel.shutdownNow();
        return response.getPropertyValue();
    }



}
```


The ***PropertiesResource*** class provides RESTful endpoints to interact with the ***system*** service. The ***/query/properties/${property}*** endpoint uses the unary service call to get the property value from the ***system*** service. The endpoint creates a ***channel***, which it uses to create a client by the ***SystemServiceGrpc.newBlockingStub()*** API. The endpoint then uses the client to get the property value, shuts down the channel, and immediately returns the value from the ***system*** service response.

Replace the query's ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/query/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/query/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<server description="query service">

    <featureManager>
        <feature>restfulWS-3.1</feature>
        <feature>jsonp-2.1</feature>
        <feature>jsonb-3.0</feature>
        <feature>cdi-4.0</feature>
        <feature>mpConfig-3.0</feature>
        <feature>grpcClient-1.0</feature>
    </featureManager>

    <variable defaultValue="9081" name="default.http.port"/>
    <variable defaultValue="9444" name="default.https.port"/>

    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="${default.http.port}"
                  httpsPort="${default.https.port}"
                  host="*"/>

    <grpcClient host="*" headersToPropagate="Cookie"/>

    <applicationManager autoExpand="true"/>

    <webApplication contextRoot="/" location="guide-grpc-intro-query.war"/>

    <logging consoleLogLevel="INFO"/>
</server>
```




Add the ***grpcClient*** feature to the Liberty ***server.xml*** configuration file for the ***query*** service. This feature enables gRPC client support on Liberty. Configure the ***grpcClient*** element with the ***headersToPropagate*** attribute to propagate cookies. This configuration applies universally to all gRPC client calls, as indicated by the wildcard (`*`) in the ***host*** attribute. If you want to learn more about ***grpcClient*** element configuration, see the [GRPC Client Properties](https://openliberty.io/docs/latest/reference/config/grpcClient.html).

Because you are running the ***system*** and ***query*** services in dev mode, the changes that you made are automatically picked up. You’re now ready to check out your application in your browser.

Click the following button to visit the ***/query/properties/os.name*** endpoint to test out the unary service call. Your operating system name is displayed. 

::startApplication{port="9081" display="external" name="/query/properties/os.name" route="/query/properties/os.name"}


::page{title="Implementing the server streaming call"}

In the server streaming call, the ***query*** client service provides the ***/query/properties/os*** endpoint that sends a message to the ***system*** server service. The ***system*** service streams any properties that start with ***os.*** back to the ***query*** service. A channel is created between the ***query*** and the ***system*** services to stream messages. The channel is closed by the ***system*** service only after sending the last message to the ***query*** service. 

Update the ***SystemService*** class to implement the server streaming RPC call.
Replace the ***SystemService*** class.

> To open the SystemService.java file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java"}



```java
package io.openliberty.guides.system;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyPrefix;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;

public class SystemService extends SystemServiceGrpc.SystemServiceImplBase {

    private static Logger logger = Logger.getLogger(SystemService.class.getName());

    public SystemService() {
    }

    @Override
    public void getProperty(
        SystemPropertyName request, StreamObserver<SystemPropertyValue> observer) {

        String pName = request.getPropertyName();
        String pValue = System.getProperty(pName);
        SystemPropertyValue value = SystemPropertyValue
                                        .newBuilder()
                                        .setPropertyValue(pValue)
                                        .build();

        observer.onNext(value);
        observer.onCompleted();

    }

    @Override
    public void getServerStreamingProperties(
        SystemPropertyPrefix request, StreamObserver<SystemProperty> observer) {

        String prefix = request.getPropertyPrefix();
        System.getProperties()
              .stringPropertyNames()
              .stream()
              .filter(name -> name.startsWith(prefix))
              .forEach(name -> {
                  String pValue = System.getProperty(name);
                  SystemProperty value = SystemProperty
                      .newBuilder()
                      .setPropertyName(name)
                      .setPropertyValue(pValue)
                      .build();
                  observer.onNext(value);
                  logger.info("server streaming sent property: " + name);
               });
        observer.onCompleted();
        logger.info("server streaming was completed!");
    }


}
```



The ***getServerStreamingProperties()*** method implements the server streaming RPC call. The ***getPropertyPrefix()*** getter method retrieves the property prefix from the client. Properties that start with the ***prefix*** are filtered out. For each property, a ***SystemProperty*** message is built and streamed to the client through the ***StreamObserver*** by using its ***onNext()*** method. When all properties are streamed, the service stops streaming by calling the ***onComplete()*** method.

Update the ***PropertiesResource*** class to implement the ***/query/properties/os*** endpoint of the ***query*** service.

Replace the ***PropertiesResource*** class.

> To open the PropertiesResource.java file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java"}



```java
package io.openliberty.guides.query;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyPrefix;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceBlockingStub;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceStub;

@ApplicationScoped
@Path("/properties")
public class PropertiesResource {

    private static Logger logger = Logger.getLogger(PropertiesResource.class.getName());

    @Inject
    @ConfigProperty(name = "system.hostname", defaultValue = "localhost")
    String SYSTEM_HOST;

    @Inject
    @ConfigProperty(name = "system.port", defaultValue = "9080")
    int SYSTEM_PORT;

    @GET
    @Path("/{property}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPropertiesString(@PathParam("property") String property) {

        ManagedChannel channel = ManagedChannelBuilder
                                     .forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceBlockingStub client = SystemServiceGrpc.newBlockingStub(channel);
        SystemPropertyName request = SystemPropertyName.newBuilder()
                                             .setPropertyName(property).build();
        SystemPropertyValue response = client.getProperty(request);
        channel.shutdownNow();
        return response.getPropertyValue();
    }

    @GET
    @Path("/os")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getOSProperties() {

        ManagedChannel channel = ManagedChannelBuilder
                                     .forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceStub client = SystemServiceGrpc.newStub(channel);

        Properties properties = new Properties();
        CountDownLatch countDown = new CountDownLatch(1);
        SystemPropertyPrefix request = SystemPropertyPrefix.newBuilder()
                                         .setPropertyPrefix("os.").build();
        client.getServerStreamingProperties(
            request, new StreamObserver<SystemProperty>() {

            @Override
            public void onNext(SystemProperty value) {
                logger.info("server streaming received: "
                   + value.getPropertyName() + "=" + value.getPropertyValue());
                properties.put(value.getPropertyName(), value.getPropertyValue());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                logger.info("server streaming completed");
                countDown.countDown();
            }
        });


        try {
            countDown.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        channel.shutdownNow();

        return properties;
    }


}
```



The endpoint creates a ***channel*** to the ***system*** service and a ***client*** by using the ***SystemServiceGrpc.newStub()*** API. Then, it calls the ***getServerStreamingProperties()*** method with an implementation of the ***StreamObserver*** interface. The ***onNext()*** method receives messages streaming from the server service individually and stores them into the ***properties*** placeholder. After all properties are received, the ***system*** service shuts down the ***channel*** and returns the placeholder. Because the RPC call is asynchronous, a ***CountDownLatch*** instance synchronizes the streaming flow.

Click the following button to visit the ***/query/properties/os*** endpoint to test out the server streaming call. The ***os.*** properties from the ***system*** service are displayed. Observe the output from the consoles running the ***system*** and ***query*** services.

::startApplication{port="9081" display="external" name="/query/properties/os" route="/query/properties/os"}



::page{title="Implementing the client streaming call"}

In the client streaming call, the ***query*** client service provides the ***/query/properties/user*** endpoint, which streams the user properties to the ***system*** server service. The ***system*** service returns a map of user properties with their values.

Update the ***SystemService*** class to implement the client streaming RPC call.

Replace the ***SystemService*** class.

> To open the SystemService.java file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java"}



```java
package io.openliberty.guides.system;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyPrefix;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;

public class SystemService extends SystemServiceGrpc.SystemServiceImplBase {

    private static Logger logger = Logger.getLogger(SystemService.class.getName());

    public SystemService() {
    }

    @Override
    public void getProperty(
        SystemPropertyName request, StreamObserver<SystemPropertyValue> observer) {

        String pName = request.getPropertyName();
        String pValue = System.getProperty(pName);
        SystemPropertyValue value = SystemPropertyValue
                                        .newBuilder()
                                        .setPropertyValue(pValue)
                                        .build();

        observer.onNext(value);
        observer.onCompleted();

    }

    @Override
    public void getServerStreamingProperties(
        SystemPropertyPrefix request, StreamObserver<SystemProperty> observer) {

        String prefix = request.getPropertyPrefix();
        System.getProperties()
              .stringPropertyNames()
              .stream()
              .filter(name -> name.startsWith(prefix))
              .forEach(name -> {
                  String pValue = System.getProperty(name);
                  SystemProperty value = SystemProperty
                      .newBuilder()
                      .setPropertyName(name)
                      .setPropertyValue(pValue)
                      .build();
                  observer.onNext(value);
                  logger.info("server streaming sent property: " + name);
               });
        observer.onCompleted();
        logger.info("server streaming was completed!");
    }

    @Override
    public StreamObserver<SystemPropertyName> getClientStreamingProperties(
        StreamObserver<SystemProperties> observer) {

        return new StreamObserver<SystemPropertyName>() {

            private Map<String, String> properties = new HashMap<String, String>();

            @Override
            public void onNext(SystemPropertyName spn) {
                String pName = spn.getPropertyName();
                String pValue = System.getProperty(pName);
                logger.info("client streaming received property: " + pName);
                properties.put(pName, pValue);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                SystemProperties value = SystemProperties.newBuilder()
                                             .putAllProperties(properties)
                                             .build();
                observer.onNext(value);
                observer.onCompleted();
                logger.info("client streaming was completed!");
            }
        };
    }

}
```



The ***getClientStreamingProperties()*** method implements client streaming RPC call. This method returns an instance of the ***StreamObserver*** interface. Its ***onNext()*** method receives the messages from the client individually and stores the property values into the ***properties*** map placeholder. When the streaming is completed, the ***properties*** placeholder is sent back to the client by the ***onCompleted()*** method.


Update the ***PropertiesResource*** class to implement the ***/query/properties/user*** endpoint of the query service.

Replace the ***PropertiesResource*** class.

> To open the PropertiesResource.java file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java"}



```java
package io.openliberty.guides.query;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyPrefix;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceBlockingStub;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceStub;

@ApplicationScoped
@Path("/properties")
public class PropertiesResource {

    private static Logger logger = Logger.getLogger(PropertiesResource.class.getName());

    @Inject
    @ConfigProperty(name = "system.hostname", defaultValue = "localhost")
    String SYSTEM_HOST;

    @Inject
    @ConfigProperty(name = "system.port", defaultValue = "9080")
    int SYSTEM_PORT;

    @GET
    @Path("/{property}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPropertiesString(@PathParam("property") String property) {

        ManagedChannel channel = ManagedChannelBuilder
                                     .forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceBlockingStub client = SystemServiceGrpc.newBlockingStub(channel);
        SystemPropertyName request = SystemPropertyName.newBuilder()
                                             .setPropertyName(property).build();
        SystemPropertyValue response = client.getProperty(request);
        channel.shutdownNow();
        return response.getPropertyValue();
    }

    @GET
    @Path("/os")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getOSProperties() {

        ManagedChannel channel = ManagedChannelBuilder
                                     .forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceStub client = SystemServiceGrpc.newStub(channel);

        Properties properties = new Properties();
        CountDownLatch countDown = new CountDownLatch(1);
        SystemPropertyPrefix request = SystemPropertyPrefix.newBuilder()
                                         .setPropertyPrefix("os.").build();
        client.getServerStreamingProperties(
            request, new StreamObserver<SystemProperty>() {

            @Override
            public void onNext(SystemProperty value) {
                logger.info("server streaming received: "
                   + value.getPropertyName() + "=" + value.getPropertyValue());
                properties.put(value.getPropertyName(), value.getPropertyValue());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                logger.info("server streaming completed");
                countDown.countDown();
            }
        });


        try {
            countDown.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        channel.shutdownNow();

        return properties;
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getUserProperties() {

        ManagedChannel channel = ManagedChannelBuilder
                                     .forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceStub client = SystemServiceGrpc.newStub(channel);
        CountDownLatch countDown = new CountDownLatch(1);
        Properties properties = new Properties();

        StreamObserver<SystemPropertyName> stream = client.getClientStreamingProperties(
            new StreamObserver<SystemProperties>() {

                @Override
                public void onNext(SystemProperties value) {
                    logger.info("client streaming received a map that has "
                        + value.getPropertiesCount() + " properties");
                    properties.putAll(value.getPropertiesMap());
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    logger.info("client streaming completed");
                    countDown.countDown();
                }
            });

        List<String> keys = System.getProperties().stringPropertyNames().stream()
                                  .filter(k -> k.startsWith("user."))
                                  .collect(Collectors.toList());

        keys.stream()
            .map(k -> SystemPropertyName.newBuilder().setPropertyName(k).build())
            .forEach(stream::onNext);
        stream.onCompleted();

        try {
            countDown.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        channel.shutdownNow();

        return properties;
    }

}
```



After a connection is created between the two services, the ***client.getClientStreamingProperties()*** method is called to get a ***stream*** and collect the properties with property names that are prefixed by ***user.***. The method creates a ***SystemPropertyName*** message individually and sends the message to the server by the ***stream::onNext*** action. When all property names are sent, the ***onCompleted()*** method is called to finish the streaming. Again, a ***CountDownLatch*** instance synchronizes the streaming flow.

Click the following button to visit the ***/query/properties/user*** endpoint to test the client streaming call. The ***user.*** properties from the ***system*** service are displayed. Observe the output from the consoles running the ***system*** and ***query*** services.

::startApplication{port="9081" display="external" name="/query/properties/user" route="/query/properties/user"}


::page{title="Implementing the bidirectional streaming call"}

In the bidirectional streaming call, the ***query*** client service provides the ***/query/properties/java*** endpoint, which streams the property names that start with ***java.*** to the ***system*** server service. The ***system*** service streams the property values back to the ***query*** service.

Update the ***SystemService*** class to implement the bidirectional streaming RPC call.

Replace the ***SystemService*** class.

> To open the SystemService.java file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java"}



```java
package io.openliberty.guides.system;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyPrefix;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;

public class SystemService extends SystemServiceGrpc.SystemServiceImplBase {

    private static Logger logger = Logger.getLogger(SystemService.class.getName());

    public SystemService() {
    }

    @Override
    public void getProperty(
        SystemPropertyName request, StreamObserver<SystemPropertyValue> observer) {

        String pName = request.getPropertyName();
        String pValue = System.getProperty(pName);
        SystemPropertyValue value = SystemPropertyValue
                                        .newBuilder()
                                        .setPropertyValue(pValue)
                                        .build();

        observer.onNext(value);
        observer.onCompleted();

    }

    @Override
    public void getServerStreamingProperties(
        SystemPropertyPrefix request, StreamObserver<SystemProperty> observer) {

        String prefix = request.getPropertyPrefix();
        System.getProperties()
              .stringPropertyNames()
              .stream()
              .filter(name -> name.startsWith(prefix))
              .forEach(name -> {
                  String pValue = System.getProperty(name);
                  SystemProperty value = SystemProperty
                      .newBuilder()
                      .setPropertyName(name)
                      .setPropertyValue(pValue)
                      .build();
                  observer.onNext(value);
                  logger.info("server streaming sent property: " + name);
               });
        observer.onCompleted();
        logger.info("server streaming was completed!");
    }

    @Override
    public StreamObserver<SystemPropertyName> getClientStreamingProperties(
        StreamObserver<SystemProperties> observer) {

        return new StreamObserver<SystemPropertyName>() {

            private Map<String, String> properties = new HashMap<String, String>();

            @Override
            public void onNext(SystemPropertyName spn) {
                String pName = spn.getPropertyName();
                String pValue = System.getProperty(pName);
                logger.info("client streaming received property: " + pName);
                properties.put(pName, pValue);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                SystemProperties value = SystemProperties.newBuilder()
                                             .putAllProperties(properties)
                                             .build();
                observer.onNext(value);
                observer.onCompleted();
                logger.info("client streaming was completed!");
            }
        };
    }

    @Override
    public StreamObserver<SystemPropertyName> getBidirectionalProperties(
        StreamObserver<SystemProperty> observer) {

        return new StreamObserver<SystemPropertyName>() {
            @Override
            public void onNext(SystemPropertyName spn) {
                String pName = spn.getPropertyName();
                String pValue = System.getProperty(pName);
                logger.info("bi-directional streaming received: " + pName);
                SystemProperty value = SystemProperty.newBuilder()
                                           .setPropertyName(pName)
                                           .setPropertyValue(pValue)
                                           .build();
                observer.onNext(value);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                observer.onCompleted();
                logger.info("bi-directional streaming was completed!");
            }
        };
    }
}
```



The ***getBidirectionalProperties()*** method implements bidirectional streaming RPC call. This method returns an instance of the ***StreamObserver*** interface. Its ***onNext()*** method receives the messages from the client individually, creates a ***SystemProperty*** message with the property name and value, and sends the message back to the client. When the client streaming is completed, the method closes the server streaming by calling the ***onCompleted()*** method.

Update the ***PropertiesResource*** class to implement of ***/query/properties/java*** endpoint of the query service.

Replace the ***PropertiesResource*** class.

> To open the PropertiesResource.java file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java"}



```java
package io.openliberty.guides.query;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyPrefix;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceBlockingStub;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceStub;

@ApplicationScoped
@Path("/properties")
public class PropertiesResource {

    private static Logger logger = Logger.getLogger(PropertiesResource.class.getName());

    @Inject
    @ConfigProperty(name = "system.hostname", defaultValue = "localhost")
    String SYSTEM_HOST;

    @Inject
    @ConfigProperty(name = "system.port", defaultValue = "9080")
    int SYSTEM_PORT;

    @GET
    @Path("/{property}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPropertiesString(@PathParam("property") String property) {

        ManagedChannel channel = ManagedChannelBuilder
                                     .forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceBlockingStub client = SystemServiceGrpc.newBlockingStub(channel);
        SystemPropertyName request = SystemPropertyName.newBuilder()
                                             .setPropertyName(property).build();
        SystemPropertyValue response = client.getProperty(request);
        channel.shutdownNow();
        return response.getPropertyValue();
    }

    @GET
    @Path("/os")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getOSProperties() {

        ManagedChannel channel = ManagedChannelBuilder
                                     .forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceStub client = SystemServiceGrpc.newStub(channel);

        Properties properties = new Properties();
        CountDownLatch countDown = new CountDownLatch(1);
        SystemPropertyPrefix request = SystemPropertyPrefix.newBuilder()
                                         .setPropertyPrefix("os.").build();
        client.getServerStreamingProperties(
            request, new StreamObserver<SystemProperty>() {

            @Override
            public void onNext(SystemProperty value) {
                logger.info("server streaming received: "
                   + value.getPropertyName() + "=" + value.getPropertyValue());
                properties.put(value.getPropertyName(), value.getPropertyValue());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                logger.info("server streaming completed");
                countDown.countDown();
            }
        });


        try {
            countDown.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        channel.shutdownNow();

        return properties;
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getUserProperties() {

        ManagedChannel channel = ManagedChannelBuilder
                                     .forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceStub client = SystemServiceGrpc.newStub(channel);
        CountDownLatch countDown = new CountDownLatch(1);
        Properties properties = new Properties();

        StreamObserver<SystemPropertyName> stream = client.getClientStreamingProperties(
            new StreamObserver<SystemProperties>() {

                @Override
                public void onNext(SystemProperties value) {
                    logger.info("client streaming received a map that has "
                        + value.getPropertiesCount() + " properties");
                    properties.putAll(value.getPropertiesMap());
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    logger.info("client streaming completed");
                    countDown.countDown();
                }
            });

        List<String> keys = System.getProperties().stringPropertyNames().stream()
                                  .filter(k -> k.startsWith("user."))
                                  .collect(Collectors.toList());

        keys.stream()
            .map(k -> SystemPropertyName.newBuilder().setPropertyName(k).build())
            .forEach(stream::onNext);
        stream.onCompleted();

        try {
            countDown.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        channel.shutdownNow();

        return properties;
    }

    @GET
    @Path("/java")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getJavaProperties() {

        ManagedChannel channel = ManagedChannelBuilder
                                      .forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                      .usePlaintext().build();
        SystemServiceStub client = SystemServiceGrpc.newStub(channel);
        Properties properties = new Properties();
        CountDownLatch countDown = new CountDownLatch(1);

        StreamObserver<SystemPropertyName> stream = client.getBidirectionalProperties(
                new StreamObserver<SystemProperty>() {

                    @Override
                    public void onNext(SystemProperty value) {
                        logger.info("bidirectional streaming received: "
                            + value.getPropertyName() + "=" + value.getPropertyValue());
                        properties.put(value.getPropertyName(),
                                       value.getPropertyValue());
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("bidirectional streaming completed");
                        countDown.countDown();
                    }
                });

        List<String> keys = System.getProperties().stringPropertyNames().stream()
                                  .filter(k -> k.startsWith("java."))
                                  .collect(Collectors.toList());

        keys.stream()
              .map(k -> SystemPropertyName.newBuilder().setPropertyName(k).build())
              .forEach(stream::onNext);
        stream.onCompleted();

        try {
            countDown.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        channel.shutdownNow();

        return properties;
    }
}
```



After a connection is created between the two services, the ***client.getBidirectionalProperties()*** method is called with an implementation of the ***StreamObserver*** interface. The ***onNext()*** method receives messages that are streaming from the server individually and stores them into the ***properties*** placeholder. Then, collect the properties . For each property name that starts with ***java.***, a ***SystemPropertyName*** message is created and sent to the server by the ***stream::onNext*** action. When all property names are sent, the streaming is ended by calling the ***onCompleted()*** method. Again, a ***CountDownLatch*** instance synchronizes the streaming flow.

Click the following button to visit the ***/query/properties/java*** endpoint to test out the bidirectional streaming call. The ***java.*** properties from the ***system*** service are displayed. Observe the output from the consoles running the ***system*** and ***query*** services.

::startApplication{port="9081" display="external" name="/query/properties/java" route="/query/properties/java"}


::page{title="Testing the application"}

Although you can test your application manually, automated tests ensure consistent code quality by triggering a failure whenever a code change introduces a defect. In this section, you'll create unit tests for the gRPC server service and integration tests for the ***query*** service.

### Implementing unit tests for the gRPC server service


The ***pom.xml*** Maven configuration file already specifies the required dependencies, including ***JUnit5***, ***grpc-testing***, and ***mockito-core*** libraries. The ***grpc-testing*** dependency provides utilities for testing gRPC services and creates a mock gRPC server that simulates client-server communication during testing. The ***mockito-core*** dependency enables the Mockito mocking framework.

Create the ***SystemServiceTest*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-grpc-intro/start/system/src/test/java/io/openliberty/guides/system/SystemServiceTest.java
```


> Then, to open the SystemServiceTest.java file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/system/src/test/java/io/openliberty/guides/system/SystemServiceTest.java, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/system/src/test/java/io/openliberty/guides/system/SystemServiceTest.java"}



```java
package io.openliberty.guides.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyPrefix;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceBlockingStub;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceStub;

public class SystemServiceTest {

    private static final String SERVER_NAME = "system";

    private static Server inProcessServer;
    private static ManagedChannel inProcessChannel;
    private static SystemServiceBlockingStub blockingStub;
    private static SystemServiceStub asyncStub;

    @BeforeAll
    public static void setUp() throws Exception {
        inProcessServer = InProcessServerBuilder.forName(SERVER_NAME)
                              .addService(new SystemService())
                              .directExecutor()
                              .build();
        inProcessServer.start();
        inProcessChannel = InProcessChannelBuilder.forName(SERVER_NAME)
                               .directExecutor()
                               .build();
        blockingStub = SystemServiceGrpc.newBlockingStub(inProcessChannel);
        asyncStub = SystemServiceGrpc.newStub(inProcessChannel);
    }

    @AfterAll
    public static void tearDown() {
        inProcessChannel.shutdownNow();
        inProcessServer.shutdownNow();
    }

    @Test
    public void testGetProperty() throws Exception {
        SystemPropertyName request = SystemPropertyName.newBuilder()
                                         .setPropertyName("os.name")
                                         .build();
        SystemPropertyValue response = blockingStub.getProperty(request);
        assertEquals(System.getProperty("os.name"), response.getPropertyValue());
    }

    @Test
    public void testGetServerStreamingProperties() throws Exception {

        SystemPropertyPrefix request = SystemPropertyPrefix.newBuilder()
                                           .setPropertyPrefix("os.")
                                           .build();
        final CountDownLatch countDown = new CountDownLatch(1);
        List<SystemProperty> properties = new ArrayList<SystemProperty>();
        StreamObserver<SystemProperty> responseObserver =
            new StreamObserver<SystemProperty>() {
                @Override
                public void onNext(SystemProperty property) {
                    properties.add(property);
                }

                @Override
                public void onError(Throwable t) {
                    fail(t.getMessage());
                }

                @Override
                public void onCompleted() {
                    countDown.countDown();
                }
            };

        asyncStub.getServerStreamingProperties(request, responseObserver);
        assertTrue(countDown.await(10, TimeUnit.SECONDS));

        for (SystemProperty property : properties) {
            String propertName = property.getPropertyName();
            assertEquals(System.getProperty(propertName),
                property.getPropertyValue(), propertName + " is incorrect");
        }
    }

    @Test
    public void testGetClientStreamingProperties() {

        @SuppressWarnings("unchecked")
        StreamObserver<SystemProperties> responseObserver =
            (StreamObserver<SystemProperties>) mock(StreamObserver.class);
        ArgumentCaptor<SystemProperties> systemPropertiesCaptor =
            ArgumentCaptor.forClass(SystemProperties.class);

        StreamObserver<SystemPropertyName> requestObserver =
            asyncStub.getClientStreamingProperties(responseObserver);
        List<String> keys = System.getProperties().stringPropertyNames().stream()
                                  .filter(k -> k.startsWith("user."))
                                  .collect(Collectors.toList());
        keys.stream()
            .map(k -> SystemPropertyName.newBuilder().setPropertyName(k).build())
            .forEach(requestObserver::onNext);
        requestObserver.onCompleted();
        verify(responseObserver, timeout(100)).onNext(systemPropertiesCaptor.capture());

        SystemProperties systemProperties = systemPropertiesCaptor.getValue();
        systemProperties.getPropertiesMap()
            .forEach((propertyName, propertyValue) ->
            assertEquals(System.getProperty(propertyName), propertyValue));
        verify(responseObserver, timeout(100)).onCompleted();
        verify(responseObserver, never()).onError(any(Throwable.class));
    }

    @Test
    public void testGetBidirectionalProperties() {

        int timesOnNext = 0;

        @SuppressWarnings("unchecked")
        StreamObserver<SystemProperty> responseObserver =
            (StreamObserver<SystemProperty>) mock(StreamObserver.class);
        StreamObserver<SystemPropertyName> requestObserver =
            asyncStub.getBidirectionalProperties(responseObserver);

        verify(responseObserver, never()).onNext(any(SystemProperty.class));

        List<String> keys = System.getProperties().stringPropertyNames().stream()
                                  .filter(k -> k.startsWith("java."))
                                  .collect(Collectors.toList());

        for (int i = 0; i < keys.size(); i++) {
            SystemPropertyName spn = SystemPropertyName.newBuilder()
                                         .setPropertyName(keys.get(i))
                                         .build();
            requestObserver.onNext(spn);
            ArgumentCaptor<SystemProperty> systemPropertyCaptor =
                ArgumentCaptor.forClass(SystemProperty.class);
            verify(responseObserver, timeout(100).times(++timesOnNext))
                .onNext(systemPropertyCaptor.capture());
            SystemProperty systemProperty = systemPropertyCaptor.getValue();
            assertEquals(System.getProperty(systemProperty.getPropertyName()),
                         systemProperty.getPropertyValue());
        }

        requestObserver.onCompleted();
        verify(responseObserver, timeout(100)).onCompleted();
        verify(responseObserver, never()).onError(any(Throwable.class));
    }
}
```




In the ***setUp()*** static method, create and start the ***inProcessServer*** in-process gRPC server. Then, create the ***inProcessChannel*** in-process channel that connects to the ***inProcessServer*** server running in the same JVM process. The unit tests can make calls to the gRPC server by using the same method signatures and functionalities as the gRPC client, even though they use different ***blockingStub*** or ***asyncStub*** stubs through the same channel.

In the ***tearDown()*** static method, shut down the ***inProcessChannel*** in-process channel and the ***inProcessServer*** in-process gRPC server.

The ***testGetProperty()*** tests the unary call to retrieve a single system property value.

The ***testGetServerStreamingProperties()*** tests the server streaming call to retrieve multiple system property values with a given property prefix.

The ***testGetClientStreamingProperties()*** tests the client streaming call to retrieve multiple system property values with given property names.

The ***testGetBidirectionalProperties()*** tests the bidirectional streaming call to retrieve multiple system property values with given property names.


### Running unit tests for the gRPC server service

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started the ***system*** service.

If the tests pass, you see output similar to the following example:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running io.openliberty.guides.system.SystemServiceTest

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.527 s - in io.openliberty.guides.system.SystemServiceTest

Results:

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```


### Implementing integration tests for the query service

In this section, you'll write integration tests using Jakarta Restful Web Services Client APIs to test the ***query*** service.

Create the ***QueryIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-grpc-intro/start/query/src/test/java/it/io/openliberty/guides/query/QueryIT.java
```


> Then, to open the QueryIT.java file in your IDE, select
> **File** > **Open** > guide-grpc-intro/start/query/src/test/java/it/io/openliberty/guides/query/QueryIT.java, or click the following button

::openFile{path="/home/project/guide-grpc-intro/start/query/src/test/java/it/io/openliberty/guides/query/QueryIT.java"}



```java
package it.io.openliberty.guides.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.MalformedURLException;

import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QueryIT {

    private static final String PORT = System.getProperty("http.port", "9081");
    private static final String URL = "http://localhost:" + PORT + "/";
    private static Client client;

    @BeforeAll
    public static void setup() {
        client = ClientBuilder.newClient();
    }

    @AfterAll
    public static void teardown() {
        client.close();
    }

    @Test
    public void testGetPropertiesString() throws MalformedURLException {
        WebTarget target = client.target(URL + "query/properties/os.name");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
                     "Incorrect response code from " + target.getUri().getPath());
        assertFalse(response.readEntity(String.class).isEmpty(),
                    "response should not be empty.");
        response.close();
    }

    @Test
    public void testGetOSProperties() throws MalformedURLException {
        WebTarget target = client.target(URL + "query/properties/os");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
                     "Incorrect response code from " + target.getUri().getPath());
        JsonObject obj = response.readEntity(JsonObject.class);
        assertFalse(obj.getString("os.name").isEmpty(),
                    "os.name should not be empty.");
        response.close();
    }

    @Test
    public void testGetUserProperties() throws MalformedURLException {
        WebTarget target = client.target(URL + "query/properties/user");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
                     "Incorrect response code from " + target.getUri().getPath());
        JsonObject obj = response.readEntity(JsonObject.class);
        assertFalse(obj.getString("user.name").isEmpty(),
                    "user.name should not be empty.");
        response.close();
    }

    @Test
    public void testGetJavaProperties() throws MalformedURLException {
        WebTarget target = client.target(URL + "query/properties/java");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
                     "Incorrect response code from " + target.getUri().getPath());
        JsonObject obj = response.readEntity(JsonObject.class);
        assertFalse(obj.getString("java.home").isEmpty(),
                    "java.home should not be empty.");
        response.close();
    }
}
```



The ***testGetPropertiesString()*** tests the ***/query/properties/os.name*** endpoint and confirms that a response is received. 

The ***testGetOSProperties()*** tests the ***/query/properties/os*** endpoint and confirms that a response is received. 

The ***testGetUserProperties()*** tests the ***/query/properties/user*** endpoint and confirms that a response is received. 

The ***testGetJavaProperties()*** tests the ***/query/properties/java*** endpoint and confirms that a response is received. 


### Running integration tests for the query service

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started the ***query*** service.

If the tests pass, you see output similar to the following example:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.query.QueryIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.247 s - in it.io.openliberty.guides.query.QueryIT

Results:

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

When you are done checking out the services, exit dev mode by pressing `Ctrl+C` in the command-line sessions where you ran the ***system*** and ***query*** services.


::page{title="Summary"}

### Nice Work!

You just developed a Java application that implements four types of gRPC calls with Open Liberty. For more information, see [Provide and consume gRPC services on Open Liberty](https://openliberty.io/docs/latest/grpc-services.html) in the Open Liberty docs.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-grpc-intro*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-grpc-intro
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Streaming%20messages%20between%20client%20and%20server%20services%20using%20gRPC&guide-id=cloud-hosted-guide-grpc-intro)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-grpc-intro/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-grpc-intro/pulls)



### Where to next?

* [Consuming RESTful services asynchronously with template interfaces](https://openliberty.io/guides/microprofile-rest-client-async.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
