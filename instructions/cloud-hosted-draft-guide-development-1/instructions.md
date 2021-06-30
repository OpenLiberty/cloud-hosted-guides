
# Welcome to the Deploying microservices to Kubernetes guide!

Deploy microservices in Open Liberty Docker containers to Kubernetes and manage them with the Kubernetes CLI, kubectl.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.





# What is Kubernetes?

Kubernetes is an open source container orchestrator that automates many tasks involved in deploying,
managing, and scaling containerized applications.

Over the years, Kubernetes has become a major tool in containerized environments as containers are being
further leveraged for all steps of a continuous delivery pipeline.

### Why use Kubernetes?

Managing individual containers can be challenging. 
A small team can easily manage a few containers for development but 
managing hundreds of containers can be a headache, even for a large team of experienced developers.
Kubernetes is a tool for deployment in containerized environments. 
It handles scheduling, deployment, as well as mass deletion and creation of containers. 
It provides update rollout abilities on a large scale that would otherwise prove extremely tedious to do. 
Imagine that you updated a Docker image, which now needs to propagate to a dozen containers. 
While you could destroy and then re-create these containers, you can also run a short one-line
command to have Kubernetes make all those updates for you. Of course, this is just a simple example.
Kubernetes has a lot more to offer.

### Architecture

Deploying an application to Kubernetes means deploying an application to a Kubernetes cluster.

A typical Kubernetes cluster is a collection of physical or virtual machines called nodes that run
containerized applications. A cluster is made up of one master node that manages the cluster, and
many worker nodes that run the actual application instances inside Kubernetes objects called pods.

A pod is a basic building block in a Kubernetes cluster. It represents a single running process that
encapsulates a container or in some scenarios many closely coupled containers. Pods can be
replicated to scale applications and handle more traffic. From the perspective of a cluster, a set
of replicated pods is still one application instance, although it might be made up of dozens of
instances of itself. A single pod or a group of replicated pods are managed by Kubernetes objects
called controllers. A controller handles replication, self-healing, rollout of updates, and general
management of pods. One example of a controller that you will use in this guide is a deployment.

A pod or a group of replicated pods are abstracted through Kubernetes objects called services
that define a set of rules by which the pods can be accessed. In a basic scenario, a Kubernetes
service exposes a node port that can be used together with the cluster IP address to access
the pods encapsulated by the service.

To learn about the various Kubernetes resources that you can configure, 
see the [official Kubernetes documentation](https://kubernetes.io/docs/concepts/).


# What you'll learn

You will learn how to deploy two microservices in Open Liberty containers to a local Kubernetes cluster.
You will then manage your deployed microservices using the **kubectl** command line interface for Kubernetes. 
The **kubectl** CLI is your primary tool for communicating with and managing your Kubernetes cluster.

The two microservices you will deploy are called **system** and **inventory**. The **system** microservice
returns the JVM system properties of the running container and it returns the pod's name in the HTTP header
making replicas easy to distinguish from each other. The **inventory** microservice
adds the properties from the **system** microservice to the inventory. 
This process demonstrates how communication can be established between pods inside a cluster.

You will use a local single-node Kubernetes cluster.


# Getting started

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-kubernetes-intro.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-kubernetes-intro.git
cd guide-kubernetes-intro
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

Run the following command to store the namespace name in a variable.

```
NAMESPACE_NAME=`bx cr namespace-list | grep sn-labs- | sed 's/ //g'`
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



# Building and containerizing the microservices

The first step of deploying to Kubernetes is to build your microservices and containerize them with Docker.

The starting Java project, which you can find in the **start** directory, is a multi-module Maven
project that's made up of the **system** and **inventory** microservices. 
Each microservice resides in its own directory, **start/system** and **start/inventory**. 
Each of these directories also contains a Dockerfile, which is necessary for building Docker images. 
If you're unfamiliar with Dockerfiles, check out the
[Containerizing Microservices](https://openliberty.io/guides/containerize.html) guide,
which covers Dockerfiles in depth.

Navigate to the **start** directory and build the applications by running the following commands:
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

During the build, you'll see various Docker messages describing what images are being downloaded and built. 
When the build finishes, run the following command to list all local Docker images:
```
docker images
```
{: codeblock}



Verify that the `system:1.0-SNAPSHOT` and `inventory:1.0-SNAPSHOT` images are listed among them, for example:

```
REPOSITORY                                TAG                       
inventory                                 1.0-SNAPSHOT
system                                    1.0-SNAPSHOT
openliberty/open-liberty                  full-java11-openj9-ubi
```

If you don't see the `system:1.0-SNAPSHOT` and `inventory:1.0-SNAPSHOT` images, then check the Maven
build log for any potential errors.
If the images built without errors, push them to your container registry on IBM Cloud with the following commands:

```
docker tag inventory:1.0-SNAPSHOT us.icr.io/$NAMESPACE_NAME/inventory:1.0-SNAPSHOT
docker tag system:1.0-SNAPSHOT us.icr.io/$NAMESPACE_NAME/system:1.0-SNAPSHOT
docker push us.icr.io/$NAMESPACE_NAME/inventory:1.0-SNAPSHOT
docker push us.icr.io/$NAMESPACE_NAME/system:1.0-SNAPSHOT
```
{: codeblock}


# Deploying the microservices

Now that your Docker images are built, deploy them using a Kubernetes resource definition.

A Kubernetes resource definition is a yaml file that contains a description of all your deployments, services, 
or any other resources that you want to deploy. 
All resources can also be deleted from the cluster by using the same yaml file that you used to deploy them.

Create the Kubernetes configuration file.

> Run the following touch command in your terminal
```
touch /home/project/guide-kubernetes-intro/start/kubernetes.yaml
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-kubernetes-intro/start/kubernetes.yaml




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
        image: system:1.0-SNAPSHOT
        ports:
        - containerPort: 9080
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



This file defines four Kubernetes resources. It defines two deployments and two services. 
A Kubernetes deployment is a resource that controls the creation and management of pods. 
A service exposes your deployment so that you can make requests to your containers. 
Three key items to look at when creating the deployments are the **labels**, 
**image**, and **containerPort** fields. 
The **labels** is a way for a Kubernetes service to reference specific deployments. 
The **image** is the name and tag of the Docker image that you want to use for this container. 
Finally, the **containerPort** is the port that your container exposes to access your application.
For the services, the key point to understand is that they expose your deployments.
The binding between deployments and services is specified by labels -- in this case the 
**app** label.
You will also notice the service has a type of **NodePort**.
This means you can access these services from outside of your cluster via a specific port.
In this case, the ports are **31000** and **32000**, but port numbers can also be randomized if the
**nodePort** field is not used.

Update the image names so that the images in your IBM Cloud container registry are used,
and remove the `nodePort` fields so that the ports can be generated automatically:

```
sed -i 's=system:1.0-SNAPSHOT=us.icr.io/'"$NAMESPACE_NAME"'/system:1.0-SNAPSHOT=g' kubernetes.yaml
sed -i 's=inventory:1.0-SNAPSHOT=us.icr.io/'"$NAMESPACE_NAME"'/inventory:1.0-SNAPSHOT=g' kubernetes.yaml
sed -i 's=nodePort: 31000==g' kubernetes.yaml
sed -i 's=nodePort: 32000==g' kubernetes.yaml
```
{: codeblock}

Run the following commands to deploy the resources as defined in kubernetes.yaml:
```
kubectl apply -f kubernetes.yaml
```
{: codeblock}


When the apps are deployed, run the following command to check the status of your pods:
```
kubectl get pods
```
{: codeblock}


You'll see an output similar to the following if all the pods are healthy and running:

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


You can also issue the **kubectl get** and **kubectl describe** commands on other Kubernetes resources, so feel
free to inspect all other resources.


In this execise, access services by using Kubernetes API.
Run the following command to start a proxy to the Kubernetes API server:

```
kubectl proxy
```
{: codeblock}

Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.
Run the following commands to store the proxy path of the **system-service** and **inventory-service** services.
```
NAMESPACE_NAME=`bx cr namespace-list | grep sn-labs- | sed 's/ //g'`
SYSTEM_PROXY=localhost:8001/api/v1/namespaces/$NAMESPACE_NAME/services/system-service/proxy
INVENTORY_PROXY=localhost:8001/api/v1/namespaces/$NAMESPACE_NAME/services/inventory-service/proxy
```
{: codeblock}

Run the following echo commands to verify the variables:

```
echo $SYSTEM_PROXY
echo $INVENTORY_PROXY
```
{: codeblock}


The output appears similar to the following:

```
localhost:8001/api/v1/namespaces/sn-labs-yourname/services/system-service/proxy
localhost:8001/api/v1/namespaces/sn-labs-yourname/services/inventory-service/proxy
```

Then use the following `curl` commands to access your microservices:

```
curl http://$SYSTEM_PROXY/system/properties | jq
```
{: codeblock}

```
curl http://$INVENTORY_PROXY/inventory/systems/system-service | jq
```
{: codeblock}

The first URL returns system properties and the name of the pod in an HTTP header called `X-Pod-Name`.
To view the header, you can use the `-I` option in the `curl` command when you make a request to the
`http://$SYSTEM_PROXY/system/properties` URL.
The second URL adds properties from `system-service` to the inventory Kubernetes Service. 
Making a request to the `http://$INVENTORY_PROXY/inventory/systems/[kube-service]` URL in general 
adds to the inventory depending on whether `kube-service` is a valid Kubernetes service that can be accessed.


# Scaling a deployment

To use load balancing, you need to scale your deployments. 
When you scale a deployment, you replicate its pods, creating more running instances of your applications. 
Scaling is one of the primary advantages of Kubernetes because you can replicate your application to accommodate more traffic, 
and then descale your deployments to free up resources when the traffic decreases.

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


Wait for your two new pods to be in the ready state, then make the following `curl` command:

```
curl -I http://$SYSTEM_PROXY/system/properties | jq
```
{: codeblock}

Notice that the **X-Pod-Name** header has a different value when you call it multiple times.
The value changes because three pods that all serve the **system** application are now running.
Similarly, to descale your deployments you can use the same scale command with fewer replicas.

```
kubectl scale deployment/system-deployment --replicas=1
```
{: codeblock}


# Redeploy microservices

When you're building your application, you might want to quickly test a change. 
To run a quick test, you can rebuild your Docker images then delete and re-create your Kubernetes resources. 
Note that there is only one **system** pod after you redeploy since you're deleting all of the existing pods.


```
kubectl delete -f kubernetes.yaml

mvn clean package
docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
docker tag inventory:1.0-SNAPSHOT us.icr.io/$NAMESPACE_NAME/inventory:1.0-SNAPSHOT
docker tag system:1.0-SNAPSHOT us.icr.io/$NAMESPACE_NAME/system:1.0-SNAPSHOT
docker push us.icr.io/$NAMESPACE_NAME/inventory:1.0-SNAPSHOT
docker push us.icr.io/$NAMESPACE_NAME/system:1.0-SNAPSHOT

kubectl apply -f kubernetes.yaml
```
{: codeblock}

Updating your applications in this way is fine for development environments, but it is not suitable for production.
If you want to deploy an updated image to a production cluster, 
you can update the container in your deployment with a new image. 
Once the new container is ready, Kubernetes automates both the creation of a new container and the decommissioning of the old one.


# Testing microservices that are running on Kubernetes

A few tests are included for you to test the basic functionality of the microservices. 
If a test failure occurs, then you might have introduced a bug into the code. 
To run the tests, wait for all pods to be in the ready state before proceeding further. 
The default properties defined in the **pom.xml** are:

| *Property*                        | *Description*
| ---| ---
| **system.kube.service**       | Name of the Kubernetes Service wrapping the **system** pods, **system-service** by default.
| **system.service.root**       | The Kubernetes Service **system-service** root path, **localhost:31000** by default.
| **inventory.service.root** | The Kubernetes Service **inventory-service** root path, **localhost:32000** by default.

Navigate back to the **start** directory.


Update the `pom.xml` files so that the `system.service.root` and `inventory.service.root` properties
match the values to access the `system-service` and `inventory-service` services.

```
sed -i 's=localhost:31000='"$SYSTEM_PROXY"'=g' inventory/pom.xml
sed -i 's=localhost:32000='"$INVENTORY_PROXY"'=g' inventory/pom.xml
sed -i 's=localhost:31000='"$SYSTEM_PROXY"'=g' system/pom.xml
```
{: codeblock}

Run the integration tests by using the following command:

```
mvn failsafe:integration-test
```
{: codeblock}

If the tests pass, you'll see an output similar to the following for each service respectively:

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


# Tearing down the environment

When you no longer need your deployed microservices, 
you can delete all Kubernetes resources by running the **kubectl delete** command:
```
kubectl delete -f kubernetes.yaml
```
{: codeblock}




# Summary

## Nice Work!

You have just deployed two microservices that are running in Open Liberty to Kubernetes.

You then scaled a microservice and ran integration tests against miroservices that are running in a Kubernetes cluster.




## Clean up your environment

Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-kubernetes-intro** project by running the following commands:

```
cd /home/project
rm -fr guide-kubernetes-intro
```
{: codeblock}

## What did you think of this guide?
We want to hear from you. To provide feedback on your experience with this guide, click the **Support/Feedback** button in the IDE,
select **Give feedback** option, fill in the fields, choose **General** category, and click the **Post Idea** button.

## What could make this guide better?
You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback](https://github.com/OpenLiberty/guide-kubernetes-intro/issues)
* [Create a pull request to contribute to this guide](https://github.com/OpenLiberty/guide-kubernetes-intro/pulls)




## Where to next? 

* [Using Docker containers to develop microservices](https://openliberty.io/guides/docker.html)
* [Managing microservice traffic using Istio](https://openliberty.io/guides/istio-intro.html)


## Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.