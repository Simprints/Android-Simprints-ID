package com.simprints.fingerprint.controllers.core.repository

import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import io.reactivex.Single
import java.io.Serializable

interface FingerprintDbManager {

    fun loadPeople(query: Serializable): Single<List<FingerprintIdentity>>
}
