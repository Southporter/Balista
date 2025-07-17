package com.github.southporter.ballista.data

data class AppItem(
    val id: String,
    val displayName: String,
    val packageName: String,
    val className: String? = null,
    val isEnabled: Boolean = true
)

object DefaultApps {
    val apps = listOf(
        AppItem("phone", "Phone", "com.android.dialer"),
        AppItem("calendar", "Calendar", "com.android.calendar"),
        AppItem("timer", "Timer", "com.android.deskclock"),
        AppItem("music", "Music", "com.android.music"),
        AppItem("hotspot", "Hotspot", "com.android.settings", "com.android.settings.TetherSettings"),
        AppItem("settings", "Settings", "com.android.settings"),
        AppItem("alarm", "Alarm", "com.android.deskclock"),
        AppItem("directions", "Directions", "com.google.android.apps.maps"),
        AppItem("notes", "Notes", "com.google.android.keep"),
        AppItem("messages", "Messages", "com.google.android.apps.messaging"),
        AppItem("camera", "Camera", "com.android.camera2"),
        AppItem("gallery", "Gallery", "com.google.android.apps.photos"),
        AppItem("calculator", "Calculator", "com.android.calculator2"),
        AppItem("weather", "Weather", "com.google.android.googlequicksearchbox"),
        AppItem("contacts", "Contacts", "com.android.contacts")
    )
}