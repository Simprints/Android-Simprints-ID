package com.simprints.id.commontesttools.di

import android.content.Context
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.di.SyncModule
import com.simprints.id.services.sync.events.master.EventSyncStateProcessor
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.testtools.common.di.DependencyRule
import javax.inject.Singleton

class TestSyncModule(
    private val peopleDownSyncScopeRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val peopleSessionEventsSyncManager: DependencyRule = DependencyRule.RealRule,
    private val peopleSyncStateProcessor: DependencyRule = DependencyRule.RealRule,
    private val peopleSyncManagerRule: DependencyRule = DependencyRule.RealRule,
    private val syncManagerRule: DependencyRule = DependencyRule.RealRule,
    private val peopleDownSyncWorkersBuilderRule: DependencyRule = DependencyRule.RealRule,
    private val peopleUpSyncWorkersBuilderRule: DependencyRule = DependencyRule.RealRule,
    private val peopleUpSyncDaoRule: DependencyRule = DependencyRule.RealRule,
    private val peopleDownSyncDaoRule: DependencyRule = DependencyRule.RealRule,
    private val peopleUpSyncManagerRule: DependencyRule = DependencyRule.RealRule,
    private val peopleUpSyncScopeRepositoryRule: DependencyRule = DependencyRule.RealRule
) : SyncModule() {


    @Singleton
    override fun providePeopleSyncStateProcessor(
        ctx: Context,
        eventSyncCache: EventSyncCache,
        personRepository: SubjectRepository
    ): EventSyncStateProcessor = peopleSyncStateProcessor.resolveDependency {
        super.providePeopleSyncStateProcessor(ctx, eventSyncCache, personRepository)
    }
}
