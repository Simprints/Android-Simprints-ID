package com.simprints.id.data.db.person.remote.models.peopleoperations.request

import androidx.annotation.Keep

@Keep
data class ApiPeopleOperations(val groups: List<ApiPeopleOperationGroup>)

/*
Structure of the body for @POST("projects/{projectId}/patient-operations/count")
    {
       "groups": [
         {
           "whereLabels": [
             {
               "key": "moduleId",
               "value": "1234"
             },
             {
               "key": "mode",
               "value": "FACE|FINGERPRINT"
             }
           ],
           "lastKnownPatient": {
             "updatedAt": 1528138440123,
             "id": "6fcd2866-1e9a-4204-a2d5-e98ffcae5b81"
           }
     }
 */
