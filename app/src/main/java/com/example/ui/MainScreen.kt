package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MhViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // Double tap back button on dashboard page minimizes the app
    BackHandler(enabled = currentScreen == Screen.Dashboard) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            activity?.moveTaskToBack(true)
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "Press back again to minimize", Toast.LENGTH_SHORT).show()
        }
    }

    // Single click back button on other pages comes to Dashboard page
    BackHandler(enabled = currentScreen != Screen.Dashboard) {
        viewModel.navigateTo(Screen.Dashboard)
    }

    // Listen for toast messages from ViewModel
    LaunchedEffect(Unit) {
        viewModel.message.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val menuItems = listOf(
        NavigationMenuItem(Screen.Dashboard, "Dashboard Overview", Icons.Default.Dashboard),
        NavigationMenuItem(Screen.Doctors, "Doctors Specialist", Icons.Default.LocalHospital),
        NavigationMenuItem(Screen.Attenders, "Clinic Attenders", Icons.Default.AssignmentInd),
        NavigationMenuItem(Screen.PatientEntry, "Patient Entry Log", Icons.Default.PersonAdd),
        NavigationMenuItem(Screen.PatientList, "Patient Search", Icons.Default.Search),
        NavigationMenuItem(Screen.Reports, "Excel Reports Summary", Icons.Default.BarChart),
        NavigationMenuItem(Screen.Backup, "ERP Data Backup", Icons.Default.Cloud)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1E293B), // Premium Dark Slate Sidebar
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
            ) {
                // Sidebar Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A)) // Slightly darker header
                        .padding(vertical = 32.dp, horizontal = 20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "MH Outreach",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "E.R.P. System Dashboard",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF334155))

                Spacer(modifier = Modifier.height(16.dp))

                // Sidebar menu options
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    menuItems.forEach { item ->
                        val isSelected = currentScreen == item.screen
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.navigateTo(item.screen)
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                unselectedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    ) {
        // Main Screen Scaffold - Content with status bar/safeDrawing insets padding.
        Scaffold(
            topBar = {
                MediumTopAppBar(
                    title = {
                        val screenTitle = when (currentScreen) {
                            Screen.Dashboard -> "Dashboard Overview"
                            Screen.Doctors -> "Doctors Registry"
                            Screen.Attenders -> "Staff Attenders"
                            Screen.PatientEntry -> "Patient Entry Form"
                            Screen.PatientList -> "Search Patient Logs"
                            Screen.Reports -> "Excel & CSV Exports"
                            Screen.Backup -> "Backup Database Security"
                        }
                        Text(
                            text = screenTitle,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Sidebar Navigation Menu",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {},
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp
                ) {
                    val bottomItems = listOf(
                        NavigationMenuItem(Screen.Dashboard, "Home", Icons.Default.Dashboard),
                        NavigationMenuItem(Screen.Doctors, "Doctors", Icons.Default.LocalHospital),
                        NavigationMenuItem(Screen.Attenders, "Staff", Icons.Default.AssignmentInd),
                        NavigationMenuItem(Screen.PatientEntry, "Add Log", Icons.Default.PersonAdd),
                        NavigationMenuItem(Screen.PatientList, "Search", Icons.Default.Search),
                        NavigationMenuItem(Screen.Reports, "Reports", Icons.Default.BarChart),
                        NavigationMenuItem(Screen.Backup, "Backup", Icons.Default.Cloud)
                    )
                    bottomItems.forEach { item ->
                        val isSelected = currentScreen == item.screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(item.screen) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            alwaysShowLabel = true
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            modifier = modifier
        ) { innerPadding ->
            // Use Box to handle padding safely, ensuring App never overlaps status bars or runs in total full screen clipping.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
                    Screen.Doctors -> DoctorsScreen(viewModel = viewModel)
                    Screen.Attenders -> AttendersScreen(viewModel = viewModel)
                    Screen.PatientEntry -> PatientEntryScreen(viewModel = viewModel)
                    Screen.PatientList -> PatientListScreen(viewModel = viewModel)
                    Screen.Reports -> ReportsScreen(viewModel = viewModel)
                    Screen.Backup -> BackupScreen(viewModel = viewModel)
                }
            }
        }
    }
}

data class NavigationMenuItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)
