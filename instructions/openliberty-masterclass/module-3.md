## Module 3: Integration Testing & Docker

# Integration Testing

Tests are essential for developing maintainable code.  Developing your application using bean-based component models like CDI makes your code easily unit-testable. Integration Tests are a little more challenging.  In this section you'll add a `barista` service integration test using the `maven-failsafe-plugin`.  During the build, the Liberty server will be started along with the `barista` application deployed, the test will be run and then the server will be stopped.  The starting and stopping of the Liberty server is configured by the Liberty parent pom (see https://search.maven.org/artifact/net.wasdev.wlp.maven.parent/liberty-maven-app-parent/2.6.3/pom), which is configured as the parent of the Masterclass poms.

Because we're going to be testing a REST `POST` request, we need JAX-RS client support and also support for serializing `json` into the request.  We also need `junit` for writing the test.  Add these dependencies to the `open-liberty-masterclass/start/barista/pom.xml`:

```XML
        <!-- Test dependencies -->  
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.4.2</version>
            <scope>test</scope>
        </dependency>    
        <dependency>
            <groupId>org.apache.cxf</groupId>
            <artifactId>cxf-rt-rs-mp-client</artifactId>
            <version>3.3.0</version>
            <scope>test</scope>
        </dependency>      
        <dependency>
            <groupId>com.fasterxml.jackson.jaxrs</groupId>
            <artifactId>jackson-jaxrs-json-provider</artifactId>
            <version>2.9.3</version>
            <scope>test</scope>
        </dependency>
```

Note, the later `Testing in Containers` module requires the JUnit 5 Jupiter API so we're using the same API here.

Note the `<scope/>` of the dependencies is set to `test` because we only want the dependencies to be used during testing.

Next add `maven-failsafe-plugin` configuration at the end of the `<plugins/>` section:

```XML
        <plugins>
            ...
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.0.0-M1</version>
                <executions>
                    <execution>
                        <phase>integration-test</phase>
                        <id>integration-test</id>
                        <goals>
                            <goal>integration-test</goal>
                        </goals>
                        <configuration>
                            <trimStackTrace>false</trimStackTrace>
                            <!-- Starting in version 3.0 of the liberty-maven-plugin these will be set automatically -->
                            <systemProperties>
                                <liberty.test.port>${testServerHttpPort}</liberty.test.port>
                            </systemProperties>
                        </configuration>
                    </execution>
                    <execution>
                        <id>verify-results</id>
                        <goals>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>                        
        </plugins>
```
Note, this configuration makes the port of the server available to the test as a system property called `liberty.test.port`.

Finally, add the test code.  Create a file called, `open-liberty-masterclass/start/barista/src/test/java/com/sebastian-daschner/barista/it/BaristaIT.java` and add the following:

```Java
package com.sebastian_daschner.barista.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import com.sebastian_daschner.barista.boundary.BrewsResource;
import com.sebastian_daschner.barista.entity.CoffeeBrew;
import com.sebastian_daschner.barista.entity.CoffeeType;

public class BaristaIT {
    private static String URL;

    @BeforeAll
    public static void init() {
        String port = System.getProperty("liberty.test.port");
        URL = "http://localhost:" + port + "/barista/resources/brews";
    }
    @Test
    public void testService() throws Exception {

        Client client = null;
        WebTarget target = null;
        try {
            client = ClientBuilder.newClient().register(JacksonJsonProvider.class);
            target = client.target(URL);

        } catch (Exception e) {
            client.close();
            throw e;
        }

        CoffeeBrew brew = new CoffeeBrew();
        brew.setType(CoffeeType.POUR_OVER);

        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(brew));

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

This test sends a `json` request to the `barista` service and checks for a `200 OK` response. 

Re-build and run the tests:

```
mvn install
```

In the output of the build, you should see:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.sebastian_daschner.barista.it.BaristaIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.365 sec - in com.sebastian_daschner.barista.it.BaristaIT

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```


# Docker

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

# Overriding Dev Server Configuration

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