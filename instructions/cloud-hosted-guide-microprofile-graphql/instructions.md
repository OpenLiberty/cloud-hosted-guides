---
markdown-version: v1
title: instructions
branch: lab-444-instruction
version-history-start-date: 2021-12-03 22:01:00 UTC
tool-type: theia
---
::page{title="Welcome to the Optimizing REST queries for microservices with GraphQL guide!"}



In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.


Learn how to use MicroProfile GraphQL to query and update data from multiple services, and how to test GraphQL queries and mutations using an interactive GraphQL tool (GraphiQL).


::page{title="What you'll learn"}

You will learn how to build and use a simple GraphQL service with [MicroProfile GraphQL](https://openliberty.io/docs/latest/reference/feature/mpGraphQL-2.0.html). 

GraphQL is an open source data query language. Unlike REST APIs, each HTTP request that is sent to a GraphQL service goes to a single HTTP endpoint. Create, read, update, and delete operations and their details are differentiated by the contents of the request. If the operation returns data, the user specifies what properties of the data that they want returned. For read operations, a JSON object is returned that contains only the data and properties that are specified. For other operations, a JSON object might be returned containing information such as a success message. 

Returning only the specified properties in a read operation has two benefits. If you're dealing with large amounts of data or large resources, it reduces the size of the responses. If you have properties that are expensive to calculate or retrieve (such as nested objects), it also saves processing time. GraphQL calculates these properties only if they are requested. 

A GraphQL service can also be used to obtain data from multiple sources such as APIs, databases, and other services. It can then collate this data into a single object for the user, simplifying the data retrieval. The user makes only a single request to the GraphQL service, instead of multiple requests to the individual data sources. GraphQL services require less data fetching than REST services, which results in lower application load times and lower data transfer costs. GraphQL also enables clients to better customize requests to the server.

All of the available operations to retrieve or modify data are available in a single GraphQL schema. The GraphQL schema describes all the data types that are used in the GraphQL service. The schema also describes all of the available operations. As well, you can add names and text descriptions to the various object types and operations in the schema.

You can learn more about GraphQL at the [GraphQL website](https://graphql.org/).

You'll create a GraphQL application that retrieves data from multiple ***system*** services. Users make requests to the GraphQL service, which then makes requests to the ***system*** services. The GraphQL service returns a single JSON object containing all the system information from the ***system*** services.

![GraphQL architecture where multiple system microservices are integrated behind one GraphQL service](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-graphql/prod/assets/architecture.png)


You'll enable the interactive [GraphiQL](https://github.com/graphql/graphiql/tree/main/packages/graphiql) tool in the Open Liberty runtime. GraphiQL helps you make queries to a GraphQL service. In the GraphiQL UI, you need to type only the body of the query for the purposes of manual tests and examples. 


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-graphql.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microprofile-graphql.git
cd guide-microprofile-graphql
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.



::page{title="Creating GraphQL object types"}

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-microprofile-graphql/start
```

Object types determine the structure of the data that GraphQL returns. These object types are defined by annotations that are applied to the declaration and properties of Java classes. 

You will define ***java***, ***systemMetrics***, and ***systemInfo*** object types by creating and applying annotations to the ***JavaInfo***, ***SystemMetrics***, and ***SystemInfo*** classes respectively. 

Create the ***JavaInfo*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-graphql/start/models/src/main/java/io/openliberty/guides/graphql/models/JavaInfo.java
```


> Then, to open the JavaInfo.java file in your IDE, select
> **File** > **Open** > guide-microprofile-graphql/start/models/src/main/java/io/openliberty/guides/graphql/models/JavaInfo.java, or click the following button

::openFile{path="/home/project/guide-microprofile-graphql/start/models/src/main/java/io/openliberty/guides/graphql/models/JavaInfo.java"}



```java
package io.openliberty.guides.graphql.models;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Type;

@Type("java")
@Description("Information about a Java installation")
public class JavaInfo {

    @Name("vendorName")
    private String vendor;

    @NonNull
    private String version;

    public String getVendor() {
        return this.vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***JavaInfo*** class is annotated with a ***@Type*** annotation. The ***@Type("java")*** annotation maps this class to define the ***java*** object type in GraphQL. The ***java*** object type gives information on the Java installation of the system. 

The ***@Description*** annotation gives a description to the ***java*** object type in GraphQL. This description is what appears in the schema and the documentation. Descriptions aren't required, but it's good practice to include them. 

The ***@Name*** annotation maps the ***vendor*** property to the ***vendorName*** name of the ***java*** object type in GraphQL. The ***@Name*** annotation can be used to change the name of the property used in the schema. Without a ***@Name*** annotation, the Java object property is automatically mapped to a GraphQL object type property of the same name. In this case, without the ***@Name*** annotation, the property would be displayed as ***vendor*** in the schema.

All data types in GraphQL are nullable by default. Non-nullable properties are annotated with the ***@NonNull*** annotation. The ***@NonNull*** annotation on the ***version*** field ensures that, when queried, a non-null value is returned by the GraphQL service. The ***getVendor()*** and ***getVersion()*** getter functions are automatically mapped to retrieve their respective properties in GraphQL. If needed, setter functions are also supported and automatically mapped. 

Create the ***SystemMetrics*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-graphql/start/models/src/main/java/io/openliberty/guides/graphql/models/SystemMetrics.java
```


> Then, to open the SystemMetrics.java file in your IDE, select
> **File** > **Open** > guide-microprofile-graphql/start/models/src/main/java/io/openliberty/guides/graphql/models/SystemMetrics.java, or click the following button

::openFile{path="/home/project/guide-microprofile-graphql/start/models/src/main/java/io/openliberty/guides/graphql/models/SystemMetrics.java"}



```java
package io.openliberty.guides.graphql.models;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Type;

@Type("systemMetrics")
@Description("System metrics")
public class SystemMetrics {

    @NonNull
    private Integer processors;

    @NonNull
    private Long heapSize;

    @NonNull
    private Long nonHeapSize;

    public Integer getProcessors() {
        return processors;
    }

    public void setProcessors(int processors) {
        this.processors = processors;
    }

    public Long getHeapSize() {
        return heapSize;
    }

    public void setHeapSize(long heapSize) {
        this.heapSize = heapSize;
    }

    public Long getNonHeapSize() {
        return nonHeapSize;
    }

    public void setNonHeapSize(Long nonHeapSize) {
        this.nonHeapSize = nonHeapSize;
    }

}
```



The ***SystemMetrics*** class is set up similarly. It maps to the ***systemMetrics*** object type, which describes system information such as the number of processor cores and the heap size.

Create the ***SystemInfo*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-graphql/start/models/src/main/java/io/openliberty/guides/graphql/models/SystemInfo.java
```


> Then, to open the SystemInfo.java file in your IDE, select
> **File** > **Open** > guide-microprofile-graphql/start/models/src/main/java/io/openliberty/guides/graphql/models/SystemInfo.java, or click the following button

::openFile{path="/home/project/guide-microprofile-graphql/start/models/src/main/java/io/openliberty/guides/graphql/models/SystemInfo.java"}



```java
package io.openliberty.guides.graphql.models;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Type;

@Type("system")
@Description("Information about a single system")
public class SystemInfo {

    @NonNull
    private String hostname;

    @NonNull
    private String username;

    private String osName;
    private String osArch;
    private String osVersion;
    private String note;

    private JavaInfo java;

    private SystemMetrics systemMetrics;

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setOsArch(String osarch) {
        this.osArch = osarch;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public JavaInfo getJava() {
        return java;
    }

    public void setJava(JavaInfo java) {
        this.java = java;
    }

    public SystemMetrics getSystemMetrics() {
        return systemMetrics;
    }

    public void setSystemMetrics(SystemMetrics systemMetrics) {
        this.systemMetrics = systemMetrics;
    }

}
```



The ***SystemInfo*** class is similar to the previous two classes. It maps to the ***system*** object type, which describes other information Java can retrieve from the system properties.

The ***java*** and ***systemMetrics*** object types are used as nested objects within the ***system*** object type. However, nested objects and other properties that are expensive to calculate or retrieve are not included in the class of an object type. Instead, expensive properties are added as part of implementing GraphQL resolvers. 



To save time, the ***SystemLoad*** class and ***SystemLoadData*** class are provided for you. The ***SystemLoad*** class maps to the ***systemLoad*** object type, which describes the resource usage of a ***system*** service. The ***SystemLoadData*** class maps to the ***loadData*** object type. The ***loadData*** object will be a nested object inside the ***systemLoad*** object type. Together, these objects will contain the details of the resource usage of a ***system*** service.


::page{title="Implementing system service"}

The ***system*** microservices are backend services that use Jakarta Restful Web Services. For more details on using Jakarta Restful Web Services, see the [Creating a RESTful web service guide](https://www.openliberty.io/guides/rest-intro.html). These ***system*** microservices report system properties. GraphQL can access multiple instances of these ***system*** microservices and collate their information. In a real scenario, GraphQL might access multiple databases or other services.

Create the ***SystemPropertiesResource*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-graphql/start/system/src/main/java/io/openliberty/guides/system/SystemPropertiesResource.java
```


> Then, to open the SystemPropertiesResource.java file in your IDE, select
> **File** > **Open** > guide-microprofile-graphql/start/system/src/main/java/io/openliberty/guides/system/SystemPropertiesResource.java, or click the following button

::openFile{path="/home/project/guide-microprofile-graphql/start/system/src/main/java/io/openliberty/guides/system/SystemPropertiesResource.java"}



```java
package io.openliberty.guides.system;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.openliberty.guides.graphql.models.JavaInfo;

@ApplicationScoped
@Path("/")
public class SystemPropertiesResource {

    @GET
    @Path("properties/{property}")
    @Produces(MediaType.TEXT_PLAIN)
    public String queryProperty(@PathParam("property") String property) {
        return System.getProperty(property);
    }

    @GET
    @Path("properties/java")
    @Produces(MediaType.APPLICATION_JSON)
    public JavaInfo java() {
        JavaInfo javaInfo = new JavaInfo();
        javaInfo.setVersion(System.getProperty("java.version"));
        javaInfo.setVendor(System.getProperty("java.vendor"));
        return javaInfo;
    }

    @POST
    @Path("note")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editNote(String text) {
        System.setProperty("note", text);
        return Response.ok().build();
    }

}
```



The ***SystemPropertiesResource*** class provides endpoints to interact with the system properties. The ***properties/{property}*** endpoint accesses system properties. The ***properties/java*** endpoint assembles and returns an object describing the system's Java installation. The ***note*** endpoint is used to write a note into the system properties.

Create the ***SystemMetricsResource*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-graphql/start/system/src/main/java/io/openliberty/guides/system/SystemMetricsResource.java
```


> Then, to open the SystemMetricsResource.java file in your IDE, select
> **File** > **Open** > guide-microprofile-graphql/start/system/src/main/java/io/openliberty/guides/system/SystemMetricsResource.java, or click the following button

::openFile{path="/home/project/guide-microprofile-graphql/start/system/src/main/java/io/openliberty/guides/system/SystemMetricsResource.java"}



```java
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.openliberty.guides.graphql.models.SystemLoadData;
import io.openliberty.guides.graphql.models.SystemMetrics;

@ApplicationScoped
@Path("metrics")
public class SystemMetricsResource {

    private static final OperatingSystemMXBean OS_MEAN =
                             ManagementFactory.getOperatingSystemMXBean();

    private static final MemoryMXBean MEM_BEAN = ManagementFactory.getMemoryMXBean();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SystemMetrics getSystemMetrics() {
        SystemMetrics metrics = new SystemMetrics();
        metrics.setProcessors(OS_MEAN.getAvailableProcessors());
        metrics.setHeapSize(MEM_BEAN.getHeapMemoryUsage().getMax());
        metrics.setNonHeapSize(MEM_BEAN.getNonHeapMemoryUsage().getMax());
        return metrics;
    }

    @GET
    @Path("/systemLoad")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemLoadData getSystemLoad() {
        SystemLoadData systemLoadData = new SystemLoadData();
        systemLoadData.setLoadAverage(OS_MEAN.getSystemLoadAverage());
        systemLoadData.setHeapUsed(MEM_BEAN.getHeapMemoryUsage().getUsed());
        systemLoadData.setNonHeapUsed(MEM_BEAN.getNonHeapMemoryUsage().getUsed());
        return systemLoadData;
    }
}
```



The ***SystemMetricsResource*** class provides information on the system resources and their usage. The ***systemLoad*** endpoint assembles and returns an object that describes the system load. It includes the JVM heap load and processor load.


::page{title="Implementing GraphQL resolvers"}

Resolvers are functions that provide instructions for GraphQL operations. Each operation requires a corresponding resolver. The ***query*** operation type is read-only and fetches data. The ***mutation*** operation type can create, delete, or modify data. 

Create the ***GraphQLService*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-graphql/start/graphql/src/main/java/io/openliberty/guides/graphql/GraphQLService.java
```


> Then, to open the GraphQLService.java file in your IDE, select
> **File** > **Open** > guide-microprofile-graphql/start/graphql/src/main/java/io/openliberty/guides/graphql/GraphQLService.java, or click the following button

::openFile{path="/home/project/guide-microprofile-graphql/start/graphql/src/main/java/io/openliberty/guides/graphql/GraphQLService.java"}



```java
package io.openliberty.guides.graphql;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import io.openliberty.guides.graphql.client.SystemClient;
import io.openliberty.guides.graphql.client.UnknownUriException;
import io.openliberty.guides.graphql.client.UnknownUriExceptionMapper;
import io.openliberty.guides.graphql.models.JavaInfo;
import io.openliberty.guides.graphql.models.SystemInfo;
import io.openliberty.guides.graphql.models.SystemLoad;
import io.openliberty.guides.graphql.models.SystemLoadData;
import io.openliberty.guides.graphql.models.SystemMetrics;

@GraphQLApi
public class GraphQLService {

    private static Map<String, SystemClient> clients =
            Collections.synchronizedMap(new HashMap<String, SystemClient>());

    @Inject
    @ConfigProperty(name = "system.http.port", defaultValue = "9080")
    String SYSTEM_PORT;

    @Query("system")
    @NonNull
    @Description("Gets information about the system")
    public SystemInfo getSystemInfo(@Name("hostname") String hostname)
        throws ProcessingException, UnknownUriException {
        SystemClient systemClient = getSystemClient(hostname);
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setHostname(hostname);
        systemInfo.setUsername(systemClient.queryProperty("user.name"));
        systemInfo.setOsName(systemClient.queryProperty("os.name"));
        systemInfo.setOsArch(systemClient.queryProperty("os.arch"));
        systemInfo.setOsVersion(systemClient.queryProperty("os.version"));
        systemInfo.setNote(systemClient.queryProperty("note"));

        return systemInfo;
    }

    @Mutation("editNote")
    @Description("Changes the note set for the system")
    public boolean editNote(@Name("hostname") String hostname,
                            @Name("note") String note)
        throws ProcessingException, UnknownUriException {
        SystemClient systemClient = getSystemClient(hostname);
        systemClient.editNote(note);
        return true;
    }

    @Query("systemLoad")
    @Description("Gets system load data from the systems")
    public SystemLoad[] getSystemLoad(@Name("hostnames") String[] hostnames)
        throws ProcessingException, UnknownUriException {
        if (hostnames == null || hostnames.length == 0) {
            return new SystemLoad[0];
        }

        List<SystemLoad> systemLoads = new ArrayList<SystemLoad>(hostnames.length);

        for (String hostname : hostnames) {
            SystemLoad systemLoad = new SystemLoad();
            systemLoad.setHostname(hostname);
            systemLoads.add(systemLoad);
        }

        return systemLoads.toArray(new SystemLoad[systemLoads.size()]);
    }

    @NonNull
    public SystemMetrics systemMetrics(
        @Source @Name("system") SystemInfo systemInfo)
        throws ProcessingException, UnknownUriException {
        String hostname = systemInfo.getHostname();
        SystemClient systemClient = getSystemClient(hostname);
        return systemClient.getSystemMetrics();
    }

    @NonNull
    public JavaInfo java(@Source @Name("system") SystemInfo systemInfo)
        throws ProcessingException, UnknownUriException {
        String hostname = systemInfo.getHostname();
        SystemClient systemClient = getSystemClient(hostname);
        return systemClient.java();
    }

    public SystemLoadData loadData(@Source @Name("systemLoad") SystemLoad systemLoad)
        throws ProcessingException, UnknownUriException {
        String hostname = systemLoad.getHostname();
        SystemClient systemClient = getSystemClient(hostname);
        return systemClient.getSystemLoad();
    }

    private SystemClient getSystemClient(String hostname) {
        SystemClient sc = clients.get(hostname);
        if (sc == null) {
            String customURIString = "http://" + hostname + ":"
                                      + SYSTEM_PORT + "/system";
            URI customURI = URI.create(customURIString);
            sc = RestClientBuilder
                   .newBuilder()
                   .baseUri(customURI)
                   .register(UnknownUriExceptionMapper.class)
                   .build(SystemClient.class);
            clients.put(hostname, sc);
        }
        return sc;
    }
}
```



The resolvers are defined in the ***GraphQLService.java*** file. The ***@GraphQLApi*** annotation enables GraphQL to use the methods that are defined in this class as resolvers.

Operations of the ***query*** type are read-only operations that retrieve data. They're defined by using the ***@Query*** annotation.

One of the ***query*** requests in this application is the ***system*** request. This request is handled by the ***getSystemInfo()*** function. It retrieves and bundles system information into a ***SystemInfo*** object that is returned.

It uses a ***@Name*** on one of its input parameters. The ***@Name*** annotation has different functions depending on the context in which it's used. In this context, it denotes input parameters for GraphQL operations. For the ***getSystemInfo()*** function, it's used to input the ***hostname*** for the system you want to look up information for.

Recall that the ***SystemInfo*** class contained nested objects. It contained a ***JavaInfo*** and an ***SystemMetrics*** object. The ***@Source*** annotation is used to add these nested objects as properties to the ***SystemInfo*** object.

The ***@Name*** appears again here. In this context alongside the ***@Source*** annotation, it's used to connect the ***java*** and ***systemMetrics*** object types to ***system*** requests and the ***system*** object type.

The other ***query*** request is the ***systemLoad*** request, which is handled by the ***getSystemLoad()*** function. The ***systemLoad*** request retrieves information about the resource usage of any number of system services. It accepts an array of ***hostnames*** as the input for the systems to look up. It's set up similarly to the ***system*** request, with the ***loadData*** function used for the nested ***SystemLoadData*** object.

Operations of the ***mutation*** type are used to edit data. They can create, update, or delete data. They're defined by using the ***@Mutation*** annotation.

There's one ***mutation*** operation in this application - the ***editNote*** request. This request is handled by the ***editNote()*** function. This request is used to write a note into the properties of a given system. There are inputs for the system you want to write into, and the note you want to write.

Each resolver function has a ***@Description*** annotation, which provides a description that is used for the schema. Descriptions aren't required, but it's good practice to include them. 


::page{title="Enabling GraphQL"}

To use GraphQL, the MicroProfile GraphQL dependencies and features need to be included. 

Replace the Maven project file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-microprofile-graphql/start/graphql/pom.xml, or click the following button

::openFile{path="/home/project/guide-microprofile-graphql/start/graphql/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-microprofile-graphql-graphql</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- Liberty configuration -->
        <liberty.var.http.port>9082</liberty.var.http.port>
        <liberty.var.https.port>9445</liberty.var.https.port>
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
            <version>6.1</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        
        <!-- Required dependencies -->
        <dependency>
           <groupId>io.openliberty.guides</groupId>
           <artifactId>guide-microprofile-graphql-models</artifactId>
           <version>1.0-SNAPSHOT</version>
        </dependency>
        
        <!-- GraphQL API dependencies -->
        <dependency>
            <groupId>org.eclipse.microprofile.graphql</groupId>
            <artifactId>microprofile-graphql-api</artifactId>
            <version>2.0</version>
            <scope>provided</scope>
        </dependency>
        <!-- For tests -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-client</artifactId>
            <version>6.2.8.Final</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-json-binding-provider</artifactId>
            <version>6.2.8.Final</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>jakarta.json</artifactId>
            <version>2.0.1</version>
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
                <version>3.10.2</version>
                <configuration>
                    <looseApplication>false</looseApplication>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
            <!-- Plugin to run functional tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.2.5</version>
                <configuration>
                    <systemPropertyVariables>
                        <http.port>${liberty.var.http.port}</http.port>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```



Adding the ***microprofile-graphql-api*** dependency to the ***pom.xml*** enables the GraphQL annotations that are used to develop the application. 

The Open Liberty needs to be configured to support the GraphQL query language. 

Replace the Liberty server.xml configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-microprofile-graphql/start/graphql/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-microprofile-graphql/start/graphql/src/main/liberty/config/server.xml"}



```xml
<server description="GraphQL service">
    <featureManager>
        <feature>restfulWS-3.1</feature>
        <feature>jsonb-3.0</feature>
        <feature>jsonp-2.1</feature>
        <feature>cdi-4.0</feature>
        <feature>mpConfig-3.1</feature>
        <feature>mpRestClient-3.0</feature>
        <feature>mpGraphQL-2.0</feature>
    </featureManager>

    <variable name="http.port" defaultValue="9082"/>
    <variable name="https.port" defaultValue="9445"/>

    <variable name="io.openliberty.enableGraphQLUI" value="true" />

    <webApplication location="guide-microprofile-graphql-graphql.war" contextRoot="/" />
    <httpEndpoint host="*" httpPort="${http.port}" 
        httpsPort="${https.port}" id="defaultHttpEndpoint"/>
</server>
```



The ***mpGraphQL*** feature that is added to the ***server.xml*** enables the use of the [MicroProfile GraphQL](https://openliberty.io/docs/latest/reference/feature/mpGraphQL-2.0.html) feature in Open Liberty. Open Liberty's MicroProfile GraphQL feature includes GraphiQL. Enable it by setting the ***io.openliberty.enableGraphQLUI*** variable to ***true***.


::page{title="Building and running the application"}

From the ***start*** directory, run the following commands:

```bash
mvn -pl models install
mvn package
```

The ***mvn install*** command compiles and packages the object types you created to a ***.jar*** file. This allows them to be used by the ***system*** and ***graphql*** services. The ***mvn package*** command packages the ***system*** and ***graphql*** services to ***.war*** files.



Dockerfiles have already been set up for you. Build your Docker images with the following commands:

```bash
docker build -t system:1.0-java11-SNAPSHOT --build-arg JAVA_VERSION=java11 system/.
docker build -t system:1.0-java17-SNAPSHOT --build-arg JAVA_VERSION=java17 system/.
docker build -t graphql:1.0-SNAPSHOT graphql/.
```

The ***--build-arg*** parameter is used to create two different ***system*** services. One uses Java 11, while the other uses Java 17. Run these Docker images using the provided ***startContainers*** script. The script creates a network for the services to communicate through. It creates two ***system*** services and a GraphQL service.


```bash
./scripts/startContainers.sh
```

The containers may take some time to become available.


::page{title="Running GraphQL queries"}
Before you make any requests, select **Terminal** > **New Terminal** from the menu of the IDE to open another command-line session. Run the following command to get the schema that describes the GraphQL service:
```bash
curl -s http://localhost:9082/graphql/schema.graphql
```

To access the GraphQL service, GraphiQL has already been set up and included for you.
Click the following button to access GraphiQL:

::startApplication{port="9082" display="external" name="Launch GraphiQL" route="/graphql-ui"}

Queries that are made through GraphiQL are the same as queries that are made through HTTP requests. You can also view the schema through GraphiQL by clicking the ***Docs*** button on the menu bar.

Run the following ***query*** operation in GraphiQL to get every system property from the container running on Java 11:


```
query {
  system(hostname: "system-java11") {
    hostname
    username
    osArch
    osName
    osVersion
    systemMetrics {
      processors
      heapSize
      nonHeapSize
    }
    java {
      vendorName
      version
    }
  }
}
```


The output is similar to the following example:

```
{
  "data": {
    "system": {
      "hostname": "system-java11",
      "username": "default",
      "osArch": "amd64",
      "osName": "Linux",
      "osVersion": "5.10.25-linuxkit",
      "systemMetrics": {
        "processors": 4,
        "heapSize": 1031864320,
        "nonHeapSize": -1
      },
      "java": {
        "vendorName": "AdoptOpenJDK",
        "version": "11.0.18"
      }
    }
  }
}
```

Run the following ***mutation*** operation to add a note to the ***system*** service running on Java 11:


```
mutation {
  editNote(
    hostname: "system-java11"
    note: "I'm trying out GraphQL on Open Liberty!"
  )
}
```

You receive a response containing the Boolean ***true*** to let you know that the request was successfully processed. You can see the note that you added by running the following query operation. Notice that there's no need to run a full query, as you only want the ***note*** property. Thus, the request only contains the ***note*** property. 


```bash
query {
  system(hostname: "system-java11") {
    note
  }
}
```

The response is similar to the following example:

```
{
  "data": {
    "system": {
      "note": "I'm trying out GraphQL on Open Liberty!"
    }
  }
}
```

GraphQL returns only the ***note*** property, as it was the only property in the request. You can try out the operations using the hostname ***system-java17*** as well. To see an example of using an array as an input for an operation, try the following operation to get system loads:


```
query {
  systemLoad(hostnames: ["system-java11", "system-java17"]) {
    hostname
    loadData {
      heapUsed
      nonHeapUsed
      loadAverage
    }
  }
}
```

The response is similar to the following example:

```
{
  "data": {
    "systemLoad": [
      {
        "hostname": "system-java11",
        "loadData": {
          "heapUsed": 32432048,
          "nonHeapUsed": 85147084,
          "loadAverage": 0.36
        }
      },
      {
        "hostname": "system-java17",
        "loadData": {
          "heapUsed": 39373688,
          "nonHeapUsed": 90736300,
          "loadAverage": 0.36
        }
      }
    ]
  }
}
```


::page{title="Tearing down the environment"}

When you're done checking out the application, run the following script to stop the application:


```bash
./scripts/stopContainers.sh
```

::page{title="Summary"}

### Nice Work!

You just created a basic GraphQL service using MicroProfile GraphQL in Open Liberty!




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-graphql*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-graphql
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Optimizing%20REST%20queries%20for%20microservices%20with%20GraphQL&guide-id=cloud-hosted-guide-microprofile-graphql)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-graphql/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-graphql/pulls)



### Where to next?

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Running GraphQL queries and mutations using a GraphQL client](https://openliberty.io/guides/graphql-client.html)
* [Accessing and persisting data in microservices using Java Persistence API (JPA)](https://openliberty.io/guides/jpa-intro.html)
* [Persisting data with MongoDB](https://openliberty.io/guides/mongodb-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
