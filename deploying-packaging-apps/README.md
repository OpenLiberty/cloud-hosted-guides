## Get started developing Java microservices with Open Liberty

### What you will learn

Learn how to build, deploy and update microservices easily with Open Liberty, an open lightweight cloud-native Java server runtime, using Maven and Docker.

Open Liberty is an application server designed for the cloud. It’s small, lightweight, and designed with modern cloud-native application development in mind. It supports the full MicroProfile and Java EE APIs and is composable, meaning that you can use only the features that you need, keeping the server lightweight, which is great for microservices. It also deploys to every major cloud platform, including Docker, Kubernetes, and Cloud Foundry.

Maven is an automation build tool that provides an efficient way to develop Java applications. Using Maven, you will build a simple microservice, called system, that collects basic system properties from your laptop and displays them on an endpoint that you can access in your web browser.

You’ll also explore how to package your application with the server runtime so that it can be deployed anywhere in one go. You will then make server configuration and code changes and see how they are picked up by a running server.
Finally, you will package the application along with the server configuration into a Docker image and run that image as a container.

## Building and running the application

Ensure you are running the Quick Lab in Google Chrome for the full experience.

If a terminal window does not open navigate:

> Terminal -> New Terminal

Check you are in the **home/project** folder:

`pwd`

The fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:

`git clone https://github.com/openliberty/guide-getting-started.git`

Navigate to the start directory where your pom.xml file is located. Your pom.xml file is configured to include the liberty-maven-plugin, which allows you to install applications into Open Liberty as well as manage the server instances.

`cd guide-getting-started/start`

Install and run the server

`mvn liberty:run`

The mvn command initiates a Maven build, during which the target directory is created to store all build-related files.

The install argument specifies the Maven install phase. During this phase, the application is built and packaged into a .war file, an Open Liberty server runtime is downloaded and installed into the target/liberty/wlp directory, a server instance is created and configured in the target/liberty/wlp/usr/servers/GettingStartedServer directory, and the application is installed into that server via loose config.

The liberty:run-server argument specifies the Open Liberty run-server goal, which starts an Open Liberty server instance in the foreground.

For more information on the Liberty Maven plug-in, see its GitHub repository.

When the server begins starting up, various messages display in your active shell. Wait for the following message, which indicates that the server startup is complete:

`The server GettingStartedServer is ready to run a smarter planet.`

Open up a new terminal window by pressing the split window icon in the top right hand corner of terminal window 

To access the **system** microservice, access the service endpoint to cause some application measurements to be recorded:

`curl http://localhost:9080/system/properties`

The output follows:

````
{"awt.toolkit":"sun.awt.X11.XToolkit","file.encoding.pkg":"sun.io","java.specification.version":"11","jdk.extensions.version":"11.0.6.1","sun.jnu.encoding":"ANSI_X3.4-1968"
````
Some of the **properties** from the ouput include:

```JSON
{
    "os.name":"Linux"
    java.version":"11.0.6",
    ...
}
```

Simply press **CTRL + C** in the shell session where you ran the server to stop the server. 

## Updating the server configuration without restarting the server

The Open Liberty Maven plug-in includes a dev goal that listens for any changes in the project, including application source code or configuration. 

The Open Liberty server automatically reloads the configuration without restarting. This goal allows for quicker turnarounds and an improved developer experience.

## Start Open Liberty Server in dev mode

Navigate back to the terminal window where you started the Open Liberty Server and stop it.

> CTRL + C

To start the server in dev mode run:

**mvn liberty:dev** in the **start** directory

Open up new a new terminal window and attempt to access the health endpoint now:

`curl http://localhost:9080/health`

you will see a 404 error because the /health endpoint does not yet exist:

`Error 404: java.io.FileNotFoundException: SRVE0190E: File not found: /health`

Open up the **server.xml** file and add the MicroProfile Health feature to the server, include the mpHealth feature in the **server.xml**.
 
Open and browse the **server.xml** file in the Development Environment at:

> [File -> Open] guide-getting-started/start/src/main/liberty/config/server.xml

Add the mpHealth feature tag between the `<feature manager>` tags:

`<feature>mpHealth-2.1</feature>`

Save the file **CMD + s** or **CTRL + s** on the server.xml, the OL terminal will update with the new changes.

When enabled, the **mpHealth** feature automatically adds a **/health** endpoint to the application. You can see the server being updated in the server log that’s displayed in your first shell session:

````
[INFO] [AUDIT] CWWKG0016I: Starting server configuration update.
[INFO] [AUDIT] CWWKT0017I: Web application removed (default_host): http://foo:9080/
[INFO] [AUDIT] CWWKZ0009I: The application io.openliberty.guides.getting-started has stopped successfully.
[INFO] [AUDIT] CWWKG0017I: The server configuration was successfully updated in 0.284 seconds.
[INFO] [AUDIT] CWWKT0016I: Web application available (default_host): http://foo:9080/health/
[INFO] [AUDIT] CWWKF0012I: The server installed the following features: [mpHealth-2.0].
[INFO] [AUDIT] CWWKF0008I: Feature update completed in 0.285 seconds.
[INFO] [AUDIT] CWWKT0016I: Web application available (default_host): http://foo:9080/
````
Try to access the /health endpoint again by visiting the health URL:

`curl http://localhost:9080/health`

You see the following JSON output:

```JSON
{
    "checks":[],
    "outcome":"UP"
}
```
You now have a means of verifying if your server is up and running.

## Updating the source code without restarting the server

The JAX-RS application that contains your **system** microservice is configured as a loose application, meaning that it runs in a server from its **.class** file and other artifacts. Open Liberty automatically monitors these artifacts, and whenever they're updated, it updates the running server without the need for the server to be restarted.

Navigate to the **pom.xml** file under start directory

The loose application support is enabled with the <looseApplication/> element in the **liberty-maven-plugin** plug-in.

Try updating the source code while the server is running. At the moment, the `/health` endpoint reports whether or not the server is running, but the endpoint doesn’t provide any details on the microservices that are running inside of the server.

MicroProfile Health offers health checks for both readiness and liveness. A readiness check allows third-party services, such as Kubernetes, to know if the microservice is ready to process requests. A liveness check allows third-party services to determine if the microservice is running.


### Create the SystemReadinessCheck class.

Head to the directory where the SystemReadinessCheck class will be created

`cd guide-getting-started/start/src/main/java/io/openliberty/sample/system`

Create the `SystemReadinessCheck` class:

`touch SystemReadinessCheck.java`

Open the **SystemReadinessCheck.java**

> [File -> Open] guide-getting-started/start/src/main/java/io/openliberty/sample/system/SystemReadinessCheck.java

Insert this code into the **SystemReadinessCheck** class:

```
package io.openliberty.sample.system;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

@Readiness
@ApplicationScoped
public class SystemReadinessCheck implements HealthCheck {

    @Inject
    @ConfigProperty(name = "io_openliberty_guides_system_inMaintenance")
    Provider<String> inMaintenance;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named(
                SystemResource.class.getSimpleName() + " readiness check");
        if (inMaintenance != null && inMaintenance.get().equalsIgnoreCase("true")) {
            return builder.withData("services", "not available").down().build();
        }
        return builder.withData("services", "available").up().build();
    }

}
```

The **SystemReadinessCheck** class verifies that the **system** microservice is not in maintenance by checking a config property.

Go to the directory that the **SystemReadinessCheck.java** will be saved

Ensure you save the java file **CMD + s**

### Create the SystemLivenessCheck class.

Create a new file called **SystemLivenessCheck.java**

`touch SystemLivenessCheck.java`

Open **SystemLivenessCheck.java** and  insert the following code: 

If you are out of the file directory from the **SystemReadinessCheck** open the **SystemLivenessCheck.java** via:

>[File -> Open] `guide-getting-started/start/src/main/java/io/openliberty/sample/system/SystemLivenessCheck.java`

```
package io.openliberty.sample.system;

import javax.enterprise.context.ApplicationScoped;

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
            SystemResource.class.getSimpleName() + " liveness check")
                                  .withData("memory used", memUsed)
                                  .withData("memory max", memMax)
                                  .state(memUsed < memMax * 0.9).build();
    }

}
```

The **SystemLivenessCheck** class reports a status of DOWN if the microservice uses over 90% of the maximum amount of memory.

After you make the file changes, Open Liberty automatically reloads its configuration and the system application.
The following messages display in your first shell session:

````
[INFO] [AUDIT] CWWKT0017I: Web application removed (default_host): http://foo:9080/
[INFO] [AUDIT] CWWKZ0009I: The application io.openliberty.guides.getting-started has stopped successfully.
[INFO] [AUDIT] CWWKT0016I: Web application available (default_host): http://foo:9080/
[INFO] [AUDIT] CWWKZ0003I: The application io.openliberty.guides.getting-started updated in xx.xx seconds.
````

Access the /health endpoint again by entering the **health** endpoint URL. This time you see the overall status of your server as well as the aggregated data of the liveness and readiness checks for the system microservice: 

`curl http://localhost:9080/health`

````
{
   "checks":[
      {
         "data":{
            "services":"available"
         },
         "name":"SystemResource readiness check",
         "status":"UP"
      },
      {
         "data":{
            "memory used":81064480,
            "memory max":4294967296
         },
         "name":"SystemResource liveness check",
         "status":"UP"
      }
   ],
   "status":"UP"
}
````
You can also access the **/health/ready** endpoint by visiting the **ready** endpoint to view the data from the readiness health check. 

`curl http://localhost:9080/health/ready` 

Similarily, access the /health/live endpoint by visiting or running the Liveness health check URLto view the data:

`curl http://localhost:9080/health/live` 

## Checking the Open Liberty server logs

Go back to the second shell session

While the server is running in the foreground, it displays various console messages in the shell. These messages are also logged to:

`target/liberty/wlp/usr/servers/defaultServer/logs/console.log` file. 

You can find the complete server logs in the 
**target/liberty/wlp/usr/servers/defaultServer/logs** directory. 

The **console.log** and **messages.log** files are the primary log files that contain console output of the running application and the server. More logs are created when runtime errors occur or whenever tracing is enabled. You can find the error logs in the **ffdc** directory and the tracing logs in the **trace.log** file.


In addition to the log files that are generated automatically, you can enable logging of specific Java packages or classes by using the **<logging/>** element:

Add the logging feature into the **server.xml**

```
<logging traceSpecification="com.ibm.ws.microprofile.health.*=all" />
```

The **component** element is a Java package or class, and the **level** element is one of the following logging levels: 

**off, fatal, severe, warning, audit, info, config, detail, fine, finer, finest, all.**

Once enabled the **server.xml** should look like this:

`src/main/liberty/config/server.xml`

```
<server description="Sample Liberty server">
    <featureManager>
        <feature>jaxrs-2.1</feature>
        <feature>jsonp-1.1</feature>
        <feature>cdi-2.0</feature>
        <feature>mpMetrics-2.0</feature>
        <feature>mpHealth-2.0</feature>
        <feature>mpConfig-1.3</feature>
    </featureManager>

    <applicationManager autoExpand="true" />
    <quickStartSecurity userName="admin" userPassword="adminpwd" />
    <keyStore id="defaultKeyStore" password="mpKeystore" />
    <logging traceSpecification="com.ibm.ws.microprofile.health.*=all" />
    <httpEndpoint host="*" httpPort="${default.http.port}"
        httpsPort="${default.https.port}" id="defaultHttpEndpoint"/>

    <variable name="io_openliberty_guides_system_inMaintenance" value="false"/>

    <webApplication location="getting-started.war" contextRoot="/"/>
</server>
```

After you change the file, Open Liberty automatically reloads its configuration.

Now, when you visit the /health endpoint, additional traces are logged in the **trace.log** file.

To stop the server in **dev** mode navigate to the terminal and quit the server my typing:

**q + enter**


## Running the application in a Docker container

To run the application in a container, you need to have Docker installed. For installation instructions, see the Official Docker Docs.

To containerize the application, you need a **Dockerfile**. This file contains a collection of instructions that define how a Docker image is built, what files are packaged into it, what commands run when the image runs as a container, and so on. You can find a complete **Dockerfile** in the **start** directory. This **Dockerfile** packages the **usr** server package into a Docker image that contains a preconfigured Open Liberty server.

Run the **mvn package** command from the **start** directory so that the .war file resides in the target directory.

`mvn package`

Build the docker image:

`docker build -t openliberty-getting-started:1.0-SNAPSHOT .`

The Docker **openliberty-getting-started:1.0-SNAPSHOT** image is also built from the Dockerfile. To verify that the image is built, run the docker images command to list all local Docker images:

`docker images`

Your image should appear in the list of all Docker images:

````
REPOSITORY                     TAG             IMAGE ID        CREATED         SIZE
openliberty-getting-started    1.0-SNAPSHOT    85085141269b    21 hours ago    487MB
````

Next, run the image as a container:

`docker run -d --name gettingstarted-app -p 9080:9080 openliberty-getting-started:1.0-SNAPSHOT`

There is a bit going on here, so let’s break down the command:

````
Flag	Description
-d
Runs the container in the background.
--name
Specifies a name for the container.
-p
Maps the container ports to the host ports.
````


The final argument in the docker run command is the Docker image name.

Next, run the docker ps command to verify that your container started:

`docker ps`

Make sure that your container is running and does not have Exited as its status:

````
CONTAINER ID    IMAGE                         CREATED          STATUS           NAMES
4294a6bdf41b    openliberty-getting-started   9 seconds ago    Up 11 seconds    gettingstarted-app
````
To access the application, `curl http://localhost:9080/system/properties` URL.

To stop and remove the container, run the following commands:

`docker stop gettingstarted-app && docker rm gettingstarted-app`

To remove the image, run the following command:

`docker rmi openliberty-getting-started:1.0-SNAPSHOT`

## Running the application from a minimal runnable JAR

So far, Open Liberty has been running out of the **target/liberty/wlp** directory, which effectively contains an Open Liberty server installation and the deployed application. The final product of the Maven build is a server package for use in a continuous integration pipeline and, ultimately, a production deployment.

Open Liberty supports a number of different server packages. The sample application currently generates a usr package that contains the servers and application to be extracted onto an Open Liberty installation.

The type of server package is configured with **<packaging.type/>** in the **pom.xml**.

Instead of creating a server package, you can generate a runnable JAR file that contains the application along with a server runtime. This JAR file can then be run anywhere and deploy your application and server at the same time. To generate a runnable JAR file, override the include property:

`mvn liberty:package -Dinclude=runnable`

To run the JAR, first stop the server if it’s running. Then, navigate to the **target** directory:

`cd target`

And run the **java -jar** command:

`java -jar guide-getting-started.jar`

When the server starts:

Access your application that is now running out of the minimal runnable JAR.

`curl http://localhost:9080/system/properties` 

At this point, you can stop the server by pressing **CTRL+C** in the shell session that the server runs in.

## Well done

Well done you have learned the basics of deploying and updating an application on an Open Liberty server.
