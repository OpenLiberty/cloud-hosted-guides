---
markdown-version: v1
branch: lab-9675-instruction
version-history-start-date: 2024-02-07T22:30:00Z
tool-type: theiadocker
---
::page{title="Welcome to the Building true-to-production integration tests with Testcontainers guide!"}

Learn how to test your microservices with multiple containers by using Testcontainers and JUnit.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You'll learn how to write true-to-production integration tests for Java microservices by using [Testcontainers](https://www.testcontainers.org/) and JUnit. You'll learn to set up and configure multiple containers, including the Open Liberty Docker container, to simulate a production-like environment for your tests.

Sometimes tests might pass in development and testing environments, but fail in production because of the differences in how the application operates across these environments. Fortunately, you can minimize these differences by testing your application with the same Docker containers you use in production. This approach helps to ensure parity across the development, testing, and production environments, enhancing quality and test reliability.

### What is Testcontainers?

Testcontainers is an open source library that provides containers as a resource at test time, creating consistent and portable testing environments. This is especially useful for applications that have external resource dependencies such as databases, message queues, or web services. By encapsulating these dependencies in containers, Testcontainers simplifies the configuration process and ensures a uniform testing setup that closely mirrors production environments.

The microservice that you'll be working with is called ***inventory***. The ***inventory*** microservice persists data into a PostgreSQL database and supports create, retrieve, update, and delete (CRUD) operations on the database records. You'll write integration tests for the application by using Testcontainers to run it in Docker containers.

![Inventory microservice](https://raw.githubusercontent.com/OpenLiberty/guide-testcontainers/prod/assets/inventory.png)


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-testcontainers.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-testcontainers.git
cd guide-testcontainers
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

In this IBM Cloud environment, you need to change the user home to ***/home/project*** by running the following command:
```bash
sudo usermod -d /home/project theia
```

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the test, first go to the ***finish*** directory and run the following Maven goal that builds the application, starts the containers, runs the tests, and then stops the containers:


```bash
export TESTCONTAINERS_RYUK_DISABLED=true
mvn verify
```

You see the following output:

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

::page{title="Writing integration tests using Testcontainers"}

Use Testcontainers to write integration tests that run in any environment with minimal setup using containers.

Navigate to the ***postgres*** directory.

```bash
cd /home/project/guide-testcontainers/postgres
```


This guide uses Docker to run an instance of the PostgreSQL database for a fast installation and setup. A ***Dockerfile*** file is provided for you. Run the following command to use the Dockerfile to build the image:

```bash
docker build -t postgres-sample .
```

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

Now, navigate to the ***start*** directory to begin.

```bash
cd /home/project/guide-testcontainers/start
```

The Liberty Maven plug-in includes a ***devc*** goal that simplifies developing your application in a container by starting [dev mode](https://openliberty.io/docs/latest/development-mode.html#_container_support_for_dev_mode) with container support. This goal builds a Docker image, mounts the required directories, binds the required ports, and then runs the application inside of a container. Dev mode also listens for any changes in the application source code or configuration and rebuilds the image and restarts the container as necessary.

In this IBM Cloud environment, you need to pre-create the ***logs*** directory by running the following commands:

```bash
mkdir -p /home/project/guide-testcontainers/start/target/liberty/wlp/usr/servers/defaultServer/logs
chmod 777 /home/project/guide-testcontainers/start/target/liberty/wlp/usr/servers/defaultServer/logs
```

Build and run the container by running the ***devc*** goal with the PostgreSQL container IP address. If your PostgreSQL container IP address is not ***172.17.0.2***, replace the command with the right IP address.

```bash
mvn liberty:devc -DcontainerRunOpts="-e DB_HOSTNAME=172.17.0.2" -DserverStartTimeout=240
```

Wait a moment for dev mode to start. After you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
*    ...
*    Container network information:
*        Container name: [ liberty-dev ]
*        IP address [ 172.17.0.2 ] on container network [ bridge ]
*    ...
```


Dev mode holds your command-line session to listen for file changes.

Click the following button to try out the ***inventory*** microservice manually by visiting the ***/openapi/ui*** endpoint. This interface provides a convenient visual way to interact with the APIs and test out their functionalities:

::startApplication{port="9080" display="external" name="Visit OpenAPI UI" route="/openapi/ui"}

Open another command-line session to continue.



### Building a REST test client

The REST test client is responsible for sending HTTP requests to an application and handling the responses. It enables accurate verification of the application's behavior by ensuring that it responds correctly to various scenarios and conditions. Using a REST client for testing ensures reliable interaction with the ***inventory*** microservice across various deployment environments: local processes, Docker containers, or containers through Testcontainers.

Begin by creating a REST test client interface for the ***inventory*** microservice.

Create the ***SystemResourceClient*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemResourceClient.java
```


> Then, to open the SystemResourceClient.java file in your IDE, select
> **File** > **Open** > guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemResourceClient.java, or click the following button

::openFile{path="/home/project/guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemResourceClient.java"}



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

Create the ***SystemData*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemData.java
```


> Then, to open the SystemData.java file in your IDE, select
> **File** > **Open** > guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemData.java, or click the following button

::openFile{path="/home/project/guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemData.java"}



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

### Building a test container for Open Liberty

Next, create a custom class that extends Testcontainers' generic container to define specific configurations that suit your application's requirements.

Define a custom ***LibertyContainer*** class, which provides a framework to start and access a containerized version of the Open Liberty application for testing.

Create the ***LibertyContainer*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/LibertyContainer.java
```


> Then, to open the LibertyContainer.java file in your IDE, select
> **File** > **Open** > guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/LibertyContainer.java, or click the following button

::openFile{path="/home/project/guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/LibertyContainer.java"}



```java
package it.io.openliberty.guides.inventory;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class LibertyContainer extends GenericContainer<LibertyContainer> {

    public LibertyContainer(ImageFromDockerfile image, int httpPort, int httpsPort) {

        super(image);
        addExposedPorts(httpPort, httpsPort);

        waitingFor(Wait.forLogMessage("^.*CWWKF0011I.*$", 1));

    }

    public String getBaseURL() throws IllegalStateException {
        return "http://" + getHost() + ":" + getFirstMappedPort();
    }

}
```



The ***LibertyContainer*** class extends the ***GenericContainer*** class from Testcontainers to create a custom container configuration specific to the Open Liberty application.

The ***addExposedPorts()*** method exposes specified ports from the container's perspective, allowing test clients to communicate with services running inside the container. To avoid any port conflicts, Testcontainers assigns random host ports to these exposed container ports. 

By default, the ***Wait.forLogMessage()*** method directs ***LibertyContainer*** to wait for the specific ***CWWKF0011I*** log message that indicates the Liberty instance has started successfully.

The ***getBaseURL()*** method contructs the base URL to access the container.

For more information about Testcontainers APIs and its functionality, refer to the [Testcontainers JavaDocs](https://javadoc.io/doc/org.testcontainers/testcontainers/latest/index.html).


### Building test cases

Next, write tests that use the ***SystemResourceClient*** REST client and Testcontainers integration. 

Create the ***SystemResourceIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemResourceIT.java
```


> Then, to open the SystemResourceIT.java file in your IDE, select
> **File** > **Open** > guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemResourceIT.java, or click the following button

::openFile{path="/home/project/guide-testcontainers/start/src/test/java/it/io/openliberty/guides/inventory/SystemResourceIT.java"}



```java
package it.io.openliberty.guides.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.Socket;
import java.util.List;
import java.nio.file.Paths;

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
import org.testcontainers.images.builder.ImageFromDockerfile;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.UriBuilder;

@TestMethodOrder(OrderAnnotation.class)
public class SystemResourceIT {

    private static Logger logger = LoggerFactory.getLogger(SystemResourceIT.class);

    private static final String DB_HOST = "postgres";
    private static final int DB_PORT = 5432;
    private static ImageFromDockerfile postgresImage
        = new ImageFromDockerfile("postgres-sample")
              .withDockerfile(Paths.get("../postgres/Dockerfile"));

    private static int httpPort = Integer.parseInt(System.getProperty("http.port"));
    private static int httpsPort = Integer.parseInt(System.getProperty("https.port"));
    private static String contextRoot = System.getProperty("context.root") + "/api";
    private static ImageFromDockerfile invImage
        = new ImageFromDockerfile("inventory:1.0-SNAPSHOT")
              .withDockerfile(Paths.get("./Dockerfile"));

    private static SystemResourceClient client;
    private static Network network = Network.newNetwork();

    private static GenericContainer<?> postgresContainer
        = new GenericContainer<>(postgresImage)
              .withNetwork(network)
              .withExposedPorts(DB_PORT)
              .withNetworkAliases(DB_HOST)
              .withLogConsumer(new Slf4jLogConsumer(logger));

    private static LibertyContainer inventoryContainer
        = new LibertyContainer(invImage, httpPort, httpsPort)
              .withEnv("DB_HOSTNAME", DB_HOST)
              .withNetwork(network)
              .waitingFor(Wait.forHttp("/health/ready").forPort(httpPort))
              .withLogConsumer(
                new Slf4jLogConsumer(
                    LoggerFactory.getLogger(LibertyContainer.class)));

    private static boolean isServiceRunning(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static SystemResourceClient createRestClient(String urlPath) {
        ClientBuilder builder = ResteasyClientBuilder.newBuilder();
        ResteasyClient client = (ResteasyClient) builder.build();
        ResteasyWebTarget target = client.target(UriBuilder.fromPath(urlPath));
        return target.proxy(SystemResourceClient.class);
    }

    @BeforeAll
    public static void setup() throws Exception {
        String urlPath;
        if (isServiceRunning("localhost", httpPort)) {
            logger.info("Testing by dev mode or local Liberty...");
            if (isServiceRunning("localhost", DB_PORT)) {
                logger.info("The application is ready to test.");
                urlPath = "http://localhost:" + httpPort;
            } else {
                throw new Exception("Postgres database is not running");
            }
        } else {
            logger.info("Testing by using Testcontainers...");
            if (isServiceRunning("localhost", DB_PORT)) {
                throw new Exception(
                      "Postgres database is running locally. Stop it and retry.");
            } else {
                postgresContainer.start();
                inventoryContainer.start();
                urlPath = inventoryContainer.getBaseURL();
            }
        }
        urlPath += contextRoot;
        logger.info("TEST: " + urlPath);
        client = createRestClient(urlPath);
    }

    @AfterAll
    public static void tearDown() {
        inventoryContainer.stop();
        postgresContainer.stop();
        network.close();
    }

    private void showSystemData(SystemData system) {
        logger.info("TEST: SystemData > "
            + system.getId() + ", "
            + system.getHostname() + ", "
            + system.getOsName() + ", "
            + system.getJavaVersion() + ", "
            + system.getHeapSize());
    }

    @Test
    @Order(1)
    public void testAddSystem() {
        logger.info("TEST: Testing add a system");
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
        logger.info("TEST: Testing update a system");
        client.updateSystem("localhost", "linux", "8", Long.valueOf(1024));
        SystemData system = client.getSystem("localhost");
        showSystemData(system);
        assertEquals("8", system.getJavaVersion());
        assertEquals(Long.valueOf(1024), system.getHeapSize());
    }

    @Test
    @Order(3)
    public void testRemoveSystem() {
        logger.info("TEST: Testing remove a system");
        client.removeSystem("localhost");
        List<SystemData> systems = client.listContents();
        assertEquals(0, systems.size());
    }
}
```






Construct the ***postgresImage*** and ***invImage*** using the ***ImageFromDockerfile*** class, which allows Testcontainers to build Docker images from a Dockerfile during the test runtime. For these instances, the provided Dockerfiles at the specified paths ***../postgres/Dockerfile*** and ***./Dockerfile*** are used to generate the respective ***postgres-sample*** and ***inventory:1.0-SNAPSHOT*** images.

Use ***GenericContainer*** class to create the ***postgresContainer*** test container to start up the ***postgres-sample*** Docker image, and use the ***LibertyContainer*** custom class to create the ***inventoryContainer*** test container to start up the ***inventory:1.0-SNAPSHOT*** Docker image. 

As containers are isolated by default, placing both the ***LibertyContainer*** and the ***postgresContainer*** on the same ***network*** allows them to communicate by using the hostname ***localhost*** and the internal port ***5432***, bypassing the need for an externally mapped port.

The ***waitingFor()*** method here overrides the ***waitingFor()*** method from ***LibertyContainer***. Given that the ***inventory*** service depends on a database service, ensuring that readiness involves more than just the microservice itself. To address this, the ***inventoryContainer*** readiness is determined by checking the ***/health/ready*** health readiness check API, which reflects both the application and database service states. For different container readiness check customizations, see to the [official Testcontainers documentation](https://www.testcontainers.org/features/startup_and_waits/).

The ***LoggerFactory.getLogger()*** and ***withLogConsumer(new Slf4jLogConsumer(Logger))*** methods integrate container logs with the test logs by piping the container output to the specified logger.

The ***createRestClient()*** method creates a REST client instance with the ***SystemResourceClient*** interface.

The ***setup()*** method prepares the test environment. It checks whether the test is running in dev mode or there is a local running Liberty instance, by using the ***isServiceRunning()*** helper. In the case of no running Liberty instance, the test starts the ***postgresContainer*** and ***inventoryContainer*** test containers. Otherwise, it ensures that the Postgres database is running locally.

The ***testAddSystem()*** verifies the ***addSystem*** and ***listContents*** endpoints.

The ***testUpdateSystem()*** verifies the ***updateSystem*** and ***getSystem*** endpoints.

The ***testRemoveSystem()*** verifies the ***removeSystem*** endpoint.

After the tests are executed, the ***tearDown()*** method stops the containers and closes the network.


### Setting up logs

Having reliable logs is essential for efficient debugging, as they provide detailed insights into the test execution flow and help pinpoint issues during test failures. Testcontainers' built-in ***Slf4jLogConsumer*** enables integration of container output directly with the JUnit process, enhancing log analysis and simplifying test creation and debugging.

Create the ***log4j.properties*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-testcontainers/start/src/test/resources/log4j.properties
```


> Then, to open the log4j.properties file in your IDE, select
> **File** > **Open** > guide-testcontainers/start/src/test/resources/log4j.properties, or click the following button

::openFile{path="/home/project/guide-testcontainers/start/src/test/resources/log4j.properties"}



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


### Configuring the Maven project

Next, prepare your Maven project for test execution by adding the necessary dependencies for Testcontainers and logging, setting up Maven to copy the PostgreSQL JDBC driver during the build phase, and configuring the Liberty Maven Plugin to handle PostgreSQL dependency.

Replace the ***pom.xml*** file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-testcontainers/start/pom.xml, or click the following button

::openFile{path="/home/project/guide-testcontainers/start/pom.xml"}



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
            <version>6.1</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.3</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Test dependencies -->
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
        <dependency>
            <groupId>org.eclipse</groupId>
            <artifactId>yasson</artifactId>
            <version>3.0.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <version>1.19.7</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-reload4j</artifactId>
            <version>2.0.12</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.12</version>
        </dependency>
    </dependencies>

    <build>
        <finalName>inventory</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
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
                <version>3.10.2</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.2.5</version>
                <configuration>
                    <systemPropertyVariables>
                        <http.port>${liberty.var.http.port}</http.port>
                        <https.port>${liberty.var.https.port}</https.port>
                        <context.root>${liberty.var.context.root}</context.root>
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



Add the required ***dependency*** for Testcontainers and Log4J libraries with ***test*** scope. The ***testcontainers*** dependency offers a general-purpose API for managing container-based test environments. The ***slf4j-reload4j*** and ***slf4j-api*** dependencies enable the Simple Logging Facade for Java (SLF4J) API for trace logging during test execution and facilitates debugging and test performance tracking. 

Also, add and configure the ***maven-failsafe-plugin*** plugin, so that the integration test can be run by the ***mvn verify*** command.

When you started Open Liberty in dev mode, all the changes were automatically picked up. You can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode. You see the following output:

```
 -------------------------------------------------------
  T E S T S
 -------------------------------------------------------
 Running it.io.openliberty.guides.inventory.SystemResourceIT
 it.io.openliberty.guides.inventory.SystemResourceIT  - Testing by dev mode or local Liberty...
 ...
 Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.873 s - in it.io.openliberty.guides.inventory.SystemResourceIT

 Results:

 Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```



::page{title="Running tests in a CI/CD pipeline"}

Running tests in dev mode is useful for local development, but there may be times when you want to test your application in other scenarios, such as in a CI/CD pipeline. For these cases, you can use Testcontainers to run tests against a running Open Liberty instance in a controlled, self-contained environment, ensuring that your tests run consistently regardless of the deployment context.

To test outside of dev mode, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran the Liberty.

Also, run the following commands to stop the PostgreSQL container that was started in the previous section:

```bash
docker stop postgres-container
```

Now, use the following Maven goal to run the tests from a cold start outside of dev mode:

****WINDOWS****
****MAC****
****LINUX****
```bash
export TESTCONTAINERS_RYUK_DISABLED=true
mvn clean verify
```

You see the following output:

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
 ...
 tc.inventory:1.0-SNAPSHOT  - Creating container for image: inventory:1.0-SNAPSHOT
 tc.inventory:1.0-SNAPSHOT  - Container inventory:1.0-SNAPSHOT is starting: 432ac739f377abe957793f358bbb85cc916439283ed2336014cacb585f9992b8
 tc.inventory:1.0-SNAPSHOT  - Container inventory:1.0-SNAPSHOT started in PT25.784899S
...

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

Delete the ***guide-testcontainers*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-testcontainers
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Building%20true-to-production%20integration%20tests%20with%20Testcontainers&guide-id=cloud-hosted-guide-testcontainers)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-testcontainers/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-testcontainers/pulls)



### Where to next?

* [Testing reactive Java microservices](https://openliberty.io/guides/reactive-service-testing.html)
* [Testing microservices with the Arquillian managed container](https://openliberty.io/guides/arquillian-managed.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
