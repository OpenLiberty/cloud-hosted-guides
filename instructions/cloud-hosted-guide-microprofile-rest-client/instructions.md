
# Consuming RESTful services with template interfaces


Learn how to use MicroProfile Rest Client to invoke RESTful microservices over HTTP in a type-safe way.


## What you'll learn

You will learn how to build a MicroProfile Rest Client to access remote RESTful services. You will create a template interface that maps to the remote service that you want to call.
MicroProfile Rest Client automatically generates a client instance based on what is defined and annotated in the template interface.
Thus, you don't have to worry about all of the boilerplate code, such as setting up a client class, connecting to the remote server, or invoking the correct URI with the correct parameters.

The application that you will be working with is an **inventory** service, which fetches and stores the system property information for different hosts.
Whenever a request is made to retrieve the system properties of a particular host, the **inventory** service will create a client to invoke the **system**
service on that host. The **system** service simulates a remote service in the application.

You will instantiate the client and use it in the **inventory** service. You can choose from two different approaches, https://openliberty.io/docs/ref/general/#contexts_dependency_injection.html[Context and Dependency Injection (CDI)^] with the help of MicroProfile Config or the https://openliberty.io/blog/2018/01/31/mpRestClient.html[RestClientBuilder^] method.
In this guide, you will explore both methods to handle scenarios for providing a valid base URL.

 - When the base URL of the remote service is static and known, define the default base URL in the configuration file. Inject the client with CDI method.

 - When the base URL is not yet known and needs to be determined during the run time, set the base URL as a variable. Build the client with the more verbose **RestClientBuilder** method.


# Getting started

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guidemicroprofilerestclient.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-microprofile-rest-client.git
cd guide-microprofile-rest-client
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.

### Try what you'll build

The **finish** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the **finish** directory and run the following
Maven goal to build the application and deploy it to Open Liberty:

```
cd finish
mvn liberty:run
```
{: codeblock}


After you see the following message, your application server is ready:

```
The defaultServer server is ready to run a smarter planet.
```

You can access the following microservices:

 The [http://localhost:9080/system/properties](http://localhost:9080/system/properties) microservice simulates the remote system service that retrieves the system property information for a specific host
```
curl http://localhost:9080/system/properties
```
{: codeblock}



 The [http://localhost:9080/inventory/systems/localhost](http://localhost:9080/inventory/systems/localhost) microservice is the inventory service that invokes the [http://localhost:9080/inventory/systems/localhost](http://localhost:9080/inventory/systems/localhost) microservice to retrieves the system property information

```
curl http://localhost:9080/inventory/systems/localhost
```
{: codeblock}



* The \http://localhost:9080/inventory/systems/{your_hostname} microservice is the **inventory** service that invokes the \http://{your_hostname}:9080/system/properties microservice. In Windows, Mac OS, and Linux, get your fully qualified domain name (FQDN) by entering **hostname** from your terminal. Visit the URL by replacing **{your_hostname}** with your FQDN.
You will see the same system property information, but the process of getting the information is different.

After you are finished checking out the application, stop the Open Liberty server by pressing **CTRL+C**
in the command-line session where you ran the server. Alternatively, you can run the **liberty:stop** goal
from the **finish** directory in another shell session:

```
mvn liberty:stop
```
{: codeblock}


# Writing the RESTful client interface

Now, navigate to the **start** directory to begin.

When you run Open Liberty in development mode, known as dev mode, the server listens for file changes and automatically recompiles and 
deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```
mvn liberty:dev
```
{: codeblock}


After you see the following message, your application server in dev mode is ready:

```
Press the Enter key to run tests on demand.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, 
or open the project in your editor.

The MicroProfile Rest Client API is included in the MicroProfile dependency specified by your **pom.xml** file. Look for the dependency with the **microprofile** artifact ID.


This dependency provides a library that is required to implement the MicroProfile Rest Client interface.

The **mpRestClient** feature is also enabled in the **src/main/liberty/config/server.xml** file. This feature enables your Open Liberty server to use MicroProfile Rest Client to invoke RESTful microservices.


The code for the **system** service in the **src/main/java/io/openliberty/guides/system** directory is provided for you. It simulates a remote RESTful service that the **inventory** service invokes.

Create a RESTful client interface for the **system** service. Write a template interface that maps the API of the remote **system** service.
The template interface describes the remote service that you want to access. The interface defines the resource to access as a method by mapping its annotations, return type, list of arguments, and exception declarations.

Create the **SystemClient** class.


> [File -> Open]guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/client/SystemClient.java



```
package io.openliberty.guides.inventory.client;

import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "systemClient", baseUri = "http://localhost:9080/system")
@RegisterProvider(UnknownUriExceptionMapper.class)
@Path("/properties")
public interface SystemClient extends AutoCloseable {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Properties getProperties() throws UnknownUriException, ProcessingException;
}
```
{: codeblock}



The MicroProfile Rest Client feature automatically builds and generates a client implementation based on what is defined in the **SystemClient** interface. There is no need to set up the client and connect with the remote service.

Notice the **SystemClient** interface inherits the **AutoCloseable** interface.
This allows the user to explicitly close the client instance by invoking the **close()** method or to implicitly close the client instance using a trywithresources block. When the client instance is closed, all underlying resources associated with the client instance are cleaned up. Refer to the [MicroProfile Rest Client specification](https://github.com/eclipse/microprofilerestclient/releases) for more details.

When the **getProperties()** method is invoked, the **SystemClient** instance sends a GET request to the **<baseUrl>/properties** endpoint, where **<baseUrl>** is the default base URL of the **system** service. You will see how to configure the base URL in the next section.

The **@Produces** annotation specifies the media (MIME) type of the expected response. The default value is **MediaType.APPLICATION_JSON**.

The **@RegisterProvider** annotation tells the framework to register the provider classes to be used when the framework invokes the interface. You can add as many providers as necessary.
In the **SystemClient** interface, add a response exception mapper as a provider to map the **404** response code with the **UnknownUriException** exception.

### Handling exceptions through ResponseExceptionMappers

Error handling is an important step to ensure that the application can fail safely. If there is an error response such as **404 NOT FOUND** when invoking the remote service, you need to handle it. First, define an exception, and map the exception with the error response code. Then, register the exception mapper in the client interface.

Look at the client interface again, the **@RegisterProvider** annotation registers the **UnknownUriExceptionMapper** response exception mapper.
An exception mapper maps various response codes from the remote service to throwable exceptions.


Implement the actual exception class and the mapper class to see how this mechanism works.

Create the **UnknownUriException** class.


> [File -> Open]guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/client/UnknownUriException.java



```
package io.openliberty.guides.inventory.client;

public class UnknownUriException extends Exception {

  private static final long serialVersionUID = 1L;

  public UnknownUriException() {
    super();
  }

  public UnknownUriException(String message) {
    super(message);
  }
}
```
{: codeblock}



Now, link the **UnknownUriException** class with the corresponding response code through a **ResponseExceptionMapper** mapper class.

Create the **UnknownUriExceptionMapper** class.


> [File -> Open]guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/client/UnknownUriExceptionMapper.java



```
package io.openliberty.guides.inventory.client;

import java.util.logging.Logger;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class UnknownUriExceptionMapper
    implements ResponseExceptionMapper<UnknownUriException> {
  Logger LOG = Logger.getLogger(UnknownUriExceptionMapper.class.getName());

  @Override
  public boolean handles(int status, MultivaluedMap<String, Object> headers) {
    LOG.info("status = " + status);
    return status == 404;
  }

  @Override
  public UnknownUriException toThrowable(Response response) {
    return new UnknownUriException();
  }
}
```
{: codeblock}



The **handles()** method inspects the HTTP response code to determine whether an exception is thrown for the specific response, and the **toThrowable()** method returns the mapped exception.

# Injecting the client with dependency injection

Now, instantiate the **SystemClient** interface and use it in the **inventory** service. If you want to connect only with the default host name, you can easily instantiate the **SystemClient** with CDI annotations. CDI injection simplifies the process of bootstrapping the client.

First, you need to define the base URL of the **SystemClient** instance.
Configure the default base URL with the MicroProfile Config feature. This feature is enabled for you in the **server.xml** file.

Create the configuration file.


> [File -> Open]guide-microprofile-rest-client/start/src/main/webapp/META-INF/microprofile-config.properties



```
systemClient/mp-rest/uri=http://localhost:9080/system
```
{: codeblock}



The **mp-rest/uri** base URL config property is configured to the default **\http://localhost:9080/system** URL.

This configuration is automatically picked up by the MicroProfile Config API.

Look at the annotations in the **SystemClient** interface again.


The **@RegisterRestClient** annotation registers the interface as a RESTful client. The runtime creates a CDI managed bean for every interface that is annotated with the **@RegisterRestClient** annotation.

The **configKey** value in the **@RegisterRestClient** annotation replaces the fully-qualified classname of the properties in the **microprofile-config.properties** configuration file.
For example, the **<fully-qualified classname>/mp-rest/uri** property becomes **systemClient/mp-rest/uri**.
The benefit of using Config Keys is when multiple client interfaces have the same **configKey** value, the interfaces can be configured with a single MP config property.

The **baseUri** value can also be set in the **@RegisterRestClient** annotation. However, this value will be overridden by the base URI property defined in the **microprofile-config.properties** configuration file, which takes precedence. In a production environment, you can use the **baseUri** variable to specify a different URI for development and testing purposes.

The **@RegisterRestClient** annotation, which is a bean defining annotation implies that the interface is manageable through CDI. You must have this annotation in order to inject the client.

Inject the **SystemClient** interface into the **InventoryManager** class, which is another CDI managed bean.

Replace the **InventoryManager** class.


> [File -> Open]guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/InventoryManager.java



```
package io.openliberty.guides.inventory;

import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.client.UnknownUriException;
import io.openliberty.guides.inventory.client.UnknownUriExceptionMapper;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;

@ApplicationScoped
public class InventoryManager {

  private List<SystemData> systems = Collections.synchronizedList(
                                       new ArrayList<SystemData>());

  @Inject
  @ConfigProperty(name = "default.http.port")
  String DEFAULT_PORT;

  @Inject
  @RestClient
  private SystemClient defaultRestClient;

  public Properties get(String hostname) {
    Properties properties = null;
    if (hostname.equals("localhost")) {
      properties = getPropertiesWithDefaultHostName();
    } else {
      properties = getPropertiesWithGivenHostName(hostname);
    }

    return properties;
  }

  public void add(String hostname, Properties systemProps) {
    Properties props = new Properties();
    props.setProperty("os.name", systemProps.getProperty("os.name"));
    props.setProperty("user.name", systemProps.getProperty("user.name"));

    SystemData host = new SystemData(hostname, props);
    if (!systems.contains(host))
      systems.add(host);
  }

  public InventoryList list() {
    return new InventoryList(systems);
  }

  private Properties getPropertiesWithDefaultHostName() {
    try {
      return defaultRestClient.getProperties();
    } catch (UnknownUriException e) {
      System.err.println("The given URI is not formatted correctly.");
    } catch (ProcessingException ex) {
      handleProcessingException(ex);
    }
    return null;
  }

  private Properties getPropertiesWithGivenHostName(String hostname) {
    String customURIString = "http://" + hostname + ":" + DEFAULT_PORT + "/system";
    URI customURI = null;
    try {
      customURI = URI.create(customURIString);
      SystemClient customRestClient = RestClientBuilder.newBuilder()
                                        .baseUri(customURI)
                                        .register(UnknownUriExceptionMapper.class)
                                        .build(SystemClient.class);
      return customRestClient.getProperties();
    } catch (ProcessingException ex) {
      handleProcessingException(ex);
    } catch (UnknownUriException e) {
      System.err.println("The given URI is unreachable.");
    }
    return null;
  }

  private void handleProcessingException(ProcessingException ex) {
    Throwable rootEx = ExceptionUtils.getRootCause(ex);
    if (rootEx != null && (rootEx instanceof UnknownHostException
        || rootEx instanceof ConnectException)) {
      System.err.println("The specified host is unknown.");
    } else {
      throw ex;
    }
  }

}
```
{: codeblock}



**@Inject** and **@RestClient** annotations inject an instance of the **SystemClient** called **defaultRestClient** to the **InventoryManager** class.

Because the **InventoryManager** class is **@ApplicationScoped**, and the **SystemClient** CDI bean maintains the same scope through the default dependent scope, the client is initialized once per application.

If the **hostname** parameter is **localhost**, the service runs the **getPropertiesWithDefaultHostName()** helper function to fetch system properties.
The helper function invokes the **system** service by calling the **defaultRestClient.getProperties()** method.


# Building the client with RestClientBuilder

The **inventory** service can also connect with a host other than the default **localhost** host, but you cannot configure a base URL that is not yet known.
In this case, set the host name as a variable and build the client by using the **RestClientBuilder** method. You can customize the base URL from the host name attribute.

Look at the **getPropertiesWithGivenHostName()** method in the **src/main/java/io/openliberty/guides/inventory/InventoryManager.java** file.


The host name is provided as a parameter. This method first assembles the base URL that consists of the new host name.
Then, the method instantiates a **RestClientBuilder** builder with the new URL, registers the response exception mapper, and builds the **SystemClient** instance.

Similarly, call the **customRestClient.getProperties()** method to invoke the **system** service.


# Running the application

You started the Open Liberty server in dev mode at the beginning of the guide, so all the changes were automatically picked up.

When the server is running, select either approach to fetch your system properties:

 Visit the [http://localhost:9080/inventory/systems/localhost](http://localhost:9080/inventory/systems/localhost) URL
```
curl http://localhost:9080/inventory/systems/localhost
```
{: codeblock}






# Testing the application

Create the **RestClientIT** class.


> [File -> Open]guide-microprofile-rest-client/start/src/test/java/it/io/openliberty/guides/client/RestClientIT.java



```
package it.io.openliberty.guides.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.WebTarget;
import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RestClientIT {

  private static String port;

  private Client client;

  private final String INVENTORY_SYSTEMS = "inventory/systems";

  @BeforeAll
  public static void oneTimeSetup() {
    port = System.getProperty("http.port");
  }

  @BeforeEach
  public void setup() {
    client = ClientBuilder.newClient();
    client.register(JsrJsonpProvider.class);
  }

  @AfterEach
  public void teardown() {
    client.close();
  }

  @Test
  public void testSuite() {
    this.testDefaultLocalhost();
    this.testRestClientBuilder();
  }

  public void testDefaultLocalhost() {
    String hostname = "localhost";

    String url = "http://localhost:" + port + "/" + INVENTORY_SYSTEMS + "/" + hostname;

    JsonObject obj = fetchProperties(url);

    assertEquals(System.getProperty("os.name"), obj.getString("os.name"),
                 "The system property for the local and remote JVM should match");
  }

  public void testRestClientBuilder() {
    String hostname = null;
    try{
      hostname = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      System.err.println("Unknown Host.");
    }

    String url = "http://localhost:" + port + "/" + INVENTORY_SYSTEMS + "/" + hostname;

    JsonObject obj = fetchProperties(url);

    assertEquals(System.getProperty("os.name"), obj.getString("os.name"),
                 "The system property for the local and remote JVM should match");
  }

  private JsonObject fetchProperties(String url) {
    WebTarget target = client.target(url);
    Response response = target.request().get();

    assertEquals(200, response.getStatus(), "Incorrect response code from " + url);

    JsonObject obj = response.readEntity(JsonObject.class);
    response.close();
    return obj;
  }

}
```
{: codeblock}



Each test case tests one of the methods for instantiating a RESTful client.

The **testDefaultLocalhost()** test fetches and compares system properties from the \http://localhost:9080/inventory/systems/localhost URL.

The **testRestClientBuilder()** test gets your IP address. Then, use your IP address as the host name to fetch your system properties and compare them.

In addition, a few endpoint tests are provided for you to test the basic functionality of the **inventory** and **system** services. If a test failure occurs, you might have introduced a bug into the code.


### Running the tests

Because you started Open Liberty in dev mode, press the **enter/return** key to run the tests.

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.377 sec - in it.io.openliberty.guides.system.SystemEndpointIT
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
Interceptor for {http://client.inventory.guides.openliberty.io/}SystemClient has thrown exception, unwinding now
Could not send Message.
[err] The specified host is unknown.
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.379 sec - in it.io.openliberty.guides.inventory.InventoryEndpointIT
Running it.io.openliberty.guides.client.RestClientIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.121 sec - in it.io.openliberty.guides.client.RestClientIT

Results :

Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

The warning and error messages are expected and result from a request to a bad or an unknown hostname. This request is made in the **testUnknownHost()** test from the **InventoryEndpointIT** integration test.

To see whether the tests detect a failure, change the base URL in the configuration file so that when the **inventory** service tries to access the invalid URL, an **UnknownUriException** is thrown.
Rerun the tests to see a test failure occur.

When you are done checking out the service, exit dev mode by pressing **CTRL+C** in the command-line session
where you ran the server, or by typing **q** and then pressing the **enter/return** key.


# Summary

## Clean up your environment

Delete the **guide-microprofile-rest-client** project by navigating to the **/home/project/** directory

```
cd ../..
rm -r -f guide-microprofile-rest-client
rmdir guide-microprofile-rest-client
```
{: codeblock}


## Great work! You're done!


You just invoked a remote service by using a template interface with MicroProfile Rest Client in Open Liberty.

MicroProfile Rest Client also provides a uniform way to configure SSL for the client.
You can learn more in the [Hostname verification with SSL on Open Liberty and MicroProfile Rest Client](https://openliberty.io/blog/2019/06/21/microprofilerestclient19006.html#ssl) blog and the https://github.com/eclipse/microprofilerestclient/releases[MicroProfile Rest Client specification^].

Feel free to try one of the related guides where you can learn more technologies and expand on what you built here.

