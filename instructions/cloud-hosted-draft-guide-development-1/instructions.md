
# Welcome to the Checking the health of microservices on Kubernetes guide!

Learn how to check the health of microservices on Kubernetes by setting up readiness and liveness probes to inspect MicroProfile Health Check endpoints.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.





# What you'll learn

You will learn how to create health check endpoints for your microservices. Then, you 
will configure Kubernetes to use these endpoints to keep your microservices running smoothly.

MicroProfile Health allows services to report their health, and it publishes the overall 
health status to defined endpoints. If a service reports **UP**, then it's available. If 
the service reports **DOWN**, then it's unavailable. MicroProfile Health reports an individual 
service status at the endpoint and indicates the overall status as **UP** if all the services 
are **UP**. A service orchestrator can then use the health statuses to make decisions.

Kubernetes provides liveness and readiness probes that are used to check the health of your 
containers. These probes can check certain files in your containers, check a TCP socket, 
or make HTTP requests. MicroProfile Health exposes readiness and liveness endpoints on 
your microservices. Kubernetes polls these endpoints as specified by the probes to react 
appropriately to any change in the microservice's status. Read the 
[Adding health reports to microservices](https://openliberty.io/guides/microprofile-health.html) 
guide to learn more about MicroProfile Health.

The two microservices you will work with are called **system** and **inventory**. The **system** microservice
returns the JVM system properties of the running container and it returns the pod's name in the HTTP header
making replicas easy to distinguish from each other. The **inventory** microservice
adds the properties from the **system** microservice to the inventory. This demonstrates
how communication can be established between pods inside a cluster.





# Getting started

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-kubernetes-microprofile-health.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-kubernetes-microprofile-health.git
cd guide-kubernetes-microprofile-health
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


# Adding health checks to the inventory microservice

Navigate to **start** directory to begin.

The **inventory** microservice should be healthy only when **system** is available. To add this 
check to the **/health/ready** endpoint, you will create a class that is annotated with the
**@Readiness** annotation and implements the **HealthCheck** interface.

Create the **InventoryReadinessCheck** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryReadinessCheck.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryReadinessCheck.java




```
package io.openliberty.guides.inventory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Readiness
@ApplicationScoped
public class InventoryReadinessCheck implements HealthCheck {

    private static final String READINESS_CHECK = InventoryResource.class
                                                .getSimpleName()
                                                + " Readiness Check";

    @Inject
    @ConfigProperty(name = "SYS_APP_HOSTNAME")
    private String hostname;

    public HealthCheckResponse call() {
        if (isSystemServiceReachable()) {
            return HealthCheckResponse.up(READINESS_CHECK);
        } else {
            return HealthCheckResponse.down(READINESS_CHECK);
        }
    }

    private boolean isSystemServiceReachable() {
        try {
            Client client = ClientBuilder.newClient();
            client
                .target("http://" + hostname + ":9080/system/properties")
                .request()
                .post(null);

            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
```
{: codeblock}



This health check verifies that the **system** microservice is available at 
**http://system-service:9080/**. The **system-service** host name is only accessible from 
inside the cluster, you can't access it yourself. If it's available, then it returns an 
**UP** status. Similarly, if it's unavailable then it returns a **DOWN** status. When the 
status is **DOWN**, the microservice is considered to be unhealthy.

Create the **InventoryLivenessCheck** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryLivenessCheck.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryLivenessCheck.java




```
package io.openliberty.guides.inventory;

import javax.enterprise.context.ApplicationScoped;

import java.lang.management.MemoryMXBean;
import java.lang.management.ManagementFactory;

import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Liveness
@ApplicationScoped
public class InventoryLivenessCheck implements HealthCheck {

  @Override
  public HealthCheckResponse call() {
      MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
      long memUsed = memBean.getHeapMemoryUsage().getUsed();
      long memMax = memBean.getHeapMemoryUsage().getMax();

      return HealthCheckResponse.named(InventoryResource.class.getSimpleName()
                                      + " Liveness Check")
                                .withData("memory used", memUsed)
                                .withData("memory max", memMax)
                                .status(memUsed < memMax * 0.9).build();
  }
}
```
{: codeblock}



This liveness check verifies that the heap memory usage is below 90% of the maximum memory.
If more than 90% of the maximum memory is used, a status of **DOWN** will be returned. 

The health checks for the **system** microservice were already been implemented. The **system**
microservice was set up to become unhealthy for 60 seconds when a specific endpoint is called. 
This endpoint has been provided for you to observe the results of an unhealthy pod and how 
Kubernetes reacts.

# Configuring readiness and liveness probes

You will configure Kubernetes readiness and liveness probes.
Readiness probes are responsible for determining that your application is ready to accept requests.
If it's not ready, traffic won't be routed to the container.
Liveness probes are responsible for determining when a container needs to be restarted. 

Create the kubernetes configuration file.

> Run the following touch command in your terminal
```
touch /home/project/guide-kubernetes-microprofile-health/start/kubernetes.yaml
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-kubernetes-microprofile-health/start/kubernetes.yaml




```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: system-deployment
  labels:
    app: system
spec:
  replicas: 2
  selector:
    matchLabels:
      app: system
  template:
    metadata:
      labels:
        app: system
    spec:
      containers:
      - name: system-container
        image: system:1.0-SNAPSHOT
        ports:
        - containerPort: 9080
        # system probes
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 9080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 1
        livenessProbe:
          httpGet:
            path: /health/live
            port: 9080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 1
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
      - name: inventory-container
        image: inventory:1.0-SNAPSHOT
        ports:
        - containerPort: 9080
        env:
        - name: SYS_APP_HOSTNAME
          value: system-service
        # inventory probe
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 9080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 1
        livenessProbe:
          httpGet:
            path: /health/live
            port: 9080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 1
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



The readiness and liveness probes are configured for the containers running the **system** 
and **inventory** microservices.

The readiness probes are configured to poll the **/health/ready** endpoint.
The readiness probe determines the READY status of the container as seen in the **kubectl get pods** output.
The **initialDelaySeconds** field defines how long the probe should wait before it 
starts to poll so the probe does not start making requests before the server has started. 
The **failureThreshold** option defines how many times the probe should fail 
before the state should be changed from ready to not ready. The **timeoutSeconds** 
option defines how many seconds before the probe times out. The **periodSeconds** 
option defines how often the probe should poll the given endpoint.

The liveness probes are configured to poll the **/health/live** endpoint.
The liveness probes determine when a container needs to be restarted.
Similar to the readiness probes, the liveness probes also define
**initialDelaySeconds**,
**failureThreshold**,
**timeoutSeconds**,
and **periodSeconds**.

# Deploying the microservices

To build these microservices, navigate to the **start** directory and run the following 
command.

```
mvn package
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

When the builds succeed, run the following command to deploy the necessary Kubernetes 
resources to serve the applications.

```
kubectl apply -f kubernetes.yaml
```
{: codeblock}


Use the following command to view the status of the pods. There will be two **system** pods 
and one **inventory** pod, later you'll observe their behavior as the **system** pods become unhealthy.

```
kubectl get pods
```
{: codeblock}


```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-694c7b74f7-hcf4q     1/1       Running   0          59s
system-deployment-694c7b74f7-lrlf7     1/1       Running   0          59s
inventory-deployment-cf8f564c6-nctcr   1/1       Running   0          59s
```

Wait until the pods are ready. After the pods are ready, you will make requests to your 
services.


Make a request to the system service to see the JVM system properties with the following command:

```
curl http://$(minikube ip):31000/system/properties
```
{: codeblock}

The readiness probe ensures the READY state won't be `1/1`
until the container is available to accept requests.
Without a readiness probe, you may notice an unsuccessful response from the server.
This scenario can occur when the container has started,
but the application server hasn't fully initialized.
With the readiness probe, you can be certain the pod will only accept traffic
when the microservice has fully started.

Similarly, access the inventory service and observe the successful request with the following command:

```
curl http://$(minikube ip):32000/inventory/systems/system-service
```
{: codeblock}

# Changing the ready state of the system microservice


An endpoint has been provided under the `system` microservice to set it to an unhealthy 
state in the health check. The unhealthy state will cause the readiness probe to fail.
Use the `curl` command to invoke this endpoint by making a POST request to the
`/system/properties/unhealthy` endpoint.

```
curl -X POST http://$(minikube ip):31000/system/properties/unhealthy
```
{: codeblock}

Run the following command to view the state of the pods:

```
kubectl get pods
```
{: codeblock}


```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-694c7b74f7-hcf4q     1/1       Running   0          1m
system-deployment-694c7b74f7-lrlf7     0/1       Running   0          1m
inventory-deployment-cf8f564c6-nctcr   1/1       Running   0          1m
```


You will notice that one of the two `system` pods is no longer in the ready state.
Make a request to the `/system/properties` endpoint with the following command:

```
curl http://$(minikube ip):31000/system/properties
```
{: codeblock}

Observe that your request will still be successful because you have two replicas and one is still healthy.

### Observing the effects on the inventory microservice


Wait until the `system` pod is ready again.
Make two POST requests to `/system/properties/unhealthy` endpoint with the following command:

```
curl -X POST http://$(minikube ip):31000/system/properties/unhealthy
```
{: codeblock}

If you see the same pod name twice, make the request again until you see that the second 
pod has been made unhealthy. You may see the same pod twice because there's a delay 
between a pod becoming unhealthy and the readiness probe noticing it.
Therefore, traffic may still be routed to the unhealthy service for approximately 5 seconds.
Continue to observe the output of `kubectl get pods`.

```
kubectl get pods
```
{: codeblock}

You will see both pods are no longer ready. 
During this process, the readiness probe for the `inventory` microservice will also fail. 
Observe it's no longer in the ready state either.

First, both **system** pods will no longer be ready because the readiness probe failed.

```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-694c7b74f7-hcf4q     0/1       Running   0          5m
system-deployment-694c7b74f7-lrlf7     0/1       Running   0          5m
inventory-deployment-cf8f564c6-nctcr   1/1       Running   0          5m
```

Next, the **inventory** pod is no longer ready because the readiness probe failed. The probe 
failed because **system-service** is now unavailable.

```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-694c7b74f7-hcf4q     0/1       Running   0          6m
system-deployment-694c7b74f7-lrlf7     0/1       Running   0          6m
inventory-deployment-cf8f564c6-nctcr   0/1       Running   0          6m
```

Then, the **system** pods will start to become healthy again after 60 seconds.

```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-694c7b74f7-hcf4q     1/1       Running   0          7m
system-deployment-694c7b74f7-lrlf7     0/1       Running   0          7m
inventory-deployment-cf8f564c6-nctcr   0/1       Running   0          7m
```

```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-694c7b74f7-hcf4q     1/1       Running   0          7m
system-deployment-694c7b74f7-lrlf7     1/1       Running   0          7m
inventory-deployment-cf8f564c6-nctcr   0/1       Running   0          7m
```

Finally, you will see all of the pods have recovered.

```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-694c7b74f7-hcf4q     1/1       Running   0          8m
system-deployment-694c7b74f7-lrlf7     1/1       Running   0          8m
inventory-deployment-cf8f564c6-nctcr   1/1       Running   0          8m
```

# Testing the microservices


Run the tests by running the following command:

```
mvn failsafe:integration-test -Dcluster.ip=$(minikube ip)
```
{: codeblock}

A few tests are included for you to test the basic functions of the microservices.
If a test failure occurs, then you might have introduced a bug into the code.
To run the tests, wait for all pods to be in the ready state before proceeding further.

When the tests succeed, you should see output similar to the following in your console.

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.65 s - in it.io.openliberty.guides.system.SystemEndpointIT

Results:

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.542 s - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results:

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

# Tearing down the environment

To remove all of the resources created during this guide, run the following command to 
delete all of the resources that you created.

```
kubectl delete -f kubernetes.yaml
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

You have used MicroProfile Health and Open Liberty to create endpoints that report on 

your microservice's status. Then, you observed how Kubernetes uses the **/health/ready** and
**/health/live** endpoints to keep your microservices running smoothly.




## Clean up your environment

Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-kubernetes-microprofile-health** project by running the following commands:

```
cd /home/project
rm -fr guide-kubernetes-microprofile-health
```
{: codeblock}

## What did you think of this guide?
We want to hear from you. To provide feedback on your experience with this guide, click the **Support/Feedback** button in the IDE,
select **Give feedback** option, fill in the fields, choose **General** category, and click the **Post Idea** button.

## What could make this guide better?
You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback](https://github.com/OpenLiberty/guide-kubernetes-microprofile-health/issues)
* [Create a pull request to contribute to this guide](https://github.com/OpenLiberty/guide-kubernetes-microprofile-health/pulls)




## Where to next? 

* [Adding health reports to microservices](https://openliberty.io/guides/microprofile-health.html)
* [Deploying microservices to Kubernetes](https://openliberty.io/guides/kubernetes-intro.html)


## Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.