package com.simprints.infra.enrolment.records.realm.store.migration.oldschemas

// Starting from V10, we would referring Person/People as Subject/Subjects
internal object SubjectsSchemaV10 {
    const val SUBJECT_TABLE: String = "DbSubject"
    const val SUBJECT_ID = "subjectId"
    const val ATTENDANT_ID = "attendantId"

    const val PERSON_TABLE = "DbPerson"
    const val PERSON_PROJECT_ID = "projectId"
    const val PERSON_PATIENT_ID = "patientId"
    const val PERSON_MODULE_ID = "moduleId"
    const val PERSON_USER_ID = "userId"
    const val PERSON_CREATE_TIME = "createdAt"
    const val PERSON_UPDATE_TIME = "updatedAt"
    const val PERSON_TO_SYNC = "toSync"
    const val PERSON_FINGERPRINT_SAMPLES = "fingerprintSamples"
    const val PERSON_FACE_SAMPLES = "faceSamples"
}
