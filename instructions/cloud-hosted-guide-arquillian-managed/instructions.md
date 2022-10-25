---
markdown-version: v1
title: instructions
branch: lab-449-instruction
version-history-start-date: 2021-11-25 22:27:51 UTC
tool-type: theia
---
::page{title="Welcome to the Testing microservices with the Arquillian managed container guide!"}

Learn how to develop tests for your microservices with the Arquillian managed container and run the tests on Open Liberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You will learn how to develop tests for your microservices by using the [Arquillian Liberty Managed container](https://github.com/OpenLiberty/liberty-arquillian/tree/master/liberty-managed) and JUnit with Maven on Open Liberty. [Arquillian](http://arquillian.org/) is a testing framework to develop automated functional, integration and acceptance tests for your Java applications. Arquillian sets up the test environment and handles the application server lifecycle for you so you can focus on writing tests.

You will develop Arquillian tests that use JUnit as the runner and build your tests with Maven using the Liberty Maven plug-in. This technique simplifies the process of managing Arquillian dependencies and the setup of your Arquillian managed container.

You will work with an ***inventory*** microservice, which stores information about various systems. The ***inventory*** service communicates with the ***system*** service on a particular host to retrieve its system properties and store them. You will develop functional and integration tests for the microservices. You will also learn about the Maven and server configurations so that you can run your tests on Open Liberty with the Arquillian Liberty Managed container.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-arquillian-managed.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-arquillian-managed.git
cd guide-arquillian-managed
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.


::page{title="Developing Arquillian tests"}

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-arquillian-managed/start
```

You'll develop tests that use Arquillian and JUnit to verify the ***inventory*** microservice as an endpoint and the functions of the ***InventoryResource*** class. The code for the microservices is in the ***src/main/java/io/openliberty/guides*** directory.

Create the ***InventoryArquillianIT*** test class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-arquillian-managed/start/src/test/java/it/io/openliberty/guides/inventory/InventoryArquillianIT.java
```


> Then, to open the InventoryArquillianIT.java file in your IDE, select
> **File** > **Open** > guide-arquillian-managed/start/src/test/java/it/io/openliberty/guides/inventory/InventoryArquillianIT.java, or click the following button

::openFile{path="/home/project/guide-arquillian-managed/start/src/test/java/it/io/openliberty/guides/inventory/InventoryArquillianIT.java"}



```java
package it.io.openliberty.guides.inventory;

import java.net.URL;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.openliberty.guides.inventory.InventoryResource;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;

@RunWith(Arquillian.class)
public class InventoryArquillianIT {

    private static final String WARNAME = System.getProperty("arquillian.war.name");
    private final String INVENTORY_SYSTEMS = "inventory/systems";
    private Client client = ClientBuilder.newClient();

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, WARNAME)
                                       .addPackages(true, "io.openliberty.guides");
        return archive;
    }

    @ArquillianResource
    private URL baseURL;

    @Inject
    InventoryResource invSrv;

    @Test
    @RunAsClient
    @InSequence(1)
    public void testInventoryEndpoints() throws Exception {
        String localhosturl = baseURL + INVENTORY_SYSTEMS + "/localhost";

        WebTarget localhosttarget = client.target(localhosturl);
        Response localhostresponse = localhosttarget.request().get();

        Assert.assertEquals("Incorrect response code from " + localhosturl, 200,
                            localhostresponse.getStatus());

        JsonObject localhostobj = localhostresponse.readEntity(JsonObject.class);
        Assert.assertEquals("The system property for the local and remote JVM "
                        + "should match", System.getProperty("os.name"),
                            localhostobj.getString("os.name"));

        String invsystemsurl = baseURL + INVENTORY_SYSTEMS;

        WebTarget invsystemstarget = client.target(invsystemsurl);
        Response invsystemsresponse = invsystemstarget.request().get();

        Assert.assertEquals("Incorrect response code from " + localhosturl, 200,
                            invsystemsresponse.getStatus());

        JsonObject invsystemsobj = invsystemsresponse.readEntity(JsonObject.class);

        int expected = 1;
        int actual = invsystemsobj.getInt("total");
        Assert.assertEquals("The inventory should have one entry for localhost",
                            expected, actual);
        localhostresponse.close();
    }

    @Test
    @InSequence(2)
    public void testInventoryResourceFunctions() {
        InventoryList invList = invSrv.listContents();
        Assert.assertEquals(1, invList.getTotal());

        List<SystemData> systemDataList = invList.getSystems();
        Assert.assertTrue(systemDataList.get(0).getHostname().equals("localhost"));

        Assert.assertTrue(systemDataList.get(0).getProperties().get("os.name")
                                        .equals(System.getProperty("os.name")));
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


Notice that the JUnit Arquillian runner runs the tests instead of the standard JUnit runner. The ***@RunWith*** annotation preceding the class tells JUnit to run the tests by using Arquillian.

The method annotated by ***@Deployment*** defines the content of the web archive, which is going to be deployed onto the Open Liberty server. The tests are either run on or against the server. The ***testable = true*** attribute enables the deployment to run the tests "in container", that is the tests are run on the server.


The ***WARNAME*** variable is used to name the web archive and is defined in the ***pom.xml*** file. This name is necessary if you don't want a randomly generated web archive name.

The ShrinkWrap API is used to create the web archive. All of the packages in the ***inventory*** service must be added to the web archive; otherwise, the code compiles successfully but fails at runtime when the injection of the ***InventoryResource*** class takes place. You can learn about the ShrinkWrap archive configuration in this [Arquillian guide](http://arquillian.org/guides/shrinkwrap_introduction/).

The ***@ArquillianResource*** annotation is used to retrieve the ***http://localhost:9080/arquillian-managed/*** base URL for this web service. The annotation provides the host name, port number and web archive information for this service, so you don't need to hardcode these values in the test case. The ***arquillian-managed*** path in the URL comes from the WAR name you specified when you created the web archive in the ***@Deployment*** annotated method. It's needed when the ***inventory*** service communicates with the ***system*** service to get the system properties.

The ***testInventoryEndpoints*** method is an integration test to test the ***inventory*** service endpoints. The ***@RunAsClient*** annotation added in this test case indicates that this test case is to be run on the client side. By running the tests on the client side, the tests are run against the managed container. The endpoint test case first calls the ***http://localhost:9080/{WARNAME}/inventory/systems/{hostname}*** endpoint with the ***localhost*** host name to add its system properties to the inventory. The test verifies that the system property for the local and service JVM match. Then, the test method calls the ***http://localhost:9080/{WARNAME}/inventory/systems*** endpoint. The test checks that the inventory has one host and that the host is ***localhost***. The test also verifies that the system property stored in the inventory for the local and service JVM match.

Contexts and Dependency Injection (CDI) is used to inject an instance of the ***InventoryResource*** class into this test class. You can learn more about CDI in the [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html) guide.

The injected ***InventoryResource*** instance is then tested by the ***testInventoryResourceFunctions*** method. This test case calls the ***listContents()*** method to get all systems that are stored in this inventory and verifies that ***localhost*** is the only system being found. Notice the functional test case doesn't store any system in the inventory, the ***localhost*** system is from the endpoint test case that ran before this test case. The ***@InSequence*** Arquillian annotation guarantees the test sequence. The sequence is important for the two tests, as the results in the first test impact the second one.

The test cases are ready to run. You will configure the Maven build and the Liberty application server to run them.

::page{title="Configuring Arquillian with Liberty"}

Configure your build to use the Arquillian Liberty Managed container and set up your Open Liberty server to run your test cases by configuring the ***server.xml*** file.

### Configuring your test build

First, configure your test build with Maven. All of the Maven configuration takes place in the ***pom.xml*** file, which is provided for you.


> From the menu of the IDE, select **File** > **Open** > guide-arquillian-managed/start/pom.xml, or click the following button

::openFile{path="/home/project/guide-arquillian-managed/start/pom.xml"}

Let's look into each of the required elements for this configuration.

You need the ***arquillian-bom*** Bill of Materials. It's a Maven artifact that defines the versions of Arquillian dependencies to make dependency management easier.

The ***arquillian-liberty-managed-junit*** dependency bundle, which includes all the core dependencies, is required to run the Arquillian tests on a managed Liberty container that uses JUnit. You can learn more about the [Arquillian Liberty dependency bundles](https://github.com/OpenLiberty/arquillian-liberty-dependencies). The ***shrinkwrap-api*** dependency allows you to create your test archive, which is packaged into a WAR file and deployed to the Open Liberty server.

The ***maven-failsafe-plugin*** artifact runs your Arquillian integration tests by using JUnit.

Lastly, specify the ***liberty-maven-plugin*** configuration that defines your Open Liberty runtime configuration. When the application runs in an Arquillian Liberty managed container, the name of the war file is used as the context root of the application. You can pass context root information to the application and customize the container by using the ***arquillianProperties*** configuration. To learn more about the ***arquillianProperties*** configuration, see the [Arquillian Liberty Managed documentation](https://github.com/OpenLiberty/liberty-arquillian/blob/main/liberty-managed/README.md#configuration).


### Configuring the server.xml file

Now that you're done configuring your Maven build, set up your Open Liberty server to run your test cases by configuring the ***server.xml*** file.

Take a look at the ***server.xml*** file.


> From the menu of the IDE, select **File** > **Open** > guide-arquillian-managed/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-arquillian-managed/start/src/main/liberty/config/server.xml"}

The ***localConnector*** feature is required by the Arquillian Liberty Managed container to connect to and communicate with the Open Liberty runtime. The ***servlet*** feature is required during the deployment of the Arquillian tests in which servlets are created to perform the in-container testing.


::page{title="Running the tests"}

It's now time to build and run your Arquillian tests. Navigate to the ***start*** directory. First, run the Maven command to package the application. Then, run the ***liberty-maven-plugin*** goals to create the application server, install the features, and deploy the application to the server. The ***configure-arquillian*** goal configures your Arquillian container. You can learn more about this goal in the [configure-arquillian goal documentation](https://github.com/OpenLiberty/ci.maven/blob/main/docs/configure-arquillian.md).

```bash
cd /home/project/guide-arquillian-managed/start
mvn clean package
mvn liberty:create liberty:install-feature
mvn liberty:configure-arquillian
```

Now, you can run your Arquillian tests with the Maven ***integration-test*** goal:

```bash
mvn failsafe:integration-test
```

In the test output, you can see that the application server launched, and that the web archive, ***arquillian-managed***, started as an application in the server. You can also see that the tests are running and that the results are reported.

After the tests stop running, the test application is automatically undeployed and the server shuts down. You should then get a message indicating that the build and tests are successful.

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running it.io.openliberty.guides.system.SystemArquillianIT
...
[AUDIT   ] CWWKE0001I: The server defaultServer has been launched.
[AUDIT   ] CWWKG0093A: Processing configuration drop-ins resource: guide-arquillian-managed/finish/target/liberty/wlp/usr/servers/defaultServer/configDropins/overrides/liberty-plugin-variable-config.xml
[INFO    ] CWWKE0002I: The kernel started after 0.854 seconds
[INFO    ] CWWKF0007I: Feature update started.
[AUDIT   ] CWWKZ0058I: Monitoring dropins for applications.
[INFO    ] Aries Blueprint packages not available. So namespaces will not be registered
[INFO    ] CWWKZ0018I: Starting application guide-arquillian-managed.
...
[INFO    ] SRVE0169I: Loading Web Module: guide-arquillian-managed.
[INFO    ] SRVE0250I: Web Module guide-arquillian-managed has been bound to default_host.
[AUDIT   ] CWWKT0016I: Web application available (default_host): http://localhost:9080/
[INFO    ] SESN0176I: A new session context will be created for application key default_host/
[INFO    ] SESN0172I: The session manager is using the Java default SecureRandom implementation for session ID generation.
[AUDIT   ] CWWKZ0001I: Application guide-arquillian-managed started in 1.126 seconds.
[INFO    ] CWWKO0219I: TCP Channel defaultHttpEndpoint has been started and is now listening for requests on host localhost  (IPv4: 127.0.0.1) port 9080.
[AUDIT   ] CWWKF0012I: The server installed the following features: [cdi-2.0, jaxrs-2.1, jaxrsClient-2.1, jndi-1.0, jsonp-1.1, localConnector-1.0, mpConfig-1.3, servlet-4.0].
[INFO    ] CWWKF0008I: Feature update completed in 2.321 seconds.
[AUDIT   ] CWWKF0011I: The defaultServer server is ready to run a smarter planet. The defaultServer server started in 3.175 seconds.
[INFO    ] CWWKZ0018I: Starting application arquillian-managed.
...
[INFO    ] SRVE0169I: Loading Web Module: arquillian-managed.
[INFO    ] SRVE0250I: Web Module arquillian-managed has been bound to default_host.
[AUDIT   ] CWWKT0016I: Web application available (default_host): http://localhost:9080/arquillian-managed/
...
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.133 s - in it.io.openliberty.guides.system.SystemArquillianIT
[INFO] Running it.io.openliberty.guides.inventory.InventoryArquillianIT
[INFO    ] CWWKZ0018I: Starting application arquillian-managed.
[INFO    ] CWWKZ0136I: The arquillian-managed application is using the archive file at the guide-arquillian-managed/finish/target/liberty/wlp/usr/servers/defaultServer/dropins/arquillian-managed.war location.
[INFO    ] SRVE0169I: Loading Web Module: arquillian-managed.
[INFO    ] SRVE0250I: Web Module arquillian-managed has been bound to default_host.
...
[INFO    ] Setting the server's publish address to be /inventory/
[INFO    ] SRVE0242I: [arquillian-managed] [/arquillian-managed] [io.openliberty.guides.inventory.InventoryApplication]: Initialization successful.
[INFO    ] Setting the server's publish address to be /system/
[INFO    ] SRVE0242I: [arquillian-managed] [/arquillian-managed] [io.openliberty.guides.system.SystemApplication]: Initialization successful.
[INFO    ] SRVE0242I: [arquillian-managed] [/arquillian-managed] [ArquillianServletRunner]: Initialization successful.
[AUDIT   ] CWWKT0017I: Web application removed (default_host): http://localhost:9080/arquillian-managed/
[INFO    ] SRVE0253I: [arquillian-managed] [/arquillian-managed] [ArquillianServletRunner]: Destroy successful.
[INFO    ] SRVE0253I: [arquillian-managed] [/arquillian-managed] [io.openliberty.guides.inventory.InventoryApplication]: Destroy successful.
[AUDIT   ] CWWKZ0009I: The application arquillian-managed has stopped successfully.
[INFO    ] SRVE9103I: A configuration file for a web server plugin was automatically generated for this server at guide-arquillian-managed/finish/target/liberty/wlp/usr/servers/defaultServer/logs/state/plugin-cfg.xml.
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.297 s - in it.io.openliberty.guides.inventory.InventoryArquillianIT
...
Stopping server defaultServer.
...
Server defaultServer stopped.
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  12.018 s
[INFO] Finished at: 2020-06-23T12:40:32-04:00
[INFO] ------------------------------------------------------------------------
```

::page{title="Summary"}

### Nice Work!

You just built some functional and integration tests with the Arquillian managed container and ran the tests for your microservices on Open Liberty.


Try one of the related guides to learn more about the technologies that you come across in this guide.


### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-arquillian-managed*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-arquillian-managed
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Testing%20microservices%20with%20the%20Arquillian%20managed%20container&guide-id=cloud-hosted-guide-arquillian-managed)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-arquillian-managed/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-arquillian-managed/pulls)



### Where to next?

* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
