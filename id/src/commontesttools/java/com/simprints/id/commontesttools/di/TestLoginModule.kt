package com.simprints.id.commontesttools.di

import android.content.Context
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.activities.login.repository.LoginRepository
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
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.SignerManager
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule

class TestLoginModule(
    private val loginActivityHelperRule: DependencyRule = RealRule,
    private val loginViewModelFactoryRule: DependencyRule = RealRule,
    private val loginRepositoryRule: DependencyRule = RealRule,
    private val projectAuthenticatorRule: DependencyRule = RealRule,
    private val authenticationHelperRule: DependencyRule = RealRule,
    private val safetyNetClientRule: DependencyRule = RealRule
) : LoginModule() {

    override fun provideLoginActivityHelper(): LoginActivityHelper {
        return loginActivityHelperRule.resolveDependency {
            super.provideLoginActivityHelper()
        }
    }

    override fun provideLoginViewModelFactory(loginRepository: LoginRepository): LoginViewModelFactory {
        return loginViewModelFactoryRule.resolveDependency {
            super.provideLoginViewModelFactory(loginRepository)
        }
    }

    override fun provideLoginRepository(
        projectAuthenticator: ProjectAuthenticator,
        authenticationHelper: AuthenticationHelper,
        sessionRepository: SessionRepository,
        timeHelper: TimeHelper
    ): LoginRepository {
        return loginRepositoryRule.resolveDependency {
            super.provideLoginRepository(
                projectAuthenticator,
                authenticationHelper,
                sessionRepository,
                timeHelper
            )
        }
    }

    override fun provideProjectAuthenticator(
        secureApiClient: SecureApiInterface,
        loginInfoManager: LoginInfoManager,
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
                secureApiClient,
                loginInfoManager,
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
        loginInfoManager: LoginInfoManager
    ): AuthenticationHelper {
        return authenticationHelperRule.resolveDependency {
            super.provideAuthenticationHelper(crashReportManager, loginInfoManager)
        }
    }

    override fun provideSafetyNetClient(context: Context): SafetyNetClient {
        return safetyNetClientRule.resolveDependency {
            super.provideSafetyNetClient(context)
        }
    }

}
