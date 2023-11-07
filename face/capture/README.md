# Face modality

The face modality was created as a way to have contactless biometrics as an option for SimprintsID.

##  Important links

* [Confluence](https://simprints.atlassian.net/wiki/spaces/CS/overview)
* [Jira](https://simprints.atlassian.net/secure/RapidBoard.jspa?rapidView=19)

## Flow overview

Calling module start the face modality navigating with `FaceCaptureControllerFragmentArgs` as bundle.
This first thing that the face capture needs to do is initialize the required SDK, loading the SDK library and making sure the SDK license is in place and is valid.

To understand more how the capture flow works, check the capture [README](src/main/java/com/simprints/face/capture/README.md).

## Detection

The detection phase is when the app analyzes a PreviewFrame (already cropped by FrameProcessor) and returns a Face that it found (or null if none).
The methods on the Detectors are suspend functions because the process to get a Face can be onerous (you should use `withContext(Dispatchers.IO)`).
Note that the Face returned also have a template already in it, making it a one step to find a face and extract the template for the app.
It was done that way because most SDKs tested returned a template when looking for a face. Currently we only use the analyze method that receives a `PreviewFrame`,
the method to analyze a `Bitmap` is there for future necessity (it was used by the R&D team).

### MockFaceDetector

This is a mock detector that always return true and wait a bit (200ms) before returning a face with an empty template.

## RankOne

RankOne is the SDK of choice for face recognition - after we did some extensive testing with lots of other providers.
RankOne gives an SDK to copy to inside Simprints. It lives inside the respective infra module.

Current RankOne version = 1.23 (with OpenMP disabled)
