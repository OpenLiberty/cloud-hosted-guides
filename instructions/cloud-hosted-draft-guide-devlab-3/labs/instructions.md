---
markdown-version: v1
title: instructions
branch: lab-5932-instruction
version-history-start-date: 2023-04-14T18:24:15Z
tool-type: theia
---
::page{title="Welcome to the Building a web application with Gradle guide!"}

Learn how to build and test a simple web application using Gradle and Open Liberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You will learn how to build and test a simple web servlet application using the Gradle ***war*** 
plug-in and the Liberty Gradle plug-in. The ***war*** plug-in compiles and builds the application 
code. The [***liberty*** Gradle plug-in](https://github.com/WASdev/ci.gradle/blob/main/README.md) 
installs the Open Liberty runtime, creates a server, and installs the application to run and test.
The application displays a simple web page with a link. When you click that link, the application 
calls the servlet to return a simple response of ***Hello! Is Gradle working for you?***.

One benefit of using a build tool like Gradle is that you can define the details of the project and any dependencies it has, and Gradle automatically downloads and installs the dependencies.
Another benefit of using Gradle is that it can run repeatable, automated tests on the application. 
You can, of course, test your application manually by starting a server and pointing a web browser at the application URL. 
However, automated tests are a much better approach because you can easily rerun the same tests each time the application is built.
If the tests don't pass after you change the application, the build fails, and you know that you introduced a regression that requires a fix to your code.

Choosing a build tool often comes down to personal or organizational preference, but you might choose to use Gradle for several reasons. 
Gradle defines its builds by using [Groovy build scripts](https://docs.gradle.org/current/userguide/writing_build_scripts.html), which gives you a lot of control and customization in your builds. 
Gradle also uses a build cache that rebuilds only the parts of your application that changed, which saves build time in larger projects.
So Gradle can be a good choice in larger, more complex projects.

Using this guide, you will create a Gradle build definition file (***build.gradle***) for the 
web application project, and use it to build the application. You will then create a simple, 
automated test, and configure Gradle to run it after building the application.

Learn more about Gradle on the [official Gradle website](https://docs.gradle.org/current/userguide/userguide.html).

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-gradle-intro.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-gradle-intro.git
cd guide-gradle-intro
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

::page{title="Creating the application"}

The web application that you will build using Gradle and Open Liberty is provided for you 
in the ***start*** directory so that you can focus on learning about Gradle. The application uses 
the standard Gradle directory structure. Using this directory structure saves you from 
customizing the ***build.gradle*** file later.

All the application source code, including the Open Liberty server configuration (***server.xml***), 
is in the ***start/src*** directory:

```

└── src
    └── main
        └── java
        └── liberty
            └── config
        └── webapp
            └── WEB-INF
```

::page{title="Testing Gradle"}

If you do not have Gradle installed, make sure that the ***JAVA_HOME*** environment variable is set, or that the Java application can run. Running the Gradle Wrapper automatically installs Gradle. To learn more about the Gradle Wrapper, see the [Gradle Wrapper documentation](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

Run the following commands to navigate to the ***start*** directory and verify that Gradle was installed correctly:


```bash
cd start
./gradlew -v
```

You should see information about the Gradle installation similar to this example:

```
------------------------------------------------------------
Gradle 7.6
------------------------------------------------------------

Build time:   2022-11-25 13:35:10 UTC
Revision:     daece9dbc5b79370cc8e4fd6fe4b2cd400e150a8

Kotlin:       1.7.10
Groovy:       3.0.13
Ant:          Apache Ant(TM) version 1.10.11 compiled on July 10 2021
JVM:          11.0.12 (Eclipse OpenJ9 openj9-0.27.0)
OS:           Mac OS X 12.6.3 x86_64

```


::page{title="Configure your project"}



The project configuration is defined in the Gradle settings and build files.
You will create these project configurations one section at a time. 

Gradle [settings](https://docs.gradle.org/current/dsl/org.gradle.api.initialization.Settings.html) 
are used to instantiate and configure the project. This sample uses the ***settings.gradle*** 
to name the project ***GradleSample***.

Create the Gradle settings file in the ***start*** directory.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-gradle-intro/start/settings.gradle
```


> Then, to open the unknown file in your IDE, select
> **File** > **Open** > guide-gradle-intro/start/unknown, or click the following button

::openFile{path="/home/project/guide-gradle-intro/start/unknown"}


This ***settings.gradle*** file isn't required for a single-module Gradle project. 
Without this definition, by default, the project name is set as the name of the folder in 
which it is contained (***start*** for this example).

Let's go through the ***build.gradle*** 
file so that you understand each part.

| *Configuration*       |   *Purpose*
| ---| ---
| Plug-ins used         |   The first part of the build file specifies the plug-ins required to 
                            build the project and some basic project configuration.
| buildscript           |   Where to find plug-ins for download.
| repositories          |   Where to find dependencies for download.
| dependencies          |   Java dependencies that are required for compiling, testing, 
                            and running the application are included here.
| ext                   |   Gradle extra properties extension for project level properties.
| test                  |   Unit test and integration test configuration.


Create the build file in the ***start*** directory.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-gradle-intro/start/build.gradle
```


> Then, to open the unknown file in your IDE, select
> **File** > **Open** > guide-gradle-intro/start/unknown, or click the following button

::openFile{path="/home/project/guide-gradle-intro/start/unknown"}


The first section of code defines the ***war*** and ***liberty*** plug-ins 
that you want to use. The ***war*** plug-in contains all the tasks to compile 
Java files, build the WAR file structure, and assemble the archive. The ***liberty*** 
plug-in contains the tasks used to install the Liberty runtime and create and manage 
servers. The compatibility and encoding settings are for Java.

The ***buildscript*** section defines plug-in versions to use in the 
build and where to find them. This guide uses the ***liberty*** plug-in, 
which is available from the ***Maven Central Repository***.

The ***repositories*** section defines where to find the dependencies 
that you are using in the build. For this build, everything you need is in ***Maven Central***.

The ***dependencies*** section defines what is needed to compile and 
test the code. This section also defines how to run the application. The 
***providedCompile*** dependencies are APIs that are needed to compile the 
application, but they do not need to be packaged with the application because Open Liberty 
provides their implementation at run time. The ***testImplementation*** dependencies 
are needed to compile and run tests.

The Gradle ***extra properties*** extension allows you to add properties to a Gradle project.
If you use a value more than once in your build file, you can simplify updates by defining 
it as a variable here and referring to the variable later in the build file.
This project defines variables for the application ports and the context-root.

You can view the default and Liberty tasks available by running the following command:


```
./gradlew tasks
```

::page{title="Running the application"}

Start Open Liberty in development mode, which starts the Open Liberty server and listens for file changes:

```bash
./gradlew libertyDev
```

After you see the following message, your application server in development mode is ready.

```
**********************************************
*    Liberty is running in dev mode.
```

The development mode holds your command prompt to listen for file changes.
You need to open another command prompt to continue, or simply open the project in your editor.



Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.


Navigate your browser to the ***http\://localhost:9080/GradleSample/servlet*** URL to access the application. 


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl http://localhost:9080/GradleSample/servlet
```


The servlet returns a simple response of ***Hello! Is Gradle working for you?***.

::page{title="Testing the web application"}




One of the benefits of building an application with a build system like Gradle is that 
it can be configured to run a set of automated tests. The ***war*** 


Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.

plug-in extends the [Java plug-in](https://docs.gradle.org/current/userguide/java_plugin.html), 
which provides test tasks. You can write tests for the individual units of code outside 
of a running application server (unit tests), or you can write them to call the application 
that runs on the server (integration tests). In this example, you will create a simple 
integration test that checks that the web page opens and that the correct response is 
returned when the link is clicked.

Create the ***EndpointIT*** test class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-gradle-intro/start/src/test/java/io/openliberty/guides/hello/it/EndpointIT.java
```


> Then, to open the unknown file in your IDE, select
> **File** > **Open** > guide-gradle-intro/start/unknown, or click the following button

::openFile{path="/home/project/guide-gradle-intro/start/unknown"}


The test class name ends in ***IT*** to indicate that it contains an integration test.
The integration tests are put in the ***it*** folder by convention.

The ***test*** section in your build file is added by the Java plug-in, and the 
***useJUnitPlatform()*** line configures Gradle to add JUnit 5 support.

The ***systemProperty*** configuration  defines some variables needed by 
the test class. While the port number and context-root information can be 
hardcoded in the test class, it is better to specify it in a single place like the Gradle 
***build.gradle*** file, in case they need to change.
The ***systemProperty*** lines passes these details to the test JVMs 
as a series of system properties, resolving the ***http.port*** 
and ***context.root*** variables.

The ***init()*** method in the ***EndpointIT.java*** test class uses these 
system variables to build the URL of the application.

In the test class, after defining how to build the application URL, the ***@Test*** 
annotation indicates the start of the test method.

In the ***try*** block of the test method, an HTTP ***GET*** request to the 
URL of the application returns a status code. If the response to the request includes the 
string ***Hello! Is Gradle working for you?***, the test passes. If that string is not in the 
response, the test fails. The HTTP client then disconnects from the application.

In the ***import*** statements of this test class, you'll notice that the 
test has some new dependencies. Earlier you added some ***testImplementation*** 
dependencies. The Apache ***httpclient*** and ***org.junit.jupiter*** 
dependencies are needed to compile and run the integration test ***EndpointIT*** 
class.

The scope for each of the dependencies is set to ***testImplementation*** 
because the libraries are needed only during the Gradle test phase and do not need to be 
packaged with the application.

Now, the created WAR file contains the web application, and development mode can run any integration 
test classes that it finds. Integration test classes are classes with names that end in ***IT***.

The directory structure of the project in the ***start*** folder should now look like this 
example:

```
└── build.gradle
├── settings.gradle
└── src
    ├── main
    │    ├── java
    │    ├── liberty
    │    │    └── config
    │    └── webapp
    │         └── WEB_INF
    └── test
         └── java

```

### A few more pieces

We show a few more Gradle tricks in this example with the ***openBrowser*** task. 
This task displays your application and the test report in the default browser.

The final Gradle magic to add is the task dependency directives.
The ***dependency directives*** organizes task execution. 
In this case, the test task is set to run after the server is started, and the
***openBrowser*** task is executed after the test task is finalized.

### Running the tests

Because you started Open Liberty in development mode at the start of the guide, press the ***enter/return*** 
key from the command-line session where you started dev mode to run the tests.
You will see that the browser opened up the test summary page, which ran one successful test.

To see whether the test detects a failure, change the ***response string*** in the 
***src/main/java/io/openliberty/guides/hello/HelloServlet.java*** file 
so that it doesn't match the string that the test is looking for. Then rerun the Gradle 
test to automatically restart and retest your application to check to see if the test fails.

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran the server, or by typing ***q*** and then pressing the ***enter/return*** key.

::page{title="Summary"}

### Nice Work!

You built and tested a web application project running on an Open Liberty server using Gradle.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-gradle-intro*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-gradle-intro
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Building%20a%20web%20application%20with%20Gradle&guide-id=cloud-hosted-guide-gradle-intro)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-gradle-intro/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-gradle-intro/pulls)



### Where to next?

* [Building a web application with Maven](https://openliberty.io/guides/maven-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
