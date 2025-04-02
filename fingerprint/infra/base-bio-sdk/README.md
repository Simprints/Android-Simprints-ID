# Base SDK

This is a base fingerprint SDK that provides three operations: init, capture fingerprint images/templates , and match images.
to implement this SDK, you need to implement the following interfaces:

`SdkInitializer` - initialize the SDK using the proper initialization params 

`FingerprintImageProvider` and `FingerprintTemplateProvider` - capture fingerprint images/templates

`FingerprintMatcher` - match fingerprint templates
