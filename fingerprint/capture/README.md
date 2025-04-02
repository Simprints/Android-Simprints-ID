# Fingerprint capture

This is how the fingerprint module works internally to capture the fingerprints, extract the templates, and return the results to
SimprintsID.

## FingerprintCaptureFragment

This fragment is the entry point into the fingepringt capture flow and it is responsible for:

- Launching scanner connection sub-flow
- Leading user over fingerprint capture step by step
- Saving captured fingerprint images if necessary
- Finishing the flow and reporting the capture results to the calling module
