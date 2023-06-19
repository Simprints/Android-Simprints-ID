package com.simprints.id.testtools

import com.simprints.core.biometrics.FingerprintGeneratorUtils
import com.simprints.core.domain.face.FaceSample
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import com.simprints.infra.events.sampledata.SampleDefaults
import java.util.Date
import java.util.UUID
import kotlin.random.Random

object TestData {

    val defaultSubject: Subject = Subject(
        UUID.randomUUID().toString(),
        SampleDefaults.DEFAULT_PROJECT_ID,
        SampleDefaults.DEFAULT_USER_ID,
        SampleDefaults.DEFAULT_MODULE_ID,
        Date(SampleDefaults.CREATED_AT),
        Date(SampleDefaults.CREATED_AT),
        listOf(FingerprintGeneratorUtils.generateRandomFingerprint()),
        listOf(FaceSample(Random.nextBytes(64), FACE_TEMPLATE_FORMAT)),
        false
    )

}
