package com.simprints.id.domain.sessionParameters

import com.simprints.id.domain.callout.CalloutAction

data class SessionParameters(val calloutAction: CalloutAction,
                             val apiKey: String?,
                             val projectId: String?,
                             val moduleId: String,
                             val userId: String,
                             val patientId: String,
                             val callingPackage: String,
                             val metadata: String,
                             val resultFormat: String)
