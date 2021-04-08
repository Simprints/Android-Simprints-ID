package com.simprints.id.orchestrator.steps.core.response

import kotlinx.parcelize.Parcelize

@Parcelize
class EnrolLastBiometricsResponse(val newSubjectId: String?) : CoreResponse(CoreResponseType.ENROL_LAST_BIOMETRICS)
