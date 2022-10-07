package com.simprints.id.testtools.di

import android.content.Context
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.consent.longconsent.local.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.remote.LongConsentRemoteDataSource
import com.simprints.id.di.DataModule
import com.simprints.infra.login.LoginManager
import com.simprints.testtools.common.di.DependencyRule

class TestDataModule(
    private val longConsentRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val longConsentLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
) : DataModule() {

    override fun provideLongConsentLocalDataSource(
        context: Context,
        loginManager: LoginManager
    ): LongConsentLocalDataSource =
        longConsentLocalDataSourceRule.resolveDependency {
            super.provideLongConsentLocalDataSource(
                context,
                loginManager
            )
        }

    override fun provideLongConsentRepository(
        longConsentLocalDataSource: LongConsentLocalDataSource,
        longConsentRemoteDataSource: LongConsentRemoteDataSource
    ): LongConsentRepository =
        longConsentRepositoryRule.resolveDependency {
            super.provideLongConsentRepository(
                longConsentLocalDataSource,
                longConsentRemoteDataSource
            )
        }
}

