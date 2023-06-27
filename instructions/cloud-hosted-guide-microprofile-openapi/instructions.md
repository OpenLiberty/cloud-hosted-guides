---
markdown-version: v1
title: instructions
branch: lab-492-instruction
version-history-start-date: 2021-03-03 17:52:52 UTC
tool-type: theia
---
::page{title="Welcome to the Documenting RESTful APIs guide!"}

Explore how to document and filter RESTful APIs from code or static files by using MicroProfile OpenAPI.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You will learn how to document and filter RESTful APIs from annotations, POJOs, and static OpenAPI files by using MicroProfile OpenAPI.

The OpenAPI specification, previously known as the Swagger specification, defines a standard interface for documenting and exposing RESTful APIs. This specification allows both humans and computers to understand or process the functionalities of services without requiring direct access to underlying source code or documentation. The MicroProfile OpenAPI specification provides a set of Java interfaces and programming models that allow Java developers to natively produce OpenAPI v3 documents from their JAX-RS applications.

You will document the RESTful APIs of the provided ***inventory*** service, which serves two endpoints, ***inventory/systems*** and ***inventory/properties***. These two endpoints function the same way as in the other MicroProfile guides.

Before you proceed, note that the 1.0 version of the MicroProfile OpenAPI specification does not define how the ***/openapi*** endpoint may be partitioned in the event of multiple JAX-RS applications running on the same server. In other words, you must stick to one JAX-RS application per server instance as the behaviour for handling multiple applications is currently undefined.


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-openapi.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microprofile-openapi.git
cd guide-microprofile-openapi
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



To open a new command-line session, select **Terminal** > **New Terminal** from the menu of the IDE.

Next, run the following curl command to see the RESTful APIs of the ***inventory*** service:
```bash
curl http://localhost:9080/openapi
```

A UI is also available for a more interactive view of the deployed APIs. Click the following button to visit the UI by the ***/openapi/ui*** endpoint. 
::startApplication{port="9080" display="external" name="Visit OpenAPI UI" route="/openapi/ui"}

This UI is built from the [Open Source Swagger UI](https://swagger.io/tools/swagger-ui), which renders the generated **/openapi** document into a very user friendly page.

After you are finished checking out the application, stop the Open Liberty server by pressing `Ctrl+C` in the command-line session where you ran the server. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```


::page{title="Generating the OpenAPI document for the inventory service"}

You can generate an OpenAPI document in various ways. First, because all Jakarta Restful Web Services annotations are processed by default, you can augment your existing Jakarta Restful Web Services annotations with OpenAPI annotations to enrich your APIs with a minimal amount of work. Second, you can use a set of predefined models to manually create all elements of the OpenAPI tree. Finally, you can filter various elements of the OpenAPI tree, changing them to your liking or removing them entirely.

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-microprofile-openapi/start
```

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), the server listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```bash
mvn liberty:dev
```

After you see the following message, your application server in dev mode is ready:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

Because the Jakarta Restful Web Services framework handles basic API generation for Jakarta Restful Web Services annotations, a skeleton OpenAPI tree will be generated from the ***inventory*** service. You can use this tree as a starting point and augment it with annotations and code to produce a complete OpenAPI document.



Now, run the following curl command to see the generated OpenAPI tree:
```bash
curl http://localhost:9080/openapi
```

Click the following button to visit the UI by the ***/openapi/ui*** endpoint for a more interactive view of the APIs.
::startApplication{port="9080" display="external" name="Visit OpenAPI UI" route="/openapi/ui"}

### Augmenting the existing Jakarta Restful Web Services annotations with OpenAPI annotations

Because all Jakarta Restful Web Services annotations are processed by default, you can augment the existing code with OpenAPI annotations without needing to rewrite portions of the OpenAPI document that are already covered by the Jakarta Restful Web Services framework.

Update the ***InventoryResource*** class.

> To open the InventoryResource.java file in your IDE, select
> **File** > **Open** > guide-microprofile-openapi/start/src/main/java/io/openliberty/guides/inventory/InventoryResource.java, or click the following button

::openFile{path="/home/project/guide-microprofile-openapi/start/src/main/java/io/openliberty/guides/inventory/InventoryResource.java"}



```java
package io.openliberty.guides.inventory;

import java.util.Properties;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import io.openliberty.guides.inventory.model.InventoryList;

@RequestScoped
@Path("/systems")
public class InventoryResource {

    @Inject
    InventoryManager manager;

    @GET
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(
        responseCode = "404",
        description = "Missing description",
        content = @Content(mediaType = "application/json"))
    @APIResponseSchema(value = Properties.class,
        responseDescription = "JVM system properties of a particular host.",
        responseCode = "200")
    @Operation(
        summary = "Get JVM system properties for particular host",
        description = "Retrieves and returns the JVM system properties from the system "
        + "service running on the particular host.")
    public Response getPropertiesForHost(
        @Parameter(
            description = "The host for whom to retrieve "
                + "the JVM system properties for.",
            required = true,
            example = "foo",
            schema = @Schema(type = SchemaType.STRING))
        @PathParam("hostname") String hostname) {
        Properties props = manager.get(hostname);
        if (props == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{ \"error\" : "
                                   + "\"Unknown hostname " + hostname
                                   + " or the resource may not be "
                                   + "running on the host machine\" }")
                           .build();
        }

        manager.add(hostname, props);
        return Response.ok(props).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = InventoryList.class,
        responseDescription = "host:properties pairs stored in the inventory.",
        responseCode = "200")
    @Operation(
        summary = "List inventory contents.",
        description = "Returns the currently stored host:properties pairs in the "
        + "inventory.")
    public InventoryList listContents() {
        return manager.list();
    }

}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


Add OpenAPI ***@APIResponse***, ***@APIResponseSchema***, ***@Operation***, and ***@Parameter*** annotations to the two JAX-RS endpoint methods, ***getPropertiesForHost()*** and ***listContents()***.



Clearly, there are many more OpenAPI annotations now, so let’s break them down:

| *Annotation*    | *Description*
| ---| ---
| ***@APIResponse***  | Describes a single response from an API operation.
| ***@APIResponseSchema*** | Convenient short-hand way to specify a simple response with a Java class that could otherwise be specified using @APIResponse.
| ***@Operation***    | Describes a single API operation on a path.
| ***@Parameter***    | Describes a single operation parameter.


Since the Open Liberty server was started in dev mode at the beginning of the guide, your changes were automatically picked up. Run the following curl command to see the updated OpenAPI tree:
```bash
curl http://localhost:9080/openapi
```

The two endpoints at which your JAX-RS endpoint methods are served are now more meaningful:

```
/inventory/systems:
  get:
    summary: List inventory contents.
    description: Returns the currently stored host:properties pairs in the inventory.
    responses:
      "200":
        description: host:properties pairs stored in the inventory.
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/InventoryList'
/inventory/systems/{hostname}:
  get:
    summary: Get JVM system properties for particular host
    description: Retrieves and returns the JVM system properties from the system
      service running on the particular host.
    parameters:
    - name: hostname
      in: path
      description: The host for whom to retrieve the JVM system properties for.
      required: true
      schema:
        type: string
      example: foo
    responses:
      "404":
        description: Missing description
        content:
          application/json: {}
      "200":
        description: JVM system properties of a particular host.
        content:
          application/json:
            schema:
              type: object
```


OpenAPI annotations can also be added to POJOs to describe what they represent. Currently, your OpenAPI document doesn't have a very meaningful description of the ***InventoryList*** POJO and hence it's very difficult to tell exactly what that POJO is used for. To describe the ***InventoryList*** POJO in more detail, augment the ***src/main/java/io/openliberty/guides/inventory/model/InventoryList.java*** file with some OpenAPI annotations.

Update the ***InventoryList*** class.

> To open the InventoryList.java file in your IDE, select
> **File** > **Open** > guide-microprofile-openapi/start/src/main/java/io/openliberty/guides/inventory/model/InventoryList.java, or click the following button

::openFile{path="/home/project/guide-microprofile-openapi/start/src/main/java/io/openliberty/guides/inventory/model/InventoryList.java"}



```java
package io.openliberty.guides.inventory.model;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "InventoryList",
description = "POJO that represents the inventory contents.")
public class InventoryList {

    @Schema(required = true)
    private List<SystemData> systems;

    public InventoryList(List<SystemData> systems) {
        this.systems = systems;
    }

    public List<SystemData> getSystems() {
        return systems;
    }

    public int getTotal() {
        return systems.size();
    }
}
```



Add OpenAPI ***@Schema*** annotations to the ***InventoryList*** class and the ***systems*** variable.


Likewise, annotate the ***src/main/java/io/openliberty/guides/inventory/model/SystemData.java*** POJO, which is referenced in the ***InventoryList*** class.

Update the ***SystemData*** class.

> To open the SystemData.java file in your IDE, select
> **File** > **Open** > guide-microprofile-openapi/start/src/main/java/io/openliberty/guides/inventory/model/SystemData.java, or click the following button

::openFile{path="/home/project/guide-microprofile-openapi/start/src/main/java/io/openliberty/guides/inventory/model/SystemData.java"}



```java
package io.openliberty.guides.inventory.model;

import java.util.Properties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "SystemData",
       description = "POJO that represents a single inventory entry.")
public class SystemData {

    @Schema(required = true)
    private final String hostname;

    @Schema(required = true)
    private final Properties properties;

    public SystemData(String hostname, Properties properties) {
        this.hostname = hostname;
        this.properties = properties;
    }

    public String getHostname() {
        return hostname;
    }

    public Properties getProperties() {
        return properties;
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



Add OpenAPI ***@Schema*** annotations to the ***SystemData*** class, the ***hostname*** variable and the ***properties*** variable.



Run the following curl command to see the updated OpenAPI tree:
```bash
curl http://localhost:9080/openapi
```

```
components:
  schemas:
    InventoryList:
      description: POJO that represents the inventory contents.
      required:
      - systems
      type: object
      properties:
        systems:
          type: array
          items:
            $ref: '#/components/schemas/SystemData'
        total:
          format: int32
          type: integer
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


### Filtering the OpenAPI tree elements

Filtering of certain elements and fields of the generated OpenAPI document can be done by using the ***OASFilter*** interface.

Create the ***InventoryOASFilter*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-openapi/start/src/main/java/io/openliberty/guides/inventory/filter/InventoryOASFilter.java
```


> Then, to open the InventoryOASFilter.java file in your IDE, select
> **File** > **Open** > guide-microprofile-openapi/start/src/main/java/io/openliberty/guides/inventory/filter/InventoryOASFilter.java, or click the following button

::openFile{path="/home/project/guide-microprofile-openapi/start/src/main/java/io/openliberty/guides/inventory/filter/InventoryOASFilter.java"}



```java
package io.openliberty.guides.inventory.filter;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.info.License;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;

public class InventoryOASFilter implements OASFilter {

  @Override
  public APIResponse filterAPIResponse(APIResponse apiResponse) {
    if ("Missing description".equals(apiResponse.getDescription())) {
      apiResponse.setDescription("Invalid hostname or the system service may not "
          + "be running on the particular host.");
    }
    return apiResponse;
  }

  @Override
  public void filterOpenAPI(OpenAPI openAPI) {
    openAPI.setInfo(
        OASFactory.createObject(Info.class).title("Inventory App").version("1.0")
                  .description(
                      "App for storing JVM system properties of various hosts.")
                  .license(
                      OASFactory.createObject(License.class)
                                .name("Eclipse Public License - v 1.0").url(
                                    "https://www.eclipse.org/legal/epl-v10.html")));

    openAPI.addServer(
        OASFactory.createServer()
                  .url("http://localhost:{port}")
                  .description("Simple Open Liberty.")
                  .variables(Collections.singletonMap("port",
                                 OASFactory.createServerVariable()
                                           .defaultValue("9080")
                                           .description("Server HTTP port."))));
  }

}
```



The ***filterAPIResponse()*** method allows filtering of ***APIResponse*** elements. When you override this method, it will be called once for every ***APIResponse*** element in the OpenAPI tree. In this case, you are matching the ***404*** response that is returned by the ***/inventory/systems/{hostname}*** endpoint and setting the previously missing description. To remove an ***APIResponse*** element or another filterable element, simply return ***null***.

The ***filterOpenAPI()*** method allows filtering of the singleton ***OpenAPI*** element. Unlike other filter methods, when you override ***filterOpenAPI()***, it is called only once as the last method for a particular filter. Hence, make sure that it doesn't override any other filter operations that are called before it. Your current OpenAPI document doesn't provide much information on the application itself or on what server and port it runs on. This information is usually provided in the ***info*** and ***servers*** elements, which are currently missing. Use the ***OASFactory*** class to manually set these and other elements of the OpenAPI tree from the ***org.eclipse.microprofile.openapi.models*** package. The ***OpenAPI*** element is the only element that cannot be removed, because that would mean removing the whole OpenAPI tree.

Each filtering method is called once for each corresponding element in the model tree. You can think of each method as a callback for various key OpenAPI elements.

Before you can use the filter class that you created, you need to create the ***microprofile-config.properties*** file.

Create the configuration file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-openapi/start/src/main/webapp/META-INF/microprofile-config.properties
```


> Then, to open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > guide-microprofile-openapi/start/src/main/webapp/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/guide-microprofile-openapi/start/src/main/webapp/META-INF/microprofile-config.properties"}



```
mp.openapi.filter = io.openliberty.guides.inventory.filter.InventoryOASFilter
```



This configuration file is picked up automatically by MicroProfile Config and registers your filter by passing in the fully qualified name of the filter class into the ***mp.openapi.filter*** property.


Run the following curl command to see the updated OpenAPI tree:
```bash
curl http://localhost:9080/openapi
```

```
info:
  title: Inventory App
  description: App for storing JVM system properties of various hosts.
  license:
    name: Eclipse Public License - v 1.0
    url: https://www.eclipse.org/legal/epl-v10.html
  version: "1.0"
servers:
- url: "http://localhost:{port}"
  description: Simple Open Liberty.
  variables:
    port:
      default: "9080"
      description: Server HTTP port.
```

```
responses:
  "404":
    description: Invalid hostname or the system service may not be running on
      the particular host.
    content:
      application/json: {}
```

For more information about which elements you can filter, see the [MicroProfile API documentation](https://openliberty.io/docs/ref/microprofile/).

To learn more about MicroProfile Config, visit the MicroProfile Config [GitHub repository](https://github.com/eclipse/microprofile-config) and try one of the MicroProfile Config [guides](https://openliberty.io/guides/?search=Config).



::page{title="Using pregenerated OpenAPI documents"}

As an alternative to generating the OpenAPI model tree from code, you can provide a valid pregenerated OpenAPI document to describe your APIs. This document must be named ***openapi*** with a ***yml***, ***yaml***, or ***json*** extension and be placed under the ***META-INF*** directory. Depending on the scenario, the document might be fully or partially complete. If the document is fully complete, then you can disable annotation scanning entirely by setting the ***mp.openapi.scan.disable*** MicroProfile Config property to ***true***. If the document is partially complete, then you can augment it with code.

To use the pre-generated OpenAPI document, create the OpenAPI document YAML file.

Create the OpenAPI document file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-openapi/start/src/main/webapp/META-INF/openapi.yaml
```


> Then, to open the openapi.yaml file in your IDE, select
> **File** > **Open** > guide-microprofile-openapi/start/src/main/webapp/META-INF/openapi.yaml, or click the following button

::openFile{path="/home/project/guide-microprofile-openapi/start/src/main/webapp/META-INF/openapi.yaml"}



```yaml
---
openapi: 3.0.3
info:
  title: Inventory App
  description: App for storing JVM system properties of various hosts.
  license:
    name: Eclipse Public License - v 1.0
    url: https://www.eclipse.org/legal/epl-v10.html
  version: "1.0"
paths:
  /inventory/properties:
    get:
      operationId: getProperties
      responses:
        "200":
          description: JVM system properties of the host running this service.
          content:
            application/json:
              schema:
                type: object
                additionalProperties:
                  type: string
  /inventory/systems:
    get:
      summary: List inventory contents.
      description: Returns the currently stored host:properties pairs in the inventory.
      responses:
        "200":
          description: host:properties pairs stored in the inventory.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/InventoryList'
  /inventory/systems/{hostname}:
    get:
      summary: Get JVM system properties for particular host
      description: Retrieves and returns the JVM system properties from the system
        service running on the particular host.
      parameters:
      - name: hostname
        in: path
        description: The host for whom to retrieve the JVM system properties for.
        required: true
        schema:
          type: string
        example: foo
      responses:
        "404":
          description: Invalid hostname or the system service may not be running on
            the particular host.
          content:
            application/json: {}
        "200":
          description: JVM system properties of a particular host.
          content:
            application/json:
              schema:
                type: object
components:
  schemas:
    InventoryList:
      description: POJO that represents the inventory contents.
      required:
      - systems
      type: object
      properties:
        systems:
          type: array
          items:
            $ref: '#/components/schemas/SystemData'
        total:
          format: int32
          type: integer
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




This document is the same as your current OpenAPI document with extra APIs for the ***/inventory/properties*** endpoint. This document is complete so you can also add the ***mp.openapi.scan.disable*** property and set it to ***true*** in the ***src/main/webapp/META-INF/microprofile-config.properties*** file.

Update the configuration file.

> To open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > guide-microprofile-openapi/start/src/main/webapp/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/guide-microprofile-openapi/start/src/main/webapp/META-INF/microprofile-config.properties"}



```
mp.openapi.scan.disable = true
mp.openapi.filter = io.openliberty.guides.inventory.filter.InventoryOASFilter
```


Add and set the ***mp.openapi.scan.disable*** property to ***true***.



Run the following curl command to see the updated OpenAPI tree:
```bash
curl http://localhost:9080/openapi
```

```
/inventory/properties:
  get:
    operationId: getProperties
    responses:
      "200":
        description: JVM system properties of the host running this service.
        content:
          application/json:
            schema:
              type: object
              additionalProperties:
                type: string
```



::page{title="Testing the service"}


No automated tests are provided to verify the correctness of the generated OpenAPI document. Manually verify the document by visiting the ***http://localhost:9080/openapi*** or the ***http://localhost:9080/openapi/ui*** URL.

A few tests are included for you to test the basic functionality of the ***inventory*** service. If a test failure occurs, then you might have introduced a bug into the code. These tests will run automatically as a part of the integration test suite.

### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

You will see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.4 sec - in it.io.openliberty.guides.system.SystemEndpointIT
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
[WARNING ] Interceptor for {http://client.inventory.guides.openliberty.io/}SystemClient has thrown exception, unwinding now
Could not send Message.
[err] The specified host is unknown: java.net.UnknownHostException: UnknownHostException invoking http://badhostname:9080/inventory/properties: badhostname
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.264 sec - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results :

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

The warning and error messages are expected and result from a request to a bad or an unknown hostname. This request is made in the ***testUnknownHost()*** test from the ***InventoryEndpointIT*** integration test.

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran the server, or by typing ***q*** and then pressing the ***enter/return*** key.



::page{title="Summary"}

### Nice Work!

You have just documented and filtered the APIs of the **inventory** service from both the code and a static file by using MicroProfile OpenAPI in Open Liberty.


Feel free to try one of the related MicroProfile guides. They demonstrate additional technologies that you can learn and expand on top of what you built here.

For more in-depth examples of MicroProfile OpenAPI, try one of the demo applications available in the MicroProfile OpenAPI [GitHub repository](https://github.com/eclipse/microprofile-open-api/tree/master/tck/src/main/java/org/eclipse/microprofile/openapi/apps).



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-openapi*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-openapi
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Documenting%20RESTful%20APIs&guide-id=cloud-hosted-guide-microprofile-openapi)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-openapi/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-openapi/pulls)



### Where to next?

* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Configuring microservices](https://openliberty.io/guides/microprofile-config.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
