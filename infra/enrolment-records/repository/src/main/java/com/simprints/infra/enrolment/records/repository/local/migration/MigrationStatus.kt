package com.simprints.infra.enrolment.records.repository.local.migration

enum class MigrationStatus {
    NOT_STARTED, // Initial state or after a failure that requires reset
    IN_PROGRESS, // Worker is  migrating records
    FAILED, // A transient error occurred, retry might be scheduled
    COMPLETED, // Migration finished successfully
}
