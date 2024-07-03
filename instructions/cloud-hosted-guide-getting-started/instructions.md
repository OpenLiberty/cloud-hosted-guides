---
markdown-version: v1
title: instructions
branch: lab-480-instruction
version-history-start-date: 2020-04-22 12:57:16 UTC
tool-type: theia
---
::page{title="Welcome to the Getting started with Open Liberty guide!"}

Learn how to develop a Java application on Open Liberty with Maven and Docker.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You will learn how to run and update a simple REST microservice on Open Liberty. You will use Maven throughout the guide to build and deploy the microservice as well as to interact with the running Liberty instance.

Open Liberty is an open application framework designed for the cloud. It's small, lightweight, and designed with modern cloud-native application development in mind. It supports the full MicroProfile and Jakarta EE APIs and is composable, meaning that you can use only the features that you need, keeping everything lightweight, which is great for microservices. It also deploys to every major cloud platform, including Docker, Kubernetes, and Cloud Foundry.

Maven is an automation build tool that provides an efficient way to develop Java applications. Using Maven, you will build a simple microservice, called ***system***, that collects basic system properties from your laptop and displays them on an endpoint that you can access in your web browser. 

You'll also explore how to package your application with Open Liberty so that it can be deployed anywhere in one go. You will then make Liberty configuration and code changes and see how they are immediately picked up by a running instance.

Finally, you will package the application along with Liberty's configuration into a Docker image and run that image as a container.



::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-getting-started.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-getting-started.git
cd guide-getting-started
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

You have cloned a Maven project. To learn how to create a Liberty Maven project from scratch and edit your application using the Liberty Tools, see [Developing a cloud-native Java application with Liberty Tools in IntelliJ IDEA](https://openliberty.io/blog/2024/05/31/liberty-project-starter-guide-IntelliJ.html).

In this IBM Cloud environment, you need to change the user home to ***/home/project*** by running the following command:
```bash
sudo usermod -d /home/project theia
```



::page{title="Building and running the application"}

Your application is configured to be built with Maven. Every Maven-configured project contains a ***pom.xml*** file, which defines the project configuration, dependencies, plug-ins, and so on.

Your ***pom.xml*** file is located in the ***start*** directory and is configured to include the ***liberty-maven-plugin***, which allows you to install applications into Open Liberty and manage the associated Liberty instances.


To begin, navigate to the ***start*** directory. Build the ***system*** microservice that is provided and deploy it to Open Liberty by running the Maven ***liberty:run*** goal:

```bash
cd start
mvn liberty:run
```

The ***mvn*** command initiates a Maven build, during which the ***target*** directory is created to store all build-related files.

The ***liberty:run*** argument specifies the Open Liberty ***run*** goal, which starts an Open Liberty instance in the foreground. As part of this phase, an Open Liberty runtime is downloaded and installed into the ***target/liberty/wlp*** directory, an instance of Liberty is created and configured in the ***target/liberty/wlp/usr/servers/defaultServer*** directory, and the application is installed into that instance using [loose config](https://www.ibm.com/support/knowledgecenter/en/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/rwlp_loose_applications.html).

For more information about the Liberty Maven plug-in, see its [GitHub repository](https://github.com/WASdev/ci.maven).

When the Liberty instance begins starting up, various messages display in your command-line session. Wait for the following message, which indicates that Liberty's startup is complete:

```
[INFO] [AUDIT] CWWKF0011I: The server defaultServer is ready to run a smarter planet.
```



Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.


To access the ***system*** microservice, see the ***http\://localhost:9080/system/properties*** URL, and you see a list of the various system properties of your JVM:


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/system/properties | jq
```


```
{
    "os.name": "Mac OS X",
    "java.version": "1.8.0_151",
    ...
}
```

When you need to stop the Liberty instance, press `Ctrl+C` in the command-line session where you ran Liberty, or run the ***liberty:stop*** goal from the ***start*** directory in another command-line session:

```bash
mvn liberty:stop
```


::page{title="Starting and stopping Open Liberty in the background"}

Although you can start and stop Liberty in the foreground by using the Maven ***liberty:run*** goal, you can also start and stop the Liberty instance in the background with the Maven ***liberty:start*** and ***liberty:stop*** goals:

```bash
mvn liberty:start
mvn liberty:stop
```



::page{title="Updating Liberty's configuration without restarting"}

The Open Liberty Maven plug-in includes a ***dev*** goal that listens for any changes in the project, including application source code or configuration. The Open Liberty instance automatically reloads the configuration without restarting. This goal allows for quicker turnarounds and an improved developer experience.

Stop the Open Liberty instance if it is running, and start it in [dev mode](https://openliberty.io/docs/latest/development-mode.html) by running the ***liberty:dev*** goal in the ***start*** directory:

```bash
mvn liberty:dev
```

Dev mode automatically picks up changes that you make to your application and allows you to run tests by pressing the ***enter/return*** key in the active command-line session. When you’re working on your application, rather than rerunning Maven commands, press the ***enter/return*** key to verify your change.


As before, you can see that the application is running by going to the ***http\://localhost:9080/system/properties*** URL.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/system/properties | jq
```




Now try updating Liberty's ***server.xml*** configuration file while the instance is running in dev mode. The ***system*** microservice does not currently include health monitoring to report whether the Liberty instance and the microservice that it runs are healthy. You can add health reports with the MicroProfile Health feature, which adds a ***/health*** endpoint to your application. If you try to access this endpoint now at the ***http\://localhost:9080/health/*** URL, you see a 404 error because the ***/health*** endpoint does not yet exist:


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl http://localhost:9080/health/
```



```
Error 404: java.io.FileNotFoundException: SRVE0190E: File not found: /health
```

To add the MicroProfile Health feature to the Liberty instance, include the ***mpHealth*** feature in the ***server.xml***.

Replace the Liberty ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-getting-started/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-getting-started/start/src/main/liberty/config/server.xml"}



```xml
<server description="Sample Liberty server">
    <featureManager>
        <feature>restfulWS-3.1</feature>
        <feature>jsonp-2.1</feature>
        <feature>jsonb-3.0</feature>
        <feature>cdi-4.0</feature>
        <feature>mpMetrics-5.1</feature>
        <feature>mpHealth-4.0</feature>
        <feature>mpConfig-3.1</feature>
    </featureManager>

    <variable name="http.port" defaultValue="9080"/>
    <variable name="https.port" defaultValue="9443"/>

    <webApplication location="guide-getting-started.war" contextRoot="/" />
    
    <mpMetrics authentication="false"/>


    <httpEndpoint host="*" httpPort="${http.port}" 
        httpsPort="${https.port}" id="defaultHttpEndpoint"/>

    <variable name="io_openliberty_guides_system_inMaintenance" value="false"/>
</server>
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


After you make the file changes, Open Liberty automatically reloads its configuration. When enabled, the ***mpHealth*** feature automatically adds a ***/health*** endpoint to the application. You can see the instance being updated in the Liberty log displayed in your command-line session:

```
[INFO] [AUDIT] CWWKG0016I: Starting server configuration update.
[INFO] [AUDIT] CWWKT0017I: Web application removed (default_host): http://foo:9080/
[INFO] [AUDIT] CWWKZ0009I: The application io.openliberty.guides.getting-started has stopped successfully.
[INFO] [AUDIT] CWWKG0017I: The server configuration was successfully updated in 0.284 seconds.
[INFO] [AUDIT] CWWKT0016I: Web application available (default_host): http://foo:9080/health/
[INFO] [AUDIT] CWWKF0012I: The server installed the following features: [mpHealth-3.0].
[INFO] [AUDIT] CWWKF0008I: Feature update completed in 0.285 seconds.
[INFO] [AUDIT] CWWKT0016I: Web application available (default_host): http://foo:9080/
[INFO] [AUDIT] CWWKZ0003I: The application io.openliberty.guides.getting-started updated in 0.173 seconds.
```


Try to access the ***/health*** endpoint again by visiting the ***http\://localhost:9080/health*** URL. You see the following JSON:


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/health | jq
```



```
{
    "checks":[],
    "status":"UP"
}
```

Now you can verify whether your Liberty instance is up and running.



::page{title="Updating the source code without restarting Liberty"}

The RESTful application that contains your ***system*** microservice runs in a Liberty instance from its ***.class*** file and other artifacts. Open Liberty automatically monitors these artifacts, and whenever they are updated, it updates the running instance without the need for the instance to be restarted.

Look at your ***pom.xml*** file.


Try updating the source code while Liberty is running in dev mode. At the moment, the ***/health*** endpoint reports whether the Liberty instance is running, but the endpoint doesn't provide any details on the microservices that are running inside of the instance.

MicroProfile Health offers health checks for both readiness and liveness. A readiness check allows third-party services, such as Kubernetes, to know if the microservice is ready to process requests. A liveness check allows third-party services to determine if the microservice is running.

Create the ***SystemReadinessCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-getting-started/start/src/main/java/io/openliberty/sample/system/SystemReadinessCheck.java
```


> Then, to open the SystemReadinessCheck.java file in your IDE, select
> **File** > **Open** > guide-getting-started/start/src/main/java/io/openliberty/sample/system/SystemReadinessCheck.java, or click the following button

::openFile{path="/home/project/guide-getting-started/start/src/main/java/io/openliberty/sample/system/SystemReadinessCheck.java"}



```java
package io.openliberty.sample.system;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Readiness
@ApplicationScoped
public class SystemReadinessCheck implements HealthCheck {

    private static final String READINESS_CHECK = SystemResource.class.getSimpleName()
                                                 + " Readiness Check";

    @Inject
    @ConfigProperty(name = "io_openliberty_guides_system_inMaintenance")
    Provider<String> inMaintenance;

    @Override
    public HealthCheckResponse call() {
        if (inMaintenance != null && inMaintenance.get().equalsIgnoreCase("true")) {
            return HealthCheckResponse.down(READINESS_CHECK);
        }
        return HealthCheckResponse.up(READINESS_CHECK);
    }

}
```



The ***SystemReadinessCheck*** class verifies that the 
***system*** microservice is not in maintenance by checking a config property.

Create the ***SystemLivenessCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-getting-started/start/src/main/java/io/openliberty/sample/system/SystemLivenessCheck.java
```


> Then, to open the SystemLivenessCheck.java file in your IDE, select
> **File** > **Open** > guide-getting-started/start/src/main/java/io/openliberty/sample/system/SystemLivenessCheck.java, or click the following button

::openFile{path="/home/project/guide-getting-started/start/src/main/java/io/openliberty/sample/system/SystemLivenessCheck.java"}



```java
package io.openliberty.sample.system;

import jakarta.enterprise.context.ApplicationScoped;

import java.lang.management.MemoryMXBean;
import java.lang.management.ManagementFactory;

import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Liveness
@ApplicationScoped
public class SystemLivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long memUsed = memBean.getHeapMemoryUsage().getUsed();
        long memMax = memBean.getHeapMemoryUsage().getMax();

        return HealthCheckResponse.named(
            SystemResource.class.getSimpleName() + " Liveness Check")
                                  .status(memUsed < memMax * 0.9).build();
    }

}
```



The ***SystemLivenessCheck*** class reports a status of 
***DOWN*** if the microservice uses over 90% of the maximum amount of memory.

After you make the file changes, Open Liberty automatically reloads its configuration and the ***system*** application.

The following messages display in your first command-line session:

```
[INFO] [AUDIT] CWWKT0017I: Web application removed (default_host): http://foo:9080/
[INFO] [AUDIT] CWWKZ0009I: The application io.openliberty.guides.getting-started has stopped successfully.
[INFO] [AUDIT] CWWKT0016I: Web application available (default_host): http://foo:9080/
[INFO] [AUDIT] CWWKZ0003I: The application io.openliberty.guides.getting-started updated in 0.136 seconds.
```


Access the ***/health*** endpoint again by going to the ***http\://localhost:9080/health*** URL. This time you see the overall status of your Liberty instance and the aggregated data of the liveness and readiness checks for the ***system*** microservice:


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/health | jq
```



```
{  
   "checks":[  
      {  
         "data":{},
         "name":"SystemResource Readiness Check",
         "status":"UP"
      },
      {  
         "data":{},
         "name":"SystemResource Liveness Check",
         "status":"UP"
      }
   ],
   "status":"UP"
}
```



You can also access the ***/health/ready*** endpoint by going to the ***http\://localhost:9080/health/ready*** URL to view the data from the readiness health check. Similarly, access the ***/health/live*** endpoint by going to the ***http\://localhost:9080/health/live*** URL to view the data from the liveness health check.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/health/ready | jq
```




_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/health/live | jq
```



Making code changes and recompiling is fast and straightforward. Open Liberty dev mode automatically picks up changes in the ***.class*** files and artifacts, without needing to be restarted. Alternatively, you can run the ***run*** goal and manually repackage or recompile the application by using the ***mvn package*** command or the ***mvn compile*** command while Liberty is running. Dev mode was added to further improve the developer experience by minimizing turnaround times.



::page{title="Checking the Open Liberty logs"}

While Liberty is running in the foreground, it displays various console messages in the command-line session. These messages are also logged to the ***target/liberty/wlp/usr/servers/defaultServer/logs/console.log*** file. You can find the complete Liberty logs in the ***target/liberty/wlp/usr/servers/defaultServer/logs*** directory. The ***console.log*** and ***messages.log*** files are the primary log files that contain console output of the running application and the Liberty instance. More logs are created when runtime errors occur or whenever tracing is enabled. You can find the error logs in the ***ffdc*** directory and the tracing logs in the ***trace.log*** file.

In addition to the log files that are generated automatically, you can enable logging of specific Java packages or classes by using the ***logging*** element:

```
<logging traceSpecification="<component_1>=<level>:<component_2>=<level>:..."/>
```

The ***component*** element is a Java package or class, and the ***level*** element is one of the following logging levels: ***off***, ***fatal***, ***severe***, ***warning***, ***audit***, ***info***, ***config***, ***detail***, ***fine***, ***finer***, ***finest***, ***all***.

For more information about logging, see the [Trace log detail levels](https://www.openliberty.io/docs/latest/log-trace-configuration.html#log_details),  [logging element](https://www.openliberty.io/docs/latest/reference/config/logging.html), and [Log and trace configuration](https://www.openliberty.io/docs/latest/log-trace-configuration.html) documentation.

Try enabling detailed logging of the MicroProfile Health feature by adding the ***logging*** element to your configuration file.

Replace the Liberty ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-getting-started/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-getting-started/start/src/main/liberty/config/server.xml"}



```xml
<server description="Sample Liberty server">
    <featureManager>
        <feature>restfulWS-3.1</feature>
        <feature>jsonp-2.1</feature>
        <feature>jsonb-3.0</feature>
        <feature>cdi-4.0</feature>
        <feature>mpMetrics-5.1</feature>
        <feature>mpHealth-4.0</feature>
        <feature>mpConfig-3.1</feature>
    </featureManager>

    <variable name="http.port" defaultValue="9080"/>
    <variable name="https.port" defaultValue="9443"/>

    <webApplication location="guide-getting-started.war" contextRoot="/" />
    
    <mpMetrics authentication="false"/>

    <logging traceSpecification="com.ibm.ws.microprofile.health.*=all" />

    <httpEndpoint host="*" httpPort="${http.port}" 
        httpsPort="${https.port}" id="defaultHttpEndpoint"/>

    <variable name="io_openliberty_guides_system_inMaintenance" value="false"/>
</server>
```



After you change the file, Open Liberty automatically reloads its configuration.

Now, when you visit the ***/health*** endpoint, additional traces are logged in the ***trace.log*** file.

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran Liberty.


::page{title="Running the application in a Docker container"}


To containerize the application, you need a ***Dockerfile***. This file contains a collection of instructions that define how a Docker image is built, what files are packaged into it, what commands run when the image runs as a container, and other information. You can find a complete ***Dockerfile*** in the ***start*** directory. This ***Dockerfile*** copies the ***.war*** file into a Docker image that contains the Java runtime and a preconfigured Open Liberty runtime.

Run the ***mvn package*** command from the ***start*** directory so that the ***.war*** file resides in the ***target*** directory.

```bash
mvn package
```



To build and containerize the application, run the following Docker build command in the ***start*** directory:

```bash
docker build -t openliberty-getting-started:1.0-SNAPSHOT .
```

The Docker ***openliberty-getting-started:1.0-SNAPSHOT*** image is also built from the ***Dockerfile***. To verify that the image is built, run the ***docker images*** command to list all local Docker images:

```bash
docker images
```

Your image should appear in the list of all Docker images:

```
REPOSITORY                     TAG             IMAGE ID        CREATED         SIZE
openliberty-getting-started    1.0-SNAPSHOT    85085141269b    21 hours ago    487MB
```

Next, run the image as a container:
```bash
docker run -d --name gettingstarted-app -p 9080:9080 openliberty-getting-started:1.0-SNAPSHOT
```

There is a bit going on here, so here's a breakdown of the command:

| *Flag* | *Description*
| ---| ---
| -d     | Runs the container in the background.
| --name | Specifies a name for the container.
| -p     | Maps the container ports to the host ports.

The final argument in the ***docker run*** command is the Docker image name.

Next, run the ***docker ps*** command to verify that your container started:
```bash
docker ps
```

Make sure that your container is running and does not have ***Exited*** as its status:

```
CONTAINER ID    IMAGE                         CREATED          STATUS           NAMES
4294a6bdf41b    openliberty-getting-started   9 seconds ago    Up 11 seconds    gettingstarted-app
```


To access the application, go to the ***http\://localhost:9080/system/properties*** URL.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/system/properties | jq
```



To stop and remove the container, run the following commands:
```bash
docker stop gettingstarted-app && docker rm gettingstarted-app
```

To remove the image, run the following command:
```bash
docker rmi openliberty-getting-started:1.0-SNAPSHOT
```


::page{title="Developing the application in a Docker container"}

The Open Liberty Maven plug-in includes a ***devc*** goal that simplifies developing your application in a Docker container by starting dev mode with container support. This goal builds a Docker image, mounts the required directories, binds the required ports, and then runs the application inside of a container. Dev mode also listens for any changes in the application source code or configuration and rebuilds the image and restarts the container as necessary.

Build and run the container by running the devc goal from the ***start*** directory:


```bash
chmod 777 /home/project/guide-getting-started/start/target/liberty/wlp/usr/servers/defaultServer/logs
mvn liberty:devc -DserverStartTimeout=300
```

When you see the following message, Open Liberty is ready to run in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

Open another command-line session and run the ***docker ps*** command to verify that your container started:
```bash
docker ps
```

Your container should be running and have ***Up*** as its status:

```
CONTAINER ID        IMAGE                                 COMMAND                  CREATED             STATUS                         PORTS                                                                    NAMES
17af26af0539        guide-getting-started-dev-mode        "/opt/ol/helpers/run…"   3 minutes ago       Up 3 minutes                   0.0.0.0:7777->7777/tcp, 0.0.0.0:9080->9080/tcp, 0.0.0.0:9443->9443/tcp   liberty-dev
```


To access the application, go to the ***http\://localhost:9080/system/properties*** URL. 


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/system/properties | jq
```



Dev mode automatically picks up changes that you make to your application and allows you to run tests by pressing the ***enter/return*** key in the active command-line session.

Update the ***server.xml*** file to change the context root from ***/*** to ***/dev***.

Replace the Liberty ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-getting-started/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-getting-started/start/src/main/liberty/config/server.xml"}



```xml
<server description="Sample Liberty server">
    <featureManager>
        <feature>restfulWS-3.1</feature>
        <feature>jsonp-2.1</feature>
        <feature>jsonb-3.0</feature>
        <feature>cdi-4.0</feature>
        <feature>mpMetrics-5.1</feature>
        <feature>mpHealth-4.0</feature>
        <feature>mpConfig-3.1</feature>
    </featureManager>

    <variable name="http.port" defaultValue="9080"/>
    <variable name="https.port" defaultValue="9443"/>

    <webApplication location="guide-getting-started.war" contextRoot="/dev" />
    <mpMetrics authentication="false"/>

    <logging traceSpecification="com.ibm.ws.microprofile.health.*=all" />

    <httpEndpoint host="*" httpPort="${http.port}" 
        httpsPort="${https.port}" id="defaultHttpEndpoint"/>

    <variable name="io_openliberty_guides_system_inMaintenance" value="false"/>
</server>
```



After you make the file changes, Open Liberty automatically reloads its configuration. When you see the following message in your command-line session, Open Liberty is ready to run again:

```
The server has been restarted.
************************************************************************
*    Liberty is running in dev mode.
```

Update the ***mpData.js*** file to change the ***url*** in the ***getSystemPropertiesRequest*** method to reflect the new context root.


Update the mpData.js file.

> From the menu of the IDE, select 
> **File** > **Open** > guide-getting-started/start/src/main/webapp/js/mpData.js, or click the following button

::openFile{path="/home/project/guide-getting-started/start/src/main/webapp/js/mpData.js"}

```
function getSystemPropertiesRequest() {
    var propToDisplay = ["java.vendor", "java.version", "user.name", "os.name", "wlp.install.dir", "wlp.server.name" ];
    var url = "http://localhost:9080/dev/system/properties";
    var req = new XMLHttpRequest();
    var table = document.getElementById("systemPropertiesTable");
    ...
```

Update the ***pom.xml*** file to change the context root from ***/*** to ***/dev*** in the ***maven-failsafe-plugin*** to reflect the new context root when you run functional tests.

Replace the pom.xml file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-getting-started/start/pom.xml, or click the following button

::openFile{path="/home/project/guide-getting-started/start/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-getting-started</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- Liberty configuration -->
        <liberty.var.http.port>9080</liberty.var.http.port>
        <liberty.var.https.port>9443</liberty.var.https.port>
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
            <version>6.2.9.Final</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-json-binding-provider</artifactId>
            <version>6.2.9.Final</version>
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
                <version>3.10.3</version>
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
                        <context.root>/dev</context.root>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```



You can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode to verify your change.


You can access the application at the ***http\://localhost:9080/dev/system/properties*** URL. Notice that the context root is now ***/dev***.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/dev/system/properties | jq
```



When you are finished, exit dev mode by pressing `Ctrl+C` in the command-line session that the container was started from. Exiting dev mode stops and removes the container. To check that the container was stopped, run the ***docker ps*** command.


::page{title="Running the application from a minimal runnable JAR"}

So far, Open Liberty was running out of the ***target/liberty/wlp*** directory, which effectively contains an Open Liberty installation and the deployed application. The final product of the Maven build is a server package for use in a continuous integration pipeline and, ultimately, a production deployment.

Open Liberty supports a number of different server packages. The sample application currently generates a ***usr*** package that contains the Liberty runtime and application to be extracted onto an Open Liberty installation.

Instead of creating a server package, you can generate a runnable JAR file that contains the application along with a Liberty runtime. This JAR file can then be run anywhere and deploy your application and runtime at the same time. To generate a runnable JAR file, override the  ***include*** property: 
```bash
mvn liberty:package -Dinclude=runnable
```

The packaging type is overridden from the ***usr*** package to the ***runnable*** package. This property then propagates to the ***liberty-maven-plugin*** plug-in, which generates the server package based on the ***openliberty-kernel*** package.

When the build completes, you can find the minimal runnable ***guide-getting-started.jar*** file in the ***target*** directory. This JAR file contains only the ***features*** that you explicitly enabled in your ***server.xml*** file. As a result, the generated JAR file is only about 50 MB.

To run the JAR file, first stop the Liberty instance if it's running. Then, navigate to the ***target*** directory and run the ***java -jar*** command:

```bash
java -jar guide-getting-started.jar
```


When Liberty starts, go to the ***http\://localhost:9080/dev/system/properties*** URL to access your application that is now running out of the minimal runnable JAR file.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/dev/system/properties | jq
```



You can stop the Liberty instance by pressing `Ctrl+C` in the command-line session that the instance runs in.





::page{title="Summary"}

### Nice Work!

You've learned the basics of deploying and updating an application on Open Liberty.




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-getting-started*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-getting-started
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Getting%20started%20with%20Open%20Liberty&guide-id=cloud-hosted-guide-getting-started)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-getting-started/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-getting-started/pulls)



### Where to next?

* [Building a web application with Maven](https://openliberty.io/guides/maven-intro.html)
* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Using Docker containers to develop microservices](https://openliberty.io/guides/docker.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
