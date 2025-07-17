package com.github.southporter.ballista.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppRepository private constructor(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ballista_apps", Context.MODE_PRIVATE)
    private val packageManager = context.packageManager
    private val _enabledApps = MutableStateFlow(loadEnabledApps())
    
    val enabledApps: StateFlow<List<AppItem>> = _enabledApps
    
    private fun loadEnabledApps(): List<AppItem> {
        val orderedApps = getOrderedApps()
        return orderedApps.filter { app ->
            app.id == "settings" || isAppEnabled(app.id)
        }.map { app ->
            app.copy(displayName = getCustomDisplayName(app.id, app.displayName))
        }
    }
    
    private fun getOrderedApps(): List<AppItem> {
        val discoveredApps = discoverInstalledApps()
        val orderString = prefs.getString("app_order", null)
        return if (orderString != null) {
            val orderIds = orderString.split(",")
            val appMap = discoveredApps.associateBy { it.id }
            orderIds.mapNotNull { appMap[it] } + discoveredApps.filter { it.id !in orderIds }
        } else {
            discoveredApps
        }
    }
    
    private fun discoverInstalledApps(): List<AppItem> {
        Log.d("AppRepository", "Starting app discovery...")
        
        // Get all launchable apps
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val launchableApps = packageManager.queryIntentActivities(mainIntent, 0)
        
        Log.d("AppRepository", "Found ${launchableApps.size} launchable apps")
        
        val discoveredApps = mutableListOf<AppItem>()
        
        // Create apps for ALL launchable activities
        launchableApps.forEach { resolveInfo ->
            val appInfo = resolveInfo.activityInfo.applicationInfo
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val packageName = resolveInfo.activityInfo.packageName
            
            // Skip our own app and Android system app
            if (packageName != "com.github.southporter.ballista" && 
                packageName != "android") {
                val appId = generateAppId(packageName, appName)
                val defaultEnabled = isDefaultEnabledApp(packageName, appName)
                
                discoveredApps.add(AppItem(
                    id = appId,
                    displayName = appName,
                    packageName = packageName,
                    className = null, // Let the launcher use default launch intent
                    isEnabled = defaultEnabled
                ))
                
                Log.d("AppRepository", "Added app: $appName ($packageName) - enabled: $defaultEnabled")
            }
        }
        
        // Always add Ballista Settings (always enabled)
        discoveredApps.add(AppItem("settings", "Settings", "com.android.settings", null, true))
        
        Log.d("AppRepository", "Discovered ${discoveredApps.size} apps total")
        return discoveredApps.sortedBy { it.displayName }
    }
    
    private fun generateAppId(packageName: String, appName: String): String {
        // Use the full package name as the ID to ensure uniqueness
        return packageName
    }
    
    private fun isDefaultEnabledApp(packageName: String, appName: String): Boolean {
        val defaultEnabledApps = setOf(
            "phone", "dialer", "call",
            "messages", "messaging", "sms", "mms", "quik", // Added quik for QUIK messaging app
            "alarm", "clock", "deskclock",
            "calendar", "etar", // Added etar for LineageOS calendar
            "camera"
        )
        
        val packageLower = packageName.lowercase()
        val nameLower = appName.lowercase()
        
        return defaultEnabledApps.any { keyword ->
            packageLower.contains(keyword) || nameLower.contains(keyword)
        }
    }
    
    fun toggleApp(appId: String, enabled: Boolean) {
        prefs.edit().putBoolean(appId, enabled).apply()
        _enabledApps.value = loadEnabledApps()
    }
    
    fun isAppEnabled(appId: String): Boolean {
        // Check if we've already stored a preference for this app
        if (prefs.contains(appId)) {
            return prefs.getBoolean(appId, false)
        }
        
        // For new apps, check if it should be enabled by default
        return isDefaultEnabledApp(appId, "")
    }
    
    fun getAllApps(): List<AppItem> {
        return getOrderedApps().map { app ->
            app.copy(
                isEnabled = if (app.id == "settings") true else isAppEnabled(app.id),
                displayName = getCustomDisplayName(app.id, app.displayName)
            )
        }
    }
    
    fun getCustomDisplayName(appId: String, defaultName: String): String {
        return prefs.getString("display_name_$appId", defaultName) ?: defaultName
    }
    
    fun setCustomDisplayName(appId: String, customName: String) {
        prefs.edit().putString("display_name_$appId", customName).apply()
        _enabledApps.value = loadEnabledApps()
    }
    
    fun reorderApps(newOrder: List<AppItem>) {
        val orderString = newOrder.joinToString(",") { it.id }
        prefs.edit().putString("app_order", orderString).apply()
        _enabledApps.value = loadEnabledApps()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: AppRepository? = null
        
        fun getInstance(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}