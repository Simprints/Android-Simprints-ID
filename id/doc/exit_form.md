The ExitForm is subject to the SimprintsID Request/Response as all other parts. So, to understand better how the flow works, read its [document](spid_request_response.md)

# Exit Form flow for SimprintsID

When the user is using the app, sometimes something can happen and the user might want to exit the app. For those cases, SimprintsID uses a concept of an Exit Form, a form that asks the user some questions to understand why they tried to finish the flow early (i.e. without completing it).

SimprintsID can show the Exit Form for the users in different places.

- If the user declines consent. That exit form will show an exit form based on the first modality that should appear for the user (right now, fingerprint or face). The classes that handle those cases are [FaceExitFormActivity.kt](/id/src/main/java/com/simprints/id/activities/faceexitform/FaceExitFormActivity.kt) and [FingerprintExitFormActivity.kt](/id/src/main/java/com/simprints/id/activities/fingerprintexitform/FingerprintExitFormActivity.kt).

- If the user presses back during biometric capture. Each modality handles the user pressing the back button inside their own module.
  - For fingerprint the class that handles it is [RefusalActivity.kt](/fingerprint/src/main/java/com/simprints/fingerprint/activities/refusal/RefusalActivity.kt)
  - For face the class that handles it is [ExitFormFragment.kt](/face/src/main/java/com/simprints/face/exitform/ExitFormFragment.kt)

Since each modality can have different options, the exit forms are copied to each modality instead of having one that sits in `id` and tries to handle all the options. We are aware of the cost of maintaining different versions of the exit forms but that gives us more freedom to implement in our own way inside each modality (activity vs fragment, for example).

The [IFaceExitFormResponse.kt](/moduleapi/src/main/java/com/simprints/moduleapi/face/responses/IFaceExitFormResponse.kt) has 2 attributes, a reason and an extra. The reason is the mapping from the option that the user selected and the extra is the text typed by the user. The reason need to be mapped to an `IFaceExitReason` because that is the interface that `moduleapi` understands.

Again, the Orchestrator maps the Exit Form from each modality to an AppRefusalFormResponse that is a standardized way for the calling app.

![exit_form_flow.png](exit_form_flow.png)

# Hawkeye event - Refusal Event

When the modality wants to send an event to Hawkeye it uses the [RefusalEvent.kt](/id/src/main/java/com/simprints/id/data/db/session/domain/models/events/RefusalEvent.kt). Currently each modality has its own RefusalEvent that is mapped to the one in `id` and that one is sent to Hawkeye. If a new option is needed, it needs to be added to that file and to Hawkeye.

