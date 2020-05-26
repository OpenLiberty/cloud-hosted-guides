
# Consuming RESTful services with template interfaces

## What you'll learn

You will learn how to build a MicroProfile Rest Client to access remote RESTful services. You will create a template interface that maps to the remote service that you want to call.
MicroProfile Rest Client automatically generates a client instance based on what is defined and annotated in the template interface.
Thus, you don't have to worry about all of the boilerplate code, such as setting up a client class, connecting to the remote server, or invoking the correct URI with the correct parameters.

The application that you will be working with is an **inventory** service, which fetches and stores the system property information for different hosts.
Whenever a request is made to retrieve the system properties of a particular host, the **inventory** service will create a client to invoke the **system**
service on that host. The **system** service simulates a remote service in the application.

You will instantiate the client and use it in the **inventory** service. You can choose from two different approaches, [CDI](https://openliberty.io/docs/ref/general/#contexts_dependency_injection.html[Context and Dependency Injection) with the help of MicroProfile Config or the [RestClientBuilder](https://openliberty.io/blog/2018/01/31/mpRestClient.html) method.
In this guide, you will explore both methods to handle scenarios for providing a valid base URL.

 - When the base URL of the remote service is static and known, define the default base URL in the configuration file. Inject the client with CDI method.

 - When the base URL is not yet known and needs to be determined during the run time, set the base URL as a variable. Build the client with the more verbose **RestClientBuilder** method.

## Getting Started

If a terminal window does not open navigate:

> Terminal -> New Terminal

Check you are in the **home/project** folder:

```
pwd
```
{: codeblock}

The fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-microprofile-rest-client.git
cd guide-docker
```
{: codeblock}

The **start** directory contains the starting project that you will build upon.

## Try what you'll build 

The **finish** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the **finish** directory and run the following Maven goal to build the application and deploy it to Open Liberty:

```
cd finish
mvn liberty:run
```
{: codeblock}

After you see the following message, your application server is ready.

```
The defaultServer server is ready to run a smarter planet.
```
You can access the following microservices:


* This simulates the remote **system** service that retrieves the system property information for a specific host. In this case, **localhost** is a specific host name: 

```
curl http://localhost:9080/system/properties 
```
{: codeblock}

* This microservice is the **inventory** service that invokes the http://localhost:9080/system/properties microservice to retrieves the system property information: 

```
curl http://localhost:9080/inventory/systems/localhost 
```
{: codeblock}

* This microservice is the **inventory** service that invokes the \http://{your_hostname}:9080/system/properties microservice. In Windows, Mac OS, and Linux, get your fully qualified domain name (FQDN) by entering **hostname** from your terminal. Visit the URL by replacing **{your_hostname}** with your FQDN: 

```
http://localhost:9080/inventory/systems/{your_hostname}
```
{: codeblock}

You will see the same system property information, but the process of getting the information is different.

After you are finished checking out the application, stop the Open Liberty server by pressing **CTRL+C** in the shell session where you ran the server. Alternatively, you can run the **liberty:stop** goal from the finish directory in another shell session:

```
mvn liberty:stop
```
{: codeblock}

## Writing the RESTful client interface

Now, navigate to the **start** directory to begin.

```
cd ../start
```
{: codeblock}

When you run Open Liberty in dev mode, the server listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following goal to start in dev mode:

```
mvn liberty:dev
```
{: codeblock}

After you see the following message, your application server in dev mode is ready:

```
Press the Enter key to run tests on demand.
```
Dev mode holds your command line to listen for file changes. Open another command line to continue, or open the project in your editor.

The MicroProfile Rest Client API is included in the MicroProfile dependency specified by your **microprofile** artifact ID.

This dependency provides a library that is required to implement the MicroProfile Rest Client interface.

The **src/main/liberty/config/server.xml** file. This feature enables your Open Liberty server to use MicroProfile Rest Client to invoke RESTful microservices.

​
>[File -> Open] guide-microprofile-config/start/pom.xml
​
>[File -> Open] guide-microprofile-config/start/src/main/liberty/config/server.xml

The code for the **system** service in the **src/main/java/io/openliberty/guides/system** directory is provided for you. It simulates a remote RESTful service that the **inventory** service invokes.

Create a RESTful client interface for the **system** service. Write a template interface that maps the API of the remote **system** service.
The template interface describes the remote service that you want to access. The interface defines the resource to access as a method by mapping its annotations, return type, list of arguments, and exception declarations.

```
touch src/main/java/io/openliberty/guides/inventory/client/SystemClient.java
```

​
>[File -> Open] guide-microprofile-config/start/src/main/java/io/openliberty/guides/inventory/client/SystemClient.java
​

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

Notice the **AutoCloseable** interface.
This allows the user to explicitly close the client instance by invoking the **close()** method or to implicitly close the client instance using a try-with-resources block. When the client instance is closed, all underlying resources associated with the client instance are cleaned up. Refer to the [MicroProfile Rest Client specification](https://github.com/eclipse/microprofile-rest-client/releases) for more details.

When the **SystemClient** instance sends a GET request to the **<baseUrl>/properties** endpoint, where **<baseUrl>** is the default base URL of the **system** service. You will see how to configure the base URL in the next section.

The **@Produces** annotation specifies the media (MIME) type of the expected response. The default value is **MediaType.APPLICATION_JSON**.

The **@RegisterProvider** annotation tells the framework to register the provider classes to be used when the framework invokes the interface. You can add as many providers as necessary.
In the **UnknownUriException** exception.

### Handling exceptions through ResponseExceptionMappers

Error handling is an important step to ensure that the application can fail safely. If there is an error response such as **404 NOT FOUND** when invoking the remote service, you need to handle it. First, define an exception, and map the exception with the error response code. Then, register the exception mapper in the client interface.

Look at the client interface again, the **@RegisterProvider** annotation registers the **UnknownUriExceptionMapper** response exception mapper.
An exception mapper maps various response codes from the remote service to throwable exceptions.

Implement the actual exception class and the mapper class to see how this mechanism works.

```
touch src/main/java/io/openliberty/guides/inventory/client/UnknownUriException.java
```
>[File -> Open] guide-microprofile-config/start/src/main/java/io/openliberty/guides/inventory/client/UnknownUriException.java

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

```
touch src/main/java/io/openliberty/guides/inventory/client/UnknownUriExceptionMapper.java
```
{: codeblock}

>[File -> Open] guide-microprofile-config/start/src/main/java/io/openliberty/guides/inventory/client/UnknownUriExceptionMapper.java

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

## Injecting the client with dependency injection

Now, instantiate the **SystemClient** and use it in the **inventory** service. If you want to connect only with the default host name, you can easily instantiate the **SystemClient** with CDI annotations. CDI injection simplifies the process of bootstrapping the client.

First, you need to define the base URL of the **SystemClient** instance.
Configure the default base URL with the MicroProfile Config feature. This feature is enabled for you in the **server.xml** file.

```
touch src/main/webapp/META-INF/microprofile-config.properties
```

>[File -> Open] guide-microprofile-config/start/src/main/webapp/META-INF/microprofile-config.properties

```
systemClient/mp-rest/uri=http://localhost:9080/system
```

The **mp-rest/uri** base URL config property is configured to the default **\http://localhost:9080/system** URL.

This configuration is automatically picked up by the MicroProfile Config API.

Look at the annotations in the **SystemClient** interface again.

>[File -> Open] guide-microprofile-config/start/src/main/java/io/openliberty/guides/inventory/client/SystemClient.java

The **@RegisterRestClient** annotation registers the interface as a RESTful client. The runtime creates a CDI managed bean for every interface that is annotated with the **@RegisterRestClient** annotation.

The **microprofile-config.properties** configuration file.
For example, the **<fully-qualified classname>/mp-rest/uri** property becomes **systemClient/mp-rest/uri**.
The benefit of using Config Keys is when multiple client interfaces have the same **configKey** value, the interfaces can be configured with a single MP config property.

The **microprofile-config.properties** configuration file, which takes precedence. In a production environment, you can use the **baseUri** variable to specify a different URI for development and testing purposes.

The **@RegisterRestClient** annotation, which is a bean defining annotation implies that the interface is manageable through CDI. You must have this annotation in order to inject the client.

Inject the **InventoryManager** class, which is another CDI managed bean. Replace the **InventoryManager** class.


>[File -> Open] guide-microprofile-config/start/src/main/java/io/openliberty/guides/inventory/InventoryManager.java

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

  private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());

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
Because the **InventoryManager** class is **@ApplicationScoped**, and the **SystemClient** CDI bean maintains the same scope through the default dependent scope, the client is initialized once per application.

If the **hostname** parameter is **localhost**, the service runs the **getPropertiesWithDefaultHostName()** helper function to fetch system properties.
The helper function invokes the **system** service by calling the **defaultRestClient.getProperties()** method.

## Building the client with RestClientBuilder

The **inventory** service can also connect with a host other than the default **localhost** host, but you cannot configure a base URL that is not yet known.
In this case, set the host name as a variable and build the client by using the **RestClientBuilder** method. You can customize the base URL from the host name attribute.

Look at the **src/main/java/io/openliberty/guides/inventory/InventoryManager.java** file.

>[File -> Open] guide-microprofile-config/start/src/main/java/io/openliberty/guides/inventory/InventoryManager.java

The host name is provided as a parameter. This method first assembles the base URL that consists of the new host name.
Then, the method instantiates a **RestClientBuilder** builder with the new URL, registers the response exception mapper, and builds the **SystemClient** instance.

Similarly, call the **customRestClient.getProperties()** method to invoke the **system** service.

## Running the application 

The Open Liberty server was started in development mode at the beginning of the guide and all the changes were automatically picked up.

When the server is running, select either approach to fetch your system properties:

Visit the following URL: 
```
curl http://localhost:9080/inventory/systems/localhost
```
{: codeblock}

The URL retrieves the system property information for **localhost** host name by invoking the http://localhost:9080/system/properties service.

Get your FQDN first. Then, visit the \http://localhost:9080/inventory/systems/{your_hostname} URL by replacing **{your_hostname}** with your FQDN, which retrieves your system properties by invoking the \http://{your_hostname}:9080/system/properties service: 

```
curl http://localhost:9080/inventory/systems/{your_hostname}
```
{: codeblock}


## Testing the application

```
touch src/test/java/it/io/openliberty/guides/client/RestClientIT.java
```
{:codeblock}

>[File -> Open] guide-microprofile-config/start/src/test/java/it/io/openliberty/guides/client/RestClientIT.java

Each test case tests one of the methods for instantiating a RESTful client.

The **testDefaultLocalhost()** test fetches and compares system properties from the \http://localhost:9080/inventory/systems/localhost URL.

The **testRestClientBuilder()** test gets your IP address. Then, use your IP address as the host name to fetch your system properties and compare them.

In addition, a few endpoint tests are provided for you to test the basic functionality of the **inventory** and **system** services. If a test failure occurs, you might have introduced a bug into the code.

### Running the tests
Since you started Open Liberty in dev mode, press the enter/return key to run the tests.

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.377 sec - in it.io.openliberty.guides.system.SystemEndpointIT
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.379 sec - in it.io.openliberty.guides.inventory.InventoryEndpointIT
Running it.io.openliberty.guides.client.RestClientIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.121 sec - in it.io.openliberty.guides.client.RestClientIT

Results :

Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

To see whether the tests detect a failure, change the base URL in the configuration file so that when the **inventory** service tries to access the invalid URL, an **UnknownUriException** is thrown.
Rerun the tests to see a test failure occur.

# Summary

## Clean up your environment

Delete the **guide-microprofile-rest-client** project by navigating to the **/home/project/** directory

```
cd ../..
rm -r -f guide-microprofile-rest-client
rmdir guide-microprofile-rest-client
```
{: codeblock}

## Well Done

Nice work! You just invoked a remote service by using a template interface with MicroProfile Rest Client in Open Liberty.

