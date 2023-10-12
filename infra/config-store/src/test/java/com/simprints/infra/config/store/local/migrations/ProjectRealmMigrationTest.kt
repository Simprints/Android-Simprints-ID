package com.simprints.infra.config.store.local.migrations

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.local.models.ProtoProject
import com.simprints.infra.config.store.testtools.protoProject
import com.simprints.infra.realm.RealmWrapper
import com.simprints.infra.realm.models.DbProject
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.realm.Realm
import io.realm.RealmQuery
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ProjectRealmMigrationTest {

    companion object {
        private const val PROJECT_ID = "projectId"
    }

    private val authStore = mockk<com.simprints.infra.authstore.AuthStore>()
    private lateinit var realmWrapper: RealmWrapper
    private lateinit var blockCapture: CapturingSlot<(Realm) -> Any>
    private lateinit var realm: Realm
    private val realmQuery = mockk<RealmQuery<DbProject>>()
    private lateinit var projectRealmMigration: ProjectRealmMigration

    @Before
    fun setup() {
        realm = mockk(relaxed = true) {
            every { where<DbProject>(any()) } returns realmQuery
        }

        realmWrapper = mockk {
            blockCapture = slot()
            coEvery {
                useRealmInstance(capture(blockCapture))
            } answers { blockCapture.captured.invoke(realm) }
        }

        every { realmQuery.equalTo(any(), PROJECT_ID) } returns realmQuery

        projectRealmMigration = ProjectRealmMigration(authStore, realmWrapper)
    }

    @Test
    fun `shouldMigrate should return true only if the project is signed in and the current data empty`() =
        runTest {
            every { authStore.signedInProjectId } returns "project_id"

            val shouldMigrate =
                projectRealmMigration.shouldMigrate(ProtoProject.getDefaultInstance())
            assertThat(shouldMigrate).isTrue()
        }

    @Test
    fun `shouldMigrate should return false if the project is not signed in`() =
        runTest {
            every { authStore.signedInProjectId } returns ""

            val shouldMigrate =
                projectRealmMigration.shouldMigrate(ProtoProject.getDefaultInstance())
            assertThat(shouldMigrate).isFalse()
        }

    @Test
    fun `shouldMigrate should return false if the current data is not empty`() =
        runTest {
            every { authStore.signedInProjectId } returns "project_id"

            val shouldMigrate = projectRealmMigration.shouldMigrate(protoProject)
            assertThat(shouldMigrate).isFalse()
        }

    @Test
    fun `migrate should not migrate the data if realm returns null`() = runTest {
        every { authStore.signedInProjectId } returns PROJECT_ID
        every { realmQuery.findFirst() } returns null

        val migratedData = projectRealmMigration.migrate(protoProject)
        assertThat(migratedData).isEqualTo(protoProject)
    }

    @Test
    fun `migrate should migrate the data if realm doesn't return null`() = runTest {
        val dbProject = DbProject()
        dbProject.id = "id"
        dbProject.name = "project name"
        val expectedProtoProject = ProtoProject.newBuilder().setId("id").setName("project name").build()

        every { authStore.signedInProjectId } returns PROJECT_ID
        every { realmQuery.findFirst() } returns dbProject

        val migratedData = projectRealmMigration.migrate(ProtoProject.newBuilder().build())
        assertThat(migratedData).isEqualTo(expectedProtoProject)
    }

    @Test
    fun `cleanup should delete all the projects`() = runTest {
        projectRealmMigration.cleanUp()

        verify(exactly = 1) { realm.beginTransaction() }
        verify(exactly = 1) { realm.delete(DbProject::class.java) }
        verify(exactly = 1) { realm.commitTransaction() }
    }
}
