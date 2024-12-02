
# **CI/CD Implementation**

## **CI Workflow**

The CI Workflow is responsible for ensuring the quality of code changes before they are merged into the main branch. It performs the following tasks:

1.  **Unit Testing:** Executes unit tests for all modules to ensure new code does not break existing functionality. This helps identify and fix bugs early in the development process.

2.  **Code Quality Analysis:** Runs SonarQube analysis to identify code quality issues and potential bugs. This helps maintain high code standards and prevent future problems.


**Workflow Trigger and Jobs**

The CI Workflow is triggered by two events:

1.  **Manual Trigger:** The workflow can be manually triggered through workflow dispatch, allowing developers to initiate the CI process on demand.

2.  **PR Changes:** The workflow is also triggered when a pull request (PR) is created or updated. This ensures that code changes are automatically tested and analyzed before being merged into the main branch.


The CI Workflow consists of 8 unit testing jobs plus the SonarQube scanning job. The unit testing jobs run in parallel, each responsible for testing a specific module or group of modules. The SonarQube scanning job waits until all the unit testing jobs are completed, the XML test coverage reports are uploaded, and then starts the sonar scan.

## **CD Workflow**
Deployment Workflow Diagram:
```mermaid
flowchart TD
    trigger([Manual Trigger]) --> setup(Release Setup)
    setup --> unit_tests(Run Unit Tests)
    unit_tests --> sonarqube(Run SonarQube Analysis)
    sonarqube --> deploy_dev(Build Dev APK)
    sonarqube --> deploy_stag(Build Staging APK)
    sonarqube --> deploy_prod(Build Production APK)

    subgraph Production Environment
        deploy_prod --> run_app_sweep(Perform App Sweep)
        run_app_sweep --> google_play_internal(Upload to Internal Track)
        google_play_internal --> google_play_alpha([Promote to Alpha Track])
        google_play_alpha --> add_release_tag(Add Release Tag)
        google_play_alpha --> google_play_prod_25([Promote 25% to Production])
        google_play_prod_25 --> google_play_prod_50([Promote 50% to Production])
        google_play_prod_50 --> google_play_prod_100([Promote 100% to Production])
    end

    subgraph Staging Environment
        deploy_stag --> deploy_firebase_stag(Upload to Firebase Staging)
    end

    subgraph Dev Environment
        deploy_dev --> deploy_firebase_dev(Upload to Firebase Dev)
    end
```


The CD Workflow is responsible for automatically deploying new code changes to different environments. It performs the following tasks:

1.  **Deployment to Dev Environment:** Deploys the latest development build to the Firebase distribution account, making it accessible for testing and development purposes.

2.  **Deployment to Staging Environment:** Deploys the latest staging build to the Firebase distribution account, allowing a wider group of users to test the application before release.

3.  **Deployment to Internal Environment:** Deploys the latest release build to the internal testing track, providing a final testing phase before deployment to production.

4.  **Promotion to Google Play Tracks:** Promotes the release build to different Google Play tracks in a controlled manner, starting with alpha and gradually progressing to the production track.



**Workflow Trigger and Jobs**

The CD Workflow is triggered by manual trigger through workflow dispatch, allowing developers to initiate the deployment process on demand.

The CD Workflow consists of several jobs, each responsible for a specific deployment task. For instance, the `deploy-to-dev` job deploys the dev build to Firebase, while the `promote-artifact` job promotes the release build to the specified Google Play track.

## **Dependency Updates workflow**

Updates project dependencies using Dependabot, an automated dependency management tool, ensuring that the project always uses the latest stable versions of its dependencies.
