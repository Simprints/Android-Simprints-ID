plugins {
    id("simprints.feature")
}

android {
    namespace = "com.simprints.feature.chatbot"
}

dependencies {
    implementation(project(":infra:ai-chat"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:logging-persistent"))
    implementation(project(":infra:network"))

    implementation(libs.support.material)

    implementation(libs.markwon.core)
    implementation(libs.markwon.tables)
    implementation(libs.markwon.strikethrough)
    implementation(libs.markwon.linkify)
}
