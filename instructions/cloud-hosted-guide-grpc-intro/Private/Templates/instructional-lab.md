---
markdown-version: v1
---
# Everything you need to know to create an instructional lab

Welcome to your instructional lab!

The environment you are in right now is called Author IDE.
> Author-IDE is a tool for creating and managing instructions for your labs and guided projects.

Instructional labs are meant to be one stream of instructions that don't require learners to use Cloud IDE.

> Cloud-IDE is a fully-online integrated development environment that supports numerous popular programming languages, allowing your learners to code, develop, and learn in one location.

IBM Skills Network provides you with Author-IDE, Cloud-IDE, as well as other tools, so you can create amazing courses with labs and guided projects for your learners.

If you want your learners to use your instructions together with the Cloud IDE, consider changing the type of this lab from **instructional Lab** to either of the three available **Cloud IDE** versions.

Let's get started with building your instructional lab!

# Best practices for creating instructions for an instructional lab

* Divide your instructions into steps, with each step having a goal your lab users will achieve.
* Make sure your instructions are actually teaching the concepts and not turning your users into robots that repeat your keystrokes. This means you should be explaining the goal and what you are trying to do not just commands to get it done.
* Use pictures (and videos) to illustrate your point. Upload images with the image button :fa-picture-o: in the toolbar.
* Always provide a call to action at the end of the lab. Tell people about a service they should try on the IBM Cloud and give them a URL for this service. Don't worry about campaign codes etc. Our CI/CD process will add proper campaign codes so that your lab will get full credit automatically.

# How to properly use code blocks

When building an instructional lab, you will find that you need to use code markdown to display some output or a piece of code to your learners.

The best way to include a piece of code to your instructions is to simply click the code block button :fa-file-code-o:, select the language, include your code, and click *Enter*. As a result, a code block will be generated like so:

```javascript
function add(a, b) {
	return a+b
}
```

- All code blocks display a copy to clipboard button for your readers which allows them to copy the content of the code block with a single click. No more highlighting the text, clicking on `Ctrl+C` (`Command+C` on Apple Mac) and possibly missing a few characters.

You can also insert inline code by using \`\`. Here is an example of an inline code: `ls`. This approach is usually used for short single-line commands.

# Author-IDE's features

You can find various buttons in the toolbar that can make your instructions better. Here are some of them:

## **Add a question dropdown** (:fa-angle-double-down:)
Creates a question in the instructions that includes hidden answers the learner can reveal with a click.

1. What is Author IDE?

<details>
	<summary>Click here for the answer</summary>
	Author-IDE is a tool for creating and managing instructions for your labs and guided projects.
</details>

## **Link** (:fa-link:)
Creates an inline link in the instructions that can link to other websites with a click.

Here is link to [Github](http://github.com "GitHub")

## **Image** (:fa-image:)
Inserts an image to your instructions

## **Emoji** (:fa-smile-o:)
Inserts inline emoji to your instructions :fa-user: :fa-star-o: :fa-sun-o:

> Feel free to explore what other buttons do on your own.

# Limitations

Please note that there are some limitations in the markdown we can process, so it is best to stay clear of these:

* Don't indent code blocks and images
* Don't nest numbered lists as it causes unexpected behaviours.
* If you use any HTML tags in your instructions, make sure there is no text adjacent to the outermost closing tag and that the line after is empty.
* We currently cannot hyperlink to a specific step in your instructions.

# Getting Started

**We do recommend that you use the following as an outline for your instructional lab.**

---
Please, remember to delete everything above before publishing your lab.
---

# Title of your lab
**Estimated time needed:** X minutes

Provide a brief  Scenario or Overview or a few introductory sentences

## Objectives
After completing this lab, you will be able to:
1. Objective for Exercise 1
1. Objective for Exercise 2
1. etc.


## Exercise 1 : < Title of Exercise 1 >
In this exercise, you will < short sentence describing what’s done in this exercise >.

1. Step 1 details ...

1. Step 2 details with sample screenshot...

1. Step 3 details...

## Exercise 2 : < Title of Exercise 2 >
In this exercise, you will ...

### Task A : < Title of Task A in Ex 2 >

1. Step 1 in Task A in Ex 2

1. Step 2 in Task A in Ex 2

### Task B : < Title of Task B in Ex2 >

1. Step 1 details


1. Step 2 details


## (Optional) Summary / Conclusion / Next Steps
Add ending info here, if required, and rename the title accordingly. Otherwise, remove this optional section.

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
