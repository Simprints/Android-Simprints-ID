package com.simprints.id.commontesttools.di

import android.content.Context
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.di.LoginModule
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.secure.*
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
    ) : ProjectAuthenticator {
        return projectAuthenticatorRule.resolveDependency {
            super.provideProjectAuthenticator(
                authManager,
                projectSecretManager,
                loginInfoManager,
                simApiClientFactory,
                baseUrlProvider,
                safetyNetClient,
                secureDataManager,
                projectRemoteDataSource,
                signerManager,
                remoteConfigWrapper,
                longConsentRepository,
                preferencesManager,
                attestationManager,
                authenticationDataManager
            )
        }
    }

    override fun provideAuthenticationHelper(
            crashReportManager: CrashReportManager,
            loginInfoManager: LoginInfoManager,
            timeHelper: TimeHelper,
            projectAuthenticator: ProjectAuthenticator,
            eventRepository: EventRepository
    ): AuthenticationHelper {
        return authenticationHelperRule.resolveDependency {
            super.provideAuthenticationHelper(
                crashReportManager,
                loginInfoManager,
                timeHelper,
                projectAuthenticator,
                eventRepository
            )
        }
    }

    override fun provideSafetyNetClient(context: Context): SafetyNetClient {
        return safetyNetClientRule.resolveDependency {
            super.provideSafetyNetClient(context)
        }
    }

}
