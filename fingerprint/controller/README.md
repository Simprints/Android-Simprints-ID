# Fingerprint Modality Module

This is the main module for the fingerprint modality in Simprints ID.
Its functionality can be invoked via calling out with a
`FingerprintRequest` from the `moduleapi` library via an `Intent` to the
[`OrchestratorActivity`](./src/main/java/com/simprints/fingerprint/activities/orchestrator/OrchestratorActivity.kt),
and it will return a `FingerprintResponse` upon completion. It is a
dynamic feature module installed upon project log-in as required.

### Further READMEs

The main domain sub-packages are:

- [Orchestrator](./src/main/java/com/simprints/fingerprint/orchestrator/README.md)
  \- which handles requests to the module and calling out to other
  activities within the module.
- [Scanner](./src/main/java/com/simprints/fingerprint/scanner/README.md)
  \- which handles high-level interfacing with the `fingerprintscanner`
  module for using a Vero fingerprint scanner.

The main activities are:

- [Connect Scanner Activity](./src/main/java/com/simprints/fingerprint/activities/connect/README.md)
  \- which guides the user through the process of connecting to the
  Vero.
- [Collect Fingerprints Activity](./src/main/java/com/simprints/fingerprint/activities/collect/README.md)
  \- in which the user is guided through the procedure of collecting
  fingerprint scans.
- [Matching Activity](./src/main/java/com/simprints/fingerprint/activities/matching/README.md)
  \- in which fingerprint matching occurs.

The satellite libraries of the fingerprint modality that are used in
this module are:

- [`fingerprintscanner`](../fingerprintscanner/README.md) - which
  handles low-level communication with the fingerprint scanner and tucks
  it behind a `Scanner` class abstraction.
- [`fingerprintscannermock`](../fingerprintscannermock/README.md) -
  which is a utility package providing mocking and simulation options
  for the fingerprint scanner for testing and debugging.
- [`fingerprintmatcher`](../fingerprintmatcher/README.md) - which houses
  the algorithms for matching fingerprints and provides an interface for
  their use.
