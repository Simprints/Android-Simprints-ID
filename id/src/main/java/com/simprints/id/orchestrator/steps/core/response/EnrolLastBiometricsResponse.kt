package com.simprints.id.orchestrator.steps.core.response

import kotlinx.android.parcel.Parcelize

@Parcelize
class EnrolLastBiometricsResponse(val newSubjectId: String?) : CoreResponse(CoreResponseType.ENROL_LAST_BIOMETRICS)
