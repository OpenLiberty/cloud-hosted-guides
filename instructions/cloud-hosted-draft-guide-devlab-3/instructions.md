---
markdown-version: v1
tool-type: theia
---
::page{title="Welcome to the Enabling observability in microservices with traces, metrics, and logs using OpenTelemetry and Grafana guide!"}

Learn how to enable the collection of traces, metrics, and logs from microservices by using MicroProfile Telemetry and the Grafana stack.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

In a microservices architecture, it can be difficult to understand how services interact, where latency occurs, and what causes failures. Without visibility across service boundaries, diagnosing issues and tuning performance can become slow and error-prone.

Observability helps address these challenges by capturing telemetry data such as logs, metrics, and traces. [OpenTelemetry](https://opentelemetry.io/) is an open source framework that provides APIs, SDKs, and tools for generating and managing this data. MicroProfile Telemetry adopts OpenTelemetry to enable both automatic and manual instrumentation in MicroProfile applications. Traces and metrics, along with runtime and application logs, can be exported in a standardized format through an OpenTelemetry Collector to any compatible backend.

In this guide, you'll use the [Grafana Docker OpenTelemetry LGTM](https://github.com/grafana/docker-otel-lgtm/?tab=readme-ov-file#docker-otel-lgtm) image (***grafana/otel-lgtm***), an open source Docker image that provides a preconfigured observability backend for OpenTelemetry, based on the [Grafana stack](https://grafana.com/about/grafana-stack/). This setup includes:

* [OpenTelemetry Collector](https://opentelemetry.io/docs/collector/): a gateway for receiving telemetry data from applications

* [Prometheus](https://github.com/prometheus/prometheus): a time-series database for storing numerical metrics, like request rates and memory usage

* [Loki](https://github.com/grafana/loki/): a log aggregation system for collecting and querying logs

* [Tempo](https://github.com/grafana/tempo/): a distributed tracing backend that stores traces, which represent the path and timing of a request as it flows across services
    
* [Grafana](https://github.com/grafana/grafana): a dashboard tool that brings together logs, metrics, and traces for visualization and analysis

The diagram shows a distributed environment with multiple services. For simplicity, this guide configures only the ***system*** and ***inventory*** services. You’ll learn how to enable automatic collection of traces, metrics, and logs from microservices by using MicroProfile Telemetry, and visualize them in Grafana.

![Application architecture](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/architecture_diagram.png)


```bash
docker run -d --name otel-lgtm -p 3000:3000 -p 4317:4317 -p 4318:4318 --rm -ti grafana/otel-lgtm
```

You can monitor the container startup by viewing its logs:

```bash
docker logs otel-lgtm
```

It may take a minute for the container to start. After you see the following message, your observability stack is ready:

```
The OpenTelemetry collector and the Grafana LGTM stack are up and running.
```

When the container is running, you can access the Grafana dashboard at the ***http\://localhost:3000*** URL.

::page{title="Getting started"}

To open a new command-line session,
select ***Terminal*** > ***New Terminal*** from the menu of the IDE.

Run the following command to navigate to the ***/home/project*** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/draft-guide-microprofile-telemetry-grafana-automatic.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/draft-guide-microprofile-telemetry-grafana-automatic.git
cd draft-guide-microprofile-telemetry-grafana-automatic
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, go to the ***finish*** directory and run the following Maven goal to build the ***system*** service and deploy it to Open Liberty:


```bash
./mvnw -pl system liberty:run
```

Next, open another command-line session in the ***finish*** directory and run the following command to start the ***inventory*** service:


```bash
./mvnw -pl inventory liberty:run
```

After you see the following message in both command-line sessions, both of your services are ready:

```
The defaultServer server is ready to run a smarter planet.
```

Visit the ***http\://localhost:9081/inventory/systems/localhost*** URL. This action triggers the ***inventory*** service to retrieve and store system load information for ***localhost*** by making a request to the ***system*** service at ******http\://localhost:9080/system/systemLoad***.***

In addition, the ***inventory*** service makes periodic background requests to the ***system*** service every 15 seconds to refresh system load information for all stored systems.

You can view the telemetry data collected from the running services in the Grafana dashboard at the ***http\://localhost:3000*** URL.

**Viewing trace with Tempo**

1. Open the **Explore** view from the left menu.

2. Select **Tempo** as the data source.

3. Set **Query type** to ***Search***.

4. Click the blue **Run query** button in the upper-right corner to list recent traces.

5. Find and click the trace ID for the request named ***GET /inventory/systems/{hostname}***. You see the following result in the **Trace** view:

+
![***GET /inventory/systems/{hostname}*** trace](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/inventory_systems_localhost_trace.png)


+
The trace contains three spans, two from the ***inventory*** service and one from the ***system*** service.




**Viewing logs with Loki**

1. Open the **Drilldown -> Logs** view from the left menu. This view displays an overview of time-series log counts and log entries for all services that send logs to Loki.

2. Click *Show logs* for a service to display its log entries inline and expand a log entry to view the full trace context.

+
![Example of log details](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/log_logger_info.png)






**Viewing metrics with Prometheus**

1. Open the **Drilldown -> Metrics** view from the left menu. This view shows a query-less experience for browsing the available metrics that are collected by Prometheus.

+
![Metrics overview](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/metrics_overview.png)


2. For a more detailed view of any metric, click the **Select** button next to its graph.




After you're finished reviewing the application, stop the Open Liberty instances by pressing `Ctrl+C` in the command-line sessions where you ran the ***system*** and ***inventory*** services. Alternatively, you can run the following goals from the ***finish*** directory in another command-line session:


```bash
./mvnw -pl system liberty:stop
./mvnw -pl inventory liberty:stop
```

::page{title="Enabling automatic telemetry collection"}

MicroProfile Telemetry automatically collects telemetry data without requiring changes to your application code. To collect and export telemetry data, you need to enable the MicroProfile Telemetry feature and configure the required OpenTelemetry properties in your application.

Navigate to the ***start*** directory to begin.

Start by adding the MicroProfile Telemetry feature to the ***server.xml*** file of each service.

Replace the ***server.xml*** file of the inventory service.

> To open the server.xml file in your IDE, select
> ***File*** > ***Open*** > draft-guide-microprofile-telemetry-grafana-automatic/start/inventory/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-grafana-automatic/start/inventory/src/main/liberty/config/server.xml"}



```xml
<server description="inventory service">

    <featureManager>
        <platform>jakartaee-10.0</platform>
        <platform>microprofile-7.1</platform>
        <feature>cdi</feature>
        <feature>jsonb</feature>
        <feature>jsonp</feature>
        <feature>restfulWS</feature>
        <feature>mpConfig</feature>
        <feature>mpRestClient</feature>
        <feature>enterpriseBeansLite</feature>
        <feature>mpTelemetry</feature>
    </featureManager>

    <httpEndpoint httpPort="${http.port}"
                  httpsPort="${https.port}"
                  id="defaultHttpEndpoint" host="*" />

    <webApplication location="guide-microprofile-telemetry-grafana-automatic-inventory.war"
                    contextRoot="/" />

    <logging consoleLogLevel="INFO" />

</server>
```


Click the :fa-copy: ***Copy*** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


The ***mpTelemetry*** feature enables MicroProfile Telemetry support in Open Liberty for the ***inventory*** service.

Replace the ***server.xml*** file of the system service.

> To open the server.xml file in your IDE, select
> ***File*** > ***Open*** > draft-guide-microprofile-telemetry-grafana-automatic/start/system/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-grafana-automatic/start/system/src/main/liberty/config/server.xml"}



```xml
<server description="system service">

    <featureManager>
        <platform>jakartaee-10.0</platform>
        <platform>microprofile-7.1</platform>
        <feature>jsonp</feature>
        <feature>restfulWS</feature>
        <feature>mpTelemetry</feature>
    </featureManager>

    <httpEndpoint httpPort="${http.port}"
                  httpsPort="${https.port}"
                  id="defaultHttpEndpoint" host="*" />

    <webApplication location="guide-microprofile-telemetry-grafana-automatic-system.war"
                    contextRoot="/" />

    <logging consoleLogLevel="INFO" />

</server>
```



Similarly, the added ***mpTelemetry*** feature enables telemetry support for the ***system*** service.

By default, the OpenTelemetry SDK is disabled to reduce performance overhead. To enable it, set the ***otel.sdk.disabled*** property to ***false*** in a valid configuration source.

Create the ***bootstrap.properties*** file for the inventory service.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-microprofile-telemetry-grafana-automatic/start/inventory/src/main/liberty/config/bootstrap.properties
```


> Then, to open the bootstrap.properties file in your IDE, select
> ***File*** > ***Open*** > draft-guide-microprofile-telemetry-grafana-automatic/start/inventory/src/main/liberty/config/bootstrap.properties, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-grafana-automatic/start/inventory/src/main/liberty/config/bootstrap.properties"}



```
otel.service.name=inventory
otel.sdk.disabled=false
```



Setting the ***otel.sdk.disabled*** property to ***false*** property in the [bootstrap properties](https://openliberty.io/docs/latest/reference/bootstrap-properties.html) file enables telemetry collection at the runtime level. This allows both runtime and application telemetry to be collected. If you instead configure this property at the application level, runtime telemetry will not be included. For more information, refer to the [MicroProfile Telemetry configuration documentation](https://openliberty.io/docs/latest/microprofile-telemetry.html#global).

The ***otel.service.name*** property sets the service name to ***inventory***, helping identify the source of the telemetry data in monitoring tools like Grafana.

The observability backend provided by the ***grafana/otel-lgtm*** image receives telemetry data through the OTLP protocol, which is the default for OpenTelemetry. Therefore, no extra exporter configuration is needed.

Create the ***bootstrap.properties*** file for the system service.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-microprofile-telemetry-grafana-automatic/start/system/src/main/liberty/config/bootstrap.properties
```


> Then, to open the bootstrap.properties file in your IDE, select
> ***File*** > ***Open*** > draft-guide-microprofile-telemetry-grafana-automatic/start/system/src/main/liberty/config/bootstrap.properties, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-grafana-automatic/start/system/src/main/liberty/config/bootstrap.properties"}



```
otel.service.name=system
otel.sdk.disabled=false
```



The ***otel.**** properties are configured in the ***bootstrap.properties*** file for the ***system*** service to enable telemetry collection and define service-specific settings.

For more information about these and other Telemetry properties, see the [MicroProfile Config properties for MicroProfile Telemetry](https://openliberty.io/docs/latest/reference/microprofile-config-properties.html#telemetry) documentation.

::page{title="Viewing the default telemetry data"}

OpenTelemetry automatically generates trace spans for incoming HTTP requests to Jakarta RESTful Web Services (JAX-RS) endpoints and outgoing requests from MicroProfile REST Clients. It also collects metrics such as HTTP request durations and JVM performance, and captures logs from both the server and the application.

Start the services to begin collecting telemetry data.

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), dev mode listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following command to start the ***system*** service in dev mode:


```bash
./mvnw -pl system liberty:dev
```

Open another command-line session and run the following command to start the ***inventory*** service in dev mode:


```bash
./mvnw -pl inventory liberty:dev
```

When you see the following message, your Liberty instances are ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

Telemetry such as Liberty startup logs and JVM metrics is generated when the servers start. To see request-scoped telemetry, interact with the services. The ***system*** service provides system load information, while the ***inventory*** service retrieves and stores this data through a MicroProfile REST Client. Both services expose REST endpoints built with Jakarta RESTful Web Services.

Visit the ***http\://localhost:9081/inventory/systems/localhost*** URL to fetch and store the ***localhost*** system information, which triggers the ***inventory*** service to call the ***system*** service at ******http\://localhost:9080/system/systemLoad***.***

Because the ***inventory*** service makes periodic background requests every 15 seconds to refresh system load information for all stored systems, telemetry data is continuously generated for you to monitor.

Open the Grafana dashboard at the ***http\://localhost:3000*** URL to view the telemetry data collected from the running services.

**Viewing request traces**

View the trace that was automatically created from your request:

1. Open the **Explore** view from the left menu.

2. Select **Tempo** as the data source.

3. Set **Query type** to ***Search***.

4. Click the blue **Run query** button at the upper right of the **Explore** view to list recent traces.

5. Find and click the trace ID for the request named ***GET /inventory/systems/{hostname}***. You see the following result in the **Trace** view:

+
![***GET /inventory/systems/{hostname}*** trace](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/inventory_systems_localhost_trace.png)


+
Verify that the trace contains three spans, one for the initial request to the ***inventory*** service, one client span from the ***inventory*** service making an outbound call to the ***system*** service, and one server span from the ***system*** service handling that request.

6. In the **Service & Operation** table, click each span to view detailed metadata. This includes when the request was received, when the response was sent, and information such as the HTTP method, status code, and endpoint path.

+
![***GET /inventory/systems/{hostname}*** spans](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/inventory_systems_localhost_spans.png)


7. Expand the **Node graph** section to see the relationship between the spans and how the ***inventory*** and ***system*** microservices interact. This graph helps visualize the request flow across services and identify any latency hotspots or bottlenecks.

+
![***GET /inventory/systems/{hostname}*** node graph](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/inventory_systems_localhost_node_graph.png)





**Viewing trace logs**

View logs associated with a specific span:

1. From the **Trace** view you opened in the previous step, click the **Log icon** on the right side of the span entry in the **Service & Operation** table.

2. Alternatively, click the blue **Logs for this span** button in the span detail section.

**Viewing server logs**

Monitor timestamped logs generated by the services:

1. Navigate to **Drilldown -> Logs** from the menu. This view displays a time-series overview of log counts and log entries for all services.

+
![Logs overview](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/logs_overview.png)


2. Click *Show logs* for a service to display its log entries inline.




**Viewing JVM metrics**

Get insights into class count, CPU usage, and heap memory utilization:

1. Open the **Dashboards** view from the menu.

2. Select the **JVM Overview (OpenTelemetry)** dashboard.

+
![JVM Overview](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/jvm_overview.png)


**Viewing HTTP metrics**

Get an overview of the HTTP request performance:

1. Navigate back to the **Dashboards** view from the menu. 

2. Open the **RED Metrics (classic histogram)** dashboard.

+
![RED Metrics](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/red_metrics.png)


::page{title="Enhancing application logs"}

When MicroProfile Telemetry is enabled, OpenTelemetry automatically collects logs from the Liberty message log stream. This includes logs that are written by using the ***java.util.logging*** API at the ***INFO*** level or higher, as well as messages from the ***System.out*** standard output and ***System.err*** standard error streams.

While ***System.out*** and ***System.err*** are useful for quick debugging, they are limited in production environments. These streams lack structure, consistent severity levels, and the contextual metadata that is critical for monitoring distributed systems. In contrast, the ***java.util.logging*** API produces structured logs with fine-grained control over log levels, built-in support for exceptions, and better integration with telemetry tools like Grafana.


The current ***InventoryManager*** class logs messages by writing to ***System.out*** and ***System.err***.

To observe a basic standard output log, visit the ***http\://localhost:9081/inventory/systems/localhost*** URL to trigger a successful request. Then, open the Grafana dashboard at the ***http\://localhost:3000*** URL.

In the **Explore** view, select the **Loki** data source. Set a filter for ***service_name = inventory*** and click the blue **Run query** button. The results appear in the **Logs** view by default. If it is not already selected, switch to **Logs** at the upper right of the **Logs** section to enable log expansion.

![Logs view](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/logs_view.png)


Locate the log entry ***Retrieved system load from localhost***. When you expand it, you see that both the ***detected_level*** and the ***io_openliberty_module*** fields are set to ***SystemOut***.

![Example log entry from ***System.out*** standard output stream](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/log_system_out.png)


Next, observe a standard error log by visiting the ***http\://localhost:9081/inventory/systems/unknown*** URL. This simulates a request to a nonexistent host and triggers a ***RuntimeException***.

Rerun the same query in Grafana. In the **Logs** section, locate and expand the following log entry:

```
Runtime exception while invoking system service: RESTEASY004655: Unable to invoke request: java.net.UnknownHostException: unknown: nodename nor servname provided, or not known
```

You see that both the ***detected_level*** and the ***io_openliberty_module*** fields are set to ***SystemErr***. The error stack trace is included directly in the log message, and no information about the exception type is provided.

![Example log entry from ***System.err*** standard error stream](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/log_system_err.png)


Although ***System.out*** and ***System.err*** logs are collected, they lack structured metadata. You cannot filter them by severity, separate stack traces from exceptions, or correlate them with originating classes and error types.

To enable structured logging, update your application to use the ***java.util.logging*** API.

Replace the ***InventoryManager*** class.

> To open the InventoryManager.java file in your IDE, select
> ***File*** > ***Open*** > draft-guide-microprofile-telemetry-grafana-automatic/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java, or click the following button

::openFile{path="/home/project/draft-guide-microprofile-telemetry-grafana-automatic/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryManager.java"}



```java
package io.openliberty.guides.inventory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;

@ApplicationScoped
public class InventoryManager {

    private static final Logger LOGGER =
        Logger.getLogger(InventoryManager.class.getName());

    @Inject
    @ConfigProperty(name = "system.http.port")
    private int SYSTEM_PORT;

    private Map<String, SystemData> systems = new ConcurrentHashMap<>();

    public JsonObject getSystemLoad(String hostname) {
        String uriString = "http://" + hostname + ":" + SYSTEM_PORT + "/system";
        try (SystemClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create(uriString))
                .build(SystemClient.class)) {

            JsonObject obj = client.getSystemLoad();
            LOGGER.log(Level.INFO,
                "Retrieved system load from {0}", hostname);
            return obj;
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING,
                "Runtime exception while invoking system service", e);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                "Unexpected exception while processing system service request", e);
        }
        return null;
    }

    public InventoryList list() {
        return new InventoryList(new ArrayList<>(systems.values()));
    }

    public void set(String host, JsonObject systemLoad) {
        SystemData system = systems.get(host);
        if (system != null) {
            system.setSystemLoad(systemLoad);
        } else {
            systems.put(host, new SystemData(host, systemLoad));
        }
    }

    public void refreshSystemsLoads() {
        for (SystemData system : systems.values()) {
            String hostname = system.getHostname();
            JsonObject systemLoad = getSystemLoad(hostname);
            system.setSystemLoad(systemLoad);
        }
    }

    public int clear() {
        int systemsClearedCount = systems.size();
        systems.clear();
        return systemsClearedCount;
    }
}
```



The updated ***InventoryManager*** class now uses the ***Logger.getLogger()*** method to obtain a logger instance and ***Logger.log()*** method to write messages at appropriate levels, such as ***INFO*** for successful operations and ***WARNING*** for exceptions.

Because the services are running in dev mode, your changes are automatically picked up.

Return to the ***http\://localhost:9081/inventory/systems/localhost*** URL to trigger a successful request. Rerun the Loki query in Grafana and locate the log entry ***Retrieved system load from localhost***. Expand the entry and verify that the ***detected_level*** is set to ***INFO*** and the ***io_openliberty_module*** field contains the logger name, ***io.openliberty.guides.inventory.client.InventoryManager***, which helps trace the origin of the log.

![Example log entry from java.util.logging API at INFO level](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/log_logger_info.png)


Next, access the ***http\://localhost:9081/inventory/systems/unknown*** URL to trigger an exception. Rerun the Loki query and locate the log entry ***Runtime exception while invoking system service***. When expanded, the entry shows that the ***detected_level*** field is set to ***WARNING***. The ***exception_stacktrace*** field contains a structured stack trace, and the ***exception_type*** field identifies the exception as ***jakarta.ws.rs.ProcessingException***.

![Example log entry from java.util.logging API at WARNING level](https://raw.githubusercontent.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/draft/assets/log_logger_warning.png)


By default, OpenTelemetry collects only message logs. For details on how to include other sources, see [Collect logs from a specified source](https://openliberty.io/docs/latest/reference/feature/mpTelemetry-2.0.html#logs).

::page{title="Testing the application"}

Manually verify the telemetry signals by inspecting them in the Grafana dashboard. You can also run the included tests to check the basic functionality of the services. If any of the tests fail, you might have introduced a bug into the code.

### Running the tests

Because you started Open Liberty in dev mode, you can run the tests for the ***system*** and ***inventory*** services by pressing the ***enter/return*** key from the command-line sessions where you started the services.

If the tests pass, you see an output for each service similar to the following:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.445 s -- in it.io.openliberty.guides.system.SystemEndpointIT

Results:

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
...
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.855 s -- in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results:

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

When you are done checking out the services, exit dev mode by pressing `Ctrl+C` in the shell sessions where you ran the ***system*** and ***inventory*** services.

Finally, run the following command to stop the container that you started from the ***grafana/otel-lgtm*** image in the **Additional prerequisites** section.

```bash
docker stop otel-lgtm
```


::page{title="Summary"}

### Nice Work!

You just used MicroProfile Telemetry in Open Liberty to enable traces, metrics, and logs for microservices and the Grafana stack to collect and visualize the data.


Try out one of the related MicroProfile guides. These guides demonstrate more technologies that you can learn to expand on what you built in this guide.


### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***draft-guide-microprofile-telemetry-grafana-automatic*** project by running the following commands:

```bash
cd /home/project
rm -fr draft-guide-microprofile-telemetry-grafana-automatic
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Enabling%20observability%20in%20microservices%20with%20traces,%20metrics,%20and%20logs%20using%20OpenTelemetry%20and%20Grafana&guide-id=cloud-hosted-draft-guide-microprofile-telemetry-grafana-automatic)

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/draft-guide-microprofile-telemetry-grafana-automatic/pulls)



### Where to next?

* [Enabling distributed tracing in microservices with OpenTelemetry and Jaeger](https://openliberty.io/guides/microprofile-telemetry-jaeger.html)
* [Providing metrics from a microservice](https://openliberty.io/guides/microprofile-metrics.html)
* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Consuming RESTful services with template interfaces](https://openliberty.io/guides/microprofile-rest-client.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
