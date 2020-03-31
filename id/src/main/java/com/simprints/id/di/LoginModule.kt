package com.simprints.id.di

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.tools.LoginActivityHelperImpl
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.session.SessionRepository
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
    open fun provideLoginViewModelFactory(
        authenticationHelper: AuthenticationHelper
    ): LoginViewModelFactory {
        return LoginViewModelFactory(authenticationHelper)
    }

    @Provides
    open fun provideSecreteManager(loginInfoManager: LoginInfoManager): ProjectSecretManager =
        ProjectSecretManager(loginInfoManager)

    @Provides
    open fun provideAuthManager(
        apiClientFactory: SimApiClientFactory,
        baseUrlProvider: BaseUrlProvider
    ): AuthManager = AuthManagerImpl(apiClientFactory, baseUrlProvider)

    @Provides
    open fun provideAuthenticationDataManager(apiClientFactory: SimApiClientFactory,
                                              baseUrlProvider: BaseUrlProvider): AuthenticationDataManager =
        AuthenticationDataManagerImpl(apiClientFactory, baseUrlProvider)

    @Provides
    open fun provideAttestationManager(): AttestationManager = AttestationManagerImpl()

    @Provides
    open fun provideProjectAuthenticator(
        authManager: AuthManager,
        projectSecretManager: ProjectSecretManager,
        loginInfoManager: LoginInfoManager,
        simApiClientFactory: SimApiClientFactory,
        baseUrlProvider: BaseUrlProvider,
        safetyNetClient: SafetyNetClient,
        secureDataManager: SecureLocalDbKeyProvider,
        projectRemoteDataSource: ProjectRemoteDataSource,
        signerManager: SignerManager,
        remoteConfigWrapper: RemoteConfigWrapper,
        longConsentRepository: LongConsentRepository,
        preferencesManager: PreferencesManager,
        attestationManager: AttestationManager,
        authenticationDataManager: AuthenticationDataManager
    ) : ProjectAuthenticator = ProjectAuthenticatorImpl(
        authManager,
        projectSecretManager,
        safetyNetClient,
        secureDataManager,
        projectRemoteDataSource,
        signerManager,
        remoteConfigWrapper,
        longConsentRepository,
        attestationManager,
        authenticationDataManager
    )

    @Provides
    open fun provideAuthenticationHelper(
        crashReportManager: CrashReportManager,
        loginInfoManager: LoginInfoManager,
        timeHelper: TimeHelper,
        projectAuthenticator: ProjectAuthenticator,
        sessionRepository: SessionRepository
    ): AuthenticationHelper {
        return AuthenticationHelperImpl(
            crashReportManager,
            loginInfoManager,
            timeHelper,
            projectAuthenticator,
            sessionRepository
        )
    }

    @Provides
    open fun provideSafetyNetClient(
        context: Context
    ): SafetyNetClient = SafetyNet.getClient(context)

}
