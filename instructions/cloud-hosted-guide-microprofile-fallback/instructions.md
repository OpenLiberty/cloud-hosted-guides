HELLLOO
---
markdown-version: v1
title: instructions
branch: lab-482-instruction
version-history-start-date: 2020-05-26 16:16:02 UTC
tool-type: theia
---
::page{title="Welcome to the Building fault-tolerant microservices with the @Fallback annotation guide!"}

You'll explore how to manage the impact of failures using MicroProfile Fault Tolerance by adding fallback behavior to microservice dependencies.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You will learn how to use MicroProfile (MP) Fault Tolerance to build resilient microservices that reduce the impact from failure and ensure continued operation of services.

MP Fault Tolerance provides a simple and flexible solution to build fault-tolerant microservices. Fault tolerance leverages different strategies to guide the execution and result of logic. As stated in the [MicroProfile website](https://microprofile.io/project/eclipse/microprofile-fault-tolerance), retry policies, bulkheads, and circuit breakers are popular concepts in this area. They dictate whether and when executions take place, and fallbacks offer an alternative result when an execution does not complete successfully.

The application that you will be working with is an ***inventory*** service, which collects, stores, and returns the system properties. It uses the ***system*** service to retrieve the system properties for a particular host. You will add fault tolerance to the ***inventory*** service so that it reacts accordingly when the ***system*** service is unavailable.

You will use the ***@Fallback*** annotations from the MicroProfile Fault Tolerance specification to define criteria for when to provide an alternative solution for a failed execution.

You will also see the application metrics for the fault tolerance methods that are automatically enabled when you add the MicroProfile Metrics feature to the server.



::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-fallback.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microprofile-fallback.git
cd guide-microprofile-fallback
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

After you see the following message, your application server is ready:

```
The defaultServer server is ready to run a smarter planet.
```


Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE. To access the ***inventory*** service with a localhost hostname, run the following curl command:
```bash
curl -s http://localhost:9080/inventory/systems/localhost | jq
```

You see the system properties for this host. When you run this curl command, some of these system properties, such as the OS name and user name, are automatically stored in the inventory.


Update the ***CustomConfigSource*** configuration file. Change the ***io_openliberty_guides_system_inMaintenance*** property from ***false*** to ***true*** and save the file.

> To open the CustomConfigSource.json file in your IDE, select 
> **File** > **Open** > guide-microprofile-fallback/finish/resources/CustomConfigSource.json, or click the following button

::openFile{path="/home/project/guide-microprofile-fallback/finish/resources/CustomConfigSource.json"}

```
{"config_ordinal":500,
"io_openliberty_guides_system_inMaintenance":true}
```


You do not need to restart the server. Next, run the following curl command:
```bash
curl -s http://localhost:9080/inventory/systems/localhost | jq
```

The fallback mechanism is triggered because the **system** service is now in maintenance. You see the cached properties for this localhost.

When you are done checking out the application, go to the ***CustomConfigSource.json*** file again.


Update the ***CustomConfigSource*** configuration file. Change the ***io_openliberty_guides_system_inMaintenance*** property from ***true*** to ***false*** to set this condition back to its original value.

> To open the CustomConfigSource.json file in your IDE, select 
> **File** > **Open** > guide-microprofile-fallback/finish/resources/CustomConfigSource.json, or click the following button

::openFile{path="/home/project/guide-microprofile-fallback/finish/resources/CustomConfigSource.json"}

```
{"config_ordinal":500,
"io_openliberty_guides_system_inMaintenance":false}
```

After you are finished checking out the application, stop the Open Liberty server by pressing `Ctrl+C` in the command-line session where you ran the server. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```


::page{title="Enabling fault tolerance"}


To begin, run the following command to navigate to the ***start*** directory:
```bash
cd /home/project/guide-microprofile-fallback/start
```

When you run Open Liberty in development mode, known as dev mode, the server listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```bash
mvn liberty:dev
```

After you see the following message, your application server in dev mode is ready:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

The MicroProfile Fault Tolerance API is included in the MicroProfile dependency that is specified in your ***pom.xml*** file. Look for the dependency with the ***microprofile*** artifact ID. This dependency provides a library that allows you to use fault tolerance policies in your microservices.

You can also find the ***mpFaultTolerance*** feature in your ***src/main/liberty/config/server.xml*** server configuration, which turns on MicroProfile Fault Tolerance capabilities in Open Liberty.

To easily work through this guide, the two provided microservices are set up to run on the same server. To simulate the availability of the services and then to enable fault tolerance, dynamic configuration with MicroProfile Configuration is used so that you can easily take one service or the other down for maintenance. If you want to learn more about setting up dynamic configuration, see [Configuring microservices](https://openliberty.io/guides/microprofile-config.html).

The following two steps set up the dynamic configuration on the ***system*** service and its client. You can move on to the next section, which adds the fallback mechanism on the ***inventory*** service.

First, the ***src/main/java/io/openliberty/guides/system/SystemResource.java*** file has the ***isInMaintenance()*** condition, which determines that the system properties are returned only if you set the ***io_openliberty_guides_system_inMaintenance*** configuration property to ***false*** in the ***CustomConfigSource*** file. Otherwise, the service returns a ***Status.SERVICE_UNAVAILABLE*** message, which makes it unavailable.

Next, the ***src/main/java/io/openliberty/guides/inventory/client/SystemClient.java*** file makes a request to the ***system*** service through the MicroProfile Rest Client API. If you want to learn more about MicroProfile Rest Client, you can follow the [Consuming RESTful services with template interfaces](https://openliberty.io/guides/microprofile-rest-client.html) guide. The ***system*** service as described in the ***SystemResource.java*** file may return a ***Status.SERVICE_UNAVAILABLE*** message, which is a 503 status code. This code indicates that the server being called is unable to handle the request because of a temporary overload or scheduled maintenance, which would likely be alleviated after some delay. To simulate that the system is unavailable, an ***IOException*** is thrown.

The ***InventoryManager*** class calls the ***getProperties()*** method in the ***SystemClient.java*** class. You will look into the ***InventoryManager*** class in more detail in the next section.







### Adding the @Fallback annotation

The ***inventory*** service is now able to recognize that the ***system*** service was taken down for maintenance. An IOException is thrown to simulate the ***system*** service is unavailable. Now, set a fallback method to deal with this failure.


Replace the ***InventoryManager*** class.

> To open the InventoryManager.java file in your IDE, select
> **File** > **Open** > guide-microprofile-fallback/start/src/main/java/io/openliberty/guides/inventory/InventoryManager.java, or click the following button

::openFile{path="/home/project/guide-microprofile-fallback/start/src/main/java/io/openliberty/guides/inventory/InventoryManager.java"}



```java
package io.openliberty.guides.inventory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Fallback;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;

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


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


The ***@Fallback*** annotation dictates a method to call when the original method encounters a failed execution. In this example, use the ***fallbackForGet()*** method.

The ***@Fallback*** annotation provides two parameters, ***applyOn*** and ***skipOn***, which allow you to configure which exceptions trigger a fallback and which exceptions do not, respectively. In this example, the ***get()*** method throws ***IOException*** when the system service is unavailable, and throws ***UnknownHostException*** when the system service cannot be found on the specified host. The ***fallbackForGet()*** method can handle the first case, but not the second.

The ***fallbackForGet()*** method, which is the designated fallback method for the original ***get()*** method, checks to see if the system's properties exist in the inventory. If the system properties entry is not found in the inventory, the method prints out a warning message in the browser. Otherwise, this method returns the cached property values from the inventory.

You successfully set up your microservice to have fault tolerance capability.


::page{title="Enabling metrics for the fault tolerance methods"}


MicroProfile Fault Tolerance integrates with MicroProfile Metrics to provide metrics for the annotated fault tolerance methods. When both the ***mpFaultTolerance*** and the ***mpMetrics*** features are included in the ***server.xml*** configuration file, the ***@Fallback*** fault tolerance annotation provides metrics that count the following things: the total number of annotated method invocations, the total number of failed annotated method invocations, and the total number of the fallback method calls.

The ***mpMetrics*** feature requires SSL and the configuration is provided for you. The ***quickStartSecurity*** configuration element provides basic security to secure the server. When you go to the ***/metrics*** endpoint, use the credentials that are defined in the server configuration to log in to view the data for the fault tolerance methods.

You can learn more about MicroProfile Metrics in the [Providing metrics from a microservice](https://openliberty.io/guides/microprofile-metrics.html) guide. You can also learn more about the MicroProfile Fault Tolerance and MicroProfile Metrics integration in the [MicroProfile Fault Tolerance specification](https://github.com/eclipse/microprofile-fault-tolerance/releases).


::page{title="Running the application"}

You started the Open Liberty server in dev mode at the beginning of the guide, so all the changes were automatically picked up.


When the server is running, run the following curl command:
```bash
curl -s http://localhost:9080/inventory/systems/localhost | jq
```

You receive the system properties of your local JVM from the **inventory** service.

Next, run the following curl command which accesses the **system** service, to retrieve the system properties for the specific localhost:
```bash
curl -s http://localhost:9080/system/properties | jq
```

Notice that the results from the two URLs are identical because the **inventory** service gets its results from calling the **system** service.

To see the application metrics, run the following curl commmand. This command will Log in using **admin** user, and you will have to enter **adminpwd** as the password.
```bash
curl -k -u admin https://localhost:9443/metrics/base | grep _ft_
```

See the following sample outputs for the **@Fallback** annotated method and the fallback method before a fallback occurs:

```
# TYPE base_ft_invocations_total counter
base_ft_invocations_total{fallback="notApplied",method="io.openliberty.guides.inventory.InventoryManager.get",result="valueReturned"} 1
base_ft_invocations_total{fallback="applied",method="io.openliberty.guides.inventory.InventoryManager.get",result="valueReturned"} 0
base_ft_invocations_total{fallback="notApplied",method="io.openliberty.guides.inventory.InventoryManager.get",result="exceptionThrown"} 0
base_ft_invocations_total{fallback="applied",method="io.openliberty.guides.inventory.InventoryManager.get",result="exceptionThrown"} 0
```

You can test the fault tolerance mechanism of your microservices by dynamically changing the ***io_openliberty_guides_system_inMaintenance*** property value to ***true*** in the ***resources/CustomConfigSource.json*** file, which puts the ***system*** service in maintenance.


Update the configuration file. Change the ***io_openliberty_guides_system_inMaintenance*** property from ***false*** to ***true*** and save the file.

> To open the CustomConfigSource.json file in your IDE, select 
> **File** > **Open** > guide-microprofile-fallback/start/resources/CustomConfigSource.json, or click the following button

::openFile{path="/home/project/guide-microprofile-fallback/start/resources/CustomConfigSource.json"}

```
{"config_ordinal":500,
"io_openliberty_guides_system_inMaintenance":true}
```




After saving the file, run the following curl command to view the cached version of the properties:
```bash
curl -s http://localhost:9080/inventory/systems/localhost | jq
```

The **fallbackForGet()** method, which is the designated fallback method, is called when the **system** service is not available. The cached system properties contain only the OS name and user name key and value pairs.


To see that the ***system*** service is down, run the following curl command:
```bash
curl -I http://localhost:9080/system/properties
```

You see that the service displays a 503 HTTP response code.


Run the following curl command again and enter ***adminpwd*** as the password:
```bash
curl -k -u admin https://localhost:9443/metrics/base | grep _ft_
```

See the following sample outputs for the ***@Fallback*** annotated method and the fallback method after a fallback occurs:

```
# TYPE base_ft_invocations_total counter
base_ft_invocations_total{fallback="notApplied",method="io.openliberty.guides.inventory.InventoryManager.get",result="valueReturned"} 1
base_ft_invocations_total{fallback="applied",method="io.openliberty.guides.inventory.InventoryManager.get",result="valueReturned"} 1
base_ft_invocations_total{fallback="notApplied",method="io.openliberty.guides.inventory.InventoryManager.get",result="exceptionThrown"} 0
base_ft_invocations_total{fallback="applied",method="io.openliberty.guides.inventory.InventoryManager.get",result="exceptionThrown"} 0
```


From the output, the ***base_ft_invocations_total{fallback="notApplied",*** ***method="io.openliberty.guides.inventory.InventoryManager.get",*** ***result="valueReturned"}*** data shows that the ***get()*** method was called once without triggering a fallback method. The ***base_ft_invocations_total{fallback="applied",*** ***method="io.openliberty.guides.inventory.InventoryManager.get",*** ***result="valueReturned"}*** data indicates that the ***get()*** method was called once and the fallback ***fallbackForGet()*** method was triggered.


Update the configuration file. After you finish, change the ***io_openliberty_guides_system_inMaintenance*** property value back to ***false*** in the ***resources/CustomConfigSource.json*** file.

> To open the CustomConfigSource.json file in your IDE, select 
> **File** > **Open** > guide-microprofile-fallback/start/resources/CustomConfigSource.json, or click the following button

::openFile{path="/home/project/guide-microprofile-fallback/start/resources/CustomConfigSource.json"}

```
{"config_ordinal":500,
"io_openliberty_guides_system_inMaintenance":false}
```


::page{title="Testing the application"}

You can test your application manually, but automated tests ensure code quality because they trigger a failure whenever a code change introduces a defect. JUnit and the JAX-RS Client API provide a simple environment for you to write tests.

Create the ***FaultToleranceIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-fallback/start/src/test/java/it/io/openliberty/guides/faulttolerance/FaultToleranceIT.java
```


> Then, to open the FaultToleranceIT.java file in your IDE, select
> **File** > **Open** > guide-microprofile-fallback/start/src/test/java/it/io/openliberty/guides/faulttolerance/FaultToleranceIT.java, or click the following button

::openFile{path="/home/project/guide-microprofile-fallback/start/src/test/java/it/io/openliberty/guides/faulttolerance/FaultToleranceIT.java"}



```java
package it.io.openliberty.guides.faulttolerance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
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
    }

    @AfterEach
    public void teardown() {
        client.close();
        response.close();
    }

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

    @Test
    public void testFallbackSkipForGet() {
        response = TestUtils.getResponse(client,
                TestUtils.INVENTORY_UNKNOWN_HOST_URL);
        assertResponse(TestUtils.baseUrl, response, 404);
        assertTrue(response.readEntity(String.class).contains("error"),
                   "Incorrect response body from "
                   + TestUtils.INVENTORY_UNKNOWN_HOST_URL);
    }

    private void assertResponse(String url, Response response, int statusCode) {
        assertEquals(statusCode, response.getStatus(),
                "Incorrect response code from " + url);
    }

    private void assertResponse(String url, Response response) {
        assertResponse(url, response, 200);
    }
}
```



The ***@BeforeEach*** and ***@AfterEach*** annotations indicate that this method runs either before or after the other test case. These methods are generally used to perform any setup and teardown tasks. In this case, the setup method creates a JAX-RS client, which makes HTTP requests to the ***inventory*** service. This client must also be registered with a JSON-P provider to process JSON resources. The teardown method simply destroys this client instance as well as the HTTP responses.

The ***testFallbackForGet()*** test case sends a request to the ***inventory*** service to get the systems properties for a hostname before and after the ***system*** service becomes unavailable. Then, it asserts outputs from the two requests to ensure that they are different from each other.

The ***testFallbackSkipForGet()*** test case sends a request to the ***inventory*** service to get the system properties for an incorrect hostname (***unknown***). Then, it confirms that the fallback method has not been called by asserting that the response's status code is ***404*** with an error message in the response body.

The ***@Test*** annotations indicate that the methods automatically execute when your test class runs.

In addition, a few endpoint tests have been included for you to test the basic functionality of the ***inventory*** and ***system*** services. If a test failure occurs, then you might have introduced a bug into the code.


### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

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

To see if the tests detect a failure, comment out the ***changeSystemProperty()*** methods in the ***FaultToleranceIT.java*** file. Rerun the tests to see that a test failure occurs for the ***testFallbackForGet()*** and ***testFallbackSkipForGet()*** test cases.

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran the server, or by typing ***q*** and then pressing the ***enter/return*** key.


::page{title="Summary"}

### Nice Work!

You just learned how to build a fallback mechanism for a microservice with MicroProfile Fault Tolerance in Open Liberty and wrote a test to validate it.


You can try one of the related MicroProfile guides. They demonstrate technologies that you can learn and expand on what you built here.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-fallback*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-fallback
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Building%20fault-tolerant%20microservices%20with%20the%20@Fallback%20annotation&guide-id=cloud-hosted-guide-microprofile-fallback)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-fallback/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-fallback/pulls)



### Where to next?

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Configuring microservices](https://openliberty.io/guides/microprofile-config.html)
* [Preventing repeated failed calls to microservices](https://openliberty.io/guides/circuit-breaker.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
