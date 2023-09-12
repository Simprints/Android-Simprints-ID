package com.simprints.feature.orchestrator.steps

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import kotlinx.parcelize.Parcelize
import kotlin.reflect.KClass

@Parcelize
data class Step(
    @IdRes val navigationActionId: Int,
    @IdRes val destinationId: Int,
    val payload: Bundle,
    val resultType: Class<out Parcelable>,
    var status: StepStatus = StepStatus.NOT_STARTED,
    var result: Parcelable? = null,
) : Parcelable

enum class StepStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
}
