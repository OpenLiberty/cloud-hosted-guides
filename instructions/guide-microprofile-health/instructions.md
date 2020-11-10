
# Adding health reports to microservices


Explore how to report and check the health of a microservice with MicroProfile Health.


## What you'll learn

You will learn how to use MicroProfile Health to report the health status of microservices and take
appropriate actions based on this report.

MicroProfile Health allows services to report their health, and it publishes the overall health status to a defined
endpoint. A service reports `UP` if it is available and reports `DOWN` if it is unavailable. MicroProfile Health reports
an individual service status at the endpoint and indicates the overall status as `UP` if all the services are `UP`. A service
orchestrator can then use the health statuses to make decisions.

A service checks its own health by performing necessary self-checks and then reports its overall status by
implementing the API provided by MicroProfile Health. A self-check can be a check on anything that the service needs, such
as a dependency, a successful connection to an endpoint, a system property, a database connection, or
the availability of required resources. MicroProfile offers checks for both liveness and readiness.

You will add liveness and readiness checks to the `system` and `inventory` services, which
are provided for you, and implement what is necessary to report health status by
using MicroProfile Health.


# Getting started

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/master.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/master.git
cd master
```
{: codeblock}


The `start` directory contains the starting project that you will build upon.

The `finish` directory contains the finished project that you will build.


### Try what you'll build

The `finish` directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the `finish` directory and run the following
Maven goal to build the application and deploy it to Open Liberty:

```
mvn liberty:run
```
{: codeblock}

After you see the following message, your application server is ready.

```
The defaultServer server is ready to run a smarter planet.
```

The `system` and `inventory` services can be found at the following URLs:

- 
```
curl http://localhost:9080/system/properties
```
{: codeblock}



- 
```
curl http://localhost:9080/inventory/systems
```
{: codeblock}



Visit the 
```
curl http://localhost:9080/health
```
{: codeblock}

 URL to see the
overall health status of the application, as well as the aggregated data of the liveness
and readiness checks. Two checks show the state of the `system` service, and the other two
checks show the state of the `inventory` service. As you might expect, both services are in the
`UP` state, and the overall health status of the application is in the `UP` state.

You can also access the `/health/ready` endpoint by visiting the 
```
curl http://localhost:9080/health/ready
```
{: codeblock}


URL to view the data from the readiness health checks. Similarly, access the `/health/live`
endpoint by visiting the 
```
curl http://localhost:9080/health/live
```
{: codeblock}


URL to view the data from the liveness health checks.

After you are finished checking out the application, stop the Open Liberty server by pressing `CTRL+C`
in the command-line session where you ran the server. Alternatively, you can run the `liberty:stop` goal
from the `finish` directory in another shell session:

```
mvn liberty:stop
```
{: codeblock}


# Adding health checks to microservices

Navigate to the `start` directory to begin.

When you run Open Liberty in dev mode, the server listens for file changes and automatically recompiles and 
deploys your updates whenever you save a new change. Run the following goal to start in dev mode:

```
mvn liberty:dev
```
{: codeblock}

After you see the following message, your application server in dev mode is ready:

```
Press the Enter key to run tests on demand.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, 
or open the project in your editor.

A health report will be generated automatically for all services that enable MicroProfile Health. The
`mpHealth` feature has already been enabled for you in the `src/main/liberty/config/server.xml`
file.

All services must provide an implementation of the `HealthCheck` interface, which will be used to
verify their health. MicroProfile Health offers health checks for both readiness and liveness.
A readiness check allows third-party services, such as Kubernetes, to determine whether a microservice
is ready to process requests. For example, a readiness check might check dependencies,
such as database connections. A liveness check allows third-party services to determine
whether a microservice is running. If the liveness check fails, the application can be
terminated. For example, a liveness check might fail if the application runs out of memory.



### Adding health checks to the system service

Create the `SystemReadinessCheck` class.


> [File -> Open]master/start/src/main/java/io/openliberty/guides/system/SystemReadinessCheck.java



```
package io.openliberty.guides.system;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Readiness
@ApplicationScoped
public class SystemReadinessCheck implements HealthCheck {

  private static final String readinessCheck = SystemResource.class.getSimpleName() 
                                               + " Readiness Check";

  @Override
  public HealthCheckResponse call() {
    if (!System.getProperty("wlp.server.name").equals("defaultServer")) {
      return HealthCheckResponse.down(readinessCheck);
    }
    return HealthCheckResponse.up(readinessCheck);
  }
}
```
{: codeblock}


The `@Readiness` annotation indicates that this particular bean is a readiness health check procedure.
By pairing this annotation with the `ApplicationScoped` context from the Contexts and
Dependency Injections API, the bean is discovered automatically when the 
```
curl http://localhost:9080/health
```
{: codeblock}


endpoint receives a request.

The `call()` method is used to return the health status of a particular service.
In this case, you are simply checking if the server name is `defaultServer` and
returning `UP` if it is, and `DOWN` otherwise.  
Overall, this is a very simple implementation of the `call()`
method. In a real development environment, you would want to orchestrate much more meaningful
health checks.


Create the `SystemLivenessCheck` class.


> [File -> Open]master/start/src/main/java/io/openliberty/guides/system/SystemLivenessCheck.java



```
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.enterprise.context.ApplicationScoped;
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

    return HealthCheckResponse.named(SystemResource.class.getSimpleName() + " Liveness Check")
                              .withData("memory used", memUsed)
                              .withData("memory max", memMax)
                              .state(memUsed < memMax * 0.9).build();
  }
}
```
{: codeblock}


The `@Liveness` annotation indicates that this is a liveness health check procedure.
In this case, you are checking the heap memory usage. If more than 90% of the maximum memory
is being used, a status of `DOWN` is returned.


### Adding health checks to the inventory service

Create the `InventoryReadinessCheck` class.


> [File -> Open]master/start/src/main/java/io/openliberty/guides/inventory/InventoryReadinessCheck.java



```
package io.openliberty.guides.inventory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Readiness
@ApplicationScoped
public class InventoryReadinessCheck implements HealthCheck {

  private static final String readinessCheck = InventoryResource.class.getSimpleName() 
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
          .down(readinessCheck);
    }
    return HealthCheckResponse
        .up(readinessCheck);
  }

}
```
{: codeblock}


This time, you are checking whether or not the service is in maintenance or if it's down.
For simplicity, the custom `io_openliberty_guides_inventory_inMaintenance`
MicroProfile Config property defined in the `resources/CustomConfigSource.json`
file is used to indicate whether the service is in maintenance or not. This file was already
created for you. To check whether the service is down, make an HTTP GET request to
the `system` service and check the status that is returned by the response. You make a GET request
to the `system` service rather than the `inventory` service because the `inventory` service
depends on the `system` service. In other words, the `inventory` service doesn't work if
the `system` service is down. If the status is not 200, then the service is not running.
Based on these two factors, the `isHealthy()` method returns whether
the `inventory` service is healthy.

If you are curious about the injected `inventoryConfig` object or if
you want more information on MicroProfile Config, see
[Configuring microservices](https://openliberty.io/guides/microprofile-config.html).

Create the `InventoryLivenessCheck` class.


> [File -> Open]master/start/src/main/java/io/openliberty/guides/inventory/InventoryLivenessCheck.java



```
package io.openliberty.guides.inventory;

import javax.enterprise.context.ApplicationScoped;

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

      return HealthCheckResponse.named(InventoryResource.class.getSimpleName() + " Liveness Check")
                                .withData("memory used", memUsed)
                                .withData("memory max", memMax)
                                .state(memUsed < memMax * 0.9).build();
  }
}
```
{: codeblock}


As with the `system` liveness check, you are checking the heap memory usage. If more
than 90% of the maximum memory is being used, a `DOWN` status is returned.



# Running the application

The Open Liberty server was started in development mode at the beginning of the guide and all the changes were automatically picked up.

While the server is running, navigate to the 
```
curl http://localhost:9080/health
```
{: codeblock}

 URL to find
the aggregated liveness and readiness health reports on the two services.

You can also navigate to the 
```
curl http://localhost:9080/health/ready
```
{: codeblock}


URL to view the readiness health report, or the 
```
curl http://localhost:9080/health/live
```
{: codeblock}


URL to view the liveness health report.

Put the `inventory` service in maintenance by setting the `io_openliberty_guides_inventory_inMaintenance`
property to `true` in the `resources/CustomConfigSource.json` file. Because
this configuration file is picked up dynamically, simply refresh the 
```
curl http://localhost:9080/health
```
{: codeblock}


URL to see that the state of the `inventory` service changed to `DOWN`. The
overall state of the application also changed to `DOWN` as a result. Go to the

```
curl http://localhost:9080/inventory/systems
```
{: codeblock}

 URL to verify that the `inventory` service is
indeed in maintenance. Set the `io_openliberty_guides_inventory_inMaintenance`
property back to `false` after you are done.



# Testing health checks

You will implement several test methods to validate the health of the `system` and `inventory` services.

Create the `HealthIT` class.


> [File -> Open]master/start/src/test/java/it/io/openliberty/guides/health/HealthIT.java



```
package it.io.openliberty.guides.health;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import javax.json.JsonArray;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HealthIT {

  private JsonArray servicesStates;
  private static HashMap<String, String> endpointData;

  private String HEALTH_ENDPOINT = "health";
  private String READINESS_ENDPOINT = "health/ready";
  private String LIVENES_ENDPOINT = "health/live";

  @BeforeEach
  public void setup() {
    endpointData = new HashMap<String, String>();
  }

  @Test
  public void testIfServicesAreUp() {
    endpointData.put("SystemResource Readiness Check", "UP");
    endpointData.put("SystemResource Liveness Check", "UP");
    endpointData.put("InventoryResource Readiness Check", "UP");
    endpointData.put("InventoryResource Liveness Check", "UP");

    servicesStates = HealthITUtil.connectToHealthEnpoint(200, HEALTH_ENDPOINT);
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
  public void testLiveness() {
    endpointData.put("SystemResource Liveness Check", "UP");
    endpointData.put("InventoryResource Liveness Check", "UP");

    servicesStates = HealthITUtil.connectToHealthEnpoint(200, LIVENES_ENDPOINT);
    checkStates(endpointData, servicesStates);
  }

  @Test
  public void testIfInventoryServiceIsDown() {
    endpointData.put("SystemResource Readiness Check", "UP");
    endpointData.put("SystemResource Liveness Check", "UP");
    endpointData.put("InventoryResource Readiness Check", "UP");
    endpointData.put("InventoryResource Liveness Check", "UP");

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
{: codeblock}


Let's break down the test cases:

- The `testIfServicesAreUp()` test case compares the generated health report
with the actual status of the services.
- The `testReadiness()` test case compares the generated health report for the
readiness checks with the actual status of the services.
- The `testLiveness()` test case compares the generated health report for the
liveness checks with the actual status of the services.
- The `testIfInventoryServiceIsDown()` test case puts the `inventory` service
in maintenance by setting the `io_openliberty_guides_inventory_inMaintenance`
property to `true` and comparing the generated health report with the actual status of
the services.

A few more tests were included to verify the basic functionality of the `system` and `inventory`
services. They can be found under the `src/test/java/it/io/openliberty/guides/inventory/InventoryEndpointIT.java`
and `src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java` files.
If a test failure occurs, then you might have introduced a bug into the code. These tests
run automatically as a part of the integration test suite.






### Running the tests

Since you started Open Liberty in dev mode, press the enter/return key to run the tests.

You see the following output:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running it.io.openliberty.guides.health.HealthIT
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.463 s - in it.io.openliberty.guides.health.HealthIT
[INFO] Running it.io.openliberty.guides.system.SystemEndpointIT
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s - in it.io.openliberty.guides.system.SystemEndpointIT
[INFO] Running it.io.openliberty.guides.inventory.InventoryEndpointIT
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.102 s - in it.io.openliberty.guides.inventory.InventoryEndpointIT
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

To see whether the tests detect a failure, manually change the configuration of
`io_openliberty_guides_inventory_inMaintenance` from `false` to `true`
in the `resources/CustomConfigSource.json` file. Rerun the tests to see a test failure occur.
The test failure occurs because the initial status of the `inventory` service is `DOWN`.

When you are done checking out the service, exit development mode by pressing `CTRL+C` in the command-line session
where you ran the server, or by typing `q` and then pressing the `enter/return` key.


# Great work! You're done!

You just learned how to add health checks to report the states of microservices by using
MicroProfile Health in Open Liberty. Then, you wrote tests to validate the generated
health report.

Feel free to try one of the related MicroProfile guides. They demonstrate additional
technologies that you can learn and expand on top of what you built here.

