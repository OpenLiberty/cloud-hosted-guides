
# Containerizing microservices


Learn how to containerize and run your microservices with Open Liberty using Docker.


## What you'll learn


You can easily deploy your microservices in different environments in a lightweight and portable manner by using containers.
From development to production and across your DevOps environments, you can deploy your microservices consistently and
efficiently with containers. You can run a container from a container image. Each container image is a package of what you
need to run your microservice or application, from the code to its dependencies and configuration.

You'll learn how to build container images and run containers using Docker for your microservices.
You'll construct **Dockerfile** files, create Docker images by using the **docker build** command, and run the image as Docker containers 
by using **docker run** command.

The two microservices that you'll be working with are called **system** and **inventory**. The **system** microservice returns the JVM system properties 
of the running container. The **inventory** microservice adds the properties from the **system** microservice to the inventory. This guide demonstrates how both microservices can run and communicate
with each other in different Docker containers. 



# Packaging your microservices

Navigate to the **start** directory to begin.

You can find the starting Java project in the **start** directory. It is a multi-module Maven project that is made up of the **system** and **inventory** microservices. Each microservice lives in its own corresponding directory, **system** and **inventory**.

To try out the microservices by using Maven, run the following Maven goal to build the **system** microservice and run it inside Open Liberty:
```
mvn -pl system liberty:run
```
Open another command-line session and run the following Maven goal to build the **inventory** microservice and run it inside Open Liberty:
```
mvn -pl inventory liberty:run
```
{: codeblock}


To access the **inventory** service, which displays the current contents of the inventory, see [http://localhost:9081/inventory/systems](http://localhost:9081/inventory/systems)

```
curl http://localhost:9081/inventory/systems
```
{: codeblock}



The **system** service shows the system properties of the running JVM and can be found at  [](http://localhost:9080/system/properties)

```
curl http://localhost:9080/system/properties
```
{: codeblock}



The system properties of your localhost can be added to the **inventory** at [http://localhost:9081/inventory/systems/localhost](http://localhost:9081/inventory/systems/localhost)

```
curl http://localhost:9081/inventory/systems/localhost
```
{: codeblock}



After you are finished checking out the microservices, stop the Open Liberty servers by pressing **CTRL+C**
in the command-line sessions where you ran the servers. Alternatively, you can run the **liberty:stop** goal in another command-line session:
```
mvn -pl system liberty:stop
mvn -pl inventory liberty:stop
```
{: codeblock}


Run the Maven **package** goal to build the application **.war** files from the **start** directory so that the **.war** files reside in the **system/target** and **inventory/target** directories.
```
mvn package
```
{: codeblock}


To learn more about RESTful web services and how to build them, see
[Creating a RESTful web service](https://openliberty.io/guides/restintro.html) for details about how to build the **system** service.
The **inventory** service is built in a similar way.


# Building your Docker images

A Docker image is a binary file. It is made up of multiple layers and is used to run code in a Docker container. Images are built from
instructions in Dockerfiles to create a containerized version of the application.

A **Dockerfile** is a collection of instructions for building a Docker image that can then be run as a container.
As each instruction is run in a **Dockerfile**, a new Docker layer is created. These layers, which are known as intermediate images, are created when a change is made to your Docker image.

Every **Dockerfile** begins with a parent or base image over which various commands are run. For example, you can start your image from scratch and run commands that download and install a Java runtime, or you can start from an image that already contains a Java installation.

Learn more about Docker on the [official Docker page](https://www.docker.com/whatdocker).

### Creating your Dockerfiles
You will be creating two Docker images to run the **inventory** service and **system** service. The first step is to create Dockerfiles for both services.

Create the **Dockerfile** for the inventory service.


> [File -> Open]guide-containerize/start/inventory/Dockerfile



```
FROM openliberty/open-liberty:kernel-java8-openj9-ubi

ARG VERSION=1.0
ARG REVISION=SNAPSHOT

LABEL \
  org.opencontainers.image.authors="Your Name" \
  org.opencontainers.image.vendor="Open Liberty" \
  org.opencontainers.image.url="local" \
  org.opencontainers.image.source="https://github.com/OpenLiberty/guide-containerize" \
  org.opencontainers.image.version="$VERSION" \
  org.opencontainers.image.revision="$REVISION" \
  vendor="Open Liberty" \
  name="inventory" \
  version="$VERSION-$REVISION" \
  summary="The inventory microservice from the Containerizing microservices guide" \
  description="This image contains the inventory microservice running with the Open Liberty runtime."

COPY --chown=1001:0 \
    # tag::inventory-config[]
    src/main/liberty/config \
    # end::inventory-config[]
    # tag::config[]
    # end::config[]

COPY --chown=1001:0 \
    # tag::inventory-war[]
    target/inventory.war \
    # end::inventory-war[]
    # tag::config-apps[]
    # end::config-apps[]

RUN configure.sh
```
{: codeblock}



The **FROM** instruction initializes a new build stage, which indicates the parent image of the built image. If you don't need a parent image, then you can use **FROM scratch**, which makes your image a base image. 

In this case, you're using the recommended production image, 
**openliberty/openliberty:kerneljava8openj9ubi**, as your parent image. If you don't want any additional runtime features for your **kernel** image, define the **FROM** instruction as **FROM openliberty:kernel**. To use the default image that comes with the Open Liberty runtime, define the **FROM** instruction as **FROM openliberty**. You can find all the official images at [openliberty Docker Hub](https://hub.docker.com/_/openliberty).

It is also recommended to label your Docker images with the **LABEL** command, as the label information can help you manage your images. For more information, see [Best practices for writing Dockerfiles](https://docs.docker.com/develop/developimages/dockerfile_bestpractices/#label).

The **COPY** instructions are structured as **COPY** **[--chown=<user>:<group>]** **<source>** **<destination>**. 
They copy local files into the specified destination within your Docker image.
In this case, the **inventory** server configuration files that are located at **src/main/liberty/config** are copied to the **/config/** destination directory.
The **inventory** application WAR file **inventory.war**, which was created from running **mvn package**, is copied to the **/config/apps** destination directory.

The **COPY** instructions use the **1001** user ID  and **0** group because the **openliberty/open-liberty:kernel-java8-openj9-ubi** image runs by default with the **USER 1001** (non-root) user for security purposes. Otherwise, the files and directories that are copied over are owned by the root user.

Place the **RUN configure.sh** command at the end to get a pre-warmed Docker image. It improves the startup time of running your Docker container.

The **Dockerfile** for the **system** service follows the same instructions as the **inventory** service, except that some **labels** are updated, and the **system.war** archive is copied into **/config/apps**.

Create the **Dockerfile** for the system service.


> [File -> Open]guide-containerize/start/system/Dockerfile



```
FROM openliberty/open-liberty:kernel-java8-openj9-ubi

ARG VERSION=1.0
ARG REVISION=SNAPSHOT

LABEL \
  org.opencontainers.image.authors="Your Name" \
  org.opencontainers.image.vendor="Open Liberty" \
  org.opencontainers.image.url="local" \
  org.opencontainers.image.source="https://github.com/OpenLiberty/guide-containerize" \
  org.opencontainers.image.version="$VERSION" \
  org.opencontainers.image.revision="$REVISION" \
  vendor="Open Liberty" \
  name="system" \
  version="$VERSION-$REVISION" \
  summary="The system microservice from the Containerizing microservices guide" \
  description="This image contains the system microservice running with the Open Liberty runtime."

COPY --chown=1001:0 src/main/liberty/config /config/

COPY --chown=1001:0 target/system.war /config/apps

RUN configure.sh
```
{: codeblock}




### Building your Docker image

Now that your microservices are packaged and you have written your Dockerfiles, you will build your Docker images by using the **docker build** command.

Run the following command to download or update to the latest Open Liberty Docker image:

```
docker pull openliberty/open-liberty:kernel-java8-openj9-ubi
```
{: codeblock}


Run the following commands to build container images for your application:

```
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
```
{: codeblock}


The **-t** flag in the **docker build** command allows the Docker image to be labeled (tagged) in the **name[:tag]** format. 
The tag for an image describes the specific image version. If the optional **[:tag]** tag is not specified, the **latest** tag is created by default.

To verify that the images are built, run the **docker images** command to list all local Docker images:

```
docker images
```
{: codeblock}


Or, run the **docker images** command with **--filter** option to list your images:
```
docker images -f "label=org.opencontainers.image.authors=Your Name"
```
{: codeblock}


Your two images, **inventory** and **system**, should appear in the list of all Docker images:

```
REPOSITORY    TAG             IMAGE ID        CREATED          SIZE
inventory     1.0-SNAPSHOT    08fef024e986    4 minutes ago    471MB
system        1.0-SNAPSHOT    1dff6d0b4f31    5 minutes ago    470MB
```


# Running your microservices in Docker containers
Now that you have your two images built, you will run your microservices in Docker containers:

```
docker run -d --name system -p 9080:9080 system:1.0-SNAPSHOT
docker run -d --name inventory -p 9081:9081 inventory:1.0-SNAPSHOT
```
{: codeblock}


The flags are described in the table below: 

| Flag | Description
| ---| ---
| -d     | Runs the container in the background.
| --name | Specifies a name for the container.
| -p     | Maps the host ports to the container ports. For example: **-p <HOST_PORT>:<CONTAINER_PORT>**

Next, run the **docker ps** command to verify that your containers are started:

```
docker ps
```
{: codeblock}


Make sure that your containers are running and show **Up** as their status:

```
CONTAINER ID    IMAGE                   COMMAND                  CREATED          STATUS          PORTS                                        NAMES
2b584282e0f5    inventory:1.0-SNAPSHOT  "/opt/ol/helpers/run…"   2 seconds ago    Up 1 second     9080/tcp, 9443/tcp, 0.0.0.0:9081->9081/tcp   inventory
99a98313705f    system:1.0-SNAPSHOT     "/opt/ol/helpers/run…"   3 seconds ago    Up 2 seconds    0.0.0.0:9080->9080/tcp, 9443/tcp             system
```

If a problem occurs and your containers exit prematurely, the containers don't appear in the container
list that the **docker ps** command displays. Instead, your containers appear with an **Exited**
status when they run the **docker ps -a** command. Run the **docker logs system** and **docker logs inventory** commands to view the
container logs for any potential problems. Run the **docker stats system** and **docker stats inventory** commands to display a live stream of usage statistics for your containers. You can also double-check that your Dockerfiles are correct. When you
find the cause of the issues, remove the faulty containers with the **docker rm system** and **docker rm inventory** commands. Rebuild
your images, and start the containers again.

To access the application, point your browser to the [http://localhost:9081/inventory/systems](http://localhost:9081/inventory/systems) URL 

```
curl http://localhost:9081/inventory/systems
```
{: codeblock}


An empty list is expected because no system properties are stored in the inventory yet. 

Next, retrieve the **system** container's IP address by using the **system** container's name that is defined when it ran the Docker containers. 
Run the following command to retrieve the **system** IP address:

```
docker inspect -f "{{.NetworkSettings.IPAddress }}" system
```
{: codeblock}


You find the **system** container's IP address:

```
172.17.0.2
```

In this case, the IP address for the **system** service is **172.17.0.2**. Take note of this IP address to add the system properties to the **inventory** service. 

Point your browser to **\http://localhost:9081/inventory/systems/pass:c[[system-ip-address]]** by replacing **[system-ip-address]** with the IP address you obtained earlier.
You see a result in JSON format with the system properties of your local JVM. When you visit this URL, these system
properties are automatically stored in the inventory Go back to [http://localhost:9081/inventory/systems](http://localhost:9081/inventory/systems) and

```
curl http://localhost:9081/inventory/systems
```
{: codeblock}


you see a new entry for **[system-ip-address]**. 


# Testing the microservices

You can test your microservices manually by hitting the endpoints or with automated tests that check your running Docker containers.

Create the **SystemEndpointIT** class.


> [File -> Open]guide-containerize/start/system/src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java



```
package it.io.openliberty.guides.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SystemEndpointIT {

    private static String clusterUrl;

    private Client client;

    @BeforeAll
    public static void oneTimeSetup() {
        String nodePort = System.getProperty("system.http.port");
        clusterUrl = "http://localhost:" + nodePort + "/system/properties/";
    }
    
    @BeforeEach
    public void setup() {
        client = ClientBuilder.newBuilder()
                    .hostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .build();
    }

    @AfterEach
    public void teardown() {
        client.close();
    }
    
    @Test
    public void testGetProperties() {
        Client client = ClientBuilder.newClient();
        client.register(JsrJsonpProvider.class);

        WebTarget target = client.target(clusterUrl);
        Response response = target.request().get();

        assertEquals(200, response.getStatus(), 
            "Incorrect response code from " + clusterUrl);
        response.close();
    }

}
```
{: codeblock}



The **testGetProperties()** method checks for a **200** response code from the **system** service endpoint.

Create the **InventoryEndpointIT** class.


> [File -> Open]guide-containerize/start/inventory/src/test/java/it/io/openliberty/guides/inventory/InventoryEndpointIT.java



```
package it.io.openliberty.guides.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.json.JsonObject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class InventoryEndpointIT {

    private static String invUrl;
    private static String sysUrl;
    private static String systemServiceIp;

    private static Client client;

    @BeforeAll
    public static void oneTimeSetup() {

        String invServPort = System.getProperty("inventory.http.port");
        String sysServPort = System.getProperty("system.http.port");

        systemServiceIp = System.getProperty("system.ip");

        invUrl = "http://localhost" + ":" + invServPort + "/inventory/systems/";
        sysUrl = "http://localhost" + ":" + sysServPort + "/system/properties/";

        client = ClientBuilder.newBuilder().hostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }).build();

        client.register(JsrJsonpProvider.class);
        client.target(invUrl + "reset").request().post(null);
    }

    @AfterAll
    public static void teardown() {
        client.close();
    }

    @Test
    @Order(1)
    public void testEmptyInventory() {
        Response response = this.getResponse(invUrl);
        this.assertResponse(invUrl, response);

        JsonObject obj = response.readEntity(JsonObject.class);

        int expected = 0;
        int actual = obj.getInt("total");
        assertEquals(expected, actual,
                        "The inventory should be empty on application start but it wasn't");

        response.close();
    }

    @Test
    @Order(2)
    public void testHostRegistration() {
        this.visitSystemService();

        Response response = this.getResponse(invUrl);
        this.assertResponse(invUrl, response);

        JsonObject obj = response.readEntity(JsonObject.class);

        int expected = 1;
        int actual = obj.getInt("total");
        assertEquals(expected, actual,
                        "The inventory should have one entry for " + systemServiceIp);

        boolean serviceExists = obj.getJsonArray("systems").getJsonObject(0)
                        .get("hostname").toString().contains(systemServiceIp);
        assertTrue(serviceExists,
                        "A host was registered, but it was not " + systemServiceIp);

        response.close();
    }

    @Test
    @Order(3)
    public void testSystemPropertiesMatch() {
        Response invResponse = this.getResponse(invUrl);
        Response sysResponse = this.getResponse(sysUrl);

        this.assertResponse(invUrl, invResponse);
        this.assertResponse(sysUrl, sysResponse);

        JsonObject jsonFromInventory = (JsonObject) invResponse
                        .readEntity(JsonObject.class).getJsonArray("systems")
                        .getJsonObject(0).get("properties");

        JsonObject jsonFromSystem = sysResponse.readEntity(JsonObject.class);

        String osNameFromInventory = jsonFromInventory.getString("os.name");
        String osNameFromSystem = jsonFromSystem.getString("os.name");
        this.assertProperty("os.name", systemServiceIp, osNameFromSystem,
                        osNameFromInventory);

        String userNameFromInventory = jsonFromInventory.getString("user.name");
        String userNameFromSystem = jsonFromSystem.getString("user.name");
        this.assertProperty("user.name", systemServiceIp, userNameFromSystem,
                        userNameFromInventory);

        invResponse.close();
        sysResponse.close();
    }

    @Test
    @Order(4)
    public void testUnknownHost() {
        Response response = this.getResponse(invUrl);
        this.assertResponse(invUrl, response);

        Response badResponse = client.target(invUrl + "badhostname")
                        .request(MediaType.APPLICATION_JSON).get();

        String obj = badResponse.readEntity(String.class);

        boolean isError = obj.contains("error");
        assertTrue(isError,
                        "badhostname is not a valid host but it didn't raise an error");

        response.close();
        badResponse.close();
    }

    private Response getResponse(String url) {
        return client.target(url).request().get();
    }


    private void assertResponse(String url, Response response) {
        assertEquals(200, response.getStatus(), "Incorrect response code from " + url);
    }

    private void assertProperty(String propertyName, String hostname, String expected,
                    String actual) {
        assertEquals(expected, actual, "JVM system property [" + propertyName + "] "
                        + "in the system service does not match the one stored in "
                        + "the inventory service for " + hostname);
    }

    private void visitSystemService() {
        Response response = this.getResponse(sysUrl);
        this.assertResponse(sysUrl, response);
        response.close();

        Response targetResponse = client.target(invUrl + systemServiceIp).request()
                        .get();

        targetResponse.close();
    }
}
```
{: codeblock}



* The **testEmptyInventory()** method checks that the **inventory** service has a total of 0 systems before anything is added to it.
* The **testHostRegistration()** method checks that the **system** service was added to **inventory** properly.
* The **testSystemPropertiesMatch()** checks that the **system** properties match what was added into the **inventory** service.
* The **testUnknownHost()** method checks that an error is raised if an unknown host name is being added into the **inventory** service.
* The **systemServiceIp** variable has the same value as what you retrieved in the previous section when manually adding the **system** service into the **inventory** service. This value of the IP address is passed in when you run the tests.

### Running the tests

Run the Maven **package** goal to compile the test classes. Run the Maven **failsafe** goal to test the services that are running in the Docker containers by replacing the **[system-ip-address]** with the IP address that was determined in the previous section.

```
mvn package
mvn failsafe:integration-test -Dsystem.ip=[system-ip-address] -Dinventory.http.port=9081 -Dsystem.http.port=9080
```
{: codeblock}


If the tests pass, you see a similar output as the following:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.653 s - in it.io.openliberty.guides.system.SystemEndpointIT

Results:

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.935 s - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results:

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

When you are finished with the services, run the following commands to stop and remove your containers:

```
docker stop inventory system 
docker rm inventory system
```
{: codeblock}




# Summary

## Clean up your environment

Delete the **guide-containerize** project by navigating to the **/home/project/** directory

```
cd ../..
rm -r -f guide-containerize
rmdir guide-containerize
```
{: codeblock}


## Great work! You're done!


You have just built Docker images and run two microservices on Open Liberty in containers. 

