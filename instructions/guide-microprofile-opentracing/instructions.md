
# Enabling distributed tracing in microservices with Zipkin
## What you'll learn

You will learn how to enable automatic tracing for JAX-RS methods as well as create custom tracers
for non-JAX-RS methods by using MicroProfile OpenTracing.

OpenTracing is a standard API for instrumenting microservices for distributed tracing. Distributed
tracing helps troubleshoot microservices by examining and logging requests as they propagate through a
distributed system, allowing developers to tackle the otherwise difficult task of debugging these requests.
Without a distributed tracing system in place, analyzing the workflows of operations becomes difficult,
particularly in regard to pinpointing when and by whom a request is received, as well as when a response
is sent back.

MicroProfile OpenTracing enables distributed tracing in microservices without adding any explicit
distributed tracing code to the application. Note that the MicroProfile OpenTracing specification does
not address the problem of defining, implementing, or configuring the underlying distributed tracing
system. Rather, the specification makes it easy to instrument services with distributed tracing given
an existing distributed tracing system.

You will configure the provided **inventory** and **system** services to use distributed tracing with
MicroProfile OpenTracing. You will run these services in two separate JVMs made of two server instances
to demonstrate tracing in a distributed environment. If all the components were to run on a single
server, then any logging software would do the trick.



# Getting Started

If a terminal window does not open navigate:

> Terminal -> New Terminal

Check you are in the **home/project** folder:

```
pwd
```
{: codeblock}

The fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-microprofile-opentracing.git
cd guide-microprofile-opentracing
```
{: codeblock}

The **start** directory contains the starting project that you will build upon.



For this guide, use Zipkin as your distributed tracing system. You can find the installation instructions
for Zipkin at the Zipkin [quickstart page](https://zipkin.io/pages/quickstart.html). You are not required
to use Zipkin, but keep in mind that you might need more instructions that are not listed here if you choose
to use another tracing system.

If you are using Java 8 or above you can easily download and run Zipkin by using the command:

```
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar
```
{: codeblock}

Before you proceed, make sure that your Zipkin server is up and running. By default, Zipkin can be found
at the [http://localhost:9411](http://localhost:9411) URL.


### Try what you'll build

The **finish** directory in the root directory of this guide contains two services that are configured
to use MicroProfile OpenTracing. Feel free to give them a try before you proceed.

To try out the services, navigate to the **finish** directory and run the Maven **install** phase to build the services
```
mvn install
```
{: codeblock}



then, run the Maven **liberty:start-server** goal to start them in two Open Liberty servers:
```
mvn liberty:start-server
```
{: codeblock}



Make sure that your Zipkin server is running and point your browser to the [http://localhost:9081/inventory/systems/localhost](http://localhost:9081/inventory/systems/localhost) URL 
```
curl http://localhost:9081/inventory/systems
```
{: codeblock}


When you visit this endpoint, you make two GET HTTP requests, one to the **system** service and one to the **inventory**
service. Both of these requests are configured to be traced, so a new trace will be recorded in Zipkin.
Visit the [http://localhost:9411](http://localhost:9411) URL or another location where you configured Zipkin to run and sort the traces
by newest first. Verify that this new trace contains three spans with the following names:

- **get:io.openliberty.guides.inventory.inventoryresource.getpropertiesforhost**
- **get:io.openliberty.guides.system.systemresource.getproperties**
- **add() span**


You can inspect each span by clicking it to reveal more detailed information, such as the time
at which the request was received and the time at which a response was sent back.

If you examine the other traces, you might notice a red trace entry, which happens when an error is
caught by the span. In this case, since one of the tests accesses the **/inventory/systems/badhostname**
endpoint, which is invalid, an error is thrown. This behavior is expected.

When you are done checking out the services, stop both Open Liberty servers using the Maven
**liberty:stop-server** goal:

```
mvn liberty:stop-server
```
{: codeblock}



// Feel free to read our [Docker guide](https://openliberty.io/guides/docker.html) to learn more about developing



# Running the services

You'll need to start the services to see basic traces appear in Zipkin. So, before you proceed, build
and start the provided **system** and **inventory** services in the starting project. Navigate to the **start**
directory and run the Maven **install** goal
```
mvn install
```
{: codeblock}



then, run the **liberty:start-server** goal:
```
mvn liberty:start-server
```
{: codeblock}



When the servers start, you can find the **system** and **inventory** services at the following URLs:

- [http://localhost:9080/system/properties](http://localhost:9080/system/properties)
- [http://localhost:9081/inventory/systems](http://localhost:9081/inventory/systems)


# Existing Tracer implementation

To collect traces across your systems, you need to implement the OpenTracing **Tracer**
interface. For this guide, you can access a bare-bones **Tracer** implementation for
the Zipkin server in the form of a user feature for Open Liberty.

This feature is already configured for you in your **pom.xml** and **server.xml** files. It will be
downloaded and installed automatically into each service when you run a Maven build. You can find the **opentracingZipkin** feature
enabled in your **server.xml** file.

The **download-maven-plugin** Maven plug-in in your **pom.xml** is responsible for downloading and installing the feature.

If you want to install this feature yourself, see
[Enabling distributed tracing](https://www.ibm.com/support/knowledgecenter/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/twlp_dist_tracing.html)
in the IBM Knowledge Centre.






# Enabling distributed tracing

The MicroProfile OpenTracing feature enables tracing of all JAX-RS methods by default.
To further control and customize these traces, use the **@Traced** annotation to enable and disable
tracing of particular methods. You can also inject a custom **Tracer** object to create and customize spans.


### Enabling distributed tracing without code instrumentation

Because tracing of all JAX-RS methods is enabled by default, you need only to enable **MicroProfile OpenTracing** feature and the **Zipkin** user feature in the **server.xml** file to see some basic traces in Zipkin.

Both of these features are already enabled in the **inventory** and **system** configuration files.




Make sure that your services are running. Then, simply point your browser to any of their endpoints and
check your Zipkin server for traces.


### Enabling explicit distributed tracing

Use the **@Traced** annotation to define explicit span creation for specific classes and methods.
If you place the annotation on a class, then it's automatically applied to all methods within that class.
If you place the annotation on a method, then it overrides the class annotation if one exists.

The **@Traced** annotation can be configured with the following two parameters:

- The **value=[true|false]** parameter indicates whether or not a particular class or method is
traced. For example, while all JAX-RS methods are traced by default, you can disable their tracing by
using the **@Traced(false)** annotation. This parameter is set to **true** by default.
- The **operationName=<Span name>** parameter indicates the name of the span that is assigned to the
particular method that is traced. If you omit this parameter, the span will be named with the following
form: **<package name>.<class name>.<method name>**. If you use this parameter at a class level, then
all methods within that class will have the same span name unless they are explicitly overridden by
another **@Traced** annotation.

Update the `InventoryManager` class.

> [File -> Open]guide-microprofile-opentracing/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java





```
package io.openliberty.guides.inventory;

import java.util.ArrayList;
import java.util.Properties;
import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Collections;

import org.eclipse.microprofile.opentracing.Traced;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.openliberty.guides.inventory.model.*;

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
            try (Scope childScope = tracer.buildSpan("add() Span")
                                              .startActive(true)) {
                systems.add(system);
            }
        }
    }

    @Traced(value = true, operationName = "InventoryManager.list")
    public InventoryList list() {
        return new InventoryList(systems);
    }
}
```
{: codeblock}


Enable tracing of the **list()** non-JAX-RS method by updating **@Traced** as shown.



Next, run the following command from the **start** directory to recompile your services. 
```
mvn compile
```
{: codeblock}


Point to the
[http://localhost:9081/inventory/systems/localhost](http://localhost:9081/inventory/systems/localhost) URL, check your Zipkin server, and sort the traces by newest first. You see a new trace record
that is two spans long with one span for the **listContents()** JAX-RS method in the **InventoryResource**
class and another span for the **list()** method in the **InventoryManager** class. Verify that these spans
have the following names:

- **get:io.openliberty.guides.inventory.inventoryresource.listcontents**
- **inventorymanager.list**




Update the `InventoryResource` class

> [File -> Open]guide-microprofile-opentracing/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java





```
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
{: codeblock}


Disable tracing of the **listContents()** JAX-RS method by setting **@Traced(false)**.



Again, run the **mvn compile** command from the **start** directory to recompile your services:
```
mvn compile
```
{: codeblock}


Point to the
[http://localhost:9081/inventory/systems](http://localhost:9081/inventory/systems) URL, check your Zipkin server, and sort the traces by newest first. You see a new trace record
that is just one span long for the remaining **list()** method in the **InventoryManager** class. Verify
that this span has the following name:

- **inventorymanager.list**



### Injecting a custom Tracer object

The MicroProfile OpenTracing specification also makes the underlying OpenTracing **Tracer** instance
available for use. You can access the configured **Tracer** by injecting it into a bean by using the **@Inject**
annotation from the Contexts and Dependency Injections API.

Inject the **Tracer** object into the **inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java** file.
Then, use it to define a new child scope in the **add()** call.

Replace the `InventoryManager` class.

> [File -> Open]guide-microprofile-opentracing/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java





```
package io.openliberty.guides.inventory;

import java.util.ArrayList;
import java.util.Properties;
import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Collections;

import org.eclipse.microprofile.opentracing.Traced;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.openliberty.guides.inventory.model.*;

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
            try (Scope childScope = tracer.buildSpan("add() Span")
                                              .startActive(true)) {
                systems.add(system);
            }
        }
    }

    @Traced(value = true, operationName = "InventoryManager.list")
    public InventoryList list() {
        return new InventoryList(systems);
    }
}
```
{: codeblock}





The **try** block that you see here is called a **try-with-resources** statement, meaning that the **childScope** object is closed at the end of the statement. It's good practice to define custom spans inside
such statements. Otherwise, any exceptions that are thrown before the span is closed will leak the active span.

Next, run the following command from the **start** directory to recompile your services. 
```
mvn compile
```
{: codeblock}


Point to the
http://localhost:9081/inventory/systems[http://localhost:9081/inventory/systems^] URL, check your Zipkin server, and sort the traces by newest first You see two new
```
curl http://localhost:9081/inventory/systems
```
{: codeblock}


trace records, one for the **system** service and one for the **inventory** service. The **system** trace 
contains one span for the **getProperties()** method in the **SystemResource** class. The **inventory** 
trace contains two spans. The first span is for the **getPropertiesForHost()** method in the **InventoryResource** 
class. The second span is the custom span that you created around the **add()** call. 
Verify that all of these spans have the following names:

The **system** trace:

- **get:io.openliberty.guides.system.systemresource.getproperties**

The **inventory** trace:

- **get:io.openliberty.guides.inventory.inventoryresource.getpropertiesforhost**
- **add() span**


This simple example shows what you can do with the injected **Tracer** object. More configuration
options are available to you, including setting a timestamp for when a span was created and destroyed.
However, these options require an implementation of their own, which does not come as a part of the Zipkin
user feature that is provided. In a real-world scenario, implement all the OpenTracing interfaces that
you deem necessary, which might include the **SpanBuilder** interface. You can use this interface for span
creation and customization, including setting timestamps.






# Testing the services

No automated tests are provided to verify the correctness of the traces. Manually verify these traces
by viewing them on the Zipkin server.

A few tests are included for you to test the basic functionality of the services. If a test failure
occurs, then you might have introduced a bug into the code. These tests will run automatically as a
part of the Maven build process when you run the **mvn install** command. You can also run these tests
separately from the build by using the **mvn verify** command, but first make sure that the servers are
stopped.

When you are done checking out the services, stop the server by using the Maven
**liberty:stop-server** goal:

```
mvn liberty:stop-server
```
{: codeblock}




# Summary

## Clean up your environment

Delete the **guide-microprofile-opentracing** project by navigating to the **/home/project/** directory

```
cd ../..
rm -r -f guide-microprofile-opentracing
rmdir guide-microprofile-opentracing
```
{: codeblock}


# Great work! You're done!

You have just used MicroProfile OpenTracing in Open Liberty to customize how and which traces are delivered to Zipkin.

Feel free to try one of the related MicroProfile guides. They demonstrate additional technologies that you
can learn to expand on top of what you built here.