package com.simprints.id.data.db.remote.models

import androidx.annotation.Keep

@Keep
data class ApiFace(val template: String = "",
                   val yaw: String = "",
                   val pitch: String = "")
