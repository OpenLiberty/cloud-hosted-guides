# Mirror Instructions
All of the folders under `https://github.com/OpenLiberty/quick-labs/tree/master/instructions/` have a matching Gitlab repository used for SkillsNetwork labs.
When creating a new quick-labs guide some steps must be taken to ensure that future changes will be mirrored.

1. Add the repository names to the `mirror.yml` file

In the following section of mirror.yml, under the `repo:` tag, you must add both the Github folder and Gitlab repository names.
```
jobs:
  deploy:
    name: Start Mirror Containers
    runs-on: ubuntu-latest
    strategy:
      matrix: # Uses an array of Json variables to pass the repo names.
              # The names differ between Github and Gitlab so this is necessary.
              # Add new quick-labs here to add them to the mirror process.
              # i.e. {"github":"new-lab-github-folder","gitlab":"new-lab-gitlab-url"}
        repo:
          - {"github":"develop-microservices-docker","gitlab":"using-docker-to-develop-java-microservices"}
          - {"github":"guide-cdi-intro","gitlab":"injecting-dependencies-into-a-java-microservices"}
          ...
```


2. Apply Gitlab deploy key

Once the Gitlab repository has been created the owner/maintainer must assign the correct deploy key.
This can be done by going to `https://gitlab.com/ibm/skills-network/quicklabs/<repo-name>/settings/repository`, scrolling down to the `Deploy Keys` section and enabling the `DEPLOY_KEY_QUICK_LABS` key. 
You must then navigate the the `Enabled deploy keys` tab and select the edit option for the newly added key, from here enable the `Write access allowed` setting.