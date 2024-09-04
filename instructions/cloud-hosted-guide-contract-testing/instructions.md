---
markdown-version: v1
tool-type: theia
---
::page{title="Welcome to the Testing microservices with consumer-driven contracts guide!"}

Learn how to test Java microservices with consumer-driven contracts in Open Liberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

With a microservices-based architecture, you need robust testing to ensure that microservices that depend on one another are able to communicate effectively.  Typically, to prevent multiple points of failure at different integration points, a combination of unit, integration, and end-to-end tests are used. While unit tests are fast, they are less trustworthy because they run in isolation and usually rely on mock data.

Integration tests address this issue by testing against real running services. However, they tend to be slow as the tests depend on other microservices and are less reliable because they are prone to external changes.

Usually, end-to-end tests are more trustworthy because they verify functionality from the perspective of a user. However, a graphical user interface (GUI) component is often required to perform end-to-end tests, and GUI components rely on third-party software, such as Selenium, which requires heavy computation time and resources.

*What is contract testing?*

Contract testing bridges the gaps among the shortcomings of these different testing methodologies. Contract testing is a technique for testing an integration point by isolating each microservice and checking whether the HTTP requests and responses that the microservice transmits conform to a shared understanding that is documented in a contract. This way, contract testing ensures that microservices can communicate with each other.

[Pact](https://docs.pact.io/) is an open source contract testing tool for testing HTTP requests, responses, and message integrations by using contract tests.

The [Pact Broker](https://docs.pact.io/pact_broker/docker_images) is an application for sharing Pact contracts and verification results. The Pact Broker is also an important piece for integrating Pact into continuous integration and continuous delivery (CI/CD) pipelines.

The two microservices you will interact with are called ***system*** and ***inventory***. The ***system*** microservice returns the JVM system properties of its host. The ***inventory*** microservice retrieves specific properties from the ***system*** microservice.

You will learn how to use the Pact framework to write contract tests for the ***inventory*** microservice that will then be verified by the ***system*** microservice.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-contract-testing.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-contract-testing.git
cd guide-contract-testing
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Starting the Pact Broker

Run the following command to start the Pact Broker:
```bash
docker-compose -f "pact-broker/docker-compose.yml" up -d --build
```

When the Pact Broker is running, you'll see the following output:
```
Creating pact-broker_postgres_1 ... done
Creating pact-broker_pact-broker_1 ... done
```


Click the following button to visit the Pact Broker to confirm that it is working. The Pact Broker can be found at the `https://accountname-9292.theiadocker-4.proxy.cognitiveclass.ai` URL, where ***accountname*** is your account name.

::startApplication{port="9292" display="external" name="Visit Pact Broker" route="/"}

Confirm that you can access the user interface of the Pact Broker. The Pact Broker interface is similar to the following image:

![Pact Broker webpage](https://raw.githubusercontent.com/OpenLiberty/guide-contract-testing/prod/assets/pact-broker-webpage.png)





You can refer to the [official Pact Broker documentation](https://docs.pact.io/pact_broker/docker_images/pactfoundation) for more information about the components of the Docker Compose file.

::page{title="Implementing pact testing in the inventory service"}

Navigate to the ***start/inventory*** directory to begin.

```bash
cd /home/project/guide-contract-testing/start/inventory
```

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), dev mode listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```bash
mvn liberty:dev
```

After you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.


Open a new command-line session.

Create the InventoryPactIT class file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-contract-testing/start/inventory/src/test/java/io/openliberty/guides/inventory/InventoryPactIT.java
```


> Then, to open the InventoryPactIT.java file in your IDE, select
> **File** > **Open** > guide-contract-testing/start/inventory/src/test/java/io/openliberty/guides/inventory/InventoryPactIT.java, or click the following button

::openFile{path="/home/project/guide-contract-testing/start/inventory/src/test/java/io/openliberty/guides/inventory/InventoryPactIT.java"}



```java

package io.openliberty.guides.inventory;

import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

public class InventoryPactIT {
  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("System", this);

  @Pact(consumer = "Inventory")
  public RequestResponsePact createPactServer(PactDslWithProvider builder) {
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", "application/json");

    return builder
      .given("wlp.server.name is defaultServer")
      .uponReceiving("a request for server name")
      .path("/system/properties/key/wlp.server.name")
      .method("GET")
      .willRespondWith()
      .headers(headers)
      .status(200)
      .body(new PactDslJsonArray().object()
        .stringValue("wlp.server.name", "defaultServer"))
      .toPact();
  }

  @Pact(consumer = "Inventory")
  public RequestResponsePact createPactEdition(PactDslWithProvider builder) {
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", "application/json");

    return builder
      .given("Default directory is true")
      .uponReceiving("a request to check for the default directory")
      .path("/system/properties/key/wlp.user.dir.isDefault")
      .method("GET")
      .willRespondWith()
      .headers(headers)
      .status(200)
      .body(new PactDslJsonArray().object()
        .stringValue("wlp.user.dir.isDefault", "true"))
      .toPact();
  }

  @Pact(consumer = "Inventory")
  public RequestResponsePact createPactVersion(PactDslWithProvider builder) {
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", "application/json");

    return builder
      .given("version is 1.1")
      .uponReceiving("a request for the version")
      .path("/system/properties/version")
      .method("GET")
      .willRespondWith()
      .headers(headers)
      .status(200)
      .body(new PactDslJsonBody()
        .decimalType("system.properties.version", 1.1))
      .toPact();
  }

  @Pact(consumer = "Inventory")
  public RequestResponsePact createPactInvalid(PactDslWithProvider builder) {

    return builder
      .given("invalid property")
      .uponReceiving("a request with an invalid property")
      .path("/system/properties/invalidProperty")
      .method("GET")
      .willRespondWith()
      .status(404)
      .toPact();
  }

  @Test
  @PactVerification(value = "System", fragment = "createPactServer")
  public void runServerTest() {
    String serverName = new Inventory(mockProvider.getUrl()).getServerName();
    assertEquals("Expected server name does not match",
      "[{\"wlp.server.name\":\"defaultServer\"}]", serverName);
  }

  @Test
  @PactVerification(value = "System", fragment = "createPactEdition")
  public void runEditionTest() {
    String edition = new Inventory(mockProvider.getUrl()).getEdition();
    assertEquals("Expected edition does not match",
      "[{\"wlp.user.dir.isDefault\":\"true\"}]", edition);
  }

  @Test
  @PactVerification(value = "System", fragment = "createPactVersion")
  public void runVersionTest() {
    String version = new Inventory(mockProvider.getUrl()).getVersion();
    assertEquals("Expected version does not match",
      "{\"system.properties.version\":1.1}", version);
  }

  @Test
  @PactVerification(value = "System", fragment = "createPactInvalid")
  public void runInvalidTest() {
    String invalid = new Inventory(mockProvider.getUrl()).getInvalidProperty();
    assertEquals("Expected invalid property response does not match",
      "", invalid);
  }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***InventoryPactIT*** class contains a ***PactProviderRule*** mock provider that mimics the HTTP responses from the ***system*** microservice. The ***@Pact*** annotation takes the name of the microservice as a parameter, which makes it easier to differentiate microservices from each other when you have multiple applications.

The ***createPactServer()*** method defines the minimal expected responses for a specific endpoint, which is known as an interaction. For each interaction, the expected request and the response are registered with the mock service by using the ***@PactVerification*** annotation.

The test sends a real request with the ***getUrl()*** method of the mock provider. The mock provider compares the actual request with the expected request and confirms whether the comparison is successful. Finally, the ***assertEquals()*** method confirms that the response is correct.

Replace the inventory Maven project file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-contract-testing/start/inventory/pom.xml, or click the following button

::openFile{path="/home/project/guide-contract-testing/start/inventory/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-contract-testing-inventory</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <!-- Liberty configuration -->
        <liberty.var.http.port>9091</liberty.var.http.port>
        <liberty.var.https.port>9454</liberty.var.https.port>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>6.1</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>10.0.0</version>
            <scope>provided</scope>
        </dependency>
        <!-- For tests -->
        <dependency>
            <groupId>au.com.dius</groupId>
            <artifactId>pact-jvm-consumer-junit</artifactId>
            <version>4.0.10</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.16</version>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-client</artifactId>
            <version>6.2.10.Final</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>au.com.dius.pact.provider</groupId>
                <artifactId>maven</artifactId>
                <version>4.6.14</version>
                <configuration>
                    <serviceProviders>
                        <serviceProvider>
                            <name>System</name>
                            <protocol>http</protocol>
                            <host>localhost</host>
                            <port>9090</port>
                            <path>/</path>
                            <pactFileDirectory>target/pacts</pactFileDirectory>
                        </serviceProvider>
                    </serviceProviders>
                    <projectVersion>${project.version}</projectVersion>
                    <skipPactPublish>false</skipPactPublish>
                    <pactBrokerUrl>http://localhost:9292</pactBrokerUrl>
                    <tags>
                        <tag>open-liberty-pact</tag>
                    </tags>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
            </plugin>
            <!-- Plugin to run functional tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.5.0</version>
                <configuration>
                    <systemPropertyVariables>
                        <http.port>${liberty.var.http.port}</http.port>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
            <!-- Enable liberty-maven plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.10.3</version>
            </plugin>
        </plugins>
    </build>
</project>
```



The Pact framework provides a ***Maven*** plugin that can be added to the build section of the ***pom.xml*** file. The ***serviceProvider*** element defines the endpoint URL for the ***system*** microservice and the ***pactFileDirectory*** directory where you want to store the pact file. The ***pact-jvm-consumer-junit*** dependency provides the base test class that you can use with JUnit to build unit tests.

After you create the ***InventoryPactIT.java*** class and replace the ***pom.xml*** file, Open Liberty automatically reloads its configuration.

The contract between the ***inventory*** and ***system*** microservices is known as a pact. Each pact is a collection of interactions. In this guide, those interactions are defined in the ***InventoryPactIT*** class.

Press the ***enter/return*** key to run the tests and generate the pact file from the command-line session where you started the ***inventory*** microservice.

When completed, you'll see a similar output to the following example:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running io.openliberty.guides.inventory.InventoryPactIT
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.631 s - in io.openliberty.guides.inventory.InventoryPactIT
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

When you integrate the Pact framework in a CI/CD build pipeline, you can use the ***mvn failsafe:integration-test*** goal to generate the pact file. The Maven failsafe plug-in provides a lifecycle phase for running integration tests that run after unit tests. By default, it looks for classes that are suffixed with ***IT***, which stands for Integration Test. You can refer to the [Maven failsafe plug-in documentation](https://maven.apache.org/surefire/maven-failsafe-plugin/) for more information.

The generated pact file is named ***Inventory-System.json*** and is located in the ***inventory/target/pacts*** directory. The pact file contains the defined interactions in JSON format:

```
{
...
"interactions": [
{
      "description": "a request for server name",
      "request": {
        "method": "GET",
        "path": "/system/properties/key/wlp.server.name"
      },
      "response": {
        "status": 200,
        "headers": {
          "Content-Type": "application/json"
        },
        "body": [
          {
            "wlp.server.name": "defaultServer"
          }
        ]
      },
      "providerStates": [
        {
          "name": "wlp.server.name is defaultServer"
        }
      ]
    }
...
  ]
}
```


Open a new command-line session and navigate to the `start/inventory` directory.

```bash
cd /home/project/guide-contract-testing/start/inventory
```

Publish the generated pact file to the Pact Broker by running the following command:
```bash
mvn pact:publish
```

After the file is published, you'll see a similar output to the following example:
```
--- maven:4.1.21:publish (default-cli) @ inventory ---
Publishing 'Inventory-System.json' with tags 'open-liberty-pact' ... OK
```

::page{title="Verifying the pact in the Pact Broker"}


Refresh the Pact Broker at the `https://accountname-9292.theiadocker-4.proxy.cognitiveclass.ai` URL, where ***accountname*** is your account name.

::startApplication{port="9292" display="external" name="Visit Pact Broker" route="/"}

The last verified column doesn't show a timestamp because the ***system*** microservice hasn't verified the pact yet.

![Pact Broker webpage for new entry](https://raw.githubusercontent.com/OpenLiberty/guide-contract-testing/prod/assets/pact-broker-webpage-refresh.png)






You can see detailed insights about each interaction by clicking the following button or going to the `https://accountname-9292.theiadocker-4.proxy.cognitiveclass.ai/pacts/provider/System/consumer/Inventory/latest` URL, where ***accountname*** is your account name.

::startApplication{port="9292" display="external" name="Visit Pact Broker" route="/pacts/provider/System/consumer/Inventory/latest"}

The insights look similar to the following image:

![Pact Broker webpage for Interactions](https://raw.githubusercontent.com/OpenLiberty/guide-contract-testing/prod/assets/pact-broker-interactions.png)


::page{title="Implementing pact testing in the system service"}


Open another command-line session and navigate to the ***start/system*** directory.

```bash
cd /home/project/guide-contract-testing/start/system
```

Start Open Liberty in dev mode for the ***system*** microservice:
```bash
mvn liberty:dev
```

After you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```




Open a new command-line session.

Create the SystemBrokerIT class file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-contract-testing/start/system/src/test/java/it/io/openliberty/guides/system/SystemBrokerIT.java
```


> Then, to open the SystemBrokerIT.java file in your IDE, select
> **File** > **Open** > guide-contract-testing/start/system/src/test/java/it/io/openliberty/guides/system/SystemBrokerIT.java, or click the following button

::openFile{path="/home/project/guide-contract-testing/start/system/src/test/java/it/io/openliberty/guides/system/SystemBrokerIT.java"}



```java
package it.io.openliberty.guides.system;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Provider("System")
@Consumer("Inventory")
@PactBroker(
  host = "localhost",
  port = "9292",
  consumerVersionSelectors = {
    @VersionSelector(tag = "open-liberty-pact")
  })
public class SystemBrokerIT {
  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  void pactVerificationTestTemplate(PactVerificationContext context) {
    context.verifyInteraction();
  }

  @BeforeAll
  static void enablePublishingPact() {
    System.setProperty("pact.verifier.publishResults", "true");
  }

  @BeforeEach
  void before(PactVerificationContext context) {
    int port = Integer.parseInt(System.getProperty("http.port"));
    context.setTarget(new HttpTestTarget("localhost", port));
  }

  @State("wlp.server.name is defaultServer")
  public void validServerName() {
  }

  @State("Default directory is true")
  public void validEdition() {
  }

  @State("version is 1.1")
  public void validVersion() {
  }

  @State("invalid property")
  public void invalidProperty() {
  }
}
```




The connection information for the Pact Broker is provided with the ***@PactBroker*** annotation. The dependency also provides a JUnit5 Invocation Context Provider with the ***pactVerificationTestTemplate()*** method to generate a test for each of the interactions.

The ***pact.verifier.publishResults*** property is set to ***true*** so that the results are sent to the Pact Broker after the tests are completed.

The test target is defined in the ***PactVerificationContext*** context to point to the running endpoint of the ***system*** microservice.

The ***@State*** annotation must match the ***given()*** parameter that was provided in the ***inventory*** test class so that Pact can identify which test case to run against which endpoint.

Replace the system Maven project file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-contract-testing/start/system/pom.xml, or click the following button

::openFile{path="/home/project/guide-contract-testing/start/system/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-contract-testing-system</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- Liberty configuration -->
        <liberty.var.http.port>9090</liberty.var.http.port>
        <liberty.var.https.port>9453</liberty.var.https.port>
        <debugPort>8787</debugPort>
    </properties>

    <dependencies>
        <!-- Provided dependencies -->
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>6.1</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>10.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>au.com.dius.pact.provider</groupId>
            <artifactId>junit5</artifactId>
            <version>4.6.14</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.16</version>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-client</artifactId>
            <version>6.2.10.Final</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <!-- Enable liberty-maven plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.10.3</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
            </plugin>
            <!-- Plugin to run functional tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.5.0</version>
                <configuration>
                    <systemPropertyVariables>
                        <http.port>${liberty.var.http.port}</http.port>
                        <pact.provider.version>${project.version}</pact.provider.version>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```



The ***system*** microservice uses the ***junit5*** pact provider dependency to connect to the Pact Broker and verify the pact file. Ideally, in a CI/CD build pipeline, the ***pact.provider.version*** element is dynamically set to the build number so that you can identify where a breaking change is introduced.

After you create the ***SystemBrokerIT.java*** class and replace the ***pom.xml*** file, Open Liberty automatically reloads its configuration.

::page{title="Verifying the contract"}

In the command-line session where you started the ***system*** microservice, press the ***enter/return*** key to run the tests to verify the pact file. When you integrate the Pact framework into a CI/CD build pipeline, you can use the ***mvn failsafe:integration-test*** goal to verify the pact file from the Pact Broker.

The tests fail with the following errors:
```
[ERROR] Failures: 
[ERROR]   SystemBrokerIT.pactVerificationTestTemplate:28 Pact between Inventory (1.0-SNAPSHOT) and System - Upon a request for the version 
Failures:

1) Verifying a pact between Inventory and System - a request for the version has a matching body

    1.1) body: $.system.properties.version Expected "1.x" (String) to be a decimal number


[INFO] 
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
```

The test from the ***system*** microservice fails because the ***inventory*** microservice was expecting a decimal, ***1.1***, for the value of the ***system.properties.version*** property, but it received a string, ***"1.1"***.

Correct the value of the ***system.properties.version*** property to a decimal.
Replace the SystemResource class file.

> To open the SystemResource.java file in your IDE, select
> **File** > **Open** > guide-contract-testing/start/system/src/main/java/io/openliberty/guides/system/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-contract-testing/start/system/src/main/java/io/openliberty/guides/system/SystemResource.java"}



```java
package io.openliberty.guides.system;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

@RequestScoped
@Path("/properties")
public class SystemResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed(name = "getPropertiesTime",
    description = "Time needed to get the JVM system properties")
  @Counted(absolute = true,
    description = "Number of times the JVM system properties are requested")

  public Response getProperties() {
    return Response.ok(System.getProperties()).build();
  }

  @GET
  @Path("/key/{key}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPropertiesByKey(@PathParam("key") String key) {
    try {
      JsonArray response = Json.createArrayBuilder()
        .add(Json.createObjectBuilder()
          .add(key, System.getProperties().get(key).toString()))
        .build();
      return Response.ok(response, MediaType.APPLICATION_JSON).build();
    } catch (java.lang.NullPointerException exception) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @GET
  @Path("/version")
  @Produces(MediaType.APPLICATION_JSON)
  public JsonObject getVersion() {
    JsonObject response = Json.createObjectBuilder()
                          .add("system.properties.version", 1.1)
                          .build();
    return response;
  }
}
```



Press the ***enter/return*** key to rerun the tests from the command-line session where you started the ***system*** microservice.

If the tests are successful, you'll see a similar output to the following example:
```
...
Verifying a pact between pact between Inventory (1.0-SNAPSHOT) and System

  Notices:
    1) The pact at http://localhost:9292/pacts/provider/System/consumer/Inventory/pact-version/XXX is being verified because it matches the following configured selection criterion: latest pact for a consumer version tagged 'open-liberty-pact'

  [from Pact Broker http://localhost:9292/pacts/provider/System/consumer/Inventory/pact-version/XXX]
  Given version is 1.1
  a request for the version
    returns a response which
      has status code 200 (OK)
      has a matching body (OK)
[main] INFO au.com.dius.pact.provider.DefaultVerificationReporter - Published verification result of 'au.com.dius.pact.core.pactbroker.TestResult$Ok@4d84dfe7' for consumer 'Consumer(name=Inventory)'
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.835 s - in it.io.openliberty.guides.system.SystemBrokerIT
...
```


After the tests are complete, refresh the Pact Broker at the `https://accountname-9292.theiadocker-4.proxy.cognitiveclass.ai` URL, where ***accountname*** is your account name.

::startApplication{port="9292" display="external" name="Visit Pact Broker" route="/"}

Confirm that the last verified column now shows a timestamp:

![Pact Broker webpage for verified](https://raw.githubusercontent.com/OpenLiberty/guide-contract-testing/prod/assets/pact-broker-webpage-verified.png)





The pact file that's created by the ***inventory*** microservice was successfully verified by the ***system*** microservice through the Pact Broker. This ensures that responses from the ***system*** microservice meet the expectations of the ***inventory*** microservice.

::page{title="Tearing down the environment"}

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line sessions where you ran the Liberty instances for the ***system*** and ***inventory*** microservices.

Navigate back to the ***/guide-contract-testing*** directory and run the following commands to remove the Pact Broker:

```bash
cd /home/project/guide-contract-testing
```

```bash
docker-compose -f "pact-broker/docker-compose.yml" down
docker rmi postgres:16.2
docker rmi pactfoundation/pact-broker:latest
docker volume rm pact-broker_postgres-volume
```

::page{title="Summary"}

### Nice Work!

You implemented contract testing in Java microservices by using Pact and verified the contract with the Pact Broker.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-contract-testing*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-contract-testing
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Testing%20microservices%20with%20consumer-driven%20contracts&guide-id=cloud-hosted-guide-contract-testing)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-contract-testing/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-contract-testing/pulls)



### Where to next?

* [Testing a MicroProfile or Jakarta EE application](https://openliberty.io/guides/microshed-testing.html)
* [Testing reactive Java microservices](https://openliberty.io/guides/reactive-service-testing.html)
* [Testing microservices with the Arquillian managed container](https://openliberty.io/guides/arquillian-managed.html)

**Learn more about the Pact framework**
* [Go to the Pact website.](https://pact.io/)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
