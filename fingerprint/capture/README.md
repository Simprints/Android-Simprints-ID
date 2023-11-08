# Fingerprint Modality Module

This is the main module for the fingerprint modality in Simprints ID.

### Further READMEs

The main domain sub-packages are:

- [Connection](../connect/README.md) \- which handles connetion to the scanner.
- [Scanner](../infra/scanner/README.md)
  \- which handles high-level interfacing with the `fingerprint:infra:scanner` module for using a Vero fingerprint scanner.

The satellite libraries of the fingerprint modality that are used in this module are:

- [`fingerprint:scanner`](../infra/scanner/README.md)
  \- which handles low-level communication with the fingerprint scanner and tucks it behind a `Scanner` class abstraction.
- [`fingerprint:scannermock`](../infra/scannermock/README.md)
  \- which is a utility package providing mocking and simulation options for the fingerprint scanner for testing and debugging.
- [`feature:module`](../../feature/matcher/README.md)
  \- which houses the algorithms for matching fingerprints and provides an interface for their use.
