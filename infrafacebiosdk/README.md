# Face biometric sdk wrapper module 

This is a base module that allows users to integrate new Face SDKs into SID's Face module.
To use this base module, simply add the base module to your new SDK module's dependencies 
and then let your new module provide and implement the operations in 

1. [`FaceBioSdkInitializer`](./src/main/java/com/simprints/infra/facebiosdk/initialization/FaceBioSdkInitializer.kt)
2. [`FaceDetector`](./src/main/java/com/simprints/infra/facebiosdk/detection/FaceDetector.kt)
3. [`FaceMatcher`](./src/main/java/com/simprints/infra/facebiosdk/matching/FaceMatcher.kt)

Here is an example on how to provide the there implemented classes
    @Binds
    abstract fun provideSdkInitializer(impl: YourBioSdkInitializer): FaceBioSdkInitializer
    @Binds
    abstract fun provideFaceDetector(impl: YourBioSdkFaceDetector): FaceDetector
    @Binds
    abstract fun provideFaceMatcher(impl: YourBioSdkFaceMatcher): FaceMatcher
