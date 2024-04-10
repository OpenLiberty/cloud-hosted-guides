---
markdown-version: v1
title: instructions
branch: lab-455-instruction
version-history-start-date: 2021-10-18 15:12:46 UTC
tool-type: theia
---
::page{title="Welcome to the Containerizing, packaging, and running a Spring Boot application guide!"}

Learn how to containerize, package, and run a Spring Boot application on Open Liberty without modification.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

The starting point of this guide is the finished application from the [Building an Application with Spring Boot](https://spring.io/guides/gs/spring-boot/) guide. If you are not familiar with Spring Boot, complete that guide first. Java 17 is required to run this project.

You will learn how to use the ***springBootUtility*** command to deploy a Spring Boot application in Docker on Open Liberty without modification. This command stores the dependent library JAR files of the application to the target library cache, and packages the remaining application artifacts into a thin application JAR file.

You will also learn how to run the Spring Boot application locally with Open Liberty, and how to package it so that it is embedded with an Open Liberty server package.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-spring-boot.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-spring-boot.git
cd guide-spring-boot
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.


::page{title="Building and running the application"}

First, build the initial Spring Boot application into an executable JAR file. Navigate to the ***start*** directory and run the Maven package command:


```bash
cd start
./mvnw package
```

You can now run the application in the embedded Tomcat web container by executing the JAR file that you built:

```bash
java -jar target/guide-spring-boot-0.1.0.jar
```

After you see the following messages, the application is ready:
```
... INFO ... [ main] com.example.springboot.Application : Started Application in 2.511 seconds (process running for 3.24)
Let's inspect the beans provided by Spring Boot:
application
...
welcomePageHandlerMapping
welcomePageNotAcceptableHandlerMapping
```


Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE. Run the following command to access the application:
```bash
curl http://localhost:8080/hello
```

The following output is returned:
```
Greetings from Spring Boot!
```

When you need to stop the application, press `Ctrl+C` in the command-line session where you ran the application.

::page{title="Building and running the application in a Docker container"}

You will build an Open Liberty Docker image to run the Spring Boot application. Using Docker, you can run your thinned application with a few simple commands. For more information on using Open Liberty with Docker, see the [Containerizing microservices](https://openliberty.io/guides/containerize.html) guide.

Learn more about Docker on the [official Docker website](https://www.docker.com/why-docker).

Install Docker by following the instructions in the [official Docker documentation](https://docs.docker.com/engine/install).

Navigate to the ***start*** directory. 

Create the ***Dockerfile*** in the ***start*** directory.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-spring-boot/start/Dockerfile
```


> Then, to open the Dockerfile file in your IDE, select
> **File** > **Open** > guide-spring-boot/start/Dockerfile, or click the following button

::openFile{path="/home/project/guide-spring-boot/start/Dockerfile"}



```
FROM icr.io/appcafe/open-liberty:full-java17-openj9-ubi as staging

COPY --chown=1001:0 target/guide-spring-boot-0.1.0.jar \
                    /staging/fat-guide-spring-boot-0.1.0.jar

RUN springBootUtility thin \
 --sourceAppPath=/staging/fat-guide-spring-boot-0.1.0.jar \
 --targetThinAppPath=/staging/thin-guide-spring-boot-0.1.0.jar \
 --targetLibCachePath=/staging/lib.index.cache

FROM icr.io/appcafe/open-liberty:kernel-slim-java17-openj9-ubi

ARG VERSION=1.0
ARG REVISION=SNAPSHOT

LABEL \
  org.opencontainers.image.authors="Your Name" \
  org.opencontainers.image.vendor="Open Liberty" \
  org.opencontainers.image.url="local" \
  org.opencontainers.image.source="https://github.com/OpenLiberty/guide-spring-boot" \
  org.opencontainers.image.version="$VERSION" \
  org.opencontainers.image.revision="$REVISION" \
  vendor="Open Liberty" \
  name="hello app" \
  version="$VERSION-$REVISION" \
  summary="The hello application from the Spring Boot guide" \
  description="This image contains the hello application running with the Open Liberty runtime."

RUN cp /opt/ol/wlp/templates/servers/springBoot3/server.xml /config/server.xml

RUN features.sh

COPY --chown=1001:0 --from=staging /staging/lib.index.cache /lib.index.cache
COPY --chown=1001:0 --from=staging /staging/thin-guide-spring-boot-0.1.0.jar \
                    /config/dropins/spring/thin-guide-spring-boot-0.1.0.jar

RUN configure.sh 
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


This Dockerfile is written in two main stages. For more information about multi-stage Dockerfiles, see the documentation on the [official Docker website](https://docs.docker.com/develop/develop-images/multistage-build/).

The first stage copies the ***guide-spring-boot-0.1.0.jar*** Spring Boot application to the ***/staging*** temporary directory, 
and then uses the Open Liberty ***springBootUtility*** command to thin the application. For more information about the ***springBootUtility*** command, see the [springBootUtility documentation](https://openliberty.io/docs/latest/reference/command/springbootUtility-thin.html).

The second stage begins with the ***Open Liberty Docker image***. The Dockerfile copies the Liberty ***server.xml*** configuration file from the ***/opt/ol/wlp/templates*** directory, which enables Spring Boot and TLS support. Then, the Dockerfile copies the Spring Boot dependent library JAR files that are at the ***lib.index.cache*** directory and the ***thin-guide-spring-boot-0.1.0.jar*** file. The ***lib.index.cache*** directory and the ***thin-guide-spring-boot-0.1.0.jar*** file were both generated in the first stage.



Use the following command to build the Docker image:
```bash
docker build -t springboot .
```

To verify that the images are built, run the ***docker images*** command to list all local Docker images:

```bash
docker images
```

Your ***springboot*** image appears in the list of Docker images:
```
REPOSITORY    TAG       IMAGE ID         CREATED           SIZE
springboot    latest    d3ffdaa81854     27 seconds ago    596MB
```

Now, you can run the Spring Boot application in a Docker container:
```bash
docker run -d --name springBootContainer -p 9080:9080 -p 9443:9443 springboot
```

Before you access your application from the browser, run the ***docker ps*** command to make sure that your container is running:

```bash
docker ps
```

You see an entry similar to the following example:
```
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                                            NAMES
e33532aa07d6        springboot          "/opt/ibm/docker/doc…"   7 seconds ago       Up 2 seconds        0.0.0.0:9080->9080/tcp, 0.0.0.0:9443->9443/tcp   springBootContainer
```

You can watch the application start by monitoring the logs:
```bash
docker logs springBootContainer
```


After the application starts, run the following command to access the application:

```bash
curl http://localhost:9080/hello
```

### Tearing down the Docker container

To stop and remove your container, run the following commands:

```bash
docker stop springBootContainer
docker rm springBootContainer
```

::page{title="Running the application on Open Liberty"}

Next, you will run the Spring Boot application locally on Open Liberty by updating the ***pom.xml*** file.

The ***pom.xml*** was created for you in this directory. 

Update the ***Maven POM*** file in the ***start*** directory.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-spring-boot/start/pom.xml, or click the following button

::openFile{path="/home/project/guide-spring-boot/start/pom.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.4</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>guide-spring-boot</artifactId>
    <version>0.1.0</version>
    <name>spring-boot-complete</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

      <!-- Enable Liberty Maven plugin -->
      <plugin>
        <groupId>io.openliberty.tools</groupId>
        <artifactId>liberty-maven-plugin</artifactId>
        <version>3.10</version>
        <configuration>
          <appsDirectory>apps</appsDirectory>
          <installAppPackages>spring-boot-project</installAppPackages>
        </configuration>
        <executions>
          <execution>
            <id>package-server</id>
            <phase>package</phase>
            <goals>
              <goal>create</goal>
              <goal>install-feature</goal>
              <goal>deploy</goal>
              <goal>package</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <!-- End of Liberty Maven plugin -->

        </plugins>
    </build>

</project>
```



Add the ***liberty-maven-plugin*** to the ***pom.xml*** file.

The ***liberty-maven-plugin*** downloads and installs Open Liberty to the ***target/liberty*** directory. The ***installAppPackages*** configuration element in the ***pom.xml*** file typically takes in the following parameters: ***dependencies***, ***project***, or ***all***. The default value is ***dependencies***, but to install the Spring Boot application to Open Liberty, the value must be ***spring-boot-project***. This value allows Maven to package, thin, and copy the ***guide-spring-boot-0.1.0.jar*** application to the Open Liberty runtime ***applications*** directory and shared library directory.

To run the Spring Boot application, the Open Liberty instance needs to be correctly configured. By default, the ***liberty-maven-plugin*** picks up the Liberty ***server.xml*** configuration file from the ***src/main/liberty/config*** directory.

Create the Liberty ***server.xml*** configuration file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-spring-boot/start/src/main/liberty/config/server.xml
```


> Then, to open the server.xml file in your IDE, select
> **File** > **Open** > guide-spring-boot/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-spring-boot/start/src/main/liberty/config/server.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="new server">

    <featureManager>
        <feature>servlet-6.0</feature>
        <feature>springBoot-3.0</feature>
    </featureManager>

    <httpEndpoint id="defaultHttpEndpoint"
                  host="*"
                  httpPort="9080"
                  httpsPort="9443" />

    <springBootApplication id="guide-spring-boot" 
                           location="thin-guide-spring-boot-0.1.0.jar"
                           name="guide-spring-boot" />

</server>
```



The ***servlet*** and ***springBoot*** features are required for the Liberty instance to run the Spring Boot application. The application port is specified as ***9080*** and the application is configured as a ***springBootApplication*** element. For more information, see the [springBootApplication element documentation](https://www.openliberty.io/docs/latest/reference/config/springBootApplication.html).

If you didn't build the Spring Boot application, run the ***package*** goal:


```bash
./mvnw package
```

Next, run the ***liberty:run*** goal. This goal creates the Open Liberty instance, installs required features, deploys the Spring Boot application to the Open Liberty instance, and starts the application.


```bash
./mvnw liberty:run
```

After you see the following message, your Liberty instance is ready:
```
The defaultServer server is ready to run a smarter planet.
```


In another command-line sesssion, run the following command to access the application:

```bash
curl http://localhost:9080/hello
```

After you finish exploring the application, press `Ctrl+C` to stop the Open Liberty instance. Alternatively, you can run the ***liberty:stop*** goal from the ***start*** directory in a separate command-line session:


```bash
./mvnw liberty:stop
```

::page{title="Packaging the application embedded with Open Liberty"}

You can update the ***pom.xml*** file to bind more Open Liberty Maven goals to the package phase. Binding these goals to the package phase allows the Maven ***package*** goal to build a Spring Boot application that is embedded with Open Liberty.

Update the Maven POM file in the ***start*** directory.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-spring-boot/start/pom.xml, or click the following button

::openFile{path="/home/project/guide-spring-boot/start/pom.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.4</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>guide-spring-boot</artifactId>
    <version>0.1.0</version>
    <name>spring-boot-complete</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

      <!-- Enable Liberty Maven plugin -->
      <plugin>
        <groupId>io.openliberty.tools</groupId>
        <artifactId>liberty-maven-plugin</artifactId>
        <version>3.10</version>
        <configuration>
          <appsDirectory>apps</appsDirectory>
          <installAppPackages>spring-boot-project</installAppPackages>
          <include>minify,runnable</include>
          <packageName>GSSpringBootApp</packageName>
        </configuration>
        <executions>
          <execution>
            <id>package-server</id>
            <phase>package</phase>
            <goals>
              <goal>create</goal>
              <goal>install-feature</goal>
              <goal>deploy</goal>
              <goal>package</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <!-- End of Liberty Maven plugin -->

        </plugins>
    </build>

</project>
```



Add the ***include*** and ***packageName*** configuration elements, and the ***executions*** element to the ***pom.xml*** file. 

The ***include*** configuration element specifies the ***minify, runnable*** values. The ***runnable*** value allows the application to be generated as a runnable JAR file. The ***minify*** value packages only what you need from your configuration files without bundling the entire Open Liberty install.

The ***packageName*** configuration element specifies that the application is generated as a ***GSSpringBootApp.jar*** file.

The ***executions*** element specifies the required Open Liberty Maven goals to generate the application that is embedded with Open Liberty. 

Next, run the Maven ***package*** goal:


```bash
./mvnw package
```

Run the repackaged Spring Boot application. This JAR file was defined previously in the ***pom.xml*** file.

```bash
java -jar target/GSSpringBootApp.jar
```

After you see the following message, your Liberty instance is ready:

```
The defaultServer server is ready to run a smarter planet.
```


In another command-line sesssion, run the following command to access the application:
```bash
curl http://localhost:9080/hello
```

When you need to stop the application, press `Ctrl+C`.


::page{title="Summary"}

### Nice Work!

You just ran a basic Spring Boot application with Open Liberty.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-spring-boot*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-spring-boot
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Containerizing,%20packaging,%20and%20running%20a%20Spring%20Boot%20application&guide-id=cloud-hosted-guide-spring-boot)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-spring-boot/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-spring-boot/pulls)



### Where to next?

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Containerizing microservices](https://openliberty.io/guides/containerize.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
