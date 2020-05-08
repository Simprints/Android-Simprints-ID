# Tools

The main auxiliary tools for capture a good image is CameraTargetOverlay and FrameProcessor.

## CameraTargetOverlay

This is the overlay that is painted on top of the camera (in `LiveFeedbackFragment`) and specify the area that the SDK will look into to check for a face. Currently it has a size of 240dp that is used as the size for `DashedCircularProgress` in `fragment_live_feedback.xml`. There was no other way of setting that size and synchronizing between both components and `FrameProcessor`, that is why it is inside this class. The overlay is drawn only once, when `drawSemiTransparentTarget` or `drawWhiteTarget` is called.

## FrameProcessor

The most important piece of code, this class:
- create a rotated rectangle based on the current orientation of the camera
- resize the new rectangle if the image on the screen has some crop in it
- create a new box based on the new rectangle
- crop the frame using just the new rectangle (using LibYuv)

All those steps are necessary because cameras on Android are usually rotated or even flipped (front face camera). Also, what is shown on the screen and the real frame are not the same image, sometimes some cropping might be occuring (e.g. the phone has 16:9 aspect ratio but the photo is 16:10).

Note that this class uses `CameraTargetOverlay.rectForPlane`, which internally uses `percentFromTop`. If the overlay is changed (or a new one is used), the FrameProcessor might need to be adjusted. This was done because there is no way of knowing where the overlay is on the screen by other means.
