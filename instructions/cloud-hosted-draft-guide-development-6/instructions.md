
# **Welcome to the Consuming RESTful services using the reactive JAX-RS client guide!**

Learn how to use a reactive JAX-RS client to asynchronously invoke RESTful microservices over HTTP.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



# **What you'll learn**

You'll first learn how to create a reactive JAX-RS client application using the default JAX-RS reactive provider APIs.
You will then learn how to improve the application to take advantage of the RxJava reactive extensions with a
pluggable reactive provider that's published by [Eclipse Jersey](https://eclipse-ee4j.github.io/jersey).
The JAX-RS client is an API used to communicate with RESTful web services. 
The API makes it easy to consume a web service that is exposed by using the HTTP protocol,
which means that you can efficiently implement client-side applications. 
The reactive client extension to JAX-RS is an API that enables you to use the reactive programming model when using the JAX-RS client.

Reactive programming is an extension of asynchronous programming and focuses on the flow of data through data streams. 
Reactive applications process data when it becomes available and respond to requests as soon as processing is complete. 
The request to the application and response from the application are decoupled so that
the application is not blocked from responding to other requests in the meantime. 
Because reactive applications can run faster than synchronous applications, they provide a much smoother user experience.

The application in this guide demonstrates how the JAX-RS client accesses remote RESTful services by using asynchronous method calls. 
You’ll first look at the supplied client application that uses the JAX-RS default **CompletionStage**-based provider. 
Then, you’ll modify the client application to use Jersey’s RxJava provider, which is an alternative JAX-RS reactive provider. 
Both Jersey and Apache CXF provide third-party reactive libraries for RxJava and were tested for use in Open Liberty.

The application that you will be working with consists of three microservices, **system**, **inventory**, and **query**. 
Every 15 seconds, the **system** microservice calculates and publishes an event that contains its current average system load. 
The **inventory** microservice subscribes to that information so that it can keep an updated list of all the systems
and their current system loads.

![Reactive Query Service](https://raw.githubusercontent.com/OpenLiberty/guide-reactive-rest-client/master/assets/QueryService.png)


The microservice that you will modify is the **query** service. It communicates with the **inventory** service 
to determine which system has the highest system load and which system has the lowest system load.

The **system** and **inventory** microservices use MicroProfile Reactive Messaging to send and receive the system load events.
If you want to learn more about reactive messaging, see the 
[Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html) guide.

# **Getting started**

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-reactive-rest-client.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-reactive-rest-client.git
cd guide-reactive-rest-client
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.

# **Creating a web client using the default JAX-RS API**


Navigate to the **start** directory to begin.

JAX-RS provides a default reactive provider that you can use to create a reactive REST client using the **CompletionStage** interface.

Create an **InventoryClient** class, which is used to retrieve inventory data,
and a **QueryResource** class, which queries data from the **inventory** service.

Create the **InventoryClient** interface.

> Run the following touch command in your terminal
```
touch /home/project/guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/client/InventoryClient.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/client/InventoryClient.java



The **getSystem()** method returns the **CompletionStage** interface. 
This interface represents a unit or stage of a computation.
When the associated computation completes, the value can be retrieved. 
The **rx()** method calls the **CompletionStage** interface. 
It retrieves the **CompletionStageRxInvoker** class and allows these methods to
function correctly with the **CompletionStage** interface return type.

Create the **QueryResource** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java




The **systemLoad** endpoint asynchronously processes the data that is
retrieved by the **InventoryClient** interface and serves that data after all of the services respond. 
The **thenAcceptAsync()** and **exceptionally()**
methods together behave like an asynchronous try-catch block. 
The data is processed in the **thenAcceptAsync()** method only after the **CompletionStage** interface finishes retrieving it. 
When you return a **CompletionStage** type in the resource, it doesn’t necessarily mean that the computation completed and the response was built.

A **CountDownLatch** object is used to track how many asynchronous requests are being waited on. 
After each thread is completed, the **countdown()** method
counts the **CountDownLatch** object down towards **0**. 
This means that the value returns only after the thread that's retrieving the value is complete.
The **await()** method stops and waits until all of the requests are complete. 
While the countdown completes, the main thread is free to perform other tasks. 
In this case, no such task is present.

# **Building and running the application**

The **system**, **inventory**, and **query** microservices will be built in Docker containers. 
If you want to learn more about Docker containers,
check out the [Containerizing microservices](https://openliberty.io/guides/containerize.html) guide.

Start your Docker environment.

To build the application, run the Maven **install** and **package** goals from the command-line session in the **start** directory:

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
docker build -t query:1.0-SNAPSHOT query/.
```
{: codeblock}


Next, use the provided script to start the application in Docker containers.
The script creates a network for the containers to communicate with each other.
It creates containers for Kafka, Zookeeper, and all of the microservices in the project.


```
./scripts/startContainers.sh
```
{: codeblock}

The services will take some time to become available.
You can access the application by using the following `curl` command:

```
curl http://localhost:9080/query/systemLoad
```
{: codeblock}

When the service is ready, you see an output similar to the following example. 
This example was formatted for readability:

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

The JSON output contains a **highest** attribute that represents the system with the highest load.
Similarly, the **lowest** attribute represents the system with the lowest load. 
The JSON output for each of these attributes contains the **hostname** and **systemLoad** of the system.

When you are done checking out the application, run the following command to stop the **query** microservice. 
Leave the **system** and **inventory** services running because they will be used when the application is rebuilt later in the guide:

```
docker stop query
```
{: codeblock}



# **Updating the web client to use an alternative reactive provider**


Although JAX-RS provides the default reactive provider that returns **CompletionStage** types,
you can alternatively use another provider that supports other reactive frameworks like [RxJava](https://github.com/ReactiveX/RxJava). 
The Apache CXF and Eclipse Jersey projects produce such providers.
You'll now update the web client to use the Jersey reactive provider for RxJava. 
With this updated reactive provider, you can write clients that use RxJava objects instead of clients that use only the **CompletionStage** interface. 
These custom objects provide a simpler and faster way for you to create scalable RESTful services with a **CompletionStage** interface.

Replace the Maven configuration file.

> From the menu of the IDE, select 
> **File** > **Open** > guide-reactive-rest-client/start/query/pom.xml



The **jersey-rx-client-rxjava** and
**jersey-rx-client-rxjava2** dependencies provide the **RxInvokerProvider** classes,
which are registered to the **jersey-client** **ClientBuilder** class.

Update the client to accommodate the custom object types that you are trying to return. 
You'll need to register the type of object that you want inside the client invocation.

Replace the **InventoryClient** interface.

> From the menu of the IDE, select 
> **File** > **Open** > guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/client/InventoryClient.java




The return type of the **getSystem()** method is now an **Observable** object instead of a **CompletionStage** interface. 
[Observable](http://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html) is a
collection of data that waits to be subscribed to before it can release any data and is part of RxJava. 
The **rx()** method now needs to contain **RxObservableInvoker.class** as an argument.
This argument calls the specific invoker, **RxObservableInvoker**, for the **Observable** class that's provided by Jersey. 
In the **getSystem()** method,
the **register(RxObservableInvokerProvider)** method call registers the **RxObservableInvoker** class,
which means that the client can recognize the invoker provider.

In some scenarios, a producer might generate more data than the consumers can handle. 
JAX-RS can deal with cases like these by using the RxJava **Flowable** class with backpressure. 
To learn more about RxJava and backpressure, see
[JAX-RS reactive extensions with RxJava backpressure](https://openliberty.io/blog/2019/04/10/jaxrs-reactive-extensions.html).

# **Updating the REST resource to support the reactive JAX-RS client**


Now that the client methods return the **Observable** class, you must update the resource to accommodate these changes.

Replace the **QueryResource** class.

> From the menu of the IDE, select 
> **File** > **Open** > guide-reactive-rest-client/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java



The goal of the **systemLoad()** method is to return the system with the largest load and the system with the smallest load. 
The **systemLoad** endpoint first gets all of the hostnames by calling the **getSystems()** method. 
Then it loops through the hostnames and calls the **getSystem()** method on each one.

Instead of using the **thenAcceptAsync()** method,
**Observable** uses the **subscribe()** method to asynchronously process data. 
Thus, any necessary data processing happens inside the **subscribe()** method. 
In this case, the necessary data processing is saving the data in the temporary **Holder** class.
The **Holder** class is used to store the value that is returned from the client because values cannot be returned inside the **subscribe()** method. 
The highest and lowest load systems are updated in the **updateValues()** method.

# **Rebuilding and running the application**

Run the Maven **install** and **package** goals from the command-line session in the **start** directory:

```
mvn -pl query package
```
{: codeblock}


Run the following command to containerize the **query** microservice:

```
docker build -t query:1.0-SNAPSHOT query/.
```
{: codeblock}


Next, use the provided script to restart the query service in a Docker container. 


```
./scripts/startQueryContainer.sh
```
{: codeblock}

You can access the application by making requests to the `query/systemLoad` endpoint using
the following `curl` command:

```
curl http://localhost:9080/query/systemLoad
```
{: codeblock}

Switching to a reactive programming model freed up the thread that was handling your request to **query/systemLoad**. 
While the client request is being handled, the thread can handle other work.

When you are done checking out the application, run the following script to stop the application:



```
./scripts/stopContainers.sh
```
{: codeblock}


# **Testing the query microservice**

A few tests are included for you to test the basic functionality of the **query** microservice. 
If a test failure occurs, then you might have introduced a bug into the code.

Create the **QueryServiceIT** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-reactive-rest-client/start/query/src/test/java/it/io/openliberty/guides/query/QueryServiceIT.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-reactive-rest-client/start/query/src/test/java/it/io/openliberty/guides/query/QueryServiceIT.java



The **testSystemLoad()** test case verifies that the
**query** service can correctly calculate the highest and lowest system loads. 




<br/>
### **Running the tests**

Navigate to the **query** directory, then verify that the tests pass by running the Maven **verify** goal:

```
cd query
mvn verify
```
{: codeblock}


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

# **Summary**

## **Nice Work!**

You modified an application to make HTTP requests by using a reactive JAX-RS client with Open Liberty and Jersey's RxJava provider.



<br/>
## **Clean up your environment**


Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-reactive-rest-client** project by running the following commands:

```
cd /home/project
rm -fr guide-reactive-rest-client
```
{: codeblock}

<br/>
## **What did you think of this guide?**

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Consuming%20RESTful%20services%20using%20the%20reactive%20JAX-RS%20client&guide-id=cloud-hosted-guide-reactive-rest-client)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

<br/>
## **What could make this guide better?**

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-reactive-rest-client/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-reactive-rest-client/pulls)



<br/>
## **Where to next?**

* [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html)
* [Consuming RESTful services asynchronously with template interfaces](https://openliberty.io/guides/microprofile-rest-client-async.html)


<br/>
## **Log out of the session**

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.