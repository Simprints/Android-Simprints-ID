package com.simprints.fingerprint.controllers.core.repository

import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import com.simprints.fingerprint.data.domain.fingerprint.fromModuleApiToDomain
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.Serializable
import javax.inject.Inject

/**
 * This class provides an implementation of FingerprintDBManager, using the fingerprint datasource
 * to load fingerprint ids
 *
 * @property enrolmentRecordManager  for retrieving subjects
 */
class FingerprintDbManagerImpl @Inject constructor(
    private val enrolmentRecordManager: EnrolmentRecordManager
) : FingerprintDbManager {


    /**
     * This method loads subjects with fingerprint id, using the provided query
     *
     * @param query  the query condition for which subjects to be loaded, see [SubjectQuery]
     * @return  the sequence of fingerprint ids that were retrieved
     */
    override suspend fun loadSubjects(query: Serializable): Flow<FingerprintIdentity> =
        enrolmentRecordManager
            .loadFingerprintIdentities(query)
            .map {
                FingerprintIdentity(
                    it.patientId,
                    it.fingerprints.map { fingerprint -> fingerprint.fromCoreToDomain() })
            }
}

fun FingerprintSample.fromCoreToDomain() =
    Fingerprint(fingerIdentifier.fromModuleApiToDomain(), template)
