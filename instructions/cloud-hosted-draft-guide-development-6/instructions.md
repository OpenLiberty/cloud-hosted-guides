
# **Welcome to the Creating a multi-module application guide!**

You will learn how to build an application with multiple modules with Maven and Open Liberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




# **What you'll learn**

A Java Platform, Enterprise Edition (Java EE) application consists of modules that work together as one entity. An enterprise archive (EAR) is a wrapper for a Java EE application, which consists of web archive (WAR) and Java archive (JAR) files. To deploy or distribute the Java EE application into new environments, all the modules and resources must first be packaged into an EAR file.

You will learn how to establish a dependency between a web module and a Java library module. You will use Maven to package the WAR file and the JAR file into an EAR file so that you can run and test the application on Open Liberty.

You will build a unit converter application that converts heights from centimeters into feet and inches. The application will request the user to enter a height value in centimeters. Then, the application processes the input by using functions that are found in the JAR file to return the height value in imperial units.



# **Getting started**

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-maven-multimodules.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-maven-multimodules.git
cd guide-maven-multimodules
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.

Access partial implementation of the application from the **start** folder. This folder includes a web module in the **war** folder, a Java library in the **jar** folder, and template files in the **ear** folder. However, the Java library and the web module are independent projects, and you will need to complete the following steps to implement the application:

1. Add a dependency relationship between the two modules.

2. Assemble the entire application into an EAR file.

3. Aggregate the entire build.

4. Test the multi-module application.

<br/>
### **Try what you'll build**

The **finish** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the **finish** directory and run the following
Maven goal to build the application:

```
cd finish
mvn install
```
{: codeblock}



To deploy your EAR application on an Open Liberty server, run the Maven **liberty:run** goal from the **ear** directory:

```
cd ear
mvn liberty:run
```
{: codeblock}





Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.


Once the server is running, you can find the application at the following URL: http://localhost:9080/converter/


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl http://localhost:9080/converter/
```
{: codeblock}



After you are finished checking out the application, stop the Open Liberty server by pressing **CTRL+C** in the command-line session where you ran the server. Alternatively, you can run the **liberty:stop** goal from the **finish\ear** directory in another command-line session:

```
mvn liberty:stop
```
{: codeblock}




# **Adding dependencies between WAR and JAR modules**

To use a Java library in your web module, you must add a dependency relationship between the two modules.

As you might have noticed, each module has its own **pom.xml** file. Each module has its own **pom.xml** file because each module is treated as an independent project. You can rebuild, reuse, and reassemble every module on its own.

Navigate to the **start** directory to begin.

Replace the war/POM file.

> From the menu of the IDE, select 
> **File** > **Open** > guide-maven-multimodules/start/war/pom.xml




```
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
    http://maven.apache.org/xsd/maven-4.0.0.xsd">

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
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <dependencies>

        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>8.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>4.0.1</version>
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
{: codeblock}


The **dependency** element is the Java library module that implements the functions that you need for the unit converter.

With this dependency, you can use any functions included in the library in the **HeightsBean.java** file of the web module.

Replace the **HeightsBean** class.

> From the menu of the IDE, select 
> **File** > **Open** > guide-maven-multimodules/start/war/src/main/java/io/openliberty/guides/multimodules/web/HeightsBean.java




```
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
{: codeblock}



The **getFeet(cm)** invocation was added to the **setHeightFeet** method to convert a measurement into feet.

The **getInches(cm)** invocation was added to the **setHeightInches** method to convert a measurement into inches.



# **Assembling multiple modules into an EAR file**

To deploy the entire application on the Open Liberty server, first package the application. Use the EAR project to assemble multiple modules into an EAR file.

Navigate to the **ear** folder and find a template **pom.xml** file.
Replace the ear/POM file.

> From the menu of the IDE, select 
> **File** > **Open** > guide-maven-multimodules/start/ear/pom.xml




```
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
    http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-maven-multimodules-ear</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>ear</packaging>
    <!-- end::packaging[] -->

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <liberty.var.default.http.port>9080</liberty.var.default.http.port>
        <liberty.var.default.https.port>9443</liberty.var.default.https.port>
    </properties>

    <dependencies>
        <!-- tag::dependencies[] -->
        <dependency>
            <groupId>io.openliberty.guides</groupId>
            <artifactId>guide-maven-multimodules-jar</artifactId>
            <version>1.0-SNAPSHOT</version>
            <type>jar</type>
        </dependency>
        <!-- tag::dependency-war[] -->
        <dependency>
            <groupId>io.openliberty.guides</groupId>
            <artifactId>guide-maven-multimodules-war</artifactId>
            <version>1.0-SNAPSHOT</version>
            <type>war</type>
        </dependency>
        <!-- end::dependencies[] -->

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.7.1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-ear-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <modules>
                        <jarModule>
                            <groupId>io.openliberty.guides</groupId>
                            <artifactId>guide-maven-multimodules-jar</artifactId>
                            <uri>/guide-maven-multimodules-jar-1.0-SNAPSHOT.jar</uri>
                        </jarModule>
                        <!-- tag::webModule[] -->
                        <webModule>
                            <groupId>io.openliberty.guides</groupId>
                            <artifactId>guide-maven-multimodules-war</artifactId>
                            <uri>/guide-maven-multimodules-war-1.0-SNAPSHOT.war</uri>
                            <!-- tag::contextRoot[] -->
                            <contextRoot>/converter</contextRoot>
                        </webModule>
                    </modules>
                </configuration>
            </plugin>

            <!-- tag::liberty-maven-plugin[] -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.3.4</version>
            </plugin>

            <!-- Since the package type is ear,
            need to run testCompile to compile the tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <executions>
                    <execution>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>testCompile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>2.22.2</version>
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
{: codeblock}



Set the **basic configuration** for the project and set the **packaging** element to **ear**.

The **Java library module** and the **web module** were added as dependencies. Specify a type of **war** for the web module. If you don’t specify this type for the web module, Maven looks for a JAR file.

The definition and configuration of the **maven-ear-plugin** plug-in were added to create an EAR file. Define the **jarModule** and **webModule** modules to be packaged into the EAR file.
To customize the context root of the application, set the **contextRoot** element to **/converter** in the **webModule**. Otherwise, Maven automatically uses the WAR file **artifactId** ID as the context root for the application while generating the **application.xml** file.

To download and start an Open Liberty server, use the **liberty-maven-plugin** plug-in for Maven. This configuration is provided, and the executions of the plug-in follow the typical phases of a Maven life cycle.

To deploy and run an EAR application on an Open Liberty server, you need to provide a server configuration file.

Create the server configuration file.

> Run the following touch command in your terminal
```
touch /home/project/guide-maven-multimodules/start/ear/src/main/liberty/config/server.xml
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-maven-multimodules/start/ear/src/main/liberty/config/server.xml




```
<server description="Sample Liberty server">

    <featureManager>
        <feature>jsp-2.3</feature>
    </featureManager>

    <variable name="default.http.port" defaultValue="9080" />
    <variable name="default.https.port" defaultValue="9443" />

    <httpEndpoint host="*" httpPort="${default.http.port}"
        httpsPort="${default.https.port}" id="defaultHttpEndpoint" />

    <enterpriseApplication id="guide-maven-multimodules-ear"
        location="guide-maven-multimodules-ear.ear"
        name="guide-maven-multimodules-ear" />
    <!-- end::server[] -->
</server>
```
{: codeblock}



You must configure the **server.xml** file with the **enterpriseApplication** element to specify the location of your EAR application.


# **Aggregating the entire build**

Because you have multiple modules, aggregate the Maven projects to simplify the build process.

Create a parent **pom.xml** file under the **start** directory to link all of the child modules together. A template is provided for you.

Replace the start/POM file.

> From the menu of the IDE, select 
> **File** > **Open** > guide-maven-multimodules/start/pom.xml




```
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
    http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <!-- tag::groupId[] -->
    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-maven-multimodules</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <!-- end::packaging[] -->

    <modules>
        <module>jar</module>
        <module>war</module>
        <module>ear</module>
    </modules>
</project>
```
{: codeblock}


Set the **basic configuration** for the project. Set **pom** as the **packaging** element of the parent **pom.xml** file.

In the parent **pom.xml** file, list all of the **modules** that you want to aggregate for the application.



# **Building the modules**

By aggregating the build in the previous section, you can run **mvn install** once from the **start** directory and it will automatically build all your modules. This command creates a JAR file in the **jar/target** directory, a WAR file in the **war/target** directory, and an EAR file in the **ear/target** directory, which contains the JAR and WAR files.

Use the following command to build the entire application from the **start** directory:

```
mvn install
```
{: codeblock}



Since the modules are independent, you can re-build them individually by running **mvn install** from the corresponding module directory.


# **Starting the application**

To deploy your EAR application on an Open Liberty server, run the Maven **liberty:run** goal from the **ear** directory:

```
cd ear
mvn liberty:run
```
{: codeblock}




Once the server is running, you can find the application at the following URL: http://localhost:9080/converter/


_To see the output for this URL in the IDE, run the following command at a terminal:_

```
curl http://localhost:9080/converter/
```
{: codeblock}



After you are finished checking out the application, stop the Open Liberty server by pressing **CTRL+C** in the command-line session where you ran the server. Alternatively, you can run the **liberty:stop** goal from the **start\ear** directory in another command-line session:

```
mvn liberty:stop
```
{: codeblock}





# **Testing the multi-module application**

To test the multi-module application, add integration tests to the EAR project.

Navigate to the **start\ear** directory.

Create the integration test class.

> Run the following touch command in your terminal
```
touch /home/project/guide-maven-multimodules/start/src/test/java/it/io/openliberty/guides/multimodules/IT.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-maven-multimodules/start/src/test/java/it/io/openliberty/guides/multimodules/IT.java




```
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
{: codeblock}



The **testIndexPage** tests to check that you can access the landing page.

The **testHeightsPage** tests to check that the application can process the input value and calculate the result correctly.

For a Maven EAR project, the **testCompile** goal is
specified for the **maven-compiler-plugin**
plug-in in your **ear/pom.xml** file so that the test cases are
compiled and picked up for execution. Run the following command to compile the
test class:

```
mvn test-compile
```
{: codeblock}




Then, enter the following command to start the server, run the tests, then stop the server:

```
mvn liberty:start failsafe:integration-test liberty:stop
```
{: codeblock}



```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.multimodules.IT
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.712 sec - in it.io.openliberty.guides.multimodules.IT

Results :

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

```


# **Summary**

## **Nice Work!**

You built and tested a multi-module unit converter application with Maven on Open Liberty.




<br/>
## **Clean up your environment**


Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-maven-multimodules** project by running the following commands:

```
cd /home/project
rm -fr guide-maven-multimodules
```
{: codeblock}

<br/>
## **What did you think of this guide?**

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Creating%20a%20multi-module%20application&guide-id=cloud-hosted-guide-maven-multimodules)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

<br/>
## **What could make this guide better?**

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-maven-multimodules/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-maven-multimodules/pulls)



<br/>
## **Where to next?**

* [Building a web application with Maven](https://openliberty.io/guides/maven-intro.html)


<br/>
## **Log out of the session**

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.