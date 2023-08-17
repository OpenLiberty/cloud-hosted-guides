---
markdown-version: v1
title: instructions
branch: lab-508-instruction
version-history-start-date: 2020-06-11 12:03:57 UTC
tool-type: theia
---
::page{title="Welcome to the Using Docker containers to develop microservices guide!"}

Learn how to use Docker containers for iterative development.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.





::page{title="What you'll learn"}

You will learn how to set up, run, and iteratively develop a simple REST application in a container with Open Liberty and Docker.

Open Liberty is a lightweight open framework for building fast and efficient cloud-native Java microservices. It’s small, lightweight, and designed with modern cloud-native application development in mind. Open Liberty simplifies the development process for these applications by automating the repetitive actions associated with running applications inside containers, like rebuilding the image and stopping and starting the container. 

You'll also learn how to create and run automated tests for your application and container.

The implementation of the REST application can be found in the ***start/src*** directory. To learn more about this application and how to build it, check out the [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html) guide.

### What is Docker?

Docker is a tool that you can use to deploy and run applications with containers. You can think of Docker like a virtual machine that runs various applications. However, unlike a typical virtual machine, you can run these applications simultaneously on a single system and independent of one another.

Learn more about Docker on the [official Docker website](https://www.docker.com/what-docker).

### What is a container?

A container is a lightweight, stand-alone package that contains a piece of software that is bundled together with the entire environment that it needs to run. Containers are small compared to regular images and can run on any environment where Docker is set up. Moreover, you can run multiple containers on a single machine at the same time in isolation from each other.

Learn more about containers on the [official Docker website](https://www.docker.com/what-container).

### Why use a container to develop?

Consider a scenario where you need to deploy your application on another environment. Your application works on your local machine, but when you try to run it on your cloud production environment, it breaks. You do some debugging and discover that you built your application with Java 8, but this cloud production environment has only Java 11 installed. Although this issue is generally easy to fix, you don't want your application to be missing dozens of version-specific dependencies. You can develop your application in this cloud environment, but that requires you to rebuild and repackage your application every time you update your code and wish to test it.

To avoid this kind of problem, you can instead choose to develop your application in a container locally, bundled together with the entire environment that it needs to run. By doing this, you know that at any point in your iterative development process, the application can run inside that container. This helps avoid any unpleasant surprises when you go to test or deploy your application down the road. Containers run quickly and do not have a major impact on the speed of your iterative development.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-docker.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-docker.git
cd guide-docker
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

In this IBM Cloud environment, you need to change the user home to ***/home/project*** by running the following command:
```bash
sudo usermod -d /home/project theia
```


::page{title="Creating the Dockerfile"}

The first step to running your application inside of a Docker container is creating a Dockerfile. A Dockerfile is a collection of instructions for building a Docker image that can then be run as a container. Every Dockerfile begins with a parent or base image on top of which various commands are run. For example, you can start your image from scratch and run commands that download and install Java, or you can start from an image that already contains a Java installation.

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-docker/start
```

Create the ***Dockerfile*** in the ***start*** directory.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-docker/start/Dockerfile
```


> Then, to open the Dockerfile file in your IDE, select
> **File** > **Open** > guide-docker/start/Dockerfile, or click the following button

::openFile{path="/home/project/guide-docker/start/Dockerfile"}



```
FROM icr.io/appcafe/open-liberty:kernel-slim-java11-openj9-ubi

ARG VERSION=1.0
ARG REVISION=SNAPSHOT

LABEL \
  org.opencontainers.image.authors="Your Name" \
  org.opencontainers.image.vendor="IBM" \
  org.opencontainers.image.url="local" \
  org.opencontainers.image.source="https://github.com/OpenLiberty/guide-docker" \
  org.opencontainers.image.version="$VERSION" \
  org.opencontainers.image.revision="$REVISION" \
  vendor="Open Liberty" \
  name="system" \
  version="$VERSION-$REVISION" \
  summary="The system microservice from the Docker Guide" \
  description="This image contains the system microservice running with the Open Liberty runtime."

USER root

COPY --chown=1001:0 src/main/liberty/config/server.xml /config/
RUN features.sh
COPY --chown=1001:0 target/*.war /config/apps/
RUN configure.sh
USER 1001
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***FROM*** instruction initializes a new build stage and indicates the parent image from which your image is built. If you don't need a parent image, then use ***FROM scratch***, which makes your image a base image. 

In this case, you’re using the ***icr.io/appcafe/open-liberty:kernel-slim-java11-openj9-ubi*** image as your parent image, which comes with the latest Open Liberty runtime.

The ***COPY*** instructions are structured as ***COPY*** ***[--chown=\<user\>:\<group\>]*** ***\<source\>*** ***\<destination\>***. They copy local files into the specified destination within your Docker image. In this case, the Liberty configuration file that is located at ***src/main/liberty/config/server.xml*** is copied to the ***/config/*** destination directory.

### Writing a .dockerignore file


When Docker runs a build, it sends all of the files and directories that are located in the same directory as the Dockerfile to its build context, making them available for use in instructions like ***ADD*** and ***COPY***. If there are files or directories you wish to exclude from the build context, you can add them to a ***.dockerignore*** file. By adding files that aren't nessecary for building your image to the ***.dockerignore*** file, you can decrease the image's size and speed up the building process. You may also want to exclude files that contain sensitive information, such as a ***.git*** folder or private keys, from the build context. 

A ***.dockerignore*** file is available to you in the ***start*** directory. This file includes the ***pom.xml*** file and some system files.


::page{title="Launching Open Liberty in dev mode"}

The Open Liberty Maven plug-in includes a ***devc*** goal that builds a Docker image, mounts the required directories, binds the required ports, and then runs the application inside of a container. This [dev mode](https://openliberty.io/docs/latest/development-mode.html), also listens for any changes in the application source code or configuration and rebuilds the image and restarts the container as necessary.

In this IBM Cloud environment, you need to pre-create the ***logs*** directory by running the following commands:

```bash
mkdir -p /home/project/guide-docker/start/target/liberty/wlp/usr/servers/defaultServer/logs
chmod 777 /home/project/guide-docker/start/target/liberty/wlp/usr/servers/defaultServer/logs
```


Build and run the container by running the ***devc*** goal from the ***start*** directory:

```bash
mvn liberty:devc
```

After you see the following message, your Liberty instance is ready in dev mode:
```
**************************************************************
*    Liberty is running in dev mode.
```

Open another command-line session and run the following command to make sure that your container is running and didn’t crash:

```bash
docker ps 
```

You should see something similar to the following output:

```
CONTAINER ID        IMAGE                   COMMAND                  CREATED             STATUS              PORTS                                                                    NAMES
ee2daf0b33e1        guide-docker-dev-mode   "/opt/ol/helpers/run…"   2 minutes ago       Up 2 minutes        0.0.0.0:7777->7777/tcp, 0.0.0.0:9080->9080/tcp, 0.0.0.0:9443->9443/tcp   liberty-dev
```


To view a full list of all available containers, you can run the ***docker ps -a*** command.


If your container runs without problems, run the following ***curl*** command to get a JSON response that contains the system properties of the JVM in your container.

```bash
curl -s http://localhost:9080/system/properties | jq
```


::page{title="Updating the application while the container is running"}

With your container running, make the following update to the source code:

Update the ***PropertiesResource*** class.

> To open the PropertiesResource.java file in your IDE, select
> **File** > **Open** > guide-docker/start/src/main/java/io/openliberty/guides/rest/PropertiesResource.java, or click the following button

::openFile{path="/home/project/guide-docker/start/src/main/java/io/openliberty/guides/rest/PropertiesResource.java"}



```java
package io.openliberty.guides.rest;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.Json;

@Path("properties-new")
public class PropertiesResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getProperties() {

        JsonObjectBuilder builder = Json.createObjectBuilder();

        System.getProperties()
              .entrySet()
              .stream()
              .forEach(entry -> builder.add((String) entry.getKey(),
                                            (String) entry.getValue()));

       return builder.build();
    }
}
```



Change the endpoint of your application from ***properties*** to ***properties-new*** by changing the ***@Path*** annotation to ***"properties-new"***.


After you make the file changes, Open Liberty automatically updates the application. To see the changes reflected in the application, run the following command in a terminal:

```bash
curl -s http://localhost:9080/system/properties-new | jq
```


::page{title="Testing the container"}


You can test this service manually by starting a Liberty instance and going to the ***http://localhost:9080/system/properties-new*** URL.
However, automated tests are a much better approach because they trigger a failure if a change introduces a bug. JUnit and the JAX-RS Client API provide a simple environment to test the application. You can write tests for the individual units of code outside of a running Liberty instance, or you can write them to call the instance directly. In this example, you will create a test that calls the instance directly.

Create the ***EndpointIT*** test class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-docker/start/src/test/java/it/io/openliberty/guides/rest/EndpointIT.java
```


> Then, to open the EndpointIT.java file in your IDE, select
> **File** > **Open** > guide-docker/start/src/test/java/it/io/openliberty/guides/rest/EndpointIT.java, or click the following button

::openFile{path="/home/project/guide-docker/start/src/test/java/it/io/openliberty/guides/rest/EndpointIT.java"}



```java
package it.io.openliberty.guides.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import jakarta.json.JsonObject;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;


public class EndpointIT {

    @Test
    public void testGetProperties() {
        String port = System.getProperty("liberty.test.port");
        String url = "http://localhost:" + port + "/";

        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(url + "system/properties-new");
        Response response = target.request().get();
        JsonObject obj = response.readEntity(JsonObject.class);

        assertEquals(200, response.getStatus(), "Incorrect response code from " + url);

        assertEquals("/opt/ol/wlp/output/defaultServer/",
                     obj.getString("server.output.dir"),
                     "The system property for the server output directory should match "
                     + "the Open Liberty container image.");

        response.close();
    }
}
```



This test makes a request to the ***/system/properties-new*** endpoint and checks to make sure that the response has a valid status code, and that the information in the response is correct. 

### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

You will see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.rest.EndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.884 sec - in it.io.openliberty.guides.rest.EndpointIT

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

When you are finished, press `Ctrl+C` in the session that the dev mode was
started from to stop and remove the container.


::page{title="Starting dev mode with run options"}

Another useful feature of dev mode with a container is the ability to pass additional options to the ***docker run*** command. You can do this by adding the ***dockerRunOpts*** tag to the ***pom.xml*** file under the ***configuration*** tag of the Liberty Maven Plugin. Here is an example of an environment variable being passed in:

```
<groupId>io.openliberty.tools</groupId>
<artifactId>liberty-maven-plugin</artifactId>
<version>3.7.1</version>
<configuration>
    <dockerRunOpts>-e ENV_VAR=exampleValue</dockerRunOpts>
</configuration>
```

If the Dockerfile isn't located in the directory that the ***devc*** goal is being run from, you can add the ***dockerfile*** tag to specify the location. Using this parameter sets the context for building the Docker image to the directory that contains this file.

Additionally, both of these options can be passed from the command line when running the ***devc*** goal by adding ***-D*** as such:

```
mvn liberty:devc \
-DdockerRunOpts="-e ENV_VAR=exampleValue" \
-Ddockerfile="./path/to/file"
```

To learn more about dev mode with a container and its different features, check out the [Documentation](http://github.com/OpenLiberty/ci.maven/blob/main/docs/dev.md#devc-container-mode).

::page{title="Summary"}

### Nice Work!

You just iteratively developed a simple REST application in a container with Open Liberty and Docker.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-docker*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-docker
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Using%20Docker%20containers%20to%20develop%20microservices&guide-id=cloud-hosted-guide-docker)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-docker/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-docker/pulls)



### Where to next?

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Containerizing microservices](https://openliberty.io/guides/containerize.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
