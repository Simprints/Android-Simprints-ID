package com.simprints.fingerprint.commontesttools.data

import java.io.Serializable

class TestDbQuery(val projectId: String? = null,
                  val patientId: String? = null,
                  val userId: String? = null,
                  val moduleId: String? = null,
                  val toSync: Boolean? = null) : Serializable

const val DEFAULT_PROJECT_ID = "TESTzbq8ZBOs1LLOOH6p"
