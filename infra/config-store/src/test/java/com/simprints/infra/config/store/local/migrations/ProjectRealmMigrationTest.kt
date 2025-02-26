package com.simprints.infra.config.store.local.migrations

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.local.models.ProtoProject
import com.simprints.infra.config.store.testtools.protoProject
import com.simprints.infra.enrolment.records.realm.store.RealmWrapper
import com.simprints.infra.enrolment.records.realm.store.models.DbProject
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.RealmSingleQuery
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ProjectRealmMigrationTest {
    companion object {
        private const val PROJECT_ID = "projectId"
    }

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var realmWrapper: RealmWrapper

    @MockK
    private lateinit var realm: Realm

    @MockK
    private lateinit var mutableRealm: MutableRealm

    @MockK
    private lateinit var realmQuery: RealmQuery<DbProject>

    private lateinit var blockCapture: CapturingSlot<(MutableRealm) -> Any>
    private lateinit var readBlockCapture: CapturingSlot<(Realm) -> Any>

    private lateinit var projectRealmMigration: ProjectRealmMigration

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        blockCapture = slot()
        readBlockCapture = slot()

        every { realm.query<DbProject>(any(), any(), any()) } returns realmQuery
        coEvery { realmWrapper.readRealm(capture(readBlockCapture)) } answers {
            readBlockCapture.captured.invoke(realm)
        }
        coEvery { realmWrapper.writeRealm(capture(blockCapture)) } answers {
            blockCapture.captured.invoke(mutableRealm)
        }

        projectRealmMigration = ProjectRealmMigration(authStore, realmWrapper)
    }

    @Test
    fun `shouldMigrate should return true only if the project is signed in and the current data empty`() = runTest {
        every { authStore.signedInProjectId } returns "project_id"

        val shouldMigrate =
            projectRealmMigration.shouldMigrate(ProtoProject.getDefaultInstance())
        assertThat(shouldMigrate).isTrue()
    }

    @Test
    fun `shouldMigrate should return false if the project is not signed in`() = runTest {
        every { authStore.signedInProjectId } returns ""

        val shouldMigrate =
            projectRealmMigration.shouldMigrate(ProtoProject.getDefaultInstance())
        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `shouldMigrate should return false if the current data is not empty`() = runTest {
        every { authStore.signedInProjectId } returns "project_id"

        val shouldMigrate = projectRealmMigration.shouldMigrate(protoProject)
        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `migrate should not migrate the data if realm returns null`() = runTest {
        every { authStore.signedInProjectId } returns PROJECT_ID
        every { realmQuery.first() } returns mockQueryResult(null)

        val migratedData = projectRealmMigration.migrate(protoProject)
        assertThat(migratedData).isEqualTo(protoProject)
    }

    @Test
    fun `migrate should migrate the data if realm doesn't return null`() = runTest {
        val dbProject = DbProject()
        dbProject.id = "id"
        dbProject.name = "project name"
        val expectedProtoProject =
            ProtoProject
                .newBuilder()
                .setId("id")
                .setName("project name")
                .build()

        every { authStore.signedInProjectId } returns PROJECT_ID
        every { realmQuery.first() } returns mockQueryResult(dbProject)

        val migratedData = projectRealmMigration.migrate(ProtoProject.newBuilder().build())
        assertThat(migratedData).isEqualTo(expectedProtoProject)
    }

    private fun mockQueryResult(dbProject: DbProject?): RealmSingleQuery<DbProject> =
        mockk(relaxed = true) { every { find() } returns dbProject }

    @Test
    fun `cleanup should delete all the projects`() = runTest {
        projectRealmMigration.cleanUp()

        verify(exactly = 1) { mutableRealm.delete(DbProject::class) }
    }
}
