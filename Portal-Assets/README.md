# OpenLiberty edX Theme

This directory contains the custom CSS and Javascript included in the OpenLiberty edX deployment.

### How does it work?

The theme deployed on the OpenLiberty edX instance points to `https://cocl.us/openliberty.css` and `https://cocl.us/openliberty.js`. https://cocl.us is just a link shortener, which allows us to swap the destination of these files without changing the theme.

This directory is using GitHub Pages to serve the static `openliberty.css` and `openliberty.js` files. The link shortener points to the GitHub Pages URLs in order to serve the CSS and Javascript content for the theme.


# edX Theme directory

In the `edX Theme` directory you can find a folder containing the `lms` file. That folder is the edX theme and it's used to build the portal.

## Making changes to the portal

 - To make changes to the theme clone the lms folder.
 - Do the required to the code
 - Zip the folder (Make sure it is names `lms.zip`)
 - Go to the openliberty portal admin page -> In the left hand menu -> Settings -> Advances -> Under Open Edx Theme -> Press Choose file -> Select the `lms.zip`
