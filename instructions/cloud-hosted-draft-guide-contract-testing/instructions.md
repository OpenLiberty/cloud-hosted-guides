
# Testing microservices with Consumer Driven Contracts


Learn how to test Java microservices with consumer driven contracts in Open Liberty.

## What you'll learn

With microservices-based architecture, there is a need for robust testing to ensure that microservices that depend on one another are able to communicate effectively.
Typically, to prevent multiple points of failures at different integration points, a combination of unit, integration, and end-to-end tests are used.
While unit tests are fast, they are less trustworthy as they run in isolation and usually rely on mock data.

Integration tests address this issue by testing against real running services. However, they tend to be slow as the tests depend on other microservices and are less reliable as they are prone to external changes.

Usually, end-to-end tests tend to be more trustworthy as they verify functionality from the perspective of a user. A GUI component
is required to perform end-to-end tests that rely on third-party software such as Selenium that requires heavy compute time and resources.

*Contract testing*

Contract testing bridges the gap among the shortcomings of those testing methodologies. Contract testing is a technique for testing an integration point by isolating each microservice and checking if the
HTTP requests and responses it transmits conform to a shared understanding that is documented in a contract.
It ensures that microservices can communicate with each other.

[Pact](https://docs.pact.io/) is an open source contract testing tool for testing HTTP requests, responses and, message integrations by using contract tests.

The [pact broker](https://docs.pact.io/pact_broker/docker_images) is an application for sharing pact contracts and verification results, and is an important piece for integrating pact into CI/CD pipelines.

The two microservices you will interact with are called **system** and **inventory**. The **system** microservice returns the JVM
system properties of its host. The **inventory** microservice retrieves specific properties from the **system** microservice. You will
learn how to use the [Pact](https://docs.pact.io/) framework to write contract tests for the **inventory** microservice to be verified by
the **system** microservice.

```
docker-compose -f "pact-broker/docker-compose.yml" up -d --build
```

When the pact broker application is running, you will see:
```
Creating pact-broker_postgres_1 ... done
Creating pact-broker_pact-broker_1 ... done
```

Go to [http://localhost:9292/](http://localhost:9292/) to confirm that you can access the pact broker's UI

```
curl http://localhost:9292/
```
{: codeblock}




You can refer to the official [Pact Broker](https://docs.pact.io/pact_broker/docker_images/pactfoundation)
documentation for more information about the components of the docker compose file.

# Implementing pact test in the inventory service

You can find the starting Java projects in the **start** directory. It is made up of the **system** and **inventory** microservices.
Each microservice lives in its own corresponding directory; **system** and **inventory**.

Navigate to the **start/inventory** directory to begin.
When you run Open Liberty in development mode, known as dev mode, the server listens for file changes and automatically recompiles and 
deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```
mvn liberty:dev
```
{: codeblock}


After you see the following message, your application server in dev mode is ready:

```
************************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, 
or open the project in your editor.



Create the InventoryPactIT.java file.


> [File -> New File]  
> draft-guide-contract-testing/start/inventory/src/test/java/io/openliberty/guides/inventory/InventoryPactIT.java



The **InventoryPactIT** class contains a **PactProviderRule** mock provider that mimics the HTTP responses from the **system** microservice.
The **@Pact** annotation takes the name of the microservice as a parameter that makes it easier to differentiate microservices from one another when there are multiple applications.

The **createPactServer()** method defines the minimal expected response for a specific endpoint and is also known as an interaction.
For each interaction, the expected request and the response are registered with the mock service by using the **@PactVerification** annotation.

The test sends a real request by the **getUrl()** method of the mock provider. The mock provider compares the actual request with the expected request and confirms if the comparison is successful.
Finally, the **assertEquals()** confirms that the response is correct.

Replace the inventory pom file.


> [File -> Open...]  
> draft-guide-contract-testing/start/inventory/pom.xml



The pact framework provides a **maven** extension that can be added to the build section of the **pom.xml**. The endpoint URL for the **system** microservice and the **pactFileDirectory** at which the pact file is to be stored is defined in the **serviceProvider** element.
The **pact-jvm-consumer-junit** dependency provides the base test class for use with JUnit to build the unit tests.

After you create **InventoryPactIT.java** and replace the **pom.xml** file, Open Liberty automatically reloads its configuration.

A contract between **inventory** and **system** microservice is known as a pact. Each pact is a collection of interactions that are defined in the **InventoryPactIT** class.

Press the **enter/return** key to run the tests and generate the pact file.

When completed, you will see a similar output to the following example:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running io.openliberty.guides.inventory.InventoryPactIT
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.631 s - in io.openliberty.guides.inventory.InventoryPactIT
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

When integrating the pact framework in a CI/CD build pipeline, the **mvn failsafe:integration-test** goal can be used instead to generate the pact file.

The generated pact file is named **Inventory-System.json** and is located in **inventory/target/pacts**. It contains the defined interactions in **.json** format:

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

Open a new command-line session and navigate to the **start/inventory** directory. Publish the generated pact file to the pact broker by running the following command:
```
mvn pact:publish
```
{: codeblock}


When completed, you will see a similar output to the following example:
```
{: codeblock}


Publishing 'Inventory-System.json' with tags 'open-liberty-pact' ... OK
```

# Verifying the pact in pact broker

Refresh the pact broker webpage at [http://localhost:9292/](http://localhost:9292/) to verify that there is a new entry Note there is no

```
curl http://localhost:9292/
```
{: codeblock}


timestamp in the last verified column as the pact is yet to be verified by the **system** microservice.


You can see detailed insights about each interaction by navigating to the [http://localhost:9292/pacts/provider/System/consumer/Inventory/latest](http://localhost:9292/pacts/provider/System/consumer/Inventory/latest) URL

```
curl http://localhost:9292/pacts/provider/System/consumer/Inventory/latest
```
{: codeblock}



A snippet of the webpage looks similar to:


# Implementing pact test in the system service




Navigate to the **start/system** directory.

Open another command-line session to start Open Liberty in dev mode for the **system** microservice:
```
mvn liberty:dev
```
{: codeblock}


After you see the following message, your application server in dev mode is ready:

```
************************************************************************
*    Liberty is running in dev mode.
```

Create the SystemBrokerIT.java file.


> [File -> New File]  
> draft-guide-contract-testing/start/system/src/test/java/it/io/openliberty/guides/system/SystemBrokerIT.java



The connection information for the pact broker is provided with the **@PactBroker** annotation. The dependency also provides a JUnit5 Invocation Context Provider by the **pactVerificationTestTemplate** method to generate a test for each of the interactions.

The **pact.verifier.publishResults** property is set to true so that the results are sent to the pact broker after the tests are completed.

The test target is defined in the **PactVerificationContext** context to point to the running endpoint of the **system** microservice.

The **@State** annotation must match the **given()** parameter that was provided in the **inventory** test class so that pact can identify which test case to run against which endpoint.

Replace the system pom file.


> [File -> Open...]  
> draft-guide-contract-testing/start/system/pom.xml



The **Junit5** pact provider dependency is used for the **system** microservice to connect to the pact broker to verify the pact file.
Ideally, in a CI/CD pipeline, the **pact.provider.version** element is dynamically set to the build number so that it is easier to identify at which point a breaking change is introduced.

After you create the **SystemBrokerIT.java** and replace the **pom.xml** file, Open Liberty automatically reloads its configuration.

# Verifying the contract

In the command-line session where the **system** microservice was started, press the **enter/return** key to run the tests to verify the pact file. When integrating the pact framework in a CI/CD build pipeline, the **mvn failsafe:integration-test** goal can be used instead to verify the pact file from the pact broker.

If successful, you will see a similar output to the following example:
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
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.835 s - in it.io.openliberty.guides.system.SystemBrokerIT
...
```
When completed, refresh the pact broker webpage at [http://localhost:9292/](http://localhost:9292/) to confirm that there is now a timestamp in the last verified column

```
curl http://localhost:9292/
```
{: codeblock}




The pact file that is created by the **inventory** microservice was successfully verified by the **system** microservice through the pact broker application.
This ensures that responses from the **system** microservice meet the expectations of the **inventory** microservice.

# Tearing down the environment

When you are done checking out the service, exit dev mode by pressing **CTRL+C** in the command-line sessions
where you ran the servers for **system** and **inventory**, or by typing **q** and then pressing the **enter/return** key.

Navigate back to the **/guide-contract-testing** directory and run the following commands to remove the pact broker application.
```
docker-compose -f "pact-broker/docker-compose.yml" down
docker rmi postgres:12
docker rmi pactfoundation/pact-broker:2.62.0.0
docker volume rm pact-broker_postgres-volume
```
{: codeblock}


# Summary

## Nice Work!

You implemented contract testing by using pact in microservices and verified the contract by using the pact broker.


# Related Links

Learn more about the pact framework.

[View the Pact website](https://pact.io/)



## Clean up your environment

Clean up your online environment so that it is ready to be used with the next guide!

You can clean up the environment by doing the following:

Delete the **draft-guide-contract-testing** project by navigating to the **/home/project/** directory

```
cd /home/project
rm -fr draft-guide-contract-testing
```
{: codeblock}

Now Log out by navigating to: 

> [Account -> Logout]

