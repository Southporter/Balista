package com.github.southporter.ballista

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.window.Dialog
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.southporter.ballista.data.AppItem
import com.github.southporter.ballista.data.AppRepository
import com.github.southporter.ballista.ui.theme.BallistaTheme

class SettingsActivity : ComponentActivity() {
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
                    SettingsNavigation(
                        appRepository = appRepository,
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
}

enum class SettingsScreen {
    MAIN, SORT_APPS, MANAGE_APPS
}

@Composable
fun SettingsNavigation(
    appRepository: AppRepository,
    onBackPressed: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(SettingsScreen.MAIN) }
    
    when (currentScreen) {
        SettingsScreen.MAIN -> MainSettingsScreen(
            appRepository = appRepository,
            onBackPressed = onBackPressed,
            onNavigateToSortApps = { currentScreen = SettingsScreen.SORT_APPS },
            onNavigateToManageApps = { currentScreen = SettingsScreen.MANAGE_APPS }
        )
        SettingsScreen.SORT_APPS -> SortAppsScreen(
            appRepository = appRepository,
            onBackPressed = { currentScreen = SettingsScreen.MAIN }
        )
        SettingsScreen.MANAGE_APPS -> ManageAppsScreen(
            appRepository = appRepository,
            onBackPressed = { currentScreen = SettingsScreen.MAIN }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsScreen(
    appRepository: AppRepository,
    onBackPressed: () -> Unit,
    onNavigateToSortApps: () -> Unit,
    onNavigateToManageApps: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    text = "Ballista Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Settings menu options
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsMenuItem(
                title = "Sort Apps",
                description = "Reorder your apps",
                onClick = onNavigateToSortApps
            )
            
            SettingsMenuItem(
                title = "Manage Apps",
                description = "Toggle apps and edit names",
                onClick = onNavigateToManageApps
            )
            
            SettingsMenuItem(
                title = "Set as Default Launcher",
                description = "Make Ballista your home screen",
                onClick = { openDefaultAppsSettings(context) },
                isHighlighted = true
            )
            
            SettingsMenuItem(
                title = "Phone Settings",
                description = "Open system settings",
                onClick = { openSystemSettings(context) }
            )
        }
    }
}

@Composable
fun AppToggleItem(
    app: AppItem,
    onToggle: (Boolean) -> Unit
) {
    var isEnabled by remember { mutableStateOf(app.isEnabled) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = app.displayName,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        Switch(
            checked = isEnabled,
            onCheckedChange = { enabled ->
                isEnabled = enabled
                onToggle(enabled)
            }
        )
    }
}

@Composable
fun DraggableAppToggleItem(
    app: AppItem,
    onToggle: (Boolean) -> Unit,
    onEditName: () -> Unit,
    isDragging: Boolean
) {
    var isEnabled by remember { mutableStateOf(app.isEnabled) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom 2-line drag indicator
        Column(
            modifier = Modifier.padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        RoundedCornerShape(1.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        RoundedCornerShape(1.dp)
                    )
            )
        }
        
        Text(
            text = app.displayName,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        // Edit button for display name
        IconButton(
            onClick = onEditName,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit name",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        
        // Only show toggle switch for non-Settings apps
        if (app.id != "settings") {
            Switch(
                checked = isEnabled,
                onCheckedChange = { enabled ->
                    isEnabled = enabled
                    onToggle(enabled)
                }
            )
        } else {
            // Show "Always On" text for Settings
            Text(
                text = "Always On",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SettingsMenuItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isHighlighted) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                fontSize = 14.sp,
                color = if (isHighlighted) {
                    MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                }
            )
        }
    }
}

@Composable
fun SetDefaultLauncherItem(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Set as Default Launcher",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Text(
                text = "Set as Default Launcher",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun SystemSettingsItem(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Text(
                text = "Phone Settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EditNameDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var textFieldValue by remember { mutableStateOf(app.displayName) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Display Name",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (textFieldValue.isNotBlank()) {
                                onSave(textFieldValue.trim())
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

private fun openDefaultAppsSettings(context: android.content.Context) {
    try {
        // Try to open the specific default apps settings
        val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to general settings if the specific intent doesn't work
        try {
            val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Final fallback to general settings
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
            context.startActivity(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortAppsScreen(
    appRepository: AppRepository,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val enabledApps by appRepository.enabledApps.collectAsStateWithLifecycle()
    var reorderableApps by remember { mutableStateOf<List<AppItem>>(enabledApps) }
    
    // Update reorderable apps when enabled apps change
    LaunchedEffect(enabledApps) {
        reorderableApps = enabledApps
    }
    
    val reorderableLazyListState = rememberReorderableLazyListState(onMove = { from, to ->
        val newList = reorderableApps.toMutableList()
        val item = newList.removeAt(from.index)
        newList.add(to.index, item)
        reorderableApps = newList
        appRepository.reorderApps(newList)
    })
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    text = "Sort Apps",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Instruction text
        Text(
            text = "Long press and drag to reorder your apps",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
        
        LazyColumn(
            state = reorderableLazyListState.listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .reorderable(reorderableLazyListState)
                .detectReorderAfterLongPress(reorderableLazyListState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reorderableApps, key = { app -> app.id }) { app ->
                ReorderableItem(
                    reorderableState = reorderableLazyListState,
                    key = app.id
                ) { isDragging ->
                    SortableAppItem(
                        app = app,
                        isDragging = isDragging
                    )
                }
            }
        }
    }
}

@Composable
fun SortableAppItem(
    app: AppItem,
    isDragging: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle
            Column(
                modifier = Modifier.padding(end = 16.dp)
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant,
                                RoundedCornerShape(1.dp)
                            )
                    )
                    if (it == 0) Spacer(modifier = Modifier.height(4.dp))
                }
            }
            
            Text(
                text = app.displayName,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAppsScreen(
    appRepository: AppRepository,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var allApps by remember { mutableStateOf(appRepository.getAllApps()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingApp by remember { mutableStateOf<AppItem?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    text = "Manage Apps",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Instruction text
        Text(
            text = "Toggle apps and tap edit to change names",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(allApps, key = { _, app -> app.id }) { index, app ->
                ManageableAppItem(
                    app = app,
                    onToggle = { enabled ->
                        appRepository.toggleApp(app.id, enabled)
                        allApps = allApps.map { 
                            if (it.id == app.id) it.copy(isEnabled = enabled) else it 
                        }
                    },
                    onEditName = {
                        editingApp = app
                        showEditDialog = true
                    }
                )
            }
        }
    }
    
    // Edit name dialog
    if (showEditDialog && editingApp != null) {
        EditNameDialog(
            app = editingApp!!,
            onDismiss = { 
                showEditDialog = false
                editingApp = null
            },
            onSave = { newName ->
                appRepository.setCustomDisplayName(editingApp!!.id, newName)
                allApps = appRepository.getAllApps()
                showEditDialog = false
                editingApp = null
            }
        )
    }
}

@Composable
fun ManageableAppItem(
    app: AppItem,
    onToggle: (Boolean) -> Unit,
    onEditName: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(app.isEnabled) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = app.displayName,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            
            // Edit button for display name
            IconButton(
                onClick = onEditName,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit name",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            // Only show toggle switch for non-Settings apps
            if (app.id != "settings") {
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { enabled ->
                        isEnabled = enabled
                        onToggle(enabled)
                    }
                )
            } else {
                Text(
                    text = "Always On",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun openSystemSettings(context: android.content.Context) {
    val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
    context.startActivity(intent)
}