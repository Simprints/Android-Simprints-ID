package com.simprints.id.di

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.activities.login.repository.LoginRepository
import com.simprints.id.activities.login.repository.LoginRepositoryImpl
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.tools.LoginActivityHelperImpl
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.session.domain.SessionEventsManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.secure.*
import com.simprints.id.tools.TimeHelper
import dagger.Module
import dagger.Provides

@Module
open class LoginModule {

    @Provides
    open fun provideLoginActivityHelper(): LoginActivityHelper = LoginActivityHelperImpl()

    @Provides
    open fun provideLoginViewModelFactory(loginRepository: LoginRepository): LoginViewModelFactory {
        return LoginViewModelFactory(loginRepository)
    }

    @Provides
    open fun provideLoginRepository(
        projectAuthenticator: ProjectAuthenticator,
        authenticationHelper: AuthenticationHelper,
        sessionEventsManager: SessionEventsManager,
        timeHelper: TimeHelper
    ): LoginRepository = LoginRepositoryImpl(
        projectAuthenticator,
        authenticationHelper,
        sessionEventsManager,
        timeHelper
    )

    @Provides
    open fun provideSecreteManager(loginInfoManager: LoginInfoManager): ProjectSecretManager =
        ProjectSecretManager(loginInfoManager)

    @Provides
    open fun provideProjectAuthenticator(
        secureApiClient: SecureApiInterface,
        projectSecretManager: ProjectSecretManager,
        safetyNetClient: SafetyNetClient,
        secureDataManager: SecureLocalDbKeyProvider,
        projectRemoteDataSource: ProjectRemoteDataSource,
        signerManager: SignerManager,
        remoteConfigWrapper: RemoteConfigWrapper,
        longConsentRepository: LongConsentRepository,
        preferencesManager: PreferencesManager
    ) : ProjectAuthenticator = ProjectAuthenticatorImpl(
        secureApiClient,
        projectSecretManager,
        safetyNetClient,
        secureDataManager,
        projectRemoteDataSource,
        signerManager,
        remoteConfigWrapper,
        longConsentRepository,
        preferencesManager
    )

    @Provides
    open fun provideAuthenticationHelper(
        crashReportManager: CrashReportManager,
        loginInfoManager: LoginInfoManager
    ): AuthenticationHelper {
        return AuthenticationHelper(
            crashReportManager,
            loginInfoManager
        )
    }

    @Provides
    open fun provideSafetyNetClient(
        context: Context
    ): SafetyNetClient = SafetyNet.getClient(context)

}
