package com.simprints.id.testtools

import android.location.Location
import com.simprints.core.biometrics.FingerprintGeneratorUtils
import com.simprints.core.domain.face.FaceSample
import com.simprints.eventsystem.sampledata.SampleDefaults
import com.simprints.infra.enrolment.records.domain.models.Subject
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
    private const val PROVIDER = "flp"
    private const val LAT = 37.377166
    private const val LNG = -122.086966
    private const val ACCURACY = 3.0f


    fun buildFakeLocation() = Location(PROVIDER).apply {
        longitude = LNG
        latitude = LAT
        accuracy = ACCURACY
    }


}
