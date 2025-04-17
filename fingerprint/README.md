# Fingerprint Modality Module

This is the main module for the fingerprint modality in Simprints ID.

### Further READMEs

The main domain sub-packages are:

- [Connection](connect/README.md) \- which handles connection to the scanner.
- [Capture](capture/README.md) \- which handles fingerprint capture UI flow.
- [Scanner](infra/scanner/README.md)
  \- which handles high-level interfacing with the `fingerprint:infra:scanner` module for using a Vero fingerprint scanner.
- Various biometric SDK related modules

The satellite libraries of the fingerprint modality that are used in this module are:

- [`fingerprint:infra:scanner`](infra/scanner/README.md)
  \- which handles low-level communication with the fingerprint scanner and tucks it behind a `Scanner` class abstraction.
- [`fingerprint:infra:scannermock`](infra/scannermock/README.md)
  \- which is a utility package providing mocking and simulation options for the fingerprint scanner for testing and debugging.
