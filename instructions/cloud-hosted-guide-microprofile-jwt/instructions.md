
# Welcome to the Securing microservices with JSON Web Tokens guide!

You'll explore how to control user and role access to microservices with MicroProfile JSON Web Token (MicroProfile JWT).

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




# What you'll learn

You will add token-based authentication mechanisms to authenticate, authorize,
and verify users by implementing MicroProfile JWT in the **system** microservice.

A JSON Web Token (JWT) is a self-contained token that is designed to securely transmit
information as a JSON object. The information in this JSON object is digitally
signed and can be trusted and verified by the recipient.

For microservices, a token-based authentication mechanism offers a lightweight
way for security controls and security tokens to propagate user identities
across different services. JSON Web Token is becoming the most common
token format because it follows well-defined and known standards.

MicroProfile JWT standards define the required format of JWT for authentication
and authorization. The standards also map JWT claims to various Jakarta EE
container APIs and make the set of claims available through getter methods.

In this guide, the application uses JWTs to authenticate a user,
allowing them to make authorized requests to a secure backend service.

You will be working with two services, a **frontend** service and a secure 
**system** backend service. The **frontend** service logs a user in, builds a JWT, and makes
authorized requests to the secure **system** service for JVM system properties.
The following diagram depicts the application that is used in this guide:

![JWT frontend and system services](https://raw.githubusercontent.com/OpenLiberty/guide-microprofile-jwt/master/assets/JWT_Diagram.png)


The user signs in to the **frontend** service with a username and a password,
at which point a JWT is created. The **frontend** service then makes
requests, with the JWT included, to the **system** backend service. The secure
**system** service verifies the JWT to ensure that the request came
from the authorized **frontend** service. After the JWT is validated, the information
in the claims, such as the user's role, can be trusted and used to
determine which system properties the user has access to.

To learn more about JSON Web Tokens, check out the
[jwt.io website](https://jwt.io/introduction/). If you want to learn more about how JWTs
can be used for user authentication and authorization, check out the Open Liberty
[Single Sign-on documentation](https://openliberty.io/docs/latest/single-sign-on.html).

# Getting started

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```
cd /home/project
```
{: codeblock}

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-microprofile-jwt.git) and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-microprofile-jwt.git
cd guide-microprofile-jwt
```
{: codeblock}


The **start** directory contains the starting project that you will build upon.

The **finish** directory contains the finished project that you will build.

### Try what you'll build

The **finish** directory contains the finished JWT security implementation for the
services in the application. Try the finished application before you
build your own.

To try out the application, run the following commands to navigate to the **finish/frontend** directory and
deploy the **frontend** service to Open Liberty:

```
cd finish/frontend
mvn liberty:run
```
{: codeblock}


Open another command-line session and run the following commands to navigate to the **finish/system** directory and
deploy the **system** service to Open Liberty:

```
cd finish/system
mvn liberty:run
```
{: codeblock}


After you see the following message in both command-line sessions, both of your services are ready:

```
The defaultServer server is ready to run a smarter planet.
```


To launch the front-end web application, 
select **Launch Application** from the menu of the IDE, type in **9090** to specify the port number for the front-end web application, 
and click the **OK** button. 
You’re redirected to a URL similar to **`https://accountname-9090.theiadocker-4.proxy.cognitiveclass.ai`**, 
where **accountname** is your account name. Click the **Log in** link on the welcome page. From here, 
you can log in to the application with the form-based login.

Log in with one of the following usernames and its corresponding password:

| *Username* | *Password* | *Role*
| --- | --- | ---
| bob | bobpwd | admin, user
| alice | alicepwd | user
| carl | carlpwd | user

You're redirected to a page that displays information that the
front end requested from the **system** service, such as the system username.
If you log in as an **admin** user, the current OS is also shown. If you log in as
a **user**, you instead see the **You are not authorized to access
this system property** message. You see this message because the **user** role doesn't
have sufficient privileges to view current OS information.

Additionally, the **groups** claim of the JWT is read by the **system** service and
requested by the front end to be displayed.


You can try accessing these services without a JWT by going to the **system** endpoint. 
Open another command-line session by selecting **Terminal** > **New Terminal** from the menu of the IDE.
Run the following curl command from the terminal in the IDE:
```
curl -k https://localhost:8443/system/properties/os
```
{: codeblock}

The response is empty because you don't have access.
Access is granted if a valid JWT is sent with the request.
The following error also appears in the command-line session of the **system** service:

```
[ERROR] CWWKS5522E: The MicroProfile JWT feature cannot perform authentication because a MicroProfile JWT cannot be found in the request.
```

When you are done with the application, stop both the **frontend** and **system**
services by pressing **CTRL+C** in the command-line sessions where you ran them.
Alternatively, you can run the following goals from the **finish** directory in
another command-line session:

```
mvn -pl system liberty:stop
mvn -pl frontend liberty:stop
```
{: codeblock}



# Creating the secure system service


To begin, run the following command to navigate to the **start** directory:
```
cd /home/project/guide-microprofile-jwt/start
```
{: codeblock}

When you run Open Liberty in development mode, known as dev mode, the server
listens for file changes and automatically recompiles and deploys your updates
whenever you save a new change. Run the following commands to navigate to the
**frontend** directory and start the **frontend** service in dev mode:

```
cd frontend
mvn liberty:dev
```
{: codeblock}


Open another command-line session and run the following commands to navigate to the
**system** directory and start the **system** service in dev mode:
```
cd system
mvn liberty:dev
```
{: codeblock}


After you see the following message, your application server in dev mode is ready:

```
************************************************************************
*    Liberty is running in dev mode.
```

The **system** service provides endpoints for the **frontend** service to use to
request system properties. This service is secure and requires a valid JWT to be
included in requests that are made to it. The claims in the JWT are used to determine
what properties the user has access to.

Create the secure **system** service.


Create the **SystemResource** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-jwt/start/system/src/main/java/io/openliberty/guides/system/SystemResource.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-jwt/start/system/src/main/java/io/openliberty/guides/system/SystemResource.java



This class has role-based access control. The role names that are used in the
**@RolesAllowed** annotations are mapped to group
names in the **groups** claim of the JWT, which results in an authorization
decision wherever the security constraint is applied.

The **/username** endpoint returns the system's username and is annotated with the
**@RolesAllowed({"admin, "user"})**
annotation. Only authenticated users with the role of **admin** or **user**
can access this endpoint.

The **/os** endpoint returns the system's current OS. Here,
the **@RolesAllowed** annotation is limited to
**admin**, meaning that only authenticated users with the role of **admin** are able to
access the endpoint.

While the **@RolesAllowed** annotation automatically reads from the **groups** claim
of the JWT to make an authorization decision, you can also manually access the
claims of the JWT by using the **@Claim** annotation. In this
case, the **groups** claim is injected into the **roles** JSON array.
The roles that are parsed from the **groups** claim of the
JWT are then exposed back to the front end at the **/jwtroles** endpoint.
To read more about different claims and ways to
access them, check out the [MicroProfile JWT documentation](https://github.com/eclipse/microprofile-jwt-auth/blob/master/spec/src/main/asciidoc/interoperability.asciidoc).


# Creating a client to access the secure system service




Create a RESTful client interface for the **frontend** service.

Create the **SystemClient** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-jwt/start/frontend/src/main/java/io/openliberty/guides/frontend/client/SystemClient.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-jwt/start/frontend/src/main/java/io/openliberty/guides/frontend/client/SystemClient.java



This interface declares methods for accessing each of the endpoints that were
previously set up in the **system** service.

The MicroProfile Rest Client feature automatically builds and generates a client
implementation based on what is defined in the **SystemClient** interface. You
don't need to set up the client and connect with the remote service.

As discussed, the **system** service is secured and requests made to it must
include a valid JWT in the **Authorization** header. The **@HeaderParam** annotations
include the JWT by specifying that the value of the **String authHeader**
parameter, which contains the JWT, be used as the value for the **Authorization**
header. This header is included in all of the requests that are made to the
**system** service through this client.

Create the application bean that the front-end UI uses to request data.

Create the **ApplicationBean** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-jwt/start/frontend/src/main/java/io/openliberty/guides/frontend/ApplicationBean.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-jwt/start/frontend/src/main/java/io/openliberty/guides/frontend/ApplicationBean.java



The application bean is used to populate the table in the front end by making
requests for data through the **defaultRestClient**, which
is an injected instance of the **SystemClient** class
that you created. The **getOs()**, **getUsername()**, and **getJwtRoles()** methods call
their associated methods of the **SystemClient** class
with the **authHeader** passed in as a parameter. The **authHeader** is a string that
consists of the JWT with **Bearer** prefixed to it. The **authHeader** is included in the
**Authorization** header of the subsequent requests that are made by the
**defaultRestClient** instance.

The JWT for these requests is retrieved from the session attributes with the
**getJwt()** method. The JWT is stored in the session
attributes by the provided **LoginBean** class. When the
user logs in to the front end, the **doLogin()** method is
called and builds the JWT. Then, the **setAttribute()**
method stores it as an **HttpSession** attribute. The JWT is built by using the
**JwtBuilder** APIs in the **buildJwt()** method.
You can see that the **claim()** method is being used to set the **groups** claim of the token.
This claim is used to provide the role-based access that you implemented.


# Configuring MicroProfile JWT



Configure the **mpJwt** feature in the **microprofile-config.properties** file for the **system** service.

Create the microprofile-config.properties file.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-jwt/start/system/src/main/webapp/META-INF/microprofile-config.properties
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-jwt/start/system/src/main/webapp/META-INF/microprofile-config.properties



The **mp.jwt.verify.issuer** config property specifies the expected value of
the issuer claim on an incoming JWT. Incoming JWTs with an issuer
claim that's different from this expected value aren't considered valid.

Next, add the MicroProfile JSON Web Token feature to the server configuration file for
the **system** service.

Replace the system server configuration file.

> From the menu of the IDE, select 
 **File** > **Open** > guide-microprofile-jwt/start/system/src/main/liberty/config/server.xml



The **mpJwt** feature adds the libraries that are required for MicroProfile JWT implementation.


# Building and running the application

Because you are running the **frontend** and **system** services in dev mode, the changes that you made were automatically picked up. You're now ready to check out your application in your browser.


To launch the front-end web application, 
select **Launch Application** from the menu of the IDE, type in **9090** to specify the port number for the front-end web application, 
and click the **OK** button. You’re redirected to the **`https://accountname-9090.theiadocker-4.proxy.cognitiveclass.ai`** URL, 
where **accountname** is your account name. Click the **Log in** link on the welcome page. 
Log in with one of the following usernames and its corresponding password:

| *Username* | *Password* | *Role*
| --- | --- | ---
| bob | bobpwd | admin, user
| alice | alicepwd | user
| carl | carlpwd | user

After you log in, you can see the information that's retrieved from the **system**
service. With successfully implemented role-based access in the application, if
you log in as a **user** role, you don't have access to the OS property.

You can also see the value of the **groups** claim in the row with the **Roles:** label.
These roles are read from the JWT and sent back to the front end to
be displayed.


You can check that the **system** service is secured against unauthenticated
requests by going to the **system** endpoint.
Run the following curl command from the terminal in the IDE:
```
curl -k https://localhost:8443/system/properties/os
```
{: codeblock}

You'll see an empty response because you didn't authenticate with a valid JWT. 

In the front end, you see your JWT displayed in the row with the **JSON Web Token** label.

To see the specific information that this JWT holds, you can enter it into the token reader on the [JWT.io website](https://JWT.io).
The token reader shows you the header, which contains information about the JWT, as shown in the following example:

```
{
  "kid": "NPzyG3ZMzljUwQgbzi44",
  "typ": "JWT",
  "alg": "RS256"
}
```

The token reader also shows you the payload, which contains the claims information:

```
{
  "token_type": "Bearer",
  "sub": "bob",
  "upn": "bob",
  "groups": [ "admin", "user" ],
  "iss": "http://openliberty.io",
  "exp": 1596723489,
  "iat": 1596637089
}
```

You can learn more about these claims in the [MicroProfile JWT documentation](https://github.com/eclipse/microprofile-jwt-auth/blob/master/spec/src/main/asciidoc/interoperability.asciidoc).


# Testing the application


You can manually check that the **system** service is secure by making requests to
each of the endpoints with and without valid JWTs. However, automated tests are a
much better approach because they are more reliable and trigger a failure if a
breaking change is introduced.

Create the **SystemEndpointIT** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-microprofile-jwt/start/system/src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-microprofile-jwt/start/system/src/test/java/it/io/openliberty/guides/system/SystemEndpointIT.java



The **testOSEndpoint()**, **testUsernameEndpoint()**, and **testRolesEndpoint()**
tests test the **/os**, **/username**, and **/roles** endpoints.

Each test makes three requests to its associated endpoint. The first
**makeRequest()** call has a JWT with the **admin** role. The second
**makeRequest()** call has a JWT with the **user** role. The third
**makeRequest()** call has no JWT at all. The responses to these
requests are checked based on the role-based access rules for the endpoints. The
**admin** requests should be successful on all endpoints. The **user** requests
should be denied by the **/os** endpoint but successfully access the **/username**
and **/jwtroles** endpoints. The requests that don't include a JWT should be
denied access to all endpoints.

### Running the tests

Because you started Open Liberty in dev mode, press the **enter/return** key from the
command-line session of the **system** service to run the tests. You see the
following output:

```
-------------------------------------------------------
  T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.system.SystemEndpointIT
[ERROR   ] CWWKS5522E: The MicroProfile JWT feature cannot perform authentication because a MicroProfile JWT cannot be found in the request.
[ERROR   ] CWWKS5522E: The MicroProfile JWT feature cannot perform authentication because a MicroProfile JWT cannot be found in the request.
[ERROR   ] CWWKS5522E: The MicroProfile JWT feature cannot perform authentication because a MicroProfile JWT cannot be found in the request.
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.648 s - in it.io.openliberty.guides.system.SystemEndpointIT

Results:

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

The three errors in the output are expected and result from the **system** service successfully
rejecting the requests that didn't include a JWT.

When you are finished testing the application, stop both the **frontend** and **system**
services by pressing **CTRL+C** in the command-line sessions where you ran them.
Alternatively, you can run the following goals from the **start** directory in
another command-line session:

```
mvn -pl system liberty:stop
mvn -pl frontend liberty:stop
```
{: codeblock}



# Summary

## Nice Work!

You learned how to use MicroProfile JWT to validate JWTs, authenticate and authorize users to secure your microservices in Open Liberty.




## Clean up your environment

Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-microprofile-jwt** project by running the following commands:

```
cd /home/project
rm -fr guide-microprofile-jwt
```
{: codeblock}

## What did you think of this guide?
We want to hear from you. To provide feedback on your experience with this guide, click the **Support/Feedback** button in the IDE,
select **Give feedback** option, fill in the fields, choose **General** category, and click the **Post Idea** button.

## What could make this guide better?
You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback](https://github.com/OpenLiberty/guide-microprofile-jwt/issues)
* [Create a pull request to contribute to this guide](https://github.com/OpenLiberty/guide-microprofile-jwt/pulls)




## Where to next? 

* [Authenticating users through social media providers](https://openliberty.io/guides/social-media-login.html)
* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)


## Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.