---
markdown-version: v1
title: instructions
branch: lab-364-instruction
version-history-start-date: 2022-02-09T14:19:17.000Z
tool-type: theia
---
::page{title="Welcome to the Checking the health of microservices on Kubernetes guide!"}

Learn how to check the health of microservices on Kubernetes by setting up startup, liveness, and readiness probes to inspect MicroProfile Health Check endpoints.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.





::page{title="What you'll learn"}

You will learn how to create health check endpoints for your microservices. Then, you will configure Kubernetes to use these endpoints to keep your microservices running smoothly. 

MicroProfile Health allows services to report their health, and it publishes the overall health status to defined endpoints. If a service reports ***UP***, then it's available. If the service reports ***DOWN***, then it's unavailable. MicroProfile Health reports an individual service status at the endpoint and indicates the overall status as ***UP*** if all the services are ***UP***. A service orchestrator can then use the health statuses to make decisions.

Kubernetes provides startup, liveness, and readiness probes that are used to check the health of your containers. These probes can check certain files in your containers, check a TCP socket, or make HTTP requests. MicroProfile Health exposes startup, liveness, and readiness endpoints on your microservices. Kubernetes polls these endpoints as specified by the probes to react appropriately to any change in the microservice's status. Read the [Adding health reports to microservices](https://openliberty.io/guides/microprofile-health.html) guide to learn more about MicroProfile Health.

The two microservices you will work with are called ***system*** and ***inventory***. The ***system*** microservice returns the JVM system properties of the running container and it returns the pod's name in the HTTP header making replicas easy to distinguish from each other. The ***inventory*** microservice adds the properties from the ***system*** microservice to the inventory. This demonstrates how communication can be established between pods inside a cluster.





::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-kubernetes-microprofile-health.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-kubernetes-microprofile-health.git
cd guide-kubernetes-microprofile-health
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.


::page{title="Adding health checks to the inventory microservice"}

Navigate to ***start*** directory to begin.

Create the ***InventoryStartupCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryStartupCheck.java
```


> Then, to open the InventoryStartupCheck.java file in your IDE, select
> **File** > **Open** > guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryStartupCheck.java, or click the following button

::openFile{path="/home/project/guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryStartupCheck.java"}



```java
package io.openliberty.guides.inventory;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Startup;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Startup
@ApplicationScoped
public class InventoryStartupCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean)
        ManagementFactory.getOperatingSystemMXBean();
        double cpuUsed = bean.getSystemCpuLoad();
        String cpuUsage = String.valueOf(cpuUsed);
        return HealthCheckResponse.named(InventoryResource.class
                                            .getSimpleName() + " Startup Check")
                                            .status(cpuUsed < 0.95).build();
    }
}

```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


A health check for startup allows applications to define startup probes that verify whether deployed application is fully initialized before the liveness probe takes over. This check is useful for applications that require additional startup time on their first initialization. The ***@Startup*** annotation must be applied on a HealthCheck implementation to define a startup check procedure. Otherwise, this annotation is ignored. This startup check verifies that the cpu usage is below 95%. If more than 95% of the cpu is used, a status of ***DOWN*** is returned. 

Create the ***InventoryLivenessCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryLivenessCheck.java
```


> Then, to open the InventoryLivenessCheck.java file in your IDE, select
> **File** > **Open** > guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryLivenessCheck.java, or click the following button

::openFile{path="/home/project/guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryLivenessCheck.java"}



```java
package io.openliberty.guides.inventory;

import jakarta.enterprise.context.ApplicationScoped;

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
                                .status(memUsed < memMax * 0.9).build();
  }
}
```



A health check for liveness allows third party services to determine whether the application is running. If this procedure fails, the application can be stopped. The ***@Liveness*** annotation must be applied on a HealthCheck implementation to define a Liveness check procedure. Otherwise, this annotation is ignored. This liveness check verifies that the heap memory usage is below 90% of the maximum memory. If more than 90% of the maximum memory is used, a status of ***DOWN*** is returned. 

The ***inventory*** microservice is healthy only when the ***system*** microservice is available. To add this check to the ***/health/ready*** endpoint, create a class that is annotated with the ***@Readiness*** annotation and implements the ***HealthCheck*** interface. A Health Check for readiness allows third party services to know whether the application is ready to process requests. The ***@Readiness*** annotation must be applied on a HealthCheck implementation to define a readiness check procedure. Otherwise, this annotation is ignored.

Create the ***InventoryReadinessCheck*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryReadinessCheck.java
```


> Then, to open the InventoryReadinessCheck.java file in your IDE, select
> **File** > **Open** > guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryReadinessCheck.java, or click the following button

::openFile{path="/home/project/guide-kubernetes-microprofile-health/start/inventory/src/main/java/io/openliberty/guides/inventory/InventoryReadinessCheck.java"}



```java
package io.openliberty.guides.inventory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

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



This health check verifies that the ***system*** microservice is available at ***http://system-service:9080/***. The ***system-service*** host name is accessible only from inside the cluster; you can't access it yourself. If it's available, then it returns an ***UP*** status. Similarly, if it's unavailable then it returns a ***DOWN*** status. When the status is ***DOWN***, the microservice is considered to be unhealthy.

The health checks for the ***system*** microservice were already been implemented. The ***system*** microservice was set up to become unhealthy for 60 seconds when a specific endpoint is called. This endpoint has been provided for you to observe the results of an unhealthy pod and how Kubernetes reacts.

::page{title="Configuring startup, liveness, and readiness probes"}

You will configure Kubernetes startup, liveness, and readiness probes. Startup probes determine whether your application is fully initialized. Liveness probes determine whether a container needs to be restarted. Readiness probes determine whether your application is ready to accept requests. If it's not ready, no traffic is routed to the container.

Create the kubernetes configuration file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-kubernetes-microprofile-health/start/kubernetes.yaml
```


> Then, to open the kubernetes.yaml file in your IDE, select
> **File** > **Open** > guide-kubernetes-microprofile-health/start/kubernetes.yaml, or click the following button

::openFile{path="/home/project/guide-kubernetes-microprofile-health/start/kubernetes.yaml"}



```yaml
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
        - containerPort: 9080
        # system probes
        startupProbe:
          httpGet:
            path: /health/started
            port: 9080
        livenessProbe:
          httpGet:
            path: /health/live
            port: 9080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 1
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 9080
          initialDelaySeconds: 30
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
        - containerPort: 9080
        env:
        - name: SYS_APP_HOSTNAME
          value: system-service
        # inventory probes
        startupProbe:
          httpGet:
            path: /health/started
            port: 9080
        livenessProbe:
          httpGet:
            path: /health/live
            port: 9080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 1
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 9080
          initialDelaySeconds: 30
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



The startup, liveness, and readiness probes are configured for the containers that are running the ***system*** and ***inventory*** microservices.

The startup probes are configured to poll the ***/health/started*** endpoint. The startup probe determines whether a container is started.

The liveness probes are configured to poll the ***/health/live*** endpoint. The liveness probes determine whether a container needs to be restarted. The ***initialDelaySeconds*** field defines the duration that the probe waits before it starts to poll so that it does not make requests before the server is started. The ***periodSeconds*** option defines how often the probe polls the given endpoint. The ***timeoutSeconds*** option defines how many seconds before the probe times out. The ***failureThreshold*** option defines how many times the probe fails before the state changes from ready to not ready.

The readiness probes are configured to poll the ***/health/ready*** endpoint. The readiness probe determines the READY status of the container, as seen in the ***kubectl get pods*** output. Similar to the liveness probes, the readiness probes also define ***initialDelaySeconds***, ***periodSeconds***, ***timeoutSeconds***, and ***failureThreshold***.

::page{title="Deploying the microservices"}

To build these microservices, navigate to the ***start*** directory and run the following command.

```bash
mvn package
```

Run the following command to download or update to the latest Open Liberty Docker image:

```bash
docker pull icr.io/appcafe/open-liberty:full-java11-openj9-ubi
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

Update the image names so that the images in your IBM Cloud container registry are used. Set the image pull policy to ***Always*** and remove the ***nodePort*** fields so that the ports can be automatically generated:
```bash
sed -i 's=system:1.0-SNAPSHOT=us.icr.io/'"$SN_ICR_NAMESPACE"'/system:1.0-SNAPSHOT\n        imagePullPolicy: Always=g' kubernetes.yaml
sed -i 's=inventory:1.0-SNAPSHOT=us.icr.io/'"$SN_ICR_NAMESPACE"'/inventory:1.0-SNAPSHOT\n        imagePullPolicy: Always=g' kubernetes.yaml
sed -i 's=nodePort: 31000==g' kubernetes.yaml
sed -i 's=nodePort: 32000==g' kubernetes.yaml
```

When the builds succeed, run the following command to deploy the necessary Kubernetes resources to serve the applications.

```bash
kubectl apply -f kubernetes.yaml
```

Use the following command to view the status of the pods. There will be two ***system*** pods and one ***inventory*** pod, later you'll observe their behavior as the ***system*** pods become unhealthy. 

```bash
kubectl get pods
```

```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-694c7b74f7-hcf4q     1/1       Running   0          59s
system-deployment-694c7b74f7-lrlf7     1/1       Running   0          59s
inventory-deployment-cf8f564c6-nctcr   1/1       Running   0          59s
```

Wait until the pods are ready. After the pods are ready, you will make requests to your services.


In this IBM cloud environment, you need to access the services by using the Kubernetes API. Run the following command to start a proxy to the Kubernetes API server:

```bash
kubectl proxy
```

Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE. Run the following commands to store the proxy path of the ***system*** and ***inventory*** services.
```bash
SYSTEM_PROXY=localhost:8001/api/v1/namespaces/$SN_ICR_NAMESPACE/services/system-service/proxy
INVENTORY_PROXY=localhost:8001/api/v1/namespaces/$SN_ICR_NAMESPACE/services/inventory-service/proxy
```

Run the following echo commands to verify the variables:

```bash
echo $SYSTEM_PROXY && echo $INVENTORY_PROXY
```

The output appears as shown in the following example:

```
localhost:8001/api/v1/namespaces/sn-labs-yourname/services/system-service/proxy
localhost:8001/api/v1/namespaces/sn-labs-yourname/services/inventory-service/proxy
```

Make a request to the system service to see the JVM system properties with the following ***curl*** command:
```bash
curl -s http://$SYSTEM_PROXY/system/properties | jq
```

The readiness probe ensures the READY state won't be ***1/1*** until the container is available to accept requests. Without a readiness probe, you might notice an unsuccessful response from the server. This scenario can occur when the container is started, but the application server isn't fully initialized. With the readiness probe, you can be certain the pod accepts traffic only when the microservice is fully started.

Similarly, access the inventory service and observe the successful request with the following command:
```bash
curl -s http://$INVENTORY_PROXY/inventory/systems/system-service | jq
```

::page{title="Changing the ready state of the system microservice"}

An ***unhealthy*** endpoint has been provided under the ***system*** microservice to set it to an unhealthy state. The unhealthy state causes the readiness probe to fail. A request to the ***unhealthy*** endpoint puts the service in an unhealthy state as a simulation.


Run the following ***curl*** command to invoke the unhealthy endpoint:
```bash
curl http://$SYSTEM_PROXY/system/unhealthy
```

Run the following command to view the state of the pods:

```bash
kubectl get pods
```

```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-694c7b74f7-hcf4q     1/1       Running   0          1m
system-deployment-694c7b74f7-lrlf7     0/1       Running   0          1m
inventory-deployment-cf8f564c6-nctcr   1/1       Running   0          1m
```


You will notice that one of the two ***system*** pods is no longer in the ready state. Make a request to the ***/system/properties*** endpoint with the following command:
```bash
curl -s http://$SYSTEM_PROXY/system/properties | jq
```

Your request is successful because you have two replicas and one is still healthy.

### Observing the effects on the inventory microservice


Wait until the ***system-service*** pod is ready again. Make several requests to the ***/system/unhealthy*** endpoint of the ***system*** service until you see two pods are unhealthy.
```bash
curl http://$SYSTEM_PROXY/system/unhealthy
```

Observe the output of ***kubectl get pods***.
```bash
kubectl get pods
```

You will see both pods are no longer ready. During this process, the readiness probe for the ***inventory*** microservice will also fail. Observe that it's no longer in the ready state either.

First, both ***system*** pods will no longer be ready because the readiness probe failed.

```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-694c7b74f7-hcf4q     0/1       Running   0          5m
system-deployment-694c7b74f7-lrlf7     0/1       Running   0          5m
inventory-deployment-cf8f564c6-nctcr   1/1       Running   0          5m
```

Next, the ***inventory*** pod is no longer ready because the readiness probe failed. The probe failed because ***system-service*** is now unavailable.

```
NAME                                   READY     STATUS    RESTARTS   AGE
system-deployment-694c7b74f7-hcf4q     0/1       Running   0          6m
system-deployment-694c7b74f7-lrlf7     0/1       Running   0          6m
inventory-deployment-cf8f564c6-nctcr   0/1       Running   0          6m
```

Then, the ***system*** pods will start to become healthy again after 60 seconds.

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

::page{title="Testing the microservices"}


Run the following commands to store the proxy path of the ***system*** and ***inventory*** services.
```bash
cd /home/project/guide-kubernetes-microprofile-health/start
SYSTEM_PROXY=localhost:8001/api/v1/namespaces/$SN_ICR_NAMESPACE/services/system-service/proxy
INVENTORY_PROXY=localhost:8001/api/v1/namespaces/$SN_ICR_NAMESPACE/services/inventory-service/proxy
```

Run the integration tests by using the following command:
```bash
mvn failsafe:integration-test \
    -Dsystem.service.root=$SYSTEM_PROXY \
    -Dinventory.service.root=$INVENTORY_PROXY
```

A few tests are included for you to test the basic functions of the microservices. If a test fails, then you might have introduced a bug into the code. Wait for all pods to be in the ready state before you run the tests. 

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
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.542 s - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results:

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

::page{title="Tearing down the environment"}

Press **CTRL+C** to stop the proxy server that was started at step 6 ***Deploying the microservices***.

To remove all of the resources created during this guide, run the following command to delete all of the resources that you created.

```bash
kubectl delete -f kubernetes.yaml
```




::page{title="Summary"}

### Nice Work!

You have used MicroProfile Health and Open Liberty to create endpoints that report on your microservice's status. Then, you observed how Kubernetes uses the **/health/started**, **/health/live**, and **/health/ready** endpoints to keep your microservices running smoothly.




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-kubernetes-microprofile-health*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-kubernetes-microprofile-health
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Checking%20the%20health%20of%20microservices%20on%20Kubernetes&guide-id=cloud-hosted-guide-kubernetes-microprofile-health)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-kubernetes-microprofile-health/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-kubernetes-microprofile-health/pulls)



### Where to next?

* [Adding health reports to microservices](https://openliberty.io/guides/microprofile-health.html)
* [Deploying microservices to Kubernetes](https://openliberty.io/guides/kubernetes-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
