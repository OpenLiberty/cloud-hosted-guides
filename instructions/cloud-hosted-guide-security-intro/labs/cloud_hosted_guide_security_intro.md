---
markdown-version: v1
title: cloud-hosted-guide-security-intro
branch: lab-653-instruction
version-history-start-date: 2022-05-27T14:10:25Z
tool-type: theiadocker
---
::page{title="Welcome to the Securing a web application guide!"}

Learn how to secure a web application through authentication and authorization.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You'll learn how to secure a web application by performing authentication and authorization using Jakarta EE Security. Authentication confirms the identity of the user by verifying a user's credentials while authorization determines whether a user has access to restricted resources.

Jakarta EE Security provides capability to configure the basic authentication, form authentication, or custom form authentication mechanism by using annotations in servlets. It also provides the SecurityContext API for programmatic security checks in application code.

You’ll implement form authentication for a simple web front end. You'll also learn to specify security constraints for a servlet and use the SecurityContext API to determine the role of a logged-in user.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-security-intro.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-security-intro.git
cd guide-security-intro
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the ***finish*** directory and run the following Maven goal to build the application and deploy it to Open Liberty:

```bash
cd finish
mvn liberty:run
```

After you see the following message, your application server is ready:

```
The defaultServer server is ready to run a smarter planet.
```

The finished application is secured with form authentication.


Click the following button to visit the application:

::startApplication{port="9080" display="external" name="Visit application" route="/"}

The application automatically switches from an HTTP connection to a secure HTTPS connection and forwards you to a login page. If the browser gives you a certificate warning, it's because the Open Liberty server created a self-signed SSL certificate by default. You can follow your browser's provided instructions to accept the certificate and continue.

Sign in to the application with one of the following user credentials from the user registry, which are provided to you:

| *Username* | *Password* | *Role* | *Group*
| --- | --- | --- | ---
| alice | alicepwd | user | Employee
| bob | bobpwd | admin, user | Manager, Employee
| carl | carlpwd | admin, user | TeamLead, Employee
| dave | davepwd | N/A | PartTime

Notice that when you sign in as Bob or Carl, the browser redirects to the ***admin*** page and you can view their names and roles. When you sign in as Alice, you can only view Alice's name. When you sign in as Dave, you are blocked and see an ***Error 403: Authorization failed*** message because Dave doesn't have a role that is supported by the application.

After you are finished checking out the application, stop the Open Liberty server by pressing `Ctrl+C` in the command-line session where you ran the server. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```


::page{title="Adding authentication and authorization"}

For this application, users are asked to log in with a form when they access the application. Users are authenticated and depending on their roles, they are redirected to the pages that they are authorized to access. If authentication or authorization fails, users are sent to an error page. The application supports two roles, ***admin*** and ***user***.

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-security-intro/start
```

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), the server listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```bash
mvn liberty:dev
```

After you see the following message, your application server in dev mode is ready:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

Create the ***HomeServlet*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-security-intro/start/src/main/java/io/openliberty/guides/ui/HomeServlet.java
```


> Then, to open the HomeServlet.java file in your IDE, select
> **File** > **Open** > guide-security-intro/start/src/main/java/io/openliberty/guides/ui/HomeServlet.java, or click the following button

::openFile{path="/home/project/guide-security-intro/start/src/main/java/io/openliberty/guides/ui/HomeServlet.java"}



```java
package io.openliberty.guides.ui;

import java.io.IOException;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/home")
@FormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(errorPage = "/error.html",
                                       loginPage = "/welcome.html"))
@ServletSecurity(value = @HttpConstraint(rolesAllowed = { "user", "admin" },
  transportGuarantee = ServletSecurity.TransportGuarantee.CONFIDENTIAL))
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private SecurityContext securityContext;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (securityContext.isCallerInRole(Utils.ADMIN)) {
            response.sendRedirect("/admin.jsf");
        } else if  (securityContext.isCallerInRole(Utils.USER)) {
            response.sendRedirect("/user.jsf");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        doGet(request, response);
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***HomeServlet*** servlet is the entry point of the application. To enable form authentication for the ***HomeServlet*** class, define the ***@FormAuthenticationMechanismDefinition*** annotation and set its ***loginToContinue*** attribute with a ***@LoginToContinue*** annotation. This ***@FormAuthenticationMechanismDefinition*** annotation defines ***welcome.html*** as the login page and ***error.html*** as the error page.

The ***welcome.html*** page implements the login form, and the ***error.html*** page implements the error page. Both pages are provided for you under the ***src/main/webapp*** directory. The login form in the ***welcome.html*** page uses the ***j_security_check*** action, which is defined by Jakarta EE and available by default.

Authorization determines whether a user can access a resource. To restrict access to authenticated users with ***user*** and ***admin*** roles, define the ***@ServletSecurity*** annotation with the ***@HttpConstraint*** annotation and set the ***rolesAllowed*** attribute to these two roles.

The ***transportGuarantee*** attribute defines the constraint on the traffic between the client and the application. Set it to ***CONFIDENTIAL*** to enforce that all user data must be encrypted, which is why an HTTP connection from a browser switches to HTTPS.

The SecurityContext interface provides programmatic access to the Jakarta EE Security API. Inject a SecurityContext instance into the ***HomeServlet*** class. The ***doGet()*** method uses the ***isCallerInRole()*** method from the SecurityContext API to check a user's role and then forwards the response to the appropriate page.

The ***src/main/webapp/WEB-INF/web.xml*** file contains the rest of the security declaration for the application.


::openFile{path="/home/project/guide-security-intro/start/src/main/webapp/WEB-INF/web.xml"}

The ***security-role*** elements define the roles that are supported by the application, which are ***user*** and ***admin***. The ***security-constraint*** elements specify that JSF resources like the ***user.jsf*** and ***admin.jsf*** pages can be accessed only by users with ***user*** and ***admin*** roles.


::page{title="Configuring the user registry"}

User registries store user account information, such as username and password, for use by applications to perform security-related operations. Typically, application servers would be configured to use an external registry like a Lightweight Directory Access Protocol (LDAP) registry. Applications would access information in the registry for authentication and authorization by using APIs like the Jakarta EE Security API.

Open Liberty provides an easy-to-use basic user registry for developers, which you will configure.

Create the ***userRegistry*** configuration file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-security-intro/start/src/main/liberty/config/userRegistry.xml 
```


> Then, to open the userRegistry.xml file in your IDE, select
> **File** > **Open** > guide-security-intro/start/src/main/liberty/config/userRegistry.xml, or click the following button

::openFile{path="/home/project/guide-security-intro/start/src/main/liberty/config/userRegistry.xml"}



```xml
<server description="Sample Liberty server">
  <basicRegistry id="basic" realm="WebRealm">
    <user name="bob"
      password="{xor}PTA9Lyg7" /> <!-- bobpwd -->
    <user name="alice"
      password="{xor}PjM2PDovKDs=" />  <!-- alicepwd -->
    <user name="carl"
      password="{xor}PD4tMy8oOw==" />  <!-- carlpwd -->
    <user name="dave"
      password="{xor}Oz4pOi8oOw==" />  <!-- davepwd -->

    <group name="Manager">
      <member name="bob" />
    </group>

    <group name="TeamLead">
      <member name="carl" />
    </group>
    
    <group name="Employee">
      <member name="alice" />
      <member name="bob" />
      <member name="carl" />
    </group>

    <group name="PartTime">
      <member name="dave" />
    </group>
  </basicRegistry>
</server>
```



The registry has four users, ***bob***, ***alice***, ***carl***, and ***dave***. It also has four groups: ***Manager***, ***TeamLead***, ***Employee***, and ***PartTime***. Each user belongs to one or more groups.

It is not recommended to store passwords in plain text. The passwords in the ***userRegistry.xml*** file are encoded by using the Liberty ***securityUtility*** command with XOR encoding.


See the server configuration file.

::openFile{path="/home/project/guide-security-intro/start/src/main/liberty/config/server.xml"}

Use the ***include*** element to add the basic user registry configuration to your server configuration. Open Liberty includes configuration information from the specified XML file in its server configuration.

The ***server.xml*** file contains the security configuration of the server under the ***application-bnd*** element. Use the ***security-role*** and ***group*** elements to map the groups in the ***userRegistry.xml*** file to the appropriate user roles supported by the application for proper user authorization. The ***Manager*** and ***TeamLead*** groups are mapped to the ***admin*** role while the ***Employee*** group is mapped to the ***user*** role.


::page{title="Running the application"}

You started the Open Liberty server in dev mode at the beginning of the guide, so all the changes were automatically picked up.



Click the following button to visit the application:

::startApplication{port="9080" display="external" name="Visit application" route="/"}

As you can see, the browser gets automatically redirected from an HTTP connection to an HTTPS connection because the transport guarantee is defined in the ***HomeServlet*** class.

You will see a login form because form authentication is implemented and configured. Sign in to the application by using one of the credentials from the following table. The credentials are defined in the configured user registry.

| *Username* | *Password* | *Role* | *Group*
| --- | --- | --- | ---
| alice | alicepwd | user | Employee
| bob | bobpwd | admin, user | Manager, Employee
| carl | carlpwd | admin, user | TeamLead, Employee
| dave | davepwd | N/A | PartTime

Notice that when you sign in as Bob or Carl, the browser redirects to the ***admin*** page and you can view their names and roles. When you sign in as Alice, you can only view Alice's name. When you sign in as Dave, you are blocked and see an ***Error 403: Authorization failed*** message because Dave doesn't have a role that is supported by the application.



::page{title="Testing the application"}

Write the ***SecurityIT*** class to test the authentication and authorization of the application.

Create the ***SecurityIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-security-intro/start/src/test/java/it/io/openliberty/guides/security/SecurityIT.java
```


> Then, to open the SecurityIT.java file in your IDE, select
> **File** > **Open** > guide-security-intro/start/src/test/java/it/io/openliberty/guides/security/SecurityIT.java, or click the following button

::openFile{path="/home/project/guide-security-intro/start/src/test/java/it/io/openliberty/guides/security/SecurityIT.java"}



```java
package it.io.openliberty.guides.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SecurityIT {

    private static String urlHttp;
    private static String urlHttps;

    @BeforeEach
    public void setup() throws Exception {
        urlHttp = "http://localhost:" + System.getProperty("http.port");
        urlHttps = "https://localhost:" + System.getProperty("https.port");
        ITUtils.trustAll();
    }

    @Test
    public void testAuthenticationFail() throws Exception {
        executeURL("/", "bob", "wrongpassword", true, -1, "Don't care");
    }

    @Test
    public void testAuthorizationForAdmin() throws Exception {
        executeURL("/", "bob", "bobpwd", false,
            HttpServletResponse.SC_OK, "admin, user");
    }

    @Test
    public void testAuthorizationForUser() throws Exception {
        executeURL("/", "alice", "alicepwd", false,
            HttpServletResponse.SC_OK, "<title>User</title>");
    }

    @Test
    public void testAuthorizationFail() throws Exception {
        executeURL("/", "dave", "davepwd", false,
            HttpServletResponse.SC_FORBIDDEN, "Error 403: Authorization failed");
    }

    private void executeURL(
        String testUrl, String userid, String password,
        boolean expectLoginFail, int expectedCode, String expectedContent)
        throws Exception {

        URI url = new URI(urlHttp + testUrl);
        HttpGet getMethod = new HttpGet(url);
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        SSLContext sslContext = SSLContext.getDefault();
        clientBuilder.setSSLContext(sslContext);
        clientBuilder.setDefaultRequestConfig(
            RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build());
        HttpClient client = clientBuilder.build();
        HttpResponse response = client.execute(getMethod);

        String loginBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        assertTrue(loginBody.contains("window.location.assign"),
            "Not redirected to home.html");
        String[] redirect = loginBody.split("'");

        HttpPost postMethod = new HttpPost(urlHttps + "/j_security_check");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("j_username", userid));
        nvps.add(new BasicNameValuePair("j_password", password));
        postMethod.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        response = client.execute(postMethod);
        assertEquals(HttpServletResponse.SC_FOUND,
            response.getStatusLine().getStatusCode(),
            "Expected " + HttpServletResponse.SC_FOUND + " status code for login");

        if (expectLoginFail) {
            String location = response.getFirstHeader("Location").getValue();
            assertTrue(location.contains("error.html"),
                "Error.html was not returned");
            return;
        }

        url = new URI(urlHttps + redirect[1]);
        getMethod = new HttpGet(url);
        response = client.execute(getMethod);
        assertEquals(expectedCode, response.getStatusLine().getStatusCode(),
            "Expected " + expectedCode + " status code for login");

        if (expectedCode != HttpServletResponse.SC_OK) {
            return;
        }

        String actual = EntityUtils.toString(response.getEntity(), "UTF-8");
        assertTrue(actual.contains(userid),
            "The actual content did not contain the userid \"" + userid
            + "\". It was:\n" + actual);
        assertTrue(actual.contains(expectedContent),
            "The url " + testUrl + " did not return the expected content \""
            + expectedContent + "\"" + "The actual content was:\n" + actual);
    }

}
```



The ***testAuthenticationFail()*** method tests an invalid user authentication while the ***testAuthorizationFail()*** method tests unauthorized access to the application.

The ***testAuthorizationForAdmin()*** and ***testAuthorizationForUser()*** methods verify that users with ***admin*** or ***user*** roles are properly authenticated and can access authorized resource.

### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

You see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.security.SecurityIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.78 sec - in it.io.openliberty.guides.security.SecurityIT

Results :

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

```

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran the server, or by typing ***q*** and then pressing the ***enter/return*** key.


::page{title="Summary"}

### Nice Work!

You learned how to use Jakarta EE Security in Open Liberty to authenticate and authorize users to secure your web application.


Next, you can try the related [MicroProfile JWT](https://openliberty.io/guides/microprofile-jwt.html) guide. It demonstrates technologies to secure backend services.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-security-intro*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-security-intro
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Securing%20a%20web%20application&guide-id=cloud-hosted-guide-security-intro)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-security-intro/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-security-intro/pulls)



### Where to next?

* [Securing microservices with JSON Web Tokens](https://openliberty.io/guides/microprofile-jwt.html)
* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
