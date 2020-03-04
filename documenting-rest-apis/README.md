# What you’ll learn

You will learn how to document and filter RESTful APIs from annotations, POJOs, and static OpenAPI files by using MicroProfile OpenAPI.

The OpenAPI specification, previously known as the Swagger specification, defines a standard interface for documenting and exposing RESTful APIs. This specification allows both humans and computers to understand or process the functionalities of services without requiring direct access to underlying source code or documentation. The MicroProfile OpenAPI specification provides a set of Java interfaces and programming models that allow Java developers to natively produce OpenAPI v3 documents from their JAX-RS applications.

You will document the RESTful APIs of the provided **inventory** service, which serves two endpoints, inventory/systems and inventory/properties. These two endpoints function the same way as in the other MicroProfile guides.

Before you proceed, note that the 1.0 version of the MicroProfile OpenAPI specification does not define how the /openapi endpoint may be partitioned in the event of multiple JAX-RS applications running on the same server. In other words, you must stick to one JAX-RS application per server instance as the behaviour for handling multiple applications is currently undefined.

# Getting started

The fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:

`git clone https://github.com/openliberty/guide-microprofile-openapi.git`

`cd guide-microprofile-openapi`


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.

## Try what you’ll build

The finish directory in the root of this guide contains the finished application. Give it a try before you proceed.

Navigate to the **finish** directory

`cd finish`

To try out the application, first go to the finish directory and run the following Maven goal to build the application and deploy it to Open Liberty:

`mvn liberty:run`

After you see the following message, your application server is ready.

`The defaultServer server is ready to run a smarter planet.`

Next, point your browser to the openapi URL and you’ll see the RESTful APIs of the **inventory** service. 

`curl http://localhost:9080/openapi`

You can also point to the ui URL for a more interactive view of the deployed APIs. 

`curl http://localhost:9080/openapi/ui`

This UI is built from the Open Source Swagger UI and renders the generated /openapi document into a very user friendly page.

After you are finished checking out the application, stop the Open Liberty server by pressing **CTRL+C** in the shell session where you ran the server. Alternatively, you can run the liberty:stop goal from the **finish** directory in another shell session:

`mvn liberty:stop`

## Generating the OpenAPI document for the inventory service

You can generate an OpenAPI document in various ways. First, because all JAX-RS annotations are processed by default, you can augment your existing JAX-RS annotations with OpenAPI annotations to enrich your APIs with a minimal amount of work. Second, you can use a set of predefined models to manually create all elements of the OpenAPI tree. Finally, you can filter various elements of the OpenAPI tree, changing them to your liking or removing them entirely.

Navigate to the **start** directory to begin.

`cd ../start`

Start Open Liberty in development mode, which starts the Open Liberty server and listens for file changes:

`mvn liberty:dev`

After you see the following message, your application server in development mode is ready.

Press the **Enter** key to run tests on demand.

The development mode holds your command prompt to listen for file changes. You need to open another command prompt to continue, or simply open the project in your editor.

Because the JAX-RS framework handles basic API generation for JAX-RS annotations, a skeleton OpenAPI tree will be generated from the **inventory** service. You can use this tree as a starting point and augment it with annotations and code to produce a complete OpenAPI document.

Now, point to the **openapi** URL to see the generated OpenAPI tree. 

`curl http://localhost:9080/openapi`

You can also point to the **ui** URL for a more interactive view of the APIs.

`curl  http://localhost:9080/openapi/ui`

## Augmenting the existing JAX-RS annotations with OpenAPI annotations

Because all JAX-RS annotations are processed by default, you can augment the existing code with OpenAPI annotations without needing to rewrite portions of the OpenAPI document that are already covered by the JAX-RS framework.

Update the **InventoryResource** class.

[File -> Open] `src/main/java/io/openliberty/guides/inventory/InventoryResource.java`

```java
package io.openliberty.guides.inventory;

import java.util.Properties;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import io.openliberty.guides.inventory.model.InventoryList;

@RequestScoped
@Path("/systems")
public class InventoryResource {

    @Inject
    InventoryManager manager;

    @GET
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "404",
                description = "Missing description",
                content = @Content(mediaType = "text/plain")),
            @APIResponse(
                responseCode = "200",
                description = "JVM system properties of a particular host.",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Properties.class))) })
    @Operation(
        summary = "Get JVM system properties for particular host",
        description = "Retrieves and returns the JVM system properties from the system "
        + "service running on the particular host.")
    public Response getPropertiesForHost(
        @Parameter(
            description = "The host for whom to retrieve the JVM system properties for.",
            required = true,
            example = "foo",
            schema = @Schema(type = SchemaType.STRING))
        @PathParam("hostname") String hostname) {
        // Get properties for host
        Properties props = manager.get(hostname);
        if (props == null) {
            return Response.status(Response.Status.NOT_FOUND)
                            .entity("ERROR: Unknown hostname or the system service may "
                                + "not be running on " + hostname)
                            .build();
        }

        //Add to inventory to host
        manager.add(hostname, props);
        return Response.ok(props).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(
        responseCode = "200",
        description = "host:properties pairs stored in the inventory.",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(
                type = SchemaType.OBJECT,
                implementation = InventoryList.class)))
    @Operation(
        summary = "List inventory contents.",
        description = "Returns the currently stored host:properties pairs in the "
        + "inventory.")
    public InventoryList listContents() {
        return manager.list();
    }

}
```

Add OpenAPI annotations to the two JAX-RS methods, getPropertiesForHost() and listContents().

Clearly, there are many more annotations now, so let’s break them down:

````
Annotation	Description
@APIResponses

A container for multiple responses from an API operation. This annotation is optional, but it can be helpful to organize a method with multiple responses.

@APIResponse

Describes a single response from an API operation.

@Content

Provides a schema and examples for a particular media type.

@Schema

Defines the input and output data types.

@Operation

Describes a single API operation on a path.

@Parameter

Describes a single operation parameter.
````

Since the Open Liberty server was started in development mode at the beginning of the guide, your changes were automatically picked up. 

Refresh the openapi URL to see the updated OpenAPI tree. 

`curl http://localhost:9080/openapi`

The two endpoints at which your JAX-RS methods are served are now more meaningful:

```
/inventory/systems/{hostname}:
  get:
    summary: Get JVM system properties for particular host
    description: Retrieves and returns the JVM system properties from the system
      service running on the particular host.
    operationId: getPropertiesForHost
    parameters:
    - name: hostname
      in: path
      description: The host for whom to retrieve the JVM system properties for.
      required: true
      schema:
        type: string
      example: foo
    responses:
      404:
        description: Missing description
        content:
          text/plain: {}
      200:
        description: JVM system properties of a particular host.
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Properties'
/inventory/systems:
  get:
    summary: List inventory contents.
    description: Returns the currently stored host:properties pairs in the inventory.
    operationId: listContents
    responses:
      200:
        description: host:properties pairs stored in the inventory.
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/InventoryList'
```

OpenAPI annotations can also be added to POJOs to describe what they represent. Currently, your OpenAPI document doesn’t have a very meaningful description of the InventoryList POJO and hence it’s very difficult to tell exactly what that POJO is used for. 

To describe the InventoryList POJO in more detail, augment the `src/main/java/io/openliberty/guides/inventory/model/InventoryList.java` file with some OpenAPI annotations.

Update the InventoryList class.

`src/main/java/io/openliberty/guides/inventory/model/InventoryList.java`

```
package io.openliberty.guides.inventory.model;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="InventoryList", description="POJO that represents the inventory contents.")
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

Likewise, annotate the src/main/java/io/openliberty/guides/inventory/model/SystemData.java POJO, which is referenced in the InventoryList class.

Update the SystemData class.

`src/main/java/io/openliberty/guides/inventory/model/SystemData.java`

```java
package io.openliberty.guides.inventory.model;

import java.util.Properties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="SystemData", description="POJO that represents a single inventory entry.")
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

Refresh the openapi URL to see the updated OpenAPI tree:

`curl http://localhost:9080/openapi`

```
components:
  schemas:
    InventoryList:
      required:
      - systems
      type: object
      properties:
        systems:
          type: array
          items:
            $ref: '#/components/schemas/SystemData'
        total:
          type: integer
      description: POJO that represents the inventory contents.
    SystemData:
      required:
      - hostname
      - properties
      type: object
      properties:
        hostname:
          type: string
        properties:
          type: object
          additionalProperties:
            type: string
      description: POJO that represents a single inventory entry.
    Properties:
      type: object
      additionalProperties:
        type: string
        
```

Filtering the OpenAPI tree elements

Filtering of certain elements and fields of the generated OpenAPI document can be done by using the OASFilter interface.

Create the **InventoryOASFilter** class.

[File -> New] `src/main/java/io/openliberty/guides/inventory/filter/InventoryOASFilter.java`

```java
package io.openliberty.guides.inventory.filter;

import java.util.Arrays;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.info.License;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.servers.ServerVariables;

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

    openAPI.setServers(Arrays.asList(
        OASFactory.createObject(Server.class).url("http://localhost:{port}")
                  .description("Simple Open Liberty.").variables(
                      OASFactory.createObject(ServerVariables.class)
                                .addServerVariable("port",
                                    OASFactory.createObject(ServerVariable.class)
                                              .description(
                                                  "Server HTTP port.")
                                              .defaultValue("9080")))));
  }
```

The filterAPIResponse() method allows filtering of APIResponse elements. When you override this method, it will be called once for every APIResponse element in the OpenAPI tree. In this case, you are matching the 404 response that is returned by the /inventory/systems/{hostname} endpoint and setting the previously missing description. To remove an APIResponse element or another filterable element, simply return null.

The filterOpenAPI() method allows filtering of the singleton OpenAPI element. Unlike other filter methods, when you override filterOpenAPI(), it is called only once as the last method for a particular filter. Hence, make sure that it doesn’t override any other filter operations that are called before it. Your current OpenAPI document doesn’t provide much information on the application itself or on what server and port it runs on. This information is usually provided in the info and servers elements, which are currently missing. Use the OASFactory class to manually set these and other elements of the OpenAPI tree from the org.eclipse.microprofile.openapi.models package. The OpenAPI element is the only element that cannot be removed since that would mean removing the whole OpenAPI tree.

Each filtering method is called once for each corresponding element in the model tree. You can think of each method as a callback for various key OpenAPI elements.

Before you can use the filter class that you created, you need to create the microprofile-config.properties file.

Create the configuration file.

[File -> New] `src/main/webapp/META-INF/microprofile-config.properties`

```
mp.openapi.scan.disable = true
mp.openapi.filter = io.openliberty.guides.inventory.filter.InventoryOASFilter
```

This configuration file is picked up automatically by MicroProfile Config and registers your filter by passing in the fully qualified name of the filter class into the mp.openapi.filter property.

Refresh the http://localhost:9080/openapi URL to see the updated OpenAPI tree:

```
info:
  title: Inventory App
  description: App for storing JVM system properties of various hosts.
  license:
    name: Eclipse Public License - v 1.0
    url: https://www.eclipse.org/legal/epl-v10.html
  version: 1.0.0
servers:
- url: http://localhost:{port}
  description: Simple Open Liberty.
  variables:
    port:
      default: "9080"
      description: Server HTTP port.
404:
  description: Invalid hostname or the system service may not be running on
    the particular host.
  content:
    text/plain: {}
```

For more information about which elements you can filter, see the [https://openliberty.io/docs/ref/microprofile/](MicroProfile API.)

To learn more about MicroProfile Config, visit the MicroProfile [https://github.com/eclipse/microprofile-config](Config GitHub repository) and try one of the MicroProfile Config [https://openliberty.io/guides/?search=Config](guides.)

# Using pregenerated OpenAPI documents

As an alternative to generating the OpenAPI model tree from code, you can provide a valid pregenerated OpenAPI document to describe your APIs. This document must be named openapi with a yml, yaml, or json extension and be placed under the META-INF directory. Depending on the scenario, the document might be fully or partially complete. If the document is fully complete, then you can disable annotation scanning entirely by setting the mp.openapi.scan.disable MicroProfile Config property to true. If the document is partially complete, then you can augment it with code.

You can find a complete OpenAPI document in the src/main/webapp/META-INF/openapi.yaml file. This document is the same as your current OpenAPI document with additional APIs for the /inventory/properties endpoint. To make use of this document, open this document in your favorite text editor and uncomment all of its lines.

Update the OpenAPI document file.
`src/main/webapp/META-INF/openapi.yaml`
Uncomment all the lines in the openapi.yaml file.

```yaml
openapi: 3.0.0
info:
  title: Inventory App
  description: App for storing JVM system properties of various hosts.
  license:
    name: Eclipse Public License - v 1.0
    url: https://www.eclipse.org/legal/epl-v10.html
  version: 1.0.0
servers:
- url: http://localhost:{port}
  description: Simple Open Liberty.
  variables:
    port:
      default: "9080"
      description: Server HTTP port.
paths:
  /inventory/systems:
    get:
      summary: List inventory contents.
      description: Returns the currently stored host:properties pairs in the inventory.
      operationId: listContents
      responses:
        200:
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
      operationId: getPropertiesForHost
      parameters:
      - name: hostname
        in: path
        description: The host for whom to retrieve the JVM system properties for.
        required: true
        schema:
          type: string
        example: foo
      responses:
        404:
          description: Invalid hostname or the system service may not be running on
            the particular host.
          content:
            text/plain: {}
        200:
          description: JVM system properties of a particular host.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Properties'
  /inventory/properties:
    get:
      operationId: getProperties
      responses:
        200:
          description: JVM system properties of the host running this service.
          content:
            application/json:
              schema:
                type: object
                additionalProperties:
                  type: string
components:
  schemas:
    InventoryList:
      required:
      - systems
      type: object
      properties:
        systems:
          type: array
          items:
            $ref: '#/components/schemas/SystemData'
        total:
          type: integer
      description: POJO that represents the inventory contents.
    SystemData:
      required:
      - hostname
      - properties
      type: object
      properties:
        hostname:
          type: string
        properties:
          type: object
          additionalProperties:
            type: string
      description: POJO that represents a single inventory entry.
    Properties:
      type: object
      additionalProperties:
        type: string
```
Since this document is complete, you can also set the mp.openapi.scan.disable property to true in the src/main/webapp/META-INF/microprofile-config.properties file.

```
mp.openapi.scan.disable = true
mp.openapi.filter = io.openliberty.guides.inventory.filter.InventoryOASFilter
```

Refresh the http://localhost:9080/openapi URL to see the updated OpenAPI tree:
```
/inventory/properties:
  get:
    operationId: getProperties
    responses:
      200:
        description: JVM system properties of the host running this service.
        content:
          application/json:
            schema:
              type: object
              additionalProperties:
                type: string

```

# Testing the service

No automated tests are provided to verify the correctness of the generated OpenAPI document. Manually verify the document by visiting the http://localhost:9080/openapi or the http://localhost:9080/openapi/ui URL.

A few tests are included for you to test the basic functionality of the inventory service. If a test failure occurs, then you might have introduced a bug into the code. These tests will run automatically as a part of the integration test suite.

Running the tests
Since you started Open Liberty in development mode at the start of the guide, press the enter/return key to run the tests.

You will see the following output:

````
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.4 sec - in it.io.openliberty.guides.system.SystemEndpointIT
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.264 sec - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results :

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
````

When you are done checking out the service, exit development mode by pressing CTRL+C in the shell session where you ran the server, or by typing q and then pressing the enter/return key.
