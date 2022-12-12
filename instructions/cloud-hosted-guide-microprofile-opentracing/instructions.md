HELLLOO
---
markdown-version: v1
title: instructions
branch: lab-497-instruction
version-history-start-date: 2020-10-05 09:33:58 UTC
tool-type: theia
---
::page{title="Welcome to the Enabling distributed tracing in microservices with Zipkin guide!"}

Explore how to enable and customize tracing of JAX-RS and non-JAX-RS methods by using MicroProfile OpenTracing and the Zipkin tracing system.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You'll learn how to enable automatic tracing for JAX-RS methods and how to create custom tracers for non-JAX-RS methods by using MicroProfile OpenTracing.

OpenTracing is a standard API for instrumenting microservices for distributed tracing. Distributed tracing helps troubleshoot microservices by examining and logging requests as they propagate through a distributed system. Distributed tracing allows developers to tackle the otherwise difficult task of debugging these requests. Without a distributed tracing system in place, analyzing the workflows of operations becomes difficult. Pinpointing when and where a request is received and when responses are sent becomes difficult.

MicroProfile OpenTracing enables distributed tracing in microservices without adding any explicit distributed tracing code to the application. Note that the MicroProfile OpenTracing specification does not address the problem of defining, implementing, or configuring the underlying distributed tracing system. Rather, the specification makes it easier to instrument services with distributed tracing given an existing distributed tracing system.

You'll configure the provided ***inventory*** and ***system*** services to use distributed tracing with MicroProfile OpenTracing. You'll run these services in two separate JVMs made of two server instances to demonstrate tracing in a distributed environment. If all the components were to run on a single server, then any logging software would do the trick.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-opentracing.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microprofile-opentracing.git
cd guide-microprofile-opentracing
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

For this guide, Zipkin is used as the distributed tracing system. You can find the installation instructions for Zipkin at the Zipkin [quickstart page](https://zipkin.io/pages/quickstart.html). You're not required to use Zipkin. You may choose to use another tracing system. However, this guide is written using Zipkin. If you use a different tracing system, the required instructions may differ.


Start Zipkin by running the following command:
```bash
docker run -d --name zipkin -p 9411:9411 openzipkin/zipkin
```

Before you continue, make sure your Zipkin server is up and running. Click the following button to visit Zipkin service at port ***9411***.
::startApplication{port="9411" display="external" name="Visit Zipkin" route="/"}

### Try what you'll build

The ***finish*** directory in the root directory of this guide contains two services that are configured to use MicroProfile OpenTracing. Give them a try before you continue.

To try out the services, navigate to the ***finish*** directory and run the Maven ***install*** phase to build the services
```bash
cd finish
mvn install
```

Then, run the Maven ***liberty:start-server*** goal to start them in two Open Liberty servers:
```bash
mvn liberty:start-server
```


Make sure your Zipkin server is running and run the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems/localhost | jq
```

When you make this curl request, you make two HTTP GET requests, one to the ***system*** service and one to the ***inventory*** service. Because tracing is configured for both these requests, a new trace is recorded in Zipkin. Visit the Zipkin service. Run an empty query and sort the traces by latest start time first. 
::startApplication{port="9411" display="external" name="Visit Zipkin" route="/"}

Verify that the new trace contains three spans with the following names:

* ***get:io.openliberty.guides.inventory.inventoryresource.getpropertiesforhost***
* ***get:io.openliberty.guides.system.systemresource.getproperties***
* ***add() span***

You can inspect each span by clicking it to reveal more detailed information, such as the time at which the request was received and the time at which a response was sent back.

If you examine the other traces, you might notice a red trace entry, which indicates the span caught an error. In this case, one of the tests accesses the ***/inventory/systems/badhostname*** endpoint, which is invalid, so an error is thrown. This behavior is expected.

When you're done checking out the services, stop both Open Liberty servers using the Maven ***liberty:stop-server*** goal:

```bash
mvn liberty:stop-server
```


::page{title="Running the services"}

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-microprofile-opentracing/start
```

You'll need to start the services to see basic traces appear in Zipkin. So, before you proceed, build and start the provided ***system*** and ***inventory*** services in the starting project by running the Maven ***install*** goal:

```bash
mvn install
```

Then, run the ***liberty:start-server*** goal:

```bash
mvn liberty:start-server
```


When the servers start, you can access the **system** service by running the following curl command:
```bash
curl -s http://localhost:9080/system/properties | jq
```

and access the ***inventory*** service by running the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems | jq
```


::page{title="Existing Tracer implementation"}

To collect traces across your systems, you need to implement the OpenTracing ***Tracer*** interface. For this guide, you can access a bare-bones ***Tracer*** implementation for the Zipkin server in the form of a user feature for Open Liberty.

This feature is already configured for you in your ***pom.xml*** and ***server.xml*** files. It's automatically downloaded and installed into each service when you run a Maven build. You can find the ***opentracingZipkin*** feature enabled in your ***server.xml*** file.

The ***download-maven-plugin*** Maven plug-in in your ***pom.xml*** downloads and installs the ***opentracingZipkin*** feature.

If you want to install this feature yourself, see [Enabling distributed tracing](https://www.ibm.com/docs/en/was-liberty/base?topic=environment-enabling-distributed-tracing) in IBM Documentation.




::page{title="Enabling distributed tracing"}

The MicroProfile OpenTracing feature enables tracing of all JAX-RS methods by default. To further control and customize these traces, use the ***@Traced*** annotation to enable and disable tracing of particular methods. You can also inject a custom ***Tracer*** object to create and customize spans.

### Enabling distributed tracing without code instrumentation

Because tracing is enabled by default for all JAX-RS methods, you need to enable only the ***mpOpenTracing*** feature and the ***usr:opentracingZipkin*** user feature in the ***server.xml*** file to see some basic traces in Zipkin.

Both of these features are already enabled in the ***inventory*** and ***system*** configuration files.

Make sure your services are running. Then, point your browser to any of their endpoints and check your Zipkin server for traces.


### Enabling explicit distributed tracing

The ***@Traced*** annotation defines explicit span creation for specific classes and methods. If you place the annotation on a class, then it's automatically applied to all methods within that class. If you place the annotation on a method, then it overrides the class annotation if one exists.

Enable tracing of the ***list()*** non-JAX-RS method by adding the ***@Traced*** annotation to the method.

Replace the ***InventoryManager*** class.

> To open the InventoryManager.java file in your IDE, select
> **File** > **Open** > guide-microprofile-opentracing/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java, or click the following button

::openFile{path="/home/project/guide-microprofile-opentracing/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java"}



```java
package io.openliberty.guides.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
public class InventoryManager {

    private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());
    private SystemClient systemClient = new SystemClient();

    public Properties get(String hostname) {
        systemClient.init(hostname, 9080);
        Properties properties = systemClient.getProperties();
        return properties;
    }

    public void add(String hostname, Properties systemProps) {
        Properties props = new Properties();
        props.setProperty("os.name", systemProps.getProperty("os.name"));
        props.setProperty("user.name", systemProps.getProperty("user.name"));

        SystemData system = new SystemData(hostname, props);
    }

    @Traced(value = true, operationName = "InventoryManager.list")
    public InventoryList list() {
        return new InventoryList(systems);
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


The ***@Traced*** annotation can be configured with the following two parameters:

* The ***value=[true|false]*** parameter indicates whether a particular class or method is traced. For example, while all JAX-RS methods are traced by default, you can disable their tracing by using the ***@Traced(false)*** annotation. This parameter is set to ***true*** by default.
* The ***operationName=\<Span name\>*** parameter indicates the name of the span that is assigned to the particular method that is traced. If you omit this parameter, the span will be named with the following form: ***\<package name\>.\<class name\>.\<method name\>***. If you use this parameter at a class level, then all methods within that class will have the same span name unless they're explicitly overridden by another ***@Traced*** annotation.

Next, run the following command from the ***start*** directory to recompile your services. 
```bash
mvn compile
```


Run the following curl command, check your Zipkin server, and sort the traces by newest first:
```bash
curl -s http://localhost:9081/inventory/systems | jq
```

Look for a new trace record that is two spans long with one span for the ***listContents()*** JAX-RS method in the ***InventoryResource*** class and another span for the ***list()*** method in the ***InventoryManager*** class. Verify that these spans have the following names:

* ***get:io.openliberty.guides.inventory.inventoryresource.listcontents***
* ***inventorymanager.list***

Now, disable tracing on the ***InventoryResource*** class by setting ***@Traced(false)*** on the ***listContents()*** JAX-RS method.

Replace the ***InventoryResource*** class.

> To open the InventoryResource.java file in your IDE, select
> **File** > **Open** > guide-microprofile-opentracing/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java, or click the following button

::openFile{path="/home/project/guide-microprofile-opentracing/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java"}



```java
package io.openliberty.guides.inventory;

import java.util.Properties;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.opentracing.Traced;

import io.openliberty.guides.inventory.model.InventoryList;

@RequestScoped
@Path("/systems")
public class InventoryResource {

    @Inject InventoryManager manager;

    @GET
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPropertiesForHost(@PathParam("hostname") String hostname) {
        Properties props = manager.get(hostname);
        if (props == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{ \"error\" : \"Unknown hostname or the system service " 
                           + "may not be running on " + hostname + "\" }")
                           .build();
        }
        manager.add(hostname, props);
        return Response.ok(props).build();
    }
    
    @GET
    @Traced(false)
    @Produces(MediaType.APPLICATION_JSON)
    public InventoryList listContents() {
        return manager.list();
    }
}
```



Again, run the ***mvn compile*** command from the ***start*** directory to recompile your services:
```bash
mvn compile
```


Run the following curl command again, check your Zipkin server, and sort the traces by newest first:
```bash
curl -s http://localhost:9081/inventory/systems | jq
```

Look for a new trace record that is just one span long for the remaining ***list()*** method in the ***InventoryManager*** class. Verify that this span has the following name:

* ***inventorymanager.list***


### Injecting a custom Tracer object

The MicroProfile OpenTracing specification also makes the underlying OpenTracing ***Tracer*** instance available. The configured ***Tracer*** is accessed by injecting it into a bean by using the ***@Inject*** annotation from the Contexts and Dependency Injections API.

After injecting it, the ***Tracer*** will be used to build a ***Span***. The ***Span*** will be activated and used in a ***Scope***.

Replace the ***InventoryManager*** class.

> To open the InventoryManager.java file in your IDE, select
> **File** > **Open** > guide-microprofile-opentracing/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java, or click the following button

::openFile{path="/home/project/guide-microprofile-opentracing/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java"}



```java
package io.openliberty.guides.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
public class InventoryManager {

    private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());
    private SystemClient systemClient = new SystemClient();
    @Inject Tracer tracer;

    public Properties get(String hostname) {
        systemClient.init(hostname, 9080);
        Properties properties = systemClient.getProperties();
        return properties;
    }

    public void add(String hostname, Properties systemProps) {
        Properties props = new Properties();
        props.setProperty("os.name", systemProps.getProperty("os.name"));
        props.setProperty("user.name", systemProps.getProperty("user.name"));

        SystemData system = new SystemData(hostname, props);
        if (!systems.contains(system)) {
            Span span = tracer.buildSpan("add() Span").start();
            try (Scope childScope = tracer.scopeManager()
                                          .activate(span)
                ) {
                systems.add(system);
            } finally {
                span.finish();
            }
        }
    }

    @Traced(value = true, operationName = "InventoryManager.list")
    public InventoryList list() {
        return new InventoryList(systems);
    }
}
```



The ***Scope*** is used in a ***try*** block. The ***try*** block that you see here is called a ***try-with-resources*** statement, meaning that the ***Scope*** object is closed at the end of the statement. Defining custom spans inside such statements is a good practice. Otherwise, any exceptions that are thrown before the span is closed will leak the active span. The ***finish()*** method sets the ending timestamp and records the span.

Next, run the following command from the ***start*** directory to recompile your services. 
```bash
mvn compile
```


Run the following curl command, check your Zipkin server, and sort the traces by newest first:
```bash
curl -s http://localhost:9081/inventory/systems/localhost | jq
```

Look for two new trace records, one for the ***system*** service and one for the ***inventory*** service. The ***system*** trace contains one span for the ***getProperties()*** method in the ***SystemResource*** class. The ***inventory*** trace contains two spans. The first span is for the ***getPropertiesForHost()*** method in the ***InventoryResource*** class. The second span is the custom span that you created around the ***add()*** call. Verify that all of these spans have the following names:

The ***system*** trace:

* ***get:io.openliberty.guides.system.systemresource.getproperties***

The ***inventory*** trace:

* ***get:io.openliberty.guides.inventory.inventoryresource.getpropertiesforhost***
* ***add() span***

This simple example shows what you can do with the injected ***Tracer*** object. More configuration options are available, including setting a timestamp for when a span was created and destroyed. However, these options require an implementation of their own, which does not come as a part of the Zipkin user feature that is provided. In a real-world scenario, implement all the OpenTracing interfaces that you consider necessary, which might include the ***SpanBuilder*** interface. You can use this interface for span creation and customization, including setting timestamps.




::page{title="Testing the services"}

No automated tests are provided to verify the correctness of the traces. Manually verify these traces by viewing them on the Zipkin server.

A few tests are included for you to test the basic functionality of the services. If a test failure occurs, then you might have introduced a bug into the code. These tests will run automatically as a part of the Maven build process when you run the ***mvn install*** command. You can also run these tests separately from the build by using the ***mvn verify*** command, but first make sure the servers are stopped.

When you're done checking out the services, stop the server by using the Maven
***liberty:stop-server*** goal:

```bash
mvn liberty:stop-server
```

Stop the Zipkin service by running the following command:
```bash
docker stop zipkin
```


::page{title="Summary"}

### Nice Work!

You have just used MicroProfile OpenTracing in Open Liberty to customize how and which traces are delivered to Zipkin.


Feel free to try one of the related MicroProfile guides. They demonstrate additional technologies that you can learn to expand on top of what you built here.

### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-opentracing*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-opentracing
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Enabling%20distributed%20tracing%20in%20microservices%20with%20Zipkin&guide-id=cloud-hosted-guide-microprofile-opentracing)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-opentracing/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-opentracing/pulls)



### Where to next?

* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Enabling distributed tracing in microservices with Jaeger](https://openliberty.io/guides/microprofile-opentracing-jaeger.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
