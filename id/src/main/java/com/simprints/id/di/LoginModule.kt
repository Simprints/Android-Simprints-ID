package com.simprints.id.di

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.tools.LoginActivityHelperImpl
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.LongConsentManager
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
    open fun provideLoginViewModelFactory(
        authenticationHelper: AuthenticationHelper
    ): LoginViewModelFactory {
        return LoginViewModelFactory(authenticationHelper)
    }

    @Provides
    open fun provideProjectAuthenticator(
        loginInfoManager: LoginInfoManager,
        simApiClientFactory: SimApiClientFactory,
        baseUrlProvider: BaseUrlProvider,
        safetyNetClient: SafetyNetClient,
        secureDataManager: SecureLocalDbKeyProvider,
        projectRemoteDataSource: ProjectRemoteDataSource,
        signerManager: SignerManager,
        remoteConfigWrapper: RemoteConfigWrapper,
        longConsentManager: LongConsentManager,
        preferencesManager: PreferencesManager
    ) : ProjectAuthenticator = ProjectAuthenticatorImpl(
        loginInfoManager,
        simApiClientFactory,
        baseUrlProvider,
        safetyNetClient,
        secureDataManager,
        projectRemoteDataSource,
        signerManager,
        remoteConfigWrapper,
        longConsentManager,
        preferencesManager
    )

    @Provides
    open fun provideAuthenticationHelper(
        crashReportManager: CrashReportManager,
        loginInfoManager: LoginInfoManager,
        timeHelper: TimeHelper,
        projectAuthenticator: ProjectAuthenticator,
        sessionEventsManager: SessionEventsManager
    ): AuthenticationHelper {
        return AuthenticationHelperImpl(
            crashReportManager,
            loginInfoManager,
            timeHelper,
            projectAuthenticator,
            sessionEventsManager
        )
    }

    @Provides
    open fun provideSafetyNetClient(
        context: Context
    ): SafetyNetClient = SafetyNet.getClient(context)

}
