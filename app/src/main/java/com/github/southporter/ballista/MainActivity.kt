package com.github.southporter.ballista

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.southporter.ballista.data.AppItem
import com.github.southporter.ballista.data.AppRepository
import com.github.southporter.ballista.ui.theme.BallistaTheme

class MainActivity : ComponentActivity() {
    private lateinit var appRepository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appRepository = AppRepository.getInstance(this)

        enableEdgeToEdge()
        setContent {
            BallistaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LauncherScreen(appRepository = appRepository)
                }
            }
        }
    }
}

@Composable
fun LauncherScreen(appRepository: AppRepository) {
    val context = LocalContext.current
    val enabledApps by appRepository.enabledApps.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    // Keep the custom order from settings, including Settings position

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp)
                .padding(vertical = 80.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Show all apps in their custom order (including Settings)
            items(enabledApps) { app ->
                AppListItem(
                    app = app,
                    onClick = { launchApp(context, app) },
                    onLongClick = { openSettings(context) }
                )

                // Add subtle spacing between items
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Scroll indicators - only show when there are more items than can fit on screen
        val canScroll = listState.canScrollForward || listState.canScrollBackward
        if (enabledApps.size > 9 && canScroll) {
            ScrollIndicators(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp),
                totalItems = enabledApps.size,
                visibleItems = 9,
                listState = listState
            )
        }
    }
}

@Composable
fun AppListItem(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Text(
        text = app.displayName,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 20.dp),
        fontSize = 26.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Center,
        color = if (isPressed) {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.onBackground
        }
    )
}

@Composable
fun ScrollIndicators(
    modifier: Modifier = Modifier,
    totalItems: Int,
    visibleItems: Int,
    listState: LazyListState
) {
    val firstVisibleIndex by derivedStateOf {
        listState.firstVisibleItemIndex
    }

    val currentPage = (firstVisibleIndex / visibleItems).coerceAtMost((totalItems - 1) / visibleItems)
    val totalPages = (totalItems + visibleItems - 1) / visibleItems

    if (totalPages > 1) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(totalPages) { page ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (page == currentPage) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            }
                        )
                )
            }
        }
    }
}

private fun launchApp(context: Context, app: AppItem) {
    try {
        // Special handling for Ballista settings
        if (app.id == "settings") {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        }
        
        // Modern launcher approach with multiple fallbacks
        var launched = false
        
        // Method 1: Try specific component if available
        if (!app.className.isNullOrEmpty()) {
            try {
                val intent = Intent().apply {
                    component = ComponentName(app.packageName, app.className)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }
                context.startActivity(intent)
                launched = true
            } catch (e: Exception) {
                // Continue to next method
            }
        }
        
        // Method 2: Use package manager's launch intent (most reliable)
        if (!launched) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    }
                    context.startActivity(launchIntent)
                    launched = true
                }
            } catch (e: Exception) {
                // Continue to next method
            }
        }
        
        // Method 3: Generic launcher intent
        if (!launched) {
            try {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setPackage(app.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }
                context.startActivity(intent)
                launched = true
            } catch (e: Exception) {
                // Continue to error handling
            }
        }
        
        // If all methods failed
        if (!launched) {
            Toast.makeText(context, "${app.displayName} cannot be launched", Toast.LENGTH_SHORT).show()
        }
        
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to launch ${app.displayName}: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun openSettings(context: Context) {
    val intent = Intent(context, SettingsActivity::class.java)
    context.startActivity(intent)
}
