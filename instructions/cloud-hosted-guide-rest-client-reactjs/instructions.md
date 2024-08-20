---
markdown-version: v1
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

The React application in this guide is provided and configured for you in the ***src/main/frontend*** directory. The application uses [Next.js](https://nextjs.org/), a [React-powered framework](https://react.dev/learn/start-a-new-react-project), to set up the modern React application. The ***Next.js*** framework provides a powerful environment for learning and building React applications, with features like server-side rendering, static site generation, and easy API routes. It is the best way to start building a highly performant React application.


The REST service that provides the resources was written for you in advance in the back end of the application, and it responds with the ***artists.json*** file  in the ***src/resources*** directory. You will implement a ReactJS client as the front end of your application, which consumes this JSON file and displays its contents on a single web page. 

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

The ***finish*** directory in the root of this guide contains the finished application. The React front end is already pre-built for you and the static files from the production build can be found in the ***src/main/webapp/_next/static*** directory.


In this IBM cloud environment, you need to update the URL to access the ***artists.json***. Run the following commands to go to the ***finish*** directory and update the files where the URL has been specified:
```bash
cd finish
sed -i 's=http://localhost:9080/artists='"https://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')/artists"'=' /home/project/guide-rest-client-reactjs/finish/src/main/webapp/_next/static/chunks/app/page-37714928d1f43656.js
sed -i 's=http://localhost:9080/artists='"https://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')/artists"'=' /home/project/guide-rest-client-reactjs/finish/src/main/frontend/src/app/ArtistTable.jsx
```

To try out the application, run the following Maven goal to build the application and deploy it to Open Liberty:
```bash
mvn liberty:run
```

After you see the following message, your application Liberty instance is ready:

```
The defaultServer server is ready to run a smarter planet.
```


When the Liberty instance is running, click the following button to check out the application:

::startApplication{port="9080" display="external" name="Visit application" route="/"}

See the following output:

![React Paginated Table](https://raw.githubusercontent.com/OpenLiberty/guide-rest-client-reactjs/prod/assets/react-table.png)


After you are finished checking out the application, stop the Liberty instance by pressing `Ctrl+C` in the command-line session where you ran Liberty. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```


::page{title="Starting the service"}

Before you begin the implementation, start the provided REST service so that the artist JSON is available to you.

Navigate to the ***start*** directory to begin.
```bash
cd /home/project/guide-rest-client-reactjs/start
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


After the Liberty instance is started, run the following curl command to view your artist JSON.
```bash
curl -s http://localhost:9080/artists | jq
```

All the dependencies for the React front end are listed in the  ***src/main/frontend/src/package.json*** file and are installed before the build process by the ***frontend-maven-plugin***. Also, ***CSS*** stylesheets files are available in the ***src/main/frontend/src/styles*** directory.


::page{title="Project configuration"}

The front end of your application uses Node.js to build your React code. The Maven project is configured for you to install Node.js and produce the production files, which are copied to the web content of your application.

Node.js is a server-side JavaScript runtime that is used for developing networking applications. Its convenient package manager, [npm](https://www.npmjs.com/), is used to run the React build scripts that are found in the ***package.json*** file. To learn more about Node.js, see the official [Node.js documentation](https://nodejs.org/en/docs/).


Take a look at the **pom.xml** file.
> From the menu of the IDE, select **File** > **Open** > guide-rest-client-reactjs/start/pom.xml, or click the following button:

::openFile{path="/home/project/guide-rest-client-reactjs/start/pom.xml"}

The ***frontend-maven-plugin*** is used to ***install*** the dependencies that are listed in your ***package.json*** file from the npm registry into a folder called ***node_modules***. The ***node_modules*** folder can be found in your ***working*** directory. Then, the configuration ***produces*** the production files to the ***src/main/frontend/build*** directory. 

The ***maven-resources-plugin*** copies the ***static*** content from the ***build*** directory to the ***web content*** of the application.


::page{title="Creating the default page"}

Create the entry point of your React application. The latest version of ***Next.js*** recommends you use the https://nextjs.org/docs/app/building-your-application/routing/defining-routes[App Router], which centralizes routing logic under the ***app*** directory. 
 
To construct the home page of the web application, create a ***page.jsx*** file.

Create the ***page.jsx*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-client-reactjs/start/src/main/frontend/src/app/page.jsx
```


> Then, to open the page.jsx file in your IDE, select
> **File** > **Open** > guide-rest-client-reactjs/start/src/main/frontend/src/app/page.jsx, or click the following button

::openFile{path="/home/project/guide-rest-client-reactjs/start/src/main/frontend/src/app/page.jsx"}



```
import "../../styles/index.css";
import ArtistTable from "./ArtistTable";
import React from 'react';

export default function Home() {
  return (
    <ArtistTable></ArtistTable>
  );
}
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***page.jsx*** file is a container for all other components. When the ***Home*** React component  is rendered, the ***ArtistTable*** components content are displayed.

To render the pages correctly, add a ***layout.jsx*** file that defines the ***RootLayout*** containing the UI that are shared across all routes.

Create the ***layout.jsx*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-client-reactjs/start/src/main/frontend/src/app/layout.jsx
```


> Then, to open the layout.jsx file in your IDE, select
> **File** > **Open** > guide-rest-client-reactjs/start/src/main/frontend/src/app/layout.jsx, or click the following button

::openFile{path="/home/project/guide-rest-client-reactjs/start/src/main/frontend/src/app/layout.jsx"}



```
export const metadata = {
  title: 'Next.js',
  description: 'Generated by Next.js',
}

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
```



For more detailed information, see the ***Next.js*** documentation on [Pages](https://nextjs.org/docs/app/building-your-application/routing/pages) and [Layouts](https://nextjs.org/docs/app/building-your-application/routing/layouts-and-templates).


::page{title="Creating the React component"}

A React web application is a collection of components, and each component has a specific function. You will create a component that the application uses to acquire and display data from the REST API. 

Create the ***ArtistTable*** function that fetches data from your back-end and renders it in a table. 

Create the ***ArtistTable.jsx*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-client-reactjs/start/src/main/frontend/src/app/ArtistTable.jsx
```


> Then, to open the ArtistTable.jsx file in your IDE, select
> **File** > **Open** > guide-rest-client-reactjs/start/src/main/frontend/src/app/ArtistTable.jsx, or click the following button

::openFile{path="/home/project/guide-rest-client-reactjs/start/src/main/frontend/src/app/ArtistTable.jsx"}



```
"use client";
import React, { useEffect, useMemo, useState } from 'react';
import { useReactTable, getCoreRowModel, getPaginationRowModel, getSortedRowModel, flexRender} from '@tanstack/react-table'; 
import '../../styles/table.css'

function ArtistTable() {

  const [posts, setPosts] = useState([]);
  const [sorting, setSorting] = useState([]);
  const [pagination, setPagination] = useState({pageIndex: 0, pageSize: 4});


  const data = useMemo(() => [...posts], [posts]);

  const columns = useMemo(() => [{
    header: 'Artist Info',
    columns: [
      {
        accessorKey: 'id',
        header: 'Artist ID'
      },
      {
        accessorKey: 'name',
        header: 'Artist Name'
      },
      {
        accessorKey: 'genres',
        header: 'Genres'
      }
    ]
  },
  {
    header: 'Albums',
    columns: [
      {
        accessorKey: 'ntracks',
        header: 'Number of Tracks'
      },
      {
        accessorKey: 'title',
        header: 'Title'
      }
    ]
  }
  ], []
  );

  const tableInstance = useReactTable({ 
          columns, 
          data,
          getCoreRowModel: getCoreRowModel(), 
          getPaginationRowModel: getPaginationRowModel(), 
          getSortedRowModel: getSortedRowModel(), 
          state:{
            sorting: sorting,
            pagination: pagination,
          },
          onSortingChange: setSorting,
          onPaginationChange: setPagination,
          }); 

  const {
    getHeaderGroups, 
    getRowModel,
    getState,
    setPageIndex,
    setPageSize,
    getCanPreviousPage,
    getCanNextPage,
    previousPage,
    nextPage,
    getPageCount,
  } = tableInstance;

  const {pageIndex, pageSize} = getState().pagination;


  return (
    <>
      <h2>Artist Web Service</h2>
      {/* tag::table[] */}
      <table>
        <thead>
          {getHeaderGroups().map(headerGroup => (
            <tr key={headerGroup.id}>
              {headerGroup.headers.map(header => (
                <th key={header.id} colSpan={header.colSpan} onClick={header.column.getToggleSortingHandler()}>
                  {header.isPlaceholder ? null :(
                    <div>
                      {flexRender(header.column.columnDef.header, header.getContext())}
                      {
                        {
                          asc: " 🔼",
                          desc: " 🔽",
                        }[header.column.getIsSorted() ?? null]
                      }
                    </div>
                  )}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody>
          {getRowModel().rows.map(row => (
            <tr key={row.id}>
              {row.getVisibleCells().map(cell => (
                <td key={cell.id}>
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
      {/* end::table[] */}
      <div className="pagination">
        <button onClick={() => previousPage()} disabled={!getCanPreviousPage()}>
          {'Previous'}
        </button>{' '}
        <div className="page-info">
          <span>
            Page{' '}
            <strong>
              {pageIndex + 1} of {getPageCount()}
            </strong>{' '}
          </span>
          <span>
            | Go to page:{' '}
            <input
              type="number"
              defaultValue={pageIndex + 1}
              onChange={e => {
                const page = e.target.value ? Number(e.target.value) - 1 : 0
                setPageIndex(page);
              }}
              style={{ width: '100px' }}
            />
          </span>{' '}
          <select
            value={pageSize}
            onChange={e => {
              setPageSize(Number(e.target.value))
            }}
          >
            {[4, 5, 6, 9].map(pageSize => (
              <option key={pageSize} value={pageSize}>
                Show {pageSize}
              </option>
            ))}
          </select>
        </div>
        <button onClick={() => nextPage()} disabled={!getCanNextPage()}>
          {'Next'}
        </button>{' '}
      </div>
    </>
  );
}

export default ArtistTable
```



At the beginning of the file, the ***use client*** directive indicates the ***ArtistTable*** component is rendered on the client side.

The ***React*** library imports the ***react*** package for you to create the ***ArtistTable*** function. This function must have the ***export*** declaration because it is being exported to the ***page.jsx*** module. The ***posts*** object is initialized using a React Hook that lets you add a state to represent the state of the posts that appear on the paginated table.

To display the returned data, you will use pagination. Pagination is the process of separating content into discrete pages, and you can use it for handling data sets in React. In your application, you'll render the columns in the paginated table. The ***columns*** constant defines the table that is present on the web page.

The ***useReactTable*** hook creates a table instance. The hook takes in the ***columns*** and  ***posts*** as parameters. The ***getCoreRowModel*** function is included for the generation of the core row model of the table, which serves as the foundational row model upon pagination and sorting build. The ***getPaginationRowModel*** function applies pagination to the core row model, returning a row model that includes only the rows that should be displayed on the current page based on the pagination state. In addition, the ***getSortedRowModel*** function sorts the paginated table by the column headers then applies the changes to the row model. The paginated table instance is assigned to the ***table*** constant, which renders the paginated table on the web page.


### Importing the HTTP client

Your application needs a way to communicate with and retrieve resources from RESTful web services to output the resources onto the paginated table. The [Axios](https://github.com/axios/axios) library will provide you with an HTTP client. This client is used to make HTTP requests to external resources. Axios is a promise-based HTTP client that can send asynchronous requests to REST endpoints. To learn more about the Axios library and its HTTP client, see the [Axios documentation](https://www.npmjs.com/package/axios).

The ***GetArtistsInfo()*** function uses the Axios API to fetch data from your back end. This function is called when the ***ArtistTable*** is rendered to the page using the ***useEffect()*** React lifecycle method.

Update the ***ArtistTable.jsx*** file.

> To open the ArtistTable.jsx file in your IDE, select
> **File** > **Open** > guide-rest-client-reactjs/start/src/main/frontend/src/app/ArtistTable.jsx, or click the following button

::openFile{path="/home/project/guide-rest-client-reactjs/start/src/main/frontend/src/app/ArtistTable.jsx"}



```
"use client";
import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { useReactTable, getCoreRowModel, getPaginationRowModel, getSortedRowModel, flexRender} from '@tanstack/react-table'; 
import '../../styles/table.css'

function ArtistTable() {

  const [posts, setPosts] = useState([]);
  const [sorting, setSorting] = useState([]);
  const [pagination, setPagination] = useState({pageIndex: 0, pageSize: 4});

  const GetArtistsInfo = async () => {
    try {
      const response = await axios.get('http://localhost:9080/artists');
      const artists = response.data;
      const processedData = [];
      for (const artist of artists) {
        const { albums, ...rest } = artist;
        for (const album of albums) {
          processedData.push({ ...rest, ...album });
        }
      };
      setPosts(processedData);
    } catch (error) {
      console.log(error);
    }
  };

  const data = useMemo(() => [...posts], [posts]);

  const columns = useMemo(() => [{
    header: 'Artist Info',
    columns: [
      {
        accessorKey: 'id',
        header: 'Artist ID'
      },
      {
        accessorKey: 'name',
        header: 'Artist Name'
      },
      {
        accessorKey: 'genres',
        header: 'Genres'
      }
    ]
  },
  {
    header: 'Albums',
    columns: [
      {
        accessorKey: 'ntracks',
        header: 'Number of Tracks'
      },
      {
        accessorKey: 'title',
        header: 'Title'
      }
    ]
  }
  ], []
  );

  const tableInstance = useReactTable({ 
          columns, 
          data,
          getCoreRowModel: getCoreRowModel(), 
          getPaginationRowModel: getPaginationRowModel(), 
          getSortedRowModel: getSortedRowModel(), 
          state:{
            sorting: sorting,
            pagination: pagination,
          },
          onSortingChange: setSorting,
          onPaginationChange: setPagination,
          }); 

  const {
    getHeaderGroups, 
    getRowModel,
    getState,
    setPageIndex,
    setPageSize,
    getCanPreviousPage,
    getCanNextPage,
    previousPage,
    nextPage,
    getPageCount,
  } = tableInstance;

  const {pageIndex, pageSize} = getState().pagination;

  useEffect(() => {
    GetArtistsInfo();
  }, []);

  return (
    <>
      <h2>Artist Web Service</h2>
      {/* tag::table[] */}
      <table>
        <thead>
          {getHeaderGroups().map(headerGroup => (
            <tr key={headerGroup.id}>
              {headerGroup.headers.map(header => (
                <th key={header.id} colSpan={header.colSpan} onClick={header.column.getToggleSortingHandler()}>
                  {header.isPlaceholder ? null :(
                    <div>
                      {flexRender(header.column.columnDef.header, header.getContext())}
                      {
                        {
                          asc: " 🔼",
                          desc: " 🔽",
                        }[header.column.getIsSorted() ?? null]
                      }
                    </div>
                  )}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody>
          {getRowModel().rows.map(row => (
            <tr key={row.id}>
              {row.getVisibleCells().map(cell => (
                <td key={cell.id}>
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
      {/* end::table[] */}
      <div className="pagination">
        <button onClick={() => previousPage()} disabled={!getCanPreviousPage()}>
          {'Previous'}
        </button>{' '}
        <div className="page-info">
          <span>
            Page{' '}
            <strong>
              {pageIndex + 1} of {getPageCount()}
            </strong>{' '}
          </span>
          <span>
            | Go to page:{' '}
            <input
              type="number"
              defaultValue={pageIndex + 1}
              onChange={e => {
                const page = e.target.value ? Number(e.target.value) - 1 : 0
                setPageIndex(page);
              }}
              style={{ width: '100px' }}
            />
          </span>{' '}
          <select
            value={pageSize}
            onChange={e => {
              setPageSize(Number(e.target.value))
            }}
          >
            {[4, 5, 6, 9].map(pageSize => (
              <option key={pageSize} value={pageSize}>
                Show {pageSize}
              </option>
            ))}
          </select>
        </div>
        <button onClick={() => nextPage()} disabled={!getCanNextPage()}>
          {'Next'}
        </button>{' '}
      </div>
    </>
  );
}

export default ArtistTable
```



Add the ***axios*** library and the ***GetArtistsInfo()*** function.

The ***axios*** HTTP call is used to read the artist JSON that contains the data from the sample JSON file in the ***resources*** directory. When a response is successful, the state of the system changes by assigning ***response.data*** to ***posts***. The ***artists*** and their ***albums*** JSON data are manipulated to allow them to be accessed by the ***ReactTable***. The ***...rest*** or ***...album*** object spread syntax is designed for simplicity. To learn more about it, see [Spread in object literals](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Spread_syntax#Spread_in_object_literals).

Finally, run the following command to update the URL to access the ***artists.json*** in the ***ArtistTable.jsx*** file:
```bash
sed -i 's=http://localhost:9080/artists='"https://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')/artists"'=' /home/project/guide-rest-client-reactjs/start/src/main/frontend/src/app/ArtistTable.jsx
```


::page{title="Building and packaging the front-end"}

After you successfully build your components, you need to build the front end and package your application. The Maven ***process-resources*** goal generates the Node.js resources, creates the front-end production build, and copies and processes the resources into the destination directory. 

In a new command-line session, build the front end by running the following command in the ***start*** directory:

```bash
cd /home/project/guide-rest-client-reactjs/start
mvn process-resources
```

The build may take a few minutes to complete. You can rebuild the front end at any time with the Maven ***process-resources*** goal. Any local changes to your JavaScript and HTML are picked up when you build the front-end.


Click the following button to view the front end of your application:

::startApplication{port="9080" display="external" name="Visit application" route="/"}


::page{title="Testing the React client"}

***Next.js*** supports various testing tools. This guide uses ***Vitest*** for unit testing the React components, with the test file ***App.test.jsx*** located in ***src/main/frontend/__tests__/*** directory. The ***App.test.jsx*** file is a simple JavaScript file that tests against the ***page.jsx*** component. No explicit test cases are written for this application. To learn more about ***Vitest***, see https://nextjs.org/docs/app/building-your-application/testing/vitest[Setting up Vitest with Next.js].


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
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <!-- Liberty configuration -->
        <liberty.var.http.port>9080</liberty.var.http.port>
        <liberty.var.https.port>9443</liberty.var.https.port>
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
            <version>6.1</version>
            <type>pom</type>
            <scope>provided</scope>
        </dependency>

        <!-- For tests -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.3</version>
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
            </plugin>
            <!-- Enable liberty-maven plugin -->
            <plugin>
                <groupId>io.openliberty.tools</groupId>
                <artifactId>liberty-maven-plugin</artifactId>
                <version>3.10.3</version>            
            </plugin>
            <!-- Frontend resources -->
            <plugin>
                <groupId>com.github.eirslett</groupId>
                <artifactId>frontend-maven-plugin</artifactId>
                <version>1.15.0</version>
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
                            <nodeVersion>v20.14.0</nodeVersion>
                            <npmVersion>10.7.0</npmVersion>
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
                <version>3.3.1</version>
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
                                        ${basedir}/src/main/frontend/out
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

```bash
cd /home/project/guide-rest-client-reactjs/start
mvn process-resources
```

If the test passes, you see a similar output to the following example:

```
[INFO]  ✓ __tests__/App.test.jsx  (1 test) 96ms
[INFO] 
[INFO]  Test Files  1 passed (1)
[INFO]       Tests  1 passed (1)
[INFO]    Start at  10:43:25
[INFO]    Duration  3.73s (transform 264ms, setup 0ms, collect 343ms, tests 96ms, environment 408ms, prepare 1.16s)
```

Although the React application in this guide is simple, when you build more complex React applications, testing becomes a crucial part of your development lifecycle. If you need to write application-oriented test cases, follow the official [React testing documentation](https://reactjs.org/docs/testing.html).

When you are done checking the application root, exit dev mode by pressing `Ctrl+C` in the shell session where you ran the Liberty.

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

Log out of the cloud-hosted guides by selecting **Account** :fa-user: > **Logout** from the Skills Network left-sided menu.
