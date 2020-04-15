## Module 1: Build & Feature-based Build

# Build

Liberty has support for building and deploying applications using Maven and Gradle.  The source and documentation for these plugins can be found here:
* https://github.com/wasdev/ci.maven
* https://github.com/wasdev/ci.gradle

The Masterclass will make use of the `liberty-maven-plugin`.

Take a look at the maven build file for the coffee-shop project: `open-liberty-masterclass/start/barista/pom.xml`

Go to the barista project:

```
cd open-liberty-masterclass/start/barista
```

Build and run the barista service:

```
mvn install liberty:run
```

Visit: http://localhost:9081/openapi/ui

This page is an OpenAPI UI that lets you try out the barista service.  

Click on `POST` and then `Try it out`

Under `Example Value` specify:

```JSON
{
  "type": "ESPRESSO"
}
```

Click on `Execute`

Scroll down and you should see the server response code of `201`.  This says that the barista request to make an `ESPRESSO` was successfully `Created`.


# Feature-based Build

The `liberty-maven-plugin` lets you specify which Liberty features you want to build against.

Take a look at the maven build file for the coffee-shop project: `open-liberty-masterclass/start/coffee-shop/pom.xml`

In order for the plugin to know what features are available, we need to tell it where to find the feature information.  This is done with the following `<dependencyManagement/>` section:

```XML
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.openliberty.features</groupId>
                <artifactId>features-bom</artifactId>
                <version>RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```
We can now specify which features we want to build against.  

In the same `coffee-shop/pom.xml` locate the `<dependencies/>` section.  You'll see, for example, that we're depending on `jaxrs-2.1` because we're using this feature to implement the REST service:

``` XML
    <dependencies>
      <!--Open Liberty features -->
        <dependency>
            <groupId>io.openliberty.features</groupId>
            <artifactId>jaxrs-2.1</artifactId>
            <type>esa</type>
            <scope>provided</scope>
        </dependency>
        ...
    </dependencies>
```

Let's add add dependency on the `MicroProfile OpenAPI` feature so we can try the `coffee-shop` service out.

Add the following dependency to the `coffee-shop/pom.xml`

```XML
        <dependency>
            <groupId>io.openliberty.features</groupId>
            <artifactId>mpOpenAPI-1.1</artifactId>
            <type>esa</type>
            <scope>provided</scope>
        </dependency> 
```
The above dependency will cause the feature to be installed during the build, but we also need to tell the server to load it at runtime.

Open the file `open-liberty-masterclass/start/coffee-shop/src/main/liberty/config/server.xml`

This file is the configuration for the `coffee-shop` server.

Near the top of the file, you'll see the following `<featureManager/>` entry:

```XML
    <featureManager>
        <feature>jaxrs-2.1</feature>
        <feature>ejbLite-3.2</feature>
        <feature>cdi-2.0</feature>
        <feature>beanValidation-2.0</feature>
        <feature>mpHealth-2.0</feature>
        <feature>mpConfig-1.3</feature>
        <feature>mpRestClient-1.3</feature>
        <feature>jsonp-1.1</feature>
    </featureManager>
```
This entry lists all the features to be loaded by the server.  Add the following entry inside the `<featureManager/>` element:

```XML
        <feature>mpOpenAPI-1.1</feature>
```

Build and run the coffee-shop service:

```
mvn install liberty:run
```

Visit: http://localhost:9080/openapi/ui

As with the barista service, this is an Open API UI page that lets to try out the service API for the coffee-shop service.

For a full list of all the features available, see https://openliberty.io/docs/ref/feature/.