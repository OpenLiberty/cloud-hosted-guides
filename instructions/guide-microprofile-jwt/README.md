# Securing microservices with JSON Web Tokens

### What you will learn

You will add MicroProfile JWT to validate security tokens in the **system** and **inventory** microservices. You will use a token-based authentication mechanism to authenticate, authorize, and verify user identities based on a security token.

In addition, you will learn how to verify token claims through getters with MicroProfile JWT.

For microservices, a token-based authentication mechanism offers a lightweight way for security controls and security tokens to propagate user identities across different services. JSON Web Token (JWT) is becoming the most common token format because it follows well-defined and known standards.

MicroProfile JWT standards define the required format of JWT for authentication and authorization. The standards also map JWT token claims to various Java EE container APIs and make the set of claims available through getters.

The application that you will be working with is an **inventory** service, which stores the information about various JVMs that run on different systems. Whenever a request is made to the **inventory** service to retrieve the JVM system properties of a particular host, the **inventory** service communicates with the **system** service on that host to get these system properties. The JWT token gets propagated and verified during the communication between two services.

# Getting Started

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
