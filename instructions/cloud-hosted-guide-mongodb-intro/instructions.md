---
markdown-version: v1
tool-type: theiadocker
---
::page{title="Welcome to the Persisting data with MongoDB guide!"}

Learn how to persist data in your microservices to MongoDB, a document-oriented NoSQL database.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You will learn how to use MongoDB to build and test a simple microservice that manages the members of a crew. The microservice will respond to ***POST***, ***GET***, ***PUT***, and ***DELETE*** requests that manipulate the database.

The crew members will be stored in MongoDB as documents in the following JSON format:

```
{
  "_id": {
    "$oid": "5dee6b079503234323db2ebc"
  },
  "Name": "Member1",
  "Rank": "Captain",
  "CrewID": "000001"
}
```

This microservice connects to MongoDB by using Transport Layer Security (TLS) and injects a ***MongoDatabase*** instance into the service with a Contexts and Dependency Injection (CDI) producer. Additionally, MicroProfile Config is used to easily configure the MongoDB driver.

For more information about CDI and MicroProfile Config, see the guides on [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html) and [Separating configuration from code in microservices](https://openliberty.io/guides/microprofile-config-intro.html).


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-mongodb-intro.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-mongodb-intro.git
cd guide-mongodb-intro
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.


### Setting up MongoDB

This guide uses Docker to run an instance of MongoDB. A multi-stage Dockerfile is provided for you. This Dockerfile uses the ***mongo*** image as the base image of the final stage and gathers the required configuration files. The resulting ***mongo*** image runs in a Docker container, and you must set up a new database for the microservice. Lastly, the truststore that's generated in the Docker image is copied from the container and placed into the Open Liberty configuration.

You can find more details and configuration options on the [MongoDB website](https://docs.mongodb.com/manual/reference/configuration-options/). For more information about the ***mongo*** image, see [mongo](https://hub.docker.com/_/mongo) in Docker Hub.

**Running MongoDB in a Docker container**

Run the following commands to use the Dockerfile to build the image, run the image in a Docker container, and map port ***27017*** from the container to your host machine:


```bash
sed -i 's=latest=7.0.15-rc1=g' assets/Dockerfile
docker build -t mongo-sample -f assets/Dockerfile .
docker run --name mongo-guide -p 27017:27017 -d mongo-sample
```

**Adding the truststore to the Open Liberty configuration**

The truststore that's created in the container needs to be added to the Open Liberty configuration so that the Liberty can trust the certificate that MongoDB presents when they connect. Run the following command to copy the ***truststore.p12*** file from the container to the ***start*** and ***finish*** directories:


```bash
docker cp \
  mongo-guide:/home/mongodb/certs/truststore.p12 \
  start/src/main/liberty/config/resources/security
docker cp \
  mongo-guide:/home/mongodb/certs/truststore.p12 \
  finish/src/main/liberty/config/resources/security
```


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


You can now check out the service by clicking the following button:

::startApplication{port="9080" display="external" name="Launch application" route="/mongo"}

After you are finished checking out the application, stop the Liberty instance by pressing `Ctrl+C` in the command-line session where you ran Liberty. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```


::page{title="Providing a MongoDatabase"}

Navigate to the ***start*** directory to begin.

```bash
cd /home/project/guide-mongodb-intro/start
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

With a CDI producer, you can easily provide a ***MongoDatabase*** to your microservice.

Create the ***MongoProducer*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-mongodb-intro/start/src/main/java/io/openliberty/guides/mongo/MongoProducer.java
```


> Then, to open the MongoProducer.java file in your IDE, select
> **File** > **Open** > guide-mongodb-intro/start/src/main/java/io/openliberty/guides/mongo/MongoProducer.java, or click the following button

::openFile{path="/home/project/guide-mongodb-intro/start/src/main/java/io/openliberty/guides/mongo/MongoProducer.java"}



```java
package io.openliberty.guides.mongo;

import java.util.Collections;

import javax.net.ssl.SSLContext;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.ibm.websphere.crypto.PasswordUtil;
import com.ibm.websphere.ssl.JSSEHelper;
import com.ibm.websphere.ssl.SSLException;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class MongoProducer {

    @Inject
    @ConfigProperty(name = "mongo.hostname", defaultValue = "localhost")
    String hostname;

    @Inject
    @ConfigProperty(name = "mongo.port", defaultValue = "27017")
    int port;

    @Inject
    @ConfigProperty(name = "mongo.dbname", defaultValue = "testdb")
    String dbName;

    @Inject
    @ConfigProperty(name = "mongo.user")
    String user;

    @Inject
    @ConfigProperty(name = "mongo.pass.encoded")
    String encodedPass;

    @Produces
    public MongoClient createMongo() throws SSLException {
        String password = PasswordUtil.passwordDecode(encodedPass);
        MongoCredential creds = MongoCredential.createCredential(
                user,
                dbName,
                password.toCharArray()
        );

        SSLContext sslContext = JSSEHelper.getInstance().getSSLContext(
                "outboundSSLContext",
                Collections.emptyMap(),
                null
        );

        return MongoClients.create(MongoClientSettings.builder()
                   .applyConnectionString(
                       new ConnectionString("mongodb://" + hostname + ":" + port))
                   .credential(creds)
                   .applyToSslSettings(builder -> {
                       builder.enabled(true);
                       builder.context(sslContext); })
                   .build());
    }

    @Produces
    public MongoDatabase createDB(
            MongoClient client) {
        return client.getDatabase(dbName);
    }

    public void close(
            @Disposes MongoClient toClose) {
        toClose.close();
    }
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.





The values from the ***microprofile-config.properties*** file are injected into the ***MongoProducer*** class. The ***MongoProducer*** class requires the following methods for the ***MongoClient***:

* The ***createMongo()*** producer method returns an instance of ***MongoClient***. In this method, the username, database name, and decoded password are passed into the ***MongoCredential.createCredential()*** method to get an instance of ***MongoCredential***. The ***JSSEHelper*** gets the ***SSLContext*** from the ***outboundSSLContext*** in the ***server.xml*** configuration file. Then, a ***MongoClient*** instance is created.

* The ***createDB()*** producer method returns an instance of ***MongoDatabase*** that depends on the ***MongoClient***. This method injects the ***MongoClient*** in its parameters and passes the database name into the ***MongoClient.getDatabase()*** method to get a ***MongoDatabase*** instance.

* The ***close()*** method is a clean-up function for the ***MongoClient*** that closes the connection to the ***MongoDatabase*** instance.



::page{title="Implementing the Create, Retrieve, Update, and Delete operations"}

You are going to implement the basic create, retrieve, update, and delete (CRUD) operations in the ***CrewService*** class. The ***com.mongodb.client*** and ***com.mongodb.client.result*** packages are used to help implement these operations for the microservice. For more information about these packages, see the [com.mongodb.client](https://mongodb.github.io/mongo-java-driver/3.12/javadoc/com/mongodb/client/package-summary.html) and [com.mongodb.client.result](https://mongodb.github.io/mongo-java-driver/3.12/javadoc/com/mongodb/client/result/package-summary.html) Javadoc. For more information about creating a RESTful service with JAX-RS, JSON-B, and Open Liberty, see the guide on [Creating a RESTful web serivce](https://openliberty.io/guides/rest-intro.html).

Create the ***CrewService*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-mongodb-intro/start/src/main/java/io/openliberty/guides/application/CrewService.java
```


> Then, to open the CrewService.java file in your IDE, select
> **File** > **Open** > guide-mongodb-intro/start/src/main/java/io/openliberty/guides/application/CrewService.java, or click the following button

::openFile{path="/home/project/guide-mongodb-intro/start/src/main/java/io/openliberty/guides/application/CrewService.java"}



```java
package io.openliberty.guides.application;

import java.util.Set;

import java.io.StringWriter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.Json;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;

import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/crew")
@ApplicationScoped
public class CrewService {

    @Inject
    MongoDatabase db;

    @Inject
    Validator validator;

    private JsonArray getViolations(CrewMember crewMember) {
        Set<ConstraintViolation<CrewMember>> violations = validator.validate(
                crewMember);

        JsonArrayBuilder messages = Json.createArrayBuilder();

        for (ConstraintViolation<CrewMember> v : violations) {
            messages.add(v.getMessage());
        }

        return messages.build();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Successfully added crew member."),
        @APIResponse(
            responseCode = "400",
            description = "Invalid crew member configuration.") })
    @Operation(summary = "Add a new crew member to the database.")
    public Response add(CrewMember crewMember) {
        JsonArray violations = getViolations(crewMember);

        if (!violations.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(violations.toString())
                    .build();
        }

        MongoCollection<Document> crew = db.getCollection("Crew");

        Document newCrewMember = new Document();
        newCrewMember.put("Name", crewMember.getName());
        newCrewMember.put("Rank", crewMember.getRank());
        newCrewMember.put("CrewID", crewMember.getCrewID());

        crew.insertOne(newCrewMember);

        return Response
            .status(Response.Status.OK)
            .entity(newCrewMember.toJson())
            .build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Successfully listed the crew members."),
        @APIResponse(
            responseCode = "500",
            description = "Failed to list the crew members.") })
    @Operation(summary = "List the crew members from the database.")
    public Response retrieve() {
        StringWriter sb = new StringWriter();

        try {
            MongoCollection<Document> crew = db.getCollection("Crew");
            sb.append("[");
            boolean first = true;
            FindIterable<Document> docs = crew.find();
            for (Document d : docs) {
                if (!first) {
                    sb.append(",");
                } else {
                    first = false;
                }
                sb.append(d.toJson());
            }
            sb.append("]");
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("[\"Unable to list crew members!\"]")
                .build();
        }

        return Response
            .status(Response.Status.OK)
            .entity(sb.toString())
            .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Successfully updated crew member."),
        @APIResponse(
            responseCode = "400",
            description = "Invalid object id or crew member configuration."),
        @APIResponse(
            responseCode = "404",
            description = "Crew member object id was not found.") })
    @Operation(summary = "Update a crew member in the database.")
    public Response update(CrewMember crewMember,
        @Parameter(
            description = "Object id of the crew member to update.",
            required = true
        )
        @PathParam("id") String id) {

        JsonArray violations = getViolations(crewMember);

        if (!violations.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(violations.toString())
                    .build();
        }

        ObjectId oid;

        try {
            oid = new ObjectId(id);
        } catch (Exception e) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("[\"Invalid object id!\"]")
                .build();
        }

        MongoCollection<Document> crew = db.getCollection("Crew");

        Document query = new Document("_id", oid);

        Document newCrewMember = new Document();
        newCrewMember.put("Name", crewMember.getName());
        newCrewMember.put("Rank", crewMember.getRank());
        newCrewMember.put("CrewID", crewMember.getCrewID());

        UpdateResult updateResult = crew.replaceOne(query, newCrewMember);

        if (updateResult.getMatchedCount() == 0) {
            return Response
                .status(Response.Status.NOT_FOUND)
                .entity("[\"_id was not found!\"]")
                .build();
        }

        newCrewMember.put("_id", oid);

        return Response
            .status(Response.Status.OK)
            .entity(newCrewMember.toJson())
            .build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Successfully deleted crew member."),
        @APIResponse(
            responseCode = "400",
            description = "Invalid object id."),
        @APIResponse(
            responseCode = "404",
            description = "Crew member object id was not found.") })
    @Operation(summary = "Delete a crew member from the database.")
    public Response remove(
        @Parameter(
            description = "Object id of the crew member to delete.",
            required = true
        )
        @PathParam("id") String id) {

        ObjectId oid;

        try {
            oid = new ObjectId(id);
        } catch (Exception e) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("[\"Invalid object id!\"]")
                .build();
        }

        MongoCollection<Document> crew = db.getCollection("Crew");

        Document query = new Document("_id", oid);

        DeleteResult deleteResult = crew.deleteOne(query);

        if (deleteResult.getDeletedCount() == 0) {
            return Response
                .status(Response.Status.NOT_FOUND)
                .entity("[\"_id was not found!\"]")
                .build();
        }

        return Response
            .status(Response.Status.OK)
            .entity(query.toJson())
            .build();
    }
}
```




In this class, a ***Validator*** is used to validate a ***CrewMember*** before the database is updated. The CDI producer is used to inject a ***MongoDatabase*** into the CrewService class.


**Implementing the Create operation**

The ***add()*** method handles the implementation of the create operation. An instance of ***MongoCollection*** is retrieved with the ***MongoDatabase.getCollection()*** method. The ***Document*** type parameter specifies that the ***Document*** type is used to store data in the ***MongoCollection***. Each crew member is converted into a ***Document***, and the ***MongoCollection.insertOne()*** method inserts a new crew member document.


**Implementing the Retrieve operation**

The ***retrieve()*** method handles the implementation of the retrieve operation. The ***Crew*** collection is retrieved with the ***MongoDatabase.getCollection()*** method. Then, the ***MongoCollection.find()*** method retrieves a ***FindIterable*** object. This object is iterable for all the crew members documents in the collection, so each crew member document is concatenated into a String array and returned.


**Implementing the Update operation**

The ***update()*** method handles the implementation of the update operation. After the ***Crew*** collection is retrieved, a document is created with the specified object ***id*** and is used to query the collection. Next, a new crew member ***Document*** is created with the updated configuration. The ***MongoCollection.replaceOne()*** method is called with the query and new crew member document. This method updates all of the matching queries with the new document. Because the object ***id*** is unique in the ***Crew*** collection, only one document is updated. The ***MongoCollection.replaceOne()*** method also returns an ***UpdateResult*** instance, which determines how many documents matched the query. If there are zero matches, then the object ***id*** doesn't exist.


**Implementing the Delete operation**

The ***remove()*** method handles the implementation of the delete operation. After the ***Crew*** collection is retrieved, a ***Document*** is created with the specified object ***id*** and is used to query the collection. Because the object ***id*** is unique in the ***Crew*** collection, only one document is deleted. After the document is deleted, the ***MongoCollection.deleteOne()*** method returns a ***DeleteResult*** instance, which determines how many documents were deleted. If zero documents were deleted, then the object ***id*** doesn't exist.



::page{title="Configuring the MongoDB driver and the Liberty"}

MicroProfile Config makes configuring the MongoDB driver simple because all of the configuration can be set in one place and injected into the CDI producer.

Create the configuration file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-mongodb-intro/start/src/main/resources/META-INF/microprofile-config.properties
```


> Then, to open the microprofile-config.properties file in your IDE, select
> **File** > **Open** > guide-mongodb-intro/start/src/main/resources/META-INF/microprofile-config.properties, or click the following button

::openFile{path="/home/project/guide-mongodb-intro/start/src/main/resources/META-INF/microprofile-config.properties"}



```
mongo.hostname=localhost
mongo.port=27017
mongo.dbname=testdb
mongo.user=sampleUser
mongo.pass.encoded={aes}APtt+/vYxxPa0jE1rhmZue9wBm3JGqFK3JR4oJdSDGWM1wLr1ckvqkqKjSB2Voty8g==
```



Values such as the hostname, port, and database name for the running MongoDB instance are set in this file. The user’s username and password are also set here. For added security, the password was encoded by using the [securityUtility encode command](https://openliberty.io/docs/latest/reference/command/securityUtility-encode.html).

To create a CDI producer for MongoDB and connect over TLS, the Open Liberty needs to be correctly configured.

Replace the Liberty ***server.xml*** configuration file.

> To open the server.xml file in your IDE, select
> **File** > **Open** > guide-mongodb-intro/start/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-mongodb-intro/start/src/main/liberty/config/server.xml"}



```xml
<server description="Sample Liberty server">
    <featureManager>
        <feature>cdi-4.0</feature>
        <feature>ssl-1.0</feature>
        <feature>mpConfig-3.1</feature>
        <feature>passwordUtilities-1.1</feature>
        <feature>beanValidation-3.0</feature>	   
        <feature>restfulWS-3.1</feature>
        <feature>jsonb-3.0</feature>
        <feature>mpOpenAPI-3.1</feature>
    </featureManager>

    <variable name="http.port" defaultValue="9080"/>
    <variable name="https.port" defaultValue="9443"/>
    <variable name="app.context.root" defaultValue="/mongo"/>

    <httpEndpoint
        host="*" 
        httpPort="${http.port}" 
        httpsPort="${https.port}" 
        id="defaultHttpEndpoint"
    />

    <webApplication 
        location="guide-mongodb-intro.war" 
        contextRoot="${app.context.root}"
    />
    <keyStore
        id="outboundTrustStore" 
        location="${server.output.dir}/resources/security/truststore.p12"
        password="mongodb"
        type="PKCS12" 
    />
    <ssl 
        id="outboundSSLContext" 
        keyStoreRef="defaultKeyStore" 
        trustStoreRef="outboundTrustStore" 
        sslProtocol="TLS" 
    />
</server>
```



The features that are required to create the CDI producer for MongoDB are [Contexts and Dependency Injection](https://openliberty.io/docs/latest/reference/feature/cdi-4.0.html) (***cdi-4.0***), [Secure Socket Layer](https://openliberty.io/docs/latest/reference/feature/ssl-1.0.html) (***ssl-1.0***), [MicroProfile Config](https://openliberty.io/docs/latest/reference/feature/mpConfig-3.1.html) (***mpConfig-3.1***), and [Password Utilities](https://openliberty.io/docs/latest/reference/feature/passwordUtilities-1.1.html) (***passwordUtilities-1.1***). These features are specified in the ***featureManager*** element. The Secure Socket Layer (SSL) context is configured in the ***server.xml*** configuration file so that the application can connect to MongoDB with TLS. The ***keyStore*** element points to the ***truststore.p12*** keystore file that was created in one of the previous sections. The ***ssl*** element specifies the ***defaultKeyStore*** as the keystore and ***outboundTrustStore*** as the truststore.

After you replace the ***server.xml*** file, the Open Liberty configuration is automatically reloaded.


::page{title="Running the application"}

You started the Open Liberty in dev mode at the beginning of the guide, so all the changes were automatically picked up.


Wait until you see a message similar to the following example:

```
CWWKZ0001I: Application guide-mongodb-intro started in 5.715 seconds.
```

Click the following button to see the OpenAPI user interface (UI) that provides API documentation and a client to test the API endpoints that you create:

::startApplication{port="9080" display="external" name="Visit OpenAPI UI" route="/openapi/ui"}

**Try the Create operation**

From the OpenAPI UI, test the create operation at the ***POST /api/crew*** endpoint by using the following code as the request body:

```bash
{
  "name": "Member1",
  "rank": "Officer",
  "crewID": "000001"
}
```

This request creates a new document in the ***Crew*** collection with a name of ***Member1***, rank of ***Officer***, and crew ID of ***000001***.

You'll receive a response that contains the JSON object of the new crew member, as shown in the following example:
```
{
  "Name": "Member1",
  "Rank": "Officer",
  "CrewID": "000001",
  "_id": {
    "$oid": "<<ID>>"
  }
}
```



The ***\<\<ID\>\>*** that you receive is a unique identifier in the collection. Save this value for future commands.

**Try the Retrieve operation**

From the OpenAPI UI, test the read operation at the ***GET /api/crew*** endpoint. This request gets all crew member documents from the collection.

You'll receive a response that contains an array of all the members in your crew. The response might include crew members that were created in the **Try what you’ll build** section of this guide:
```
[
  {
    "_id": {
      "$oid": "<<ID>>"
    },
    "Name": "Member1",
    "Rank": "Officer",
    "CrewID": "000001"
  }
]
```


**Try the Update operation**


From the OpenAPI UI, test the update operation at the ***PUT /api/crew/{id}*** endpoint, where the ***{id}*** parameter is the ***\<\<ID\>\>*** that you saved from the create operation. Use the following code as the request body:

```bash
{
  "name": "Member1",
  "rank": "Captain",
  "crewID": "000001"
}
```

This request updates the rank of the crew member that you created from ***Officer*** to ***Captain***.

You'll receive a response that contains the JSON object of the updated crew member, as shown in the following example:

```
{
  "Name": "Member1",
  "Rank": "Captain",
  "CrewID": "000001",
  "_id": {
    "$oid": "<<ID>>"
  }
}
```


**Try the Delete operation**


From the OpenAPI UI, test the delete operation at the ***DELETE/api/crew/{id}*** endpoint, where the ***{id}*** parameter is the ***\<\<ID\>\>*** that you saved from the create operation. This request removes the document that contains the specified crew member object ***id*** from the collection.

You'll receive a response that contains the object ***id*** of the deleted crew member, as shown in the following example:

```
{
  "_id": {
    "$oid": "<<ID>>"
  }
}
```


Now, you can check out the microservice that you created by clicking the following button:

::startApplication{port="9080" display="external" name="Launch application" route="/mongo"}



::page{title="Testing the application"}

Next, you'll create integration tests to ensure that the basic operations you implemented function correctly.

Create the ***CrewServiceIT*** class.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-mongodb-intro/start/src/test/java/it/io/openliberty/guides/application/CrewServiceIT.java
```


> Then, to open the CrewServiceIT.java file in your IDE, select
> **File** > **Open** > guide-mongodb-intro/start/src/test/java/it/io/openliberty/guides/application/CrewServiceIT.java, or click the following button

::openFile{path="/home/project/guide-mongodb-intro/start/src/test/java/it/io/openliberty/guides/application/CrewServiceIT.java"}



```java
package it.io.openliberty.guides.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.Entity;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CrewServiceIT {

    private static Client client;
    private static JsonArray testData;
    private static String rootURL;
    private static ArrayList<String> testIDs = new ArrayList<>(2);

    @BeforeAll
    public static void setup() {
        client = ClientBuilder.newClient();

        String port = System.getProperty("app.http.port");
        String context = System.getProperty("app.context.root");
        rootURL = "http://localhost:" + port + context;

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        jsonBuilder.add("name", "Member1");
        jsonBuilder.add("crewID", "000001");
        jsonBuilder.add("rank", "Captain");
        arrayBuilder.add(jsonBuilder.build());
        jsonBuilder = Json.createObjectBuilder();
        jsonBuilder.add("name", "Member2");
        jsonBuilder.add("crewID", "000002");
        jsonBuilder.add("rank", "Engineer");
        arrayBuilder.add(jsonBuilder.build());
        testData = arrayBuilder.build();
    }

    @AfterAll
    public static void teardown() {
        client.close();
    }

    @Test
    @Order(1)
    public void testAddCrewMember() {
        System.out.println("   === Adding " + testData.size()
                + " crew members to the database. ===");

        for (int i = 0; i < testData.size(); i++) {
            JsonObject member = (JsonObject) testData.get(i);
            String url = rootURL + "/api/crew";
            Response response = client.target(url).request().post(Entity.json(member));
            this.assertResponse(url, response);

            JsonObject newMember = response.readEntity(JsonObject.class);
            testIDs.add(newMember.getJsonObject("_id").getString("$oid"));

            response.close();
        }
        System.out.println("      === Done. ===");
    }

    @Test
    @Order(2)
    public void testUpdateCrewMember() {
        System.out.println("   === Updating crew member with id " + testIDs.get(0)
                + ". ===");

        JsonObject oldMember = (JsonObject) testData.get(0);

        JsonObjectBuilder newMember = Json.createObjectBuilder();
        newMember.add("name", oldMember.get("name"));
        newMember.add("crewID", oldMember.get("crewID"));
        newMember.add("rank", "Officer");

        String url = rootURL + "/api/crew/" + testIDs.get(0);
        Response response = client.target(url).request()
                .put(Entity.json(newMember.build()));

        this.assertResponse(url, response);

        System.out.println("      === Done. ===");
    }

    @Test
    @Order(3)
    public void testGetCrewMembers() {
        System.out.println("   === Listing crew members from the database. ===");

        String url = rootURL + "/api/crew";
        Response response = client.target(url).request().get();

        this.assertResponse(url, response);

        String responseText = response.readEntity(String.class);
        JsonReader reader = Json.createReader(new StringReader(responseText));
        JsonArray crew = reader.readArray();
        reader.close();

        int testMemberCount = 0;
        for (JsonValue value : crew) {
            JsonObject member = (JsonObject) value;
            String id = member.getJsonObject("_id").getString("$oid");
            if (testIDs.contains(id)) {
                testMemberCount++;
            }
        }

        assertEquals(testIDs.size(), testMemberCount,
                "Incorrect number of testing members.");

        System.out.println("      === Done. There are " + crew.size()
                + " crew members. ===");

        response.close();
    }

    @Test
    @Order(4)
    public void testDeleteCrewMember() {
        System.out.println("   === Removing " + testIDs.size()
                + " crew members from the database. ===");

        for (String id : testIDs) {
            String url = rootURL + "/api/crew/" + id;
            Response response = client.target(url).request().delete();
            this.assertResponse(url, response);
            response.close();
        }

        System.out.println("      === Done. ===");
    }

    private void assertResponse(String url, Response response) {
        assertEquals(200, response.getStatus(), "Incorrect response code from " + url);
    }
}
```



The test methods are annotated with the ***@Test*** annotation.

The following test cases are included in this class:

* ***testAddCrewMember()*** verifies that new members are correctly added to the database.

* ***testUpdateCrewMember()*** verifies that a crew member's information is correctly updated.

* ***testGetCrewMembers()*** verifies that a list of crew members is returned by the microservice API.

* ***testDeleteCrewMember()*** verifies that the crew members are correctly removed from the database.

### Running the tests

Because you started Open Liberty in dev mode, you can run the tests by pressing the ***enter/return*** key from the command-line session where you started dev mode.

You'll see the following output:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.application.CrewServiceIT
   === Adding 2 crew members to the database. ===
      === Done. ===
   === Updating crew member with id 5df8e0a004ccc019976c7d0a. ===
      === Done. ===
   === Listing crew members from the database. ===
      === Done. There are 2 crew members. ===
   === Removing 2 crew members from the database. ===
      === Done. ===
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.411 s - in it.io.openliberty.guides.application.CrewServiceIT
Results:
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

::page{title="Tearing down the environment"}

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran Liberty.

Then, run the following commands to stop and remove the ***mongo-guide*** container and to remove the ***mongo-sample*** and ***mongo*** images.

```bash
docker stop mongo-guide
docker rm mongo-guide
docker rmi mongo-sample
```

::page{title="Summary"}

### Nice Work!

You've successfully accessed and persisted data to a MongoDB database from a Java microservice using Contexts and Dependency Injection (CDI) and MicroProfile Config with Open Liberty.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-mongodb-intro*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-mongodb-intro
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Persisting%20data%20with%20MongoDB&guide-id=cloud-hosted-guide-mongodb-intro)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-mongodb-intro/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-mongodb-intro/pulls)



### Where to next?

* [Injecting dependencies into microservices](https://openliberty.io/guides/cdi-intro.html)
* [Configuring microservices](https://openliberty.io/guides/microprofile-config.html)
* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)

**Learn more about MicroProfile**
* [See the MicroProfile specs](https://microprofile.io/)
* [View the MicroProfile API](https://openliberty.io/docs/ref/microprofile)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
