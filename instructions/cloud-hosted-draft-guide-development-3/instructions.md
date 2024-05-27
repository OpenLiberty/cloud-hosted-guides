---
markdown-version: v1
tool-type: theiadocker
---

# **Welcome to the Deploying a microservice to Kubernetes by using a Kubernetes Operator guide!**

Explore how to deploy a microservice to Kubernetes by using a Kubernetes Operator.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




# **What you'll learn**

You will learn how to deploy a cloud-native application with a microservice to Kubernetes by using the Open Liberty Kubernetes Operator. 

[Kubernetes](https://www.kubernetes.io/) is a container orchestration system. It streamlines the DevOps
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


In this Skills Network environment, the Open Liberty Operator is already installed by the administrator.
If you like to learn how to install the Open Liberty Operator,
you can learn from the [Deploying microservices to OpenShift by using Kubernetes Operators](https://openliberty.io/guides/cloud-openshift-operator.html#installing-the-operators) guide or the Open Liberty Operator [document](https://github.com/OpenLiberty/open-liberty-operator/blob/master/deploy/releases/0.7.1/readme.adoc).



# **Logging into your cluster**

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


To check that the Open Liberty Operator has been installed successfully, run the following command to view all the supported API resources that are available through the Open Liberty Operator:
```
kubectl api-resources --api-group=openliberty.io
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

# **Deploying the system microservice to Kubernetes**

To deploy the **system** microservice, you must first package the microservice, then create and build
a runnable container image of the packaged microservice.

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
### **Building the image**

Run the following command to download or update to the latest Open Liberty Docker image:

```
docker pull icr.io/appcafe/open-liberty:full-java11-openj9-ubi
```
{: codeblock}


Next, run the **docker build** command to build the container image for your application:
```
docker build -t system:1.0-SNAPSHOT system/.
```
{: codeblock}


The **-t** flag in the **docker build** command allows the Docker image to be labeled (tagged) in the **name[:tag]** format. 
The tag for an image describes the specific image version.
If the optional **[:tag]** tag is not specified, the **latest** tag is created by default.

Next, push your images to the container registry on IBM Cloud with the following commands:

```
docker tag system:1.0-SNAPSHOT us.icr.io/$NAMESPACE_NAME/system:1.0-SNAPSHOT
docker push us.icr.io/$NAMESPACE_NAME/system:1.0-SNAPSHOT
```
{: codeblock}

Now you're ready to deploy the image.

<br/>
### **Deploying the image**

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
  applicationImage: system:1.0-SNAPSHOT
  service:
    port: 9080
  expose: true
  env:
    - name: WLP_LOGGING_MESSAGE_FORMAT
      value: "json"
    - name: WLP_LOGGING_MESSAGE_SOURCE
      value: "message,trace,accessLog,ffdc,audit"
  startupProbe:
    httpGet:
      path: /health/started
      port: 9080
      scheme: HTTP
  readinessProbe:
    failureThreshold: 12
    httpGet:
      path: /health/ready
      port: 9080
      scheme: HTTP
    initialDelaySeconds: 30
    periodSeconds: 2
    timeoutSeconds: 10
  livenessProbe:
    failureThreshold: 12
    httpGet:
      path: /health/live
      port: 9080
      scheme: HTTP
    initialDelaySeconds: 30
    periodSeconds: 2
    timeoutSeconds: 10
```
{: codeblock}


The **deploy.yaml** file is configured to deploy one **OpenLibertyApplication**
resource, **system**, which is controlled by the Open Liberty Operator.

The **applicationImage** parameter defines what container image is deployed as part of the **OpenLibertyApplication** CRD. 
This parameter follows the **[image-name][:tag]** format. The parameter can also point to an image hosted on an external registry, such as Docker Hub. 
The **system** microservice is configured to use the **image** created from the earlier build. 

The **env** parameter is used to specify environment variables that are passed to the container at runtime.

Additionally, the microservice includes the **service** and **expose** parameters.
The **service.port** parameter specifies which port is exposed by the container,
allowing the microservice to be accessed from outside the container. To access the microservice from outside of the cluster,
it must be exposed by setting the **expose** parameter to **true**.
After you expose the microservice, the Operator automatically creates and configures routes for external access to your microservice.


Run the following commands to update the **applicationImage** and deploy the **system** microservice with the previously explained configuration:
```
sed -i 's=system:1.0-SNAPSHOT=us.icr.io/'"$NAMESPACE_NAME"'/system:1.0-SNAPSHOT\n  imagePullPolicy: Always=g' deploy.yaml
kubectl apply -f deploy.yaml
```
{: codeblock}

Next, run the following command to view your newly created **OpenLibertyApplications** resources:

```
kubectl get OpenLibertyApplications
```
{: codeblock}


You can also replace **OpenLibertyApplications** with the shortname **olapps**.

Look for output that is similar to the following example:

```
NAME      IMAGE                  EXPOSED   RECONCILED   AGE
system    system:1.0-SNAPSHOT    true      True         10s
```

A **RECONCILED** state value of **True** indicates that the operator was able to successfully process the **OpenLibertyApplications** instances. 
Run the following command to view details of your microservice:

```
kubectl describe olapps/system
```
{: codeblock}


This example shows part of the **olapps/system** output:

```
Name:         system
Namespace:    default
Labels:       app.kubernetes.io/part-of=system
              name=system
Annotations:  <none>
API Version:  openliberty.io/v1beta1
Kind:         OpenLibertyApplication

...
```

# **Accessing the microservice**

To access the exposed **system** microservice, the service must be port-forwarded.
Run the following command to set up port forwarding to access the **system** service:

```
kubectl port-forward svc/system 9080
```
{: codeblock}



Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.
Access the microservice by running the following command:
```
curl -s http://localhost:9080/system/properties | jq
```
{: codeblock}

When you're done trying out the microservice, press **CTRL+C** in the command line session
where you ran the **kubectl port-forward** command to stop the port forwarding.

# **Tearing down the environment**


When you no longer need your deployed microservice, you can delete all resources by running the following command:

```
kubectl delete -f deploy.yaml
```
{: codeblock}

# **Summary**

## **Nice Work!**

You just deployed a microservice running in Open Liberty to Kubernetes by using the Open Liberty Operator.



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

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Deploying%20a%20microservice%20to%20Kubernetes%20by%20using%20a%20Kubernetes%20Operator&guide-id=cloud-hosted-draft-guide-openliberty-operator-intro)

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