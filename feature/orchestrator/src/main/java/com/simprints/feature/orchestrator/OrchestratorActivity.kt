package com.simprints.feature.orchestrator

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.chatbot.ChatOverlayHost
import com.simprints.feature.chatbot.ChatOverlayManager
import com.simprints.feature.clientapi.models.ClientApiConstants
import com.simprints.feature.orchestrator.databinding.ActivityOrchestratorBinding
import com.simprints.infra.config.store.LastCallingPackageStore
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionConstants
import com.simprints.infra.orchestration.data.results.AppResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class OrchestratorActivity : BaseActivity(), ChatOverlayHost {
    private val binding by viewBinding(ActivityOrchestratorBinding::inflate)

    @Inject
    lateinit var chatOverlayManager: ChatOverlayManager

    @Inject
    lateinit var activityTracker: ExecutionTracker

    @Inject
    lateinit var lastCallingPackageStore: LastCallingPackageStore

    /**
     * Flag for the navigation graph initialization state. The graph should only be initialized once
     * during the existence of this activity, and the flag tracks graph's state.
     */
    private var isGraphInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Simber.d("OrchestratorActivity.onCreate isGraphInitialized=$isGraphInitialized", tag = ORCHESTRATION)

        isGraphInitialized = savedInstanceState?.getBoolean(KEY_IS_GRAPH_INITIALIZED) ?: false
        lifecycle.addObserver(activityTracker)

        setContentView(binding.root)

        chatOverlayManager.attach(this, binding.orchestratorRoot)
        chatOverlayManager.clearWorkflow()
        chatOverlayManager.updateWorkflow(mapWorkflowName(intent.action.orEmpty()))
        observeNavigationForChatContext()

        binding.orchestrationHost.handleResult<AppResult>(
            this,
            R.id.orchestratorRootFragment,
        ) { result ->
            Simber.d("OrchestratorActivity result code ${result.resultCode}", tag = ORCHESTRATION)

            setResult(result.resultCode, Intent().putExtras(result.extras))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        Simber.d("OrchestratorActivity.onStart isGraphInitialized=$isGraphInitialized", tag = ORCHESTRATION)

        if (activityTracker.isMain(activity = this)) {
            if (!isGraphInitialized) {
                val action = intent.action.orEmpty()
                val extras = intent.extras ?: bundleOf()
                Simber.setUserProperty("Intent_received", action)
                Simber.setUserProperty("Caller", callingPackage.orEmpty())

                // Some co-sync functionality depends on the exact package name of the caller app,
                // e.g. to switch content providers of debug and release variants of the caller app
                extras.putString(ClientApiConstants.CALLER_PACKAGE_NAME, callingPackage)
                lastCallingPackageStore.lastCallingPackageName = callingPackage

                findNavController(R.id.orchestrationHost).setGraph(
                    R.navigation.graph_orchestration,
                    OrchestratorFragmentArgs(action, extras).toBundle(),
                )
                isGraphInitialized = true
            }
        } else {
            Simber.i("Orchestrator already executing, finishing with RESULT_CANCELED", tag = ORCHESTRATION)
            finish()
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle,
        outPersistentState: PersistableBundle,
    ) {
        outState.putBoolean(KEY_IS_GRAPH_INITIALIZED, isGraphInitialized)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    companion object {
        private const val KEY_IS_GRAPH_INITIALIZED = "KEY_IS_GRAPH_INITIALIZED"

        private val SCREEN_NAME_MAP = mapOf(
            "ClientApi" to "Processing Request",
            "Setup" to "Setup / Download",
            "Consent" to "Consent",
            "PrivacyNotice" to "Privacy Notice",
            "FaceCapture" to "Face Capture",
            "FaceDetection" to "Face Capture — Live Feedback",
            "Confirmation" to "Face Capture — Confirmation",
            "Preparation" to "Face Capture — Preparation",
            "FingerprintCapture" to "Fingerprint Capture",
            "ConnectScanner" to "Scanner Connection",
            "NfcOff" to "NFC Off",
            "Ota" to "Scanner Firmware Update",
            "ExitForm" to "Exit Form (user wants to quit)",
            "Alert" to "Error Alert",
            "Matcher" to "Matching",
            "SelectSubject" to "Select Subject",
            "FetchSubject" to "Fetching Subject",
            "ValidateSubjectPool" to "Validating ID Pool",
            "SelectSubjectAge" to "Select Age Group",
            "ExternalCredential" to "External Credential",
            "EnrolLastBiometric" to "Enrolling Last Biometric",
            "LoginForm" to "Login",
            "LoginQr" to "Login — QR Scanner",
        )
    }

    override fun minimizeChatOverlay() {
        chatOverlayManager.minimize()
    }

    private fun observeNavigationForChatContext() {
        try {
            val navController = findNavController(R.id.orchestrationHost)
            navController.addOnDestinationChangedListener { _, destination, _ ->
                val rawLabel = destination.label?.toString() ?: destination.displayName
                chatOverlayManager.updateScreen(mapScreenName(rawLabel))
            }
        } catch (_: Exception) {
            // NavController may not be available yet; context updates will still work
            // once navigation starts since the listener is added lazily
        }
    }

    private fun mapScreenName(label: String): String = SCREEN_NAME_MAP.entries
        .firstOrNull { label.contains(it.key, ignoreCase = true) }
        ?.value
        ?: label

    private fun mapWorkflowName(action: String): String = when {
        action.endsWith(ActionConstants.ACTION_ENROL) -> "Enrolment"
        action.endsWith(ActionConstants.ACTION_IDENTIFY) -> "Identification"
        action.endsWith(ActionConstants.ACTION_VERIFY) -> "Verification"
        action.endsWith(ActionConstants.ACTION_CONFIRM_IDENTITY) -> "Confirm Identity"
        action.endsWith(ActionConstants.ACTION_ENROL_LAST_BIOMETRICS) -> "Enrol Last Biometrics"
        else -> action
    }
}
