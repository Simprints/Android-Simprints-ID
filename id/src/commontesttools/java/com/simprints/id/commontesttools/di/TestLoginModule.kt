package com.simprints.id.commontesttools.di

import android.content.Context
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.di.LoginModule
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.secure.BaseUrlProvider
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.SignerManager
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule

class TestLoginModule(
    private val loginActivityHelperRule: DependencyRule = RealRule,
    private val loginViewModelFactoryRule: DependencyRule = RealRule,
    private val projectAuthenticatorRule: DependencyRule = RealRule,
    private val authenticationHelperRule: DependencyRule = RealRule,
    private val safetyNetClientRule: DependencyRule = RealRule
) : LoginModule() {

    override fun provideLoginActivityHelper(): LoginActivityHelper {
        return loginActivityHelperRule.resolveDependency {
            super.provideLoginActivityHelper()
        }
    }

    override fun provideLoginViewModelFactory(
        authenticationHelper: AuthenticationHelper
    ): LoginViewModelFactory {
        return loginViewModelFactoryRule.resolveDependency {
            super.provideLoginViewModelFactory(authenticationHelper)
        }
    }

    override fun provideProjectAuthenticator(
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
    ): ProjectAuthenticator {
        return projectAuthenticatorRule.resolveDependency {
            super.provideProjectAuthenticator(
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
        }
    }

    override fun provideAuthenticationHelper(
        crashReportManager: CrashReportManager,
        loginInfoManager: LoginInfoManager,
        timeHelper: TimeHelper,
        projectAuthenticator: ProjectAuthenticator,
        sessionEventsManager: SessionEventsManager
    ): AuthenticationHelper {
        return authenticationHelperRule.resolveDependency {
            super.provideAuthenticationHelper(
                crashReportManager,
                loginInfoManager,
                timeHelper,
                projectAuthenticator,
                sessionEventsManager
            )
        }
    }

    override fun provideSafetyNetClient(context: Context): SafetyNetClient {
        return safetyNetClientRule.resolveDependency {
            super.provideSafetyNetClient(context)
        }
    }

}
