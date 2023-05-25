plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.infra.authstore"
}

dependencies {

    implementation(libs.firebase.auth)
    implementation(libs.kotlin.coroutinesPlayServices)

}
