---
markdown-version: v1
title: instructions
branch: lab-363-instruction
version-history-start-date: 2022-02-11T18:24:15Z
tool-type: theia
---
::page{title="Welcome to the Consuming RESTful services with template interfaces guide!"}

Learn how to use MicroProfile Rest Client to invoke RESTful microservices over HTTP in a type-safe way.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You will learn how to build a MicroProfile Rest Client to access remote RESTful services. You will create a template interface that maps to the remote service that you want to call. MicroProfile Rest Client automatically generates a client instance based on what is defined and annotated in the template interface. Thus, you don't have to worry about all of the boilerplate code, such as setting up a client class, connecting to the remote server, or invoking the correct URI with the correct parameters.

The application that you will be working with is an ***inventory*** service, which fetches and stores the system property information for different hosts. Whenever a request is made to retrieve the system properties of a particular host, the ***inventory*** service will create a client to invoke the ***system*** service on that host. The ***system*** service simulates a remote service in the application.

You will instantiate the client and use it in the ***inventory*** service. You can choose from two different approaches, [Context and Dependency Injection (CDI)](https://openliberty.io/docs/latest/cdi-beans.html) with the help of MicroProfile Config or the [RestClientBuilder](https://openliberty.io/blog/2018/01/31/mpRestClient.html) method. In this guide, you will explore both methods to handle scenarios for providing a valid base URL.

 * When the base URL of the remote service is static and known, define the default base URL in the configuration file. Inject the client with a CDI method.

 * When the base URL is not yet known and needs to be determined during the run time, set the base URL as a variable. Build the client with the more verbose ***RestClientBuilder*** method.


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-rest-client.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microprofile-rest-client.git
cd guide-microprofile-rest-client
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the ***finish*** directory and run the following Maven goal to build the application and deploy it to Open Liberty:

```bash
cd finish
mvn liberty:run
```

After you see the following message, your application server is ready:

```
The defaultServer server is ready to run a smarter planet.
```



Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.


The ***system*** microservice simulates a service that returns the system property information for the host. The ***system*** service is accessible at the ***http\://localhost:9080/system/properties*** URL. In this case, ***localhost*** is the host name.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/system/properties | jq
```




The ***inventory*** microservice makes a request to the ***system*** microservice and stores the system property information.  To fetch and store your system information, visit the ***http\://localhost:9080/inventory/systems/localhost*** URL.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/inventory/systems/localhost | jq
```




You can also use the ***http://localhost:9080/inventory/systems/{your-hostname}*** URL. In Windows, MacOS, and Linux, get your fully qualified domain name (FQDN) by entering **hostname** into your command-line. Visit the URL by replacing ***{your-hostname}*** with your FQDN.


After you are finished checking out the application, stop the Open Liberty server by pressing `Ctrl+C` in the command-line session where you ran the server. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```

::page{title="Writing the RESTful client interface"}

Now, navigate to the ***start*** directory to begin.

When you run Open Liberty in development mode, known as dev mode, the server listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```bash
mvn liberty:dev
```

After you see the following message, your application server in dev mode is ready:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

The MicroProfile Rest Client API is included in the MicroProfile dependency specified by your ***pom.xml*** file. Look for the dependency with the ***microprofile*** artifact ID.


This dependency provides a library that is required to implement the MicroProfile Rest Client interface.

The ***mpRestClient*** feature is also enabled in the ***src/main/liberty/config/server.xml*** file. This feature enables your Open Liberty server to use MicroProfile Rest Client to invoke RESTful microservices.


The code for the ***system*** service in the ***src/main/java/io/openliberty/guides/system*** directory is provided for you. It simulates a remote RESTful service that the ***inventory*** service invokes.

Create a RESTful client interface for the ***system*** service. Write a template interface that maps the API of the remote ***system*** service. The template interface describes the remote service that you want to access. The interface defines the resource to access as a method by mapping its annotations, return type, list of arguments, and exception declarations.

Create the ***SystemClient*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/client/SystemClient.java
```


> Then, to open the SystemClient.java file in your IDE, select
> **File** > **Open** > guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/client/SystemClient.java, or click the following button

::openFile{path="/home/project/guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/client/SystemClient.java"}



```java
package io.openliberty.guides.inventory.client;

import java.util.Properties;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "systemClient",
                     baseUri = "http://localhost:9080/system")
@RegisterProvider(UnknownUriExceptionMapper.class)
@Path("/properties")
public interface SystemClient extends AutoCloseable {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Properties getProperties() throws UnknownUriException, ProcessingException;
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The MicroProfile Rest Client feature automatically builds and generates a client implementation based on what is defined in the ***SystemClient*** interface. There is no need to set up the client and connect with the remote service.

Notice the ***SystemClient*** interface inherits the ***AutoCloseable*** interface. This allows the user to explicitly close the client instance by invoking the ***close()*** method or to implicitly close the client instance using a try-with-resources block. When the client instance is closed, all underlying resources associated with the client instance are cleaned up. Refer to the [MicroProfile Rest Client specification](https://github.com/eclipse/microprofile-rest-client/releases) for more details.

When the ***getProperties()*** method is invoked, the ***SystemClient*** instance sends a GET request to the ***\<baseUrl\>/properties*** endpoint, where ***\<baseUrl\>*** is the default base URL of the ***system*** service. You will see how to configure the base URL in the next section.

The ***@Produces*** annotation specifies the media (MIME) type of the expected response. The default value is ***MediaType.APPLICATION_JSON***.

The ***@RegisterProvider*** annotation tells the framework to register the provider classes to be used when the framework invokes the interface. You can add as many providers as necessary. In the ***SystemClient*** interface, add a response exception mapper as a provider to map the ***404*** response code with the ***UnknownUriException*** exception.

### Handling exceptions through ResponseExceptionMappers

Error handling is an important step to ensure that the application can fail safely. If there is an error response such as ***404 NOT FOUND*** when invoking the remote service, you need to handle it. First, define an exception, and map the exception with the error response code. Then, register the exception mapper in the client interface.

Look at the client interface again, the ***@RegisterProvider*** annotation registers the ***UnknownUriExceptionMapper*** response exception mapper. An exception mapper maps various response codes from the remote service to throwable exceptions.


Implement the actual exception class and the mapper class to see how this mechanism works.

Create the ***UnknownUriException*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/client/UnknownUriException.java
```


> Then, to open the UnknownUriException.java file in your IDE, select
> **File** > **Open** > guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/client/UnknownUriException.java, or click the following button

::openFile{path="/home/project/guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/client/UnknownUriException.java"}



```java
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



Now, link the ***UnknownUriException*** class with the corresponding response code through a ***ResponseExceptionMapper*** mapper class.

Create the ***UnknownUriExceptionMapper*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/client/UnknownUriExceptionMapper.java
```


> Then, to open the UnknownUriExceptionMapper.java file in your IDE, select
> **File** > **Open** > guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/client/UnknownUriExceptionMapper.java, or click the following button

::openFile{path="/home/project/guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/client/UnknownUriExceptionMapper.java"}



```java
package io.openliberty.guides.inventory.client;

import java.util.logging.Logger;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
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



The ***handles()*** method inspects the HTTP response code to determine whether an exception is thrown for the specific response, and the ***toThrowable()*** method returns the mapped exception.

::page{title="Injecting the client with dependency injection"}

Now, instantiate the ***SystemClient*** interface and use it in the ***inventory*** service. If you want to connect only with the default host name, you can easily instantiate the ***SystemClient*** with CDI annotations. CDI injection simplifies the process of bootstrapping the client.

First, you need to define the base URL of the ***SystemClient*** instance. Configure the default base URL with the MicroProfile Config feature. This feature is enabled for you in the ***server.xml*** file.

Create the configuration file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-rest-client/start/src/main/webapp/META-INF/microprofile-config.properties
```


> Then, to open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > guide-microprofile-rest-client/start/src/main/webapp/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/guide-microprofile-rest-client/start/src/main/webapp/META-INF/microprofile-config.properties"}



```
systemClient/mp-rest/uri=http://localhost:9080/system
```



The ***mp-rest/uri*** base URL config property is configured to the default ***http://localhost:9080/system*** URL.

This configuration is automatically picked up by the MicroProfile Config API.

Look at the annotations in the ***SystemClient*** interface again.


The ***@RegisterRestClient*** annotation registers the interface as a RESTful client. The runtime creates a CDI managed bean for every interface that is annotated with the ***@RegisterRestClient*** annotation.

The ***configKey*** value in the ***@RegisterRestClient*** annotation replaces the fully-qualified classname of the properties in the ***microprofile-config.properties*** configuration file. For example, the ***\<fully-qualified classname\>/mp-rest/uri*** property becomes ***systemClient/mp-rest/uri***. The benefit of using Config Keys is when multiple client interfaces have the same ***configKey*** value, the interfaces can be configured with a single MP config property.

The ***baseUri*** value can also be set in the ***@RegisterRestClient*** annotation. However, this value will be overridden by the base URI property defined in the ***microprofile-config.properties*** configuration file, which takes precedence. In a production environment, you can use the ***baseUri*** variable to specify a different URI for development and testing purposes.

The ***@RegisterRestClient*** annotation, which is a bean defining annotation implies that the interface is manageable through CDI. You must have this annotation in order to inject the client.

Inject the ***SystemClient*** interface into the ***InventoryManager*** class, which is another CDI managed bean.

Replace the ***InventoryManager*** class.

> To open the InventoryManager.java file in your IDE, select
> **File** > **Open** > guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/InventoryManager.java, or click the following button

::openFile{path="/home/project/guide-microprofile-rest-client/start/src/main/java/io/openliberty/guides/inventory/InventoryManager.java"}



```java
package io.openliberty.guides.inventory;

import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;

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
    if (!systems.contains(host)) {
      systems.add(host);
    }
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



***@Inject*** and ***@RestClient*** annotations inject an instance of the ***SystemClient*** called ***defaultRestClient*** to the ***InventoryManager*** class.

Because the ***InventoryManager*** class is ***@ApplicationScoped***, and the ***SystemClient*** CDI bean maintains the same scope through the default dependent scope, the client is initialized once per application.

If the ***hostname*** parameter is ***localhost***, the service runs the ***getPropertiesWithDefaultHostName()*** helper function to fetch system properties. The helper function invokes the ***system*** service by calling the ***defaultRestClient.getProperties()*** method.


::page{title="Building the client with RestClientBuilder"}

The ***inventory*** service can also connect with a host other than the default ***localhost*** host, but you cannot configure a base URL that is not yet known. In this case, set the host name as a variable and build the client by using the ***RestClientBuilder*** method. You can customize the base URL from the host name attribute.

Look at the ***getPropertiesWithGivenHostName()*** method in the ***src/main/java/io/openliberty/guides/inventory/InventoryManager.java*** file.


The host name is provided as a parameter. This method first assembles the base URL that consists of the new host name. Then, the method instantiates a ***RestClientBuilder*** builder with the new URL, registers the response exception mapper, and builds the ***SystemClient*** instance.

Similarly, call the ***customRestClient.getProperties()*** method to invoke the ***system*** service.


::page{title="Running the application"}

You started the Open Liberty server in dev mode at the beginning of the guide, so all the changes were automatically picked up.

When the server is running, select either approach to fetch your system properties:


 Visit the ***http\://localhost:9080/inventory/systems/localhost*** URL. The URL retrieves the system property information for the ***localhost*** host name by making a request to the ***system*** service at ***http://localhost:9080/system/properties***.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/inventory/systems/localhost | jq
```




Or, get your FQDN first. Then, visit the ***http://localhost:9080/inventory/systems/{your-hostname}*** URL by replacing ***{your-hostname}*** with your FQDN, which retrieves your system properties by making a request to the ***system*** service at ***http://{your-hostname}:9080/system/properties***.


::page{title="Testing the application"}

Create the ***RestClientIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-rest-client/start/src/test/java/it/io/openliberty/guides/client/RestClientIT.java
```


> Then, to open the RestClientIT.java file in your IDE, select
> **File** > **Open** > guide-microprofile-rest-client/start/src/test/java/it/io/openliberty/guides/client/RestClientIT.java, or click the following button

::openFile{path="/home/project/guide-microprofile-rest-client/start/src/test/java/it/io/openliberty/guides/client/RestClientIT.java"}



```java
package it.io.openliberty.guides.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.WebTarget;
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
    try {
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



Each test case tests one of the methods for instantiating a RESTful client.

The ***testDefaultLocalhost()*** test fetches and compares system properties from the ***http\://localhost:9080/inventory/systems/localhost*** URL.

The ***testRestClientBuilder()*** test gets your IP address. Then, use your IP address as the host name to fetch your system properties and compare them.

In addition, a few endpoint tests are provided for you to test the basic functionality of the ***inventory*** and ***system*** services. If a test failure occurs, you might have introduced a bug into the code.


### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

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

The warning and error messages are expected and result from a request to a bad or an unknown hostname. This request is made in the ***testUnknownHost()*** test from the ***InventoryEndpointIT*** integration test.

To see whether the tests detect a failure, change the base URL in the configuration file so that when the ***inventory*** service tries to access the invalid URL, an ***UnknownUriException*** is thrown. Rerun the tests to see a test failure occur.

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran the server, or by typing ***q*** and then pressing the ***enter/return*** key.

::page{title="Summary"}

### Nice Work!

You just invoked a remote service by using a template interface with MicroProfile Rest Client in Open Liberty.


MicroProfile Rest Client also provides a uniform way to configure SSL for the client. You can learn more in the [Hostname verification with SSL on Open Liberty and MicroProfile Rest Client](https://openliberty.io/blog/2019/06/21/microprofile-rest-client-19006.html#ssl) blog and the [MicroProfile Rest Client specification](https://github.com/eclipse/microprofile-rest-client/releases).

Feel free to try one of the related guides where you can learn more technologies and expand on what you built here.


### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-rest-client*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-rest-client
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Consuming%20RESTful%20services%20with%20template%20interfaces&guide-id=cloud-hosted-guide-microprofile-rest-client)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-rest-client/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-rest-client/pulls)



### Where to next?

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Configuring microservices](https://openliberty.io/guides/microprofile-config.html)
* [Consuming RESTful services asynchronously with template interfaces](https://openliberty.io/guides/microprofile-rest-client-async.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
