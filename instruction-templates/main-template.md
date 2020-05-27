# The Title of the quicklab

### What you will learn

Some text talking about what the user will learn from doing this quick lab

### Introduction

Introduction to the technology they will use

## Getting Started

If a terminal window does not open navigate:

>[Terminal -> New Terminal]

Check you are in the **home/project** folder:

```
pwd
```
{: codeblock}

The fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:

```
git clone https://github.com/openliberty/guide-microprofile-config.git
cd guide-microprofile-config
```
{: codeblock}

The **start** directory contains the starting project that you will build upon. 

The **finish** directory contains the finished project that you will build. 

# Title for Step 1

## Summary of Step 1

### Sub Heading 1

File creation example

Create the **ErrorPathIT** class:

```
touch src/test/java/io/openliberty/guides/testing/ErrorPathIT.java
```
{: codeblock}

> [File -> Open] guide-microprofile-rest-client/start/src/test/java/io/openliberty/guides/testing/ErrorPathIT.java

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
{: codeblock}

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

## Clean up your environment

Delete the **guide-microprofile-rest-client** project by navigating to the **/home/project/** directory

```
cd ../..
rm -r -f guide-microprofile-rest-client
rmdir guide-microprofile-rest-client
```
{: codeblock}

## Well Done

Nice work! You just invoked a remote service by using a template interface with MicroProfile Rest Client in Open Liberty.
