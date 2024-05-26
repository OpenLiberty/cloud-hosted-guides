# cloud-hosted-guides
A temp repo to store instructions and other artifacts in regards to our Quick Labs and Workshops

# Mirror Instructions
All of the folders under `https://github.com/OpenLiberty/cloud-hosted-guides/tree/master/instructions/` have a matching Gitlab repository used for SkillsNetwork labs.
When creating a new cloud-hosted-guides guide some steps must be taken to ensure that future changes will be mirrored.

## Step-by-Step Instructions

### Step 1: Create the QuickLab

1. **Create a New Project:**

- Navigate to the SkillsNetwork Author portal at [Become an Author](https://skills.network/authors).
- Follow the process to [create a Guided Project in Author Workbench](https://author.skills.network/quicklabs/new?how_to_continue=true)

### Step 2: Add Collaborators

1. **Access Your QuickLab:**
   - Go to your QuickLab page: `https://author.skills.network/quicklabs/:id?show=team`.

2. **Invite Team Members:**
   - Click on "Invite Member."
   - Search for @Gilbert's email and add him as an `Admin` or `Instructor`.

### Step 3: Prepare Instruction Templates

1. **Duplicate the Template Folder:**
   - Copy and rename the `cloud-hosted-guide-template` folder under `instruction-templates` to and change the folder name to match your QuickLab name.
   - Keep the original `instructions.md` filename as it will be the file used for editing content.

### Step 4: Update Configuration

1. **Modify the `mirror.json` and `StagingMirror.json` File:**
   - Add a new entry to the `mirror.json` and `StagingMirror.json` file using the following format:

   ```json
   {
     "guide": "name-of-your-quicklab",
     "github": "the-folder-name",
     "quick_lab_id": "quicklab_id",
     "lab_id": "lab_id"
   }

2. **Locate QuickLab and Lab IDs:**

- Find the QuickLab ID from the URL: https://author.skills.network/quicklabs/:quicklab_id.
- To find the Lab ID, navigate to the Labs tab, hover over the lab, and note the Lab ID from the URL shown at the bottom left of your screen: https://author.skills.network/labs/:lab_id.
- Update the mirror.json with these IDs.


### Step 5: Deploy Your Changes

1. **Mirror Guide Setup:**

- Initiate a `Mirror a guide to AWB` action.
- Enter the `Guide name` and `To` Options
- - For `To`, `prod` for publishing the lab to the world, `staging` for saving the lab without publishing.

## How to Contribute

To contribute to this project, please follow these steps:

1. **Create a Pull Request (PR):**
   - Always create a pull request targeting the `staging` branch when you wish to make updates. This is where all initial changes should be made and reviewed.

2. **Review Process:**
   - Have someone review your content changes in the `staging` branch. This ensures all updates meet the project's standards and requirements before they are deployed.

3. **Deploy to Staging:**
   - Use the `Mirror a guide to AWB` GitHub Action to deploy your updates to staging. Set the deployment target by selecting `staging` in the action's inputs. This step saves the content on the Author Workbench (AWB) side for further review.

4. **Approval and Merging:**
   - Once your changes are reviewed and approved in the `staging` branch, merge them into the `prod` branch.

5. **Publish to Production:**
   - After merging to `prod`, use the `Mirror a guide to AWB` GitHub Action again to publish your content. Set the `To` parameter to `prod` to finalize the publication on the production environment.

Following this workflow ensures a structured and error-minimized deployment of content, maintaining high standards of quality and consistency.
