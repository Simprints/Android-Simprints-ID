extra.apply {

    /**
     * Debug Version code. The version code for staging and production builds are taken from the CI.
     * Read more about our versioning here:
     * https://simprints.atlassian.net/wiki/spaces/KB/pages/1761378305/Releasing+Simprints+ID
     */
    set("VERSION_CODE", 1)

    /**
     * Current release version. This base version name corresponds with the latest release being worked
     * on, and the version of the BFSID API being used. The version name is automatically set by the
     * CI. Read more about our versioning here:
     * https://simprints.atlassian.net/wiki/spaces/KB/pages/1761378305/Releasing+Simprints+ID
     *
     * Dev version >= 2024.2.1 is required for receiving biometric sdk age restrictions
     * Dev version >= 2024.3.0 is required to receive configuration ID
     */
    set("VERSION_NAME", "2024.3.0")

    /**
     * Build type. The version code describes which build type was used for the build.
     * Read more about our versioning here:
     * https://simprints.atlassian.net/wiki/spaces/KB/pages/1761378305/Releasing+Simprints+ID
     */
    set("VERSION_SUFFIX", "dev")

    /**
     * Set debuggable, default it true so local staging and release builds can be debugged. NOTE: THE
     * CD WILL OVERWRITE TO FALSE AS THE PLAYSTORE DOES NOT ACCEPT DEBUGGABLE TRUE APPS
     */
    set("DEBUGGABLE", true)
    set("DB_ENCRYPTION", true)
}
