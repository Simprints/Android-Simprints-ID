# Face modality

The face modality was created as a way to have contactless biometrics as an option for SimprintsID.

##  Important links
[Confluence](https://simprints.atlassian.net/wiki/spaces/CS/overview)  
[Jira](https://simprints.atlassian.net/secure/RapidBoard.jspa?rapidView=19)

## Flow overview

SimprintsID start the face modality by creating an `Intent` to [FaceOrchestratorActivity.kt](src/main/java/com/simprints/face/orchestrator/FaceOrchestratorActivity.kt). In this `Intent` an [IFaceRequest.kt](../moduleapi/src/main/java/com/simprints/moduleapi/face/requests/IFaceRequest.kt) is mandatory. When getting the request from SimprintsID, `FaceOrchestratorViewModel` needs to transform it from a ModuleAPI interface to a domain class.

After the flow is finished (images are captured or matched against a probe), `FaceOrchestratorViewModel` transforms the domain response to a ModuleAPI interface and returns the result to SimprintsID as an `Intent` inside the activity result.
