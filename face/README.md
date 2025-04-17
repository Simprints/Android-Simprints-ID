# Face modality

The face modality was created as a way to have contactless biometrics as an option for SimprintsID.

## Flow overview

This first thing that the face capture needs to do is initialize the required SDK, loading the SDK library and making sure the SDK license
is in place and is valid.

To understand more how the capture flow works, check the capture [README](capture/README.md).

## Detection

The detection phase is when the app analyzes the (already cropped by `CropToTargetOverlayAnalyzer`) and returns a Face that it
found (or null if none).

Note that the Face returned also have a template already in it, making it a one step to find a face and extract the template for the app.
It was done that way because most SDKs tested returned a template when looking for a face. 

## RankOne

RankOne is the SDK of choice for face recognition - after we did some extensive testing with lots of other providers.
RankOne gives an SDK to copy to inside Simprints. It lives inside the respective infra module.

Current RankOne versions are 
 * 1.23 for legacy projects 
 * 3.1 for all new projects
