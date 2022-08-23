---
markdown-version: v1
title: instructions
branch: lab-1145-instruction
version-history-start-date: 2022-08-11T09:54:57Z
---
::page{title="Welcome to the Streaming messages between client and server services using gRPC guide!"}

Learn how to use gRPC unary, server streaming, client streaming, and bidirectional streaming to communicate client and server services in Open Liberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What is gRPC?"}

The [gRPC](https://grpc.io/) Remote Procedure Call is a technology that implements remote procedure call (RPC) style APIs with HTTP/2. gRPC uses [protocol buffers](https://developers.google.com/protocol-buffers/docs/reference/overview) to define its routines that include service calls and expected messages. For each service defined in a ***.proto*** file, gRPC uses it to generate the skeleton code for users to implement and extend. Protocol buffers use a binary format to send and receive messages that are much faster and lightweight compared to JSON used in RESTful APIs.

Protocol buffers allow cross project support through the ***.proto*** file, as a result gRPC clients and servers are also able to run and communicate with each other on different environments. For example, a gRPC client running in Java codebase is able to call a gRPC server from either [supported languages](https://grpc.io/docs/languages/). This feature of protocol buffers allows for easier integration between services.

::page{title="What you'll learn"}

You will learn how to create gRPC client and server services by using protocol buffers and how to implement them with Open Liberty. You will use Maven throughout the guide to generate the gRPC stubs and deploy the services and to interact with the running Liberty server.

The application that you will build in this guide consists of the ***systemproto*** model project, the ***query*** client service, and the ***system*** server service. The ***query*** service implements four RESTful APIs by using four different gRPC streaming methods.

![gRPC application architecture that the system service provides gRPC server and the query service makes different gRPC streaming calls](https://raw.githubusercontent.com/OpenLiberty/draft-guide-grpc-intro/draft/assets/architecture.png)


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/draft-guide-grpc-intro.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/draft-guide-grpc-intro.git
cd draft-guide-grpc-intro
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

Next, open another command-line session and start the ***query*** service by using the following command:
```bash
mvn -pl query liberty:run
```

Click the following button to visit the ***/query/properties/os.name*** endpoint to test out the basic unary service. You will see your operating system name.  

::startApplication{port="9081" display="external" name="/query/properties/os.name" route="/query/properties/os.name"}

Next, click the following button to visit the ***/query/properties/os*** endpoint to test out the server streaming. You should see the information of your localhost operating system.

::startApplication{port="9081" display="external" name="/query/properties/os" route="/query/properties/os"}

Visit the ***/query/properties/user*** endpoint to test out the client streaming. You should see the information of your localhost user properties. 

::startApplication{port="9081" display="external" name="/query/properties/user" route="/query/properties/user"}

Visit the ***/query/properties/java*** endpoint to test out the bidirectional streaming. You should see the information of your localhost Java properties.

::startApplication{port="9081" display="external" name="/query/properties/java" route="/query/properties/java"}

Observe the output from the consoles running the ***system*** and ***query*** services.

After you are finished checking out the application, stop both the ***query*** and ***system*** services by pressing `Ctrl+C` in the command-line sessions where you ran them. Alternatively, you can run the following goals from the ***finish*** directory in another command-line session:
```bash
mvn -pl system liberty:stop
mvn -pl query liberty:stop
```


::page{title="Creating and defining gRPC service"}

Navigate to the ***start*** directory to begin.

```bash
cd /home/project/draft-guide-grpc-intro/start
```

First, create the ***.proto*** file and generate gRPC classes. You will implement the gRPC server with the generated classes later. The ***.proto*** file defines all the service calls and message types. The message types are used in the service call definition for the parameters and returns.

Create the ***SystemService.proto*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-grpc-intro/start/systemproto/src/main/proto/SystemService.proto
```


> Then, to open the SystemService.proto file in your IDE, select
> **File** > **Open** > draft-guide-grpc-intro/start/systemproto/src/main/proto/SystemService.proto, or click the following button

::openFile{path="/home/project/draft-guide-grpc-intro/start/systemproto/src/main/proto/SystemService.proto"}



```

syntax = "proto3";
package io.openliberty.guides.systemproto;
option java_multiple_files = true;

service SystemService {
    rpc getProperty (SystemPropertyName) returns (SystemPropertyValue) {}

    rpc getPropertiesServer (SystemPropertyName) returns (stream SystemProperty) {}

    rpc getPropertiesClient (stream SystemPropertyName) returns (SystemProperties) {}

    rpc getPropertiesBidirect (stream SystemPropertyName) returns (stream SystemProperty) {}
}

message SystemPropertyName {
    string propertyName = 1;
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



The first few lines define the ***syntax***, ***package***, and ***option*** basic configuration of the ***.proto*** file. The ***SystemService*** service contains the four service calls that will be implemented in the coming sections.

The ***getProperty*** rpc defines the unary call. In this call, the client side sends a ***SystemPropertyName*** message to the server side that returns back a ***SystemPropertyValue*** message with the property value. The ***SystemPropertyName*** and ***SystemPropertyValue*** message types define that the ***propertyName*** and ***propertyValue*** must be string.

The ***getPropertiesServer*** rpc defines the server streaming call. The client side sends a ***SystemPropertyName*** message to the server side. The server returns back a stream of ***SystemProperty*** messages. Each ***SystemProperty*** message contains a ***propertyName*** and a ***propertyValue*** strings.

The ***getPropertiesClient*** rpc defines the client streaming call. The client side streams ***SystemPropertyName*** messages to the server side. The server returns back a ***SystemProperties*** message that contains a map of the properties with their respective values.


The ***getPropertiesBidirect*** rpc defines the bidirectional streaming call. In this service, the client side streams ***SystemPropertyName*** messages to the server side. The server returns back a stream of ***SystemProperty*** messages.

To compile the ***.proto*** file, the ***pom.xml*** Maven configuration file needs the  ***grpc-protobuf*** and ***grpc-stub*** dependencies, and the ***protobuf-maven-plugin*** plugin. To install the correct version of Protobuf compiler automatically, the ***os-maven-plugin*** extension is required in the ***build*** configuration.

Run the following command to generate the gRPC classes.
```bash
mvn -pl systemproto install
```


::page{title="Implementing unary call"}

Navigate to the ***start*** directory.

```bash
cd /home/project/draft-guide-grpc-intro/start
```

When you run Open Liberty in development mode, known as dev mode, the server listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following command to start the ***system*** service in dev mode:

```bash
mvn -pl system liberty:dev
```

Open another command-line session and run the following commands to start the ***query*** service in dev mode:

```bash
mvn -pl query liberty:dev
```

After you see the following message, your application servers in dev mode are ready:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

Start by implementing the first service call, the unary call. In this service call, the ***query*** client service sends a property to the ***system*** server service that will return the property value. This type of service call resembles the RESTful API. 

Create the ***SystemService*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java
```


> Then, to open the SystemService.java file in your IDE, select
> **File** > **Open** > draft-guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java, or click the following button

::openFile{path="/home/project/draft-guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java"}



```java
package io.openliberty.guides.system;

import java.util.HashMap;
import java.util.Map;

import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;

public class SystemService extends SystemServiceGrpc.SystemServiceImplBase {

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



The ***SystemService*** class implements the ***SystemServiceGrpc*** class that is generated by the proto file. The four types of services defined in the proto file are implemented in this class.

The ***getProperty()*** method implements the rpc unary call defined in the proto file. Use the ***getPropertyName()*** getter method that is generated by gRPC to retrieve the property name from the client, and store into the ***pName*** variable. Get and store the System property value into the ***pValue*** variable. Use the the gRPC APIs to create a ***SystemPropertyValue*** message that its type is defined in the ***SystemService.proto*** file. Then, send the message to the client service through the ***StreamObserver*** by using its ***onNext()*** and ***onComplete()*** methods.

Replace the ***system*** server configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > draft-guide-grpc-intro/start/system/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/draft-guide-grpc-intro/start/system/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<server description="system service">

    <featureManager>
        <feature>jaxrs-2.1</feature>
        <feature>grpc-1.0</feature>
    </featureManager>

    <!-- Due to target="*", this configuration will be applied to every gRPC service 
         running on the server. This configuration registers a ServerInterceptor -->
    <grpc target="*"/>

    <applicationManager autoExpand="true"/>

    <webApplication contextRoot="/" location="guide-grpc-intro-system.war"/>
</server>
```





Implement the RESTful endpoint in the ***query*** service.

Create the ***PropertiesResource*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java
```


> Then, to open the PropertiesResource.java file in your IDE, select
> **File** > **Open** > draft-guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java, or click the following button

::openFile{path="/home/project/draft-guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java"}



```java
package io.openliberty.guides.query;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceBlockingStub;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceStub;

@ApplicationScoped
@Path("/properties")
public class PropertiesResource {

    @Inject
    @ConfigProperty(name = "system.hostname", defaultValue = "localhost")
    String SYSTEM_HOST;

    @Inject
    @ConfigProperty(name = "system.port", defaultValue = "9080")
    int SYSTEM_PORT;

    @GET
    @Path("/{propertyName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPropertiesString(@PathParam("propertyName") String propertyName) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceBlockingStub client = SystemServiceGrpc.newBlockingStub(channel);
        SystemPropertyName request = SystemPropertyName.newBuilder()
                                             .setPropertyName(propertyName).build();
        SystemPropertyValue response = client.getProperty(request);
        channel.shutdownNow();
        return response.getPropertyValue();
    }



}
```


The ***PropertiesResource*** class provides RESTful endpoints to interact with the ***system*** service. The ***/query/properties/${propertyName}*** endpoint uses the unary service call to get the property value from the ***system*** service. The endpoint creates a ***channel***, uses the channel to create a client by the ***SystemServiceGrpc.newBlockingStub()*** API, uses the client to get the property value, shutdowns the channel, and then returns the value that the ***system*** service responses immediately.

Replace the ***query*** server configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > draft-guide-grpc-intro/start/query/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/draft-guide-grpc-intro/start/query/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<server description="new server">

    <featureManager>
        <feature>jaxrs-2.1</feature>
        <feature>cdi-2.0</feature>
        <feature>mpConfig-2.0</feature>
        <feature>grpc-1.0</feature>
        <feature>grpcClient-1.0</feature>
    </featureManager>

    <variable defaultValue="9081" name="default.http.port"/>
    <variable defaultValue="9444" name="default.https.port"/>

    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="${default.http.port}"
                  httpsPort="${default.https.port}"
                  host="*"/>

    <!-- Due to host="*", this configuration will be applied to every gRPC client call
         that gets made. This configuration registers a ClientInterceptor, and it directs
         Cookie headers to get forwarded with any outbound RPC calls, in this case, that
         enables authorization propagation. -->
    <grpcClient headersToPropagate="Cookie" host="*"/>

    <applicationManager autoExpand="true"/>

    <webApplication contextRoot="/" location="guide-grpc-intro-query.war"/>
</server>
```





Because you are running the ***system*** and ***query*** services in dev mode, the changes that you made were automatically picked up. You’re now ready to check out your application in your browser.

Click the following button to visit the ***/query/properties/os.name*** endpoint to test out the unary service. You should see your operating system name. You can try out the URL with other property name.

::startApplication{port="9081" display="external" name="/query/properties/os.name" route="/query/properties/os.name"}


::page{title="Implementing server streaming call"}

In the server streaming call, the ***query*** client service provides the ***/query/properties/os*** endpoint that sends a message to the ***system*** server service. The ***system*** service streams the properties started with ***os.*** back to the ***query*** service. A channel is created between the ***query*** and the ***system*** services to stream messages. The channel is only closed by the ***system*** service after sending the last message to the ***query*** service. 

Update the ***SystemService*** class to implement the server streaming rpc call.
Replace the ***SystemService*** class.

> To open the SystemService.java file in your IDE, select
> **File** > **Open** > draft-guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java, or click the following button

::openFile{path="/home/project/draft-guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java"}



```java
package io.openliberty.guides.system;

import java.util.HashMap;
import java.util.Map;

import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;

public class SystemService extends SystemServiceGrpc.SystemServiceImplBase {

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
    public void getPropertiesServer(
        SystemPropertyName request, StreamObserver<SystemProperty> observer) {

        String prefix = request.getPropertyName();
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
                  System.out.println("server streaming sent property: " + name);
               });
        observer.onCompleted();
        System.out.println("server streaming was completed!");
    }


}
```



The ***getPropertiesServer()*** method implements server streaming rpc call. Use the ***getPropertyName()*** getter method to retrieve the property prefix from the client. Filter out the properties that starts with the ***prefix***. For each property, build a ***SystemProperty*** message and stream the message to the client through the ***StreamObserver*** by using its ***onNext()*** method. When all properties are streamed, finish the streaming by calling the ***onComplete()*** method.

Update the ***PropertiesResource*** class to implement the ***/query/properties/os*** endpoint of the ***query*** service.

Replace the ***PropertiesResource*** class.

> To open the PropertiesResource.java file in your IDE, select
> **File** > **Open** > draft-guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java, or click the following button

::openFile{path="/home/project/draft-guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java"}



```java
package io.openliberty.guides.query;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceBlockingStub;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceStub;

@ApplicationScoped
@Path("/properties")
public class PropertiesResource {

    @Inject
    @ConfigProperty(name = "system.hostname", defaultValue = "localhost")
    String SYSTEM_HOST;

    @Inject
    @ConfigProperty(name = "system.port", defaultValue = "9080")
    int SYSTEM_PORT;

    @GET
    @Path("/{propertyName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPropertiesString(@PathParam("propertyName") String propertyName) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceBlockingStub client = SystemServiceGrpc.newBlockingStub(channel);
        SystemPropertyName request = SystemPropertyName.newBuilder()
                                             .setPropertyName(propertyName).build();
        SystemPropertyValue response = client.getProperty(request);
        channel.shutdownNow();
        return response.getPropertyValue();
    }

    @GET
    @Path("/os")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getOSProperties() {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceStub client = SystemServiceGrpc.newStub(channel);

        Properties properties = new Properties();
        CountDownLatch countDown = new CountDownLatch(1);
        SystemPropertyName request = SystemPropertyName.newBuilder()
                                         .setPropertyName("os.").build();
        client.getPropertiesServer(request, new StreamObserver<SystemProperty>() {

            @Override
            public void onNext(SystemProperty value) {
                System.out.println("server streaming received: "
                   + value.getPropertyName() + "=" + value.getPropertyValue());
                properties.put(value.getPropertyName(), value.getPropertyValue());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("server streaming completed");
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



The endpoint creates a ***channel*** to the ***system*** service and a ***client*** by using the ***SystemServiceGrpc.newStub()*** API. Then, call the ***getPropertiesServer()*** method with an implementation of the ***StreamObserver*** interface. The ***onNext()*** method receives messages streaming from the server individually and stores them into the ***properties*** placeholder. After all properties are received, shutdown the ***channel*** and returns the placeholder. Because the rpc call is asynchronous, use a ***CountDownLatch*** to synchronize the streaming flow.

Click the following button to visit the ***/query/properties/os*** endpoint to test out the server streaming. You should see the ***os.*** properties from the ***system*** service. Observe the output from the consoles running the ***system*** and ***query*** services.

::startApplication{port="9081" display="external" name="/query/properties/os" route="/query/properties/os"}



::page{title="Implementing client streaming call"}

In the client streaming call, the ***query*** client service provides the ***/query/properties/user*** endpoint that streams the user properties to the ***system*** server service. The ***system*** service returns a map of user properties with their values.

Update the ***SystemService*** class to implement the client streaming rpc call.

Replace the ***SystemService*** class.

> To open the SystemService.java file in your IDE, select
> **File** > **Open** > draft-guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java, or click the following button

::openFile{path="/home/project/draft-guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java"}



```java
package io.openliberty.guides.system;

import java.util.HashMap;
import java.util.Map;

import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;

public class SystemService extends SystemServiceGrpc.SystemServiceImplBase {

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
    public void getPropertiesServer(
        SystemPropertyName request, StreamObserver<SystemProperty> observer) {

        String prefix = request.getPropertyName();
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
                  System.out.println("server streaming sent property: " + name);
               });
        observer.onCompleted();
        System.out.println("server streaming was completed!");
    }

    @Override
    public StreamObserver<SystemPropertyName> getPropertiesClient(
        StreamObserver<SystemProperties> observer) {

        return new StreamObserver<SystemPropertyName>() {

            private Map<String, String> properties = new HashMap<String, String>();

            @Override
            public void onNext(SystemPropertyName spn) {
                String pName = spn.getPropertyName();
                String pValue = System.getProperty(pName);
                System.out.println("client streaming received property: " + pName);
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
                System.out.println("client streaming was completed!");
            }
        };
    }

}
```



The ***getPropertiesClient()*** method implements client streaming rpc call. This method returns an instance of the ***StreamObserver*** interface. Its ***onNext()*** method receives the messages from the client individually and stores the property values into the ***properties*** map placeholder. When the streaming is completed, the ***properties*** placeholder is sent back to the client by the ***onCompleted()*** method.


Update the ***PropertiesResource*** class to implement of ***/query/properties/user*** endpoint of the query service.

Replace the ***PropertiesResource*** class.

> To open the PropertiesResource.java file in your IDE, select
> **File** > **Open** > draft-guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java, or click the following button

::openFile{path="/home/project/draft-guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java"}



```java
package io.openliberty.guides.query;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceBlockingStub;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceStub;

@ApplicationScoped
@Path("/properties")
public class PropertiesResource {

    @Inject
    @ConfigProperty(name = "system.hostname", defaultValue = "localhost")
    String SYSTEM_HOST;

    @Inject
    @ConfigProperty(name = "system.port", defaultValue = "9080")
    int SYSTEM_PORT;

    @GET
    @Path("/{propertyName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPropertiesString(@PathParam("propertyName") String propertyName) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceBlockingStub client = SystemServiceGrpc.newBlockingStub(channel);
        SystemPropertyName request = SystemPropertyName.newBuilder()
                                             .setPropertyName(propertyName).build();
        SystemPropertyValue response = client.getProperty(request);
        channel.shutdownNow();
        return response.getPropertyValue();
    }

    @GET
    @Path("/os")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getOSProperties() {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceStub client = SystemServiceGrpc.newStub(channel);

        Properties properties = new Properties();
        CountDownLatch countDown = new CountDownLatch(1);
        SystemPropertyName request = SystemPropertyName.newBuilder()
                                         .setPropertyName("os.").build();
        client.getPropertiesServer(request, new StreamObserver<SystemProperty>() {

            @Override
            public void onNext(SystemProperty value) {
                System.out.println("server streaming received: "
                   + value.getPropertyName() + "=" + value.getPropertyValue());
                properties.put(value.getPropertyName(), value.getPropertyValue());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("server streaming completed");
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

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceStub client = SystemServiceGrpc.newStub(channel);
        CountDownLatch countDown = new CountDownLatch(1);
        Properties properties = new Properties();

        StreamObserver<SystemPropertyName> stream = client.getPropertiesClient(
            new StreamObserver<SystemProperties>() {

                @Override
                public void onNext(SystemProperties value) {
                    System.out.println("client streaming received a map that has " 
                        + value.getPropertiesCount() + " properties");
                    properties.putAll(value.getPropertiesMap());
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    System.out.println("client streaming completed");
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



After a connection is created between the two services, call the ***client.getPropertiesClient()*** method to get a ***stream***, collect the properties started with ***user.***, create a ***SystemPropertyName*** message individually, and send the message to the server through by the ***stream::onNext*** action. When all property names are sent, finish the streaming by calling the ***onCompleted()***. Again, use a ***CountDownLatch*** to synchronize the streaming flow.

Click the following button to visit the ***/query/properties/user*** endpoint to test out the client streaming. You should see the ***user.*** properties from the ***system*** service. Observe the output from the consoles running the ***system*** and ***query*** services.

::startApplication{port="9081" display="external" name="/query/properties/user" route="/query/properties/user"}


::page{title="Implementing bidirectional streaming call"}

In the bidirectional streaming call, the ***query*** client service provides the ***/query/properties/java*** endpoint that streams the property names started with ***java.*** to the ***system*** server service. The ***system*** service streams the property values back to the ***query*** service.

Update the ***SystemService*** class to implement the bidirectional streaming rpc call.

Replace the ***SystemService*** class.

> To open the SystemService.java file in your IDE, select
> **File** > **Open** > draft-guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java, or click the following button

::openFile{path="/home/project/draft-guide-grpc-intro/start/system/src/main/java/io/openliberty/guides/system/SystemService.java"}



```java
package io.openliberty.guides.system;

import java.util.HashMap;
import java.util.Map;

import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;

public class SystemService extends SystemServiceGrpc.SystemServiceImplBase {

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
    public void getPropertiesServer(
        SystemPropertyName request, StreamObserver<SystemProperty> observer) {

        String prefix = request.getPropertyName();
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
                  System.out.println("server streaming sent property: " + name);
               });
        observer.onCompleted();
        System.out.println("server streaming was completed!");
    }

    @Override
    public StreamObserver<SystemPropertyName> getPropertiesClient(
        StreamObserver<SystemProperties> observer) {

        return new StreamObserver<SystemPropertyName>() {

            private Map<String, String> properties = new HashMap<String, String>();

            @Override
            public void onNext(SystemPropertyName spn) {
                String pName = spn.getPropertyName();
                String pValue = System.getProperty(pName);
                System.out.println("client streaming received property: " + pName);
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
                System.out.println("client streaming was completed!");
            }
        };
    }

    @Override
    public StreamObserver<SystemPropertyName> getPropertiesBidirect(
        StreamObserver<SystemProperty> observer) {

        return new StreamObserver<SystemPropertyName>() {
            @Override
            public void onNext(SystemPropertyName spn) {
                String pName = spn.getPropertyName();
                String pValue = System.getProperty(pName);
                System.out.println("bi-directional streaming received: " + pName);
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
                System.out.println("bi-directional streaming was completed!");
            }
        };
    }
}
```



The ***getPropertiesBidirect()*** method implements bidirectional streaming rpc call. This method returns an instance of the ***StreamObserver*** interface. Its ***onNext()*** method receives the messages from the client individually, creates a ***SystemProperty*** message with the property name and value, and sends the message back to the client by the ***onNext()*** method. When the client streaming is completed, close the server streaming by calling the ***onCompleted()*** method.


Update the ***PropertiesResource*** class to implement of ***/query/properties/java*** endpoint of the query service.

Replace the ***PropertiesResource*** class.

> To open the PropertiesResource.java file in your IDE, select
> **File** > **Open** > draft-guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java, or click the following button

::openFile{path="/home/project/draft-guide-grpc-intro/start/query/src/main/java/io/openliberty/guides/query/PropertiesResource.java"}



```java
package io.openliberty.guides.query;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.openliberty.guides.systemproto.SystemProperties;
import io.openliberty.guides.systemproto.SystemProperty;
import io.openliberty.guides.systemproto.SystemPropertyName;
import io.openliberty.guides.systemproto.SystemPropertyValue;
import io.openliberty.guides.systemproto.SystemServiceGrpc;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceBlockingStub;
import io.openliberty.guides.systemproto.SystemServiceGrpc.SystemServiceStub;

@ApplicationScoped
@Path("/properties")
public class PropertiesResource {

    @Inject
    @ConfigProperty(name = "system.hostname", defaultValue = "localhost")
    String SYSTEM_HOST;

    @Inject
    @ConfigProperty(name = "system.port", defaultValue = "9080")
    int SYSTEM_PORT;

    @GET
    @Path("/{propertyName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPropertiesString(@PathParam("propertyName") String propertyName) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceBlockingStub client = SystemServiceGrpc.newBlockingStub(channel);
        SystemPropertyName request = SystemPropertyName.newBuilder()
                                             .setPropertyName(propertyName).build();
        SystemPropertyValue response = client.getProperty(request);
        channel.shutdownNow();
        return response.getPropertyValue();
    }

    @GET
    @Path("/os")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getOSProperties() {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceStub client = SystemServiceGrpc.newStub(channel);

        Properties properties = new Properties();
        CountDownLatch countDown = new CountDownLatch(1);
        SystemPropertyName request = SystemPropertyName.newBuilder()
                                         .setPropertyName("os.").build();
        client.getPropertiesServer(request, new StreamObserver<SystemProperty>() {

            @Override
            public void onNext(SystemProperty value) {
                System.out.println("server streaming received: "
                   + value.getPropertyName() + "=" + value.getPropertyValue());
                properties.put(value.getPropertyName(), value.getPropertyValue());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("server streaming completed");
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

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                     .usePlaintext().build();
        SystemServiceStub client = SystemServiceGrpc.newStub(channel);
        CountDownLatch countDown = new CountDownLatch(1);
        Properties properties = new Properties();

        StreamObserver<SystemPropertyName> stream = client.getPropertiesClient(
            new StreamObserver<SystemProperties>() {

                @Override
                public void onNext(SystemProperties value) {
                    System.out.println("client streaming received a map that has " 
                        + value.getPropertiesCount() + " properties");
                    properties.putAll(value.getPropertiesMap());
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    System.out.println("client streaming completed");
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

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SYSTEM_HOST, SYSTEM_PORT)
                                      .usePlaintext().build();
        SystemServiceStub client = SystemServiceGrpc.newStub(channel);
        Properties properties = new Properties();
        CountDownLatch countDown = new CountDownLatch(1);

        StreamObserver<SystemPropertyName> stream = client.getPropertiesBidirect(
                new StreamObserver<SystemProperty>() {

                    @Override
                    public void onNext(SystemProperty value) {
                        System.out.println("bidirectional streaming received: "
                            + value.getPropertyName() + "=" + value.getPropertyValue());
                        properties.put(value.getPropertyName(), value.getPropertyValue());
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("bidirectional streaming completed");
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



After a connection is created between the two services, call the ***client.getPropertiesBidirect()*** method with an implementation of the ***StreamObserver*** interface. The ***onNext()*** method receives messages streaming from the server individually and stores them into the ***properties*** placeholder. Then, collect the properties started with ***java.***. For each property name, create a ***SystemPropertyName*** message and send the message to the server through by the ***stream::onNext*** action. When all property names are sent, finish the streaming by calling the ***onCompleted()*** method. Again, use a ***CountDownLatch*** to synchronize the streaming flow.

Click the following button to visit the ***/query/properties/java*** endpoint to test out the bidirectional streaming. You should see the ***java.*** properties from the ***system*** service. Observe the output from the consoles running the ***system*** and ***query*** services.

::startApplication{port="9081" display="external" name="/query/properties/java" route="/query/properties/java"}


::page{title="Testing the application"}
Create the ***QueryIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-grpc-intro/start/query/src/test/java/it/io/openliberty/guides/query/QueryIT.java
```


> Then, to open the QueryIT.java file in your IDE, select
> **File** > **Open** > draft-guide-grpc-intro/start/query/src/test/java/it/io/openliberty/guides/query/QueryIT.java, or click the following button

::openFile{path="/home/project/draft-guide-grpc-intro/start/query/src/test/java/it/io/openliberty/guides/query/QueryIT.java"}



```java
package it.io.openliberty.guides.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.MalformedURLException;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QueryIT {

    private static final String PORT = System.getProperty("http.port", "9081");
    private static final String URL = "http://localhost:" + PORT + "/";
    private static Client client;

    @BeforeAll
    private static void setup() {
        client = ClientBuilder.newClient();
        client.register(JsrJsonpProvider.class);
    }

    @AfterAll
    private static void teardown() {
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


Each test case tests one of the methods for instantiating a RESTful client.

The ***testGetPropertiesString()*** tests the ***/query/properties/os.name*** endpoint and confirms that a response is received. 

The ***testGetOSProperties()*** tests the ***/query/properties/os*** endpoint and confirms that a response is received. 

The ***testGetUserProperties()*** tests the ***/query/properties/user*** endpoint and confirms that a response is received. 

The ***testGetJavaProperties()*** tests the ***/query/properties/java*** endpoint and confirms that a response is received. 

### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.query.QueryIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.247 s - in it.io.openliberty.guides.query.QueryIT

Results:

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

When you are done checking out the services, exit dev mode by pressing `Ctrl+C` in the command-line sessions where you ran the ***system*** and ***query*** services,  or by typing ***q*** and then pressing the ***enter/return*** key. Alternatively, you can run the ***liberty:stop*** goal from the ***start*** directory in another command-line session for the ***system*** and ***query*** services:
```bash
cd /home/project/guide-grpc-intro/start
mvn -pl system liberty:stop
mvn -pl query liberty:stop
```


::page{title="Summary"}

### Nice Work!

You just developed an application that implements four gRPC streaming calls with Open Liberty.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***draft-guide-grpc-intro*** project by running the following commands:

```bash
cd /home/project
rm -fr draft-guide-grpc-intro
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Streaming%20messages%20between%20client%20and%20server%20services%20using%20gRPC&guide-id=cloud-hosted-draft-guide-grpc-intro)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/draft-guide-grpc-intro/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/draft-guide-grpc-intro/pulls)



### Where to next?

* [Consuming RESTful services asynchronously with template interfaces](https://openliberty.io/guides/microprofile-rest-client-async.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
