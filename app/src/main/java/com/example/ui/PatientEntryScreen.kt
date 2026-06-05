package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Patient
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientEntryScreen(
    viewModel: MhViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val doctors by viewModel.doctors.collectAsState()
    val attenders by viewModel.attenders.collectAsState()

    // Form values
    val campDate by viewModel.patientDate.collectAsState()
    val selectedOutreach by viewModel.outreachCentre.collectAsState()
    val selectedDoctor by viewModel.attendingDoctor.collectAsState()
    val selectedAttender by viewModel.attenderNameSelected.collectAsState()

    val type by viewModel.patientType.collectAsState()
    val regNo by viewModel.patientRegNo.collectAsState()
    val name by viewModel.patientName.collectAsState()
    val age by viewModel.patientAge.collectAsState()
    val sex by viewModel.patientSex.collectAsState()
    val diagSelect by viewModel.diagnosisSelect.collectAsState()
    val diagManual by viewModel.diagnosisManual.collectAsState()
    val selectedCats by viewModel.selectedCategories.collectAsState()

    val suggestions by viewModel.oldPatientsSuggestions.collectAsState()
    val editingPatientId by viewModel.editingPatientId.collectAsState()
    val isEditing = editingPatientId != null

    // Dropdown toggles
    var outreachExpanded by remember { mutableStateOf(false) }
    var doctorExpanded by remember { mutableStateOf(false) }
    var attenderExpanded by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }
    var diagnosisExpanded by remember { mutableStateOf(false) }

    // Date picker setup
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            viewModel.patientDate.value = sdf.format(cal.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Form list of options
    val outreachList = listOf(
        "Gosanimari RH", "Bamanhat RH", "Sitai RH", "Ashokbari BPHC",
        "Ghoksadanga RH", "Sitalkuchi RH", "Changrabandha BPHC", "Haldibari RH"
    )

    val sexList = listOf("Male", "Female", "Other")

    val diagnosisList = listOf(
        "Moderate Depression with Somatic Symptoms", "F20", "OCD", "GAD",
        "BPAD", "ADHD", "IDD", "Mental & Behaviour Disorder",
        "Seizure Disorder", "Conduct Disorder", "Psychosis NOS", "Manual"
    )

    val categories = listOf(
        "CMD" to "Common Mental Disorder (CMD)",
        "SMD" to "Severe Mental Disorder (SMD)",
        "SUD" to "Substance Use Disorder (SUD)",
        "Epilepsy" to "Epilepsy",
        "Dementia" to "Dementia"
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
                text = if (isEditing) "Edit Outreach Camp Record" else "Outreach Camp Registration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // SECTION 1: Camp & Outreach Information
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
                        text = "1. Camp & Location Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Date Picker Text field
                    OutlinedTextField(
                        value = campDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Registration Date") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }
                    )

                    // Outreach Centre Dropdown
                    ExposedDropdownMenuBox(
                        expanded = outreachExpanded,
                        onExpandedChange = { outreachExpanded = !outreachExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedOutreach,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Outreach Location Centre") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = outreachExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = outreachExpanded,
                            onDismissRequest = { outreachExpanded = false }
                        ) {
                            outreachList.forEach { outreach ->
                                DropdownMenuItem(
                                    text = { Text(outreach) },
                                    onClick = {
                                        viewModel.outreachCentre.value = outreach
                                        outreachExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Attending Doctor Dropdown
                    ExposedDropdownMenuBox(
                        expanded = doctorExpanded,
                        onExpandedChange = { doctorExpanded = !doctorExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDoctor,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Attending Specialist Doctor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doctorExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = doctorExpanded,
                            onDismissRequest = { doctorExpanded = false }
                        ) {
                            if (doctors.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No doctors found! Register Doctor first.") },
                                    onClick = { doctorExpanded = false }
                                )
                            } else {
                                doctors.forEach { doc ->
                                    DropdownMenuItem(
                                        text = { Text(doc.name) },
                                        onClick = {
                                            viewModel.attendingDoctor.value = doc.name
                                            doctorExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Attender Desk Dropdown
                    ExposedDropdownMenuBox(
                        expanded = attenderExpanded,
                        onExpandedChange = { attenderExpanded = !attenderExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedAttender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assistant Staff Attender") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = attenderExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = attenderExpanded,
                            onDismissRequest = { attenderExpanded = false }
                        ) {
                            if (attenders.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No staff attenders found! (Optional)") },
                                    onClick = { attenderExpanded = false }
                                )
                            } else {
                                attenders.forEach { att ->
                                    DropdownMenuItem(
                                        text = { Text(att.name) },
                                        onClick = {
                                            viewModel.attenderNameSelected.value = att.name
                                            attenderExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: Patient Registry Parameters
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
                        text = "2. Patient Visit Specifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Visit Type (New / Old) Toggle switch
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Patient Visit Type:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                selected = type == "New",
                                onClick = { viewModel.patientType.value = "New" },
                                label = { 
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("New Registration") 
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = type == "Old",
                                onClick = { viewModel.patientType.value = "Old" },
                                label = { 
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("Old Patient Case") 
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Patient ID Number Card representation
                    OutlinedTextField(
                        value = regNo,
                        onValueChange = { viewModel.patientRegNo.value = it },
                        readOnly = false,
                        label = { Text("Registration No. (Patient ID)") },
                        leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter or select ID number") }
                    )

                    // Name Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.patientName.value = it },
                            label = {
                                if (type == "Old") {
                                    Text("Patient Name (Search existing database...)")
                                } else {
                                    Text("Patient Full Name")
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Face, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                if (type == "Old") Text("Type name to find matching patient...")
                            }
                        )

                        // Auto Suggestions for Old Patient visits
                        if (type == "Old" && suggestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text(
                                        text = "Auto-fill database records:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                    suggestions.forEach { p ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    focusManager.clearFocus()
                                                    viewModel.selectOldPatient(p)
                                                }
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = p.patientName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "ID: ${p.regNo}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    }
                                }
                            }
                        }
                    }

                    // Age and Sex (Horizontal split Row)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { viewModel.patientAge.value = it },
                            label = { Text("Age (Years)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        ExposedDropdownMenuBox(
                            expanded = sexExpanded,
                            onExpandedChange = { sexExpanded = !sexExpanded },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = sex,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Biological Sex") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = sexExpanded,
                                onDismissRequest = { sexExpanded = false }
                            ) {
                                sexList.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s) },
                                        onClick = {
                                            viewModel.patientSex.value = s
                                            sexExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Diagnosis Select dropdown
                    ExposedDropdownMenuBox(
                        expanded = diagnosisExpanded,
                        onExpandedChange = { diagnosisExpanded = !diagnosisExpanded }
                    ) {
                        OutlinedTextField(
                            value = diagSelect,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Clinical Diagnosis Presumed") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diagnosisExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = diagnosisExpanded,
                            onDismissRequest = { diagnosisExpanded = false }
                        ) {
                            diagnosisList.forEach { diag ->
                                DropdownMenuItem(
                                    text = { Text(diag) },
                                    onClick = {
                                        viewModel.diagnosisSelect.value = diag
                                        diagnosisExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Manual manual text field if "Manual" is selected
                    AnimatedVisibility(visible = diagSelect == "Manual") {
                        OutlinedTextField(
                            value = diagManual,
                            onValueChange = { viewModel.diagnosisManual.value = it },
                            label = { Text("Specify Custom Diagnosis Details") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Type complete symptoms or ICD classification here...") }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Checkbox Categories select box
                    Text(
                        text = "Screening Mental Health Categories (Select all applicable)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { (catId, catDesc) ->
                            val isChecked = selectedCats.contains(catId)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleCategory(catId) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.toggleCategory(catId) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = catDesc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 3: Action Buttons Action Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.clearPatientForm() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isEditing) "Cancel Edit" else "Reset Entry")
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.savePatient()
                    },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isEditing) "Update Record" else "Submit Registration")
                }
            }
        }
    }
}
