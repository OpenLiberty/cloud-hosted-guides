---
markdown-version: v1
title: instructions
branch: lab-152-instruction
version-history-start-date: 2021-03-03 17:52:50 UTC
---


::page{title="What you'll learn"}

You will learn how to use MicroProfile Metrics to provide metrics from a microservice. You can monitor metrics to determine the performance and health of a service. You can also use them to pinpoint issues, collect data for capacity planning, or to decide when to scale a service to run with more or fewer resources.

The application that you will work with is an ***inventory*** service that stores information about various
systems. The ***inventory*** service communicates with the ***system*** service on a particular host to retrieve
its system properties when necessary.

You will use annotations provided by MicroProfile Metrics to instrument the ***inventory*** service to
provide application-level metrics data. You will add counter, gauge, and timer metrics to the service.

You will also check well-known REST endpoints that are defined by MicroProfile Metrics to review the metrics data collected. Monitoring agents can access these endpoints to collect metrics.





Open another command-line session by selecting ***Terminal*** > ***New Terminal*** from the menu of the IDE.

Run the following curl command to access the **inventory** service. Because you just started the application, the inventory is empty. 
```bash
curl -s http://localhost:9080/inventory/systems | jq
```

Run the following curl command to add the ***localhost*** into the inventory.
```bash
curl -s http://localhost:9080/inventory/systems/localhost | jq
```

Access the ***inventory*** service at the ***http://localhost:9080/inventory/systems*** URL at least once so that application metrics are collected. Otherwise, the metrics do not appear.

Next, run the following curl command to visit the MicroProfile Metrics endpoint by the ***admin*** user with ***adminpwd*** as the password.  You can see both the system and application metrics in a text format.
```bash
curl -k --user admin:adminpwd https://localhost:9443/metrics
```

To see only the application metrics, run the following curl command:
```bash
curl -k --user admin:adminpwd https://localhost:9443/metrics/application
```

See the following sample outputs for the ***@Timed***, ***@Gauge***, and ***@Counted*** metrics:

```
# TYPE application_inventoryProcessingTime_rate_per_second gauge
application_inventoryProcessingTime_rate_per_second{method="get"} 0.0019189661542898407
...
# TYPE application_inventoryProcessingTime_seconds summary
# HELP application_inventoryProcessingTime_seconds Time needed to process the inventory
application_inventoryProcessingTime_seconds_count{method="get"} 1
application_inventoryProcessingTime_seconds{method="get",quantile="0.5"} 0.127965469
...
# TYPE application_inventoryProcessingTime_rate_per_second gauge
application_inventoryProcessingTime_rate_per_second{method="list"} 0.0038379320982686884
...
# TYPE application_inventoryProcessingTime_seconds summary
# HELP application_inventoryProcessingTime_seconds Time needed to process the inventory
application_inventoryProcessingTime_seconds_count{method="list"} 2
application_inventoryProcessingTime_seconds{method="list",quantile="0.5"} 2.2185000000000002E-5
...
```
```
# TYPE application_inventorySizeGauge gauge
# HELP application_inventorySizeGauge Number of systems in the inventory
application_inventorySizeGauge 1
```
```
# TYPE application_inventoryAccessCount_total counter
# HELP application_inventoryAccessCount_total Number of times the list of systems method is requested
application_inventoryAccessCount_total 1
```


To see only the system metrics, run the following curl command:
```bash
curl -k --user admin:adminpwd https://localhost:9443/metrics/base
```

See the following sample output:

```
# TYPE base_jvm_uptime_seconds gauge
# HELP base_jvm_uptime_seconds Displays the start time of the Java virtual machine in milliseconds. This attribute displays the approximate time when the Java virtual machine started.
base_jvm_uptime_seconds 30.342000000000002
```
```
# TYPE base_classloader_loadedClasses_count gauge
# HELP base_classloader_loadedClasses_count Displays the number of classes that are currently loaded in the Java virtual machine.
base_classloader_loadedClasses_count 11231
```


To see only the vendor metrics, run the following curl command:
```bash
curl -k --user admin:adminpwd https://localhost:9443/metrics/vendor
```

See the following sample output:

```
# TYPE vendor_threadpool_size gauge
# HELP vendor_threadpool_size The size of the thread pool.
vendor_threadpool_size{pool="Default_Executor"} 32
```
```
# TYPE vendor_servlet_request_total counter
# HELP vendor_servlet_request_total The number of visits to this servlet since the start of the server.
vendor_servlet_request_total{servlet="microprofile_metrics_io_openliberty_guides_inventory_InventoryApplication"} 1
```



::page{title="Adding MicroProfile Metrics to the inventory service"}




To begin, run the following command to navigate to the **start** directory:
```bash
cd /home/project/guide-microprofile-metrics/start
```


The MicroProfile Metrics API is included in the MicroProfile dependency specified by your ***pom.xml*** file.
Look for the dependency with the ***microprofile*** artifact ID.
This dependency provides a library that allows you to use the MicroProfile Metrics API
in your code to provide metrics from your microservices.

Replace the server configuration file.

> To open the unknown file in your IDE, select
> **File** > **Open** > guide-microprofile-metrics/start/unknown, or click the following button

::openFile{path="/home/project/guide-microprofile-metrics/start/unknown"}


The ***mpMetrics*** feature enables MicroProfile Metrics support in Open Liberty. Note that this
feature requires SSL and the configuration has been provided for you.

The ***quickStartSecurity*** configuration element provides basic security to secure the server. 
When you visit the ***/metrics*** endpoint, use the credentials defined in the server configuration to log in and view the data.

The ***quickStartSecurity*** configuration element provides basic security to secure the server. When you visit the ***/metrics*** endpoint, use the credentials defined in the server configuration to log in and view the data.

### Adding the annotations

Replace the ***InventoryManager*** class.

> To open the InventoryManager.java file in your IDE, select
> **File** > **Open** > guide-microprofile-metrics/start/src/main/java/io/openliberty/guides/inventory/InventoryManager.java, or click the following button

::openFile{path="/home/project/guide-microprofile-metrics/start/src/main/java/io/openliberty/guides/inventory/InventoryManager.java"}

::openFile{path="/home/project/guide-microprofile-metrics/start/src/main/java/io/openliberty/guides/inventory/InventoryManager.java"}


```java
package io.openliberty.guides.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;

import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;

@ApplicationScoped
public class InventoryManager {

  private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());
  private InventoryUtils invUtils = new InventoryUtils();

  @Timed(name = "inventoryProcessingTime",
         tags = {"method=get"},
         absolute = true,
         description = "Time needed to process the inventory")
  public Properties get(String hostname) {
    return invUtils.getProperties(hostname);
  }

  @SimplyTimed(name = "inventoryAddingTime",
    absolute = true,
    description = "Time needed to add system properties to the inventory")
  public void add(String hostname, Properties systemProps) {
    Properties props = new Properties();
    props.setProperty("os.name", systemProps.getProperty("os.name"));
    props.setProperty("user.name", systemProps.getProperty("user.name"));

    SystemData host = new SystemData(hostname, props);
    if (!systems.contains(host)) {
      systems.add(host);
    }
  }

  @Timed(name = "inventoryProcessingTime",
         tags = {"method=list"},
         absolute = true,
         description = "Time needed to process the inventory")
  @Counted(name = "inventoryAccessCount",
           absolute = true,
           description = "Number of times the list of systems method is requested")
  public InventoryList list() {
    return new InventoryList(systems);
  }

  @Gauge(unit = MetricUnits.NONE,
         name = "inventorySizeGauge",
         absolute = true,
         description = "Number of systems in the inventory")
  public int getTotal() {
    return systems.size();
  }
}
```



Apply the ***@Timed*** annotation to the ***get()*** method,
and apply the ***@Timed*** annotation to the ***list()*** method.

This annotation has these metadata fields:

|***name*** | Optional. Use this field to name the metric.
| ---| ---
|***tags*** | Optional. Use this field to add tags to the metric with the same ***name***.
|***absolute*** | Optional. Use this field to determine whether the metric name is the exact name that is specified in the ***name*** field or that is specified with the package prefix.
|***description*** | Optional. Use this field to describe the purpose of the metric.

The ***@Timed*** annotation tracks how frequently the method is invoked and how long it takes for each invocation of the method to complete.
Both the ***get()*** and ***list()*** methods are annotated with the ***@Timed*** metric and have the same ***inventoryProcessingTime*** name. The ***method=get*** and ***method=list*** tags add a dimension that uniquely identifies the collected metric data from the inventory processing time in getting the system properties.

* The ***method=get*** tag identifies the ***inventoryProcessingTime*** metric that measures the elapsed time to get the system properties when you call the ***system*** service.
* The ***method=list*** tag identifies the ***inventoryProcessingTime*** metric that measures the elapsed time for the ***inventory*** service to list all of the system properties in the inventory.

The tags allow you to query the metrics together or separately based on the functionality of the monitoring tool of your choice. The ***inventoryProcessingTime*** metrics for example could be queried to display an aggregate time of both tagged metrics or individual times.

Apply the ***@SimplyTimed*** annotation to the ***add()*** method to track how frequently the method is invoked and how long it takes for each invocation of the method to complete. ***@SimplyTimed*** supports the same fields as ***@Timed*** in the previous table.

Apply the ***@Counted*** annotation to the ***list()*** method to count how many times the
***http://localhost:9080/inventory/systems*** URL is accessed monotonically, which is counting up sequentially.

Apply the ***@Gauge*** annotation to the ***getTotal()*** method to track the number of systems that are stored in
the inventory. When the value of the gauge is retrieved, the underlying ***getTotal()*** method
is called to return the size of the inventory. Note the additional metadata field:

| ***unit*** | Set the unit of the metric. If it is ***MetricUnits.NONE***, the metric name is used without appending the unit name, no scaling is applied.
| ---| ---

Additional information about these annotations, relevant metadata fields, and more are available at
the [MicroProfile Metrics Annotation Javadoc](https://openliberty.io/docs/latest/reference/javadoc/microprofile-4.0-javadoc.html#package=org/eclipse/microprofile/metrics/annotation/package-frame.html&class=org/eclipse/microprofile/metrics/annotation/package-summary.html).


::page{title="Enabling vendor metrics for the microservices"}


MicroProfile Metrics API implementers can provide vendor metrics in the same forms as the base and application metrics do.
Open Liberty as a vendor supplies server component metrics when the ***mpMetrics*** feature is enabled in the ***server.xml*** configuration file.

You can see the vendor-only metrics in the ***metrics/vendor*** endpoint.
You see metrics from the runtime components, such as Web Application, ThreadPool and Session Management.
Note that these metrics are specific to the Liberty application server. Different vendors may provide other metrics.
Visit the [Metrics reference list](https://openliberty.io/docs/ref/general/#metrics-list.html) for more information.


::page{title="Building and running the application"}

The Open Liberty server was started in development mode at the beginning of the guide and all the changes were automatically picked up.


Run the following curl command to review all the metrics that are enabled through MicroProfile Metrics. You see only the system and vendor metrics because the server just started, and the ***inventory*** service has not been accessed.
```bash
curl -k --user admin:adminpwd https://localhost:9443/metrics
```

Next, run the following curl command to access the **inventory** service:
```bash
curl -s http://localhost:9080/inventory/systems | jq
```

Rerun the following curl command to access the all metrics:
```bash
curl -k --user admin:adminpwd https://localhost:9443/metrics
```

or access only the application metrics by running following curl command:
```bash
curl -k --user admin:adminpwd https://localhost:9443/metrics/application
```

You can see the system metrics by running following curl command:
```bash
curl -k --user admin:adminpwd https://localhost:9443/metrics/base
```

as well as see the vendor metrics by running following curl command:
```bash
curl -k --user admin:adminpwd https://localhost:9443/metrics/vendor
```



::page{title="Testing the metrics"}

You can test your application manually, but automated tests ensure code quality because they trigger a failure whenever a code change introduces a defect. JUnit and the restfulWS Client API provide a simple environment for you to write tests.

You can test your application manually, but automated tests ensure code quality because they trigger a
failure whenever a code change introduces a defect. JUnit and the JAX-RS Client API provide a simple
environment for you to write tests.

Create the ***MetricsIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-metrics/start/src/test/java/it/io/openliberty/guides/metrics/MetricsIT.java
```

::openFile{path="/home/project/guide-microprofile-metrics/start/src/test/java/it/io/openliberty/guides/metrics/MetricsIT.java"}

> Then, to open the unknown file in your IDE, select
> **File** > **Open** > guide-microprofile-metrics/start/unknown, or click the following button

::openFile{path="/home/project/guide-microprofile-metrics/start/unknown"}


* The ***testPropertiesRequestTimeMetric()*** test case validates the ***@Timed*** metric. The test case sends a request to the
***http://localhost:9080/inventory/systems/localhost*** URL to access the ***inventory*** service, which adds
the ***localhost*** host to the inventory. Next, the test case makes a connection to the
***https://localhost:9443/metrics/application*** URL to retrieve application metrics as plain text.
Then, it asserts whether the time that is needed to retrieve
the system properties for localhost is less than 4 seconds.

* The ***testInventoryAccessCountMetric()*** test case validates the ***@Counted*** metric. 
The test case obtains metric data before and after a request to the ***http://localhost:9080/inventory/systems*** URL.
It then asserts that the metric was increased after the URL was accessed.

* The ***testInventorySizeGaugeMetric()*** test case validates the ***@Gauge*** metric. The test case first ensures
that the localhost is in the inventory, then looks for the ***@Gauge*** metric and asserts
that the inventory size is greater or equal to 1.

* The ***testPropertiesAddSimplyTimeMetric()*** test case validates the ***@SimplyTimed*** metric. The test case sends a request to the
***http://localhost:9080/inventory/systems/localhost*** URL to access the ***inventory*** service, which adds
the ***localhost*** host to the inventory. Next, the test case makes a connection to the
***https://localhost:9443/metrics/application*** URL to retrieve application metrics as plain text. Then, it looks for the ***@SimplyTimed*** metric and asserts true if the metric exists.

The ***oneTimeSetup()*** method retrieves the port number for the server and builds a base URL string
to set up the tests. Apply the ***@BeforeAll*** annotation to this method to run it before any of
the test cases.

The ***setup()*** method creates a JAX-RS client that makes HTTP requests to the ***inventory*** service.
Register this client with a ***JsrJsonpProvider*** JSON-P provider to process JSON resources. The
***teardown()*** method destroys this client instance. Apply the ***@BeforeEach*** annotation so that a method
runs before a test case and apply the ***@AfterEach*** annotation so that a method runs after a test
case. Apply these annotations to methods that are generally used to perform any setup and teardown tasks
before and after a test.

To force these test cases to run in a particular order, annotate your ***MetricsIT*** test class with the ***@TestMethodOrder(OrderAnnotation.class)*** annotation.
***OrderAnnotation.class*** runs test methods in numerical order, 
according to the values specified in the ***@Order*** annotation. 
You can also create a custom ***MethodOrderer*** class or use built-in ***MethodOrderer*** implementations, 
such as ***OrderAnnotation.class***, ***Alphanumeric.class***, or ***Random.class***. Label your test cases
with the ***@Test*** annotation so that they automatically run when your test class runs.

In addition, the endpoint tests ***src/test/java/it/io/openliberty/guides/inventory/InventoryEndpointIT.java***
and ***src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java*** are provided for you to
test the basic functionality of the ***inventory*** and ***system*** services. If a test failure occurs, then you might have
introduced a bug into the code.

In addition, the endpoint tests ***src/test/java/it/io/openliberty/guides/inventory/InventoryEndpointIT.java*** and ***src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java*** are provided for you to test the basic functionality of the ***inventory*** and ***system*** services. If a test failure occurs, then you might have introduced a bug into the code.

### Running the tests

Because you started Open Liberty in development mode at the start of the guide, press the ***enter/return*** key to run the tests and see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.4 sec - in it.io.openliberty.guides.system.SystemEndpointIT
Running it.io.openliberty.guides.metrics.MetricsIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.476 sec - in it.io.openliberty.guides.metrics.MetricsIT
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
[WARNING ] Interceptor for {http://client.inventory.guides.openliberty.io/}SystemClient has thrown exception, unwinding now
Could not send Message.
[err] The specified host is unknown.
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.264 sec - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results :

Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

The warning and error messages are expected and result from a request to a bad or an unknown hostname. 
This request is made in the ***testUnknownHost()*** test from the ***InventoryEndpointIT*** integration test.

To determine whether the tests detect a failure, go to the ***MetricsIT.java*** file and change any of the assertions
in the test methods. Then re-run the tests to see a test failure occur.



::page{title="Summary"}

### Nice Work!

You learned how to enable system, application and vendor metrics for microservices by using MicroProfile Metrics

and wrote tests to validate them in Open Liberty.


### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-metrics*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-metrics
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Providing%20metrics%20from%20a%20microservice&guide-id=cloud-hosted-guide-microprofile-metrics)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-metrics/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-metrics/pulls)



### Where to next?

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Adding health reports to microservices](https://openliberty.io/guides/microprofile-health.html)
* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
