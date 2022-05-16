---
markdown-version: v1
title: instructions
branch: lab-557-instruction
version-history-start-date: 2022-05-03T13:58:19Z
---
::page{title="Welcome to the Running GraphQL queries and mutations using a GraphQL client guide!"}



In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.


Learn how to use a GraphQL client to run GraphQL queries and mutations.

::page{title="What you'll learn"}

GraphQL is an open source data query language. Unlike REST APIs, each request sent to a GraphQL service goes to a single HTTP endpoint. To learn more about GraphQL, see the [Optimizing REST queries for microservices with GraphQL](https://openliberty.io/guides/microprofile-graphql.html) guide.

You will start with the ***graphql*** microservice created as part of the [Optimizing REST queries for microservices with GraphQL](https://openliberty.io/guides/microprofile-graphql.html) guide. Then, you'll use the [SmallRye GraphQL client](https://github.com/smallrye/smallrye-graphql#client) to create a ***query*** microservice that will make requests to the GraphQL microservice. The GraphQL microservice retrieves data from multiple ***system*** microservices. 

The results of the requests will be displayed at REST endpoints. OpenAPI will be used to help make the requests and display the data. To learn more about OpenAPI, check out the https://openliberty.io/guides/microprofile-openapi.html[Documenting RESTful APIs] guide.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/draft-guide-graphql-client.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/draft-guide-graphql-client.git
cd draft-guide-graphql-client
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.


::page{title="Implementing a GraphQL client"}

Navigate to the ***start*** directory to begin.

The [SmallRye GraphQL client](https://github.com/smallrye/smallrye-graphql#client) will be used to implement the GraphQL client service. SmallRye GraphQL client supports two types of clients: typesafe and dynamic clients. A typesafe client provides ease of use and a high-level approach, while a dynamic client provides a more customizable and low-level approach to handle operations and responses. You will implement a typesafe client microservice. 

The typesafe client interface contains a function for each resolver available in the ***graphql*** microservice. The JSON objects returned by the ***graphql*** microservice are converted to Java objects.

Create the ***GraphQlClient*** interface.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-graphql-client/start/query/src/main/java/io/openliberty/guides/client/GraphQlClient.java
```


> Then, to open the GraphQlClient.java file in your IDE, select
> **File** > **Open** > draft-guide-graphql-client/start/query/src/main/java/io/openliberty/guides/client/GraphQlClient.java, or click the following button

::openFile{path="/home/project/draft-guide-graphql-client/start/query/src/main/java/io/openliberty/guides/client/GraphQlClient.java"}



```java
package io.openliberty.guides.query.client;

import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;

import io.openliberty.guides.graphql.models.SystemInfo;
import io.openliberty.guides.graphql.models.SystemLoad;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;

@GraphQlClientApi
public interface GraphQlClient {

    SystemInfo system(@Name("hostname") String hostname);

    SystemLoad[] systemLoad(@Name("hostnames") String[] hostnames);

    @Mutation
    boolean editNote(@Name("hostname") String hostname, @Name("note") String note);

}
```



The ***GraphQlClient*** interface is annotated with the ***@GraphQlClientApi*** annotation. This annotation denotes that this interface is used to create a typesafe GraphQL client.

Inside the interface, a function header is written for each resolver available in the ***graphql*** microservice. The names of the functions match the names of the resolvers in the GraphQL schema. Resolvers that require input variables have the input variables passed in using the ***@Name*** annotation on the function inputs. The return types of the functions should match those of the GraphQL resolvers.

For example, the ***system()*** function maps to the ***system*** resolver. The resolver returns a ***SystemInfo*** object, which is described by the ***SystemInfo*** class. Thus, the ***system()*** function returns the type ***SystemInfo***.

Because the ***editNote*** resolver is for a ***mutation*** operation, it has the ***@Mutation*** annotation on it. A ***mutation*** operation allows you to modify data, in this case, it allows you to add and edit a note to the system service. If the ***@Mutation*** annotation was not placed on the function, it would be treated as if it mapped to a ***query*** operation.

Create the ***QueryResource*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-graphql-client/start/query/src/main/java/io/openliberty/guides/client/QueryResource.java
```


> Then, to open the QueryResource.java file in your IDE, select
> **File** > **Open** > draft-guide-graphql-client/start/query/src/main/java/io/openliberty/guides/client/QueryResource.java, or click the following button

::openFile{path="/home/project/draft-guide-graphql-client/start/query/src/main/java/io/openliberty/guides/client/QueryResource.java"}



```java
package io.openliberty.guides.query;

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.openliberty.guides.graphql.models.SystemInfo;
import io.openliberty.guides.graphql.models.SystemLoad;
import io.openliberty.guides.graphql.models.NoteInfo;
import io.openliberty.guides.query.client.GraphQlClient;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientBuilder;

@ApplicationScoped
@Path("query")
public class QueryResource {

    private GraphQlClient gc = GraphQlClientBuilder.newBuilder()
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
        return gc.systemLoad(hostnameArray);
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



The ***QueryResource*** class uses the ***GraphQlClient*** interface to make requests to the ***graphql*** microservice and display the results. In a real application, you would be making requests to an external GraphQL API, or you would be doing further manipulation of the data after retrieval.

Use the ***GraphQLClientBuilder*** class to create a client object that implements the ***GraphQlClient*** interface and can interact with the ***graphql*** microservice. The ***GraphQlClient*** client can make requests to the URL specified by the ***graphql.server*** variable in the ***server.xml***. The client is used in the ***querySystem()***, ***querySystemLoad()***, and ***editNote()*** functions.


Replace the Maven project file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > draft-guide-graphql-client/start/query/pom.xml, or click the following button

::openFile{path="/home/project/draft-guide-graphql-client/start/query/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>query</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <liberty.var.default.http.port>9084</liberty.var.default.http.port>
        <liberty.var.default.https.port>9447</liberty.var.default.https.port>
    </properties>

    <dependencies>
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>8.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>4.0.1</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        
        <dependency>
           <groupId>io.openliberty.guides</groupId>
           <artifactId>models</artifactId>
           <version>1.0-SNAPSHOT</version>
        </dependency>
        
        <dependency>
            <groupId>io.smallrye</groupId>
            <artifactId>smallrye-graphql-client</artifactId>
            <version>1.1.2</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>1.7.30</version>
        </dependency>
        
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.5.13</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.7.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.cxf</groupId>
            <artifactId>cxf-rt-rs-client</artifactId>
            <version>3.4.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.cxf</groupId>
            <artifactId>cxf-rt-rs-extension-providers</artifactId>
            <version>3.4.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>javax.json</artifactId>
            <version>1.1.4</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse</groupId>
            <artifactId>yasson</artifactId>
            <version>1.0.9</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.3.4</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.1</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.2</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>2.22.2</version>
                <configuration>
                    <systemPropertyVariables>
                        <http.port>${liberty.var.default.http.port}</http.port>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```



The ***smallrye-graphql-client*** dependency provides the classes that use to interact with a ***graphql*** microservice.

To get the service running, the Liberty server needs to be correctly configured.

Replace the server configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > draft-guide-graphql-client/start/query/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/draft-guide-graphql-client/start/query/src/main/liberty/config/server.xml"}



```xml
<server description="Query Service">

  <featureManager>
    <feature>jaxrs-2.1</feature>
    <feature>cdi-2.0</feature>
    <feature>jsonb-1.0</feature>
    <feature>mpConfig-2.0</feature>
    <feature>mpOpenAPI-2.0</feature>
  </featureManager>

  <variable name="default.http.port" defaultValue="9084"/>
  <variable name="default.https.port" defaultValue="9447"/>
  <variable name="graphql.server" defaultValue="http://graphql:9082/graphql"/>

  <httpEndpoint host="*" httpPort="${default.http.port}"
      httpsPort="${default.https.port}" id="defaultHttpEndpoint"/>

  <webApplication location="query.war" contextRoot="/"/>
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

Run the following command to download or update to the latest Open Liberty Docker image:

```bash
docker pull icr.io/appcafe/open-liberty:full-java11-openj9-ubi
```

Dockerfiles have already been set up for you. Build your Docker images with the following commands:

```bash
docker build -t system:1.0-java8-SNAPSHOT --build-arg JAVA_VERSION=java8 system/.
docker build -t system:1.0-java11-SNAPSHOT --build-arg JAVA_VERSION=java11 system/.
docker build -t graphql:1.0-SNAPSHOT graphql/.
docker build -t query:1.0-SNAPSHOT query/.
```

Run these Docker images using the provided ***startContainers*** script. The script will create a network for the services to communicate through. It will create the two ***system*** microservices, a ***graphql*** microservice, and a ***query*** microservice that will interact with eachother.


```bash
./scripts/startContainers.sh
```

The containers may take some time to become available. 

::page{title="Accessing the application"}



Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.


To access the client service, visit the http://localhost:9084/openapi/ui/ URL. This URL displays the available REST endpoints that test the API endpoints that you created.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl http://localhost:9084/openapi/ui/
```



### Try the query operations

From the OpenAPI UI, test the read operation at the ***GET /query/system/{hostname}*** endpoint. This request retrieves the system properties for the ***hostname*** specified.

When the ***hostname*** is specified to ***system-java8***. The response is similar to the following example:

```
{
  "hostname": "system-java8",
  "java": {
    "vendor": "International Business Machines Corporation",
    "version": "1.8.0_312"
  },
  "osArch": "amd64",
  "osName": "Linux",
  "osVersion": "5.10.25-linuxkit",
  "systemMetrics": {
    "heapSize": 2086993920,
    "nonHeapSize": -1,
    "processors": 8
  },
  "username": "default"
}
```

You can try out the operations using the hostname ***system-java11*** as well. 

You can retrieve the information about the resource usage of any number of system services at the ***GET /query/systemLoad/{hostnames}*** endpoint. When the ***hostnames*** are specified to ***system-java8,system-java11***. The response is similar to the following example:

```
[
  {
    "hostname": "system-java8",
    "loadData": {
      "heapUsed": 34251904,
      "loadAverage": 0.11,
      "nonHeapUsed": 84034688
    }
  },
  {
    "hostname": "system-java11",
    "loadData": {
      "heapUsed": 41953280,
      "loadAverage": 0.11,
      "nonHeapUsed": 112506520
    }
  }
]
```

### Try the mutation operation

You can also make requests to add a note to a system service at the ***POST /query/mutation/system/note*** endpoint. To add a note to the system service running on Java 8, specify the following in the request body:

```bash
{
  "hostname": "system-java8",
  "text": "I'm trying out GraphQL on Open Liberty!"
}
```

You will recieve a ***200*** response code if the request is processed succesfully. 

::page{title="Tearing down the environment"}

When you're done checking out the application, run the following script to stop the application:


```bash
./scripts/stopContainers.sh
```

::page{title="Summary"}

### Nice Work!

You just learnt how to use a GraphQL client to run GraphQL queries and mutations!




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***draft-guide-graphql-client*** project by running the following commands:

```bash
cd /home/project
rm -fr draft-guide-graphql-client
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Running%20GraphQL%20queries%20and%20mutations%20using%20a%20GraphQL%20client&guide-id=cloud-hosted-draft-guide-graphql-client)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/draft-guide-graphql-client/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/draft-guide-graphql-client/pulls)



### Where to next?

* [Optimizing REST queries for microservices with GraphQL](https://openliberty.io/guides/microprofile-graphql.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
