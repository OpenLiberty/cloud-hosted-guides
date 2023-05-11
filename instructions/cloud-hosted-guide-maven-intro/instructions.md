---
markdown-version: v1
title: instructions
branch: lab-438-instruction
version-history-start-date: 2021-12-03 21:25:57 UTC
tool-type: theia
---
::page{title="Welcome to the Building a web application with Maven guide!"}

Learn how to build and test a simple web application using Maven and Open Liberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You will learn how to configure a simple web servlet application using [Maven](https://maven.apache.org/what-is-maven.html) and the [Liberty Maven plugin](https://github.com/OpenLiberty/ci.maven/blob/main/README.md). When you compile and build the application code, Maven downloads and installs Open Liberty. If you run the application, Maven creates an Open Liberty server and runs the application on it. The application displays a simple web page with a link that, when clicked, calls the servlet to return a simple response of ***Hello! How are you today?***.

One benefit of using a build tool like Maven is that you can define the details of the project and any dependencies it has, and Maven automatically downloads and installs the dependencies. Another benefit of using Maven is that it can run repeatable, automated tests on the application. You can, of course, test your application manually by starting a server and pointing a web browser at the application URL. However, automated tests are a much better approach because you can easily rerun the same tests each time the application is built. If the tests don't pass after you change the application, the build fails, and you know that you introduced a regression that requires a fix to your code. 

Choosing a build tool often comes down to personal or organizational preference, but you might choose to use Maven for several reasons. Maven defines its builds by using XML, which is probably familiar to you already. As a mature, commonly used build tool, Maven probably integrates with whichever IDE you prefer to use. Maven also has an extensive plug-in library that offers various ways to quickly customize your build. Maven can be a good choice if your team is already familiar with it. 

You will create a Maven build definition file that's called a ***pom.xml*** file, which stands for Project Object Model, and use it to build your web application. You will then create a simple, automated test and configure Maven to automatically run the test.


::page{title="Installing Maven"}


Run the following command to test that Maven is installed:

```bash
mvn -v
```

If Maven is installed properly, you see information about the Maven installation similar to the following example:

```
Apache Maven 3.8.1 (05c21c65bdfed0f71a2f2ada8b84da59348c4c5d)
Maven home: /Applications/Maven/apache-maven-3.8.1
Java version: 11.0.12, vendor: International Business Machines Corporation, runtime: /Library/Java/JavaVirtualMachines/ibm-semeru-open-11.jdk/Contents/Home
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "11.6", arch: "x86_64", family: "mac"
```

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-maven-intro.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-maven-intro.git
cd guide-maven-intro
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.


### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the ***finish*** directory and run Maven with the ***liberty:run*** goal to build the application and deploy it to Open Liberty:

```bash
cd finish
mvn liberty:run
```

After you see the following message, your application server is ready.

```
The guideServer server is ready to run a smarter planet.
```


Select **Terminal** > **New Terminal** from the menu of the IDE to open another command-line session. Run the following curl command to view the output of the application: 
```bash
curl -s http://localhost:9080/ServletSample/servlet
```

The servlet returns a simple response of ***Hello! How are you today?***.

After you are finished checking out the application, stop the Open Liberty server by pressing `Ctrl+C` in the command-line session where you ran the server. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```


::page{title="Creating a simple application"}

The simple web application that you will build using Maven and Open Liberty is provided for you in the ***start*** directory so that you can focus on learning about Maven. This application uses a standard Maven directory structure, eliminating the need to customize the ***pom.xml*** file so that Maven understands your project layout.

All the application source code, including the Open Liberty server configuration (***server.xml***), is in the ***src/main/liberty/config*** directory:

```
    └── src
        └── main
           └── java
           └── resources
           └── webapp
           └── liberty
                  └── config
```


::page{title="Creating the project POM file"}
Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-maven-intro/start
```

Before you can build the project, define the Maven Project Object Model (POM) file, the ***pom.xml***. 

Create the pom.xml file in the ***start*** directory.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-maven-intro/start/pom.xml
```


> Then, to open the pom.xml file in your IDE, select
> **File** > **Open** > guide-maven-intro/start/pom.xml, or click the following button

::openFile{path="/home/project/guide-maven-intro/start/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>ServletSample</artifactId>
    <packaging>war</packaging>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <!-- Liberty configuration -->
        <liberty.var.default.http.port>9080</liberty.var.default.http.port>
        <liberty.var.default.https.port>9443</liberty.var.default.https.port>
        <liberty.var.app.context.root>${project.artifactId}</liberty.var.app.context.root>
    </properties>

    <dependencies>
        <!-- Provided dependencies -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>9.1.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>5.0</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        <!-- For testing -->
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.5.14</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.8.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
            </plugin>
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.7.1</version>
                <configuration>
                    <serverName>guideServer</serverName>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>2.22.2</version>
                <configuration>
                    <systemPropertyVariables>
                        <http.port>${liberty.var.default.http.port}</http.port>
                        <war.name>${liberty.var.app.context.root}</war.name>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***pom.xml*** file starts with a root ***project*** element and a ***modelversion*** element, which is always set to ***4.0.0***. 

A typical POM for a Liberty application contains the following sections:

* **Project coordinates**: The identifiers for this application.
* **Properties** (***properties***): Any properties for the project go here, including compilation details and any values that are referenced during compilation of the Java source code and generating the application.
* **Dependencies** (***dependencies***): Any Java dependencies that are required for compiling, testing, and running the application are listed here.
* **Build plugins** (***build***): Maven is modular and each of its capabilities is provided by a separate plugin. This is where you specify which Maven plugins should be used to build this project and any configuration information needed by those plugins.

The project coordinates describe the name and version of the application. The ***artifactId*** gives a name to the web application project, which is used to name the output files that are generated by the build (e.g. the WAR file) and the Open Liberty server that is created. You'll notice that other fields in the ***pom.xml*** file use variables that are resolved by the ***artifactId*** field. This is so that you can update the name of the sample application, including files generated by Maven, in a single place in the ***pom.xml*** file. The value of the ***packaging*** field is ***war*** so that the project output artifact is a WAR file.

The first four properties in the properties section of the project, just define the encoding (***UTF-8***) and version of Java (***Java 8***) that Maven uses to compile the application source code.

Open Liberty configuration properties provide you with a single place to specify values that are used in multiple places throughout the application. For example, the ***default.http.port*** value is used in both the server configuration (***server.xml***) file and will be used in the test class that you will add (***EndpointIT.java***) to the application. Because the ***default.http.port*** value is specified in the ***pom.xml*** file, you can easily change the port number that the server runs on without updating the application code in multiple places.


The ***HelloServlet.java*** class depends on ***javax.servlet-api*** to compile. Maven will download this dependency from the Maven Central repository using the ***groupId***, ***artifactId***, and ***version*** details that you provide here. The dependency is set to ***provided***, which means that the API is in the server runtime and doesn't need to be packaged by the application.

The ***build*** section gives details of the two plugins that Maven uses to build this project.

* The Maven plugin for generating a WAR file as one of the output files.
* The Liberty Maven plug-in, which allows you to install applications into Open Liberty and manage the server instances.

In the ***liberty-maven-plugin*** plug-in section, you can add a ***configuration*** element to specify Open Liberty configuration details. For example, the ***serverName*** field defines the name of the Open Liberty server that Maven creates. You specified ***guideServer*** as the value for ***serverName***. If the ***serverName*** field is not included, the default value is ***defaultServer***.



::page{title="Running the application"}

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


Select **Terminal** > **New Terminal** from the menu of the IDE to open another command-line session. Run the following curl command to view the output of the application: 
```bash
curl -s http://localhost:9080/ServletSample/servlet
```

The servlet returns a simple response of ***Hello! How are you today?***.

::page{title="Testing the web application"}

One of the benefits of building an application with Maven is that Maven can be configured to run a set of tests. You can write tests for the individual units of code outside of a running application server (unit tests), or you can write them to call the application server directly (integration tests). In this example you will create a simple integration test that checks that the web page opens and that the correct response is returned when the link is clicked.

Create the ***EndpointIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-maven-intro/start/src/test/java/io/openliberty/guides/hello/it/EndpointIT.java  
```


> Then, to open the EndpointIT.java file in your IDE, select
> **File** > **Open** > guide-maven-intro/start/src/test/java/io/openliberty/guides/hello/it/EndpointIT.java, or click the following button

::openFile{path="/home/project/guide-maven-intro/start/src/test/java/io/openliberty/guides/hello/it/EndpointIT.java"}



```java
package io.openliberty.guides.hello.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EndpointIT {
    private static String siteURL;

    @BeforeAll
    public static void init() {
        String port = System.getProperty("http.port");
        String war = System.getProperty("war.name");
        siteURL = "http://localhost:" + port + "/" + war + "/" + "servlet";
    }

    @Test
    public void testServlet() throws Exception {

        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(siteURL);
        CloseableHttpResponse response = null;

        try {
            response = client.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            assertEquals(HttpStatus.SC_OK, statusCode, "HTTP GET failed");

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                                        response.getEntity().getContent()));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
            assertTrue(buffer.toString().contains("Hello! How are you today?"),
                "Unexpected response body: " + buffer.toString());
        } finally {
            response.close();
            httpGet.releaseConnection();
        }
    }
}
```



The test class name ends in ***IT*** to indicate that it contains an integration test. 

Maven is configured to run the integration test using the ***maven-failsafe-plugin***. The ***systemPropertyVariables*** section defines some variables that the test class uses. The test code needs to know where to find the application that it is testing. While the port number and context root information can be hardcoded in the test class, it is better to specify it in a single place like the Maven ***pom.xml*** file because this information is also used by other files in the project. The ***systemPropertyVariables*** section passes these details to the Java test program as a series of system properties, resolving the ***http.port*** and ***war.name*** variables.


The following lines in the ***EndpointIT*** test class uses these system variables to build up the URL of the application.

In the test class, after defining how to build the application URL, the ***@Test*** annotation indicates the start of the test method.

In the ***try block*** of the test method, an HTTP ***GET*** request to the URL of the application returns a status code. If the response to the request includes the string ***Hello! How are you today?***, the test passes. If that string is not in the response, the test fails.  The HTTP client then disconnects from the application.

In the ***import*** statements of this test class, you'll notice that the test has some new dependencies. Before the test can be compiled by Maven, you need to update the ***pom.xml*** to include these dependencies.

The Apache ***httpclient*** and ***junit-jupiter-engine*** dependencies are needed to compile and run the integration test ***EndpointIT*** class. The scope for each of the dependencies is set to ***test*** because the libraries are needed only during the Maven build and do not needed to be packaged with the application.

Now, the created WAR file contains the web application, and development mode can run any integration test classes that it finds. Integration test classes are classes with names that end in ***IT***.

The directory structure of the project should now look like this:

```
    └── src
        ├── main
        │  └── java
        │  └── resources
        │  └── webapp
        │  └── liberty
        │         └── config
        └── test
            └── java
```


### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

You see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running io.openliberty.guides.hello.it.EndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.255 sec - in io.openliberty.guides.hello.it.EndpointIT

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

To see whether the test detects a failure, change the ***response string*** in the servlet ***src/main/java/io/openliberty/guides/hello/HelloServlet.java*** so that it doesn't match the string that the test is looking for. Then re-run the tests and check that the test fails.


When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran the server, or by typing ***q*** and then pressing the ***enter/return*** key.

::page{title="Summary"}

### Nice Work!

You built and tested a web application project with an Open Liberty server using Maven.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-maven-intro*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-maven-intro
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Building%20a%20web%20application%20with%20Maven&guide-id=cloud-hosted-guide-maven-intro)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-maven-intro/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-maven-intro/pulls)



### Where to next?

* [Creating a multi-module application](https://openliberty.io/guides/maven-multimodules.html)
* [Building a web application with Gradle](https://openliberty.io/guides/gradle-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
