---
markdown-version: v1
title: instructions
branch: lab-500-instruction
version-history-start-date: 2020-09-14 09:20:41 UTC
tool-type: theia
---
::page{title="Welcome to the Consuming RESTful services using the reactive JAX-RS client guide!"}

Learn how to use a reactive JAX-RS client to asynchronously invoke RESTful microservices over HTTP.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

First, you'll learn how to create a reactive JAX-RS client application by using the default reactive JAX-RS client APIs. You will then learn how to take advantage of the RxJava reactive extensions with a pluggable reactive JAX-RS client provider that's published by [Eclipse Jersey](https://eclipse-ee4j.github.io/jersey). The JAX-RS client is an API used to communicate with RESTful web services.  The API makes it easy to consume a web service by using the HTTP protocol, which means that you can efficiently implement client-side applications. The reactive client extension to JAX-RS is an API that enables you to use the reactive programming model when using the JAX-RS client.

Reactive programming is an extension of asynchronous programming and focuses on the flow of data through data streams. Reactive applications process data when it becomes available and respond to requests as soon as processing is complete. The request to the application and response from the application are decoupled so that the application is not blocked from responding to other requests in the meantime. Because reactive applications can run faster than synchronous applications, they provide a much smoother user experience.

The application in this guide demonstrates how the JAX-RS client accesses remote RESTful services by using asynchronous method calls. You’ll first look at the supplied client application that uses the JAX-RS default ***CompletionStage***-based provider. Then, you’ll modify the client application to use Jersey’s RxJava provider, which is an alternative JAX-RS reactive provider. Both Jersey and Apache CXF provide third-party reactive libraries for RxJava and were tested for use in Open Liberty.

The application that you will be working with consists of three microservices, ***system***, ***inventory***, and ***query***. Every 15 seconds, the ***system*** microservice calculates and publishes an event that contains its current average system load. The ***inventory*** microservice subscribes to that information so that it can keep an updated list of all the systems and their current system loads.

![Reactive Query Service](https://raw.githubusercontent.com/OpenLiberty/guide-reactive-rest-client/prod/assets/QueryService.png)


The microservice that you will modify is the ***query*** service. It communicates with the ***inventory*** service to determine which system has the highest system load and which system has the lowest system load.

The ***system*** and ***inventory*** microservices use MicroProfile Reactive Messaging to send and receive the system load events. If you want to learn more about reactive messaging, see the  [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html) guide.


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-reactive-rest-client.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-reactive-rest-client.git
cd guide-reactive-rest-client
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

::page{title="Creating a web client using the default JAX-RS API"}

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-reactive-rest-client/start
```

JAX-RS provides a default reactive provider that you can use to create a reactive REST client using the ***CompletionStage*** interface.

Create an ***InventoryClient*** class, which retrieves inventory data, and a ***QueryResource*** class, which queries data from the ***inventory*** service.

Create the ***InventoryClient*** interface.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/client/InventoryClient.java
```


> Then, to open the InventoryClient.java file in your IDE, select
> **File** > **Open** > guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/client/InventoryClient.java, or click the following button

::openFile{path="/home/project/guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/client/InventoryClient.java"}



```java
package io.openliberty.guides.query.client;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@RequestScoped
public class InventoryClient {

    @Inject
    @ConfigProperty(name = "INVENTORY_BASE_URI", defaultValue = "http://localhost:9085")
    private String baseUri;

    public List<String> getSystems() {
        return ClientBuilder.newClient()
                            .target(baseUri)
                            .path("/inventory/systems")
                            .request()
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .get(new GenericType<List<String>>(){});
    }

    public CompletionStage<Properties> getSystem(String hostname) {
        return ClientBuilder.newClient()
                            .target(baseUri)
                            .path("/inventory/systems")
                            .path(hostname)
                            .request()
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .rx()
                            .get(Properties.class);
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***getSystem()*** method returns the ***CompletionStage*** interface. This interface represents a unit or stage of a computation. When the associated computation completes, the value can be retrieved. The ***rx()*** method calls the ***CompletionStage*** interface. It retrieves the ***CompletionStageRxInvoker*** class and allows these methods to function correctly with the ***CompletionStage*** interface return type.

Create the ***QueryResource*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java
```


> Then, to open the QueryResource.java file in your IDE, select
> **File** > **Open** > guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java, or click the following button

::openFile{path="/home/project/guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java"}



```java
package io.openliberty.guides.query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.openliberty.guides.query.client.InventoryClient;

@ApplicationScoped
@Path("/query")
public class QueryResource {
    
    @Inject
    private InventoryClient inventoryClient;

    @GET
    @Path("/systemLoad")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Properties> systemLoad() {
        List<String> systems = inventoryClient.getSystems();
        CountDownLatch remainingSystems = new CountDownLatch(systems.size());
        final Holder systemLoads = new Holder();

        for (String system : systems) {
            inventoryClient.getSystem(system)
                           .thenAcceptAsync(p -> {
                                if (p != null) {
                                    systemLoads.updateValues(p);
                                }
                                remainingSystems.countDown();
                           })
                           .exceptionally(ex -> {
                                remainingSystems.countDown();
                                ex.printStackTrace();
                                return null;
                           });
        }

        try {
            remainingSystems.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return systemLoads.getValues();
    }

    private class Holder {
        private volatile Map<String, Properties> values;

        public Holder() {
            this.values = new ConcurrentHashMap<String, Properties>();
            init();
        }

        public Map<String, Properties> getValues() {
            return this.values;
        }

        public void updateValues(Properties p) {
            final BigDecimal load = (BigDecimal) p.get("systemLoad");

            this.values.computeIfPresent("lowest", (key, curr_val) -> {
                BigDecimal lowest = (BigDecimal) curr_val.get("systemLoad");
                return load.compareTo(lowest) < 0 ? p : curr_val;
            });
            this.values.computeIfPresent("highest", (key, curr_val) -> {
                BigDecimal highest = (BigDecimal) curr_val.get("systemLoad");
                return load.compareTo(highest) > 0 ? p : curr_val;
            });
        }

        private void init() {
            this.values.put("highest", new Properties());
            this.values.put("lowest", new Properties());
            this.values.get("highest").put("hostname", "temp_max");
            this.values.get("lowest").put("hostname", "temp_min");
            this.values.get("highest").put("systemLoad", new BigDecimal(Double.MIN_VALUE));
            this.values.get("lowest").put("systemLoad", new BigDecimal(Double.MAX_VALUE));
        }
    }
}
```



The ***systemLoad*** endpoint asynchronously processes the data that is retrieved by the ***InventoryClient*** interface and serves that data after all of the services respond. The ***thenAcceptAsync()*** and ***exceptionally()*** methods together behave like an asynchronous try-catch block. The data is processed in the ***thenAcceptAsync()*** method only after the ***CompletionStage*** interface finishes retrieving it.  When you return a ***CompletionStage*** type in the resource, it doesn’t necessarily mean that the computation completed and the response was built.

A ***CountDownLatch*** object is used to track how many asynchronous requests are being waited on. After each thread is completed, the ***countdown()*** methodcounts the ***CountDownLatch*** object down towards ***0***. This means that the value returns only after the thread that's retrieving the value is complete.The ***await()*** method stops and waits until all of the requests are complete. While the countdown completes, the main thread is free to perform other tasks. In this case, no such task is present.


::page{title="Building and running the application"}

The ***system***, ***inventory***, and ***query*** microservices will be built in Docker containers. If you want to learn more about Docker containers, check out the [Containerizing microservices](https://openliberty.io/guides/containerize.html) guide.

Start your Docker environment.

To build the application, run the Maven ***install*** and ***package*** goals from the command-line session in the ***start*** directory:

```bash
mvn -pl models install
mvn package
```



Run the following commands to containerize the microservices:

```bash
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
docker build -t query:1.0-SNAPSHOT query/.
```

Next, use the provided script to start the application in Docker containers. The script creates a network for the containers to communicate with each other. It creates containers for Kafka, Zookeeper, and all of the microservices in the project.


```bash
./scripts/startContainers.sh
```


The microservices will take some time to become available. Run the following commands to confirm that the ***inventory*** and ***query*** microservices are up and running:
```bash
curl -s http://localhost:9085/health | jq
```

```bash
curl -s http://localhost:9080/health | jq
```

Once the microservices are up and running, you can access the application by making requests to the ***query/systemLoad*** endpoint by using the following ***curl*** command:
```bash
curl -s http://localhost:9080/query/systemLoad | jq
```

When the service is ready, you see an output similar to the following example. This example was formatted for readability:

```

    "highest": {
        "hostname":"30bec2b63a96",       
        ”systemLoad": 6.1
    },     
    "lowest": { 
        "hostname":"55ec2b63a96",    
        ”systemLoad": 0.1
    }
}
```

The JSON output contains a ***highest*** attribute that represents the system with the highest load. Similarly, the ***lowest*** attribute represents the system with the lowest load. The JSON output for each of these attributes contains the ***hostname*** and ***systemLoad*** of the system.

When you are done checking out the application, run the following command to stop the ***query*** microservice. Leave the ***system*** and ***inventory*** services running because they will be used when the application is rebuilt later in the guide:

```bash
docker stop query
```


::page{title="Updating the web client to use an alternative reactive provider"}

Although JAX-RS provides the default reactive provider that returns ***CompletionStage*** types, you can alternatively use another provider that supports other reactive frameworks like [RxJava](https://github.com/ReactiveX/RxJava). The Apache CXF and Eclipse Jersey projects produce such providers. You'll now update the web client to use the Jersey reactive provider for RxJava. With this updated reactive provider, you can write clients that use RxJava objects instead of clients that use only the ***CompletionStage*** interface. These custom objects provide a simpler and faster way for you to create scalable RESTful services with a ***CompletionStage*** interface.

Replace the Maven configuration file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-reactive-rest-client/start/query/pom.xml, or click the following button

::openFile{path="/home/project/guide-reactive-rest-client/start/query/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>query</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- Liberty configuration -->
        <liberty.var.default.http.port>9080</liberty.var.default.http.port>
        <liberty.var.default.https.port>9443</liberty.var.default.https.port>
    </properties>

    <dependencies>
        <!-- Provided dependencies -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>8.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>javax.enterprise.concurrent</groupId>
            <artifactId>javax.enterprise.concurrent-api</artifactId>
            <version>1.1</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>javax.validation</groupId>
            <artifactId>validation-api</artifactId>
            <version>2.0.1.Final</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>3.3</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        <!-- Required dependencies -->
        <dependency>
            <groupId>io.openliberty.guides</groupId>
            <artifactId>models</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <!-- Reactive dependencies -->
        <dependency>
            <groupId>org.glassfish.jersey.core</groupId>
            <artifactId>jersey-client</artifactId>
            <version>2.35</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.ext.rx</groupId>
            <artifactId>jersey-rx-client-rxjava</artifactId>
            <version>2.35</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.ext.rx</groupId>
            <artifactId>jersey-rx-client-rxjava2</artifactId>
            <version>2.35</version>
        </dependency>
        <!-- For tests -->
        <dependency>
            <groupId>org.microshed</groupId>
            <artifactId>microshed-testing-liberty</artifactId>
            <version>0.9.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>mockserver</artifactId>
            <version>1.16.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mock-server</groupId>
            <artifactId>mockserver-client-java</artifactId>
            <version>5.11.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.8.1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
                <configuration>
                    <packagingExcludes>pom.xml</packagingExcludes>
                </configuration>
            </plugin>

            <!-- Liberty plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.8.2</version>
            </plugin>

            <!-- Plugin to run unit tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.2</version>
            </plugin>

            <!-- Plugin to run integration tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>2.22.2</version>
                <executions>
                    <execution>
                        <id>integration-test</id>
                        <goals>
                            <goal>integration-test</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>verify</id>
                        <goals>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```



The ***jersey-rx-client-rxjava*** and ***jersey-rx-client-rxjava2*** dependencies provide the ***RxInvokerProvider*** classes, which are registered to the ***jersey-client*** ***ClientBuilder*** class.

Update the client to accommodate the custom object types that you are trying to return. You'll need to register the type of object that you want inside the client invocation.

Replace the ***InventoryClient*** interface.

> To open the InventoryClient.java file in your IDE, select
> **File** > **Open** > guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/client/InventoryClient.java, or click the following button

::openFile{path="/home/project/guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/client/InventoryClient.java"}



```java
package io.openliberty.guides.query.client;

import java.util.List;
import java.util.Properties;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvokerProvider;

import rx.Observable;

@RequestScoped
public class InventoryClient {

    @Inject
    @ConfigProperty(name = "INVENTORY_BASE_URI", defaultValue = "http://localhost:9085")
    private String baseUri;

    public List<String> getSystems() {
        return ClientBuilder.newClient()
                            .target(baseUri)
                            .path("/inventory/systems")
                            .request()
                            .header(HttpHeaders.CONTENT_TYPE,
                            MediaType.APPLICATION_JSON)
                            .get(new GenericType<List<String>>() { });
    }

    public Observable<Properties> getSystem(String hostname) {
        return ClientBuilder.newClient()
                            .target(baseUri)
                            .register(RxObservableInvokerProvider.class)
                            .path("/inventory/systems")
                            .path(hostname)
                            .request()
                            .header(HttpHeaders.CONTENT_TYPE,
                            MediaType.APPLICATION_JSON)
                            .rx(RxObservableInvoker.class)
                            .get(new GenericType<Properties>() { });
    }
}
```



The return type of the ***getSystem()*** method is now an ***Observable*** object instead of a ***CompletionStage*** interface. [Observable](http://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html) is a collection of data that waits to be subscribed to before it can release any data and is part of RxJava. The ***rx()*** method now needs to contain ***RxObservableInvoker.class*** as an argument. This argument calls the specific invoker, ***RxObservableInvoker***, for the ***Observable*** class that's provided by Jersey. In the ***getSystem()*** method,the ***register(RxObservableInvokerProvider)*** method call registers the ***RxObservableInvoker*** class,which means that the client can recognize the invoker provider.

In some scenarios, a producer might generate more data than the consumers can handle. JAX-RS can deal with cases like these by using the RxJava ***Flowable*** class with backpressure. To learn more about RxJava and backpressure, see [JAX-RS reactive extensions with RxJava backpressure](https://openliberty.io/blog/2019/04/10/jaxrs-reactive-extensions.html).


::page{title="Updating the REST resource to support the reactive JAX-RS client"}

Now that the client methods return the ***Observable*** class, you must update the resource to accommodate these changes.

Replace the ***QueryResource*** class.

> To open the QueryResource.java file in your IDE, select
> **File** > **Open** > guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java, or click the following button

::openFile{path="/home/project/guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java"}



```java
package io.openliberty.guides.query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.openliberty.guides.query.client.InventoryClient;

@ApplicationScoped
@Path("/query")
public class QueryResource {
    
    @Inject
    private InventoryClient inventoryClient;

    @GET
    @Path("/systemLoad")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Properties> systemLoad() {
        List<String> systems = inventoryClient.getSystems();
        CountDownLatch remainingSystems = new CountDownLatch(systems.size());
        final Holder systemLoads = new Holder();
        for (String system : systems) {
            inventoryClient.getSystem(system)
                           .subscribe(p -> {
                                if (p != null) {
                                    systemLoads.updateValues(p);
                                }
                                remainingSystems.countDown();
                           }, e -> {
                                remainingSystems.countDown();
                                e.printStackTrace();
                           });
        }

        try {
            remainingSystems.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return systemLoads.getValues();
    }

    private class Holder {
        private volatile Map<String, Properties> values;

        public Holder() {
            this.values = new ConcurrentHashMap<String, Properties>();
            init();
        }

        public Map<String, Properties> getValues() {
            return this.values;
        }

        public void updateValues(Properties p) {
            final BigDecimal load = (BigDecimal) p.get("systemLoad");

            this.values.computeIfPresent("lowest", (key, curr_val) -> {
                BigDecimal lowest = (BigDecimal) curr_val.get("systemLoad");
                return load.compareTo(lowest) < 0 ? p : curr_val;
            });
            this.values.computeIfPresent("highest", (key, curr_val) -> {
                BigDecimal highest = (BigDecimal) curr_val.get("systemLoad");
                return load.compareTo(highest) > 0 ? p : curr_val;
            });
        }

        private void init() {
            this.values.put("highest", new Properties());
            this.values.put("lowest", new Properties());
            this.values.get("highest").put("hostname", "temp_max");
            this.values.get("lowest").put("hostname", "temp_min");
            this.values.get("highest").put("systemLoad", new BigDecimal(Double.MIN_VALUE));
            this.values.get("lowest").put("systemLoad", new BigDecimal(Double.MAX_VALUE));
        }
    }
}
```



The goal of the ***systemLoad()*** method is to return the system with the largest load and the system with the smallest load. The ***systemLoad*** endpoint first gets all of the hostnames by calling the ***getSystems()*** method.  Then it loops through the hostnames and calls the ***getSystem()*** method on each one.

Instead of using the ***thenAcceptAsync()*** method, ***Observable*** uses the ***subscribe()*** method to asynchronously process data. Thus, any necessary data processing happens inside the ***subscribe()*** method. In this case, the necessary data processing is saving the data in the temporary ***Holder*** class. The ***Holder*** class is used to store the value that is returned from the client because values cannot be returned inside the ***subscribe()*** method.  The highest and lowest load systems are updated in the ***updateValues()*** method.


::page{title="Rebuilding and running the application"}

Run the Maven ***install*** and ***package*** goals from the command-line session in the ***start*** directory:

```bash
mvn -pl query package
```

Run the following command to containerize the ***query*** microservice:

```bash
docker build -t query:1.0-SNAPSHOT query/.
```

Next, use the provided script to restart the query service in a Docker container. 


```bash
./scripts/startQueryContainer.sh
```


The ***query*** microservice will take some time to become available. Run the following command to confirm that the ***query*** microservice is up and running:
```bash
curl -s http://localhost:9080/health | jq
```

Once the ***query*** microservice is up and running, you can access the application by making requests to the ***query/systemLoad*** endpoint using the following ***curl*** command:
```bash
curl -s http://localhost:9080/query/systemLoad | jq
```

Switching to a reactive programming model freed up the thread that was handling your request to ***query/systemLoad***. While the client request is being handled, the thread can handle other work.

When you are done checking out the application, run the following script to stop the application:


```bash
./scripts/stopContainers.sh
```



::page{title="Testing the query microservice"}

A few tests are included for you to test the basic functionality of the ***query*** microservice. If a test failure occurs, then you might have introduced a bug into the code.

Create the ***QueryServiceIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-reactive-rest-client/start/query/src/test/java/it/io/openliberty/guides/query/QueryServiceIT.java
```


> Then, to open the QueryServiceIT.java file in your IDE, select
> **File** > **Open** > guide-reactive-rest-client/start/query/src/test/java/it/io/openliberty/guides/query/QueryServiceIT.java, or click the following button

::openFile{path="/home/project/guide-reactive-rest-client/start/query/src/test/java/it/io/openliberty/guides/query/QueryServiceIT.java"}



```java
package it.io.openliberty.guides.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;
import org.microshed.testing.SharedContainerConfig;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import io.openliberty.guides.query.QueryResource;

@MicroShedTest
@SharedContainerConfig(AppContainerConfig.class)
public class QueryServiceIT {

    @RESTClient
    public static QueryResource queryResource;

    private static String testHost1 = 
        "{" + 
            "\"hostname\" : \"testHost1\"," +
            "\"systemLoad\" : 1.23" +
        "}";
    private static String testHost2 = 
        "{" + 
            "\"hostname\" : \"testHost2\"," +
            "\"systemLoad\" : 3.21" +
        "}";
    private static String testHost3 =
        "{" + 
            "\"hostname\" : \"testHost3\"," +
            "\"systemLoad\" : 2.13" +
        "}";

    @BeforeAll
    public static void setup() throws InterruptedException {
        AppContainerConfig.mockClient.when(HttpRequest.request()
                                         .withMethod("GET")
                                         .withPath("/inventory/systems"))
                                     .respond(HttpResponse.response()
                                         .withStatusCode(200)
                                         .withBody("[\"testHost1\"," + 
                                                    "\"testHost2\"," +
                                                    "\"testHost3\"]")
                                         .withHeader("Content-Type", "application/json"));

        AppContainerConfig.mockClient.when(HttpRequest.request()
                                         .withMethod("GET")
                                         .withPath("/inventory/systems/testHost1"))
                                     .respond(HttpResponse.response()
                                         .withStatusCode(200)
                                         .withBody(testHost1)
                                         .withHeader("Content-Type", "application/json"));

        AppContainerConfig.mockClient.when(HttpRequest.request()
                                         .withMethod("GET")
                                         .withPath("/inventory/systems/testHost2"))
                                     .respond(HttpResponse.response()
                                         .withStatusCode(200)
                                         .withBody(testHost2)
                                         .withHeader("Content-Type", "application/json"));

        AppContainerConfig.mockClient.when(HttpRequest.request()
                                         .withMethod("GET")
                                         .withPath("/inventory/systems/testHost3"))
                                     .respond(HttpResponse.response()
                                         .withStatusCode(200)
                                         .withBody(testHost3)
                                         .withHeader("Content-Type", "application/json"));
    }

    @Test
    public void testSystemLoad() {
        Map<String, Properties> response = queryResource.systemLoad();
        assertEquals(
            "testHost2",
            response.get("highest").get("hostname"),
            "Returned highest system load incorrect"
        );
        assertEquals(
            "testHost1",
            response.get("lowest").get("hostname"),
            "Returned lowest system load incorrect"
        );
    }

}
```



The ***testSystemLoad()*** test case verifies that the ***query*** service can correctly calculate the highest and lowest system loads. 


### Running the tests

Navigate to the ***query*** directory, then verify that the tests pass by running the Maven ***verify*** goal:

```bash
cd query
mvn verify
```

When the tests succeed, you see output similar to the following example:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.query.QueryServiceIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.88 s - in it.io.openliberty.guides.query.QueryServiceIT

Results:

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

::page{title="Summary"}

### Nice Work!

You modified an application to make HTTP requests by using a reactive JAX-RS client with Open Liberty and Jersey's RxJava provider.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-reactive-rest-client*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-reactive-rest-client
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Consuming%20RESTful%20services%20using%20the%20reactive%20JAX-RS%20client&guide-id=cloud-hosted-guide-reactive-rest-client)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-reactive-rest-client/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-reactive-rest-client/pulls)



### Where to next?

* [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html)
* [Consuming RESTful services asynchronously with template interfaces](https://openliberty.io/guides/microprofile-rest-client-async.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
