package com.simprints.id.data.model.calloutParameter.concrete

import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.model.calloutParameter.mockTypeParameter
import com.simprints.id.data.model.calloutParameter.mockValidCalloutParameter
import org.junit.Assert.assertEquals
import org.junit.Test


class PatientIdParameterTest {

    private val updateTypeParameter = mockTypeParameter(CalloutType.UPDATE)
    private val verifyTypeParameter = mockTypeParameter(CalloutType.VERIFY)
    private val identifyTypeParameter = mockTypeParameter(CalloutType.IDENTIFY)
    private val registerTypeParameter = mockTypeParameter(CalloutType.REGISTER)
    private val invalidOrMissingTypeParameter = mockTypeParameter(CalloutType.INVALID_OR_MISSING)

    private val validUpdateId: String = "6dfd1cee-0fe0-4894-9518-ea39e26d9bec"
    private val validVerifyId: String = "bc48fc0b-9eb7-4f06-86e5-1460d96ff685"
    private val emptyId: String = ""

    private val validUpdateIdParameter: UpdateIdParameter = mockValidCalloutParameter(validUpdateId)
    private val validVerifyIdParameter: VerifyIdParameter = mockValidCalloutParameter(validVerifyId)

    @Test
    fun testPatientIdIsVerifyIdWhenActionIsVerify() {
        val patientIdParameter = PatientIdParameter(verifyTypeParameter, validUpdateIdParameter,
                validVerifyIdParameter)
        assertEquals(validVerifyId, patientIdParameter.value)
    }

    @Test
    fun testPatientIdIsUpdateIdWhenActionIsUpdate() {
        val patientIdParameter = PatientIdParameter(updateTypeParameter, validUpdateIdParameter,
                validVerifyIdParameter)
        assertEquals(validUpdateId, patientIdParameter.value)
    }

    @Test
    fun testPatientIdIsEmptyWhenActionIsRegister() {
        val patientIdParameter = PatientIdParameter(registerTypeParameter, validUpdateIdParameter,
                validVerifyIdParameter)
        assertEquals(emptyId, patientIdParameter.value)
    }

    @Test
    fun testPatientIdIsEmptyWhenActionIsIdentify() {
        val patientIdParameter = PatientIdParameter(identifyTypeParameter, validUpdateIdParameter,
                validVerifyIdParameter)
        assertEquals(emptyId, patientIdParameter.value)
    }

    @Test
    fun testPatientIdIsEmptyWhenActionIsInvalidOrMissing() {
        val patientIdParameter = PatientIdParameter(invalidOrMissingTypeParameter,
                validUpdateIdParameter, validVerifyIdParameter)
        assertEquals(emptyId, patientIdParameter.value)
    }

    @Test
    fun testValidateThrowsErrorWhenValueIsMissing() {

    }

}
