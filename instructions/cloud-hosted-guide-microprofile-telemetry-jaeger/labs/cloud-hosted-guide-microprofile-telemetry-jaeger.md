---
markdown-version: v1
title: cloud-hosted-guide-microprofile-telemetry-jaeger
branch: lab-7221-instruction
version-history-start-date: 2023-06-06T12:58:02Z
tool-type: theia
---
::page{title="Welcome to the Enabling distributed tracing in microservices with OpenTelemetry and Jaeger guide!"}

Distributed tracing helps teams keep track of requests between microservices. MicroProfile Telemetry adopts OpenTelemetry tracing, so you can observe requests across your distributed systems.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}
The complexity of microservices architecture can make it more difficult to understand how services depend on or affect each other and to identify sources of latency or inaccuracies.

One way to increase the observability of an application is by emitting traces. [OpenTelemetry](https://opentelemetry.io/) is a set of APIs, SDKs, tooling, and integrations designed to create and manage telemetry data such as traces, metrics, and logs. MicroProfile Telemetry adopts OpenTelemetry so your applications can benefit from both manual and automatic traces.

Traces represent requests, which can contain multiple operations or spans. Each span comprises a name, time-related data, log messages, and metadata that describe what occurred during a transaction. Spans are associated with a context, which identifies the request within which the span occurred. Developers can then follow a single request between services through a potentially complex distributed system. Exporters send the data that MicroProfile Telemetry collects to Jaeger so you can visualize and monitor the generated spans.

The diagram shows multiple services, which is where distributed tracing is valuable. However, for simplicity, in this guide, you'll configure only the ***system*** and ***inventory*** services to use [Jaeger](https://www.jaegertracing.io/) for distributed tracing with MicroProfile Telemetry. You'll run these services in two separate JVMs made of two server instances to demonstrate tracing in a distributed environment.

![Application architecture](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/architecture_diagram.png)




::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/draft-guide-microprofile-telemetry-jaeger.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/draft-guide-microprofile-telemetry-jaeger.git
cd draft-guide-microprofile-telemetry-jaeger
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

Run the following `docker` command to start the Jaeger server:
```bash 
docker run -d --name jaeger \
  -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 14250:14250 \
  -p 14268:14268 \
  -p 14269:14269 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.46
```

You can find information about the Jaeger server and instructions for starting the all-in-one executable file in the [Jaeger documentation](https://www.jaegertracing.io/docs/1.46/getting-started/#all-in-one).

Before you proceed, make sure that your Jaeger server is up and running. Click the following button to visit the Jaeger service:

::startApplication{port="16686" display="external" name="Visit Jaeger service" route="/"}

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.



Navigate to the ***finish/system*** directory. Run the following Maven goal to build the ***system*** service and deploy it to Open Liberty:
```bash
cd /home/project/draft-guide-microprofile-telemetry-jaeger/finish/system
mvn liberty:run
```

Open another command-line session and navigate to the ***finish/inventory*** directory. Run the following Maven goal to build the ***inventory*** service and deploy it to Open Liberty:
```bash
cd /home/project/draft-guide-microprofile-telemetry-jaeger/finish/inventory
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

When you visit this endpoint, you make two GET HTTP requests, one to the ***system*** service and another to the ***inventory*** service. Both of these requests are configured to be traced, so a new trace is recorded in Jaeger. To view the traces, click the following button to visit the Jaeger service:

::startApplication{port="16686" display="external" name="Visit Jaeger service" route="/"}

You can view the traces for the ***system*** or ***inventory*** services under the **Search** tab. If you see only the **jaeger-query** option in the drop-down menu, wait a little longer and refresh the page to see the application services.

Select the services in the **Select A Service** menu and click the **Find Traces** button at the end of the section. You will see the following result:

![Get traces for the inventory service](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/inventory_service_spans.png)



The trace has five spans, four from the ***inventory*** service and one from the ***system*** service. Click the trace to view its details. Under **Service & Operation**, you see the spans in this trace. You can inspect each span by clicking it to reveal more detailed information, such as the times that a request was received and a response was sent.

![Inventory details spans](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/inventory_details_spans.png)



After you’re finished reviewing the application, stop the Open Liberty servers by pressing `Ctrl+C` in the command-line sessions where you ran the ***system*** and ***inventory*** services. Alternatively, you can run the following goals from the ***finish*** directory in another command-line session:


```bash
cd /home/project/draft-guide-microprofile-telemetry-jaeger/finish
mvn -pl system liberty:stop
mvn -pl inventory liberty:stop
```

::page{title="Building the application "}

You need to start the services to see basic traces appear in Jaeger.

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), the server listens for file changes and automatically recompiles and deploys your updates whenever you save a new change.

Open a command-line session and navigate to the ***start/system*** directory. Run the following Maven goal to start the ***system*** service in dev mode:


```bash
cd /home/project/draft-guide-microprofile-telemetry-jaeger/start/system
mvn liberty:dev
```

Open a command-line session and navigate to the ***start/inventory*** directory. Run the following Maven goal to start the ***inventory*** service in dev mode:


```bash
cd /home/project/draft-guide-microprofile-telemetry-jaeger/start/inventory
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

::page{title="Enabling Telemetry implementation "}

Navigate to the ***start*** directory to begin.

MicroProfile Telemetry allows you to observe traces without modifying the source code in your Jakarta RESTful applications. You can enable the ***mpTelemetry*** feature in the ***server.xml*** configuration file.

Replace the ***server.xml*** file of the system service:

> To open the server.xml file in your IDE, select
> **File** > **Open** > draft-guide-microprofile-telemetry-jaeger/start/system/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-jaeger/start/system/src/main/liberty/config/server.xml"}



```xml
<server description="system service">

    <featureManager>
        <feature>cdi-4.0</feature>
        <feature>jsonb-3.0</feature>
        <feature>jsonp-2.1</feature>
        <feature>restfulWS-3.1</feature>
        <feature>mpTelemetry-1.0</feature>
    </featureManager>

    <httpEndpoint httpPort="${default.http.port}"
                  httpsPort="${default.https.port}"
                  id="defaultHttpEndpoint" host="*" />

    <webApplication location="guide-microprofile-telemetry-system.war"
                    contextRoot="/" />

</server>
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


The ***mpTelemetry*** feature is now enabled in the ***server.xml*** of the ***system*** service.

Replace the ***server.xml*** file of the inventory service:

> To open the server.xml file in your IDE, select
> **File** > **Open** > draft-guide-microprofile-telemetry-jaeger/start/inventory/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-jaeger/start/inventory/src/main/liberty/config/server.xml"}



```xml
<server description="inventory service">

    <featureManager>
        <feature>cdi-4.0</feature>
        <feature>jsonb-3.0</feature>
        <feature>jsonp-2.1</feature>
        <feature>restfulWS-3.1</feature>
        <feature>mpConfig-3.0</feature>
        <feature>mpTelemetry-1.0</feature>
    </featureManager>

    <httpEndpoint httpPort="${default.http.port}"
                  httpsPort="${default.https.port}"
                  id="defaultHttpEndpoint" host="*" />

    <webApplication location="guide-microprofile-telemetry-inventory.war"
                    contextRoot="/">
    </webApplication>

</server>
```



The ***mpTelemetry*** feature is now enabled in the ***server.xml*** of the ***inventory*** service.


By default, MicroProfile Telemetry tracing is off. To enable any tracing aspects, specify the ***otel*** properties in the MicroProfile configuration file. 

Create the ***microprofile-config.properties*** file of the system service:

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-microprofile-telemetry-jaeger/start/system/src/main/resources/META-INF/microprofile-config.properties
```


> Then, to open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > draft-guide-microprofile-telemetry-jaeger/start/system/src/main/resources/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-jaeger/start/system/src/main/resources/META-INF/microprofile-config.properties"}



```
otel.service.name=system
otel.sdk.disabled=false
```



The MicroProfile properties file sets the ***otel.service.name*** property with the ***system*** service name and sets the ***otel.sdk.disabled*** property to ***false*** to enable tracing.


Create the ***microprofile-config.properties*** file of the inventory service:

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-microprofile-telemetry-jaeger/start/inventory/src/main/resources/META-INF/microprofile-config.properties
```


> Then, to open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > draft-guide-microprofile-telemetry-jaeger/start/inventory/src/main/resources/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-jaeger/start/inventory/src/main/resources/META-INF/microprofile-config.properties"}



```
otel.service.name=inventory
otel.sdk.disabled=false
```



Similarly, specify the ***otel*** properties for the ***inventory*** service.

For more information about these and other Telemetry properties, see the [MicroProfile Config properties for MicroProfile Telemetry](https://openliberty.io/docs/latest/microprofile-config-properties.html#telemetry) documentation.


To run the ***system*** and ***inventory*** services, run the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems/localhost | jq
```

To view the traces, click the following button to visit the Jaeger service:

::startApplication{port="16686" display="external" name="Visit Jaeger service" route="/"}

You can view the traces for the ***system*** or ***inventory*** services under the **Search** tab. Select the services in the **Select A Service** menu and click the **Find Traces** button at the end of the section. You'll see the result as:

![Default spans](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/default_spans.png)



Verify that there are two spans from the ***inventory*** service and one span from the ***system*** service. Click the trace to view its details.

![Details default spans](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/details_default_spans.png)


::page{title="Enabling explicit distributed tracing"}

Automatic instrumentation only instruments Jakarta RESTful web services and MicroProfile REST clients. To get further spans on other operations, such as database calls, you can add manual instrumentation to the source code.

### Enabling OpenTelemetry APIs

The MicroProfile Telemetry feature has been enabled to trace all REST endpoints by default in the previous section. To further control and customize traces, use the ***@WithSpan*** annotation to enable particular methods. You can also inject a ***Tracer*** object to create and customize spans.

Replace the ***pom.xml*** Maven project file of the inventory service:

> To open the pom.xml file in your IDE, select
> **File** > **Open** > draft-guide-microprofile-telemetry-jaeger/start/inventory/pom.xml, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-jaeger/start/inventory/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-microprofile-telemetry-inventory</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>

        <!-- OpenLiberty runtime -->
        <liberty.var.system.http.port>9080</liberty.var.system.http.port>
        <liberty.var.default.http.port>9081</liberty.var.default.http.port>
        <liberty.var.default.https.port>9444</liberty.var.default.https.port>
    </properties>

    <dependencies>
        <!-- Provided dependencies -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>10.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>6.0</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-api</artifactId>
            <version>1.26.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>io.opentelemetry.instrumentation</groupId>
            <artifactId>opentelemetry-instrumentation-annotations</artifactId>
            <version>1.26.0</version>
            <scope>provided</scope>
        </dependency>
        <!-- For tests -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.9.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-client</artifactId>
            <version>6.0.0.Final</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-json-binding-provider</artifactId>
            <version>6.0.0.Final</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>jakarta.json</artifactId>
            <version>2.0.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse</groupId>
            <artifactId>yasson</artifactId>
            <version>2.0.4</version>
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
            </plugin>

            <!-- Liberty plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.8.2</version>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>2.22.0</version>
                <configuration>
                    <systemPropertyVariables>
                        <sys.http.port>${liberty.var.system.http.port}</sys.http.port>
                        <inv.http.port>${liberty.var.default.http.port}</inv.http.port>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```



The OpenTelemetry API and OpenTelemetry Instrumentation Annotations must be provided as dependencies to your build path. The ***pom.xml*** now includes two ***io.opentelemetry*** dependencies.

Replace the ***server.xml*** file of the inventory service:

> To open the server.xml file in your IDE, select
> **File** > **Open** > draft-guide-microprofile-telemetry-jaeger/start/inventory/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-jaeger/start/inventory/src/main/liberty/config/server.xml"}



```xml
<server description="inventory service">

    <featureManager>
        <feature>cdi-4.0</feature>
        <feature>jsonb-3.0</feature>
        <feature>jsonp-2.1</feature>
        <feature>restfulWS-3.1</feature>
        <feature>mpConfig-3.0</feature>
        <feature>mpTelemetry-1.0</feature>
    </featureManager>

    <httpEndpoint httpPort="${default.http.port}"
                  httpsPort="${default.https.port}"
                  id="defaultHttpEndpoint" host="*" />

    <webApplication location="guide-microprofile-telemetry-inventory.war"
                    contextRoot="/">
        <!-- enable visibility to third party apis -->
        <classloader apiTypeVisibility="+third-party"/>
    </webApplication>

</server>
```



The OpenTelemetry APIs are exposed as third-party APIs in Open Liberty. To add the visibility of OpenTelemetry APIs to the application, add ***third-party*** to the types of API packages that this class loader supports. Instead of explicitly configuring a list of API packages that includes ***third-party***, set the ***+third-party*** value to the ***apiTypeVisibility*** attribute in the ***classLoader*** configuration. This configuration adds ***third-party*** to the default list of API package types that are supported.


### Enabling tracing in Jakarta CDI beans

You can trace your Jakarta CDI beans by annotating their methods with a ***@WithSpan*** annotation.

Replace the ***InventoryManager*** class:

> To open the InventoryManager.java file in your IDE, select
> **File** > **Open** > draft-guide-microprofile-telemetry-jaeger/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-jaeger/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java"}



```java

package io.openliberty.guides.inventory;

import java.util.ArrayList;
import java.util.Properties;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Collections;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class InventoryManager {

    @Inject
    @ConfigProperty(name = "system.http.port")
    private int SYSTEM_PORT;

    private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());
    private SystemClient systemClient = new SystemClient();

    public Properties get(String hostname) {
        systemClient.init(hostname, SYSTEM_PORT);
        Properties properties = systemClient.getProperties();
        return properties;
    }

    @WithSpan
    public void add(@SpanAttribute("hostname") String host,
                    Properties systemProps) {
        Properties props = new Properties();
        props.setProperty("os.name", systemProps.getProperty("os.name"));
        props.setProperty("user.name", systemProps.getProperty("user.name"));
        SystemData system = new SystemData(host, props);
        if (!systems.contains(system)) {
            systems.add(system);
        }
    }

    @WithSpan
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



The ***add()*** and ***list()*** methods are annotated with the ***@WithSpan*** annotation. The OpenTelemetry instrumentation provides a new span for each method. You can now collect and trace the spans across different services. After creating a span, you have the option to include desired parameters with their values in the span by using the ***@SpanAttribute*** annotation. For example, the ***host*** parameter is assigned ***hostname*** as its attribute name using the ***@SpanAttribute*** annotation, which gives the benefit of tracing the parameters within the ***add*** span.

To learn more about how to use OpenTelemetry annotations to instrument code, see the [OpenTelemetry Annotations](https://opentelemetry.io/docs/instrumentation/java/automatic/annotations/) documentation.



Now, you can check out the traces that are generated by the ***@WithSpan*** annotation. Run the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems | jq
```

and click the following button to visit the Jaeger service:

::startApplication{port="16686" display="external" name="Visit Jaeger service" route="/"}

Select the ***inventory*** service and click the **Find Traces** button at the end of the section. You'll see the result as:

![Inventory Manager span](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/inventory_manager_span.png)



Verify that there are two spans from the ***inventory*** service. Click the trace to view its details. You'll see the ***InventoryManager.list*** span that is created by the ***@WithSpan*** annotation.

![Inventory Manager list span](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/inventory_manager_list_span.png)



To check out the information generated by the ***@SpanAttribute*** annotation, run the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems/localhost | jq
```

Click the following button to visit the Jaeger service:

::startApplication{port="16686" display="external" name="Visit Jaeger service" route="/"}


Select the ***inventory*** service and click the **Find Traces** button at the end of the section. You will see the following result:

![Get traces for the inventory service](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/inventory_service_4_spans.png)



Verify that there are three spans from the ***inventory*** service and one span from the ***system*** service. Click the trace to view its details.

![Inventory details spans](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/inventory_details_4_spans.png)



Click the ***InventoryManager.add*** span and its ***Tags***. You can see the ***hostname*** tag with the ***localhost*** value that is created by the ***@SpanAttribute*** annotation.

![Inventory Manager add span](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/inventory_manager_add_span.png)



### Injecting a custom Tracer object

The MicroProfile Telemetry specification makes the underlying OpenTelemetry Tracer instance available. The configured Tracer is accessed by injecting it into a bean. You can use it to instrument your code to create traces.

Replace the ***InventoryResource*** class:

> To open the InventoryResource.java file in your IDE, select
> **File** > **Open** > draft-guide-microprofile-telemetry-jaeger/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-jaeger/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryResource.java"}



```java
package io.openliberty.guides.inventory;

import java.util.Properties;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

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

    @Inject
    private InventoryManager manager;

    @Inject
    private Tracer tracer;

    @GET
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPropertiesForHost(@PathParam("hostname") String hostname) {
        Span getPropertiesSpan = tracer.spanBuilder("GettingProperties").startSpan();
        Properties props = null;
        try (Scope scope = getPropertiesSpan.makeCurrent()) {
            props = manager.get(hostname);
            if (props == null) {
                getPropertiesSpan.addEvent("Cannot get properties");
                return Response.status(Response.Status.NOT_FOUND)
                         .entity("{ \"error\" : \"Unknown hostname or the system "
                               + "service may not be running on " + hostname + "\" }")
                         .build();
            }
            getPropertiesSpan.addEvent("Received properties");
            manager.add(hostname, props);
        } finally {
            getPropertiesSpan.end();
        }
        return Response.ok(props).build();

    }

    @GET
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



To access the Tracer, the ***@Inject*** annotation from the Contexts and Dependency Injections API injects the Tracer into a bean. 

Before the ***InventoryManager*** calls the ***system*** service, it creates and starts a span called the ***GettingProperties*** by using the ***spanBuilder()*** and ***startSpan()*** Tracer APIs.

When you start a span, you must also end it by calling ***end()*** on the span. If you don't end a span, it won't be recorded at all and won't show up in Jaeger. This code ensures that ***end()*** is always called by including it in a ***finally*** block.

After you start the span, make it current with the ***makeCurrent()*** call. Making a span current means that any new spans created in the same thread, either automatically by open liberty or manually by calling the API, will use this span as their parent span.

The ***makeCurrent()*** call returns a ***Scope***. Make sure to always close the ***Scope***, which stops the span from being current and makes the previous span current again. Use a ***try-with-resources*** block, which automatically closes the ***Scope*** at the end of the block.

Use the ***addEvent()*** Span API to create an event when the properties are received and an event when it fails to get the properties from the ***system*** service. Use the ***end()*** Span API to mark the ***GettingProperties*** span as completed.



To check out the traces that contain the ***GettingProperties*** span, run the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems/localhost | jq
```

Click the following button to visit the Jaeger service:

::startApplication{port="16686" display="external" name="Visit Jaeger service" route="/"}

Select the ***inventory*** service and click the **Find Traces** button at the end of the section. You'll see the result:

![Get traces for the inventory service](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/inventory_service_spans.png)



Verify that there are four spans from the ***inventory*** service and one span from the ***system*** service. Click the trace to view its details. You'll see the ***GettingProperties*** span.

![Inventory details spans](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/inventory_details_spans.png)



To check out the event adding to the ***GettingProperties*** span, run the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems/unknown | jq
```

Click the following button to visit the Jaeger service:

::startApplication{port="16686" display="external" name="Visit Jaeger service" route="/"}

Select the ***inventory*** service and click the **Find Traces** button at the end of the section. You will see the following result:

![Get traces for unknown hostname](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/inventory_service_unknown_spans.png)



There are two spans from the ***inventory*** service. Click the trace to view its details. You'll see the ***GettingProperties*** span. Click the ***GettingProperties*** span and its ***Logs***. You can see the ***Cannot get properties*** message.

![Logs at GettingProperties span](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/draft/assets/logs_at_gettingProperties.png)



To learn more about how to use OpenTelemetry APIs to instrument code, see the [OpenTelemetry Manual Instrumentation](https://opentelemetry.io/docs/instrumentation/java/manual/) documentation.


::page{title="Testing the application "}

Manually verify the traces by inspecting them on the Jaeger server. You will find some tests included to test the basic functionality of the services. If any of the tests fail, you might have introduced a bug into the code.

### Running the tests

Since you started Open Liberty in dev mode, run the tests for the ***system*** and ***inventory*** services by pressing the ***enter/return*** key in the command-line sessions where you started the services.

When you are done checking out the services, exit dev mode by pressing `Ctrl+C` in the shell sessions where you ran the ***system*** and ***inventory*** services, or by typing ***q*** and then pressing the ***enter/return*** key.


Finally, stop the ***Jaeger*** service that you started in the previous step.
```bash
docker stop jaeger
docker rm jaeger
```


::page{title="Summary"}

### Nice Work!

You just used MicroProfile Telemetry in Open Liberty to customize how and which traces are delivered to Jaeger.


Try out one of the related MicroProfile guides. These guides demonstrate more technologies that you can learn to expand on what you built in this guide.


### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***draft-guide-microprofile-telemetry-jaeger*** project by running the following commands:

```bash
cd /home/project
rm -fr draft-guide-microprofile-telemetry-jaeger
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Enabling%20distributed%20tracing%20in%20microservices%20with%20OpenTelemetry%20and%20Jaeger&guide-id=cloud-hosted-draft-guide-microprofile-telemetry-jaeger)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/draft-guide-microprofile-telemetry-jaeger/pulls)



### Where to next?

* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Providing metrics from a microservice](https://openliberty.io/guides/microprofile-metrics.html)
* [Adding health reports to microservices](https://openliberty.io/guides/microprofile-health.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
