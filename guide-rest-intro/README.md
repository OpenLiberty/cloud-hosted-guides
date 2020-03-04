# Creating a RESTful web service

### What you will learn

You will learn how to build and test a simple REST service with JAX-RS and JSON-B, which will expose
the JVM's system properties. The REST service will respond to **GET** requests made to the **\http://localhost:9080/LibertyProject/System/properties** URL.

The service responds to a **GET** request with a JSON representation of the system properties, where
each property is a field in a JSON object like this:

[source,json]
----
{
  "os.name":"Mac",
  "java.version": "1.8"
}
----

### Introduction

When you create a new REST application, the design of the API is important. The JAX-RS APIs can be
used to create JSON-RPC, or XML-RPC APIs, but it wouldn't be a RESTful service. A good RESTful service
is designed around the resources that are exposed, and on how to create, read, update, and delete the
resources.

The service responds to **GET** requests to the **/System/properties** path. The **GET** request should
return a **200 OK** response that contains all of the JVM's system properties.

## Getting Started

As an example:
If a terminal window does not open navigate:

`Terminal -> New Terminal`

Check you are in the **home/project** folder:

`pwd`

The fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:

`git clone https://github.com/yasmin-aumeeruddy/SkillsNetworkLabs.git`

`cd SkillsNetworkLabs`

# Title for Step 1

## Summary of Step 1

### Sub Heading 1

Java Code Example
```Java
@Readiness
@ApplicationScoped
public class GreetingReadinessCheck implements HealthCheck {

    public boolean isReady() {

        // Check the health of dependencies here

        return true;

    }

    @Override
    public HealthCheckResponse call() {
        boolean up = isReady();
        return HealthCheckResponse.named("GreetingServiceReadiness").state(up).build();
    }
}
```

### Sub Heading 2

### Further Reading


# Title for Step 2

## Summary of Step 2

Bold words reference things that need changing.
**pom.xml**

### Sub Heading 1
Json Code Example
```JSON
{
  "checks": [
    {
      "data": {
        
      },
      "name": "GreetingServiceReadiness",
      "status": "UP"
    }
  ],
  "status": "UP"
}
```

### Build and run in Docker

### Further Reading


# Title for Step 3

## Summary of Step 3

### Sub Heading 1

### Sub Heading 2
Have as many steps as you want. They are all seperated via the h1 heading such as one with one #

# Summary

## Well Done

Example:
Congratulations, you have built, a cloud-native application, seen how you can monitor it for health and metrics, change its configuration, and package and run it in Docker, ready for deployment to your cloud of choice.  I recommend IBM Cloud or IBM Cloud Private, of course ;)
