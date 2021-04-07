# OpenLiberty edX Theme

This directory contains the custom CSS and Javascript included in the OpenLiberty edX deployment.

### How does it work?

# edX Theme directory

In the `edX Theme` directory you can find a folder containing the `lms` file. That folder is the edX theme and it's used to build the portal.

## Making changes to the portal

 - Export the required folder from the admin portal (for LMS = Settings -> Advanced ([Staging](https://ol-staging.skillsnetwork.site/admin/advanced/edit)) || [Production](https://openliberty.skillsnetwork.site/admin/advanced/edit) or for Themes = admin portal -> Theme [Staging](https://ol-staging.skillsnetwork.site/admin/themes) || [Production](https://ol-staging.skillsnetwork.site/admin/themes))
 - Do the required changed to the code
 - Zip the folder (Make sure it is named `lms` (the zip could be named whatever as long as the folder inside is called `lms`))
 - Go to the openliberty portal admin page ([Staging](https://ol-staging.skillsnetwork.site/admin)) || [Production](https://openliberty.skillsnetwork.site/admin) -> In the left hand menu -> Settings -> Advances -> Under Open Edx Theme -> Press Choose file -> Select the `lms.zip`
 - Also from within the LMS file you can make changes to the JS and CSS used by the page. These can be found within `static/custom` 


## Code in ol-admin-portal-backup directory

- Custom_Body_css_Section.css
  - In this file there is the backup of code from the portals admin site that can be found [here](https://openliberty.skillsnetwork.site/admin/advanced/edit) in the file name sections. e.g- "Custom Body CSS" is the code that can be found in [Custom_Body_css_Section.css](https://github.com/OpenLiberty/cloud-hosted-guides/blob/prod/Portal-Assets/Custom_Body_css_Section.md)


## Restoring previous versions
- If at any point a portal is broken due to changes, you can use the files in this repository to restore to the latest fully functional stage of the portal. To do this follow the following:
  - To restore LMS:
    - Pull the LMSyyyy_mm_dd.zip from this repository (Can be found in `default-theme(Liquid)` dir)
    - Import it to the admin portal -> Settings -> Advanced ([Staging](https://ol-staging.skillsnetwork.site/admin/advanced/edit)) || [Production](https://openliberty.skillsnetwork.site/admin/advanced/edit))
  - To restore themes:
    - Pull the default-themeYYYY_mm_dd.zip from this repository (Can be found in `edX-Theme` dir)
    - Import it to the admin portal -> Themes ([Staging](https://ol-staging.skillsnetwork.site/admin/themes) || [Production](https://ol-staging.skillsnetwork.site/admin/themes)).