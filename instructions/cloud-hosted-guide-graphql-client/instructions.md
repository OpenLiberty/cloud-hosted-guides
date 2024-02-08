---
markdown-version: v1
title: instructions
branch: lab-329-instruction
version-history-start-date: 2022-05-03T13:58:19Z
tool-type: theia
---
::page{title="Welcome to the Running GraphQL queries and mutations using a GraphQL client guide!"}



In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.


Learn how to use the SmallRye GraphQL client's typesafe interface to query and mutate data from multiple microservices.

::page{title="What you'll learn"}

GraphQL is an open source data query language. You can use a GraphQL service to obtain data from multiple sources, such as APIs, databases, and other services, by sending a single request to a GraphQL service. GraphQL services require less data fetching than REST services, which results in faster application load times and lower data transfer costs. This guide assumes you have a basic understanding of [GraphQL concepts](https://openliberty.io/docs/latest/microprofile-graphql.html). If you're new to GraphQL, you might want to start with the [Optimizing REST queries for microservices with GraphQL](https://openliberty.io/guides/microprofile-graphql.html) guide first.

You'll use the [SmallRye GraphQL client](https://github.com/smallrye/smallrye-graphql#client) to create a ***query*** microservice that will make requests to the ***graphql*** microservice. The ***graphql*** microservice retrieves data from multiple ***system*** microservices and is identical to the one created as part of the [Optimizing REST queries for microservices with GraphQL](https://openliberty.io/guides/microprofile-graphql.html) guide. 

![GraphQL client application architecture where multiple system microservices are integrated behind the graphql service](https://raw.githubusercontent.com/OpenLiberty/guide-graphql-client/prod/assets/architecture.png)


The results of the requests will be displayed at REST endpoints. OpenAPI will be used to help make the requests and display the data. To learn more about OpenAPI, check out the [Documenting RESTful APIs](https://openliberty.io/guides/microprofile-openapi.html) guide.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-graphql-client.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-graphql-client.git
cd guide-graphql-client
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.


::page{title="Implementing a GraphQL client"}

Navigate to the ***start*** directory to begin.

```bash
cd /home/project/guide-graphql-client/start
```

The [SmallRye GraphQL client](https://github.com/smallrye/smallrye-graphql#client) is used to implement the GraphQL client service. The SmallRye GraphQL client supports two types of clients: typesafe and dynamic. A typesafe client is easy to use and provides a high-level approach, while a dynamic client provides a more customizable and low-level approach to handle operations and responses. You will implement a typesafe client microservice. 

The typesafe client interface contains a method for each resolver available in the ***graphql*** microservice. The JSON objects returned by the ***graphql*** microservice are converted to Java objects.

Create the ***GraphQlClient*** interface.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-graphql-client/start/query/src/main/java/io/openliberty/guides/query/client/GraphQlClient.java
```


> Then, to open the GraphQlClient.java file in your IDE, select
> **File** > **Open** > guide-graphql-client/start/query/src/main/java/io/openliberty/guides/query/client/GraphQlClient.java, or click the following button

::openFile{path="/home/project/guide-graphql-client/start/query/src/main/java/io/openliberty/guides/query/client/GraphQlClient.java"}



```java
package io.openliberty.guides.query.client;

import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;

import io.openliberty.guides.graphql.models.SystemInfo;
import io.openliberty.guides.graphql.models.SystemLoad;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

@GraphQLClientApi
public interface GraphQlClient {
    @Query
    SystemInfo system(@Name("hostname") String hostname);

    @Query("systemLoad")
    SystemLoad[] getSystemLoad(@Name("hostnames") String[] hostnames);

    @Mutation
    boolean editNote(@Name("hostname") String host, @Name("note") String note);

}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***GraphQlClient*** interface is annotated with the ***@GraphQlClientApi*** annotation. This annotation denotes that this interface is used to create a typesafe GraphQL client.

Inside the interface, a method header is written for each resolver available in the ***graphql*** microservice. The names of the methods match the names of the resolvers in the GraphQL schema. Resolvers that require input variables have the input variables passed in using the ***@Name*** annotation on the method inputs. The return types of the methods should match those of the GraphQL resolvers.

For example, the ***system()*** method maps to the ***system*** resolver. The resolver returns a ***SystemInfo*** object, which is described by the ***SystemInfo*** class. Thus, the ***system()*** method returns the type ***SystemInfo***.

The name of each resolver is the method name, but it can be overridden with the ***@Query*** or ***@Mutation*** annotations. For example, the name of the method ***getSystemLoad*** is overridden as ***systemLoad***. The GraphQL request that goes over the wire will use the name overridden by the ***@Query*** and ***@Mutation*** annotation. Similarly, the name of the method inputs can be overridden by the ***@Name*** annotation. For example, input ***host*** is overridden as ***hostname*** in the ***editNote()*** method. 

The ***editNote*** ***mutation*** operation has the ***@Mutation*** annotation on it. A ***mutation*** operation allows you to modify data, in this case, it allows you to add and edit a note to the system service. If the ***@Mutation*** annotation were not placed on the method, it would be treated as if it mapped to a ***query*** operation. 

Create the ***QueryResource*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-graphql-client/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java
```


> Then, to open the QueryResource.java file in your IDE, select
> **File** > **Open** > guide-graphql-client/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java, or click the following button

::openFile{path="/home/project/guide-graphql-client/start/query/src/main/java/io/openliberty/guides/query/QueryResource.java"}



```java
package io.openliberty.guides.query;

import io.openliberty.guides.graphql.models.NoteInfo;
import io.openliberty.guides.graphql.models.SystemInfo;
import io.openliberty.guides.graphql.models.SystemLoad;
import io.openliberty.guides.query.client.GraphQlClient;
import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("query")
public class QueryResource {

    private GraphQlClient gc = TypesafeGraphQLClientBuilder.newBuilder()
                                                   .build(GraphQlClient.class);

    @GET
    @Path("system/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemInfo querySystem(@PathParam("hostname") String hostname) {
        return gc.system(hostname);
    }

    @GET
    @Path("systemLoad/{hostnames}")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemLoad[] querySystemLoad(@PathParam("hostnames") String hostnames) {
        String[] hostnameArray = hostnames.split(",");
        return gc.getSystemLoad(hostnameArray);
    }

    @POST
    @Path("mutation/system/note")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editNote(NoteInfo text) {
        if (gc.editNote(text.getHostname(), text.getText())) {
            return Response.ok().build();
        } else {
            return Response.serverError().build();
        }
    }
}
```



The ***QueryResource*** class uses the ***GraphQlClient*** interface to make requests to the ***graphql*** microservice and display the results. In a real application, you would make requests to an external GraphQL service, and you might do further manipulation of the data after retrieval.

The ***TypesafeGraphQLClientBuilder*** class creates a client object that implements the ***GraphQlClient*** interface and can interact with the ***graphql*** microservice. The ***GraphQlClient*** client can make requests to the URL specified by the ***graphql.server*** variable in the ***server.xml*** file. The client is used in the ***querySystem()***, ***querySystemLoad()***, and ***editNote()*** methods.

Add the SmallRye GraphQL client dependency to the project configuration file.

Replace the Maven project file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-graphql-client/start/query/pom.xml, or click the following button

::openFile{path="/home/project/guide-graphql-client/start/query/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-graphql-client-query</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- Liberty configuration -->
        <liberty.var.default.http.port>9084</liberty.var.default.http.port>
        <liberty.var.default.https.port>9447</liberty.var.default.https.port>
    </properties>

    <dependencies>
        <!-- Provided dependencies -->
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
        
        <!-- Required dependencies -->
        <dependency>
           <groupId>io.openliberty.guides</groupId>
           <artifactId>guide-graphql-client-models</artifactId>
           <version>1.0-SNAPSHOT</version>
        </dependency>
        
        <!-- GraphQL API dependencies -->
        <dependency>
            <groupId>io.smallrye</groupId>
            <artifactId>smallrye-graphql-client</artifactId>
            <version>2.1.3</version>
        </dependency>
        <dependency>
            <groupId>io.smallrye</groupId>
            <artifactId>smallrye-graphql-client-implementation-vertx</artifactId>
            <version>2.1.3</version>
        </dependency>
        <dependency>
            <groupId>io.smallrye.stork</groupId>
            <artifactId>stork-core</artifactId>
            <version>2.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.7</version>
        </dependency>
             
        <!-- For tests -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.9.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-client</artifactId>
            <version>6.2.3.Final</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-json-binding-provider</artifactId>
            <version>6.2.3.Final</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>jakarta.json</artifactId>
            <version>2.0.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <version>1.18.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>1.18.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.36</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <!-- Enable liberty-maven plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.8.2</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.0.0</version>
                <configuration>
                    <systemPropertyVariables>
                        <http.port>${liberty.var.default.http.port}</http.port>
                    </systemPropertyVariables>
                </configuration>
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



The ***smallrye-graphql-client*** dependencies provide the classes that you use to interact with a ***graphql*** microservice.

To run the service, you must correctly configure the Liberty.

Replace the Liberty server.xml configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-graphql-client/start/query/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-graphql-client/start/query/src/main/liberty/config/server.xml"}



```xml
<server description="Query Service">

  <featureManager>
    <feature>restfulWS-3.1</feature>
    <feature>cdi-4.0</feature>
    <feature>jsonb-3.0</feature>
    <feature>mpConfig-3.0</feature>
    <feature>mpOpenAPI-3.1</feature>
  </featureManager>

  <variable name="default.http.port" defaultValue="9084"/>
  <variable name="default.https.port" defaultValue="9447"/>
  <variable name="graphql.server" defaultValue="http://graphql:9082/graphql"/>

  <httpEndpoint host="*" httpPort="${default.http.port}"
      httpsPort="${default.https.port}" id="defaultHttpEndpoint"/>

  <webApplication location="guide-graphql-client-query.war" contextRoot="/"/>
</server>
```



The ***graphql.server*** variable is defined in the ***server.xml*** file. This variable defines where the GraphQL client makes requests to.


::page{title="Building and running the application"}

From the ***start*** directory, run the following commands:

```bash
mvn -pl models install
mvn package
```

The ***mvn install*** command compiles and packages the object types you created to a ***.jar*** file. This allows them to be used by the ***system*** and ***graphql*** services. The ***mvn package*** command packages the ***system***, ***graphql***, and ***query*** services to ***.war*** files. 



Dockerfiles are already set up for you. Build your Docker images with the following commands:

```bash
docker build -t system:1.0-java11-SNAPSHOT --build-arg JAVA_VERSION=java11 system/.
docker build -t system:1.0-java17-SNAPSHOT --build-arg JAVA_VERSION=java17 system/.
docker build -t graphql:1.0-SNAPSHOT graphql/.
docker build -t query:1.0-SNAPSHOT query/.
```

Run these Docker images using the provided ***startContainers*** script. The script creates a network for the services to communicate through. It creates the two ***system*** microservices, a ***graphql*** microservice, and a ***query*** microservice that interact with each other.


```bash
./scripts/startContainers.sh
```

The containers might take some time to become available. 

::page{title="Accessing the application"}



To access the client service, there are several available REST endpoints that test the API endpoints that you created. 

**Try the query operations**

First, make a GET request to the ***/query/system/{hostname}*** endpoint by the following command. This request retrieves the system properties for the specified ***hostname***.

The ***hostname*** is set to ***system-java11***. You can try out the operations using the hostname ***system-java17*** as well. 

```bash
curl -s 'http://localhost:9084/query/system/system-java11' | jq
```
You can expect a response similar to the following example:


```
{
  "hostname": "system-java11",
  "java": {
    "vendor": "IBM Corporation",
    "version": "11.0.18"
  },
  "osArch": "amd64",
  "osName": "Linux",
  "osVersion": "5.15.0-67-generic",
  "systemMetrics": {
    "heapSize": 536870912,
    "nonHeapSize": -1,
    "processors": 2
  },
  "username": "default"
}
```



You can retrieve the information about the resource usage of any number of system services by making a GET request at ***/query/systemLoad/{hostnames}*** endpoint. 
The ***hostnames*** are set to ***system-java11,system-java17***.

```bash
curl -s 'http://localhost:9084/query/systemLoad/system-java11,system-java17' | jq
```

You can expect the following response is similar to the following example:


```
[
  {
    "hostname": "system-java11",
    "loadData": {
      "heapUsed": 30090920,
      "loadAverage": 0.08,
      "nonHeapUsed": 87825316
    }
  },
  {
    "hostname": "system-java17",
    "loadData": {
      "heapUsed": 39842888,
      "loadAverage": 0.08,
      "nonHeapUsed": 93098960
    }
  }
]
```


**Try the mutation operation**

You can also make POST requests to add a note to a system service at the ***/query/mutation/system/note*** endpoint.
To add a note to the system service running on Java 8, run the following command:

```bash
curl -i -X 'POST' 'http://localhost:9084/query/mutation/system/note' -H 'Content-Type: application/json' -d '{"hostname": "system-java11","text": "I am trying out GraphQL on Open Liberty!"}'
```

You will recieve a `200` response code, similar to below, if the request is processed succesfully. 

```
HTTP/1.1 200 OK
Content-Language: en-US
Content-Length: 0
Date: Fri, 21 Apr 2023 14:17:47 GMT
```

You can see the note you added to the system service at the ***GET /query/system/{hostname}*** endpoint.

::page{title="Tearing down the environment"}

When you're done checking out the application, run the following script to stop the application:


```bash
./scripts/stopContainers.sh
```


::page{title="Testing the application"}

Although you can test your application manually, you should rely on automated tests. In this section, you'll create integration tests using Testcontainers to verify that the basic operations you implemented function correctly. 

First, create a RESTful client interface for the ***query*** microservice.

Create the ***QueryResourceClient.java*** interface.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-graphql-client/start/query/src/test/java/it/io/openliberty/guides/query/QueryResourceClient.java
```


> Then, to open the QueryResourceClient.java file in your IDE, select
> **File** > **Open** > guide-graphql-client/start/query/src/test/java/it/io/openliberty/guides/query/QueryResourceClient.java, or click the following button

::openFile{path="/home/project/guide-graphql-client/start/query/src/test/java/it/io/openliberty/guides/query/QueryResourceClient.java"}



```java
package it.io.openliberty.guides.query;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.openliberty.guides.graphql.models.SystemInfo;
import io.openliberty.guides.graphql.models.SystemLoad;
import io.openliberty.guides.graphql.models.NoteInfo;

@ApplicationScoped
@Path("query")
public interface QueryResourceClient {

    @GET
    @Path("system/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    SystemInfo querySystem(@PathParam("hostname") String hostname);

    @GET
    @Path("systemLoad/{hostnames}")
    @Produces(MediaType.APPLICATION_JSON)
    List<SystemLoad> querySystemLoad(@PathParam("hostnames") String hostnames);

    @POST
    @Path("mutation/system/note")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response editNote(NoteInfo text);
}
```



This interface declares ***querySystem()***, ***querySystemLoad()***, and ***editNote()*** methods for accessing each of the endpoints that are set up to access the ***query*** microservice.

Create the test container class that accesses the ***query*** image that you built in previous section.

Create the ***LibertyContainer.java*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-graphql-client/start/query/src/test/java/it/io/openliberty/guides/query/LibertyContainer.java
```


> Then, to open the LibertyContainer.java file in your IDE, select
> **File** > **Open** > guide-graphql-client/start/query/src/test/java/it/io/openliberty/guides/query/LibertyContainer.java, or click the following button

::openFile{path="/home/project/guide-graphql-client/start/query/src/test/java/it/io/openliberty/guides/query/LibertyContainer.java"}



```java
package it.io.openliberty.guides.query;

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

    public LibertyContainer(final String dockerImageName) {
        super(dockerImageName);
        waitingFor(Wait.forLogMessage("^.*CWWKF0011I.*$", 1));
        this.addExposedPorts(9084);
        return;
    }

    public <T> T createRestClient(Class<T> clazz) {
        String urlPath = getBaseURL();
        ClientBuilder builder = ResteasyClientBuilder.newBuilder();
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
        baseURL =  "http://" + this.getContainerIpAddress()
            + ":" + this.getFirstMappedPort();
        System.out.println("TEST: " + baseURL);
        return baseURL;
    }
}
```



The ***createRestClient()*** method creates a REST client instance with the ***QueryResourceClient*** interface. The ***getBaseURL()*** method constructs the URL that can access the ***query*** image.

Now, create your integration test cases.

Create the ***QueryResourceIT.java*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-graphql-client/start/query/src/test/java/it/io/openliberty/guides/query/QueryResourceIT.java
```


> Then, to open the QueryResourceIT.java file in your IDE, select
> **File** > **Open** > guide-graphql-client/start/query/src/test/java/it/io/openliberty/guides/query/QueryResourceIT.java, or click the following button

::openFile{path="/home/project/guide-graphql-client/start/query/src/test/java/it/io/openliberty/guides/query/QueryResourceIT.java"}



```java
package it.io.openliberty.guides.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import jakarta.ws.rs.core.Response;
import io.openliberty.guides.graphql.models.NoteInfo;
import io.openliberty.guides.graphql.models.SystemLoad;
import io.openliberty.guides.graphql.models.SystemLoadData;
import io.openliberty.guides.graphql.models.SystemInfo;

@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
public class QueryResourceIT {

    private static Logger logger = LoggerFactory.getLogger(QueryResourceIT.class);
    private static String system8ImageName = "system:1.0-java11-SNAPSHOT";
    private static String queryImageName = "query:1.0-SNAPSHOT";
    private static String graphqlImageName = "graphql:1.0-SNAPSHOT";

    public static QueryResourceClient client;
    public static Network network = Network.newNetwork();

    @Container
    public static GenericContainer<?> systemContainer
        = new GenericContainer<>(system8ImageName)
              .withNetwork(network)
              .withExposedPorts(9080)
              .withNetworkAliases("system-java11")
              .withLogConsumer(new Slf4jLogConsumer(logger));

    @Container
    public static LibertyContainer graphqlContainer
        = new LibertyContainer(graphqlImageName)
              .withNetwork(network)
              .withExposedPorts(9082)
              .withNetworkAliases("graphql")
              .withLogConsumer(new Slf4jLogConsumer(logger));

    @Container
    public static LibertyContainer libertyContainer
        = new LibertyContainer(queryImageName)
              .withNetwork(network)
              .withExposedPorts(9084)
              .withLogConsumer(new Slf4jLogConsumer(logger));

    @BeforeAll
    public static void setupTestClass() throws Exception {
        System.out.println("TEST: Starting Liberty Container setup");
        client = libertyContainer.createRestClient(QueryResourceClient.class);
    }

    @Test
    @Order(1)
    public void testGetSystem() {
        System.out.println("TEST: Testing get system /system/system-java11");
        SystemInfo systemInfo = client.querySystem("system-java11");
        assertEquals(systemInfo.getHostname(), "system-java11");
        assertNotNull(systemInfo.getOsVersion(), "osVersion is null");
        assertNotNull(systemInfo.getJava(), "java is null");
        assertNotNull(systemInfo.getSystemMetrics(), "systemMetrics is null");
    }

    @Test
    @Order(2)
    public void testGetSystemLoad() {
        System.out.println("TEST: Testing get system load /systemLoad/system-java11");
        List<SystemLoad> systemLoad = client.querySystemLoad("system-java11");
        assertEquals(systemLoad.get(0).getHostname(), "system-java11");
        SystemLoadData systemLoadData = systemLoad.get(0).getLoadData();
        assertNotNull(systemLoadData.getLoadAverage(), "loadAverage is null");
        assertNotNull(systemLoadData.getHeapUsed(), "headUsed is null");
        assertNotNull(systemLoadData.getNonHeapUsed(), "nonHeapUsed is null");
    }

    @Test
    @Order(3)
    public void testEditNote() {
        System.out.println("TEST: Testing editing note /mutation/system/note");
        NoteInfo note = new NoteInfo();
        note.setHostname("system-java11");
        note.setText("I am trying out GraphQL on Open Liberty!");
        Response response = client.editNote(note);
        assertEquals(200, response.getStatus(), "Incorrect response code");
        SystemInfo systemInfo = client.querySystem("system-java11");
        assertEquals(systemInfo.getNote(), "I am trying out GraphQL on Open Liberty!");
    }
}
```



Define the ***systemContainer*** test container to start up the ***system-java11*** image, the ***graphqlContainer*** test container to start up the ***graphql*** image, and the ***libertyContainer*** test container to start up the ***query*** image. Make sure that the containers use the same network.

The ***@Testcontainers*** annotation finds all fields that are annotated with the ***@Container*** annotation and calls their container lifecycle methods. The ***static*** function declaration on each container indicates that this container will be started only once before any test method is executed and stopped after the last test method is executed.

The ***testGetSystem()*** verifies the ***/query/system/{hostname}*** endpoint with ***hostname*** set to ***system-java11***.

The ***testGetSystemLoad()*** verifies the ***/query/systemLoad/{hostnames}*** endpoint with ***hostnames*** set to ***system-java11***.

The ***testEditNote()*** verifies the mutation operation at the ***/query/mutation/system/note*** endpoint.


The required ***dependencies*** are already added to the ***pom.xml*** Maven configuration file for you, including JUnit5, JBoss RESTEasy client, Glassfish JSON, Testcontainers, and Log4J libraries.

To enable running the integration test by the Maven ***verify*** goal, the ***maven-failsafe-plugin*** plugin is also required.

### Running the tests

You can run the Maven ***verify*** goal, which compiles the java files, starts the containers, runs the tests, and then stops the containers.


```bash
cd /home/project/guide-graphql-client/start/query
export TESTCONTAINERS_RYUK_DISABLED=true
mvn verify
```

You will see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.query.QueryResourceIT
...
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 11.694 s - in it.io.openliberty.guides.query.QueryResourceIT

Results :

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```


::page{title="Summary"}

### Nice Work!

You just learnt how to use a GraphQL client to run GraphQL queries and mutations!




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-graphql-client*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-graphql-client
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Running%20GraphQL%20queries%20and%20mutations%20using%20a%20GraphQL%20client&guide-id=cloud-hosted-guide-graphql-client)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-graphql-client/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-graphql-client/pulls)



### Where to next?

* [Optimizing REST queries for microservices with GraphQL](https://openliberty.io/guides/microprofile-graphql.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
