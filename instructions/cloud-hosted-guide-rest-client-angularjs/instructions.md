HELLLOO
---
markdown-version: v1
title: instructions
branch: lab-436-instruction
version-history-start-date: 2021-12-03 21:54:52 UTC
tool-type: theia
---
::page{title="Welcome to the Consuming a RESTful web service with AngularJS guide!"}

Explore how to access a simple RESTful web service and consume its resources with AngularJS in Open Liberty.

In this guide, you will use a pre-configured environment that runs in containers on the cloud and includes everything that you need to complete the guide.

This panel contains the step-by-step guide instructions. You can customize these instructions by using the toolbar at the top of this panel. Move between steps by using either the arrows or the buttons at the bottom of this panel.

The other panel displays the IDE that you will use to create files, edit the code, and run commands. This IDE is based on Visual Studio Code. It includes pre-installed tools and a built-in terminal.




::page{title="What you'll learn"}

You will learn how to access a REST service and deserialize the returned JSON that contains a list of artists and their albums by using the high-level ***$resource*** service of AngularJS.

The REST service that provides the artists and albums resource was written for you in advance and responds with the ***artists.json***.



You will implement an AngularJS client that consumes this JSON and displays its contents at a URL.


To learn more about REST services and how you can write them, see
[Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html).


::page{title="Getting started"}

To open a new command-line session,
select **Terminal** > **New Terminal** from the menu of the IDE.

Run the following command to navigate to the **/home/project** directory:

```bash
cd /home/project
```

The fastest way to work through this guide is to clone the [Git repository](https://github.com/openliberty/guide-rest-client-angularjs.git) and use the projects that are provided inside:

```bash
git clone https://github.com/openliberty/guide-rest-client-angularjs.git
cd guide-rest-client-angularjs
```


The ***start*** directory contains the starting project that you will build upon.

The ***finish*** directory contains the finished project that you will build.

### Try what you'll build

The ***finish*** directory in the root of this guide contains the finished application. Give it a try before you proceed.


In this IBM cloud environment, you need to update the URL to access the ***artists.json*** in the ***consume-rest.js*** file. Run the following commands to go to the ***finish*** directory and update the ***consume-rest.js*** file:
```bash
cd finish
sed -i 's=http://localhost:9080/artists='"http://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')/artists"'=' src/main/webapp/js/consume-rest.js
```

To try out the application, run the following Maven goal to build the application and deploy it to Open Liberty:
```bash
mvn liberty:run
```

After you see the following message, your application server is ready:

```
The defaultServer server is ready to run a smarter planet.
```


When the server is running, select **Terminal** > **New Terminal** from the menu of the IDE to open another command-line session.
Open your browser and check out the application by going to the URL that the following command returns:
```bash
echo http://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')
```

See the following output:

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
cd /home/project/guide-rest-client-angularjs/start
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

Any local changes to your JavaScript and HTML are picked up automatically, so you don't need to restart the server.


::page{title="Creating the AngularJS controller"}

Begin by registering your application module. Every application must contain at least one module, the application module, which will be bootstrapped to launch the application.


Create the ***consume-rest*** file.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-client-angularjs/start/src/main/webapp/js/consume-rest.js
```


> Then, to open the consume-rest.js file in your IDE, select
> **File** > **Open** > guide-rest-client-angularjs/start/src/main/webapp/js/consume-rest.js, or click the following button

::openFile{path="/home/project/guide-rest-client-angularjs/start/src/main/webapp/js/consume-rest.js"}



```javascript
var app = angular.module('consumeRestApp', ['ngResource']);

app.factory("artists", function($resource) {
    return $resource("http://localhost:9080/artists");
});

app.controller("ArtistsCtrl", function($scope, artists) {
    artists.query(function(data) {
        $scope.artists = data;
    }, function(err) {
        console.error("Error occured: ", err);
    });
});
```


Click the :fa-copy: **copy** button to copy the code and press `Ctrl+V` or `Command+V` in the IDE to add the code to the file.


Run the following command to update the URL to access the ***artists.json*** in the ***consume-rest.js*** file:
```bash
sed -i 's=http://localhost:9080/artists='"http://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')/artists"'=' /home/project/guide-rest-client-angularjs/start/src/main/webapp/js/consume-rest.js
```

The application module is defined by ***consumeRestApp***.

Your application will need some way of communicating with RESTful web services in order to retrieve their resources. In the case of this guide, your application will need to communicate with the artists service to retrieve the artists JSON. While there exists a variety of ways of doing this, you can use the fairly straightforward AngularJS ***$resource*** service.

The ***ngResource*** module is registered as it is appended after ***consumeRestApp***. By registering another module, you are performing a dependency injection, exposing all functionalities of that module to your main application module.

Next, the ***Artists*** AngularJS service is defined by using the Factory recipe. The Factory recipe constructs a new service instance with the return value of a passed in function. In this case, the ***$resource*** module that you imported earlier is the passed in function. Target the artist JSON URL in the ***$resource()*** call.

The ***controller*** controls the flow of data in your application.Each controller is instantiated with its own isolated scope, accessible through the ***$scope*** parameter. All data that is bound to this parameter is available in the view to which the controller is attached.

You can now access the ***artists*** property from the template at the point in the Document Object Model (DOM) where the controller is registered.


::page{title="Creating the AngularJS template"}

You will create the starting point of your application. This file will contain all elements and attributes specific to AngularJS.

Create the starting point of your application.

> Run the following touch command in your terminal
```bash
touch /home/project/guide-rest-client-angularjs/start/src/main/webapp/index.html
```


> Then, to open the index.html file in your IDE, select
> **File** > **Open** > guide-rest-client-angularjs/start/src/main/webapp/index.html, or click the following button

::openFile{path="/home/project/guide-rest-client-angularjs/start/src/main/webapp/index.html"}



```html
<!DOCTYPE html>
<html>
    <head>
        <script 
            src='http://ajax.googleapis.com/ajax/libs/angularjs/1.6.6/angular.js'/>
        </script>
        <script 
           src='http://ajax.googleapis.com/ajax/libs/angularjs/1.6.6/angular-resource.js'>
        </script>
        <script src='./js/consume-rest.js'></script>
    </head>
    <body ng-app='consumeRestApp'>
        <div ng-controller='ArtistsCtrl'>
            <div ng-repeat='artist in artists'>
                <p>{{ artist.name }} wrote {{ artist.albums.length }} albums:</p>
                <div ng-repeat='album in artist.albums'>
                    <p style='text-indent: 20px'>
                        Album titled <b>{{ album.title }}</b> by 
                                     <b>{{ album.artist }}</b> contains 
                                     <b>{{ album.ntracks }}</b> tracks
                    </p>
                </div>
            </div>
        </div>
    </body>
</html>
```




Before your application is bootstrapped, you must pull in two ***AngularJS*** libraries and import ***consume-rest.js***.

The first import is the base AngularJS library, which defines the ***angular.js*** script in your HTML. The second import is the library responsible for providing the APIs for the ***$resource*** service, which also defines the ***angular-resource.js*** script in your HTML. The application is bootstrapped because the ***consumeRestApp*** application module is attached to the ***body*** of the template.

Next, the ***ArtistCtrl*** controller is attached to the DOM to create a new child scope. The controller will make the ***artists*** property of the ***$scope*** object available to access at the point in the DOM where the controller is attached.

Once the controller is attached, the ***artists*** property can be data-bounded to the template and accessed using the ***{{ artists }}*** expression. You can use the ***ng-repeat*** directive to iterate over the contents of the ***artists*** property.


After everything is set up, open your browser and check out the application by going to the URL that the following command returns:
```bash
echo http://${USERNAME}-9080.$(echo $TOOL_DOMAIN | sed 's/\.labs\./.proxy./g')
```

See the following output:

```
foo wrote 2 albums:
    Album titled *album_one* by *foo* contains *12* tracks
    Album tilted *album_two* by *foo* contains *15* tracks
bar wrote 1 albums:
    Album titled *foo walks into a bar* by *bar* contains *12* tracks
dj wrote 0 albums:
```


::page{title="Testing the AngularJS client"}

No explicit code directly uses the consumed artist JSON, so you do not need to write any test cases for this guide.


Whenever you change your AngularJS implementation, the application root at `http://accountname-9080.theiadocker-4.proxy.cognitiveclass.ai` will reflect the changes automatically. You can visit the root to manually check whether the artist JSON was consumed correctly.

When you are done checking the application root, exit development mode by pressing CTRL+C in the command-line session where you ran the server, or by typing q and then pressing the ***enter/return*** key.

When you develop your own applications, testing becomes a crucial part of your development lifecycle. If you need to write test cases, follow the official unit testing and end-to-end testing documentation on the [official AngularJS website](https://docs.angularjs.org/guide/unit-testing).


::page{title="Summary"}

### Nice Work!

You have just accessed a simple RESTful web service and consumed its resources by using AngularJS in Open Liberty.



### Clean up your environment


Clean up your online environment so that it is ready to be used with the next guide:

Delete the ***guide-rest-client-angularjs*** project by running the following commands:

```bash
cd /home/project
rm -fr guide-rest-client-angularjs
```

### What did you think of this guide?

We want to hear from you. To provide feedback, click the following link.

* [Give us feedback](https://openliberty.skillsnetwork.site/thanks-for-completing-our-content?guide-name=Consuming%20a%20RESTful%20web%20service%20with%20AngularJS&guide-id=cloud-hosted-guide-rest-client-angularjs)

Or, click the **Support/Feedback** button in the IDE and select the **Give feedback** option. Fill in the fields, choose the **General** category, and click the **Post Idea** button.

### What could make this guide better?

You can also provide feedback or contribute to this guide from GitHub.
* [Raise an issue to share feedback.](https://github.com/OpenLiberty/guide-rest-client-angularjs/issues)
* [Create a pull request to contribute to this guide.](https://github.com/OpenLiberty/guide-rest-client-angularjs/pulls)



### Where to next?

* [Creating a RESTful web service](https://openliberty.io/guides/rest-intro.html)
* [Consuming a RESTful web service](https://openliberty.io/guides/rest-client-java.html)


### Log out of the session

Log out of the cloud-hosted guides by selecting **Account** > **Logout** from the Skills Network menu.
