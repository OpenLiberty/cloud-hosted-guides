## Module 2: Application APIs & Configuration

# Application APIs

Open Liberty has support for many standard APIs out of the box, including all the latest Java EE 8 APIs and the latest MicroProfile APIs.  To lead in the delivery of new APIs, a new version of Liberty is released every 4 weeks and aims to provide MicroProfile implementations soon after they are finalized.

As we've seen, to use a new feature, we need to add them to the build.  There is no need to add a dependency on the APIs for the feature because each feature depends on the APIs.  That means during build, the API dependencies are automatically added from maven central.

For example, take a look at: https://search.maven.org/artifact/io.openliberty.features/mpMetrics-2.0/19.0.0.8/esa

You'll see in the XML on the left that this feature depends on:

```XML
    <dependency>
      <groupId>io.openliberty.features</groupId>
      <artifactId>com.ibm.websphere.appserver.org.eclipse.microprofile.metrics-2.0</artifactId>
      <version>19.0.0.8</version>
      <type>esa</type>
    </dependency>
```
Which depends on the Metrics API from Eclipse MicroProfile:

```XML
    <dependency>
      <groupId>org.eclipse.microprofile.metrics</groupId>
      <artifactId>microprofile-metrics-api</artifactId>
      <version>2.0.0</version>
    </dependency>
```

And so during build, this API will be added for you.

We're now going to add Metrics to the `coffee-shop`.  Edit the `open-liberty-masterclass/start/coffee-shop/pom.xml` file and add the following dependency:

```XML
        <dependency>
            <groupId>io.openliberty.features</groupId>
            <artifactId>mpMetrics-2.0</artifactId>
            <type>esa</type>
        </dependency>
```

Build the project:

```
mvn install
```

You should see that during the build, the following features are installed, and include mpMetrics-2.0:

```
[INFO] [AUDIT   ] CWWKF0012I: The server installed the following features: [beanValidation-2.0, cdi-2.0, distributedMap-1.0, ejbLite-3.2, el-3.0, jaxrs-2.1, jaxrsClient-2.1, jndi-1.0, json-1.0, jsonp-1.1, mpConfig-1.3, mpHealth-2.0, mpMetrics-2.0, mpOpenAPI-1.1, mpRestClient-1.3, servlet-4.0, ssl-1.0].
```
Now we have the API available, we can update the application to include a metric which will count the number of times a coffee order is requested. In the file `open-liberty-masterclass/start/coffee-shop/src/main/java/com/sebastian_daschner/coffee_shop/boundary/OrdersResource.java`, add the following `@Counted` annotation to the `orderCoffee` method:

```Java
    @POST
    @Counted(name="order", displayName="Order count", description="Number of times orders requested.")
    public Response orderCoffee(@Valid @NotNull CoffeeOrder order) {
        ...
    }
```

You'll also need to add the following package import:
```Java
import org.eclipse.microprofile.metrics.annotation.Counted;
```


Rebuild the project:

```
mvn install
```

# Server Configuration

In the previous module you added the `mpMetrics-2.0` feature to the Liberty build.  This makes the feature available for use by the Liberty runtime, but as we saw with the `mpOpenAPI` feature loading the feature at runtime is a separate explicit choice.

Open the file `open-liberty-masterclass/start/coffee-shop/src/main/liberty/config/server.xml`

Near the top of the file, you'll see the following `<featureManager/>` entry:

```XML
    <featureManager>
        <feature>jaxrs-2.1</feature>
        <feature>ejbLite-3.2</feature>
        <feature>cdi-2.0</feature>
        <feature>beanValidation-2.0</feature>
        <feature>mpHealth-2.0</feature>
        <feature>mpConfig-1.3</feature>
        <feature>mpRestClient-1.3</feature>
        <feature>jsonp-1.1</feature>
        <feature>mpOpenAPI-2.0</feature>
    </featureManager>
```

Add the following inside the `<featureManager/>` element to include the `mpMetrics-2.0` feature:

```XML
        <feature>mpMetrics-2.0</feature>
```

In the `open-liberty-masterclass/start/coffee-shop` directory, build the updated application and start the server:

```
mvn install liberty:run
```

You should now see a message for a new metrics endpoint that looks like:

```
[INFO] [AUDIT   ] CWWKT0016I: Web application available (default_host): http://localhost:9080/metrics/

```

Open the metrics endpoint in your browser.  You should see a message like this:

```
Error 403: Resource must be accessed with a secure connection try again using an HTTPS connection.
```

If you take a look at the server output, you should see the following error:

```
[INFO] [ERROR   ] CWWKS9113E: The SSL port is not active. The incoming http request cannot be redirected to a secure port. Check the server.xml file for configuration errors. The https port may be disabled. The keyStore element may be missing or incorrectly specified. The SSL feature may not be enabled.
```

It's one thing to configure the server to load a feature, but many Liberty features require additional configuration.  The complete set of Liberty features and their configuration can be found here: https://openliberty.io/docs/ref/config/.

The error message suggests we need to add a `keyStore` and one route to solve this would be to add a `keyStore` and user registry (e.g. a `basicRegistry` for test purposes).  However, if we take a look at the configuration for mpMetrics (https://openliberty.io/docs/ref/config/#mpMetrics.html) we can see that it has an option to turn the metrics endpoint authentication off.

Add the following to the `open-liberty-masterclass/start/coffee-shop/src/main/liberty/config/server.xml`

```XML
    <mpMetrics authentication="false" />
```

Rebuild, restart the server and visit the metrics endpoint, you should see a number of metrics automatically generated by the JVM:

```
 TYPE base:classloader_total_loaded_class_count counter
# HELP base:classloader_total_loaded_class_count Displays the total number of classes that have been loaded since the Java virtual machine has started execution.
base:classloader_total_loaded_class_count 10616
...
```
This doesn't contain the metrics you added because the service hasn't been called and so no application metrics have been recorded. Use the OpenAPI UI (http://localhost:9080/openapi/ui/) to send a few requests to the service.

As with the `barista` service, you'll need to specify the following payload for the `POST` request:

```JSON
{
  "type": "ESPRESSO"
}
```

Reload the metrics page and at the bottom of the metrics results you should see:

```
...
# TYPE application:com_sebastian_daschner_coffee_shop_boundary_orders_resource_order counter
# HELP application:com_sebastian_daschner_coffee_shop_boundary_orders_resource_order Number of times orders requested.
application:com_sebastian_daschner_coffee_shop_boundary_orders_resource_order 3
```

# Externalizing Configuration

If you're familiar with the concept of 12-factor applications (see http://12factor.net) you'll know that factor III states that an applications configuration should be stored in the environment.  Config here is referring to things which vary between development, staging and production. In doing so you can build the deployment artefact once and deploy it in the different environments unchanged.

Liberty lets your application pick up configuration from a number of sources, such as environment variables, bootstrap.properties and Kubernetes configuration.

Bootstrap.properties lets you provide simple configuration values to substitute in the server configuration and also to use within the application.  The following example replaces the hard-coded base URL the `coffee-shop` service uses to talk to the `barista` service, as well as the ports it exposes.

In the `open-liberty-masterclass/start/coffee-shop/pom.xml` file, in the existing `<properties/>` element, add the following port and url values:

```XML
    <properties>
        ...
        <testServerHttpPort>9080</testServerHttpPort>
        <testServerHttpsPort>9443</testServerHttpsPort>
        <baristaBaseURL>http://localhost:9081</baristaBaseURL>
        ...
    </properties>
```
This `<properties/>` element is where the property values are set that can then be re-used within the maven project.  

In the `<bootstrapProperties/>` section of the `liberty-maven-plugin` configuration, add the following:

```XML
                    <bootstrapProperties>
                        ...
                        <env.default_http_port>${testServerHttpPort}</env.default_http_port>
                        <env.default_https_port>${testServerHttpsPort}</env.default_https_port>
                        <default_barista_base_url>${baristaBaseURL}</default_barista_base_url>
                    </bootstrapProperties>
```
The above takes the properties we defined in the maven project and passes them to Liberty as bootstrap properties.

Note, we're using the `env.` prefix because in the Docker modules of this Masterclass you will set these through environment variables. Note, also the names use underscores (`_`) so they can be passed as environment variables.

Build the project:

```
mvn install
```
The `liberty-maven-plugin` generated the following file `target/liberty/wlp/usr/servers/defaultServer/bootstrap.properties` which contains the configuration that will be loaded and applied to the server configuration.  If you view the file you'll see the values you specified:

```YAML
# Generated by liberty-maven-plugin
default_barista_base_url=http://localhost:9081
env.default_http_port=9080
env.default_https_port=9443
war.name=coffee-shop.war
```
We now need to change the server configuration to use these values.  In the `open-liberty-masterclass/start/coffee-shop/src/main/liberty/config/server.xml` file, change this line:

```XML
    <httpEndpoint host="*" httpPort="9080" httpsPort="9443" id="defaultHttpEndpoint"/>
```
to 

```XML
    <httpEndpoint host="*" httpPort="${env.default_http_port}" httpsPort="${env.default_https_port}" id="defaultHttpEndpoint"/>
```

Next we'll use the `default_barista_base_url` in the code to avoid hard-coding the location of the `barista` service.

Edit the file `open-liberty-masterclass/start/coffee-shop/src/main/java/com/sebastian_daschner/coffee_shop/control/Barista.java`

Change:

```Java
    String baristaBaseURL = "http://localhost:9081";
```

To:

```Java
    @Inject
    @ConfigProperty(name="default_barista_base_url")
    String baristaBaseURL;
```
You'll also need to add the following imports:

```Java
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
```
This is using the MicroProfile Config specification to inject the configuration value.  Configuration can come from a number of sources, including `bootstrap.properties`.

We also need to make the same changes to the CoffeeShopHealth of the `coffee-shop` service. Edit the file: `open-liberty-masterclass/start/coffee-shop/src/main/java/com/sebastian_daschner/coffee_shop/boundary/CoffeeShopHealth.java`

Change:

```Java
    String baristaBaseURL = "http://localhost:9081";
```

To:

```Java
    @Inject
    @ConfigProperty(name="default_barista_base_url")
    String baristaBaseURL;
```
Add the following imports:

```Java
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
```

For more information on MicroProfile Config see https://openliberty.io/guides/microprofile-config.html.

Rebuild the code, start the `coffee-shop` and `barista` servers and try out the endpoint through the Open API UI.  You can also try out the health endpoint at `http://localhost:9080/health`.
