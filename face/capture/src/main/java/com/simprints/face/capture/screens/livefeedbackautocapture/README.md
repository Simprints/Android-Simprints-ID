# Face auto-capture

This `livefeedbackautocapture` package is similar to the manual capture [livefeedback](../livefeedback/README.md) one, except:
* Auto-capture has a preparation period from the moment `LiveFeedbackAutoCaptureFragment` becomes visible and for `LiveFeedbackAutoCaptureFragmentViewModel.AUTO_CAPTURE_VIEWFINDER_RESUME_DELAY_MS` amount of time. This helps prevent startling the user with a too early start of auto-capture - before the user would aim the viewfinder at a face.
* The auto-capture imaging progress starts when a qualifying face image appears in the viewfinder, and lasts for `LiveFeedbackAutoCaptureFragmentViewModel.AUTO_CAPTURE_IMAGING_DURATION_MS` amount of time.
* The number of images to accept is `LiveFeedbackAutoCaptureFragmentViewModel.samplesToKeep` - similarly to `LiveFeedbackFragmentViewModel.samplesToCapture`. The best quality sampled images are selected to keep.
* The kept sampled images and the fallback image are included in the capture events - unlike in `LiveFeedbackFragmentViewModel` where all conventionally captured and the fallback images are included.
* The capture events have an `isAutoCapture` flag value of `true`.

The reason for having this package as a near-copy of `livefeedback` is to keep both implementations independent but similar. The layout for `LiveFeedbackAutoCaptureFragment` is `fragment_live_feedback_auto_capture.xml`.
