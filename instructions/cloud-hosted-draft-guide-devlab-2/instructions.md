---
markdown-version: v1
tool-type: theia
---
::page{title="Welcome to the Producing and consuming messages in Java microservices guide!"}

Learn how to produce and consume messages to communicate between Java microservices in a standard way by using the Jakarta Messaging API with the embedded Liberty Messaging Server or an external messaging server, IBM MQ.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.



::page{title="What you'll learn"}

You’ll learn how to communicate between Java web services when one service is producing a continuous stream of asynchronous messages or events to be consumed by other services, rather than just sending and receiving individual requests for data. You will also learn how to use a messaging server and client to manage the production and consumption of the messages by the services.

In this guide, you will first use the embedded Liberty Messaging Server to manage messages, then you will optionally switch to using an external messaging server to manage the messages, in this case, [IBM MQ](https://www.ibm.com/products/mq). You might use an external messaging server if it is critical that none of the messages is lost if there is a system overload or outage; for example during a bank transfer in a banking application.

You will learn how to write your Java application using the Jakarta Messaging API which provides a standard way to produce and consume messages in Java application, regardless of which messaging server your application will ultimately use.

The application in this guide consists of two microservices, ***system*** and ***inventory***. Every 15 seconds, the ***system*** microservice computes and publishes a message that contains the system’s current CPU and memory load usage. The ***inventory*** microservice subscribes to that information at the ***/systems*** REST endpoint so that it can keep an updated list of all the systems and their current system loads.

You’ll create the ***system*** and ***inventory*** microservices using the Jakarta Messaging API to produce and consume the messages using the embedded Liberty Messaging Server.

![Application architecture where system and inventory services use the Jakarta Messaging to communicate.](https://raw.githubusercontent.com/OpenLiberty/guide-jms-intro/prod/assets/architecture.png)


You will then, optionally, reconfigure the application, without changing the application's Java code, to use an external IBM MQ messaging server instead.

::page{title="Getting started"}

To open a new command-line session,
select ***Terminal*** > ***New Terminal*** from the menu of the IDE.

Run the following command to navigate to the ***/home/project*** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-jms-intro.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-jms-intro.git
cd guide-jms-intro
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the ***finish*** directory and run the following Maven goal to build and install the ***models*** module. The ***models*** module contains the ***SystemLoad*** data class for both the ***system*** and ***inventory*** microservices to use.


```bash
cd /home/project/guide-jms-intro/finish
./mvnw -pl models clean install
```


Start the ***inventory*** microservice by running the following command:


Start IBM MQ by running the following command on the command-line session:

```bash
docker pull icr.io/ibm-messaging/mq:9.4.0.0-r3

docker volume create qm1data

docker run \
--env LICENSE=accept \
--env MQ_QMGR_NAME=QM1 \
--volume qm1data:/mnt/mqm \
--publish 1414:1414 --publish 9443:9443 \
--detach \
--env MQ_APP_PASSWORD=passw0rd \
--env MQ_ADMIN_PASSWORD=passw0rd \
--rm \
--platform linux/amd64 \
--name QM1 \
icr.io/ibm-messaging/mq:9.4.0.0-r3
```


Run the following command to make sure that the IBM MQ container is running:
```bash
docker ps
```

Replace the ***pom.xml*** file of the inventory service.

> To open the pom.xml file in your IDE, select
> ***File*** > ***Open*** > guide-jms-intro/start/inventory/pom.xml, or click the following button

::openFile{path="/home/project/guide-jms-intro/start/inventory/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-jms-intro-inventory</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- Liberty configuration -->
        <liberty.var.http.port>9081</liberty.var.http.port>
        <liberty.var.https.port>9444</liberty.var.https.port>
        <!-- IBM MQ -->
        <liberty.var.ibmmq-hostname>localhost</liberty.var.ibmmq-hostname>
        <liberty.var.ibmmq-port>1414</liberty.var.ibmmq-port>
        <liberty.var.ibmmq-channel>DEV.APP.SVRCONN</liberty.var.ibmmq-channel>
        <liberty.var.ibmmq-queue-manager>QM1</liberty.var.ibmmq-queue-manager>
        <liberty.var.ibmmq-username>app</liberty.var.ibmmq-username>
        <liberty.var.ibmmq-password>passw0rd</liberty.var.ibmmq-password>
        <liberty.var.ibmmq-inventory-queue-name>DEV.QUEUE.1</liberty.var.ibmmq-inventory-queue-name>
    </properties>
    
    <dependencies>
        <!-- Provided dependencies -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>10.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>7.0</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        
        <!--  Required dependencies -->
        <dependency>
           <groupId>io.openliberty.guides</groupId>
           <artifactId>guide-jms-intro-models</artifactId>
           <version>1.0-SNAPSHOT</version>
        </dependency>
        <!-- For tests -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.12.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-client</artifactId>
            <version>6.2.12.Final</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-json-binding-provider</artifactId>
            <version>6.2.12.Final</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
                <configuration>
                    <packagingExcludes>pom.xml</packagingExcludes>
                </configuration>
            </plugin>

            <!-- Liberty plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.11.3</version>
            </plugin>

            <!-- Plugin to run unit tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.3</version>
            </plugin>

            <!-- Plugin to run integration tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.5.3</version>
                <configuration>
                    <systemPropertyVariables>
                        <http.port>${liberty.var.http.port}</http.port>
                        <https.port>${liberty.var.https.port}</https.port>
                    </systemPropertyVariables>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```


Click the :fa-copy: ***Copy*** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to replace the code to the file.



Add the ***liberty.var.ibmmq-**** properties for the IBM MQ container. You can change to different values when you deploy the application on a production environment without modifying the Liberty ***server.xml*** configuration file.


Replace the ***server.xml*** file of the inventory service.

> To open the server.xml file in your IDE, select
> ***File*** > ***Open*** > guide-jms-intro/start/inventory/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-jms-intro/start/inventory/src/main/liberty/config/server.xml"}



```xml
<server description="Inventory Service">

  <featureManager>
    <platform>jakartaee-10.0</platform>
    <platform>microprofile-7.0</platform>
    <feature>restfulWS</feature>
    <feature>cdi</feature>
    <feature>jsonb</feature>
    <feature>mpHealth</feature>
    <feature>mpConfig</feature>
    <feature>messaging</feature>
    <feature>messagingClient</feature>
    <feature>messagingServer</feature>
    <feature>enterpriseBeansLite</feature>
    <feature>mdb</feature>
  </featureManager>

  <variable name="http.port" defaultValue="9081"/>
  <variable name="https.port" defaultValue="9444"/>

  <httpEndpoint id="defaultHttpEndpoint" host="*"
                httpPort="${http.port}" httpsPort="${https.port}"/>

  <wasJmsEndpoint id="InboundJmsCommsEndpoint"
                  host="*"
                  wasJmsPort="7277"
                  wasJmsSSLPort="9101"/>

  <jmsQueue id="InventoryQueue" jndiName="jms/InventoryQueue">
    <properties.wmqjmsra baseQueueName="${ibmmq-inventory-queue-name}"/>
  </jmsQueue>

  <jmsActivationSpec id="guide-jms-intro-inventory/InventoryQueueListener">
    <properties.wmqjmsra
      hostName="${ibmmq-hostname}"
      port="${ibmmq-port}"
      channel="${ibmmq-channel}"
      queueManager="${ibmmq-queue-manager}"
      userName="${ibmmq-username}"
      password="${ibmmq-password}"
      transportType="CLIENT"/>
  </jmsActivationSpec>

  <resourceAdapter id="wmqjmsra"
    location="https://repo.maven.apache.org/maven2/com/ibm/mq/wmq.jakarta.jmsra/9.4.0.0/wmq.jakarta.jmsra-9.4.0.0.rar"/>
    
  <logging consoleLogLevel="INFO"/>

  <webApplication location="guide-jms-intro-inventory.war" contextRoot="/"/>

</server>
```




Refine the ***jmsQueue*** and ***jmsActivationSpec*** configurations with the variables for IBM MQ settings. Add the ***resourceAdapter*** element to define the RAR file that provides the IBM MQ classes for Java and JMS. Note that the ***messagingEngine*** and ***jmsConnectionFactory*** configurations are removed from the configuration because they are no longer required.

Replace the ***pom.xml*** file of the system service.

> To open the pom.xml file in your IDE, select
> ***File*** > ***Open*** > guide-jms-intro/start/system/pom.xml, or click the following button

::openFile{path="/home/project/guide-jms-intro/start/system/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.openliberty.guides</groupId>
    <artifactId>guide-jms-intro-system</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- Liberty configuration -->
        <liberty.var.http.port>9082</liberty.var.http.port>
        <liberty.var.https.port>9445</liberty.var.https.port>
        <liberty.var.inventory.jms.host>localhost</liberty.var.inventory.jms.host>
        <liberty.var.inventory.jms.port>7277</liberty.var.inventory.jms.port>
        <!-- IBM MQ -->
        <liberty.var.ibmmq-hostname>localhost</liberty.var.ibmmq-hostname>
        <liberty.var.ibmmq-port>1414</liberty.var.ibmmq-port>
        <liberty.var.ibmmq-channel>DEV.APP.SVRCONN</liberty.var.ibmmq-channel>
        <liberty.var.ibmmq-queue-manager>QM1</liberty.var.ibmmq-queue-manager>
        <liberty.var.ibmmq-username>app</liberty.var.ibmmq-username>
        <liberty.var.ibmmq-password>passw0rd</liberty.var.ibmmq-password>
        <liberty.var.ibmmq-inventory-queue-name>DEV.QUEUE.1</liberty.var.ibmmq-inventory-queue-name>
    </properties>

    <dependencies>
        <!-- Provided dependencies -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>10.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>7.0</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>
        <!-- Required dependencies -->
        <dependency>
            <groupId>io.openliberty.guides</groupId>
            <artifactId>guide-jms-intro-models</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.17</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.17</version>
        </dependency>
        <!-- For tests -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.12.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
                <configuration>
                    <packagingExcludes>pom.xml</packagingExcludes>
                </configuration>
            </plugin>

            <!-- Liberty plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.11.3</version>
            </plugin>

            <!-- Plugin to run unit tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.3</version>
            </plugin>

            <!-- Plugin to run integration tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.5.3</version>
                <executions>
                    <execution>
                        <id>integration-test</id>
                        <goals>
                            <goal>integration-test</goal>
                        </goals>
                        <configuration>
                            <trimStackTrace>false</trimStackTrace>
                        </configuration>
                    </execution>
                    <execution>
                        <id>verify</id>
                        <goals>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```




Add the ***liberty.var.ibmmq-**** properties for the IBM MQ container as you did for the ***inventory*** microservice previously.


Replace the ***server.xml*** file of the system service.

> To open the server.xml file in your IDE, select
> ***File*** > ***Open*** > guide-jms-intro/start/system/src/main/liberty/config/server.xml, or click the following button

::openFile{path="/home/project/guide-jms-intro/start/system/src/main/liberty/config/server.xml"}



```xml
<server description="System Service">

  <featureManager>
    <platform>jakartaee-10.0</platform>
    <platform>microprofile-7.0</platform>
    <feature>cdi</feature>
    <feature>jsonb</feature>
    <feature>mpHealth</feature>
    <feature>mpConfig</feature>
    <feature>messaging</feature>
    <feature>messagingClient</feature>
    <feature>enterpriseBeansLite</feature>
    <feature>mdb</feature>
  </featureManager>

  <variable name="http.port" defaultValue="9082"/>
  <variable name="https.port" defaultValue="9445"/>
  <variable name="inventory.jms.host" defaultValue="localhost"/>
  <variable name="inventory.jms.port" defaultValue="7277"/>

  <httpEndpoint id="defaultHttpEndpoint" host="*"
                httpPort="${http.port}" httpsPort="${https.port}" />

  <connectionManager id="InventoryCM" maxPoolSize="400" minPoolSize="1"/>

  <jmsConnectionFactory
    connectionManagerRef="InventoryCM"
    jndiName="InventoryConnectionFactory">
    <properties.wmqjmsra
      hostName="${ibmmq-hostname}"
      port="${ibmmq-port}"
      channel="${ibmmq-channel}"
      queueManager="${ibmmq-queue-manager}"
      userName="${ibmmq-username}"
      password="${ibmmq-password}"
      transportType="CLIENT" />
  </jmsConnectionFactory>

  <jmsQueue id="InventoryQueue" jndiName="jms/InventoryQueue">
    <properties.wmqjmsra baseQueueName="${ibmmq-inventory-queue-name}"/>
  </jmsQueue>

  <resourceAdapter id="wmqjmsra"
    location="https://repo.maven.apache.org/maven2/com/ibm/mq/wmq.jakarta.jmsra/9.4.0.0/wmq.jakarta.jmsra-9.4.0.0.rar"/>

  <logging consoleLogLevel="INFO"/>

  <webApplication location="guide-jms-intro-system.war" contextRoot="/"/>

</server>
```




Replace the ***properties.wasJms*** configuration by the ***properties.wmqjmsra*** configuration. All property values are defined in the ***pom.xml*** file that you replaced. Also, modify the ***jmsQueue*** property to set the ***baseQueueName*** value with the ***${ibmmq-inventory-queue-name}*** variable. Add the ***resourceAdapter*** element like you did for the ***inventory*** microservice.


Start the ***inventory*** microservice by running the following command in dev mode:


```bash
cd /home/project/guide-jms-intro/start
./mvnw -pl inventory liberty:dev
```

Next, open another command-line session, navigate to the ***start*** directory, and start the ***system*** microservice by using the following command:


```bash
cd /home/project/guide-jms-intro/start
./mvnw -pl system liberty:dev
```

When you see the following message, your Liberty instances are ready in dev mode:

```
The defaultServer server is ready to run a smarter planet.
```



Open another command-line session by selecting ***Terminal*** > ***New Terminal*** from the menu of the IDE.


You can access the ***inventory*** microservice by the ***http\://localhost:9081/inventory/systems*** URL.


_To see the output for this URL in the IDE, run the following command at a terminal:_

```bash
curl -s http://localhost:9081/inventory/systems | jq
```



In the command shell where ***inventory*** dev mode is running, press ***enter/return*** to run the tests. If the tests pass, you'll see output that is similar to the following example:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running it.io.openliberty.guides.inventory.InventoryEndpointIT
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.325 sec - in it.io.openliberty.guides.inventory.InventoryEndpointIT

Results :

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

After you are finished checking out the application, stop the Liberty instances by pressing `Ctrl+C` in the command-line sessions where you ran the ***system*** and ***inventory*** microservices.

Run the following commands to stop the running IBM MQ container and clean up the ***qm1data*** volume:

```bash
docker stop QM1
docker rm QM1
docker volume remove qm1data
```

::page{title="Summary"}

### Nice Work!

You just developed a Java cloud-native application that uses Jakarta Messaging to produce and consume messages in Open Liberty.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-jms-intro*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-jms-intro
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Producing%20and%20consuming%20messages%20in%20Java%20microservices&guide-id=cloud-hosted-guide-jms-intro)

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-jms-intro/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-jms-intro/pulls)



### Where to next?

* [Bidirectional communication between services using Jakarta WebSocket](https://openliberty.io/guides/jakarta-websocket.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
