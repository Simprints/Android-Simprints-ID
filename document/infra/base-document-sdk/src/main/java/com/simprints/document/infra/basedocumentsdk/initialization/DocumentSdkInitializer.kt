package com.simprints.document.infra.basedocumentsdk.initialization

import android.app.Activity

fun interface DocumentSdkInitializer {

    fun init(
        activity: Activity,
    ): Boolean
}
