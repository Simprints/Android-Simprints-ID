package com.simprints.infra.protection.auxiliary

import com.simprints.infra.logging.Simber
import com.simprints.infra.realm.RealmWrapper
import com.simprints.infra.realm.exceptions.RealmUninitialisedException
import com.simprints.infra.realm.models.DbAuxData
import javax.inject.Inject

class AuxDataRepository @Inject internal constructor(
    private val realmWrapper: RealmWrapper,
    private val auxDataFactory: AuxDataFactory,
) {

    companion object {
        private const val SUBJECT_ID_FIELD = "subjectId"
    }

    suspend fun getOrCreateAuxData(subjectId: String): TemplateAuxData =
        getAuxData(subjectId) ?: createAuxData(subjectId)

    suspend fun createAuxData(subjectId: String): TemplateAuxData = auxDataFactory.createAuxData(
        subjectId = subjectId
    )

    suspend fun getAuxData(subjectId: String): TemplateAuxData? = try {
        realmWrapper.readRealm { realm ->
            realm.query(DbAuxData::class)
                .query("$SUBJECT_ID_FIELD == $0", subjectId)
                .first()
                .find()
                ?.fromDbToDomain()
        }
    } catch (e: Exception) {
        when (e) {
            is RealmUninitialisedException -> {}
            else -> Simber.e(e)
        }
        null
    }

    suspend fun saveAuxData(templateAuxData: TemplateAuxData) = try {
        realmWrapper.writeRealm { it.copyToRealm(templateAuxData.fromDomainToDb()) }
    } catch (e: Exception) {
        when (e) {
            is RealmUninitialisedException -> Unit
            else -> Simber.e(e)
        }
    }

}
