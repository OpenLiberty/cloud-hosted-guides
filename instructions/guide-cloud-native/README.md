# Get started with cloud-native on the open Java stack

### What you will learn

Have a go at developing a cloud-native microservice on a fully open source and open standards stack. Use the Eclipse MicroProfile programming model to develop a robust and flexible microservice. Deploy it to the Open Liberty server running on the Eclipse OpenJ9 JVM.  Handle microservice metrics and alerting with MicroProfile Metrics and Health.  Finally, build and run the application in a Docker container ready for deployment to your favorite cloud.

### Introduction

Cloud-native is an approach to application development and deployment.  It's the product of a number of industry movements over the past 10-15 years - agile development practices, DevOps, Microservices and Cloud.  Cloud-native applications are developed using agile practices, use continuous integration/continuous delivery to streamline deployment, are architected around team-aligned microservices, and leverage the cloud for rapid deployment at scale.

Cloud-native doesn't change the principles around which solutions are chosen and so often avoiding vendor lock-in is key.  Open source and open standards are essential enablers for avoiding vendors lock-in.  This quick tutorial takes you through using an Open Java Stack with Open Source and Open Standards at its heart; OpenJ9, AdoptOpenJDK, Open Liberty, MicroProfile, and Docker.

If a terminal window does not open navigate:

```
Terminal -> New Terminal
```

Check you are in the **home/project** folder:

```
pwd
```
{: codeblock}

The fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:

```
git clone https://github.com/yasmin-aumeeruddy/SkillsNetworkLabs.git
cd SkillsNetworkLabs
```
{: codeblock}


# A look at OpenJ9 and AdoptOpenJDK

<a href="http://www.eclipse.org/openj9/">OpenJ9</a> is an Eclipse open source JVM. It resulted from the contribution of IBM's JVM implementation to Eclipse and so has many years of high-volume, high-availability production use behind it. Its
low footprint, fast startup and high throughput characteristics make it an ideal choice for cloud-native applications - if you pay for your cloud by memory footprint, this is going to be important to you.

Every JVM needs a class library, and most people don't want to build their own Java distribution.  The best place to get a build of OpenJ9 is <a href="https://adoptopenjdk.net/">AdoptOpenJDK</a>.  This provides pre-built binaries of the OpenJDK class libraries with different JVMs.  The OpenJ9 + OpenJDK builds can be found here: https://adoptopenjdk.net/?variant=openjdk8-openj9 . 

In a terminal, type: 

```
which java
```
{: codeblock}

To find out more about the Java you have installed, type: 

```
java -version
```
{: codeblock}

You should see something like the following:

```
openjdk version "11.0.6" 2020-01-14
OpenJDK Runtime Environment AdoptOpenJDK (build 11.0.6+10)
Eclipse OpenJ9 VM AdoptOpenJDK (build openj9-0.18.1, JRE 11 Linux amd64-64-Bit Compressed References 20200122_441 (JIT enabled, AOT enabled)
OpenJ9   - 51a5857d2
OMR      - 7a1b0239a
JCL      - da35e0c380 based on jdk-11.0.6+10)
```

# Build a cloud-native microservice 

This tutorial comes with a pre-build Microservice for you to study and extend.

Inside this directory, you'll see a **pom.xml** file for the maven build, a **Dockerfile** to build a docker image and a **src** directory containing the implementation.

Build and run the microservice application:

```
mvn liberty:run
```
{: codeblock}

In another terminal, run the following command to call the microservice URL: 

```
curl http://localhost:9080/mpservice/greeting/hello/John%20Doe`
```
{: codeblock}

The response should look like:

```JSON
{
    "message": "Hello",
    "name": "John Doe"
}
```
Stop the server by entering `CTRL+C`

# A look at MicroProfile

<a href="http://microprofile.io">MicroProfile</a> is a set of industry specifications for developing Cloud-native applications. At its foundation are a small number of Java EE specifications; JAX-RS, CDI and JSON-P, which are then augmented with technologies addressing the needs of Cloud-native applications.  

The tutorial code shows example use of MicroProfile Health and Metrics.  

### MicroProfile Health

Start the server again with the following command:

```
mvn liberty:dev
```
{: codeblock}

The goal, dev, invokes the create, install-feature, and deploy goals before starting the server. Note: This goal is designed to be executed directly from the Maven command line. To exit dev mode, type **q** and press **Enter**.

Dev mode provides three key features. Code changes are detected, recompiled, and picked up by your running server. Unit and integration tests are run on demand when you press Enter in the command terminal where dev mode is running, or optionally on every code change to give you instant feedback on the status of your code. Finally, it allows you to attach a debugger to the running server at any time to step through your code.

When you started Open Liberty, it wrote out a number of available endpoints.  One of those is the health endpoint for the application:

```
curl http://localhost:9080/health/
```
{: codeblock}

You should see:

```JSON
{
  "checks": [
    {
      "data": {
        
      },
      "name": "GreetingServiceReadiness",
      "status": "UP"
    }
  ],
  "status": "UP"
}
```

The MicroProfile health for this application has an overall "outcome" which is determined by the outcome of any available individual health "checks".  If any of those checks are "DOWN" then the overall outcome is considered to be "DOWN".

As well as returning a JSON description of the health outcome, the health endpoint also reflects the outcome in the http response code.  An outcome of "UP" returns a 200 OK, whereas an outcome of "DOWN" returns a 503 Service Unavailable.  This means the endpoint can be hooked up to Kubernetes liveness or readiness probes to reflect the service availability.

The tutorial application health has one "check".  This is implemented in **src/main/java/my/demo/health/GreetingReadinessCheck.java**, the main code of which looks like:

```Java
@Readiness
@ApplicationScoped
public class GreetingReadinessCheck implements HealthCheck {

    public boolean isReady() {

        // Check the health of dependencies here

        return true;

    }

    @Override
    public HealthCheckResponse call() {
        boolean up = isReady();
        return HealthCheckResponse.named("GreetingServiceReadiness").state(up).build();
    }
}
```
{: codeblock}

MicroProfile supports two types of health checks: readiness and liveness.  These match the health checks supported by deployment environments like Kubernetes and, indeed, the MicroProfile Health APIs have been designed to integrate perfectly and Kubernetes liveness and readiness probes.

A readiness check will typically check the availability of resources the service requires in order to correctly function (e.g. services it depends on, database connections, etc).  The tutorial application has a simple example readiness check which just returns true because this service does not have any other dependencies.

You can implement many checks as part of your service and their outcomes are aggregated at the **/health/ready** endpoint.  Liveness checks are aggregated at **/health/live** and all checks are aggregated at **/health**.

Feel free to try each of these endpoints.  You'll see there's a default **/health/live** endpoint that always reports as **UP**.

```
curl http://localhost:9080/health/ready
```
{: codeblock}

```
curl http://localhost:9080/health/live
```
{: codeblock}

```
curl http://localhost:9080/health
```
{: codeblock}

### MicroProfile Metrics

When you started Open Liberty it wrote out an endpoint for MicroProfile Metrics:

```
curl http://localhost:9080/metrics/
```
{: codeblock}

If you tried to access the endpoint you will have found that it requires security configuration to work.  The Metrics endpoint is only available over https and, by default, also requires an authorized user in order to prevent disclosing potentially sensitive information.

The MicroProfile Metrics feature allows you to turn off the security requirements.  This makes it easier to test out Metrics, but is not intended for production systems.

Edit the **server.xml**:

> [File->Open] SkillsNetworkLabs/src/main/liberty/config/server.xml

Add the following line:

```XML
    <mpMetrics authentication="false" /> 
```
{: codeblock}

Now when you access the metrics endpoint you will be able to access it over http and not be asked to authenticate.

You should now see metrics data like this:

```
# TYPE base:classloader_total_loaded_class_count counter
# HELP base:classloader_total_loaded_class_count Displays the total number of classes that have been loaded since the Java virtual machine has started execution.
base:classloader_total_loaded_class_count 8807
# TYPE base:cpu_system_load_average gauge
...
```

The MicroProfile system metrics, for example, JVM heap, cpu, and garbage collection information, don't require any additional coding - they're produced automatically from the JVM.  The metrics data is in <a href="https://prometheus.io">Prometheus</a> format, the default for MicroProfile.  Using an `Accept` header on the request, you can also receive json format (not shown in this tutorial).

The tutorial application also shows a MicroProfile application metrics in the microservice implementation. 

Open the **GreetingService.java**:

> [File->Open] SkillsNetworkLabs/src/main/java/my/demo/GreetingService.java

```Java
@Path("/hello")
@RequestScoped
public class GreetingService {


    @Inject
    @ConfigProperty(name="greetingServiceGreeting", defaultValue = "Hello")
    private String greetingStr;

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(name = "sayHelloTime", displayName = "Call duration", 
           description = "Time spent in call")
    public Greeting sayHello(@PathParam("name") String name) {
        return new Greeting(greetingStr, name);
    }

}
```
{: codeblock}

The **@Timed** annotation is an example of one of a number of MicroProfile metric types.  This metric produces timing information for the execution of the **sayHello** service method.  Other metrics include counting method access to measure load, or gauges for custom measurement. 

Access the service endpoint to cause some application measurements to be recorded: 

```
curl http://localhost:9080/mpservice/greeting/hello/John%20Doe
```
{: codeblock}

These measurements will be available at the `/metrics` endpoint, but you can also just see the application metrics at: 

```
curl --insecure https://localhost:9443/metrics/application
```
{: codeblock}

### MicroProfile Config

Externalizing configuration is one of the key tenets of <a href="https://12factor.net/">12-factor applications</a>. Externalizing everything that varies between deployments into configuration means you can build once and deploy in the many stages of your DevOps pipeline, thus removing the risk of your application changing between deployments and invalidating previous testing.  

The tutorial application has also included the use of MicroProfile Config for injecting a configuration property using **@ConfigProperty**.  Open Liberty supports a number of **config sources**.  The tutorial shows the use of Open Liberty **bootstrap.properties**.  

The **pom.xml** file contains the following configuration for the greeting:

```XML
<bootstrapProperties>
    ...
    <greetingServiceGreeting>Hello</greetingServiceGreeting>
</bootstrapProperties>
```

The maven build puts this value in: **target/liberty/wlp/usr/servers/mpserviceServer/bootstrap.properties**

```
greetingServiceGreeting=Hello
```

This file is read at server startup and the value injected into the GreetingService bean when it is created.

Edit line 100 of the pom.xml file which is situated in the SkillsNetworkLabs folder and change the greeting to **Bonjour**

```XML
<bootstrapProperties>
    ...
    <greetingServiceGreeting>Bonjour</greetingServiceGreeting>
</bootstrapProperties>
```
{: codeblock}

Stop the server by entering **q** in the terminal and start it again: `mvn liberty:dev`.

*Note: if you trigger a rebuild, the integration test will fail as it's expecting the response message to be "Hello". However, the server will still build and run.*

Call the service again to see the greeting change:

```
curl http://localhost:9080/mpservice/greeting/hello/John%20Doe
```
{: codeblock}

You should now see:

```JSON
{
    "message": "Bonjour",
    "name": "John Doe"
}
```

This example shows static config injection, where the configuration is read at server start-up.  MicroProfile and Open Liberty also support dynamic configuration injection which means the configuration is re-read periodically (e.g. every 500ms) and so does not require a server restart.

### MicroProfile OpenAPI

When you started Open Liberty it wrote out two endpoints for MicroProfile OpenAPI:

```
curl http://localhost:9080/openapi/` and `curl http://localhost:9080/openapi/ui/
```
{: codeblock}

Clicking on the first link displays a machine-readable yaml description of the service, the format of which is defined by the <a href="https://www.openapis.org/">OpenAPI Initiative</a>.  

```YAML
openapi: 3.0.0
info:
  title: Deployed APIs
  version: 1.0.0
servers:
- url: http://localhost:9080/mpservice
- url: https://localhost:9443/mpservice
paths:
  /greeting/hello/{name}:
    get:
      operationId: sayHello
      parameters:
      - name: name
        in: path
        required: true
        schema:
          type: string
      responses:
        default:
          description: default response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Greeting'
components:
  schemas:
    Greeting:
      type: object
      properties:
        message:
          type: string
        name:
          type: string
```

This yaml form of the API can be used by API Gateways or generators for clients to work with your service - for example, to generate client code to call your service.  A number of generators are available for a variety of languages.

The second link is to a web page that gives a human-readable representation of the API and also allows you to browse and call the API.  

The machine-readable and Web page API descriptions are created automatically from the JAX-RS definition with no additional work required.  As a result, the information provided for your service is pretty basic.  One of the things MicroProfile OpenAPI provides is a number of annotations to enable you to provide better API documentation.

Edit the **src/main/java/my/demo/GreetingService.java** to add documentation for the operation using the @Operation annotation on line 45:

```Java
   ...
    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(name = "sayHelloTime", displayName = "Call duration", 
           description = "Time spent in call")
    @Operation(
        summary = "Get a greeting",
        description = "Returns a greeting for the provided name.")
    public Greeting sayHello(@PathParam("name") String name) {
        return new Greeting(greetingStr, name);
    }
    ...
```
{: codeblock}

You'll also need to add the package import for the annotation:

```Java
import org.eclipse.microprofile.openapi.annotations.Operation;
```
{: codeblock}

Browse the OpenAPI endpoint:

```
curl http://localhost:9080/openapi/
```
{: codeblock}

You'll see that your API now has additional documentation:

```yaml
...
  /greeting/hello/{name}:
    get:
      summary: Get a greeting
      description: Returns a greeting for the provided name.
      operationId: sayHello
      parameters:
...
```

Stop the server by entering **q** in the terminal. 

There are additional annotations available to help you document the parameters and more.

### Further Reading

MicroProfile has many other important capabilities, such as a type-safe REST client, and fault tolerance (the ability to gracefully handle failures in service dependencies).  Visit the <a href="https://openliberty.io/guides/?search=MicroProfile&key=tag">Open Liberty MicroProfile Guides</a> for more details and deeper dives into what we've covered here.

## Containerization (Docker)

Docker has rapidly become the containerization technology of choice for deploying cloud-native applications. All major cloud vendors support Docker, including IBM Cloud and IBM Cloud Private. 

The tutorial includes a Dockerfile for building a docker image for the Microservice.  This Dockerfile is based on the Open Liberty docker image from Docker Hub and adds in the project's server configuration and application from an Open Liberty 'usr server package'.  A usr server package only contains an application and server configuration and is designed to be unzipped over an existing Open Liberty installation (such as the one on the Liberty Docker image).  The advantage of this approach over putting a 'fat jar' (an option supported by Liberty as well as Spring Boot) which contains a lot of infrastructure code, in a docker container, is Docker will cache the pre-req infrastructure layers (e.g. Open Liberty, Java, etc) which makes building and deploying much faster.

## Build a usr server package

By default the **pom.xml** builds a 'fat jar': **target/mpservice.jar** so we need to build a different package that only includes the server configuration and application (not the server runtime) - a **usr** server package.

Change the value of line 100 in the **pom.xml** file back to "hello". 

The project's maven pom file includes a maven profile for building a usr package, which isn't built by default.  Build the usr server package with: 

```
mvn -P usr-package install
```
{: codeblock}

This results in a server zip package: **target/defaultServer.zip**.  In the **usr-package** build we also use the name **defaultServer** for the server because this is the name of the server the base Liberty Docker images automatically runs when the container is started.

## Build and run in Docker

In the directory where the **Dockerfile* is located run:

```
docker build -t my-demo:mpservice .
```
{: codeblock}

To see the image that you created, run the following command:

```
docker images
```
{: codeblock}

If the server is already running, stop it: 

```
Enter `q` + `enter` in the terminal.
```

Run the docker image: 

```
docker run -d --name mpservice -p 9080:9080 -p 9443:9443 my-demo:mpservice
```
{: codeblock}

To see the docker container that is running, use the following command in another terminal: 

```
docker ps
```
{: codeblock}

Access it with 

```
curl localhost:9080/mpservice/greeting/hello/John%20Doe
```
{: codeblock}

Note: the **open-liberty** image referenced in the Dockerfile is based on IBM Java (built on Open J9) because we wanted to re-use the official Open Liberty Docker image. Creating an image based on Open J9 would be relatively straightforward.

To stop and remove the container, run the following command:

```
docker stop mpservice && docker rm mpservice
```
{: codeblock}

To remove the image, run the following command:

```
docker rmi my-demo:mpservice
```
{: codeblock}

# Summary

## Clean up your environment
Delete the **SkillsNetworkLabs** project by navigating to the **/home/project/** directory

```
rm -r -f SkillsNetworkLabs
```
{: codeblock}

## Well Done
Congratulations, you have built, a cloud-native application, seen how you can monitor it for health and metrics, change its configuration, and package and run it in Docker, ready for deployment to your cloud of choice.  I recommend IBM Cloud or IBM Cloud Private, of course ;)
