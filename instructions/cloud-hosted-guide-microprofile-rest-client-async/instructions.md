HELLLOO
---
markdown-version: v1
title: instructions
branch: lab-502-instruction
version-history-start-date: 2020-07-28 12:58:42 UTC
tool-type: theia
---
::page{title="Welcome to the Consuming RESTful services asynchronously with template interfaces guide!"}

Learn how to use MicroProfile Rest Client to invoke RESTful microservices asynchronously over HTTP.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You will learn how to build a MicroProfile Rest Client to access remote RESTful services using asynchronous method calls. You'll update the template interface for a MicroProfile Rest Client to use the ***CompletionStage*** return type. The template interface maps to the remote service that you want to call. A ***CompletionStage*** interface allows you to work with the result of your remote service call asynchronously.

*What is asynchronous programming?*

Imagine asynchronous programming as a restaurant. After you're seated, a waiter takes your order. Then, you must wait a few minutes for your food to be prepared. While your food is being prepared, your waiter may take more orders or serve other tables. After your food is ready, your waiter brings out the food to your table. However, in a synchronous model, the waiter must wait for your food to be prepared before serving any other customers. This method blocks other customers from placing orders or receiving their food.

You can perform lengthy operations, such as input/output (I/O), without blocking with asynchronous methods. The I/O operation can occur in the background and a callback notifies the caller to continue its computation when the original request is complete. As a result, the original thread frees up so it can handle other work rather than wait for the I/O to complete. Revisiting the restaurant analogy, food is prepared asynchronously in the kitchen and your waiter is freed up to attend to other tables.

In the context of REST clients, HTTP request calls can be time consuming. The network might be slow, or maybe the upstream service is overwhelmed and can't respond quickly. These lengthy operations can block the execution of your thread when it's in use and prevent other work from being completed.

The application in this guide consists of three microservices, ***system***, ***inventory***, and ***query***. Every 15 seconds the ***system*** microservice calculates and publishes an event that contains its average system load. The ***inventory*** microservice subscribes to that information so that it can keep an updated list of all the systems and their current system loads. 

![Reactive Inventory System](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-rest-client-async/prod/assets/QueryService.png)


The microservice that you will modify is the ***query*** service. It communicates with the ***inventory*** service to determine which system has the highest system load and which system has the lowest system load. 

The ***system*** and ***inventory*** microservices use MicroProfile Reactive Messaging to send and receive the system load events. If you want to learn more about reactive messaging, see the [Creating Reactive Java Microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html) guide.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-rest-client-async.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microprofile-rest-client-async.git
cd guide-microprofile-rest-client-async
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

::page{title="Updating the template interface of a REST client to use asynchronous methods"}


To begin, run the following command to navigate to the ***start*** directory:
```bash
cd /home/project/guide-microprofile-rest-client-async/start
```

The ***query*** service uses a MicroProfile Rest Client to access the ***inventory*** service. You will update the methods in the template interface for this client to be asynchronous.

Replace the ***InventoryClient*** interface.

> To open the InventoryClient.java file in your IDE, select
> **File** > **Open** > guide-microprofile-rest-client-async/start/query/src/main/java/io/openliberty/guides/query/client/InventoryClient.java, or click the following button

::openFile{path="/home/project/guide-microprofile-rest-client-async/start/query/src/main/java/io/openliberty/guides/query/client/InventoryClient.java"}



```java
package io.openliberty.guides.query.client;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/inventory")
@RegisterRestClient(configKey = "InventoryClient", baseUri = "http://localhost:9085")
public interface InventoryClient extends AutoCloseable {

    @GET
    @Path("/systems")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getSystems();

    @GET
    @Path("/systems/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<Properties> getSystem(@PathParam("hostname") String hostname);

}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


The changes involve the ***getSystem*** method. Change the return type to ***CompletionStage\<Properties\>*** to make the method asynchronous. The method now has the return type of ***CompletionStage\<Properties\>*** so you aren't able to directly manipulate the ***Properties*** inner type. As you will see in the next section, you're able to indirectly use the ***Properties*** by chaining callbacks.

::page{title="Updating a REST resource to asynchronously handle HTTP requests"}

To reduce the processing time, you will update the ***/query/systemLoad*** endpoint to asynchronously send the requests. Multiple client requests will be sent synchronously in a loop. The asynchronous calls do not block the program so the endpoint needs to ensure that all calls are completed and all returned data is processed before proceeding.

Replace the ***QueryResource*** class.

> To open the QueryResource.java file in your IDE, select
> **File** > **Open** > guide-microprofile-rest-client-async/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java, or click the following button

::openFile{path="/home/project/guide-microprofile-rest-client-async/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java"}



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

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.openliberty.guides.query.client.InventoryClient;

@ApplicationScoped
@Path("/query")
public class QueryResource {
    
    @Inject
    @RestClient
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
                                ex.printStackTrace();
                                remainingSystems.countDown();
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



First, the ***systemLoad*** endpoint first gets all the hostnames by calling ***getSystems()***. In the ***getSystem()*** method, multiple requests are sent asynchronously to the ***inventory*** service for each hostname. When the requests return, the ***thenAcceptAsync()*** method processes the returned data with the ***CompletionStage\<Properties\>*** interface.

The ***CompletionStage\<Properties\>*** interface represents a unit of computation. After a computation is complete, it can either be finished or it can be chained with more ***CompletionStage\<Properties\>*** interfaces using the ***thenAcceptAsync()*** method. Exceptions are handled in a callback that is provided to the ***exceptionally()*** method, which behaves like a catch block. When you return a ***CompletionStage\<Properties\>*** type in the resource, it doesn’t necessarily mean that the computation completed and the response was built. JAX-RS responds to the caller after the computation completes.

In the ***systemLoad()*** method a ***CountDownLatch*** object is used to track asynchronous requests. The ***countDown()*** method is called whenever a request is complete. When the ***CountDownLatch*** is at zero, it indicates that all asynchronous requests are complete. By using the ***await()*** method of the ***CountDownLatch***, the program waits for all the asynchronous requests to be complete. When all asynchronous requests are complete, the program resumes execution with all required data processed. 

A ***Holder*** class is used to wrap a variable called ***values*** that has the ***volatile*** keyword. The ***values*** variable is instantiated as a ***ConcurrentHashMap*** object. Together, the ***volatile*** keyword and ***ConcurrentHashMap*** type allow the ***Holder*** class to store system information and safely access it asynchronously from multiple threads.


::page{title="Building and running the application"}

You will build and run the ***system***, ***inventory***, and ***query*** microservices in Docker containers. You can learn more about containerizing microservices with Docker in the [Containerizing microservices](https://openliberty.io/guides/containerize.html) guide.

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
docker build -t query:1.0-SNAPSHOT query/.
```

Next, use the provided ***startContainers*** script to start the application in Docker containers. The script creates containers for Kafka, Zookeeper, and all of the microservices in the project, in addition to a network for the containers to communicate with each other. The script also creates three instances of the ***system*** microservice. 


```bash
./scripts/startContainers.sh
```


The services might take several minutes to become available. You can access the application by making requests to the ***query/systemLoad*** endpoint by running the following curl command:
```bash
curl -s http://localhost:9080/query/systemLoad | jq
```

When the service is ready, you see an output similar to the following example which was formatted for readability. 

```
{
    "highest": {
        "hostname" : "8841bd7d6fcd",
        "systemLoad" : 6.96
    },
    "lowest": {
        "hostname" : "37140ec44c9b",
        "systemLoad" : 6.4
    }
}
```

Switching to an asynchronous programming model freed up the thread that handles requests to the ***inventory*** service. While requests process, the thread can handle other work or requests. In the ***/query/systemLoad*** endpoint, multiple systems are read and compared at once.

When you are done checking out the application, run the following script to stop the application:


```bash
./scripts/stopContainers.sh
```


::page{title="Testing the query microservice"}

You will create an endpoint test to test the basic functionality of the ***query*** microservice. If a test failure occurs, then you might have introduced a bug into the code.

Create the ***QueryServiceIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-rest-client-async/start/query/src/test/java/it/io/openliberty/guides/query/QueryServiceIT.java
```


> Then, to open the QueryServiceIT.java file in your IDE, select
> **File** > **Open** > guide-microprofile-rest-client-async/start/query/src/test/java/it/io/openliberty/guides/query/QueryServiceIT.java, or click the following button

::openFile{path="/home/project/guide-microprofile-rest-client-async/start/query/src/test/java/it/io/openliberty/guides/query/QueryServiceIT.java"}



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
    public void testLoads() {
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


The ***testLoads()*** test case verifies that the ***query*** service can calculate the highest and lowest system loads. 



### Running the tests


Run the following commands to navigate to the ***query*** directory and verify that the tests pass by using the Maven ***verify*** goal:
```bash
cd /home/project/guide-microprofile-rest-client-async/start/query
mvn verify
```

The tests might take a few minutes to complete. When the tests succeed, you see output similar to the following example:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.query.QueryServiceIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 32.123 s - in it.io.openliberty.guides.query.QueryServiceIT

Results:

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

::page{title="Summary"}

### Nice Work!

You have just modified an application to make asynchronous HTTP requests using Open Liberty and MicroProfile Rest Client.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-rest-client-async*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-rest-client-async
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Consuming%20RESTful%20services%20asynchronously%20with%20template%20interfaces&guide-id=cloud-hosted-guide-microprofile-rest-client-async)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-rest-client-async/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-rest-client-async/pulls)



### Where to next?

* [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html)
* [Consuming RESTful services using the reactive JAX-RS client](https://openliberty.io/guides/reactive-rest-client.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
