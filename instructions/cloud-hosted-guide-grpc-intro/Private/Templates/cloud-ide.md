---
markdown-version: v1
---
::page{title="Please Read This"}

# Please, READ and Delete this section before publishing your Guided Project

Welcome to your Guided Project. Just a reminder that these labs are meant to be *quick* tutorials not a comprehensive course. If you are looking to create a master class or a workshop, you should consider doing it as a Skills Network *course*. Your course can have a series of labs that can use exactly the same technology as these Guided Projects but it will also allow you to do labs that build on the outcome of the previous lab. Most important, course participants get completion certificates, badges and other credentials as a reward for their hard work. Read this short [knowledge base article](https://support.skills.network/knowledgebase/articles/1946458) to learn how to create a course.

Let's move on with building your lab. This file you are in (instructions.md) is the place where you create the the instructions for your Guided Project users to follow. Here are a few recomendations:
* Divide your Guided Project in to steps with each step having a goal your lab users will achieve. You create a step by clicking the new page button above which will add our page directive to the markdown. Make sure to have a new line before and after your page directives.
* Make sure your instructions are actually teaching the concepts and not turning your users in to robots that repeat your keystrokes. This means you should be explaining the goal and what you are trying to do not just commands to get it done.
* Use pictures (and videos) to illustrate your point. Upload images with the image button :fa-picture-o: in the toolbar.
* Always provide a call to action at the end of the lab. Tell people about a service they should try on the IBM Cloud and give them a url for this service. Don't worry about campaign codes etc. Our CI/CD process will add proper campaign codes so that your lab will get full credit automatically.
* These are coding labs. Please use code markdown to illustrate the output and, more important, the code that learners should learn.
 
### How to properly use code blocks

When building a Guided Project you will find that you need to use code markdown in the following situations:
1. To display the output the user will see
2. For commands they will type/run in a terminal window
3. For contents they will type in to editor while editing files that contain code

The best way to markdown output is to simply click the code block button :fa-file-code-o:include which will generate code block markdown like so:

```shell
echo "Hello World"
```

- All code blocks display a copy to clipboard button for your readers which allows them to copy your code with a single click. No more highlighting the text, clicking on `Ctrl+C` (`Command+C` on Apple Mac) and possibly missing a few characters.
- Choose `shell` or `bash` as the language and learners will get a button directly in their instructions to run the commands in their terminal.

### Other features

- The _Open File_ button (:fa-file-text:) creates a button learners can click to open a file directly in their Cloud IDE editor.
- The _Open an Application_ button (:fa-rocket:) creates a button learners can click to open a web application they started in Cloud IDE.
- The _Open a Database_ button (:fa-database:) create a button learners can click which opens (and optionally _starts_) one of Cloud IDE's supported databases. This currently includes MySQL, MongoDB, Cassandra, and PostgreSQL.
- The _Add a Question_ button (:fa-question:) creates questions in the learners instructions that includes hidden answers the learner can reveal with a click.


Please note that there are some limitations in the markdown we can process so it is best to stay clear of these:

* We currently cannot hyper-link to a specific step on the Guided Project


**We do recommend that you use the following as an outline for your Guided Project**

---
Please, remember to delete everything above before publishing your lab.
---


::page{title="TITLE"}

Make the title short and meaningfull. Let the title give people an idea of what to expect not the steps. 

This is the section where you want to welcome people to your lab and to excite them with an expectation of what they will be able to learn. Please don't be dry and boring; that is what bad university lectures are for. People spend time on your lab not because they have to but because they want to. Please, make it worth their while. 

## Learning Objectives
Tell your audience what they can expect to learn. Better yet, tell them what they will be able to do as a result of completing your lab.

## Prerequisites (optional)
List the prior knowledge if any, required to take this tutorial

::page{title="Step 1: learn something simple"}

This is the first section. You may have as many sections as you want

::page{title="Step 2: advance to the next step"}

Now we are rolling. 

...

::page{title="Step n: another step in conquering knowledge"}

You may have as many sections as you want

::page{title="Summary"}

Summarize key learning points against the learning objectives. Make the user feel good about what they have achieved. Also, tell tehm if they did miss something they can always come back and do the lab agian.

::page{title="Next Steps"}

Tell people what they should explore next. Here is an example:

In this lab you got to deploy your Node.js application to a Kubernetes cluster. You used a shared cluster provided to you by the IBM Developer Skills Network. If you are interested in continuing to learn about Kubernetes and containers, you should get your own [free Kubernetes cluster](https://www.ibm.com/cloud/container-service/) and your own free [IBM Container Registry](https://www.ibm.com/cloud/container-registry).
