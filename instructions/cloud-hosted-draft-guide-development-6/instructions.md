
# **Welcome to the Containerizing, packaging, and running a Spring Boot application guide!**



In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.


The starting point of this guide is the finished application from Spring's
[Building an Application with Spring Boot](https://spring.io/guides/gs/spring-boot/) guide.
If you are not familiar with Spring Boot, complete that guide first.
Java 8 is required to run this project.

You will learn how to use the **springBootUtility** command to deploy a Spring Boot application in Docker on an Open Liberty server without modification.
This command stores the dependent library JAR files of the application to the target library cache,
and packages the remaining application artifacts into a thin application JAR file.

You will also learn how to run the Spring Boot application locally with an Open Liberty server,
and how to package it so that it is embedded with an Open Liberty server.

# **Getting started**

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-spring-boot.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-spring-boot.git
cd guide-spring-boot
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.


# **Building and running the application**

First, build the initial Spring Boot application into an executable JAR file. 
Navigate to the **start** directory and run the Maven package command:


```
cd start
./mvnw package
```

You can now run the application in the embedded Tomcat web container by executing the JAR file that you built:

```
java -jar target/guide-spring-boot-0.1.0.jar
```

Notice that the console output displays a message about the application running in Tomcat on port **8080**. 
```
... INFO ... [ main] o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat started on port(s): 8080 (http) with context path ''
```


Run the following command to access the application:

```
curl http://localhost:8080/hello
```
{: codeblock}

The following output is returned:

```
Greetings from Spring Boot!
```

When you need to stop the application, press **CTRL+C** in the command-line session where you ran the application.

# **Building and running the application in a Docker container**

You will build an Open Liberty Docker image to run the Spring Boot application.
Using Docker, you can run your thinned application with a few simple commands.
For more information on using Open Liberty with Docker, see the
[Containerizing microservices](https://openliberty.io/guides/containerize.html) guide.

Learn more about Docker on the [official Docker website](https://www.docker.com/why-docker).

Install Docker by following the instructions in the
[official Docker documentation](https://docs.docker.com/engine/install).

Navigate to the **start** directory. 

Create the **Dockerfile**.

> Run the following touch command in your terminal
```
touch /home/project/guide-spring-boot/start/Dockerfile
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-spring-boot/start/Dockerfile




This Dockerfile is written in two main stages.
For more information about multi-stage Dockerfiles, see the documentation on the
[official Docker website](https://docs.docker.com/develop/develop-images/multistage-build/).

The first stage copies the **guide-spring-boot-0.1.0.jar**
Spring Boot application to the **/staging** temporary directory, 
and then uses the Open Liberty **springBootUtility** command to thin the application. 
For more information about the **springBootUtility** command, 
see the [springBootUtility documentation](https://openliberty.io/docs/latest/reference/command/springbootUtility-thin.html).

The second stage begins with the **Open Liberty Docker image**. 
The Dockerfile copies the **server.xml** file from the **/opt/ol/wlp/templates** directory, 
which enables Spring Boot and TLS support. 
Then, the Dockerfile copies the Spring Boot dependent library JAR files that are at the **lib.index.cache** directory 
and the **thin-guide-spring-boot-0.1.0.jar** file. 
The **lib.index.cache** directory and the **thin-guide-spring-boot-0.1.0.jar** file were both generated in the first stage.

Run the following command to download or update to the latest Open Liberty Docker image:

```
docker pull openliberty/open-liberty:full-java11-openj9-ubi
```
{: codeblock}


Use the following command to build the Docker image:
```
docker build -t springboot .
```
{: codeblock}


To verify that the images are built, run the **docker images** command to list all local Docker images:

```
docker images
```
{: codeblock}


Your **springboot** image appears in the list of Docker images:
```
REPOSITORY    TAG       IMAGE ID         CREATED           SIZE
springboot    latest    d3ffdaa81854     27 seconds ago    486MB
```

Now, you can run the Spring Boot application in a Docker container:
```
docker run -d --name springBootContainer -p 9080:9080 -p 9443:9443 springboot
```
{: codeblock}


Before you access your application from the browser,
run the **docker ps** command to make sure that your container is running:

```
docker ps
```
{: codeblock}



You see an entry similar to the following example:
```
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                                            NAMES
e33532aa07d6        springboot          "/opt/ibm/docker/doc…"   7 seconds ago       Up 2 seconds        0.0.0.0:9080->9080/tcp, 0.0.0.0:9443->9443/tcp   springBootContainer
```

You can watch the application start by monitoring the logs:
```
docker logs springBootContainer
```
{: codeblock}



After the application starts, run the following command to access the application:

```
curl http://localhost:9080/hello
```
{: codeblock}

<br/>
### **Tearing down the Docker container**

To stop and remove your container, run the following commands:

```
docker stop springBootContainer
docker rm springBootContainer
```
{: codeblock}


# **Running the application on Open Liberty**

Next, you will run the Spring Boot application locally on Open Liberty by updating the **pom.xml** file.

The **pom.xml** was created for you in this directory. 

Update the **Maven POM** file.

> From the menu of the IDE, select 
> **File** > **Open** > guide-spring-boot/start/pom.xml





The **liberty-maven-plugin** downloads and installs Open Liberty to the **target/liberty** directory.
The **`<installAppPackages/>`** configuration tag in the
**pom.xml** file typically takes in the following parameters: **dependencies**, **project**, or **all**.
The default value is **dependencies**, but to install the Spring Boot application to Open Liberty,
the value must be **spring-boot-project**.
This value allows Maven to package, thin, and copy the **guide-spring-boot-0.1.0.jar** application
to the Open Liberty runtime **applications** directory and shared library directory.

To run the Spring Boot application, the Open Liberty server needs to be correctly configured.
By default, the **liberty-maven-plugin** picks up the server configuration file from the **src/main/liberty/config** directory.

Create the **server.xml**.

> Run the following touch command in your terminal
```
touch /home/project/guide-spring-boot/start/src/main/liberty/config/server.xml
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-spring-boot/start/src/main/liberty/config/server.xml



The **servlet** and **springBoot** features
are required for the Liberty server to run the Spring Boot application.
The application port is specified as **9080** and
the application is configured as a **`<springBootApplication />`**.


If you didn't build the Spring Boot application, run the **package** goal:


```
./mvnw package
```
{: codeblock}



Next, run the **liberty:run** goal.
This goal creates the Open Liberty server, installs required features,
deploys the Spring Boot application to the Open Liberty server, and starts the application.


```
./mvnw liberty:run
```
{: codeblock}




In another command line sesssion, run the following command to access the application:

```
curl http://localhost:9080/hello
```
{: codeblock}

After you finish exploring the application, press **CTRL+C** to stop the Open Liberty server.
Alternatively, you can run the **liberty:stop** goal from the **start** directory in a separate command-line session:


```
./mvnw liberty:stop
```
{: codeblock}



# **Packaging the application embedded with Open Liberty**

You can update the **pom.xml** file to bind more Open Liberty Maven goals to the package phase.
Binding these goals to the package phase allows the Maven **package** goal to build a Spring Boot application that is embedded with Open Liberty.

Update the Maven POM file.

> From the menu of the IDE, select 
> **File** > **Open** > guide-spring-boot/start/pom.xml



and the **`<executions/>`** tag to the **pom.xml** file. 
 

The **`<include/>`** configuration tag specifies the **minify, runnable** values.
The **runnable** value allows the application to be generated as a runnable JAR file.
The **minify** value packages only what you need from your configuration files without bundling the entire Open Liberty install.

The **`<packageName/>`** configuration tag specifies
that the application is generated as a **GSSpringBootApp.jar** file.

The **`<executions/>`** tag specifies the required
Open Liberty Maven goals to generate the application that is embedded with Open Liberty. 

Next, run the Maven **package** goal:


```
./mvnw package
```
{: codeblock}



Run the repackaged Spring Boot application. This JAR file was defined previously in the **pom.xml** file.

```
java -jar target/GSSpringBootApp.jar
```
{: codeblock}



In another command line sesssion, run the following command to access the application:

```
curl http://localhost:9080/hello
```
{: codeblock}

When you need to stop the application, press **CTRL+C**.


# **Summary**

## **Nice Work!**

You just ran a basic Spring Boot application with Open Liberty.



<br/>
## **Clean up your environment**


Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-spring-boot** project by running the following commands:

```
cd /home/project
rm -fr guide-spring-boot
```
{: codeblock}

<br/>
## **What did you think of this guide?**

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Containerizing,%20packaging,%20and%20running%20a%20Spring%20Boot%20application&guide-id=cloud-hosted-guide-spring-boot)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

<br/>
## **What could make this guide better?**

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-spring-boot/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-spring-boot/pulls)



<br/>
## **Where to next?**

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Containerizing microservices](https://openliberty.io/guides/containerize.html)


<br/>
## **Log out of the session**

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.