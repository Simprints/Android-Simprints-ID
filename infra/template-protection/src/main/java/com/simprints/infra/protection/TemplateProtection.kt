package com.simprints.infra.protection

import com.simprints.biometrics.polyprotect.AuxData
import com.simprints.biometrics.polyprotect.PolyProtect
import com.simprints.infra.protection.database.AuxDataDao
import com.simprints.infra.protection.database.DbAuxData
import javax.inject.Inject

class TemplateProtection @Inject internal constructor(
    private val auxDao: AuxDataDao,
    private val polyProtect: PolyProtect,
) {
    fun createAuxData(): AuxData = polyProtect.generateAuxData()

    suspend fun getAuxData(subjectId: String): AuxData? = auxDao.get(subjectId)?.let { (_, exponents, coefficients) ->
        AuxData(
            exponents = exponents.split(SEPARATOR).map { it.toInt() }.toIntArray(),
            coefficients = coefficients.split(SEPARATOR).map { it.toInt() }.toIntArray(),
        )
    }

    suspend fun saveAuxData(
        subjectId: String,
        auxData: AuxData,
    ) = auxDao.save(
        DbAuxData(
            subjectId = subjectId,
            exponents = auxData.exponents.joinToString(SEPARATOR),
            coefficients = auxData.coefficients.joinToString(SEPARATOR),
        ),
    )

    suspend fun cleatAuxData() = auxDao.deleteAll()

    fun encodeTemplate(
        template: ByteArray,
        auxData: AuxData,
    ): ByteArray = polyProtect.transformTemplate(template, auxData)

    companion object {
        private const val SEPARATOR = ";"
    }
}
