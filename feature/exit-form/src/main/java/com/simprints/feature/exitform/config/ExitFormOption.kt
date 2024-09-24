package com.simprints.feature.exitform.config

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.RefusalEvent

@Keep
enum class ExitFormOption(
    val requiresInfo: Boolean,
    val logName: String,
    val answer: RefusalEvent.RefusalPayload.Answer,
) {
    ReligiousConcerns(
        requiresInfo = false,
        logName = "Religious concerns",
        answer = RefusalEvent.RefusalPayload.Answer.REFUSED_RELIGION,
    ),
    DataConcerns(
        requiresInfo = false,
        logName = "Data concerns",
        answer = RefusalEvent.RefusalPayload.Answer.REFUSED_DATA_CONCERNS,
    ),
    NoPermission(
        requiresInfo = false,
        logName = "Does not have permission",
        answer = RefusalEvent.RefusalPayload.Answer.REFUSED_PERMISSION,
    ),
    ScannerNotWorking(
        requiresInfo = true,
        logName = "Scanner not working",
        answer = RefusalEvent.RefusalPayload.Answer.SCANNER_NOT_WORKING,
    ),
    AppNotWorking(
        requiresInfo = true,
        logName = "App not working",
        answer = RefusalEvent.RefusalPayload.Answer.APP_NOT_WORKING,
    ),
    PersonNotPresent(
        requiresInfo = false,
        logName = "Person not present",
        answer = RefusalEvent.RefusalPayload.Answer.REFUSED_NOT_PRESENT,
    ),
    TooYoung(
        requiresInfo = false,
        logName = "Too young",
        answer = RefusalEvent.RefusalPayload.Answer.REFUSED_YOUNG,
    ),
    WrongAgeGroupSelected(
        requiresInfo = false,
        logName = "Wrong age group selected",
        answer = RefusalEvent.RefusalPayload.Answer.WRONG_AGE_GROUP_SELECTED,
    ),
    UncooperativeChild(
        requiresInfo = false,
        logName = "Uncooperative child selected",
        answer = RefusalEvent.RefusalPayload.Answer.UNCOOPERATIVE_CHILD,
    ),
    Other(
        requiresInfo = true,
        logName = "Other",
        answer = RefusalEvent.RefusalPayload.Answer.OTHER,
    );

}
