
# Welcome to the Consuming a RESTful web service guide!

Explore how to access a simple RESTful web service and consume its resources in Java

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.


using JSON-B and JSON-P.


# What you'll learn

You will learn how to access a REST service, serialize a Java object that contains a
list of artists and their albums, and use two different approaches to deserialize
the returned JSON resources. The first approach consists of using the Java API for JSON Binding (JSON-B)
to directly convert JSON messages into Java objects. The second approach consists of
using the Java API for JSON Processing (JSON-P) to process the JSON.

You can find your service at `\http://localhost:9080/artists`.

Run the following curl command to retrieve the total number of artists.
```
curl http://localhost:9080/artists/total
```
{: codeblock}

Run the following curl command to retrieve a particular artist's total number of albums.
```
curl http://localhost:9080/artists/total/<artist>
```
{: codeblock}

After you are finished checking out the application, stop the Open Liberty server by pressing **CTRL+C**
in the command-line session where you ran the server. Alternatively, you can run the **liberty:stop** goal
from the **finish** directory in another shell session:

```
mvn liberty:stop
```
{: codeblock}



# Starting the service

Navigate to the **start** directory to begin.

When you run Open Liberty in development mode, known as dev mode, the server listens for file changes and automatically recompiles and 
deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```
mvn liberty:dev
```
{: codeblock}


After you see the following message, your application server in dev mode is ready:

```
Press the Enter key to run tests on demand.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, 
or open the project in your editor.

The application that you'll build upon was created for you. After your server is
ready, run the following curl command to access the service. 
```
curl http://localhost:9080/artists
```
{: codeblock}


# Creating POJOs



To deserialize a JSON message, start with creating Plain Old Java Objects (POJOs) that represent what
is in the JSON and whose instance members map to the keys in the JSON.

For the purpose of this guide, you are given two POJOs.
The **Artist** object has two instance members **name** and **albums**, 
which map to the artist name and the collection of the albums they have written. The **Album** object represents a 
single object within the album collection, and contains three instance members **title**, **artistName**, and **totalTracks**, which map to the album title, the artist who wrote the album, and the number of tracks the album contains.

# Introducing JSON-B and JSON-P

JSON-B is a feature introduced with Java EE 8 and strengthens Java support for JSON.
With JSON-B you directly serialize and deserialize POJOs. This API gives you a
variety of options for working with JSON resources.

In contrast, you need to use helper methods with JSON-P to process a JSON response.
This tactic is more straightforward, but it can be cumbersome with more complex classes.

JSON-B is built on top of the existing JSON-P API. JSON-B can do everything that JSON-P can do
and allows for more customization for serializing and deserializing.

### Using JSON-B

JSON-B requires a POJO to have a public default no-argument constructor for deserialization
and binding to work properly.

The JSON-B engine includes a set of default mapping rules, which can be run without
any customization annotations or custom configuration. In some instances, you might
find it useful to deserialize a JSON message with only certain fields, specific
field names, or classes with custom constructors. In these cases,
annotations are necessary and recommended:

* The **@JsonbProperty** annotation to map JSON keys to class instance members and vice versa.
Without the use of this annotation, JSON-B will attempt to do POJO mapping, matching the keys in
the JSON to the class instance members by name. JSON-B will attempt to match the JSON key 
with a Java field or method annotated with **@JsonbProperty** where the value in the
annotation exactly matches the JSON key. If no annotation exists with the given JSON key, 
JSON-B will attempt to find a matching field with the same name. If no match is found, 
JSON-B attempts to find a matching getter method for serialization or a matching setter 
method for de-serialization. A match occurs when the property name of the method matches 
the JSON key. If no matching getter or setter method is found, serialization or 
de-serialization, respectively, fails with an exception. The Artist POJO does not require 
this annotation because all instance members match the JSON keys by name.

* The **@JsonbCreator** and **@JsonbProperty** annotations to annotate a custom constructor.
These annotations are required for proper parameter substitution when a custom constructor is used.

* The **@JsonbTransient** annotation to define an object property that does not map to a JSON
property. While the use of this annotation is good practice, it is only necessary for serialization.

For more information on customization with JSON-B, see the [official JSON-B site](http://json-b.net).


# Consuming the REST resource




The **Artist** and **Album** POJOs are ready for deserialization. 
Next, we'll learn to consume the JSON response from your REST service.

Create the **Consumer** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-rest-client-java/start/src/main/java/io/openliberty/guides/consumingrest/Consumer.java
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-rest-client-java/start/src/main/java/io/openliberty/guides/consumingrest/Consumer.java



### Processing JSON using JSON-B


JSON-B is a Java API that is used to serialize Java objects to JSON messages and vice versa.

Open Liberty's JSON-B feature on Maven Central includes the JSON-B provider through transitive dependencies.
The JSON-B APIs are provided by the MicroProfile dependency in your **pom.xml** file. 
Look for the dependency with the **microprofile** artifact ID. 

The **consumeWithJsonb()** method in the **Consumer** class makes a **GET** request to the
running artist service and retrieves the JSON. To bind the JSON into an **Artist**
array, use the **Artist[]** entity type in the **readEntity** call.

### Processing JSON using JSON-P

The **consumeWithJsonp()** method in the **Consumer** class makes a **GET** request
to the running artist service and retrieves the JSON. This method then uses the
**collectArtists** and **collectAlbums** helper methods. These helper methods will
parse the JSON and collect its objects into individual POJOs. Notice that you can
use the custom constructors to create instances of **Artist** and **Album**.

# Creating additional REST resources


Now that you can consume a JSON resource you can put that data to use.

Replace the **ArtistResource** class.

> From the menu of the IDE, select 
 **File** > **Open** > guide-rest-client-java/start/src/main/java/io/openliberty/guides/consumingrest/service/ArtistResource.java



* The **getArtists()** method provides the raw JSON data service that you accessed at the
beginning of this guide.

* The **getJsonString()** method uses JSON-B to return the JSON as a string that will
be used later for testing.

* The **getTotalAlbums()** method uses JSON-B to return the total number of albums present
in the JSON for a particular artist. The method returns -1 if this artist does not exist.

* The **getTotalArtists()** method uses JSON-P to return the total number of artists
present in the JSON.

The methods that you wrote in the **Consumer** class could be written directly in the
**ArtistResource** class. However, if you are consuming a REST resource from a third
party service, you should separate your **GET**/**POST** requests from your data consumption.


# Running the application

The Open Liberty server was started in development mode at the beginning of the guide and all the changes were automatically picked up.

You can find your service at `\http://localhost:9080/artists`.

Run the following curl command to retrieve the total number of artists.
```
curl http://localhost:9080/artists/total
```
{: codeblock}

Run the following curl command to retrieve a particular artist's total number of albums.
```
curl http://localhost:9080/artists/total/<artist>
```
{: codeblock}



# Testing deserialization

Create the **ConsumingRestIT** class.

> Run the following touch command in your terminal
```
touch /home/project/guide-rest-client-java/start/src/test/java/it/io/openliberty/guides/consumingrest/ConsumingRestIT.java 
```
{: codeblock}


> Then from the menu of the IDE, select **File** > **Open** > guide-rest-client-java/start/src/test/java/it/io/openliberty/guides/consumingrest/ConsumingRestIT.java 



Maven finds and executes all tests under the **src/test/java/it/** directory, 
and each test method must be marked with the **@Test** annotation.

You can use the **@BeforeAll** and **@AfterAll** annotations to perform any one-time setup and teardown
tasks before and after all of your tests run. You can also use the **@BeforeEach** and **@AfterEach** annotations
to perform setup and teardown tasks for individual test cases.

### Testing the binding process


The **yasson** dependency was added in your **pom.xml** file so that your test classes have access to JSON-B.

The **testArtistDeserialization** test case checks that **Artist** instances created from
the REST data and those that are hardcoded perform the same.

The **assertResponse** helper method ensures that the response code you receive is valid (200).

### Processing with JSON-B test

The **testJsonBAlbumCount** and **testJsonBAlbumCountForUnknownArtist** tests both use the **total/{artist}**
endpoint which invokes JSON-B.

The **testJsonBAlbumCount** test case checks that deserialization with JSON-B was done correctly
and that the correct number of albums is returned for each artist in the JSON.

The **testJsonBAlbumCountForUnknownArtist** test case is similar to **testJsonBAlbumCount**
but instead checks an artist that does not exist in the JSON and ensures that a
value of `-1` is returned.

### Processing with JSON-P test

The **testJsonPArtistCount** test uses the **total** endpoint which invokes JSON-P. This test
checks that deserialization with JSON-P was done correctly and that the correct number
of artists is returned.


### Running the tests

Since you started Open Liberty in development mode at the start of the guide, press the **enter/return** key to run the tests.

If the tests pass, you see a similar output to the following example:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.consumingrest.ConsumingRestIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.59 sec - in it.io.openliberty.guides.consumingrest.ConsumingRestIT

Results :

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

```

When you are done checking out the service, exit development mode by typing `q` in the command-line session where you ran the server, 
and then press the **enter/return** key.

# Building the application

If you are satisfied with your application, run the Maven **package** goal to build the WAR file in the **target** directory:

```
mvn package
```
{: codeblock}



# Summary

## Nice Work!

You just accessed a simple RESTful web service and consumed its resources by using JSON-B and JSON-P in Open Liberty.






## Clean up your environment

Clean up your online environment so that it is ready to be used with the next guide:

Delete the **guide-rest-client-java** project by running the following commands:

```
cd /home/project
rm -fr guide-rest-client-java
```
{: codeblock}

## What could make this guide better?
* [Raise an issue to share feedback](https://github.com/OpenLiberty/guide-rest-client-java/issues)
* [Create a pull request to contribute to this guide](https://github.com/OpenLiberty/guide-rest-client-java/pulls)




## Where to next? 

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Consuming a RESTful web service with AngularJS](https://openliberty.io/guides/rest-client-angularjs.html)


## Log out of the session

- [View the MicroProfile](https://openliberty.io/docs/latest/microprofile.html){:target="_blank"}

- <a href="https://openliberty.io/docs/latest/microprofile.html" target="blank">View the MicroProfile</a>
