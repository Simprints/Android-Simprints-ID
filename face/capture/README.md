# Capture Flow

This is how the face module works internally to capture the faces, extract the templates, and return the results to SimprintsID.

## FaceCaptureControllerFragment

The shared fragment (and its ViewModel) is responsible for:

- Checking that the correct parameters were sent from calling module for a correct capture
- Start the internal Navigation Graph
- Managing the next steps of the navigation by using events and the Navigation Graph
- Saving the captures on disk once the flow is finished correctly
- Finish the flow correctly and return the correct result to calling module

## When the flow finishes correctly

The FaceCaptureViewModel receives all captures and saves the valid ones on disk.

## PreparationFragment

It is a simple fragment where the user sees a preparation on how to capture a good face image. Its only responsibility is to show the
message and start the LiveFeedbackFragment.

## LiveFeedbackFragment

The responsibility of this fragment (and its ViewModel) is to make sure the user receives correct information on the state of the face on
the screen, i.e. if the face is valid or not and how to fix it.
This fragment is also responsible to start the camera and handle the frames being passed downstream from our camera library.

The FaceCaptureViewModel has a channel that starts receiving frames when the camera starts. The LiveFeedbackFragment subscribe to that
channel and process each frame.

If the face is valid, the fragment will prompt the user to start a capture and at that time will start saving each new frame as a capture.
After the number of required captures is done, the fragment finishes and goes to the ConfirmationFragment.

During the live feedback process, while the user is trying to center the face on the frame, the ViewModel will keep any good image as a
fallback image. This is to make sure that the user always has at least one good image, even if they move the phone too fast during the
capture process.

## ConfirmationFragment

If all goes well, the user comes to a screen that congratulates them on taking a good photo and ask them to continue their flow.
If the previewed captures are not satisfactory, user can return to the LiveFeedbackFragment and try again.
