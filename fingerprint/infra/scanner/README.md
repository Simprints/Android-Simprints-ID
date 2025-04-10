# Fingerprint Scanner

This self-contained module is for communicating with Simprints's Vero fingerprint scanner over Bluetooth.
It is split into two subpackages depending on which generation of Vero is being used:

- [Vero 1 (v1)](./src/main/java/com/simprints/fingerprint/infra/scanner/v1/README.md)
- [Vero 2 (v2)](./src/main/java/com/simprints/fingerprint/infra/scanner/v2/README.md)

### Bluetooth Component Abstractions

It's important to use the[Component abstractions](src/main/java/com/simprints/fingerprint/infra/scanner/component/bluetooth) for connecting
via Bluetooth and acquiring a Bluetooth socket so that mocking and replacing of the Bluetooth adapter can occur easily.
