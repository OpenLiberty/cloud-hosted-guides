---
markdown-version: v1
title: instructions
branch: lab-5932-instruction
version-history-start-date: 2023-04-14T18:24:15Z
tool-type: theia
---
::page{title="Welcome to the Consuming a RESTful web service with Angular guide!"}

Explore how to access a simple RESTful web service and consume its resources with Angular in OpenLiberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

[Angular](https://angular.io) is a framework for creating interactive web applications. Angular applications are written in HTML, CSS, and [TypeScript](https://www.typescriptlang.org), a variant of JavaScript. Angular helps you create responsive and intuitive applications that download once and run as a single web page. Consuming REST services with your Angular application allows you to request only the data and operations that you need, minimizing loading times.

You will learn how to access a REST service and deserialize the returned JSON that contains a list of artists and their albums by using an Angular service and the Angular HTTP Client. You will then present this data using an Angular component.

The REST service that provides the artists and albums resource was written for you in advance and responds with the ***artists.json***.

The Angular application was created and configured for you in the ***frontend*** directory. It contains the default starter application. There are many files that make up an Angular application, but you only need to edit a few to consume the REST service and display its data.

Angular applications must be compiled before they can be used. The Angular compilation step was configured as part of the Maven build. You can use the ***start*** folder of this guide as a template for getting started with your own applications built on Angular and Open Liberty.



You will implement an Angular client that consumes this JSON and displays its contents.

To learn more about REST services and how you can write them, see
[Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html).


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-rest-client-angular.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-rest-client-angular.git
cd guide-rest-client-angular
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.

To try out the application, first go to the ***finish*** directory and run the following Maven goal to build the application and deploy it to Open Liberty:

```bash
cd finish
mvn liberty:run
```

After you see the following message, your application server is ready:

```
The defaultServer server is ready to run a smarter planet.
```


Click the following button to visit the web application ***/app*** root endpoint:
::startApplication{port="9080" display="external" name="Visit application" route="/app"}

 You will see the following output:



```
foo wrote 2 albums:
    Album titled *album_one* by *foo* contains *12* tracks
    Album tilted *album_two* by *foo* contains *15* tracks
bar wrote 1 albums:
    Album titled *foo walks into a bar* by *bar* contains *12* tracks
dj wrote 0 albums:
```

After you are finished checking out the application, stop the Open Liberty server by pressing `Ctrl+C` in the command-line session where you ran the server. Alternatively, you can run the ***liberty:stop*** goal from the ***finish*** directory in another shell session:

```bash
mvn liberty:stop
```


::page{title="Starting the service"}

Before you begin the implementation, start the provided REST service so that the artist JSON is available to you.

Navigate to the ***start*** directory to begin.

```bash
cd /home/project/guide-rest-client-angular/start
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


You can find your artist JSON by running the following command at a terminal:
```bash
curl -s http://localhost:9080/artists | jq
```


::page{title="Project configuration"}

The front end of your application uses Node.js to execute your Angular code. The Maven project is configured for you to install Node.js and produce the production files, which are copied to the web content of your application.

Node.js is server-side JavaScript runtime that is used for developing networking applications. Its convenient package manager, [npm](https://www.npmjs.com/), is used to execute the Angular scripts found in the ***package.json*** file. To learn more about Node.js, see the official [Node.js documentation](https://nodejs.org/en/docs/).

The ***frontend-maven-plugin*** is used to ***install*** the dependencies listed in your ***package.json*** file from the npm registry into a folder called ***node_modules***. The ***node_modules*** folder is found in your ***working*** directory. Then, the configuration ***produces*** the production files to the ***src/main/frontend/src/app*** directory. 

The ***src/main/frontend/src/angular.json*** file is defined so that the production build is copied into the web content of your application.



::page{title="Creating the root Angular module"}

Your application needs a way to communicate with and retrieve resources from RESTful web services. In this case, the provided Angular application needs to communicate with the artists service to retrieve the artists JSON. While there are various ways to perform this task, Angular contains a built-in ***HttpClientModule*** that you can use.

Angular applications consist of modules, which are groups of classes that perform specific functions. The Angular framework provides its own modules for applications to use. One of these modules, the HTTP Client module, includes convenience classes that make it easier and quicker for you to consume a RESTful API from your application.

You will create the module that organizes your application, which is called the root module. The root module includes the Angular HTTP Client module.

Create the ***app.module.ts*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-client-angular/start/src/main/frontend/src/app/app.module.ts
```


> Then, to open the app.module.ts file in your IDE, select
> **File** > **Open** > guide-rest-client-angular/start/src/main/frontend/src/app/app.module.ts, or click the following button

::openFile{path="/home/project/guide-rest-client-angular/start/src/main/frontend/src/app/app.module.ts"}



```
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { AppComponent } from './app.component';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


The ***HttpClientModule*** imports the class into the file. By using the ***@NgModule*** tag, you can declare a module and organize  your dependencies within the Angular framework. The ***imports*** array is a declaration array that imports the ***HttpClientModule*** so that you can use the HTTP Client module in your application.


::page{title="Creating the Angular service to fetch data"}

You need to create the component that is used in the application to acquire and display data from the REST API. The component file contains two classes: the service, which handles data access, and the component itself, which handles the presentation of the data.

Services are classes in Angular that are designed to share their functionality across entire applications. A good service performs only one function, and it performs this function well. In this case, the ***ArtistsService*** class requests artists data from the REST service.

Create the ***app.component.ts*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-client-angular/start/src/main/frontend/src/app/app.component.ts
```


> Then, to open the app.component.ts file in your IDE, select
> **File** > **Open** > guide-rest-client-angular/start/src/main/frontend/src/app/app.component.ts, or click the following button

::openFile{path="/home/project/guide-rest-client-angular/start/src/main/frontend/src/app/app.component.ts"}



```
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable()
export class ArtistsService {
  constructor(private http: HttpClient) { }

  private static ARTISTS_URL = '/artists';

  async fetchArtists() {
    try {
      const data: any = await this.http.get(ArtistsService.ARTISTS_URL).toPromise();
      return data;
    } catch (error) {
      console.error('Error occurred: ' + error);
    }
  }
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  artists: any[] = [];

  constructor(private artistsService: ArtistsService) { }

  ngOnInit() {
    this.artistsService.fetchArtists().then(data => {
      this.artists = data;
    });
  }
}
```



The file imports the ***HttpClient*** class and the ***Injectable*** decorator.

The ***ArtistsService*** class is defined. While it shares the file of the component class ***AppComponent***, it can also be defined in its own file. The class is annotated by ***@Injectable*** so instances of it can be provided to other classes anywhere in the application.

The class injects an instance of the ***HttpClient*** class, which it uses to request data from the REST API. It contains the ***ARTISTS_URL*** constant, which points to the API endpoint it requests data from. The URL does not contain a host name because the artists API endpoint is accessible from the same host as the Angular application. You can send requests to external APIs by specifying the full URL. Finally, it implements a ***fetchArtists()*** method that makes the request and returns the result.

To obtain the data for display on the page, the ***fetchArtists()*** method tries to use the injected ***http*** instance to perform a ***GET*** HTTP request to the ***ARTISTS_URL*** constant. If successful, it returns the result. If an error occurs, it prints the error message to the console.

The ***fetchArtists()*** method uses a feature of JavaScript called ***async***, ***await*** to make requests and receive responses without preventing the application from working while it waits. For the result of the ***HttpClient.get()*** method to be compatible with this feature, it must be converted to a Promise by invoking its ***toPromise()*** method. APromise is how JavaScript represents the state of an asynchronous operation. If you want to learn more, check out [promisejs.org](https://promisejs.org) for an introduction.


::page{title="Defining the component to consume the service"}

Components are the basic building blocks of Angular application user interfaces. Components are made up of a TypeScript class annotated with the ***@Component*** annotation and the HTML template file (specified by ***templateUrl***) and CSS style files (specified by ***styleUrls***.)

Update the ***AppComponent*** class to use the artists service to fetch the artists data and save it so the component can display it.

Update the ***app.component.ts*** file.

> To open the app.component.ts file in your IDE, select
> **File** > **Open** > guide-rest-client-angular/start/src/main/frontend/src/app/app.component.ts, or click the following button

::openFile{path="/home/project/guide-rest-client-angular/start/src/main/frontend/src/app/app.component.ts"}



```
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable()
export class ArtistsService {
  constructor(private http: HttpClient) { }

  private static ARTISTS_URL = '/artists';

  async fetchArtists() {
    try {
      const data: any = await this.http.get(ArtistsService.ARTISTS_URL).toPromise();
      return data;
    } catch (error) {
      console.error('Error occurred: ' + error);
    }
  }
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  providers: [ ArtistsService ],
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  artists: any[] = [];

  constructor(private artistsService: ArtistsService) { }

  ngOnInit() {
    this.artistsService.fetchArtists().then(data => {
      this.artists = data;
    });
  }
}
```



Replace the entire ***AppComponent*** class along with the ***@Component*** annotation. Add ***OnInit*** to the list of imported classes at the top.

The ***providers*** property on the ***@Component*** annotation indicates that this component provides the ***ArtistsService*** to other classes in the application.

***AppComponent*** implements ***OnInit***, which is a special interface called a lifecycle hook. When Angular displays, updates, or removes a component, it calls a specific function, the lifecycle hook, on the component so the component can run code in response to this event. This component responds to the ***OnInit*** event via the ***ngOnInit*** method, which fetches and populates the component's template with data when it is initialized for display. The file imports the ***OnInit*** interface from the ***@angular/core*** package.

***artists*** is a class member of type ***any[]*** that starts out as an empty array. It holds the artists retrieved from the service so the template can display them.

An instance of the ***ArtistsService*** class is injected into the constructor and is accessible by any function that is defined in the class. The ***ngOnInit*** function uses the ***artistsService*** instance to request the artists data. The ***fetchArtists()*** method is an ***async*** function so it returns a Promise. To retrieve the data from the request, ***ngOnInit*** calls the ***then()*** method on the Promise which takes in the data and stores it to the ***artists*** class member.


::page{title="Creating the Angular component template"}

Now that you have a service to fetch the data and a component to store it in, you will create a template to specify how the data will be displayed on the page. When you visit the page in the browser, the component populates the template to display the artists data with formatting.

Create the ***app.component.html*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-client-angular/start/src/main/frontend/src/app/app.component.html
```


> Then, to open the app.component.html file in your IDE, select
> **File** > **Open** > guide-rest-client-angular/start/src/main/frontend/src/app/app.component.html, or click the following button

::openFile{path="/home/project/guide-rest-client-angular/start/src/main/frontend/src/app/app.component.html"}



```html
<div *ngFor="let artist of artists">
  <p>{{ artist.name }} wrote {{ artist.albums.length }} albums: </p>
  <div *ngFor="let album of artist.albums">
    <p style="text-indent: 20px">
      Album titled <b>{{ album.title }}</b> by
                   <b>{{ album.artist }}</b> contains
                   <b>{{ album.ntracks }}</b> tracks
    </p>
  </div>
</div>
```



The template contains a ***div*** element that is enumerated by using the ***ngFor*** directive. The ***artist*** variable is bound to the ***artists*** member of the component. The ***div*** element itself and all elements contained within it are repeated for each artist, and the ***{{ artist.name }}*** and ***{{ artist.albums.length }}*** placeholders are populated with the information from each artist. The same strategy is used to display each ***album*** by each artist.


::page{title="Building the front end"}

The Open Liberty server is already started, and the REST service is running. In a new command-line session, build the front end by running the following command in the ***start*** directory:

```bash
cd /home/project/guide-rest-client-angular/start
mvn generate-resources
```

The build might take a few minutes to complete. You can rebuild the front end at any time with the ***generate-resources*** Maven goal. Any local changes to your TypeScript or HTML are picked up when you build the front end.


Click the following button to visit the web application ***/app*** root endpoint:
::startApplication{port="9080" display="external" name="Visit application" route="/app"}

You will see the following output:

```
foo wrote 2 albums:
    Album titled *album_one* by *foo* contains *12* tracks
    Album tilted *album_two* by *foo* contains *15* tracks
bar wrote 1 albums:
    Album titled *foo walks into a bar* by *bar* contains *12* tracks
dj wrote 0 albums:
```

If you use the ***curl*** command to access the web application root URL, you see only the application root page in HTML. The Angular framework uses JavaScript to render the HTML to display the application data. A web browser runs JavaScript, and the ***curl*** command doesn't.


::page{title="Testing the Angular client"}

No explicit code directly uses the consumed artist JSON, so you don't need to write any test cases.


Whenever you change and build your Angular implementation, the changes are automatically reflected at the URL for the launched application.

When you are done checking the application root, exit development mode by pressing `Ctrl+C` in the command-line session where you ran the server, or by typing ***q*** and then pressing the ***enter/return*** key.

Although the Angular application that this guide shows you how to build is simple, when you build more complex Angular applications, testing becomes a crucial part of your development lifecycle. If you need to write test cases, follow the official unit testing and end-to-end testing documentation on the [official Angular page](https://angular.io/guide/testing).

::page{title="Summary"}

### Nice Work!

You just accessed a simple RESTful web service and consumed its resources by using Angular in Open Liberty.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-rest-client-angular*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-rest-client-angular
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Consuming%20a%20RESTful%20web%20service%20with%20Angular&guide-id=cloud-hosted-guide-rest-client-angular)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-rest-client-angular/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-rest-client-angular/pulls)



### Where to next?

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Consuming a RESTful web service](https://openliberty.io/guides/rest-client-java.html)
* [Consuming a RESTful web service with AngularJS](https://openliberty.io/guides/rest-client-angularjs.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
