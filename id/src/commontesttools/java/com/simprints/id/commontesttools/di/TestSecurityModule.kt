package com.simprints.id.commontesttools.di

import android.content.Context
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.di.SecurityModule
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.secure.*
import com.simprints.id.secure.securitystate.local.SecurityStateLocalDataSource
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSource
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.subjects.master.SubjectsSyncManager
import com.simprints.id.services.securitystate.SecurityStateScheduler
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule
import kotlinx.coroutines.ExperimentalCoroutinesApi

class TestSecurityModule(
    private val loginActivityHelperRule: DependencyRule = RealRule,
    private val loginViewModelFactoryRule: DependencyRule = RealRule,
    private val projectAuthenticatorRule: DependencyRule = RealRule,
    private val authenticationHelperRule: DependencyRule = RealRule,
    private val safetyNetClientRule: DependencyRule = RealRule,
    private val signerManagerRule: DependencyRule = RealRule,
    private val securityStateRepositoryRule: DependencyRule = RealRule
) : SecurityModule() {

    override fun provideSignerManager(
        projectRepository: ProjectRepository,
        remoteDbManager: RemoteDbManager,
        loginInfoManager: LoginInfoManager,
        preferencesManager: PreferencesManager,
        subjectsSyncManager: SubjectsSyncManager,
        syncManager: SyncManager,
        securityStateScheduler: SecurityStateScheduler,
        longConsentRepository: LongConsentRepository,
        eventRepository: EventRepository,
        baseUrlProvider: BaseUrlProvider,
        remoteConfigWrapper: RemoteConfigWrapper
    ): SignerManager = signerManagerRule.resolveDependency {
        super.provideSignerManager(
            projectRepository,
            remoteDbManager,
            loginInfoManager,
            preferencesManager,
            subjectsSyncManager,
            syncManager,
            securityStateScheduler,
            longConsentRepository,
            eventRepository,
            baseUrlProvider,
            remoteConfigWrapper
        )
    }

    override fun provideLoginActivityHelper(
        securityStateRepository: SecurityStateRepository
    ): LoginActivityHelper {
        return loginActivityHelperRule.resolveDependency {
            super.provideLoginActivityHelper(securityStateRepository)
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

    @ExperimentalCoroutinesApi
    override fun provideSecurityStateRepository(
        remoteDataSource: SecurityStateRemoteDataSource,
        localDataSource: SecurityStateLocalDataSource
    ): SecurityStateRepository = securityStateRepositoryRule.resolveDependency {
        super.provideSecurityStateRepository(remoteDataSource, localDataSource)
    }

}
