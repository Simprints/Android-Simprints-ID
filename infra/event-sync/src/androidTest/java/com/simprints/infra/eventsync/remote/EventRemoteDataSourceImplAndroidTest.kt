// TODO fix
@file:Suppress("ktlint:standard:no-empty-file")

package com.simprints.infra.eventsync.remote

// @RunWith(AndroidJUnit4::class)
// class EventRemoteDataSourceImplAndroidTest {
//
//    companion object {
//        const val URL = "https://dev.simprints-apis.com/androidapi/v2/"
//        const val SIGNED_ID_USER = "some_signed_user"
//        const val DEFAULT_TIME = 1000L
//    }
//
//    private val app = ApplicationProvider.getApplicationContext<Application>()
//
//    private val remoteTestingManager: RemoteTestingManager by lazy {
//        RemoteTestingManager.create(app)
//    }
//
//    private val facePayloadId = randomUUID()
//    private val fingerPayloadId = randomUUID()
//
//    @MockK
//    lateinit var timeHelper: TimeHelper
//
//    private lateinit var testProject: TestProject
//
//    @get:Rule
//    val testProjectRule = TestProjectRule(app)
//
//    private lateinit var eventRemoteDataSource: EventRemoteDataSource
//    private lateinit var eventLabels: EventLabels
//
//    @MockK
//    var loginManager = mockk<LoginManager>()
//
//    @Before
//    fun setUp() {
//        MockKAnnotations.init(this)
//        testProject = testProjectRule.testProject
//        eventLabels = EventLabels(sessionId = GUID1, deviceId = GUID1, projectId = testProject.id)
//
//        val firebaseTestToken = runTest {
//            remoteTestingManager.generateFirebaseToken(
//                projectId = testProject.id,
//                userId = SIGNED_ID_USER
//            )
//        }
//        coEvery { loginManager.buildClient<EventRemoteInterface>(any()) } returns SimApiClientImpl(
//            EventRemoteInterface::class,
//            app,
//            URL,
//            "deviceId",
//            "Test",
//            firebaseTestToken.token,
//        )
//        eventRemoteDataSource = EventRemoteDataSourceImpl(
//            loginManager,
//            JsonHelper
//        )
//        every { timeHelper.nowMinus(any(), any()) } returns 100
//        every { timeHelper.now() } returns 100
//    }
//
//    /**
//     * This test case makes calls to BFSID, and its a way for us to validate that, changes to any of
//     * the [Event] subtypes, is accepted by BFSID. Thus ensuring that at development time, we catch
//     * any issues that might arise as a result of updating the captured events' data structures.
//     */
//    @Test(expected = Test.None::class)
//    fun aSessionWithAllEvents_shouldGetUploaded() {
//        runBlocking {
//            val events = mutableListOf<Event>()
//            EventType.values().forEach {
//                events.addEventFor(it)
//            }
//
//            Simber.d("UPLOAD ALL EVENTS")
//            executeUpload(events)
//            Simber.d("UPLOAD ENROLMENT V1")
//            executeUpload(listOf(createEnrolmentEventV1().apply { labels = eventLabels }))
//        }
//    }
//
//    private suspend fun executeUpload(events: List<Event>) {
//        eventRemoteDataSource.post(
//            projectId = testProject.id,
//            acceptInvalidEvents = false,
//            events = events.toImmutableList()
//        )
//    }
//
//    private fun MutableList<Event>.addAlertScreenEvents() {
//        AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.values()
//            .forEach {
//                add(
//                    AlertScreenEvent(
//                        createdAt = DEFAULT_TIME,
//                        alertType = it,
//                        labels = eventLabels
//                    )
//                )
//            }
//    }
//
//    private fun MutableList<Event>.addArtificialTerminationEvent() {
//        ArtificialTerminationPayload.Reason.values().forEach {
//            add(
//                ArtificialTerminationEvent(
//                    createdAt = DEFAULT_TIME,
//                    reason = it,
//                    labels = eventLabels
//                )
//            )
//        }
//    }
//
//    private fun MutableList<Event>.addAuthenticationEvent() {
//        listOf(
//            Result.OFFLINE,
//            Result.OFFLINE,
//            Result.OFFLINE,
//            Result.OFFLINE,
//            Result.TECHNICAL_FAILURE,
//            Result.BACKEND_MAINTENANCE_ERROR,
//            Result.INTEGRITY_SERVICE_ERROR,
//            Result.UNKNOWN
//        ).forEach {
//            add(
//                AuthenticationEvent(
//                    createdAt = DEFAULT_TIME,
//                    endTime = DEFAULT_TIME,
//                    userInfo = UserInfo("some_project", DEFAULT_USER_ID),
//                    result = it,
//                    labels = eventLabels
//                )
//            )
//        }
//    }
//
//    private fun MutableList<Event>.addAuthorizationEvent() {
//        AuthorizationPayload.AuthorizationResult.values().forEach {
//            add(
//                AuthorizationEvent(
//                    createdAt = DEFAULT_TIME,
//                    result = it,
//                    userInfo = AuthorizationPayload.UserInfo("some_project", DEFAULT_USER_ID),
//                    labels = eventLabels
//                )
//            )
//        }
//    }
//
//    private fun MutableList<Event>.addCandidateReadEvent() {
//        CandidateReadPayload.LocalResult.values().forEach { local ->
//            CandidateReadPayload.RemoteResult.values().forEach { remote ->
//                add(
//                    CandidateReadEvent(
//                        createdAt = DEFAULT_TIME,
//                        endTime = DEFAULT_TIME,
//                        candidateId = randomUUID(),
//                        localResult = local,
//                        remoteResult = remote,
//                        labels = eventLabels
//                    )
//                )
//            }
//        }
//    }
//
//    private fun MutableList<Event>.addConnectivitySnapshotEvent() {
//        add(
//            ConnectivitySnapshotEvent(
//                createdAt = DEFAULT_TIME,
//                connections = listOf(
//                    SimNetworkUtils.Connection(
//                        SimNetworkUtils.ConnectionType.MOBILE,
//                        SimNetworkUtils.ConnectionState.CONNECTED
//                    )
//                ), labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addConsentEvent() {
//        ConsentPayload.Type.values().forEach { type ->
//            ConsentPayload.Result.values().forEach { result ->
//                add(
//                    ConsentEvent(
//                        createdAt = DEFAULT_TIME,
//                        endTime = DEFAULT_TIME,
//                        consentType = type,
//                        result = result,
//                        labels = eventLabels
//                    )
//                )
//            }
//        }
//    }
//
//    private fun MutableList<Event>.addEnrolmentEvent() {
//        add(
//            EnrolmentEventV2(
//                createdAt = DEFAULT_TIME,
//                subjectId = randomUUID(),
//                projectId = testProject.id,
//                moduleId = DEFAULT_MODULE_ID,
//                attendantId = DEFAULT_USER_ID,
//                personCreationEventId = randomUUID(),
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addFingerprintCaptureEvent() {
//        FingerprintCaptureEvent.FingerprintCapturePayload.Result.values().forEach { result ->
//            FingerIdentifier.values().forEach { fingerIdentifier ->
//
//                val fingerprint = FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(
//                    finger = fingerIdentifier.fromDomainToModuleApi(),
//                    quality = 0,
//                    format = "ISO_19794_2"
//                )
//
//                val event = FingerprintCaptureEvent(
//                    createdAt = DEFAULT_TIME,
//                    endTime = DEFAULT_TIME,
//                    finger = fingerIdentifier.fromDomainToModuleApi(),
//                    qualityThreshold = 0,
//                    result = result,
//                    fingerprint = fingerprint,
//                    labels = eventLabels,
//                    payloadId = fingerPayloadId
//                )
//
//                add(event)
//            }
//        }
//    }
//
//    private fun MutableList<Event>.addFingerprintBiometricCaptureEvent() {
//        FingerIdentifier.values().forEach { fingerIdentifier ->
//            val fakeTemplate = EncodingUtilsImpl.byteArrayToBase64(
//                Random.nextBytes(64)
//            )
//
//            val fingerprint =
//                FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint(
//                    finger = fingerIdentifier.fromDomainToModuleApi(),
//                    template = fakeTemplate,
//                    quality = 1,
//                    format = "ISO_19794_2"
//                )
//
//            val event = FingerprintCaptureBiometricsEvent(
//                createdAt = DEFAULT_TIME,
//                fingerprint = fingerprint,
//                labels = eventLabels,
//                payloadId = fingerPayloadId
//            )
//
//            add(event)
//        }
//    }
//
//
//    private fun MutableList<Event>.addFaceCaptureEvent() {
//        FaceCaptureEvent.FaceCapturePayload.Result.values().forEachIndexed { index, result ->
//
//            val face =
//                FaceCaptureEvent.FaceCapturePayload.Face(
//                    yaw = 30f,
//                    roll = 40f,
//                    quality = 100f,
//                    format = RANK_ONE_1_23
//                )
//
//            val event = FaceCaptureEvent(
//                startTime = DEFAULT_TIME,
//                endTime = DEFAULT_TIME + 100,
//                attemptNb = index + 1,
//                qualityThreshold = 0f,
//                result = result,
//                isFallback = false,
//                face = face,
//                labels = eventLabels,
//                payloadId = facePayloadId
//            )
//
//            add(event)
//        }
//    }
//
//    private fun MutableList<Event>.addFaceCaptureBiometricCaptureEvent() {
//        val template = EncodingUtilsImpl.byteArrayToBase64(Random.nextBytes(64))
//
//        val face =
//            FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
//                roll = 0.0f,
//                yaw = 0.0f,
//                template = template,
//                quality = 1.0f,
//                format = RANK_ONE_1_23
//            )
//
//        val event = FaceCaptureBiometricsEvent(
//            startTime = DEFAULT_TIME,
//            face = face,
//            labels = eventLabels,
//            payloadId = facePayloadId
//        )
//
//        add(event)
//    }
//
//    private fun MutableList<Event>.addFaceCaptureConfirmationEvent() {
//        FaceCaptureConfirmationPayload.Result.values().forEach { result ->
//            val event = FaceCaptureConfirmationEvent(
//                startTime = DEFAULT_TIME,
//                endTime = DEFAULT_TIME + 100,
//                result = result,
//                labels = eventLabels
//            )
//
//            add(event)
//        }
//    }
//
//    private fun MutableList<Event>.addFaceFallbackCaptureEvent() {
//        val event = FaceFallbackCaptureEvent(
//            startTime = DEFAULT_TIME,
//            endTime = DEFAULT_TIME + 100,
//            labels = eventLabels
//        )
//        add(event)
//    }
//
//    private fun MutableList<Event>.addFaceOnboardingCompleteEvent() {
//        val event = FaceOnboardingCompleteEvent(
//            startTime = DEFAULT_TIME,
//            endTime = DEFAULT_TIME + 100,
//            labels = eventLabels
//        )
//        add(event)
//    }
//
//    private fun MutableList<Event>.addGuidSelectionEvent() {
//        add(
//            GuidSelectionEvent(
//                createdAt = DEFAULT_TIME,
//                selectedId = randomUUID(),
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addIntentParsingEvent() {
//        IntentParsingPayload.IntegrationInfo.values().forEach {
//            add(
//                IntentParsingEvent(
//                    createdAt = DEFAULT_TIME,
//                    integration = it,
//                    labels = eventLabels
//                )
//            )
//        }
//    }
//
//    private fun MutableList<Event>.addInvalidIntentEvent() {
//        add(
//            InvalidIntentEvent(
//                creationTime = DEFAULT_TIME,
//                action = "some_action",
//                extras = mapOf("wrong_field" to "wrong_value"),
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addOneToManyMatchEvent() {
//        OneToManyMatchPayload.MatchPoolType.values().forEach {
//            add(
//                OneToManyMatchEvent(
//                    createdAt = DEFAULT_TIME,
//                    endTime = DEFAULT_TIME,
//                    pool = OneToManyMatchPayload.MatchPool(it, 0),
//                    matcher = Matcher.SIM_AFIS,
//                    result = emptyList(),
//                    labels = eventLabels
//                )
//            )
//        }
//    }
//
//    private fun MutableList<Event>.addOneToOneMatchEvent() {
//        add(
//            OneToOneMatchEvent(
//                createdAt = DEFAULT_TIME,
//                endTime = DEFAULT_TIME,
//                candidateId = randomUUID(),
//                matcher = Matcher.SIM_AFIS,
//                result = MatchEntry(randomUUID(), 0F),
//                fingerComparisonStrategy = FingerComparisonStrategy.SAME_FINGER,
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addPersonCreationEvent(
//        fingerprintCaptureEvent: FingerprintCaptureEvent?,
//        faceCaptureEvent: FaceCaptureEvent?
//    ) {
//        add(
//            PersonCreationEvent(
//                startTime = DEFAULT_TIME,
//                fingerprintCaptureIds = listOf(
//                    fingerprintCaptureEvent?.payload?.id
//                        ?: ""
//                ),
//                fingerprintReferenceId = randomUUID(),
//                faceCaptureIds = listOf(faceCaptureEvent?.payload?.id ?: ""),
//                faceReferenceId = randomUUID(),
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addRefusalEvent() {
//        RefusalPayload.Answer.values().forEach {
//            add(
//                RefusalEvent(
//                    createdAt = DEFAULT_TIME,
//                    endTime = DEFAULT_TIME,
//                    reason = it,
//                    otherText = "other_text",
//                    labels = eventLabels
//                )
//            )
//        }
//    }
//
//    private fun MutableList<Event>.addScannerConnectionEvent() {
//        add(
//            ScannerConnectionEvent(
//                createdAt = DEFAULT_TIME,
//                scannerInfo = ScannerConnectionPayload.ScannerInfo(
//                    "scanner_id", "macAddress",
//                    ScannerGeneration.VERO_2, "hardware"
//                ),
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addVero2InfoSnapshotEvents() {
//        add(
//            Vero2InfoSnapshotEvent(
//                createdAt = DEFAULT_TIME,
//                version = Vero2InfoSnapshotEvent.Vero2Version.Vero2NewApiVersion(
//                    "E-1", "1.23",
//                    "api", "stmApp"
//                ),
//                battery = Vero2InfoSnapshotEvent.BatteryInfo(70, 15, 1, 37),
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addScannerFirmwareUpdateEvent() {
//        add(
//            ScannerFirmwareUpdateEvent(
//                createdAt = DEFAULT_TIME, endTime = DEFAULT_TIME, chip = "stm",
//                targetAppVersion = "targetApp", failureReason = "failureReason",
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addSuspiciousIntentEvent() {
//        add(
//            SuspiciousIntentEvent(
//                createdAt = DEFAULT_TIME,
//                unexpectedExtras = mapOf("some_extra_key" to "value"),
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addCompletionCheckEvent() {
//        add(CompletionCheckEvent(createdAt = DEFAULT_TIME, completed = true, labels = eventLabels))
//    }
//
//    private fun MutableList<Event>.addSessionCaptureEvent() {
//        val deviceArg = Device(
//            androidSdkVersion = VERSION.SDK_INT.toString(),
//            deviceModel = Build.MANUFACTURER + "_" + Build.MODEL,
//            deviceId = GUID1
//        )
//
//        val event = SessionCaptureEvent(
//            id = randomUUID(),
//            projectId = testProject.id,
//            createdAt = CREATED_AT,
//            modalities = listOf(Modality.FINGERPRINT, Modality.FACE),
//            appVersionName = "appVersionName",
//            libVersionName = "libSimprintsVersionName",
//            language = "EN",
//            device = deviceArg,
//            databaseInfo = DatabaseInfo(sessionCount = 0, recordCount = 2)
//        )
//
//        event.payload.location = Location(latitude = 0.0, longitude = 0.0)
//        event.payload.uploadedAt = 1
//        event.payload.endedAt = 1
//
//        add(event)
//    }
//
//    private fun MutableList<Event>.addCallbackErrorEvent() {
//        ErrorCallbackPayload.Reason.values().forEach {
//            add(ErrorCallbackEvent(createdAt = DEFAULT_TIME, reason = it, labels = eventLabels))
//        }
//    }
//
//    private fun MutableList<Event>.addCallbackEnrolmentEvent() {
//        add(
//            EnrolmentCallbackEvent(
//                createdAt = DEFAULT_TIME,
//                guid = randomUUID(),
//                eventLabels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addCallbackRefusalEvent() {
//        add(
//            RefusalCallbackEvent(
//                createdAt = DEFAULT_TIME,
//                reason = "reason",
//                extra = "other_text",
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addCallbackVerificationEvent() {
//        IAppResponseTier.values().forEach {
//            add(
//                VerificationCallbackEvent(
//                    createdAt = DEFAULT_TIME,
//                    score = CallbackComparisonScore(randomUUID(), 0, it),
//                    labels = eventLabels
//                )
//            )
//        }
//    }
//
//    private fun MutableList<Event>.addCallbackIdentificationEvent() {
//        IAppResponseTier.values().forEach {
//            add(
//                IdentificationCallbackEvent(
//                    createdAt = DEFAULT_TIME,
//                    sessionId = randomUUID(),
//                    scores = listOf(CallbackComparisonScore(randomUUID(), 0, it)),
//                    labels = eventLabels
//                )
//            )
//        }
//    }
//
//    private fun MutableList<Event>.addCallbackConfirmationEvent() {
//        add(
//            ConfirmationCallbackEvent(
//                createdAt = DEFAULT_TIME,
//                identificationOutcome = true,
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addCalloutEnrolmentEvent() {
//        add(
//            EnrolmentCalloutEvent(
//                createdAt = DEFAULT_TIME,
//                projectId = testProject.id,
//                userId = DEFAULT_USER_ID,
//                moduleId = DEFAULT_MODULE_ID,
//                metadata = "metadata",
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addCalloutIdentificationEvent() {
//        add(
//            IdentificationCalloutEvent(
//                createdAt = DEFAULT_TIME,
//                projectId = testProject.id,
//                userId = DEFAULT_USER_ID,
//                moduleId = DEFAULT_MODULE_ID,
//                metadata = "metadata",
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addCalloutVerificationEvent() {
//        add(
//            VerificationCalloutEvent(
//                createdAt = DEFAULT_TIME,
//                projectId = testProject.id,
//                userId = DEFAULT_USER_ID,
//                moduleId = DEFAULT_MODULE_ID,
//                verifyGuid = randomUUID(),
//                metadata = "metadata",
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addCalloutLastBiomentricsEvent() {
//        add(
//            EnrolmentLastBiometricsCalloutEvent(
//                createdAt = DEFAULT_TIME,
//                projectId = testProject.id,
//                userId = DEFAULT_USER_ID,
//                moduleId = DEFAULT_MODULE_ID,
//                metadata = "metadata",
//                sessionId = randomUUID(),
//                labels = eventLabels
//            )
//        )
//    }
//
//    private fun MutableList<Event>.addCalloutConfirmationCallbackEvent() {
//        add(
//            ConfirmationCalloutEvent(
//                createdAt = DEFAULT_TIME,
//                projectId = testProject.id,
//                selectedGuid = randomUUID(),
//                sessionId = randomUUID(),
//                labels = eventLabels
//            )
//        )
//    }
//
//    // Never invoked, but used to enforce that the implementation of a test for every event class
//    private fun MutableList<Event>.addEventFor(type: EventType) {
//
//        when (type) {
//            SESSION_CAPTURE -> addSessionCaptureEvent()
//            ARTIFICIAL_TERMINATION -> addArtificialTerminationEvent()
//            AUTHENTICATION -> addAuthenticationEvent()
//            CONSENT -> addConsentEvent()
//            ENROLMENT_V2 -> addEnrolmentEvent()
//            AUTHORIZATION -> addAuthorizationEvent()
//            FINGERPRINT_CAPTURE -> addFingerprintCaptureEvent()
//            FINGERPRINT_CAPTURE_BIOMETRICS -> addFingerprintBiometricCaptureEvent()
//            ONE_TO_ONE_MATCH -> addOneToOneMatchEvent()
//            ONE_TO_MANY_MATCH -> addOneToManyMatchEvent()
//            PERSON_CREATION -> addPersonCreationEvent(
//                this.filterIsInstance<FingerprintCaptureEvent>().firstOrNull(),
//                this.filterIsInstance<FaceCaptureEvent>().firstOrNull()
//            )
//            ALERT_SCREEN -> addAlertScreenEvents()
//            GUID_SELECTION -> addGuidSelectionEvent()
//            CONNECTIVITY_SNAPSHOT -> addConnectivitySnapshotEvent()
//            REFUSAL -> addRefusalEvent()
//            CANDIDATE_READ -> addCandidateReadEvent()
//            SCANNER_CONNECTION -> addScannerConnectionEvent()
//            VERO_2_INFO_SNAPSHOT -> addVero2InfoSnapshotEvents()
//            SCANNER_FIRMWARE_UPDATE -> addScannerFirmwareUpdateEvent()
//            INVALID_INTENT -> addInvalidIntentEvent()
//            CALLOUT_CONFIRMATION -> addCalloutConfirmationCallbackEvent()
//            CALLOUT_IDENTIFICATION -> addCalloutIdentificationEvent()
//            CALLOUT_ENROLMENT -> addCalloutEnrolmentEvent()
//            CALLOUT_VERIFICATION -> addCalloutVerificationEvent()
//            CALLOUT_LAST_BIOMETRICS -> addCalloutLastBiomentricsEvent()
//            CALLBACK_IDENTIFICATION -> addCallbackIdentificationEvent()
//            CALLBACK_ENROLMENT -> addCallbackEnrolmentEvent()
//            CALLBACK_REFUSAL -> addCallbackRefusalEvent()
//            CALLBACK_VERIFICATION -> addCallbackVerificationEvent()
//            CALLBACK_ERROR -> addCallbackErrorEvent()
//            CALLBACK_CONFIRMATION -> addCallbackConfirmationEvent()
//            SUSPICIOUS_INTENT -> addSuspiciousIntentEvent()
//            INTENT_PARSING -> addIntentParsingEvent()
//            COMPLETION_CHECK -> addCompletionCheckEvent()
//            FACE_ONBOARDING_COMPLETE -> addFaceOnboardingCompleteEvent()
//            FACE_FALLBACK_CAPTURE -> addFaceFallbackCaptureEvent()
//            FACE_CAPTURE -> addFaceCaptureEvent()
//            FACE_CAPTURE_BIOMETRICS -> addFaceCaptureBiometricCaptureEvent()
//            FACE_CAPTURE_CONFIRMATION -> addFaceCaptureConfirmationEvent()
//            ENROLMENT_RECORD_DELETION,
//            ENROLMENT_RECORD_CREATION,
//            ENROLMENT_RECORD_MOVE,
//            ENROLMENT_V1 -> {
//            }
//        }.safeSealedWhens
//    }
// }
