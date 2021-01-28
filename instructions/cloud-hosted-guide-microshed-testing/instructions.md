
# Testing a MicroProfile or Jakarta EE application


Learn how to use MicroShed Testing to test a MicroProfile or Jakarta EE application.

## What you'll learn

You'll start with an existing REST application that runs on Open Liberty and use [MicroShed Testing](https://microshed.org/microshed-testing/) 
to write tests for the application that exercise the application inside of a Docker container.

Sometimes tests might pass in development and testing (dev/test) environments, but fail in production because the application is
running differently in production than it is in dev/test. Fortunately, you can minimize these parity issues between development and production
by testing your application in the same Docker container that you'll use in production.

### What is Docker?

Docker is a tool that you can use to deploy and run applications with containers. You
can think of Docker as a virtual machine that runs various applications. However, unlike with a typical virtual
machine, you can run these applications simultaneously on a single system and independent of
one another.

Learn more about Docker on the [official Docker website](https://www.docker.com/what-docker).

```
mvn verify
```
{: codeblock}



This command might take some time to run the first time because the dependencies and the Docker image for Open Liberty must download. If you 
run the same command again, it will be faster.

The previous example shows how you can run integration tests from a cold start. With Open Liberty development mode, you can use MicroShed Testing to run tests on
an already running Open Liberty server. Run the following Maven goal to start Open Liberty in development mode:

```
mvn liberty:dev
```
{: codeblock}


After the Open Liberty server starts and you see the **Press the Enter key to run tests on demand.** message, you can press the 
**enter/return** key to run the integration tests. After the tests finish, you can press the **enter/return** key to run the tests again, or you 
can make code changes to the application or tests. Development mode automatically
recompiles and updates any application or test code changes that you make.

After you are finished running tests, exit development mode by pressing **CTRL+C** in the command-line session
where you ran the server, or by typing **q** and then pressing the **enter/return** key.

# Bootstrapping your application for testing

Navigate to the **start** directory to begin.

When you run Open Liberty in development mode, known as dev mode, the server listens for file changes and automatically recompiles and 
deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```
mvn liberty:dev
```
{: codeblock}


After you see the following message, your application server in dev mode is ready:

```
Press the Enter key to run tests on demand.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, 
or open the project in your editor.

Wait for the **Press the Enter key to run tests on demand.** message, and then press the **enter/return** key to run the tests. You see that one test runs:

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

Update the **PersonServiceIT** class.

> [File -> Open...]  
> guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java




```
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
            "Expected at least 2 people to be registered, but there were only: " + 
            allPeople);
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
{: codeblock}


Import the **MicroShedTest** annotation and annotate the **PersonServiceIT** class with **@MicroShedTest**.


Next, the **PersonServiceIT** class outlines some basic information that informs how MicroShed Testing starts the application runtime and at which URL path the application will be available:

Update the **PersonServiceIT** class.

> [File -> Open...]  
> guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java




```
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
            "Expected at least 2 people to be registered, but there were only: " + 
            allPeople);
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
{: codeblock}


Import the **ApplicationContainer** class and the **Container** annotation, create the **ApplicationContainer** application, and annotate the application with **@Container**.


The **withAppContextRoot(String)** method indicates the base path of the application. The app context root is the portion of the URL after the hostname and port. In this case, the application is deployed at the **http://localhost:9080/guide-microshed-testing** URL, so the app context root is **/guide-microshed-testing**.



Open a command-line session:

> [Terminal -> New Terminal]

Navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}


The **withReadinessPath(String)** method indicates what path is polled by HTTP to determine application readiness

_(or run the following curl command)_

```
curl http://localhost:9080/health/ready
```
{: codeblock}




Save your changes to the **PersonServiceIT** class and press the **enter/return** key in your console window to rerun the tests. You still see only one test running, but the output is different. Notice that MicroShed Testing is using a **hollow** configuration mode. This configuration mode means that MicroShed Testing is reusing an existing application runtime for the test, not starting up a new application instance each time you initiate a test run.

# Talking to your application with a REST client

With MicroShed Testing, applications are exercised in a black box fashion. Black box means the tests cannot access the application internals. Instead, the application is exercised from the outside, usually with HTTP requests. To simplify the HTTP interactions, inject a REST client into the tests.

Update the **PersonServiceIT** class.

> [File -> Open...]  
> guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java




```
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
            "Expected at least 2 people to be registered, but there were only: " + 
            allPeople);
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
{: codeblock}


Import the **org.microshed.testing.jaxrs.RESTClient** annotation, create a **PersonService** REST client, and annotate the REST client with **@RESTClient**.



In this example, the **PersonService** injected type is the same **io

_(or run the following curl command)_

```
curl http://localhost:9080/guide-microshed-testing/people
```
{: codeblock}





# Writing your first test

Now that the setup is complete, you can write your first test case. Start by testing the basic "create person" use case for your REST-based application. To test this use case, use the REST client that's injected by MicroShed Testing to make the HTTP POST request to the application and read the response.

Update the **PersonServiceIT** class.

> [File -> Open...]  
> guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java




```
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
            "Expected at least 2 people to be registered, but there were only: " + 
            allPeople);
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
{: codeblock}


Import the **assertNotNull** static method and write the test logic in the **testCreatePerson()** method.


Save the changes. Then, press the **enter/return** key in your console window to run the test. You see that the test ran again and exercised the REST endpoint of your application, including the response of your application's endpoint:

```
INFO org.microshed.testing.jaxrs.RestClientBuilder  - Building rest client for class io.openliberty.guides.testing.PersonService with base path: http://localhost:9080/guide-microshed-testing/ and providers: [class org.microshed.testing.jaxrs.JsonBProvider]
INFO org.microshed.testing.jaxrs.JsonBProvider  - Response from server: 1809686877352335426
```

Next, add more tests.

Replace the **PersonServiceIT** class.

> [File -> Open...]  
> guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java




```
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
            "Expected at least 2 people to be registered, but there were only: " + 
            allPeople);
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
{: codeblock}



The following tests are added: **testMinSizeName()**, **testMinAge()**, **testGetPerson()**, **testGetAllPeople()**, and **testUpdateAge()**.


Save the changes, and  press the **enter/return** key in your console window to run the tests.

# Testing outside of development mode

Running tests in development mode is convenient for local development, but it can be tedious to test against a running Open Liberty server in non-development scenarios such as CI/CD pipelines. For this reason, MicroShed Testing can start and stop the application runtime before and after the tests are run. This process is primarily accomplished by using Docker and Testcontainers.

To test outside of development mode, exit development mode by pressing **CTRL+C** in the command-line session
where you ran the server, or by typing **q** and then pressing the **enter/return** key.

Next, use the following Maven goal to run the tests from a cold start:
```
mvn verify
```
{: codeblock}


Running tests from a cold start takes a little longer than running tests from development mode because the application runtime needs to start each time. However, tests that are run from a cold start use a clean instance on each run to ensure consistent results. These tests also automatically hook into existing build pipelines that are set up to run the **integration-test** phase.

# Sharing configuration across multiple classes

Typically, projects have multiple test classes that all use the same type of application deployment. For these cases, it is useful to reuse an existing configuration and application lifecycle across multiple test classes.

First, create another test class.

Create the **ErrorPathIT** class.

> [File -> New File]  
> guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/ErrorPathIT.java




```
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
{: codeblock}



The **ErrorPathIT** test class has the same **@Container** configuration and **PersonService** REST client as the **PersonServiceIT** class.

Now, run the tests again outside of development mode:
```
mvn verify
```
{: codeblock}


Notice that tests for both the **PersonServiceIT** and **ErrorPathIT** classes run, but a new server starts for each test class, resulting in a longer test runtime.

To solve this issue, common configuration can be placed in a class that implements **SharedContainerConfiguration**.

Create the **AppDeploymentConfig** class.

> [File -> New File]  
> guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/AppDeploymentConfig.java




```
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
{: codeblock}



After the common configuration is created, the test classes can be updated to reference this shared configuration.

Remove the container code from the **PersonServiceIT** class.
Update the **PersonServiceIT** class.

> [File -> Open...]  
> guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java




```
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
            "Expected at least 2 people to be registered, but there were only: " + 
            allPeople);
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
{: codeblock}


Remove **import** statements and the **ApplicationContainer app** field.


Annotate the **PersonServiceIT** class with the **@SharedContainerConfig** annotation that references the **AppDeploymentConfig** shared configuration class.
Update the **PersonServiceIT** class.

> [File -> Open...]  
> guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/PersonServiceIT.java




```
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
            "Expected at least 2 people to be registered, but there were only: " + 
            allPeople);
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
{: codeblock}


Import the **SharedContainerConfig** annotation and annotate the **PersonServiceIT** class with **@SharedContainerConfig**. 


Similarly, update the **ErrorPathIT** class to remove the container code.
Update the **ErrorPathIT** class.

> [File -> Open...]  
> guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/ErrorPathIT.java




```
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
{: codeblock}


Remove **import** statements and the **ApplicationContainer app** field


Annotate the **ErrorPathIT** class with the **@SharedContainerConfig** annotation.
Update the **ErrorPathIT** class.

> [File -> Open...]  
> guide-microshed-testing/start/src/test/java/io/openliberty/guides/testing/ErrorPathIT.java




```
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
{: codeblock}


Import the **SharedContainerConfig** annotation and annotate the **ErrorPathIT** class with **@SharedContainerConfig**. 


If you rerun the tests now, they run in about half the time because the same server instance is being used for both test classes:
```
mvn verify
```
{: codeblock}


# Summary

## Nice Work!

You developed automated tests for a REST service in Open Liberty by using MicroShed Testing and Open Liberty development mode.


# Related Links

Learn more about MicroShed Testing.

[View the MicroShed Testing website](https://microshed.org/microshed-testing/)



## Clean up your environment

Clean up your online environment so that it is ready to be used with the next guide!

You can clean up the environment by doing the following:

Delete the **guide-microshed-testing** project by navigating to the **/home/project/** directory

```
cd /home/project
rm -fr guide-microshed-testing
```
{: codeblock}

Now Log out by navigating to: 

> [Account -> Logout]

