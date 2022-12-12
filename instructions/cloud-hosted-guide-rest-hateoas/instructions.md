HELLLOO
---
markdown-version: v1
title: instructions
branch: lab-433-instruction
version-history-start-date: 2021-12-03 21:43:01 UTC
tool-type: theia
---
::page{title="Welcome to the Creating a hypermedia-driven RESTful web service guide!"}

You'll explore how to use Hypermedia As The Engine Of Application State (HATEOAS) to drive your RESTful web service on Open Liberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You will learn how to use hypermedia to create a specific style of a response JSON, which has contents that you can use to navigate your REST service. You'll build on top of a simple inventory REST service that you can develop with MicroProfile technologies. You can find the service at the following URL:

```
http://localhost:9080/inventory/hosts
```

The service responds with a JSON file that contains all of the registered hosts. Each host has a collection of HATEOAS links:

```
{
  "foo": [
    {
      "href": "http://localhost:9080/inventory/hosts/foo",
      "rel": "self"
    }
  ],
  "bar": [
    {
      "href": "http://localhost:9080/inventory/hosts/bar",
      "rel": "self"
    }
  ],
  "*": [
    {
      "href": "http://localhost:9080/inventory/hosts/*",
      "rel": "self"
    }
  ]
}
```

### What is HATEOAS?

HATEOAS is a constraint of REST application architectures. With HATEOAS, the client receives information about the available resources from the REST application. The client does not need to be hardcoded to a fixed set of resources, and the application and client can evolve independently. In other words, the application tells the client where it can go and what it can access by providing it with a simple collection of links to other available resources.

### Response JSON

In the context of HATEOAS, each resource must contain a link reference to itself, which is commonly referred to as ***self***. In this guide, the JSON structure features a mapping between the hostname and its corresponding list of HATEOAS links:

```
  "*": [
    {
      "href": "http://localhost:9080/inventory/hosts/*",
      "rel": "self"
    }
  ]
```

#### Link types

The following example shows two different links. The first link has a ***self*** relationship with the resource object and is generated whenever you register a host. The link points to that host entry in the inventory:

```
  {
    "href": "http://localhost:9080/inventory/hosts/<hostname>",
    "rel": "self"
  }
```

The second link has a ***properties*** relationship with the resource object and is generated if the host ***system*** service is running. The link points to the properties resource on the host:

```
  {
    "href": "http://<hostname>:9080/system/properties",
    "rel": "properties"
  }
```

#### Other formats

Although you should stick to the previous format for the purpose of this guide, another common convention has the link as the value of the relationship:

```
  "_links": {
      "self": "http://localhost:9080/inventory/hosts/<hostname>",
      "properties": "http://<hostname>:9080/system/properties"
  }
```

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-rest-hateoas.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-rest-hateoas.git
cd guide-rest-hateoas
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


After the server runs, you can find your hypermedia-driven ***inventory*** service at the ***/inventory/hosts*** endpoint. Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE. Run the following curl command:
```bash
curl -s http://localhost:9080/inventory/hosts | jq
```

After you are finished checking out the application, stop the Open Liberty server by pressing `Ctrl+C` in the command-line session where you ran the server. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```



::page{title="Creating the response JSON"}

Navigate to the ***start*** directory.
```bash
cd /home/project/guide-rest-hateoas/start
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

Begin by building your response JSON, which is composed of the name of the host machine and its list of HATEOAS links.

### Linking to inventory contents

As mentioned before, your starting point is an existing simple inventory REST service. 

Look at the request handlers in the ***InventoryResource.java*** file.

The ***.../inventory/hosts/*** URL will no longer respond with a JSON representation of your inventory contents, so you can discard the ***listContents*** method and integrate it into the ***getPropertiesForHost*** method.

Replace the ***InventoryResource*** class.

> To open the InventoryResource.java file in your IDE, select
> **File** > **Open** > guide-rest-hateoas/start/src/main/java/io/openliberty/guides/microprofile/InventoryResource.java, or click the following button

::openFile{path="/home/project/guide-rest-hateoas/start/src/main/java/io/openliberty/guides/microprofile/InventoryResource.java"}



```java
package io.openliberty.guides.microprofile;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

@ApplicationScoped
@Path("hosts")
public class InventoryResource {

    @Inject
    InventoryManager manager;

    @Context
    UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject handler() {
        return manager.getSystems(uriInfo.getAbsolutePath().toString());
    }

    @GET
    @Path("{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getPropertiesForHost(@PathParam("hostname") String hostname) {
        return (hostname.equals("*")) ? manager.list() : manager.get(hostname);
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.



The contents of your inventory are now under the asterisk (`*`) wildcard and reside at the `http://localhost:9080/inventory/hosts/*` URL.

The ***GET*** request handler is responsible for handling all ***GET*** requests that are made to the target URL. This method responds with a JSON that contains HATEOAS links.

The ***UriInfo*** object is what will be used to build your HATEOAS links.

The ***@Context*** annotation is a part of CDI and indicates that the ***UriInfo*** will be injected when the resource is instantiated.

Your new ***InventoryResource*** class is now replaced. Next, you will implement the ***getSystems*** method and build the response JSON object.


### Linking to each available resource

Take a look at your ***InventoryManager*** and ***InventoryUtil*** files.

Replace the ***InventoryManager*** class.

> To open the InventoryManager.java file in your IDE, select
> **File** > **Open** > guide-rest-hateoas/start/src/main/java/io/openliberty/guides/microprofile/InventoryManager.java, or click the following button

::openFile{path="/home/project/guide-rest-hateoas/start/src/main/java/io/openliberty/guides/microprofile/InventoryManager.java"}



```java
package io.openliberty.guides.microprofile;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import io.openliberty.guides.microprofile.util.ReadyJson;
import io.openliberty.guides.microprofile.util.InventoryUtil;

@ApplicationScoped
public class InventoryManager {

    private ConcurrentMap<String, JsonObject> inv = new ConcurrentHashMap<>();

    public JsonObject get(String hostname) {
        JsonObject properties = inv.get(hostname);
        if (properties == null) {
            if (InventoryUtil.responseOk(hostname)) {
                properties = InventoryUtil.getProperties(hostname);
                this.add(hostname, properties);
            } else {
                return ReadyJson.SERVICE_UNREACHABLE.getJson();
            }
        }
        return properties;
    }

    public void add(String hostname, JsonObject systemProps) {
        inv.putIfAbsent(hostname, systemProps);
    }

    public JsonObject list() {
        JsonObjectBuilder systems = Json.createObjectBuilder();
        inv.forEach((host, props) -> {
            JsonObject systemProps = Json.createObjectBuilder()
                                         .add("os.name", props.getString("os.name"))
                                         .add("user.name", props.getString("user.name"))
                                         .build();
            systems.add(host, systemProps);
        });
        systems.add("hosts", systems);
        systems.add("total", inv.size());
        return systems.build();
    }

    public JsonObject getSystems(String url) {
        JsonObjectBuilder systems = Json.createObjectBuilder();
        systems.add("*", InventoryUtil.buildLinksForHost("*", url));

        for (String host : inv.keySet()) {
            systems.add(host, InventoryUtil.buildLinksForHost(host, url));
        }

        return systems.build();
    }

}
```



The ***getSystems*** method accepts a target URL as an argument and returns a JSON object that contains HATEOAS links.

Replace the ***InventoryUtil*** class.

> To open the InventoryUtil.java file in your IDE, select
> **File** > **Open** > guide-rest-hateoas/start/src/main/java/io/openliberty/guides/microprofile/util/InventoryUtil.java, or click the following button

::openFile{path="/home/project/guide-rest-hateoas/start/src/main/java/io/openliberty/guides/microprofile/util/InventoryUtil.java"}



```java
package io.openliberty.guides.microprofile.util;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;

public class InventoryUtil {

    private static final int PORT = 9080;
    private static final String PROTOCOL = "http";
    private static final String SYSTEM_PROPERTIES = "/system/properties";

    public static JsonObject getProperties(String hostname) {
        Client client = ClientBuilder.newClient();
        URI propURI = InventoryUtil.buildUri(hostname);
        return client.target(propURI)
                     .request(MediaType.APPLICATION_JSON)
                     .get(JsonObject.class);
    }

    public static JsonArray buildLinksForHost(String hostname, String invUri) {

        JsonArrayBuilder links = Json.createArrayBuilder();

        links.add(Json.createObjectBuilder()
                      .add("href", StringUtils.appendIfMissing(invUri, "/") + hostname)
                      .add("rel", "self"));

        if (!hostname.equals("*")) {
            links.add(Json.createObjectBuilder()
                 .add("href", InventoryUtil.buildUri(hostname).toString())
                 .add("rel", "properties"));
        }

        return links.build();
    }

    public static boolean responseOk(String hostname) {
        try {
            URL target = new URL(buildUri(hostname).toString());
            HttpURLConnection http = (HttpURLConnection) target.openConnection();
            http.setConnectTimeout(50);
            int response = http.getResponseCode();
            return (response != 200) ? false : true;
        } catch (Exception e) {
            return false;
        }
    }

    private static URI buildUri(String hostname) {
        return UriBuilder.fromUri(SYSTEM_PROPERTIES)
                .host(hostname)
                .port(PORT)
                .scheme(PROTOCOL)
                .build();
    }

}
```



The helper builds a link that points to the inventory entry with a ***self*** relationship. The helper also builds a link that points to the ***system*** service with a ***properties*** relationship:


* `http://localhost:9080/inventory/hosts/<hostname>`
* `http://<hostname>:9080/system/properties`

### Linking to inactive services or unavailable resources

Consider what happens when one of the return links does not work or when a link should be available for one object but not for another. In other words, it is important that a resource or service is available and running before it is added in the HATEOAS links array of the hostname.

Although this guide does not cover this case, always make sure that you receive a good response code from a service before you link that service. Similarly, make sure that it makes sense for a particular object to access a resource it is linked to. For instance, it doesn't make sense for an account holder to be able to withdraw money from their account when their balance is 0. Hence, the account holder should not be linked to a resource that provides money withdrawal.

::page{title="Running the application"}

You started the Open Liberty server in dev mode at the beginning of the guide, so all the changes were automatically picked up.


After the server updates, you can find your new hypermedia-driven ***inventory*** service at the ***/inventory/hosts*** endpoint. Run the following curl command by another command-line session:
```bash
curl -s http://localhost:9080/inventory/hosts | jq
```


::page{title="Testing the hypermedia-driven RESTful web service"}

If the servers are running, you can test the application manually by running the following curl commands to access the **inventory** service that is now driven by hypermedia: 
```bash
curl -s http://localhost:9080/inventory/hosts | jq
```

```bash
curl -s http://localhost:9080/inventory/hosts/localhost| jq
```

Nevertheless, you should rely on automated tests because they are more reliable and trigger a failure if a change introduces a defect.

### Setting up your tests

Create the ***EndpointIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-hateoas/start/src/test/java/it/io/openliberty/guides/hateoas/EndpointIT.java
```


> Then, to open the EndpointIT.java file in your IDE, select
> **File** > **Open** > guide-rest-hateoas/start/src/test/java/it/io/openliberty/guides/hateoas/EndpointIT.java, or click the following button

::openFile{path="/home/project/guide-rest-hateoas/start/src/test/java/it/io/openliberty/guides/hateoas/EndpointIT.java"}



```java
package it.io.openliberty.guides.hateoas;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(OrderAnnotation.class)
public class EndpointIT {
    private String port;
    private String baseUrl;

    private Client client;

    private final String SYSTEM_PROPERTIES = "system/properties";
    private final String INVENTORY_HOSTS = "inventory/hosts";

    @BeforeEach
    public void setup() {
        port = System.getProperty("http.port");
        baseUrl = "http://localhost:" + port + "/";

        client = ClientBuilder.newClient();
    }

    @AfterEach
    public void teardown() {
        client.close();
    }

    /**
     * Checks if the HATEOAS link for the inventory contents (hostname=*)
     * is as expected.
     */
    @Test
    @Order(1)
    public void testLinkForInventoryContents() {
        Response response = this.getResponse(baseUrl + INVENTORY_HOSTS);
        assertEquals(200, response.getStatus(),
                    "Incorrect response code from " + baseUrl);

        JsonObject systems = response.readEntity(JsonObject.class);

        String expected;
        String actual;
        boolean isFound = false;


        if (!systems.isNull("*")) {
            isFound = true;
            JsonArray links = systems.getJsonArray("*");

            expected = baseUrl + INVENTORY_HOSTS + "/*";
            actual = links.getJsonObject(0).getString("href");
            assertEquals(expected, actual, "Incorrect href");

            expected = "self";
            actual = links.getJsonObject(0).getString("rel");
            assertEquals(expected, actual, "Incorrect rel");
        }


        assertTrue(isFound, "Could not find system with hostname *");

        response.close();
    }

    /**
     * Checks that the HATEOAS links, with relationships 'self' and 'properties' for
     * a simple localhost system is as expected.
     */
    @Test
    @Order(2)
    public void testLinksForSystem() {
        this.visitLocalhost();

        Response response = this.getResponse(baseUrl + INVENTORY_HOSTS);
        assertEquals(200, response.getStatus(),
                     "Incorrect response code from " + baseUrl);

        JsonObject systems = response.readEntity(JsonObject.class);

        String expected;
        String actual;
        boolean isHostnameFound = false;


        if (!systems.isNull("localhost")) {
            isHostnameFound = true;
            JsonArray links = systems.getJsonArray("localhost");

            expected = baseUrl + INVENTORY_HOSTS + "/localhost";
            actual = links.getJsonObject(0).getString("href");
            assertEquals(expected, actual, "Incorrect href");

            expected = "self";
            actual = links.getJsonObject(0).getString("rel");
            assertEquals(expected, actual, "Incorrect rel");

            expected = baseUrl + SYSTEM_PROPERTIES;
            actual = links.getJsonObject(1).getString("href");
            assertEquals(expected, actual, "Incorrect href");

            expected = "properties";
            actual = links.getJsonObject(1).getString("rel");

            assertEquals(expected, actual, "Incorrect rel");
        }


        assertTrue(isHostnameFound, "Could not find system with hostname *");
        response.close();

    }

    /**
     * Returns a Response object for the specified URL.
     */
    private Response getResponse(String url) {
        return client.target(url).request().get();
    }

    /**
     * Makes a GET request to localhost at the Inventory service.
     */
    private void visitLocalhost() {
        Response response = this.getResponse(baseUrl + SYSTEM_PROPERTIES);
        assertEquals(200, response.getStatus(),
                     "Incorrect response code from " + baseUrl);
        response.close();
        Response targetResponse =
        client.target(baseUrl + INVENTORY_HOSTS + "/localhost")
                                        .request()
                                        .get();
        targetResponse.close();
    }
}
```



The ***@BeforeEach*** and ***@AfterEach*** annotations are placed on setup and teardown tasks that are run for each individual test.

### Writing the tests

Each test method must be marked with the ***@Test*** annotation. The execution order of test methods is controlled by marking them with the ***@Order*** annotation. The value that is passed into the annotation denotes the order in which the methods are run.

The ***testLinkForInventoryContents*** test is responsible for asserting that the correct HATEOAS link is created for the inventory contents.

Finally, the ***testLinksForSystem*** test is responsible for asserting that the correct HATEOAS links are created for the ***localhost*** system. This method checks for both the ***self*** link that points to the ***inventory*** service and the ***properties*** link that points to the ***system*** service, which is running on the ***localhost*** system.

### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.
You will see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.hateoas.EndpointIT
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.951 s - in it.io.openliberty.guides.hateoas.EndpointIT

Results:

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

Integration tests finished.
```

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran the server, or by typing ***q*** and then pressing the ***enter/return*** key.

::page{title="Summary"}

### Nice Work!

You've just built and tested a hypermedia-driven RESTful web service on top of Open Liberty.




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-rest-hateoas*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-rest-hateoas
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Creating%20a%20hypermedia-driven%20RESTful%20web%20service&guide-id=cloud-hosted-guide-rest-hateoas)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-rest-hateoas/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-rest-hateoas/pulls)



### Where to next?

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Creating a MicroProfile application](https://openliberty.io/guides/microprofile-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
