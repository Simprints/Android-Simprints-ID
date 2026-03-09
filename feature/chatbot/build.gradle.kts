plugins {
    id("simprints.feature")
}

android {
    namespace = "com.simprints.feature.chatbot"
}

dependencies {
    implementation(project(":infra:ai-chat"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:network"))

    implementation(libs.support.material)
}
