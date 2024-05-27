---
markdown-version: v1
title: instructions
branch: lab-5932-instruction
version-history-start-date: 2023-04-14T18:24:15Z
tool-type: theia
---
::page{title="Welcome to the Accessing and persisting data in microservices using Java Persistence API (JPA) guide!"}

Learn how to use Java Persistence API (JPA) to access and persist data to a database for your microservices.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.





::page{title="What you'll learn"}

You will learn how to use the Java Persistence API (JPA) to map Java objects to relational database tables and perform create, read, update and delete (CRUD) operations on the data in your microservices. 

JPA is a Jakarta EE specification for representing relational database table data as Plain Old Java Objects (POJO). JPA simplifies object-relational mapping (ORM) by using annotations to map Java objects to tables in a relational database. In addition to providing an efficient API for performing CRUD operations, JPA also reduces the burden of having to write JDBC and SQL code when performing database operations and takes care of database vendor-specific differences. This capability allows you to focus on the business logic of your application instead of wasting time implementing repetitive CRUD logic.

The application that you will be working with is an event manager, which is composed of a UI and an event microservice for creating, retrieving, updating, and deleting events. In this guide, you will be focused on the event microservice. The event microservice consists of a JPA entity class whose fields will be persisted to a database. The database logic is implemented in a Data Access Object (DAO) to isolate the database operations from the rest of the service. This DAO accesses and persists JPA entities to the database and can be injected and consumed by other components in the microservice. An Embedded Derby database is used as a data store for all the events.

You will use JPA annotations to define an entity class whose fields are persisted to the database. The interaction between your service and the database is mediated by the persistence context that is managed by an entity manager. In a Jakarta EE environment, you can use an application-managed entity manager or a container-managed entity manager. In this guide, you will use a container-managed entity manager that is injected into the DAO so Liberty manages the opening and closing of the entity manager for you. 


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-jpa-intro.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-jpa-intro.git
cd guide-jpa-intro
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, run the following commands to navigate to the ***finish/frontendUI*** directory and deploy the ***frontendUI*** service to Open Liberty:

```bash
cd finish/frontendUI
mvn liberty:run
```

Open another command-line session and run the following commands to navigate to the ***finish/backendServices*** directory and deploy the service to Open Liberty:
```bash
cd /home/project/guide-jpa-intro/finish/backendServices
mvn liberty:run
```


After you see the following message in both command-line sessions, both your services are ready.

```
The defaultServer server is ready to run a smarter planet.
```

Click the following button to view the Event Manager application:
::startApplication{port="9090" display="external" name="Visit Event Manager application" route="/"}
The event application does not display any events because no events are stored in the database. Go ahead and click ***Create Event***, located in the left navigation bar. After entering an event name, location and time, click ***Submit*** to persist your event entity to the database. The event is now stored in the database and is visible in the list of current events.

Notice that if you stop the Open Liberty instance and then restart it, the events created are still displayed in the list of current events. Ensure you are in the ***finish/backendServices*** directory and run the following Maven goals to stop and then restart the instance:
```bash
cd /home/project/guide-jpa-intro/finish/backendServices
mvn liberty:stop
mvn liberty:run
```


The events created are still displayed in the list of current events. The ***Update*** action link located beside each event allows you to make modifications to the persisted entity and the ***Delete*** action link allows you to remove entities from the database.

After you are finished checking out the application, stop the Open Liberty instances by pressing `Ctrl+C` in the command-line sessions where you ran the ***backendServices*** and ***frontendUI*** services. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another command-line session for the ***frontendUI*** and ***backendServices*** services:
```bash
cd /home/project/guide-jpa-intro/finish
mvn -pl frontendUI liberty:stop
mvn -pl backendServices liberty:stop
```



::page{title="Defining a JPA entity class"}

Navigate to the ***start*** directory to begin.

When you run Open Liberty in [dev mode](https://openliberty.io/docs/latest/development-mode.html), dev mode listens for file changes and automatically recompiles and deploys your updates whenever you save a new change.

Run the following commands to navigate to the ***frontendUI*** directory and start the ***frontendUI*** service in dev mode:
```bash
cd /home/project/guide-jpa-intro/start/frontendUI
mvn liberty:dev
```

Open another command-line session and run the following commands to navigate to the ***backendServices*** directory and start the service in dev mode:
```bash
cd /home/project/guide-jpa-intro/start/backendServices
mvn liberty:dev
```

After you see the following message, your Liberty instance is ready in dev mode:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command line to listen for file changes. Open another command-line session to continue, or open the project in your editor.

To store Java objects in a database, you must define a JPA entity class. A JPA entity is a Java object whose non-transient and non-static fields will be persisted to the database. Any Plain Old Java Object (POJO) class can be designated as a JPA entity. However, the class must be annotated with the ***@Entity*** annotation, must not be declared final and must have a public or protected non-argument constructor. JPA maps an entity type to a database table and persisted instances will be represented as rows in the table.

The ***Event*** class is a data model that represents events in the event microservice and is annotated with JPA annotations.

Create the ***Event*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jpa-intro/start/backendServices/src/main/java/io/openliberty/guides/event/models/Event.java
```


> Then, to open the Event.java file in your IDE, select
> **File** > **Open** > guide-jpa-intro/start/backendServices/src/main/java/io/openliberty/guides/event/models/Event.java, or click the following button

::openFile{path="/home/project/guide-jpa-intro/start/backendServices/src/main/java/io/openliberty/guides/event/models/Event.java"}



```java
package io.openliberty.guides.event.models;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "Event")
@NamedQuery(name = "Event.findAll", query = "SELECT e FROM Event e")
@NamedQuery(name = "Event.findEvent", query = "SELECT e FROM Event e WHERE "
    + "e.name = :name AND e.location = :location AND e.time = :time")
public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    @Column(name = "eventId")
    private int id;

    @Column(name = "eventLocation")
    private String location;
    @Column(name = "eventTime")
    private String time;
    @Column(name = "eventName")
    private String name;

    public Event() {
    }

    public Event(String name, String location, String time) {
        this.name = name;
        this.location = location;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                 + (int) (serialVersionUID ^ (serialVersionUID >>> 32));
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Event other = (Event) obj;
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        if (time == null) {
            if (other.time != null) {
                return false;
            }
        } else if (!time.equals(other.time)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Event [name=" + name + ", location=" + location + ", time=" + time
                + "]";
    }
}

```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The following table breaks down the new annotations:

| *Annotation*    | *Description*
| ---| ---
| ***@Entity*** | Declares the class as an entity
| ***@Table***  | Specifies details of the table such as name 
| ***@NamedQuery*** | Specifies a predefined database query that is run by an ***EntityManager*** instance.
| ***@Id***       |  Declares the primary key of the entity
| ***@GeneratedValue***    | Specifies the strategy used for generating the value of the primary key. The ***strategy = GenerationType.AUTO*** code indicates that the generation strategy is automatically selected
| ***@Column***    | Specifies that the field is mapped to a column in the database table. The ***name*** attribute is optional and indicates the name of the column in the table


::page{title="Configuring JPA"}

The ***persistence.xml*** file is a configuration file that defines a persistence unit. The persistence unit specifies configuration information for the entity manager.

Create the configuration file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jpa-intro/start/backendServices/src/main/resources/META-INF/persistence.xml
```


> Then, to open the persistence.xml file in your IDE, select
> **File** > **Open** > guide-jpa-intro/start/backendServices/src/main/resources/META-INF/persistence.xml, or click the following button

::openFile{path="/home/project/guide-jpa-intro/start/backendServices/src/main/resources/META-INF/persistence.xml"}



```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.2"
    xmlns="http://xmlns.jcp.org/xml/ns/persistence" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence 
                        http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
    <persistence-unit name="jpa-unit" transaction-type="JTA">
        <jta-data-source>jdbc/eventjpadatasource</jta-data-source>
        <properties>
            <property name="jakarta.persistence.schema-generation.database.action"
                      value="create"/>
            <property name="jakarta.persistence.schema-generation.scripts.action"
                      value="create"/>
            <property name="jakarta.persistence.schema-generation.scripts.create-target"
                      value="createDDL.ddl"/>
        </properties>
    </persistence-unit>
</persistence>
```



The persistence unit is defined by the ***persistence-unit*** XML element. The ***name*** attribute is required and is used to identify the persistent unit when using the ***@PersistenceContext*** annotation to inject the entity manager later in this guide. The ***transaction-type="JTA"*** attribute specifies to use Java Transaction API (JTA) transaction management. Because of using a container-managed entity manager, JTA transactions must be used. 

A JTA transaction type requires a JTA data source to be provided. The ***jta-data-source*** element specifies the Java Naming and Directory Interface (JNDI) name of the data source that is used. The ***data source*** has already been configured for you in the ***backendServices/src/main/liberty/config/server.xml*** file. This data source configuration is where the Java Database Connectivity (JDBC) connection is defined along with some database vendor-specific properties.


The ***jakarta.persistence.schema-generation*** properties are used here so that you aren't required to manually create a database table to run this sample application. To learn more about the JPA schema generation and available properties, see https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a12917[Schema Generation, Section 9.4 of the JPA Specification]


::page{title="Performing CRUD operations using JPA"}

The CRUD operations are defined in the DAO. To perform these operations by using JPA, you need an ***EventDao*** class. 

Create the ***EventDao*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jpa-intro/start/backendServices/src/main/java/io/openliberty/guides/event/dao/EventDao.java
```


> Then, to open the EventDao.java file in your IDE, select
> **File** > **Open** > guide-jpa-intro/start/backendServices/src/main/java/io/openliberty/guides/event/dao/EventDao.java, or click the following button

::openFile{path="/home/project/guide-jpa-intro/start/backendServices/src/main/java/io/openliberty/guides/event/dao/EventDao.java"}



```java
package io.openliberty.guides.event.dao;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import io.openliberty.guides.event.models.Event;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class EventDao {

    @PersistenceContext(name = "jpa-unit")
    private EntityManager em;

    public void createEvent(Event event) {
        em.persist(event);
    }

    public Event readEvent(int eventId) {
        return em.find(Event.class, eventId);
    }

    public void updateEvent(Event event) {
        em.merge(event);
    }

    public void deleteEvent(Event event) {
        em.remove(event);
    }

    public List<Event> readAllEvents() {
        return em.createNamedQuery("Event.findAll", Event.class).getResultList();
    }

    public List<Event> findEvent(String name, String location, String time) {
        return em.createNamedQuery("Event.findEvent", Event.class)
            .setParameter("name", name)
            .setParameter("location", location)
            .setParameter("time", time).getResultList();
    }
}
```



To use the entity manager at runtime, inject it into the CDI bean through the ***@PersistenceContext*** annotation. The entity manager interacts with the persistence context. Every ***EntityManager*** instance is associated with a persistence context. The persistence context manages a set of entities and is aware of the different states that an entity can have. The persistence context synchronizes with the database when a transaction commits.

The ***EventDao*** class has a method for each CRUD operation, so let's break them down:

* The ***createEvent()*** method persists an instance of the ***Event*** entity class to the data store by calling the ***persist()*** method on an ***EntityManager*** instance. The entity instance becomes managed and changes to it will be tracked by the entity manager.

* The ***readEvent()*** method returns an instance of the ***Event*** entity class with the specified primary key by calling the ***find()*** method on an ***EntityManager*** instance. If the event instance is found, it is returned in a managed state, but, if the event instance is not found, ***null*** is returned.

* The ***readAllEvents()*** method demonstrates an alternative way to retrieve event objects from the database. This method returns a list of instances of the ***Event*** entity class by using the ***Event.findAll*** query specified in the ***@NamedQuery*** annotation on the ***Event*** class. Similarly, the ***findEvent()*** method uses the ***Event.findEvent*** named query to find an event with the given name, location and time. 


* The ***updateEvent()*** method creates a managed instance of a detached entity instance. The entity manager automatically tracks all managed entity objects in its persistence context for changes and synchronizes them with the database. However, if an entity becomes detached, you must merge that entity into the persistence context by calling the ***merge()*** method so that changes to loaded fields of the detached entity are tracked.

* The ***deleteEvent()*** method removes an instance of the ***Event*** entity class from the database by calling the ***remove()*** method on an ***EntityManager*** instance. The state of the entity is changed to removed and is removed from the database upon transaction commit. 

The DAO is injected into the ***backendServices/src/main/java/io/openliberty/guides/event/resources/EventResource.java*** class and used to access and persist data. The ***@Transactional*** annotation is used in the ***EventResource*** class to declaratively control the transaction boundaries on the ***@RequestScoped*** CDI bean. This ensures that the methods run within the boundaries of an active global transaction, which is why it is not necessary to explicitly begin, commit or rollback transactions. At the end of the transactional method invocation, the transaction commits and the persistence context flushes any changes to Event entity instances it is managing to the database.



::page{title="Running the application"}

You started the Open Liberty in dev mode at the beginning of the guide, so all the changes were automatically picked up.


When Liberty is running, click the following button to view the Event Manager application:
::startApplication{port="9090" display="external" name="Visit Event Manager application" route="/"}

Click ***Create Event*** in the left navigation bar to create events that are persisted to the database. After you create an event, it is available to view, update, and delete in the ***Current Events*** section.


::page{title="Testing the application"}

Create the ***EventEntityIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-jpa-intro/start/backendServices/src/test/java/it/io/openliberty/guides/event/EventEntityIT.java 
```


> Then, to open the EventEntityIT.java file in your IDE, select
> **File** > **Open** > guide-jpa-intro/start/backendServices/src/test/java/it/io/openliberty/guides/event/EventEntityIT.java, or click the following button

::openFile{path="/home/project/guide-jpa-intro/start/backendServices/src/test/java/it/io/openliberty/guides/event/EventEntityIT.java"}



```java
package it.io.openliberty.guides.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.openliberty.guides.event.models.Event;

public class EventEntityIT extends EventIT {

    private static final String JSONFIELD_LOCATION = "location";
    private static final String JSONFIELD_NAME = "name";
    private static final String JSONFIELD_TIME = "time";
    private static final String EVENT_TIME = "12:00 PM, January 1 2018";
    private static final String EVENT_LOCATION = "IBM";
    private static final String EVENT_NAME = "JPA Guide";
    private static final String UPDATE_EVENT_TIME = "12:00 PM, February 1 2018";
    private static final String UPDATE_EVENT_LOCATION = "IBM Updated";
    private static final String UPDATE_EVENT_NAME = "JPA Guide Updated";

    private static final int NO_CONTENT_CODE = Status.NO_CONTENT.getStatusCode();
    private static final int NOT_FOUND_CODE = Status.NOT_FOUND.getStatusCode();

    @BeforeAll
    public static void oneTimeSetup() {
        port = System.getProperty("backend.http.port");
        baseUrl = "http://localhost:" + port + "/";
    }

    @BeforeEach
    public void setup() {
        form = new Form();
        client = ClientBuilder.newClient();

        eventForm = new HashMap<String, String>();

        eventForm.put(JSONFIELD_NAME, EVENT_NAME);
        eventForm.put(JSONFIELD_LOCATION, EVENT_LOCATION);
        eventForm.put(JSONFIELD_TIME, EVENT_TIME);
    }

    @Test
    public void testInvalidRead() {
        assertEquals(true, getIndividualEvent(-1).isEmpty(),
          "Reading an event that does not exist should return an empty list");
    }

    @Test
    public void testInvalidDelete() {
        int deleteResponse = deleteRequest(-1);
        assertEquals(NOT_FOUND_CODE, deleteResponse,
          "Trying to delete an event that does not exist should return the "
          + "HTTP response code " + NOT_FOUND_CODE);
    }

    @Test
    public void testInvalidUpdate() {
        int updateResponse = updateRequest(eventForm, -1);
        assertEquals(NOT_FOUND_CODE, updateResponse,
          "Trying to update an event that does not exist should return the "
          + "HTTP response code " + NOT_FOUND_CODE);
    }

    @Test
    public void testReadIndividualEvent() {
        int postResponse = postRequest(eventForm);
        assertEquals(NO_CONTENT_CODE, postResponse,
          "Creating an event should return the HTTP reponse code " + NO_CONTENT_CODE);

        Event e = new Event(EVENT_NAME, EVENT_LOCATION, EVENT_TIME);
        JsonObject event = findEvent(e);
        event = getIndividualEvent(event.getInt("id"));
        assertData(event, EVENT_NAME, EVENT_LOCATION, EVENT_TIME);

        int deleteResponse = deleteRequest(event.getInt("id"));
        assertEquals(NO_CONTENT_CODE, deleteResponse,
          "Deleting an event should return the HTTP response code " + NO_CONTENT_CODE);
    }

    @Test
    public void testCRUD() {
        int eventCount = getRequest().size();
        int postResponse = postRequest(eventForm);
        assertEquals(NO_CONTENT_CODE, postResponse,
          "Creating an event should return the HTTP reponse code " + NO_CONTENT_CODE);

        Event e = new Event(EVENT_NAME, EVENT_LOCATION, EVENT_TIME);
        JsonObject event = findEvent(e);
        assertData(event, EVENT_NAME, EVENT_LOCATION, EVENT_TIME);

        eventForm.put(JSONFIELD_NAME, UPDATE_EVENT_NAME);
        eventForm.put(JSONFIELD_LOCATION, UPDATE_EVENT_LOCATION);
        eventForm.put(JSONFIELD_TIME, UPDATE_EVENT_TIME);
        int updateResponse = updateRequest(eventForm, event.getInt("id"));
        assertEquals(NO_CONTENT_CODE, updateResponse,
          "Updating an event should return the HTTP response code " + NO_CONTENT_CODE);

        e = new Event(UPDATE_EVENT_NAME, UPDATE_EVENT_LOCATION, UPDATE_EVENT_TIME);
        event = findEvent(e);
        assertData(event, UPDATE_EVENT_NAME, UPDATE_EVENT_LOCATION, UPDATE_EVENT_TIME);

        int deleteResponse = deleteRequest(event.getInt("id"));
        assertEquals(NO_CONTENT_CODE, deleteResponse,
          "Deleting an event should return the HTTP response code " + NO_CONTENT_CODE);
        assertEquals(eventCount, getRequest().size(),
          "Total number of events stored should be the same after testing "
          + "CRUD operations.");
    }

    @AfterEach
    public void teardown() {
        response.close();
        client.close();
    }

}
```



The ***testInvalidRead()***, ***testInvalidDelete()*** and ***testInvalidUpdate()*** methods use a primary key that is not in the database to test reading, updating and deleting an event that does not exist, respectively.

The ***testReadIndividualEvent()*** method persists a test event to the database and retrieves the event object from the database using the primary key of the entity.

The ***testCRUD()*** method creates a test event and persists it to the database. The event object is then retrieved from the database to verify that the test event was actually persisted. Next, the name, location, and time of the test event are updated. The event object is retrieved from the database to verify that the updated event is stored. Finally, the updated test event is deleted and one final check is done to ensure that the updated test event is no longer stored in the database.

### Running the tests

Since you started Open Liberty in dev mode, press the ***enter/return*** key in the command-line session where you started the ***backendServices*** service to run the tests for the ***backendServices***.

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.event.EventEntityIT
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.703 sec - in it.io.openliberty.guides.event.EventEntityIT

Results :

Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 
```

When you are done checking out the services, exit dev mode by pressing `Ctrl+C` in the command-line sessions where you ran the ***frontendUI*** and ***backendServices*** services.


::page{title="Summary"}

### Nice Work!

You learned how to map Java objects to database tables by defining a JPA entity class whose instances are represented as rows in the table. You have injected a container-managed entity manager into a DAO and learned how to perform CRUD operations in your microservice in Open Liberty.




### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-jpa-intro*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-jpa-intro
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Accessing%20and%20persisting%20data%20in%20microservices%20using%20Java%20Persistence%20API%20(JPA)&guide-id=cloud-hosted-guide-jpa-intro)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-jpa-intro/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-jpa-intro/pulls)



### Where to next?

* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
