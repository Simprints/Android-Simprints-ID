package com.simprints.id.data.db.person.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.domain.Person

@Keep
data class ApiPostPerson(val id: String,
                         val userId: String,
                         val moduleId: String,
                         val fingerprints: List<ApiFingerprintSample>? = null,
                         var faces: List<ApiFaceSample>? = null)

fun Person.fromDomainToPostApi(): ApiPostPerson =
    ApiPostPerson(
        id = patientId,
        userId = userId,
        moduleId = moduleId,
        fingerprints = fingerprintSamples.map(FingerprintSample::fromDomainToApi),
        faces = faceSamples.map(FaceSample::fromDomainToApi)
    )
