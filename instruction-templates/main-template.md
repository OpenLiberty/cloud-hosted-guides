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
cd guide-docker
```
{: codeblock}

The **start** directory contains the starting project that you will build upon. 

The **finish** directory contains the finished project that you will build. 

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

## Well Done

Example:
Congratulations, you have built, a cloud-native application, seen how you can monitor it for health and metrics, change its configuration, and package and run it in Docker, ready for deployment to your cloud of choice.  I recommend IBM Cloud or IBM Cloud Private, of course ;)
