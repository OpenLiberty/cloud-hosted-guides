## Module 4: Testing with Containers & Support Licensing

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
