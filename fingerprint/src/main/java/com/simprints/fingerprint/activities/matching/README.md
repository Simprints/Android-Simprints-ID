# Matching Activity

A lightweight activity in which the fingerprint matching occurs. The
behaviour of the UI depends on whether an identification or a
verification is occurring:

- Identification - The progress bar updates gradually as candidates are
  loaded from the database and the matching stages progress. Test
  prompts are shown to the user displaying statistics about matching
  such as how many candidates were loaded and how many successful
  matches were found. The screen pauses for a brief time once complete
  to allow the user to read the text.
- Verification - The progress bar shoots to 100% very quickly as the
  matching is complete. No text updates are shown and the screen is only
  visible very briefly while the matching occurs.

These differences are manifest in
[`IdentificationTask`](./IdentificationTask.kt) and
[`VerificationTask`](./VerificationTask.kt) which both implement a
common interface [`MatchTask`](./MatchTask.kt) which is used by the
[view model](./MatchingViewModel.kt).

The actual matching is performed by the `fingerprintmatcher` module in
the background.
