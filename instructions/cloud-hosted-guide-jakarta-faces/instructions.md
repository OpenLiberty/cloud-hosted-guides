---
markdown-version: v1
tool-type: theia
---
::page{title="Welcome to the Building a dynamic web application with integrated user interface and backend logic guide!"}

Learn how to build a dynamic web application using Jakarta Faces, Jakarta Contexts and Dependency Injection, and Jakarta Expression Language.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You'll learn how to build a dynamic web application using Jakarta Faces for the user interface (UI), Jakarta Contexts and Dependency Injection (CDI) for managing backend logic, and Jakarta Expression Language (EL) for data binding.

Jakarta Faces is a framework for building component-based web applications that simplifies UI development by managing reusable components, handling user interactions, and binding data to backend logic. It provides built-in lifecycle management, event handling, and server-side validation, reducing the need for manual request processing. Jakarta Faces also includes tag libraries that allows developers define UI components using markup and connect them to backend objects without writing repetitive setup code.

To further streamline development, Jakarta Faces works with CDI to manage backend components. CDI allows beans to be automatically created and injected where needed, making it easier to manage application logic. Jakarta Expression Language enables data binding between the UI and backend, allowing UI components to dynamically display data and trigger backend actions.

The application you will build in this guide is a dynamic web application that displays system load data on demand. Using Jakarta Faces for the UI, you'll create a table to show the system CPU load and heap memory usage. You'll also learn how to use CDI to provide the system load data from a managed bean, and to use Jakarta Expression Language to bind this data to the UI components.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-jakarta-faces.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-jakarta-faces.git
cd guide-jakarta-faces
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed. 

To try out the application, first go to the ***finish*** directory and run Maven with the ***liberty:run*** goal to build the application and deploy it to Open Liberty:

```bash
cd finish
mvn liberty:run
```

After you see the following message, your Liberty instance is ready.

```
The defaultServer server is ready to run a smarter planet.
```

Check out the web application at the ***http\://localhost:9080/index.xhtml*** URL. Click the image:refresh.png[refresh icon, 18, 18] refresh button, located next to the table title, to update and display the latest system load data in the table.

After you are finished checking out the application, stop the Liberty instance by pressing `Ctrl+C` in the command-line session where you ran Liberty. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```

::page{title="Creating a static Jakarta Faces page"}

Start by creating a page that displays an empty table by using Jakarta Faces to extend standard HTML. The table will display the system load data and serves as the starting point for your application.

Navigate to the ***start*** directory to begin.

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), dev mode listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```bash
mvn liberty:dev
```

After you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.

Create the index.xhtml file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jakarta-faces/start/src/main/webapp/index.xhtml
```


> Then, to open the index.xhtml file in your IDE, select
> **File** > **Open** > guide-jakarta-faces/start/src/main/webapp/index.xhtml, or click the following button

::openFile{path="/home/project/guide-jakarta-faces/start/src/main/webapp/index.xhtml"}



```
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:f="jakarta.faces.core"
      xmlns:ui="jakarta.faces.facelets">

  <h:head>
    <meta charset="UTF-8" />
    <title>Open Liberty - Jakarta Faces Example</title>
    <h:outputStylesheet library="css" name="styles.css" />
    <link href="favicon.ico" rel="icon" />
    <link href="favicon.ico" rel="shortcut icon" />
  </h:head>
  <h:body>
    <section id="appIntro">
      <div id="titleSection">
        <h1 id="appTitle">Jakarta Faces Example</h1>
        <div class="line"></div>
        <div class="headerImage"></div>
      </div>

      <div class="msSection" id="systemLoads">
        <div class="headerRow">
          <div class="headerIcon">
            <img src="#{resource['img/sysProps.svg']}" />
          </div>
          <div class="headerTitleWithButton" id="sysPropTitle">
            <h2>System Loads</h2>
          </div>
        </div>
        <div class="sectionContent">
          <h:dataTable id="systemLoadsTable">
            <h:column>
              <f:facet name="header">Time</f:facet>
            </h:column>
            <h:column>
              <f:facet name="header">CPU Load (%)</f:facet>
            </h:column>
            <h:column>
              <f:facet name="header">Heap Memory Usage (%)</f:facet>
            </h:column>
          </h:dataTable>
        </div>
      </div>
    </section>
    <ui:include src="/WEB-INF/includes/footer.xhtml" />
  </h:body>
</html>
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.



In the ***index.xhtml*** file, the ***xmlns*** attributes define the XML namespaces for various Jakarta Faces tag libraries. These namespaces allow the page to use Jakarta Faces tags for templating, creating UI components, and enabling core functionality, such as form submissions and data binding. For more information on the various tag libraries and their roles in Jakarta Faces, refer to the [Jakarta Faces Tag Libraries](https://jakarta.ee/learn/docs/jakartaee-tutorial/current/web/faces-facelets/faces-facelets.html#_tag_libraries_supported_by_facelets) and the [VDL Documentation Generator](https://jakarta.ee/specifications/faces/4.0/vdldoc) documentation.

The ***index.xhtml*** file combines standard HTML elements with Jakarta Faces components, providing both static layout and dynamic functionality. Standard HTML elements, like ***div*** and ***section***, structure the page's layout. Jakarta Faces tags offer additional features beyond standard HTML, such as managing UI components, including resources, and binding data. For example, the ***h:outputStylesheet*** tag loads a CSS file for styling, and the ***ui:include*** tag incorporates reusable components, such as the provided ***footer.xhtml*** file, to streamline maintenance and reuse across multiple pages. The ***h:dataTable*** tag is used to display a table.

At this point, the page defines a table that has no data entries. We'll add dynamic content in the following steps.

::page{title="Configuring the Faces Servlet"}

Before you can access the Jakarta Faces page, you need to configure a Faces servlet in your application. This servlet handles all requests for ***.xhtml*** pages and processes them using Jakarta Faces.

Create the web.xml file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jakarta-faces/start/src/main/webapp/WEB-INF/web.xml
```


> Then, to open the web.xml file in your IDE, select
> **File** > **Open** > guide-jakarta-faces/start/src/main/webapp/WEB-INF/web.xml, or click the following button

::openFile{path="/home/project/guide-jakarta-faces/start/src/main/webapp/WEB-INF/web.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <context-param>
        <param-name>jakarta.faces.PROJECT_STAGE</param-name>
        <param-value>Development</param-value>
    </context-param>

    <!-- Faces Servlet Configuration -->
    <servlet>
        <servlet-name>Faces Servlet</servlet-name>
        <servlet-class>jakarta.faces.webapp.FacesServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <!-- Servlet Mapping -->
    <servlet-mapping>
        <servlet-name>Faces Servlet</servlet-name>
        <url-pattern>*.xhtml</url-pattern>
    </servlet-mapping>

</web-app>
```



The ***servlet*** element defines the Faces servlet that is responsible for processing requests for Jakarta Faces pages. The ***load-on-startup*** element with a value of ***1*** specifies that the servlet is loaded and initialized first when the application starts.

The ***servlet-mapping*** element specifies which URL patterns are routed to the Faces servlet. In this case, all URLs ending with ***.xhtml*** are mapped to be processed by Jakarta Faces. This ensures that any request for an ***.xhtml*** page is handled by the Faces servlet, which manages the lifecycle of Jakarta Faces components, processes the page, and renders the output. 

By configuring both the servlet and the servlet mapping, you're ensuring that Jakarta Faces pages are properly processed and delivered in response to user requests.

The ***jakarta.faces.PROJECT_STAGE*** context parameter determines the current stage of the application in its development lifecycle. Because it is currently set to ***Development***, you will see additional debugging information, including developer-friendly warning messages such as ***WARNING: Apache MyFaces Core is running in DEVELOPMENT mode.*** For more information about valid values and how to set the ***PROJECT_STAGE*** parameter, see the official [Jakarta Faces ProjectStage documentation](https://jakarta.ee/specifications/faces/4.1/apidocs/jakarta.faces/jakarta/faces/application/projectstage).

In your dev mode console, type ***r*** and press the ***enter/return*** key to restart the Liberty instance so that Liberty reads the configuration changes. When you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

Check out the web application that you created at the ***http\://localhost:9080/index.xhtml*** URL. You should see the static page with the system loads table displaying only the headers and no data.

::page{title="Implementing backend logic with dependency injection"}

To provide system load data to your web application, you'll create a CDI-managed bean that retrieves information about the system CPU load and memory usage. This bean is accessible from the Jakarta Faces page and supplies the data that is displayed.

Create the SystemLoadBean class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jakarta-faces/start/src/main/java/io/openliberty/guides/bean/SystemLoadBean.java
```


> Then, to open the SystemLoadBean.java file in your IDE, select
> **File** > **Open** > guide-jakarta-faces/start/src/main/java/io/openliberty/guides/bean/SystemLoadBean.java, or click the following button

::openFile{path="/home/project/guide-jakarta-faces/start/src/main/java/io/openliberty/guides/bean/SystemLoadBean.java"}



```java
package io.openliberty.guides.bean;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import com.sun.management.OperatingSystemMXBean;

import io.openliberty.guides.bean.model.SystemLoadData;

@Named("systemLoadBean")
@ApplicationScoped
public class SystemLoadBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<SystemLoadData> systemLoads;

    private static final OperatingSystemMXBean OS =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    private static final MemoryMXBean MEM =
        ManagementFactory.getMemoryMXBean();

    @PostConstruct
    public void init() {
        systemLoads = new ArrayList<>();
        fetchSystemLoad();
    }

    public void fetchSystemLoad() {
        String time = Calendar.getInstance().getTime().toString();

        double cpuLoad = OS.getCpuLoad() * 100;

        long heapMax = MEM.getHeapMemoryUsage().getMax();
        long heapUsed = MEM.getHeapMemoryUsage().getUsed();
        double memoryUsage = heapUsed * 100.0 / heapMax;

        SystemLoadData data = new SystemLoadData(time, cpuLoad, memoryUsage);

        systemLoads.add(data);
    }

    public List<SystemLoadData> getSystemLoads() {
        return systemLoads;
    }
}
```



Annotate the ***SystemLoadBean*** class with a ***@Named*** annotation to make it accessible in the Jakarta Faces pages under the ***systemLoadBean*** name. Because the ***SystemLoadBean*** bean is a CDI-managed bean, a scope is necessary. Annotating it with the ***@ApplicationScoped*** annotation indicates that it is initialized once and is shared between all requests while the application runs. To learn more about CDI, see the [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html) guide.

The ***@PostConstruct*** annotation ensures the ***init()*** method runs after the ***SystemLoadBean*** is initialized and dependencies are injected. The ***init()*** method sets up any required resources for the bean's lifecyccle.

The ***fetchSystemLoad()*** method retrieves the current system load and memory usage, then updates the list of system load data.

The ***getSystemLoads()*** method is a getter method for accessing the list of system load data from the Jakarta Faces page.

::page{title="Binding data to the UI with expression language"}

Now that you have implemented the backend logic with CDI, you'll update the Jakarta Faces page to display the dynamic system load data. You'll do this by using Jakarta Expression Language to bind the UI components to the backend data.

Replace the index.xhtml file.

> To open the index.xhtml file in your IDE, select
> **File** > **Open** > guide-jakarta-faces/start/src/main/webapp/index.xhtml, or click the following button

::openFile{path="/home/project/guide-jakarta-faces/start/src/main/webapp/index.xhtml"}



```
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:f="jakarta.faces.core"
      xmlns:ui="jakarta.faces.facelets">
  <h:head>
    <meta charset="UTF-8" />
    <title>Open Liberty - Jakarta Faces Example</title>
    <h:outputStylesheet library="css" name="styles.css" />
    <link href="favicon.ico" rel="icon" />
    <link href="favicon.ico" rel="shortcut icon" />
  </h:head>
  <h:body>
    <section id="appIntro">
      <div id="titleSection">
        <h1 id="appTitle">Jakarta Faces Example</h1>
        <div class="line"></div>
        <div class="headerImage"></div>
      </div>

      <div class="msSection" id="systemLoads">
        <h:form id="systemLoadForm">
          <div class="headerRow">
            <div class="headerIcon">
              <img src="#{resource['img/sysProps.svg']}" />
            </div>
            <div class="headerTitleWithButton" id="sysPropTitle">
              <h2>System Loads</h2>
              <h:commandButton id="refreshButton" styleClass="refreshButton" value=""
                               title="Refresh system load data"
                               action="#{systemLoadBean.fetchSystemLoad}" >
                <f:ajax render="systemLoadForm" />
              </h:commandButton>
            </div>
          </div>
          <div class="sectionContent">
            <h:dataTable id="systemLoadsTable"
                         value="#{systemLoadBean.systemLoads}"
                         var="systemLoadData"
                         styleClass = "systemLoadsTable"
                         headerClass = "systemLoadsTableHeader"
                         rowClasses = "systemLoadsTableOddRow,systemLoadsTableEvenRow">
              <h:column>
                <f:facet name="header">Time</f:facet>
                <h:outputText value="#{systemLoadData.time}" />
              </h:column>

              <h:column>
                <f:facet name="header">CPU Load (%)</f:facet>
                <h:outputText
                  value="#{systemLoadData.cpuLoad == null ? '-' : systemLoadData.cpuLoad}">
                  <f:convertNumber pattern="#0.0000000" />
                </h:outputText>
              </h:column>

              <h:column>
                <f:facet name="header">Heap Memory Usage (%)</f:facet>
                <h:outputText
                  value="#{systemLoadData.memoryUsage == null ? '-' : systemLoadData.memoryUsage}">
                  <f:convertNumber pattern="#0.00" />
                </h:outputText>
              </h:column>
            </h:dataTable>
          </div>
        </h:form>
      </div>
    </section>
    <ui:include src="/WEB-INF/includes/footer.xhtml" />
  </h:body>
</html>
```





The ***index.xhtml*** uses an ***h:commandButton*** tag to create the refresh button. When the button is clicked, the ***#{systemLoadBean.fetchSystemLoad}*** action invokes the ***fetchSystemLoad()*** method using Jakarta Expression Language. This expression references the ***systemLoadBean*** managed bean, triggering the method to update the system load data. The ***f:ajax*** tag ensures that the ***systemLoadForm*** component is re-rendered without requiring a full page reload.

The ***systemLoadsTable*** is populated using the ***h:dataTable*** tag, which iterates over the list of system load data provided by the ***systemLoadBean***. The ***#{systemLoadBean.systemLoads}*** expression calls the ***getSystemLoads()*** method from the managed bean, binding the data to the UI components. If the ***systemLoadBean*** isn't created yet, it is automatically initialized at this point. For each entry, the ***time***, ***cpuLoad***, and ***memoryUsage*** fields are displayed by using the ***h:outputText*** tag. The ***f:convertNumber*** tag formats ***cpuLoad*** to seven decimal places and ***memoryUsage*** to two decimal places.

To format the table, set the ***styleClass***, ***headerClass***, and ***rowClasses*** attributes in the ***h:dataTable*** tag. The style elements are defined in the ***src/main/webapp/resources/css/styles.css*** file.

::page{title="Running the application"}


The required ***faces***, ***expressionLanguage***, and ***cdi*** features are enabled for you in the Liberty ***server.xml*** configuration file.

Because you started the Open Liberty in dev mode at the beginning of the guide, all the changes were automatically picked up.

Navigate to the ***http\://localhost:9080/index.xhtml*** URL to view your web application. Click on the image:refresh.png[refresh icon, 18, 18] refresh button to trigger an update on the system loads table.

::page{title="Testing the application"}

While you can manually verify the web application by visiting ***http\://localhost:9080/index.xhtml,*** automated tests are a much better approach because they are more reliable and trigger a failure if a breaking change is introduced. You can write unit tests for your CDI bean to ensure that the basic operations you implemented function correctly.

Create the SystemLoadBeanTest class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jakarta-faces/start/src/test/java/io/openliberty/guides/bean/SystemLoadBeanTest.java
```


> Then, to open the SystemLoadBeanTest.java file in your IDE, select
> **File** > **Open** > guide-jakarta-faces/start/src/test/java/io/openliberty/guides/bean/SystemLoadBeanTest.java, or click the following button

::openFile{path="/home/project/guide-jakarta-faces/start/src/test/java/io/openliberty/guides/bean/SystemLoadBeanTest.java"}



```java
package io.openliberty.guides.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.openliberty.guides.bean.model.SystemLoadData;

public class SystemLoadBeanTest {

    private SystemLoadBean systemLoadBean;

    @BeforeEach
    public void setUp() {
        systemLoadBean = new SystemLoadBean();
        systemLoadBean.init();
    }

    @Test
    public void testInitMethod() {
        assertNotNull(systemLoadBean.getSystemLoads(),
                      "System loads should not be null after initialization");
        assertFalse(systemLoadBean.getSystemLoads().isEmpty(),
                    "System loads should not be empty after initialization");
    }

    @Test
    public void testFetchSystemLoad() {
        int initialSize = systemLoadBean.getSystemLoads().size();
        systemLoadBean.fetchSystemLoad();
        int newSize = systemLoadBean.getSystemLoads().size();
        assertEquals(initialSize + 1, newSize,
                     "System loads size should increase by 1 after fetching new data");
    }

    @Test
    public void testDataIntegrity() {
        systemLoadBean.fetchSystemLoad();
        SystemLoadData data = systemLoadBean.getSystemLoads().get(0);
        assertNotNull(data.getTime(), "Time should not be null");
        assertNotNull(data.getCpuLoad(), "Recent load should not be null");
        assertNotNull(data.getMemoryUsage(), "Memory usage should not be null");
    }
}
```



The ***setUp()*** method is annotated with the ***@BeforeEach*** annotation, indicating that it is run before each test case to ensure a clean state for each test execution. In this case, it creates a new instance of ***SystemLoadBean*** and manually calls the ***init()*** method to initialize the list of system load data before each test.

The ***testInitMethod()*** test case verifies that after initializing ***SystemLoadBean***, the list of system load data is not null and contains at least one entry.

The ***testFetchSystemLoad()*** test case verifies that after calling the ***fetchSystemLoad()*** method, the size of the list of system load data increases by one.

The ***testDataIntegrity()*** test case verifies that each ***SystemLoadData*** entry in the list of system load data contains valid values for ***time***, ***cpuLoad***, and ***memoryUsage***.

### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

You see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running io.openliberty.guides.bean.SystemLoadBeanTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.037 s -- in io.openliberty.guides.bean.SystemLoadBeanTest

Results:

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran Liberty.


::page{title="Summary"}

### Nice Work!

You just built a dynamic web application on Open Liberty by using Jakarta Faces for the user interface, CDI for managing beans, and Jakarta Expression Language for binding and handling data.




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-jakarta-faces*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-jakarta-faces
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Building%20a%20dynamic%20web%20application%20with%20integrated%20user%20interface%20and%20backend%20logic&guide-id=cloud-hosted-guide-jakarta-faces)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-jakarta-faces/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-jakarta-faces/pulls)



### Where to next?

* [Streaming messages between client and server services using gRPC](https://openliberty.io/guides/grpc-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
