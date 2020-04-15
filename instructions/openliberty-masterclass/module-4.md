## Module 4: Docker & Testing with Containers

## Docker

We're now going to dockerize the two services and show how we can override the defaults to re-wire the two services.  We're going to use a Docker user-defined network (see https://docs.docker.com/network/network-tutorial-standalone/#use-user-defined-bridge-networks) because we'll be running them on the same host and it keeps things simple.  For real-world production deployments you would use a Kubernetes environment, such as IBM Cloud Private or the IBM Cloud Kubernetes Service.

Take a look at the `open-liberty-masterclass/start/coffee-shop/Dockerfile`:

```Dockerfile
FROM open-liberty:kernel-java8-ibm as server-setup
COPY /target/defaultServer.zip /config/

USER 0
RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip \
    && unzip /config/defaultServer.zip \
    && mv /wlp/usr/servers/defaultServer/* /config/ \
    && rm -rf /config/wlp \
    && rm -rf /config/defaultServer.zip \
    && apt-get remove -y unzip
USER 1001

RUN rm /opt/ol/wlp/usr/servers/defaultServer/bootstrap.properties

FROM open-liberty
LABEL maintainer="Graham Charters" vendor="IBM" github="https://github.com/WASdev/ci.maven"
COPY --from=server-setup /config/ /config/
EXPOSE 9080 9443
```

This Dockerfile uses Docker build stages.  The first stage gets all the application and server configuration contents into the right location and the second builds the actual final image.  Using stages means any temporary files from the first stage don't end up in the final image, so it's smaller.  

The `FROM` statement is building this image using the Open Liberty kernel image (see https://hub.docker.com/_/open-liberty/ for the available images).  The second `RUN` removes the `bootstrap.properties` file to avoid accidentally using it and avoid conflicts with the environment variables we will pass in later through Docker.  The `EXPOSE` makes the two server ports available outside the container.

Let's build the docker image.  In the `open-liberty-masterclass/start/coffee-shop` directory, run (note the period (`.`) at the end of the line is important):

```
docker build -t masterclass:coffee-shop .
```

In the `open-liberty-masterclass/start/barista` directory, run (note the period (`.`) at the end of the line is important):

```
docker build -t masterclass:barista .
```
Next, create the user-defined bridge network:

```
docker network create --driver bridge masterclass-net
```
You can now run the two Docker containers and get them to join the same bridge network.  Providing names to the containers makes those names available for DNS resolution within the bridge network so there's no need to use ip addresses.

Run the `barista` container:

```
docker run --network=masterclass-net --name=barista masterclass:barista
```

Note, we don't need map the `barista` service ports outside the container because the bridge network gives access to the other containers on the same network.

Next, we're going to run the `coffee-shop` container.  For it to work we'll need to provide new values for ports and the location of the barista service.  Run the `coffee-shop` container

```
docker run -p 9080:9080 -p 9445:9443 --network=masterclass-net --name=coffee-shop -e default_barista_base_url='http://barista:9081' -e default_http_port=9080 -e default_https_port=9443 masterclass:coffee-shop
```

You can take a look at the bridge network using:

```
docker network inspect masterclass-net
```
You'll see something like:

```JSON
[
    {
        "Name": "masterclass-net",
        ...
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "172.19.0.0/16",
                    "Gateway": "172.19.0.1"
                }
            ]
        },
        ...
        "Containers": {
            "0fc740d52f2ed8dfdb04127fe3e49366dcbeb7924fee6b0cbf6f891c0909b0e8": {
                "Name": "coffee-shop",
                "EndpointID": "157d697fb4bff2722d654c68e3a5e5fe7554a91e860213d22362cd7cc074fc8f",
                "MacAddress": "02:42:ac:13:00:02",
                "IPv4Address": "172.19.0.2/16",
                "IPv6Address": ""
            },
            "2b78ebf13596147042c8f2f5bd3171ca1c6f77241f419472010ddc2f28fd7a0c": {
                "Name": "barista",
                "EndpointID": "c93163547eb7e3c2c84dd0f72beb77127cfc319b6d9d7f6d9d99e17b85ff6d30",
                "MacAddress": "02:42:ac:13:00:03",
                "IPv4Address": "172.19.0.3/16",
                "IPv6Address": ""
            }
        },
        "Options": {},
        "Labels": {}
    }
]
```
If you need to remove a container, use:

```
docker container rm <container name>
```
You should now be able to load the `coffee-shop` service's Open API page and call the service.  Give it a try.

### Overriding Dev Server Configuration

The above works fine, but still has a metrics endpoint with authentication turned off.  We'll now show how `configDropins/overrides` can be used to override existing, or add new, server configuration.  For example, this can be used to add server configuration in a production environment.  The approach we're going to take is to use a Docker volume, but in a real-world scenario you would use Kubernetes ConfigMaps and secrets to include the production server configuration, security configuration and environment variables.  

In fact, unlike what we have done here, the best practice is to build an image that does not contain any environment specific configuration (such as the unsecured endpoint in our example) and then add those things through external configuration in the development, staging and production environments.  The goal is to ensure deployment of the image without configuration doesn't not cause undesirable results such as security vulnerabilities or talking to the wrong data sources.

Take a look at the file `open-liberty-masterclass/start/coffee-shop/configDropins/overrides/metrics-prod.xml`:

```XML
<?xml version="1.0" encoding="UTF-8"?>
<server description="Coffee Shop Server">

    <featureManager>
        <feature>mpMetrics-2.0</feature>
    </featureManager>
    
    <mpMetrics authentication="true" />

     <!-- 
     Note, this configuration is for demo purposes
     only and MUST NOT BE USED IN PRODUCTION AS IT 
     IS INSECURE. -->  
    <variable name="admin.password" value="change_it" />
    <variable name="keystore.password" value="change_it" />
    
    <quickStartSecurity userName="admin" userPassword="${admin.password}"/>
    <keyStore id="defaultKeyStore" password="${keystore.password}"/>    
     
</server>
```
You'll see that this turns metrics authentication on and sets up some simple security required for securing/accessing the metrics endpoint.  Note, this configuration really is **NOT FOR PRODUCTION**, it's simply aiming to show how to override, or provide new, server configuration.

If you're on a unix-based OS, in the `open-liberty-masterclass/start/coffee-shop` directory, run the `coffee-shop` container:

```
docker run -p 9080:9080 -p 9445:9443 --network=masterclass-net --name=coffee-shop -e default_barista_base_url='http://barista:9081' -e default_http_port=9080 -e default_https_port=9443 -v $(pwd)/configDropins/overrides:/opt/ol/wlp/usr/servers/defaultServer/configDropins/overrides  masterclass:coffee-shop
```
The above relies on `pwd` to fill in the docker volume source path.  If you're on Windows, replace `$(pwd)` with the absolute path to the `open-liberty-masterclass/start/coffee-shop` directory in the above command.

You should see the following message as the server is starting:

```
[AUDIT   ] CWWKG0102I: Found conflicting settings for mpMetrics configuration.
  Property authentication has conflicting values:
    Value false is set in file:/opt/ol/wlp/usr/servers/defaultServer/server.xml.
    Value true is set in file:/opt/ol/wlp/usr/servers/defaultServer/configDropins/overrides/metrics-prod.xml.
  Property authentication will be set to true.
```
This shows that we have turned metrics authentication back on.

Access the metrics endpoint at: `https://localhost:9445/metrics`

You will see that the browser complains about the certificate.  This is a self-signed certificate generated by Liberty for test purposes.  Accept the exception (note,  Firefox may not allow you to do this in which case you'll need to use a different browser).  You'll be presented with a login prompt.  Sign in with userid `admin` and password `change_it` (the values in the `metrics-prod.xml`).

## Testing in Containers

We saw in an earlier module, how to perform Integration Tests against the application running in the server.  We then showed how to package the application and server and run them inside a Docker container.  Assuming we're going to deploy our application in production inside Containers it would be a good idea to actually performs tests against that configuration.  The more we can make our development and test environments the same as production, the less likely we are to encounter issues in production.  MicroShed Testing (microshed.org) is a project that enables us to do just that.

Let's create a new Integration Test that will perform the same test, but inside a running container.  In the Barista project, add the follow dependencies to the `pom.xml` file in the `<dependencies>` element:

```XML
        <!-- For MicroShed Testing -->        
        <dependency>
            <groupId>com.github.microshed.microshed-testing</groupId>
            <artifactId>microshed-testing-liberty</artifactId>
            <version>0.4.1-beta</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.26</version>
            <scope>test</scope>
        </dependency>
```

Add the following `<repository>` element to the `pom.xml`.  This should be as a peer of the `<properties>` element:

```XML
    <repositories>
        <!-- https://jitpack.io/#microshed/microshed-testing -->
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
```

The MicroShed Testing project is not released to Maven Central at the moment and so this entries tells maven about the repository from where it can be downloaded.

Create a new Integration Test called `BaristaContainerIT.java` in the directory `start/barista/src/test/java/com/sebastian_daschner/barista/it` and add the following code:

```Java
package com.sebastian_daschner.barista.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.microshed.testing.jupiter.MicroShedTest;
import org.microshed.testing.testcontainers.MicroProfileApplication;
import org.testcontainers.junit.jupiter.Container;

import com.sebastian_daschner.barista.boundary.BrewsResource;
import com.sebastian_daschner.barista.entity.CoffeeBrew;
import com.sebastian_daschner.barista.entity.CoffeeType;

@MicroShedTest
public class BaristaContainerIT {

    @Container
    public static MicroProfileApplication app = new MicroProfileApplication()
                    .withAppContextRoot("/barista")
                    .withExposedPorts(9081, 9444)
                    .withReadinessPath("/health");
    
    @Inject
    public static BrewsResource brews;

    @Test
    public void testService() throws Exception {
        CoffeeBrew brew = new CoffeeBrew();
        brew.setType(CoffeeType.POUR_OVER);
        Response response = brews.startCoffeeBrew(brew);

        try {
            if (response == null) {
                assertNotNull("GreetingService response must not be NULL", response);
            } else {
                assertEquals("Response must be 200 OK", 200, response.getStatus());
            }
        } finally {
            response.close();
        }
    }
}
```

You'll see that the class is marked as a MicroShed test with the `@MicroShedTest` annotation.

The test also contains the following Container configuration:

```Java
    @Container
    public static MicroProfileApplication app = new MicroProfileApplication()
                    .withAppContextRoot("/barista")
                    .withExposedPorts(9081, 9444)
                    .withReadinessPath("/health");
```


You'll see that the unit test is like any other.

We need to configure `log4j` in order to see the detailed progress of the MicroShed test.  In the directory `start/barista/src/test/resources/` create the file `log4j.properties` and add the following configuration to it:

```properties
log4j.rootLogger=INFO, stdout

log4j.appender=org.apache.log4j.ConsoleAppender
log4j.appender.layout=org.apache.log4j.PatternLayout

log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%r %p %c %x - %m%n

log4j.logger.org.microshed=DEBUG
```

Build and run the test:

```
mvn install
```

You should see the following output:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.sebastian_daschner.barista.it.BaristaContainerIT
0 INFO org.microshed.testing.jupiter.MicroShedTestExtension  - Using ApplicationEnvironment class: org.microshed.testing.testcontainers.config.HollowTestcontainersConfiguration
70 INFO org.testcontainers.dockerclient.DockerClientProviderStrategy  - Loaded org.testcontainers.dockerclient.UnixSocketClientProviderStrategy from ~/.testcontainers.properties, will try it first
710 INFO org.testcontainers.dockerclient.UnixSocketClientProviderStrategy  - Accessing docker with local Unix socket
710 INFO org.testcontainers.dockerclient.DockerClientProviderStrategy  - Found Docker environment with local Unix socket (unix:///var/run/docker.sock)
868 INFO org.testcontainers.DockerClientFactory  - Docker host IP address is localhost
914 INFO org.testcontainers.DockerClientFactory  - Connected to docker: 
  Server Version: 19.03.1
  API Version: 1.40
  Operating System: Docker Desktop
  Total Memory: 1998 MB
1638 INFO org.testcontainers.utility.RegistryAuthLocator  - Credential helper/store (docker-credential-desktop) does not have credentials for quay.io
2627 INFO org.testcontainers.DockerClientFactory  - Ryuk started - will monitor and terminate Testcontainers containers on JVM exit
        ℹ︎ Checking the system...
        ✔ Docker version should be at least 1.6.0
        ✔ Docker environment should have more than 2GB free disk space
2827 INFO org.microshed.testing.testcontainers.MicroProfileApplication  - Discovered ServerAdapter: class org.testcontainers.containers.liberty.LibertyAdapter
2828 INFO org.microshed.testing.testcontainers.MicroProfileApplication  - Using ServerAdapter: org.testcontainers.containers.liberty.LibertyAdapter
2834 DEBUG org.microshed.testing.testcontainers.config.TestcontainersConfiguration  - No networks explicitly defined. Using shared network for all containers in class com.sebastian_daschner.barista.it.BaristaContainerIT
2842 INFO org.microshed.testing.testcontainers.config.HollowTestcontainersConfiguration  - exposing port: 9081 for container alpine:3.5
2843 INFO org.microshed.testing.testcontainers.config.HollowTestcontainersConfiguration  - exposing port: 9444 for container alpine:3.5
2844 INFO org.microshed.testing.testcontainers.config.TestcontainersConfiguration  - Starting containers in parallel for class com.sebastian_daschner.barista.it.BaristaContainerIT
2845 INFO org.microshed.testing.testcontainers.config.TestcontainersConfiguration  -   java.util.concurrent.CompletableFuture@465232e9[Completed normally]
2848 INFO org.microshed.testing.testcontainers.config.TestcontainersConfiguration  - All containers started in 3ms
2868 DEBUG org.microshed.testing.jaxrs.RestClientBuilder  - no classes implementing Application found in pkg: com.sebastian_daschner.barista.boundary
2868 DEBUG org.microshed.testing.jaxrs.RestClientBuilder  - checking in pkg: com.sebastian_daschner.barista
2873 DEBUG org.microshed.testing.jaxrs.RestClientBuilder  - Using ApplicationPath of 'resources'
2874 INFO org.microshed.testing.jaxrs.RestClientBuilder  - Building rest client for class com.sebastian_daschner.barista.boundary.BrewsResource with base path: http://localhost:9081/barista/resources and providers: [class org.microshed.testing.jaxrs.JsonBProvider]
3273 DEBUG org.microshed.testing.jupiter.MicroShedTestExtension  - Injecting rest client for public static com.sebastian_daschner.barista.boundary.BrewsResource com.sebastian_daschner.barista.it.BaristaContainerIT.brews
3419 INFO org.microshed.testing.jaxrs.JsonBProvider  - Sending data to server: {"type":"POUR_OVER"}
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.93 s - in com.sebastian_daschner.barista.it.BaristaContainerIT
[INFO] Running com.sebastian_daschner.barista.it.BaristaIT
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.271 s - in com.sebastian_daschner.barista.it.BaristaIT
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
```

## Support Licensing

Open Liberty is Open Source under the Eclipse Public License v1, as a result there is no fee to use in production.  Community support is available via StackOverflow, Gitter, or the mail list, and bugs can be raised in github (https://github.com/openliberty/open-liberty).  Commercial support from IBM is available for Open Liberty, you can find out more on the IBM Marketplace. The WebSphere Liberty product is built on Open Liberty, there is no migration required to use WebSphere Liberty, you simply point to WebSphere Liberty in your build.  Users of WebSphere Liberty get support for the packaged Open Liberty function.

WebSphere Liberty is also available in Maven Central - see https://search.maven.org/search?q=g:com.ibm.websphere.appserver.runtime

You can use WebSphere Liberty for development even if you haven't purchased it, but if you have production entitlement you can easily change to use it, as follows:

In the `open-liberty-masterclass/start/coffee-shop/pom.xml` change these two lines from:

```XML
                        <groupId>io.openliberty</groupId>
                        <artifactId>openliberty-kernel</artifactId>
```
To:
```XML
                        <groupId>com.ibm.websphere.appserver.runtime</groupId>
                        <artifactId>wlp-kernel</artifactId>
```
Rebuild and re-start the `coffee-shop` service:

```
mvn install liberty:run
```

Try the service out using the Open API Web page and you should see the behavior is identical.  Not surprising since the code is identical, from the same build, just built into WebSphere Liberty.

## Conclusion

Thanks for trying the Open Liberty Masterclass. If you're interested in finding out more, please visit http://openliberty.io, and for more hands-on experience, why not try the Open Liberty Guides - http://openliberty.io/guides.
