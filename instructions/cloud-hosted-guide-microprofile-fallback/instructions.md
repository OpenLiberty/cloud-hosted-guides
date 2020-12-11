
# Building fault-tolerant microservices with the @Fallback annotation


You'll explore how to manage the impact of failures using MicroProfile Fault Tolerance by adding fallback behavior to microservice dependencies.


## What you'll learn

You will learn how to use MicroProfile (MP) Fault Tolerance to build resilient microservices
that reduce the impact from failure and ensure continued operation of services.

MP Fault Tolerance provides a simple and flexible solution to build fault-tolerant microservices.
Fault tolerance leverages different strategies to guide the execution and result of logic.
As stated in the [MicroProfile website](https://microprofile.io/project/eclipse/microprofilefaulttolerance),
retry policies, bulkheads, and circuit breakers are popular concepts in this area.
They dictate whether and when executions take place, and fallbacks offer an alternative result
when an execution does not complete successfully.

The application that you will be working with is an **inventory** service, which collects,
stores, and returns the system properties.
It uses the **system** service to retrieve the system properties for a particular host.
You will add fault tolerance to the **inventory** service so that it reacts accordingly when the **system**
service is unavailable.

You will use the **@Fallback** annotations from the MicroProfile Fault Tolerance
specification to define criteria for when to provide an alternative solution for
a failed execution.

You will also see the application metrics for the fault tolerance methods that are automatically enabled
when you add the MicroProfile Metrics feature to the server.



# Getting started

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guidemicroprofilefallback.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-microprofile-fallback.git
cd guide-microprofile-fallback
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.


### Try what you'll build

The **finish** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the **finish** directory and run the following
Maven goal to build the application and deploy it to Open Liberty:

```
cd finish
mvn liberty:run
```
{: codeblock}


After you see the following message, your application server is ready:

```
The defaultServer server is ready to run a smarter planet.
```

Point your browser to the 
```
curl http://localhost:9080/inventory/systems/localhost
```
{: codeblock}


 URL, which accesses the
**inventory** service with a localhost hostname. You see the system properties for this host.
When you visit this URL, some of these system
properties, such as the OS name and user name, are automatically stored in the inventory.

Update the **CustomConfigSource** configuration file.


> [File -> Open]guide-microprofile-fallback/start/resources/CustomConfigSource.json



```
{"config_ordinal":500,
"io_openliberty_guides_system_inMaintenance":false}
```
{: codeblock}



Change the **io_openliberty_guides_system_inMaintenance** property from **false** to **true** and save the file.

You do not need
to restart the server. Next, return to your browser and point back to the
[http://localhost:9080/inventory/systems/localhost](http://localhost:9080/inventory/systems/localhost) URL

```
curl http://localhost:9080/inventory/systems/localhost
```
{: codeblock}


The fallback mechanism is triggered because the **system** service is now in maintenance.
You see the cached properties for this localhost.

When you are done checking out the application, go to the **CustomConfigSource.json** file again.

Update the **CustomConfigSource** configuration file.


> [File -> Open]guide-microprofile-fallback/start/resources/CustomConfigSource.json



```
{"config_ordinal":500,
"io_openliberty_guides_system_inMaintenance":false}
```
{: codeblock}


Change the **io_openliberty_guides_system_inMaintenance** property from **true** to **false** to set this
condition back to its original value.

After you are finished checking out the application, stop the Open Liberty server by pressing **CTRL+C**
in the command-line session where you ran the server. Alternatively, you can run the **liberty:stop** goal
from the **finish** directory in another shell session:

```
mvn liberty:stop
```
{: codeblock}



# Enabling fault tolerance

Navigate to the **start** directory to begin.

When you run Open Liberty in development mode, known as dev mode, the server listens for file changes and automatically recompiles and 
deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

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

The MicroProfile Fault Tolerance API is included in the MicroProfile dependency that is specified in your **pom.xml** file.
Look for the dependency with the **microprofile** artifact ID.
This dependency provides a library that allows you to use fault tolerance policies in your microservices.

You can also find the **mpFaultTolerance** feature in your **src/main/liberty/config/server.xml** server configuration,
which turns on MicroProfile Fault Tolerance capabilities in Open Liberty.

To easily work through this guide, the two provided microservices are set up to run
on the same server. To simulate the availability of the services and then to enable fault tolerance,
dynamic configuration with MicroProfile Configuration is used so that you can easily take one service
or the other down for maintenance. If you want to learn more about setting up dynamic configuration,
see [Configuring microservices](https://openliberty.io/guides/microprofileconfig.html).

The following two steps set up the dynamic configuration on the **system** service and its client.
You can move on to the next section, which adds the fallback mechanism on the **inventory** service.

First, the **src/main/java/io/openliberty/guides/system/SystemResource.java** file has
the **isInMaintenance()** condition, which determines that the system properties are returned only if you set the **io_openliberty_guides_system_inMaintenance** configuration property
to **false** in the **CustomConfigSource** file.
Otherwise, the service returns a **Status.SERVICE_UNAVAILABLE** message, which makes it unavailable.

Next, the **src/main/java/io/openliberty/guides/inventory/client/SystemClient.java** file
makes a request to the **system** service through the MicroProfile Rest Client API.
If you want to learn more about MicroProfile Rest Client,
you can follow the [Consuming RESTful services with template interfaces](https://openliberty.io/guides/microprofilerestclient.html) guide.
The **system** service as described in the **SystemResource.java** file
may return a **Status.SERVICE_UNAVAILABLE** message, which is a 503 status code.
This code indicates that the server being called
is unable to handle the request because of a temporary overload or scheduled maintenance, which would
likely be alleviated after some delay. To simulate that the system is unavailable, an **IOException** is thrown.

The **InventoryManager** class calls the **getProperties()** method
in the **SystemClient.java** class.
You will look into the **InventoryManager** class in more detail in the next section.







### Adding the @Fallback annotation

The **inventory** service is now able to recognize that the **system** service
was taken down for maintenance.
An IOException is thrown to simulate the **system** service is unavailable.
Now, set a fallback method to deal with this failure.


Replace the **InventoryManager** class.


> [File -> Open]guide-microprofile-fallback/start/src/main/java/io/openliberty/guides/inventory/InventoryManager.java



```
package io.openliberty.guides.inventory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Fallback;
import io.openliberty.guides.inventory.model.*;

@ApplicationScoped
public class InventoryManager {

    private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());
    private InventoryUtils invUtils = new InventoryUtils();

    @Fallback(fallbackMethod = "fallbackForGet",
            applyOn = {IOException.class},
            skipOn = {UnknownHostException.class})
    public Properties get(String hostname) throws IOException {
        return invUtils.getProperties(hostname);
    }

    public Properties fallbackForGet(String hostname) {
        Properties properties = findHost(hostname);
        if (properties == null) {
            Properties msgProp = new Properties();
            msgProp.setProperty(hostname,
                    "System is not found in the inventory or system is in maintenance");
            return msgProp;
        }
        return properties;
    }

    public void add(String hostname, Properties systemProps) {
        Properties props = new Properties();

        String osName = systemProps.getProperty("os.name");
        if (osName == null) {
            return;
        }

        props.setProperty("os.name", systemProps.getProperty("os.name"));
        props.setProperty("user.name", systemProps.getProperty("user.name"));

        SystemData system = new SystemData(hostname, props);
        if (!systems.contains(system)) {
            systems.add(system);
        }
    }

    public InventoryList list() {
        return new InventoryList(systems);
    }

    private Properties findHost(String hostname) {
        for (SystemData system : systems) {
            if (system.getHostname().equals(hostname)) {
                return system.getProperties();
            }
        }
        return null;
    }
}
```
{: codeblock}



The **@Fallback** annotation dictates a method to call when the original method encounters a failed execution. In this
example, use the **fallbackForGet()** method.

The **@Fallback** annotation provides two parameters, **applyOn** and **skipOn**, which allow you to configure
which exceptions trigger a fallback and which exceptions do not, respectively.
In this example, the **get()** method throws **IOException** when the system service is unavailable, and throws
**UnknownHostException** when the system service cannot be found on the specified host.
The **fallbackForGet()** method can handle the first case, but not the second.

The **fallbackForGet()** method, which is the designated fallback method for the original
**get()** method, checks to see if the system's properties exist in the inventory.
If the system properties entry is not found in the inventory, the method prints out a warning
message in the browser. Otherwise, this method returns the cached property values from the inventory.

You successfully set up your microservice to have fault tolerance capability.


# Enabling metrics for the fault tolerance methods


MicroProfile Fault Tolerance integrates with MicroProfile Metrics to provide metrics for the annotated fault tolerance methods.
When both the **mpFaultTolerance** and the **mpMetrics** features are included in the **server.xml** configuration file,
the **@Fallback** fault tolerance annotation provides metrics that count the following things: the total number of annotated method invocations, the total number of failed annotated method invocations, and the total number of the fallback method calls.

The **mpMetrics** feature requires SSL and the configuration is provided for you. The **quickStartSecurity** and **keyStore** configuration elements provide basic security to secure the
server. When you go to the **/metrics** endpoint, use the credentials that are defined in the server configuration to log in to view the data for the fault tolerance methods.

You can learn more about MicroProfile Metrics in the [Providing metrics from a microservice](https://openliberty.io/guides/microprofilemetrics.html) guide. You can also learn more about the MicroProfile Fault Tolerance and MicroProfile Metrics integration in the https://github.com/eclipse/microprofilefaulttolerance/releases[MicroProfile Fault Tolerance specification^].


# Running the application

You started the Open Liberty server in dev mode at the beginning of the guide, so all the changes were automatically picked up.

When the server is running, point your browser to the
[http://localhost:9080/inventory/systems/localhost](http://localhost:9080/inventory/systems/localhost) URL

```
curl http://localhost:9080/inventory/systems/localhost
```
{: codeblock}


You receive the system properties of your local JVM from the **inventory** service. Next, point your
browser to the **system** service URL, which is located at 
```
curl http://localhost:9080/system/properties
```
{: codeblock}


,
to retrieve the system properties for the specific localhost.
Notice that the results from the two URLs are identical because the **inventory** service gets its results from
calling the **system** service.

To see the application metrics, go to the https://localhost:9443/metrics/application[https://localhost:9443/metrics/application^] URL
```
curl https://localhost:9443/metrics/application
```
{: codeblock}


See the following sample outputs for the **@Fallback** annotated method and the fallback method before a fallback occurs:

```
# TYPE application:ft_io_openliberty_guides_inventory_inventory_manager_get_invocations_total counter
application:ft_io_openliberty_guides_inventory_inventory_manager_get_invocations_total 1
# TYPE application:ft_io_openliberty_guides_inventory_inventory_manager_get_invocations_failed_total counter
application:ft_io_openliberty_guides_inventory_inventory_manager_get_invocations_failed_total 0
# TYPE application:ft_io_openliberty_guides_inventory_inventory_manager_get_fallback_calls_total counter
application:ft_io_openliberty_guides_inventory_inventory_manager_get_fallback_calls_total 0
```

You can test the fault tolerance mechanism of your microservices by dynamically changing
the **io_openliberty_guides_system_inMaintenance** property value to **true** in the
**resources/CustomConfigSource.json** file, which turns the **system** service in maintenance.

Update the configuration file.


> [File -> Open]guide-microprofile-fallback/start/resources/CustomConfigSource.json



```
{"config_ordinal":500,
"io_openliberty_guides_system_inMaintenance":false}
```
{: codeblock}


Change the **io_openliberty_guides_system_inMaintenance** property from **false** to **true** and save the file.



After saving the file, go back to your browser and
refresh to the 
```
curl http://localhost:9080/inventory/systems/localhost
```
{: codeblock}


 URL to view the cached version of
the properties. The **fallbackForGet()** method, which is the designated fallback method, is called
when the **system** service is not available.
The cached system properties contain only the OS name and user name key and value pairs.

To see that the **system** service is down, point your browser to the [http://localhost:9080/system/properties](http://localhost:9080/system/properties) URL again

```
curl http://localhost:9080/system/properties
```
{: codeblock}


You see that the service displays a 503 HTTP response code.

Go to the https://localhost:9443/metrics/application[https://localhost:9443/metrics/application^] URL again

```
curl https://localhost:9443/metrics/application
```
{: codeblock}


See the following sample outputs for the **@Fallback** annotated method and the fallback method after a fallback occurs:

```
# TYPE application:ft_io_openliberty_guides_inventory_inventory_manager_get_invocations_total counter
application:ft_io_openliberty_guides_inventory_inventory_manager_get_invocations_total 2
# TYPE application:ft_io_openliberty_guides_inventory_inventory_manager_get_invocations_failed_total counter
application:ft_io_openliberty_guides_inventory_inventory_manager_get_invocations_failed_total 0
# TYPE application:ft_io_openliberty_guides_inventory_inventory_manager_get_fallback_calls_total counter
application:ft_io_openliberty_guides_inventory_inventory_manager_get_fallback_calls_total 1
```

From the output, the **ft_io_openliberty_guides_inventory_inventory_manager_get_invocations_total**
data indicates that the **get()** was called twice including the previous call before turning the **system** service in maintenance.
The **ft_io_openliberty_guides_inventory_inventory_manager_get_fallback_calls_total** data
indicates that the **fallbackForGet()** method was called once.

Update the configuration file.


> [File -> Open]guide-microprofile-fallback/start/resources/CustomConfigSource.json



```
{"config_ordinal":500,
"io_openliberty_guides_system_inMaintenance":false}
```
{: codeblock}


After you finish, change the **io_openliberty_guides_system_inMaintenance**
property value back to **false** in the **resources/CustomConfigSource.json** file.



# Testing the application

You can test your application manually, but automated tests ensure code quality because they trigger a failure
whenever a code change introduces a defect. JUnit and the JAX-RS Client API provide a simple environment for you to write tests.

Create the **FaultToleranceIT** class.


> [File -> Open]guide-microprofile-fallback/start/src/test/java/it/io/openliberty/guides/faulttolerance/FaultToleranceIT.java



```
package it.io.openliberty.guides.faulttolerance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.io.openliberty.guides.utils.TestUtils;

public class FaultToleranceIT {

    private Response response;
    private Client client;

    @BeforeEach
    public void setup() {
        client = ClientBuilder.newClient();
        client.register(JsrJsonpProvider.class);
    }

    @AfterEach
    public void teardown() {
        client.close();
        response.close();
    }
     * testFallbackForGet - test for checking if the fallback is being called
     * correctly 1. Return system properties for a hostname when inventory
     * service is available. 2. Make System service down and get the system
     * properties from inventory service when it is down. 3. Check if system
     * properties for the specific host was returned when the inventory service
     * was down by: Asserting if the total number of the system properties, when
     * service is up, is greater than the total number of the system properties
     * when service is down.
     */

    @Test
    public void testFallbackForGet() throws InterruptedException {
        response = TestUtils.getResponse(client,
                                         TestUtils.INVENTORY_LOCALHOST_URL);
        assertResponse(TestUtils.baseUrl, response);
        JsonObject obj = response.readEntity(JsonObject.class);
        int propertiesSize = obj.size();
        TestUtils.changeSystemProperty(TestUtils.SYSTEM_MAINTENANCE_FALSE,
                                       TestUtils.SYSTEM_MAINTENANCE_TRUE);
        Thread.sleep(3000);
        response = TestUtils.getResponse(client,
                                         TestUtils.INVENTORY_LOCALHOST_URL);
        assertResponse(TestUtils.baseUrl, response);
        obj = response.readEntity(JsonObject.class);
        int propertiesSizeFallBack = obj.size();
        assertTrue(propertiesSize > propertiesSizeFallBack, 
                   "The total number of properties from the @Fallback method "
                 + "is not smaller than the number from the system service" 
                 +  "as expected.");
        TestUtils.changeSystemProperty(TestUtils.SYSTEM_MAINTENANCE_TRUE,
                                       TestUtils.SYSTEM_MAINTENANCE_FALSE);
        Thread.sleep(3000);
    }

     * testFallbackForGet - test for checking if the fallback skip mechanism is working as intended:
     * 1. Access system properties for the wrong hostname (localhot)
     * 2. Verify that the response code is 404
     * 3. Verify that the response text contains an error
     */
    @Test
    public void testFallbackSkipForGet() {
        response = TestUtils.getResponse(client,
                TestUtils.INVENTORY_UNKNOWN_HOST_URL);
        assertResponse(TestUtils.baseUrl, response, 404);
        assertTrue(response.readEntity(String.class).contains("error"),
                   "Incorrect response body from " + TestUtils.INVENTORY_UNKNOWN_HOST_URL);
    }

     * Asserts that the given URL's response code matches the given status code.
     */
    private void assertResponse(String url, Response response, int status_code) {
        assertEquals(status_code, response.getStatus(),
                "Incorrect response code from " + url);
    }

     * Asserts that the given URL has the correct response code of 200.
     */
    private void assertResponse(String url, Response response) {
        assertResponse(url, response, 200);
    }
}
```
{: codeblock}


The **@BeforeEach** and **@AfterEach** annotations indicate that this method runs either
before or after the other test case.
These methods are generally used to perform any setup and teardown tasks. In this case,
the setup method creates a JAX-RS client, which makes HTTP requests to the **inventory** service. This
client must also be registered with a JSON-P provider to process JSON resources.
The teardown method simply destroys this client instance as well as the HTTP responses.

The **testFallbackForGet()** test case sends a request to the **inventory** service to get the systems properties for a
hostname before and after the **system** service becomes unavailable. Then, it asserts outputs from the two requests
to ensure that they are different from each other.

The **testFallbackSkipForGet()** test case sends a request to the **inventory** service to
get the system properties for an incorrect hostname (**unknown**). Then, it confirms that the fallback method has not been
called by asserting that the response's status code is **404** with an error message in the response body.

The **@Test** annotations indicate that the methods automatically execute when your test class runs.

In addition, a few endpoint tests have been included for you to test the basic functionality of the
**inventory** and **system** services. If a test failure occurs, then you might have introduced a bug into
the code.


### Running the tests

Because you started Open Liberty in dev mode, press the **enter/return** key to run the tests.

If the tests pass, you see a similar output to the following example:
```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.faulttolerance.FaultToleranceIT
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.517 sec - in it.io.openliberty.guides.faulttolerance.FaultToleranceIT
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.937 sec - in it.io.openliberty.guides.system.SystemEndpointIT
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.396 sec - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results :

Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

To see if the tests detect a failure, comment out the **changeSystemProperty()** methods
in the **FaultToleranceIT.java** file. Rerun the tests to see that a test failure occurs for the
**testFallbackForGet()** and **testFallbackSkipForGet()** test cases.

When you are done checking out the service, exit dev mode by pressing **CTRL+C** in the command-line session
where you ran the server, or by typing **q** and then pressing the **enter/return** key.



# Summary

## Clean up your environment

Delete the **guide-microprofile-fallback** project by navigating to the **/home/project/** directory

```
cd ../..
rm -r -f guide-microprofile-fallback
rmdir guide-microprofile-fallback
```
{: codeblock}


## Great work! You're done!


You just learned how to build a fallback mechanism for a microservice with MicroProfile Fault Tolerance in Open Liberty and wrote a test to validate it.

You can try one of the related MicroProfile guides. They demonstrate technologies that you can
learn and expand on what you built here.


