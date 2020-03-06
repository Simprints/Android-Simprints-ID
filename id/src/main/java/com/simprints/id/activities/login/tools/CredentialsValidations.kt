package com.simprints.id.activities.login.tools

fun areMandatoryCredentialsPresent(
    projectId: String,
    projectSecret: String,
    userId: String
): Boolean {
    return projectId.isNotEmpty()
        && projectSecret.isNotEmpty()
        && userId.isNotEmpty()
}

fun areSuppliedProjectIdAndProjectIdFromIntentEqual(
    suppliedProjectId: String,
    projectIdFromIntent: String
): Boolean {
    return suppliedProjectId == projectIdFromIntent
}
