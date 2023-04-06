---
markdown-version: v1
title: instructions
branch: lab-204-instruction
version-history-start-date: 2022-02-09T14:19:17.000Z
---
::page{title="Welcome to the A Technical Deep Dive on Liberty guide!"}

Liberty is a cloud-optimized Java runtime that is fast to start up with a low memory footprint and a development mode, known as dev mode, for quick iteration. With Liberty, adopting the latest open cloud-native Java APIs, like MicroProfile and Jakarta EE, is as simple as adding features to your server configuration. The Liberty zero migration architecture lets you focus on what's important and not the APIs changing under you.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You will learn how to build a RESTful microservice on Liberty with Jakarta EE and MicroProfile. You will use Gradle throughout this exercise to build the microservice and to interact with the running Liberty instance. Then, you’ll build a container image for the microservice. You will also learn how to secure the REST endpoints and use JSON Web Tokens to communicate with the provided ***system*** secured microservice.

The microservice that you’ll work with is called ***inventory***. The ***inventory*** microservice persists data into a PostgreSQL database. 

![Inventory microservice](https://raw.githubusercontent.com/OpenLiberty/guide-liberty-deep-dive-gradle/prod/assets/inventory.png)


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

Clone the [Git repository](https://github.com/OpenLiberty/guide-liberty-deep-dive-gradle.git):

```bash
git clone https://github.com/openliberty/guide-liberty-deep-dive-gradle.git
cd guide-liberty-deep-dive-gradle
```

The ***start*** directory is an empty directory where you will build the ***inventory*** service.

The ***finish*** directory contains the finished projects of different modules that you will build.


In this IBM Cloud environment, you need to change the user home to ***/home/project*** by running the following command:
```bash
sudo usermod -d /home/project theia
```

::page{title="Getting started with Liberty and REST"}

Liberty now offers an easier way to get started with developing your application: the Open Liberty Starter. This tool provides a simple and quick way to get the necessary files to start building an application on Liberty. Through this tool, you can specify your application and project name. You can also choose a build tool from either Maven or Gradle, and pick the Java SE, Jakarta EE, and MicroProfile versions for your application.

In this workshop, the starting project is provided for you or you can use the Open Liberty Starter to create the starting point of the application. Gradle is used as the selected build tool and the application uses of Jakarta EE 9.1 and MicroProfile 5.

To get started with this tool, see the Getting Started page: [https://openliberty.io/start/](https://openliberty.io/start/)

On that page, enter the following properties in the **Create a starter application** wizard.

* Under Group specify: ***io.openliberty.deepdive***
* Under Artifact specify: ***inventory***
* Under Build Tool select: ***Gradle***
* Under Java SE Version select: ***your version***
* Under Java EE/Jakarta EE Version select: ***9.1***
* Under MicroProfile Version select: `5` 


In this Skills Network environment, instead of manually downloading and extracting the project, run the following commands:
```bash
cd /home/project/guide-liberty-deep-dive-gradle/start
curl -o inventory.zip 'https://start.openliberty.io/api/start?a=inventory&b=maven&e=9.1&g=io.openliberty.deepdive&j=11&m=5.0'
unzip inventory.zip -d inventory
```

After getting the ***inventory*** project, switch the workspace to the ***/home/project/guide-liberty-deep-dive-gradle/start/inventory*** directory.
> - Select **File** > **Close Workspace** from the menu of the IDE.
>   - Click the OK button to confirm to close.
>   - Wait for the IDE to refresh.
> - Select **File** > **Open Workspace...** from the menu of the IDE.
>   - In the **Open Workspace** window, select the ***/home/project/guide-liberty-deep-dive-gradle/start/inventory*** directory and click the **Open** button.
>   - Wait for the IDE to refresh.

### Building the application

This application is configured to be built with Gradle. Every Gradle-configured project contains a ***settings.gradle*** and a ***build.gradle*** file that defines the project configuration, dependencies, and plug-ins.



Your ***settings.gradle*** and ***build.gradle*** files are located in the ***start/inventory*** directory and is configured to include the ***io.openliberty.tools.gradle.Liberty*** Liberty Gradle plugin. Using the plug-in, you can install applications into Liberty and manage the server instances.

To begin, open a command-line session and navigate to your application directory. 


```bash
cd /home/project/guide-liberty-deep-dive-gradle/start/inventory
```

Build and deploy the ***inventory*** microservice to Liberty by running the Gradle ***libertyRun*** task:

```bash
./gradlew libertyRun
```

The ***gradlew*** command initiates a Gradle build, during which the target directory is created to store all build-related files.

The ***libertyRun*** argument specifies the Liberty ***run*** task, which starts a Liberty server instance in the foreground. As part of this phase, a Liberty server runtime is downloaded and installed into the ***build/wlp*** directory. Additionally, a server instance is created and configured in the ***build/wlp/usr/servers/defaultServer*** directory, and the application is installed into that server by using [loose config](https://www.ibm.com/support/knowledgecenter/en/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/rwlp_loose_applications.html).

For more information about the Liberty Gradle plug-in, see its [GitHub repository](https://github.com/OpenLiberty/ci.gradle).

While the server starts up, various messages display in your command-line session. Wait for the following message, which indicates that the server startup is complete:

```
[INFO] [AUDIT] CWWKF0011I: The server defaultServer is ready to run a smarter planet.
```

When you need to stop the server, press `Ctrl+C` in the command-line session where you ran the server, or run the ***libertyStop*** goal from the ***start/inventory*** directory in another command-line session:

```bash
./gradlew libertyStop
```


### Starting and stopping the Liberty server in the background

Although you can start and stop the server in the foreground by using the Gradle ***libertyRun*** task, you can also start and stop the server in the background with the Gradle ***libertyStart*** and ***libertyStop*** goals:

```bash
./gradlew libertyStart
./gradlew libertyStop
```

### Updating the server configuration without restarting the server

The Liberty Gradle plug-in includes a ***dev*** task that listens for any changes in the project, including application source code or configuration. The Liberty server automatically reloads the configuration without restarting. This goal allows for quicker turnarounds and an improved developer experience.

If the Liberty server is running, stop it and restart it in dev mode by running the ***libertyDev*** goal in the ***start/inventory*** directory:

```bash
./gradlew libertyDev
```

After you see the following message, your application server in dev mode is ready:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode automatically picks up changes that you make to your application and allows you to run tests by pressing the ***enter/return*** key in the active command-line session. When you’re working on your application, rather than rerunning Gradle tasks, press the ***enter/return*** key to verify your change.

### Developing a RESTful microservice

Now that a basic Liberty application is running, the next step is to create the additional application and resource classes that the application needs. Within these classes, you use Jakarta REST and other MicroProfile and Jakarta APIs.

Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE. Go to the ***start/inventory*** directory.
```bash
cd /home/project/guide-liberty-deep-dive-gradle/start/inventory
```

Create the ***Inventory*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/Inventory.java
```


> Then, to open the Inventory.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/Inventory.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/Inventory.java"}



```java
package io.openliberty.deepdive.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.openliberty.deepdive.rest.model.SystemData;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Inventory {

    private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());

    public List<SystemData> getSystems() {
        return systems;
    }

    public SystemData getSystem(String hostname) {
        for (SystemData s : systems) {
            if (s.getHostname().equalsIgnoreCase(hostname)) {
                return s;
            }
        }
        return null;
    }

    public void add(String hostname, String osName, String javaVersion, Long heapSize) {
        systems.add(new SystemData(hostname, osName, javaVersion, heapSize));
    }

    public void update(SystemData s) {
        for (SystemData systemData : systems) {
            if (systemData.getHostname().equalsIgnoreCase(s.getHostname())) {
                systemData.setOsName(s.getOsName());
                systemData.setJavaVersion(s.getJavaVersion());
                systemData.setHeapSize(s.getHeapSize());
            }
        }
    }

    public boolean removeSystem(SystemData s) {
        return systems.remove(s);
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


This ***Inventory*** class stores a record of all systems and their system properties. The ***getSystem()*** method within this class retrieves and returns the system data from the system. The ***add()*** method enables the addition of a system and its data to the inventory. The ***update()*** method enables a system and its data on the inventory to be updated. The ***removeSystem()*** method enables the deletion of a system from the inventory.


Create the ***model*** subdirectory, then create the ***SystemData*** class. The ***SystemData*** class is a Plain Old Java Object (POJO) that represents a single inventory entry. 


```bash
mkdir /home/project/guide-liberty-deep-dive-gradle/start/inventory/src/main/java/io/openliberty/deepdive/rest/model
```

Create the ***SystemData*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/model/SystemData.java
```


> Then, to open the SystemData.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/model/SystemData.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/model/SystemData.java"}



```java
package io.openliberty.deepdive.rest.model;

public class SystemData {

    private int id;
    private String hostname;
    private String osName;
    private String javaVersion;
    private Long   heapSize;

    public SystemData() {
    }

    public SystemData(String hostname, String osName, String javaVer, Long heapSize) {
        this.hostname = hostname;
        this.osName = osName;
        this.javaVersion = javaVer;
        this.heapSize = heapSize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public Long getHeapSize() {
        return heapSize;
    }

    public void setHeapSize(Long heapSize) {
        this.heapSize = heapSize;
    }

    @Override
    public boolean equals(Object host) {
      if (host instanceof SystemData) {
        return hostname.equals(((SystemData) host).getHostname());
      }
      return false;
    }
}
```



The ***SystemData*** class contains the hostname, operating system name, Java version, and heap size properties. The various methods within this class allow the viewing or editing the properties of each system in the inventory.


Create the ***SystemResource*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java
```


> Then, to open the SystemResource.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



```java
package io.openliberty.deepdive.rest;

import java.util.List;

import io.openliberty.deepdive.rest.model.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/systems")
public class SystemResource {

    @Inject
    Inventory inventory;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SystemData> listContents() {
        return inventory.getSystems();
    }

    @GET
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemData getSystem(@PathParam("hostname") String hostname) {
        return inventory.getSystem(hostname);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSystem(
        @QueryParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            return fail(hostname + " already exists.");
        }
        inventory.add(hostname, osName, javaVersion, heapSize);
        return success(hostname + " was added.");
    }

    @PUT
    @Path("/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSystem(
        @PathParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventory.getSystem(hostname);
        if (s == null) {
            return fail(hostname + " does not exists.");
        }
        s.setOsName(osName);
        s.setJavaVersion(javaVersion);
        s.setHeapSize(heapSize);
        inventory.update(s);
        return success(hostname + " was updated.");
    }

    @DELETE
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeSystem(@PathParam("hostname") String hostname) {
        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            inventory.removeSystem(s);
            return success(hostname + " was removed.");
        } else {
            return fail(hostname + " does not exists.");
        }
    }

    @POST
    @Path("/client/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSystemClient(@PathParam("hostname") String hostname) {
        return fail("This api is not implemented yet.");
    }

    private Response success(String message) {
        return Response.ok("{ \"ok\" : \"" + message + "\" }").build();
    }

    private Response fail(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("{ \"error\" : \"" + message + "\" }")
                       .build();
    }
}
```




In Jakarta RESTful Web Services, a single class like the ***SystemResource.java*** class must represent a single resource, or a group of resources of the same type. In this application, a resource might be a system property, or a set of system properties. It is efficient to have a single class handle multiple different resources, but keeping a clean separation between types of resources helps with maintainability.

The ***@Path*** annotation on this class indicates that this resource responds to the ***/systems*** path in the RESTful application. The ***@ApplicationPath*** annotation in the ***RestApplication*** class, together with the ***@Path*** annotation in the ***SystemResource*** class, indicates that this resource is available at the ***/api/systems*** path.

The Jakarta RESTful Web Services API maps the HTTP methods on the URL to the methods of the class by using annotations. This application uses the ***GET*** annotation to map an HTTP ***GET*** request to the ***/api/systems*** path.

The ***@GET*** annotation on the ***listContents*** method indicates that the method is to be called for the HTTP ***GET*** method. The ***@Produces*** annotation indicates the format of the content that is returned. The value of the ***@Produces*** annotation is specified in the HTTP ***Content-Type*** response header. For this application, a JSON structure is returned for these ***Get*** methods. The ***Content-Type*** for a JSON response is ***application/json*** with ***MediaType.APPLICATION_JSON*** instead of the ***String*** content type. Using a constant such as ***MediaType.APPLICATION_JSON*** is better as in case of a spelling error, a compile failure occurs.

The Jakarta RESTful Web Services API supports a number of ways to marshal JSON. The Jakarta RESTful Web Services specification mandates JSON-Binding (JSON-B). The method body returns the result of ***inventory.getSystems()***. Because the method is annotated with ***@Produces(MediaType.APPLICATION_JSON)***, the Jakarta RESTful Web Services API uses JSON-B to automatically convert the returned object to JSON data in the HTTP response.


### Running the application

Because you started the Liberty server in dev mode at the beginning of this exercise, all the changes were automatically picked up.

Check out the service that you created at the ***http\://localhost:9080/inventory/api/systems*** URL. If successful, it returns `[]` to you.

Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE, and run the following curl command:
```bash
curl 'http://localhost:9080/inventory/api/systems'
```

You can expect to see the following output:

```
[]
```


::page{title="Documenting APIs"}

Next, you will investigate how to document and filter RESTful APIs from annotations, POJOs, and static OpenAPI files by using MicroProfile OpenAPI.

The OpenAPI specification, previously known as the Swagger specification, defines a standard interface for documenting and exposing RESTful APIs. This specification allows both humans and computers to understand or process the functionalities of services without requiring direct access to underlying source code or documentation. The MicroProfile OpenAPI specification provides a set of Java interfaces and programming models that allow Java developers to natively produce OpenAPI v3 documents from their RESTful applications.



The MicroProfile OpenAPI API is included in the ***microProfile*** dependency that is specified in your ***build.gradle*** file. The ***microProfile*** feature that includes the ***mpOpenAPI*** feature is also enabled in the ***server.xml*** file.

### Generating the OpenAPI document

Because the Jakarta RESTful Web Services framework handles basic API generation for Jakarta RESTful Web Services annotations, a skeleton OpenAPI tree can be generated from the existing inventory service. You can use this tree as a starting point and augment it with annotations and code to produce a complete OpenAPI document.

To see the generated OpenAPI tree, you can either visit the ***http\://localhost:9080/openapi*** URL or visit the ***http\://localhost:9080/openapi/ui*** URL for a more interactive view of the APIs. Click the ***interactive UI*** link on the welcome page. Within this UI, you can view each of the endpoints that are available in your application and any schemas. Each endpoint is color coordinated to easily identify the type of each request (for example GET, POST, PUT, DELETE, etc.). Clicking each endpoint within this UI enables you to view further details of each endpoint's parameters and responses. This UI is used for the remainder of this workshop to view and test the application endpoints.


### Augmenting the existing Jakarta RESTful Web Services annotations with OpenAPI annotations

Because all Jakarta RESTful Web Services annotations are processed by default, you can augment the existing code with OpenAPI annotations without needing to rewrite portions of the OpenAPI document that are already covered by the Jakarta RESTful Web Services framework.

Replace the ***SystemResources*** class.

> To open the SystemResource.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



```java
package io.openliberty.deepdive.rest;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import io.openliberty.deepdive.rest.model.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/systems")
public class SystemResource {

    @Inject
    Inventory inventory;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = SystemData.class,
        responseDescription = "A list of system data stored within the inventory.",
        responseCode = "200")
    @Operation(
        summary = "List contents.",
        description = "Returns the currently stored system data in the inventory.",
        operationId = "listContents")
    public List<SystemData> listContents() {
        return inventory.getSystems();
    }

    @GET
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = SystemData.class,
        responseDescription = "System data of a particular host.",
        responseCode = "200")
    @Operation(
        summary = "Get System",
        description = "Retrieves and returns the system data from the system "
                      + "service running on the particular host.",
        operationId = "getSystem")
    public SystemData getSystem(
        @Parameter(
            name = "hostname", in = ParameterIn.PATH,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING))
        @PathParam("hostname") String hostname) {
        return inventory.getSystem(hostname);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully added system to inventory"),
        @APIResponse(responseCode = "400",
            description = "Unable to add system to inventory")
    })
    @Parameters(value = {
        @Parameter(
            name = "hostname", in = ParameterIn.QUERY,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "osName", in = ParameterIn.QUERY,
            description = "The operating system of the system",
            required = true, example = "linux",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "javaVersion", in = ParameterIn.QUERY,
            description = "The Java version of the system",
            required = true, example = "11",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "heapSize", in = ParameterIn.QUERY,
            description = "The heap size of the system",
            required = true, example = "1048576",
            schema = @Schema(type = SchemaType.NUMBER)),
    })
    @Operation(
        summary = "Add system",
        description = "Add a system and its data to the inventory.",
        operationId = "addSystem"
    )
    public Response addSystem(
        @QueryParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            return fail(hostname + " already exists.");
        }
        inventory.add(hostname, osName, javaVersion, heapSize);
        return success(hostname + " was added.");
    }

    @PUT
    @Path("/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully updated system"),
        @APIResponse(responseCode = "400",
            description =
                "Unable to update because the system does not exist in the inventory.")
    })
    @Parameters(value = {
        @Parameter(
            name = "hostname", in = ParameterIn.PATH,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "osName", in = ParameterIn.QUERY,
            description = "The operating system of the system",
            required = true, example = "linux",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "javaVersion", in = ParameterIn.QUERY,
            description = "The Java version of the system",
            required = true, example = "11",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "heapSize", in = ParameterIn.QUERY,
            description = "The heap size of the system",
            required = true, example = "1048576",
            schema = @Schema(type = SchemaType.NUMBER)),
    })
    @Operation(
        summary = "Update system",
        description = "Update a system and its data on the inventory.",
        operationId = "updateSystem"
    )
    public Response updateSystem(
        @PathParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventory.getSystem(hostname);
        if (s == null) {
            return fail(hostname + " does not exists.");
        }
        s.setOsName(osName);
        s.setJavaVersion(javaVersion);
        s.setHeapSize(heapSize);
        inventory.update(s);
        return success(hostname + " was updated.");
    }

    @DELETE
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully deleted the system from inventory"),
        @APIResponse(responseCode = "400",
            description =
                "Unable to delete because the system does not exist in the inventory")
    })
    @Parameter(
        name = "hostname", in = ParameterIn.PATH,
        description = "The hostname of the system",
        required = true, example = "localhost",
        schema = @Schema(type = SchemaType.STRING)
    )
    @Operation(
        summary = "Remove system",
        description = "Removes a system from the inventory.",
        operationId = "removeSystem"
    )
    public Response removeSystem(@PathParam("hostname") String hostname) {
        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            inventory.removeSystem(s);
            return success(hostname + " was removed.");
        } else {
            return fail(hostname + " does not exists.");
        }
    }

    @POST
    @Path("/client/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully added system client"),
        @APIResponse(responseCode = "400",
            description = "Unable to add system client")
    })
    @Parameter(
        name = "hostname", in = ParameterIn.PATH,
        description = "The hostname of the system",
        required = true, example = "localhost",
        schema = @Schema(type = SchemaType.STRING)
    )
    @Operation(
        summary = "Add system client",
        description = "This adds a system client.",
        operationId = "addSystemClient"
    )
    public Response addSystemClient(@PathParam("hostname") String hostname) {
        return fail("This api is not implemented yet.");
    }

    private Response success(String message) {
        return Response.ok("{ \"ok\" : \"" + message + "\" }").build();
    }

    private Response fail(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("{ \"error\" : \"" + message + "\" }")
                       .build();
    }
}
```




Add OpenAPI ***@APIResponseSchema***, ***@APIResponses***, ***@APIResponse***, ***@Parameters***, ***@Parameter***, and ***@Operation*** annotations to the REST methods, ***listContents()***, ***getSystem()***, ***addSystem()***, ***updateSystem()***, ***removeSystem()***, and ***addSystemClient()***.

Note, the ***@Parameter*** annotation can be placed either ***inline*** or ***outline***. Examples of both are provided within this workshop.

Many OpenAPI annotations are available and can be used according to what's best for your application and its classes. You can find all the annotations in the [MicroProfile OpenAPI specification](https://download.eclipse.org/microprofile/microprofile-open-api-3.0/microprofile-openapi-spec-3.0.html#_annotations).

Because the Liberty server was started in dev mode at the beginning of this exercise, your changes were automatically picked up. Go to the ***http\://localhost:9080/openapi*** URL to see the updated endpoint descriptions. The endpoints at which your REST methods are served now more meaningful:

```bash
curl http://localhost:9080/openapi
```

```
---
openapi: 3.0.3
info:
  title: Generated API
  version: "1.0"
servers:
- url: http://localhost:9080/inventory
- url: https://localhost:9443/inventory
paths:
  /api/systems:
    get:
      summary: List contents.
      description: Returns the currently stored host:properties pairs in the inventory.
      operationId: listContents
      responses:
        "200":
          description: Returns the currently stored host:properties pairs in the inventory.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SystemData'
...
```

You can also visit the ***http\://localhost:9080/openapi/ui*** URL to see each endpoint's updated description. Click each of the icons within the UI to see the updated descriptions for each of the endpoints.
In this Skills Network environment, simply click the following button:

::startApplication{port="9080" display="external" name="Visit OpenAPI UI" route="/openapi/ui"}

### Augmenting POJOs with OpenAPI annotations

OpenAPI annotations can also be added to POJOs to describe what they represent. Currently, the OpenAPI document doesn't have a meaningful description of the ***SystemData*** POJO so it's difficult to tell exactly what this POJO is used for. To describe the ***SystemData*** POJO in more detail, augment the ***SystemData.java*** file with some OpenAPI annotations.

Replace the ***SystemData*** class.

> To open the SystemData.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/model/SystemData.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/model/SystemData.java"}



```java
package io.openliberty.deepdive.rest.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@Schema(name = "SystemData",
        description = "POJO that represents a single inventory entry.")
public class SystemData {

    private int id;

    @Schema(required = true)
    private String hostname;

    private String osName;
    private String javaVersion;
    private Long   heapSize;

    public SystemData() {
    }

    public SystemData(String hostname, String osName, String javaVer, Long heapSize) {
        this.hostname = hostname;
        this.osName = osName;
        this.javaVersion = javaVer;
        this.heapSize = heapSize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public Long getHeapSize() {
        return heapSize;
    }

    public void setHeapSize(Long heapSize) {
        this.heapSize = heapSize;
    }

    @Override
    public boolean equals(Object host) {
      if (host instanceof SystemData) {
        return hostname.equals(((SystemData) host).getHostname());
      }
      return false;
    }
}
```




Add OpenAPI ***@Schema*** annotations to the ***SystemData*** class and the ***hostname*** variable.


Refresh the ***http\://localhost:9080/openapi*** URL to see the updated OpenAPI tree. You should see much more meaningful data for the Schema:

```bash
curl http://localhost:9080/openapi
```

```
components:
  schemas:
    SystemData:
      description: POJO that represents a single inventory entry.
      required:
      - hostname
      - properties
      type: object
      properties:
        hostname:
          type: string
        properties:
          type: object
```

Again, you can also view this at the ***http\://localhost:9080/openapi/ui*** URL. Scroll down in the UI to the schemas section and open up the SystemData schema icon.

::startApplication{port="9080" display="external" name="Visit OpenAPI UI" route="/openapi/ui"}

You can also use this UI to try out the various endpoints. In the UI, head to the POST request ***/api/systems***. This endpoint enables you to create a system. Once you've opened this icon up, click the ***Try it out*** button. Now enter appropriate values for each of the required parameters and click the ***Execute*** button.

You can verify that this system was created by testing the ***/api/systems*** GET request that returns the currently stored system data in the inventory. Execute this request in the UI, then in the response body you should see your system and its data listed.

You can follow these same steps for updating and deleting systems: visiting the corresponding endpoint in the UI, executing the endpoint, and then verifying the result by using the ***/api/systems*** GET request endpoint.

You can learn more about MicroProfile OpenAPI from the [Documenting RESTful APIs guide](https://openliberty.io/guides/microprofile-openapi.html).


::page{title=" Configuring the microservice"}

Next, you can externalize your Liberty server configuration and inject configuration for your microservice by using MicroProfile Config.


### Enabling configurable ports and context root

So far, you used hardcoded values to set the HTTP and HTTPS ports and the context root for the Liberty server. These configurations can be externalized so you can easily change their values when you want to deploy your microservice by different ports and context root.

Replace the ***server.xml*** file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="inventory">

    <!-- Enable features -->
    <featureManager>
        <feature>jakartaee-9.1</feature>
        <feature>microProfile-5.0</feature>
    </featureManager>

    <variable name="default.http.port" defaultValue="9080" />
    <variable name="default.https.port" defaultValue="9443" />
    <variable name="default.context.root" defaultValue="/inventory" />

    <!-- To access this server from a remote client,
         add a host attribute to the following element, e.g. host="*" -->
    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="${default.http.port}" 
                  httpsPort="${default.https.port}" />
    
    <!-- Automatically expand WAR files and EAR files -->
    <applicationManager autoExpand="true"/>

    <!-- Configures the application on a specified context root -->
    <webApplication contextRoot="${default.context.root}" 
                    location="inventory.war" /> 

    <!-- Default SSL configuration enables trust for default certificates from the Java runtime -->
    <ssl id="defaultSSLConfig" trustDefaultCerts="true" />
</server>
```




Add variables for the ***HTTP*** port, ***HTTPS*** port, and the ***context root*** to the ***server.xml*** file. Change the ***httpEndpoint*** element to reflect the new ***default.http.port*** and ***default.http.port*** variables and change the ***contextRoot*** to use the new ***default.context.root*** variable too.

Replace the ***build.gradle*** file.

> To open the build.gradle file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/build.gradle, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/build.gradle"}



```
plugins {
    id 'war'
    id 'io.openliberty.tools.gradle.Liberty' version '3.5.2'
}

version '1.0-SNAPSHOT'
group 'io.openliberty.deepdive'

sourceCompatibility = 11
targetCompatibility = 11
tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}

repositories {
    mavenCentral()
}

dependencies {
    providedCompile 'jakarta.platform:jakarta.jakartaee-api:9.1.0'
    providedCompile 'org.eclipse.microprofile:microprofile:5.0'
}

war {
    archiveVersion = ''
}

ext  {
    liberty.server.var.'default.http.port' = '9081'
    liberty.server.var.'default.https.port' = '9445'
    liberty.server.var.'default.context.root' = '/trial'
}

clean.dependsOn 'libertyStop'
```



build.gradle
```
```

Set the ***archiveVersion*** property to an empty string for the ***war*** task and add properties for the ***HTTP*** port, ***HTTPS*** port, and the ***context root*** to the ***build.gradle*** file.

* ***liberty.server.var.'default.http.port'*** to ***9081***
* ***liberty.server.var.'default.https.port'*** to ***9445***
* ***liberty.server.var.'default.context.root'*** to ***/trial***

Because you are using dev mode, these changes are automatically picked up by the server. After you see the following message, your application server in dev mode is ready again:

```
**************************************************************
*    Liberty is running in dev mode.
```


Now, you can access the application by running the following command:
```bash
curl http://localhost:9081/trial/api/systems
```

Alternatively, for the updated OpenAPI UI, click the following button to visit ***/openapi/ui*** endpoint:

::startApplication{port="9081" display="external" name="Visit OpenAPI UI" route="/openapi/ui"}

build.gradle
```
```

When you are finished trying out changing this configuration, change the variables back to their original values.

* update ***liberty.server.var.'default.http.port'*** to ***9080***
* update ***liberty.server.var.'default.https.port'*** to ***9443***
* update ***liberty.server.var.'default.context.root'*** to ***/inventory***

Wait until you see the following message:

```
**************************************************************
*    Liberty is running in dev mode.
```

### Injecting static configuration

You can now explore how to use MicroProfile's Config API to inject static configuration into your microservice.

The MicroProfile Config API is included in the MicroProfile dependency that is specified in your ***build.gradle*** file. Look for the dependency with the ***microprofile*** artifact ID. This dependency provides a library that allows the use of the MicroProfile Config API. The ***microProfile*** feature is also enabled in the ***server.xml*** file.


First, you need to edit the ***SystemResource*** class to inject static configuration into the ***CLIENT_PORT*** variable.

Replace the ***SystemResource*** class.

> To open the SystemResource.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



```java
package io.openliberty.deepdive.rest;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.openliberty.deepdive.rest.model.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/systems")
public class SystemResource {

    @Inject
    Inventory inventory;

    @Inject
    @ConfigProperty(name = "client.https.port")
    String CLIENT_PORT;


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = SystemData.class,
        responseDescription = "A list of system data stored within the inventory.",
        responseCode = "200")
    @Operation(
        summary = "List contents.",
        description = "Returns the currently stored system data in the inventory.",
        operationId = "listContents")
    public List<SystemData> listContents() {
        return inventory.getSystems();
    }

    @GET
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = SystemData.class,
        responseDescription = "System data of a particular host.",
        responseCode = "200")
    @Operation(
        summary = "Get System",
        description = "Retrieves and returns the system data from the system "
        + "service running on the particular host.",
        operationId = "getSystem"
    )
    public SystemData getSystem(
        @Parameter(
            name = "hostname", in = ParameterIn.PATH,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)
        )
        @PathParam("hostname") String hostname) {
        return inventory.getSystem(hostname);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully added system to inventory"),
        @APIResponse(responseCode = "400",
            description = "Unable to add system to inventory")
    })
    @Parameters(value = {
        @Parameter(
            name = "hostname", in = ParameterIn.QUERY,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "osName", in = ParameterIn.QUERY,
            description = "The operating system of the system",
            required = true, example = "linux",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "javaVersion", in = ParameterIn.QUERY,
            description = "The Java version of the system",
            required = true, example = "11",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "heapSize", in = ParameterIn.QUERY,
            description = "The heap size of the system",
            required = true, example = "1048576",
            schema = @Schema(type = SchemaType.NUMBER)),
    })
    @Operation(
        summary = "Add system",
        description = "Add a system and its data to the inventory.",
        operationId = "addSystem"
    )
    public Response addSystem(
        @QueryParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            return fail(hostname + " already exists.");
        }
        inventory.add(hostname, osName, javaVersion, heapSize);
        return success(hostname + " was added.");
    }

    @PUT
    @Path("/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully updated system"),
        @APIResponse(responseCode = "400",
           description =
               "Unable to update because the system does not exist in the inventory.")
    })
    @Parameters(value = {
        @Parameter(
            name = "hostname", in = ParameterIn.PATH,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "osName", in = ParameterIn.QUERY,
            description = "The operating system of the system",
            required = true, example = "linux",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "javaVersion", in = ParameterIn.QUERY,
            description = "The Java version of the system",
            required = true, example = "11",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "heapSize", in = ParameterIn.QUERY,
            description = "The heap size of the system",
            required = true, example = "1048576",
            schema = @Schema(type = SchemaType.NUMBER)),
    })
    @Operation(
        summary = "Update system",
        description = "Update a system and its data on the inventory.",
        operationId = "updateSystem"
    )
    public Response updateSystem(
        @PathParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventory.getSystem(hostname);
        if (s == null) {
            return fail(hostname + " does not exists.");
        }
        s.setOsName(osName);
        s.setJavaVersion(javaVersion);
        s.setHeapSize(heapSize);
        inventory.update(s);
        return success(hostname + " was updated.");
    }

    @DELETE
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully deleted the system from inventory"),
        @APIResponse(responseCode = "400",
            description =
                "Unable to delete because the system does not exist in the inventory")
    })
    @Parameter(
        name = "hostname", in = ParameterIn.PATH,
        description = "The hostname of the system",
        required = true, example = "localhost",
        schema = @Schema(type = SchemaType.STRING)
    )
    @Operation(
        summary = "Remove system",
        description = "Removes a system from the inventory.",
        operationId = "removeSystem"
    )
    public Response removeSystem(@PathParam("hostname") String hostname) {
        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            inventory.removeSystem(s);
            return success(hostname + " was removed.");
        } else {
            return fail(hostname + " does not exists.");
        }
    }

    @POST
    @Path("/client/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully added system client"),
        @APIResponse(responseCode = "400",
            description = "Unable to add system client")
    })
    @Parameter(
        name = "hostname", in = ParameterIn.PATH,
        description = "The hostname of the system",
        required = true, example = "localhost",
        schema = @Schema(type = SchemaType.STRING)
    )
    @Operation(
        summary = "Add system client",
        description = "This adds a system client.",
        operationId = "addSystemClient"
    )
    public Response addSystemClient(@PathParam("hostname") String hostname) {
        System.out.println(CLIENT_PORT);
        return success("Client Port: " + CLIENT_PORT);
    }

    private Response success(String message) {
        return Response.ok("{ \"ok\" : \"" + message + "\" }").build();
    }

    private Response fail(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("{ \"error\" : \"" + message + "\" }")
                       .build();
    }
}
```



The ***@Inject*** annotation injects the value from other configuration sources to the ***CLIENT_PORT*** variable. The ***@ConfigProperty*** defines the external property name as ***client.https.port***.

Update the ***POST*** request so that the ***/client/{hostname}*** endpoint prints the ***CLIENT_PORT*** value.


### Adding the microprofile-config.properties file

Define the configurable variables in the ***microprofile-config.properties*** configuration file for MicroProfile Config at the ***src/main/resources/META-INF*** directory.


```bash
mkdir -p /home/project/guide-liberty-deep-dive-gradle/start/inventory/src/main/resources/META-INF
```

Create the ***microprofile-config.properties*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive-gradle/start/src/main/resources/META-INF/microprofile-config.properties
```


> Then, to open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/resources/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/resources/META-INF/microprofile-config.properties"}



```
config_ordinal=100

client.https.port=5555
```



Using the ***config_ordinal*** variable in this properties file, you can set the ordinal of this file and thus other configuration sources.

The ***client.https.port*** variable enables the client port to be overwritten.

Revisit the OpenAPI UI ***http\://localhost:9080/openapi/ui*** to view these changes. Open the ***/api/systems/client/{hostname}*** endpoint and run it within the UI to view the ***CLIENT_PORT*** value.

::startApplication{port="9080" display="external" name="Visit OpenAPI UI" route="/openapi/ui"}

You can learn more about MicroProfile Config from the [Configuring microservices guide](https://openliberty.io/guides/microprofile-config.html).



::page{title="Securing RESTful APIs"}

Now you can secure your RESTful APIs. Navigate to your application directory. 


```bash
cd /home/project/guide-liberty-deep-dive-gradle/start/inventory
```

Begin by adding some users and user groups to your ***server.xml*** Liberty configuration file.

Replace the ***server.xml*** file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="inventory">

    <featureManager>
        <feature>jakartaee-9.1</feature>
        <feature>microProfile-5.0</feature>
    </featureManager>

    <variable name="default.http.port" defaultValue="9080" />
    <variable name="default.https.port" defaultValue="9443" />
    <variable name="default.context.root" defaultValue="/inventory" />

    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="${default.http.port}" 
                  httpsPort="${default.https.port}" />

    <!-- Automatically expand WAR files and EAR files -->
    <applicationManager autoExpand="true"/>

    <basicRegistry id="basic" realm="WebRealm">
        <user name="bob" password="{xor}PTA9Lyg7" />
        <user name="alice" password="{xor}PjM2PDovKDs=" />
        <group name="admin">
            <member name="bob" />
        </group>
        <group name="user">
            <member name="bob" />
            <member name="alice" />
        </group>
    </basicRegistry>

    <!-- Configures the application on a specified context root -->
    <webApplication contextRoot="${default.context.root}"
                    location="inventory.war">
        <application-bnd>
            <security-role name="admin">
                <group name="admin" />
            </security-role>
            <security-role name="user">
                <group name="user" />
            </security-role>
        </application-bnd>
     </webApplication>

    <!-- Default SSL configuration enables trust for default certificates from the Java runtime -->
    <ssl id="defaultSSLConfig" trustDefaultCerts="true" />

</server>
```



The ***basicRegistry*** element contains a list of all users for the application and their passwords, as well as all of the user groups. Note that this ***basicRegistry*** element is a very simple case for learning purposes. For more information about the different user registries, see the [User registries documentation](https://openliberty.io/docs/latest/user-registries-application-security.html). The ***admin*** group tells the application which of the users are in the administrator group. The ***user*** group tells the application that users are in the user group.

The ***security-role*** maps the ***admin*** role to the ***admin*** group, meaning that all users in the ***admin*** group have the administrator role. Similarly, the ***user*** role is mapped to the ***user*** group, meaning all users in the ***user*** group have the user role.

Your application has the following users and passwords:

| *Username* | *Password* | *Role*
| --- | --- | ---
| bob | bobpwd | admin, user
| alice | alicepwd | user

Now you can secure the ***inventory*** service.

Replace the ***SystemResource*** class.

> To open the SystemResource.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



```java
package io.openliberty.deepdive.rest;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.openliberty.deepdive.rest.model.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/systems")
public class SystemResource {

    @Inject
    Inventory inventory;

    @Inject
    @ConfigProperty(name = "client.https.port")
    String CLIENT_PORT;


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = SystemData.class,
        responseDescription = "A list of system data stored within the inventory.",
        responseCode = "200")
    @Operation(
        summary = "List contents.",
        description = "Returns the currently stored system data in the inventory.",
        operationId = "listContents")
    public List<SystemData> listContents() {
        return inventory.getSystems();
    }

    @GET
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = SystemData.class,
        responseDescription = "System data of a particular host.",
        responseCode = "200")
    @Operation(
        summary = "Get System",
        description = "Retrieves and returns the system data from the system "
                      + "service running on the particular host.",
        operationId = "getSystem"
    )
    public SystemData getSystem(
        @Parameter(
            name = "hostname", in = ParameterIn.PATH,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)
        )
        @PathParam("hostname") String hostname) {
        return inventory.getSystem(hostname);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully added system to inventory"),
        @APIResponse(responseCode = "400",
            description = "Unable to add system to inventory")
    })
    @Parameters(value = {
        @Parameter(
            name = "hostname", in = ParameterIn.QUERY,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "osName", in = ParameterIn.QUERY,
            description = "The operating system of the system",
            required = true, example = "linux",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "javaVersion", in = ParameterIn.QUERY,
            description = "The Java version of the system",
            required = true, example = "11",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "heapSize", in = ParameterIn.QUERY,
            description = "The heap size of the system",
            required = true, example = "1048576",
            schema = @Schema(type = SchemaType.NUMBER)),
    })
    @Operation(
        summary = "Add system",
        description = "Add a system and its data to the inventory.",
        operationId = "addSystem"
    )
    public Response addSystem(
        @QueryParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            return fail(hostname + " already exists.");
        }
        inventory.add(hostname, osName, javaVersion, heapSize);
        return success(hostname + " was added.");
    }

    @PUT
    @Path("/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "admin", "user" })
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully updated system"),
        @APIResponse(responseCode = "400",
           description =
           "Unable to update because the system does not exist in the inventory.")
    })
    @Parameters(value = {
        @Parameter(
            name = "hostname", in = ParameterIn.PATH,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "osName", in = ParameterIn.QUERY,
            description = "The operating system of the system",
            required = true, example = "linux",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "javaVersion", in = ParameterIn.QUERY,
            description = "The Java version of the system",
            required = true, example = "11",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "heapSize", in = ParameterIn.QUERY,
            description = "The heap size of the system",
            required = true, example = "1048576",
            schema = @Schema(type = SchemaType.NUMBER)),
    })
    @Operation(
        summary = "Update system",
        description = "Update a system and its data on the inventory.",
        operationId = "updateSystem"
    )
    public Response updateSystem(
        @PathParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventory.getSystem(hostname);
        if (s == null) {
            return fail(hostname + " does not exists.");
        }
        s.setOsName(osName);
        s.setJavaVersion(javaVersion);
        s.setHeapSize(heapSize);
        inventory.update(s);
        return success(hostname + " was updated.");
    }

    @DELETE
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "admin" })
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully deleted the system from inventory"),
        @APIResponse(responseCode = "400",
            description =
                "Unable to delete because the system does not exist in the inventory")
    })
    @Parameter(
        name = "hostname", in = ParameterIn.PATH,
        description = "The hostname of the system",
        required = true, example = "localhost",
        schema = @Schema(type = SchemaType.STRING)
    )
    @Operation(
        summary = "Remove system",
        description = "Removes a system from the inventory.",
        operationId = "removeSystem"
    )
    public Response removeSystem(@PathParam("hostname") String hostname) {
        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            inventory.removeSystem(s);
            return success(hostname + " was removed.");
        } else {
            return fail(hostname + " does not exists.");
        }
    }

    @POST
    @Path("/client/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "admin" })
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully added system client"),
        @APIResponse(responseCode = "400",
            description = "Unable to add system client")
    })
    @Parameter(
        name = "hostname", in = ParameterIn.PATH,
        description = "The hostname of the system",
        required = true, example = "localhost",
        schema = @Schema(type = SchemaType.STRING)
    )
    @Operation(
        summary = "Add system client",
        description = "This adds a system client.",
        operationId = "addSystemClient"
    )
    public Response addSystemClient(@PathParam("hostname") String hostname) {
        System.out.println(CLIENT_PORT);
        return success("Client Port: " + CLIENT_PORT);
    }

    private Response success(String message) {
        return Response.ok("{ \"ok\" : \"" + message + "\" }").build();
    }

    private Response fail(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("{ \"error\" : \"" + message + "\" }")
                       .build();
    }
}

```



This class now has role-based access control. The role names that are used in the ***@RolesAllowed*** annotations are mapped to group names in the groups claim of the JSON Web Token (JWT). This mapping results in an authorization decision wherever the security constraint is applied.

The ***/{hostname}*** endpoint that is annotated with the ***@PUT*** annotation updates a system in the inventory. This PUT endpoint is annotated with the ***@RolesAllowed({ "admin", "user" })*** annotation. Only authenticated users with the role of ***admin*** or ***user*** can access this endpoint.

The ***/{hostname}*** endpoint that is annotated with the ***@DELETE*** annotation removes a system from the inventory. This DELETE endpoint is annotated with the ***@RolesAllowed({ "admin" })*** annotation. Only authenticated users with the role of ***admin*** can access this endpoint.

You can manually check that the ***inventory*** microservice is secured by making requests to the PUT and DELETE endpoints.

Before making requests, you must add a system to the inventory. Try adding a system by using the POST endpoint ***/systems*** by running the following command:

```bash
curl -X POST "http://localhost:9080/inventory/api/systems?hostname=localhost&osName=mac&javaVersion=11&heapSize=1"
```

You can expect the following response:

```
{ "ok" : "localhost was added." }
```

This command calls the ***/systems*** endpoint and adds a system ***localhost*** to the inventory. You can validate that the command worked by calling the ***/systems*** endpoint with a ***GET*** request to retrieve all the systems in the inventory, with the following curl command:


```bash
curl -s 'http://localhost:9080/inventory/api/systems' | jq
```

You can now expect the following response:

```
[{"heapSize":1,"hostname":"localhost","javaVersion":"11","osName":"mac","id":23}]
```

Now try calling your secure PUT endpoint to update the system that you just added by the following curl command:

```bash
curl -k --user alice:alicepwd -X PUT "http://localhost:9080/inventory/api/systems/localhost?heapSize=2097152&javaVersion=11&osName=linux"
```

As this endpoint is accessible to the groups ***user*** and ***admin***, you must log in with ***user*** credentials to update the system.

You should see the following response:

```
{ "ok" : "localhost was updated." }
```

This response means that you logged in successfully as an authenticated ***user***, and that the endpoint works as expected.

Now try calling the DELETE endpoint. As this endpoint is only accessible to ***admin*** users, you can expect this command to fail if you attempt to access it with a user in the ***user*** group.

You can check that your application is secured against these requests with the following command:

```bash
curl -k --user alice:alicepwd -X DELETE "https://localhost:9443/inventory/api/systems/localhost"
```

As ***alice*** is part of the ***user*** group, this request cannot work. In your dev mode console, you can expect the following output:

```
jakarta.ws.rs.ForbiddenException: Unauthorized
```

Now attempt to call this endpoint with an authenticated ***admin*** user that can work correctly. Run the following curl command:

```bash
curl -k --user bob:bobpwd -X DELETE "https://localhost:9443/inventory/api/systems/localhost"
```

You can expect to see the following response:

```
{ "ok" : "localhost was removed." }
```

This response means that your endpoint is secure. Validate that it works correctly by calling the ***/systems*** endpoint with the following curl command:

```bash
curl "http://localhost:9080/inventory/api/systems"
```

You can expect to see the following output:

```
[]
```

This response shows that the endpoints work as expected and that the system you added was successfully deleted.

::page{title="Consuming the secured RESTful APIs by JWT"}

You can now implement JSON Web Tokens (JWT) and configure them as Single Sign On (SSO) cookies to use the RESTful APIs. The JWT that is generated by Liberty is used to communicate securely between the ***inventory*** and ***system*** microservices. You can implement the ***/client/{hostname}*** POST endpoint to collect the properties from the ***system*** microservices and create a system in the inventory. 

The ***system*** microservice is provided for you.

### Writing the RESTful client interface
Create the ***client*** subdirectory. Then, create a RESTful client interface for the ***system*** microservice in the ***inventory*** microservice.


```bash
mkdir /home/project/guide-liberty-deep-dive-gradle/start/inventory/src/main/java/io/openliberty/deepdive/rest/client
```

Create the ***SystemClient*** interface.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/client/SystemClient.java
```


> Then, to open the SystemClient.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/client/SystemClient.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/client/SystemClient.java"}



```java
package io.openliberty.deepdive.rest.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
public interface SystemClient extends AutoCloseable {

    @GET
    @Path("/property/{property}")
    @Produces(MediaType.TEXT_PLAIN)
    String getProperty(@HeaderParam("Authorization") String authHeader,
                       @PathParam("property") String property);

    @GET
    @Path("/heapsize")
    @Produces(MediaType.TEXT_PLAIN)
    Long getHeapSize(@HeaderParam("Authorization") String authHeader);

}
```




This interface declares methods for accessing each of the endpoints that are set up for you in the ***system*** service. The MicroProfile Rest Client feature automatically builds and generates a client implementation based on what is defined in the ***SystemClient*** interface. You don’t need to set up the client and connect with the remote service.

Now create the required exception classes that are used by the ***SystemClient*** instance.

Create the ***UnknownUriException*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/client/UnknownUriException.java
```


> Then, to open the UnknownUriException.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/client/UnknownUriException.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/client/UnknownUriException.java"}



```java
package io.openliberty.deepdive.rest.client;

public class UnknownUriException extends Exception {

    private static final long serialVersionUID = 1L;

    public UnknownUriException() {
        super();
    }

    public UnknownUriException(String message) {
        super(message);
    }

}
```



This class is an exception that is thrown when an unknown URI is passed to the ***SystemClient***.

Create the ***UnknownUriExceptionMapper*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/client/UnknownUriExceptionMapper.java
```


> Then, to open the UnknownUriExceptionMapper.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/client/UnknownUriExceptionMapper.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/client/UnknownUriExceptionMapper.java"}



```java
package io.openliberty.deepdive.rest.client;

import java.util.logging.Logger;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

public class UnknownUriExceptionMapper
    implements ResponseExceptionMapper<UnknownUriException> {
    Logger LOG = Logger.getLogger(UnknownUriExceptionMapper.class.getName());

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        LOG.info("status = " + status);
        return status == 404;
    }

    @Override
    public UnknownUriException toThrowable(Response response) {
        return new UnknownUriException();
    }
}
```



This class links the ***UnknownUriException*** class with the corresponding response code through a ***ResponseExceptionMapper*** mapper class.

### Implementing the ***/client/{hostname}*** endpoint

Now implement the ***/client/{hostname}*** POST endpoint of the ***SystemResource*** class to consume the secured ***system*** microservice.

Replace the ***SystemResource*** class.

> To open the SystemResource.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



```java
package io.openliberty.deepdive.rest;

import java.net.URI;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import io.openliberty.deepdive.rest.client.SystemClient;
import io.openliberty.deepdive.rest.client.UnknownUriExceptionMapper;
import io.openliberty.deepdive.rest.model.SystemData;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/systems")
public class SystemResource {

    @Inject
    Inventory inventory;

    @Inject
    @ConfigProperty(name = "client.https.port")
    String CLIENT_PORT;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = SystemData.class,
        responseDescription = "A list of system data stored within the inventory.",
        responseCode = "200")
    @Operation(
        summary = "List contents.",
        description = "Returns the currently stored system data in the inventory.",
        operationId = "listContents")
    public List<SystemData> listContents() {
        return inventory.getSystems();
    }

    @GET
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = SystemData.class,
        responseDescription = "System data of a particular host.",
        responseCode = "200")
    @Operation(
        summary = "Get System",
        description = "Retrieves and returns the system data from the system "
                      + "service running on the particular host.",
        operationId = "getSystem"
    )
    public SystemData getSystem(
        @Parameter(
            name = "hostname", in = ParameterIn.PATH,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)
        )
        @PathParam("hostname") String hostname) {
        return inventory.getSystem(hostname);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully added system to inventory"),
        @APIResponse(responseCode = "400",
            description = "Unable to add system to inventory")
    })
    @Parameters(value = {
        @Parameter(
            name = "hostname", in = ParameterIn.QUERY,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "osName", in = ParameterIn.QUERY,
            description = "The operating system of the system",
            required = true, example = "linux",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "javaVersion", in = ParameterIn.QUERY,
            description = "The Java version of the system",
            required = true, example = "11",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "heapSize", in = ParameterIn.QUERY,
            description = "The heap size of the system",
            required = true, example = "1048576",
            schema = @Schema(type = SchemaType.NUMBER)),
    })
    @Operation(
        summary = "Add system",
        description = "Add a system and its data to the inventory.",
        operationId = "addSystem"
    )
    public Response addSystem(
        @QueryParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            return fail(hostname + " already exists.");
        }
        inventory.add(hostname, osName, javaVersion, heapSize);
        return success(hostname + " was added.");
    }

    @PUT
    @Path("/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "admin", "user" })
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully updated system"),
        @APIResponse(responseCode = "400",
           description =
               "Unable to update because the system does not exist in the inventory.")
    })
    @Parameters(value = {
        @Parameter(
            name = "hostname", in = ParameterIn.PATH,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "osName", in = ParameterIn.QUERY,
            description = "The operating system of the system",
            required = true, example = "linux",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "javaVersion", in = ParameterIn.QUERY,
            description = "The Java version of the system",
            required = true, example = "11",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "heapSize", in = ParameterIn.QUERY,
            description = "The heap size of the system",
            required = true, example = "1048576",
            schema = @Schema(type = SchemaType.NUMBER)),
    })
    @Operation(
        summary = "Update system",
        description = "Update a system and its data on the inventory.",
        operationId = "updateSystem"
    )
    public Response updateSystem(
        @PathParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventory.getSystem(hostname);
        if (s == null) {
            return fail(hostname + " does not exists.");
        }
        s.setOsName(osName);
        s.setJavaVersion(javaVersion);
        s.setHeapSize(heapSize);
        inventory.update(s);
        return success(hostname + " was updated.");
    }

    @DELETE
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "admin" })
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully deleted the system from inventory"),
        @APIResponse(responseCode = "400",
            description =
                "Unable to delete because the system does not exist in the inventory")
    })
    @Parameter(
        name = "hostname", in = ParameterIn.PATH,
        description = "The hostname of the system",
        required = true, example = "localhost",
        schema = @Schema(type = SchemaType.STRING)
    )
    @Operation(
        summary = "Remove system",
        description = "Removes a system from the inventory.",
        operationId = "removeSystem"
    )
    public Response removeSystem(@PathParam("hostname") String hostname) {
        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            inventory.removeSystem(s);
            return success(hostname + " was removed.");
        } else {
            return fail(hostname + " does not exists.");
        }
    }

    @POST
    @Path("/client/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "admin" })
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully added system client"),
        @APIResponse(responseCode = "400",
            description = "Unable to add system client")
    })
    @Parameter(
        name = "hostname", in = ParameterIn.PATH,
        description = "The hostname of the system",
        required = true, example = "localhost",
        schema = @Schema(type = SchemaType.STRING)
    )
    @Operation(
        summary = "Add system client",
        description = "This adds a system client.",
        operationId = "addSystemClient"
    )
    public Response addSystemClient(@PathParam("hostname") String hostname) {

        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            return fail(hostname + " already exists.");
        }

        SystemClient customRestClient = null;
        try {
            customRestClient = getSystemClient(hostname);
        } catch (Exception e) {
            return fail("Failed to create the client " + hostname + ".");
        }

        String authHeader = "Bearer " + jwt.getRawToken();
        try {
            String osName = customRestClient.getProperty(authHeader, "os.name");
            String javaVer = customRestClient.getProperty(authHeader, "java.version");
            Long heapSize = customRestClient.getHeapSize(authHeader);
            inventory.add(hostname, osName, javaVer, heapSize);
        } catch (Exception e) {
            return fail("Failed to reach the client " + hostname + ".");
        }
        return success(hostname + " was added.");
    }

    private SystemClient getSystemClient(String hostname) throws Exception {
        String customURIString = "https://" + hostname + ":" + CLIENT_PORT + "/system";
        URI customURI = URI.create(customURIString);
        return RestClientBuilder.newBuilder()
                                .baseUri(customURI)
                                .register(UnknownUriExceptionMapper.class)
                                .build(SystemClient.class);
    }

    private Response success(String message) {
        return Response.ok("{ \"ok\" : \"" + message + "\" }").build();
    }

    private Response fail(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("{ \"error\" : \"" + message + "\" }")
                       .build();
    }
}
```



The ***getSystemClient()*** method builds and returns a new instance of the ***SystemClient*** class for the hostname provided. The ***/client/{hostname}*** POST endpoint uses this method to create a REST client that is called ***customRestClient*** to consume the ***system*** microservice.

A JWT instance is injected to the ***jwt*** field variable by the ***jwtSso*** feature. It is used to create the ***authHeader*** authentication header. It is then passed as a parameter to the endpoints of the ***customRestClient*** to get the properties from the ***system*** microservice. A ***system*** is then added to the inventory.

### Configuring the JSON Web Token

Next, add the JSON Web Token (Single Sign On) feature to the server configuration file for the ***inventory*** service.

Replace the ***server.xml*** file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="inventory">

    <featureManager>
        <feature>jakartaee-9.1</feature>
        <feature>microProfile-5.0</feature>
        <feature>jwtSso-1.0</feature>
    </featureManager>

    <variable name="default.http.port" defaultValue="9080" />
    <variable name="default.https.port" defaultValue="9443" />
    <variable name="default.context.root" defaultValue="/inventory" />

    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="${default.http.port}" 
                  httpsPort="${default.https.port}" />

    <!-- Automatically expand WAR files and EAR files -->
    <applicationManager autoExpand="true"/>
    
    <keyStore id="defaultKeyStore" password="secret" />
    
    <basicRegistry id="basic" realm="WebRealm">
        <user name="bob" password="{xor}PTA9Lyg7" />
        <user name="alice" password="{xor}PjM2PDovKDs=" />

        <group name="admin">
            <member name="bob" />
        </group>

        <group name="user">
            <member name="bob" />
            <member name="alice" />
        </group>
    </basicRegistry>

    <!-- Configures the application on a specified context root -->
    <webApplication contextRoot="${default.context.root}"
                    location="inventory.war">
        <application-bnd>
            <security-role name="admin">
                <group name="admin" />
            </security-role>
            <security-role name="user">
                <group name="user" />
            </security-role>
        </application-bnd>
    </webApplication>

    <jwtSso jwtBuilderRef="jwtInventoryBuilder"/> 
    <jwtBuilder id="jwtInventoryBuilder" 
                issuer="http://openliberty.io" 
                audiences="systemService"
                expiry="24h"/>
    <mpJwt audiences="systemService" 
           groupNameAttribute="groups" 
           id="myMpJwt" 
           issuer="http://openliberty.io"/>

    <!-- Default SSL configuration enables trust for default certificates from the Java runtime -->
    <ssl id="defaultSSLConfig" trustDefaultCerts="true" />

</server>
```




The ***jwtSso*** feature adds the libraries that are required for JWT SSO implementation. Configure the ***jwtSso*** feature by adding the ***jwtBuilder*** configuration to your ***server.xml***. Also, configure the MicroProfile ***JWT*** with the ***audiences*** and ***issuer*** properties that match the ***microprofile-config.properties*** defined at the ***system/src/main/webapp/META-INF*** directory under the ***system*** project. For more information, see the [JSON Web Token Single Sign-On feature](https://www.openliberty.io/docs/latest/reference/feature/jwtSso-1.0.html), [jwtSso element](https://www.openliberty.io/docs/latest/reference/config/jwtSso.html), and [jwtBuilder element](https://www.openliberty.io/docs/latest/reference/config/jwtBuilder.html) documentation.

The ***keyStore*** element is used to define the repository of security certificates used for SSL encryption. The ***id*** attribute is a unique configuration ID that is set to ***defaultKeyStore***. The ***password*** attribute is used to load the keystore file, and its value can be stored in clear text or encoded form. To learn more about other attributes, see the [keyStore](https://openliberty.io/docs/latest/reference/config/keyStore.html#keyStore.html) attribute documentation. 

Because the keystore file is not provided at the ***src*** directory, Liberty creates a Public Key Cryptography Standards #12 (PKCS12) keystore file for you by default. This file needs to be replaced, as the ***keyStore*** configuration must be the same in both ***system*** and ***inventory*** microservices. As the configured ***system*** microservice is already provided for you, copy the ***key.p12*** keystore file from the ***system*** microservice to your ***inventory*** service.


```bash
mkdir -p /home/project/guide-liberty-deep-dive-gradle/start/inventory/src/main/liberty/config/resources/security
cp /home/project/guide-liberty-deep-dive-gradle/finish/system/src/main/liberty/config/resources/security/key.p12 \
   /home/project/guide-liberty-deep-dive-gradle/start/inventory/src/main/liberty/config/resources/security/key.p12
```

Now configure the client https port in the ***build.gradle*** configuration file.

Replace the ***build.gradle*** file.

> To open the build.gradle file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/build.gradle, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/build.gradle"}



```
plugins {
    id 'war'
    id 'io.openliberty.tools.gradle.Liberty' version '3.5.2'
}

version '1.0-SNAPSHOT'
group 'io.openliberty.deepdive'

sourceCompatibility = 11
targetCompatibility = 11
tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}

repositories {
    mavenCentral()
}

dependencies {
    providedCompile 'jakarta.platform:jakarta.jakartaee-api:9.1.0'
    providedCompile 'org.eclipse.microprofile:microprofile:5.0'
}

war {
    archiveVersion = ''
}

ext  {
    liberty.server.var.'default.http.port' = '9080'
    liberty.server.var.'default.https.port' = '9443'
    liberty.server.var.'default.context.root' = '/inventory'
    liberty.server.var.'client.https.port' = '9444'
}

clean.dependsOn 'libertyStop'
```



Configure the client https port by setting the ***liberty.server.var.'client.https.port'*** to ***9444***.

In your dev mode console for the ***inventory*** microservice, press `Ctrl+C` to stop the server. Then, restart the dev mode of the ***inventory*** microservice.

```bash
./gradlew libertyDev
```

After you see the following message, your application server in dev mode is ready again:

```
**************************************************************
*    Liberty is running in dev mode.
```

### Running the ***/client/{hostname}*** endpoint

Open another command-line session and run the ***system*** microservice from the ***finish*** directory.


```bash
cd /home/project/guide-liberty-deep-dive-gradle/finish/system
mvn liberty:run
```

Wait until the following message displays on the ***system*** microservice console.
```
CWWKF0011I: The defaultServer server is ready to run a smarter planet. ...
```

You can check that the ***system*** microservice is secured against unauthenticated requests at the ***https\://localhost:9444/system/api/heapsize*** URL. You can expect to see the following error in the console of the ***system*** microservice:

Open another command-line session and run the following command:
```bash
curl -k 'https://localhost:9444/system/api/heapsize'
```

```
CWWKS5522E: The MicroProfile JWT feature cannot perform authentication because a MicroProfile JWT cannot be found in the request.
```

You can check that the ***/client/{hostname}*** endpoint you updated can access the ***system*** microservice. 

Make an authorized request to the new ***/client/{hostname}*** endpoint.
As this endpoint is restricted to ***admin***, you can use the login credentials for ***bob***, which is in the ***admin*** group.

```bash
curl -k --user bob:bobpwd -X POST "https://localhost:9443/inventory/api/systems/client/localhost"
```

You can expect the following output:

```
{ "ok" : "localhost was added." }
```

You can verify that this endpoint works as expected by running the following command:


```bash
curl -s 'http://localhost:9080/inventory/api/systems' | jq
```

You can expect to see your system listed in the output.

```
[
  {
    "heapSize": 2999975936,
    "hostname": "localhost",
    "id": 11,
    "javaVersion": "11.0.11",
    "osName": "Linux"
  }
]
```

::page{title="Adding health checks"}
Next, you'll use [MicroProfile Health](https://download.eclipse.org/microprofile/microprofile-health-4.0/microprofile-health-spec-4.0.html) to report the health status of the microservice and PostgreSQL database connection.

Navigate to your application directory


```bash
cd /home/project/guide-liberty-deep-dive-gradle/start/inventory
```

A health report is generated automatically for all health services that enable MicroProfile Health.

All health services must provide an implementation of the ***HealthCheck*** interface, which is used to verify their health. MicroProfile Health offers health checks for startup, liveness, and readiness.

A startup check allows applications to define startup probes that are used for initial verification of the application before the liveness probe takes over. For example, a startup check might check which applications require additional startup time on their first initialization.

A liveness check allows third-party services to determine whether a microservice is running. If the liveness check fails, the application can be terminated. For example, a liveness check might fail if the application runs out of memory.

A readiness check allows third-party services, such as Kubernetes, to determine whether a microservice is ready to process requests.

Create the ***health*** subdirectory before creating the health check classes.


```bash
mkdir /home/project/guide-liberty-deep-dive-gradle/start/inventory/src/main/java/io/openliberty/deepdive/rest/health
```

Create the ***StartupCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/health/StartupCheck.java
```


> Then, to open the StartupCheck.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/health/StartupCheck.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/health/StartupCheck.java"}



```java
package io.openliberty.deepdive.rest.health;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Startup;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Startup
@ApplicationScoped
public class StartupCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean)
        ManagementFactory.getOperatingSystemMXBean();
        double cpuUsed = bean.getSystemCpuLoad();
        String cpuUsage = String.valueOf(cpuUsed);
        return HealthCheckResponse.named("Startup Check")
                                  .status(cpuUsed < 0.95).build();
    }
}
```



The ***@Startup*** annotation indicates that this class is a startup health check procedure. Navigate to the ***http\://localhost:9080/health/started*** URL to check the status of the startup health check. In this case, you are checking the cpu usage. If more than 95% of the cpu is being used, a status of ***DOWN*** is returned.
```bash
curl -s http://localhost:9080/health/started | jq
```

Create the ***LivenessCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/health/LivenessCheck.java
```


> Then, to open the LivenessCheck.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/health/LivenessCheck.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/health/LivenessCheck.java"}



```java
package io.openliberty.deepdive.rest.health;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Liveness
@ApplicationScoped
public class LivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long memUsed = memBean.getHeapMemoryUsage().getUsed();
        long memMax = memBean.getHeapMemoryUsage().getMax();

        return HealthCheckResponse.named("Liveness Check")
                                  .status(memUsed < memMax * 0.9)
                                  .build();
    }
}
```



The ***@Liveness*** annotation indicates that this class is a liveness health check procedure. Navigate to the ***http\://localhost:9080/health/live*** URL to check the status of the liveness health check. In this case, you are checking the heap memory usage. If more than 90% of the maximum memory is being used, a status of ***DOWN*** is returned.

```bash
curl -s http://localhost:9080/health/live | jq
```

Create the ***ReadinessCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/health/ReadinessCheck.java
```


> Then, to open the ReadinessCheck.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/health/ReadinessCheck.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/health/ReadinessCheck.java"}



```java
package io.openliberty.deepdive.rest.health;

import java.time.LocalDateTime;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Readiness
@ApplicationScoped
public class ReadinessCheck implements HealthCheck {

    private static final int ALIVE_DELAY_SECONDS = 10;
    private static final String READINESS_CHECK = "Readiness Check";
    private static LocalDateTime aliveAfter = LocalDateTime.now();

    @Override
    public HealthCheckResponse call() {
        if (isAlive()) {
            return HealthCheckResponse.up(READINESS_CHECK);
        }

        return HealthCheckResponse.down(READINESS_CHECK);
    }

    public static void setUnhealthy() {
        aliveAfter = LocalDateTime.now().plusSeconds(ALIVE_DELAY_SECONDS);
    }

    private static boolean isAlive() {
        return LocalDateTime.now().isAfter(aliveAfter);
    }
}
```


The ***@Readiness*** annotation indicates that this class is a readiness health check procedure. Navigate to the ***http\://localhost:9080/health/ready*** URL to check the status of the readiness health check. This readiness check tests the connection to the PostgreSQL container that was created earlier in the guide. If the connection is refused, a status of ***DOWN*** is returned.

```bash
curl -s http://localhost:9080/health/ready | jq
```

Or, you can visit the ***http\://localhost:9080/health*** URL to see the overall health status of the application.

```bash
curl -s http://localhost:9080/health | jq
```

::page{title="Providing metrics"}

Next, you can learn how to use [MicroProfile Metrics](https://download.eclipse.org/microprofile/microprofile-metrics-4.0/microprofile-metrics-spec-4.0.html) to provide metrics from the ***inventory*** microservice.

Go to your application directory.


```bash
cd /home/project/guide-liberty-deep-dive-gradle/start/inventory
```

Enable the ***bob*** user to access the ***/metrics*** endpoints.

Replace the ***server.xml*** file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="inventory">

    <featureManager>
        <feature>jakartaee-9.1</feature>
        <feature>microProfile-5.0</feature>
        <feature>jwtSso-1.0</feature>
    </featureManager>

    <variable name="default.http.port" defaultValue="9080" />
    <variable name="default.https.port" defaultValue="9443" />
    <variable name="default.context.root" defaultValue="/inventory" />

    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="${default.http.port}" 
                  httpsPort="${default.https.port}" />

    <!-- Automatically expand WAR files and EAR files -->
    <applicationManager autoExpand="true"/>
    
    <keyStore id="defaultKeyStore" password="secret" />
    
    <basicRegistry id="basic" realm="WebRealm">
        <user name="bob" password="{xor}PTA9Lyg7" />
        <user name="alice" password="{xor}PjM2PDovKDs=" />

        <group name="admin">
            <member name="bob" />
        </group>

        <group name="user">
            <member name="bob" />
            <member name="alice" />
        </group>
    </basicRegistry>

    <administrator-role>
        <user>bob</user>
        <group>AuthorizedGroup</group>
    </administrator-role>

    <!-- Configures the application on a specified context root -->
    <webApplication contextRoot="${default.context.root}"
                    location="inventory.war">
        <application-bnd>
            <security-role name="admin">
                <group name="admin" />
            </security-role>
            <security-role name="user">
                <group name="user" />
            </security-role>
        </application-bnd>
    </webApplication>

    <jwtSso jwtBuilderRef="jwtInventoryBuilder"/> 
    <jwtBuilder id="jwtInventoryBuilder" 
                issuer="http://openliberty.io" 
                audiences="systemService"
                expiry="24h"/>
    <mpJwt audiences="systemService" 
           groupNameAttribute="groups" 
           id="myMpJwt" 
           issuer="http://openliberty.io"/>

    <!-- Default SSL configuration enables trust for default certificates from the Java runtime -->
    <ssl id="defaultSSLConfig" trustDefaultCerts="true" />

</server>
```



The ***administrator-role*** configuration authorizes the ***bob*** user as an administrator.

Use annotations that are provided by MicroProfile Metrics to instrument the ***inventory*** microservice to provide application-level metrics data.

Replace the ***SystemResource*** class.

> To open the SystemResource.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



```java
package io.openliberty.deepdive.rest;

import java.net.URI;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.metrics.annotation.Counted;

import io.openliberty.deepdive.rest.client.SystemClient;
import io.openliberty.deepdive.rest.client.UnknownUriExceptionMapper;
import io.openliberty.deepdive.rest.model.SystemData;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/systems")
public class SystemResource {

    @Inject
    Inventory inventory;

    @Inject
    @ConfigProperty(name = "client.https.port")
    String CLIENT_PORT;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = SystemData.class,
        responseDescription = "A list of system data stored within the inventory.",
        responseCode = "200")
    @Operation(
        summary = "List contents.",
        description = "Returns the currently stored system data in the inventory.",
        operationId = "listContents")
    public List<SystemData> listContents() {
        return inventory.getSystems();
    }

    @GET
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = SystemData.class,
        responseDescription = "System data of a particular host.",
        responseCode = "200")
    @Operation(
        summary = "Get System",
        description = "Retrieves and returns the system data from the system "
                      + "service running on the particular host.",
        operationId = "getSystem"
    )
    public SystemData getSystem(
        @Parameter(
            name = "hostname", in = ParameterIn.PATH,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)
        )
        @PathParam("hostname") String hostname) {
        return inventory.getSystem(hostname);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Counted(name = "addSystem",
             absolute = true,
             description = "Number of times adding system endpoint is called")
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully added system to inventory"),
        @APIResponse(responseCode = "400",
            description = "Unable to add system to inventory")
    })
    @Parameters(value = {
        @Parameter(
            name = "hostname", in = ParameterIn.QUERY,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "osName", in = ParameterIn.QUERY,
            description = "The operating system of the system",
            required = true, example = "linux",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "javaVersion", in = ParameterIn.QUERY,
            description = "The Java version of the system",
            required = true, example = "11",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "heapSize", in = ParameterIn.QUERY,
            description = "The heap size of the system",
            required = true, example = "1048576",
            schema = @Schema(type = SchemaType.NUMBER)),
    })
    @Operation(
        summary = "Add system",
        description = "Add a system and its data to the inventory.",
        operationId = "addSystem"
    )
    public Response addSystem(
        @QueryParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            return fail(hostname + " already exists.");
        }
        inventory.add(hostname, osName, javaVersion, heapSize);
        return success(hostname + " was added.");
    }

    @PUT
    @Path("/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "admin", "user" })
    @Counted(name = "updateSystem",
             absolute = true,
             description = "Number of times updating a system endpoint is called")
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully updated system"),
        @APIResponse(responseCode = "400",
           description =
               "Unable to update because the system does not exist in the inventory.")
    })
    @Parameters(value = {
        @Parameter(
            name = "hostname", in = ParameterIn.PATH,
            description = "The hostname of the system",
            required = true, example = "localhost",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "osName", in = ParameterIn.QUERY,
            description = "The operating system of the system",
            required = true, example = "linux",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "javaVersion", in = ParameterIn.QUERY,
            description = "The Java version of the system",
            required = true, example = "11",
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "heapSize", in = ParameterIn.QUERY,
            description = "The heap size of the system",
            required = true, example = "1048576",
            schema = @Schema(type = SchemaType.NUMBER)),
    })
    @Operation(
        summary = "Update system",
        description = "Update a system and its data on the inventory.",
        operationId = "updateSystem"
    )
    public Response updateSystem(
        @PathParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventory.getSystem(hostname);
        if (s == null) {
            return fail(hostname + " does not exists.");
        }
        s.setOsName(osName);
        s.setJavaVersion(javaVersion);
        s.setHeapSize(heapSize);
        inventory.update(s);
        return success(hostname + " was updated.");
    }

    @DELETE
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "admin" })
    @Counted(name = "removeSystem",
             absolute = true,
             description = "Number of times removing a system endpoint is called")
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully deleted the system from inventory"),
        @APIResponse(responseCode = "400",
            description =
                "Unable to delete because the system does not exist in the inventory")
    })
    @Parameter(
        name = "hostname", in = ParameterIn.PATH,
        description = "The hostname of the system",
        required = true, example = "localhost",
        schema = @Schema(type = SchemaType.STRING)
    )
    @Operation(
        summary = "Remove system",
        description = "Removes a system from the inventory.",
        operationId = "removeSystem"
    )
    public Response removeSystem(@PathParam("hostname") String hostname) {
        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            inventory.removeSystem(s);
            return success(hostname + " was removed.");
        } else {
            return fail(hostname + " does not exists.");
        }
    }

    @POST
    @Path("/client/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "admin" })
    @Counted(name = "addSystemClient",
             absolute = true,
             description = "Number of times adding a system by client is called")
    @APIResponses(value = {
        @APIResponse(responseCode = "200",
            description = "Successfully added system client"),
        @APIResponse(responseCode = "400",
            description = "Unable to add system client")
    })
    @Parameter(
        name = "hostname", in = ParameterIn.PATH,
        description = "The hostname of the system",
        required = true, example = "localhost",
        schema = @Schema(type = SchemaType.STRING)
    )
    @Operation(
        summary = "Add system client",
        description = "This adds a system client.",
        operationId = "addSystemClient"
    )
    public Response addSystemClient(@PathParam("hostname") String hostname) {

        SystemData s = inventory.getSystem(hostname);
        if (s != null) {
            return fail(hostname + " already exists.");
        }

        SystemClient customRestClient = null;
        try {
            customRestClient = getSystemClient(hostname);
        } catch (Exception e) {
            return fail("Failed to create the client " + hostname + ".");
        }

        String authHeader = "Bearer " + jwt.getRawToken();
        try {
            String osName = customRestClient.getProperty(authHeader, "os.name");
            String javaVer = customRestClient.getProperty(authHeader, "java.version");
            Long heapSize = customRestClient.getHeapSize(authHeader);
            inventory.add(hostname, osName, javaVer, heapSize);
        } catch (Exception e) {
            return fail("Failed to reach the client " + hostname + ".");
        }
        return success(hostname + " was added.");
    }

    private SystemClient getSystemClient(String hostname) throws Exception {
        String customURIString = "https://" + hostname + ":" + CLIENT_PORT + "/system";
        URI customURI = URI.create(customURIString);
        return RestClientBuilder.newBuilder()
                                .baseUri(customURI)
                                .register(UnknownUriExceptionMapper.class)
                                .build(SystemClient.class);
    }

    private Response success(String message) {
        return Response.ok("{ \"ok\" : \"" + message + "\" }").build();
    }

    private Response fail(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("{ \"error\" : \"" + message + "\" }")
                       .build();
    }
}
```



Import the ***Counted*** annotation and apply it to the ***POST /api/systems***, ***PUT /api/systems/{hostname}***, ***DELETE /api/systems/{hostname}***, and ***POST /api/systems/client/{hostname}*** endpoints to monotonically count how many times that the endpoints are accessed. 

Additional information about the annotations that MicroProfile metrics provides, relevant metadata fields, and more are available at the [MicroProfile Metrics Annotation Javadoc](https://openliberty.io/docs/22.0.0.4/reference/javadoc/microprofile-5.0-javadoc.html?package=org/eclipse/microprofile/metrics/annotation/package-frame.html&class=overview-summary.html).


Run the following commands to call some of the endpoints that you annotated:

```bash
curl -k --user bob:bobpwd -X DELETE \
  'https://localhost:9443/inventory/api/systems/localhost'
```

```bash
curl -X POST 'http://localhost:9080/inventory/api/systems?heapSize=1048576&hostname=localhost&javaVersion=9&osName=linux'
```

```bash
curl -k --user alice:alicepwd -X PUT \
  'http://localhost:9080/inventory/api/systems/localhost?heapSize=2097152&javaVersion=11&osName=linux'
```

```bash
curl -s 'http://localhost:9080/inventory/api/systems' | jq
```

MicroProfile Metrics provides 4 different REST endpoints.

* The ***/metrics*** endpoint provides you with all the metrics in text format. 
* The ***/metrics/application*** endpoint provides you with application-specific metrics.
* The ***/metrics/base*** endpoint provides you with metrics that are defined in MicroProfile specifications. Metrics in the base scope are intended to be portable between different MicroProfile-compatible runtimes.
* The ***/metrics/vendor*** endpoint provides you with metrics that are specific to the runtime.


Run the following curl command to see the application metrics that are enabled through MicroProfile Metrics:
```bash
curl -k --user bob:bobpwd https://localhost:9443/metrics/application
```

You can expect to see your application metrics in text format as the following output:

```
# TYPE application_addSystemClient_total counter
# HELP application_addSystemClient_total Number of times adding a system by client is called
application_addSystemClient_total 0
# TYPE application_addSystem_total counter
# HELP application_addSystem_total Number of times adding system endpoint is called
application_addSystem_total 1
# TYPE application_updateSystem_total counter
# HELP application_updateSystem_total Number of times updating a system endpoint is called
application_updateSystem_total 1
# TYPE application_removeSystem_total counter
# HELP application_removeSystem_total Number of times removing a system endpoint is called
application_removeSystem_total 1
```

To see the system metrics, run the following curl command:
```bash
curl -k --user bob:bobpwd https://localhost:9443/metrics/base
```

To see the vendor metrics, run the following curl command:
```bash
curl -k --user bob:bobpwd https://localhost:9443/metrics/vendor
```

To review all the metrics, run the following curl command:
```bash
curl -k --user bob:bobpwd https://localhost:9443/metrics
```


::page{title="Building the container "}

Press `Ctrl+C` in the command-line session to stop the ***gradlew libertyDev*** dev mode that you started in the previous section.

Navigate to your application directory:


```bash
cd /home/project/guide-liberty-deep-dive-gradle/start/inventory
```

The first step to containerizing your application inside of a container is creating a Containerfile. A Containerfile is a collection of instructions for building a container image that can then be run as a container. 

Make sure to start your podman daemon before you proceed.

Create the ***Containerfile*** in the ***start/inventory*** directory.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive-gradle/start/Containerfile
```


> Then, to open the Containerfile file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive-gradle/start/Containerfile, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive-gradle/start/Containerfile"}



```
FROM icr.io/appcafe/open-liberty:full-java11-openj9-ubi

ARG VERSION=1.0
ARG REVISION=SNAPSHOT

LABEL \
  org.opencontainers.image.authors="My Name" \
  org.opencontainers.image.vendor="Open Liberty" \
  org.opencontainers.image.url="local" \
  org.opencontainers.image.source="https://github.com/OpenLiberty/draft-guide-liberty-deepdive" \
  org.opencontainers.image.version="$VERSION" \
  org.opencontainers.image.revision="$REVISION" \
  vendor="Open Liberty" \
  name="inventory" \
  version="$VERSION-$REVISION" \
  summary="" \
  description="This image contains the inventory microservice running with the Open Liberty runtime."

USER root

COPY --chown=1001:0 \
    src/main/liberty/config/ \
    /config/

COPY --chown=1001:0 \
    build/libs/inventory.war \
    /config/apps

USER 1001

RUN configure.sh
```



The ***FROM*** instruction initializes a new build stage and indicates the parent image from which your image is built. In this case, you’re using the ***icr.io/appcafe/open-liberty:full-java11-openj9-ubi*** image that comes with the latest Open Liberty runtime as your parent image.

To help you manage your images, you can label your container images with the ***LABEL*** command. 

The ***COPY*** instructions are structured as ***COPY*** ***[--chown=\<user\>:\<group\>]*** ***\<source\>*** ***\<destination\>***. They copy local files into the specified destination within your container  image. In this case, the first ***COPY*** instruction copies the server configuration file that is at ***src/main/liberty/config/server.xml*** to the ***/config/*** destination directory. Similarly, the second ***COPY*** instruction copies the ***.war*** file to the ***/config/apps*** destination directory.



### Building the container image

Run the ***war*** task from the ***start/inventory*** directory so that the ***.war*** file resides in the ***build/libs*** directory.

```bash
cd /home/project/guide-liberty-deep-dive-gradle/start/inventory
```


```bash
./gradlew war
```

Build your container image with the following commands:

```bash
podman build -t liberty-deepdive-inventory:1.0-SNAPSHOT .
```

In this Skills Network environment, you need to push the image to your container registry on IBM Cloud by running the following commands:
```bash
docker tag liberty-deepdive-inventory:1.0-SNAPSHOT us.icr.io/$SN_ICR_NAMESPACE/liberty-deepdive-inventory:1.0-SNAPSHOT
docker push us.icr.io/$SN_ICR_NAMESPACE/liberty-deepdive-inventory:1.0-SNAPSHOT
```

When the build finishes, run the following command to list all local container images:
```bash
podman images
```

Verify that the ***liberty-deepdive-inventory:1.0-SNAPSHOT*** image is listed among the container images, for example:
```
REPOSITORY                             TAG
localhost/liberty-deepdive-inventory   1.0-SNAPSHOT
icr.io/appcafe/open-liberty            full-java11-openj9-ubi
```


### Running the application in container

Now that the ***inventory*** container image is built, you will run the application in container:

```bash
podman run -d --name inventory -p 9080:9080 liberty-deepdive-inventory:1.0-SNAPSHOT
```

The following table describes the flags in this command: 

| *Flag* | *Description*
| ---| ---
| -d     | Runs the container in the background.
| --name | Specifies a name for the container.
| -p     | Maps the host ports to the container ports. For example: ***-p \<HOST_PORT\>:\<CONTAINER_PORT\>***

Next, run the ***podman ps*** command to verify that your container is started:

```bash
podman ps
```

Make sure that your container is running and show ***Up*** as their status:

```
CONTAINER ID    IMAGE                                              COMMAND                CREATED          STATUS          PORTS                                        NAMES
2b584282e0f5    localhost/liberty-deepdive-inventory:1.0-SNAPSHOT  /opt/ol/wlp/bin/s...   8 seconds ago    Up 8 second     0.0.0.0:9080->9080/tcp   inventory
```

If a problem occurs and your container exit prematurely, the container don't appear in the container
list that the ***podman ps*** command displays. Instead, your container appear with an ***Exited*** status when you run the ***podman ps -a*** command. Run the ***podman logs inventory*** command to view the container logs for any potential problems. Run the ***podman stats inventory*** command to display a live stream of usage statistics for your container. You can also double-check that your ***Containerfile*** file is correct. When you find the cause of the issues, remove the faulty container with the ***podman rm inventory*** command. Rebuild your image, and start the container again.

Now, you can access the application by the ***http\://localhost:9080/inventory/api/systems*** URL. Alternatively, for the updated OpenAPI UI, use the following URL ***http\://localhost:9080/openapi/ui/.***

When you’re finished trying out the application, run the following commands to stop the container:

```bash
podman stop inventory
podman rm inventory
```

To learn how to optimize the image size, check out the [Containerizing microservices with Podman guide](https://openliberty.io/guides/containerize-podman.html#optimizing-the-image-size).


::page{title="Support Licensing"}

Open Liberty is open source under the Eclipse Public License v1 so there is no fee to use it in production. Community support is available at StackOverflow, Gitter, or the mail list, and bugs can be raised in [GitHub](https://github.com/openliberty/open-liberty). Commercial support is available for Open Liberty from IBM. For more information, see the [IBM Marketplace](https://www.ibm.com/uk-en/marketplace/elite-support-for-open-liberty). The WebSphere Liberty product is built on Open Liberty. No migration is required to use WebSphere Liberty, you simply point to WebSphere Liberty in your build. WebSphere Liberty users get support for the packaged Open Liberty function.

WebSphere Liberty is also available in [Maven Central](https://search.maven.org/search?q=g:com.ibm.websphere.appserver.runtime).

You can use WebSphere Liberty for development even without purchasing it. However, if you have production entitlement, you can easily change to use it with the following steps.

In the ***build.gradle***, add the ***liberty*** element as the following:

```
liberty {
    runtime = [ 'group':'com.ibm.websphere.appserver.runtime',
                'name':'wlp-kernel']
}
```

Rebuild and restart the ***inventory*** service by dev mode:

```
./gradlew clean
./gradlew libertyDev
```

In the ***Containerfile***, replace the Liberty image at the ***FROM*** statement with ***websphere-liberty*** as shown in the following example:
```
FROM icr.io/appcafe/websphere-liberty:full-java11-openj9-ubi

ARG VERSION=1.0
ARG REVISION=SNAPSHOT
...
```

::page{title="Summary"}

### Nice Work!

You just completed a hands-on deep dive on Liberty!



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-liberty-deep-dive-gradle*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-liberty-deep-dive-gradle
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=A%20Technical%20Deep%20Dive%20on%20Liberty&guide-id=cloud-hosted-guide-liberty-deep-dive-gradle)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-liberty-deep-dive-gradle/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-liberty-deep-dive-gradle/pulls)



### Where to next?

* [Documenting RESTful APIs](https://openliberty.io/guides/microprofile-openapi.html)
* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Configuring microservices](https://openliberty.io/guides/microprofile-config.html)
* [Accessing and persisting data in microservices using Java Persistence API (JPA)](https://openliberty.io/guides/jpa-intro.html)
* [Securing microservices with JSON Web Tokens](https://openliberty.io/guides/microprofile-jwt.html)
* [Adding health reports to microservices](https://openliberty.io/guides/microprofile-health.html)
* [Providing metrics from a microservice](https://openliberty.io/guides/microprofile-metrics.html)
* [Containerizing microservices](https://openliberty.io/guides/containerize.html)
* [Deploying a microservice to Kubernetes using Open Liberty Operator](https://openliberty.io/guides/openliberty-operator-intro.html)
* [Configuring microservices running in Kubernetes](https://openliberty.io/guides/kubernetes-microprofile-config.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
