package com.example.ui

import android.app.DatePickerDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(
    viewModel: MhViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val patients by viewModel.patients.collectAsState()

    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }

    // Date Dialog setup
    val calendar = Calendar.getInstance()
    val fromDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            fromDate = sdf.format(cal.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val toDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            toDate = sdf.format(cal.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 800.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Generate Spreadsheets & Excel Layouts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // FILTER CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Report Date Range Filtering",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // From Date Selector
                        OutlinedTextField(
                            value = fromDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("From Date") },
                            trailingIcon = {
                                IconButton(onClick = { fromDialog.show() }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { fromDialog.show() }
                        )

                        // To Date Selector
                        OutlinedTextField(
                            value = toDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To Date") },
                            trailingIcon = {
                                IconButton(onClick = { toDialog.show() }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { toDialog.show() }
                        )
                    }

                    if (fromDate.isNotEmpty() || toDate.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                fromDate = ""
                                toDate = ""
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Clear Filters")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Filter Range")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Structured Reports Catalog",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (patients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No metrics found in database.\nRegister some patient first to compute summary worksheets.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportTypeCard(
                        title = "Complete Patient Log",
                        subtitle = "Generates complete visual log records including full diagnostics details and categorical flags.",
                        icon = Icons.Default.Summarize,
                        tintColor = MaterialTheme.colorScheme.primary,
                        onClickAction = {
                            ReportExporter.shareReport(context, "log", patients, fromDate, toDate)
                            viewModel.triggerMessage("Drafted Complete Patient Log Spreadsheet")
                        }
                    )

                    ReportTypeCard(
                        title = "Category-wise Counts Summary",
                        subtitle = "Computes summary distribution footfalls aggregating SMD, CMD, SUD, Epilepsy, and Dementia rows.",
                        icon = Icons.Default.PieChart,
                        tintColor = Color(0xFF198754),
                        onClickAction = {
                            ReportExporter.shareReport(context, "category", patients, fromDate, toDate)
                            viewModel.triggerMessage("Drafted Category-wise Counts Summary Spreadsheet")
                        }
                    )

                    ReportTypeCard(
                        title = "Outreach Location Summary",
                        subtitle = "Aggregates overall registered indices grouped cleanly by local outreach camps and centres.",
                        icon = Icons.Default.Map,
                        tintColor = Color(0xFF17A2B8),
                        onClickAction = {
                            ReportExporter.shareReport(context, "outreach", patients, fromDate, toDate)
                            viewModel.triggerMessage("Drafted Outreach Location Summary Spreadsheet")
                        }
                    )

                    ReportTypeCard(
                        title = "Doctor Specialist Summary",
                        subtitle = "Summarizes patient counts attended by each active visiting medical specialist doctor.",
                        icon = Icons.Default.LocalHospital,
                        tintColor = Color(0xFFE0A800),
                        onClickAction = {
                            ReportExporter.shareReport(context, "doctor", patients, fromDate, toDate)
                            viewModel.triggerMessage("Drafted Doctor Specialist Summary Spreadsheet")
                        }
                    )

                    ReportTypeCard(
                        title = "Camp Daily Header-Merged Summary",
                        subtitle = "Generates spreadsheet featuring detailed New vs Old visits split across Male & Female gender vectors.",
                        icon = Icons.Default.GridOn,
                        tintColor = Color(0xFF6C757D),
                        onClickAction = {
                            ReportExporter.shareReport(context, "date-summary", patients, fromDate, toDate)
                            viewModel.triggerMessage("Drafted Merged Header Daily Camp Summary Spreadsheet")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportTypeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tintColor: Color,
    onClickAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickAction() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(8.dp),
                color = tintColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share Report",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
