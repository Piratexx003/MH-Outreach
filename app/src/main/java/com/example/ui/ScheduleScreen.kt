package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OutreachSchedule
import java.text.SimpleDateFormat
import java.util.*

fun shareScheduleOnWhatsApp(context: Context, schedule: OutreachSchedule) {
    val doctorDisplay = schedule.doctorName.ifBlank { "Not Assigned" }
    val attenderDisplay = schedule.attenderName.ifBlank { "Not Assigned" }
    val shareText = """
        📢 *Outreach Camp Schedule*
        
        🏥 *Hospital/Location:* ${schedule.location}
        📅 *Plan Date:* ${DateUtils.formatToIndianDate(schedule.dateString)}
        🗓️ *Target Month:* ${schedule.targetMonth}
        🩺 *Specialist Doctor:* $doctorDisplay
        👥 *Staff Attender:* $attenderDisplay
        
        *Status:* Approved
        _Sent via MH Outreach System_
    """.trimIndent()

    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share Camp Schedule via")
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun shareMonthlySchedules(context: Context, monthName: String, schedules: List<OutreachSchedule>) {
    val sortedSchedules = schedules.sortedWith { s1, s2 ->
        val date1 = DateUtils.parseDate(s1.dateString)
        val date2 = DateUtils.parseDate(s2.dateString)
        date1.compareTo(date2)
    }
    val sb = StringBuilder()
    sb.append("📋 *Outreach Camp Plan for $monthName*\n\n")
    sortedSchedules.forEachIndexed { index, s ->
        val doctorDisplay = s.doctorName.ifBlank { "Not Assigned" }
        val attenderDisplay = s.attenderName.ifBlank { "Not Assigned" }
        sb.append("${index + 1}. *${s.location}*\n")
          .append("   📅 Date: ${DateUtils.formatToIndianDate(s.dateString)}\n")
          .append("   🩺 Specialist Doctor: $doctorDisplay\n")
          .append("   👥 Staff Attender: $attenderDisplay\n\n")
    }
    sb.append("_Prepared via MH Outreach System_")

    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share $monthName Plan List")
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: MhViewModel,
    modifier: Modifier = Modifier
) {
    val schedules by viewModel.schedules.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val attenders by viewModel.attenders.collectAsState()

    // Form states from ViewModel
    val dateString by viewModel.scheduleDate.collectAsState()
    val selectedDoc by viewModel.scheduleDoctor.collectAsState()
    val selectedAtt by viewModel.scheduleAttender.collectAsState()
    val location by viewModel.scheduleLocation.collectAsState()
    val targetMonth by viewModel.scheduleTargetMonth.collectAsState()
    val editingId by viewModel.editingScheduleId.collectAsState()

    var isFormVisible by remember { mutableStateOf(false) }

    LaunchedEffect(editingId) {
        if (editingId != null) {
            isFormVisible = true
        } else {
            isFormVisible = false
        }
    }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var showDeleteConfirmDialog by remember { mutableStateOf<OutreachSchedule?>(null) }

    // Specialist list dropdown state
    var docDropdownExpanded by remember { mutableStateOf(false) }
    var attDropdownExpanded by remember { mutableStateOf(false) }
    var targetMonthDropdownExpanded by remember { mutableStateOf(false) }
    var locationDropdownExpanded by remember { mutableStateOf(false) }

    val locationInteractionSource = remember { MutableInteractionSource() }
    val isLocationPressed by locationInteractionSource.collectIsPressedAsState()
    LaunchedEffect(isLocationPressed) {
        if (isLocationPressed) {
            locationDropdownExpanded = true
        }
    }

    val targetMonthInteractionSource = remember { MutableInteractionSource() }
    val isTargetMonthPressed by targetMonthInteractionSource.collectIsPressedAsState()
    LaunchedEffect(isTargetMonthPressed) {
        if (isTargetMonthPressed) {
            targetMonthDropdownExpanded = true
        }
    }

    val docInteractionSource = remember { MutableInteractionSource() }
    val isDocPressed by docInteractionSource.collectIsPressedAsState()
    LaunchedEffect(isDocPressed) {
        if (isDocPressed) {
            docDropdownExpanded = true
        }
    }

    val attInteractionSource = remember { MutableInteractionSource() }
    val isAttPressed by attInteractionSource.collectIsPressedAsState()
    LaunchedEffect(isAttPressed) {
        if (isAttPressed) {
            attDropdownExpanded = true
        }
    }

    val monthsList = remember {
        listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }

    val hospitalList = remember {
        listOf(
            "Gosanimari Rural Hospital",
            "Bamanhat Rural Hospital",
            "Sitai Rural Hospital",
            "Ashokbari BPHC",
            "Ghoksadanga Rural Hospital",
            "Sitalkuchi Rural Hospital",
            "Changrabandha BPHC",
            "Haldibari Rural Hospital"
        )
    }

    // Current Date Information
    val today = remember { Calendar.getInstance() }
    val todayDayOfMonth = today.get(Calendar.DAY_OF_MONTH)
    val todayMonthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(today.time)
    
    // Check if within 27th to 30/31st of the month rule
    val maxDayOfMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH)
    val isPeriodActive = todayDayOfMonth >= 27 && todayDayOfMonth <= maxDayOfMonth
    val suggestedTargetCampMonthName = remember {
        val nextMonth = Calendar.getInstance()
        nextMonth.add(Calendar.MONTH, 1)
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(nextMonth.time)
    }

    // Auto-calculate the target camp month if not custom set
    LaunchedEffect(dateString) {
        if (targetMonth.isEmpty()) {
            try {
                val parsed = DateUtils.parseDate(dateString)
                val cal = Calendar.getInstance().apply { time = parsed }
                viewModel.scheduleTargetMonth.value = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
            } catch (e: Exception) {
                // fall back
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Planning Window Announcement Banner
        item {
            PlanningRuleBanner(
                isPeriodActive = isPeriodActive,
                todayDayOfMonth = todayDayOfMonth,
                todayMonthName = todayMonthName,
                suggestedTargetCampMonthName = suggestedTargetCampMonthName
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Outreach Planning",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = {
                        isFormVisible = !isFormVisible
                        if (!isFormVisible && editingId != null) {
                            viewModel.cancelEditingSchedule()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormVisible) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFormVisible) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isFormVisible) "Close Form" else "Plan New Camp", fontSize = 12.sp)
                }
            }
        }

        // Form Section Card
        item {
            AnimatedVisibility(
                visible = isFormVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                    Text(
                        text = if (editingId != null) "Edit Camp Schedule" else "Plan New Outreach Camp",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Calendar Day Matrix Widget
                    DateSelectorWidget(
                        dateString = dateString,
                        onDateSelected = {
                            viewModel.scheduleDate.value = it
                            // Force recalculate target month
                            try {
                                val parsed = DateUtils.parseDate(it)
                                val cal = Calendar.getInstance().apply { time = parsed }
                                cal.add(Calendar.MONTH, 2)
                                viewModel.scheduleTargetMonth.value = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
                            } catch (e: Exception) { /* ignore */ }
                        }
                    )

                    // Target Month Indicator & Dropdown Picker
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = targetMonth,
                            onValueChange = { viewModel.scheduleTargetMonth.value = it },
                            label = { Text("Target Outreach Month") },
                            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { targetMonthDropdownExpanded = !targetMonthDropdownExpanded }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand month options"
                                    )
                                }
                            },
                            supportingText = { Text("Select monthly outreach period or type manually") },
                            singleLine = true,
                            interactionSource = targetMonthInteractionSource,
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = targetMonthDropdownExpanded,
                            onDismissRequest = { targetMonthDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            monthsList.forEach { monthName ->
                                DropdownMenuItem(
                                    text = { Text(monthName) },
                                    onClick = {
                                        val yearSuffix = try {
                                            val parsed = DateUtils.parseDate(dateString)
                                            val cal = Calendar.getInstance().apply { time = parsed }
                                            cal.get(Calendar.YEAR).toString()
                                        } catch (e: Exception) {
                                            Calendar.getInstance().get(Calendar.YEAR).toString()
                                        }
                                        viewModel.scheduleTargetMonth.value = "$monthName $yearSuffix"
                                        targetMonthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Outreach Location Selection & Hospital Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = location,
                                onValueChange = { viewModel.scheduleLocation.value = it },
                                label = { Text("Hospital or Camp Location") },
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { locationDropdownExpanded = !locationDropdownExpanded }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Show default hospitals list"
                                        )
                                    }
                                },
                                placeholder = { Text("Select or type camp location...") },
                                singleLine = true,
                                interactionSource = locationInteractionSource,
                                modifier = Modifier.fillMaxWidth()
                            )

                            DropdownMenu(
                                expanded = locationDropdownExpanded,
                                onDismissRequest = { locationDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                hospitalList.forEach { hosp ->
                                    DropdownMenuItem(
                                        text = { Text(hosp) },
                                        onClick = {
                                            viewModel.scheduleLocation.value = hosp
                                            locationDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                    }

                    // Doctor Dropdown Select
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedDoc,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Assign Specialist Doctor (Optional)") },
                            leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { docDropdownExpanded = !docDropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            interactionSource = docInteractionSource,
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = docDropdownExpanded,
                            onDismissRequest = { docDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("None (Leave Optional)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary) },
                                onClick = {
                                    viewModel.scheduleDoctor.value = ""
                                    docDropdownExpanded = false
                                }
                            )
                            if (doctors.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No Specialists registered. Register first.", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        docDropdownExpanded = false
                                        viewModel.navigateTo(Screen.Doctors)
                                    }
                                )
                            } else {
                                doctors.forEach { doc ->
                                    DropdownMenuItem(
                                        text = { Text(doc.name) },
                                        onClick = {
                                            viewModel.scheduleDoctor.value = doc.name
                                            docDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Attender Selection Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedAtt,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Assign Staff Attender (Optional)") },
                            leadingIcon = { Icon(Icons.Default.AssignmentInd, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { attDropdownExpanded = !attDropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            interactionSource = attInteractionSource,
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = attDropdownExpanded,
                            onDismissRequest = { attDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("None (Leave Optional)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary) },
                                onClick = {
                                    viewModel.scheduleAttender.value = ""
                                    attDropdownExpanded = false
                                }
                            )
                            if (attenders.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No Attenders registered. Register first.", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        attDropdownExpanded = false
                                        viewModel.navigateTo(Screen.Attenders)
                                    }
                                )
                            } else {
                                attenders.forEach { att ->
                                    DropdownMenuItem(
                                        text = { Text(att.name) },
                                        onClick = {
                                            viewModel.scheduleAttender.value = att.name
                                            attDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (editingId != null) {
                            OutlinedButton(
                                onClick = { viewModel.cancelEditingSchedule() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.saveSchedule()
                                isFormVisible = false
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (editingId != null) "Update Schedule" else "Commit Schedule")
                        }
                    }
                }
            }
        }
    }

        // List Header
        item {
            Text(
                text = "Planned Outreach Calendar (${schedules.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (schedules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No planned schedules on record.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Group schedules by target planning month
            val groupedSchedules = schedules.groupBy { it.targetMonth }
            groupedSchedules.forEach { (targetM, scheduleList) ->
                val sortedScheduleList = scheduleList.sortedWith { s1, s2 ->
                    val date1 = DateUtils.parseDate(s1.dateString)
                    val date2 = DateUtils.parseDate(s2.dateString)
                    date1.compareTo(date2)
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target camp: $targetM",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        TextButton(
                            onClick = { shareMonthlySchedules(context, targetM, sortedScheduleList) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share, 
                                contentDescription = "Share monthly schedules on WhatsApp",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Month List", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                items(sortedScheduleList) { schedule ->
                    ScheduleItemCard(
                        schedule = schedule,
                        onEdit = { viewModel.startEditingSchedule(schedule) },
                        onDelete = { showDeleteConfirmDialog = schedule },
                        onShare = { shareScheduleOnWhatsApp(context, schedule) }
                    )
                }
            }
        }
    }

    // Deletion confirmation Dialog
    if (showDeleteConfirmDialog != null) {
        val entry = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Schedule Entrance?") },
            text = { Text("This will remove the planned outreach camp planned at ${entry.location} config database parameters permanently. Confirm?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSchedule(entry.id)
                        showDeleteConfirmDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PlanningRuleBanner(
    isPeriodActive: Boolean,
    todayDayOfMonth: Int,
    todayMonthName: String,
    suggestedTargetCampMonthName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPeriodActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
            } else {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = if (isPeriodActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPeriodActive) Icons.Default.Timer else Icons.Default.Event,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPeriodActive) "Official Planning Window OPEN" else "Planning Window Upcoming",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPeriodActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPeriodActive) {
                        "Today is Day $todayDayOfMonth of $todayMonthName. You are currently within the official 27th to end-of-month camp assignment window for the upcoming $suggestedTargetCampMonthName outreach schedule!"
                    } else {
                        "The standard scheduling window opens from the 27th to the end of each month to assign next month's outreach camps (e.g. June 27th assigns July camps)."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPeriodActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectorWidget(
    dateString: String,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

    // Parse the initial date if valid, else use current time
    val initialMillis = remember(dateString) {
        try {
            DateUtils.parseDate(dateString).time
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        ) {
            OutlinedTextField(
                value = dateString,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Outreach Camp Date") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Open Calendar",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            // Invisible Box overlaying the text field to intercept taps perfectly anywhere!
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showDatePicker = true }
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                val formatted = formatter.format(Date(selectedMillis))
                                onDateSelected(formatted)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun ScheduleItemCard(
    schedule: OutreachSchedule,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = schedule.location,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Schedule via WhatsApp",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Schedule",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Schedule",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PLANNING DATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = DateUtils.formatToIndianDate(schedule.dateString),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SPECIALIST",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = schedule.doctorName.ifBlank { "Not Assigned" },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (schedule.doctorName.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "STAFF ATTENDER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = schedule.attenderName.ifBlank { "Not Assigned" },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (schedule.attenderName.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Prepared in: ${schedule.creationMonth}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Status: Approved",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
