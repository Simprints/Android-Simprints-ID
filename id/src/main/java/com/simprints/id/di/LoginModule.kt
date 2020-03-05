package com.simprints.id.di

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.activities.login.repository.LoginRepository
import com.simprints.id.activities.login.repository.LoginRepositoryImpl
import com.simprints.id.activities.login.tools.AuthenticationHelper
import com.simprints.id.activities.login.tools.AuthenticationHelperImpl
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.domain.SessionEventsManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.ProjectAuthenticatorImpl
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.tools.TimeHelper
import dagger.Module
import dagger.Provides

@Module
class LoginModule {

    @Provides
    fun provideLoginViewModelFactory(loginRepository: LoginRepository): LoginViewModelFactory {
        return LoginViewModelFactory(loginRepository)
    }

    @Provides
    fun provideLoginRepository(
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
    fun provideProjectAuthenticator(
        safetyNetClient: SafetyNetClient,
        secureApiClient: SecureApiInterface
    ) : ProjectAuthenticator {
        return ProjectAuthenticatorImpl(safetyNetClient, secureApiClient)
    }

    @Provides
    fun provideAuthenticationHelper(
        crashReportManager: CrashReportManager,
        loginInfoManager: LoginInfoManager
    ): AuthenticationHelper {
        return AuthenticationHelperImpl(crashReportManager, loginInfoManager)
    }

    @Provides
    fun provideSafetyNetClient(context: Context): SafetyNetClient = SafetyNet.getClient(context)

}
