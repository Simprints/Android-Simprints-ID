package com.simprints.feature.orchestrator.usecases.steps

import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.orchestrator.exceptions.SubjectAgeNotSupportedException
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.usecases.MapStepsForLastBiometricEnrolUseCase
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.Finger
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.NEC
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.orchestration.data.ActionRequest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
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
    private lateinit var secugenSimMatcher: FingerprintConfiguration.FingerprintSdkConfiguration

    @RelaxedMockK
    private lateinit var nec: FingerprintConfiguration.FingerprintSdkConfiguration

    private lateinit var useCase: BuildStepsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = BuildStepsUseCase(buildMatcherSubjectQuery, cache, mapStepsForLastBiometrics)
    }

    private fun mockCommonProjectConfiguration(): ProjectConfiguration {
        val projectConfiguration = mockk<ProjectConfiguration>(relaxed = true)
        every { projectConfiguration.general.modalities } returns listOf(Modality.FINGERPRINT, Modality.FACE)
        every { projectConfiguration.general.matchingModalities } returns listOf(Modality.FINGERPRINT, Modality.FACE)
        every { projectConfiguration.consent.collectConsent } returns true
        every { projectConfiguration.fingerprint?.allowedSDKs } returns listOf(
            SECUGEN_SIM_MATCHER,
            NEC
        )

        every { secugenSimMatcher.fingersToCapture } returns listOf(
            Finger.LEFT_THUMB,
            Finger.RIGHT_THUMB,
        )
        every { secugenSimMatcher.allowedAgeRange } returns AgeGroup(0, null)
        every { projectConfiguration.fingerprint?.secugenSimMatcher } returns secugenSimMatcher
        every { projectConfiguration.fingerprint?.getSdkConfiguration(SECUGEN_SIM_MATCHER) } returns secugenSimMatcher

        every { nec.fingersToCapture } returns listOf(
            Finger.LEFT_INDEX_FINGER,
            Finger.RIGHT_INDEX_FINGER,
        )
        every { nec.allowedAgeRange } returns AgeGroup(0, null)
        every { projectConfiguration.fingerprint?.nec } returns nec
        every { projectConfiguration.fingerprint?.getSdkConfiguration(NEC) } returns nec

        every { projectConfiguration.face?.allowedSDKs } returns listOf(FaceConfiguration.BioSdk.RANK_ONE)
        every { projectConfiguration.face?.nbOfImagesToCapture } returns 3
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns null

        return projectConfiguration
    }

    private fun assertStepOrder(steps: List<Step>, vararg expectedStepIds: Int) {
        assertEquals("Number of steps does not match the expected number.", expectedStepIds.size, steps.size)
        steps.zip(expectedStepIds.toList()).forEachIndexed { index, pair ->
            val (step, expectedStepId) = pair
            assertEquals("Step at index $index does not match the expected step ID.", expectedStepId, step.id)
        }
    }

    @Test
    fun `build - enrol action - no age restriction - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns false

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
        )
    }

    @Test
    fun `build - enrol action - no consent - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.consent.collectConsent } returns false

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
        )
    }

    @Test
    fun `build - enrol action - no age restriction - duplicate check - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER
        )
    }

    @Test
    fun `build - identify action - no age restriction - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER
        )
    }

    @Test
    fun `build - identify action - no age restriction - id pool validation - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null
        every { projectConfiguration.experimental().idPoolValidationEnabled } returns true

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.VALIDATE_ID_POOL,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER
        )
    }

    @Test
    fun `build - verify action - no age restriction - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.FETCH_GUID,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER
        )
    }

    @Test
    fun `build - confirm identity action - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.ConfirmIdentityActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.CONFIRM_IDENTITY
        )
    }

    @Test
    fun `build - enrol last biometric action - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.EnrolLastBiometricActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null
        every { cache.steps } returns listOf(
            Step(StepId.FINGERPRINT_CAPTURE, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true)),
            Step(StepId.FACE_CAPTURE, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true)),
        )

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.ENROL_LAST_BIOMETRIC
        )
    }

    @Test
    fun `build - enrol action - age restriction - missing subject age - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.SELECT_SUBJECT_AGE,
            StepId.CONSENT
        )
    }

    @Test
    fun `build - identify action - age restriction - missing subject age - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.SELECT_SUBJECT_AGE,
            StepId.CONSENT
        )
    }

    @Test
    fun `build - verify action - age restriction - missing subject age - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns null

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.SELECT_SUBJECT_AGE,
            StepId.FETCH_GUID,
            StepId.CONSENT
        )
    }

    @Test
    fun `build - enrol action - age restriction - subject age within range - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 25 // Subject age within the supported range

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
        )
    }

    @Test
    fun `build - enrol action - age restriction - subject age within range - duplicate check - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 25 // Subject age within the supported range

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER
        )
    }

    @Test
    fun `build - identify action - age restriction - subject age within range - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 25 // Subject age within the supported range

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER
        )
    }

    @Test
    fun `build - identify action - CoSync data source - returns steps without validate ID pool`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true) {
            every { getSubjectAgeIfAvailable() } returns 25
            every { biometricDataSource } returns "COMMCARE"
        }
        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER
        )
    }

    @Test
    fun `build - verify action - age restriction - subject age within range - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 25 // Subject age within the supported range

        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            StepId.FETCH_GUID,
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER
        )
    }

    @Test
    fun `build - verify action - co-sync data source - returns steps without fetch GUID`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 25 // Subject age within the supported range
        every { action.biometricDataSource } returns "COMMCARE"
        val steps = useCase.build(action, projectConfiguration)

        assertStepOrder(steps,
            StepId.SETUP,
            // no StepId.FETCH_GUID
            StepId.CONSENT,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER
        )
    }

    @Test
    fun `build - enrol action - age restriction - subject age not supported - throws SubjectAgeNotSupportedException`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(30, 40)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 20 // Subject age not supported by any SDK

        assertThrows(SubjectAgeNotSupportedException::class.java) {
            useCase.build(action, projectConfiguration)
        }
    }

    @Test
    fun `build - identify action - age restriction - subject age not supported - throws SubjectAgeNotSupportedException`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(30, 40)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 20 // Subject age not supported by any SDK

        assertThrows(SubjectAgeNotSupportedException::class.java) {
            useCase.build(action, projectConfiguration)
        }
    }

    @Test
    fun `build - verify action - age restriction - subject age not supported - throws SubjectAgeNotSupportedException`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(30, 40)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)
        every { action.getSubjectAgeIfAvailable() } returns 20 // Subject age not supported by any SDK

        assertThrows(SubjectAgeNotSupportedException::class.java) {
            useCase.build(action, projectConfiguration)
        }
    }

    @Test
    fun `build capture and match steps - enrol action - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, ageGroup)

        assertStepOrder(steps,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
        )
    }

    @Test
    fun `build capture and match steps - enrol action - duplicate check - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns true
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, ageGroup)

        assertStepOrder(steps,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER
        )
    }

    @Test
    fun `build capture and match steps - identify action - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, ageGroup)

        assertStepOrder(steps,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER
        )
    }

    @Test
    fun `build capture and match steps - verify action - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()
        val ageGroup = AgeGroup(18, 60)
        every { secugenSimMatcher.allowedAgeRange } returns ageGroup
        every { nec.allowedAgeRange } returns ageGroup
        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroup

        val action = mockk<ActionRequest.VerifyActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, ageGroup)

        assertStepOrder(steps,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FINGERPRINT_CAPTURE,
            StepId.FACE_CAPTURE,
            StepId.FINGERPRINT_MATCHER,
            StepId.FINGERPRINT_MATCHER,
            StepId.FACE_MATCHER
        )
    }

    @Test
    fun `build capture and match steps - confirm identity action - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.ConfirmIdentityActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, AgeGroup(18, 60))

        assertEquals(0, steps.size)
    }

    @Test
    fun `build capture and match steps - enrol last action - returns expected steps`() {
        val projectConfiguration = mockCommonProjectConfiguration()

        val action = mockk<ActionRequest.EnrolLastBiometricActionRequest>(relaxed = true)

        val steps = useCase.buildCaptureAndMatchStepsForAgeGroup(action, projectConfiguration, AgeGroup(18, 60))

        assertEquals(0, steps.size)
    }
}
