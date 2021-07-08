object Dependencies {

    const val kotlin_version = "1.5.10"
    const val androidx_navigation_version = "2.3.0-alpha04"

    private const val kotlin_coroutine_version = "1.3.1"
    private const val app_compat_version = "1.2.0"
    private const val androidx_version = "1.3.0-beta01"
    private const val espresso_version = "3.2.0-alpha04"
    private const val androidx_lifecycle_version = "2.2.0"
    private const val androidx_room_version = "2.3.0"
    private const val androidx_camerax_version = "1.0.0-beta01"
    private const val dagger_version = "2.22"
    private const val work_version = "2.4.0"
    private const val playservices_version = "16.0.0"
    private const val retrofit_version = "2.7.1"
    private const val okttp_version = "4.2.2"
    private const val fuzzywuzzy_version = "1.2.0"
    private const val jackson_version = "2.11.1"
    private const val koin_version = "2.2.2"
    private const val rootbeer_version = "0.0.7"
    private const val mockito_version = "3.2.4"
    private const val robolectric_version = "4.3.1"
    private const val commons_io_version = "2.6"
    private const val kronos_version = "0.0.1-alpha09"
    private const val fragment_version = "1.3.3"

    object Kotlin {
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"
        const val anko = "org.jetbrains.anko:anko:0.10.8"
        const val coroutines_android =
            "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlin_coroutine_version"
        const val coroutines_play_services =
            "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$kotlin_coroutine_version"
        const val serialization_json = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1"
    }

    object AndroidX {
        const val core = "androidx.core:core-ktx:1.0.2"
        const val multidex = "androidx.multidex:multidex:2.0.1"
        const val appcompat = "androidx.appcompat:appcompat:$app_compat_version"
        const val legacy = "androidx.legacy:legacy-support-v4:1.0.0"

        object Room {
            const val core = "androidx.room:room-runtime:$androidx_room_version"
            const val compiler = "androidx.room:room-compiler:$androidx_room_version"
            const val ktx = "androidx.room:room-ktx:$androidx_room_version"
        }

        object Lifecycle {
            const val livedata = "androidx.lifecycle:lifecycle-livedata:$androidx_lifecycle_version"
            const val compiler = "androidx.lifecycle:lifecycle-compiler:$androidx_lifecycle_version"
            const val viewmodel =
                "androidx.lifecycle:lifecycle-viewmodel-ktx:$androidx_lifecycle_version"
            const val scope = "androidx.lifecycle:lifecycle-runtime-ktx:$androidx_lifecycle_version"
            const val ktx = "androidx.lifecycle:lifecycle-livedata-ktx:$androidx_lifecycle_version"
            const val ext = "androidx.lifecycle:lifecycle-extensions:$androidx_lifecycle_version"
            const val java8 =
                "androidx.lifecycle:lifecycle-common-java8:$androidx_lifecycle_version"
        }

        object UI {
            const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.0-beta4"
            const val cardview = "androidx.cardview:cardview:1.0.0"
            const val fragment = "androidx.fragment:fragment:$fragment_version"
            const val preference = "androidx.preference:preference:1.1.1"
            const val viewpager2 = "androidx.viewpager2:viewpager2:1.0.0"
        }

        const val security = "androidx.security:security-crypto:1.0.0-rc04"

        object CameraX {
            const val core = "androidx.camera:camera-core:$androidx_camerax_version"
            const val camera2 = "androidx.camera:camera-camera2:$androidx_camerax_version"
            const val lifecycle = "androidx.camera:camera-lifecycle:$androidx_camerax_version"
            const val view = "androidx.camera:camera-view:1.0.0-alpha08"
        }

        object Navigation {
            const val fragment =
                "androidx.navigation:navigation-fragment-ktx:$androidx_navigation_version"
            const val ui = "androidx.navigation:navigation-ui-ktx:$androidx_navigation_version"
            const val dynamicfeatures =
                "androidx.navigation:navigation-dynamic-features-fragment:$androidx_navigation_version"
        }

        object Annotation {
            const val annotation = "androidx.annotation:annotation:1.2.0"
        }

        const val sqlite = "androidx.sqlite:sqlite:2.1.0"

    }

    object Dagger {
        const val core = "com.google.dagger:dagger:$dagger_version"
        const val compiler = "com.google.dagger:dagger-compiler:$dagger_version"
        const val javax = "javax.inject:javax.inject:1@jar"
    }

    object SqlCipher {
        const val core = "net.zetetic:android-database-sqlcipher:4.4.0"
    }

    object Support {
        const val material = "com.google.android.material:material:1.1.0-alpha03"
    }

    object WorkManager {
        const val work = "androidx.work:work-runtime-ktx:$work_version"
    }

    object PlayServices {
        const val location = "com.google.android.gms:play-services-location:$playservices_version"
        const val places = "com.google.android.gms:play-services-places:$playservices_version"
        const val safetynet = "com.google.android.gms:play-services-safetynet:$playservices_version"
    }

    object Playcore {
        const val core = "com.google.android.play:core:1.8.0"
        const val core_ktx = "com.google.android.play:core-ktx:1.8.1"
    }

    object Firebase {
        const val analytics = "com.google.firebase:firebase-analytics-ktx:18.0.3"
        const val auth = "com.google.firebase:firebase-auth:19.2.0"
        const val config = "com.google.firebase:firebase-config:19.1.3"
        const val perf = "com.google.firebase:firebase-perf:19.0.5"
        const val storage = "com.google.firebase:firebase-storage-ktx:19.1.1"
        const val mlkit = "com.google.firebase:firebase-ml-vision:24.0.1"
        const val mlkit_barcode = "com.google.firebase:firebase-ml-vision-barcode-model:16.0.2"
    }

    object Retrofit {
        const val core = "com.squareup.retrofit2:retrofit:$retrofit_version"
        const val adapter = "com.squareup.retrofit2:adapter-rxjava2:$retrofit_version"
        const val jackson = "com.squareup.retrofit2:converter-jackson:$retrofit_version"
        const val converterScalars = "com.squareup.retrofit2:converter-scalars:$retrofit_version"
        const val logging = "com.squareup.okhttp3:logging-interceptor:$okttp_version"
        const val okhttp = "com.squareup.okhttp3:okhttp:$okttp_version"
    }

    object Jackson {
        const val core = "com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version"
    }

    object RxJava2 {
        const val core = "io.reactivex.rxjava2:rxjava:2.2.6"
        const val android = "io.reactivex.rxjava2:rxandroid:2.1.0"
        const val kotlin = "io.reactivex.rxjava2:rxkotlin:2.3.0"
        const val permissions = "com.github.tbruyelle:rxpermissions:2.x.v0.9.3"
        const val location = "pl.charmas.android:android-reactive-location2:2.1@aar"
        const val tasks = "io.ashdavies.rx:rx-tasks:2.2.0"
    }

    // Fuzzywuzzy (fuzzy search)
    object Fuzzywuzzy {
        const val core = "me.xdrop:fuzzywuzzy:$fuzzywuzzy_version"
    }

    // Koin Service Location
    object Koin {
        const val core = "org.koin:koin-core:$koin_version"
        const val core_ext = "org.koin:koin-core-ext:$koin_version"
        const val android = "org.koin:koin-android:$koin_version"
        const val viewmodel = "org.koin:koin-android-viewmodel:$koin_version"
    }

    // RootBeer (root detection)
    object Rootbeer {
        const val core = "com.scottyab:rootbeer-lib:$rootbeer_version"
    }

    const val cameraView = "com.otaliastudios:cameraview:2.7.0"
    const val circleImageView = "de.hdodenhof:circleimageview:3.0.1"

    // Testing
    object Testing {
        const val junit = "junit:junit:4.13"
        const val kotlin = "io.kotlintest:kotlintest-runner-junit4:3.4.2"

        object Robolectric {
            const val core = "org.robolectric:robolectric:$robolectric_version"
            const val multidex = "org.robolectric:multidex:3.4.2"
            const val annotation = "org.robolectric:annotations:$robolectric_version"
        }

        const val truth = "com.google.truth:truth:0.42"

        object Mockito {
            const val core = "org.mockito:mockito-core:$mockito_version"
            const val inline =
                "org.mockito:mockito-inline:$mockito_version" //Required to mock final classes
            const val android =
                "org.mockito:mockito-android:$mockito_version" //Required to use Mock in AndroidTests
            const val kotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0"
        }

        object Mockk {
            const val core = "io.mockk:mockk:1.9.2"
            const val android = "io.mockk:mockk-android:1.9.2"
        }

        const val mockwebserver = "com.squareup.okhttp3:mockwebserver:$okttp_version"

        object Espresso {
            const val core = "androidx.test.espresso:espresso-core:$espresso_version"
            const val intents = "androidx.test.espresso:espresso-intents:$espresso_version"
            const val contrib = "androidx.test.espresso:espresso-contrib:$espresso_version"
            const val barista =
                "com.schibsted.spain:barista:2.7.1" //Used to grant permissions in AndroidTests
            const val idling = "androidx.test.espresso:espresso-idling-resource:$espresso_version"

        }

        const val dagger = "com.github.fabioCollini.daggermock:daggermock:0.8.4"
        const val dagger_kotlin = "com.github.fabioCollini.daggermock:daggermock-kotlin:0.8.4"
        const val work = "androidx.work:work-testing:$work_version"
        const val retrofit = "com.squareup.retrofit2:retrofit-mock:$retrofit_version"

        //https://developer.android.com/jetpack/androidx/releases/test
        object AndroidX {
            const val monitor = "androidx.test:monitor:$androidx_version"
            const val ext_junit = "androidx.test.ext:junit:1.1.2-beta01"
            const val core = "androidx.test:core:$androidx_version"
            const val core_testing = "android.arch.core:core-testing:$androidx_version"
            const val orchestrator = "androidx.test:orchestrator:$androidx_version"
            const val runner = "androidx.test:runner:$androidx_version"
            const val rules = "androidx.test:rules:$androidx_version"
            const val room = "androidx.room:room-testing:$androidx_room_version"
            const val navigation =
                "androidx.navigation:navigation-testing:$androidx_navigation_version"
        }

        const val rx2_idler = "com.squareup.rx.idler:rx2-idler:0.9.1"
        const val coroutines_test =
            "org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlin_coroutine_version"
        const val koin = "org.koin:koin-test:$koin_version"
        const val live_data = "com.jraska.livedata:testing-ktx:1.1.0"
        const val kappuccino = "br.com.concretesolutions:kappuccino:1.2.1"
        const val awaitility = "org.awaitility:awaitility-kotlin:4.0.1"

        // Navigation
        const val fragment_testing = "androidx.fragment:fragment-testing:$fragment_version"
        const val navigation_testing =
            "androidx.navigation:navigation-testing:$androidx_navigation_version"
    }

    // For Tee-d stream
    object CommonsIO {
        const val commons_io = "commons-io:commons-io:$commons_io_version"
    }

    // For NTP time
    object Kronos {
        const val kronos = "com.lyft.kronos:kronos-android:$kronos_version"
    }

    const val libsimprints = "com.simprints:libsimprints:2021.1.0"

}



