package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BackupScreen(
    viewModel: MhViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Database current counts
    val patients by viewModel.patients.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val attenders by viewModel.attenders.collectAsState()

    // Import file picker activity result launcher
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            var fileName = ""
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val index = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index >= 0) {
                            fileName = c.getString(index) ?: ""
                        }
                    }
                }
            }
            if (fileName.isEmpty()) {
                fileName = uri.path ?: ""
            }

            if (fileName.isNotEmpty() && !fileName.endsWith(".json", ignoreCase = true)) {
                viewModel.triggerMessage("Error: Only .json files are allowed for restoring database backup.")
            } else {
                viewModel.restoreDatabaseFromJson(context, uri) {
                    viewModel.navigateTo(Screen.Dashboard)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Icon & Title Card
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secured",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Text(
                text = "Database Security & Backups",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Secure local backup and restoration without cloud dependancy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Offline Guarantee banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "All database tables (Specialists, Patient Registers, and Personnel records) are exported dynamically to a validated backup on demand. Your details never leave this offline device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Local Backups Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BackupOptionCard(
                title = "Save Backup to Downloads",
                description = "Build and save a secure backup (.json) of all specialists, staff rosters, and patient records directly to your device local storage under 'Downloads/MH Outreach'.",
                actionText = "Save Backup to Downloads",
                icon = Icons.Default.SaveAlt,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = {
                    val backupStr = viewModel.getBackupJson()
                    val path = ReportExporter.exportBackupToDownloads(context, backupStr)
                    if (path != null) {
                        viewModel.triggerMessage("Backup saved directly to Download folder: $path")
                    } else {
                        viewModel.triggerMessage("Failed to write to local storage downloads directly.")
                    }
                }
            )

            BackupOptionCard(
                title = "Share Backup via WhatsApp & Others",
                description = "Generate and immediately send/share the database backup file with colleagues, sync contacts, or storage clouds using WhatsApp, Gmail, or other systems.",
                actionText = "Share Backup File",
                icon = Icons.Default.Share,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = {
                    val backupStr = viewModel.getBackupJson()
                    ReportExporter.shareJsonBackup(context, backupStr)
                    viewModel.triggerMessage("Preparing backup for direct sharing...")
                }
            )

            BackupOptionCard(
                title = "Restore Local Database",
                description = "Safely ingest a previously exported backup file (.json) received over WhatsApp, email, or files. The system automatically merges records to update specialists, attenders, and patient visits without duplication.",
                actionText = "Restore Database Backup",
                icon = Icons.Default.FileUpload,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = {
                    importFileLauncher.launch("application/json")
                }
            )
        }

        // Active State Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Database Statistics",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Current Local Database Footprint",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LocalStatCell(label = "Patient Visits", count = patients.size)
                    LocalStatCell(label = "Specialists", count = doctors.size)
                    LocalStatCell(label = "Staff Register", count = attenders.size)
                }
            }
        }
    }
}

@Composable
fun BackupOptionCard(
    title: String,
    description: String,
    actionText: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = containerColor,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LocalStatCell(
    label: String,
    count: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
