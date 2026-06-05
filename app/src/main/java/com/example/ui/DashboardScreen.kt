package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Patient
import kotlin.math.max

@Composable
fun DashboardScreen(
    viewModel: MhViewModel,
    modifier: Modifier = Modifier
) {
    val patients by viewModel.patients.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val attenders by viewModel.attenders.collectAsState()
    val schedules by viewModel.schedules.collectAsState()

    var newPatCount by remember { mutableIntStateOf(0) }
    var oldPatCount by remember { mutableIntStateOf(0) }
    var smdCount by remember { mutableIntStateOf(0) }
    var cmdCount by remember { mutableIntStateOf(0) }
    var sudCount by remember { mutableIntStateOf(0) }
    var epilepsyCount by remember { mutableIntStateOf(0) }
    var dementiaCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(patients) {
        var nPat = 0
        var oPat = 0
        var smd = 0
        var cmd = 0
        var sud = 0
        var epi = 0
        var dem = 0

        patients.forEach { p ->
            if (p.type == "New") nPat++
            if (p.type == "Old") oPat++
            
            val cats = p.getCategoryList()
            if (cats.contains("SMD")) smd++
            if (cats.contains("CMD")) cmd++
            if (cats.contains("SUD")) sud++
            if (cats.contains("Epilepsy")) epi++
            if (cats.contains("Dementia")) dem++
        }

        newPatCount = nPat
        oldPatCount = oPat
        smdCount = smd
        cmdCount = cmd
        sudCount = sud
        epilepsyCount = epi
        dementiaCount = dem
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header intro
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Welcome to Mental Health Outreach",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Offline ready status",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Local Storage Database: Offline Ready",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Dashboard Stats Summary Grid
        Text(
            text = "Statistical Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        BoxWithConstraints {
            val isWide = maxWidth >= 600.dp
            if (isWide) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = "Total Patients",
                            count = patients.size.toString(),
                            icon = Icons.Default.People,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.PatientList) }
                        )
                        StatCard(
                            title = "New Visits",
                            count = newPatCount.toString(),
                            icon = Icons.Default.PersonAdd,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.PatientList) }
                        )
                        StatCard(
                            title = "Old Visits",
                            count = oldPatCount.toString(),
                            icon = Icons.Default.AssignmentInd,
                            color = Color(0xFF0EA5E9),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.PatientList) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = "Severe Cases",
                            count = smdCount.toString(),
                            icon = Icons.Default.Warning,
                            color = Color(0xFFF43F5E),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.PatientList) }
                        )
                        StatCard(
                            title = "Doctors Listed",
                            count = doctors.size.toString(),
                            icon = Icons.Default.LocalHospital,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.Doctors) }
                        )
                        StatCard(
                            title = "Staff Attenders",
                            count = attenders.size.toString(),
                            icon = Icons.Default.ContactPhone,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.Attenders) }
                        )
                        StatCard(
                            title = "Planned Camps",
                            count = schedules.size.toString(),
                            icon = Icons.Default.DateRange,
                            color = Color(0xFF06B6D4),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.Schedule) }
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = "Total Patients",
                            count = patients.size.toString(),
                            icon = Icons.Default.People,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.PatientList) }
                        )
                        StatCard(
                            title = "New Visits",
                            count = newPatCount.toString(),
                            icon = Icons.Default.PersonAdd,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.PatientList) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = "Old Visits",
                            count = oldPatCount.toString(),
                            icon = Icons.Default.AssignmentInd,
                            color = Color(0xFF0EA5E9),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.PatientList) }
                        )
                        StatCard(
                            title = "Severe Cases",
                            count = smdCount.toString(),
                            icon = Icons.Default.Warning,
                            color = Color(0xFFF43F5E),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.PatientList) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = "Doctors Listed",
                            count = doctors.size.toString(),
                            icon = Icons.Default.LocalHospital,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.Doctors) }
                        )
                        StatCard(
                            title = "Staff Attenders",
                            count = attenders.size.toString(),
                            icon = Icons.Default.ContactPhone,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.Attenders) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = "Planned Camps",
                            count = schedules.size.toString(),
                            icon = Icons.Default.DateRange,
                            color = Color(0xFF06B6D4),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(Screen.Schedule) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Visualizations - Custom Canvas Bar Chart for categories
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Category-wise Case Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CategoryBarChart(
                        cmdCount = cmdCount,
                        smdCount = smdCount,
                        sudCount = sudCount,
                        epilepsyCount = epilepsyCount,
                        dementiaCount = dementiaCount,
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .height(200.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = count,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CategoryBarChart(
    cmdCount: Int,
    smdCount: Int,
    sudCount: Int,
    epilepsyCount: Int,
    dementiaCount: Int,
    modifier: Modifier = Modifier
) {
    val counts = listOf(cmdCount, smdCount, sudCount, epilepsyCount, dementiaCount)
    val labels = listOf("CMD", "SMD", "SUD", "Epilepsy", "Dementia")
    val colors = listOf(
        Color(0xFF6366F1), // Vibrant Indigo
        Color(0xFFF43F5E), // Bold Rose
        Color(0xFFF59E0B), // Warm Amber
        Color(0xFF10B981), // Energetic Emerald
        Color(0xFF8B5CF6)  // Cosmic Purple
    )

    // Animated ratios
    val animatedRatios = counts.map { count ->
        val maxVal = max(counts.maxOrNull() ?: 0, 1)
        val targetRatio = count.toFloat() / maxVal.toFloat()
        animateFloatAsState(
            targetValue = targetRatio,
            animationSpec = tween(durationMillis = 800),
            label = "BarHeightAnimation"
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.SpaceBetween) {
        if (counts.all { it == 0 }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No cases recorded yet.\nRegister patients to plot data statistics here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Bars layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                counts.forEachIndexed { idx, count ->
                    val ratio = animatedRatios[idx].value
                    val barColor = colors[idx]
                    val label = labels[idx]

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Value counter at top of each bar
                        if (count > 0) {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "-",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Animated Column Canvas bar
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(120.dp * max(ratio, 0.05f))
                        ) {
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(0f, 0f),
                                size = Size(size.width, size.height),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Category Label
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
