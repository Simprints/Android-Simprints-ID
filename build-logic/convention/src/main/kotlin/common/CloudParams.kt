package common

object CloudParams {
    /**
     * The cloud project ids used in google integrity service
     *
     * Extra set of quotes prevent Gradle from converting number-ish looking string into number type
     */
    const val RELEASE_CLOUD_PROJECT_ID = "\"964685613552\""
    const val STAGING_CLOUD_PROJECT_ID = "\"423867324644\""
    const val DEV_CLOUD_PROJECT_ID = "\"79630518081\""
}
