---
markdown-version: v1
tool-type: theia
---
::page{title="Welcome to the Enabling distributed tracing in microservices with Jaeger guide!"}

Explore how to enable and customize tracing of JAX-RS and non-JAX-RS endpoint methods by using MicroProfile OpenTracing and Jaeger.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.






::page{title="What you'll learn"}

You will learn how to enable automatic tracing for JAX-RS endpoint methods and create custom tracers for non-JAX-RS endpoint methods by using MicroProfile OpenTracing.

OpenTracing is a standard API for instrumenting microservices for distributed tracing. Distributed tracing helps troubleshoot microservices by examining and logging requests as they propagate through a distributed system, allowing developers to tackle the otherwise difficult task of debugging these requests. Without a distributed tracing system in place, analyzing the workflows of operations becomes difficult, particularly in regard to pinpointing when and by whom a request is received or when a response is sent back.

***Tracer*** and ***Span*** are two critical types in the OpenTracing specification. The ***Span*** type is the primary building block of a distributed trace, representing an individual unit of work done in a distributed system. The ***Trace*** type in OpenTracing can be thought of as a directed acyclic graph (DAG) of ***Spans***, where the edges between ***Spans*** are called References. The ***Tracer*** interface creates ***Spans*** and ***Traces*** and understands how to serialize and deserialize their metadata across process boundaries.

MicroProfile OpenTracing enables distributed tracing in microservices. The MicroProfile OpenTracing specification doesn’t address the problem of defining, implementing, or configuring the underlying distributed tracing system. Rather, the specification makes it easier to instrument services with distributed tracing given an existing distributed tracing system.

[Jaeger](https://www.jaegertracing.io/) is an open source distributed tracing system that is compatible with the OpenTracing specification. Jaeger also provides an implementation of ***Tracer*** in the client package that is compatible with MicroProfile OpenTracing.

You’ll configure the provided ***inventory*** and ***system*** services to use Jaeger for distributed tracing with MicroProfile OpenTracing.
You’ll run these services in two separate JVMs made of two server instances to demonstrate tracing in a distributed environment.
If all the components were run on a single server, then any logging software would be sufficient.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-opentracing-jaeger.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microprofile-opentracing-jaeger.git
cd guide-microprofile-opentracing-jaeger
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

Run the following docker command to start Jaeger server:
```bash
docker run -d --name jaeger \
  -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
  -p 5775:5775/udp \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 14268:14268 \
  -p 14250:14250 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.22
```

You can find information about the Jaeger server and instructions for starting the all-in-one executable file in the [Jaeger documentation](https://www.jaegertracing.io/docs/1.22/getting-started/#all-in-one).

Before you proceed, make sure that your Jaeger server is up and running. Click the following button to visit the Jaeger service:
::startApplication{port="16686" display="external" name="Visit Jaeger service" route="/"}

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.


Navigate to the ***finish/inventory*** directory. Run the following Maven goal to build the ***inventory*** service and deploy it to Open Liberty:
```bash
cd /home/project/guide-microprofile-opentracing-jaeger/finish/inventory
mvn liberty:run
```

Open another command-line session and navigate to the ***finish/system*** directory. Run the following Maven goal to build the ***system*** service and deploy it to Open Liberty:
```bash
cd /home/project/guide-microprofile-opentracing-jaeger/finish/system
mvn liberty:run
```

After you see the following message in both command-line sessions, both of your services are ready:

```
The defaultServer server is ready to run a smarter planet.
```


Open another command-line session and run the following curl command from the terminal:
```bash
curl -s http://localhost:9081/inventory/systems/localhost | jq
```

When you visit this endpoint, you make two GET HTTP requests, one to the ***system*** service and one to the **inventory** service. Both of these requests are configured to be traced, so a new trace is recorded in Jaeger.

To view the traces, click the following button to visit the Jaeger service: 
::startApplication{port="16686" display="external" name="Visit Jaeger service" route="/"}

You can view the traces for the inventory or system services under the **Search** tab. Select the services in the **Select a service** menu and click the **Find Traces** button at the end of the section.

If you only see the **jaeger-query** option listed in the dropdown, you might need to wait a little longer and refresh the page to see the application services.

View the traces for ***inventory***. You'll see the following trace:

![Trace result](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-opentracing-jaeger/prod/assets/tracelist.png)


The trace has four spans, three from inventory and one from system. Click the trace to view its details. Under **Service & Operation**, you see the spans in this trace. You can inspect each span by clicking it to reveal more detailed information, such as the time at which a request was received and the time at which a response was sent back.

Verify that there are three spans from ***inventory*** and one span from ***system***:

![Finished application's trace](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-opentracing-jaeger/prod/assets/trace01.png)


After you’re finished reviewing the application, stop the Open Liberty servers by pressing `Ctrl+C` in the command-line sessions where you ran the system and inventory services. Alternatively, you can run the following goals from the ***finish*** directory in another command-line session:


```bash
cd /home/project/guide-microprofile-opentracing-jaeger/finish
mvn -pl system liberty:stop
mvn -pl inventory liberty:stop
```

::page{title="Building the application"}

You need to start the services to see basic traces appear in Jaeger.

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), the server listens for file changes and automatically recompiles and deploys your updates whenever you save a new change.

Open a command-line session and navigate to the ***start/inventory*** directory.
Run the following Maven goal to start the ***inventory*** service in dev mode:

```bash
cd /home/project/guide-microprofile-opentracing-jaeger/start/inventory
mvn liberty:dev
```


Open a command-line session and navigate to the ***start/system*** directory.
Run the following Maven goal to start the ***system*** service in dev mode:

```bash
cd /home/project/guide-microprofile-opentracing-jaeger/start/system
mvn liberty:dev
```

After you see the following message, your application server in dev mode is ready:
```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.


When the servers start, you can find the ***system*** service by running the following curl command:
```bash
curl -s http://localhost:9080/system/properties | jq
```

and the ***inventory*** service by running the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems | jq
```


::page{title="Enabling existing Tracer implementation"}

To collect traces across your systems, you need to implement the OpenTracing ***Tracer*** interface. Jaeger provides a ***Tracer*** implementation for the Jaeger server in the ***jaeger-client*** package.

This package is already added as a dependency for you in your ***pom.xml*** file. It's downloaded and installed automatically into each service when you run a Maven build.

### Configuring the Jaeger client

In a development environment, it is important that every trace is sampled. When every trace is sampled, all spans are available in the Jaeger UI.

The ***JAEGER_SAMPLER_TYPE*** and ***JAEGER_SAMPLER_PARAM*** environment variables are set as Open Liberty ***configuration properties*** to sample all traces.

The ***const*** value for ***JAEGER_SAMPLER_TYPE*** environment variable configures the Jaeger client sampler to make the same sampling decision for each trace, based on the sampler parameter. If the sampler parameter is 1, it samples all traces. If the sampler parameter is 0, it doesn't sample any traces.

The ***1*** value for ***JAEGER_SAMPLER_PARAM*** variable configures the Jaeger sampler to sample all traces.

In a production environment, this configuration might cause a lot of overhead on the application and a lower sampling rate can be used. The different values for client sampling configuration can be found in the [sampling documentation](https://www.jaegertracing.io/docs/1.18/sampling/#client-sampling-configuration).

Similarly, in a production environment, Jaeger might not be running in the same host as the application. In this case, set the hostname of the Jaeger server to the ***JAEGER_AGENT_HOST*** environment variable and set the port that communicates with the Jaeger host to the ***JAEGER_AGENT_PORT*** environment variable.

You can view the configuration environment variables at the [Jaeger Java client documentation](https://github.com/jaegertracing/jaeger-client-java/tree/master/jaeger-core#configuration-via-environment).




::page{title="Enabling and disabling distributed tracing"}

The [MicroProfile OpenTracing feature](https://github.com/eclipse/microprofile-opentracing) enables tracing of all JAX-RS endpoint methods by default. To further control and customize these traces, use the ***@Traced*** annotation to enable and disable tracing of particular methods. You can also inject a custom ***Tracer*** object to create and customize spans.

This feature is already enabled in the ***inventory*** and ***system*** configuration files.

### Enabling distributed tracing without code instrumentation

Because tracing of all JAX-RS endpoint methods is enabled by default, you only need to enable the ***MicroProfile OpenTracing*** feature in the ***server.xml*** file to see some basic traces in Jaeger.

The OpenTracing API is exposed as a third-party API in Open Liberty. To add the visibility of OpenTracing APIs to the application, add ***third-party*** to the types of API packages that this class loader supports. Instead of explicitly configuring a list of API packages that includes ***third-party***, set the ***+third-party*** value to the ***apiTypeVisibility*** attribute in the ***classLoader*** configuration. This configuration adds ***third-party*** to the default list of API package types that are supported.


Make sure that your services are running. Then, point your browser to any of the services' endpoints and check your Jaeger server for traces.

### Enabling explicit distributed tracing

Use the ***@Traced*** annotation to define explicit span creation for specific classes and methods. If you place the annotation on a class, then the annotation is automatically applied to all methods within that class. If you place the annotation on a method, then the annotation overrides the class annotation if one exists.

The ***@Traced*** annotation can be configured with the following two parameters:

* The ***value=[true|false]*** parameter indicates whether a particular class or method is traced. For example, while all JAX-RS endpoint methods are traced by default, you can disable their tracing by using the ***@Traced(false)*** annotation. This parameter is set to ***true*** by default.
* The ***operationName=\<Span name\>*** parameter indicates the name of the span that is assigned to the method that is traced. If you omit this parameter, the span is named with the ***\<package name\>.\<class name\>.\<method name\>*** format. If you use this parameter at a class level, then all methods within that class have the same span name unless they are explicitly overridden by another ***@Traced*** annotation.

Replace the ***InventoryManager*** class.

> To open the InventoryManager.java file in your IDE, select
> **File** > **Open** > guide-microprofile-opentracing-jaeger/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java, or click the following button

::openFile{path="/home/project/guide-microprofile-opentracing-jaeger/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java"}



```java
package io.openliberty.guides.inventory;

import java.util.ArrayList;
import java.util.Properties;
import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Collections;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.Span;

@ApplicationScoped
public class InventoryManager {

    @Inject
    @ConfigProperty(name = "system.http.port")
    int SYSTEM_PORT;

    private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());
    private SystemClient systemClient = new SystemClient();
    @Inject Tracer tracer;

    public Properties get(String hostname) {
        systemClient.init(hostname, SYSTEM_PORT);
        Properties properties = systemClient.getProperties();
        return properties;
    }

    public void add(String hostname, Properties systemProps) {
        Properties props = new Properties();
        props.setProperty("os.name", systemProps.getProperty("os.name"));
        props.setProperty("user.name", systemProps.getProperty("user.name"));

        SystemData system = new SystemData(hostname, props);
    }

    @Traced(operationName = "InventoryManager.list")
    public InventoryList list() {
        return new InventoryList(systems);
    }

    int clear() {
        int propertiesClearedCount = systems.size();
        systems.clear();
        return propertiesClearedCount;
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


Enable tracing of the ***list()*** non-JAX-RS endpoint method by updating ***@Traced*** as shown.


Run the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems | jq
```

Check your Jaeger server. If you have the Jaeger UI open from a previous step, refresh the page. Select the ***inventory*** traces and click the **Find Traces** button.
::startApplication{port="16686" display="external" name="Visit Jaeger service" route="/"}

You see a new trace record that is two spans long. One span is for the ***listContents()*** JAX-RS endpoint method in the ***InventoryResource*** class, and the other span is for the ***list()*** method in the ***InventoryManager*** class.

Verify that you see the following spans:

![Explicit trace span](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-opentracing-jaeger/prod/assets/trace02.png)



### Disable automatic distributed tracing

You can use the ***@Traced*** annotation with a value of ***false*** to disable automatic distributed tracing of JAX-RS endpoint methods.

Replace the ***InventoryResource*** class.

> To open the InventoryResource.java file in your IDE, select
> **File** > **Open** > guide-microprofile-opentracing-jaeger/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java, or click the following button

::openFile{path="/home/project/guide-microprofile-opentracing-jaeger/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java"}



```java
package io.openliberty.guides.inventory;

import java.util.Properties;

import org.eclipse.microprofile.opentracing.Traced;

import io.openliberty.guides.inventory.model.InventoryList;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
                           .entity("{ \"error\" : \"Unknown hostname or the system "
                           + "service may not be running on " + hostname + "\" }")
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

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearContents() {
        int cleared = manager.clear();

        if (cleared == 0) {
            return Response.status(Response.Status.NOT_MODIFIED)
                    .build();
        }
        return Response.status(Response.Status.OK)
                .build();
    }
}
```



Disable tracing of the ***listContents()*** JAX-RS endpoint method by setting ***@Traced(false)***.


Run the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems | jq
```

Check your Jaeger server. If you have the Jaeger UI open from a previous step, refresh the page. Select the **inventory** traces and click the **Find Traces** button. You see a new trace record that is just one span long for the remaining **list()** method in the **InventoryManager** class.
::startApplication{port="16686" display="external" name="Visit the Jaeger service" route="/"}

Verify that you see the following span:

![Disable trace span](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-opentracing-jaeger/prod/assets/trace03.png)



### Injecting a custom Tracer object

The MicroProfile OpenTracing specification also makes the underlying OpenTracing ***Tracer*** instance available for use. You can access the configured ***Tracer*** by injecting it into a bean by using the ***@Inject*** annotation from the Contexts and Dependency Injections API.

Inject the ***Tracer*** object into the ***inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java*** file. Then, use it to define a new child scope in the ***add()*** call.

Replace the ***InventoryManager*** class.

> To open the InventoryManager.java file in your IDE, select
> **File** > **Open** > guide-microprofile-opentracing-jaeger/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java, or click the following button

::openFile{path="/home/project/guide-microprofile-opentracing-jaeger/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java"}



```java
package io.openliberty.guides.inventory;

import java.util.ArrayList;
import java.util.Properties;
import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Collections;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.Span;

@ApplicationScoped
public class InventoryManager {

    @Inject
    @ConfigProperty(name = "system.http.port")
    int SYSTEM_PORT;

    private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());
    private SystemClient systemClient = new SystemClient();
    @Inject Tracer tracer;

    public Properties get(String hostname) {
        systemClient.init(hostname, SYSTEM_PORT);
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
            try (Scope childScope = tracer.activateSpan(span)) {
                systems.add(system);
            } finally {
                span.finish();
            }
        }
    }

    @Traced(operationName = "InventoryManager.list")
    public InventoryList list() {
        return new InventoryList(systems);
    }

    int clear() {
        int propertiesClearedCount = systems.size();
        systems.clear();
        return propertiesClearedCount;
    }
}
```



This ***try*** block is called a ***try-with-resources*** statement, meaning that the ***childScope*** object is closed at the end of the statement. It's good practice to define custom spans inside such statements. Otherwise, any exceptions that are thrown before the span closes will leak the active span.


Run the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems/localhost | jq
```

Check your Jaeger server. If you have the Jaeger UI open from a previous step, refresh the page. Select the **inventory** traces and click the **Find Traces** button.
::startApplication{port="16686" display="external" name="Visit the Jaeger service" route="/"}

Verify that there are three spans from ***inventory*** and one span from ***system***:

![Trace with custom span](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-opentracing-jaeger/prod/assets/trace01.png)


This simple example shows what you can do with the injected ***Tracer*** object. More configuration options are available to you, including setting a timestamp for when a span was created and destroyed. However, these options require an implementation of their own, which doesn't come as a part of the Jaeger user feature that is provided. In a real-world scenario, implement all the OpenTracing interfaces that you deem necessary, which might include the ***SpanBuilder*** interface. You can use this interface for span creation and customization, including setting timestamps.




::page{title="Testing the services"}

No automated tests are provided to verify the correctness of the traces. Manually verify these traces by viewing them on the Jaeger server.

A few tests are included for you to test the basic functionality of the services. If a test failure occurs, then you might have introduced a bug into the code.

### Running the tests

Since you started Open Liberty in dev mode, run the tests for the system and inventory services by pressing the ***enter/return*** key in the command-line sessions where you started the services.

When you are done checking out the services, exit dev mode by pressing `Ctrl+C` in the shell sessions where you ran the ***system*** and ***inventory*** services,  or by typing ***q*** and then pressing the ***enter/return key***.


Finally, stop the ***Jaeger*** service that you started in the previous step.
```bash
docker stop jaeger
docker rm jaeger
```


::page{title="Summary"}

### Nice Work!

You just used MicroProfile OpenTracing in Open Liberty to customize how and which traces are delivered to Jaeger.


Try out one of the related MicroProfile guides. These guides demonstrate more technologies that you can learn to expand on what you built in this guide.


### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-opentracing-jaeger*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-opentracing-jaeger
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Enabling%20distributed%20tracing%20in%20microservices%20with%20Jaeger&guide-id=cloud-hosted-guide-microprofile-opentracing-jaeger)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-opentracing-jaeger/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-opentracing-jaeger/pulls)



### Where to next?

* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Enabling distributed tracing in microservices with Zipkin](https://openliberty.io/guides/microprofile-opentracing.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
