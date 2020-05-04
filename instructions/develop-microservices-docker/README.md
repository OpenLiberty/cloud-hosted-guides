# Using Docker containers to develop microservices

### What you’ll learn

You will learn how to set up, run, and iteratively develop a simple REST application in a container with Docker and Open Liberty.

### What is Docker?
Docker is a tool that you can use to deploy and run applications with containers. You can think of Docker like a virtual machine that runs various applications. However, unlike a typical virtual machine, you can run these applications simultaneously on a single system and independent of one another.

If you are interested learn more about Docker on the [official Docker website.](https://www.docker.com/why-docker)

### What is a container?
A container is a lightweight, stand-alone package that contains a piece of software that is bundled together with the entire environment that it needs to run. Containers are small compared to regular images and can run on any environment where Docker is set up. Moreover, you can run multiple containers on a single machine at the same time in isolation from each other.

Learn more about containers on the [official Docker website.](https://www.docker.com/resources/what-container)

### Why use containers?
Consider a scenario where you need to deploy your application on another environment. Your application works on your local machine, but when you try to run it on a different environment, it breaks. You do some debugging and discover that you built your application with Java 8, but this new environment has only Java 11 installed. Although this issue is generally easy to fix, you don’t want your application to be missing dozens of version-specific dependencies. You can create a virtual machine specifically for testing your application, but VM images generally take up a huge amount of space and are slow to run.

To solve the problem, you can containerize your application by bundling it together with the entire environment that it needs to run. You can then run this container on any machine that is running Docker regardless of how that machine’s environment is set up. You can also run multiple containers on a single machine in isolation from one another so that two containers that have different versions of Java do not interfere with each other. Containers are quick to run compared to individual VMs, and they take up only a fraction of the memory of a single virtual machine image.

The implementation of the REST application can be found in the **start/src** directory. To learn more about this application and how to build it, read Creating a [RESTful web service](https://openliberty.io/guides/rest-intro.html).

To iteratively develop your application in a container, first build it with Maven and add it to the servers of your choice. Second, create a Docker image that contains an Open Liberty runtime. Third, run this image and mount a single server directory or the directory that contains all of your servers to the container’s file system. Finally, run one of the mounted servers inside of a container.

## Getting Started

If a terminal window does not open navigate:

> Terminal -> New Terminal

Check you are in the **home/project** folder:

```
pwd
```
{: codeblock}

The fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-docker.git
cd guide-docker
```
{: codeblock}

The **start** directory contains the starting project that you will build upon.

# Building your application

Navigate to the **start** directory and run the Maven install goal to build your application:

```
cd start
mvn install
```
{: codeblock}

Your **pom.xml** file is already configured to add your REST application to the **defaultServer.** But you can tweak this configuration or add your own for another server by updating the **<execution/>** element.

The install-apps goal copies the application into the specified directory of the specified server. In this case, the goal copies the **rest.war** file into the apps directory of the **defaultServer** server.

To learn more about this goal on the official [Maven Liberty plug-in repository.](https://github.com/OpenLiberty/ci.maven/blob/2.x/docs/install-apps.md)

# Creating the Dockerfile

A Dockerfile is a collection of instructions for building a Docker image that can then be run as a container. Every Dockerfile begins with a parent or base image on top of which various commands are run. For example, you can start your image from scratch and execute commands that download and install Java, or you can start from an image that already contains a Java installation.

Create the Dockerfile.

```
touch dockerfile
```
{: codeblock}

Open the **dockerfile**

> [File -> open] guide-docker/start/dockerfile

Add the contents into the **dockerfile**

```
# Start with OL runtime.
FROM open-liberty

USER root
# Symlink servers directory for easier mounts.
RUN ln -s /opt/ol/wlp/usr/servers /servers
USER 1001

# Run the server script and start the defaultServer by default.
ENTRYPOINT ["/opt/ol/wlp/bin/server", "run"]
CMD ["defaultServer"]
```
{: codeblock}

Close and save **dockerfile** by pressing the 'x' button and pressing 'save'.

### A breakdown of the dockerfile

#### FROM

This instruction initializes a new build stage and indicates the parent image from which your image is built. 

In this case, you’re using the **openliberty/open-liberty:javaee8** image as your parent image, which comes with the latest Open Liberty runtime.

#### RUN

The **RUN** instruction executes various shell commands in a new layer on top of the current image. In this case, you create a symlink between the **/opt/ol/wlp/usr/servers** directory and the **/servers** directory. This way, you can mount your servers more easily because you don’t need to use long path names. Note that since the Open Liberty Docker image runs by default with user 1001 (which is a non-root user), you must temporarily switch to the **root** user to create the symlink. This is done by using the **USER** instruction.

#### ENTRYPOINT

The ****ENTRYPOINT**** and **CMD** instructions define a default command that executes when the image runs as a container. These two instructions function the same way, except that the **CMD** instruction is overridden with any arguments that are passed at the end of the docker run command. In contrast, the **ENTRYPOINT** instruction requires the --entrypoint flag to be overridden. In this case, you use the **ENTRYPOINT** instruction to start an Open Liberty server and the **CMD** instruction to indicate which server to start. Because the **CMD** instruction is easily overridden, starting any server is convenient.

For a complete list of available instructions, see the [Docker documentation.](https://docs.docker.com/engine/reference/builder/)

### Optional: Writing a .dockerignore file

When Docker runs a build, it sends all of the files and directories that are located in the same directory as the Dockerfile to its build context, making them available for use in instructions like **ADD** and **COPY**. To make image building faster, add all files and directories that aren’t necessary for building your image to a **.dockerignore** file. This excludes them from the build context.

A **.dockerignore** file is available to you in the **start** directory. This file includes the **src** directory, the **pom.xml** file, and some system files. Feel free to add anything else that you want to exclude.

```
.DS_Store
src/
pom.xml
```

# Building the image

If you execute your build from the same directory as your Dockerfile, (which you are) you can use the period character (.) notation to specify the location for the build context. Otherwise, (if you weren't you could) use the **-f** flag to point to your Dockerfile.

Run 

```
docker build -t ol-runtime .
```
{: codeblock}

Use the **-t** flag to give the image an optional name. In this case, **ol-runtime** is the name of your image.

The first build usually takes much longer to complete than subsequent builds because Docker needs to download all dependencies that your image requires, including the parent image.

If your build runs successfully, you’ll see an output similar to the following:

```
...
Digest: sha256:b7576e4278030537765d4185c4641ee2769194226263f979109ed4fa0e1aa4e4
Status: Downloaded newer image for open-liberty:latest
 ---> 45d4b67ace5b
Step 2/4 : RUN ln -s /opt/ol/wlp/usr/servers /servers
 ---> Running in cbeb275770ab
Removing intermediate container cbeb275770ab
 ---> 937183f8460b
Step 3/4 : ENTRYPOINT ["/opt/ol/wlp/bin/server", "run"]
 ---> Running in 856a4bdec82b
Removing intermediate container 856a4bdec82b
 ---> 6cf732381877
Step 4/4 : CMD ["defaultServer"]
 ---> Running in 1a543a9e37d8
Removing intermediate container 1a543a9e37d8
 ---> 8fdcad065d25
Successfully built 8fdcad065d25
Successfully tagged ol-runtime:latest
```

Each step of the build has a unique ID, which represents the ID of an intermediate image. For example, step 2 has the ID **937183f8460b**, and step 4 has the ID **8fdcad065d25**, which is also the ID of the final image. During the first build of your image, Docker caches every new layer as a separate image and reuses them for future builds for layers that didn’t change. For example, if you run the build again, Docker reuses the images that it cached for steps 2 - 4. However, if you make a change in your Dockerfile, Docker would need to rebuild the subsequent layer since this layer also changed.

### The '**no-cache=true flag**'

However, you can also completely disable the caching of intermediate layers by running the build with the **--no-cache=true flag**

```
docker build -t ol-runtime --no-cache=true .
```
{: codeblock}

# Running your application in Docker container

Now that your image is built, execute the Docker **run** command

```
docker run -d --name rest-app -p 9080:9080 -p 9443:9443 -v $(pwd)/target/liberty/wlp/usr/servers:/servers -u `id -u` ol-runtime
```
{: codeblock}

### A breakdown of the flags

**-d** | Flag tells Docker to run the container in the background. Without this flag, Docker runs the container in the foreground.

**--name** | Flag specifies a name for the container.

**-p** | Flag maps the container ports to the host ports.

**-v** | This flag mounts a directory or file to the file system of the container.

As an insight you can pass in an optional server name at the end of the **run** command to override the **defaultServer** server in the **CMD** instruction. For example, if your servers directory also contains a server called **testServer**, then it can be started as shown in the following example:

```
docker run -d --name rest-app -p 9080:9080 -p 9443:9443 -v /home/project/guide-docker/start/target/liberty/wlp/usr/servers:/servers ol-runtime testServer
```
{: codeblock}

# Testing the container

Check the container is running:

```
docker ps
```
{: codeblock}

Output:
```
theia@theiadocker:/home/project/guide-docker/finish$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                                            NAMES
ff0209191be9        ol-runtime          "/opt/ol/wlp/bin/ser…"   2 minutes ago       Up 2 minutes        0.0.0.0:9080->9080/tcp, 0.0.0.0:9443->9443/tcp   rest-app
```

As well, you can view the logs and see the Open Liberty server starting. This is important because if it does not start or errors occur, thats one the first places to look. Replace <Container ID> with the container ID from your output.

`docker logs <Container ID> ` (ff0209101be9)

```
[AUDIT   ] CWWKF0013I: The server removed the following features: [el-3.0, jsp-2.3, servlet-3.1].
[AUDIT   ] CWWKF0011I: The defaultServer server is ready to run a smarter planet. The defaultServer server started in 4.371 seconds.
```

Curl the **/System/properties**, where you can see a JSON file that contains the system properties of the JVM in your container.

```
curl http://localhost:9080/LibertyProject/System/properties
```
{: codeblock}

Also, you can see that the docker container is running by accessing Open Liberty via the web browser.
To view this click on **Launch Application** and type in the **port number**.

The Open Liberty server runs on `9080`.

### Update the PropertiesResource class.

To open the file click the the file button in the top left hand corner

> [File -> Open] guide-docker/start/src/main/java/io/openliberty/guides/rest/PropertiesResource.java

Note the endpoint of your application is going to change from **properties** to **properties-new**. This has been updated by the **@Path** annotation being changed.

Replace the existing code with:

```java
package io.openliberty.guides.rest;

import java.util.Properties;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.GET;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArray;
import javax.json.Json;
import javax.json.JsonNumber;

@Path("properties-new")
public class PropertiesResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getProperties() {

        JsonObjectBuilder builder = Json.createObjectBuilder();

        System.getProperties()
              .entrySet()
              .stream()
              .forEach(entry -> builder.add((String)entry.getKey(),
                                            (String)entry.getValue()));

       return builder.build();
    }
}
```
{: codeblock}

Close and save **PropertiesResource** by pressing the 'x' button and pressing 'save'.

Rebuild the application 

Ensure you are in **/home/project/guide-docker/start**

Run: 

```
mvn package
```
{: codeblock}

View the changes reflected in the container, and point your browser to `/System/properties-new`.

```
curl http://localhost:9080/LibertyProject/System/properties-new
```
{: codeblock}

You see the same JSON file that you saw previously.

To stop your container, run:

```
docker stop rest-app
```
{: codeblock}

# Summary 

### Clean up your environment 

Delete the **guide-docker** project by navigating to the **/home/project/** directory

```
rm -r -f guide-docker
```
{: codeblock}

### Well done

Congratulations, you have successfully completed the lab. 

From this lab you should now have an understanding of: what Docker is, why you would use containers, what a **dockerfile** is, creating a dockerfile, building the image and also running the container to see the JVM output and updating the container. 

