Simprints ID's main module `id` communicates with its dynamic feature modules (`clientapi`, `fingerprint` and `face`) indirectly via Orchestrator, by sending requests and receiving responses in the form of `moduleapi` interfaces through different steps.

The solution for configuration is to add a step prior to that of actually performing the action, which will be a callout to the modalities, where they will run their own configuration and then return a response to the orchestrator, allowing it to carry on with its remaining steps if the configuration succeeds.

# Step
As mentioned above, a new step will be added to the orchestrator, and this step will call the modalities' orchestrators to allow them to configure themselves. It will be triggered before any action, in order to make sure the modalities are properly configured. The step is run before all modalities, and in all callouts, for example in [Enrol](/id/src/main/java/com/simprints/id/orchestrator/modality/ModalityFlowEnrol.kt#43).

# Request
The request will be sent as `moduleapi` interfaces that will need to be created, which will be called [IFingerprintConfigurationRequest](/moduleapi/src/main/java/com/simprints/moduleapi/fingerprint/requests/IFingerprintConfigurationRequest.kt) and [IFaceConfigurationRequest](/moduleapi/src/main/java/com/simprints/moduleapi/face/requests/IFaceConfigurationRequest.kt).

# Configuration
The configuration of the modalities will take place in their respective modules, and they will be responsible for returning the response to `id`.

# Response
The response, just like the request, will be received as `moduleapi` interfaces, which are called [IFingerprintConfigurationResponse](/moduleapi/src/main/java/com/simprints/moduleapi/fingerprint/responses/IFingerprintConfigurationResponse.kt) and [IFaceConfigurationResponse](/moduleapi/src/main/java/com/simprints/moduleapi/face/responses/IFaceConfigurationResponse.kt) if the configuration is successful. Since the response is only to determine whether the modality configuration was successful, the response will not have any parameters.

As for the error case, an IFingerprintErrorResponse or an IFaceErrorResponse will be returned, with a new IFingerprintErrorReason, which will be called [FINGERPRINT_CONFIGURATION_ERROR](/moduleapi/src/main/java/com/simprints/moduleapi/fingerprint/responses/IFingerprintErrorResponse.kt#10). For face, we will use the 3 new error reasons that are being introduced as part of the work on the camera events: [FACE_LICENSE_MISSING, FACE_LICENSE_INVALID, and FACE_CONFIGURATION_ERROR](/moduleapi/src/main/java/com/simprints/moduleapi/face/responses/IFaceErrorResponse.kt).

Since we're adding the CONFIGURATION_ERROR for eventual fingerprint configuration errors, it'll be reflected in the `id` module as FINGERPRINT_CONFIGURATION_ERROR, which will need to have an [error callback event](/id/src/main/java/com/simprints/id/data/db/session/domain/models/events/callback/ErrorCallbackEvent.kt) created in the cloud with the same name.
