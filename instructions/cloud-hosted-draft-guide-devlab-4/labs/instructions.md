---
markdown-version: v1
title: instructions
branch: lab-5933-instruction
version-history-start-date: 2023-04-14T18:24:15Z
tool-type: theia
---
::page{title="Welcome to the Enabling Cross-Origin Resource Sharing (CORS) guide!"}

Learn how to enable Cross-Origin Resource Sharing (CORS) in Open Liberty without writing Java code.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You will learn how to add two server configurations to enable CORS. Next, you will write and run tests to validate that the CORS configurations work. These tests send two different CORS requests to a REST service that has two different endpoints.

### CORS and its purpose

Cross-Origin Resource Sharing (CORS) is a W3C specification and mechanism that you can use to request restricted resources from a domain outside the current domain. In other words, CORS is a technique for consuming an API served from an origin different than yours.

CORS is useful for requesting different kinds of data from websites that aren't your own. These types of data might include images, videos, scripts, stylesheets, iFrames, or web fonts.

However, you cannot request resources from another website domain without proper permission. In JavaScript, cross-origin requests with an ***XMLHttpRequest*** API and Ajax cannot happen unless CORS is enabled on the server that receives the request. Otherwise, same-origin security policy prevents the requests. For example, a web page that is served from the ***http://aboutcors.com*** server sends a request to get data to the ***http://openliberty.io*** server. Because of security concerns, browsers block the server response unless the server adds HTTP response headers to allow the web page to consume the data.

Different ports and different protocols also trigger CORS. For example, the ***http://abc.xyz:1234*** domain is considered to be different from the ***https://abc.xyz:4321*** domain.

Open Liberty has built-in support for CORS that gives you an easy and powerful way to configure the runtime to handle CORS requests without the need to write Java code.

### Types of CORS requests

Familiarize yourself with two kinds of CORS requests to understand the attributes that you will add in the two CORS configurations.

#### Simple CORS request

According to the CORS specification, an HTTP request is a simple CORS request if the request method is ***GET***, ***HEAD***, or ***POST***. The header fields are any one of the ***Accept***, ***Accept-Language***, ***Content-Language***, or ***Content-Type*** headers. The ***Content-Type*** header has a value of ***application/x-www-form-urlencoded***, ***multipart/form-data***, or ***text/plain***.

When clients, such as browsers, send simple CORS requests to servers on different domains, the clients include an ***Origin*** header with the original (referring)  host name as the value. If the server allows the origin, the server includes an ***Access-Control-Allow-Origin*** header with a list of allowed origins or an asterisk (*) in the response back to the client. The asterisk indicates that all origins are allowed to access the endpoint on the server.

#### Preflight CORS request

A CORS request is not a simple CORS request if a client first sends a preflight CORS request before it sends the actual request. For example, the client sends a preflight request before it sends a ***DELETE*** HTTP request. To determine whether the request is safe to send, the client sends a preflight request, which is an ***OPTIONS*** HTTP request, to gather more information about the server. This preflight request has the ***Origin*** header and other headers to indicate the HTTP method and headers of the actual request to be sent after the preflight request.

Once the server receives the preflight request, if the origin is allowed, the server responds with headers that indicate the HTTP methods and headers that are allowed in the actual requests. The response might include more CORS-related headers.

Next, the client sends the actual request, and the server responds.


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-cors.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-cors.git
cd guide-cors
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.



::page{title="Enabling CORS"}
Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-cors/start
```

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

You will use a REST service that is already provided for you to test your CORS configurations. You can find this service in the ***src/main/java/io/openliberty/guides/cors/*** directory.

You will send a simple request to the ***/configurations/simple*** endpoint and the preflight request to the ***/configurations/preflight*** endpoint.


### Enabling a simple CORS configuration
Configure the server to allow the ***/configurations/simple*** endpoint to accept a ***simple*** CORS request. Add a simple CORS configuration to the ***server.xml*** file:

Replace the server configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-cors/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-cors/start/src/main/liberty/config/server.xml"}



```xml
<server description="Sample Liberty server">

<featureManager>
    <feature>restfulWS-3.1</feature>
    <feature>jsonb-3.0</feature>
</featureManager>

<variable name="default.http.port" defaultValue="9080"/>
<variable name="default.https.port" defaultValue="9443"/>

<httpEndpoint host="*" httpPort="${default.http.port}" httpsPort="${default.https.port}"
    id="defaultHttpEndpoint"/>

<webApplication location="guide-cors.war" contextRoot="/"/>

<cors domain="/configurations/simple"
    allowedOrigins="openliberty.io"
    allowedMethods="GET"
    allowCredentials="true"
    exposeHeaders="MyHeader"/>

</server>
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


The CORS configuration contains the following attributes:

| *Configuration Attribute* | *Value*
| ---| ---
|***domain*** | The endpoint to be configured for CORS requests. The value is set to ***/configurations/simple***.
|***allowedOrigins*** | Origins that are allowed to access the endpoint. The value is set to ***openliberty.io***.
|***allowedMethods*** | HTTP methods that a client is allowed to use when it makes requests to the endpoint. The value is set to ***GET***.
|***allowCredentials*** | A boolean that indicates whether the user credentials can be included in the request. The value is set to ***true***.
|***exposeHeaders*** | Headers that are safe to expose to clients. The value is set to ***MyHeader***.

For more information about these and other CORS attributes, see the [cors element documentation](https://www.openliberty.io/docs/latest/reference/config/cors.html).

Save the changes to the ***server.xml*** file. The ***/configurations/simple*** endpoint is now ready to be tested with a simple CORS request.

The Open Liberty server was started in development mode at the beginning of the guide and all the changes were automatically picked up.

Now, test the simple CORS configuration that you added. Add the ***testSimpleCorsRequest*** method to the ***CorsIT*** class.

Replace the ***CorsIT*** class.

> To open the CorsIT.java file in your IDE, select
> **File** > **Open** > guide-cors/start/src/test/java/it/io/openliberty/guides/cors/CorsIT.java, or click the following button

::openFile{path="/home/project/guide-cors/start/src/test/java/it/io/openliberty/guides/cors/CorsIT.java"}



```java
package it.io.openliberty.guides.cors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CorsIT {

    String port = System.getProperty("default.http.port");
    String pathToHost = "http://localhost:" + port + "/";

    @BeforeEach
    public void setUp() {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    @Test
    public void testSimpleCorsRequest() throws IOException {
        HttpURLConnection connection = HttpUtils.sendRequest(
                        pathToHost + "configurations/simple", "GET",
                        TestData.simpleRequestHeaders);
        checkCorsResponse(connection, TestData.simpleResponseHeaders);

        printResponseHeaders(connection, "Simple CORS Request");
    }


    public void checkCorsResponse(HttpURLConnection connection,
                    Map<String, String> expectedHeaders) throws IOException {
        assertEquals(200, connection.getResponseCode(), "Invalid HTTP response code");
        expectedHeaders.forEach((responseHeader, value) -> {
            assertEquals(value, connection.getHeaderField(responseHeader),
                            "Unexpected value for " + responseHeader + " header");
        });
    }

    public static void printResponseHeaders(HttpURLConnection connection,
                    String label) {
        System.out.println("--- " + label + " ---");
        Map<String, java.util.List<String>> map = connection.getHeaderFields();
        for (Entry<String, java.util.List<String>> entry : map.entrySet()) {
            System.out.println("Header " + entry.getKey() + " = " + entry.getValue());
        }
        System.out.println();
    }

}
```



The ***testSimpleCorsRequest*** test simulates a client. It first sends a simple CORS request to the ***/configurations/simple*** endpoint, and then it checks for a valid response and expected headers. Lastly, it prints the response headers for you to inspect.

The request is a ***GET*** HTTP request with the following header:

| *Request Header* | *Request Value*
| ---| ---
| Origin | The value is set to ***openliberty.io***. Indicates that the request originates from ***openliberty.io***.

Expect the following response headers and values if the simple CORS request is successful, and the server is correctly configured:

| *Response Header* | *Response Value*
| ---| ---
| Access-Control-Allow-Origin | The expected value is ***openliberty.io***. Indicates whether a resource can be shared based on the returning value of the Origin request header ***openliberty.io***.
| Access-Control-Allow-Credentials | The expected value is ***true***. Indicates that the user credentials can be included in the request.
| Access-Control-Expose-Headers |  The expected value is ***MyHeader***. Indicates that the header ***MyHeader*** is safe to expose.

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

If the ***testSimpleCorsRequest*** test passes, the response headers with their values from the endpoint are printed. The ***/configurations/simple*** endpoint now accepts simple CORS requests.

Response headers with their values from the endpoint:
```
--- Simple CORS Request ---
Header null = [HTTP/1.1 200 OK]
Header Access-Control-Expose-Headers = [MyHeader]
Header Access-Control-Allow-Origin = [openliberty.io]
Header Access-Control-Allow-Credentials = [true]
Header Content-Length = [22]
Header Content-Language = [en-CA]
Header Date = [Thu, 21 Mar 2019 17:50:09 GMT]
Header Content-Type = [text/plain]
Header X-Powered-By = [Servlet/4.0]
```

### Enabling a preflight CORS configuration

Configure the server to allow the ***/configurations/preflight*** endpoint to accept a ***preflight*** CORS request. Add another CORS configuration in the ***server.xml*** file:

Replace the server configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-cors/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-cors/start/src/main/liberty/config/server.xml"}



```xml
<server description="Sample Liberty server">

<featureManager>
    <feature>restfulWS-3.1</feature>
    <feature>jsonb-3.0</feature>
</featureManager>

<variable name="default.http.port" defaultValue="9080"/>
<variable name="default.https.port" defaultValue="9443"/>

<httpEndpoint host="*" httpPort="${default.http.port}" httpsPort="${default.https.port}"
    id="defaultHttpEndpoint"/>

<webApplication location="guide-cors.war" contextRoot="/"/>

<cors domain="/configurations/simple"
    allowedOrigins="openliberty.io"
    allowedMethods="GET"
    allowCredentials="true"
    exposeHeaders="MyHeader"/>

<cors domain="/configurations/preflight"
    allowedOrigins="*"
    allowedMethods="OPTIONS, DELETE"
    allowCredentials="true"
    allowedHeaders="MyOwnHeader1, MyOwnHeader2"
    maxAge="10"/>
</server>
```



The preflight CORS configuration has different values than the simple CORS configuration.

| *Configuration Attribute* | *Value*
| ---| ---
| ***domain***|The value is set to ***/configurations/preflight*** because the ***domain*** is a different endpoint.
| ***allowedOrigins***| Origins that are allowed to access the endpoint. The value is set to an asterisk (*) to allow requests from all origins.
| ***allowedMethods***| HTTP methods that a client is allowed to use when it makes requests to the endpoint. The value is set to ***OPTIONS, DELETE***.
| ***allowCredentials***| A boolean that indicates whether the user credentials can be included in the request. The value is set to ***true***.

The following attributes were added:

* ***allowedHeaders***: Headers that a client can use in requests. Set the value to ***MyOwnHeader1, MyOwnHeader2***.
* ***maxAge***: The number of seconds that a client can cache a response to a preflight request. Set the value to ***10***.

Save the changes to the ***server.xml*** file. The ***/configurations/preflight*** endpoint is now ready to be tested with a preflight CORS request.

Add another test to the ***CorsIT.java*** file to test the preflight CORS configuration that you just added:

Replace the ***CorsIT*** class.

> To open the CorsIT.java file in your IDE, select
> **File** > **Open** > guide-cors/start/src/test/java/it/io/openliberty/guides/cors/CorsIT.java, or click the following button

::openFile{path="/home/project/guide-cors/start/src/test/java/it/io/openliberty/guides/cors/CorsIT.java"}



```java
package it.io.openliberty.guides.cors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CorsIT {

    String port = System.getProperty("default.http.port");
    String pathToHost = "http://localhost:" + port + "/";

    @BeforeEach
    public void setUp() {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    @Test
    public void testSimpleCorsRequest() throws IOException {
        HttpURLConnection connection = HttpUtils.sendRequest(
                        pathToHost + "configurations/simple", "GET",
                        TestData.simpleRequestHeaders);
        checkCorsResponse(connection, TestData.simpleResponseHeaders);

        printResponseHeaders(connection, "Simple CORS Request");
    }

    @Test
    public void testPreflightCorsRequest() throws IOException {
        HttpURLConnection connection = HttpUtils.sendRequest(
                        pathToHost + "configurations/preflight", "OPTIONS",
                        TestData.preflightRequestHeaders);
        checkCorsResponse(connection, TestData.preflightResponseHeaders);

        printResponseHeaders(connection, "Preflight CORS Request");
    }

    public void checkCorsResponse(HttpURLConnection connection,
                    Map<String, String> expectedHeaders) throws IOException {
        assertEquals(200, connection.getResponseCode(), "Invalid HTTP response code");
        expectedHeaders.forEach((responseHeader, value) -> {
            assertEquals(value, connection.getHeaderField(responseHeader),
                            "Unexpected value for " + responseHeader + " header");
        });
    }

    public static void printResponseHeaders(HttpURLConnection connection,
                    String label) {
        System.out.println("--- " + label + " ---");
        Map<String, java.util.List<String>> map = connection.getHeaderFields();
        for (Entry<String, java.util.List<String>> entry : map.entrySet()) {
            System.out.println("Header " + entry.getKey() + " = " + entry.getValue());
        }
        System.out.println();
    }

}
```



The ***testPreflightCorsRequest*** test simulates a client sending a preflight CORS request. It first sends the request to the ***/configurations/preflight*** endpoint, and then it checks for a valid response and expected headers. Lastly, it prints the response headers for you to inspect.

The request is an ***OPTIONS*** HTTP request with the following headers:

| *Request Header* | *Request Value*
| ---| ---
| Origin | The value is set to ***anywebsiteyoulike.com***. Indicates that the request originates from ***anywebsiteyoulike.com***.
| Access-Control-Request-Method | The value is set to ***DELETE***. Indicates that the HTTP DELETE method will be used in the actual request.
| Access-Control-Request-Headers | The value is set to ***MyOwnHeader2***. Indicates the header ***MyOwnHeader2*** will be used in the actual request.

Expect the following response headers and values if the preflight CORS request is successful, and the server is correctly configured:

| *Response Header* | *Response Value*
| ---| ---
| Access-Control-Max-Age | The expected value is ***10***. Indicates that the preflight request can be cached within ***10*** seconds.
| Access-Control-Allow-Origin | The expected value is ***anywebsiteyoulike.com***. Indicates whether a resource can be shared based on the returning value of the Origin request header ***anywebsiteyoulike.com***.
| Access-Control-Allow-Methods | The expected value is ***OPTIONS, DELETE***. Indicates that HTTP OPTIONS and DELETE methods can be used in the actual request.
| Access-Control-Allow-Credentials | The expected value is ***true***. Indicates that the user credentials can be included in the request.
| Access-Control-Allow-Headers | The expected value is ***MyOwnHeader1, MyOwnHeader2***. Indicates that the header ***MyOwnHeader1*** and ***MyOwnHeader2*** are safe to expose.

The ***Access-Control-Allow-Origin*** header has a value of ***anywebsiteyoulike.com*** because the server is configured to allow all origins, and the request came with an origin of ***anywebsiteyoulike.com***.

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

If the ***testPreflightCorsRequest*** test passes, the response headers with their values from the endpoint are printed. The ***/configurations/preflight*** endpoint now allows preflight CORS requests.

Response headers with their values from the endpoint:
```
--- Preflight CORS Request ---
Header null = [HTTP/1.1 200 OK]
Header Access-Control-Allow-Origin = [anywebsiteyoulike.com]
Header Access-Control-Allow-Methods = [OPTIONS, DELETE]
Header Access-Control-Allow-Credentials = [true]
Header Content-Length = [0]
Header Access-Control-Max-Age = [10]
Header Date = [Thu, 21 Mar 2019 18:21:13 GMT]
Header Content-Language = [en-CA]
Header Access-Control-Allow-Headers = [MyOwnHeader1, MyOwnHeader2]
Header X-Powered-By = [Servlet/4.0]
```

You can modify the server configuration and the test code to experiment with the various CORS configuration attributes.

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran the server, or by typing ***q*** and then pressing the ***enter/return*** key.


::page{title="Summary"}

### Nice Work!

You enabled CORS support in Open Liberty. You added two different CORS configurations to allow two kinds of CORS requests in the **server.xml** file.




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-cors*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-cors
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Enabling%20Cross-Origin%20Resource%20Sharing%20(CORS)&guide-id=cloud-hosted-guide-cors)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-cors/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-cors/pulls)



### Where to next?

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Consuming a RESTful web service](https://openliberty.io/guides/rest-client-java.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
