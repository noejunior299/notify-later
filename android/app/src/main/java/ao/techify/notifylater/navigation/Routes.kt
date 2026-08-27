package ao.techify.notifylater.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val APPLICATIONS = "applications"
    const val APPLICATION_DETAILS = "application_details"
    const val QUEUE = "queue"
    const val LOGS = "logs"
    const val SETTINGS = "settings"

    const val ARG_PACKAGE_NAME = "packageName"
    const val APPLICATION_DETAILS_WITH_ARG = "application_details/{$ARG_PACKAGE_NAME}"

    fun applicationDetails(packageName: String): String =
        "application_details/$packageName"
}
