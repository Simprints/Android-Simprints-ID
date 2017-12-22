package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import com.simprints.id.domain.calloutValidation.CalloutType
import com.simprints.id.domain.calloutValidation.calloutParameter.mockInvalidCalloutParameter
import com.simprints.id.domain.calloutValidation.calloutParameter.mockTypeParameter
import com.simprints.id.domain.calloutValidation.calloutParameter.mockValidCalloutParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.testUtils.mock
import org.junit.Assert.assertEquals
import org.junit.Test


class PatientIdParameterTest {

    private val updateTypeParam = mockTypeParameter(CalloutType.UPDATE)
    private val verifyTypeParam = mockTypeParameter(CalloutType.VERIFY)
    private val identifyTypeParam = mockTypeParameter(CalloutType.IDENTIFY)
    private val registerTypeParam = mockTypeParameter(CalloutType.REGISTER)
    private val invalidOrMissingTypeParam = mockTypeParameter(CalloutType.INVALID_OR_MISSING)
    private val anyTypeParam = mock<TypeParameter>()

    private val emptyId: String = ""
    private val validUpdateId: String = "6dfd1cee-0fe0-4894-9518-ea39e26d9bec"
    private val validVerifyId: String = "bc48fc0b-9eb7-4f06-86e5-1460d96ff685"
    private val invalidUpdateId: String = "invalidUpdateId"
    private val invalidVerifyId: String = "invalidVerifyId"

    private val invalidCalloutError = mock<InvalidCalloutError>()

    private val validUpdateIdParam: UpdateIdParameter = mockValidCalloutParameter(validUpdateId)
    private val validVerifyIdParam: VerifyIdParameter = mockValidCalloutParameter(validVerifyId)
    private val invalidUpdateIdParam: UpdateIdParameter =
            mockInvalidCalloutParameter(invalidUpdateId, invalidCalloutError)
    private val invalidVerifyIdParam: VerifyIdParameter =
            mockInvalidCalloutParameter(invalidVerifyId, invalidCalloutError)

    @Test
    fun testPatientIdIsVerifyIdWhenActionIsVerify() {
        val patientIdParam = PatientIdParameter(verifyTypeParam, validUpdateIdParam, validVerifyIdParam)
        assertEquals(validVerifyId, patientIdParam.value)
    }

    @Test
    fun testPatientIdIsUpdateIdWhenActionIsUpdate() {
        val patientIdParam = PatientIdParameter(updateTypeParam, validUpdateIdParam, validVerifyIdParam)
        assertEquals(validUpdateId, patientIdParam.value)
    }

    @Test
    fun testPatientIdIsEmptyWhenActionIsRegister() {
        val patientIdParam = PatientIdParameter(registerTypeParam, validUpdateIdParam, validVerifyIdParam)
        assertEquals(emptyId, patientIdParam.value)
    }

    @Test
    fun testPatientIdIsEmptyWhenActionIsIdentify() {
        val patientIdParam = PatientIdParameter(identifyTypeParam, validUpdateIdParam, validVerifyIdParam)
        assertEquals(emptyId, patientIdParam.value)
    }

    @Test
    fun testPatientIdIsEmptyWhenActionIsInvalidOrMissing() {
        val patientIdParam = PatientIdParameter(invalidOrMissingTypeParam, validUpdateIdParam, validVerifyIdParam)
        assertEquals(emptyId, patientIdParam.value)
    }

}
