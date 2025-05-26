---
markdown-version: v1
tool-type: theia
---
::page{title="Welcome to the Deploying microservices to an OpenShift cluster using OpenShift Local guide!"}

Explore how to deploy microservices to a local OpenShift cluster running with OpenShift Local (formerly known as CodeReady Containers)

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.





::page{title="What you'll learn "}

You'll learn how to deploy two microservices in Open Liberty containers to an OpenShift 4 cluster that is running locally on your computer by using OpenShift Local. To learn how to deploy to an OpenShift 4 cluster by using operators, see the [Deploying microservices to OpenShift by using Kubernetes Operators](https://openliberty.io/guides/cloud-openshift-operator.html) guide. 

Different cloud-based solutions are available for running your Kubernetes workloads. With a cloud-based infrastructure, you can focus on developing your microservices without worrying about low-level infrastructure details for deployment. By using the cloud, you can easily scale and manage your microservices in a high-availability setup.

Kubernetes is an open source container orchestrator that automates many tasks that are involved in deploying, managing, and scaling containerized applications. To learn more about Kubernetes, check out the [Deploying microservices to Kubernetes](https://openliberty.io/guides/kubernetes-intro.html) guide.

[Red Hat OpenShift Local](https://access.redhat.com/documentation/en-us/red_hat_openshift_local/2.23/html/getting_started_guide/index) is a tool that you can use to quickly build and run a minimal OpenShift 4 cluster on your local computer. OpenShift Local simplifies setup and testing while providing all of tools that are needed to develop container-based applications. 

The two microservices that you'll deploy are called ***system*** and ***inventory***. The ***system*** microservice returns the JVM system properties of the running container. It also returns the pod name in the HTTP header, which makes pod replicas more distinguishable from each other. The ***inventory*** microservice adds the properties from the ***system*** microservice to the inventory. This process demonstrates how communication can be established between pods inside a cluster.


::page{title="Getting started"}

To open a new command-line session,
select ***Terminal*** > ***New Terminal*** from the menu of the IDE.

Run the following command to navigate to the ***/home/project*** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-openshift-codeready-containers.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-openshift-codeready-containers.git
cd guide-openshift-codeready-containers
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.


::page{title="Starting OpenShift Local"}

### Setting up OpenShift Local

Run the following command to set up your host machine for OpenShift Local:

```bash
crc setup
```

### Starting the virtual machine

Next, run the following command to start the OpenShift Local virtual machine and OpenShift cluster:

```bash
crc start
```

Supply your user pull secret at the prompt. You can find the pull secret  on the page where you downloaded the https://cloud.redhat.com/openshift/create/local[latest release of OpenShift Local].

If the cluster starts successfully, you can see output similar to the following example:

```
Started the OpenShift cluster.

The server is accessible via web console at:
  https://console-openshift-console.apps-crc.testing

Log in as administrator:
  Username: kubeadmin
  Password: jPvDv-jgRZB-qhYP4-Hmkmj

Log in as user:
  Username: developer
  Password: developer
```

Save this output as it might be required later in this guide. 


### Logging in to the cluster 

To interact with the OpenShift cluster, you need to use the ***oc*** commands. To build containers, you need a containerization software such as podman. OpenShift Local already includes the ***oc*** and ***podman*** binary. To use the ***oc*** and ***podman*** commands, run the following command to see instructions on how to configure your PATH:

```bash
crc podman-env
```

The resulting output differs based on your OS and environment, but the output is similar to the following example:

```
export PATH="/Users/developer/.bin/oc:$PATH"
export CONTAINER_SSHKEY="/Users/developer/.crc/machines/crc/id_ecdsa"
export CONTAINER_HOST="ssh://core@127.0.0.1:2222/run/user/1000/podman/podman.sock"
export DOCKER_HOST="unix:///Users/developer/.crc/machines/crc/docker.sock"
::page{title="Run this command to configure your shell session:"}
::page{title="eval $(crc podman-env)"}
```

Run the command that is specified in the output to configure your shell session:

```bash
eval $(crc podman-env)
alias podman=podman-remote
```

```bash
cd start
./mvnw package
```

Next, run the ***podman build*** commands to build container images for your application:
```bash
podman build -t system:1.0-SNAPSHOT system/.
podman build -t inventory:1.0-SNAPSHOT inventory/.
```

During the build, you see various Docker messages that describe what images are being downloaded and built. When the build finishes, run the following command to list all local Docker images:

```bash
podman images
```

Verify that the ***system:1.0-SNAPSHOT*** and ***inventory:1.0-SNAPSHOT*** images are listed among them, for example:

```
REPOSITORY                     TAG
localhost/system               1.0-SNAPSHOT
localhost/inventory            1.0-SNAPSHOT
icr.io/appcafe/open-liberty    kernel-slim-java11-openj9-ubi
```

If you don't see the ***system:1.0-SNAPSHOT*** and ***inventory:1.0-SNAPSHOT*** images, check the Maven build log for any potential errors.


### Pushing the images to OpenShift's internal registry

In order to run the microservices on the cluster, you need to push the microservice images to a container image registry. You'll use OpenShift Container Registry (OCR), which is the OpenShift integrated container image registry. After your images are pushed to the registry, you can use them in the pods that you create later in the guide.

Run the following command to login your OCR.:

```bash
oc registry login --insecure=true
```


You can view the registry address by running the following command:

```bash
oc registry info
```

The output is similar to the following:

```
default-route-openshift-image-registry.apps-crc.testing
```

Ensure that you're logged in to OpenShift and the registry, and run the following commands to tag your applications:


```bash
podman tag system:1.0-SNAPSHOT $(oc registry info)/$(oc project -q)/system:1.0-SNAPSHOT
podman tag inventory:1.0-SNAPSHOT $(oc registry info)/$(oc project -q)/inventory:1.0-SNAPSHOT
```

```bash
./mvnw verify \ 
-Dsystem.ip=system-route-my-project.apps-crc.testing \
-Dinventory.ip=inventory-route-my-project.apps-crc.testing
```

* Replace the ***system.ip*** parameter with the appropriate hostname to access your system microservice.
* Replace the ***inventory.ip*** parameter with the appropriate hostname to access your inventory microservice.

If the tests pass, you see an output for each service similar to the following examples:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.673 sec - in it.io.openliberty.guides.system.SystemEndpointIT

Results:

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.222 sec - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results:

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```


::page{title="Tearing down the environment"}

When you no longer need your deployed microservices, you can delete the Kubernetes deployments, services, and routes by running the following command:

```bash
oc delete -f kubernetes.yaml
```

To delete the pushed images, run the following commands:

```bash
oc delete imagestream/inventory
oc delete imagestream/system
```

Next, you can delete the project by running the following command:

```bash
oc delete project my-project
```

Finally, you can stop and delete the OpenShift Local virtual machine by running the following commands:

```bash
crc stop
crc delete
```


::page{title="Summary"}

### Nice Work!

You just deployed two microservices running in Open Liberty to an OpenShift cluster by using OpenShift Local. You also learned how to use **oc** to deploy your microservices on a Kubernetes cluster.




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-openshift-codeready-containers*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-openshift-codeready-containers
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Deploying%20microservices%20to%20an%20OpenShift%20cluster%20using%20OpenShift%20Local&guide-id=cloud-hosted-guide-openshift-codeready-containers)

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-openshift-codeready-containers/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-openshift-codeready-containers/pulls)



### Where to next?

* [Deploying microservices to Kubernetes](https://openliberty.io/guides/kubernetes-intro.html)
* [Configuring microservices running in Kubernetes](https://openliberty.io/guides/kubernetes-microprofile-config.html)
* [Checking the health of microservices on Kubernetes](https://openliberty.io/guides/kubernetes-microprofile-health.html)
* [Managing microservice traffic using Istio](https://openliberty.io/guides/istio-intro.html)
* [Deploying microservices to OpenShift 4 using Kubernetes Operators](https://openliberty.io/guides/cloud-openshift-operator.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
