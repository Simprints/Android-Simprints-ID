package com.simprints.feature.orchestrator.usecases.steps

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.reference.TemplateIdentifier
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.orchestrator.exceptions.SubjectAgeNotSupportedException
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.usecases.MapStepsForLastBiometricEnrolUseCase
import com.simprints.feature.selectsubject.SelectSubjectParams
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.NEC
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.orchestration.data.ActionRequest
import io.mockk.*
import io.mockk.impl.annotations.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class BuildStepsUseCaseTest {
    @RelaxedMockK
    private lateinit var buildMatcherSubjectQuery: BuildMatcherSubjectQueryUseCase

    @RelaxedMockK
    private lateinit var cache: OrchestratorCache

    @RelaxedMockK
    private lateinit var mapStepsForLastBiometrics: MapStepsForLastBiometricEnrolUseCase

    @RelaxedMockK
    private lateinit var fallbackToCommCareDataSourceIfNeeded: FallbackToCommCareDataSourceIfNeededUseCase

    @RelaxedMockK
    private lateinit var secugenSimMatcher: FingerprintConfiguration.FingerprintSdkConfiguration

    @RelaxedMockK
    private lateinit var nec: FingerprintConfiguration.FingerprintSdkConfiguration

    @RelaxedMockK
    private lateinit var cachedScannedCredential: ScannedCredential

    private lateinit var useCase: BuildStepsUseCase
    private lateinit var enrolmentSubjectId: String

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = BuildStepsUseCase(buildMatcherSubjectQuery, cache, mapStepsForLastBiometrics, fallbackToCommCareDataSourceIfNeeded)
        enrolmentSubjectId = "enrolmentSubjectId"

        // Setup fallback use case to return the input actions unchanged by default
        coEvery { fallbackToCommCareDataSourceIfNeeded(any<ActionRequest.EnrolActionRequest>(), any()) } answers { firstArg() }
        coEvery { fallbackToCommCareDataSourceIfNeeded(any<ActionRequest.IdentifyActionRequest>(), any()) } answers { firstArg() }
    }

    private fun mockCommonProjectConfiguration(): ProjectConfiguration {
        val projectConfiguration = mockk<ProjectConfiguration>(relaxed = true)
        every { projectConfiguration.general.modalities } returns listOf(Modality.FINGERPRINT, Modality.FACE)
        every { projectConfiguration.general.matchingModalities } returns listOf(Modality.FINGERPRINT, Modality.FACE)
        every { projectConfiguration.consent.collectConsent } returns true
        every { projectConfiguration.fingerprint?.allowedSDKs } returns listOf(
            SECUGEN_SIM_MATCHER,
            NEC,
        )

        every { secugenSimMatcher.fingersToCapture } returns listOf(
            TemplateIdentifier.LEFT_THUMB,
            TemplateIdentifier.RIGHT_THUMB,
        )
        every { secugenSimMatcher.allowedAgeRange } returns AgeGroup(0, null)
        every { projectConfiguration.fingerprint?.secugenSimMatcher } returns secugenSimMatcher
        every { projectConfiguration.fingerprint?.getSdkConfiguration(SECUGEN_SIM_MATCHER) } returns secugenSimMatcher

        every { nec.fingersToCapture } returns listOf(
            TemplateIdentifier.LEFT_INDEX_FINGER,
            TemplateIdentifier.RIGHT_INDEX_FINGER,
        )
        every { nec.allowedAgeRange } returns AgeGroup(0, null)
        every { projectConfiguration.fingerprint?.nec } returns nec
        every { projectConfiguration.fingerprint?.getSdkConfiguration(NEC) } returns nec

        every { projectConfiguration.face?.allowedSDKs } returns listOf(FaceConfiguration.BioSdk.RANK_ONE)
        every { projectConfiguration.face?.rankOne?.nbOfImagesToCapture } returns 3
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns null
        every { projectConfiguration.face?.simFace?.allowedAgeRange } returns null

        return projectConfiguration
    }

    private fun assertStepOrder(
        steps: List<Step>,
        vararg expectedStepIds: Int,
    ) {
        assertEquals("Number of steps does not match the expected number.", expectedStepIds.size, steps.size)
        steps.zip(expectedStepIds.toList()).forEachIndexed { index, pair ->
            val (step, expectedStepId) = pair
            assertEquals("Step at index $index does not match the expected step ID.", expectedStepId, step.id)
        }
    }

    @Test
    fun `build - enrol action - no age restriction - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns false

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
        )
    }

    @Test
    fun `build - enrol action - no consent - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.consent.collectConsent } returns false

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
        )
    }

    @Test
    fun `build - enrol action - no age restriction - duplicate check - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build - enrol action - no age restriction - duplicate check - matching modalities - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.general.matchingModalities } returns listOf(Modality.FACE)
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build - identify action - no age restriction - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build - identify action - no age restriction - matching modalities - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.general.matchingModalities } returns listOf(Modality.FACE)

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FACE_CAPTURE,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build - identify action - no age restriction - id pool validation - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null
        every { projectConfiguration.experimental().idPoolValidationEnabled } returns true

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.VALIDATE_ID_POOL,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build - verify action - no age restriction - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.FETCH_GUID,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build - verify action - no age restriction - matching modalities - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.general.matchingModalities } returns listOf(Modality.FACE)

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.FETCH_GUID,
            StepId.CONSENT,
            StepId.FACE_CAPTURE,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build - confirm identity action - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.ConfirmIdentityActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.CONFIRM_IDENTITY,
        )
    }

    @Test
    fun `build - enrol last biometric action - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.EnrolLastBiometricActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null
        every { cache.steps } returns listOf(
            Step(StepId.FINGERPRINT_CAPTURE, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true)),
            Step(StepId.FACE_CAPTURE, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true)),
        )

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.ENROL_LAST_BIOMETRIC,
        )
    }

    @Test
    fun `build - enrol last biometric action - missing modality capture - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.general.matchingModalities } returns listOf(Modality.FACE)

        val action = mockk<ActionRequest.EnrolLastBiometricActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null
        every { cache.steps } returns listOf(
            Step(StepId.FACE_CAPTURE, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true)),
        )

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.ENROL_LAST_BIOMETRIC,
        )
    }

    @Test
    fun `build - enrol action - age restriction - missing subject age - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.SELECT_SUBJECT_AGE,
            StepId.CONSENT,
        )
    }

    @Test
    fun `build - identify action - age restriction - missing subject age - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.SELECT_SUBJECT_AGE,
            StepId.CONSENT,
        )
    }

    @Test
    fun `build - verify action - age restriction - missing subject age - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.SELECT_SUBJECT_AGE,
            StepId.FETCH_GUID,
            StepId.CONSENT,
        )
    }

    @Test
    fun `build - enrol action - age restriction - subject age within range - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 25 // Subject age within the supported range

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
        )
    }

    @Test
    fun `build - enrol action - age restriction - subject age within range - duplicate check - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 25 // Subject age within the supported range

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build - identify action - age restriction - subject age within range - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 25 // Subject age within the supported range

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build - identify action - CoSync data source - returns steps without validate ID pool`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true) {
            every { getSubjectAgeIfAvailable() } returns 25
            every { biometricDataSource } returns "COMMCARE"
        }
        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build - verify action - age restriction - subject age within range - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 25 // Subject age within the supported range

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.FETCH_GUID,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build - verify action - co-sync data source - returns steps without fetch GUID`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 25 // Subject age within the supported range
        every { action.biometricDataSource } returns "COMMCARE"
        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            // no StepId.FETCH_GUID
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build - enrol action - age restriction - subject age not supported - throws SubjectAgeNotSupportedException`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(30, 40)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 20 // Subject age not supported by any SDK

        assertThrows(SubjectAgeNotSupportedException::class.java) {
            runBlocking { useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential) }
        }
    }

    @Test
    fun `build - identify action - age restriction - subject age not supported - throws SubjectAgeNotSupportedException`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(30, 40)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 20 // Subject age not supported by any SDK

        assertThrows(SubjectAgeNotSupportedException::class.java) {
            runBlocking { useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential) }
        }
    }

    @Test
    fun `build - verify action - age restriction - subject age not supported - throws SubjectAgeNotSupportedException`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(30, 40)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 20 // Subject age not supported by any SDK

        assertThrows(SubjectAgeNotSupportedException::class.java) {
            runBlocking { useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential) }
        }
    }

    @Test
    fun `build capture and match steps - enrol action - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, ageGroup, enrolmentSubjectId)

        assertStepOrder(
            steps,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
        )
    }

    @Test
    fun `build capture and match steps - enrol action - duplicate check - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, ageGroup, enrolmentSubjectId)

        assertStepOrder(
            steps,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build capture and match steps - identify action - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, ageGroup, enrolmentSubjectId)

        assertStepOrder(
            steps,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build capture and match steps - verify action - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, ageGroup, enrolmentSubjectId)

        assertStepOrder(
            steps,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build capture and match steps with all SDKs - verify action - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.simFace?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, ageGroup, enrolmentSubjectId)

        assertStepOrder(
            steps,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build capture and match steps - confirm identity action - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.ConfirmIdentityActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, AgeGroup(18, 60), enrolmentSubjectId)

        assertEquals(0, steps.size)
    }

    @Test
    fun `build capture and match steps - enrol last action - returns expected steps`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.EnrolLastBiometricActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, AgeGroup(18, 60), enrolmentSubjectId)

        assertEquals(0, steps.size)
    }

    @Test
    fun `build external credential not enabled - no external credential step`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.multifactorId?.allowedExternalCredentials } returns emptyList()

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        // Should not contain EXTERNAL_CREDENTIAL step
        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
        )
    }

    @Test
    fun `build enrol action - external credential enabled - returns external credential step`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.multifactorId?.allowedExternalCredentials } returns ExternalCredentialType.entries

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.EXTERNAL_CREDENTIAL,
        )
    }

    @Test
    fun `build identify action - external credential enabled - returns external credential step`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.multifactorId?.allowedExternalCredentials } returns ExternalCredentialType.entries

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.EXTERNAL_CREDENTIAL,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build verify action - external credential enabled - no external credential step`() = runTest {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.multifactorId?.allowedExternalCredentials } returns ExternalCredentialType.entries

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration, enrolmentSubjectId, cachedScannedCredential)

        // Should not contain EXTERNAL_CREDENTIAL step for VERIFY flow
        assertStepOrder(
            steps,
            StepId.SETUP,
            StepId.FETCH_GUID,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER,
        )
    }

    @Test
    fun `build confirm identity action - cachedScannedCredential is passed correctly`() = runTest {
        val testProjectId = "projectId"
        val guid = "guid"
        val projectConfiguration = mockCommonProjectConfiguration()
        val cachedScannedCredential = mockk<ScannedCredential>(relaxed = true)

        val action = mockk<ActionRequest.ConfirmIdentityActionRequest>(relaxed = true) {
            every { projectId } returns testProjectId
            every { selectedGuid } returns guid
            every { getSubjectAgeIfAvailable() } returns null
        }

        val steps = useCase.build(
            action = action,
            projectConfiguration = projectConfiguration,
            enrolmentSubjectId = enrolmentSubjectId,
            cachedScannedCredential = cachedScannedCredential,
        )

        assertStepOrder(steps, StepId.CONFIRM_IDENTITY)

        val confirmIdentityStep = steps.first()
        val params = confirmIdentityStep.params as? SelectSubjectParams

        assertThat(params).isNotNull()
        assertThat(params?.projectId).isEqualTo(testProjectId)
        assertThat(params?.subjectId).isEqualTo(guid)
        assertThat(params?.scannedCredential).isEqualTo(cachedScannedCredential)
    }
}
