# Creating a RESTful web service

### What you will learn

You will learn how to build and test a simple REST service with JAX-RS and JSON-B, which will expose the JVM's system properties. The REST service will respond to **GET** requests made to the **http://localhost:9080/LibertyProject/System/properties** URL.

The service responds to a **GET** request with a JSON representation of the system properties, where each property is a field  in a JSON object like this:
```JSON
{
  "os.name":"Mac",
  "java.version": "1.8"
}
```
### Introduction

When you create a new REST application, the design of the API is important. The JAX-RS APIs can be used to create JSON-RPC, or XML-RPC APIs, but it wouldn't be a RESTful service. A good RESTful service is designed around the resources that are exposed, and on how to create, read, update, and delete the resources.

The service responds to **GET** requests to the **/System/properties** path. The **GET** request should return a **200 OK** response that contains all of the JVM's system properties.

# Getting Started

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
git clone https://github.com/openliberty/guide-rest-intro.gi
cd guide-rest-intro
```
{: codeblock}

The **finish** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the **finish** directory and run the following Maven  goal to build the application and deploy it to Open Liberty:

```
cd finish
mvn liberty:run
```
{: codeblock}

Check out the service in another shell:

```
curl http://localhost:9080/LibertyProject/System/properties
```
{: codeblock}

After you are done checking out the application, stop the Open Liberty server by pressing **CTRL+C** in the shell session where you ran the server. Alternatively, you can run the **liberty:stop** goal  from the **finish** directory in another shell session:

```
mvn liberty:stop
```
{: codeblock}

# Creating a JAX-RS application

Navigate to the **start** directory to begin.

Start Open Liberty in development mode, which starts the Open Liberty server and listens 
for file changes:

```
cd ../start
mvn liberty:dev
```
{: codeblock}

JAX-RS has two key concepts for creating REST APIs. The most obvious one is the resource itself, which is modelled as a class. The second is a JAX-RS application, which groups all exposed resources under a common path. You can think of the JAX-RS application as a wrapper for all of your resources.

Replace the **SystemApplication** class:

> [File -> Open]src/main/java/io/openliberty/guides/rest/SystemApplication.java

```java
package io.openliberty.guides.rest;

import javax.ws.rs.core.Application;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("System")
public class SystemApplication extends Application {

}
```
{: codeblock}

The **SystemApplication** class extends the **Application** class, which in turn associates all JAX-RS resource classes in the WAR file with this JAX-RS application, making them available under the common path specified in the **SystemApplication** class. The **@ApplicationPath** annotation has a value that indicates the path within the WAR that the JAX-RS application accepts requests from.

# Creating the JAX-RS resource

In JAX-RS, a single class should represent a single resource, or a group of resources of the same type. In this application, a resource might be a system property, or a set of system properties. It is easy to have a single class handle multiple different resources, but keeping a clean separation between types of resources helps with maintainability in the long run.

Create the `PropertiesResource` class.
> [File -> New File]src/main/java/io/openliberty/guides/rest/PropertiesResource.java

Add the following:

```java
package io.openliberty.guides.rest;

import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("properties")
public class PropertiesResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getProperties() {
        return System.getProperties();
    }

}
```
{: codeblock}

This resource class has quite a bit of code in it, so let's break it down into manageable chunks.

The **@Path** annotation on the class indicates that this resource responds to the **properties** path in the JAX-RS application. The **@ApplicationPath** annotation in the **SystemApplication** class together with the **@Path** annotation in this class indicates that the resource is available at the **System/properties** path.

JAX-RS maps the HTTP methods on the URL to the methods on the class. The method to call is determined by the annotations that are specified on the methods. In the application you are building, an HTTP **GET** request to the **System/properties** path results in the system properties being returned.

The **@GET** annotation on the method indicates that this method is to be called for the HTTP **GET** method. The **@Produces** annotation indicates the format of the content that will be returned. The value of the **@Produces** annotation will be specified in the HTTP **Content-Type** response header. For this application, a JSON structure is to be returned. The desired **Content-Type** for a JSON response is **application/json** with **MediaType.APPLICATION_JSON** instead of the **String** content type. Using a constant such as **MediaType.APPLICATION_JSON** is better because if there's a spelling error, a compile failure occurs.

JAX-RS supports a number of ways to marshal JSON. The JAX-RS 2.1 specification mandates JSON-Binding
(JSON-B) and JAX-B. 

The method body returns the result of **System.getProperties()** that is of type **java.util.Properties**. Since the method 
is annotated with **@Produces(MediaType.APPLICATION_JSON)**, JAX-RS uses JSON-B to automatically convert the returned object
to JSON data in the HTTP response.

# Configuring the server

To get the service running, the Liberty server needs to be correctly configured. 

Replace the server configuration file.
> [File -> Open]src/main/liberty/config/server.xml

```source
<server description="Intro REST Guide Liberty server">
  <featureManager>
      <feature>jaxrs-2.1</feature>
  </featureManager>

  <httpEndpoint httpPort="${default.http.port}" httpsPort="${default.https.port}"
                id="defaultHttpEndpoint" host="*" />

  <webApplication location="guide-rest-intro.war" contextRoot="${app.context.root}"/>
</server>
```
{: codeblock}

The configuration does the following actions:

. Configures the server to enable JAX-RS. This is specified in the **featureManager** element.
. Configures the server to resolve the HTTP port numbers from variables, which are then specified in the Maven **pom.xml** file. This is specified in the **<httpEndpoint/>** element. Variables use the **${variableName}** syntax. 
. Configures the server to run the produced web application on a context root specified in the **pom.xml** file. This is specified in the **<webApplication/>** element.

Take a look at the **pom.xml** file. 
> [File -> Open]pom.xml

The variables that are being used in the **server.xml** file are provided by the properties set in the Maven **pom.xml** file. The properties must be formatted as **liberty.var.variableName**.

## Building and running the application

The Open Liberty server was started in development mode at the beginning of the guide and all the 
changes were automatically picked up.

Check out the service that you created at the
http://localhost:9080/LibertyProject/System/properties URL. 

## Testing the service

You can test this service manually by using the following command in another shell:
```
curl http://localhost:9080/LibertyProject/System/properties
```
{: codeblock}

Automated tests are a much better approach because they trigger a failure if a change introduces a bug. JUnit and the JAX-RS Client API provide a simple environment to test the application.

You can write tests for the individual units of code outside of a running application server, or they can be written to call the application server directly. In this example, you will create a test that does the latter.

Create the `EndpointIT` class

> [File -> New File]src/test/java/it/io/openliberty/guides/rest/EndpointIT.java

Add the following to the class: 

```java
package it.io.openliberty.guides.rest;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Test;

public class EndpointIT {

    private static final Jsonb jsonb = JsonbBuilder.create();

    @Test
    public void testGetProperties() {
        String port = System.getProperty("http.port");
        String context = System.getProperty("context.root");
        String url = "http://localhost:" + port + "/" + context + "/";

        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(url + "System/properties");
        Response response = target.request().get();

        assertEquals("Incorrect response code from " + url,
                     Response.Status.OK.getStatusCode(), response.getStatus());

        String json = response.readEntity(String.class);
        Properties sysProps = jsonb.fromJson(json, Properties.class);

        assertEquals("The system property for the local and remote JVM should match",
                     System.getProperty("os.name"),
                     sysProps.getProperty("os.name"));
        response.close();
    }
}
```
{: codeblock}

This test class has more lines of code than the resource implementation. This situation is common. The test method is indicated with the **@Test** annotation.

The test code needs to know some information about the application to make requests. The server port and the application context root are key, and are dictated by the server configuration. While this information can be hardcoded, it is better to specify it in a single place like the Maven **pom.xml** file. Refer to the **pom.xml** file to see how the application information such as the **default.http.port**, **default.https.port** and **app.context.root** elements are provided in the file.

These Maven properties are then passed to the Java test program as the **<systemPropertyVariables/>** element in the **pom.xml** file.

Getting the values to create a representation of the URL is simple. The test class uses the **getProperty** method to get the application details.

To call the JAX-RS service using the JAX-RS client, first create a **WebTarget** object by calling the **target** method that provides the URL. To cause the HTTP request to occur, the **request().get()** method is called on the **WebTarget** object. The **get** method call is a synchronous call that blocks until a response is received. This call returns a **Response** object, which can be inspected to determine whether the request was successful.

The first thing to check is that a **200** response was received. The JUnit **assertEquals** method can be used for this check.

Check the response body to ensure it returned the right information. Since the client and the server are running on the same machine, it is reasonable to expect that the system properties for the local and remote JVM would be the same. In this case, an **assertEquals** assertion is made so that the **os.name** system property for both JVMs is the same. You can write additional assertions to check for more values.

### Running the tests

Since you started Open Liberty in development mode at the start of the guide, press **enter/return** key to run the tests. You will see the following output:

```source
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.rest.EndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.884 sec - in it.io.openliberty.guides.rest.EndpointIT

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

To see whether the tests detect a failure, add an assertion that you know fails, or change the existing
assertion to a constant value that doesn't match the **os.name** system property.

When you are done checking out the service, exit development mode by typing **q** in the shell session where
you ran the server and then pressing the **enter/return** key.

# Summary

## Clean up your environment

Delete the **guide-rest-intro** project by navigating to the **/home/project/** directory

```
rm -r -f guide-rest-intro
```
{: codeblock}

## Well Done

Nice work! You developed a REST service in Open Liberty by using JAX-RS and JSON-B.
