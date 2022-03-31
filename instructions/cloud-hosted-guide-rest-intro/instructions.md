---
markdown-version: v1
title: instructions
branch: lab-141-instruction
version-history-start-date: 2020-04-22 13:17:27 UTC
---



::page{title="What you'll learn"}

You will learn how to build and test a simple REST service with JAX-RS and JSON-B, which will expose
the JVM's system properties. The REST service will respond to ***GET*** requests made to the ***http://localhost:9080/LibertyProject/system/properties*** URL.

The service responds to a ***GET*** request with a JSON representation of the system properties, where
each property is a field in a JSON object like this:

```
{
  "os.name":"Mac",
  "java.version": "1.8"
}
```

The design of an HTTP API is essential when creating a web application. The REST API has 
become the go-to architectural style for building an HTTP API. The JAX-RS API offers 
functionality for creating, reading, updating, and deleting exposed resources. The JAX-RS API 
supports the creation of RESTful web services that come with desirable properties, 
such as performance, scalability, and modifiability.




Check out the service at the http://localhost:9080/LibertyProject/system/properties URL. 


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/LibertyProject/system/properties | jq
```





::page{title="Creating a JAX-RS application"}

Navigate to the ***start*** directory to begin.
```
cd /home/project/guide-rest-intro/start
```


JAX-RS has two key concepts for creating REST APIs. The most obvious one is the resource itself, which is
modelled as a class. The second is a JAX-RS application, which groups all exposed resources under a
common path. You can think of the JAX-RS application as a wrapper for all of your resources.


Replace the ***SystemApplication*** class.

> To open the SystemApplication.java file in your IDE, select
> **File** > **Open** > guide-rest-intro/start/src/main/java/io/openliberty/guides/rest/SystemApplication.java, or click the following button

::openFile{path="/home/project/guide-rest-intro/start/src/main/java/io/openliberty/guides/rest/SystemApplication.java"}



```java
package io.openliberty.guides.rest;

import javax.ws.rs.core.Application;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("system")
public class SystemApplication extends Application {

}
```


The ***SystemApplication*** class extends the ***Application*** class, which associates all JAX-RS resource classes in the WAR file with this JAX-RS application. These resources become available under the common path that's specified with the ***@ApplicationPath*** 
annotation. The ***@ApplicationPath*** annotation has a value that indicates the path in the WAR file that 
the JAX-RS application accepts requests from.




::page{title="Creating the JAX-RS resource"}

In JAX-RS, a single class should represent a single resource, or a group of resources of the same type.
In this application, a resource might be a system property, or a set of system properties. It is easy
to have a single class handle multiple different resources, but keeping a clean separation between types
of resources helps with maintainability in the long run.

Create the ***PropertiesResource*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-intro/start/src/main/java/io/openliberty/guides/rest/PropertiesResource.java
```


> Then, to open the unknown file in your IDE, select
> **File** > **Open** > guide-rest-intro/start/unknown, or click the following button

::openFile{path="/home/project/guide-rest-intro/start/unknown"}


The ***@Path*** annotation on the class indicates that this resource responds to the ***properties*** path
in the JAX-RS application. The ***@ApplicationPath*** annotation in the ***SystemApplication*** class together with
the ***@Path*** annotation in this class indicates that the resource is available at the ***system/properties***
path.

JAX-RS maps the HTTP methods on the URL to the methods of the class by using annotations. 
Your application uses the ***GET*** annotation to map an HTTP ***GET*** request
to the ***system/properties*** path.

The ***@GET*** annotation on the method indicates that this method is to be called for the HTTP ***GET***
method. The ***@Produces*** annotation indicates the format of the content that will be returned. The
value of the ***@Produces*** annotation will be specified in the HTTP ***Content-Type*** response header.
For this application, a JSON structure is to be returned. The desired ***Content-Type*** for a JSON
response is ***application/json*** with ***MediaType.APPLICATION_JSON*** instead of the ***String*** content type. Using a constant such as ***MediaType.APPLICATION_JSON*** is better because if there's a spelling error, a compile failure occurs.

JAX-RS supports a number of ways to marshal JSON. The JAX-RS 2.1 specification mandates JSON-Binding
(JSON-B). The method body returns the result of ***System.getProperties()***, which is of type ***java.util.Properties***. Since the method 
is annotated with ***@Produces(MediaType.APPLICATION_JSON)***, JAX-RS uses JSON-B to automatically convert the returned object
to JSON data in the HTTP response.





::page{title="Configuring the server"}

To get the service running, the Liberty server needs to be correctly configured.

Replace the server configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-rest-intro/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-rest-intro/start/src/main/liberty/config/server.xml"}



```xml
<server description="Intro REST Guide Liberty server">
  <featureManager>
      <feature>jaxrs-2.1</feature>
  </featureManager>

  <httpEndpoint httpPort="${default.http.port}" httpsPort="${default.https.port}"
                id="defaultHttpEndpoint" host="*" />

  <webApplication location="guide-rest-intro.war" contextRoot="${app.context.root}"/>
</server>
```



The configuration does the following actions:

* Configures the server to enable JAX-RS. This is specified in the ***featureManager*** element.
* Configures the server to resolve the HTTP port numbers from variables, which are then specified in the Maven ***pom.xml*** file. This is specified in the ***httpEndpoint*** element. Variables use the ***${variableName}*** syntax.
* Configures the server to run the produced web application on a context root specified in the ***pom.xml*** file. This is specified in the ***webApplication*** element.


The variables that are being used in the ***server.xml*** file are provided by the properties set in the Maven ***pom.xml*** file. The properties must be formatted as ***liberty.var.variableName***.




Check out the service that you created at the http://localhost:9080/LibertyProject/system/properties URL. 


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/LibertyProject/system/properties | jq
```




::page{title="Testing the service"}


You can test this service manually by starting a server and visiting the
http://localhost:9080/LibertyProject/system/properties URL. However, automated tests are a 
much better approach because they trigger a failure if a change introduces a bug. JUnit and the JAX-RS 
Client API provide a simple environment to test the application.

You can write tests for the individual units of code outside of a running application server, or they
can be written to call the application server directly. In this example, you will create a test that
does the latter.

Create the ***EndpointIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-intro/start/src/test/java/it/io/openliberty/guides/rest/EndpointIT.java
```


> Then, to open the EndpointIT.java file in your IDE, select
> **File** > **Open** > guide-rest-intro/start/src/test/java/it/io/openliberty/guides/rest/EndpointIT.java, or click the following button

::openFile{path="/home/project/guide-rest-intro/start/src/test/java/it/io/openliberty/guides/rest/EndpointIT.java"}



```java
package it.io.openliberty.guides.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Properties;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

public class EndpointIT {
    private static final Jsonb JSONB = JsonbBuilder.create();
    @Test
    public void testGetProperties() {
        String port = System.getProperty("http.port");
        String context = System.getProperty("context.root");
        String url = "http://localhost:" + port + "/" + context + "/";

        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(url + "system/properties");
        Response response = target.request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                     "Incorrect response code from " + url);

        String json = response.readEntity(String.class);
        Properties sysProps = JSONB.fromJson(json, Properties.class);

        assertEquals(System.getProperty("os.name"), sysProps.getProperty("os.name"),
                     "The system property for the local and remote JVM should match");
        response.close();
    }
}
```



This test class has more lines of code than the resource implementation. This situation is common.
The test method is indicated with the ***@Test*** annotation.


The test code needs to know some information about the application to make requests. The server port and the application context root are key, and are dictated by the server configuration. While this information can be hardcoded, it is better to specify it in a single place like the Maven ***pom.xml*** file. Refer to the ***pom.xml*** file to see how the application information such as the ***default.http.port***, ***default.https.port*** and ***app.context.root*** elements are provided in the file.


These Maven properties are then passed to the Java test program as the ***systemPropertyVariables*** element in the ***pom.xml*** file.

Getting the values to create a representation of the URL is simple. The test class uses the ***getProperty*** method
to get the application details.

To call the JAX-RS service using the JAX-RS client, first create a ***WebTarget*** object by calling
the ***target*** method that provides the URL. To cause the HTTP request to occur, the ***request().get()*** method
is called on the ***WebTarget*** object. The ***get*** method
call is a synchronous call that blocks until a response is received. This call returns a ***Response***
object, which can be inspected to determine whether the request was successful.

The first thing to check is that a ***200*** response was received. The JUnit ***assertEquals*** method can be used for this check.

Check the response body to ensure it returned the right information. Since the client and the server
are running on the same machine, it is reasonable to expect that the system properties for the local
and remote JVM would be the same. In this case, an ***assertEquals*** assertion is made so that the ***os.name*** system property
for both JVMs is the same. You can write additional assertions to check for more values.


You will see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.rest.EndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.884 sec - in it.io.openliberty.guides.rest.EndpointIT

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

To see whether the tests detect a failure, add an assertion that you know fails, or change the existing
assertion to a constant value that doesn't match the ***os.name*** system property.



::page{title="Summary"}

### Nice Work!

You just developed a REST service in Open Liberty by using JAX-RS and JSON-B.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-rest-intro*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-rest-intro
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Creating%20a%20RESTful%20web%20service&guide-id=cloud-hosted-guide-rest-intro)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-rest-intro/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-rest-intro/pulls)



### Where to next?

* [Consuming a RESTful web service](https://openliberty.io/guides/rest-client-java.html)
* [Consuming a RESTful web service with AngularJS](https://openliberty.io/guides/rest-client-angularjs.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
