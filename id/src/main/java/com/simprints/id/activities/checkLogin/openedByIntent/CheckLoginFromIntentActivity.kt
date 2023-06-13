package com.simprints.id.activities.checkLogin.openedByIntent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.VISIBLE
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.extentions.removeAnimationsToNextActivity
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.feature.alert.ShowAlertWrapper
import com.simprints.feature.alert.toArgs
import com.simprints.feature.login.LoginContract
import com.simprints.feature.login.LoginError
import com.simprints.feature.login.LoginError.*
import com.simprints.feature.login.ShowLoginWrapper
import com.simprints.id.activities.orchestrator.OrchestratorActivity
import com.simprints.id.databinding.CheckLoginFromIntentScreenBinding
import com.simprints.id.di.IdAppModule
import com.simprints.id.alert.AlertType
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.domain.moduleapi.app.fromModuleApiToDomain
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import com.simprints.moduleapi.app.responses.IAppResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

// App launched when user open SimprintsID using a client app (by intent)
@AndroidEntryPoint
open class CheckLoginFromIntentActivity : BaseSplitActivity(), CheckLoginFromIntentContract.View {

    private val binding by viewBinding(CheckLoginFromIntentScreenBinding::inflate)

    @Inject
    lateinit var presenterFactory: IdAppModule.CheckLoginFromIntentPresenterFactory

    override val viewPresenter: CheckLoginFromIntentContract.Presenter by lazy {
        presenterFactory.create(this)
    }

    private val showAlert = registerForActivityResult(ShowAlertWrapper()) {
        val alertType = AlertType.fromPayload(it)
        val response = AppErrorResponse(AppErrorResponse.Reason.fromDomainAlertTypeToAppErrorType(alertType))

        setResultErrorAndFinish(DomainToModuleApiAppResponse.fromDomainToModuleApiAppErrorResponse(response))
    }

    private val showLoginFlow = registerForActivityResult(ShowLoginWrapper()) {
        if (it.isSuccess) {
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                    viewPresenter.checkSignedInStateIfPossible()
                }
            }
        } else handleLoginFormErrors(it.error)
    }

    private fun handleLoginFormErrors(error: LoginError?) = when (error) {
        null, LoginNotCompleted -> {
            viewPresenter.onLoginScreenErrorReturn(AppErrorResponse(AppErrorResponse.Reason.LOGIN_NOT_COMPLETE))
        }

        MissingPlayServices -> showAlert.launch(AlertType.MISSING_GOOGLE_PLAY_SERVICES.toAlertConfig().toArgs())
        OutdatedPlayServices -> showAlert.launch(AlertType.GOOGLE_PLAY_SERVICES_OUTDATED.toAlertConfig().toArgs())
        IntegrityServiceError -> showAlert.launch(AlertType.INTEGRITY_SERVICE_ERROR.toAlertConfig().toArgs())
        MissingOrOutdatedPlayServices -> showAlert.launch(AlertType.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP.toAlertConfig().toArgs())
        Unknown -> showAlert.launch(AlertType.UNEXPECTED_ERROR.toAlertConfig().toArgs())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = getString(IDR.string.title_activity_front)

        lifecycleScope.launch {
            viewPresenter.onViewCreated(savedInstanceState != null)
        }
    }

    override fun setResultErrorAndFinish(appResponse: IAppErrorResponse) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IAppResponse.BUNDLE_KEY, appResponse)
        })
        finish()
    }

    override fun parseRequest() = intent.extras
        ?.getParcelable<IAppRequest>(IAppRequest.BUNDLE_KEY)
        ?.fromModuleApiToDomain()
        ?: throw InvalidAppRequest()

    override fun getCheckCallingApp() = getCallingPackageName()

    override fun showConfirmationText() {
        binding.confirmationSent.visibility = VISIBLE
        binding.redirectingBack.visibility = VISIBLE
    }

    open fun getCallingPackageName(): String {
        return callingPackage ?: ""
    }

    override fun openAlertActivityForError(alertType: AlertType) {
        showAlert.launch(alertType.toAlertConfig().toArgs())
    }

    override fun openLoginActivity(appRequest: AppRequest) {
        showLoginFlow.launch(LoginContract.toArgs(appRequest.projectId, appRequest.userId))
    }

    override fun openOrchestratorActivity(appRequest: AppRequest) {
        val intent = Intent(this, OrchestratorActivity::class.java).apply {
            putExtra(AppRequest.BUNDLE_KEY, appRequest)
            addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        }
        startActivity(intent)
        this.removeAnimationsToNextActivity()
        finish()
    }

    override fun finishCheckLoginFromIntentActivity() {
        finish()
    }
}
