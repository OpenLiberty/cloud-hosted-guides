---
markdown-version: v1
title: instructions
branch: lab-478-instruction
version-history-start-date: 2020-04-22 13:18:21 UTC
tool-type: theia
---
::page{title="Welcome to the Testing a MicroProfile or Jakarta EE application guide!"}

Learn how to use MicroShed Testing to test a MicroProfile or Jakarta EE application.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You'll start with an existing REST application that runs on Open Liberty and use [MicroShed Testing](https://microshed.org/microshed-testing/) to write tests for the application that exercise the application in a Docker container.

Sometimes tests might pass in development and testing (dev/test) environments, but fail in production because the application runs differently in production than in dev/test. Fortunately, you can minimize these differences between dev/test and production by testing your application in the same Docker container that you'll use in production.

### What is Docker?

Docker is a tool that you can use to deploy and run applications with containers. You can think of Docker as a virtual machine that runs various applications. However, unlike with a typical virtual machine, you can run these applications simultaneously on a single system and independent of one another.

Learn more about Docker on the [official Docker website](https://www.docker.com/what-docker).


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microshed-testing.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microshed-testing.git
cd guide-microshed-testing
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

First, review the ***PersonServiceIT*** class to see what the tests look like:
From the menu of the IDE, select ***File*** > ***Open*** > guide-microshed-testing/finish/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java


To try out the application, go to the ***finish*** directory and run the following Maven goal to build the application and run the integration tests on an Open Liberty server in a container:


```bash
cd /home/project/guide-microshed-testing/finish
mvn verify
```

This command might take some time to run initially because the dependencies and the Docker image for Open Liberty must download. If you run the same command again, it will be faster.

The previous example shows how you can run integration tests from a cold start. With Open Liberty development mode, you can use MicroShed Testing to run tests on an active Open Liberty server. Run the following Maven goal to start Open Liberty in development mode:

```bash
mvn liberty:dev
```

After you see the following message, your application server in dev mode is ready:

```
**************************************************************
*    Liberty is running in dev mode.
```

After the Open Liberty server starts and you see the ***To run tests on demand, press Enter.*** message, you can press the ***enter/return*** key to run the integration tests. After the tests finish, you can press the ***enter/return*** key to run the tests again, or you can make code changes to the application or tests. Development mode automatically recompiles and updates any application or test code changes that you make.

After you're finished running tests, exit development mode by pressing `Ctrl+C` in the command-line session where you ran the server, or by typing ***q*** and then pressing the ***enter/return*** key.


::page{title="Bootstrapping your application for testing"}


To begin, run the following command to navigate to the ***start*** directory:
```bash
cd /home/project/guide-microshed-testing/start
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

Wait for the ***To run tests on demand, press Enter.*** message, and then press the ***enter/return*** key to run the tests. You see that one test runs:

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

To begin bootstrapping, import the ***MicroShedTest*** annotation and annotate the ***PersonServiceIT*** class with ***@MicroShedTest***. This annotation indicates that the test class uses MicroShed Testing. 

The ***PersonServiceIT*** class outlines some basic information that informs how MicroShed Testing starts the application runtime and at which URL path the application is available:

Replace the ***PersonServiceIT*** class.

> To open the PersonServiceIT.java file in your IDE, select
> **File** > **Open** > guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java, or click the following button

::openFile{path="/home/project/guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java"}



```java
package io.openliberty.guides.testing;


import org.junit.jupiter.api.Test;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.junit.jupiter.Container;

@MicroShedTest
public class PersonServiceIT {

    @RESTClient
    public static PersonService personSvc;

    @Container
    public static ApplicationContainer app = new ApplicationContainer()
                    .withAppContextRoot("/guide-microshed-testing")
                    .withReadinessPath("/health/ready");

    @Test
    public void testCreatePerson() {
    }

}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


Import the ***ApplicationContainer*** class and the ***Container*** annotation, create the ***ApplicationContainer*** application, and annotate the application with ***@Container*** annotation.

The ***withAppContextRoot(String)*** method indicates the base path of the application. The app context root is the portion of the URL after the hostname and port. In this case, the application is deployed at the ***http://localhost:9080/guide-microshed-testing*** URL, so the app context root is ***/guide-microshed-testing***.




Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.


The ***withReadinessPath(String)*** method indicates what path is polled by HTTP to determine application readiness. MicroShed Testing automatically starts the ApplicationContainer application and waits for it to be ready before the tests start running. In this case, you're using the default application readiness check at the ***http\://localhost:9080/health/ready*** URL, which is enabled by the ***MicroProfile Health*** feature in the server.xml configuration file. When the readiness URL returns the ***HTTP 200*** message, the application is considered ready and the tests begin running.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9080/health/ready | jq
```



Save your changes to the ***PersonServiceIT*** class and press the ***enter/return*** key in your console window to rerun the tests. You still see only one test running, but the output is different. Notice that MicroShed Testing is using a ***hollow*** configuration mode. This configuration mode means that MicroShed Testing is reusing an existing application runtime for the test, not starting up a new application instance each time you initiate a test run.


::page{title="Talking to your application with a REST client"}

With MicroShed Testing, applications are exercised in a black-box fashion. Black-box means the tests can't access the application internals. Instead, the application is exercised from the outside, usually with HTTP requests. To simplify the HTTP interactions, a REST client is injected into the tests. To do this, you imported the ***org.microshed.testing.jaxrs.RESTClient*** annotation, created a ***PersonService*** REST client, and annotated the REST client with ***@RESTClient***.

In this example, the ***PersonService*** injected type is the same ***io.openliberty.guides.testing.PersonService*** class that is used in your application. However, the _instance_ that gets injected is a REST client proxy. So, if you call ***personSvc.createPerson("Bob", 42)***, the REST client makes an HTTP POST request to the application that is running at ***http://localhost:9080/guide-microshed-testing/people*** URL, which triggers the corresponding Java method in the application.




::page{title="Writing your first test"}

Now that the setup is complete, you can write your first test case. Start by testing the basic "create person" use case for your REST-based application. To test this use case, use the REST client that's injected by MicroShed Testing to make the HTTP POST request to the application and read the response.

Replace the ***PersonServiceIT*** class.

> To open the PersonServiceIT.java file in your IDE, select
> **File** > **Open** > guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java, or click the following button

::openFile{path="/home/project/guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java"}



```java
package io.openliberty.guides.testing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.junit.jupiter.Container;

@MicroShedTest
public class PersonServiceIT {

    @RESTClient
    public static PersonService personSvc;

    @Container
    public static ApplicationContainer app = new ApplicationContainer()
                    .withAppContextRoot("/guide-microshed-testing")
                    .withReadinessPath("/health/ready");

    @Test
    public void testCreatePerson() {
        Long createId = personSvc.createPerson("Hank", 42);
        assertNotNull(createId);
    }

}
```



Replace the ***PersonServiceIT*** class to include the ***assertNotNull*** static method and write the test logic in the ***testCreatePerson()*** method.

Save the changes. Then, press the ***enter/return*** key in your console window to run the test. You see that the test ran again and exercised the REST endpoint of your application, including the response of your application's endpoint:

```
[INFO] Building rest client for class io.openliberty.guides.testing.PersonService with base path: http://localhost:9080/guide-microshed-testing/ and providers: [class org.microshed.testing.jaxrs.JsonBProvider]
[INFO] Response from server: 1809686877352335426
```

Next, add more tests.

Replace the ***PersonServiceIT*** class.

> To open the PersonServiceIT.java file in your IDE, select
> **File** > **Open** > guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java, or click the following button

::openFile{path="/home/project/guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java"}



```java
package io.openliberty.guides.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.junit.jupiter.Container;

@MicroShedTest
public class PersonServiceIT {

    @RESTClient
    public static PersonService personSvc;

    @Container
    public static ApplicationContainer app = new ApplicationContainer()
                    .withAppContextRoot("/guide-microshed-testing")
                    .withReadinessPath("/health/ready");

    @Test
    public void testCreatePerson() {
        Long createId = personSvc.createPerson("Hank", 42);
        assertNotNull(createId);
    }

    @Test
    public void testMinSizeName() {
        Long minSizeNameId = personSvc.createPerson("Ha", 42);
        assertEquals(new Person("Ha", 42, minSizeNameId),
                     personSvc.getPerson(minSizeNameId));
    }

    @Test
    public void testMinAge() {
        Long minAgeId = personSvc.createPerson("Newborn", 0);
        assertEquals(new Person("Newborn", 0, minAgeId),
                     personSvc.getPerson(minAgeId));
    }

    @Test
    public void testGetPerson() {
        Long bobId = personSvc.createPerson("Bob", 24);
        Person bob = personSvc.getPerson(bobId);
        assertEquals("Bob", bob.name);
        assertEquals(24, bob.age);
        assertNotNull(bob.id);
    }

    @Test
    public void testGetAllPeople() {
        Long person1Id = personSvc.createPerson("Person1", 1);
        Long person2Id = personSvc.createPerson("Person2", 2);

        Person expected1 = new Person("Person1", 1, person1Id);
        Person expected2 = new Person("Person2", 2, person2Id);

        Collection<Person> allPeople = personSvc.getAllPeople();
        assertTrue(allPeople.size() >= 2,
            "Expected at least 2 people to be registered, but there were only: "
            + allPeople);
        assertTrue(allPeople.contains(expected1),
            "Did not find person " + expected1 + " in all people: " + allPeople);
        assertTrue(allPeople.contains(expected2),
            "Did not find person " + expected2 + " in all people: " + allPeople);
    }

    @Test
    public void testUpdateAge() {
        Long personId = personSvc.createPerson("newAgePerson", 1);

        Person originalPerson = personSvc.getPerson(personId);
        assertEquals("newAgePerson", originalPerson.name);
        assertEquals(1, originalPerson.age);
        assertEquals(personId, Long.valueOf(originalPerson.id));

        personSvc.updatePerson(personId,
            new Person(originalPerson.name, 2, originalPerson.id));
        Person updatedPerson = personSvc.getPerson(personId);
        assertEquals("newAgePerson", updatedPerson.name);
        assertEquals(2, updatedPerson.age);
        assertEquals(personId, Long.valueOf(updatedPerson.id));
    }
}
```



The following tests are added: ***testMinSizeName()***, ***testMinAge()***, ***testGetPerson()***, ***testGetAllPeople()***, and ***testUpdateAge()***.


Save the changes, and  press the ***enter/return*** key in your console window to run the tests.


::page{title="Testing outside of development mode"}

Running tests in development mode is convenient for local development, but it can be tedious to test against a running Open Liberty server in non-development scenarios such as CI/CD pipelines. For this reason, MicroShed Testing can start and stop the application runtime before and after the tests are run. This process is primarily accomplished by using Docker and Testcontainers.

To test outside of development mode, exit development mode by pressing `Ctrl+C` in the command-line session where you ran the server, or by typing ***q*** and then pressing the ***enter/return*** key.

Next, use the following Maven goal to run the tests from a cold start:
```bash
mvn verify
```

Running tests from a cold start takes a little longer than running tests from development mode because the application runtime needs to start each time. However, tests that are run from a cold start use a clean instance on each run to ensure consistent results. These tests also automatically hook into existing build pipelines that are set up to run the ***integration-test*** phase.


::page{title="Sharing configuration across multiple classes"}

Typically, projects have multiple test classes that all use the same type of application deployment. For these cases, it's useful to reuse an existing configuration and application lifecycle across multiple test classes.

First, create another test class.

Create the ***ErrorPathIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/ErrorPathIT.java
```


> Then, to open the ErrorPathIT.java file in your IDE, select
> **File** > **Open** > guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/ErrorPathIT.java, or click the following button

::openFile{path="/home/project/guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/ErrorPathIT.java"}



```java
package io.openliberty.guides.testing;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import org.junit.jupiter.api.Test;
import org.microshed.testing.jupiter.MicroShedTest;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.junit.jupiter.Container;
import org.microshed.testing.jaxrs.RESTClient;

@MicroShedTest
public class ErrorPathIT {

    @Container
    public static ApplicationContainer app = new ApplicationContainer()
                    .withAppContextRoot("/guide-microshed-testing")
                    .withReadinessPath("/health/ready");

    @RESTClient
    public static PersonService personSvc;

    @Test
    public void testGetUnknownPerson() {
        assertThrows(NotFoundException.class, () -> personSvc.getPerson(-1L));
    }

    @Test
    public void testCreateBadPersonNullName() {
        assertThrows(BadRequestException.class, () -> personSvc.createPerson(null, 5));
    }

    @Test
    public void testCreateBadPersonNegativeAge() {
        assertThrows(BadRequestException.class, () ->
          personSvc.createPerson("NegativeAgePersoN", -1));
    }

    @Test
    public void testCreateBadPersonNameTooLong() {
        assertThrows(BadRequestException.class, () ->
          personSvc.createPerson("NameTooLongPersonNameTooLongPersonNameTooLongPerson",
          5));
    }
}
```



The ***ErrorPathIT*** test class has the same ***@Container*** configuration and ***PersonService*** REST client as the ***PersonServiceIT*** class.

Now, run the tests again outside of development mode:
```bash
mvn verify
```

Notice that tests for both the ***PersonServiceIT*** and ***ErrorPathIT*** classes run, but a new server starts for each test class, resulting in a longer test runtime.

### Creating a common configuration

To solve this issue, common configuration can be placed in a class that implements ***SharedContainerConfiguration***.

Create the ***AppDeploymentConfig*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/AppDeploymentConfig.java
```


> Then, to open the AppDeploymentConfig.java file in your IDE, select
> **File** > **Open** > guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/AppDeploymentConfig.java, or click the following button

::openFile{path="/home/project/guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/AppDeploymentConfig.java"}



```java
package io.openliberty.guides.testing;

import org.microshed.testing.SharedContainerConfiguration;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.junit.jupiter.Container;

public class AppDeploymentConfig implements SharedContainerConfiguration {

    @Container
    public static ApplicationContainer app = new ApplicationContainer()
                    .withAppContextRoot("/guide-microshed-testing")
                    .withReadinessPath("/health/ready");

}
```



After the common configuration is created, the test classes can be updated to reference this shared configuration.

### Updating the PersonServiceIT class


Remove the container code from the ***PersonServiceIT*** class. Remove ***import*** statements for ***ApplicationContainer*** and ***Container*** and the ***ApplicationContainer app*** field.

Next, annotate the ***PersonServiceIT*** class with the ***@SharedContainerConfig*** annotation that references the ***AppDeploymentConfig*** shared configuration class.

Replace the ***PersonServiceIT*** class.

> To open the PersonServiceIT.java file in your IDE, select
> **File** > **Open** > guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java, or click the following button

::openFile{path="/home/project/guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java"}



```java
package io.openliberty.guides.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;

@MicroShedTest
@SharedContainerConfig(AppDeploymentConfig.class)
public class PersonServiceIT {

    @RESTClient
    public static PersonService personSvc;

    @Test
    public void testCreatePerson() {
        Long createId = personSvc.createPerson("Hank", 42);
        assertNotNull(createId);
    }

    @Test
    public void testMinSizeName() {
        Long minSizeNameId = personSvc.createPerson("Ha", 42);
        assertEquals(new Person("Ha", 42, minSizeNameId),
                     personSvc.getPerson(minSizeNameId));
    }

    @Test
    public void testMinAge() {
        Long minAgeId = personSvc.createPerson("Newborn", 0);
        assertEquals(new Person("Newborn", 0, minAgeId),
                     personSvc.getPerson(minAgeId));
    }

    @Test
    public void testGetPerson() {
        Long bobId = personSvc.createPerson("Bob", 24);
        Person bob = personSvc.getPerson(bobId);
        assertEquals("Bob", bob.name);
        assertEquals(24, bob.age);
        assertNotNull(bob.id);
    }

    @Test
    public void testGetAllPeople() {
        Long person1Id = personSvc.createPerson("Person1", 1);
        Long person2Id = personSvc.createPerson("Person2", 2);

        Person expected1 = new Person("Person1", 1, person1Id);
        Person expected2 = new Person("Person2", 2, person2Id);

        Collection<Person> allPeople = personSvc.getAllPeople();
        assertTrue(allPeople.size() >= 2,
            "Expected at least 2 people to be registered, but there were only: "
            + allPeople);
        assertTrue(allPeople.contains(expected1),
            "Did not find person " + expected1 + " in all people: " + allPeople);
        assertTrue(allPeople.contains(expected2),
            "Did not find person " + expected2 + " in all people: " + allPeople);
    }

    @Test
    public void testUpdateAge() {
        Long personId = personSvc.createPerson("newAgePerson", 1);

        Person originalPerson = personSvc.getPerson(personId);
        assertEquals("newAgePerson", originalPerson.name);
        assertEquals(1, originalPerson.age);
        assertEquals(personId, Long.valueOf(originalPerson.id));

        personSvc.updatePerson(personId,
            new Person(originalPerson.name, 2, originalPerson.id));
        Person updatedPerson = personSvc.getPerson(personId);
        assertEquals("newAgePerson", updatedPerson.name);
        assertEquals(2, updatedPerson.age);
        assertEquals(personId, Long.valueOf(updatedPerson.id));
    }
}
```



Import the ***SharedContainerConfig*** annotation and annotate the ***PersonServiceIT*** class with ***@SharedContainerConfig***. 

### Updating the ErrorPathIT class


Similarly, replace the ***ErrorPathIT*** class to remove the container code. Remove ***import*** statements for ***ApplicationContainer*** and ***Container*** and the ***ApplicationContainer app*** field.

Next, annotate the ***ErrorPathIT*** class with the ***@SharedContainerConfig*** annotation.

Replace the ***ErrorPathIT*** class.

> To open the ErrorPathIT.java file in your IDE, select
> **File** > **Open** > guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/ErrorPathIT.java, or click the following button

::openFile{path="/home/project/guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/ErrorPathIT.java"}



```java
package io.openliberty.guides.testing;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;

@MicroShedTest
@SharedContainerConfig(AppDeploymentConfig.class)
public class ErrorPathIT {

    @RESTClient
    public static PersonService personSvc;

    @Test
    public void testGetUnknownPerson() {
        assertThrows(NotFoundException.class, () -> personSvc.getPerson(-1L));
    }

    @Test
    public void testCreateBadPersonNullName() {
        assertThrows(BadRequestException.class, () -> personSvc.createPerson(null, 5));
    }

    @Test
    public void testCreateBadPersonNegativeAge() {
        assertThrows(BadRequestException.class, () ->
          personSvc.createPerson("NegativeAgePersoN", -1));
    }

    @Test
    public void testCreateBadPersonNameTooLong() {
        assertThrows(BadRequestException.class, () ->
           personSvc.createPerson("NameTooLongPersonNameTooLongPersonNameTooLongPerson",
           5));
    }
}
```



Import the ***SharedContainerConfig*** annotation and annotate the ***ErrorPathIT*** class with ***@SharedContainerConfig***. 

If you rerun the tests now, they run in about half the time because the same server instance is being used for both test classes:
```bash
mvn verify
```


::page{title="Summary"}

### Nice Work!

You developed automated tests for a REST service in Open Liberty by using MicroShed Testing and Open Liberty development mode.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microshed-testing*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microshed-testing
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Testing%20a%20MicroProfile%20or%20Jakarta%20EE%20application&guide-id=cloud-hosted-guide-microshed-testing)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microshed-testing/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microshed-testing/pulls)



### Where to next?

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Using Docker containers to develop microservices](https://openliberty.io/guides/docker.html)
* [Consuming a RESTful web service](https://openliberty.io/guides/rest-client-java.html)

**Learn more about MicroShed Testing**
* [View the MicroShed Testing website](https://microshed.org/microshed-testing/)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
