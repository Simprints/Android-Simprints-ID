# Exit Form flow

To understand how the Exit Form should work, take a look at the [Confluence page](https://simprints.atlassian.net/wiki/spaces/CS/pages/1112702991/Camera+Exit+Form).

For more information about the Exit Form flow as a whole, check the platform Exit Form README.

The layout used inside the face module for the Exit Form is the same one used in the [FaceExitFormActivity.kt](../../../../../../../../id/src/main/java/com/simprints/id/activities/faceexitform/FaceExitFormActivity.kt) with some small modifications because the scrolling was not working correctly.

The ExitForm is another fragment that is added to the Capture navigation graph. When the user presses back, the fragment is added to the backstack on top of the current fragment. To go back where the user was (i.e. when user presses "Capture Face" button), the ExitForm just needs to pop itself from the back stack.

The reason (`RefusalAnswer`) used by the [FaceExitFormResponse](../data/moduleapi/face/responses/FaceExitFormResponse.kt) is the same one used by the [RefusalEvent](../controllers/core/events/model/RefusalEvent.kt). This was done because it makes it easier to maintain the same set of options in just one place. After all, it will be mapped to an `IFaceExitReason` when returning to SimprintsID.

The main back button flow is handled at the activity level ([FaceCaptureActivity](../capture/FaceCaptureActivity.kt)). Because of the usage of Navigation Components, it should be in a place where the component knows about the different fragments and which context the user is in currently. The logic for where to go after the back button is pressed sits inside [FaceCaptureViewModel](../capture/FaceCaptureViewModel.kt).

Since the activity might not know or need to handle all contexts, there is a possibility that the context is `null`. In that case the control of handling the back button is on the activity or fragment level. One of those `null` contexts is when the user is inside the `ExitFormFragment`. This fragment needs to handle the back button itself because it needs information from the form to show the correct toast for the user.

After the user presses the Submit button, an event is sent to SimprintsID to save on Hawkeye. The event is a [RefusalEvent](../controllers/core/events/model/RefusalEvent.kt) that will be mapped from the face modality to a core `RefusalEvent`.

The response returned to SimprintsID should be a `FaceExitFormResponse` instead of a successful response. That is done in the [FaceCaptureViewModel](../capture/FaceCaptureViewModel.kt).
