plugins {
    id("simprints.android.library")
    id("simprints.library.hilt")
}

android {
    namespace = "com.simprints.testtools"
}

dependencies {
    api(project(":infra:core"))

    api(libs.testing.androidX.core)
    api(libs.androidX.multidex)
    api(libs.androidX.appcompat)
    api(libs.testing.fragment)
    api(libs.testing.androidX.ext.junit)
    api(libs.testing.androidX.runner)
    api(libs.testing.androidX.navigation)

    api(libs.testing.navigation)
    api(libs.testing.hilt)
    api(libs.testing.live.data)
    api(libs.testing.work)

    api(libs.testing.junit)
    api(libs.testing.mockk.core)
    api(libs.testing.truth)
    api(libs.testing.robolectric.core)
    api(libs.testing.coroutines)

    api(libs.testing.espresso.core)
    api(libs.testing.espresso.intents)
    api(libs.testing.espresso.contrib)
    api(libs.testing.espresso.accessibility)
}
