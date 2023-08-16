---
markdown-version: v1
title: instructions
branch: lab-451-instruction
version-history-start-date: 2021-11-26 20:42:26 UTC
tool-type: theia
---
::page{title="Welcome to the Validating constraints with microservices guide!"}

Explore how to use bean validation to validate user input data for microservices.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You will learn the basics of writing and testing a microservice that uses bean validation and the new functionality of Bean Validation 2.0. The service uses bean validation to validate that the supplied JavaBeans meet the defined constraints.

Bean Validation is a Java specification that simplifies data validation and error checking. Bean validation uses a standard way to validate data stored in JavaBeans. Validation can be performed manually or with integration with other specifications and frameworks, such as Contexts and Dependency Injection (CDI), Java Persistence API (JPA), or JavaServer Faces (JSF). To set rules on data, apply constraints by using annotations or XML configuration files. Bean validation provides both built-in constraints and the ability to create custom constraints. Bean validation allows for validation of both JavaBean fields and methods. For method-level validation, both the input parameters and return value can be validated.

Several additional built-in constraints are included in Bean Validation 2.0, which reduces the need for custom validation in common validation scenarios. Some of the new built-in constraints include ***@Email***, ***@NotBlank***, ***@Positive***, and ***@Negative***. Also in Bean Validation 2.0, you can now specify constraints on type parameters.

The example microservice uses both field-level and method-level validation as well as several of the built-in constraints and a custom constraint.


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-bean-validation.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-bean-validation.git
cd guide-bean-validation
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

After you see the following message, your Liberty instance is ready:

```
The defaultServer server is ready to run a smarter planet.
```

Once your application is up and running, click the following button to check out your service by visiting the ***/openapi/ui*** endpoint.
::startApplication{port="9080" display="external" name="Visit OpenAPI UI" route="/openapi/ui"}
You see the OpenAPI user interface documenting the REST endpoints used in this guide. If you are interested in learning more about OpenAPI, read [Documenting RESTful APIs](https://openliberty.io/guides/microprofile-openapi.html). Expand the ***/beanvalidation/validatespacecraft POST request to validate your spacecraft bean*** section and click ***Try it out***. Copy the following example input into the text box:

```bash
{
  "astronaut": {
    "name": "Libby",
    "age": 25,
    "emailAddress": "libbybot@openliberty.io"
  },
  "destinations": {
    "Mars": 500
  },
  "serialNumber": "Liberty0001"
}
```

Click ***Execute*** and you receive the response ***No Constraint Violations*** because the values specified pass the constraints you will create in this guide. Now try copying the following value into the box:

```bash
{
  "astronaut": {
    "name": "Libby",
    "age": 12,
    "emailAddress": "libbybot@openliberty.io"
  },
  "destinations": {
    "Mars": 500
  },
  "serialNumber": "Liberty0001"
}
```

This time you receive ***Constraint Violation Found: must be greater than or equal to 18*** as a response because the age specified was under the minimum age of 18. Try other combinations of values to get a feel for the constraints that will be defined in this guide.

After you are finished checking out the application, stop the Liberty instance by pressing `Ctrl+C` in the command-line session where you ran Liberty. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```

::page{title="Applying constraints on the JavaBeans"}

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-bean-validation/start
```

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

First, create the JavaBeans to be constrained. 
Create the ***Astronaut*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/Astronaut.java
```


> Then, to open the Astronaut.java file in your IDE, select
> **File** > **Open** > guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/Astronaut.java, or click the following button

::openFile{path="/home/project/guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/Astronaut.java"}



```java
package io.openliberty.guides.beanvalidation;

import java.io.Serializable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public class Astronaut implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private String name;

    @Min(18)
    @Max(100)
    private Integer age;

    @Email
    private String emailAddress;

    public Astronaut() {
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The bean stores the attributes of an astronaut, ***name***, ***age***, and ***emailAddress***, and provides getters and setters to access and set the values.

The ***Astronaut*** class has the following constraints applied:

* The astronaut needs to have a name. Bean Validation 2.0 provides a built-in ***@NotBlank*** constraint, which ensures the value is not null and contains one character that isn't a blank space. The annotation constrains the ***name*** field.

* The email supplied needs to be a valid email address. Another built-in constraint in Bean Validation 2.0 is ***@Email***, which can validate that the ***Astronaut*** bean includes a correctly formatted email address. The annotation constrains the ***emailAddress*** field.

* The astronaut needs to be between 18 and 100 years old. Bean validation allows you to specify multiple constraints on a single field. The ***@Min*** and ***@Max*** built-in constraints applied to the ***age*** field check that the astronaut is between the ages of 18 and 100.

In this example, the annotation is on the field value itself. You can also place the annotation on the getter method, which has the same effect.

Create the ***Spacecraft*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/Spacecraft.java
```


> Then, to open the Spacecraft.java file in your IDE, select
> **File** > **Open** > guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/Spacecraft.java, or click the following button

::openFile{path="/home/project/guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/Spacecraft.java"}



```java
package io.openliberty.guides.beanvalidation;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;

@Named
@RequestScoped
public class Spacecraft implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    private Astronaut astronaut;

    private Map<@NotBlank String, @Positive Integer> destinations;

    @SerialNumber
    private String serialNumber;

    public Spacecraft() {
        destinations = new HashMap<String, Integer>();
    }

    public void setAstronaut(Astronaut astronaut) {
        this.astronaut = astronaut;
    }

    public void setDestinations(Map<String, Integer> destinations) {
        this.destinations = destinations;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Astronaut getAstronaut() {
        return astronaut;
    }

    public Map<String, Integer> getDestinations() {
        return destinations;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    @AssertTrue
    public boolean launchSpacecraft(@NotNull String launchCode) {
        if (launchCode.equals("OpenLiberty")) {
            return true;
        }
        return false;
    }
}
```




The ***Spacecraft*** bean contains 3 fields, ***astronaut***, ***serialNumber***, and ***destinations***. The JavaBean needs to be a CDI managed bean to allow for method-level validation, which uses CDI interceptions. Because the ***Spacecraft*** bean is a CDI managed bean, a scope is necessary. A request scope is used in this example. To learn more about CDI, see [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html).

The ***Spacecraft*** class has the following constraints applied:

* Every destination that is specified needs a name and a positive distance. In Bean Validation 2.0, you can specify constraints on type parameters. The ***@NotBlank*** and ***@Positive*** annotations constrain the ***destinations*** map so that the destination name is not blank, and the distance is positive. The ***@Positive*** constraint ensures that numeric value fields are greater than 0.

* A correctly formatted serial number is required. In addition to specifying the built-in constraints, you can create custom constraints to allow user-defined validation rules. The ***@SerialNumber*** annotation that constrains the ***serialNumber*** field is a custom constraint, which you will create later.

Because you already specified constraints on the ***Astronaut*** bean, the constraints do not need to be respecified in the ***Spacecraft*** bean. Instead, because of the ***@Valid*** annotation on the field, all the nested constraints on the ***Astronaut*** bean are validated.

You can also use bean validation with CDI to provide method-level validation. The ***launchSpacecraft()*** method on the ***Spacecraft*** bean accepts a ***launchCode*** parameter, and if the ***launchCode*** parameter is ***OpenLiberty***, the method returns ***true*** that the spacecraft is launched. Otherwise, the method returns ***false***. The ***launchSpacecraft()*** method uses both parameter and return value validation. The ***@NotNull*** constraint eliminates the need to manually check within the method that the parameter is not null. Additionally, the method has the ***@AssertTrue*** return-level constraint to enforce that the method must return the ***true*** boolean.

::page{title="Creating custom constraints"}

To create the custom constraint for ***@SerialNumber***, begin by creating an annotation.


Replace the annotation.

> To open the SerialNumber.java file in your IDE, select
> **File** > **Open** > guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/SerialNumber.java, or click the following button

::openFile{path="/home/project/guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/SerialNumber.java"}



```java
package io.openliberty.guides.beanvalidation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ FIELD })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { SerialNumberValidator.class })
public @interface SerialNumber {

    String message() default "serial number is not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```




The ***@Target*** annotation indicates the element types to which you can apply the custom constraint. Because the ***@SerialNumber*** constraint is used only on a field, only the ***FIELD*** target is specified.

When you define a constraint annotation, the specification requires the ***RUNTIME*** retention policy.

The ***@Constraint*** annotation specifies the class that contains the validation logic for the custom constraint.

In the ***SerialNumber*** body, the ***message()*** method provides the message that is output when a validation constraint is violated. The ***groups()*** and ***payload()*** methods associate this constraint only with certain groups or payloads. The defaults are used in the example.

Now, create the class that provides the validation for the ***@SerialNumber*** constraint.

Replace the ***SerialNumberValidator*** class.

> To open the SerialNumberValidator.java file in your IDE, select
> **File** > **Open** > guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/SerialNumberValidator.java, or click the following button

::openFile{path="/home/project/guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/SerialNumberValidator.java"}



```java
package io.openliberty.guides.beanvalidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SerialNumberValidator
    implements ConstraintValidator<SerialNumber, Object> {

    @Override
    public boolean isValid(Object arg0, ConstraintValidatorContext arg1) {
        boolean isValid = false;
        if (arg0 == null) {
            return isValid;
        }
        String serialNumber = arg0.toString();
        isValid = serialNumber.length() == 11 && serialNumber.startsWith("Liberty");
        try {
            Integer.parseInt(serialNumber.substring(7));
        } catch (Exception ex) {
            isValid = false;
        }
        return isValid;
    }
}
```




The ***SerialNumberValidator*** class has one method, ***isValid()***, which contains the custom validation logic. In this case, the serial number must start with ***Liberty*** followed by 4 numbers, such as ***Liberty0001***. If the supplied serial number matches the constraint, ***isValid()*** returns ***true***. If the serial number does not match, it returns ***false***.

::page{title="Programmatically validating constraints"}

Next, create a service to programmatically validate the constraints on the ***Spacecraft*** and ***Astronaut*** JavaBeans.



Create the ***BeanValidationEndpoint*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/BeanValidationEndpoint.java
```


> Then, to open the BeanValidationEndpoint.java file in your IDE, select
> **File** > **Open** > guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/BeanValidationEndpoint.java, or click the following button

::openFile{path="/home/project/guide-bean-validation/start/src/main/java/io/openliberty/guides/beanvalidation/BeanValidationEndpoint.java"}



```java
package io.openliberty.guides.beanvalidation;

import java.util.Set;

import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/")
public class BeanValidationEndpoint {

    @Inject
    Validator validator;

    @Inject
    Spacecraft bean;

    @POST
    @Path("/validatespacecraft")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "POST request to validate your spacecraft bean")
    public String validateSpacecraft(
        @RequestBody(description = "Specify the values to create the "
                + "Astronaut and Spacecraft beans.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Spacecraft.class)))
        Spacecraft spacecraft) {

            Set<ConstraintViolation<Spacecraft>> violations
                = validator.validate(spacecraft);

            if (violations.size() == 0) {
                return "No Constraint Violations";
            }

            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<Spacecraft> violation : violations) {
                sb.append("Constraint Violation Found: ")
                .append(violation.getMessage())
                .append(System.lineSeparator());
            }
            return sb.toString();
    }

    @POST
    @Path("/launchspacecraft")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "POST request to specify a launch code")
    public String launchSpacecraft(
        @RequestBody(description = "Enter the launch code.  Must not be "
                + "null and must equal OpenLiberty for successful launch.",
            content = @Content(mediaType = "text/plain"))
        String launchCode) {
            try {
                bean.launchSpacecraft(launchCode);
                return "launched";
            } catch (ConstraintViolationException ex) {
                return ex.getMessage();
            }
    }
}
```




Two resources, a validator and an instance of the ***Spacecraft*** JavaBean, are injected into the class. The default validator is used and is obtained through CDI injection. However, you can also obtain the default validator with resource injection or a JNDI lookup. The ***Spacecraft*** JavaBean is injected so that the method-level constraints can be validated.

The programmatic validation takes place in the ***validateSpacecraft()*** method. To validate the data, the ***validate()*** method is called on the ***Spacecraft*** bean. Because the ***Spacecraft*** bean contains the ***@Valid*** constraint on the ***Astronaut*** bean, both JavaBeans are validated. Any constraint violations found during the call to the ***validate()*** method are returned as a set of ***ConstraintViolation*** objects.

The method level validation occurs in the ***launchSpacecraft()*** method. A call is then made to the ***launchSpacecraft()*** method on the ***Spacecraft*** bean, which throws a ***ConstraintViolationException*** exception if either of the method-level constraints is violated.

::page{title="Enabling the Bean Validation feature"}

Finally, add the Bean Validation feature in the application by updating the Liberty ***server.xml*** configuration file.

Replace the Liberty ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-bean-validation/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-bean-validation/start/src/main/liberty/config/server.xml"}



```xml
<server description="Liberty Server for Bean Validation Guide">

    <featureManager>
        <feature>beanValidation-3.0</feature>
        <feature>cdi-4.0</feature>
        <feature>restfulWS-3.1</feature>
        <feature>jsonb-3.0</feature>
        <feature>mpOpenAPI-3.1</feature>
    </featureManager>

    <variable name="default.http.port" defaultValue="9080"/>
    <variable name="default.https.port" defaultValue="9443"/>
    <variable name="app.context.root" defaultValue="Spacecraft"/>

    <httpEndpoint httpPort="${default.http.port}" httpsPort="${default.https.port}"
        id="defaultHttpEndpoint" host="*" />

   <webApplication location="guide-bean-validation.war" contextRoot="${app.context.root}"/>
</server>
```



You can now use the ***beanValidation*** feature to validate that the supplied JavaBeans meet the defined constraints.


::page{title="Running the application"}

You started the Open Liberty in dev mode at the beginning of the guide, so all the changes were automatically picked up.

Click the following button to check out your service by visiting the ***/openapi/ui*** endpoint:
::startApplication{port="9080" display="external" name="Visit OpenAPI UI" route="/openapi/ui"}
Expand the ***/beanvalidation/validatespacecraft POST request to validate your spacecraft bean*** section and click ***Try it out***. Copy the following example input into the text box:

```bash
{
  "astronaut": {
    "name": "Libby",
    "age": 25,
    "emailAddress": "libbybot@openliberty.io"
  },
  "destinations": {
    "Mars": 500
  },
  "serialNumber": "Liberty0001"
}
```

Click ***Execute*** and you receive the response ***No Constraint Violations*** because the values specified pass previously defined constraints.

Next, modify the following values, all of which break the previously defined constraints:

```
Age = 10
Email = libbybot
SerialNumber = Liberty1
```

After you click ***Execute***, the response contains the following constraint violations:

```
Constraint Violation Found: serial number is not valid.
Constraint Violation Found: must be greater than or equal to 18
Constraint Violation Found: must be a well-formed email address
```

To try the method-level validation, expand the ***/beanvalidation/launchspacecraft POST request to specify a launch code*** section. Enter ***OpenLiberty*** in the text box. Note that ***launched*** is returned because the launch code passes the defined constraints. Replace ***OpenLiberty*** with anything else to note that a constraint violation is returned.


::page{title="Testing the constraints"}

Now, write automated tests to drive the previously created service. 

Create ***BeanValidationIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-bean-validation/start/src/test/java/it/io/openliberty/guides/beanvalidation/BeanValidationIT.java
```


> Then, to open the BeanValidationIT.java file in your IDE, select
> **File** > **Open** > guide-bean-validation/start/src/test/java/it/io/openliberty/guides/beanvalidation/BeanValidationIT.java, or click the following button

::openFile{path="/home/project/guide-bean-validation/start/src/test/java/it/io/openliberty/guides/beanvalidation/BeanValidationIT.java"}



```java
package it.io.openliberty.guides.beanvalidation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.openliberty.guides.beanvalidation.Astronaut;
import io.openliberty.guides.beanvalidation.Spacecraft;

import java.util.HashMap;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BeanValidationIT {

    private Client client;
    private static String port;

    @BeforeEach
    public void setup() {
        client = ClientBuilder.newClient();
        port = System.getProperty("http.port");
    }

    @AfterEach
    public void teardown() {
        client.close();
    }

    @Test
    public void testNoFieldLevelConstraintViolations() throws Exception {
        Astronaut astronaut = new Astronaut();
        astronaut.setAge(25);
        astronaut.setEmailAddress("libby@openliberty.io");
        astronaut.setName("Libby");
        Spacecraft spacecraft = new Spacecraft();
        spacecraft.setAstronaut(astronaut);
        spacecraft.setSerialNumber("Liberty1001");
        HashMap<String, Integer> destinations = new HashMap<String, Integer>();
        destinations.put("Mars", 1500);
        destinations.put("Pluto", 10000);
        spacecraft.setDestinations(destinations);

        Jsonb jsonb = JsonbBuilder.create();
        String spacecraftJSON = jsonb.toJson(spacecraft);
        Response response = postResponse(getURL(port, "validatespacecraft"),
                spacecraftJSON, false);
        String actualResponse = response.readEntity(String.class);
        String expectedResponse = "No Constraint Violations";

        assertEquals(expectedResponse, actualResponse,
                "Unexpected response when validating beans.");
    }

    @Test
    public void testFieldLevelConstraintViolation() throws Exception {
        Astronaut astronaut = new Astronaut();
        astronaut.setAge(25);
        astronaut.setEmailAddress("libby");
        astronaut.setName("Libby");

        Spacecraft spacecraft = new Spacecraft();
        spacecraft.setAstronaut(astronaut);
        spacecraft.setSerialNumber("Liberty123");

        HashMap<String, Integer> destinations = new HashMap<String, Integer>();
        destinations.put("Mars", -100);
        spacecraft.setDestinations(destinations);

        Jsonb jsonb = JsonbBuilder.create();
        String spacecraftJSON = jsonb.toJson(spacecraft);
        Response response = postResponse(getURL(port, "validatespacecraft"),
                spacecraftJSON, false);
        String actualResponse = response.readEntity(String.class);
        String expectedDestinationResponse = "must be greater than 0";
        assertTrue(actualResponse.contains(expectedDestinationResponse),
                "Expected response to contain: " + expectedDestinationResponse);
        String expectedEmailResponse = "must be a well-formed email address";
        assertTrue(actualResponse.contains(expectedEmailResponse),
                "Expected response to contain: " + expectedEmailResponse);
        String expectedSerialNumberResponse = "serial number is not valid";
        assertTrue(actualResponse.contains(expectedSerialNumberResponse),
                "Expected response to contain: " + expectedSerialNumberResponse);
    }

    @Test
    public void testNoMethodLevelConstraintViolations() throws Exception {
        String launchCode = "OpenLiberty";
        Response response = postResponse(getURL(port, "launchspacecraft"),
                launchCode, true);

        String actualResponse = response.readEntity(String.class);
        String expectedResponse = "launched";

        assertEquals(expectedResponse, actualResponse,
                "Unexpected response from call to launchSpacecraft");

    }

    @Test
    public void testMethodLevelConstraintViolation() throws Exception {
        String launchCode = "incorrectCode";
        Response response = postResponse(getURL(port, "launchspacecraft"),
                launchCode, true);

        String actualResponse = response.readEntity(String.class);
        assertTrue(
                actualResponse.contains("must be true"),
                "Unexpected response from call to launchSpacecraft");
    }

    private Response postResponse(String url, String value,
                                  boolean isMethodLevel) {
        if (isMethodLevel) {
                return client.target(url).request().post(Entity.text(value));
        } else {
                return client.target(url).request().post(Entity.entity(value,
                MediaType.APPLICATION_JSON));
        }
    }

    private String getURL(String port, String function) {
        return "http://localhost:" + port + "/Spacecraft/beanvalidation/"
                + function;
    }
}
```




The ***@BeforeEach*** annotation causes the ***setup()*** method to execute before the test cases. The ***setup()*** method retrieves the port number for the Open Liberty and creates a ***Client*** that is used throughout the tests, which are described as follows:

* The ***testNoFieldLevelConstraintViolations()*** test case verifies that constraint violations do not occur when valid data is supplied to the ***Astronaut*** and ***Spacecraft*** bean attributes.

* The ***testFieldLevelConstraintViolation()*** test case verifies that the appropriate constraint violations occur when data that is supplied to the ***Astronaut*** and ***Spacecraft*** attributes violates the defined constraints. Because 3 constraint violations are defined, 3 ***ConstraintViolation*** objects are returned as a set from the ***validate*** call. The 3 expected messages are ***"must be greater than 0"*** for the negative distance specified in the destination map, ***"must be a well-formed email address"*** for the incorrect email address, and the custom ***"serial number is not valid"*** message for the serial number.

* The ***testNoMethodLevelConstraintViolations()*** test case verifies that the method-level constraints that are specified on the ***launchSpacecraft()*** method of the ***Spacecraft*** bean are validated when the method is called with no violations. In this test, the call to the ***launchSpacecraft()*** method is made with the ***OpenLiberty*** argument. A value of ***true*** is returned, which passes the specified constraints.

* The ***testMethodLevelConstraintViolation()*** test case verifies that a ***ConstraintViolationException*** exception is thrown when one of the method-level constraints is violated. A call with an incorrect parameter, ***incorrectCode***, is made to the ***launchSpacecraft()*** method of the ***Spacecraft*** bean. The method returns ***false***, which violates the defined constraint, and a ***ConstraintViolationException*** exception is thrown. The exception includes the constraint violation message, which in this example is ***must be true***.


### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.beanvalidation.BeanValidationIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.493 sec - in
it.io.openliberty.guides.beanvalidation.BeanValidationIT

Results :

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran Liberty, or by typing ***q*** and then pressing the ***enter/return*** key.

::page{title="Summary"}

### Nice Work!

You developed and tested a Java microservice by using bean validation and Open Liberty.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-bean-validation*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-bean-validation
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Validating%20constraints%20with%20microservices&guide-id=cloud-hosted-guide-bean-validation)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-bean-validation/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-bean-validation/pulls)



### Where to next?

* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
