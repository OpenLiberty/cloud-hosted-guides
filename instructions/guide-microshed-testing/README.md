# Learn how to use MicroShed Testing to test a MicroProfile or Jakarta EE application

### What you will learn

You'll start with an existing REST application that runs on Open Liberty and use [MicroShed Testing](https://microshed.org/microshed-testing/) to write tests for the application that exercise the application inside of a Docker container.

Sometimes tests might pass in development and testing (dev/test) environments, but fail in production because the application is running differently in production than it is in dev/test. Fortunately, you can minimize these parity issues between development and production by testing your application in the same Docker container that you'll use in production.

### Introduction

Docker is a tool that you can use to deploy and run applications with containers. You
can think of Docker as a virtual machine that runs various applications. However, unlike with a typical virtual
machine, you can run these applications simultaneously on a single system and independent of
one another.

Learn more about Docker on the https://www.docker.com/what-docker[official Docker website^].

## Getting Started

As an example:
If a terminal window does not open navigate:

`Terminal -> New Terminal`

Check you are in the **home/project** folder:

`pwd`

The fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:

`git clone https://github.com/openliberty/guide-microshed-testing.git`

`cd guide-microshed-testing`

# Try what you'll build

The `finish` directory in the root of this guide contains the finished application. Give it a try before you proceed.

First, review the **PersonServiceIT** class to see what the tests look like:

> [File -> Open] finish/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java

To try out the application, go to the **finish** directory and run the following Maven 
goal to build the application and run the integration tests on an Open Liberty server in a container:

`cd finish`

`mvn verify`

This command might take some time to run the first time because the dependencies and the Docker image for Open Liberty must download. If you run the same command again, it will be faster.

The previous example shows how you can run integration tests from a cold start. With Open Liberty development mode, you can use MicroShed Testing to run tests on an already running Open Liberty server. Run the following Maven goal to start Open Liberty in development mode:


`mvn liberty:dev`

After the Open Liberty server starts and you see the `Press the Enter key to run tests on demand.` message, you can press the 
`enter/return` key to run the integration tests. After the tests finish, you can press the `enter/return` key to run the tests again, or you can make code changes to the application or tests. Development mode automatically recompiles and updates any application or test code changes that you make.

After you are finished running tests, stop the Open Liberty server by typing `q` in the shell session where you ran the server, and then press the `enter/return` key. 

# Bootstrapping your application for testing

Navigate to the `start` directory to begin.

`cd ../start`

Start Open Liberty in development mode, which starts the Open Liberty server and listens for file changes:

`mvn liberty:dev`

Wait for the `Press the Enter key to run tests on demand.` message, and then press the `enter/return` key to run the tests. You see that one test runs:

```
 Running integration tests...

 -------------------------------------------------------
  T E S T S
 -------------------------------------------------------
 Running io.openliberty.guides.testing.PersonServiceIT
 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.024 s - in io.openliberty.guides.testing.PersonServiceIT

 Results:

 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

 Integration tests finished.
```

To begin bootstrapping, annotate the **src/test/java/io/openliberty/guides/testing/PersonServiceIT.java** class with the **@MicroShedTest** annotation. This annotation indicates that the test class uses MicroShed Testing.

> [File -> Open] src/test/java/io/openliberty/guides/testing/PersonServiceIT.java

Insert the following in line 22 of PersonServiceIT.java: 


`import org.microshed.testing.jupiter.MicroShedTest`;

`@MicroShedTest`

Next, the **PersonServiceIT* class outlines some basic information that informs how MicroShed Testing starts the application runtime and at which URL path the application will be available. Import the `ApplicationContainer` class and the `Container` annotation, create the `ApplicationContainer` application, and annotate the application with @Container by adding the following after line 24 of `PersonServiceIT.java` file:

`import org.microshed.testing.testcontainers.ApplicationContainer`;

```
 @Container
    public static ApplicationContainer app = new ApplicationContainer()
                    .withAppContextRoot("/guide-microshed-testing")
                    .withReadinessPath("/health/ready");
```

The **withAppContextRoot(String)** method indicates the base path of the application. The app context root is the portion of the URL after the hostname and port. In this case, the application is deployed at the **http://localhost:9080/guide-microshed-testing** URL, so the app context root is /guide-microshed-testing.

The **withReadinessPath(String)** method indicates what path is polled by HTTP to determine application readiness. MicroShed Testing automatically starts the ApplicationContainer application and waits for it to be ready before the tests start running. In this case, you are using the default application readiness check at the http://localhost:9080/health/ready URL, which is enabled by the MicroProfile Health feature in our **server.xml** configuration file. When the readiness URL returns **HTTP 200**, the application is considered ready and the tests begin running.

In another terminal, run the following command to call the microservice URL:

`curl http://localhost:9080/health/ready`

Save your changes to the **PersonServiceIT** class and press the `enter/return key` in your console window to rerun the tests. You still see only one test running, but the output is different. Notice that MicroShed Testing is using a **hollow** configuration mode. This configuration mode means that MicroShed Testing is reusing an existing application runtime for the test, not starting up a new application instance each time you initiate a test run.

## Talking to your application with a REST client

With MicroShed Testing, applications are exercised in a black box fashion. Black box means the tests cannot access the application internals. Instead, the application is exercised from the outside, usually with HTTP requests. To simplify the HTTP interactions, inject a REST client into the tests.

Import the **org.microshed.testing.jaxrs.RESTClient** annotation, create a PersonService REST client, and annotate the REST client with **@RESTClient**.

`import org.microshed.testing.testcontainers.ApplicationContainer;`

# Writing your first test

Now that the setup is complete, you can write your first test case. Start by testing the basic "create person" use case for your REST-based application. To test this use case, use the REST client that’s injected by MicroShed Testing to make the HTTP POST request to the application and read the response.

> [File -> Open] src/test/java/io/openliberty/guides/testing/PersonServiceIT.java

Import the `assertNotNull` static method and write the test logic in the `testCreatePerson()` method.

import static org.junit.jupiter.api.Assertions.assertNotNull;

Long createId = personSvc.createPerson("Hank", 42);
        assertNotNull(createId);
        
Save the changes. Then, press the enter/return key in your console window to run the test. You see that the test ran again and exercised the REST endpoint of your application, including the response of your application’s endpoint:

# Testing outside of development mode

Running tests in development mode is convenient for local development, but it can be tedious to test against a running Open Liberty server in non-development scenarios such as CI/CD pipelines. For this reason, MicroShed Testing can start and stop the application runtime before and after the tests are run. This process is primarily accomplished by using Docker and Testcontainers.

To test outside of development mode, exit development mode by typing **q** in the shell session where you ran the server, and then press the **enter/return** key.

Next, use the following Maven goal to run the tests from a cold start:

`mvn verify`

Running tests from a cold start takes a little longer than running tests from development mode because the application runtime needs to start each time. However, tests that are run from a cold start use a clean instance on each run to ensure consistent results. These tests also automatically hook into existing build pipelines that are set up to run the **integration-test** phase.

# Summary

## Well Done

Example:
Congratulations, you have built, a cloud-native application, seen how you can monitor it for health and metrics, change its configuration, and package and run it in Docker, ready for deployment to your cloud of choice.  I recommend IBM Cloud or IBM Cloud Private, of course ;)
