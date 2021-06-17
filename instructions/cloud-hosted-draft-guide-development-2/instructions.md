
# Welcome to the Configuring microservices running in Kubernetes guide!

Explore how to externalize configuration using MicroProfile Config and configure your microservices using Kubernetes ConfigMaps and Secrets.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.





# What you'll learn
You will learn how and why to externalize your microservice's configuration.
Externalized configuration is useful because configuration usually changes depending on your environment.
You will also learn how to configure the environment by providing required values to your application using Kubernetes.
Using environment variables allows for easier deployment to different environments.

MicroProfile Config provides useful annotations that you can use to inject configured values into your code.
These values can come from any configuration source, such as environment variables.
To learn more about MicroProfile Config,
read the [Configuring microservices](https://openliberty.io/guides/microprofile-config.html) guide.

Furthermore, you'll learn how to set these environment variables with ConfigMaps and Secrets.
These resources are provided by Kubernetes and act as a data source for your environment variables.
You can use a ConfigMap or Secret to set environment variables for any number of containers.


# Getting started

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-kubernetes-microprofile-config.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-kubernetes-microprofile-config.git
cd guide-kubernetes-microprofile-config
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.




# Logging into your cluster

For this guide, you will use a container registry on IBM Cloud to deploy to Kubernetes.
Get the name of your namespace with the following command:

```
bx cr namespace-list
```
{: codeblock}

Look for output that is similar to the following:

```
Listing namespaces for account 'QuickLabs - IBM Skills Network' in registry 'us.icr.io'...

Namespace
sn-labs-yourname
```

Store the namespace name in a variable.
Use the namespace name that was obtained from the previous command.

```
NAMESPACE_NAME={namespace_name}
```
{: codeblock}

Verify that the variable contains your namespace name:

```
echo $NAMESPACE_NAME
```
{: codeblock}

Log in to the registry with the following command:
```
bx cr login
```
{: codeblock}


# Deploying the microservices

The two microservices you will deploy are called **system** and **inventory**. The **system** microservice
returns the JVM system properties of the running container. The **inventory** microservice
adds the properties from the **system** microservice to the inventory. This demonstrates
how communication can be established between pods inside a cluster.
To build these applications, navigate to the **start** directory and run the following command.

```
cd start
mvn clean package
```
{: codeblock}


Run the following command to download or update to the latest Open Liberty Docker image:

```
docker pull openliberty/open-liberty:full-java11-openj9-ubi
```
{: codeblock}


Next, run the **docker build** commands to build container images for your application:
```
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
```
{: codeblock}


The **-t** flag in the **docker build** command allows the Docker image to be labeled (tagged) in the **name[:tag]** format. 
The tag for an image describes the specific image version.
If the optional **[:tag]** tag is not specified, the **latest** tag is created by default.

Run the following command to deploy the necessary Kubernetes resources to serve the applications.
```
kubectl apply -f kubernetes.yaml
```
{: codeblock}


When this command finishes, wait for the pods to be in the Ready state.
Run the following command to view the status of the pods.
```
kubectl get pods
```
{: codeblock}


When the pods are ready, the output shows **1/1** for READY and **Running** for STATUS.

```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-6bd97d9bf6-6d2cj     1/1       Running   0          34s
inventory-deployment-645767664f-7gnxf  1/1       Running   0          34s
```

After the pods are ready, you will make requests to your services.


Run the following `curl` command to access the system microservice.
The `-u` option is used to pass in the username `bob` and the password `bobpwd`.

```
curl http://$(minikube ip):31000/system/properties -u bob:bobpwd
```
{: codeblock}

Run the following `curl` command to add the system to the inventory microservice.

```
curl http://$(minikube ip):32000/inventory/systems/system-service
```
{: codeblock}

# Modifying system microservice


The **system** service is hardcoded to use a single forward slash as the context root.
The context root is set in the **webApplication**
element where the **contextRoot** attribute is specified as **"/"**.
You'll make the value of the **contextRoot** attribute configurable by
implementing it as a variable.

Replace the **server.xml** file.

> From the menu of the IDE, select 
 **File** > **Open** > guide-kubernetes-microprofile-config/start/system/src/main/liberty/config/server.xml




```
<server description="Sample Liberty server">

  <featureManager>
    <feature>jaxrs-2.1</feature>
    <feature>cdi-2.0</feature>
    <feature>jsonp-1.1</feature>
    <feature>mpConfig-2.0</feature>
    <feature>appSecurity-3.0</feature>
  </featureManager>

  <variable name="default.http.port" defaultValue="9080"/>
  <variable name="default.https.port" defaultValue="9443"/>
  <variable name="system.app.username" defaultValue="bob"/>
  <variable name="system.app.password" defaultValue="bobpwd"/>
  <variable name="context.root" defaultValue="/"/>

  <httpEndpoint host="*" httpPort="${default.http.port}" 
    httpsPort="${default.https.port}" id="defaultHttpEndpoint" />

  <webApplication location="system.war" contextRoot="${context.root}"/>

  <basicRegistry id="basic" realm="BasicRegistry">
    <user name="${system.app.username}" password="${system.app.password}" />
  </basicRegistry>

</server>
```
{: codeblock}


The **contextRoot** attribute in the **webApplication**
element now gets its value from the **context.root** variable.
To find a value for the **context.root** variable,
Open Liberty will look for the following environment variables, in order:

* **context.root**
* **`context_root`**
* **`CONTEXT_ROOT`**


# Modifying inventory microservice

The **inventory** service is hardcoded to use **bob** and **bobpwd** as the credentials to authenticate against the **system** service.
You'll make these credentials configurable. 

Replace the **SystemClient** class.

> From the menu of the IDE, select 
 **File** > **Open** > guide-kubernetes-microprofile-config/start/inventory/src/main/java/io/openliberty/guides/inventory/client/SystemClient.java




```
package io.openliberty.guides.inventory.client;

import java.net.URI;
import java.util.Base64;
import java.util.Properties;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@RequestScoped
public class SystemClient {

  private final String SYSTEM_PROPERTIES = "/system/properties";
  private final String PROTOCOL = "http";

  @Inject
  @ConfigProperty(name = "CONTEXT_ROOT", defaultValue = "")
  String CONTEXT_ROOT;

  @Inject
  @ConfigProperty(name = "default.http.port")
  String DEFAULT_PORT;

  @Inject
  @ConfigProperty(name = "SYSTEM_APP_USERNAME")
  private String username;

  @Inject
  @ConfigProperty(name = "SYSTEM_APP_PASSWORD")
  private String password;

  public Properties getProperties(String hostname) {
    String url = buildUrl(PROTOCOL,
                          hostname,
                          Integer.valueOf(DEFAULT_PORT),
                          CONTEXT_ROOT + SYSTEM_PROPERTIES);
    Builder clientBuilder = buildClientBuilder(url);
    return getPropertiesHelper(clientBuilder);
  }

  /**
   * Builds the URI string to the system service for a particular host.
   * @param protocol
   *          - http or https.
   * @param host
   *          - name of host.
   * @param port
   *          - port number.
   * @param path
   *          - Note that the path needs to start with a slash!!!
   * @return String representation of the URI to the system properties service.
   */
  protected String buildUrl(String protocol, String host, int port, String path) {
    try {
      URI uri = new URI(protocol, null, host, port, path, null, null);
      return uri.toString();
    } catch (Exception e) {
      System.err.println("Exception thrown while building the URL: " + e.getMessage());
      return null;
    }
  }

  protected Builder buildClientBuilder(String urlString) {
    try {
      Client client = ClientBuilder.newClient();
      Builder builder = client.target(urlString).request();
      return builder
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, getAuthHeader());
    } catch (Exception e) {
      System.err.println("Exception thrown while building the client: "
                         + e.getMessage());
      return null;
    }
  }

  protected Properties getPropertiesHelper(Builder builder) {
    try {
      Response response = builder.get();
      if (response.getStatus() == Status.OK.getStatusCode()) {
        return response.readEntity(Properties.class);
      } else {
        System.err.println("Response Status is not OK.");
      }
    } catch (RuntimeException e) {
      System.err.println("Runtime exception: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("Exception thrown while invoking the request: "
                         + e.getMessage());
    }
    return null;
  }

  private String getAuthHeader() {
    String usernamePassword = username + ":" + password;
    String encoded = Base64.getEncoder().encodeToString(usernamePassword.getBytes());
    return "Basic " + encoded;
  }
}
```
{: codeblock}



The changes introduced here use MicroProfile Config and CDI to inject the value of the
environment variables **`SYSTEM_APP_USERNAME`** and
**`SYSTEM_APP_PASSWORD`** into the **SystemClient** class.


# Creating a ConfigMap and Secret

There are several ways to configure an environment variable in a Docker container.
You can set it directly in the **Dockerfile** with the **ENV** command.
You can also set it in your **kubernetes.yaml** file by specifying a
name and a value for the environment variable you want to set for a specific container.
With these options in mind, you're going to use a ConfigMap and Secret to set these values.
These are resources provided by Kubernetes that are used as a way to provide configuration values to your containers.
A benefit is that they can be reused across many different containers,
even if they all require different environment variables to be set with the same value.

Create a ConfigMap to configure the app name with the following **kubectl** command.
```
kubectl create configmap sys-app-root --from-literal contextRoot=/dev
```
{: codeblock}


This command deploys a ConfigMap named **sys-app-root** to your cluster.
It has a key called **contextRoot** with a value of **/dev**.
The **--from-literal** flag allows you to specify individual key-value pairs to store in this ConfigMap.
Other available options, such as **--from-file** and **--from-env-file**,
provide more versatility as to what you want to configure.
Details about these options can be found in the
[Kubernetes CLI documentation](https://kubernetes.io/docs/reference/generated/kubectl/kubectl-commands#-em-configmap-em-).

Create a Secret to configure the new credentials that **inventory** will use to
authenticate against **system** with the following **kubectl** command.
```
kubectl create secret generic sys-app-credentials --from-literal username=alice --from-literal password=wonderland
```
{: codeblock}


This command looks similar to the command to create a ConfigMap, but one difference is the word **generic**.
This word creates a Secret that doesn't store information in any specialized way.
There are different types of secrets, such as secrets to store Docker credentials
and secrets to store public and private key pairs.

A Secret is similar to a ConfigMap.
A key difference is that a Secret is used for confidential information such as credentials.
One of the main differences is that you must explicitly tell **kubectl** to show you the contents of a Secret.
Additionally, when it does show you the information,
it only shows you a Base64 encoded version so that a casual onlooker doesn't accidentally see any sensitive data.
Secrets don't provide any encryption by default,
that is something you'll either need to do yourself or find an alternate option to configure.



# Updating Kubernetes resources

Next, you will update your Kubernetes deployments to set the environment variables in your containers
based on the values configured in the ConfigMap and Secret created previously. 

Replace the kubernetes file.

> From the menu of the IDE, select 
 **File** > **Open** > guide-kubernetes-microprofile-config/start/kubernetes.yaml




```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: system-deployment
  labels:
    app: system
spec:
  selector:
    matchLabels:
      app: system
  template:
    metadata:
      labels:
        app: system
    spec:
      containers:
      # tag::system-container[]
      - name: system-container
        image: system:1.0-SNAPSHOT
        ports:
        - containerPort: 9080
        # Set the environment variables
        # tag::env1[]
        env:
        # end::env1[]
        # tag::contextRoot1[]
        - name: CONTEXT_ROOT
          # tag::valueFrom1[]
          valueFrom:
          # end::valueFrom1[]
            # tag::configRef1[]
            configMapKeyRef:
              # tag::root1[]
              name: sys-app-root
              # end::root1[]
              # tag::contextRootKey1[]
              key: contextRoot
              # end::contextRootKey1[]
            # end::configRef1[]
        # end::contextRoot1[]
        # tag::sysUsername1[]
        - name: SYSTEM_APP_USERNAME
          # tag::valueFrom2[]
          valueFrom:
          # end::valueFrom2[]
            # tag::secretRef1[]
            secretKeyRef:
              # tag::credentials1[]
              name: sys-app-credentials
              # end::credentials1[]
              # tag::username1[]
              key: username
              # end::username1[]
            # end::secretRef1[]
        # end::sysUsername1[]
        # tag::sysPassword1[]
        - name: SYSTEM_APP_PASSWORD
          # tag::valueFrom3[]
          valueFrom:
          # end::valueFrom3[]
            # tag::secretRef2[]
            secretKeyRef:
              # tag::credentials2[]
              name: sys-app-credentials
              # end::credentials2[]
              # tag::password1[]
              key: password
              # end::password1[]
            # end::secretRef2[]
        # end::sysPassword1[]
      # end::system-container[]
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: inventory-deployment
  labels:
    app: inventory
spec:
  selector:
    matchLabels:
      app: inventory
  template:
    metadata:
      labels:
        app: inventory
    spec:
      containers:
      # tag::inventory-container[]
      - name: inventory-container
        image: inventory:1.0-SNAPSHOT
        ports:
        - containerPort: 9080
        # Set the environment variables
        # tag::env2[]
        env:
        # end::env2[]
        # tag::contextRoot2[]
        - name: CONTEXT_ROOT
          # tag::valueFrom4[]
          valueFrom:
          # end::valueFrom4[]
            # tag::configRef2[]
            configMapKeyRef:
              # tag::root2[]
              name: sys-app-root
              # end::root2[]
              # tag::contextRootKey2[]
              key: contextRoot
              # end::contextRootKey2[]
            # end::configRef2[]
        # end::contextRoot2[]
        # tag::sysUsername2[]
        - name: SYSTEM_APP_USERNAME
          # tag::valueFrom5[]
          valueFrom:
          # end::valueFrom5[]
            # tag::secretRef3[]
            secretKeyRef:
              # tag::credentials3[]
              name: sys-app-credentials
              # end::credentials3[]
              # tag::username2[]
              key: username
              # end::username2[]
            # end::secretRef3[]
        # end::sysUsername2[]
        # tag::sysPassword2[]
        - name: SYSTEM_APP_PASSWORD
          # tag::valueFrom6[]
          valueFrom:
          # end::valueFrom6[]
            # tag::secretRef4[]
            secretKeyRef:
              # tag::credentials4[]
              name: sys-app-credentials
              # end::credentials4[]
              # tag::password2[]
              key: password
              # end::password2[]
            # end::secretRef4[]
        # end::sysPassword2[]
      # end::inventory-container[]
---
apiVersion: v1
kind: Service
metadata:
  name: system-service
spec:
  type: NodePort
  selector:
    app: system
  ports:
  - protocol: TCP
    port: 9080
    targetPort: 9080
    nodePort: 31000
---
apiVersion: v1
kind: Service
metadata:
  name: inventory-service
spec:
  type: NodePort
  selector:
    app: inventory
  ports:
  - protocol: TCP
    port: 9080
    targetPort: 9080
    nodePort: 32000
```
{: codeblock}



The **`CONTEXT_ROOT`**,
**`SYSTEM_APP_USERNAME`**, and
**`SYSTEM_APP_PASSWORD`** environment
variables are set in the **env** sections of
**system-container** and
**inventory-container**.

Using the **valueFrom** field,
you can specify the value of an environment variable from various sources. These sources
include a ConfigMap, a Secret, and information about the cluster. In this
example **configMapKeyRef** gets the
value **contextRoot** from the
**sys-app-root** ConfigMap. Similarly,
**secretKeyRef**
gets the values **username** and
**password** from the
**sys-app-credentials** Secret.

# Deploying your changes

Rebuild the application using **mvn clean package**.
```
mvn clean package
```
{: codeblock}


Run the **docker build** commands to rebuild container images for your application:
```
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
```
{: codeblock}


Run the following command to deploy your changes to the Kubernetes cluster.
```
kubectl replace --force -f kubernetes.yaml
```
{: codeblock}



Your application will now be available at the `http://$(minikube ip):31000/dev/system/properties` URL.
You now need to use the new username, `alice`, and the new password `wonderland`.
Access your application with the following command:

```
curl http://$(minikube ip):31000/dev/system/properties -u alice:wonderland
```
{: codeblock}

Notice that the URL you are using to reach the application now has **/dev** as the context root. 


Verify the inventory service is working as intended by using the following command:

```
curl http://$(minikube ip):32000/inventory/systems/system-service
```
{: codeblock}

If it is not working, then check the configuration of the credentials.

# Testing the microservices




Run the integration tests by running the following command:

```
mvn failsafe:integration-test -Dsystem.context.root=/dev -Dcluster.ip=$(minikube ip)
```
{: codeblock}


The tests for **inventory** verify that the service can communicate with **system**
using the configured credentials. If the credentials are misconfigured, then the
**inventory** test fails, so the **inventory** test indirectly verifies that the
credentials are correctly configured.

After the tests succeed, you should see output similar to the following in your console.

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.706 s - in it.io.openliberty.guides.system.SystemEndpointIT

Results:

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.696 s - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results:

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

# Tearing down the environment

Run the following commands to delete all the resources that you created.

```
kubectl delete -f kubernetes.yaml
kubectl delete configmap sys-app-root
kubectl delete secret sys-app-credentials
```
{: codeblock}




Perform the following steps to return your environment to a clean state.

. Point the Docker daemon back to your local machine:
+
```
eval $(minikube docker-env -u)
```
. Stop your Minikube cluster:
+
```
minikube stop
```
{: codeblock}


. Delete your cluster:
+
```
minikube delete
```
{: codeblock}










# Summary

## Nice Work!

You have used MicroProfile Config to externalize the configuration of two microservices,

and then you configured them by creating a ConfigMap and Secret in your Kubernetes cluster.




## Clean up your environment

Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-kubernetes-microprofile-config** project by running the following commands:

```
cd /home/project
rm -fr guide-kubernetes-microprofile-config
```
{: codeblock}

## What did you think of this guide?
We want to hear from you. To provide feedback on your experience with this guide, click the **Support/Feedback** button in the IDE,
select **Give feedback** option, fill in the fields, choose **General** category, and click the **Post Idea** button.

## What could make this guide better?
You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback](https://github.com/OpenLiberty/guide-kubernetes-microprofile-config/issues)
* [Create a pull request to contribute to this guide](https://github.com/OpenLiberty/guide-kubernetes-microprofile-config/pulls)




## Where to next? 

* [Deploying microservices to Kubernetes](https://openliberty.io/guides/kubernetes-intro.html)
* [Configuring microservices](https://openliberty.io/guides/microprofile-config.html)
* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Using Docker containers to develop microservices](https://openliberty.io/guides/docker.html)


## Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.