---
markdown-version: v1
---
::page{title="Welcome to your Datasette lab!"}

The environment you are in right now is called Author IDE.
> Author IDE is a tool for creating and managing instructions for your labs.

The instructions that you create will be complimented by a fully configured Datasette running on the cloud and a powerful development environment for learners to practice in.

Your learners will see both the Datasette environment and your Instructions on one page in a browser. The Instructions are going to be on the left, and Datasette is on the right.

Let's get started with building your Datasette lab!

::page{title="About Datasette"}

Upload, explore, and tell a story with your data using Datasette. With its integrated set of data analysis tools, Datasette allows you to explore patterns in your data and easily share your findings with the world as an interactive website. From scientists to data journalists, Datasette has been embraced by a wide range of users for its ease of use and impressive capability.

::page{title="About Author IDE"}

Author IDE is a tool for creating and managing your instructions.

> To learn more about Author IDE Basics and some of its features, click on :fa-lightbulb-o: in the upper right corner, select `I want to...`, and explore available options in the widget that will appear in the bottom right corner.

In the following steps, we will discuss best practices for creating your Datasette-based lab instructions and introduce you to some shortcuts you might want to use to make your instructions clearer.

::page{title="Best practices for creating instructions for a Datasette-based lab"}

* Divide your instructions into steps, with each step having a goal your lab users will achieve. You create a step by clicking the *New Page* button located in the toolbar above which will add a page directive to the editor.
**Note**: All instructions for Datasette-based labs must contain at least one page to be displayed properly!
* Make sure your instructions are actually teaching the concepts and not turning your users into robots that repeat your keystrokes. This means you should be explaining the goal and what you are trying to do not just commands to get it done.
* Use pictures (and videos) to illustrate your point. Upload images with the image button :fa-picture-o: in the toolbar.
* Always provide a call to action at the end of the lab. Tell people about a service they should try on the IBM Cloud and give them a URL for this service. Don't worry about campaign codes etc. Our CI/CD process will add proper campaign codes so that your lab will get full credit automatically.
* These are coding labs. Please use code markdown to illustrate the output and, more importantly, the code that learners should learn.

::page{title="How to properly use code blocks"}

When building a Datasette-based lab you will find that you need to use code markdown in the following situations:
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


## **Add a question dropdown** (:fa-angle-double-down:)
Creates a question in the instructions that includes hidden answers the learner can reveal with a click.

1. Does the **Datasette** environment allow Control Panel access?

<details>
	<summary>Click here for the answer</summary>
	Yes, it does!
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

**We do recommend that you use the following as an outline for your Datasette-based lab instructions.**

---
Please, remember to delete everything above before publishing your Datasette-based lab.
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

In this lab, you got to deploy your Node.js application to a Kubernetes cluster. You used a shared cluster provided to you by the IBM Developer Skills Network. If you are interested in continuing to learn about Kubernetes and containers, you should get your own [free Kubernetes cluster](https://www.ibm.com/cloud/container-service/) and your own free [IBM Container Registry](https://www.ibm.com/cloud/container-registry).

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