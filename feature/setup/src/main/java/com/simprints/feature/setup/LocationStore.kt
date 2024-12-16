package com.simprints.feature.setup

interface LocationStore {
    fun collectLocationInBackground()

    fun cancelLocationCollection()
}
