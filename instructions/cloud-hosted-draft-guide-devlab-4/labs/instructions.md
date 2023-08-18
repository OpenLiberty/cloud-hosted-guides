---
markdown-version: v1
title: instructions
branch: lab-5933-instruction
version-history-start-date: 2023-04-14T18:24:15Z
tool-type: theia
---
::page{title="Welcome to the Building true-to-production integration tests with Testcontainers guide!"}

Learn how to test your microservices with multiple containers using Testcontainers and JUnit.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You'll learn how to write true-to-production integration tests for Java microservices by using [Testcontainers](https://www.testcontainers.org/) and JUnit. You'll learn to set up and configure multiple containers, including the Open Liberty Docker container, to simulate a production-like environment for your tests.

Sometimes tests might pass in development and testing (dev/test) environments, but fail in production because the application runs differently in production than in dev/test. Fortunately, you can minimize these differences between dev/test and production by testing your application in the same Docker containers that you'll use in production.

### What is Testcontainers?

Testcontainers is an open source library that wraps Docker in a Java API. It is often used in testing applications that involve external resource dependencies such as databases, message queues, or web services. Testcontainers supports any Docker image, which allows for uniform and portable testing environments. By encapsulating dependencies in containers, it ensures test consistency and simplifies the setup process.

The microservice that you'll be working with is called ***inventory***. The ***inventory*** microservice persists data into a PostgreSQL database and supports create, retrieve, update, and delete (CRUD) operations on the database records. You will write integration tests for the application by using Testcontainers to run it in Docker containers.

![Inventory microservice](https://raw.githubusercontent.com/OpenLiberty/draft-guide-testcontainers/draft/assets/inventory.png)


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/draft-guide-testcontainers.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/draft-guide-testcontainers.git
cd draft-guide-testcontainers
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

This guide uses Docker to run an instance of the PostgreSQL database for a fast installation and setup. A Dockerfile file is provided for you. Navigate to the ***postgres*** directory and run the following commands to use the Dockerfile to build the image:

```bash
cd postgres
docker build -t postgres-sample .
```

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the test, first go to the ***finish*** directory and run the ***mvn package*** command so that the ***.war*** file resides in the ***target*** directory, and the ***.jar*** PostgreSQL JDBC driver file resides in the ***target/liberty/wlp/usr/shared/resources*** directory:

```bash
cd ../finish
mvn package
```

Build the ***inventory*** Docker image with the following command:

```bash
docker build -t inventory:1.0-SNAPSHOT .
```

Now, run the Maven ***verify*** goal, which compiles the Java files, starts the containers, runs the tests, and then stops the containers.

```bash
export TESTCONTAINERS_RYUK_DISABLED=true
mvn verify
```

You will see the following output:

```
 -------------------------------------------------------
  T E S T S
 -------------------------------------------------------
 Running it.io.openliberty.guides.inventory.SystemResourceIT
 ...
 Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 10.118 s - in it.io.openliberty.guides.inventory.SystemResourceIT

 Results:

 Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

::page{title="Implementing integration tests to use Testcontainers"}

You'll develop integration tests capable of running in both [dev mode](https://openliberty.io/docs/latest/development-mode.html) and a CI/CD pipeline. These flexible tests ensure the application's reliability across diverse environments.

Navigate to the ***start*** directory to begin.

The PostgreSQL database is integral for the ***inventory*** microservice as it handles the persistence of data. Run the following command to start the PostgreSQL database, which runs the ***postgres-sample*** image in a Docker container and maps ***5432*** port from the container to your host machine:

```bash
docker run --name postgres-container --rm -p 5432:5432 -d postgres-sample
```

Retrieve the PostgreSQL container IP address by running the following command:

```bash
docker inspect -f "{{.NetworkSettings.IPAddress }}" postgres-container
```

The command returns the PostgreSQL container IP address:

```
172.17.0.2
```

The Open Liberty Maven plug-in includes a ***devc*** goal that simplifies developing your application in a container by starting dev mode with container support. This goal builds a Docker image, mounts the required directories, binds the required ports, and then runs the application inside of a container. Dev mode also listens for any changes in the application source code or configuration and rebuilds the image and restarts the container as necessary.

Build and run the container by running the ***devc*** goal with the PostgreSQL container IP address. If your PostgreSQL container IP address is not ***172.17.0.2***, replace the command with the right IP address.

```bash
mvn liberty:devc -DdockerRunOpts="-e DB_HOSTNAME=172.17.0.2" -DserverStartTimeout=240
```

You need to wait a while to let dev mode start. After you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
*    ...
*    Docker network information:
*        Container name: [ liberty-dev ]
*        IP address [ 172.17.0.3 ] on Docker network [ bridge ]
*    ...
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

Point your browser to the ***http\://localhost:9080/openapi/ui*** URL to try out the ***inventory*** microservice manually. This interface provides a convenient visual way to interact with the APIs and test out their functionalities. 

### Building test REST client

Test REST client is responsible for sending HTTP requests to an application and handling the responses. It enables accurate verification of the application's behavior by ensuring that it responds correctly to various scenarios and conditions. Using a REST client for testing ensures reliable interaction with the ***inventory*** microservice across various deployment environments: local, Docker container, or Testcontainers.

Begin by creating a test REST client interface for the ***inventory*** microservice.

Create the ***SystemResourceClient.java*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemResourceClient.java
```


> Then, to open the SystemResourceClient.java file in your IDE, select
> **File** > **Open** > draft-guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemResourceClient.java, or click the following button

::openFile{path="/home/project/draft-guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemResourceClient.java"}



```java
package it.io.openliberty.guides.inventory;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
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
    Response updateSystem(
        @PathParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize);

    @DELETE
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    Response removeSystem(
        @PathParam("hostname") String hostname);
}

```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***SystemResourceClient*** interface declares the ***listContents()***, ***getSystem()***, ***addSystem()***, ***updateSystem()***, and ***removeSystem()*** methods for accessing the corresponding endpoints within the ***inventory*** microservice.

Next, create the ***SystemData*** data model for testing.

Create the ***SystemData.java*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemData.java
```


> Then, to open the SystemData.java file in your IDE, select
> **File** > **Open** > draft-guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemData.java, or click the following button

::openFile{path="/home/project/draft-guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemData.java"}



```java
package it.io.openliberty.guides.inventory;

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

### Building Testcontainer for Open Liberty

Next, you'll learn to create a custom class that extends Testcontainers' generic container to define specific configurations that suit your application's requirements.

Define a custom ***LibertyContainer*** class, which provides a framework to start and access a containerized version of the Open Liberty application for testing.

Create the ***LibertyContainer.java*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/LibertyContainer.java
```


> Then, to open the LibertyContainer.java file in your IDE, select
> **File** > **Open** > draft-guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/LibertyContainer.java, or click the following button

::openFile{path="/home/project/draft-guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/LibertyContainer.java"}



```java
package it.io.openliberty.guides.inventory;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class LibertyContainer extends GenericContainer<LibertyContainer> {

    public LibertyContainer(
        String imageName, boolean testHttps, int httpsPort, int httpPort) {

        super(imageName);
        if (testHttps) {
            addExposedPorts(httpsPort, httpPort);
        } else {
            addExposedPorts(httpPort);
        }
        waitingFor(Wait.forLogMessage("^.*CWWKF0011I.*$", 1));

    }

    public String getBaseURL(String protocol) throws IllegalStateException {
        return protocol + "://" + getHost() + ":" + getFirstMappedPort();
    }

}
```



The ***LibertyContainer*** class extends the ***GenericContainer*** class from Testcontainers to create a custom container configuration specific to the Open Liberty application.

The ***addExposedPorts(port)*** method exposes specified ports from the container's perspective, allowing test clients to communicate with services running inside the container. To avoid any port conflicts, Testcontainers assigns random host ports to these exposed container ports. 

The ***Wait.forLogMessage()*** method directs ***LibertyContainer*** to wait for the specific ***CWWKF0011I*** log message that indicates the Liberty instance has started successfully.

For more information about Testcontainers APIs and its functionality, refer to the [Testcontainers JavaDocs](https://javadoc.io/doc/org.testcontainers/testcontainers/latest/index.html).

Now you can set up trace logging for your tests. 

Having reliable logs is essential for efficient debugging, as they provide detailed insights into the test execution flow and help pinpoint issues during test failures. Testcontainers' built-in ***Slf4jLogConsumer*** enables integration of container output directly with the JUnit process, enhancing log analysis and simplifying test creation and debugging.

Create the log4j properties to configure the logging behavior in your tests.

Create the ***log4j.properties*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-testcontainers/start/src/test/resources/log4j.properties
```


> Then, to open the log4j.properties file in your IDE, select
> **File** > **Open** > draft-guide-testcontainers/start/src/test/resources/log4j.properties, or click the following button

::openFile{path="/home/project/draft-guide-testcontainers/start/src/test/resources/log4j.properties"}



```
log4j.rootLogger=INFO, stdout

log4j.appender=org.apache.log4j.ConsoleAppender
log4j.appender.layout=org.apache.log4j.PatternLayout

log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%r %p %c %x - %m%n

log4j.logger.it.io.openliberty.guides.inventory=DEBUG
```



The ***log4j.properties*** file configures the root logger, appenders, and layouts for console output. It sets the logging level to ***DEBUG*** for the ***it.io.openliberty.guides.inventory*** package. This level provides detailed logging information for the specified package, which can be helpful for debugging and understanding test behavior.

### Building test cases

Next, you'll learn to write tests that use the ***SystemResourceClient*** REST client and the Testcontainers integration. You'll set up a multi-container test environment, determine the appropriate runtime environment to launch Testcontainers, and configure the test REST client for HTTPS communication.

Create the ***SystemResourceIT.java*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/draft-guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemResourceIT.java
```


> Then, to open the SystemResourceIT.java file in your IDE, select
> **File** > **Open** > draft-guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemResourceIT.java, or click the following button

::openFile{path="/home/project/draft-guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemResourceIT.java"}



```java
package it.io.openliberty.guides.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.jupiter.api.AfterAll;
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

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.UriBuilder;

@TestMethodOrder(OrderAnnotation.class)
public class SystemResourceIT {

    private static Logger logger = LoggerFactory.getLogger(SystemResourceIT.class);

    private static final String DB_HOST = "postgres";
    private static final int DB_PORT = 5432;
    private static final String DB_IMAGE = "postgres-sample:latest";

    private static int httpPort = Integer.parseInt(System.getProperty("http.port"));
    private static int httpsPort = Integer.parseInt(System.getProperty("https.port"));
    private static String contextRoot = System.getProperty("context.root") + "/api";
    private static String invImage = "inventory:1.0-SNAPSHOT";

    private static SystemResourceClient client;
    private static Network network = Network.newNetwork();

    private static GenericContainer<?> postgresContainer
        = new GenericContainer<>(DB_IMAGE)
              .withNetwork(network)
              .withExposedPorts(DB_PORT)
              .withNetworkAliases(DB_HOST)
              .withLogConsumer(new Slf4jLogConsumer(logger));

    private static LibertyContainer inventoryContainer
        = new LibertyContainer(invImage, testHttps(), httpsPort, httpPort)
              .withEnv("DB_HOSTNAME", DB_HOST)
              .withNetwork(network)
              .waitingFor(Wait.forHttp("/health/ready").forPort(httpPort))
              .withLogConsumer(new Slf4jLogConsumer(logger));

    private static boolean isServiceRunning(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getProtocol() {
        return System.getProperty("test.protocol", "https");
    }

    private static boolean testHttps() {
        return getProtocol().equalsIgnoreCase("https");
    }

    private static SystemResourceClient createRestClient(String urlPath)
            throws KeyStoreException {
        ClientBuilder builder = ResteasyClientBuilder.newBuilder();
        if (testHttps()) {
            builder.trustStore(KeyStore.getInstance("PKCS12"));
            HostnameVerifier v = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return hostname.equals("localhost") || hostname.equals("docker");
                } };
            builder.hostnameVerifier(v);
        }
        ResteasyClient client = (ResteasyClient) builder.build();
        ResteasyWebTarget target = client.target(UriBuilder.fromPath(urlPath));
        return target.proxy(SystemResourceClient.class);
    }

    @BeforeAll
    public static void setup() throws Exception {
        String urlPath;
        if (isServiceRunning("localhost", httpPort)) {
            logger.info("Testing by dev mode or local runtime...");
            if (isServiceRunning("localhost", DB_PORT)) {
                logger.info("The application is ready to test.");
                urlPath = getProtocol() + "://localhost:"
                          + (testHttps() ? httpsPort : httpPort);
            } else {
                throw new Exception(
                      "Postgres database is not running");
            }
        } else {
            logger.info("Testing by using Testcontainers...");
            if (isServiceRunning("localhost", DB_PORT)) {
                throw new Exception(
                      "Postgres database is running locally. Stop it and retry.");
            } else {
                postgresContainer.start();
                inventoryContainer.start();
                urlPath = inventoryContainer.getBaseURL(getProtocol());
            }
        }
        urlPath += contextRoot;
        System.out.println("TEST: " + urlPath);
        client = createRestClient(urlPath);
    }

    @AfterAll
    public static void tearDown() {
        inventoryContainer.stop();
        postgresContainer.stop();
        network.close();
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
        client.addSystem("localhost", "linux", "11", Long.valueOf(2048));
        List<SystemData> systems = client.listContents();
        assertEquals(1, systems.size());
        showSystemData(systems.get(0));
        assertEquals("11", systems.get(0).getJavaVersion());
        assertEquals(Long.valueOf(2048), systems.get(0).getHeapSize());
    }

    @Test
    @Order(2)
    public void testUpdateSystem() {
        System.out.println("TEST: Testing update a system");
        client.updateSystem("localhost", "linux", "8", Long.valueOf(1024));
        SystemData system = client.getSystem("localhost");
        showSystemData(system);
        assertEquals("8", system.getJavaVersion());
        assertEquals(Long.valueOf(1024), system.getHeapSize());
    }

    @Test
    @Order(3)
    public void testRemoveSystem() {
        System.out.println("TEST: Testing remove a system");
        client.removeSystem("localhost");
        List<SystemData> systems = client.listContents();
        assertEquals(0, systems.size());
    }
}
```




Use ***GenericContainer*** class to create the ***postgresContainer*** test container to start up the PostgreSQL Docker image, and use the ***LibertyContainer*** custom class to create the ***inventoryContainer*** test container to start up the ***inventory*** Docker image. Because containers are isolated by default, make sure both containers use the same ***network*** for them to communicate.

The ***waitingFor()*** method overrides the ***waitingFor()*** method in ***LibertyContainer***, ensuring the ***inventoryContainer*** is ready before tests run by checking the ***/health/ready*** health readiness check API. For different container readiness check customizations, refer to the [official Testcontainers documentation](https://www.testcontainers.org/features/startup_and_waits/).

The ***LoggerFactory.getLogger()*** and ***withLogConsumer(new Slf4jLogConsumer(Logger))*** methods integrate container logs with the test logs by piping the container output to the specified logger.

The ***createRestClient()*** method creates a REST client instance with the ***SystemResourceClient*** interface, and configures a hostname verifier if the tests run over HTTPS. 

The ***setup()*** method prepares the test environment. It checks whether the tests are running in dev mode or local runtime, or via Testcontainers, by using the ***isServiceRunning()*** helper. If it's in dev mode or local runtime, it ensures that the Postgres database is running locally. In the case of no running runtime, the test starts the ***postgresContainer*** and ***inventoryContainer*** test containers.

The ***testAddSystem()*** verifies the ***addSystem*** and ***listContents*** endpoints.

The ***testUpdateSystem()*** verifies the ***updateSystem*** and ***getSystem*** endpoints.

The ***testRemoveSystem()*** verifies the ***removeSystem*** endpoint.

After tests, the ***tearDown()*** method stops the containers and closes the network.

### Configuring Maven project

Next, you'll learn to prepare your Maven project for test execution. You'll add the required dependencies for Testcontainers and logging, set up Maven to copy the PostgreSQL JDBC driver during the build phase, and configure the Liberty Maven Plugin to handle PostgreSQL dependency.

Configure your test build with Maven. 

Replace the ***pom.xml*** file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > draft-guide-testcontainers/start/pom.xml, or click the following button

::openFile{path="/home/project/draft-guide-testcontainers/start/pom.xml"}



```xml
<?xml version="1.0" encoding="UTF-8" ?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-testcontainers</artifactId>
    <packaging>war</packaging>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <liberty.var.default.http.port>9080</liberty.var.default.http.port>
        <liberty.var.default.https.port>9443</liberty.var.default.https.port>
        <liberty.var.default.context.root>/inventory</liberty.var.default.context.root>
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
            <version>42.3.8</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Test dependencies -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.9.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-client</artifactId>
            <version>6.0.0.Final</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-json-binding-provider</artifactId>
            <version>6.0.0.Final</version>
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
            <version>2.0.4</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <version>1.18.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-reload4j</artifactId>
            <version>2.0.7</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.7</version>
        </dependency>
    </dependencies>

    <build>
        <finalName>inventory</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <version>3.2.0</version>
                <executions>
                    <execution>
                        <id>copy-dependencies</id>
                        <phase>prepare-package</phase>
                        <goals>
                            <goal>copy-dependencies</goal>
                        </goals>
                        <configuration>
                            <includeGroupIds>org.postgresql</includeGroupIds>
                            <includeArtifactIds>postgresql</includeArtifactIds>
                            <outputDirectory>${project.build.directory}/liberty/wlp/usr/shared/resources</outputDirectory>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
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
                            </dependency>
                        </dependencyGroup>
                    </copyDependencies>
                </configuration>
                <version>3.8.2</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>2.22.0</version>
                <configuration>
                    <systemPropertyVariables>
                        <http.port>${liberty.var.default.http.port}</http.port>
                        <https.port>${liberty.var.default.https.port}</https.port>
                        <context.root>${liberty.var.default.context.root}</context.root>
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




The Maven pom.xml file contains a configuration for the ***maven-dependency-plugin*** to copy the PostgreSQL JDBC driver into the Liberty configuration's shared resources directory. This setup occurs during the ***prepare-package*** phase. As a result, running the ***mvn package*** command ensures the PostgreSQL driver is prepared and accessible for your application when it runs on the Liberty.

Also, add and configure the ***maven-failsafe-plugin*** plugin, so that the integration test can be run by the Maven ***verify*** goal.

Save the changes, and press the ***enter/return*** key in your console window to run the tests. You will see the following output:

```
 -------------------------------------------------------
  T E S T S
 -------------------------------------------------------
 Running it.io.openliberty.guides.inventory.SystemResourceIT
 it.io.openliberty.guides.inventory.SystemResourceIT  - Testing by dev mode or local runtime...
 ...
 Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.873 s - in it.io.openliberty.guides.inventory.SystemResourceIT

 Results:

 Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

Notice that the ***Testing by dev mode or local runtime*** log indicates that the tests ran in dev mode.

::page{title="Running tests in a CI/CD pipeline"}

Running tests in dev mode is useful for local development, but there may be times when you want to test your application in other scenarios, such as in a CI/CD pipeline. For these cases, you can use Testcontainers to run tests against a running Open Liberty instance in a controlled, self-contained environment, ensuring that your tests run consistently regardless of the deployment context.

To test outside of dev mode, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran the Liberty, or by typing ***q*** and then pressing the ***enter/return*** key.

Also, run the following commands to stop the PostgreSQL container that was started in the previous section:

```bash
docker stop postgres-container
```

Because you have already built the ***inventory*** Docker image in the *Try what you'll build* section, there's no need to build it again here.

Now, use the following Maven goal to run the tests from a cold start outside of dev mode:

```bash
export TESTCONTAINERS_RYUK_DISABLED=true
mvn verify
```

You will see the following output:

```
 -------------------------------------------------------
  T E S T S
 -------------------------------------------------------
 Running it.io.openliberty.guides.inventory.SystemResourceIT
 it.io.openliberty.guides.inventory.SystemResourceIT  - Testing by using Testcontainers...
 ...
 tc.postgres-sample:latest  - Creating container for image: postgres-sample:latest
 tc.postgres-sample:latest  - Container postgres-sample:latest is starting: 7cf2e2c6a505f41877014d08b7688399b3abb9725550e882f1d33db8fa4cff5a
 tc.postgres-sample:latest  - Container postgres-sample:latest started in PT2.925405S
 tc.inventory:1.0-SNAPSHOT  - Creating container for image: inventory:1.0-SNAPSHOT
 tc.inventory:1.0-SNAPSHOT  - Container inventory:1.0-SNAPSHOT is starting: 432ac739f377abe957793f358bbb85cc916439283ed2336014cacb585f9992b8
 it.io.openliberty.guides.inventory.SystemResourceIT  - STDOUT: [AUDIT   ] CWWKF0011I: The defaultServer server is ready to run a smarter planet. The defaultServer server started in 7.855 seconds.

 Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 12.208 s - in it.io.openliberty.guides.inventory.SystemResourceIT

 Results:

 Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

Notice that the test initiates a new Docker container each for the PostgreSQL database and the ***inventory*** microservice, resulting in a longer test runtime. Despite this, cold start testing benefits from a clean instance per run and ensures consistent results. These tests also automatically hook into existing build pipelines that are set up to run the ***integration-test*** phase.

::page{title="Summary"}

### Nice Work!

You just tested your microservices with multiple Docker containers using Testcontainers.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***draft-guide-testcontainers*** project by running the following commands:

```bash
cd /home/project
rm -fr draft-guide-testcontainers
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Building%20true-to-production%20integration%20tests%20with%20Testcontainers&guide-id=cloud-hosted-draft-guide-testcontainers)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/draft-guide-testcontainers/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/draft-guide-testcontainers/pulls)



### Where to next?

* [Testing a MicroProfile or Jakarta EE application](https://openliberty.io/guides/microshed-testing.html)
* [Testing reactive Java microservices](https://openliberty.io/guides/reactive-service-testing.html)
* [Testing microservices with the Arquillian managed container](https://openliberty.io/guides/arquillian-managed.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
