---
markdown-version: v1
tool-type: theiadocker
---
::page{title="Everything you need to know to create a Cloud-IDE-based lab"}

Welcome to your Cloud-IDE-based lab!

The environment you are in right now is called Author IDE.
> Author IDE is a tool for creating and managing instructions for your labs and guided projects.

The instructions that you create will be used together with the Cloud IDE.
> Cloud IDE is a fully-online integrated development environment that supports numerous popular programming languages, allowing your learners to code, develop, and learn in one location.

IBM Skills Network provides you with Author IDE, Cloud IDE, as well as other tools, so you can create amazing courses with labs and guided projects for your learners.

Your learners will see both  the Cloud IDE and your Instructions on one page in a browser. The Instructions are going to be on the left, and the Cloud IDE is on the right.

Let's get started with building your Cloud-IDE-based lab!

::page{title="About Cloud IDE"}

Cloud IDE is a very powerful tool as it brings hands-on experience to your learners.
Besides the fact that Cloud IDE supports various programming languages, it also has an integrated terminal, file system, source control management (SCM) that includes Git support, a robust ecosystem of add-ons and plugins, Skills Network custom features, and other stuff.

Cloud IDE is supported in three environments:
1. **Cloud IDE**
1. **Cloud IDE with Docker**
1. **Cloud IDE with OpenShift**

> ## **Important:** The only secure environment is *Cloud IDE*
 **Cloud IDE with Docker** and **Cloud IDE with OpenShift** environments are not completely secure. This means that any personal information, usernames, passwords, or access tokens SHOULD NOT be used by the learners in these environments for any purposes. Beware of that and DO NOT encourage learners to log in anywhere using their personal accounts from inside **Cloud IDE with Docker** or  **Cloud IDE with OpenShift** environments.

When you created the lab, you specified **Cloud IDE with Docker** as the *tool*. This means your learners **SHOULD NOT** use their personal usernames, passwords, or access tokens to authenticate with external services (for example Github); **DO NOT** encourage them to do so.

::page{title="About Cloud IDE with Docker"}

**Cloud IDE with Docker** provides your learners with even more advanced tools than the basic **Cloud IDE** environment.

## IBM Cloud Container Registry

When your learners launch **Cloud IDE with Docker**, they are automatically logged into the IBM Cloud account that has been automatically generated for them by the Skills Network Labs. Your learners will also have an IBM Cloud Container Registry (ICR) namespace created for them, to which they have full access to push and pull images. The learner's namespace will be stored in the environment variable `$SN_ICR_NAMESPACE`.

## Docker

**Cloud IDE with Docker** contains a running docker daemon which allows your learners to use docker commands such as `docker pull`, `docker run`, etc. You can leverage the fact that your learners have access to their namespace on ICR, and instruct them to push images to the ICR like so:

1. Tag your image so that it can be pushed to IBM Cloud Container Registry.
```bash
docker tag someimage:v1 us.icr.io/$SN_ICR_NAMESPACE/hello-world:1
```
2. Push the newly tagged image to IBM Cloud Container Registry.
```bash
docker push us.icr.io/$SN_ICR_NAMESPACE/hello-world:1
```

## Kubernetes

Your learners can leverage Kubernetes inside **Cloud IDE with Docker** environment using `kubectl` command-line tool. Each of your learners will have a Kubernetes namespace already configured and ready to use. The name of a learner's Kubernetes namespace is stored in the environment variable `$SN_ICR_NAMESPACE` and is the same as the learner's namespace for ICR.

> When a learner launches **Cloud IDE with Docker** environment, a Kubernetes Secret `icr` is automatically created for them. The learner can interact with their ICR namespace via Kubernetes using this secret. As long as the Kubernetes pods point to the images stored in the learner's ICR namespace `$SN_ICR_NAMESPACE`, the `icr` secret will be set as the default imagePullSecrets (i.e. the learner doesn't need to configure anything).  The `icr` secret can be viewed by running `kubectl get secrets -n $SN_ICR_NAMESPACE`.

To learn more about Kubernetes in Cloud IDE, click on :fa-lightbulb-o: in the upper right corner, select `I want to...`, and choose `Use Kubernetes` in the widget that will appear in the bottom right corner.

::page{title="About Author IDE"}

Author IDE is a tool for creating and managing your instructions.

> To learn more about Author IDE Basics and some of its features, click on :fa-lightbulb-o: in the upper right corner, select `I want to...`, and explore available options in the widget that will appear in the bottom right corner.

When creating your Instructions for a Cloud-IDE-based lab, keep in mind that they are meant to be used in conjunction with Cloud IDE by your leaners. With that idea in mind, Author IDE was designed to provide you with various shortcuts that allow your learners to interact with Cloud IDE directly from instructions.

In the following steps, we will discuss best practices for creating your Cloud-IDE-based lab instructions and introduce you to some shortcuts you might want to use to make your learners interact with Cloud IDE directly from instructions.

::page{title="Best practices for creating instructions for a Cloud-IDE-based lab"}

* Divide your instructions into steps, with each step having a goal your lab users will achieve. You create a step by clicking the *New Page* button located in the toolbar above which will add a page directive to the editor.
**Note**: All instructions for Cloud-IDE-based labs must contain at least one page to be displayed properly!
* Make sure your instructions are actually teaching the concepts and not turning your users into robots that repeat your keystrokes. This means you should be explaining the goal and what you are trying to do not just commands to get it done.
* Use pictures (and videos) to illustrate your point. Upload images with the image button :fa-picture-o: in the toolbar.
* Always provide a call to action at the end of the lab. Tell people about a service they should try on the IBM Cloud and give them a URL for this service. Don't worry about campaign codes etc. Our CI/CD process will add proper campaign codes so that your lab will get full credit automatically.
* These are coding labs. Please use code markdown to illustrate the output and, more importantly, the code that learners should learn.

::page{title="How to properly use code blocks"}

When building a Cloud-IDE-based lab you will find that you need to use code markdown in the following situations:
1. To display the output the user will see
2. For commands they will type/run in a terminal window
3. For contents they will type into the editor while editing files that contain code

The best way to include a piece of code to your instructions is to simply click the code block button :fa-file-code-o:, select the language, include your code, and click *Enter*. As a result, a code block will be generated like so:

```javascript
function add(a, b) {
	return a+b
}
```

- All code blocks display a copy to clipboard button for your readers which allows them to copy your code with a single click. No more highlighting the text, clicking on `Ctrl+C` (`Command+C` on Apple Mac) and possibly missing a few characters.

- Choose `shell` or `bash` as the language and learners will get a button directly in their instructions to run the commands in their terminal.

```bash
echo "Hello World"
```

You can also insert inline code by using \`\`. Here is an example of an inline code: `ls`. This approach is usually used for short single-line commands.

::page{title="How to interact with Cloud IDE directly from instructions"}

You can find various buttons under the **Cloud IDE** dropdown menu located in the toolbar. These buttons are used to generate custom directives that are rendered as buttons that enable your learners to interact with Cloud IDE directly from instructions:

## **Open a file in IDE** (:fa-file-text:) 
Creates a button learners can click to open a file directly in their Cloud IDE. You can also leverage this feature if you want to create a new file since Cloud IDE will detect if the file doesn't exist and prompt the user to create one.

::openFile{path="file.txt"}

## **Open an application** (:fa-rocket:) 
Creates a button learners can click to open a web application in Cloud IDE. Instruct your learners on how to run an application at a specific port, and use the same port while creating this custom button. Your learners can then use this button from instructions to open their web application inside Cloud IDE.

::startApplication{port="5000" display="internal" name="Web Application" route="/"}

## **Open a database in IDE** (:fa-database:) 
Creates a button learners can click to open a database page in their Cloud IDE. You can optionally specify to also start the database after the page is opened in Cloud IDE.

Supported databases:
- MySQL
- PostgreSQL
- Cassandra
- MongoDB

**Important**: Databases are only supported in **Cloud IDE with Docker** and **Cloud IDE with OpenShift** environments.

::openDatabase{db="MySQL" start="true"}

## **Open a big data tool in IDE** (:fa-database:) 
Creates a button learners can click to open a big data tool page in their Cloud IDE. You can optionally specify to also start the tool after the page is opened in Cloud IDE.

Supported big data tools:
- Apache Airflow

**Important**: Big data tools are only supported in **Cloud IDE with Docker** and **Cloud IDE with OpenShift** environments.

::openBigDataTool{tool="Apache Airflow" start="false"}

---

To learn more about custom features that allow learners to interact with Cloud IDE from instructions, click on :fa-lightbulb-o: in the upper right corner, select `I want learners to...`, and explore available options in the widget that will appear in the bottom right corner.

::page{title="Other features"}

You can find other various buttons in the toolbar that can make your instructions better. Here are some of them:

## **Add a question dropdown** (:fa-angle-double-down:)
Creates a question in the instructions that includes hidden answers the learner can reveal with a click.

1. Is **Cloud IDE with Docker** environment completely secure?

<details>
	<summary>Click here for the answer</summary>
	No, it's not. The only environment that is completely secure is Cloud IDE
</details>

## **Link** (:fa-link:)
Creates an inline link in the instructions that can link to other websites with a click.

Here is link to [Github](http://github.com "GitHub")

## **Image** (:fa-image:)
Inserts an image to your instructions

## **Emoji** (:fa-smile-o:)
Inserts inline emoji to your instructions :fa-user: :fa-star-o: :fa-sun-o:

## **Header**
Creates a header block directive on top of the page(s) of your instructions to highlight text that you want your learners to see or be reminded of. The header will be added to the top of your instructions and is consistent across all pages. The header also comes with the option to be stickied, meaning they will stay on top of the page even if the learner scrolls down. You can only have one header in your instructions.

<br/>

> Feel free to explore what other buttons do on your own.

::page{title="Limitations"}

Please note that there are some limitations in the markdown we can process, so it is best to stay clear of these:

* Don't indent code blocks and images
* Don't nest numbered lists as it causes unexpected behaviours.
* If you use any HTML tags in your instructions, make sure there is no text adjacent to the outermost closing tag and that the line after is empty.
* Custom directives have to be surrounded by a new line.
* We currently cannot hyperlink to a specific step(page) in your instructions.

::page{title="Getting Started"}

**We do recommend that you use the following as an outline for your Cloud-IDE-based lab instructions.**

---
Please, remember to delete everything above before publishing your Cloud-IDE-based lab.
---

::page{title="TITLE"}

**Estimated time needed:** X minutes

Make the title short and meaningful. Let the title give people an idea of what to expect, not the steps.

This is the section where you want to welcome people to your lab and to excite them with an expectation of what they will be able to learn. Please don't be dry and boring; that is what bad university lectures are for. People spend time on your lab not because they have to but because they want to. Please, make it worth their while.

## Learning Objectives
Tell your audience what they can expect to learn and what they will be able to do as a result of completing your lab.

- Learning Objective 1
- Learning Objective 2
- Learning Objective 3
- ...

## Prerequisites (optional)
List the prior knowledge if any, required to take this tutorial

::page{title="Step 1: learn something simple"}

This is the first section. You may have as many sections as you want

::page{title="Step 2: advance to the next step"}

Now we are rolling.

...

::page{title="Step n: another step in conquering knowledge"}

You may have as many sections as you want

::page{title="Conclusion"}

Summarize key learning points against the learning objectives. Make the user feel good about what they have achieved. Also, tell them if they did miss something they can always come back and do the lab again.

## Next Steps
Tell people what they should explore next. Here is an example:

In this lab, you got to deploy your Node.js application to a Kubernetes cluster. You used a shared cluster provided to you by the IBM Developer Skills Network. If you are interested in continuing to learn about Kubernetes and containers, you should get your own [free Kubernetes cluster](https://www.ibm.com/cloud/container-service/?utm_source=skills_network&utm_content=in_lab_content_link&utm_id=Lab-test+-+v1) and your own free [IBM Container Registry](https://www.ibm.com/cloud/container-registry?utm_source=skills_network&utm_content=in_lab_content_link&utm_id=Lab-test+-+v1).

## Author(s)
[Author1 Name](optional link to profile) 
[Author2 Name](optional link to profile) 

### Other Contributor(s) 
< Contributor 1 Name >, < Contributor 2 Name >, etc.

## Changelog
| Date | Version | Changed by | Change Description |
|------|--------|--------|---------|
| yyyy-mm-dd | 0.1 | changer name | Initial version created |
|   |   |   |   |
|   |   |   |   |