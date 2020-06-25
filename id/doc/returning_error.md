The Error flow is subject to the SimprintsID Request/Response as all other parts. So, to understand better how the flow works, read its [document](spid_request_response.md)

# Returning errors

Errors happen and SimprintsID is not free of problems. The good part is that we catch those errors and return them to the calling app in a nice way, in a way that they can also handle them. If an error happens inside any of the modalities, that modality needs to return an IErrorResponse for that modality (ex: [IFaceErrorResponse.kt](/moduleapi/src/main/java/com/simprints/moduleapi/face/responses/IFaceErrorResponse.kt)) to `id` the same way as with any other response (ExitFormResponse, CaptureResponse). The same way the ExitForm has a reason, error also needs a reason.

Since the error is returned all the way to the calling app, almost all modules need to be updated to include the new error. This is different from ExitForm that is more generic. In order of change in a top-bottom approach, top being the modality:

- Your modality needs to create IErrorResponse (ex: [IFaceErrorResponse.kt](/moduleapi/src/main/java/com/simprints/moduleapi/face/responses/IFaceErrorResponse.kt)) and the reason Enum
- The same `ErrorResponse` needs to be create in the `moduleapi` or the new Enum constants that are needed. For example, [IFaceErrorResponse](/moduleapi/src/main/java/com/simprints/moduleapi/face/responses/IFaceErrorResponse.kt).
- While in the `moduleapi` module you should also create the [AppResponse](/moduleapi/src/main/java/com/simprints/moduleapi/app/responses/IAppErrorResponse.kt) that is going to be returned to the `clientapi` from `id`.
- Then you need to create the responses in the `id` module, for example [FaceErrorResponse](/id/src/main/java/com/simprints/id/domain/moduleapi/face/responses/FaceErrorResponse.kt) and also map from `id` to `moduleapi` to return to `clientapi`. Very confusing, but needed. 
- The mapping occurs in the [DomainToModuleApiAppResponse](/id/src/main/java/com/simprints/id/domain/moduleapi/app/DomainToModuleApiAppResponse.kt). 
- If your modality doesn't return an error from [BaseAppResponseBuilder](/id/src/main/java/com/simprints/id/orchestrator/responsebuilders/BaseAppResponseBuilder.kt), create a new check for your modality `ErrorResponse` inside the `getErrorOrRefusalResponseIfAny` method.
- Time to respond to the calling app! We do that inside the `clientapi` module, first mapping the previous Enums to this module Enums [in here](/clientapi/src/main/java/com/simprints/clientapi/domain/responses/ErrorResponse.kt), by adding making the errors return `biometricsComplete` true or false [in here](/clientapi/src/main/java/com/simprints/clientapi/extensions/ErrorResponse.ext.kt), and finally returning the correct error code [in here](/clientapi/src/main/java/com/simprints/clientapi/activities/libsimprints/ErrorResponse.ext.kt).

After all those million steps you should be able to return an error all the way from your modality to the calling app. Congratulations, have a cookie!

# Events that need changes

For data purposes, we need to know what is happening with the user and what type of errors they are getting. For that we send events to our backend when some error happen. After you created all the errors, in case you don't want to send the backend a generic UnknownError, you need to change some events to accommodate them. They are:
- [ApiErrorCallback](/id/src/main/java/com/simprints/id/data/db/session/remote/events/callback/ApiErrorCallback.kt)
- [ErrorCallbackEvent](/id/src/main/java/com/simprints/id/data/db/session/domain/models/events/callback/ErrorCallbackEvent.kt)

Even if you are going to send a generic UnknownError (not advised), you need to change those classes to map correctly.

# LibSimprints error codes

You might have noticed that in the last step you need to map the ErrorResponse to some `LibSimprints` error code. If you want to create a new error code you need to add it to the [Constants file](https://github.com/Simprints/LibSimprints/blob/f0f77f611d7e7885a9f7b80dae7be2d20a259dff/src/main/java/com/simprints/libsimprints/Constants.java) or talk to someone responsible for it to do it for you. After that, and updating `LibSimprints` inside the project, you can map your new ErrorResponse to an error code.
