
# **Welcome to the Caching HTTP session data using JCache and Hazelcast guide!**

WINDOWS

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



# **What you'll learn**

<br/>
### **What is a session?**
On the internet, a web server doesn't know who you are or what you do
because it's processing stateless HTTP requests. An HTTP session provides a way to store
information to be used across multiple requests.
Session variables store user information like user name or items in a shopping cart.
By default, session variables will timeout after 30 minutes of being unused.
Cookies, which also store user information, are maintained on a client's computer,
whereas session variables are maintained on a web server. For security reasons,
an HTTP session is preferred over cookies when used with sensitive data.
A session hides data from users.
Cookies can be manipulated by a savvy user to make fake requests to your site.

<br/>
### **What is session persistence?**
High traffic websites must support thousands of users in a fast and reliable way.
Load balancing requires running several instances of the same application in parallel
so that traffic can be routed to different instances to maximize speed and reliability.
Unless a user is tied to a particular instance, running multiple instances of the same
application can pose an out-of-sync problem when each instance keeps an isolated copy of its
session data. HTTP session data caching can solve this problem by allowing all
instances of the application to share caches among each other.
Sharing caches among instances eliminates the need to route a user to the same instance
and helps in failover situations by distributing the cache.

![Session Cache](https://raw.githubusercontent.com/OpenLiberty/guide-sessions/master/assets/sessionCache.png)


You will learn how to build an application that creates and uses HTTP session data.
You will also learn how to use Open Liberty's **sessionCache** feature to persist HTTP sessions
by using Java Caching (JCache), the standard caching API for Java.

You will containerize and deploy the application to a local Kubernetes cluster.
You will then replicate the application in multiple pods and see that the session data is cached and
shared among all instances of the application. Even if an instance is unavailable, the other instances
are able to take over and handle requests from the same user by using the cached session data.



# **Getting started**

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-sessions.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-sessions.git
cd guide-sessions
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.


# **Creating the application**

The application that you are working with is a shopping cart web service that uses JAX-RS,
which is a Java API for building RESTful web services.
You'll learn how to persist a user's shopping cart data between servers by using the
**sessionCache** feature in Open Liberty. The **sessionCache** feature persists HTTP
sessions using JCache. You can have high-performance HTTP session persistence
without using a relational database.

Navigate to the **start** directory to begin.

Create the **CartApplication** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-sessions/start/src/main/java/io/openliberty/guides/cart/CartApplication.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-sessions/start/src/main/java/io/openliberty/guides/cart/CartApplication.java




```
package io.openliberty.guides.cart;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class CartApplication extends Application {

}
```
{: codeblock}



The **CartApplication** class extends the generic JAX-RS application class that is needed to run the
application.

Create the **CartResource** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-sessions/start/src/main/java/io/openliberty/guides/cart/CartResource.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-sessions/start/src/main/java/io/openliberty/guides/cart/CartResource.java




```
package io.openliberty.guides.cart;

import java.util.Enumeration;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("/")
public class CartResource {

    @POST
    @Path("cart/{item}&{price}")
    @Produces(MediaType.TEXT_PLAIN)
    @APIResponse(responseCode = "200", description = "Item successfully added to cart.")
    @Operation(summary = "Add a new item to cart.")
    public String addToCart(@Context HttpServletRequest request,
                    @Parameter(description = "Item you need for intergalatic travel.",
                               required = true)
                    @PathParam("item") String item,
                    @Parameter(description = "Price for this item.",
                               required = true)
                    @PathParam("price") double price) {
        HttpSession session = request.getSession();
        session.setAttribute(item, price);
        return item + " added to your cart and costs $" + price;
    }

    @GET
    @Path("cart")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200",
        description = "Items successfully retrieved from your cart.")
    @Operation(summary = "Return an JsonObject instance which contains " +
                         "the items in your cart and the subtotal.")
    public JsonObject getCart(@Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Enumeration<String> names = session.getAttributeNames();
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("pod-name", getHostname());
        builder.add("session-id", session.getId());
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Double subtotal = 0.0;
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String price = session.getAttribute(name).toString();
            arrayBuilder.add(name + " | $" + price);
            subtotal += Double.valueOf(price).doubleValue();
        }
        builder.add("cart", arrayBuilder);
        builder.add("subtotal", subtotal);
        return builder.build();
    }

    private String getHostname() {
        String hostname = System.getenv("HOSTNAME");
        if (hostname == null)
        	hostname = "localhost";
        	return hostname;
    }
}
```
{: codeblock}



The **CartResource** class defines the REST endpoints at which a user can make
an HTTP request.

The **addToCart** and **getCart** methods
have a number of annotations. Most of these annotations are used by the
MicroProfile OpenAPI and JAX-RS features to document the REST endpoints and map Java objects to web resources.
More information about these annotations can be found in the
[Documenting RESTful APIs](https://openliberty.io/guides/microprofile-openapi.html#augmenting-the-existing-jax-rs-annotations-with-openapi-annotations)
and
[Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html#creating-a-jax-rs-application)
guides.

The **cart/{item}&{price}** endpoint demonstrates how to set session data.
The **@PathParam** annotation injects a custom **item** and
**price** from the POST request into the method parameter.
The **addToCart** method gets the current **session** and binds
the **{item}:{price}** key-value pair into the session by the **setAttribute()** method.
A response is then built and returned to confirm that an item was added to your cart and session.

The **cart** endpoint demonstrates how to get session data.
The **getCart** method gets the current session, iterates through all key-value
pairs that are stored in the current session, and creates a **JsonObject** response.
The **JsonObject** response is returned to confirm the server instance by
**pod-name**, the session by **session-id**,
and the items in your cart by **cart**.


# **Configuring session persistence**

<br/>
### **Using client-server vs peer-to-peer model**

Session caching is only valuable when a server is connected to at least
one other member. There are two different ways session caching can behave in a
cluster environment:

* Client-server model: A Liberty server can act as the JCache client and connect
to a dedicated JCache server.
* Peer-to-peer model: A Liberty server can connect with other Liberty servers
that are also running with the session cache and configured to be
part of the same cluster.

You'll use the peer-to-peer model in a Kubernetes environment for this guide.

<br/>
### **Configuring session persistence with JCache in Open Liberty**

JCache, which stands for Java Caching, is an interface
to standardize distributed caching on the Java platform.
The **sessionCache** feature uses JCache, which allows for session
persistence by providing a common cache of session data between servers.
This feature doesn't include a JCache implementation.
For this guide, you'll use Hazelcast as an open source JCache provider.

Hazelcast is a JCache provider. Open Liberty needs to be configured to use
Hazelcast after the **sessionCache** feature is enabled.

Create the **server.xml** file.

> Run the following touch command in your terminal
```
touch /home/project/guide-sessions/start/src/main/liberty/config/server.xml
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-sessions/start/src/main/liberty/config/server.xml




```
<server description="Liberty Server for Sessions Management">

    <featureManager>
        <feature>servlet-4.0</feature>
        <feature>sessionCache-1.0</feature>
        <feature>jaxrs-2.1</feature>
        <feature>jsonp-1.1</feature>
        <feature>mpOpenAPI-2.0</feature>
    </featureManager>

    <variable name="default.http.port" defaultValue="9080"/>
    <variable name="default.https.port" defaultValue="9443"/>
    <variable name="app.context.root" defaultValue="guide-sessions"/>
    <variable name="hazelcast.lib" defaultValue="${shared.resource.dir}/hazelcast.jar"/>

    <httpEndpoint httpPort="${default.http.port}" httpsPort="${default.https.port}"
        id="defaultHttpEndpoint" host="*" />
    <httpSessionCache libraryRef="jCacheVendorLib"
        uri="file:${server.config.dir}/hazelcast-config.xml" />
    <!-- tag::library[] -->
    <library id="jCacheVendorLib">
        <file name="${hazelcast.lib}" />
    </library>

    <webApplication location="guide-sessions.war" contextRoot="${app.context.root}" />

</server>
```
{: codeblock}



The **library** element includes the library reference that indicates
to the server where the Hazelcast implementation of JCache is located. 
Your Hazelcast implementation of JCache is a JAR file that resides in the location that is defined by
the **${hazelcast.lib}** variable.
The **hazelcast.jar** file is downloaded as a dependency and copied to the
predefined **target** directory when the Maven build runs. This goal is defined in the
provided Maven POM file.

<br/>
### **Configuring Hazelcast**


By default, all Open Liberty servers that run the **sessionCache**
feature and Hazelcast are connected using a peer-to-peer model.

You can share the session cache only among certain Hazelcast instances
by using the **group** configuration element in the Hazelcast configuration file.

Create the **hazelcast-config.xml** configuration file.

> Run the following touch command in your terminal
```
touch /home/project/guide-sessions/start/src/main/liberty/config/hazelcast-config.xml
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-sessions/start/src/main/liberty/config/hazelcast-config.xml




```
<hazelcast xmlns="http://www.hazelcast.com/schema/config"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.hazelcast.com/schema/config
       https://hazelcast.com/schema/config/hazelcast-config-3.12.xsd">
    <group>
        <name>CartCluster</name>
    </group>
</hazelcast>
```
{: codeblock}



The cluster group **CartCluster** is defined in the **hazelcast-config.xml**.

In the **server.xml** file, a reference to the Hazelcast configuration file is made by using
the **httpSessionCache** tag.

There are more configuration settings that you can explore in the
[Hazelcast documentation](https://docs.hazelcast.org/docs/latest/manual/html-single/#understanding-configuration).


# **Running the application**

When you run Open Liberty in development mode, known as dev mode, the server listens for file changes and automatically recompiles and 
deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```
mvn liberty:dev
```
{: codeblock}


After you see the following message, your application server in dev mode is ready:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, 
or open the project in your editor.


Once your application is up and running, use the following command to get the URL.
Open your browser and check out your service by going to the URL that the command returns.
```
echo http://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')/openapi/ui
```
{: codeblock} 

First, make a POST request to the **/cart/{item}&{price}** endpoint. To make this request, expand the POST
endpoint on the UI, click the **Try it out** button, provide an item and a price,
and then click the **Execute** button.
The POST request adds a user-specified item and price to a session
that represents data in a user's cart.

Next, make a GET request to the **/cart** endpoint. To make this request, expand the GET
endpoint on the UI, click the **Try it out** button,
and then click the **Execute** button. The GET request
returns a pod name, a session ID, and all the items from your session.

When you are done checking out the service, exit dev mode by pressing **CTRL+C** in the command-line session
where you ran the server, or by typing **q** and then pressing the **enter/return** key.





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


# **Containerizing the application**

Before you can deploy the application to Kubernetes, you need to containerize it with Docker.

Make sure to start your Docker daemon before you proceed.

The Dockerfile is provided at the **start** directory. If you're unfamiliar with Dockerfile,
check out the [Containerizing microservices](https://openliberty.io/guides/containerize.html) guide,
which covers Dockerfile in depth.

Run the **mvn package** command from the **start** directory so that the **.war** file resides in the **target** directory.

```
mvn package
```
{: codeblock}


Run the following command to download or update to the latest Open Liberty Docker image:

```
docker pull openliberty/open-liberty:full-java11-openj9-ubi
```
{: codeblock}


To build and containerize the application, run the following Docker build command in the **start** directory:

```
docker build -t cart-app:1.0-SNAPSHOT .
```
{: codeblock}


When the build finishes, run the following command to list all local Docker images:
```
docker images
```
{: codeblock}


Verify that the **cart-app:1.0-SNAPSHOT** image is listed among the Docker images, for example:
```
REPOSITORY                      TAG
cart-app                        1.0-SNAPSHOT
openliberty/open-liberty        full-java11-openj9-ubi
```

If the images built without errors, push them to your container registry on IBM Cloud with the following commands:
```
docker tag cart-app:1.0-SNAPSHOT us.icr.io/$NAMESPACE_NAME/cart-app:1.0-SNAPSHOT
docker push us.icr.io/$NAMESPACE_NAME/cart-app:1.0-SNAPSHOT
```
{: codeblock}

Update the image names so that the images in your IBM Cloud container registry are used,
and remove the **nodePort** fields so that the ports can be generated automatically:

```
sed -i 's=cart-app:1.0-SNAPSHOT=us.icr.io/'"$NAMESPACE_NAME"'/cart-app:1.0-SNAPSHOT\n        imagePullPolicy: Always=g' kubernetes.yaml
sed -i 's=nodePort: 31000==g' kubernetes.yaml
```
{: codeblock}

# **Deploying and running the application in Kubernetes**


Now that the containerized application is built, deploy it to a local Kubernetes cluster by using
a Kubernetes resource definition, which is provided in the **kubernetes.yaml** file
at the **start** directory.

Run the following command to deploy the application into `3` replicated pods as defined
in the **kubernetes.yaml** file:
```
kubectl apply -f kubernetes.yaml
```
{: codeblock}


When the application is deployed, run the following command to check the status of your pods:
```
kubectl get pods
```
{: codeblock}


You see an output similar to the following if all the pods are working correctly:

```
NAME                             READY  STATUS   RESTARTS  AGE
cart-deployment-98f4ff789-2xlhs  1/1    Running  0         17s
cart-deployment-98f4ff789-6rvfj  1/1    Running  0         17s
cart-deployment-98f4ff789-qrh45  1/1    Running  0         17s
```

Run the following commands to get the necessary port to connect to the application.
```
CART_NODEPORT=`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services cart-service`
kubectl port-forward svc/cart-service $CART_NODEPORT:9080
```
{: codeblock}



Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.

Run the following commands to get the URL to access the application. This URL displays the available REST endpoints.
```
CART_NODEPORT=`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services cart-service`
echo https://${USERNAME}-${CART_NODEPORT}.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')/openapi/ui
```
{: codeblock}

Make a POST request to the **/cart/{item}&{price}** endpoint. To make this request, expand the POST
endpoint on the UI, click the **Try it out** button, provide an item and a price,
and then click the **Execute** button.
The POST request adds a user-specified item and price to a session
that represents data in a user's cart.

Next, make a GET request to the **/cart** endpoint. To make this request, expand the GET
endpoint on the UI, click the **Try it out** button, and then click the **Execute** button.
The GET request returns a pod name, a session ID, and all the items from your session.

```
{
  "pod-name": "cart-deployment-98f4ff789-2xlhs",
  "session-id": "RyJKzmka6Yc-ZCMzEA8-uPq",
  "cart": [
    "eggs | $2.89"
  ],
  "subtotal": 2.89
}
```

Replace the **[pod-name]** in the following command, and then run the command to pause
the pod for the GET request that you just ran:

```
kubectl exec -it [pod-name] -- /opt/ol/wlp/bin/server pause
```
{: codeblock}


Repeat the GET request. You see the same **session-id**
but a different **pod-name** because the session data is cached but the request
is served by a different pod (server).

Verify that the Hazelcast cluster is running by checking the Open Liberty log. 
To check the log, run the following command:

```
kubectl exec -it [pod-name] -- cat /logs/messages.log
```
{: codeblock}


You see a message similar to the following:

```
... I [10.1.0.46]:5701 [CartCluster] [3.11.2]

Members {size:3, ver:3} [
	Member [10.1.0.40]:5701 - 01227d80-501e-4789-ae9d-6fb348d794ea
	Member [10.1.0.41]:5701 - a68d0ed1-f50e-4a4c-82b0-389f356b8c73 this
	Member [10.1.0.42]:5701 - b0dfa05a-c110-45ed-9424-adb1b2896a3d
]
```

You can resume the paused pod by running the following command:

```
kubectl exec -it [pod-name] -- /opt/ol/wlp/bin/server resume
```
{: codeblock}




# **Tearing down the environment**

When you no longer need your deployed application, you can delete all Kubernetes resources
by running the **kubectl delete** command:

```
kubectl delete -f kubernetes.yaml
```


# **Summary**

## **Nice Work!**

You have created, used, and cached HTTP session data for an application that was running on Open Liberty server and deployed in a Kubernetes cluster.




<br/>
## **Clean up your environment**


Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-sessions** project by running the following commands:

```
cd /home/project
rm -fr guide-sessions
```
{: codeblock}

<br/>
## **What did you think of this guide?**

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Caching%20HTTP%20session%20data%20using%20JCache%20and%20Hazelcast&guide-id=cloud-hosted-guide-sessions)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

<br/>
## **What could make this guide better?**

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-sessions/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-sessions/pulls)



<br/>
## **Where to next?**

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Documenting RESTful APIs](https://openliberty.io/guides/microprofile-openapi.html)
* [Deploying microservices to Kubernetes](https://openliberty.io/guides/kubernetes-intro.html)


<br/>
## **Log out of the session**

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.