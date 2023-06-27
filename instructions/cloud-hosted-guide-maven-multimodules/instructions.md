---
markdown-version: v1
title: instructions
branch: lab-418-instruction
version-history-start-date: 2021-12-03 21:25:57 UTC
tool-type: theia
---
::page{title="Welcome to the Creating a multi-module application guide!"}

You will learn how to build an application with multiple modules with Maven and Open Liberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

A Jakarta Platform, Enterprise Edition (Jakarta EE) application consists of modules that work together as one entity. An enterprise archive (EAR) is a wrapper for a Jakarta EE application, which consists of web archive (WAR) and Java archive (JAR) files. To deploy or distribute the Jakarta EE application into new environments, all the modules and resources must first be packaged into an EAR file.

In this guide, you will learn how to:

* establish a dependency between a web module and a Java library module,
* use Maven to package the WAR file and the JAR file into an EAR file so that you can run and test the application on Open Liberty, and
 use Liberty Maven plug-in to develop a multi-module application in [dev mode](https://openliberty.io/docs/latest/development-mode.html#_run_multi_module_maven_projects_in_dev_mode) without having to prebuild the JAR and WAR files. In dev mode, your changes are automatically picked up by the running server.

You will build a unit converter application that converts heights from centimeters into feet and inches. The application will request the user to enter a height value in centimeters. Then, the application processes the input by using functions that are found in the JAR file to return the height value in imperial units.



::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-maven-multimodules.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-maven-multimodules.git
cd guide-maven-multimodules
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

Access partial implementation of the application from the ***start*** folder. This folder includes a web module in the ***war*** folder, a Java library in the ***jar*** folder, and template files in the ***ear*** folder. However, the Java library and the web module are independent projects, and you will need to complete the following steps to implement the application:

1. Add a dependency relationship between the two modules.

2. Assemble the entire application into an EAR file.

3. Aggregate the entire build.

4. Test the multi-module application.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the ***finish*** directory and run the following Maven goal to build the application:

```bash
cd finish
mvn install
```

To deploy your EAR application on an Open Liberty server, run the Maven ***liberty:run*** goal from the finish directory using the ***-pl*** flag to specify the ***ear*** project. The ***-pl*** flag specifies the project where the Maven goal runs.

```bash
mvn -pl ear liberty:run
```

After the server is running, click the following button to check out your service by visiting the ***/converter*** endpoint.
::startApplication{port="9080" display="external" name="Visit application" route="/converter"}

After you are finished checking out the application, stop the Open Liberty server by pressing `Ctrl+C` in the command-line session where you ran the server. Alternatively, you can run the ***liberty:stop*** goal using the ***-pl ear*** flag from the ***finish*** directory in another command-line session:

```bash
mvn -pl ear liberty:stop
```


::page{title="Adding dependencies between WAR and JAR modules"}

To use a Java library in your web module, you must add a dependency relationship between the two modules.

As you might have noticed, each module has its own ***pom.xml*** file. Each module has its own ***pom.xml*** file because each module is treated as an independent project. You can rebuild, reuse, and reassemble every module on its own.

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-maven-multimodules/start
```

Replace the war/POM file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-maven-multimodules/start/war/pom.xml, or click the following button

::openFile{path="/home/project/guide-maven-multimodules/start/war/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
    http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <parent>
        <groupId>io.openliberty.guides</groupId>
        <artifactId>guide-maven-multimodules</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-maven-multimodules-war</artifactId>
    <packaging>war</packaging>
    <version>1.0-SNAPSHOT</version>
    <name>guide-maven-multimodules-war</name>
    <url>http://maven.apache.org</url>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- Provided dependencies -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>10.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>6.0</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>io.openliberty.guides</groupId>
            <artifactId>guide-maven-multimodules-jar</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

    </dependencies>

</project>
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.

The added ***dependency*** element is the Java library module that implements the functions that you need for the unit converter.

Although the ***parent/child*** structure is not normally needed for multi-module applications, adding it helps us to better organize all of the projects. This structure allows all of the child projects to make use of the plugins that are defined in the parent ***pom.xml*** file, without having to define them again in the child ***pom.xml*** files.


::page{title="Assembling multiple modules into an EAR file"}

To deploy the entire application on the Open Liberty server, first package the application. Use the EAR project to assemble multiple modules into an EAR file.

Navigate to the ***ear*** folder and find a template ***pom.xml*** file.
Replace the ear/POM file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-maven-multimodules/start/ear/pom.xml, or click the following button

::openFile{path="/home/project/guide-maven-multimodules/start/ear/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
    http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <parent>
        <groupId>io.openliberty.guides</groupId>
        <artifactId>guide-maven-multimodules</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-maven-multimodules-ear</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>ear</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <!-- Liberty configuration -->
        <liberty.var.default.http.port>9080</liberty.var.default.http.port>
        <liberty.var.default.https.port>9443</liberty.var.default.https.port>
    </properties>

    <dependencies>
        <!-- web and jar modules as dependencies -->
        <dependency>
            <groupId>io.openliberty.guides</groupId>
            <artifactId>guide-maven-multimodules-jar</artifactId>
            <version>1.0-SNAPSHOT</version>
            <type>jar</type>
        </dependency>
        <dependency>
            <groupId>io.openliberty.guides</groupId>
            <artifactId>guide-maven-multimodules-war</artifactId>
            <version>1.0-SNAPSHOT</version>
            <type>war</type>
        </dependency>

        <!-- For tests -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.9.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-ear-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <modules>
                        <jarModule>
                            <groupId>io.openliberty.guides</groupId>
                            <artifactId>guide-maven-multimodules-jar</artifactId>
                            <uri>/guide-maven-multimodules-jar-1.0-SNAPSHOT.jar</uri>
                        </jarModule>
                        <webModule>
                            <groupId>io.openliberty.guides</groupId>
                            <artifactId>guide-maven-multimodules-war</artifactId>
                            <uri>/guide-maven-multimodules-war-1.0-SNAPSHOT.war</uri>
                            <!-- Set custom context root -->
                            <contextRoot>/converter</contextRoot>
                        </webModule>
                    </modules>
                </configuration>
            </plugin>

            <!-- Since the package type is ear,
            need to run testCompile to compile the tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>testCompile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Plugin to run integration tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.0.0</version>
                <configuration>
                    <systemPropertyVariables>
                        <default.http.port>
                            ${liberty.var.default.http.port}
                        </default.http.port>
                        <default.https.port>
                            ${liberty.var.default.https.port}
                        </default.https.port>
                        <cf.context.root>/converter</cf.context.root>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```



Set the ***basic configuration*** for the project and set the ***packaging*** element to ***ear***.

The ***Java library module*** and the ***web module*** were added as dependencies. Specify a type of ***war*** for the web module. If you don’t specify this type for the web module, Maven looks for a JAR file.

The definition and configuration of the ***maven-ear-plugin*** plug-in were added to create an EAR file. Define the ***jarModule*** and ***webModule*** modules to be packaged into the EAR file. To customize the context root of the application, set the ***contextRoot*** element to ***/converter*** in the ***webModule***. Otherwise, Maven automatically uses the WAR file ***artifactId*** ID as the context root for the application while generating the ***application.xml*** file.

To deploy and run an EAR application on an Open Liberty server, you need to provide a server configuration file.

Create the server configuration file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-maven-multimodules/start/ear/src/main/liberty/config/server.xml
```


> Then, to open the server.xml file in your IDE, select
> **File** > **Open** > guide-maven-multimodules/start/ear/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-maven-multimodules/start/ear/src/main/liberty/config/server.xml"}



```xml
<server description="Sample Liberty server">

    <featureManager>
        <feature>pages-3.1</feature>
    </featureManager>

    <variable name="default.http.port" defaultValue="9080" />
    <variable name="default.https.port" defaultValue="9443" />

    <httpEndpoint host="*" httpPort="${default.http.port}"
        httpsPort="${default.https.port}" id="defaultHttpEndpoint" />

    <enterpriseApplication id="guide-maven-multimodules-ear"
        location="guide-maven-multimodules-ear.ear"
        name="guide-maven-multimodules-ear" />
</server>
```



You must configure the ***server.xml*** file with the ***enterpriseApplication*** element to specify the location of your EAR application.


::page{title="Aggregating the entire build"}

Because you have multiple modules, aggregate the Maven projects to simplify the build process.

Create a parent ***pom.xml*** file under the ***start*** directory to link all of the child modules together. A template is provided for you.

Replace the start/POM file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-maven-multimodules/start/pom.xml, or click the following button

::openFile{path="/home/project/guide-maven-multimodules/start/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
    http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-maven-multimodules</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>jar</module>
        <module>war</module>
        <module>ear</module>
    </modules>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <artifactId>maven-war-plugin</artifactId>
                    <version>3.3.2</version>
                </plugin>
                <plugin>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.11.0</version>
                </plugin>
            </plugins>
        </pluginManagement>
        <plugins>
            <!-- Enable liberty-maven plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.7.1</version>
            </plugin>
        </plugins>
    </build>
</project>
```



Set the ***basic configuration*** for the project. Set ***pom*** as the value for the ***packaging*** element of the parent ***pom.xml*** file.

In the parent ***pom.xml*** file, list all of the ***modules*** that you want to aggregate for the application.

Adding the ***maven-war-plugin***, ***maven-compiler-plugin***, and ***liberty-maven-plugin*** plug-ins allows each child module to inherit the plug-ins, so that you can use the these to develop the modules.


::page{title="Developing the application"}

You can now develop the application and the different modules together in dev mode by using the Liberty Maven plug-in. To learn more about how to use dev mode with multiple modules, check out the [Documentation](https://github.com/OpenLiberty/ci.maven/blob/main/docs/dev.md#multiple-modules).

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-maven-multimodules/start
```

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), the server listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```bash
mvn liberty:dev
```

After you see the following message, your application server in dev mode is ready:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

### Updating the Java classes in different modules

Update the ***HeightsBean*** class to use the Java library module that implements the functions that you need for the unit converter.

Navigate to the ***start*** directory.

Replace the ***HeightsBean*** class in the ***war*** directory.

> To open the HeightsBean.java file in your IDE, select
> **File** > **Open** > guide-maven-multimodules/start/war/src/main/java/io/openliberty/guides/multimodules/web/HeightsBean.java, or click the following button

::openFile{path="/home/project/guide-maven-multimodules/start/war/src/main/java/io/openliberty/guides/multimodules/web/HeightsBean.java"}



```java
package io.openliberty.guides.multimodules.web;

public class HeightsBean implements java.io.Serializable {
    private String heightCm = null;
    private String heightFeet = null;
    private String heightInches = null;
    private int cm = 0;
    private int feet = 0;
    private int inches = 0;

    public HeightsBean() {
    }

    public String getHeightCm() {
        return heightCm;
    }

    public String getHeightFeet() {
        return heightFeet;
    }

    public String getHeightInches() {
        return heightInches;
    }

    public void setHeightCm(String heightcm) {
        this.heightCm = heightcm;
    }

    public void setHeightFeet(String heightfeet) {
        this.cm = Integer.valueOf(heightCm);
        this.feet = io.openliberty.guides.multimodules.lib.Converter.getFeet(cm);
        String result = String.valueOf(feet);
        this.heightFeet = result;
    }

    public void setHeightInches(String heightinches) {
        this.cm = Integer.valueOf(heightCm);
        this.inches = io.openliberty.guides.multimodules.lib.Converter.getInches(cm);
        String result = String.valueOf(inches);
        this.heightInches = result;
    }

}
```



The ***getFeet(cm)*** invocation was added to the ***setHeightFeet*** method to convert a measurement into feet.

The ***getInches(cm)*** invocation was added to the ***setHeightInches*** method to convert a measurement into inches.

Click the following button to check out the running application by visiting the ***/converter*** endpoint:
::startApplication{port="9080" display="external" name="Visit application" route="/converter"}

Now try updating the converter so that it converts heights correctly, rather than returning 0.

Replace the ***Converter*** class in the ***jar*** directory.

> To open the Converter.java file in your IDE, select
> **File** > **Open** > guide-maven-multimodules/start/jar/src/main/java/io/openliberty/guides/multimodules/lib/Converter.java, or click the following button

::openFile{path="/home/project/guide-maven-multimodules/start/jar/src/main/java/io/openliberty/guides/multimodules/lib/Converter.java"}



```java
package io.openliberty.guides.multimodules.lib;

public class Converter {

    public static int getFeet(int cm) {
        int feet = (int) (cm / 30.48);
        return feet;
    }

    public static int getInches(int cm) {
        double feet = cm / 30.48;
        int inches = (int) (cm / 2.54) - ((int) feet * 12);
        return inches;
    }

    public static int sum(int a, int b) {
        return a + b;
    }

    public static int diff(int a, int b) {
        return a - b;
    }

    public static int product(int a, int b) {
        return a * b;
    }

    public static int quotient(int a, int b) {
        return a / b;
    }

}
```



Change the ***getFeet*** method so that it converts from centimetres to feet, and the ***getInches*** method so that it converts from centimetres to inches. Update the ***sum***, ***diff***, ***product*** and ***quotient*** functions so that they add, subtract, multiply, and divide 2 numbers respectively.

Now revisit the application by visiting the ***/converter*** endpoint:
::startApplication{port="9080" display="external" name="Visit application" route="/converter"}

Try entering a height in centimetres and see if it converts correctly.


### Testing the multi-module application

To test the multi-module application, add integration tests to the EAR project.

Create the integration test class in the ***ear*** directory.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-maven-multimodules/start/ear/src/test/java/it/io/openliberty/guides/multimodules/IT.java
```


> Then, to open the IT.java file in your IDE, select
> **File** > **Open** > guide-maven-multimodules/start/ear/src/test/java/it/io/openliberty/guides/multimodules/IT.java, or click the following button

::openFile{path="/home/project/guide-maven-multimodules/start/ear/src/test/java/it/io/openliberty/guides/multimodules/IT.java"}



```java
package it.io.openliberty.guides.multimodules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.Test;

public class IT {
    String port = System.getProperty("default.http.port");
    String war = "converter";
    String urlBase = "http://localhost:" + port + "/" + war + "/";

    @Test
    public void testIndexPage() throws Exception {
        String url = this.urlBase;
        HttpURLConnection con = testRequestHelper(url, "GET");
        assertEquals(200, con.getResponseCode(), "Incorrect response code from " + url);
        assertTrue(testBufferHelper(con).contains("Enter the height in centimeters"),
                        "Incorrect response from " + url);
    }

    @Test
    public void testHeightsPage() throws Exception {
        String url = this.urlBase + "heights.jsp?heightCm=10";
        HttpURLConnection con = testRequestHelper(url, "POST");
        assertTrue(testBufferHelper(con).contains("3        inches"),
                        "Incorrect response from " + url);
    }

    private HttpURLConnection testRequestHelper(String url, String method)
                    throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod(method);
        return con;
    }

    private String testBufferHelper(HttpURLConnection con) throws Exception {
        BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

}
```



The ***testIndexPage*** tests to check that you can access the landing page.

The ***testHeightsPage*** tests to check that the application can process the input value and calculate the result correctly.


### Running the tests

Because you started Open Liberty in dev mode, press the *enter/return* key to run the tests.

You will see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.multimodules.IT
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.712 sec - in it.io.openliberty.guides.multimodules.IT

Results :

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

```


When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran the server, or by typing *q* and then pressing the *enter/return* key.


::page{title="Building the multi-module application"}

You aggregated and developed the application. Now, you can run ***mvn install*** once from the ***start*** directory and it will automatically build all your modules. This command creates a JAR file in the ***jar/target*** directory, a WAR file in the ***war/target*** directory, and an EAR file that contains the JAR and WAR files in the ***ear/target*** directory.

Run the following commands to navigate to the start directory and build the entire application:
```bash
cd /home/project/guide-maven-multimodules/start
mvn install
```

Since the modules are independent, you can re-build them individually by running ***mvn install*** from the corresponding ***start*** directory for each module.

Or, run `mvn -pl <child project> install` from the start directory.


::page{title="Summary"}

### Nice Work!

You built and tested a multi-module Java application for unit conversion with Maven on Open Liberty.




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-maven-multimodules*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-maven-multimodules
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Creating%20a%20multi-module%20application&guide-id=cloud-hosted-guide-maven-multimodules)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-maven-multimodules/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-maven-multimodules/pulls)



### Where to next?

* [Building a web application with Maven](https://openliberty.io/guides/maven-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
