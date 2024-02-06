package com.simprints.infra.orchestration.data.results

import android.os.Bundle
import java.io.Serializable

data class AppResult(
    val resultCode: Int,
    val extras: Bundle,
) : Serializable
