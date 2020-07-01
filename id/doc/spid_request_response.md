# SimprintsID to modality request and response

When SimprintsID calls the modality, it calls it with an `Intent` based on a `moduleapi` interface for the current job for that modality, like [IFaceCaptureRequest.kt](/moduleapi/src/main/java/com/simprints/moduleapi/face/requests/IFaceCaptureRequest.kt). More about it can be read in the [Modularisation document](https://docs.google.com/document/d/1E-SNLGbqsAjn1IVamQhDEcWQJ092tLnNLLeTCo_A160/edit#). Each Request has its own Response, that can be seen in `moduleapi`. So, for example a `IFaceRequest` have an associated [IFaceResponse.kt](/moduleapi/src/main/java/com/simprints/moduleapi/face/responses/IFaceResponse.kt).

Considering that the user can exit the app by using the Exit Form (previously known as Refusal Form), SimprintsID needs to be able to recognize and handle this response as well. What happens is that [IFaceResponse.kt](/moduleapi/src/main/java/com/simprints/moduleapi/face/responses/IFaceResponse.kt) has a type, that SimprintsID will handle when it gets the response.

![modality_response_inheritance.png](modality_response_inheritance.png)

The [IFaceExitFormResponse.kt](/moduleapi/src/main/java/com/simprints/moduleapi/face/responses/IFaceExitFormResponse.kt) has 2 attributes, a reason and an extra. The reason is the mapping from the option that the user selected and the extra is the text typed by the user. The reason need to be mapped to an `IFaceExitReason` because that is the interface that `moduleapi` understands.

When the modality step is finished, SimprintsID Orchestrator (and related classes) will check if the response was successful or if it was a refusal event. If it was a refusal event, all subsequent steps will be marked as completed. You can check this logic in [ModalityFlowBaseImpl.kt](/id/src/main/java/com/simprints/id/orchestrator/modality/ModalityFlowBaseImpl.kt#L68).

After every step, the Orchestrator checks if there is at least one step waiting to run. In the case of a refusal event, all steps were marked as complete so the Orchestrator will create the response for the calling app. The first step in that is checking if there was any error or refusal event in the flow. If there was one, SimprintsID should return the error or refusal event to the calling app. That is done in [BaseAppResponseBuilder.kt](/id/src/main/java/com/simprints/id/orchestrator/responsebuilders/BaseAppResponseBuilder.kt#L21).


