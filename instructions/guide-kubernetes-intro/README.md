# Deploying microservices to Kubernetes

## What is kubernetes?

Kubernetes is an open source container orchestrator that automates many tasks involved in deploying, managing, and scaling containerized applications.

Over the years, Kubernetes has become a major tool in containerized environments as containers are being further leveraged for all steps of a continuous delivery pipeline.

### Why use Kubernetes

Managing individual containers can be challenging. A few containers used for development by a small team might not pose a problem, but managing hundreds of containers can give even a large team of experienced developers a headache. Kubernetes is a primary tool for deployment in containerized environments. It handles scheduling, deployment, as well as mass deletion and creation of containers. It provides update rollout abilities on a large scale that would otherwise prove extremely tedious to do. Imagine that you updated a Docker image, which now needs to propagate to a dozen containers. While you could destroy and then re-create these containers, you can also run a short one-line command to have Kubernetes make all those updates for you. Of course, this is just a simple example. Kubernetes has a lot more to offer.

### Architecture

Deploying an application to Kubernetes means deploying an application to a Kubernetes cluster.

A typical Kubernetes cluster is a collection of physical or virtual machines called nodes that run containerized applications. A cluster is made up of one master node that manages the cluster, and many worker nodes that run the actual application instances inside Kubernetes objects called pods.

A pod is a basic building block in a Kubernetes cluster. It represents a single running process that encapsulates a container or in some scenarios many closely coupled containers. Pods can be replicated to scale applications and handle more traffic. From the perspective of a cluster, a set of replicated pods is still one application instance, although it might be made up of dozens of instances of itself. A single pod or a group of replicated pods are managed by Kubernetes objects called controllers. A controller handles replication, self-healing, rollout of updates, and general management of pods. One example of a controller that you will use in this guide is a deployment.

A pod or a group of replicated pods are abstracted through Kubernetes objects called services that define a set of rules by which the pods can be accessed. In a basic scenario, a Kubernetes service exposes a node port that can be used together with the cluster IP address to access the pods encapsulated by the service.

To learn about the various Kubernetes resources that you can configure, see the [https://kubernetes.io/docs/concepts/](official Kubernetes documentation).

## What you'll learn

You will learn how to deploy two microservices in Open Liberty containers to a local Kubernetes cluster. You will then manage your deployed microservices using the **kubectl** command line interface for Kubernetes. The **kubectl** CLI is your primary tool for communicating with and managing your Kubernetes cluster.

The two microservices you will deploy are called **system** and **inventory**. The **system** microservice returns the JVM system properties of the running container and it returns the pod's name in the HTTP header making replicas easy to distinguish from each other. The **inventor** microservice adds the properties from the **system** microservice to the inventory. This demonstrates how communication can be established between pods inside a cluster.

You will use a local single-node kubernetes cluster.

## Getting Started

If a terminal window does not open navigate:

> Terminal -> New Terminal

Check you are in the **home/project** folder:

```
pwd
```
{: codeblock}

The fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-kubernetes-intro.git
cd guide-kubernetes-intro
```
{: codeblock}

The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.

# Building and containerizing the microservices

The first step of deploying to Kubernetes is to build your microservices and containerize them with Docker.

The starting Java project, which you can find in the **start** directory, is a multi-module Maven project that’s made up of the **system** and **inventory** microservices. Each microservice resides in its own directory, **start/system** and **start/inventory**. Each of these directories also contains a Dockerfile, which is necessary for building Docker images.

Navigate to the **start** directory and run the following command to build the applications:

```
cd start
mvn clean package
```
{: codeblock}

Next, run the docker build commands to build container images for your application:

```
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
```
{: codeblock}

The **-t** flag in the **docker build** command allows the Docker image to be labeled (tagged) in the **name[:tag]** format. The tag for an image describes the specific image version. If the optional **[:tag]** tag is not specified, the **latest** tag is created by default.

During the build, you’ll see various Docker messages describing what images are being downloaded and built. When the build finishes, run the following command to list all local Docker images:

```
docker images
```
{: codeblock}

Verify that the **system:1.0-SNAPSHOT** and **inventory:1.0-SNAPSHOT** images are listed among them, for example:

```
REPOSITORY                                       TAG
inventory                                        1.0-SNAPSHOT
system                                           1.0-SNAPSHOT
open-liberty                                     latest
```

If you don’t see the **system:1.0-SNAPSHOT** and **inventory:1.0-SNAPSHOT** images, then check the Maven build log for any potential errors. 

You now need to push these images to your givern container registry in **ibmcloud** so you can deploy these Kubernetes. Find the name of your namespace with the following command: 

```
bx cr namespace-list
```
{: codeblock}

You'll see an output similar to the following: 

```
Listing namespaces for account 'QuickLabs - IBM Skills Network' in registry 'us.icr.io'...

Namespace   
sn-labs-johndoe
```

In this case, the namespace is **sn-labs-johndoe**. Store your namespace in a variable, e.g. NAMESPACE_NAME=sn-labs-johndoe: 

```
NAMESPACE_NAME=
```
{: codeblock}

Verify that the variable contains your namepsace name correctly: 

```
echo $NAMESPACE_NAME
```
{: codeblock}

Login to the given registry with the following command:

```
bx cr login
```
{: codeblock}

Run the following commands to push the inventory images to your container registry namespace: 

```
docker tag inventory:1.0-SNAPSHOT us.icr.io/$NAMESPACE_NAME/inventory:1.0-SNAPSHOT
docker push us.icr.io/$NAMESPACE_NAME/inventory:1.0-SNAPSHOT
```
{: codeblock}

Do the same for the system images: 

```
docker tag system:1.0-SNAPSHOT us.icr.io/$NAMESPACE_NAME/system:1.0-SNAPSHOT
docker push us.icr.io/$NAMESPACE_NAME/system:1.0-SNAPSHOT
```
{: codeblock}

# Deploying the microservices

Now that your Docker images are built, deploy them using a Kubernetes resource definition.

A Kubernetes resource definition is a yaml file that contains a description of all your deployments, services, or any other resources that you want to deploy. All resources can also be deleted from the cluster by using the same yaml file that you used to deploy them.

Create the Kubernetes configuration file:

```
touch kubernetes.yaml
```
{: codeblock}


> [File -> Open] guide-kubernetes-intro/start/kubernetes.yaml

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
      - name: system-container
        image: us.icr.io/$NAMESPACE_NAME/system:1.0-SNAPSHOT
        resources:
          requests:
            ephemeral-storage: "1Gi"
          limits:
            ephemeral-storage: "1Gi"
        ports:
        - containerPort: 9080
      imagePullSecrets:
      - name: icr
      
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
        image: us.icr.io/$NAMESPACE_NAME/inventory:1.0-SNAPSHOT
        resources:
          requests:
            ephemeral-storage: "1Gi"
          limits:
            ephemeral-storage: "1Gi"
        ports:
        - containerPort: 9080
      imagePullSecrets:
      - name: icr
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
```
{: codeblock}

This file defines four Kubernetes resources. It defines two deployments and two services. A Kubernetes deployment is a resource responsible for controlling the creation and management of pods. A service exposes your deployment so that you can make requests to your containers. Three key items to look at when creating the deployments are the **labels**, **image**, and **containerPort** fields. The **labels** is a way for a Kubernetes service to reference specific deployments. The **image** is the name and tag of the Docker image that you want to use for this container. Finally, the **containerPort** is the port that your container exposes for purposes of accessing your application. For the services, the key point to understand is that they expose your deployments. The binding between deployments and services is specified by the use of labels — in this case the **app** label. You will also notice the service has a type of **NodePort**. This means you can access these services from outside of your cluster via a specific port.

Correct the image names so they are pulled from your namespace using the following command: 

```
sed -i 's=$NAMESPACE_NAME='"$NAMESPACE_NAME"'=g'  kubernetes.yaml
```
{: codeblock}

Run the following command to deploy the resources as defined in kubernetes.yaml:

```
kubectl apply -f kubernetes.yaml
```
{: codeblock}

When the apps are deployed, run the following command to check the status of your pods:

```
kubectl get pods
```
{: codeblock}

You’ll see an output similar to the following if all the pods are healthy and running:

```
NAME                                    READY     STATUS    RESTARTS   AGE
system-deployment-6bd97d9bf6-4ccds      1/1       Running   0          15s
inventory-deployment-645767664f-nbtd9   1/1       Running   0          15s
```

You can also inspect individual pods in more detail by running the following command:

```
kubectl describe pods
```
{: codeblock}

This output shows the node IP which you will use to access the services. For example:

```
Name:         system-deployment-c5667f8dc-tpnff
Namespace:    sn-labs-johndoe
Priority:     0
Node:         10.114.85.172/10.114.85.172
Start Time:   Fri, 24 Apr 2020 07:45:43 +0000
Labels:       app=system
              pod-template-hash=c5667f8dc
Annotations:  kubernetes.io/limit-ranger:
                LimitRanger plugin set: cpu, ephemeral-storage, memory request for container system-container; cpu, ephemeral-storage, memory limit for co...
              kubernetes.io/psp: ibm-privileged-psp
Status:       Running
```

In this case, the node IP is 10.114.85.172 for the system service. Find the IP for each service and store them in variables, e.g. **SYSTEM_HOST=10.114.85.172**: 

```
SYSTEM_HOST=
```
{: codeblock}

```
INVENTORY_HOST=
```
{: codeblock}

Verify that they have stored correctly with the following command: 

```
echo $SYSTEM_HOST && echo $INVENTORY_HOST
```
{: codeblock}

To find the node ports that you need to access your services, run the following command: 

```
kubectl get services
```
{: codeblock}

You will see an output similar to the following:

```
NAME                TYPE       CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
inventory-service   NodePort   172.21.208.199   <none>        9080:30065/TCP   24h
system-service      NodePort   172.21.3.206     <none>        9080:32554/TCP   24h
```

You will see the node port listed under the heading "PORT(S)", next to "9080:". In this case, the node port for the **inventory-service** is 30065 and the node port for the **system-service** is 32554. Store these in variables, e.g. **INVENTORY_PORT=30065**:

```
INVENTORY_PORT=
```
{: codeblock}

```
SYSTEM_PORT=
```
{: codeblock}

Verify that they have stored correctly with the following command: 

```
echo $SYSTEM_PORT && echo $INVENTORY_PORT
```
{: codeblock}

You can also issue the **kubectl get** and **kubectl describe** commands on other Kubernetes resources, so feel free to inspect all other resources.

Next you will make requests to your services.

Then curl or visit the following URLs to access your microservices, substituting the appropriate host name:

```
curl http://$SYSTEM_HOST:$SYSTEM_PORT/system/properties
```
{: codeblock}

```
curl http://$INVENTORY_HOST:$INVENTORY_PORT/inventory/systems/system-service
```
{: codeblock}

The first URL returns system properties and the name of the pod in an HTTP header called X-Pod-Name. To view the header, you may use the -I option in the curl when making a request to **http://$SYSTEM_HOST:$SYSTEM_PORT/system/properties**. The second URL adds properties from system-service to the inventory Kubernetes Service. Visiting **http://$INVENTORY_HOST:$INVENTORY_PORT/inventory/systems/** in general adds to the inventory depending on whether kube-service is a valid Kubernetes Service that can be accessed.

## Scaling a deployment

We can consider doing that. This post is particularly long and detailed (it surprised me how much info people had given). In general, they shouldn’t be this long but we can look at breaking them out maybe.

As an example, scale the **system** deployment to three pods by running the following command:

```
kubectl scale deployment/system-deployment --replicas=3
```
{: codeblock}

Use the following command to verify that two new pods have been created.

```
kubectl get pods
```
{: codeblock}

```
NAME                                    READY     STATUS    RESTARTS   AGE
system-deployment-6bd97d9bf6-4ccds      1/1       Running   0          1m
system-deployment-6bd97d9bf6-jf9rs      1/1       Running   0          25s
system-deployment-6bd97d9bf6-x4zth      1/1       Running   0          25s
inventory-deployment-645767664f-nbtd9   1/1       Running   0          1m
```

Wait for your two new pods to be in the ready state, then enter:

```
curl http://$SYSTEM_HOST:$SYSTEM_PORT/system/properties
```
{: codeblock}

You’ll notice that the X-Pod-Name header will have a different value when you call it multiple times. This is because there are now three pods running all serving the **system** application. Similarly, to descale your deployments you can use the same scale command with fewer replicas.

This is not how you would want to update your applications when running in production, but in a development environment this is fine. If you want to deploy an updated image to a production cluster, you can update the container in your deployment with a new image. Then, Kubernetes will automate the creation of a new container and decommissioning of the old one once the new container is ready.

## Testing microservices that are running on Kubernetes

A few tests are included for you to test the basic functionality of the microservices. If a test failure occurs, then you might have introduced a bug into the code. To run the tests, wait for all pods to be in the ready state before proceeding further. The default properties defined in the **pom.xml** are:

* **cluster.ip**: IP or host name for your cluster, localhost by default, which is appropriate when using Docker Desktop.

* **system.kube.service**: Name of the Kubernetes Service wrapping the system pods, system-service by default.

* **system.node.port**: The NodePort of the Kubernetes Service system-service, 31000 by default.

* **inventory.node.port**: The NodePort of the Kubernetes Service inventory-service, 32000 by default

Navigate back to the **start** directory.

Run the following commands to replace the above variables in your pom.xml's that you are using to build & test this application.

```
sed -i 's=31000='"$SYSTEM_PORT"'=g'  system/pom.xml
sed -i 's=31000='"$SYSTEM_PORT"'=g'  inventory/pom.xml 
sed -i 's=32000='"$INVENTORY_PORT"'=g'  inventory/pom.xml 
sed -i 's=localhost='"$INVENTORY_HOST"'=g'  inventory/pom.xml
sed -i 's=localhost='"$SYSTEM_HOST"'=g'  system/pom.xml
```
{: codeblock}

Run the integration tests against a cluster:

```
mvn failsafe:integration-test
```
{: codeblock}

If the tests pass, you’ll see an output similar to the following for each service respectively:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.372 s - in it.io.openliberty.guides.system.SystemEndpointIT

Results:

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.714 s - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results:

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## Tearing down the environment

When you no longer need your deployed microservices, you can delete all Kubernetes resources by running the **kubectl delete** command:

```
kubectl delete -f kubernetes.yaml
```
{: codeblock}

Delete the guide-docker project by navigating to the /home/project/ directory

```
rm -r -f guide-kubernetes-intro
```
{: codeblock}


# Summary

## Well Done

Nice work! You have just deployed two microservices running in Open Liberty to Kubernetes. You then scaled a microservice and ran integration tests against miroservices that are running in a Kubernetes cluster.

Please log out in the top right so that your environment is cleaned up and ready for the next lab!
