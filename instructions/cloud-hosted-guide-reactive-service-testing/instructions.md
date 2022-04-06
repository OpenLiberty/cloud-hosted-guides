---
markdown-version: v1
title: instructions
branch: lab-204-instruction
version-history-start-date: 2022-02-09T14:19:17.000Z
---

::page{title="What you'll learn"}

You will learn how to write integration tests for reactive Java microservices and to run the tests in true-to-production
environments by using containers with [MicroShed Testing](https://microshed.org/microshed-testing/). MicroShed Testing tests
your containerized application from outside the container so that you are testing the exact same image that runs in
production. The reactive application in this guide sends and receives messages between services by using an external message
broker, [Apache Kafka](https://kafka.apache.org/). Using an external message broker enables asynchronous communications
between services so that requests are non-blocking and decoupled from responses. You can learn more about reactive Java 
services that use an external message broker to manage communications in the
[Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html) guide.

![Reactive system inventory application](https://raw.githubusercontent.com/OpenLiberty/guide-reactive-service-testing/prod/assets/reactive-messaging-system-inventory.png)


*True-to-production integration testing with MicroShed Testing*

Tests sometimes pass during the development and testing stages of an application's lifecycle but then fail in production
because of differences between your development and production environments. While you can create mock objects and custom
setups to minimize differences between environments, it is difficult to mimic a production system for an application
that uses an external messaging system. MicroShed Testing addresses this problem by enabling the testing of applications
in the same Docker containers that you’ll use in production. As a result, your environment remains the same throughout
the application’s lifecycle – from development, through testing, and into production.
You can learn more about MicroShed Testing in the
[Testing a MicroProfile or Jakarta EE application](https://openliberty.io/guides/microshed-testing.html) guide.


### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the tests, go to the ***finish*** directory and run the following Maven goal to install the ***models*** artifact to
the local Maven repository:

```bash
cd finish
mvn -pl models install
```


Next, navigate to the ***finish/system*** directory and run the following Maven goal to build the ***system*** service and run
the integration tests on an Open Liberty server in a container:

```bash
cd system
mvn verify
```

You will see the following output:

```
 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 33.001 s - in it.io.openliberty.guides.system.SystemServiceIT

 Results:

 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0


 --- maven-failsafe-plugin:2.22.2:verify (verify) @ system ---
 ------------------------------------------------------------------------
 BUILD SUCCESS
 ------------------------------------------------------------------------
 Total time:  52.817 s
 Finished at: 2020-03-13T16:28:55-04:00
 ------------------------------------------------------------------------
```

This command might take some time to run the first time because the dependencies and the Docker image for Open Liberty
must download. If you run the same command again, it will be faster.

You can also try out the ***inventory*** integration tests by repeating the same commands in the ***finish/inventory*** directory.

::page{title="Testing with the Kafka consumer client"}







Navigate to the ***start*** directory to begin.
```
cd /home/project/guide-reactive-service-testing/start
```

The example reactive application consists of the ***system*** and ***inventory*** microservices. The ***system*** microservice produces
messages to the Kafka message broker, and the ***inventory*** microservice consumes messages from the Kafka message broker.
You will write integration tests to see how you can use the Kafka consumer and producer client APIs to test each service.
MicroShed Testing and Kafka Testcontainers have already been included as required test dependencies in your Maven ***pom.xml***
files for the ***system*** and ***inventory*** services.

The ***start*** directory contains three directories: the ***system*** service directory, the ***inventory*** service directory, and the
***models*** directory. The ***models*** directory contains the model class that defines the structure of the system load data that is
used in the application. Run the following Maven goal to install the packaged ***models*** artifact to the local Maven repository
so it can be used later by the ***system*** and ***inventory*** services:

```bash
mvn -pl models install
```

If you don't have the latest Docker image, pull it by running the following command:

```bash
docker pull icr.io/appcafe/open-liberty:full-java11-openj9-ubi
```

With Open Liberty development mode, known as dev mode, you can use MicroShed Testing to run tests on an already running
Open Liberty server. Navigate to the ***start/system*** directory.
```
cd /home/project/guide-reactive-service-testing/start/system
```


Now you can add your test files.

The running ***system*** service searches for a Kafka topic to push its messages to. Because there are not yet any running Kafka
services, the ***system*** service throws errors. Later in the guide, you will write and run tests that start a Kafka
Testcontainer that can communicate with the ***system*** service. This will resolve the errors that you see now.

### Configuring your containers

Create a class to externalize your container configurations.

Create the ***AppContainerConfig*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-reactive-service-testing/start/system/src/test/java/it/io/openliberty/guides/system/AppContainerConfig.java
```


> Then, to open the unknown file in your IDE, select
> **File** > **Open** > guide-reactive-service-testing/start/unknown, or click the following button

::openFile{path="/home/project/guide-reactive-service-testing/start/unknown"}


The ***AppContainerConfig*** class externalizes test container setup and configuration, so
you can use the same application containers across multiple tests.The ***@Container***
annotation denotes an application container that is started up and used in the tests.

Two containers are used for testing the ***system*** service: the ***system*** container, which you built,
and the ***kafka*** container, which receives messages from the ***system*** service.

The ***dependsOn()*** method specifies that the ***system*** service container must wait until the ***kafka***
container is ready before it can start.

### Testing your containers

Now you can start writing the test that uses the configured containers.


Create the ***SystemServiceIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-reactive-service-testing/start/system/src/test/java/it/io/openliberty/guides/system/SystemServiceIT.java
```


> Then, to open the unknown file in your IDE, select
> **File** > **Open** > guide-reactive-service-testing/start/unknown, or click the following button

::openFile{path="/home/project/guide-reactive-service-testing/start/unknown"}



The test uses the ***KafkaConsumer*** client API and
is configured by using the ***@KafkaConsumerClient*** annotation. The consumer client is configured
to consume messages from the ***system.load*** topic in the ***kafka*** container.
To learn more about Kafka APIs and how to use them, check out the
[official Kafka Documentation](https://kafka.apache.org/documentation/#api).

To consume messages from a stream, the messages need to be deserialized from bytes. Kafka has its own default deserializer,
but a custom deserializer is provided for you. The deserializer is configured to the consumer’s
***valueDeserializer*** and is implemented in the
***SystemLoad*** class.

The running ***system*** service container produces messages to the ***systemLoad*** Kafka topic,
as denoted by the ***@Outgoing*** annotation. The ***testCpuStatus()***
test method ***polls*** a record from Kafka every 3 seconds until the timeout limit. It then
***verifies*** that the record polled matches the expected record.


You will see the following output:

```
 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 25.674 s - in it.io.openliberty.guides.system.SystemServiceIT

 Results:

 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

 Integration tests finished.
```

After you are finished running tests, stop the Open Liberty server by typing `q` in the command-line session where you ran
the server, and then press the ***enter/return*** key.

If you aren't running in dev mode, you can run the tests by running the following command:

```bash
mvn verify
```

You will see the following output:

```
 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 33.001 s - in it.io.openliberty.guides.system.SystemServiceIT

 Results:

 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0


 --- maven-failsafe-plugin:2.22.2:verify (verify) @ system ---
 ------------------------------------------------------------------------
 BUILD SUCCESS
 ------------------------------------------------------------------------
 Total time:  52.817 s
 Finished at: 2020-03-13T16:28:55-04:00
 ------------------------------------------------------------------------
```

::page{title="Testing with the Kafka producer client"}




The ***inventory*** service is tested in the same way as the ***system*** service. The only difference is that the ***inventory*** service
consumes messages, which means that tests are written to use the Kafka producer client.

### Configuring your containers

Navigate to the ***start/inventory*** directory.
```
cd /home/project/guide-reactive-service-testing/start/inventory
```

The ***AppContainerConfig*** class is provided, and it is configured in the same way as it was for the ***system*** service. The two
containers that are configured for use in the ***inventory*** service integration test are the ***kafka*** and ***inventory*** containers.

### Testing your containers

As you did with the ***system*** service, run Open Liberty in dev mode to listen for file changes:

```bash
mvn liberty:dev
```

Now you can create your integrated test.

Create the ***InventoryServiceIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-reactive-service-testing/start/inventory/src/test/java/it/io/openliberty/guides/inventory/InventoryServiceIT.java
```


> Then, to open the unknown file in your IDE, select
> **File** > **Open** > guide-reactive-service-testing/start/unknown, or click the following button

::openFile{path="/home/project/guide-reactive-service-testing/start/unknown"}


The ***InventoryServiceIT*** class uses the ***KafkaProducer***
client API to produce messages in the test environment for the ***inventory*** service container to consume. The
***@KafkaProducerClient*** annotation configures the producer to use the custom serializer provided in
the ***SystemLoad*** class. The ***@KafkaProducerClient*** annotation
doesn't include a topic that the client produces messages to because it has the flexibility to produce messages to any topic.
In this example, it is configured to produce messages to the ***system.load*** topic.

The ***testCpuUsage*** test method produces a message to Kafka and then
***verifies*** that the response from the ***inventory*** service matches what is expected.

The ***@RESTClient*** annotation injects a REST client proxy of the
***InventoryResource*** class, which allows HTTP requests to be made to the running application.
To learn more about REST clients, check out the [Consuming RESTful services with template interfaces](https://openliberty.io/guides/microprofile-rest-client.html)
guide.


You will see the following output:

```
 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 32.564 s - in it.io.openliberty.guides.inventory.InventoryServiceIT

 Results:

 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

 Integration tests finished.
```

After you are finished running tests, stop the Open Liberty server by typing `q` in the command-line session where you ran
the server, and then press the ***enter/return*** key.

If you aren't running in dev mode, you can run the tests by running the following command:

```bash
mvn verify
```

You will see the following output:

```
 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 42.345 s - in it.io.openliberty.guides.inventory.InventoryServiceIT

 Results:

 Tests run: 1, Failures: 0, Errors: 0, Skipped: 0


 --- maven-failsafe-plugin:2.22.2:verify (verify) @ inventory ---
 ------------------------------------------------------------------------
 BUILD SUCCESS
 ------------------------------------------------------------------------
 Total time:  48.213 s
 Finished at: 2020-03-13T16:43:34-04:00
 ------------------------------------------------------------------------
```

::page{title="Summary"}

### Nice Work!

You just tested two reactive Java microservices using MicroShed Testing.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-reactive-service-testing*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-reactive-service-testing
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Testing%20reactive%20Java%20microservices&guide-id=cloud-hosted-guide-reactive-service-testing)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-reactive-service-testing/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-reactive-service-testing/pulls)



### Where to next?

* [Creating reactive Java microservices](https://openliberty.io/guides/microprofile-reactive-messaging.html)
* [Testing a MicroProfile or Jakarta EE application](https://openliberty.io/guides/microshed-testing.html)

**Learn more about MicroShed Testing**
* [Visit the official MicroShed Testing website](https://microshed.org/microshed-testing/)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
