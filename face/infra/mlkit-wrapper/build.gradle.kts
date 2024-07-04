plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.feature.facenetwrapper"
}

dependencies {
    implementation(project(":face:infra:face-bio-sdk"))
    implementation (libs.face.detection)


    // TensorFlow Lite dependencies
    implementation (libs.tensorflow.lite)
    implementation (libs.tensorflow.lite.gpu)
    implementation (libs.tensorflow.lite.gpu.api)
    implementation (libs.tensorflow.lite.support)


}
