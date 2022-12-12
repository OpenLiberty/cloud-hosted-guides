HELLLOO
---
markdown-version: v1
title: instructions
branch: lab-435-instruction
version-history-start-date: 2021-12-03 21:48:34 UTC
tool-type: theia
---
::page{title="Welcome to the Consuming a RESTful web service with ReactJS guide!"}

Explore how to access a simple RESTful web service and consume its resources with ReactJS in Open Liberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You will learn how to access a REST service and deserialize the returned JSON that contains a list of artists and their albums by using an HTTP client with the ReactJS library. You will then present this data by using a ReactJS paginated table component.

[ReactJS](https://reactjs.org/) is a JavaScript library that is used to build user interfaces. Its main purpose is to incorporate a component-based approach to create reusable UI elements. With ReactJS, you can also interface with other libraries and frameworks. Note that the names ReactJS and React are used interchangeably.

The React application in this guide is provided and configured for you in the ***src/main/frontend*** directory. The application uses the [Create React App](https://reactjs.org/docs/create-a-new-react-app.html) prebuilt configuration to set up the modern single-page React application. The [create-react-app](https://github.com/facebook/create-react-app) integrated toolchain is a comfortable environment for learning React and is the best way to start building a new single-page application with React.


The REST service that provides the resources was written for you in advance in the back end of the application, and it responds with the ***artists.json*** in the ***src/resources*** directory. You will implement a ReactJS client as the front end of your application, which consumes this JSON file and displays its contents on a single-page webpage. 

To learn more about REST services and how you can write them, see the [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html) guide.

::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-rest-client-reactjs.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-rest-client-reactjs.git
cd guide-rest-client-reactjs
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.


### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. The React front end is already pre-built for you and the static files from the production build can be found in the ***src/main/webapp/static*** directory.


In this IBM cloud environment, you need to update the URL to access the ***artists.json***. Run the following commands to go to the ***finish*** directory and update the file where specified the URL:
```bash
cd finish
sed -i 's=http://localhost:9080/artists='"http://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')/artists"'=' src/main/webapp/static/js/main.a223975a.js
```

To try out the application, run the following Maven goal to build the application and deploy it to Open Liberty:
```bash
mvn liberty:run
```

After you see the following message, your application server is ready:

```
The defaultServer server is ready to run a smarter planet.
```


When the server is running, select **Terminal** > **New Terminal** from the menu of the IDE to open another command-line session. Open your browser and check out the application by going to the URL that the following command returns:
```bash
echo http://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')
```

See the following output:

![React Paginated Table](https://raw.githubusercontent.com/OpenLiberty/guide-rest-client-reactjs/prod/assets/react-table.png)


After you are finished checking out the application, stop the Open Liberty server by pressing `Ctrl+C` in the command-line session where you ran the server. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```


::page{title="Starting the service"}

Before you begin the implementation, start the provided REST service so that the artist JSON is available to you.

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-rest-client-reactjs/start
```

When you run Open Liberty in development mode, known as dev mode, the server listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following goal to start Open Liberty in dev mode:

```bash
mvn liberty:dev
```

After you see the following message, your application server in dev mode is ready:

```
**************************************************************
*    Liberty is running in dev mode.
```

Dev mode holds your command-line session to listen for file changes. Open another command-line session to continue, or open the project in your editor.


After the server is started, run the following curl command to view your artist JSON.
```bash
curl -s http://localhost:9080/artists | jq
```

All the dependencies for the React front end can be found in ***src/main/frontend/src/package.json***, and they are installed before the front end is built by the ***frontend-maven-plugin***. Additionally, some provided ***CSS*** stylesheets files are provided and can be found in the ***src/main/frontend/src/Styles*** directory.


::page{title="Project configuration"}

The front end of your application uses Node.js to build your React code. The Maven project is configured for you to install Node.js and produce the production files, which are copied to the web content of your application.

Node.js is a server-side JavaScript runtime that is used for developing networking applications. Its convenient package manager, [npm](https://www.npmjs.com/), is used to run the React build scripts that are found in the ***package.json*** file. To learn more about Node.js, see the official [Node.js documentation](https://nodejs.org/en/docs/).


Take a look at the **pom.xml** file.
> From the menu of the IDE, select **File** > **Open** > guide-rest-client-reactjs/start/pom.xml, or click the following button
::openFile{path="/home/project/guide-rest-client-reactjs/start/pom.xml"}

The ***frontend-maven-plugin*** is used to ***install*** the dependencies that are listed in your ***package.json*** file from the npm registry into a folder called ***node_modules***. The ***node_modules*** folder can be found in your ***working*** directory. Then, the configuration ***produces*** the production files to the ***src/main/frontend/build*** directory. 

The ***maven-resources-plugin*** copies the ***static*** content from the ***build*** directory to the ***web content*** of the application.


::page{title="Creating the default page"}

You need to create the entry point of your React application. ***create-react-app*** uses the ***index.js*** file as the main entry point of the application. This JavaScript file corresponds with the ***index.html*** file, which is the entry point where your code runs in the browser.

Create the ***index.js*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-client-reactjs/start/src/main/frontend/src/index.js
```


> Then, to open the index.js file in your IDE, select
> **File** > **Open** > guide-rest-client-reactjs/start/src/main/frontend/src/index.js, or click the following button

::openFile{path="/home/project/guide-rest-client-reactjs/start/src/main/frontend/src/index.js"}



```javascript
import React from 'react';
import ReactDOM from 'react-dom';
import './Styles/index.css';
import App from './Components/App';

ReactDOM.render(<App />, document.getElementById('root'));
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.



The ***React*** library imports the ***react*** package. A DOM, or Document Object Model, is a programming interface for HTML and XML documents. React offers a virtual DOM, which is essentially a copy of the browser DOM that resides in memory. The React virtual DOM improves the performance of your web application and plays a crucial role in the rendering process. The ***react-dom*** package provides DOM-specific methods that can be used in your application to get outside of the React model, if necessary. 

The ***render*** method takes an HTML DOM element and tells the ReactDOM to render your React application inside of this DOM element. To learn more about the React virtual DOM, see the [ReactDOM](https://reactjs.org/docs/react-dom.html) documentation.


::page{title="Creating the React components"}

A React web application is a collection of components, and each component has a specific function. You will create the components that are used in the application to acquire and display data from the REST API. 

The main component in your React application is the ***App*** component. You need to create the ***App.js*** file to act as a container for all other components. 

Create the ***App.js*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-client-reactjs/start/src/main/frontend/src/Components/App.js
```


> Then, to open the App.js file in your IDE, select
> **File** > **Open** > guide-rest-client-reactjs/start/src/main/frontend/src/Components/App.js, or click the following button

::openFile{path="/home/project/guide-rest-client-reactjs/start/src/main/frontend/src/Components/App.js"}



```javascript
import React from 'react';
import ArtistTable from './ArtistTable';

function App() {
  return (
      <ArtistTable/>
  );
}

export default App;
```



The ***App.js*** file returns the ***ArtistTable*** component to create a reusable element that encompasses your web application. 

Next, create the ***ArtistTable*** component that fetches data from your back end and renders it in a table. 

Create the ***ArtistTable.js*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-client-reactjs/start/src/main/frontend/src/Components/ArtistTable.js
```


> Then, to open the ArtistTable.js file in your IDE, select
> **File** > **Open** > guide-rest-client-reactjs/start/src/main/frontend/src/Components/ArtistTable.js, or click the following button

::openFile{path="/home/project/guide-rest-client-reactjs/start/src/main/frontend/src/Components/ArtistTable.js"}



```javascript
import React, { Component } from 'react';
import ReactTable from 'react-table-6';
import 'react-table-6/react-table.css';

class ArtistTable extends Component {
  state = {
    posts: [],
    isLoading: true,
    error: null,
  };


  render() {
    const { isLoading, posts } = this.state;
    const columns = [{
      Header: 'Artist Info',
      columns: [
        {
          Header: 'Artist ID',
          accessor: 'id'
        },
        {
          Header: 'Artist Name',
          accessor: 'name'
        },
        {
          Header: 'Genres',
          accessor: 'genres',
        }
      ]
    },
    {
      Header: 'Albums',
      columns: [
        {
          Header: 'Title',
          accessor: 'title',
        },
        {
          Header: 'Number of Tracks',
          accessor: 'ntracks',
        }
      ]
    }
  ]

  return (
    <div>
      <h2>Artist Web Service</h2>
      {!isLoading ? (
        <ReactTable
          data={posts}
          columns={columns}
          defaultPageSize={4}
          pageSizeOptions={[4, 5, 6]}
        />) : (
          <p>Loading .....</p>
        )}
    </div>
    );
  }
}

export default ArtistTable;
```



The ***React*** library imports the ***react*** package for you to create the ***ArtistTable*** component as inheritance of the ***React Component*** and use its values. The ***state*** object is initialized to represent the state of the posts that appear on the paginated table. The ***ArtistTable*** component also needs to be ***exported*** as a reusable UI element that can be used across your application.

To display the returned data, you will use pagination. Pagination is the process of separating content into discrete pages, and it can be used for handling data sets in React. In your application, you'll render the columns in the paginated table. The ***columns*** constant is used to define the table that is present on the webpage.

The ***return*** statement returns the paginated table where you defined the properties for the ***ReactTable***. The ***data*** property corresponds to the consumed data from the API endpoint and is assigned to the ***data*** of the table. The ***columns*** property corresponds to the rendered column object and is assigned to the ***columns*** of the table.


### Importing the HTTP client

Your application needs a way to communicate with and retrieve resources from RESTful web services to output the resources onto the paginated table. The [Axios](https://github.com/axios/axios) library will provide you with an HTTP client. This client is used to make HTTP requests to external resources. Axios is a promise-based HTTP client that can send asynchronous requests to REST endpoints. To learn more about the Axios library and its HTTP client, see the [Axios documentation](https://www.npmjs.com/package/axios).

The ***getArtistsInfo()*** function uses the Axios API to fetch data from your back end. This function is called when the ***ArtistTable*** is rendered to the page using the ***componentDidMount()*** React lifecycle method.

Update the ***ArtistTable.js*** file.

> To open the ArtistTable.js file in your IDE, select
> **File** > **Open** > guide-rest-client-reactjs/start/src/main/frontend/src/Components/ArtistTable.js, or click the following button

::openFile{path="/home/project/guide-rest-client-reactjs/start/src/main/frontend/src/Components/ArtistTable.js"}



```javascript
import React, { Component } from 'react';
import axios from 'axios';
import ReactTable from 'react-table-6';
import 'react-table-6/react-table.css';

class ArtistTable extends Component {
  state = {
    posts: [],
    isLoading: true,
    error: null,
  };

  getArtistsInfo() {
    axios('http://localhost:9080/artists')
      .then(response => {
        const artists = response.data;
        const posts = [];
        for (const artist of artists) {
          const { albums, ...rest } = artist;
          for (const album of albums) {
            posts.push({ ...rest, ...album });
          }
        };
        this.setState({
          posts,
          isLoading: false
        });
      })
      .catch(error => this.setState({ error, isLoading: false }));
  }

  componentDidMount() {
    this.getArtistsInfo();
  }
  render() {
    const { isLoading, posts } = this.state;
    const columns = [{
      Header: 'Artist Info',
      columns: [
        {
          Header: 'Artist ID',
          accessor: 'id'
        },
        {
          Header: 'Artist Name',
          accessor: 'name'
        },
        {
          Header: 'Genres',
          accessor: 'genres',
        }
      ]
    },
    {
      Header: 'Albums',
      columns: [
        {
          Header: 'Title',
          accessor: 'title',
        },
        {
          Header: 'Number of Tracks',
          accessor: 'ntracks',
        }
      ]
    }
  ]

  return (
    <div>
      <h2>Artist Web Service</h2>
      {!isLoading ? (
        <ReactTable
          data={posts}
          columns={columns}
          defaultPageSize={4}
          pageSizeOptions={[4, 5, 6]}
        />) : (
          <p>Loading .....</p>
        )}
    </div>
    );
  }
}

export default ArtistTable;
```




The ***axios*** HTTP call is used to read the artist JSON that contains the data from the sample JSON file in the ***resources*** directory. When a response is successful, the state of the system changes by assigning ***response.data*** to ***posts***. The ***convertData*** function manipulates the JSON data to allow it to be accessed by the ***ReactTable***. You will notice the ***object spread syntax*** that the ***convertData*** function uses, which is a relatively new sytnax made for simplicity. To learn more about it, see [Spread in object literals](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Spread_syntax#Spread_in_object_literals).

The ***this.setState*** function is used to update the state of your React component with the data that was fetched from the server. This update triggers a rerender of your React component, which updates the table with the artist data. For more information on how state in React works, see the React documentation on [state and lifecycle](https://reactjs.org/docs/faq-state.html).

Finally, run the following command to update the URL to access the ***artists.json*** in the ***ArtistTable.js*** file:
```bash
sed -i 's=http://localhost:9080/artists='"http://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')/artists"'=' /home/project/guide-rest-client-reactjs/start/src/main/frontend/src/Components/ArtistTable.js
```


::page{title="Building and packaging the front end"}

After you successfully build your components, you need to build the front end and package your application. The Maven ***process-resources*** goal generates the Node.js resources, creates the front-end production build, and copies and processes the resources into the destination directory. 

In a new command-line session, build the front end by running the following command in the ***start*** directory:

```bash
cd /home/project/guide-rest-client-reactjs/start
mvn process-resources
```

The build may take a few minutes to complete. You can rebuild the front end at any time with the Maven ***process-resources*** goal. Any local changes to your JavaScript and HTML are picked up when you build the front end.


Open your browser and view the front end of your application by going to the URL that the following command returns:
```bash
echo http://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')
```


::page{title="Testing the React client"}

New projects that are created with ***create-react-app*** comes with a test file called ***App.test.js***, which is included in the ***src/main/frontend/src*** directory. The ***App.test.js*** file is a simple JavaScript file that tests against the ***App.js*** component. There are no explicit test cases that are written for this application. The ***create-react-app*** configuration uses [Jest](https://jestjs.io/) as its test runner.
To learn more about Jest, go to their documentation on [Testing React apps](https://jestjs.io/docs/en/tutorial-react). 


Update the ***pom.xml*** file.

> To open the pom.xml file in your IDE, select
> **File** > **Open** > guide-rest-client-reactjs/start/pom.xml, or click the following button

::openFile{path="/home/project/guide-rest-client-reactjs/start/pom.xml"}



```xml
<?xml version='1.0' encoding='utf-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.microprofile.demo</groupId>
    <artifactId>guide-rest-client-reactjs</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <!-- Liberty configuration -->
        <liberty.var.default.http.port>9080</liberty.var.default.http.port>
        <liberty.var.default.https.port>9443</liberty.var.default.https.port>
    </properties>

    <dependencies>
        <!-- Provided dependencies -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>9.1.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.eclipse.microprofile</groupId>
            <artifactId>microprofile</artifactId>
            <version>5.0</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>

        <!-- For tests -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.8.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
            </plugin>
            <!-- Enable liberty-maven plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.5.1</version>            
            </plugin>
            <!-- Frontend resources -->
            <plugin>
                <groupId>com.github.eirslett</groupId>
                <artifactId>frontend-maven-plugin</artifactId>
                <version>1.10.0</version>
                <configuration>
                    <workingDirectory>src/main/frontend</workingDirectory>
                </configuration>
                <executions>
                    <execution>
                        <id>install node and npm</id>
                        <goals>
                            <goal>install-node-and-npm</goal>
                        </goals>
                        <configuration>
                            <nodeVersion>v16.16.0</nodeVersion>
                            <npmVersion>8.11.0</npmVersion>
                        </configuration>
                    </execution>
                    <execution>
                        <id>npm install</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <configuration>
                            <arguments>install</arguments>
                        </configuration>
                    </execution>
                    <execution>
                        <id>npm run build</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <configuration>
                            <arguments>run build</arguments>
                        </configuration>
                    </execution>
                    <execution>
                        <id>run tests</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <configuration>
                            <arguments>test a</arguments>
                            <environmentVariables>
                                <CI>true</CI>
                            </environmentVariables>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <!-- Copy frontend static files to target directory -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.2.0</version>
                <executions>
                 <execution>
                        <id>Copy frontend build to target</id>
                        <phase>process-resources</phase>
                        <goals>
                            <goal>copy-resources</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>
                                ${basedir}/src/main/webapp
                            </outputDirectory>
                            <resources>
                                <resource>
                                    <directory>
                                        ${basedir}/src/main/frontend/build
                                    </directory>
                                    <filtering>true</filtering>
                                </resource>
                            </resources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```



To run the default test, you can add the ***testing*** configuration to the ***frontend-maven-plugin***. Rerun the Maven ***process-resources*** goal to rebuild the front end and run the tests.

Although the React application in this guide is simple, when you build more complex React applications, testing becomes a crucial part of your development lifecycle. If you need to write application-oriented test cases, follow the official [React testing documentation](https://reactjs.org/docs/testing.html).

When you are done checking the application root, exit dev mode by pressing CTRL+C in the shell session where you ran the server, or by typing ***q*** and then pressing the ***enter/return*** key.

::page{title="Summary"}

### Nice Work!

Nice work! You just accessed a simple RESTful web service and consumed its resources by using ReactJS in Open Liberty.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-rest-client-reactjs*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-rest-client-reactjs
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Consuming%20a%20RESTful%20web%20service%20with%20ReactJS&guide-id=cloud-hosted-guide-rest-client-reactjs)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-rest-client-reactjs/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-rest-client-reactjs/pulls)



### Where to next?

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Consuming a RESTful web service](https://openliberty.io/guides/rest-client-java.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
