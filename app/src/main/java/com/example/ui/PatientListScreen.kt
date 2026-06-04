package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Patient

@Composable
fun PatientListScreen(
    viewModel: MhViewModel,
    modifier: Modifier = Modifier
) {
    val patients by viewModel.filteredPatients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var patientToDelete by remember { mutableStateOf<Patient?>(null) }

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Patient Registry Records Search",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Search Section Card
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search by name, ID no., location center...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Total Found: ${patients.size} Records",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (patients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No patients have been registered in database yet."
                        else "No records match search prefix criteria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(patients, key = { p -> p.id }) { p ->
                        PatientVisitRowCard(
                            patient = p,
                            onEditClick = { viewModel.startEditingPatient(p) },
                            onDeleteClick = { patientToDelete = p }
                        )
                    }
                }
            }
        }

        patientToDelete?.let { patient ->
            AlertDialog(
                onDismissRequest = { patientToDelete = null },
                title = { Text(text = stringResource(id = R.string.delete_confirm_title_patient)) },
                text = {
                    Text(
                        text = stringResource(
                            id = R.string.delete_confirm_message_patient,
                            patient.patientName,
                            patient.regNo
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletePatient(patient.id)
                            patientToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(text = stringResource(id = R.string.delete_confirm_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { patientToDelete = null }) {
                        Text(text = stringResource(id = R.string.delete_confirm_no))
                    }
                }
            )
        }
    }
}

@Composable
fun PatientVisitRowCard(
    patient: Patient,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: ID & Type Badge & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = patient.regNo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    val visitBadgeBg = if (patient.type == "New") Color(0xFF198754) else Color(0xFF17A2B8)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(visitBadgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = patient.type,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Visit Record",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Visit Record",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Body: Patient demographics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.5f)) {
                    Text(
                        text = patient.patientName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Classification: ${patient.age} yrs / ${patient.sex}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = patient.date,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = patient.outreach,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Sub: Doctor and Attenders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Dr. ${patient.doctorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (patient.attenderName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AssignmentInd,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = patient.attenderName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Diagnoses details and categories
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Presumed Diagnosis: ${patient.diagnosis}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val cats = patient.getCategoryList()
                    if (cats.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            cats.forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = c,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
