
# Injecting dependencies into microservices


Learn how to use Contexts and Dependency Injection (CDI) to manage scopes and inject dependencies into microservices.

## What you'll learn

You will learn how to use Contexts and Dependency Injection (CDI) to manage scopes and inject dependencies in a simple inventory management application.

The application that you will be working with is an **inventory** service,
which stores the information about various JVMs that run on different systems.
Whenever a request is made to the **inventory** service to retrieve the JVM
system properties of a particular host, the **inventory** service communicates with the **system**
service on that host to get these system properties. The system properties are then stored and returned.

You will use scopes to bind objects in this application to their well-defined contexts.
CDI provides a variety of scopes for you to work with and while you will not use all of them in this guide,
there is one for almost every scenario that you may encounter. Scopes are defined by using CDI annotations.
You will also use dependency injection to inject one bean into another to make use of its functionalities.
This enables you to inject the bean in its specified context without having to instantiate it yourself.

The implementation of the application and its services are provided for you in the **start/src** directory.
The **system** service can be found in the **start/src/main/java/io/openliberty/guides/system** directory, 
and the **inventory** service can be found in the **start/src/main/java/io/openliberty/guides/inventory** directory. 
If you want to learn more about RESTful web services and how to build them, see
[Creating a RESTful web service](https://openliberty.io/guides/restintro.html) for details about how to build the **system** service.
The **inventory** service is built in a similar way.

### What is CDI?

Contexts and Dependency Injection (CDI) defines a rich set of complementary services that improve the application structure.
The most fundamental services that are provided by CDI are contexts that bind the lifecycle of stateful components to well-defined contexts,
and dependency injection that is the ability to inject components into an application in a typesafe way.
With CDI, the container does all the daunting work of instantiating dependencies, and
controlling exactly when and how these components are instantiated and destroyed.



# Getting started

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guidecdiintro.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-cdi-intro.git
cd guide-cdi-intro
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

Point your browser to the [http://localhost:9080/inventory/systems](http://localhost:9080/inventory/systems) URL This is the starting point of the **inventory**

```
curl http://localhost:9080/inventory/systems
```
{: codeblock}


service and it displays the current contents of the inventory. As you might expect, these are empty since
nothing is stored in the inventory yet
```
curl http://localhost:9080/inventory/systems/localhost
```
{: codeblock}


You see a result in JSON format with the system properties of your local JVM. When you visit this URL, these system
properties are automatically stored in the inventory Go back to [http://localhost:9080/inventory/systems](http://localhost:9080/inventory/systems) and

```
curl http://localhost:9080/inventory/systems
```
{: codeblock}


you see a new entry for **localhost**. For simplicity, only the OS name and username are shown here for
each host. You can repeat this process for your own hostname or any other machine that is running
the **system** service.

After you are finished checking out the application, stop the Open Liberty server by pressing **CTRL+C**
in the command-line session where you ran the server. Alternatively, you can run the **liberty:stop** goal
from the **finish** directory in another shell session:

```
mvn liberty:stop
```
{: codeblock}


# Handling dependencies in the application

You will use CDI to inject dependencies into the inventory manager application and learn how to manage the life cycles of your objects.

### Managing scopes and contexts

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

Create the **InventoryManager** class.


> [File -> Open]guide-cdi-intro/start/src/main/java/io/openliberty/guides/inventory/InventoryManager.java



```
package io.openliberty.guides.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InventoryManager {

  private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());

  public void add(String hostname, Properties systemProps) {
    Properties props = new Properties();
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
}
```
{: codeblock}



This bean contains two simple functions. 
The **add()** function is for adding entries to the inventory.
The **list()** function is for listing all the entries currently stored in the inventory.

This bean must be persistent between all of the clients, which means multiple clients need to share the same instance.
To achieve this by using CDI, you can simply add the **@ApplicationScoped** annotation onto the class.

This annotation indicates that this particular bean is to be initialized once per application.
By making it application-scoped, the container ensures that the same instance of the bean is used whenever
it is injected into the application.

Create the **InventoryResource** class.


> [File -> Open]guide-cdi-intro/start/src/main/java/io/openliberty/guides/inventory/InventoryResource.java



```
package io.openliberty.guides.inventory;

import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.client.SystemClient;

@ApplicationScoped
@Path("/systems")
public class InventoryResource {

  @Inject
  InventoryManager manager;

  @Inject
  SystemClient systemClient;

  @GET
  @Path("/{hostname}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPropertiesForHost(@PathParam("hostname") String hostname) {
    Properties props = systemClient.getProperties(hostname);
    if (props == null) {
      return Response.status(Response.Status.NOT_FOUND)
                     .entity("{ \"error\" : \"Unknown hostname " + hostname
                             + " or the inventory service may not be running "
                             + "on the host machine \" }")
                     .build();
    }

    manager.add(hostname, props);
    return Response.ok(props).build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public InventoryList listContents() {
    return manager.list();
  }
}
```
{: codeblock}



The inventory resource is a RESTful service that is served at the **inventory/systems** endpoint. 

Annotating a class with the **@ApplicationScoped** annotation indicates that the bean is initialized once and is shared between all requests while the application runs.

If you want this bean to be initialized once for every request, you can annotate the class with the **@RequestScoped** annotation instead. With the **@RequestScoped** annotation, the bean is instantiated when the request is received and destroyed when a response is sent back to the client. A request scope is short-lived.

### Injecting a dependency

Refer to the **InventoryResource** class you created above.

The **@Inject** annotation indicates a dependency injection.
You are injecting your **InventoryManager** and **SystemClient** beans into the **InventoryResource** class.
This injects the beans in their specified context and makes all of their functionalities
available without the need of instantiating them yourself.
The injected bean **InventoryManager** can then be invoked directly through the **manager.add(hostname, props)**
and **manager.list()** function calls.  The injected bean **SystemClient** can be invoked through the 
**systemClient.getProperties(hostname)** function call.

Finally, you have a client component **SystemClient** that can be found in the
**src/main/java/io/openliberty/guides/inventory/client** directory. This class communicates 
with the **system** service to retrieve the JVM system properties for a particular host 
that exposes them. This class also contains detailed Javadocs that you can read for reference.

Your inventory application is now completed.




# Running the application

You started the Open Liberty server in dev mode at the beginning of the guide, so all the changes were automatically picked up.

You can find the **inventory** and **system** services at the following URLs:

 
```
curl http://localhost:9080/inventory/systems
```
{: codeblock}



 
```
curl http://localhost:9080/system/properties
```
{: codeblock}





# Testing the inventory application

While you can test your application manually, you should rely on automated tests since they trigger
a failure whenever a code change introduces a defect.
Since the application is a RESTful web service application, you can use
JUnit and the RESTful web service Client API to write tests.
In testing the functionality of the application, the scopes and dependencies are being tested.

Create the **InventoryEndpointIT** class.


> [File -> Open]guide-cdi-intro/start/src/test/java/it/io/openliberty/guides/inventory/InventoryEndpointIT.java



```
package it.io.openliberty.guides.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class InventoryEndpointIT {

  private static String port;
  private static String baseUrl;

  private Client client;

  private final String SYSTEM_PROPERTIES = "system/properties";
  private final String INVENTORY_SYSTEMS = "inventory/systems";

  @BeforeAll
  public static void oneTimeSetup() {
    port = System.getProperty("http.port");
    baseUrl = "http://localhost:" + port + "/";
  }

  @BeforeEach
  public void setup() {
    client = ClientBuilder.newClient();
    client.register(JsrJsonpProvider.class);
  }

  @AfterEach
  public void teardown() {
    client.close();
  }

  @Test
  @Order(1)
  public void testHostRegistration() {
    this.visitLocalhost();

    Response response = this.getResponse(baseUrl + INVENTORY_SYSTEMS);
    this.assertResponse(baseUrl, response);

    JsonObject obj = response.readEntity(JsonObject.class);

    JsonArray systems = obj.getJsonArray("systems");

    boolean localhostExists = false;
    for (int n = 0; n < systems.size(); n++) {
      localhostExists = systems.getJsonObject(n)
                                .get("hostname").toString()
                                .contains("localhost");
      if (localhostExists) {
          break;
      }
    }
    assertTrue(localhostExists, 
              "A host was registered, but it was not localhost");

    response.close();
  }

  @Test
  @Order(2)
  public void testSystemPropertiesMatch() {
    Response invResponse = this.getResponse(baseUrl + INVENTORY_SYSTEMS);
    Response sysResponse = this.getResponse(baseUrl + SYSTEM_PROPERTIES);

    this.assertResponse(baseUrl, invResponse);
    this.assertResponse(baseUrl, sysResponse);

    JsonObject jsonFromInventory = (JsonObject) invResponse.readEntity(JsonObject.class)
                                                           .getJsonArray("systems")
                                                           .getJsonObject(0)
                                                           .get("properties");

    JsonObject jsonFromSystem = sysResponse.readEntity(JsonObject.class);

    String osNameFromInventory = jsonFromInventory.getString("os.name");
    String osNameFromSystem = jsonFromSystem.getString("os.name");
    this.assertProperty("os.name", "localhost", osNameFromSystem,
                        osNameFromInventory);

    String userNameFromInventory = jsonFromInventory.getString("user.name");
    String userNameFromSystem = jsonFromSystem.getString("user.name");
    this.assertProperty("user.name", "localhost", userNameFromSystem,
                        userNameFromInventory);

    invResponse.close();
    sysResponse.close();
  }

  @Test
  @Order(3)
  public void testUnknownHost() {
    Response response = this.getResponse(baseUrl + INVENTORY_SYSTEMS);
    this.assertResponse(baseUrl, response);

    Response badResponse = client.target(baseUrl + INVENTORY_SYSTEMS + "/"
        + "badhostname").request(MediaType.APPLICATION_JSON).get();

    assertEquals(404, badResponse.getStatus(),
        "BadResponse expected status: 404. Response code not as expected.");

    String obj = badResponse.readEntity(String.class);

    boolean isError = obj.contains("error");
    assertTrue(isError, 
              "badhostname is not a valid host but it didn't raise an error");

    response.close();
    badResponse.close();
  }

  private Response getResponse(String url) {
    return client.target(url).request().get();
  }

  private void assertResponse(String url, Response response) {
    assertEquals(200, response.getStatus(), "Incorrect response code from " + url);
  }

  private void assertProperty(String propertyName, String hostname,
      String expected, String actual) {
    assertEquals(expected, actual, "JVM system property [" + propertyName + "] "
        + "in the system service does not match the one stored in "
        + "the inventory service for " + hostname);
  }

  private void visitLocalhost() {
    Response response = this.getResponse(baseUrl + SYSTEM_PROPERTIES);
    this.assertResponse(baseUrl, response);
    response.close();

    Response targetResponse = client.target(baseUrl + INVENTORY_SYSTEMS
        + "/localhost").request().get();
    targetResponse.close();
  }
}
```
{: codeblock}


The **@BeforeAll** annotation is placed on a method that runs before any of the test cases.
In this case, the **oneTimeSetup()** method retrieves the port number for the Open Liberty server and builds
a base URL string that is used throughout the tests.

The **@BeforeEach** and **@AfterEach** annotations are placed on methods that run before and after every test case.
These methods are generally used to perform any setup and teardown tasks. In this case, the **setup()** method
creates a JAX-RS client, which makes HTTP requests to the **inventory** service. This client must
also be registered with a JSON-P provider (**JsrJsonpProvider**) to process JSON resources. The **teardown()**
method simply destroys this client instance.

See the following descriptions of the test cases:

* **testHostRegistration()** verifies that a host is correctly added to the inventory.

* **testSystemPropertiesMatch()** verifies that the JVM system properties returned by the **system** service match
the ones stored in the **inventory** service.

* **testUnknownHost()** verifies that an unknown host or a host that does not expose their JVM system
properties is correctly handled as an error.

To force these test cases to run in a particular order, annotate your **InventoryEndpointIT** test class with the **@TestMethodOrder(OrderAnnotation.class)** annotation.
**OrderAnnotation.class** runs test methods in numerical order, 
according to the values specified in the **@Order** annotation. 
You can also create a custom **MethodOrderer** class or use built-in **MethodOrderer** implementations, 
such as **OrderAnnotation.class**, **Alphanumeric.class**, or **Random.class**. Label your test cases
with the **@Test** annotation so that they automatically run when your test class runs.

Finally, the **src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java** file
is included for you to test the basic functionality of the **system** service.
If a test failure occurs, then you might have introduced a bug into the code.




### Running the tests

Because you started Open Liberty in dev mode, press the **enter/return** key to run the tests.

If the tests pass, you see a similar output to the following example:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.99 sec - in it.io.openliberty.guides.system.SystemEndpointIT
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
[WARNING ] Interceptor for {http://badhostname:9080/system/properties}WebClient has thrown exception, unwinding now
Could not send Message.
[err] Runtime exception: java.net.UnknownHostException: UnknownHostException invoking http://badhostname:9080/system/properties: badhostname
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.325 sec - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results :

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

The warning and error messages are expected and result from a request to a bad or an unknown hostname. This request is made in the **testUnknownHost()** test from the **InventoryEndpointIT** integration test.

To see whether the tests detect a failure, change the **endpoint** for the **inventory** service in
the **src/main/java/io/openliberty/guides/inventory/InventoryResource.java** file to something else. Then,
run the tests again to see that a test failure occurs.


When you are done checking out the service, exit dev mode by pressing **CTRL+C** in the command-line session
where you ran the server, or by typing **q** and then pressing the **enter/return** key.


# Summary

## Clean up your environment

Delete the **guide-cdi-intro** project by navigating to the **/home/project/** directory

```
cd ../..
rm -r -f guide-cdi-intro
rmdir guide-cdi-intro
```
{: codeblock}


## Great work! You're done!


You just used CDI services in Open Liberty to build a simple inventory application.

