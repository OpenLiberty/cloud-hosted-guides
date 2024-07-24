---
markdown-version: v1
tool-type: theiadocker
---
::page{title="Welcome to the Externalizing environment-specific microservice configuration for CI/CD guide!"}

Learn how to create environment-specific configurations for microservices by using MicroProfile Config configuration profiles for easy management and portable deployments throughout the CI/CD lifecycle.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

Managing configurations for microservices can be challenging, especially when configurations require adjustments across various stages of the software development and delivery lifecycle. The MicroProfile Config configuration profile feature, also known as the [Config Profile](https://download.eclipse.org/microprofile/microprofile-config-3.0/microprofile-config-spec-3.0.html#configprofile), is a direct solution to this challenge. It simplifies the management of microservice configurations across diverse environments - from development to production and throughout the  continuous integration/continuous delivery (CI/CD) pipeline. By externalizing and tailoring configuration properties to each environment, the CI/CD process becomes more seamless, so you can concentrate on perfecting your application code and capabilities.

You'll learn how to provide environment-specific configurations by using the MicroProfile Config configuration profile feature. You'll work with the MicroProfile Config API to create configuration profiles that use profile-specific configuration properties and configuration sources.

This guide builds on the [Separating configuration from code in microservices](https://openliberty.io/guides/microprofile-config-intro.html) guide and the [Configuring microservices](https://openliberty.io/guides/microprofile-config.html) guide. If you are not familiar with externalizing the configuration of microservices, it will be helpful to read the [External configuration of microservices](https://openliberty.io/docs/latest/external-configuration.html) document and complete the aforementioned guides before you proceed.

The application that you will work with is a ***query*** service, which fetches information about the running JVM from a ***system*** microservice. You'll use configuration profiles to externalize and manage the configurations across the development, testing, and production environments.

![System and query services DevOps](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-config-profile/prod/assets/system-query-devops.png)



::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-config-profile.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-microprofile-config-profile.git
cd guide-microprofile-config-profile
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

::page{title="Creating a configuration profile for the dev environment"}

The dev environment is used to test, experiment, debug, and refine your code, ensuring an application's functional readiness before progressing to subsequent stages in a software development and delivery lifecycle.

Navigate to the ***start*** directory to begin.

The starting Java project, which you can find in the ***start*** directory, is a multi-module Maven project comprised of the ***system*** and ***query*** microservices. Each microservice is in its own corresponding directory, ***system*** and ***query***.



The ***system*** microservice contains the three Maven build profiles: ***dev***, ***test***, and ***prod***, in which the ***dev*** profile is set as the default. Each build profile defines properties for a particular deployment configuration that the microservice uses.

The MicroProfile Config configuration profile feature supplies configurations for different environments while only a single profile is active. The active profile is set using the ***mp.config.profile*** property. You can set it in any of the [configuration sources](https://openliberty.io/docs/latest/external-configuration.html#default) and it is read once during application startup. When a profile is active, its associated configuration properties are used. For the ***query*** service, the ***mp.config.profile*** property is set to ***dev*** in its Maven ***pom.xml***. This Liberty configuration variable indicates to the runtime that ***dev*** is the active configuration profile.

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), the dev mode listens for file changes and automatically recompiles and deploys your updates whenever you save a new change.

Open a command-line session and run the following commands to navigate to the ***system*** directory and start the ***system*** service in ***dev*** environment:

```bash
cd /home/project/guide-microprofile-config-profile/start/system
mvn liberty:dev
```

Open another command-line session and run the following commands to navigate to the ***query*** directory and start the ***query*** service in ***dev*** environment:

```bash
cd /home/project/guide-microprofile-config-profile/start/query
mvn liberty:dev
```

After you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************
*     Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.


In the dev environment, the ***dev*** configuration profile is set in the ***system/pom.xml*** file as the configuration profile to use for running the ***system*** service. The ***system*** service runs on HTTP port ***9081*** and HTTPS port ***9444*** using the context root ***system/dev***. It uses a basic user registry with username ***alice*** and password ***alicepwd*** for resource authorization. Note that the ***basicRegistry*** element is a simple registry configuration for learning purposes. For more information on user registries, see the [User registries documentation](https://openliberty.io/docs/latest/user-registries-application-security.html).

Click the following button to check out the ***query*** service:

::startApplication{port="9085" display="external" name="Check out the query service" route="/query/systems/localhost"}


The ***query*** service returns the message: ***{"fail":"Failed to reach the client localhost."}***. This is because the current ***query*** service uses the default properties in the ***query/src/main/resources/META-INF/microprofile-config.properties*** file to access the ***system*** service.

For proper communication with the development ***system*** service, the ***query*** service uses the properties in the ***dev*** configuration profile.

![System service running in development environment](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-config-profile/prod/assets/system-query-devops-development.png)


There are two ways to define configuration properties associated with your configuration profile. The first is as individual configuration properties associated with a configuration profile that can be specified in any kind of MicroProfile configuration source. The second is through default ***microprofile-config.properties*** configuration files embedded inside your application that can be associated with different configuration profiles. The former allows for flexibility in defining profile-specific configuration properties in the best configuration sources for your needs while the latter enables default profiles of configuration properties to be provided in your application.

### Creating profile-specific configuration properties

This approach involves directly associating individual configuration properties with a configuration profile. To define a configuration property for a particular config profile, use the ***%\<config_profile_id\>.\<property_name\>=\<value\>*** syntax, where ***\<config_profile_id\>*** is the unique identifier for the configuration profile and ***\<property_name\>*** is the name of the property you want to set.

Replace the ***microprofile-config.properties*** file.

> To open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > guide-microprofile-config-profile/start/query/src/main/resources/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/guide-microprofile-config-profile/start/query/src/main/resources/META-INF/microprofile-config.properties"}



```
system.httpsPort=9443
system.user=admin
system.password=adminpwd
system.contextRoot=system

%dev.system.httpsPort=9444
%dev.system.user=alice
%dev.system.password=alicepwd
%dev.system.contextRoot=system/dev
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.



Configure the ***%dev.**** properties in the ***microprofile-config.properties*** file based on the values from the ***dev*** profile of the ***system*** service.

Because the active profile is set to ***dev***, each ***%dev.**** property overrides the value of the plain non-profile-specific property. For example, in this case the ***%dev.system.httpsPort*** property overrides the ***system.httpsPort*** property and the value is resolved to ***9444***.

Because you are running the ***query*** service in dev mode, the changes that you made are automatically picked up. 

Click the following button to try out the application:

::startApplication{port="9085" display="external" name="Try out the application" route="/query/systems/localhost"}

You can see the current OS and Java version in JSON format.


### Creating profile-specific ***microprofile-config.properties*** configuration files

Creating profile-specific ***microprofile-config.properties*** configuration files is a structured way to provide and manage more extensive sets of default configurations. You can create a configuration file for each configuration profile in the ***META-INF*** folder on the classpath of your application by using the ***microprofile-config-\<config_profile_id\>*** naming convention, where ***\<config_profile_id\>*** is the unique identifier for a configuration profile. Once you create the file, you can add your configuration properties to it with the standard ***\<property_name\>=\<value\>*** syntax.

Open another command-line session.

Create the ***microprofile-config-dev.properties*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-config-profile/start/query/src/main/resources/META-INF/microprofile-config-dev.properties
```


> Then, to open the microprofile-config-dev.properties file in your IDE, select
> **File** > **Open** > guide-microprofile-config-profile/start/query/src/main/resources/META-INF/microprofile-config-dev.properties, or click the following button

::openFile{path="/home/project/guide-microprofile-config-profile/start/query/src/main/resources/META-INF/microprofile-config-dev.properties"}



```
system.httpsPort=9444
system.user=alice
system.password=alicepwd
system.contextRoot=system/dev
```




Define the ***system.**** properties in the ***microprofile-config-dev.properties*** file based on the values from the ***dev*** profile of the ***system*** service.

Replace the ***microprofile-config.properties*** file.

> To open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > guide-microprofile-config-profile/start/query/src/main/resources/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/guide-microprofile-config-profile/start/query/src/main/resources/META-INF/microprofile-config.properties"}



```
system.httpsPort=9443
system.user=admin
system.password=adminpwd
system.contextRoot=system

```




Remove the ***%dev.**** properties from the ***microprofile-config.properties*** file.

Because the active profile is set to ***dev***, any ***system.**** properties specified in the ***microprofile-config-dev.properties*** file take precedence over the ***system.**** property values in the ***microprofile-config.properties*** file.

Now, click the following button to try out the application again:

::startApplication{port="9085" display="external" name="Try out the application" route="/query/systems/localhost"}

You can see the current OS and Java version in JSON format.

When you are done checking out the application in ***dev*** environment, exit dev mode by pressing `Ctrl+C` in the command-line sessions where you ran the ***system*** and ***query*** services. 

::page{title="Creating a configuration profile for the test environment"}

In CI/CD, the test environment is where integration tests ensure the readiness and quality of an application. A good testing configuration not only ensures smooth operations but also aligns the environment closely with potential production settings.

![System service running in testing environment](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-config-profile/prod/assets/system-query-devops-testing.png)


Create the ***microprofile-config-test.properties*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-config-profile/start/query/src/main/resources/META-INF/microprofile-config-test.properties
```


> Then, to open the microprofile-config-test.properties file in your IDE, select
> **File** > **Open** > guide-microprofile-config-profile/start/query/src/main/resources/META-INF/microprofile-config-test.properties, or click the following button

::openFile{path="/home/project/guide-microprofile-config-profile/start/query/src/main/resources/META-INF/microprofile-config-test.properties"}



```
system.httpsPort=9445
system.user=bob
system.password=bobpwd
system.contextRoot=system/test
```




Define the ***system.**** properties in the ***microprofile-config-test.properties*** file based on the values from the ***test*** profile of the ***system*** service.

Create the ***QueryEndpointIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-microprofile-config-profile/start/query/src/test/java/it/io/openliberty/guides/query/QueryEndpointIT.java
```


> Then, to open the QueryEndpointIT.java file in your IDE, select
> **File** > **Open** > guide-microprofile-config-profile/start/query/src/test/java/it/io/openliberty/guides/query/QueryEndpointIT.java, or click the following button

::openFile{path="/home/project/guide-microprofile-config-profile/start/query/src/test/java/it/io/openliberty/guides/query/QueryEndpointIT.java"}



```java
package it.io.openliberty.guides.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

public class QueryEndpointIT {

    private static String port = System.getProperty("http.port");
    private static String baseUrl = "http://localhost:" + port + "/query";
    private static String systemHost = System.getProperty("system.host");

    private static Client client;

    @BeforeEach
    public void setup() {
        client = ClientBuilder.newClient();
    }

    @AfterEach
    public void teardown() {
        client.close();
    }

    @Test
    public void testQuerySystem() {

        Response response = this.getResponse(baseUrl + "/systems/" + systemHost);
        this.assertResponse(baseUrl, response);

        JsonObject jsonObj = response.readEntity(JsonObject.class);
        assertNotNull(jsonObj.getString("os.name"), "os.name is null");
        assertNotNull(jsonObj.getString("java.version"), "java.version is null");

        response.close();
    }

    @Test
    public void testUnknownHost() {
        Response response = this.getResponse(baseUrl + "/systems/unknown");
        this.assertResponse(baseUrl, response);

        JsonObject json = response.readEntity(JsonObject.class);
        assertEquals("Failed to reach the client unknown.", json.getString("fail"),
            "Fail message is wrong.");
        response.close();
    }

    private Response getResponse(String url) {
        return client.target(url).request().get();
    }

    private void assertResponse(String url, Response response) {
        assertEquals(200, response.getStatus(), "Incorrect response code from " + url);
    }

}
```



Implement endpoint tests to test the basic functionality of the ***query*** microservice. If a test failure occurs, you might have introduced a bug into the code.

See the following descriptions of the test cases:

* ***testQuerySystem()*** verifies the ***/query/systems/{hostname}*** endpoint.

* ***testUnknownHost()*** verifies that an unknown host or a host that does not expose their JVM system properties is correctly handled with a fail message.

### Running the tests in the test environment

Now, navigate to the ***start*** directory.



Test the application under the ***test*** environment by running the following script that contains different Maven goals to ***build***, ***start***, ***test***, and ***stop*** the services.

```bash
cd /home/project/guide-microprofile-config-profile/start
./scripts/testApp.sh
```

If the tests pass, you see output similar to the following example:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.539 s - in it.io.openliberty.guides.system.SystemEndpointIT

Results:

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

...

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.query.QueryEndpointIT
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.706 s - in it.io.openliberty.guides.query.QueryEndpointIT

Results:

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

```

::page{title="Next steps"}

Deploying the application to a Kubernetes environment using the Open Liberty Operator is an optional learning step in this guide.

To further explore deploying microservices using Kubernetes and the Open Liberty Operator, you can read the following guides:

 [Deploying a microservice to Kubernetes using Open Liberty Operator](https://openliberty.io/guides/openliberty-operator-intro.html)
 [Deploying a microservice to OpenShift 4 using Open Liberty Operator](https://openliberty.io/guides/openliberty-operator-openshift.html)

A secure production environment is essential to application security. In the previous sections, you learned how to use the MicroProfile Config API to externalize credentials and other properties for accessing the ***system*** service. This strategy makes the application more adaptable to different environments without the need to change code and rebuild your application. 

In the this section, you'll learn how to use Kubernetes secrets to provide the credentials and how to pass them to the ***query*** service by using MicroProfile Config.

### Deploying the application in the prod environment with Kubernetes






Before deploying, create the Dockerfile files for both ***system*** and ***query*** microservices. Then, build their ***.war*** files and Docker images in the ***start*** directory.

```bash
cp /home/project/guide-microprofile-config-profile/finish/system/Dockerfile /home/project/guide-microprofile-config-profile/start/system
cp /home/project/guide-microprofile-config-profile/finish/query/Dockerfile /home/project/guide-microprofile-config-profile/start/query
cd /home/project/guide-microprofile-config-profile/start
mvn -P prod clean package
docker build -t system:1.0-SNAPSHOT system/.
docker build -t query:1.0-SNAPSHOT query/.
```

The Maven ***clean*** and ***package*** goals can clean the ***target*** directories and build the ***.war*** application files from scratch. The ***microprofile-config-dev.properties*** and ***microprofile-config-test.properties*** of the ***query*** microservice are excluded from the ***prod*** build. The default ***microprofile-config.properties*** file is automatically applied.

The Docker ***build*** commands package the ***.war*** files of the ***system*** and ***query*** microservices with their default configuration into your Docker images.

After building the images, push your images to the container registry on IBM Cloud with the following commands:

```bash
docker tag system:1.0-SNAPSHOT us.icr.io/$SN_ICR_NAMESPACE/system:1.0-SNAPSHOT
docker tag query:1.0-SNAPSHOT us.icr.io/$SN_ICR_NAMESPACE/query:1.0-SNAPSHOT
docker push us.icr.io/$SN_ICR_NAMESPACE/system:1.0-SNAPSHOT
docker push us.icr.io/$SN_ICR_NAMESPACE/query:1.0-SNAPSHOT
```

And, you can create a Kubernetes secret for storing sensitive data such as credentials.

```bash
kubectl create secret generic sys-app-credentials \
        --from-literal username=$USERNAME \
        --from-literal password=password
```

For more information about managing secrets, see the [Managing Secrets using kubectl](https://kubernetes.io/docs/tasks/configmap-secret/managing-secret-using-kubectl) documentation.

Finally, write up the ***deploy.yaml*** deployment file to configure the deployment of the ***system*** and ***query*** microservices by using the Open Liberty Operator. The ***sys-app-credentials*** Kubernetes secrets set the environment variables ***DEFAULT_USERNAME*** and ***DEFAULT_PASSWORD*** for the ***system*** microservice, and ***SYSTEM_USER*** and ***SYSTEM_PASSWORD*** for the ***query*** microservice.

```bash
cp /home/project/guide-microprofile-config-profile/finish/deploy.yaml /home/project/guide-microprofile-config-profile/start
sed -i 's=system:1.0-SNAPSHOT=us.icr.io/'"${SN_ICR_NAMESPACE}"'/system:1.0-SNAPSHOT\n  pullPolicy: Always\n  pullSecret: icr=g' deploy.yaml
sed -i 's=query:1.0-SNAPSHOT=us.icr.io/'"${SN_ICR_NAMESPACE}"'/query:1.0-SNAPSHOT\n  pullPolicy: Always\n  pullSecret: icr=g' deploy.yaml
```

If you want to override another property, you can specify it in the ***env*** sections of the ***deploy.yaml*** file. For example, set the ***CONTEXT_ROOT*** environment variable in the ***system*** deployment and the ***SYSTEM_CONTEXTROOT*** environment variable in the ***query*** deployment.

Once the images and the secret are ready, you can deploy the microservices to your production environment with Kubernetes.

```bash
kubectl apply -f deploy.yaml
```
When the apps are deployed, run the following command to check the status of your pods:
```bash
kubectl get pods
```

You'll see an output similar to the following if all the pods are healthy and running:

```
----
NAME                     READY   STATUS    RESTARTS   AGE
query-7b7b6db4b6-cqtqx   1/1     Running   0          4s
system-bc85bc8dc-rw5pb   1/1     Running   0          5s
----
```

To access the exposed **query** microservice, the service must be port-forwarded. Run the following command to set up port forwarding to access the **query** service:

```bash
kubectl port-forward svc/query 9448
```

Open another command-line session and access the microservice by running the following command:
```bash
curl -k -s "https://localhost:9448/query/systems/system.${SN_ICR_NAMESPACE}.svc" | jq
```

You'll see an output similar to the following:

```
{
  "hostname": "system.sn-labs-gkwan.svc",
  "java.version": "11.0.23",
  "os.name": "Linux"
}
```

When you're done trying out the microservice, press **CTRL+C** in the command line session where you ran the `kubectl port-forward` command to stop the port forwarding, and then delete all resources by running the following commands:
```bash
cd /home/project/guide-microprofile-config-profile/start
kubectl delete -f deploy.yaml
kubectl delete secret sys-app-credentials
docker image prune -a -f
```

::page{title="Summary"}

### Nice Work!

You just learned how to use the MicroProfile Config's configuration profile feature to configure your application for multiple CI/CD environments.


Feel free to try one of the related guides. They demonstrate new technologies that you can learn to expand on what you built in this guide.


### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-microprofile-config-profile*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-microprofile-config-profile
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Externalizing%20environment-specific%20microservice%20configuration%20for%20CI/CD&guide-id=cloud-hosted-guide-microprofile-config-profile)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-microprofile-config-profile/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-microprofile-config-profile/pulls)



### Where to next?

* [Separating configuration from code in microservices](https://openliberty.io/guides/microprofile-config-intro.html)
* [Configuring microservices](https://openliberty.io/guides/microprofile-config.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
