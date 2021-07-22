
# **Welcome to the Configuring microservices guide!**

Learn how to provide external configuration to microservices using MicroProfile Config.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




# **What you'll learn**
You will learn how to externalize and inject both static and dynamic configuration properties for microservices using MicroProfile Config.

You will learn to aggregate multiple configuration sources, assign prioritization values to these sources, merge configuration values, and create custom configuration sources.

The application that you will be working with is an **inventory** service which stores the information about various JVMs running on different hosts.
Whenever a request is made to the **inventory** service to retrieve the JVM
system properties of a particular host, the **inventory** service will communicate with the **system**
service on that host to get these system properties. You will add configuration properties to simulate if a service is down for maintenance.


# **Getting started**

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-config.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-microprofile-config.git
cd guide-microprofile-config
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.

<br/>
### **Try what you'll build**

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


Run the following curl command to test the availability of the **system** microservice and retrieve the system information:
```
curl -s http://localhost:9080/system/properties | jq
```
{: codeblock}

Run the following curl command to test the availability of the **inventory** microservice and 
retrieve the information for a list of all previously registered hosts:
```
curl -s http://localhost:9080/inventory/systems | jq
```
{: codeblock}

In addition, you can run the following curl command to access a third microservice, 
which retrieves and aggregates all of the configuration properties and sources that are added throughout this guide.
```
curl -s http://localhost:9080/config | jq
```
{: codeblock}

After you are finished checking out the application, stop the Open Liberty server by pressing **CTRL+C**
in the command-line session where you ran the server. Alternatively, you can run the **liberty:stop** goal
from the **finish** directory in another shell session:

```
mvn liberty:stop
```
{: codeblock}


# **Ordering multiple configuration sources**


To begin, run the following command to navigate to the **start** directory:
```
cd /home/project/guide-microprofile-config/start
```
{: codeblock}


When you run Open Liberty in development mode, known as dev mode, the server listens for file changes and automatically recompiles and 
deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```
mvn liberty:dev
```
{: codeblock}


After you see the following message, your application server in dev mode is ready:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, 
or open the project in your editor.

MicroProfile Config combines configuration properties from multiple sources, each known as a ConfigSource. Each ConfigSource has a specified priority, defined by its **`config_ordinal`** value.

A higher ordinal value means that the values taken from this ConfigSource will override values from ConfigSources with a lower ordinal value.

The following four sources are the default configuration sources:

* A **`<variable name="..." value="..."/>`** element in the server.xml file has a default ordinal of 500.
* System properties has a default ordinal of 400. (e.g. **bootstrap.properties** file)
* Environment variables have a default ordinal of 300. (e.g. **server.env** file)
* The **META-INF/microprofile-config.properties** configuration property file on the classpath has a default ordinal of 100.

Access the **src/main/resources/META-INF/microprofile-config.properties** local configuration file. This configuration file is the default configuration source for an application that uses MicroProfile Config.


# **Injecting static configuration**

The MicroProfile Config API is included in the MicroProfile dependency that is specified in your **pom.xml** file. Look for the dependency with the **microprofile** artifact ID. This dependency provides a library that allows you to use the MicroProfile Config API to externalize configurations for your microservices.
The **mpConfig** feature is also enabled in the **src/main/liberty/config/server.xml** file.



Now navigate to the **src/main/resources/META-INF/microprofile-config.properties** local configuration file to check some static configuration.
This configuration file is the default configuration source for an application that uses MicroProfile Config.

The **`io_openliberty_guides_port_number`** property that has already been defined in this file, determines the port number of the REST service.


To use this configuration property,
create the **InventoryConfig.java** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-config/start/src/main/java/io/openliberty/guides/inventory/InventoryConfig.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-config/start/src/main/java/io/openliberty/guides/inventory/InventoryConfig.java




```
package io.openliberty.guides.inventory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.openliberty.guides.config.Email;

@RequestScoped
public class InventoryConfig {

  @Inject
  @ConfigProperty(name = "io_openliberty_guides_port_number")
  private int portNumber;

  private Provider<Boolean> inMaintenance;


  public int getPortNumber() {
    return portNumber;
  }


}
```
{: codeblock}



Inject the **`io_openliberty_guides_port_number`** property, and add the **getPortNumber()** class method to the **InventoryConfig.java** file.

The **@Inject** annotation injects the port number directly, the injection value is static and fixed on application starting.

The **getPortNumber()** method directly returns the value of **portNumber** because it has been injected.

# **Injecting dynamic configuration**

Note that three default config sources mentioned above are static and fixed on application starting, so the properties within them cannot be modified while the server is running.
However, you can externalize configuration data out of the application package, through the creation of custom configuration sources, so that the service updates configuration changes dynamically.

<br/>
### **Creating custom configuration sources**

Custom configuration sources can be created by implementing the **org.eclipse.microprofile.config.spi.ConfigSource** interface and using the **java.util.ServiceLoader** mechanism.

A **CustomConfigSource.json** JSON file has already been created in the **resources** directory. This JSON file simulates a remote configuration resource in real life.
This file contains 4 custom config properties and has an ordinal of **150**.
To use these properties in the application, the data object needs to be transformed from this JSON file to the configuration for your application.

To link this JSON file to your application and to implement the **ConfigSource** interface,

create the **CustomConfigSource** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-config/start/src/main/java/io/openliberty/guides/config/CustomConfigSource.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-config/start/src/main/java/io/openliberty/guides/config/CustomConfigSource.java




```
package io.openliberty.guides.config;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.Json;
import java.math.BigDecimal;
import java.util.*;
import java.io.StringReader;

import java.io.BufferedReader;
import java.io.FileReader;
import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * User-provided ConfigSources are dynamic.
 * The getProperties() method will be periodically invoked by the runtime
 * to retrieve up-to-date values. The frequency is controlled by
 * the microprofile.config.refresh.rate Java system property,
 * which is in milliseconds and can be customized.
 */
public class CustomConfigSource implements ConfigSource {

  String fileLocation = System.getProperty("user.dir").split("target")[0]
      + "resources/CustomConfigSource.json";

  @Override
  public int getOrdinal() {
    return Integer.parseInt(getProperties().get("config_ordinal"));
  }

  @Override
  public Set<String> getPropertyNames() {
    return getProperties().keySet();
  }

  @Override
  public String getValue(String key) {
    return getProperties().get(key);
  }

  @Override
  public String getName() {
    return "Custom Config Source: file:" + this.fileLocation;
  }

  public Map<String, String> getProperties() {
    Map<String, String> m = new HashMap<String, String>();
    String jsonData = this.readFile(this.fileLocation);
    JsonParser parser = Json.createParser(new StringReader(jsonData));
    String key = null;
    while (parser.hasNext()) {
      final Event event = parser.next();
      switch (event) {
      case KEY_NAME:
        key = parser.getString();
        break;
      case VALUE_STRING:
        String string = parser.getString();
        m.put(key, string);
        break;
      case VALUE_NUMBER:
        BigDecimal number = parser.getBigDecimal();
        m.put(key, number.toString());
        break;
      case VALUE_TRUE:
        m.put(key, "true");
        break;
      case VALUE_FALSE:
        m.put(key, "false");
        break;
      default:
        break;
      }
    }
    parser.close();
    return m;
  }

  public String readFile(String fileName) {
    String result = "";
    try {
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();
      while (line != null) {
        sb.append(line);
        line = br.readLine();
      }
      result = sb.toString();
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }
}
```
{: codeblock}



The **getProperties()** method reads the key value pairs from the **resources/CustomConfigSource.json** JSON file and writes the information into a map.

Finally, register the custom configuration source.

Create the configuration file.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-config/start/src/main/resources/META-INF/services/org.eclipse.microprofile.config.spi.ConfigSource
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-config/start/src/main/resources/META-INF/services/org.eclipse.microprofile.config.spi.ConfigSource




```
io.openliberty.guides.config.CustomConfigSource
```
{: codeblock}





<br/>
### **Enabling dynamic configuration injection**

Now that the custom configuration source has successfully been set up, you can enable dynamic configuration injection of the properties being set in this ConfigSource.
To enable this dynamic injection,

replace the **InventoryConfig.java** class.

> From the menu of the IDE, select   
> **File** > **Open** > guide-microprofile-config/start/src/main/java/io/openliberty/guides/inventory/InventoryConfig.java




```
package io.openliberty.guides.inventory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.openliberty.guides.config.Email;

@RequestScoped
public class InventoryConfig {

  @Inject
  @ConfigProperty(name = "io_openliberty_guides_port_number")
  private int portNumber;

  @Inject
  @ConfigProperty(name = "io_openliberty_guides_inventory_inMaintenance")
  private Provider<Boolean> inMaintenance;


  public int getPortNumber() {
    return portNumber;
  }

  public boolean isInMaintenance() {
    return inMaintenance.get();
  }

}
```
{: codeblock}


Inject the **`io_openliberty_guides_inventory_inMaintenance`** property, and add the **isInMaintenance()** class method.

The **@Inject** and **@ConfigProperty** annotations inject the **`io_openliberty_guides_inventory_inMaintenance`** configuration property from the **CustomConfigSource.json** file.
The **Provider<>** interface used, forces the service to retrieve the inMaintenance value just in time. This retrieval of the value just in time makes the config injection dynamic and able to change without having to restart the application.

Every time that you invoke the **inMaintenance.get()** method, the **Provider<>** interface picks up the
latest value of the **`io_openliberty_guides_inventory_inMaintenance`** property from configuration sources.


# **Creating custom converters**
Configuration values are purely Strings. MicroProfile Config API has built-in converters that automatically converts configured Strings into target types such as **int**, **Integer**, **boolean**, **Boolean**, **float**, **Float**, **double** and **Double**.
Therefore, in the previous section, it is type-safe to directly set the variable type to **Provider<Boolean>**.

To convert configured Strings to an arbitrary class type, such as the **Email** class type,
replace the **Email** Class.

> From the menu of the IDE, select   
> **File** > **Open** > guide-microprofile-config/start/src/main/java/io/openliberty/guides/config/Email.java




```

package io.openliberty.guides.config;

public class Email {
  private String name;
  private String domain;

  public Email(String value) {
    String[] components = value.split("@");
    if (components.length == 2) {
      name = components[0];
      domain = components[1];
    }
  }

  public String getEmailName() {
    return name;
  }

  public String getEmailDomain() {
    return domain;
  }

  public String toString() {
    return name + "@" + domain;
  }
}
```
{: codeblock}



To use this **Email** class type, add a custom converter by implementing the generic interface **org.eclipse.microprofile.config.spi.Converter<T>**.
The Type parameter of the interface is the target type the String is converted to.

Create the **CustomEmailConverter** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-config/start/src/main/java/io/openliberty/guides/config/CustomEmailConverter.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-config/start/src/main/java/io/openliberty/guides/config/CustomEmailConverter.java




```
package io.openliberty.guides.config;

import org.eclipse.microprofile.config.spi.Converter;
import io.openliberty.guides.config.Email;

public class CustomEmailConverter implements Converter<Email> {

  @Override
  public Email convert(String value) {
    return new Email(value);
  }

}
```
{: codeblock}



This implements the **Converter<T>** interface.

To register your implementation,
create the configuration file.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-config/start/src/main/resources/META-INF/services/org.eclipse.microprofile.config.spi.Converter
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-config/start/src/main/resources/META-INF/services/org.eclipse.microprofile.config.spi.Converter




```
io.openliberty.guides.config.CustomEmailConverter
```
{: codeblock}



To use the custom **Email** converter,
replace the **InventoryConfig** class.

> From the menu of the IDE, select   
> **File** > **Open** > guide-microprofile-config/start/src/main/java/io/openliberty/guides/inventory/InventoryConfig.java




```
package io.openliberty.guides.inventory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.openliberty.guides.config.Email;

@RequestScoped
public class InventoryConfig {

  @Inject
  @ConfigProperty(name = "io_openliberty_guides_port_number")
  private int portNumber;

  @Inject
  @ConfigProperty(name = "io_openliberty_guides_inventory_inMaintenance")
  private Provider<Boolean> inMaintenance;

  @Inject
  @ConfigProperty(name = "io_openliberty_guides_email")
  private Provider<Email> email;

  public int getPortNumber() {
    return portNumber;
  }

  public boolean isInMaintenance() {
    return inMaintenance.get();
  }

  public Email getEmail() {
    return email.get();
  }
}
```
{: codeblock}


Inject the **`io_openliberty_guides_email`** property, and add the **getEmail()** method.

# **Adding configuration to the microservice**

To use externalized configuration in the **inventory** service,
replace the **InventoryResource** class.

> From the menu of the IDE, select   
> **File** > **Open** > guide-microprofile-config/start/src/main/java/io/openliberty/guides/inventory/InventoryResource.java




```
package io.openliberty.guides.inventory;

import java.util.Properties;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.openliberty.guides.inventory.InventoryConfig;

@RequestScoped
@Path("systems")
public class InventoryResource {

  @Inject
  InventoryManager manager;

  @Inject
  InventoryConfig inventoryConfig;

  @GET
  @Path("{hostname}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPropertiesForHost(@PathParam("hostname") String hostname) {

    if (!inventoryConfig.isInMaintenance()) {
      Properties props = manager.get(hostname, inventoryConfig.getPortNumber());
      if (props == null) {
        return Response.status(Response.Status.NOT_FOUND)
                       .entity("{ \"error\" : \"Unknown hostname or the system service " 
                       + "may not be running on " + hostname + "\" }")
                       .build();
      }

      manager.add(hostname, props);
      return Response.ok(props).build();
    } else {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                     .entity("{ \"error\" : \"Service is currently in maintenance. " 
                     + "Contact: " + inventoryConfig.getEmail().toString() + "\" }")
                     .build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response listContents() {
    if (!inventoryConfig.isInMaintenance()) {
      return Response.ok(manager.list()).build();
    } else {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                     .entity("{ \"error\" : \"Service is currently in maintenance. " 
                     + "Contact: " + inventoryConfig.getEmail().toString() + "\" }")
                     .build();
    }
  }

}

```
{: codeblock}


To add configuration to the **inventory** service, the **InventoryConfig** object is injected to the existing class.

The port number from the configuration is retrieved by the **inventoryConfig.getPortNumber()** method and passed to the **manager.get()** method as a parameter.

To determine whether the inventory service is in maintenance or not (according to the configuration value), **inventoryConfig.isInMaintenance()** class method is used.
If you set the **`io_openliberty_guides_inventory_inMaintenance`** property to **true** in the configuration, the inventory service returns the message, **ERROR: Service is currently in maintenance**, along with the contact email.
The email configuration value can be obtained by calling **inventoryConfig.getEmail()** method.




# **Running the application**

You started the Open Liberty server in dev mode at the beginning of the guide, so all the changes were automatically picked up.


While the server is running, run the following curl command to access the **system** microservice:
```
curl -s http://localhost:9080/system/properties | jq
```
{: codeblock}

and run the following curl command to access the **inventory** microservice:
```
curl -s http://localhost:9080/inventory/systems | jq
```
{: codeblock}

You can find the service that retrieves configuration information that is specific to this guide by running the following curl command:
```
curl -s http://localhost:9080/config | jq
```
{: codeblock}

The **`config_ordinal`** value of the custom configuration source is set to **150**. It overrides configuration values of the default **microprofile-config.properties** source, which has a **`config_ordinal`** value of **100**.




Play with this application by changing configuration values for each property in the **resources/CustomConfigSource.json** file.
Your changes are added dynamically, and you do not need to restart the server. Rerun the following curl command to see the dynamic changes:
```
curl -s http://localhost:9080/config | jq
```
{: codeblock}

For example, change **`io_openliberty_guides_inventory_inMaintenance`** from **false** to **true**, then try to access http://localhost:9080/inventory/systems again by running the following curl command:
```
curl -s http://localhost:9080/inventory/systems | jq
```
{: codeblock}

The following message displays: **ERROR: Service is currently in maintenance**.



# **Testing the application**

Create the **ConfigurationIT** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-config/start/src/test/java/it/io/openliberty/guides/config/ConfigurationIT.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-config/start/src/test/java/it/io/openliberty/guides/config/ConfigurationIT.java




```
package it.io.openliberty.guides.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class ConfigurationIT {

  private String port;
  private String baseUrl;
  private Client client;

  private final String INVENTORY_HOSTS = "inventory/systems";
  private final String USER_DIR = System.getProperty("user.dir");
  private final String DEFAULT_CONFIG_FILE = USER_DIR
      + "/src/main/resources/META-INF/microprofile-config.properties";
  private final String CUSTOM_CONFIG_FILE = USER_DIR.split("target")[0]
      + "/resources/CustomConfigSource.json";
  private final String INV_MAINTENANCE_PROP = "io_openliberty_guides"
      + "_inventory_inMaintenance";

  @BeforeEach
  public void setup() {
    port = System.getProperty("default.http.port");
    baseUrl = "http://localhost:" + port + "/";
    ConfigITUtil.setDefaultJsonFile(CUSTOM_CONFIG_FILE);

    client = ClientBuilder.newClient();
    client.register(JsrJsonpProvider.class);
  }

  @AfterEach
  public void teardown() {
    ConfigITUtil.setDefaultJsonFile(CUSTOM_CONFIG_FILE);
    client.close();
  }

  @Test
  @Order(1)
  public void testInitialServiceStatus() {
    boolean status = Boolean.valueOf(ConfigITUtil.readPropertyValueInFile(
        INV_MAINTENANCE_PROP, DEFAULT_CONFIG_FILE));
    if (!status) {
      Response response = ConfigITUtil.getResponse(client, baseUrl + INVENTORY_HOSTS);

      int expected = Response.Status.OK.getStatusCode();
      int actual = response.getStatus();
      assertEquals(expected, actual);
    } else {
      assertEquals(
        "{ \"error\" : \"Service is currently in maintenance. Contact: admin@guides.openliberty.io\" }",
          ConfigITUtil.getStringFromURL(client, baseUrl + INVENTORY_HOSTS),
          "The Inventory Service should be in maintenance");
    }
  }

  @Test
  @Order(2)
  public void testPutServiceInMaintenance() {
    Response response = ConfigITUtil.getResponse(client, baseUrl + INVENTORY_HOSTS);

    int expected = Response.Status.OK.getStatusCode();
    int actual = response.getStatus();
    assertEquals(expected, actual);

    ConfigITUtil.switchInventoryMaintenance(CUSTOM_CONFIG_FILE, true);

    String error = ConfigITUtil.getStringFromURL(client, baseUrl + INVENTORY_HOSTS);

    assertEquals(
      "{ \"error\" : \"Service is currently in maintenance. Contact: admin@guides.openliberty.io\" }",
        error, "The inventory service should be down in the end");
  }

  @Test
  @Order(3)
  public void testChangeEmail() {
    ConfigITUtil.switchInventoryMaintenance(CUSTOM_CONFIG_FILE, true);

    String error = ConfigITUtil.getStringFromURL(client, baseUrl + INVENTORY_HOSTS);

    assertEquals(
      "{ \"error\" : \"Service is currently in maintenance. Contact: admin@guides.openliberty.io\" }",
        error, "The email should be admin@guides.openliberty.io in the beginning");

    ConfigITUtil.changeEmail(CUSTOM_CONFIG_FILE, "service@guides.openliberty.io");

    error = ConfigITUtil.getStringFromURL(client, baseUrl + INVENTORY_HOSTS);

    assertEquals(
      "{ \"error\" : \"Service is currently in maintenance. Contact: service@guides.openliberty.io\" }",
        error, "The email should be service@guides.openliberty.io in the beginning");
  }

}
```
{: codeblock}





The **testInitialServiceStatus()** test case reads the value of the **`io_openliberty_guides_inventory_inMaintenance`** configuration property in the **META-INF/microprofile-config.properties** file and checks the HTTP response of the inventory service.
If the configuration value is **false**, the service returns a valid response. Otherwise, the service returns the following message: **ERROR: Service is currently in maintenance**.

Because the **`io_openliberty_guides_inventory_inMaintenance`** configuration property is set to **false** by default, the **testPutServiceInMaintenance()** test case first checks that the inventory service is not in maintenance in the beginning.
Next, this test switches the value of the **`io_openliberty_guides_inventory_inMaintenance`** configuration property to **true**.
In the end, the inventory service returns the following message: **ERROR: Service is currently in maintenance**.

The **testChangeEmail()** test case first puts the **inventory** service in maintenance, then it changes the email address in the configuration file. In the end, the **inventory** service should display the error message with the latest email address.

In addition, a few endpoint tests have been provided for you to test the basic functionality of the **inventory** and **system** services. If a test failure occurs, then you must have introduced a bug into the code.
Remember that you must register the custom configuration source and custom converter in the **src/main/resources/META-INF/services/** directory. If you don't complete these steps, the tests will fail. These tests run automatically as a part of the integration test suite.


<br/>
### **Running the tests**

Because you started Open Liberty in dev mode, press the **enter/return** key to run the tests.

You see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.config.ConfigurationIT
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.92 s - in it.io.openliberty.guides.config.ConfigurationIT
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.017 s - in it.io.openliberty.guides.system.SystemEndpointIT
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
[WARNING ] Interceptor for {http://client.inventory.guides.openliberty.io/}SystemClient has thrown exception, unwinding now
Could not send Message.
[err] The specified host is unknown.
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.077 s - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results:

Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

The warning and error messages are expected and result from a request to a bad or an unknown hostname. This request is made in the **testUnknownHost()** test from the **InventoryEndpointIT** integration test.

To see whether the tests detect a failure, remove the configuration resetting line in the **setup()** method of the **ConfigurationIT.java** file.
Then, manually change some configuration values in the **resources/CustomConfigSource.json** file.
Rerun the tests. You will see a test failure occur.

When you are done checking out the service, exit dev mode by pressing **CTRL+C** in the command-line session
where you ran the server, or by typing **q** and then pressing the **enter/return** key.


# **Summary**

## **Nice Work!**

You just built and tested a MicroProfile application with MicroProfile Config in Open Liberty.


Feel free to try one of the related guides. They demonstrate new technologies that you can learn and
expand on top what you built in this guide.


<br/>
## **Clean up your environment**


Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-microprofile-config** project by running the following commands:

```
cd /home/project
rm -fr guide-microprofile-config
```
{: codeblock}

<br/>
## **What did you think of this guide?**

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Configuring%20microservices&guide-id=cloud-hosted-guide-microprofile-config)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

<br/>
## **What could make this guide better?**

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-config/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-config/pulls)



<br/>
## **Where to next?**

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Separating configuration from code in microservices](https://openliberty.io/guides/microprofile-config-intro.html)


<br/>
## **Log out of the session**

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.