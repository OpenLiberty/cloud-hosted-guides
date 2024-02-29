---
markdown-version: v1
title: instructions
branch: lab-485-instruction
version-history-start-date: 2021-08-23 16:28:42 UTC
tool-type: theia
---
::page{title="Welcome to the Configuring microservices running in Kubernetes guide!"}

Explore how to externalize configuration using MicroProfile Config and configure your microservices using Kubernetes ConfigMaps and Secrets.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.





::page{title="What you'll learn"}
You will learn how and why to externalize your microservice's configuration. Externalized configuration is useful because configuration usually changes depending on your environment. You will also learn how to configure the environment by providing required values to your application using Kubernetes.

MicroProfile Config provides useful annotations that you can use to inject configured values into your code. These values can come from any configuration source, such as environment variables. Using environment variables allows for easier deployment to different environments. To learn more about MicroProfile Config, read the [Configuring microservices](https://openliberty.io/guides/microprofile-config.html) guide.

Furthermore, you'll learn how to set these environment variables with ConfigMaps and Secrets. These resources are provided by Kubernetes and act as a data source for your environment variables. You can use a ConfigMap or Secret to set environment variables for any number of containers.


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-kubernetes-microprofile-config.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-kubernetes-microprofile-config.git
cd guide-kubernetes-microprofile-config
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.


::page{title="Deploying the microservices"}

The two microservices you will deploy are called ***system*** and ***inventory***. The ***system*** microservice returns the JVM system properties of the running container. The ***inventory*** microservice adds the properties from the ***system*** microservice to the inventory. This demonstrates how communication can be established between pods inside a cluster. To build these applications, navigate to the ***start*** directory and run the following command.

```bash
cd start
mvn clean package
```



Next, run the ***docker build*** commands to build container images for your application:
```bash
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
```

The ***-t*** flag in the ***docker build*** command allows the Docker image to be labeled (tagged) in the ***name[:tag]*** format. The tag for an image describes the specific image version. If the optional ***[:tag]*** tag is not specified, the ***latest*** tag is created by default.

Push your images to the container registry on IBM Cloud with the following commands:

```bash
docker tag inventory:1.0-SNAPSHOT us.icr.io/$SN_ICR_NAMESPACE/inventory:1.0-SNAPSHOT
docker tag system:1.0-SNAPSHOT us.icr.io/$SN_ICR_NAMESPACE/system:1.0-SNAPSHOT
docker push us.icr.io/$SN_ICR_NAMESPACE/inventory:1.0-SNAPSHOT
docker push us.icr.io/$SN_ICR_NAMESPACE/system:1.0-SNAPSHOT
```

Update the image names and set the image pull policy to **Always** so that the images in your IBM Cloud container registry are used, and remove the **nodePort** fields so that the ports can be automatically generated:

```bash
sed -i 's=system:1.0-SNAPSHOT=us.icr.io/'"$SN_ICR_NAMESPACE"'/system:1.0-SNAPSHOT\n        imagePullPolicy: Always=g' kubernetes.yaml
sed -i 's=inventory:1.0-SNAPSHOT=us.icr.io/'"$SN_ICR_NAMESPACE"'/inventory:1.0-SNAPSHOT\n        imagePullPolicy: Always=g' kubernetes.yaml
sed -i 's=nodePort: 31000==g' kubernetes.yaml
sed -i 's=nodePort: 32000==g' kubernetes.yaml
```

Run the following command to deploy the necessary Kubernetes resources to serve the applications.
```bash
kubectl apply -f kubernetes.yaml
```

When this command finishes, wait for the pods to be in the Ready state. Run the following command to view the status of the pods.
```bash
kubectl get pods
```

When the pods are ready, the output shows ***1/1*** for READY and ***Running*** for STATUS.

```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-6bd97d9bf6-6d2cj     1/1       Running   0          34s
inventory-deployment-645767664f-7gnxf  1/1       Running   0          34s
```

After the pods are ready, you will make requests to your services.


In this IBM cloud environment, you need to set up port forwarding to access the services. Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE. Run the following commands to set up port forwarding to access the **system** service.
```bash
SYSTEM_NODEPORT=`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services system-service`
kubectl port-forward svc/system-service $SYSTEM_NODEPORT:9090
```

Then, open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE. Run the following commands to set up port forwarding to access the **inventory** service.
```bash
INVENTORY_NODEPORT=`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services inventory-service`
kubectl port-forward svc/inventory-service $INVENTORY_NODEPORT:9090
```

Then use the following commands to access your **system** microservice. The ***-u*** option is used to pass in the username ***bob*** and the password ***bobpwd***.
```bash
SYSTEM_NODEPORT=`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services system-service`
curl -s http://localhost:$SYSTEM_NODEPORT/system/properties -u bob:bobpwd | jq
```

Use the following commands to access your ***inventory*** microservice.
```bash
INVENTORY_NODEPORT=`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services inventory-service`
curl -s http://localhost:$INVENTORY_NODEPORT/inventory/systems/system-service | jq
```

When you're done trying out the microservices, press **CTRL+C** in the command line sessions where you ran the ***kubectl port-forward*** commands to stop the port forwarding.

::page{title="Modifying system microservice"}

The ***system*** service is hardcoded to use a single forward slash as the context root. The context root is set in the ***webApplication***
element, where the ***contextRoot*** attribute is specified as ***"/"***. You'll make the value of the ***contextRoot*** attribute configurable by implementing it as a variable.

Replace the ***server.xml*** file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-kubernetes-microprofile-config/start/system/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-kubernetes-microprofile-config/start/system/src/main/liberty/config/server.xml"}



```xml
<server description="Sample Liberty server">

  <featureManager>
    <feature>restfulWS-3.1</feature>
    <feature>jsonb-3.0</feature>
    <feature>cdi-4.0</feature>
    <feature>jsonp-2.1</feature>
    <feature>mpConfig-3.1</feature>
    <feature>mpHealth-4.0</feature>
    <feature>appSecurity-5.0</feature>
  </featureManager>

  <variable name="http.port" defaultValue="9090"/>
  <variable name="https.port" defaultValue="9453"/>
  <variable name="system.app.username" defaultValue="bob"/>
  <variable name="system.app.password" defaultValue="bobpwd"/>
  <variable name="context.root" defaultValue="/"/>

  <httpEndpoint host="*" httpPort="${http.port}" 
    httpsPort="${https.port}" id="defaultHttpEndpoint" />

  <webApplication location="guide-kubernetes-microprofile-config-system.war" contextRoot="${context.root}"/>

  <basicRegistry id="basic" realm="BasicRegistry">
    <user name="${system.app.username}" password="${system.app.password}" />
  </basicRegistry>

</server>
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


The ***contextRoot*** attribute in the ***webApplication*** element now gets its value from the ***context.root*** variable. To find a value for the ***context.root*** variable, Open Liberty looks for the following environment variables, in order:


* `context.root`
* `context_root`
* `CONTEXT_ROOT`

::page{title="Modifying inventory microservice"}

The ***inventory*** service is hardcoded to use ***bob*** and ***bobpwd*** as the credentials to authenticate against the ***system*** service. You'll make these credentials configurable. 

Replace the ***SystemClient*** class.

> To open the SystemClient.java file in your IDE, select
> **File** > **Open** > guide-kubernetes-microprofile-config/start/inventory/src/main/java/io/openliberty/guides/inventory/client/SystemClient.java, or click the following button

::openFile{path="/home/project/guide-kubernetes-microprofile-config/start/inventory/src/main/java/io/openliberty/guides/inventory/client/SystemClient.java"}



```java
package io.openliberty.guides.inventory.client;

import java.net.URI;
import java.util.Base64;
import java.util.Properties;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@RequestScoped
public class SystemClient {

  private final String SYSTEM_PROPERTIES = "/system/properties";
  private final String PROTOCOL = "http";

  @Inject
  @ConfigProperty(name = "CONTEXT_ROOT", defaultValue = "")
  String CONTEXT_ROOT;

  @Inject
  @ConfigProperty(name = "http.port")
  String HTTP_PORT;

  @Inject
  @ConfigProperty(name = "SYSTEM_APP_USERNAME")
  private String username;

  @Inject
  @ConfigProperty(name = "SYSTEM_APP_PASSWORD")
  private String password;

  public Properties getProperties(String hostname) {
    Properties properties = null;
    Client client = ClientBuilder.newClient();
    try {
        Builder builder = getBuilder(hostname, client);
        properties = getPropertiesHelper(builder);
    } catch (Exception e) {
        System.err.println(
        "Exception thrown while getting properties: " + e.getMessage());
    } finally {
        client.close();
    }
    return properties;
  }

  private Builder getBuilder(String hostname, Client client) throws Exception {
    URI uri = new URI(
                  PROTOCOL, null, hostname, Integer.valueOf(HTTP_PORT),
                  CONTEXT_ROOT + SYSTEM_PROPERTIES, null, null);
    String urlString = uri.toString();
    Builder builder = client.target(urlString).request();
    builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
           .header(HttpHeaders.AUTHORIZATION, getAuthHeader());
    return builder;
  }

  private Properties getPropertiesHelper(Builder builder) throws Exception {
    Response response = builder.get();
    if (response.getStatus() == Status.OK.getStatusCode()) {
        return response.readEntity(Properties.class);
    } else {
        System.err.println("Response Status is not OK.");
        return null;
    }
  }

  private String getAuthHeader() {
    String usernamePassword = username + ":" + password;
    String encoded = Base64.getEncoder().encodeToString(usernamePassword.getBytes());
    return "Basic " + encoded;
  }
}
```



The changes introduced here use MicroProfile Config and CDI to inject the value of the environment variables ***CONTEXT_ROOT***, ***SYSTEM_APP_USERNAME*** and ***SYSTEM_APP_PASSWORD*** into the ***SystemClient*** class.


::page{title="Creating a ConfigMap and Secret"}

Several options exist to configure an environment variable in a Docker container. You can set it directly in the ***Dockerfile*** with the ***ENV*** command. You can also set it in your ***kubernetes.yaml*** file by specifying a name and a value for the environment variable that you want to set for a specific container. With these options in mind, you're going to use a ConfigMap and Secret to set these values. These are resources provided by Kubernetes as a way to provide configuration values to your containers. A benefit is that they can be reused across many different containers, even if they all require different environment variables to be set with the same value.

Create a ConfigMap to configure the app name with the following ***kubectl*** command.
```bash
kubectl create configmap sys-app-root --from-literal contextRoot=/dev
```

This command deploys a ConfigMap named ***sys-app-root*** to your cluster. It has a key called ***contextRoot*** with a value of ***/dev***. The ***--from-literal*** flag allows you to specify individual key-value pairs to store in this ConfigMap. Other available options, such as ***--from-file*** and ***--from-env-file***, provide more versatility as to what you want to configure. Details about these options can be found in the [Kubernetes CLI documentation](https://kubernetes.io/docs/reference/generated/kubectl/kubectl-commands#-em-configmap-em-).

Run the following command to display details of the ConfigMap.
```bash
kubectl describe configmaps sys-app-root
```

Create a Secret to configure the new credentials that ***inventory*** uses to authenticate against ***system*** with the following ***kubectl*** command.
```bash
kubectl create secret generic sys-app-credentials --from-literal username=alice --from-literal password=wonderland
```
 
This command looks similar to the command to create a ConfigMap, but one difference is the word ***generic***. This word creates a Secret that doesn't store information in any specialized way. Different types of secrets are available, such as secrets to store Docker credentials and secrets to store public and private key pairs.

Run the following command to display details of the Secret.
```bash
kubectl describe secrets/sys-app-credentials
```

A Secret is similar to a ConfigMap. A key difference is that a Secret is used for confidential information such as credentials. One of the main differences is that you must explicitly tell ***kubectl*** to show you the contents of a Secret. Additionally, when it does show you the information, it only shows you a Base64 encoded version so that a casual onlooker doesn't accidentally see any sensitive data. Secrets don't provide any encryption by default, that is something you'll either need to do yourself or find an alternate option to configure. Encryption is not required for the application to run.



::page{title="Updating Kubernetes resources"}

Next, you will update your Kubernetes deployments to set the environment variables in your containers based on the values that are configured in the ConfigMap and Secret that you created previously. 

Replace the kubernetes file.

> To open the kubernetes.yaml file in your IDE, select
> **File** > **Open** > guide-kubernetes-microprofile-config/start/kubernetes.yaml, or click the following button

::openFile{path="/home/project/guide-kubernetes-microprofile-config/start/kubernetes.yaml"}



```yaml
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
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  template:
    metadata:
      labels:
        app: system
    spec:
      containers:
      - name: system-container
        image: system:1.0-SNAPSHOT
        ports:
        - containerPort: 9090
        # system probes
        startupProbe:
          httpGet:
            path: /health/started
            port: 9090
        livenessProbe:
          httpGet:
            path: /health/live
            port: 9090
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 1
        readinessProbe:
           httpGet:
            path: /health/ready
            port: 9090
           initialDelaySeconds: 30
           periodSeconds: 10
           timeoutSeconds: 3
           failureThreshold: 1
        # Set the environment variables
        env:
        - name: CONTEXT_ROOT
          valueFrom:
            configMapKeyRef:
              name: sys-app-root
              key: contextRoot
        - name: SYSTEM_APP_USERNAME
          valueFrom:
            secretKeyRef:
              name: sys-app-credentials
              key: username
        - name: SYSTEM_APP_PASSWORD
          valueFrom:
            secretKeyRef:
              name: sys-app-credentials
              key: password
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
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  template:
    metadata:
      labels:
        app: inventory
    spec:
      containers:
      - name: inventory-container
        image: inventory:1.0-SNAPSHOT
        ports:
        - containerPort: 9090
        # inventory probes
        startupProbe:
          httpGet:
            path: /health/started
            port: 9090
        livenessProbe:
          httpGet:
            path: /health/live
            port: 9090
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 1
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 9090
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 1
        # Set the environment variables
        env:
        - name: SYS_APP_HOSTNAME
          value: system-service
        - name: CONTEXT_ROOT
          valueFrom:
            configMapKeyRef:
              name: sys-app-root
              key: contextRoot
        - name: SYSTEM_APP_USERNAME
          valueFrom:
            secretKeyRef:
              name: sys-app-credentials
              key: username
        - name: SYSTEM_APP_PASSWORD
          valueFrom:
            secretKeyRef:
              name: sys-app-credentials
              key: password
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
    port: 9090
    targetPort: 9090
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
    port: 9090
    targetPort: 9090
    nodePort: 32000
```



The ***CONTEXT_ROOT***, ***SYSTEM_APP_USERNAME***, and ***SYSTEM_APP_PASSWORD*** environment variables are set in the ***env*** sections of ***system-container*** and ***inventory-container***.

Using the ***valueFrom*** field, you can specify the value of an environment variable from various sources. These sources include a ConfigMap, a Secret, and information about the cluster. In this example ***configMapKeyRef*** gets the value ***contextRoot*** from the ***sys-app-root*** ConfigMap. Similarly, ***secretKeyRef*** gets the values ***username*** and ***password*** from the ***sys-app-credentials*** Secret.


::page{title="Deploying your changes"}


Rebuild the application using ***mvn clean package***.
```bash
cd /home/project/guide-kubernetes-microprofile-config/start
mvn clean package
```

Run the ***docker build*** commands to rebuild container images for your application:
```bash
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
```


Push your updated images to the container registry on IBM Cloud with the following commands:

```bash
docker tag inventory:1.0-SNAPSHOT us.icr.io/$SN_ICR_NAMESPACE/inventory:1.0-SNAPSHOT
docker tag system:1.0-SNAPSHOT us.icr.io/$SN_ICR_NAMESPACE/system:1.0-SNAPSHOT
docker push us.icr.io/$SN_ICR_NAMESPACE/inventory:1.0-SNAPSHOT
docker push us.icr.io/$SN_ICR_NAMESPACE/system:1.0-SNAPSHOT
```

Update the image names and set the image pull policy to **Always** so that the images in your IBM Cloud container registry are used, and remove the **nodePort** fields so that the ports can be automatically generated:

```bash
sed -i 's=system:1.0-SNAPSHOT=us.icr.io/'"$SN_ICR_NAMESPACE"'/system:1.0-SNAPSHOT\n        imagePullPolicy: Always=g' kubernetes.yaml
sed -i 's=inventory:1.0-SNAPSHOT=us.icr.io/'"$SN_ICR_NAMESPACE"'/inventory:1.0-SNAPSHOT\n        imagePullPolicy: Always=g' kubernetes.yaml
sed -i 's=nodePort: 31000==g' kubernetes.yaml
sed -i 's=nodePort: 32000==g' kubernetes.yaml
```

Run the following command to deploy your changes to the Kubernetes cluster.
```bash
kubectl replace --force -f kubernetes.yaml
```

When this command finishes, wait for the pods to be in the Ready state. Run the following command to view the status of the pods.
```bash
kubectl get pods
```

When the pods are ready, the output shows ***1/1*** for READY and ***Running*** for STATUS.


Set up port forwarding to the new services.

Run the following commands to set up port forwarding to access the ***system*** service.

```bash
SYSTEM_NODEPORT=`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services system-service`
kubectl port-forward svc/system-service $SYSTEM_NODEPORT:9090
```

Then, run the following commands to set up port forwarding to access the **inventory** service.

```bash
INVENTORY_NODEPORT=`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services inventory-service`
kubectl port-forward svc/inventory-service $INVENTORY_NODEPORT:9090
```

You now need to use the new username, ***alice***, and the new password, ***wonderland***, to log in. Access your application with the following commands:

```bash
SYSTEM_NODEPORT=`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services system-service`
curl -s http://localhost:$SYSTEM_NODEPORT/dev/system/properties -u alice:wonderland | jq
```

Notice that the URL you are using to reach the application now has ***/dev*** as the context root. 


Verify the inventory service is working as intended by using the following commands:

```bash
INVENTORY_NODEPORT=`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services inventory-service`
curl -s http://localhost:$INVENTORY_NODEPORT/inventory/systems/system-service | jq
```

If it is not working, then check the configuration of the credentials.

::page{title="Testing the microservices"}



Update the ***pom.xml*** files so that the ***system.service.root*** and ***inventory.service.root*** properties have the correct ports to access the **system** and **inventory** services.

```bash
cd /home/project/guide-kubernetes-microprofile-config/start
SYSTEM_NODEPORT=`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services system-service`
INVENTORY_NODEPORT=`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services inventory-service`
sed -i 's=localhost:31000='"localhost:$SYSTEM_NODEPORT"'=g' inventory/pom.xml
sed -i 's=localhost:32000='"localhost:$INVENTORY_NODEPORT"'=g' inventory/pom.xml
sed -i 's=localhost:31000='"localhost:$SYSTEM_NODEPORT"'=g' system/pom.xml
```

Run the integration tests by using the following command:

```bash
mvn failsafe:integration-test \
    -Dsystem.service.root=localhost:$SYSTEM_NODEPORT \
    -Dsystem.context.root=/dev \
    -Dinventory.service.root=localhost:$INVENTORY_NODEPORT
```

The tests for ***inventory*** verify that the service can communicate with ***system*** using the configured credentials. If the credentials are misconfigured, then the ***inventory*** test fails, so the ***inventory*** test indirectly verifies that the credentials are correctly configured.

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
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.696 s - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results:

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

::page{title="Tearing down the environment"}

Press **CTRL+C** in the command-line sessions where you ran ***kubectl port-forward*** to stop the port forwarding. 

Run the following commands to delete all the resources that you created.

```bash
kubectl delete -f kubernetes.yaml
kubectl delete configmap sys-app-root
kubectl delete secret sys-app-credentials
```




::page{title="Summary"}

### Nice Work!

You have used MicroProfile Config to externalize the configuration of two microservices, and then you configured them by creating a ConfigMap and Secret in your Kubernetes cluster.




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-kubernetes-microprofile-config*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-kubernetes-microprofile-config
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Configuring%20microservices%20running%20in%20Kubernetes&guide-id=cloud-hosted-guide-kubernetes-microprofile-config)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-kubernetes-microprofile-config/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-kubernetes-microprofile-config/pulls)



### Where to next?

* [Deploying microservices to Kubernetes](https://openliberty.io/guides/kubernetes-intro.html)
* [Configuring microservices](https://openliberty.io/guides/microprofile-config.html)
* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Using Docker containers to develop microservices](https://openliberty.io/guides/docker.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
