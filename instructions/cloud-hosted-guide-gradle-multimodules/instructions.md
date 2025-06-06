---
markdown-version: v1
tool-type: theia
---
::page{title="Welcome to the Creating a multi-module application with Gradle guide!"}

You will learn how to build an application with multiple modules with Gradle and Open Liberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

A Jakarta EE application consists of modules that work together as one entity. An enterprise archive (EAR) is a wrapper for a Jakarta EE application, which consists of web archive (WAR) and Java archive (JAR) files. To deploy or distribute the Jakarta EE application into new environments, all the modules and resources must first be packaged into an EAR file.

In this guide, you will learn how to:

* establish a dependency between a web module and a Java library module,
* use Gradle to package the WAR file and the JAR file into an EAR file so that you can run and test the application on Open Liberty, and
 use the Liberty Gradle plug-in to develop a multi-module application in [dev mode](https://openliberty.io/docs/latest/development-mode.html#_run_multi_module_gradle_projects_in_dev_mode) without having to prebuild the JAR and WAR files. In dev mode, your changes are automatically picked up by the running Liberty instance.

You will build a unit converter application that converts heights from centimeters into feet and inches. The application prompts the user to enter a height value in centimeters. Then, the application processes the input by using functions that are found in the JAR file to return the height value in imperial units.



::page{title="Getting started"}

To open a new command-line session,
select ***Terminal*** > ***New Terminal*** from the menu of the IDE.

Run the following command to navigate to the ***/home/project*** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-gradle-multimodules.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-gradle-multimodules.git
cd guide-gradle-multimodules
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

You can access a partial implementation of the application from the ***start*** folder. This folder includes a web module in the ***war*** folder, a Java library in the ***jar*** folder, and template files in the ***ear*** folder. However, the Java library and the web module are independent projects, and you will need to complete the following steps to implement the application:

1. Add a dependency relationship between the two modules.

2. Assemble the entire application into an EAR file.

3. Aggregate the entire build.

4. Test the multi-module application.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the ***finish*** directory and run the following Gradle task to build the application:

```bash
cd finish
./gradlew libertyPackage
```

To deploy your EAR application on Open Liberty, run the Gradle ***libertyRun*** task from the ***finish*** directory.

```bash
./gradlew libertyRun
```

After you see the following message, your Liberty instance is ready:

```
The sampleLibertyServer server is ready to run a smarter planet.
```

When the Liberty instance is running, click the following button to check out your service by visiting the ***/converter*** endpoint.
::startApplication{port="9080" display="external" name="Visit application" route="/converter"}

After you finish checking out the application, stop the Open Liberty instance by pressing **CTRL+C** in the command-line session where you ran Liberty. Alternatively, you can run the ***libertyStop*** task from the ***finish*** directory in another command-line session:

```bash
./gradlew libertyStop
```


::page{title="Adding dependencies between WAR and JAR modules"}

To use a Java library in your web module, you must add a dependency relationship between the two modules.

As you might have noticed, each module has its own ***build.gradle*** file because each module is treated as an independent project. You can rebuild, reuse, and reassemble every module on its own.

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-gradle-multimodules/start
```

Replace the war/build.gradle file.

> To open the build.gradle file in your IDE, select
> ***File*** > ***Open*** > guide-gradle-multimodules/start/war/build.gradle, or click the following button

::openFile{path="/home/project/guide-gradle-multimodules/start/war/build.gradle"}



```
apply plugin: 'war'

description = 'WAR Module'

dependencies {
    implementation project(':jar')
    compileOnly 'jakarta.platform:jakarta.jakartaee-api:10.0.0'
    compileOnly 'org.eclipse.microprofile:microprofile:7.0'
}

war {
    archiveFileName = rootProject.name + '-' + getArchiveBaseName().get() + '-' +
                      rootProject.version + '.' + getArchiveExtension().get()
}

war.dependsOn ':jar:jar'

```


Click the :fa-copy: ***Copy*** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.

The added ***project*** dependency and ***dependsOn*** element declare the Java library project and module that implements the functions that you need for the unit converter.


::page{title="Assembling multiple modules into an EAR file"}

To deploy the entire application on Open Liberty, first package the application. Use the EAR project to assemble multiple modules into an EAR file.

Navigate to the ***ear*** folder and find a template ***build.gradle*** file.
Replace the ear/build.gradle file.

> To open the build.gradle file in your IDE, select
> ***File*** > ***Open*** > guide-gradle-multimodules/start/ear/build.gradle, or click the following button

::openFile{path="/home/project/guide-gradle-multimodules/start/ear/build.gradle"}



```
apply plugin: 'ear'
apply plugin: 'liberty'

description = 'EAR Module'

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            name = 'Sonatype Nexus Snapshots'
            url = 'https://oss.sonatype.org/content/repositories/snapshots/'
        }
    }
    dependencies {
        classpath 'io.openliberty.tools:liberty-gradle-plugin:3.9.3'
    }
}


dependencies {
    deploy project(path:':war', configuration:'archives')
}

ear {
    archiveFileName = rootProject.name + '-' + getArchiveBaseName().get() + '-' +
                      rootProject.version + '.' + getArchiveExtension().get()
    deploymentDescriptor {
        webModule ('guide-gradle-multimodules-war-1.0-SNAPSHOT.war', '/converter')
    }
}

liberty {
    server {
        name = 'sampleLibertyServer'
        deploy {
            apps = [ear]
            copyLibsDirectory = file("${project.getLayout().getBuildDirectory().getAsFile().get()}/libs")
        }
        var.'http.port' = '9080'
        var.'https.port' = '9443'
        verifyAppStartTimeout = 30
        looseApplication = true
    }
}

test {
    systemProperty 'http.port', liberty.server.var.'http.port'
    enabled = gradle.startParameter.taskNames.contains('test')
}

deploy.dependsOn 'ear'
ear.dependsOn ':jar:jar', ':war:war'
```



Let's look at what each section does.

The first two lines specify that you want to use the ***ear*** and ***liberty*** plug-ins for Gradle. The ***ear*** section configures the ***ear*** task with the deployment descriptor that provides the web module file name and the context root as ***/converter***.

If no context path is specified, Gradle automatically uses the WAR file artifact ID as the context root for the application, when the ***application.xml*** file is being generated. The default artifact ID is ***{projectName}-{moduleName}-{version}***, like ***guide-gradle-multimodules-war-1.0-SNAPSHOT***.

The ***liberty*** section configures the ***liberty*** task that creates the Liberty server name as ***sampleLibertyServer*** and deploys the ear file from the path that is specified by the ***copyLibsDirectory*** variable.

The ***war*** project is added as a project dependency. The ***deploy*** task depends on the ***ear*** task and the ***ear*** task depends on the ***jar*** task from the ***jar*** project and the ***war*** task from the ***war*** project.

To deploy and run an EAR application on an Open Liberty instance, you need to provide a Liberty ***server.xml*** configuration file.

Create the Liberty ***server.xml*** configuration file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-gradle-multimodules/start/ear/src/main/liberty/config/server.xml
```


> Then, to open the server.xml file in your IDE, select
> ***File*** > ***Open*** > guide-gradle-multimodules/start/ear/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-gradle-multimodules/start/ear/src/main/liberty/config/server.xml"}



```xml
<server description="Sample Liberty server">

    <featureManager>
        <platform>jakartaee-10.0</platform>
        <feature>pages</feature>
    </featureManager>

    <variable name="http.port" defaultValue="9080" />
    <variable name="https.port" defaultValue="9443" />

    <httpEndpoint host="*" httpPort="${http.port}"
        httpsPort="${https.port}" id="defaultHttpEndpoint" />

    <enterpriseApplication id="guide-gradle-multimodules-ear"
                           location="guide-gradle-multimodules-ear-1.0-SNAPSHOT.ear"
                           name="guide-gradle-multimodules-ear">
    </enterpriseApplication>
</server>
```



The ***server.xml*** configuration file configures with the ***enterpriseApplication*** element to specify the location of your EAR application.


::page{title="Aggregating the entire build"}

Because you have multiple modules, aggregate the Gradle projects to simplify the build process.

Replace the settings.gradle file.

> To open the settings.gradle file in your IDE, select
> ***File*** > ***Open*** > guide-gradle-multimodules/start/settings.gradle, or click the following button

::openFile{path="/home/project/guide-gradle-multimodules/start/settings.gradle"}



```
rootProject.name = 'guide-gradle-multimodules'
include ':jar'
include ':war'
include ':ear'

project(':jar').projectDir = "$rootDir/jar" as File
project(':war').projectDir = "$rootDir/war" as File
project(':ear').projectDir = "$rootDir/ear" as File
```



The ***settings.gradle*** file is used to specify multiple modules that includes the ***jar***, ***war***, and ***ear*** projects and their directories.

Create a parent ***build.gradle*** file under the ***start*** directory to link all of the child modules together. A template is provided for you.

Replace the build.gradle file.

> To open the build.gradle file in your IDE, select
> ***File*** > ***Open*** > guide-gradle-multimodules/start/build.gradle, or click the following button

::openFile{path="/home/project/guide-gradle-multimodules/start/build.gradle"}



```
allprojects  {
    group = 'io.openliberty.guides'
    version = '1.0-SNAPSHOT'
}

subprojects {
    apply plugin: 'java'

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType(JavaCompile).configureEach {
        options.encoding = 'UTF-8'
        options.release.set(11)
    }

    test {
        useJUnitPlatform()
    }

    dependencies {
        testImplementation platform('org.junit:junit-bom:5.13.0')
        testImplementation 'org.junit.jupiter:junit-jupiter'
        testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

}
```



The ***allprojects*** task sets the basic configuration for the project. The ***subprojects*** task applies the ***java*** plug-in with its ***options*** and ***junit*** dependencies to all subprojects. The plug-ins and dependencies will be downloaded from the repositories that are specified in the ***repositories*** configuration. Each child module inherits all the configurations.




::page{title="Developing the application"}

You can now develop the application and the different modules together in dev mode by using the Liberty Gradle plug-in. To learn more about how to use dev mode with multiple modules, check out the [Documentation](https://github.com/OpenLiberty/ci.gradle/blob/main/docs/libertyDev.md#multi-project-builds).

```bash
cd /home/project/guide-gradle-multimodules/start
```

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), dev mode listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following task to start Open Liberty in dev mode:

```bash
./gradlew libertyDev
```

After you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

### Updating the Java classes in different modules

To get the height conversion working correctly in the application, you'll need to update two Java classes: one in the web module and one in the library module. 

First, update the ***HeightsBean*** class to use the Java library module that implements the functions that you need for the unit converter.

Replace the ***HeightsBean*** class in the ***war*** directory.

> To open the HeightsBean.java file in your IDE, select
> ***File*** > ***Open*** > guide-gradle-multimodules/start/war/src/main/java/io/openliberty/guides/multimodules/web/HeightsBean.java, or click the following button

::openFile{path="/home/project/guide-gradle-multimodules/start/war/src/main/java/io/openliberty/guides/multimodules/web/HeightsBean.java"}



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



The ***getFeet(cm)*** invocation is added to the ***setHeightFeet*** method to convert a measurement into feet.

The ***getInches(cm)*** invocation is added to the ***setHeightInches*** method to convert a measurement into inches.

Click the following button to check out the running application by visiting the ***/converter*** endpoint:
::startApplication{port="9080" display="external" name="Visit application" route="/converter"}

Note that the application currently returns 0 for height conversions because the logic in the converter hasn't been implemented yet. You'll fix this by updating the converter in the following step.

Replace the ***Converter*** class in the ***jar*** directory.

> To open the Converter.java file in your IDE, select
> ***File*** > ***Open*** > guide-gradle-multimodules/start/jar/src/main/java/io/openliberty/guides/multimodules/lib/Converter.java, or click the following button

::openFile{path="/home/project/guide-gradle-multimodules/start/jar/src/main/java/io/openliberty/guides/multimodules/lib/Converter.java"}



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



The ***getFeet*** method is changed to convert the ***cm*** integer parameter from centimeters to feet, and the ***getInches*** method to convert the ***cm*** integer parameter from centimeters to inches. The ***sum***, ***diff***, ***product***, and ***quotient*** methods are updated to add, subtract, multiply, and divide 2 numbers respectively.

Now check out the application again at the ***/converter*** endpoint:
::startApplication{port="9080" display="external" name="Check out the application" route="/converter"}

Try entering a height in centimeters and see if it converts correctly.

### Testing the multi-module application

To test the multi-module application, add integration tests to the EAR project.

Create the integration test class in the ***ear*** directory.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-gradle-multimodules/start/ear/src/test/java/it/io/openliberty/guides/multimodules/IT.java
```


> Then, to open the IT.java file in your IDE, select
> ***File*** > ***Open*** > guide-gradle-multimodules/start/ear/src/test/java/it/io/openliberty/guides/multimodules/IT.java, or click the following button

::openFile{path="/home/project/guide-gradle-multimodules/start/ear/src/test/java/it/io/openliberty/guides/multimodules/IT.java"}



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
    String port = System.getProperty("http.port");
    String war = "converter";
    String urlBase = "http://localhost:" + port + "/" + war + "/";

    @Test
    public void testIndexPage() throws Exception {
        String url = this.urlBase;
        HttpURLConnection con = testRequestHelper(url, "GET");
        assertEquals(200, con.getResponseCode(), "Incorrect response code from " + url);
        assertTrue(testBufferHelper(con).contains("Enter the height in centimeter"),
                        "Incorrect response from " + url);
    }

    @Test
    public void testHeightsPage() throws Exception {
        String url = this.urlBase + "heights.jsp?heightCm=10";
        HttpURLConnection con = testRequestHelper(url, "POST");
        assertTrue(testBufferHelper(con).contains("3        in"),
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


The ***test*** task configuration is already included in the ***ear/build.gradle*** file for you. It passes the same ***http.port*** value that is used by the Liberty server to the tests so they can connect to the application during execution.

### Running the tests

Because you started Open Liberty in dev mode, press the **enter/return** key to run the tests.

You will see the following output:

```
Running tests...

> Task :ear:cleanTest
> Task :jar:cleanTest
> Task :war:cleanTest
> Task :ear:compileJava NO-SOURCE
> Task :ear:processResources NO-SOURCE
> Task :ear:classes UP-TO-DATE
> Task :ear:compileTestJava UP-TO-DATE
> Task :ear:processTestResources NO-SOURCE
> Task :ear:testClasses UP-TO-DATE
> Task :ear:test
> Task :jar:compileJava UP-TO-DATE
> Task :jar:processResources NO-SOURCE
> Task :jar:classes UP-TO-DATE
> Task :jar:jar UP-TO-DATE
> Task :jar:compileTestJava UP-TO-DATE
> Task :jar:processTestResources NO-SOURCE
> Task :jar:testClasses UP-TO-DATE
> Task :jar:test
> Task :war:compileJava UP-TO-DATE
> Task :war:processResources NO-SOURCE
> Task :war:classes UP-TO-DATE
> Task :war:compileTestJava UP-TO-DATE
> Task :war:processTestResources NO-SOURCE
> Task :war:testClasses UP-TO-DATE
> Task :war:test

BUILD SUCCESSFUL in 3s
12 actionable tasks: 6 executed, 6 up-to-date

> Task :ear:libertyDev
Tests finished.
```

You can find the test result of each module from their build directory:

* ***jar/build/reports/tests/test/index.html***
* ***ear/build/reports/tests/test/index.html***
* ***war/build/reports/tests/test/index.html***

When you are done checking out the service, exit dev mode by pressing **CTRL+C** in the command-line session where you ran Liberty.


::page{title="Building the multi-module application"}

You aggregated and developed the application. Now, you can run ***./gradlew clean libertyPackage*** from the ***start*** directory to build all your modules. This command creates a JAR file in the ***jar/build/libs*** directory, a WAR file in the ***war/build/libs*** directory, and an EAR file that contains the WAR file in the ***ear/build/libs*** directory.

Run the following commands to navigate to the `start` directory and build the entire application:
```bash
cd /home/project/guide-gradle-multimodules/start
./gradlew clean libertyPackage
```

Because the modules are independent, you can rebuild them individually by running ***./gradlew clean build*** from the corresponding ***start*** directory for each module.

Or, run `./gradlew <child project>:build` from the `start` directory.


::page{title="Summary"}

### Nice Work!

You built and tested a multi-module Java application for unit conversion with Gradle on Open Liberty.




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-gradle-multimodules*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-gradle-multimodules
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Creating%20a%20multi-module%20application%20with%20Gradle&guide-id=cloud-hosted-guide-gradle-multimodules)

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-gradle-multimodules/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-gradle-multimodules/pulls)



### Where to next?

* [Building a web application with Gradle](https://openliberty.io/guides/gradle-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
