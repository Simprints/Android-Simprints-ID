package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.domain.sessionParameters.calloutParameter.mockTypeParameter
import com.simprints.id.domain.sessionParameters.calloutParameter.mockValidCalloutParameter
import org.junit.Assert.assertEquals
import org.junit.Test


class PatientIdParameterTest {

    private val updateTypeParam = mockTypeParameter(CalloutAction.UPDATE)
    private val verifyTypeParam = mockTypeParameter(CalloutAction.VERIFY)
    private val identifyTypeParam = mockTypeParameter(CalloutAction.IDENTIFY)
    private val registerTypeParam = mockTypeParameter(CalloutAction.REGISTER)
    private val invalidTypeParam = mockTypeParameter(CalloutAction.INVALID)
    private val missingTypeParam = mockTypeParameter(CalloutAction.MISSING)

    private val emptyId: String = ""
    private val validUpdateId: String = "6dfd1cee-0fe0-4894-9518-ea39e26d9bec"
    private val validVerifyId: String = "bc48fc0b-9eb7-4f06-86e5-1460d96ff685"

    private val validUpdateIdParam: UpdateIdParameter = mockValidCalloutParameter(validUpdateId)
    private val validVerifyIdParam: VerifyIdParameter = mockValidCalloutParameter(validVerifyId)

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
    fun testPatientIdIsEmptyWhenActionIsInvalid() {
        val patientIdParam = PatientIdParameter(invalidTypeParam, validUpdateIdParam, validVerifyIdParam)
        assertEquals(emptyId, patientIdParam.value)
    }

    @Test
    fun testPatientIdIsEmptyWhenActionIsMissing() {
        val patientIdParam = PatientIdParameter(missingTypeParam, validUpdateIdParam, validVerifyIdParam)
        assertEquals(emptyId, patientIdParam.value)
    }

}
