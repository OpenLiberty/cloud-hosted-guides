# OpenLiberty edX Theme

This directory contains the custom CSS and Javascript included in the OpenLiberty edX deployment.

### How does it work?

The theme deployed on the OpenLiberty edX instance points to `https://cocl.us/openliberty.css` and `https://cocl.us/openliberty.js`. https://cocl.us is just a link shortener, which allows us to swap the destination of these files without changing the theme.

This directory is using GitHub Pages to serve the static `openliberty.css` and `openliberty.js` files. The link shortener points to the GitHub Pages URLs in order to serve the CSS and Javascript content for the theme.
