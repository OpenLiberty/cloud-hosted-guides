---
markdown-version: v1
title: instructions
branch: lab-5933-instruction
version-history-start-date: 2023-04-14T18:24:15Z
tool-type: theia
---
::page{title="Welcome to the Containerizing microservices guide!"}

Learn how to containerize and run your microservices with Open Liberty using Docker.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}


From development to production, and across your DevOps environments, you can deploy your microservices in a lightweight and portable manner by using containers. You can run a container from a container image. Each container image is a package of what you need to run your microservice or application, from the code to its dependencies and configuration. If you're new to the development of applications in containers, you might want to start with the [Using Docker containers to develop microservices](https://openliberty.io/guides/docker.html) guide before you work through this guide.

You'll learn how to build container images and run containers using [Docker](https://www.docker.com/) for your microservices. You'll learn about the [Open Liberty container images](https://github.com/OpenLiberty/ci.docker) and how to use them for your containerized applications. You'll construct ***Dockerfile*** files, create Docker images by using the ***docker build*** command, and run the image as Docker containers by using ***docker run*** command.

The two microservices that you'll be working with are called ***system*** and ***inventory***. The ***system*** microservice returns the JVM system properties of the running container. The ***inventory*** microservice adds the properties from the ***system*** microservice to the inventory. This guide demonstrates how both microservices can run and communicate with each other in different Docker containers. 

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-containerize.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-containerize.git
cd guide-containerize
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.


::page{title="Packaging your microservices"}


To begin, run the following command to navigate to the **start** directory:
```bash
cd start
```

You can find the starting Java project in the ***start*** directory. This project is a multi-module Maven project that is made up of the ***system*** and ***inventory*** microservices. Each microservice is located in its own corresponding directory, ***system*** and ***inventory***.

To try out the microservices by using Maven, run the following Maven goal to build the ***system*** microservice and run it inside Open Liberty:
```bash
mvn -pl system liberty:run
```


Select **Terminal** > **New Terminal** from the menu of the IDE to open another command-line session and run the following Maven goal to build the **inventory** microservice and run it inside Open Liberty:
```bash
cd /home/project/guide-containerize/start
mvn -pl inventory liberty:run
```

After you see the following message in both command-line sessions, both of your services are ready:

```
The defaultServer server is ready to run a smarter planet.
```

Select **Terminal** > **New Terminal** from the menu of the IDE to open a new command-line session. To access the **inventory** service, which displays the current contents of the inventory, run the following curl command: 
```bash
curl -s http://localhost:9081/inventory/systems | jq
```

The **system** service shows the system properties of the running JVM and can be found by running the following curl command:
```bash
curl -s http://localhost:9080/system/properties | jq
```

The system properties of your localhost can be added to the **inventory** service at **http://localhost:9081/inventory/systems/localhost**. Run the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems/localhost | jq
```


After you are finished checking out the microservices, stop the Open Liberty servers by pressing **CTRL+C** in the command-line sessions where you ran the servers. Alternatively, you can run the **liberty:stop** goal in another command-line session from the 
**start** directory:
```bash
cd /home/project/guide-containerize/start
mvn -pl system liberty:stop
mvn -pl inventory liberty:stop
```

To package your microservices, run the Maven package goal to build the application ***.war*** files from the start directory so that the ***.war*** files are in the ***system/target*** and ***inventory/target*** directories.
```bash
mvn package
```

To learn more about RESTful web services and how to build them, see [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html) for details about how to build the ***system*** service. The ***inventory*** service is built in a similar way.


::page{title="Building your Docker images"}

A Docker image is a binary file. It is made up of multiple layers and is used to run code in a Docker container. Images are built from instructions in Dockerfiles to create a containerized version of the application.

A ***Dockerfile*** is a collection of instructions for building a Docker image that can then be run as a container. As each instruction is run in a ***Dockerfile***, a new Docker layer is created. These layers, which are known as intermediate images, are created when a change is made to your Docker image.

Every ***Dockerfile*** begins with a parent or base image over which various commands are run. For example, you can start your image from scratch and run commands that download and install a Java runtime, or you can start from an image that already contains a Java installation.

Learn more about Docker on the [official Docker page](https://www.docker.com/what-docker).

### Creating your Dockerfiles
You will be creating two Docker images to run the ***inventory*** service and ***system*** service. The first step is to create Dockerfiles for both services.

In this guide, you're using an official image from the IBM Container Registry (ICR), ***icr.io/appcafe/open-liberty:full-java11-openj9-ubi***, as your parent image. This image is tagged with the word ***full***, meaning it includes all Liberty features. ***full*** images are recommended for development only because they significantly expand the image size with features that are not required by the application.

To minimize your image footprint in production, you can use one of the ***kernel-slim*** images, such as ***icr.io/appcafe/open-liberty:kernel-slim-java11-openj9-ubi***.  This image installs the basic server. You can then add all the necessary features for your application with the usage pattern that is detailed in the Open Liberty [container image documentation](https://openliberty.io/docs/latest/container-images.html#build). To use the default image that comes with the Open Liberty runtime, define the ***FROM*** instruction as ***FROM icr.io/appcafe/open-liberty***. You can find all official images on the Open Liberty [container image repository](https://openliberty.io/docs/latest/container-images.html).

Create the ***Dockerfile*** for the inventory service.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-containerize/start/inventory/Dockerfile
```


> Then, to open the Dockerfile file in your IDE, select
> **File** > **Open** > guide-containerize/start/inventory/Dockerfile, or click the following button

::openFile{path="/home/project/guide-containerize/start/inventory/Dockerfile"}



```
FROM icr.io/appcafe/open-liberty:full-java11-openj9-ubi

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
    src/main/liberty/config \
    /config/

COPY --chown=1001:0 \
    target/guide-containerize-inventory.war \
    /config/apps

RUN configure.sh
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***FROM*** instruction initializes a new build stage, which indicates the parent image of the built image. If you don't need a parent image, then you can use ***FROM scratch***, which makes your image a base image. 

It is also recommended to label your Docker images with the ***LABEL*** command, as the label information can help you manage your images. For more information, see [Best practices for writing Dockerfiles](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/#label).

The ***COPY*** instructions are structured as ***COPY*** ***[--chown=\<user\>:\<group\>]*** ***\<source\>*** ***\<destination\>***. They copy local files into the specified destination within your Docker image. In this case, the ***inventory*** server configuration files that are located at ***src/main/liberty/config*** are copied to the ***/config/*** destination directory. The ***inventory*** application WAR file ***inventory.war***, which was created from running ***mvn package***, is copied to the ***/config/apps*** destination directory.

The ***COPY*** instructions use the ***1001*** user ID  and ***0*** group because the ***icr.io/appcafe/open-liberty:full-java11-openj9-ubi*** image runs by default with the ***USER 1001*** (non-root) user for security purposes. Otherwise, the files and directories that are copied over are owned by the root user.

Place the ***RUN configure.sh*** command at the end to get a pre-warmed Docker image. It improves the startup time of running your Docker container.

The ***Dockerfile*** for the ***system*** service follows the same instructions as the ***inventory*** service, except that some ***labels*** are updated, and the ***system.war*** archive is copied into ***/config/apps***.

Create the ***Dockerfile*** for the system service.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-containerize/start/system/Dockerfile
```


> Then, to open the Dockerfile file in your IDE, select
> **File** > **Open** > guide-containerize/start/system/Dockerfile, or click the following button

::openFile{path="/home/project/guide-containerize/start/system/Dockerfile"}



```
FROM icr.io/appcafe/open-liberty:full-java11-openj9-ubi

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

COPY --chown=1001:0 target/guide-containerize-system.war /config/apps

RUN configure.sh
```




### Building your Docker image

Now that your microservices are packaged and you have written your Dockerfiles, you will build your Docker images by using the ***docker build*** command.

Run the following command to download or update to the latest Open Liberty Docker image:

```bash
docker pull icr.io/appcafe/open-liberty:full-java11-openj9-ubi
```

Run the following commands to build container images for your application:

```bash
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
```

The ***-t*** flag in the ***docker build*** command allows the Docker image to be labeled (tagged) in the ***name[:tag]*** format. The tag for an image describes the specific image version. If the optional ***[:tag]*** tag is not specified, the ***latest*** tag is created by default.

To verify that the images are built, run the ***docker images*** command to list all local Docker images:

```bash
docker images
```

Or, run the ***docker images*** command with ***--filter*** option to list your images:
```bash
docker images -f "label=org.opencontainers.image.authors=Your Name"
```

Your ***inventory*** and ***system*** images appear in the list of all Docker images:

```
REPOSITORY    TAG             IMAGE ID        CREATED          SIZE
inventory     1.0-SNAPSHOT    08fef024e986    4 minutes ago    1GB
system        1.0-SNAPSHOT    1dff6d0b4f31    5 minutes ago    977MB
```


::page{title="Running your microservices in Docker containers"}
Now that your two images are built, you will run your microservices in Docker containers:

```bash
docker run -d --name system -p 9080:9080 system:1.0-SNAPSHOT
docker run -d --name inventory -p 9081:9081 inventory:1.0-SNAPSHOT
```

The following table describes the flags in these commands:

| *Flag* | *Description*
| ---| ---
| -d     | Runs the container in the background.
| --name | Specifies a name for the container.
| -p     | Maps the host ports to the container ports. For example: ***-p \<HOST_PORT\>:\<CONTAINER_PORT\>***

Next, run the ***docker ps*** command to verify that your containers are started:

```bash
docker ps
```

Make sure that your containers are running and show ***Up*** as their status:

```
CONTAINER ID    IMAGE                   COMMAND                  CREATED          STATUS          PORTS                                        NAMES
2b584282e0f5    inventory:1.0-SNAPSHOT  "/opt/ol/helpers/run…"   2 seconds ago    Up 1 second     9080/tcp, 9443/tcp, 0.0.0.0:9081->9081/tcp   inventory
99a98313705f    system:1.0-SNAPSHOT     "/opt/ol/helpers/run…"   3 seconds ago    Up 2 seconds    0.0.0.0:9080->9080/tcp, 9443/tcp             system
```

If a problem occurs and your containers exit prematurely, the containers don't appear in the container list that the ***docker ps*** command displays. Instead, your containers appear with an ***Exited*** status when they run the ***docker ps -a*** command. Run the ***docker logs system*** and ***docker logs inventory*** commands to view the container logs for any potential problems. Run the ***docker stats system*** and ***docker stats inventory*** commands to display a live stream of usage statistics for your containers. You can also double-check that your Dockerfiles are correct. When you find the cause of the issues, remove the faulty containers with the ***docker rm system*** and ***docker rm inventory*** commands. Rebuild your images, and start the containers again.


To access the application, run the following curl command. An empty list is expected because no system properties are stored in the inventory yet:
```bash
curl -s http://localhost:9081/inventory/systems | jq
```

Next, retrieve the ***system*** container's IP address by running the following:

```bash
docker inspect -f "{{.NetworkSettings.IPAddress }}" system
```

The command returns the system container IP address:

```
172.17.0.2
```

In this case, the IP address for the ***system*** service is ***172.17.0.2***. Take note of this IP address to construct the URL to view the system properties. 


Run the following commands to go to the **http://localhost:9081/inventory/systems/[system-ip-address]** by replacing **[system-ip-address]** URL with the IP address that you obtained earlier:
```bash
SYSTEM_IP=`docker inspect -f "{{.NetworkSettings.IPAddress }}" system`
curl -s http://localhost:9081/inventory/systems/{$SYSTEM_IP} | jq
```

You see a result in JSON format with the system properties of your local JVM. When you visit this URL, these system properties are automatically stored in the inventory. Run the following curl command and you see a new entry for **[system-ip-address]**:
```bash
curl -s http://localhost:9081/inventory/systems | jq
```

::page{title="Externalizing server configuration"}


As mentioned at the beginning of this guide, one of the advantages of using containers is that they are portable and can be moved and deployed efficiently across all of your DevOps environments. Configuration often changes across different environments, and by externalizing your server configuration, you can simplify the development process.

Imagine a scenario where you are developing an Open Liberty application on port ***9081*** but to deploy it to production, it must be available on port ***9091***. To manage this scenario, you can keep two different versions of the ***server.xml*** file; one for production and one for development. However, trying to maintain two different versions of a file might lead to mistakes. A better solution would be to externalize the configuration of the port number and use the value of an environment variable that is stored in each environment. 

In this example, you will use an environment variable to externally configure the HTTP port number of the ***inventory*** service. 

In the ***inventory/server.xml*** file, the ***default.http.port*** variable is declared and is used in the ***httpEndpoint*** element to define the service endpoint. The default value of the ***default.http.port*** variable is ***9081***. However, this value is only used if no other value is specified. You can replace this value in the container by using the -e flag for the podman run command. 

Run the following commands to stop and remove the ***inventory*** container and rerun it with the ***default.http.port*** environment variable set:

```bash
docker stop inventory
docker rm inventory 
docker run -d --name inventory -e default.http.port=9091 -p 9091:9091 inventory:1.0-SNAPSHOT
```

The ***-e*** flag can be used to create and set the values of environment variables in a Docker container. In this case, you are setting the ***default.http.port*** environment variable to ***9091*** for the ***inventory*** container.

Now, when the service is starting up, Open Liberty finds the ***default.http.port*** environment variable and uses it to set the value of the ***default.http.port*** variable to be used in the HTTP endpoint.


The **inventory** service is now available on the new port number that you specified. You can see the contents of the inventory at the **http://localhost:9091/inventory/systems** URL. Run the following curl command:
```bash
curl -s http://localhost:9091/inventory/systems | jq
```

You can add your local system properties at the **http://localhost:9091/inventory/systems/[system-ip-address]** URL by replacing **[system-ip-address]** with the IP address that you obtained in the previous section. Run the following commands:
```bash
SYSTEM_IP=`docker inspect -f "{{.NetworkSettings.IPAddress }}" system`
curl -s http://localhost:9091/inventory/systems/{$SYSTEM_IP} | jq
```

The **system** service remains unchanged and is available at the **http://localhost:9080/system/properties** URL. Run the following curl command:
```bash
curl -s http://localhost:9080/system/properties | jq
```

You can externalize the configuration of more than just the port numbers. To learn more about Open Liberty server configuration, check out the [Server Configuration Overview](https://openliberty.io/docs/latest/reference/config/server-configuration-overview.html) docs. 

::page{title="Optimizing the image size"}

As mentioned previously, the parent image that is used in each ***Dockerfile*** contains the ***full*** tag, which includes all of the Liberty features. This parent image with the ***full*** tag is recommended for development, but while deploying to production it is recommended to use a parent image with the ***kernel-slim*** tag. The ***kernel-slim*** tag provides a bare minimum server with the ability to add the features required by the application.

Replace the ***Dockerfile*** for the inventory service.

> To open the Dockerfile file in your IDE, select
> **File** > **Open** > guide-containerize/start/inventory/Dockerfile, or click the following button

::openFile{path="/home/project/guide-containerize/start/inventory/Dockerfile"}



```
FROM icr.io/appcafe/open-liberty:kernel-slim-java11-openj9-ubi

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
    src/main/liberty/config \
    /config/

RUN features.sh

COPY --chown=1001:0 \
    target/guide-containerize-inventory.war \
    /config/apps

RUN configure.sh
```



Replace the parent image with ***icr.io/appcafe/open-liberty:kernel-slim-java11-openj9-ubi*** at the top of your ***Dockerfile***. This image contains the ***kernel-slim*** tag that is recommended when deploying to production.

Place ***RUN features.sh*** command after the ***COPY*** command that copies the local ***/config/*** directory into the ***Docker*** image. The ***features.sh*** script adds the Liberty features that your application is required to operate.

Ensure that you repeat these instructions for the ***system*** service.

Replace the ***Dockerfile*** for the system service.

> To open the Dockerfile file in your IDE, select
> **File** > **Open** > guide-containerize/start/system/Dockerfile, or click the following button

::openFile{path="/home/project/guide-containerize/start/system/Dockerfile"}



```
FROM icr.io/appcafe/open-liberty:kernel-slim-java11-openj9-ubi

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

RUN features.sh

COPY --chown=1001:0 target/guide-containerize-system.war /config/apps

RUN configure.sh
```



Continue by running the following commands to stop and remove your current ***Docker*** containers that are using the ***full*** parent image:

```bash
docker stop inventory system
docker rm inventory system
```

Next, build your new ***Docker*** images with the ***kernel-slim*** parent image:

```bash
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
```

Verify that the images have been built by executing the following command to list all the local ***Docker*** images:

```bash
docker images
```

Notice that the images for the ***inventory*** and ***system*** services now have a reduced image size.
```
REPOSITORY      TAG             IMAGE ID        CREATED         SIZE
inventory       1.0-SNAPSHOT	d5a3d1b2c20e    4 minutes ago	682MB
system          1.0-SNAPSHOT	6346cf87eae0	5 minutes ago	694MB
```

After confirming that the images have been built, run the following commands to start the ***Docker*** containers:

```bash
docker run -d --name system -p 9080:9080 system:1.0-SNAPSHOT
docker run -d --name inventory -p 9081:9081 inventory:1.0-SNAPSHOT
```

Once your ***Docker*** containers are running, run the following command to see the list of required features installed by ***features.sh***:

```bash
docker exec -it inventory /opt/ol/wlp/bin/productInfo featureInfo
```

Your list of Liberty features should be similar to the following:
```
jndi-1.0
servlet-5.0
cdi-3.0
concurrent-2.0
jsonb-2.0
jsonp-2.0
mpConfig-3.0
restfulWS-3.0
restfulWSClient-3.0
```


The **system** service which shows the system properties of the running JVM is now available to be accessed at **http://localhost:9080/system/properties**. Run the following curl command:
```bash
curl -s http://localhost:9080/system/properties | jq
```

Next, you can add your local system properties at the **http://localhost:9081/inventory/systems/[system-ip-address]** URL by replacing **[system-ip-address]** with the IP address that you obtained in the previous section. Run the following commands:
```bash
SYSTEM_IP=`docker inspect -f "{{.NetworkSettings.IPAddress }}" system`
curl -s http://localhost:9081/inventory/systems/{$SYSTEM_IP} | jq
```

Then, verify the addition of your localhost system properties to the **inventory** service at **http://localhost:9081/inventory/systems**. Run the following curl command:
```bash
curl -s http://localhost:9081/inventory/systems | jq
```

::page{title="Testing the microservices"}

You can test your microservices manually by hitting the endpoints or with automated tests that check your running Docker containers.

Create the ***SystemEndpointIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-containerize/start/system/src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java
```


> Then, to open the SystemEndpointIT.java file in your IDE, select
> **File** > **Open** > guide-containerize/start/system/src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java, or click the following button

::openFile{path="/home/project/guide-containerize/start/system/src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java"}



```java
package it.io.openliberty.guides.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

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

        WebTarget target = client.target(clusterUrl);
        Response response = target.request().get();

        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + clusterUrl);
        response.close();
    }

}
```



The ***testGetProperties()*** method checks for a ***200*** response code from the ***system*** service endpoint.

Create the ***InventoryEndpointIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-containerize/start/inventory/src/test/java/it/io/openliberty/guides/inventory/InventoryEndpointIT.java
```


> Then, to open the InventoryEndpointIT.java file in your IDE, select
> **File** > **Open** > guide-containerize/start/inventory/src/test/java/it/io/openliberty/guides/inventory/InventoryEndpointIT.java, or click the following button

::openFile{path="/home/project/guide-containerize/start/inventory/src/test/java/it/io/openliberty/guides/inventory/InventoryEndpointIT.java"}



```java
package it.io.openliberty.guides.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.json.JsonObject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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



* The ***testEmptyInventory()*** method checks that the ***inventory*** service has a total of 0 systems before anything is added to it.
* The ***testHostRegistration()*** method checks that the ***system*** service was added to ***inventory*** properly.
* The ***testSystemPropertiesMatch()*** checks that the ***system*** properties match what was added into the ***inventory*** service.
* The ***testUnknownHost()*** method checks that an error is raised if an unknown host name is being added into the ***inventory*** service.
* The ***systemServiceIp*** variable has the same value as the IP address that you retrieved in the previous section when you manually added the ***system*** service into the ***inventory*** service. This value of the IP address is passed in when you run the tests.

### Running the tests

Run the Maven **package** goal to compile the test classes. Run the Maven **failsafe** goal to test the services that are running in the Docker containers by setting **-Dsystem.ip** to the IP address that you determined previously.

```bash
SYSTEM_IP=`docker inspect -f "{{.NetworkSettings.IPAddress }}" system`
mvn package
mvn failsafe:integration-test -Dsystem.ip="$SYSTEM_IP" -Dinventory.http.port=9081 -Dsystem.http.port=9080
```

If the tests pass, you see output similar to the following example:

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

```bash
docker stop inventory system 
docker rm inventory system
```


::page{title="Summary"}

### Nice Work!

You have just built Docker images and run two microservices on Open Liberty in containers. 



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-containerize*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-containerize
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Containerizing%20microservices&guide-id=cloud-hosted-guide-containerize)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-containerize/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-containerize/pulls)



### Where to next?

* [Using Docker containers to develop microservices](https://openliberty.io/guides/docker.html)
* [Deploying microservices to Kubernetes](https://openliberty.io/guides/kubernetes-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
