# base Face biometric sdk module 

This is a base module that allows users to integrate new Face SDKs into SID's Face module.
To use this base module, simply add the base module to your new SDK module's dependencies 
and then let your new module provide and implement the operations in 

1. [`FaceBioSdkInitializer`](src/main/java/com/simprints/infra/face.basebiosdk/initialization/FaceBioSdkInitializer.kt)
2. [`FaceDetector`](src/main/java/com/simprints/infra/face.basebiosdk/detection/FaceDetector.kt)
3. [`FaceMatcher`](src/main/java/com/simprints/infra/face.basebiosdk/matching/FaceMatcher.kt)

Here is an example on how to provide the there implemented classes
```kotlin
    @Binds
    abstract fun provideSdkInitializer(impl: YourBioSdkInitializer): FaceBioSdkInitializer
    @Binds
    abstract fun provideFaceDetector(impl: YourBioSdkFaceDetector): FaceDetector
    @Binds
    abstract fun provideFaceMatcher(impl: YourBioSdkFaceMatcher): FaceMatcher
```
Once providing the classes the Face module will detect it and use them.
