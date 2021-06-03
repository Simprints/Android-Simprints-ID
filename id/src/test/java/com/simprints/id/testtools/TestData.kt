package com.simprints.id.testtools

import com.simprints.core.biometrics.FingerprintGeneratorUtils
import com.simprints.core.domain.face.FaceSample
import com.simprints.eventsystem.sampledata.SampleDefaults
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.moduleapi.face.responses.entities.IFaceTemplateFormat
import java.util.*
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
        listOf(FaceSample(Random.nextBytes(64), IFaceTemplateFormat.RANK_ONE_1_23)),
        false
    )

}
