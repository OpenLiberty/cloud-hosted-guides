# Mirror Instructions
All of the folders under `https://github.com/OpenLiberty/cloud-hosted-guides/tree/master/instructions/` have a matching Gitlab repository used for SkillsNetwork labs.
When creating a new cloud-hosted-guides guide some steps must be taken to ensure that future changes will be mirrored.

1. Create the Quicklab 

Create the neccessary Quicklab repository on Gitlab via the SkillsNetwork Author page: https://www.skills.network/become-an-author/

2. Add the OpenLiberty Guides group

Once your Quicklab repository has been created navigate to it on Gitlab and select the "Members" tab. From here select the "Invite Group" tab and invite the "[OpenLiberty Guides](https://gitlab.com/openliberty-guides)" group as a `maintainer`. If you are not yet a member of the OpenLiberty Guides group then tag @gkwan-ibm, @jamiecoleman92, or @jakub-pomykala on Github.

3. Add the repository names to the `mirror.yml` file

In the following section of mirror.yml, under the `repo:` tag, you must add both the Github folder and Gitlab repository (URL) names.
```
jobs:
  deploy:
    name: Start Mirror Containers
    runs-on: ubuntu-latest
    strategy:
      matrix: # Uses an array of Json variables to pass the repo names.
              # The names differ between Github and Gitlab so this is necessary.
              # Add new cloud-hosted-guides here to add them to the mirror process.
              # i.e. {"github":"new-lab-github-folder","gitlab":"new-lab-gitlab-url"}
        repo:
          - {"github":"develop-microservices-docker","gitlab":"using-docker-to-develop-java-microservices"}
          - {"github":"guide-cdi-intro","gitlab":"injecting-dependencies-into-a-java-microservices"}
          ...
```


4. Apply Gitlab deploy key

Once the Gitlab repository has been created the owner/maintainer must assign the correct deploy key.
This can be done by going to `https://gitlab.com/ibm/skills-network/quicklabs/<repo-name>/settings/repository`, scrolling down to the `Deploy Keys` section and enabling the `DEPLOY_KEY_QUICK_LABS` key. 
You must then navigate the the `Enabled deploy keys` tab and select the edit option for the newly added key, from here enable the `Write access allowed` setting.
