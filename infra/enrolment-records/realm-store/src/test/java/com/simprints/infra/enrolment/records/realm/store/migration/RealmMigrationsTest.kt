package com.simprints.infra.enrolment.records.realm.store.migration

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.enrolment.records.realm.store.migration.oldschemas.SubjectsSchemaV10
import com.simprints.infra.enrolment.records.realm.store.migration.oldschemas.SubjectsSchemaV11
import com.simprints.infra.enrolment.records.realm.store.migration.oldschemas.SubjectsSchemaV12
import com.simprints.infra.enrolment.records.realm.store.migration.oldschemas.SubjectsSchemaV13
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.realm.kotlin.dynamic.DynamicMutableRealm
import io.realm.kotlin.dynamic.DynamicMutableRealmObject
import io.realm.kotlin.dynamic.DynamicRealm
import io.realm.kotlin.dynamic.DynamicRealmObject
import io.realm.kotlin.dynamic.getNullableValue
import io.realm.kotlin.dynamic.getValue
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.migration.AutomaticSchemaMigration
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmUUID
import org.junit.Before
import org.junit.Test
import java.util.Date

class RealmMigrationsTest {
    @MockK
    private lateinit var migrationContext: AutomaticSchemaMigration.MigrationContext

    @MockK
    private lateinit var oldRealm: DynamicRealm

    @MockK
    private lateinit var newRealm: DynamicMutableRealm

    private lateinit var realmMigrations: RealmMigrations

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { migrationContext.oldRealm } returns oldRealm
        every { migrationContext.newRealm } returns newRealm

        realmMigrations = RealmMigrations()
    }

    @Test
    fun `when migrating 9 to 10 moves data from person table to subject table`() {
        setVersions(9L, 10L)

        val oldObject = DynamicMutableRealmObject.create(
            type = SubjectsSchemaV10.PERSON_TABLE,
            properties = mapOf(
                SubjectsSchemaV10.PERSON_PATIENT_ID to GUID,
                SubjectsSchemaV10.PERSON_USER_ID to USER_ID,
                SubjectsSchemaV10.PERSON_PROJECT_ID to PROJECT_ID,
                SubjectsSchemaV10.PERSON_MODULE_ID to MODULE_ID,
                SubjectsSchemaV10.PERSON_CREATE_TIME to Date(0),
                SubjectsSchemaV10.PERSON_UPDATE_TIME to null,
                SubjectsSchemaV10.PERSON_TO_SYNC to true,
                SubjectsSchemaV10.PERSON_FINGERPRINT_SAMPLES to realmListOf<DbFingerprintSample>(),
                SubjectsSchemaV10.PERSON_FACE_SAMPLES to realmListOf<DbFaceSample>(),
            ),
        )

        val newObjectSlot = slot<DynamicMutableRealmObject>()
        every { newRealm.copyToRealm(capture(newObjectSlot)) } returns mockk()

        val enumeratorSlot = slot<(DynamicRealmObject, DynamicMutableRealmObject?) -> Unit>()
        every { migrationContext.enumerate(any(), capture(enumeratorSlot)) } answers {
            enumeratorSlot.captured.invoke(oldObject, null)
        }

        realmMigrations.migrate(migrationContext)

        verify { migrationContext.enumerate(eq(SubjectsSchemaV10.PERSON_TABLE), any()) }

        val newObject = newObjectSlot.captured
        assertThat(newObject.type).isEqualTo(SubjectsSchemaV10.SUBJECT_TABLE)
        assertThat(newObject.getValue<String>(SubjectsSchemaV10.SUBJECT_ID)).isEqualTo(GUID)
        assertThat(newObject.getValue<String>(SubjectsSchemaV10.ATTENDANT_ID)).isEqualTo(USER_ID)
        assertThat(newObject.getValue<String>(SubjectsSchemaV10.PERSON_PROJECT_ID)).isEqualTo(PROJECT_ID)
        assertThat(newObject.getValue<String>(SubjectsSchemaV10.PERSON_MODULE_ID)).isEqualTo(MODULE_ID)
        assertThat(newObject.getNullableValue<RealmInstant>(SubjectsSchemaV10.PERSON_CREATE_TIME)).isEqualTo(RealmInstant.from(0, 0))
        assertThat(newObject.getNullableValue<RealmInstant>(SubjectsSchemaV10.PERSON_UPDATE_TIME)).isNull()
        assertThat(newObject.getValueList(SubjectsSchemaV10.PERSON_FINGERPRINT_SAMPLES, DbFingerprintSample::class)).isEmpty()
        assertThat(newObject.getValueList(SubjectsSchemaV10.PERSON_FACE_SAMPLES, DbFaceSample::class)).isEmpty()
    }

    @Test
    fun `when migrating 10 to 11 adds format field to sample table`() {
        setVersions(10L, 11L)

        val newFingerprintObject = captureNewObjectForTable(SubjectsSchemaV11.FINGERPRINT_TABLE)
        val newFaceObject = captureNewObjectForTable(SubjectsSchemaV11.FACE_TABLE)

        realmMigrations.migrate(migrationContext)

        verify { newFingerprintObject.set(eq(SubjectsSchemaV11.FIELD_FORMAT), any<String>()) }
        verify { newFaceObject.set(eq(SubjectsSchemaV11.FIELD_FORMAT), any<String>()) }
    }

    @Test
    fun `when migrating 11 to 12 deduplicate samples`() {
        setVersions(11L, 12L)

        // Setup to stub orphan deletion flow
        setupSubjectQueryResponse(emptyList())
        every { oldRealm.query(any(), any(), any<Set<String>>()).find() } returns mockk<RealmResults<DynamicRealmObject>>()

        val newFingerObject = setupDeduplicationTestForTable(SubjectsSchemaV11.FINGERPRINT_TABLE, SubjectsSchemaV12.FINGERPRINT_FIELD_ID)
        val newFaceObject = setupDeduplicationTestForTable(SubjectsSchemaV11.FACE_TABLE, SubjectsSchemaV12.FACE_FIELD_ID)

        realmMigrations.migrate(migrationContext)

        verify(exactly = 1) { oldRealm.query(eq(SubjectsSchemaV11.FINGERPRINT_TABLE), any(), eq(UNIQUE_ID)).count().find() }
        verify(exactly = 1) { oldRealm.query(eq(SubjectsSchemaV11.FINGERPRINT_TABLE), any(), eq(NOT_UNIQUE_ID)).count().find() }
        verify(exactly = 1) { oldRealm.query(eq(SubjectsSchemaV11.FACE_TABLE), any(), eq(UNIQUE_ID)).count().find() }
        verify(exactly = 1) { oldRealm.query(eq(SubjectsSchemaV11.FACE_TABLE), any(), eq(NOT_UNIQUE_ID)).count().find() }
        verify(exactly = 1) { newFingerObject.set(eq(SubjectsSchemaV12.FINGERPRINT_FIELD_ID), eq(NOT_UNIQUE_ID, inverse = true)) }
        verify(exactly = 1) { newFaceObject.set(eq(SubjectsSchemaV12.FACE_FIELD_ID), eq(NOT_UNIQUE_ID, inverse = true)) }
    }

    @Test
    fun `when migrating 12 to 13 ensures that subject ID is UUID in subjects table`() {
        setVersions(12L, 13L)

        val oldObject = mockk<DynamicRealmObject>()
        val newObject = mockk<DynamicMutableRealmObject>()

        val enumeratorSlot = slot<(DynamicRealmObject, DynamicMutableRealmObject?) -> Unit>()
        every { migrationContext.enumerate(any(), capture(enumeratorSlot)) } answers {
            enumeratorSlot.captured.invoke(oldObject, newObject)
        }

        every { oldObject.getValue<String>(eq(SubjectsSchemaV13.SUBJECT_ID_FIELD)) } returns GUID
        every { newObject.set(any(), any<RealmUUID>()) } returns mockk()

        realmMigrations.migrate(migrationContext)

        verify { newObject.set(eq(SubjectsSchemaV13.SUBJECT_ID_FIELD), eq(RealmUUID.from(GUID))) }
    }

    private fun setVersions(
        old: Long,
        new: Long,
    ) {
        every { oldRealm.schemaVersion() } returns old
        every { newRealm.schemaVersion() } returns new
    }

    private fun captureNewObjectForTable(table: String): DynamicMutableRealmObject {
        val newObject = mockk<DynamicMutableRealmObject>()
        val enumeratorSlot = slot<(DynamicRealmObject, DynamicMutableRealmObject?) -> Unit>()
        every { newObject.set(any(), any<String>()) } returns mockk()
        every { migrationContext.enumerate(eq(table), capture(enumeratorSlot)) } answers {
            enumeratorSlot.captured.invoke(mockk(), newObject)
        }
        return newObject
    }

    private fun setupSubjectQueryResponse(subjects: List<DynamicMutableRealmObject>) {
        every { oldRealm.query(eq(SubjectsSchemaV10.SUBJECT_TABLE)).find() } returns
            mockk<RealmResults<DynamicRealmObject>>(relaxed = true) {
                every { iterator() } returns subjects.iterator()
            }
    }

    private fun setupDeduplicationTestForTable(
        table: String,
        idField: String,
    ): DynamicMutableRealmObject {
        val newObject = mockk<DynamicMutableRealmObject>()
        val enumeratorSlot = slot<(DynamicRealmObject, DynamicMutableRealmObject?) -> Unit>()
        every { migrationContext.enumerate(eq(table), capture(enumeratorSlot)) } answers {
            with(enumeratorSlot.captured) {
                invoke(DynamicMutableRealmObject.create(table, idField to UNIQUE_ID), newObject)
                invoke(DynamicMutableRealmObject.create(table, idField to NOT_UNIQUE_ID), newObject)
            }
        }
        every { oldRealm.query(eq(table), any(), eq(UNIQUE_ID)).count().find() } returns 1
        every { oldRealm.query(eq(table), any(), eq(NOT_UNIQUE_ID)).count().find() } returns 2
        every { newObject.set<String>(any(), any()) } returns mockk()
        return newObject
    }

    companion object {
        private const val GUID = "2fd19445-c094-43d9-b231-df1abdb4ae6d"
        private const val USER_ID = "userId"
        private const val PROJECT_ID = "projectId"
        private const val MODULE_ID = "moduleId"

        private const val UNIQUE_ID = "uniqueId"
        private const val NOT_UNIQUE_ID = "nonUniqueId"
    }
}
