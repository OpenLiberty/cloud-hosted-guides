---
markdown-version: v1
title: instructions
branch: lab-5932-instruction
version-history-start-date: 2023-04-14T18:24:15Z
tool-type: theia
---
::page{title="Welcome to the A Technical Deep Dive on Liberty guide!"}

Liberty is a cloud-optimized Java runtime that is fast to start up with a low memory footprint and a [dev mode](https://openliberty.io/docs/latest/development-mode.html), for quick iteration. With Liberty, adopting the latest open cloud-native Java APIs, like MicroProfile and Jakarta EE, is as simple as adding features to your Liberty configuration. The Liberty zero migration architecture lets you focus on what's important and not the APIs changing under you.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You will learn how to build a RESTful microservice on Liberty with Jakarta EE and MicroProfile. You will use Maven throughout this exercise to build the microservice and to interact with the running Liberty instance. Then, you’ll build a container image for the microservice and deploy it to Kubernetes in a Liberty Docker container. You will also learn how to secure the REST endpoints and use JSON Web Tokens to communicate with the provided ***system*** secured microservice.

The microservice that you’ll work with is called ***inventory***. The ***inventory*** microservice persists data into a PostgreSQL database. 

![Inventory microservice](https://raw.githubusercontent.com/OpenLiberty/guide-liberty-deep-dive/prod/assets/inventory.png)


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

Clone the [Git repository](https://github.com/OpenLiberty/guide-liberty-deep-dive.git):

```bash
git clone https://github.com/openliberty/guide-liberty-deep-dive.git
cd guide-liberty-deep-dive
```

The ***start*** directory is an empty directory where you will build the ***inventory*** service.

The ***finish*** directory contains the finished projects of different modules that you will build.


In this IBM Cloud environment, you need to change the user home to ***/home/project*** by running the following command:
```bash
sudo usermod -d /home/project theia
```

::page{title="Getting started with Liberty and REST"}

Liberty now offers an easier way to get started with developing your application: the Open Liberty Starter. This tool provides a simple and quick way to get the necessary files to start building an application on Liberty. Through this tool, you can specify your application and project name. You can also choose a build tool from either Maven or Gradle, and pick the Java SE, Jakarta EE, and MicroProfile versions for your application.

In this workshop, the Open Liberty Starter is used to create the starting point of the application. Maven is used as the selected build tool and the application uses of Jakarta EE 10 and MicroProfile 6.

To get started with this tool, see the Getting Started page: [https://openliberty.io/start/](https://openliberty.io/start/)

On that page, enter the following properties in the **Create a starter application** wizard.

* Under Group specify: ***io.openliberty.deepdive***
* Under Artifact specify: ***inventory***
* Under Build Tool select: ***Maven***
* Under Java SE Version select: ***17***
* Under Java EE/Jakarta EE Version select: ***10.0***
* Under MicroProfile Version select: ***6.0***


In this Skills Network environment, instead of manually downloading and extracting the project, run the following commands:
```bash
cd /home/project/guide-liberty-deep-dive/start
curl -o inventory.zip 'https://start.openliberty.io/api/start?a=inventory&b=maven&e=10.0&g=io.openliberty.deepdive&j=17&m=6.0'
unzip inventory.zip -d inventory
```

After getting the ***inventory*** project, switch the workspace to the ***/home/project/guide-liberty-deep-dive/start/inventory*** directory.
> - Select **File** > **Close Workspace** from the menu of the IDE.
>   - Click the OK button to confirm to close.
>   - Wait for the IDE to refresh.
> - Select **File** > **Open...** from the menu of the IDE.
>   - In the **Open** window, select the ***home*** directory from the top dropdown list, and then select the ***/home/project/guide-liberty-deep-dive/start/inventory*** directory and click the **Open** button.
>   - Wait for the IDE to refresh.
>   - Click **Yes** to trust the workspace.

### Building the application

This application is configured to be built with Maven. Every Maven-configured project contains a ***pom.xml*** file that defines the project configuration, dependencies, and plug-ins.


Your ***pom.xml*** file is located in the ***start/inventory*** directory and is configured to include the ***liberty-maven-plugin***. Using the plug-in, you can install applications into Liberty and manage the associated Liberty instances.

To begin, open a command-line session and navigate to your application directory. 


```bash
cd /home/project/guide-liberty-deep-dive/start/inventory
```

Build and deploy the ***inventory*** microservice to Liberty by running the Maven ***liberty:run*** goal:

```bash
mvn liberty:run
```

The ***mvn*** command initiates a Maven build, during which the target directory is created to store all build-related files.

The ***liberty:run*** argument specifies the Liberty ***run*** goal, which starts a Liberty instance in the foreground. As part of this phase, a Liberty runtime is downloaded and installed into the ***target/liberty/wlp*** directory. Additionally, a Liberty instance is created and configured in the ***target/liberty/wlp/usr/servers/defaultServer*** directory, and the application is installed into that Liberty instance by using [loose config](https://www.ibm.com/support/knowledgecenter/en/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/rwlp_loose_applications.html).

For more information about the Liberty Maven plug-in, see its [GitHub repository](https://github.com/WASdev/ci.maven).

While the Liberty instance starts up, various messages display in your command-line session. Wait for the following message, which indicates that the instance startup is complete:

```
[INFO] [AUDIT] CWWKF0011I: The server defaultServer is ready to run a smarter planet.
```

When you need to stop the Liberty instance, press `Ctrl+C` in the command-line session where you ran the Liberty.


### Starting and stopping the Liberty in the background

Although you can start and stop the Liberty instance in the foreground by using the Maven ***liberty:run*** goal, you can also start and stop the instance in the background with the Maven ***liberty:start*** and ***liberty:stop*** goals:

```bash
mvn liberty:start
mvn liberty:stop
```


### Updating the Liberty configuration without restarting the instance

The Liberty Maven plug-in includes a ***dev*** goal that listens for any changes in the project, including application source code or configuration. The Liberty instance automatically reloads the configuration without restarting. This goal allows for quicker turnarounds and an improved developer experience.

If the Liberty instance is running, stop it and restart it in dev mode by running the ***liberty:dev*** goal in the ***start/inventory*** directory:

```bash
mvn liberty:dev
```

After you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode automatically picks up changes that you make to your application and allows you to run tests by pressing the ***enter/return*** key in the active command-line session. When you’re working on your application, rather than rerunning Maven commands, press the ***enter/return*** key to verify your change.

### Developing a RESTful microservice

Now that a basic Liberty application is running, the next step is to create the additional application and resource classes that the application needs. Within these classes, you use Jakarta REST and other MicroProfile and Jakarta APIs.

Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE. Go to the ***start/inventory*** directory.
```bash
cd /home/project/guide-liberty-deep-dive/start/inventory
```

Create the ***Inventory*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/Inventory.java
```


> Then, to open the Inventory.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/Inventory.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/Inventory.java"}



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
mkdir /home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/model
```

Create the ***SystemData*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/model/SystemData.java
```


> Then, to open the SystemData.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/model/SystemData.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/model/SystemData.java"}



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
touch /home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java
```


> Then, to open the SystemResource.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



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

Because you started the Liberty in dev mode at the beginning of this exercise, all the changes were automatically picked up.

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



The MicroProfile OpenAPI API is included in the ***microProfile*** dependency that is specified in your ***pom.xml*** file. The ***microProfile*** feature that includes the ***mpOpenAPI*** feature is also enabled in the ***server.xml*** configuration file.

### Generating the OpenAPI document

Because the Jakarta RESTful Web Services framework handles basic API generation for Jakarta RESTful Web Services annotations, a skeleton OpenAPI tree can be generated from the existing inventory service. You can use this tree as a starting point and augment it with annotations and code to produce a complete OpenAPI document.

To see the generated OpenAPI tree, you can either visit the ***http\://localhost:9080/openapi*** URL or visit the ***http\://localhost:9080/openapi/ui*** URL for a more interactive view of the APIs. Click the ***interactive UI*** link on the welcome page. Within this UI, you can view each of the endpoints that are available in your application and any schemas. Each endpoint is color coordinated to easily identify the type of each request (for example GET, POST, PUT, DELETE, etc.). Clicking each endpoint within this UI enables you to view further details of each endpoint's parameters and responses. This UI is used for the remainder of this workshop to view and test the application endpoints.


### Augmenting the existing Jakarta RESTful Web Services annotations with OpenAPI annotations

Because all Jakarta RESTful Web Services annotations are processed by default, you can augment the existing code with OpenAPI annotations without needing to rewrite portions of the OpenAPI document that are already covered by the Jakarta RESTful Web Services framework.

Replace the ***SystemResources*** class.

> To open the SystemResource.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



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
            required = true, example = "17",
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
            required = true, example = "17",
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

Because the Liberty was started in dev mode at the beginning of this exercise, your changes were automatically picked up. Go to the ***http\://localhost:9080/openapi*** URL to see the updated endpoint descriptions. The endpoints at which your REST methods are served now more meaningful:

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
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/model/SystemData.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/model/SystemData.java"}



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

Next, you can externalize your Liberty configuration and inject configuration for your microservice by using MicroProfile Config.


### Enabling configurable ports and context root

So far, you used hardcoded values to set the HTTP and HTTPS ports and the context root for the Liberty. These configurations can be externalized so you can easily change their values when you want to deploy your microservice by different ports and context root.

Replace the Liberty ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="inventory">

    <!-- Enable features -->
    <featureManager>
        <feature>jakartaee-10.0</feature>
        <feature>microProfile-6.0</feature>
    </featureManager>

    <variable name="http.port" defaultValue="9080" />
    <variable name="https.port" defaultValue="9443" />
    <variable name="context.root" defaultValue="/inventory" />

    <!-- To access this server from a remote client,
         add a host attribute to the following element, e.g. host="*" -->
    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="${http.port}" 
                  httpsPort="${https.port}" />
    
    <!-- Automatically expand WAR files and EAR files -->
    <applicationManager autoExpand="true"/>

    <!-- Configures the application on a specified context root -->
    <webApplication contextRoot="${context.root}" 
                    location="inventory.war" /> 

    <!-- Default SSL configuration enables trust for default certificates from the Java runtime -->
    <ssl id="defaultSSLConfig" trustDefaultCerts="true" />
</server>
```




Add variables for the ***HTTP*** port, ***HTTPS*** port, and the ***context root*** to the ***server.xml*** configuration file. Change the ***httpEndpoint*** element to reflect the new ***http.port*** and ***http.port*** variables and change the ***contextRoot*** to use the new ***context.root*** variable too.

Replace the ***pom.xml*** file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/pom.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/pom.xml"}



```xml
<?xml version="1.0" encoding="UTF-8" ?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.deepdive</groupId>
    <artifactId>inventory</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <liberty.var.http.port>9081</liberty.var.http.port>
        <liberty.var.https.port>9445</liberty.var.https.port>
        <liberty.var.context.root>/trial</liberty.var.context.root>
    </properties>

    <dependencies>
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
    </dependencies>

    <build>
        <finalName>inventory</finalName>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-war-plugin</artifactId>
                    <version>3.3.2</version>
                </plugin>
                <plugin>
                    <groupId>io.openliberty.tools</groupId>
                    <artifactId>liberty-maven-plugin</artifactId>
                    <version>3.10</version>
                </plugin>
            </plugins>
        </pluginManagement>
        <plugins>
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```




Add properties for the ***HTTP*** port, ***HTTPS*** port, and the ***context root*** to the ***pom.xml*** file. 

* ***liberty.var.http.port*** to ***9081***
* ***liberty.var.https.port*** to ***9445***
* ***liberty.var.context.root*** to ***/trial***.

Because you are using dev mode, these changes are automatically picked up by the Liberty instance.


Now, you can access the application by running the following command:
```bash
curl http://localhost:9081/trial/api/systems
```

Alternatively, for the updated OpenAPI UI, click the following button to visit ***/openapi/ui*** endpoint:

::startApplication{port="9081" display="external" name="Visit OpenAPI UI" route="/openapi/ui"}

Replace the ***pom.xml*** file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/pom.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/pom.xml"}



```xml
<?xml version="1.0" encoding="UTF-8" ?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.deepdive</groupId>
    <artifactId>inventory</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <liberty.var.http.port>9080</liberty.var.http.port>
        <liberty.var.https.port>9443</liberty.var.https.port>
        <liberty.var.context.root>/inventory</liberty.var.context.root>
    </properties>

    <dependencies>
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
    </dependencies>

    <build>
        <finalName>inventory</finalName>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-war-plugin</artifactId>
                    <version>3.3.2</version>
                </plugin>
                <plugin>
                    <groupId>io.openliberty.tools</groupId>
                    <artifactId>liberty-maven-plugin</artifactId>
                    <version>3.10</version>
                </plugin>
            </plugins>
        </pluginManagement>
        <plugins>
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```




When you are finished trying out changing this configuration, change the variables back to their original values.

* update ***liberty.var.http.port*** to ***9080***
* update ***liberty.var.https.port*** to ***9443***
* update ***liberty.var.context.root*** to ***/inventory***.


### Injecting static configuration

You can now explore how to use MicroProfile's Config API to inject static configuration into your microservice.

The MicroProfile Config API is included in the MicroProfile dependency that is specified in your ***pom.xml*** file. Look for the dependency with the ***microprofile*** artifact ID. This dependency provides a library that allows the use of the MicroProfile Config API. The ***microProfile*** feature is also enabled in the ***server.xml*** configuration file.


First, you need to edit the ***SystemResource*** class to inject static configuration into the ***CLIENT_PORT*** variable.

Replace the ***SystemResource*** class.

> To open the SystemResource.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



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
            required = true, example = "17",
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
            required = true, example = "17",
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
mkdir -p /home/project/guide-liberty-deep-dive/start/inventory/src/main/resources/META-INF
```

Create the ***microprofile-config.properties*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive/start/inventory/src/main/resources/META-INF/microprofile-config.properties
```


> Then, to open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/resources/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/resources/META-INF/microprofile-config.properties"}



```
config_ordinal=100

client.https.port=5555
```



Using the ***config_ordinal*** variable in this properties file, you can set the ordinal of this file and thus other configuration sources.

The ***client.https.port*** variable enables the client port to be overwritten.

Revisit the OpenAPI UI ***http\://localhost:9080/openapi/ui*** to view these changes. Open the ***/api/systems/client/{hostname}*** endpoint and run it within the UI to view the ***CLIENT_PORT*** value.

::startApplication{port="9080" display="external" name="Visit OpenAPI UI" route="/openapi/ui"}

You can learn more about MicroProfile Config from the [Configuring microservices guide](https://openliberty.io/guides/microprofile-config.html).

::page{title="Persisting data"}

Next, you’ll persist the system data into the PostgreSQL database by using  the [Jakarta Persistence API](https://jakarta.ee/specifications/persistence) (JPA).

Navigate to your application directory. 


```bash
cd /home/project/guide-liberty-deep-dive/start/inventory
```

### Defining a JPA entity class

To store Java objects in a database, you must define a JPA entity class. A JPA entity is a Java object whose nontransient and nonstatic fields are persisted to the database. Any POJO class can be designated as a JPA entity. However, the class must be annotated with the ***@Entity*** annotation, must not be declared final, and must have a public or protected nonargument constructor. JPA maps an entity type to a database table and persisted instances will be represented as rows in the table.

The ***SystemData*** class is a data model that represents systems in the ***inventory*** microservice. Annotate it with JPA annotations.

Replace the ***SystemData*** class.

> To open the SystemData.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/model/SystemData.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/model/SystemData.java"}



```java
package io.openliberty.deepdive.rest.model;

import java.io.Serializable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Schema(name = "SystemData",
        description = "POJO that represents a single inventory entry.")
@Entity
@Table(name = "SystemData")
@NamedQuery(name = "SystemData.findAll", query = "SELECT e FROM SystemData e")
@NamedQuery(name = "SystemData.findSystem",
            query = "SELECT e FROM SystemData e WHERE e.hostname = :hostname")
public class SystemData implements Serializable {
    private static final long serialVersionUID = 1L;

    @SequenceGenerator(name = "SEQ",
                       sequenceName = "systemData_id_seq",
                       allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "SEQ")
    @Id
    @Column(name = "id")
    private int id;

    @Schema(required = true)
    @Column(name = "hostname")
    private String hostname;

    @Column(name = "osName")
    private String osName;
    @Column(name = "javaVersion")
    private String javaVersion;
    @Column(name = "heapSize")
    private Long heapSize;

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
    public int hashCode() {
        return hostname.hashCode();
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



The following table breaks down the new annotations:

| *Annotation* | *Description*
| ---| ---
| ***@Entity*** | Declares the class as an entity.
| ***@Table*** | Specifies details of the table such as name. 
| ***@NamedQuery*** | Specifies a predefined database query that is run by an ***EntityManager*** instance.
| ***@Id*** | Declares the primary key of the entity.
| ***@GeneratedValue*** | Specifies the strategy that is used for generating the value of the primary key. The ***strategy = GenerationType.IDENTITY*** code indicates that the database automatically increments the ***inventoryid*** upon inserting it into the database.
| ***@Column*** | Specifies that the field is mapped to a column in the database table. The ***name*** attribute is optional and indicates the name of the column in the table.

### Performing CRUD operations using JPA

The create, retrieve, update, and delete (CRUD) operations are defined in the Inventory. To perform these operations by using JPA, you need to update the ***Inventory*** class. 

Replace the ***Inventory*** class.

> To open the Inventory.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/Inventory.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/Inventory.java"}



```java
package io.openliberty.deepdive.rest;

import java.util.List;

import io.openliberty.deepdive.rest.model.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class Inventory {

    @PersistenceContext(name = "jpa-unit")
    private EntityManager em;

    public List<SystemData> getSystems() {
        return em.createNamedQuery("SystemData.findAll", SystemData.class)
                 .getResultList();
    }

    public SystemData getSystem(String hostname) {
        List<SystemData> systems =
            em.createNamedQuery("SystemData.findSystem", SystemData.class)
              .setParameter("hostname", hostname)
              .getResultList();
        return systems == null || systems.isEmpty() ? null : systems.get(0);
    }

    public void add(String hostname, String osName, String javaVersion, Long heapSize) {
        em.persist(new SystemData(hostname, osName, javaVersion, heapSize));
    }

    public void update(SystemData s) {
        em.merge(s);
    }

    public void removeSystem(SystemData s) {
        em.remove(s);
    }

}
```



To use the entity manager at run time, inject it into your CDI bean through the ***@PersistenceContext*** annotation. The entity manager interacts with the persistence context. Every ***EntityManager*** instance is associated with a persistence context. The persistence context manages a set of entities and is aware of the different states that an entity can have. The persistence context synchronizes with the database when a transaction commits.


The ***Inventory*** class has a method for each CRUD operation, so let's break them down:

* The ***add()*** method persists an instance of the ***SystemData*** entity class to the data store by calling the ***persist()*** method on an ***EntityManager*** instance. The entity instance becomes managed and changes to it are tracked by the entity manager.

* The ***getSystems()*** method demonstrates a way to retrieve system objects from the database. This method returns a list of instances of the ***SystemData*** entity class by using the ***SystemData.findAll*** query that is specified in the ***@NamedQuery*** annotation on the ***SystemData*** class. Similarly, the ***getSystem()*** method uses the ***SystemData.findSystem*** named query to find a system with the given hostname. 

* The ***update()*** method creates a managed instance of a detached entity instance. The entity manager automatically tracks all managed entity objects in its persistence context for changes and synchronizes them with the database. However, if an entity becomes detached, you must merge that entity into the persistence context by calling the ***merge()*** method so that changes to loaded fields of the detached entity are tracked.

* The ***removeSystem()*** method removes an instance of the ***SystemData*** entity class from the database by calling the ***remove()*** method on an ***EntityManager*** instance. The state of the entity is changed to removed and is removed from the database upon transaction commit. 

Declare the endpoints with transaction management. 

Replace the ***SystemResource*** class.

> To open the SystemResource.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



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
import jakarta.transaction.Transactional;
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
    @Transactional
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
            required = true, example = "17",
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
    @Transactional
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
            required = true, example = "17",
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
    @Transactional
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
    @Transactional
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



The ***@Transactional*** annotation is used in the ***POST***, ***PUT***, and ***DELETE*** endpoints of the ***SystemResource*** class to declaratively control the transaction boundaries on the ***inventory*** CDI bean. This configuration ensures that the methods run within the boundaries of an active global transaction, and therefore you don't need to explicitly begin, commit, or rollback transactions. At the end of the transactional method invocation, the transaction commits and the persistence context flushes any changes to the Event entity instances that it is managing to the database.

### Configuring JPA

The ***persistence.xml*** file is a configuration file that defines a persistence unit. The
persistence unit specifies configuration information for the entity manager.

Create the configuration file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive/start/inventory/src/main/resources/META-INF/persistence.xml
```


> Then, to open the persistence.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/resources/META-INF/persistence.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/resources/META-INF/persistence.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.2"
    xmlns="http://xmlns.jcp.org/xml/ns/persistence" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence 
                        http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
    <persistence-unit name="jpa-unit" transaction-type="JTA">
      <jta-data-source>jdbc/postgresql</jta-data-source>
      <exclude-unlisted-classes>false</exclude-unlisted-classes>
      <properties>
        <property name="jakarta.persistence.schema-generation.database.action"
                  value="create"/>
        <property name="jakarta.persistence.schema-generation.scripts.action"
                  value="create"/>
        <property name="jakarta.persistence.schema-generation.scripts.create-target"
                  value="createDDL.ddl"/>
      </properties>
    </persistence-unit>
</persistence>
```



The persistence unit is defined by the ***persistence-unit*** XML element. The ***name*** attribute is required. This attribute identifies the persistent unit when you use the ***@PersistenceContext*** annotation to inject the entity manager later in this exercise. The ***transaction-type="JTA"*** attribute specifies to use Java Transaction API (JTA) transaction management. When you use a container-managed entity manager, you must use JTA transactions. 

A JTA transaction type requires a JTA data source to be provided. The ***jta-data-source*** element specifies the Java Naming and Directory Interface (JNDI) name of the data source that is used. 


Configure the ***jdbc/postgresql*** data source in the Liberty ***server.xml*** configuration file.

Replace the Liberty ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="inventory">

    <featureManager>
        <feature>jakartaee-10.0</feature>
        <feature>microProfile-6.0</feature>
    </featureManager>

    <variable name="http.port" defaultValue="9080" />
    <variable name="https.port" defaultValue="9443" />
    <variable name="context.root" defaultValue="/inventory" />
    <variable name="postgres/hostname" defaultValue="localhost" />
    <variable name="postgres/portnum" defaultValue="5432" />

    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="${http.port}" 
                  httpsPort="${https.port}" />

    <!-- Automatically expand WAR files and EAR files -->
    <applicationManager autoExpand="true"/>

    <!-- Configures the application on a specified context root -->
    <webApplication contextRoot="${context.root}" 
                    location="inventory.war" /> 

    <!-- Default SSL configuration enables trust for default certificates from the Java runtime -->
    <ssl id="defaultSSLConfig" trustDefaultCerts="true" />
    
    <library id="postgresql-library">
        <fileset dir="${shared.resource.dir}/" includes="*.jar" />
    </library>

    <!-- Datasource Configuration -->
    <dataSource id="DefaultDataSource" jndiName="jdbc/postgresql">
        <jdbcDriver libraryRef="postgresql-library" />
        <properties.postgresql databaseName="admin"
                               serverName="localhost"
                               portNumber="5432"
                               user="admin"
                               password="adminpwd"/>
    </dataSource>
</server>
```



The ***library*** element tells the Liberty where to find the PostgreSQL library. The ***dataSource*** element points to where the Java Database Connectivity (JDBC) driver connects, along with some database vendor-specific properties. For more information, see the [Data source configuration](https://www.openliberty.io/docs/latest/relational-database-connections-JDBC.html#_data_source_configuration) and [dataSource element](https://www.openliberty.io/docs/latest/reference/config/dataSource.html) documentation.

To use a PostgreSQL database, you need to download its library and store it to the Liberty shared resources directory. Configure the Liberty Maven plug-in in the ***pom.xml*** file.

Replace the ***pom.xml*** configuration file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/pom.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/pom.xml"}



```xml
<?xml version="1.0" encoding="UTF-8" ?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.deepdive</groupId>
    <artifactId>inventory</artifactId>
    <packaging>war</packaging>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <liberty.var.http.port>9080</liberty.var.http.port>
        <liberty.var.https.port>9443</liberty.var.https.port>
        <liberty.var.context.root>/inventory</liberty.var.context.root>
    </properties>

    <dependencies>
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
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.6.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>inventory</finalName>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-war-plugin</artifactId>
                    <version>3.3.2</version>
                </plugin>
                <plugin>
                    <groupId>io.openliberty.tools</groupId>
                    <artifactId>liberty-maven-plugin</artifactId>
                    <version>3.10</version>
                </plugin>
            </plugins>
        </pluginManagement>
        <plugins>
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <configuration>
                    <copyDependencies>
                        <dependencyGroup>
                            <location>${project.build.directory}/liberty/wlp/usr/shared/resources</location>
                            <dependency>
                                <groupId>org.postgresql</groupId>
                                <artifactId>postgresql</artifactId>
                                <version>42.7.1</version>
                            </dependency>
                        </dependencyGroup>
                    </copyDependencies>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```




The ***postgresql*** dependency ensures that Maven downloads the PostgreSQL library to local project. The ***copyDependencies*** configuration tells the Liberty Maven plug-in to copy the library to the Liberty shared resources directory.


### Starting PostgreSQL database ###

Use Docker to run an instance of the PostgreSQL database for a fast installation and setup.

A container file is provided for you. First, navigate to the ***finish/postgres*** directory. Then, run the following commands to use the ***Dockerfile*** to build the image, run the image in a Docker container, and map ***5432*** port from the container to your machine:


```bash
cd /home/project/guide-liberty-deep-dive/finish/postgres
docker build -t postgres-sample .
docker run --name postgres-container -p 5432:5432 -d postgres-sample
```

### Running the application ###


In your dev mode console for the ***inventory*** microservice, type `r` and press ***enter/return*** key to restart the Liberty instance.

After you see the following message, your Liberty instance is ready in dev mode again:

```
**************************************************************
*    Liberty is running in dev mode.
```

First, make a POST request to the ***/api/systems/*** endpoint by the following command. The POST request adds a system with the specified values to the database.

```bash
curl -X POST 'http://localhost:9080/inventory/api/systems?heapSize=1048576&hostname=localhost&javaVersion=9&osName=linux'
```

Next, make a GET request to the ***/api/systems*** endpoint by the following command. The GET request returns all systems from the database.

```bash
curl -s 'http://localhost:9080/inventory/api/systems' | jq
```

Next, make a PUT request to the ***/api/systems/{hostname}*** endpoint with the same value for the ***hostname*** path as in the previous step, and different values to the ***heapSize***, ***javaVersion***, and ***osName*** parameters. The PUT request updates the system with the specified values. 

```bash
curl -X PUT 'http://localhost:9080/inventory/api/systems/localhost?heapSize=2097152&javaVersion=17&osName=linux'
```

To see the updated system, make a GET request to the ***/api/systems/{hostname}*** endpoint with the same value for the ***hostname*** path as in the previous step. The GET request returns the system from the database.

```bash
curl -s 'http://localhost:9080/inventory/api/systems/localhost' | jq
```

Next, make a DELETE request to the ***/api/systems/{hostname}*** endpoint. The DELETE request removes the system from the database.

```bash
curl -X DELETE 'http://localhost:9080/inventory/api/systems/localhost'
```

Run the GET request again to see that the system no longer exists in the database. 
```bash
curl 'http://localhost:9080/inventory/api/systems'
```

::page{title="Securing RESTful APIs"}

Now you can secure your RESTful APIs. Navigate to your application directory. 


```bash
cd /home/project/guide-liberty-deep-dive/start/inventory
```

Begin by adding some users and user groups to your Liberty ***server.xml*** configuration file.

Replace the Liberty ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="inventory">

    <featureManager>
        <feature>jakartaee-10.0</feature>
        <feature>microProfile-6.0</feature>
    </featureManager>

    <variable name="http.port" defaultValue="9080" />
    <variable name="https.port" defaultValue="9443" />
    <variable name="context.root" defaultValue="/inventory" />
    <variable name="postgres/hostname" defaultValue="localhost" />
    <variable name="postgres/portnum" defaultValue="5432" />

    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="${http.port}" 
                  httpsPort="${https.port}" />

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
    <webApplication contextRoot="${context.root}"
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

    <library id="postgresql-library">
        <fileset dir="${shared.resource.dir}/" includes="*.jar" />
    </library>

    <dataSource id="DefaultDataSource" jndiName="jdbc/postgresql">
        <jdbcDriver libraryRef="postgresql-library" />
        <properties.postgresql databaseName="admin"
                               serverName="localhost"
                               portNumber="5432"
                               user="admin"
                               password="adminpwd"/>
    </dataSource>
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
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



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
import jakarta.transaction.Transactional;
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
    @Transactional
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
            required = true, example = "17",
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
    @Transactional
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
            required = true, example = "17",
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
    @Transactional
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
    @Transactional
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
curl -X POST 'http://localhost:9080/inventory/api/systems?hostname=localhost&osName=mac&javaVersion=17&heapSize=1'
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
[{"heapSize":1,"hostname":"localhost","javaVersion":"17","osName":"mac","id":23}]
```

Now try calling your secure PUT endpoint to update the system that you just added by the following curl command:

```bash
curl -k --user alice:alicepwd -X PUT 'http://localhost:9080/inventory/api/systems/localhost?heapSize=2097152&javaVersion=17&osName=linux'
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
curl -k --user alice:alicepwd -X DELETE 'https://localhost:9443/inventory/api/systems/localhost'
```

As ***alice*** is part of the ***user*** group, this request cannot work. In your dev mode console, you can expect the following output:

```
jakarta.ws.rs.ForbiddenException: Unauthorized
```

Now attempt to call this endpoint with an authenticated ***admin*** user that can work correctly. Run the following curl command:

```bash
curl -k --user bob:bobpwd -X DELETE 'https://localhost:9443/inventory/api/systems/localhost'
```

You can expect to see the following response:

```
{ "ok" : "localhost was removed." }
```

This response means that your endpoint is secure. Validate that it works correctly by calling the ***/systems*** endpoint with the following curl command:

```bash
curl 'http://localhost:9080/inventory/api/systems'
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
mkdir /home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/client
```

Create the ***SystemClient*** interface.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/client/SystemClient.java
```


> Then, to open the SystemClient.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/client/SystemClient.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/client/SystemClient.java"}



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
touch /home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/client/UnknownUriException.java
```


> Then, to open the UnknownUriException.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/client/UnknownUriException.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/client/UnknownUriException.java"}



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
touch /home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/client/UnknownUriExceptionMapper.java
```


> Then, to open the UnknownUriExceptionMapper.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/client/UnknownUriExceptionMapper.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/client/UnknownUriExceptionMapper.java"}



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
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



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
import jakarta.transaction.Transactional;
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
    @Transactional
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
            required = true, example = "17",
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
    @Transactional
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
            required = true, example = "17",
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
    @Transactional
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
    @Transactional
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

Next, add the JSON Web Token (Single Sign On) feature to the Liberty ***server.xml*** configuration file for the ***inventory*** service.

Replace the Liberty ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="inventory">

    <featureManager>
        <feature>jakartaee-10.0</feature>
        <feature>microProfile-6.0</feature>
        <feature>jwtSso-1.0</feature>
    </featureManager>

    <variable name="http.port" defaultValue="9080" />
    <variable name="https.port" defaultValue="9443" />
    <variable name="context.root" defaultValue="/inventory" />
    <variable name="postgres/hostname" defaultValue="localhost" />
    <variable name="postgres/portnum" defaultValue="5432" />

    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="${http.port}" 
                  httpsPort="${https.port}" />

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
    <webApplication contextRoot="${context.root}"
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
    <ssl id="guideSSLConfig" keyStoreRef="guideKeyStore" trustDefaultCerts="true" />
    <sslDefault sslRef="guideSSLConfig" />

    <keyStore id="guideKeyStore"
              password="secret"
              location="${server.config.dir}/resources/security/key.p12" />

    <jwtSso jwtBuilderRef="jwtInventoryBuilder"/>
    <jwtBuilder id="jwtInventoryBuilder" 
                issuer="http://openliberty.io" 
                audiences="systemService"
                expiry="24h"/>
    <mpJwt audiences="systemService" 
           groupNameAttribute="groups" 
           id="myMpJwt"
           sslRef="guideSSLConfig"
           issuer="http://openliberty.io"/>

    <library id="postgresql-library">
        <fileset dir="${shared.resource.dir}/" includes="*.jar" />
    </library>

    <dataSource id="DefaultDataSource" jndiName="jdbc/postgresql">
        <jdbcDriver libraryRef="postgresql-library" />
        <properties.postgresql databaseName="admin"
                               serverName="localhost"
                               portNumber="5432"
                               user="admin"
                               password="adminpwd"/>
    </dataSource>
</server>
```




The ***jwtSso*** feature adds the libraries that are required for JWT SSO implementation. Configure the ***jwtSso*** feature by adding the ***jwtBuilder*** configuration to your ***server.xml*** file. Also, configure the MicroProfile ***JWT*** with the ***audiences*** and ***issuer*** properties that match the ***microprofile-config.properties*** defined at the ***system/src/main/webapp/META-INF*** directory under the ***system*** project. For more information, see the [JSON Web Token Single Sign-On feature](https://www.openliberty.io/docs/latest/reference/feature/jwtSso-1.0.html), [jwtSso element](https://www.openliberty.io/docs/latest/reference/config/jwtSso.html), and [jwtBuilder element](https://www.openliberty.io/docs/latest/reference/config/jwtBuilder.html) documentation.

The ***keyStore*** element is used to define the repository of security certificates used for SSL encryption. The ***id*** attribute is a unique configuration ID that is set to ***guideKeyStore***. The ***password*** attribute is used to load the keystore file, and its value can be stored in clear text or encoded form. To learn more about other attributes, see the [keyStore](https://openliberty.io/docs/latest/reference/config/keyStore.html#keyStore.html) attribute documentation. 

To avoid the conflict with the default ssl configuration, define your own ssl configuration by setting the ***id*** attribute to other value, the ***sslDefault*** element, and the ***sslRef*** attribute in the ***mpJwt*** element.

Because the keystore file is not provided at the ***src*** directory, Liberty creates a Public Key Cryptography Standards #12 (PKCS12) keystore file for you by default. This file needs to be replaced, as the ***keyStore*** configuration must be the same in both ***system*** and ***inventory*** microservices. As the configured ***system*** microservice is already provided for you, copy the ***key.p12*** keystore file from the ***system*** microservice to your ***inventory*** service.


```bash
mkdir -p /home/project/guide-liberty-deep-dive/start/inventory/src/main/liberty/config/resources/security
cp /home/project/guide-liberty-deep-dive/finish/system/src/main/liberty/config/resources/security/key.p12 \
   /home/project/guide-liberty-deep-dive/start/inventory/src/main/liberty/config/resources/security/key.p12
```

Now configure the client https port in the ***pom.xml*** configuration file.

Replace the ***pom.xml*** file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/pom.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/pom.xml"}



```xml
<?xml version="1.0" encoding="UTF-8" ?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
	       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.deepdive</groupId>
    <artifactId>inventory</artifactId>
    <packaging>war</packaging>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <liberty.var.http.port>9080</liberty.var.http.port>
        <liberty.var.https.port>9443</liberty.var.https.port>
        <liberty.var.context.root>/inventory</liberty.var.context.root>
        <liberty.var.client.https.port>9444</liberty.var.client.https.port>
    </properties>

    <dependencies>
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
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.1</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>inventory</finalName>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-war-plugin</artifactId>
                    <version>3.3.2</version>
                </plugin>
                <plugin>
                    <groupId>io.openliberty.tools</groupId>
                    <artifactId>liberty-maven-plugin</artifactId>
                    <version>3.10</version>
                </plugin>
            </plugins>
        </pluginManagement>
        <plugins>
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <configuration>
                    <copyDependencies>
                        <dependencyGroup>
                            <location>${project.build.directory}/liberty/wlp/usr/shared/resources</location>
                            <dependency>
                                <groupId>org.postgresql</groupId>
                                <artifactId>postgresql</artifactId>
                                <version>42.7.1</version>
                            </dependency>
                        </dependencyGroup>
                    </copyDependencies>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```



Configure the client https port by setting the ***\<liberty.var.client.https.port\>*** to ***9444***.

In your dev mode console for the ***inventory*** microservice, press `Ctrl+C` to stop the Liberty instance. Then, restart the dev mode of the ***inventory*** microservice.
```bash
mvn liberty:dev
```

After you see the following message, your Liberty instance is ready in dev mode again:

```
**************************************************************
*    Liberty is running in dev mode.
```

### Running the ***/client/{hostname}*** endpoint

Open another command-line session and run the ***system*** microservice from the ***finish*** directory.


```bash
cd /home/project/guide-liberty-deep-dive/finish/system
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
curl -k --user bob:bobpwd -X POST 'https://localhost:9443/inventory/api/systems/client/localhost'
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
    "javaVersion": "17.0.9",
    "osName": "Linux"
  }
]
```

::page{title="Adding health checks"}
Next, you'll use [MicroProfile Health](https://download.eclipse.org/microprofile/microprofile-health-4.0/microprofile-health-spec-4.0.html) to report the health status of the microservice and PostgreSQL database connection.

Navigate to your application directory


```bash
cd /home/project/guide-liberty-deep-dive/start/inventory
```

A health report is generated automatically for all health services that enable MicroProfile Health.

All health services must provide an implementation of the ***HealthCheck*** interface, which is used to verify their health. MicroProfile Health offers health checks for startup, liveness, and readiness.

A startup check allows applications to define startup probes that are used for initial verification of the application before the liveness probe takes over. For example, a startup check might check which applications require additional startup time on their first initialization.

A liveness check allows third-party services to determine whether a microservice is running. If the liveness check fails, the application can be terminated. For example, a liveness check might fail if the application runs out of memory.

A readiness check allows third-party services, such as Kubernetes, to determine whether a microservice is ready to process requests.

Create the ***health*** subdirectory before creating the health check classes.


```bash
mkdir /home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/health
```

Create the ***StartupCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/health/StartupCheck.java
```


> Then, to open the StartupCheck.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/health/StartupCheck.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/health/StartupCheck.java"}



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
touch /home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/health/LivenessCheck.java
```


> Then, to open the LivenessCheck.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/health/LivenessCheck.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/health/LivenessCheck.java"}



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
touch /home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/health/ReadinessCheck.java
```


> Then, to open the ReadinessCheck.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/health/ReadinessCheck.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/health/ReadinessCheck.java"}



```java
package io.openliberty.deepdive.rest.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

import java.io.IOException;
import java.net.Socket;

@Readiness
@ApplicationScoped
public class ReadinessCheck implements HealthCheck {

    @Inject
    @ConfigProperty(name = "postgres/hostname")
    private String host;

    @Inject
    @ConfigProperty(name = "postgres/portnum")
    private int port;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder =
            HealthCheckResponse.named("Readiness Check");

        try {
            Socket socket = new Socket(host, port);
            socket.close();
            responseBuilder.up();
        } catch (Exception e) {
            responseBuilder.down();
        }
        return responseBuilder.build();
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
cd /home/project/guide-liberty-deep-dive/start/inventory
```

Enable the ***bob*** user to access the ***/metrics*** endpoints.

Replace the Liberty ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="inventory">

    <featureManager>
        <feature>jakartaee-10.0</feature>
        <feature>microProfile-6.0</feature>
        <feature>jwtSso-1.0</feature>
    </featureManager>

    <variable name="http.port" defaultValue="9080" />
    <variable name="https.port" defaultValue="9443" />
    <variable name="context.root" defaultValue="/inventory" />
    <variable name="postgres/hostname" defaultValue="localhost" />
    <variable name="postgres/portnum" defaultValue="5432" />

    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="${http.port}" 
                  httpsPort="${https.port}" />

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

    <administrator-role>
        <user>bob</user>
        <group>AuthorizedGroup</group>
    </administrator-role>

    <!-- Configures the application on a specified context root -->
    <webApplication contextRoot="${context.root}"
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
    <ssl id="guideSSLConfig" keyStoreRef="guideKeyStore" trustDefaultCerts="true" />
    <sslDefault sslRef="guideSSLConfig" />

    <keyStore id="guideKeyStore"
              password="secret"
              location="${server.config.dir}/resources/security/key.p12" />
    
    <jwtSso jwtBuilderRef="jwtInventoryBuilder"/>
    <jwtBuilder id="jwtInventoryBuilder" 
                issuer="http://openliberty.io" 
                audiences="systemService"
                expiry="24h"/>
    <mpJwt audiences="systemService" 
           groupNameAttribute="groups" 
           id="myMpJwt" 
           issuer="http://openliberty.io"/>

    <library id="postgresql-library">
        <fileset dir="${shared.resource.dir}/" includes="*.jar" />
    </library>

    <dataSource id="DefaultDataSource" jndiName="jdbc/postgresql">
        <jdbcDriver libraryRef="postgresql-library" />
        <properties.postgresql databaseName="admin"
                               serverName="localhost"
                               portNumber="5432"
                               user="admin"
                               password="adminpwd"/>
    </dataSource>
</server>
```



The ***administrator-role*** configuration authorizes the ***bob*** user as an administrator.

Use annotations that are provided by MicroProfile Metrics to instrument the ***inventory*** microservice to provide application-level metrics data.

Replace the ***SystemResource*** class.

> To open the SystemResource.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/java/io/openliberty/deepdive/rest/SystemResource.java"}



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
import jakarta.transaction.Transactional;
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
    @Transactional
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
            required = true, example = "17",
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
    @Transactional
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
            required = true, example = "17",
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
    @Transactional
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
    @Transactional
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

Additional information about the annotations that MicroProfile metrics provides, relevant metadata fields, and more are available at the [MicroProfile Metrics Annotation Javadoc](https://openliberty.io/docs/latest/reference/javadoc/microprofile-6.0-javadoc.html?package=org/eclipse/microprofile/metrics/annotation/package-frame.html&class=overview-summary.html).


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
  'http://localhost:9080/inventory/api/systems/localhost?heapSize=2097152&javaVersion=17&osName=linux'
```

```bash
curl -s 'http://localhost:9080/inventory/api/systems' | jq
```

MicroProfile Metrics provides 4 different REST endpoints.

* The ***/metrics*** endpoint provides you with all the metrics in text format. 
* The ***/metrics?scope=application*** endpoint provides you with application-specific metrics.
* The ***/metrics?scope=base*** endpoint provides you with metrics that are defined in MicroProfile specifications. Metrics in the base scope are intended to be portable between different MicroProfile-compatible runtimes.
* The ***/metrics?scope=vendor*** endpoint provides you with metrics that are specific to the runtime.


Run the following curl command to see the application metrics that are enabled through MicroProfile Metrics:
```bash
curl -k --user bob:bobpwd https://localhost:9443/metrics?scope=application
```

You can expect to see your application metrics in text format as the following output:

```
# HELP updateSystem_total Number of times updating a system endpoint is called
# TYPE updateSystem_total counter
updateSystem_total{mp_scope="application",} 1.0
# HELP removeSystem_total Number of times removing a system endpoint is called
# TYPE removeSystem_total counter
removeSystem_total{mp_scope="application",} 1.0
# HELP addSystemClient_total Number of times adding a system by client is called
# TYPE addSystemClient_total counter
addSystemClient_total{mp_scope="application",} 0.0
# HELP addSystem_total Number of times adding system endpoint is called
# TYPE addSystem_total counter
addSystem_total{mp_scope="application",} 1.0
```

To see the system metrics, run the following curl command:
```bash
curl -k --user bob:bobpwd https://localhost:9443/metrics\?scope=base
```

To see the vendor metrics, run the following curl command:
```bash
curl -k --user bob:bobpwd https://localhost:9443/metrics\?scope=vendor
```

To review all the metrics, run the following curl command:
```bash
curl -k --user bob:bobpwd https://localhost:9443/metrics
```


::page{title="Building the container "}

Press `Ctrl+C` in the command-line session to stop the ***mvn liberty:dev*** dev mode that you started in the previous section.

Navigate to your application directory:


```bash
cd /home/project/guide-liberty-deep-dive/start/inventory
```

The first step to containerizing your application inside of a Docker container is creating a Dockerfile. A Dockerfile is a collection of instructions for building a Docker image that can then be run as a container. 

Make sure to start your Docker daemon before you proceed.

Replace the ***Dockerfile*** in the ***start/inventory*** directory.

> To open the Dockerfile file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/Dockerfile, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/Dockerfile"}



```
FROM icr.io/appcafe/open-liberty:full-java17-openj9-ubi

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
    target/inventory.war \
    /config/apps

COPY --chown=1001:0 \
    target/liberty/wlp/usr/shared/resources/*.jar \
    /opt/ol/wlp/usr/shared/resources/

USER 1001

RUN configure.sh
```



The ***FROM*** instruction initializes a new build stage and indicates the parent image from which your image is built. In this case, you’re using the ***icr.io/appcafe/open-liberty:full-java17-openj9-ubi*** image that comes with the latest Open Liberty runtime as your parent image.

To help you manage your images, you can label your container images with the ***LABEL*** command. 

The ***COPY*** instructions are structured as ***COPY*** ***[--chown=\<user\>:\<group\>]*** ***\<source\>*** ***\<destination\>***. They copy local files into the specified destination within your Docker image. In this case, the first ***COPY*** instruction copies the Liberty configuration file that is at ***src/main/liberty/config/server.xml*** to the ***/config/*** destination directory. Similarly, the second ***COPY*** instruction copies the ***.war*** file to the ***/config/apps*** destination directory. The third ***COPY*** instruction copies the PostgreSQL library file to the Liberty shared resources directory.


### Developing the application in a container

Make the PostgreSQL database configurable in the Liberty ***server.xml*** configuraton file.

Replace the Liberty ***server.xml*** configuraton file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="inventory">

    <featureManager>
        <feature>jakartaee-10.0</feature>
        <feature>microProfile-6.0</feature>
        <feature>jwtSso-1.0</feature>
    </featureManager>

    <variable name="http.port" defaultValue="9080" />
    <variable name="https.port" defaultValue="9443" />
    <variable name="context.root" defaultValue="/inventory" />
    <variable name="postgres/hostname" defaultValue="localhost" />
    <variable name="postgres/portnum" defaultValue="5432" />
    <variable name="postgres/username" defaultValue="admin" />
    <variable name="postgres/password" defaultValue="adminpwd" />

    <httpEndpoint id="defaultHttpEndpoint"
                  httpPort="${http.port}" 
                  httpsPort="${https.port}" />

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
    <webApplication contextRoot="${context.root}"
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
    <ssl id="guideSSLConfig" keyStoreRef="guideKeyStore" trustDefaultCerts="true" />
    <sslDefault sslRef="guideSSLConfig" />

    <keyStore id="guideKeyStore"
              password="secret"
              location="${server.config.dir}/resources/security/key.p12" />

    <jwtSso jwtBuilderRef="jwtInventoryBuilder"/>
    <jwtBuilder id="jwtInventoryBuilder" 
                issuer="http://openliberty.io" 
                audiences="systemService"
                expiry="24h"/>
    <mpJwt audiences="systemService" 
           groupNameAttribute="groups" 
           id="myMpJwt"
           sslRef="guideSSLConfig"
           issuer="http://openliberty.io"/>

    <library id="postgresql-library">
        <fileset dir="${shared.resource.dir}/" includes="*.jar" />
    </library>

    <dataSource id="DefaultDataSource" jndiName="jdbc/postgresql">
        <jdbcDriver libraryRef="postgresql-library" />
        <properties.postgresql databaseName="admin"
                               serverName="${postgres/hostname}"
                               portNumber="${postgres/portnum}"
                               user="${postgres/username}"
                               password="${postgres/password}"/>
    </dataSource>
</server>
```




Instead of the hard-coded ***serverName***, ***portNumber***, ***user***, and ***password*** values in the ***properties.postgresql*** properties, use ***${postgres/hostname}***, ***${postgres/portnum}***, ***${postgres/username}***, and ***${postgres/password}***, which are defined by the ***variable*** elements.

You can use the Dockerfile to try out your application with the PostGreSQL database by running the ***devc*** goal.

The Open Liberty Maven plug-in includes a ***devc*** goal that simplifies developing your application in a container by starting dev mode with container support. This goal builds a Docker image, mounts the required directories, binds the required ports, and then runs the application inside of a container. Dev mode also listens for any changes in the application source code or configuration and rebuilds the image and restarts the container as necessary.

Retrieve the PostgreSQL container IP address by running the following command:

```bash
docker inspect -f "{{.NetworkSettings.IPAddress }}" postgres-container
```

The command returns the PostgreSQL container IP address:

```
172.17.0.2
```

Build and run the container by running the ***devc*** goal with the PostgreSQL container IP address from the ***start/inventory*** directory. If your PostgreSQL container IP address is not ***172.17.0.2***, replace the command with the right IP address.


```bash
chmod 777 /home/project/guide-liberty-deep-dive/start/inventory/target/liberty/wlp/usr/servers/defaultServer/logs
POSTGRES_IP=`docker inspect -f "{{.NetworkSettings.IPAddress }}" postgres-container`
mvn liberty:devc \
  -DdockerRunOpts="-e POSTGRES_HOSTNAME=$POSTGRES_IP" \
  -DserverStartTimeout=240
```

You need to wait a while to let dev mode start. After you see the following message, your Liberty instance is ready in dev mode:
```
**************************************************************
*    Liberty is running in dev mode.
*    ...
*    Docker network information:
*        Container name: [ liberty-dev ]
*        IP address [ 172.17.0.2 ] on Docker network [ bridge ]
*    ...
```

Open another command-line session and run the following command to make sure that your container is running and didn’t crash:

```bash
docker ps 
```

You can see something similar to the following output:

```
CONTAINER ID  IMAGE               COMMAND                 CREATED        STATUS        PORTS                                                                   NAMES
ee2daf0b33e1  inventory-dev-mode  "/opt/ol/helpers/run…"  2 minutes ago  Up 2 minutes  0.0.0.0:7777->7777/tcp, 0.0.0.0:9080->9080/tcp, 0.0.0.0:9443->9443/tcp  liberty-dev
```


Try out your application by the following commands:

```bash
curl -s http://localhost:9080/health | jq
```

```bash
curl 'http://localhost:9080/inventory/api/systems'
```

When you're finished trying out the microservice, press `Ctrl+C` in the command-line session where you started dev mode to stop and remove the container.

Also, run the following commands to stop the PostgreSQL container that was started in the previous section.

```bash
docker stop postgres-container
docker rm postgres-container
```

### Building the container image

Run the ***mvn package*** command from the ***start/inventory*** directory so that the ***.war*** file resides in the ***target*** directory.

```bash
cd /home/project/guide-liberty-deep-dive/start/inventory
```

```bash
mvn package
```

Build your Docker image with the following commands:

```bash
docker build -t liberty-deepdive-inventory:1.0-SNAPSHOT .
```

In this Skills Network environment, you need to push the image to your container registry on IBM Cloud by running the following commands:
```bash
docker tag liberty-deepdive-inventory:1.0-SNAPSHOT us.icr.io/$SN_ICR_NAMESPACE/liberty-deepdive-inventory:1.0-SNAPSHOT
docker push us.icr.io/$SN_ICR_NAMESPACE/liberty-deepdive-inventory:1.0-SNAPSHOT
```

When the build finishes, run the following command to list all local Docker images:
```bash
docker images
```

Verify that the ***liberty-deepdive-inventory:1.0-SNAPSHOT*** image is listed among the Docker images, for example:
```
REPOSITORY                    TAG
liberty-deepdive-inventory    1.0-SNAPSHOT
icr.io/appcafe/open-liberty   full-java17-openj9-ubi
```

::page{title="Testing the microservice with Testcontainers"}

Although you can test your microservice manually, you should rely on automated tests. In this section, you can learn how to use Testcontainers to verify your microservice in the same Docker container that you’ll use in production.

First, create the ***test*** directory at the ***src*** directory of your Maven project.


```bash
mkdir -p /home/project/guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest
mkdir /home/project/guide-liberty-deep-dive/start/inventory/src/test/resources
```

Create a RESTful client interface for the ***inventory*** microservice.

Create the ***SystemResourceClient.java*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest/SystemResourceClient.java
```


> Then, to open the SystemResourceClient.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest/SystemResourceClient.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest/SystemResourceClient.java"}



```java
package it.io.openliberty.deepdive.rest;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
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
public interface SystemResourceClient {

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    List<SystemData> listContents();

    @GET
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    SystemData getSystem(
        @PathParam("hostname") String hostname);

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response addSystem(
        @QueryParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize);

    @PUT
    @Path("/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "admin", "user" })
    Response updateSystem(
        @HeaderParam("Authorization") String authHeader,
        @PathParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize);

    @DELETE
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "admin" })
    Response removeSystem(
        @HeaderParam("Authorization") String authHeader,
        @PathParam("hostname") String hostname);
}

```



This interface declares ***listContents()***, ***getSystem()***, ***addSystem()***, ***updateSystem()***, and ***removeSystem()*** methods for accessing each of the endpoints that are set up to access the ***inventory*** microservice. 


Create the ***SystemData*** data model for each system in the inventory.

Create the ***SystemData.java*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest/SystemData.java
```


> Then, to open the SystemData.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest/SystemData.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest/SystemData.java"}



```java
package it.io.openliberty.deepdive.rest;

public class SystemData {

    private int id;
    private String hostname;
    private String osName;
    private String javaVersion;
    private Long heapSize;

    public SystemData() {
    }

    public int getId() {
        return id;
    }

    public String getHostname() {
        return hostname;
    }

    public String getOsName() {
        return osName;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public Long getHeapSize() {
        return heapSize;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public void setHeapSize(Long heapSize) {
        this.heapSize = heapSize;
    }
}
```



The ***SystemData*** class contains the ID, hostname, operating system name, Java version, and heap size properties. The various ***get*** and ***set*** methods within this class enable you to view and edit the properties of each system in the inventory.

Create the test container class that access the ***inventory*** docker image that you built in previous section.

Create the ***LibertyContainer.java*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest/LibertyContainer.java
```


> Then, to open the LibertyContainer.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest/LibertyContainer.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest/LibertyContainer.java"}



```java
package it.io.openliberty.deepdive.rest;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.UriBuilder;

public class LibertyContainer extends GenericContainer<LibertyContainer> {

    static final Logger LOGGER = LoggerFactory.getLogger(LibertyContainer.class);

    private String baseURL;

    private KeyStore keystore;
    private SSLContext sslContext;

    public static String getProtocol() {
        return System.getProperty("test.protocol", "https");
    }

    public static boolean testHttps() {
        return getProtocol().equalsIgnoreCase("https");
    }

    public LibertyContainer(final String dockerImageName) {
        super(dockerImageName);
        waitingFor(Wait.forLogMessage("^.*CWWKF0011I.*$", 1));
        init();
    }

    public <T> T createRestClient(Class<T> clazz, String applicationPath) {
        String urlPath = getBaseURL();
        if (applicationPath != null) {
            urlPath += applicationPath;
        }
        ClientBuilder builder = ResteasyClientBuilder.newBuilder();
        if (testHttps()) {
            builder.sslContext(sslContext);
            builder.trustStore(keystore);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        }
        ResteasyClient client = (ResteasyClient) builder.build();
        ResteasyWebTarget target = client.target(UriBuilder.fromPath(urlPath));
        return target.proxy(clazz);
    }

    public String getBaseURL() throws IllegalStateException {
        if (baseURL != null) {
            return baseURL;
        }
        if (!this.isRunning()) {
            throw new IllegalStateException(
                "Container must be running to determine hostname and port");
        }
        baseURL =  getProtocol() + "://" + this.getHost()
            + ":" + this.getFirstMappedPort();
        System.out.println("TEST: " + baseURL);
        return baseURL;
    }

    private void init() {

        if (!testHttps()) {
            this.addExposedPorts(9080);
            return;
        }

        this.addExposedPorts(9443, 9080);
        try {
            String keystoreFile = System.getProperty("user.dir")
                    + "/../../finish/system/src/main"
                    + "/liberty/config/resources/security/key.p12";
            keystore = KeyStore.getInstance("PKCS12");
            keystore.load(new FileInputStream(keystoreFile), "secret".toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                                        KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keystore, "secret".toCharArray());
            X509TrustManager xtm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException { }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException { }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            TrustManager[] tm = new TrustManager[] {
                                    xtm
                                };
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tm, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```



The ***createRestClient()*** method creates a REST client instance with the ***SystemResourceClient*** interface. The ***getBaseURL()*** method constructs the URL that can access the ***inventory*** docker image.

Now, you can create your integration test cases.

Create the ***SystemResourceIT.java*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest/SystemResourceIT.java
```


> Then, to open the SystemResourceIT.java file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest/SystemResourceIT.java, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/test/java/it/io/openliberty/deepdive/rest/SystemResourceIT.java"}



```java
package it.io.openliberty.deepdive.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
public class SystemResourceIT {

    private static Logger logger = LoggerFactory.getLogger(SystemResourceIT.class);
    private static String appPath = "/inventory/api";
    private static String postgresHost = "postgres";
    private static String postgresImageName = "postgres-sample:latest";
    private static String appImageName = "liberty-deepdive-inventory:1.0-SNAPSHOT";

    public static SystemResourceClient client;
    public static Network network = Network.newNetwork();
    private static String authHeader;

    @Container
    public static GenericContainer<?> postgresContainer
        = new GenericContainer<>(postgresImageName)
              .withNetwork(network)
              .withExposedPorts(5432)
              .withNetworkAliases(postgresHost)
              .withLogConsumer(new Slf4jLogConsumer(logger));

    @Container
    public static LibertyContainer libertyContainer
        = new LibertyContainer(appImageName)
              .withEnv("POSTGRES_HOSTNAME", postgresHost)
              .withNetwork(network)
              .waitingFor(Wait.forHttp("/health/ready").forPort(9080))
              .withLogConsumer(new Slf4jLogConsumer(logger));

    @BeforeAll
    public static void setupTestClass() throws Exception {
        System.out.println("TEST: Starting Liberty Container setup");
        client = libertyContainer.createRestClient(
            SystemResourceClient.class, appPath);
        String userPassword = "bob" + ":" + "bobpwd";
        authHeader = "Basic "
            + Base64.getEncoder().encodeToString(userPassword.getBytes());
    }

    private void showSystemData(SystemData system) {
        System.out.println("TEST: SystemData > "
            + system.getId() + ", "
            + system.getHostname() + ", "
            + system.getOsName() + ", "
            + system.getJavaVersion() + ", "
            + system.getHeapSize());
    }

    @Test
    @Order(1)
    public void testAddSystem() {
        System.out.println("TEST: Testing add a system");
        client.addSystem("localhost", "linux", "17", Long.valueOf(2048));
        List<SystemData> systems = client.listContents();
        assertEquals(1, systems.size());
        showSystemData(systems.get(0));
        assertEquals("17", systems.get(0).getJavaVersion());
        assertEquals(Long.valueOf(2048), systems.get(0).getHeapSize());
    }

    @Test
    @Order(2)
    public void testUpdateSystem() {
        System.out.println("TEST: Testing update a system");
        client.updateSystem(authHeader, "localhost", "linux", "8", Long.valueOf(1024));
        SystemData system = client.getSystem("localhost");
        showSystemData(system);
        assertEquals("8", system.getJavaVersion());
        assertEquals(Long.valueOf(1024), system.getHeapSize());
    }

    @Test
    @Order(3)
    public void testRemoveSystem() {
        System.out.println("TEST: Testing remove a system");
        client.removeSystem(authHeader, "localhost");
        List<SystemData> systems = client.listContents();
        assertEquals(0, systems.size());
    }
}
```



Define the ***postgresContainer*** test container to start up the PostgreSQL docker image, and define the ***libertyContainer*** test container to start up the ***inventory*** docker image. Make sure that both containers use the same ***network***. The ***/health/ready*** endpoint can tell you whether the container is ready to start testing.

The ***testAddSystem()*** verifies the ***addSystem*** and ***listContents*** endpoints.

The ***testUpdateSystem()*** verifies the ***updateSystem*** and ***getSystem*** endpoints.

The ***testRemoveSystem()*** verifies the ***removeSystem*** endpoint.


Create the log4j properites that are required by the Testcontainers framework.

Create the ***log4j.properties*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive/start/inventory/src/test/resources/log4j.properties
```


> Then, to open the log4j.properties file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/src/test/resources/log4j.properties, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/src/test/resources/log4j.properties"}



```
log4j.rootLogger=INFO, stdout

log4j.appender=org.apache.log4j.ConsoleAppender
log4j.appender.layout=org.apache.log4j.PatternLayout

log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%r %p %c %x - %m%n

log4j.logger.io.openliberty.guides.testing=DEBUG
```



Update the Maven configuration file with the required dependencies.

Replace the ***pom.xml*** file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/pom.xml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/pom.xml"}



```xml
<?xml version="1.0" encoding="UTF-8" ?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
	       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.deepdive</groupId>
    <artifactId>inventory</artifactId>
    <packaging>war</packaging>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <liberty.var.http.port>9080</liberty.var.http.port>
        <liberty.var.https.port>9443</liberty.var.https.port>
        <liberty.var.context.root>/inventory</liberty.var.context.root>
        <liberty.var.client.https.port>9444</liberty.var.client.https.port>
    </properties>

    <dependencies>
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
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.1</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Test dependencies -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-reload4j</artifactId>
            <version>2.0.9</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.9</version>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-client</artifactId>
            <version>6.2.6.Final</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-json-binding-provider</artifactId>
            <version>6.2.6.Final</version>
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
            <version>3.0.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>cglib</groupId>
            <artifactId>cglib-nodep</artifactId>
            <version>3.3.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.vertx</groupId>
            <artifactId>vertx-auth-jwt</artifactId>
            <version>4.5.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>inventory</finalName>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-war-plugin</artifactId>
                    <version>3.3.2</version>
                </plugin>
                <plugin>
                    <groupId>io.openliberty.tools</groupId>
                    <artifactId>liberty-maven-plugin</artifactId>
                    <version>3.10</version>
                </plugin>
            </plugins>
        </pluginManagement>
        <plugins>
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <configuration>
                    <copyDependencies>
                        <dependencyGroup>
                            <location>${project.build.directory}/liberty/wlp/usr/shared/resources</location>
                            <dependency>
                                <groupId>org.postgresql</groupId>
                                <artifactId>postgresql</artifactId>
                                <version>42.7.1</version>
                            </dependency>
                        </dependencyGroup>
                    </copyDependencies>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.2.2</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```



Add each required ***dependency*** with ***test*** scope, including JUnit5, Testcontainers, Log4J, JBoss RESTEasy client, Glassfish JSON, and Vert.x libraries. Also, add the ***maven-failsafe-plugin*** plugin, so that the integration test can be run by the Maven ***verify*** goal.

### Running the tests

You can run the Maven ***verify*** goal, which compiles the java files, starts the containers, runs the tests, and then stops the containers.


In this Skills Network environment, you can test the HTTP protcol only.
```bash
export TESTCONTAINERS_RYUK_DISABLED=true
mvn verify
```


You will see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.deepdive.rest.SystemResourceIT
...
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 17.413 s - in it.io.openliberty.deepdive.rest.SystemResourceIT

Results :

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```


::page{title="Deploying the microservice to Kubernetes"}

Now that the containerized application is built and tested, deploy it to a local Kubernetes cluster. 

### Installing the Open Liberty Operator 


In this Skills Network environment, the Open Liberty Operator is already installed by the administrator. If you would like to learn how to install the Open Liberty Operator, see the [Deploying a microservice to Kubernetes by using Open Liberty Operator](https://openliberty.io/guides/openliberty-operator-intro.html) guide or the Open Liberty Operator [documentation](https://github.com/OpenLiberty/open-liberty-operator/tree/main/deploy/releases/1.2.1#readme).

To check that the Open Liberty Operator is installed successfully, run the following command to view all the supported API resources that are available through the Open Liberty Operator:

```bash
kubectl api-resources --api-group=apps.openliberty.io
```

Look for the following output, which shows the [custom resource definitions](https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/) (CRDs) that can be used by the Open Liberty Operator:

```
NAME                      SHORTNAMES         APIGROUP              NAMESPACED   KIND
openlibertyapplications   olapp,olapps       apps.openliberty.io   true         OpenLibertyApplication
openlibertydumps          oldump,oldumps     apps.openliberty.io   true         OpenLibertyDump
openlibertytraces         oltrace,oltraces   apps.openliberty.io   true         OpenLibertyTrace
```

Each CRD defines a kind of object that can be used, which is specified in the previous example by the ***KIND*** value. The ***SHORTNAME*** value specifies alternative names that you can substitute in the configuration to refer to an object kind. For example, you can refer to the ***OpenLibertyApplication*** object kind by one of its specified shortnames, such as ***olapps***. 

The ***openlibertyapplications*** CRD defines a set of configurations for deploying an Open Liberty-based application, including the application image, number of instances, and storage settings. The Open Liberty Operator watches for changes to instances of the ***OpenLibertyApplication*** object kind and creates Kubernetes resources that are based on the configuration that is defined in the CRD.

### Deploying the container image


Create the ***inventory.yaml*** in the ***start/inventory*** directory.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-liberty-deep-dive/start/inventory/inventory.yaml
```


> Then, to open the inventory.yaml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/inventory.yaml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/inventory.yaml"}



```yaml
apiVersion: apps.openliberty.io/v1
kind: OpenLibertyApplication
metadata:
  name: inventory-deployment
  labels:
    name: inventory-deployment
spec:
  applicationImage: liberty-deepdive-inventory:1.0-SNAPSHOT
  service:
    port: 9443
  env:
    - name: POSTGRES_HOSTNAME
      value: "postgres"
```



In the ***inventory.yaml*** file, the custom resource (CR) is specified to be ***OpenLibertyApplication***. The CR triggers the Open Liberty Operator to create, update, or delete Kubernetes resources that are needed by the application to run on your cluster. Additionally, the ***applicationImage*** field must be specified and set to the image that was created in the previous module. 


Similarly, a Kubernetes resource definition is provided in the ***postgres.yaml*** file at the ***finish/postgres*** directory. In the ***postgres.yaml*** file, the deployment for the PostgreSQL database is defined. 


Create a Kubernetes Secret to configure the credentials for the ***admin*** user to access the database.
```bash
kubectl create secret generic post-app-credentials --from-literal username=admin --from-literal password=adminpwd
```

The credentials are passed to the PostgreSQL database service as environment variables in the ***env*** field.


To deploy the **inventory** microservice and ***Postgres*** database in this Skills Network environment, you need to update the image name so that the image in your IBM Cloud container registry is used, and add the **pullSecret** and ***pullPolicy*** settings. Run the following commands:

```bash
sed -i 's=namespace: default=namespace: '"$SN_ICR_NAMESPACE"'=g' /home/project/guide-liberty-deep-dive/finish/postgres/postgres.yaml
kubectl apply -f /home/project/guide-liberty-deep-dive/finish/postgres/postgres.yaml
sed -i 's=liberty-deepdive-inventory:1.0-SNAPSHOT=us.icr.io/'"$SN_ICR_NAMESPACE"'/liberty-deepdive-inventory:1.0-SNAPSHOT\n  pullPolicy: Always\n  pullSecret: icr=g' /home/project/guide-liberty-deep-dive/start/inventory/inventory.yaml
kubectl apply -f /home/project/guide-liberty-deep-dive/start/inventory/inventory.yaml
```

When your pods are deployed, run the following command to check their status:
```bash
kubectl get pods
```

If all the pods are working correctly, you see an output similar to the following example:

```
NAME                                    READY   STATUS    RESTARTS   AGE
inventory-deployment-75f9dc56d9-g9lzl   1/1     Running   0          35s
postgres-58bd9b55c7-6vzz8               1/1     Running   0          13s
olo-controller-manager-6fc6b456dc-s29wl 1/1     Running   0          10m
```

Run the following command to set up port forwarding to access the ***inventory*** microservice:

```bash
kubectl port-forward svc/inventory-deployment 9443
```

The ***port-forward*** command pauses the command-line session until you click **Ctrl+C** after you try out the microservice.


The application might take some time to get ready. To confirm that the ***inventory*** microservice is up and running, run the following curl command:

```bash
curl -k https://localhost:9443/health | jq
```

If the application is up and running, you are ready to access the microservice.
In another command-line session, access the microservice by running the following commands:

```bash
curl -k --user bob:bobpwd -X DELETE \
  'https://localhost:9443/inventory/api/systems/localhost'
```

```bash
curl -k -X POST 'https://localhost:9443/inventory/api/systems?heapSize=1048576&hostname=localhost&javaVersion=9&osName=linux'
```

```bash
curl -k --user alice:alicepwd -X PUT \
  'https://localhost:9443/inventory/api/systems/localhost?heapSize=2097152&javaVersion=17&osName=linux'
```

```bash
curl -k -s 'https://localhost:9443/inventory/api/systems' | jq
```


When you're done trying out the microservice, press **CTRL+C** in the command line session where you ran the ***kubectl port-forward*** command to stop the port forwarding. Then, run the ***kubectl delete*** command to stop the ***inventory*** microservice.


```bash
kubectl delete -f /home/project/guide-liberty-deep-dive/start/inventory/inventory.yaml
```

### Customizing deployments


You can modify the inventory deployment to customize the service. Customizations for a service include changing the port number, changing the context root, and passing confidential information by using Secrets. 

The ***context.root*** variable is defined in the ***server.xml*** configuration file. The context root for the inventory service can be changed by using this variable. The value for the ***context.root*** variable can be defined in a ConfigMap and accessed as an environment variable.

Create a ConfigMap to configure the app name with the following ***kubectl*** command.
```bash
kubectl create configmap inv-app-root --from-literal contextRoot=/dev
```
This command deploys a ConfigMap named ***inv-app-root*** to your cluster. It has a key called ***contextRoot*** with a value of ***/dev***. The ***--from-literal*** flag specifies individual key-value pairs to store in this ConfigMap. 


Replace the ***inventory.yaml*** file.

> To open the inventory.yaml file in your IDE, select
> **File** > **Open** > guide-liberty-deep-dive/start/inventory/inventory.yaml, or click the following button

::openFile{path="/home/project/guide-liberty-deep-dive/start/inventory/inventory.yaml"}



```yaml
apiVersion: apps.openliberty.io/v1
kind: OpenLibertyApplication
metadata:
  name: inventory-deployment
  labels:
    name: inventory-deployment
spec:
  applicationImage: liberty-deepdive-inventory:1.0-SNAPSHOT
  service:
    port: 9443
  volumeMounts:
  - name: postgres
    mountPath: "/config/variables/postgres"
    readOnly: true
  volumes:
  - name: postgres
    secret:
      secretName: post-app-credentials 
  env:
    - name: POSTGRES_HOSTNAME
      value: "postgres"
    - name: CONTEXT_ROOT
      valueFrom:
        configMapKeyRef:
          name: inv-app-root
          key: contextRoot
```



During deployment, the ***post-app-credentials*** secret can be mounted to the ***/config/variables/postgres*** in the pod to create Liberty config variables. Liberty creates variables from the files in the ***/config/variables/postgres*** directory. Instead of including confidential information in the ***server.xml*** configuration file, users can access it using normal Liberty variable syntax, ***${postgres/username}*** and ***${postgres/password}***.

Run the following command to deploy your changes.

```bash
sed -i 's=liberty-deepdive-inventory:1.0-SNAPSHOT=us.icr.io/'"$SN_ICR_NAMESPACE"'/liberty-deepdive-inventory:1.0-SNAPSHOT\n  pullPolicy: Always\n  pullSecret: icr=g' /home/project/guide-liberty-deep-dive/start/inventory/inventory.yaml
kubectl apply -f /home/project/guide-liberty-deep-dive/start/inventory/inventory.yaml
```

Run the following command to check your pods status:
```bash
kubectl get pods
```

If all the pods are working correctly, you see an output similar to the following example:

```
NAME                                    READY   STATUS    RESTARTS   AGE
inventory-deployment-75f9dc56d9-g9lzl   1/1     Running   0          35s
postgres-58bd9b55c7-6vzz8               1/1     Running   0          13s
```

Run the following command to set up port forwarding to access the ***inventory*** microservice:

```bash
kubectl port-forward svc/inventory-deployment 9443
```


The application might take some time to get ready. To confirm that the `inventory` microservice is up and running, run the following curl command in another command-line session:

```bash
curl -k https://localhost:9443/health | jq
```

If the application is up and running, you are ready to access the microservice.

Access the microservice with the context root ***/dev*** by running the following commands:

```bash
curl -k --user bob:bobpwd -X DELETE \
  'https://localhost:9443/dev/api/systems/localhost'
```

```bash
curl -k -X POST 'https://localhost:9443/dev/api/systems?heapSize=1048576&hostname=localhost&javaVersion=9&osName=linux'
```

```bash
curl -k --user alice:alicepwd -X PUT \
  'https://localhost:9443/dev/api/systems/localhost?heapSize=2097152&javaVersion=17&osName=linux'
```

```bash
curl -k -s 'https://localhost:9443/dev/api/systems' | jq
```

### Tearing down the environment 

When you're finished trying out the microservice, press **CTRL+C** in the command line session where you ran the ***kubectl port-forward*** command to stop the port forwarding. You can delete all Kubernetes resources by running the ***kubectl delete*** commands:


```bash
kubectl delete -f /home/project/guide-liberty-deep-dive/start/inventory/inventory.yaml
kubectl delete -f /home/project/guide-liberty-deep-dive/finish/postgres/postgres.yaml
kubectl delete configmap inv-app-root
kubectl delete secret post-app-credentials
```

::page{title="Support Licensing"}

Open Liberty is open source under the Eclipse Public License v1 so there is no fee to use it in production. Community support is available at StackOverflow, Gitter, or the mail list, and bugs can be raised in [GitHub](https://github.com/openliberty/open-liberty). Commercial support is available for Open Liberty from IBM. For more information, see the [IBM Marketplace](https://www.ibm.com/uk-en/marketplace/elite-support-for-open-liberty). The WebSphere Liberty product is built on Open Liberty. No migration is required to use WebSphere Liberty, you simply point to WebSphere Liberty in your build. WebSphere Liberty users get support for the packaged Open Liberty function.

WebSphere Liberty is also available in [Maven Central](https://search.maven.org/search?q=g:com.ibm.websphere.appserver.runtime).

You can use WebSphere Liberty for development even without purchasing it. However, if you have production entitlement, you can easily change to use it with the following steps.

In the ***pom.xml***, add the ***\<configuration\>*** element as the following:

```XML
  <plugin>
      <groupId>io.openliberty.tools</groupId>
      <artifactId>liberty-maven-plugin</artifactId>
      <version>3.10</version>
      <configuration>
          <runtimeArtifact>
              <groupId>com.ibm.websphere.appserver.runtime</groupId>
              <artifactId>wlp-kernel</artifactId>
               <version>[23.0.0.12,)</version>
               <type>zip</type>
          </runtimeArtifact>
      </configuration>
  </plugin>
```

Rebuild and restart the ***inventory*** service by dev mode:

```
mvn clean
mvn liberty:dev
```

In the ***Dockerfile***, replace the Liberty image at the ***FROM*** statement with ***websphere-liberty*** as shown in the following example:
```
FROM icr.io/appcafe/websphere-liberty:full-java17-openj9-ubi

ARG VERSION=1.0
ARG REVISION=SNAPSHOT
...
```

::page{title="Summary"}

### Nice Work!

You just completed a hands-on deep dive on Liberty!



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-liberty-deep-dive*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-liberty-deep-dive
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=A%20Technical%20Deep%20Dive%20on%20Liberty&guide-id=cloud-hosted-guide-liberty-deep-dive)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-liberty-deep-dive/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-liberty-deep-dive/pulls)



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
