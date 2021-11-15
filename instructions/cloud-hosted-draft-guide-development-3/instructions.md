
# **Welcome to the Deploying a microservice to OpenShift by using a Kubernetes Operator guide!**

Explore how to deploy a microservice to Red Hat OpenShift by using a Kubernetes Operator.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



# **What you'll learn**

You will learn how to deploy a cloud-native application with a microservice to Red Hat OpenShift 4 by using the Open Liberty Kubernetes Operator. 
You will install an operator into an OpenShift cluster and use them to deploy and scale a sample microservice. 

[OpenShift](https://www.openshift.com/) is a Kubernetes-based platform with added functions. It streamlines the DevOps
process by providing an intuitive development pipeline. It also provides integration with multiple tools to make the
deployment and management of cloud applications easier.
You can learn more about Kubernetes by checking out the [Deploying microservices to Kubernetes](https://openliberty.io/guides/kubernetes-intro.html) guide.

[Kubernetes operators](https://kubernetes.io/docs/concepts/extend-kubernetes/operator/#operators-in-kubernetes)
provide an easy way to automate the management and updating of applications by abstracting away some of the details of cloud application management.
To learn more about operators, check out this [Operators tech topic article](https://www.openshift.com/learn/topics/operators). 

The application in this guide consists of one microservice, **system**. Every 15 seconds, the **system**
microservice calculates and publishes events that contain its current average system load.

You will deploy the Open Liberty microservice by using the Open Liberty Operator. 
The [Open Liberty Operator](https://github.com/OpenLiberty/open-liberty-operator) provides a method of packaging,
deploying, and managing Open Liberty applications on Kubernetes-based clusters. 
The Open Liberty Operator watches Open Liberty resources and creates various Kubernetes resources,
including **Deployments**, **Services**, and **Routes**, depending on the configurations. 
The Operator then continuously compares the current state of the resources, the desired state
of application deployment, and reconciles them when necessary.



# **Getting started**

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/draft-guide-openliberty-operator-intro.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/draft-guide-openliberty-operator-intro.git
cd draft-guide-openliberty-operator-intro
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.


# **Installing the Operator**


A project is created for you to use in this execise. Run the following command to see your project name:

```
oc projects
```
{: codeblock}

In this Skill Network enviornment, the Open Liberty Operator is already installed by the administrator.
If you like to learn how to install the Open Liberty Operator,
you can learn from the [Deploying microservices to OpenShift by using Kubernetes Operators](https://openliberty.io/guides/cloud-openshift-operator.html#installing-the-operators) guide or the Open Liberty Operator [document](https://github.com/OpenLiberty/open-liberty-operator/blob/master/deploy/releases/0.7.1/readme.adoc).

Run the following command to view all the supported API resources that are available through the Open Liberty Operator:

```
oc api-resources --api-group=openliberty.io
```
{: codeblock}


Look for the following output, which shows the [custom resource definitions](https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/) (CRDs) that can be used by the Open Liberty Operator:

```
NAME                      SHORTNAMES         APIGROUP         NAMESPACED   KIND
openlibertyapplications   olapp,olapps       openliberty.io   true         OpenLibertyApplication
openlibertydumps          oldump,oldumps     openliberty.io   true         OpenLibertyDump
openlibertytraces         oltrace,oltraces   openliberty.io   true         OpenLibertyTrace
```

Each CRD defines a kind of object that can be used, which is specified in the previous example by the **KIND** value.
The **SHORTNAME** value specifies alternative names that you can substitute in the configuration to refer to an object kind. 
For example, you can refer to the **OpenLibertyApplication** object kind by one of its specified shortnames, such as **olapps**. 

The **openlibertyapplications** CRD defines a set of configurations for
deploying an Open Liberty-based application, including the application image, number of instances, and storage settings.
The Open Liberty Operator watches for changes to instances of the **OpenLibertyApplication** object kind and 
creates Kubernetes resources that are based on the configuration that is defined in the CRD.


# **Deploying the system microservice to OpenShift**


To deploy the **system** microservice, you must first package the microservice, then create and
run an OpenShift build to produce runnable container images of the packaged microservice.

<br/>
### **Packaging the microservice**

Ensure that you are in the **start** directory and run the following command to package the **system**
microservice:


```
cd /home/project/draft-guide-openliberty-operator-intro/start
mvn clean package
```
{: codeblock}

<br/>
### **Building and pushing the images**

Create a build template to configure how to build your container images.

Create the **build.yaml** template file in the **start** directory.

> Run the following touch command in your terminal
```
touch /home/project/draft-guide-openliberty-operator-intro/start/build.yaml
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > draft-guide-openliberty-operator-intro/start/build.yaml




```
apiVersion: template.openshift.io/v1
kind: Template
metadata:
  name: "build-template"
  annotations:
    description: "Build template for the system service"
    tags: "build"
objects:
  - apiVersion: v1
    kind: ImageStream
    metadata:
      name: "system-imagestream"
      labels:
        name: "system"
  - apiVersion: v1
    kind: BuildConfig
    metadata:
      name: "system-buildconfig"
      labels:
        name: "system"
    spec:
      source:
        type: Binary
      strategy:
        type: Docker
      output:
        to:
          kind: ImageStreamTag
          name: "system-imagestream:1.0-SNAPSHOT"
```
{: codeblock}


The **build.yaml** template includes two objects. 
The **ImageStream** object provides an abstraction from the image in the image registry. 
This allows you to reference and tag the image. 
The image registry used is the integrated internal OpenShift Container Registry.

The **BuildConfig** object defines a single
build definition and any triggers that kickstart the build. The **source** spec defines the build input. In this case,
the build inputs are your **binary** (local) files, which are streamed to OpenShift for the build.
The uploaded files need to include the packaged **WAR** application binaries, which is why you needed to run the Maven commands. The template specifies
a **Docker** strategy build, which invokes the **docker build** command, and creates a runnable container image of the microservice
from the build input.

Run the following command to create the objects for the **system** microservice:

```
oc process -f build.yaml | oc create -f -
```
{: codeblock}


Next, run the following command to view the newly created **ImageStream** objects and the build configurations for the microservice:

```
oc get all -l name=system
```
{: codeblock}


Look for the following resources:

```
NAME                                                TYPE     FROM     LATEST
buildconfig.build.openshift.io/system-buildconfig   Docker   Binary   0

NAME                                                IMAGE REPOSITORY                                                                   TAGS           UPDATED
imagestream.image.openshift.io/system-imagestream   default-route-openshift-image-registry.apps-crc.testing/guide/system-imagestream
```   

Ensure that you are in the **start** directory and trigger the build by running the following command:

```
oc start-build system-buildconfig --from-dir=system/.
```
{: codeblock}


The local **system** directory is uploaded to OpenShift to be built into the Docker image. Run the
following command to list the build and track its status:

```
oc get builds
```
{: codeblock}


Look for the output that is similar to the following example:

```
NAME                    TYPE     FROM             STATUS     STARTED
system-buildconfig-1    Docker   Binary@f24cb58   Running    45 seconds ago
```

You may need to wait some time until the build is complete. To check whether the build is complete, run the following
command to view the build log until the **Push successful** message appears:

```
oc logs build/system-buildconfig-1
```
{: codeblock}


Run the following command to view the **ImageStream** object:

```
oc get imagestreams
```
{: codeblock}


Run the following command to get more details on the newly pushed image within the stream:

```
oc describe imagestream/system-imagestream
```
{: codeblock}


The following example shows part of the **system-imagestream** output:

```
Name:               system-imagestream
Namespace:          guide
Created:            2 minutes ago
Labels:             name=system
Annotations:        <none>
Image Repository:   default-route-openshift-image-registry.apps-crc.testing/guide/system-imagestream
Image Lookup:       local=false
Unique Images:      1
Tags:               1

...
```

Now you're ready to deploy the images.

<br/>
### **Deploying the images**

You can configure the specifics of the Open Liberty Operator-controlled deployment with a YAML configuration file.

Create the **deploy.yaml** configuration file in the **start** directory.

> Run the following touch command in your terminal
```
touch /home/project/draft-guide-openliberty-operator-intro/start/deploy.yaml
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > draft-guide-openliberty-operator-intro/start/deploy.yaml




```
apiVersion: openliberty.io/v1beta1
kind: OpenLibertyApplication
metadata:
  name: system
  labels:
    name: system
spec:
  applicationImage: guide/system-imagestream:1.0-SNAPSHOT
  service:
    port: 9080
  expose: true
  env:
    - name: WLP_LOGGING_MESSAGE_FORMAT
      value: "json"
    - name: WLP_LOGGING_MESSAGE_SOURCE
      value: "message,trace,accessLog,ffdc,audit"
```
{: codeblock}


The **deploy.yaml** file is configured to deploy one **OpenLibertyApplication**
resource, **system**, which is controlled by the Open Liberty Operator.

The **applicationImage** parameter defines what container image is deployed as part of the **OpenLibertyApplication** CRD. 
This parameter follows the **<project-name>/<image-stream-name>[:tag]** format.
The parameter can also point to an image hosted on an external registry, such as Docker Hub. 
The **system** microservice is configured to use the **image** created from the earlier build. 

One of the benefits of using **ImageStream** objects is that the operator redeploys the application when it detects a new image is pushed.
The **env** parameter is used to specify environment variables that are passed to the container at runtime.

Additionally, the microservice includes the **service** and **expose** parameters.
The **service.port** parameter specifies which port is exposed by the container,
allowing the microservice to be accessed from outside the container. To access the microservice from outside of the cluster,
it must be exposed by setting the **expose** parameter to **true**.
After you expose the microservice, the Operator automatically creates and configures routes for external access to your microservice.


Run the following commands to update the **applicationImage** and deploy the **system** microservice with the previously explained configuration:
```
PROJECT_NAME=`oc projects -q | grep sn-labs- | sed 's/ //g'`
sed -i 's=guide='"$PROJECT_NAME"'=g' deploy.yaml
oc apply -f deploy.yaml
```
{: codeblock}

Next, run the following command to view your newly created **OpenLibertyApplications** resources:

```
oc get OpenLibertyApplications
```
{: codeblock}


You can also replace **OpenLibertyApplications** with the shortname **olapps**.

Look for output that is similar to the following example:

```
NAME      IMAGE                                    EXPOSED   RECONCILED   AGE
system    guide/system-imagestream:1.0-SNAPSHOT    true      True         10s
```

A **RECONCILED** state value of **True** indicates that the operator was able to successfully process the **OpenLibertyApplications** instances. 
Run the following command to view details of your microservice:

```
oc describe olapps/system
```
{: codeblock}


This example shows part of the **olapps/system** output:

```
Name:         system
Namespace:    guide
Labels:       app.kubernetes.io/part-of=system
              name=system
Annotations:  <none>
API Version:  openliberty.io/v1beta1
Kind:         OpenLibertyApplication

...
```

# **Accessing the microservice**

To access the exposed **system** microservice, run the following command and make note of the **HOST**:

```
oc get routes
```
{: codeblock}


Look for an output that is similar to the following example:

```
NAME     HOST/PORT                                                     PATH   SERVICES   PORT       TERMINATION   WILDCARD
system   system-guide.2886795274-80-kota02.environments.katacoda.com          system     9080-tcp                 None
```


Visit the microservice by going to the following URL: 
**http://[HOST]/system/properties**

Make sure to substitute the appropriate **[HOST]** value.
For example, using the output from the command above, **system-guide.2886795274-80-kota02.environments.katacoda.com** is the **HOST**.
The following example shows this value substituted for **HOST** in the URL:
**http://system-guide.2886795274-80-kota02.environments.katacoda.com/system/properties**.

Or, you can run the following command to get the URL:
```
IFS=' ' read -r -a system_url <<< "`oc get routes | grep system`"
echo http://${system_url[1]}/system/properties
```
{: codeblock}

# **Tearing down the environment**


When you no longer need your deployed microservice, you can delete all resources by running the following commands:

```
oc delete -f deploy.yaml
oc delete imagestream.image.openshift.io/system-imagestream
oc delete bc system-buildconfig
```
{: codeblock}

# **Summary**

## **Nice Work!**

You just deployed a microservice running in Open Liberty to OpenShift by using the Open Liberty Operator.



<br/>
## **Clean up your environment**


Clean up your online environment so that it is ready to be used with the next guide:

Delete the **draft-guide-openliberty-operator-intro** project by running the following commands:

```
cd /home/project
rm -fr draft-guide-openliberty-operator-intro
```
{: codeblock}

<br/>
## **What did you think of this guide?**

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Deploying%20a%20microservice%20to%20OpenShift%20by%20using%20a%20Kubernetes%20Operator&guide-id=cloud-hosted-draft-guide-openliberty-operator-intro)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

<br/>
## **What could make this guide better?**

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/draft-guide-openliberty-operator-intro/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/draft-guide-openliberty-operator-intro/pulls)



<br/>
## **Where to next?**

* [Deploying microservices to OpenShift](https://openliberty.io/guides/cloud-openshift.html)
* [Deploying microservices to OpenShift by using Kubernetes Operators](https://openliberty.io/guides/cloud-openshift-operator.html)
* [Deploying microservices to an OKD cluster using Minishift](https://openliberty.io/guides/okd.html)


<br/>
## **Log out of the session**

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.