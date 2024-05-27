---
markdown-version: v1
tool-type: theiadocker
---

# **Welcome to the Getting started with Open Liberty guide!**

Learn how to develop a Java application on Open Liberty with Maven and Docker.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



# **What you'll learn**

You will learn how to run and update a simple REST microservice on Open Liberty.
You will use Maven throughout the guide to build and deploy the microservice as well as
to interact with the running Liberty instance.

Open Liberty is an open application framework designed for the cloud. It's small, lightweight,
and designed with modern cloud-native application development in mind. It supports the
full MicroProfile and Jakarta EE APIs and is composable, meaning that you can use only the
features that you need, keeping everything lightweight, which is great for microservices.
It also deploys to every major cloud platform, including Docker, Kubernetes, and Cloud
Foundry.

Maven is an automation build tool that provides an efficient way to develop Java applications.
Using Maven, you will build a simple microservice, called **system**, that collects basic
system properties from your laptop and displays them on an endpoint that you can access
in your web browser. 

You'll also explore how to package your application with Open Liberty
so that it can be deployed anywhere in one go. You will then make Liberty configuration and code changes and see how
they are immediately picked up by a running instance.

Finally, you will package the application along with the server configuration into a Docker
image and run that image as a container.


# **Getting started**

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-getting-started.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-getting-started.git
cd guide-getting-started
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.




# **Building and running the application**

Your application is configured to be built with Maven. Every Maven-configured project
contains a **pom.xml** file, which defines the project configuration, dependencies, plug-ins,
and so on.

Your **pom.xml** file is located in the **start** directory and is configured to
include the **liberty-maven-plugin**, which allows you
to install applications into Open Liberty and manage the server instances.


To begin, navigate to the **start** directory. Build the **system** microservice
that is provided and deploy it to Open Liberty by running the Maven
**liberty:run** goal:

```
cd start
mvn liberty:run
```
{: codeblock}


The **mvn** command initiates a Maven build, during which the **target** directory is created
to store all build-related files.

The **liberty:run** argument specifies the Open Liberty **run** goal, which
starts an Open Liberty server instance in the foreground.
As part of this phase, an Open Liberty server runtime is downloaded and installed into
the **target/liberty/wlp** directory, a server instance is created and configured in the
**target/liberty/wlp/usr/servers/defaultServer** directory, and the application is
installed into that server via [loose config](https://www.ibm.com/support/knowledgecenter/en/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/rwlp_loose_applications.html).

For more information about the Liberty Maven plug-in, see its [GitHub repository](https://github.com/WASdev/ci.maven).

When the server begins starting up, various messages display in your command-line session. Wait
for the following message, which indicates that the server startup is complete:

```
[INFO] [AUDIT] CWWKF0011I: The server defaultServer is ready to run a smarter planet.
```



Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.


To access the **system** microservice, see the http://localhost:9080/system/properties URL,


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9080/system/properties | jq
```
{: codeblock}


and you see a list of the various system properties of your JVM:

```
{
    "os.name": "Mac OS X",
    "java.version": "1.8.0_151",
    ...
}
```

When you need to stop the server, press **CTRL+C** in the command-line session where
you ran the server, or run the **liberty:stop** goal from the **start** directory in
another command-line session:

```
mvn liberty:stop
```
{: codeblock}




# **Starting and stopping the Open Liberty server in the background**

Although you can start and stop the server in the foreground by using the Maven
**liberty:run** goal, you can also start and stop the server in the background with
the Maven **liberty:start** and **liberty:stop** goals:

```
mvn liberty:start
mvn liberty:stop
```
{: codeblock}





# **Updating the server configuration without restarting the server**

The Open Liberty Maven plug-in includes a **dev** goal that listens for any changes in the project, 
including application source code or configuration. The Open Liberty server automatically reloads the configuration without restarting. This goal allows for quicker turnarounds and an improved developer experience.

Stop the Open Liberty server if it is running, and start it in dev mode by running the **liberty:dev** goal in the **start** directory:

```
mvn liberty:dev
```
{: codeblock}


Dev mode automatically picks up changes that you make to your application and allows you to run tests by pressing the **enter/return** key in the active command-line session. When you’re working on your application, rather than rerunning Maven commands, press the **enter/return** key to verify your change.


As before, you can see that the application is running by going to the http://localhost:9080/system/properties URL.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9080/system/properties | jq
```
{: codeblock}



Now try updating the server configuration while the server is running in dev mode.
The **system** microservice does not currently include health monitoring to report whether the server and the microservice that it runs are healthy.
You can add health reports with the MicroProfile Health feature, which adds a **/health** endpoint to your application.

If you try to access this endpoint now at the http://localhost:9080/health/ URL, you see a 404 error because the **/health** endpoint does not yet exist:


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl http://localhost:9080/health/
```
{: codeblock}



```
Error 404: java.io.FileNotFoundException: SRVE0190E: File not found: /health
```

To add the MicroProfile Health feature to the server, include the **mpHealth** feature in the **server.xml**.

Replace the server configuration file.

> From the menu of the IDE, select 
> **File** > **Open** > guide-getting-started/start/src/main/liberty/config/server.xml




```
<server description="Sample Liberty server">
    <featureManager>
        <feature>jaxrs-2.1</feature>
        <feature>jsonp-2.1</feature>
        <feature>cdi-4.0</feature>
        <feature>mpMetrics-4.0</feature>
        <feature>mpHealth-4.0</feature>
        <feature>mpConfig-3.0</feature>
    </featureManager>

    <variable name="default.http.port" defaultValue="9080"/>
    <variable name="default.https.port" defaultValue="9443"/>

    <webApplication location="guide-getting-started.war" contextRoot="/" />
    
    <mpMetrics authentication="false"/>


    <httpEndpoint host="*" httpPort="${default.http.port}" 
        httpsPort="${default.https.port}" id="defaultHttpEndpoint"/>

    <variable name="io_openliberty_guides_system_inMaintenance" value="false"/>
</server>
```
{: codeblock}



After you make the file changes, Open Liberty automatically reloads its configuration.
When enabled, the **mpHealth** feature automatically adds a **/health** endpoint to the application.
You can see the server being updated in the server log displayed in your command-line session:

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


Try to access the **/health** endpoint again by visiting the http://localhost:9080/health URL.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9080/health | jq
```
{: codeblock}


You see the following JSON:

```
{
    "checks":[],
    "status":"UP"
}
```

Now you can verify whether your server is up and running.



# **Updating the source code without restarting the server**

The JAX-RS application that contains your **system** microservice runs in a server from its **.class** file and other artifacts.
Open Liberty automatically monitors these artifacts, and whenever they are updated, it updates the running server without the need for the server to be restarted.

Look at your **pom.xml** file.


Try updating the source code while the server is running in dev mode.
At the moment, the **/health** endpoint reports whether the server is running, but the endpoint doesn't provide any details on the microservices that are running inside of the server.

MicroProfile Health offers health checks for both readiness and liveness.
A readiness check allows third-party services, such as Kubernetes, to know if the microservice is ready to process requests.
A liveness check allows third-party services to determine if the microservice is running.

Create the **SystemReadinessCheck** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-getting-started/start/src/main/java/io/openliberty/sample/system/SystemReadinessCheck.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-getting-started/start/src/main/java/io/openliberty/sample/system/SystemReadinessCheck.java




```
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
{: codeblock}



The **SystemReadinessCheck** class verifies that the 
**system** microservice is not in maintenance by checking a config property.

Create the **SystemLivenessCheck** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-getting-started/start/src/main/java/io/openliberty/sample/system/SystemLivenessCheck.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-getting-started/start/src/main/java/io/openliberty/sample/system/SystemLivenessCheck.java




```
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
                                  .withData("memory used", memUsed)
                                  .withData("memory max", memMax)
                                  .status(memUsed < memMax * 0.9).build();
    }

}
```
{: codeblock}



The **SystemLivenessCheck** class reports a status of 
**DOWN** if the microservice uses over 90% of the maximum amount of memory.

After you make the file changes, Open Liberty automatically reloads its configuration and the **system** application.

The following messages display in your first command-line session:

```
[INFO] [AUDIT] CWWKT0017I: Web application removed (default_host): http://foo:9080/
[INFO] [AUDIT] CWWKZ0009I: The application io.openliberty.guides.getting-started has stopped successfully.
[INFO] [AUDIT] CWWKT0016I: Web application available (default_host): http://foo:9080/
[INFO] [AUDIT] CWWKZ0003I: The application io.openliberty.guides.getting-started updated in 0.136 seconds.
```


Access the **/health** endpoint again by going to the http://localhost:9080/health URL.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9080/health | jq
```
{: codeblock}


This time you see the overall status of your server and the aggregated data of the liveness and readiness checks for the **system** microservice:

```
{  
   "checks":[  
      {  
         "data":{},
         "name":"SystemResource Readiness Check",
         "status":"UP"
      },
      {  
         "data":{
            "memory used":40434888,
            "memory max":4294967296
         },
         "name":"SystemResource Liveness Check",
         "status":"UP"
      }
   ],
   "status":"UP"
}
```


You can also access the **/health/ready** endpoint by going to the http://localhost:9080/health/ready URL to view the data from the readiness health check.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9080/health/ready | jq
```
{: codeblock}



Similarly, access the **/health/live** endpoint by going to the http://localhost:9080/health/live URL to view the data from the liveness health check.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9080/health/live | jq
```
{: codeblock}



Making code changes and recompiling is fast and straightforward.
Open Liberty dev mode automatically picks up changes in the **.class** files and artifacts, without needing to be restarted.
Alternatively, you can run the **run** goal and manually repackage or recompile the application by using the **mvn package** command or the **mvn compile** command while the server is running. Dev mode was added to further improve the developer experience by minimizing turnaround times.



# **Checking the Open Liberty server logs**

While the server is running in the foreground, it displays various console messages in
the command-line session. These messages are also logged to the **target/liberty/wlp/usr/servers/defaultServer/logs/console.log**
file. You can find the complete server logs in the **target/liberty/wlp/usr/servers/defaultServer/logs**
directory. The **console.log** and **messages.log** files are the primary log files that contain
console output of the running application and the server. More logs are created when runtime errors 
occur or whenever tracing is enabled. You can find the error logs in the
**ffdc** directory and the tracing logs in the **trace.log** file.

In addition to the log files that are generated automatically, you can enable logging of
specific Java packages or classes by using the **logging** element:

```
<logging traceSpecification="<component_1>=<level>:<component_2>=<level>:..."/>
```

The **component** element is a Java package or class, and the **level** element is one
of the following logging levels: **off**, **fatal**, **severe**, **warning**, **audit**, **info**,
**config**, **detail**, **fine**, **finer**, **finest**, **all**.

Try enabling detailed logging of the MicroProfile Health feature by adding the
**logging** element to your configuration file.

Replace the server configuration file.

> From the menu of the IDE, select 
> **File** > **Open** > guide-getting-started/start/src/main/liberty/config/server.xml




```
<server description="Sample Liberty server">
    <featureManager>
        <feature>jaxrs-2.1</feature>
        <feature>jsonp-2.1</feature>
        <feature>cdi-4.0</feature>
        <feature>mpMetrics-4.0</feature>
        <feature>mpHealth-4.0</feature>
        <feature>mpConfig-3.0</feature>
    </featureManager>

    <variable name="default.http.port" defaultValue="9080"/>
    <variable name="default.https.port" defaultValue="9443"/>

    <webApplication location="guide-getting-started.war" contextRoot="/" />
    
    <mpMetrics authentication="false"/>

    <logging traceSpecification="com.ibm.ws.microprofile.health.*=all" />

    <httpEndpoint host="*" httpPort="${default.http.port}" 
        httpsPort="${default.https.port}" id="defaultHttpEndpoint"/>

    <variable name="io_openliberty_guides_system_inMaintenance" value="false"/>
</server>
```
{: codeblock}



After you change the file, Open Liberty automatically reloads its configuration.

Now, when you visit the **/health** endpoint, additional traces are logged in the **trace.log** file.

When you are done checking out the service, exit dev mode by pressing **CTRL+C** in the command-line session
where you ran the server, or by typing **q** and then pressing the **enter/return** key.


# **Running the application in a Docker container**

To run the application in a container, Docker needs to be installed. For installation
instructions, see the [Official Docker Docs](https://docs.docker.com/install/).

Make sure to start your Docker daemon before you proceed.

To containerize the application, you need a **Dockerfile**. This file contains a collection
of instructions that define how a Docker image is built, what files are packaged into it,
what commands run when the image runs as a container, and other information. You can find a complete
**Dockerfile** in the **start** directory. This **Dockerfile** copies the **.war** file into a Docker
image that contains the Java runtime and a preconfigured Open Liberty server.

Run the **mvn package** command from the **start** directory so that the **.war** file resides in the **target** directory.

```
mvn package
```
{: codeblock}


Run the following command to download or update to the latest Open Liberty Docker image:

```
docker pull icr.io/appcafe/open-liberty:full-java11-openj9-ubi
```
{: codeblock}


To build and containerize the application, run the
following Docker build command in the **start** directory:

```
docker build -t openliberty-getting-started:1.0-SNAPSHOT .
```
{: codeblock}


The Docker **openliberty-getting-started:1.0-SNAPSHOT** image is also built from the **Dockerfile**.
To verify that the image is built, run the **docker images** command to list all local Docker images:

```
docker images
```
{: codeblock}


Your image should appear in the list of all Docker images:

```
REPOSITORY                     TAG             IMAGE ID        CREATED         SIZE
openliberty-getting-started    1.0-SNAPSHOT    85085141269b    21 hours ago    487MB
```

Next, run the image as a container:
```
docker run -d --name gettingstarted-app -p 9080:9080 openliberty-getting-started:1.0-SNAPSHOT
```
{: codeblock}


There is a bit going on here, so here's a breakdown of the command:

| *Flag* | *Description*
| ---| ---
| -d     | Runs the container in the background.
| --name | Specifies a name for the container.
| -p     | Maps the container ports to the host ports.

The final argument in the **docker run** command is the Docker image name.

Next, run the **docker ps** command to verify that your container started:
```
docker ps
```
{: codeblock}


Make sure that your container is running and does not have **Exited** as its status:

```
CONTAINER ID    IMAGE                         CREATED          STATUS           NAMES
4294a6bdf41b    openliberty-getting-started   9 seconds ago    Up 11 seconds    gettingstarted-app
```


To access the application, go to the http://localhost:9080/system/properties URL.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9080/system/properties | jq
```
{: codeblock}



To stop and remove the container, run the following commands:
```
docker stop gettingstarted-app && docker rm gettingstarted-app
```
{: codeblock}


To remove the image, run the following command:
```
docker rmi openliberty-getting-started:1.0-SNAPSHOT
```
{: codeblock}



# **Developing the application in a Docker container**

The Open Liberty Maven plug-in includes a **devc** goal that simplifies developing
your application in a Docker container by starting dev mode with container
support. This goal builds a Docker image, mounts the required directories, binds
the required ports, and then runs the application inside of a container. Dev
mode also listens for any changes in the application source code or
configuration and rebuilds the image and restarts the container as necessary.

Build and run the container by running the devc goal from the **start** directory:


```
mvn liberty:devc -DserverStartTimeout=300
```
{: codeblock}

When you see the following message, Open Liberty is ready to run in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

Open another command-line session and run the **docker ps** command to verify that your container started:
```
docker ps
```
{: codeblock}


Your container should be running and have **Up** as its status:

```
CONTAINER ID        IMAGE                                 COMMAND                  CREATED             STATUS                         PORTS                                                                    NAMES
17af26af0539        guide-getting-started-dev-mode        "/opt/ol/helpers/run…"   3 minutes ago       Up 3 minutes                   0.0.0.0:7777->7777/tcp, 0.0.0.0:9080->9080/tcp, 0.0.0.0:9443->9443/tcp   liberty-dev
```


To access the application, go to the http://localhost:9080/system/properties URL. 


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9080/system/properties | jq
```
{: codeblock}



Dev mode automatically picks up changes that you make to your
application and allows you to run tests by pressing the **enter/return** key in the
active command-line session.

Update the **server.xml** file to change the context root from **/** to **/dev**.

Replace the server configuration file.

> From the menu of the IDE, select 
> **File** > **Open** > guide-getting-started/start/src/main/liberty/config/server.xml




```
<server description="Sample Liberty server">
    <featureManager>
        <feature>jaxrs-2.1</feature>
        <feature>jsonp-2.1</feature>
        <feature>cdi-4.0</feature>
        <feature>mpMetrics-4.0</feature>
        <feature>mpHealth-4.0</feature>
        <feature>mpConfig-3.0</feature>
    </featureManager>

    <variable name="default.http.port" defaultValue="9080"/>
    <variable name="default.https.port" defaultValue="9443"/>

    <webApplication location="guide-getting-started.war" contextRoot="/dev" />
    <mpMetrics authentication="false"/>

    <logging traceSpecification="com.ibm.ws.microprofile.health.*=all" />

    <httpEndpoint host="*" httpPort="${default.http.port}" 
        httpsPort="${default.https.port}" id="defaultHttpEndpoint"/>

    <variable name="io_openliberty_guides_system_inMaintenance" value="false"/>
</server>
```
{: codeblock}



Update the **mpData.js** file to change the **url** in the **getSystemPropertiesRequest** method to reflect the new context root.


Update the mpData.js file.

> From the menu of the IDE, select 
> **File** > **Open** > guide-getting-started/start/src/main/webapp/js/mpData.js

```
function getSystemPropertiesRequest() {
    var propToDisplay = ["java.vendor", "java.version", "user.name", "os.name", "wlp.install.dir", "wlp.server.name" ];
    var url = "http://localhost:9080/dev/system/properties";
    var req = new XMLHttpRequest();
    var table = document.getElementById("systemPropertiesTable");
    ...
```

After you make the file changes, Open Liberty automatically reloads its
configuration. You can access the application at the

http://localhost:9080/dev/system/properties


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9080/dev/system/properties | jq
```
{: codeblock}


URL. Notice that context root is now **/dev**.

When you are finished, exit dev mode by pressing **CTRL+C** in the
command-line session that the container was started from, or by typing `q` and
then pressing the **enter/return** key. Either of these options stops and 
removes the container. To check that the container was stopped, run the **docker ps** command.


# **Running the application from a minimal runnable JAR**

So far, Open Liberty was running out of the **target/liberty/wlp** directory, which
effectively contains an Open Liberty server installation and the deployed application. The
final product of the Maven build is a server package for use in a continuous integration
pipeline and, ultimately, a production deployment.

Open Liberty supports a number of different server packages. The sample application
currently generates a **usr** package that contains the servers and application to be
extracted onto an Open Liberty installation.

Instead of creating a server package, you can generate a runnable JAR file that contains
the application along with a server runtime. This JAR file can then be run anywhere and deploy
your application and server at the same time. To generate a runnable JAR file, override the 
**include** property: 
```
mvn liberty:package -Dinclude=runnable
```
{: codeblock}


The packaging type is overridden from the **usr** package to the **runnable**
package. This property then propagates to the **liberty-maven-plugin**
plug-in, which generates the server package based on the **openliberty-kernel** package.

When the build completes, you can find the minimal runnable **guide-getting-started.jar** file in the
**target** directory. This JAR file contains only the **features** that you
explicitly enabled in your **server.xml** file. As a result, the
generated JAR file is only about 50 MB.

To run the JAR file, first stop the server if it's running. Then, navigate to the **target**
directory and run the **java -jar** command:

```
java -jar guide-getting-started.jar
```
{: codeblock}



When the server starts, go to the http://localhost:9080/dev/system/properties URL to access


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl -s http://localhost:9080/dev/system/properties | jq
```
{: codeblock}


your application that is now running out of the minimal runnable JAR file.

You can stop the server by pressing **CTRL+C** in the command-line session that the server runs in.





# **Summary**

## **Nice Work!**

You've learned the basics of deploying and updating an application on an Open Liberty server.




<br/>
## **Clean up your environment**


Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-getting-started** project by running the following commands:

```
cd /home/project
rm -fr guide-getting-started
```
{: codeblock}

<br/>
## **What did you think of this guide?**

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Getting%20started%20with%20Open%20Liberty&guide-id=cloud-hosted-guide-getting-started)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

<br/>
## **What could make this guide better?**

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-getting-started/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-getting-started/pulls)



<br/>
## **Where to next?**

* [Building a web application with Maven](https://openliberty.io/guides/maven-intro.html)
* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Using Docker containers to develop microservices](https://openliberty.io/guides/docker.html)


<br/>
## **Log out of the session**

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.