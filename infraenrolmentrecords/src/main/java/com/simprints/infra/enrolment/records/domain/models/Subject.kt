package com.simprints.infra.enrolment.records.domain.models

import android.os.Parcelable
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.config.domain.models.GeneralConfiguration
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Subject(
    val subjectId: String,
    val projectId: String,
    val attendantId: String,
    val moduleId: String,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    var fingerprintSamples: List<FingerprintSample> = emptyList(),
    var faceSamples: List<FaceSample> = emptyList(),

    @Deprecated("See SubjectToEventDbMigrationManagerImpl doc")
    val toSync: Boolean = false
) : Parcelable
