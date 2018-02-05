package com.simprints.id.secure.models

import org.json.JSONObject

data class AuthRequest(var encryptedProjectSecret: String = "", var projectId: String = "", var attestation: JSONObject = JSONObject())
