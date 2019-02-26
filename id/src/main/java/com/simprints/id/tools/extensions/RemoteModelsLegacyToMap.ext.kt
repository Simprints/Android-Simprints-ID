package com.simprints.id.tools.extensions

import com.google.firebase.database.Exclude
import com.simprints.id.data.db.remote.models.fb_IdEvent
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.models.fb_VfEvent

@Exclude
fun fb_Person.toMap(): Map<String, Any> {
    val result = HashMap<String, Any>()
    result["patientId"] = patientId
    result["projectId"] = projectId
    result["userId"] = userId
    result["createdAt"] = createdAt.toString()
    result["updatedAt"] = updatedAt.toString()
    result["fingerprints"] = fingerprints.mapKeys { it.key.name }
    return result
}

@Exclude
fun fb_IdEvent.toMap(): Map<String, Any> {
    val result = HashMap<String, Any>()
    result["ProbePerson"] = ProbePerson.toMap()
    result["userId"] = userId
    result["matchSize"] = matchSize
    result["sessionId"] = sessionId
    result["date"] = date
    result["fbMatches"] = fbMatches
    result["serverDate"] = serverDate
    return result
}

@Exclude
fun fb_VfEvent.toMap(): Map<String, Any> {
    val result = HashMap<String, Any>()
    result["ProbePerson"] = ProbePerson.toMap()
    result["userId"] = userId
    result["date"] = date
    result["guidFound"] = guid
    result["guidExistsResult"] = guidExistsResult
    result["confidence"] = confidence
    result["serverDate"] = serverDate
    return result
}
