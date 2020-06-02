package com.simprints.id.data.db.session.remote.events.callout

import androidx.annotation.Keep

@Keep
class ApiEnrolmentLastBiometricsCallout(val projectId: String,
                                        val userId: String,
                                        val moduleId: String,
                                        val metadata: String?,
                                        val sessionId: String) : ApiCallout(ApiCalloutType.ENROLMENT_LAST_BIOMETRICS)
