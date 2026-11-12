package com.simprints.feature.externalcredential.ext

import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent

/**
 * Returns the ids of the [ExternalCredentialCaptureValueEvent]s that correspond to the external credentials
 * actually linked to the [enrolmentRecord].
 *
 * A session may contain several capture value events (e.g. one per discarded recapture), and the credential may
 * not be linked at all (e.g. the attendant declined to add it), so only the events whose ids match the record's
 * credentials are referenced.
 */
fun List<Event>.getLinkedExternalCredentialIds(enrolmentRecord: EnrolmentRecord): List<String> =
    getLinkedExternalCredentialIds(enrolmentRecord.externalCredentials.map { it.id }.toSet())

/**
 * Returns the ids of the [ExternalCredentialCaptureValueEvent]s whose ids are in [linkedCredentialIds],
 * i.e. the `credentialScanId`s of the credentials that are actually being linked.
 */
fun List<Event>.getLinkedExternalCredentialIds(linkedCredentialIds: Set<String>): List<String> =
    filterIsInstance<ExternalCredentialCaptureValueEvent>()
        .map { it.payload.id }
        .filter { it in linkedCredentialIds }
        .distinct()
