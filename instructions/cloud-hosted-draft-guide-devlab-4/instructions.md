---
markdown-version: v1
tool-type: theia
---
::page{title="Welcome to the Managing microservice traffic using Istio guide!"}

Explore how to manage microservice traffic using Istio.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.





::page{title="What you'll learn"}

You will learn how to deploy an application to a Kubernetes cluster and enable {istio} on it. You will also learn how to configure
{istio} to shift traffic to implement blue-green deployments for microservices.

### What is {istio}?

[istio](https://istio.io/) is a service mesh, meaning that it's a platform for managing
how microservices interact with each other and the outside world.
{istio} consists of a control plane and sidecars that are injected into application pods. The sidecars contain
the [Envoy](https://www.envoyproxy.io/) proxy. You can think of Envoy as a sidecar that intercepts
and controls all the HTTP and TCP traffic to and from your container.

While {istio} runs on top of Kubernetes and that will be the focus of this guide, you can also use {istio} with
other environments such as [Docker Compose](https://docs.docker.com/compose/overview/). istio has many features such as
traffic shifting, request routing, access control, and distributed tracing, but the focus of this guide will be on traffic shifting.

### Why {istio}?

{istio} provides a collection of features that allows you to manage several aspects of your services.
One example is {istio}'s routing features. You can route HTTP requests based on several factors such as HTTP headers or cookies.
Another use case for {istio} is telemetry, which you can use to enable distributed tracing. Distributed tracing allows you
to visualize how HTTP requests travel between different services in your cluster by using a tool such as [Jaeger](https://www.jaegertracing.io/).
Additionally, as part of its collection of security features, {istio} allows you to enable mutual TLS between pods in your cluster.
Enabling TLS between pods secures communication between microservices internally.

https://openliberty.io/guides/istio-intro.html#what-are-blue-green-deployments[Blue-green deployments] are a method of deploying your applications such that you have two nearly identical environments where one acts
as a sort of staging environment and the other is a production environment. This allows you to switch traffic from staging to production
once a new version of your application has been verified to work.
You'll use {istio} to implement blue-green deployments. The traffic shifting feature allows you to allocate a percentage of
traffic to certain versions of services. You can use this feature to shift 100 percent of live traffic to blue deployments and 100 percent
of test traffic to green deployments. Then, you can shift the traffic to point to the opposite deployments as necessary to
perform blue-green deployments.

The microservice you'll deploy is called ***system***.
It responds with your current system's JVM properties and it returns the app version in the response header.
You will increment the version number when you update the application.
With this number, you can determine which version of the microservice is running in your production or test environments.

### What are blue-green deployments?

Blue-green deployments are a way of deploying your applications such that you have two environments where your application runs.
In this scenario, you will have a production environment and a test environment.
At any point in time, the blue deployment can accept production traffic and the green deployment can accept test traffic, or vice versa.
When you want to deploy a new version of your application, you deploy to the color that is acting as your test environment.
After the new version is verified on the test environment, the traffic is shifted over.
Thus, your live traffic is now being handled by what used to be the test site.




::page{title="Getting started"}

To open a new command-line session,
select ***Terminal*** > ***New Terminal*** from the menu of the IDE.

Run the following command to navigate to the ***/home/project*** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-istio-intro.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-istio-intro.git
cd guide-istio-intro
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.






::page{title="Deploying Istio"}

Install istio by following the instructions in the official [istio Getting started documentation](https://istio.io/latest/docs/setup/getting-started).

Run the following command to verify that the ***istioctl*** path was set successfully:

```bash
istioctl version
```

The output will be similar to the following example:
```
no running Istio pods in "istio-system"
1.24.2
```

Run the following command to configure the {istio} profile on Kubernetes:
```bash
istioctl install --set profile=demo
```

The following output appears when the installation is complete:
```
✔ Istio core installed
✔ Istiod installed
✔ Egress gateways installed
✔ Ingress gateways installed
✔ Installation complete
```

Verify that Istio was successfully deployed by running the following command:

```bash
kubectl get deployments -n istio-system
```

All the values in the ***AVAILABLE*** column will have a value of ***1*** after
the deployment is complete.

```
NAME                     READY   UP-TO-DATE   AVAILABLE   AGE
istio-egressgateway      1/1     1            1           2m48s
istio-ingressgateway     1/1     1            1           2m48s
istiod                   1/1     1            1           2m48s
```
 
Ensure that the {istio} deployments are all available before you continue. The deployments might take a few minutes to become available. If the deployments aren't available after a few minutes, then increase the amount of memory available to your Kubernetes cluster. On Docker Desktop, you can increase the memory from your {docker} preferences. On {minikube}, you can increase the memory by using the ***--memory*** flag.

Finally, create the ***istio-injection*** label and set its value to ***enabled***:

```bash
kubectl label namespace default istio-injection=enabled
```

Adding this label enables automatic {istio} sidecar injection. Automatic injection means that sidecars are automatically injected into your pods when you deploy your application.

::page{title="Deploying version 1 of the system microservice"}

Navigate to the ***guide-{projectid}/start*** directory and run the following command to build the application locally.

```bash
./mvnw clean package
```



Next, run the ***docker build*** commands to build the container image for your application:
```bash
docker build -t system:1.0-SNAPSHOT .
```

The command builds a {docker} image for the ***system*** microservice.
The ***-t*** flag in the ***docker build*** command allows the Docker image to be labeled (tagged) in the ***name[:tag]*** format.
The tag for an image describes the specific image version.
If the optional ***[:tag]*** tag is not specified, the ***latest*** tag is created by default.
You can verify that this image was created by running the following command: 

```bash
docker images
```

You'll see an image called ***system:1.0-SNAPSHOT*** listed in a table similar to the output.

```
REPOSITORY                     TAG                              IMAGE ID        CREATED          SIZE
system                         1.0-SNAPSHOT                     8856039f4c42    9 minutes ago    745MB
istio/proxyv2                  1.24.2                           7a3aaffcf645    3 weeks ago      347MB
istio/pilot                    1.24.2                           4974b5b22dcc    3 weeks ago      261MB
icr.io/appcafe/open-liberty    kernel-slim-java11-openj9-ubi    d6ef646493e1    8 days ago       729MB
```

To deploy the ***system*** microservice to the Kubernetes cluster, use the following command to deploy the microservice.

```bash
kubectl apply -f system.yaml
```

You can see that your resources are created:

```
gateway.networking.istio.io/sys-app-gateway created
service/system-service created
deployment.apps/system-deployment-blue created
deployment.apps/system-deployment-green created
destinationrule.networking.istio.io/system-destination-rule created
```

system.yaml
```
```

View the ***system.yaml*** file. It contains two ***deployments***, a ***service***, a ***gateway***, and a ***destination rule***. One of the deployments is labeled ***blue*** and the second deployment is labeled ***green***. The service points to both of these deployments. The {istio} gateway is the entry point for HTTP requests to the cluster. A destination rule is used to apply policies post-routing, in this situation it is used to define service subsets that can be specifically routed to.

traffic.yaml
```
```

View the ***traffic.yaml*** file. It contains two virtual services. A virtual service defines how requests are routed to your applications. In the virtual services, you can configure the weight, which controls the amount of traffic going to each deployment. In this case, the weights should be 100 or 0, which corresponds to which deployment is live.

Deploy the resources defined in the ***traffic.yaml*** file.

```bash
kubectl apply -f traffic.yaml
```

You can see that the virtual services have been created.

```
virtualservice.networking.istio.io/system-virtual-service created
virtualservice.networking.istio.io/system-test-virtual-service created
```

You can check that all of the deployments are available by running the following command.

```bash
kubectl get deployments
```

The command produces a list of deployments for your microservices that is similar to the following output.

```
NAME                     DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
system-deployment-blue    1         1         1            1           1m
system-deployment-green   1         1         1            1           1m
```

After all the deployments are available, you will make a request to version 1 of the deployed application. As defined in the ***system.yaml***, file the ***gateway*** is expecting the host to be ***example.com***. However, requests to ***example.com*** won't be routed to the appropriate IP address. To ensure that the gateway routes your requests appropriately, ensure that the Host header is set to ***example.com***. For instance, you can set the ***Host*** header with the ***-H*** option of the ***curl*** command.


Make a request to the service by running the following ***curl*** command.


```bash
export INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')
curl -H "Host:example.com" -I http://***minikube ip***:$INGRESS_PORT/system/properties
```


You'll see a header called ***x-app-version*** along with the corresponding version.

```
x-app-version: 1.0-SNAPSHOT
```


::page{title="Deploying version 2 of the system microservice"}

Replace the ***SystemResource*** class.

> To open the SystemResource.java file in your IDE, select
> ***File*** > ***Open*** > guide-istio-intro/start/src/main/java/io/openliberty/guides/system/SystemResource.java, or click the following button

::openFile{path="/home/project/guide-istio-intro/start/src/main/java/io/openliberty/guides/system/SystemResource.java"}



```java
package io.openliberty.guides.system;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/properties")
public class SystemResource {

  public static String appVersion = "2.0-SNAPSHOT";

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getProperties() {
    return Response.ok(System.getProperties())
      .header("X-Pod-Name", System.getenv("HOSTNAME"))
      .header("X-App-Version", appVersion)
      .build();
  }
}
```


Click the :fa-copy: ***Copy*** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.


The ***system*** microservice is set up to respond with the version that is set in the ***SystemResource.java*** file.
The tag for the {docker} image is also dependent on the version that is specified in the ***SystemResource.java*** file.
Manually update the ***APP_VERSION*** field of the microservice to ***2.0-SNAPSHOT***.

Use Maven to repackage your microservice:

```bash
./mvnw clean package
```

Next, build the new version of the container image as ***2.0-SNAPSHOT***:
```bash
docker build -t system:2.0-SNAPSHOT .
```

Deploy the new image to the green deployment.

```bash
kubectl set image deployment/system-deployment-green system-container=system:2.0-SNAPSHOT
```

You will work with two environments.
One of the environments is a test site that is located at ***test.example.com***.
The other environment is your production environment that is located at ***example.com***.
To begin with, the production environment is tied to the blue deployment and the test environment is tied to the green deployment.

Test the updated microservice by making requests to the test site.
The ***x-app-version*** header now has a value of ***2.0-SNAPSHOT*** on the test site and is still ***1.0-SNAPSHOT*** on the live site.

Make a request to the service by running the following ***curl*** command.


```bash
curl -H "Host:test.example.com" -I http://***minikube ip***:$INGRESS_PORT/system/properties
```

You'll see the new version in the ***x-app-version*** response header.

```
x-app-version: 2.0-SNAPSHOT
```

Update the ***traffic.yaml*** file in the ***start*** directory.

> To open the traffic.yaml file in your IDE, select
> ***File*** > ***Open*** > guide-istio-intro/start/traffic.yaml, or click the following button

::openFile{path="/home/project/guide-istio-intro/start/traffic.yaml"}



After you see that the microservice is working on the test site, modify the ***weights*** in the ***traffic.yaml*** file to shift 100 percent of the ***example.com*** traffic to the green deployment, and 100 percent of the ***test.example.com*** traffic to the blue deployment.


Deploy the updated ***traffic.yaml*** file.

```bash
kubectl apply -f traffic.yaml
```

Ensure that the live traffic is now being routed to version 2 of the microservice.


Make a request to the service by running the following ***curl*** command.


```bash
curl -H "Host:example.com" -I http://***minikube ip***:$INGRESS_PORT/system/properties
```


You'll see the new version in the ***x-app-version*** response header.

```
x-app-version: 2.0-SNAPSHOT
```

::page{title="Testing microservices that are running on Kubernetes"}

Next, you will create a test to verify that the correct version of your microservice is running.

Create the ***SystemEndpointIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-istio-intro/start/src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java
```


> Then, to open the SystemEndpointIT.java file in your IDE, select
> ***File*** > ***Open*** > guide-istio-intro/start/src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java, or click the following button

::openFile{path="/home/project/guide-istio-intro/start/src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java"}



```java
package it.io.openliberty.guides.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import io.openliberty.guides.system.SystemResource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.AfterEach;

@TestMethodOrder(OrderAnnotation.class)
public class SystemEndpointIT {

    private static String clusterUrl;

    private Client client;
    private Response response;

    @BeforeAll
    public static void oneTimeSetup() {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        String clusterIp = System.getProperty("cluster.ip");
        String nodePort = System.getProperty("port");

        clusterUrl = "http://" + clusterIp + ":" + nodePort + "/system/properties/";
    }

    @BeforeEach
    public void setup() {
        response = null;
        client = ClientBuilder.newBuilder()
                    .hostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .build();
    }

    @AfterEach
    public void teardown() {
        client.close();
    }

    @Test
    @Order(1)
    public void testPodNameNotNull() {
        response = this.getResponse(clusterUrl);
        this.assertResponse(clusterUrl, response);
        String greeting = response.getHeaderString("X-Pod-Name");

        String message = "Container name should not be null but it was. "
            + "The service is probably not running inside a container";

        assertNotNull(greeting, message);
    }

    @Test
    @Order(2)
    public void testAppVersion() {
        response = this.getResponse(clusterUrl);

        String expectedVersion = SystemResource.appVersion;
        String actualVersion = response.getHeaderString("X-App-Version");

        assertEquals(expectedVersion, actualVersion);
    }

    @Test
    @Order(3)
    public void testGetProperties() {
        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(clusterUrl);
        Response response = target
            .request()
            .header("Host", System.getProperty("host-header"))
            .get();

        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + clusterUrl);

        response.close();
    }

    private Response getResponse(String url) {
        return client
            .target(url)
            .request()
            .header("Host", System.getProperty("host-header"))
            .get();
    }

    private void assertResponse(String url, Response response) {
        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + url);
    }

}
```



The ***testAppVersion()*** test case verifies that the correct version number is returned in the response headers.

Run the following commands to compile and start the tests:


```bash
./mvnw test-compile
./mvnw failsafe:integration-test -Dcluster.ip=***minikube ip*** -Dport=$INGRESS_PORT
```
The ***cluster.ip*** and ***port*** parameters refer to the IP address and port for the {istio} gateway.

If the tests pass, then you should see output similar to the following example:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.503 s - in it.io.openliberty.guides.system.SystemEndpointIT

Results:

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

::page{title="Tearing down your environment"}

You might want to teardown all the deployed resources as a cleanup step.

Delete your resources from the cluster:

```bash
kubectl delete -f system.yaml
kubectl delete -f traffic.yaml
```

Delete the ***istio-injection*** label from the default namespace. The hyphen immediately
after the label name indicates that the label should be deleted.

```bash
kubectl label namespace default istio-injection-
```

Delete all {istio} resources from the cluster:

```bash
istioctl uninstall --purge
```


Perform the following steps to return your environment to a clean state.

. Point the Docker daemon back to your local machine:
+
```bash
eval $(minikube docker-env -u)
```

. Stop and delete your Minikube cluster:
+
```bash
minikube stop
minikube delete
```



::page{title="Summary"}

### Nice Work!

You have deployed a microservice that runs on Open Liberty to a Kubernetes cluster and used {istio} to implement a blue-green deployment scheme.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-istio-intro*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-istio-intro
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Managing%20microservice%20traffic%20using%20Istio&guide-id=cloud-hosted-guide-istio-intro)

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-istio-intro/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-istio-intro/pulls)



### Where to next?

* [Using Docker containers to develop microservices](https://openliberty.io/guides/docker.html)
* [Deploying microservices to Kubernetes](https://openliberty.io/guides/kubernetes-intro.html)
* [Configuring microservices running in Kubernetes](https://openliberty.io/guides/kubernetes-microprofile-config.html)
* [Checking the health of microservices on Kubernetes](https://openliberty.io/guides/kubernetes-microprofile-health.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
