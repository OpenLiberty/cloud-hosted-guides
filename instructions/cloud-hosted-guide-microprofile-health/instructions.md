---
markdown-version: v1
tool-type: theia
---
::page{title="Welcome to the Adding health reports to microservices guide!"}

Explore how to report and check the health of a microservice with MicroProfile Health.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You will learn how to use MicroProfile Health to report the health status of microservices and take appropriate actions based on this report.

MicroProfile Health allows services to report their health, and it publishes the overall health status to a defined endpoint. A service reports ***UP*** if it is available and reports ***DOWN*** if it is unavailable. MicroProfile Health reports an individual service status at the endpoint and indicates the overall status as ***UP*** if all the services are ***UP***. A service orchestrator can then use the health statuses to make decisions.

A service checks its own health by performing necessary self-checks and then reports its overall status by implementing the API provided by MicroProfile Health. A self-check can be a check on anything that the service needs, such as a dependency, a successful connection to an endpoint, a system property, a database connection, or the availability of required resources. MicroProfile offers checks for startup, liveness, and readiness.

You will add startup, liveness, and readiness checks to the ***system*** and ***inventory*** services, that are provided for you, and implement what is necessary to report health status by using MicroProfile Health.


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-health.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microprofile-health.git
cd guide-microprofile-health
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.


### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the ***finish*** directory and run the following Maven goal to build the application and deploy it to Open Liberty:

```bash
cd finish
mvn liberty:run
```

After you see the following message, your Liberty instance is ready:

```
The defaultServer server is ready to run a smarter planet.
```


Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE. To access the **system** service, run the following curl command:
```bash
curl -s http://localhost:9080/system/properties | jq
```

To access the ***inventory*** service, run the following curl command:
```bash
curl -s http://localhost:9080/inventory/systems | jq
```

Visit the http://localhost:9080/health URL to see the overall health status of the application, as well as the aggregated data of the startup, liveness, and readiness checks. Run the following curl command:
```bash
curl -s http://localhost:9080/health | jq
```

Three checks show the state of the ***system*** service, and the other three checks show the state of the ***inventory*** service. As you might expect, all services are in the **UP** state, and the overall health status of the application is in the ***UP*** state.

Access the ***/health/started*** endpoint by visiting the http://localhost:9080/health/started URL to view the data from the startup health checks. Run the following curl command:
```bash
curl -s http://localhost:9080/health/started | jq
```

You can also access the ***/health/live*** endpoint by visiting the http://localhost:9080/health/live URL to view the data from the liveness health checks. Run the following curl command:
```bash
curl -s http://localhost:9080/health/live | jq
```

Similarly, access the ***/health/ready*** endpoint by visiting the http://localhost:9080/health/ready URL to view the data from the readiness health checks. Run the following curl command:
```bash
curl -s http://localhost:9080/health/ready | jq
```

After you are finished checking out the application, stop the Liberty instance by pressing `Ctrl+C` in the command-line session where you ran Liberty. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```


::page{title="Adding health checks to microservices"}


To begin, run the following command to navigate to the ***start*** directory:
```bash
cd /home/project/guide-microprofile-health/start
```

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), dev mode listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```bash
mvn liberty:dev
```

After you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

A health report will be generated automatically for all services that enable MicroProfile Health. The ***mpHealth*** feature has already been enabled for you in the ***src/main/liberty/config/server.xml*** file.

All services must provide an implementation of the ***HealthCheck*** interface, which is used to verify their health. MicroProfile Health offers health checks for startup, liveness, and readiness. A startup check allows applications to define startup probes that are used for initial verification of the application before the Liveness probe takes over. For example, a startup check might check which applications require additional startup time on their first initialization. A liveness check allows third-party services to determine whether a microservice is running. If the liveness check fails, the application can be terminated. For example, a liveness check might fail if the application runs out of memory. A readiness check allows third-party services, such as Kubernetes, to determine whether a microservice is ready to process requests. For example, a readiness check might check dependencies, such as database connections.



### Adding health checks to the system service

Create the ***SystemStartupCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-health/start/src/main/java/io/openliberty/guides/system/SystemStartupCheck.java
```


> Then, to open the SystemStartupCheck.java file in your IDE, select
> **File** > **Open** > guide-microprofile-health/start/src/main/java/io/openliberty/guides/system/SystemStartupCheck.java, or click the following button

::openFile{path="/home/project/guide-microprofile-health/start/src/main/java/io/openliberty/guides/system/SystemStartupCheck.java"}



```java
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Startup;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Startup
@ApplicationScoped
public class SystemStartupCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean)
        ManagementFactory.getOperatingSystemMXBean();
        double cpuUsed = bean.getSystemCpuLoad();
        String cpuUsage = String.valueOf(cpuUsed);
        return HealthCheckResponse.named(SystemResource.class
                                            .getSimpleName() + " Startup Check")
                                            .status(cpuUsed < 0.95).build();
    }
}

```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***@Startup*** annotation indicates that this class is a startup health check procedure. In this case, you are checking the cpu usage. If more than 95% of the cpu is being used, a status of ***DOWN*** is returned.

Create the ***SystemLivenessCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-health/start/src/main/java/io/openliberty/guides/system/SystemLivenessCheck.java
```


> Then, to open the SystemLivenessCheck.java file in your IDE, select
> **File** > **Open** > guide-microprofile-health/start/src/main/java/io/openliberty/guides/system/SystemLivenessCheck.java, or click the following button

::openFile{path="/home/project/guide-microprofile-health/start/src/main/java/io/openliberty/guides/system/SystemLivenessCheck.java"}



```java
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Liveness
@ApplicationScoped
public class SystemLivenessCheck implements HealthCheck {

  @Override
  public HealthCheckResponse call() {
    MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
    long memUsed = memBean.getHeapMemoryUsage().getUsed();
    long memMax = memBean.getHeapMemoryUsage().getMax();

    return HealthCheckResponse.named(
      SystemResource.class.getSimpleName() + " Liveness Check")
                              .status(memUsed < memMax * 0.9).build();
  }
}
```



The ***@Liveness*** annotation indicates that this class is a liveness health check procedure. In this case, you are checking the heap memory usage. If more than 90% of the maximum memory is being used, a status of ***DOWN*** is returned.

Create the ***SystemReadinessCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-health/start/src/main/java/io/openliberty/guides/system/SystemReadinessCheck.java
```


> Then, to open the SystemReadinessCheck.java file in your IDE, select
> **File** > **Open** > guide-microprofile-health/start/src/main/java/io/openliberty/guides/system/SystemReadinessCheck.java, or click the following button

::openFile{path="/home/project/guide-microprofile-health/start/src/main/java/io/openliberty/guides/system/SystemReadinessCheck.java"}



```java
package io.openliberty.guides.system;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Readiness
@ApplicationScoped
public class SystemReadinessCheck implements HealthCheck {

  private static final String READINESS_CHECK = SystemResource.class.getSimpleName()
                                               + " Readiness Check";
  @Override
  public HealthCheckResponse call() {
    if (!System.getProperty("wlp.server.name").equals("defaultServer")) {
      return HealthCheckResponse.down(READINESS_CHECK);
    }
    return HealthCheckResponse.up(READINESS_CHECK);
  }
}
```




The ***@Readiness*** annotation indicates that this class is a readiness health check procedure. By pairing this annotation with the ***ApplicationScoped*** context from the Contexts and Dependency Injections API, the bean is discovered automatically when the http://localhost:9080/health endpoint receives a request.


The ***call()*** method is used to return the health status of a particular service. In this case, you are checking if the server name is ***defaultServer*** and returning ***UP*** if it is, and ***DOWN*** otherwise. This example is a very simple implementation of the ***call()*** method. In a real environment, you would orchestrate more meaningful health checks.


### Adding health checks to the inventory service

Create the ***InventoryStartupCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-health/start/src/main/java/io/openliberty/guides/inventory/InventoryStartupCheck.java
```


> Then, to open the InventoryStartupCheck.java file in your IDE, select
> **File** > **Open** > guide-microprofile-health/start/src/main/java/io/openliberty/guides/inventory/InventoryStartupCheck.java, or click the following button

::openFile{path="/home/project/guide-microprofile-health/start/src/main/java/io/openliberty/guides/inventory/InventoryStartupCheck.java"}



```java
package io.openliberty.guides.inventory;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Startup;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Startup
@ApplicationScoped
public class InventoryStartupCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean)
        ManagementFactory.getOperatingSystemMXBean();
        double cpuUsed = bean.getSystemCpuLoad();
        String cpuUsage = String.valueOf(cpuUsed);
        return HealthCheckResponse.named(InventoryResource.class
                                            .getSimpleName() + " Startup Check")
                                            .status(cpuUsed < 0.95).build();
    }
}

```



This startup check verifies that the cpu usage is below 95%.
If more than 95% of the cpu is being used, a status of ***DOWN*** is returned.

Create the ***InventoryLivenessCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-health/start/src/main/java/io/openliberty/guides/inventory/InventoryLivenessCheck.java
```


> Then, to open the InventoryLivenessCheck.java file in your IDE, select
> **File** > **Open** > guide-microprofile-health/start/src/main/java/io/openliberty/guides/inventory/InventoryLivenessCheck.java, or click the following button

::openFile{path="/home/project/guide-microprofile-health/start/src/main/java/io/openliberty/guides/inventory/InventoryLivenessCheck.java"}



```java
package io.openliberty.guides.inventory;

import jakarta.enterprise.context.ApplicationScoped;

import java.lang.management.MemoryMXBean;
import java.lang.management.ManagementFactory;

import org.eclipse.microprofile.health.Liveness;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Liveness
@ApplicationScoped
public class InventoryLivenessCheck implements HealthCheck {
  @Override
  public HealthCheckResponse call() {
      MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
      long memUsed = memBean.getHeapMemoryUsage().getUsed();
      long memMax = memBean.getHeapMemoryUsage().getMax();

      return HealthCheckResponse.named(
        InventoryResource.class.getSimpleName() + " Liveness Check")
                                .status(memUsed < memMax * 0.9).build();
  }
}
```



As with the ***system*** liveness check, you are checking the heap memory usage. If more than 90% of the maximum memory is being used, a ***DOWN*** status is returned.

Create the ***InventoryReadinessCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-health/start/src/main/java/io/openliberty/guides/inventory/InventoryReadinessCheck.java
```


> Then, to open the InventoryReadinessCheck.java file in your IDE, select
> **File** > **Open** > guide-microprofile-health/start/src/main/java/io/openliberty/guides/inventory/InventoryReadinessCheck.java, or click the following button

::openFile{path="/home/project/guide-microprofile-health/start/src/main/java/io/openliberty/guides/inventory/InventoryReadinessCheck.java"}



```java
package io.openliberty.guides.inventory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Readiness
@ApplicationScoped
public class InventoryReadinessCheck implements HealthCheck {

  private static final String READINESS_CHECK = InventoryResource.class.getSimpleName()
                                               + " Readiness Check";
  @Inject
  InventoryConfig config;

  public boolean isHealthy() {
    if (config.isInMaintenance()) {
      return false;
    }
    try {
      String url = InventoryUtils.buildUrl("http", "localhost", config.getPortNumber(),
          "/system/properties");
      Client client = ClientBuilder.newClient();
      Response response = client.target(url).request(MediaType.APPLICATION_JSON).get();
      if (response.getStatus() != 200) {
        return false;
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public HealthCheckResponse call() {
    if (!isHealthy()) {
      return HealthCheckResponse
          .down(READINESS_CHECK);
    }
    return HealthCheckResponse
        .up(READINESS_CHECK);
  }

}
```



In the ***isHealthy()*** method, you report the ***inventory*** service as not ready if the service is in maintenance or if its dependant service is unavailable.

For simplicity, the custom ***io_openliberty_guides_inventory_inMaintenance*** MicroProfile Config property, which is defined in the ***resources/CustomConfigSource.json*** file, indicates whether the service is in maintenance. This file was already created for you.

Moreover, the readiness health check procedure makes an HTTP ***GET*** request to the ***system*** service and checks its status. If the request is successful, the ***inventory*** service is healthy and ready because its dependant service is available. Otherwise, the ***inventory*** service is not ready and an unhealthy readiness status is returned.

If you are curious about the injected ***inventoryConfig*** object or if you want to learn more about MicroProfile Config, see [Configuring microservices](https://openliberty.io/guides/microprofile-config.html).



::page{title="Running the application"}

You started the Open Liberty in dev mode at the beginning of the guide, so all the changes were automatically picked up.


While the Liberty is running, run the following curl command to find the aggregated startup ,liveness, and readiness health reports on the two services:
```bash
curl -s http://localhost:9080/health | jq
```

You can also run the following curl command to view the startup health report:
```bash
curl -s http://localhost:9080/health/started | jq
```

or run the following curl command to view the liveness health report:
```bash
curl -s http://localhost:9080/health/live | jq
```

or run the following curl command to view the readiness health report:
```bash
curl -s http://localhost:9080/health/ready | jq
```

Put the ***inventory*** service in maintenance by setting the ***io_openliberty_guides_inventory_inMaintenance*** property to ***true*** in the ***resources/CustomConfigSource.json*** file. 

> From the menu of the IDE, select 
 **File** > **Open** > guide-microprofile-health/start/resources/CustomConfigSource.json, or click the following button

::openFile{path="/home/project/guide-microprofile-health/start/resources/CustomConfigSource.json"}

```text
{
  "config_ordinal":700,
  "io_openliberty_guides_inventory_inMaintenance":true
}
```

Because this configuration file is picked up dynamically, simply refresh the http://localhost:9080/health URL to see that the state of the **inventory** service changed to ***DOWN***. Run the following curl command:
```bash
curl -s http://localhost:9080/health | jq
```

The overall state of the application also changed to ***DOWN*** as a result. Run the following curl command to verify that the **inventory** service is indeed in maintenance:
```bash
curl -s http://localhost:9080/inventory/systems | jq
```

Set the ***io_openliberty_guides_inventory_inMaintenance*** property back to **false** after you are done.

> From the menu of the IDE, select 
 **File** > **Open** > guide-microprofile-health/start/resources/CustomConfigSource.json, or click the following button

::openFile{path="/home/project/guide-microprofile-health/start/resources/CustomConfigSource.json"}

```text
{
  "config_ordinal":700,
  "io_openliberty_guides_system_inMaintenance":false
}
```



::page{title="Testing health checks"}

You will implement several test methods to validate the health of the ***system*** and ***inventory*** services.

Create the ***HealthIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-health/start/src/test/java/it/io/openliberty/guides/health/HealthIT.java
```


> Then, to open the HealthIT.java file in your IDE, select
> **File** > **Open** > guide-microprofile-health/start/src/test/java/it/io/openliberty/guides/health/HealthIT.java, or click the following button

::openFile{path="/home/project/guide-microprofile-health/start/src/test/java/it/io/openliberty/guides/health/HealthIT.java"}



```java
package it.io.openliberty.guides.health;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.beans.Transient;
import java.util.HashMap;

import jakarta.json.JsonArray;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HealthIT {

  private JsonArray servicesStates;
  private static HashMap<String, String> endpointData;

  private String HEALTH_ENDPOINT = "health";
  private String READINESS_ENDPOINT = "health/ready";
  private String LIVENES_ENDPOINT = "health/live";
  private String STARTUP_ENDPOINT = "health/started";

  @BeforeEach
  public void setup() {
    endpointData = new HashMap<String, String>();
  }

  @Test
  public void testStartup() {
    endpointData.put("InventoryResource Startup Check", "UP");
    endpointData.put("SystemResource Startup Check", "UP");

    servicesStates = HealthITUtil.connectToHealthEnpoint(200, STARTUP_ENDPOINT);
    checkStates(endpointData, servicesStates);
  }

  @Test
  public void testLiveness() {
    endpointData.put("SystemResource Liveness Check", "UP");
    endpointData.put("InventoryResource Liveness Check", "UP");

    servicesStates = HealthITUtil.connectToHealthEnpoint(200, LIVENES_ENDPOINT);
    checkStates(endpointData, servicesStates);
  }

  @Test
  public void testReadiness() {
    endpointData.put("SystemResource Readiness Check", "UP");
    endpointData.put("InventoryResource Readiness Check", "UP");

    servicesStates = HealthITUtil.connectToHealthEnpoint(200, READINESS_ENDPOINT);
    checkStates(endpointData, servicesStates);
  }

  @Test
  public void testHealth() {
    endpointData.put("SystemResource Startup Check", "UP");
    endpointData.put("SystemResource Liveness Check", "UP");
    endpointData.put("SystemResource Readiness Check", "UP");
    endpointData.put("InventoryResource Startup Check", "UP");
    endpointData.put("InventoryResource Liveness Check", "UP");
    endpointData.put("InventoryResource Readiness Check", "UP");

    servicesStates = HealthITUtil.connectToHealthEnpoint(200, HEALTH_ENDPOINT);
    checkStates(endpointData, servicesStates);

    endpointData.put("InventoryResource Readiness Check", "DOWN");
    HealthITUtil.changeInventoryProperty(HealthITUtil.INV_MAINTENANCE_FALSE,
        HealthITUtil.INV_MAINTENANCE_TRUE);
    servicesStates = HealthITUtil.connectToHealthEnpoint(503, HEALTH_ENDPOINT);
    checkStates(endpointData, servicesStates);
  }

  private void checkStates(HashMap<String, String> testData, JsonArray servStates) {
    testData.forEach((service, expectedState) -> {
      assertEquals(expectedState, HealthITUtil.getActualState(service, servStates),
          "The state of " + service + " service is not matching.");
    });
  }

  @AfterEach
  public void teardown() {
    HealthITUtil.cleanUp();
  }

}
```




Let's break down the test cases:

* The ***testStartup()*** test case compares the generated health report for the startup checks with the actual status of the services.
* The ***testLiveness()*** test case compares the generated health report for the liveness checks with the actual status of the services.
* The ***testReadiness()*** test case compares the generated health report for the readiness checks with the actual status of the services.
* The ***testHealth()*** test case compares the generated health report with the actual status of the services. This test also puts the ***inventory*** service in maintenance by setting the ***io_openliberty_guides_inventory_inMaintenance*** property to ***true*** and comparing the generated health report with the actual status of the services.

A few more tests were included to verify the basic functionality of the ***system*** and ***inventory*** services. They can be found under the ***src/test/java/it/io/openliberty/guides/inventory/InventoryEndpointIT.java*** and ***src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java*** files. If a test failure occurs, then you might have introduced a bug into the code. These tests run automatically as a part of the integration test suite.






### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

You see the following output:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running it.io.openliberty.guides.health.HealthIT
[INFO] [WARNING ] CWMMH0052W: The class io.openliberty.microprofile.health30.impl.HealthCheck30ResponseImpl implementing HealthCheckResponse in the guide-microprofile-health application in module guide-microprofile-health.war, reported a DOWN status with data Optional[{}].
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.706 s - in it.io.openliberty.guides.health.HealthIT
[INFO] Running it.io.openliberty.guides.system.SystemEndpointIT
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 s - in it.io.openliberty.guides.system.SystemEndpointIT
[INFO] Running it.io.openliberty.guides.inventory.InventoryEndpointIT
[INFO] [WARNING ] Interceptor for {http://client.inventory.guides.openliberty.io/}SystemClient has thrown exception, unwinding now
[INFO] Could not send Message.
[INFO] [err] The specified host is unknown.
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.171 s - in it.io.openliberty.guides.inventory.InventoryEndpointIT
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

The warning messages are expected. The first warning results from a request to a service that is under maintenance. This request is made in the ***testHealth()*** test from the ***InventoryEndpointIT*** integration test. The second warning and error results from a request to a bad or an unknown hostname. This request is made in the ***testUnknownHost()*** test from the ***InventoryEndpointIT*** integration test.

The tests might fail if your system CPU or memory use is high. The status of the system is DOWN if the CPU usage is over 95%, or the memory usage is over 90%.

To see whether the tests detect a failure, manually change the configuration of ***io_openliberty_guides_inventory_inMaintenance*** from ***false*** to ***true*** in the ***resources/CustomConfigSource.json*** file. Rerun the tests to see a test failure occur. The test failure occurs because the initial status of the ***inventory*** service is ***DOWN***.

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran Liberty.


::page{title="Summary"}

### Nice Work!

You just learned how to add health checks to report the states of microservices by using MicroProfile Health in Open Liberty. Then, you wrote tests to validate the generated health report.


Feel free to try one of the related MicroProfile guides. They demonstrate additional technologies that you can learn and expand on top of what you built here.


### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-health*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-health
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Adding%20health%20reports%20to%20microservices&guide-id=cloud-hosted-guide-microprofile-health)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-health/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-health/pulls)



### Where to next?

* [Configuring microservices](https://openliberty.io/guides/microprofile-config.html)
* [Providing metrics from a microservice](https://openliberty.io/guides/microprofile-metrics.html)
* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
