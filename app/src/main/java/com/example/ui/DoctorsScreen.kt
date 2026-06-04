package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.Doctor

@Composable
fun DoctorsScreen(
    viewModel: MhViewModel,
    modifier: Modifier = Modifier
) {
    val doctors by viewModel.doctors.collectAsState()
    val name by viewModel.doctorName.collectAsState()
    val mobile by viewModel.doctorMobile.collectAsState()
    val editingDoctorId by viewModel.editingDoctorId.collectAsState()
    val focusManager = LocalFocusManager.current
    var doctorToDelete by remember { mutableStateOf<Doctor?>(null) }

    val isEditing = editingDoctorId != null

    @Composable
    fun DoctorFormCard(modifierInner: Modifier = Modifier) {
        Card(
            modifier = modifierInner,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Medical Specialist" else "Add New Medical Specialist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.doctorName.value = it },
                    label = { Text("Doctor Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { viewModel.doctorMobile.value = it },
                    label = { Text("Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isEditing) {
                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.cancelEditingDoctor()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.saveDoctor()
                        },
                        modifier = Modifier.weight(if (isEditing) 1.5f else 1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEditing) "Update Specialist" else "Save Doctor Specialist")
                    }
                }
            }
        }
    }

    @Composable
    fun DoctorListSection(modifierInner: Modifier = Modifier) {
        Column(
            modifier = modifierInner,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registered Doctors (${doctors.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (doctors.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No doctors registered yet.\nPlease use the form to add doctors.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(doctors, key = { it.id }) { doc ->
                        DoctorRowItem(
                            doctor = doc,
                            onEditClick = { viewModel.startEditingDoctor(doc) },
                            onDeleteClick = { doctorToDelete = doc }
                        )
                    }
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val isWide = maxWidth >= 720.dp
        if (isWide) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                DoctorFormCard(modifierInner = Modifier.weight(1.2f))
                DoctorListSection(modifierInner = Modifier.weight(1.8f))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DoctorFormCard(modifierInner = Modifier.fillMaxWidth())
                DoctorListSection(modifierInner = Modifier.weight(1f))
            }
        }

        doctorToDelete?.let { doctor ->
            AlertDialog(
                onDismissRequest = { doctorToDelete = null },
                title = { Text(text = stringResource(id = R.string.delete_confirm_title_doctor)) },
                text = {
                    Text(
                        text = stringResource(
                            id = R.string.delete_confirm_message_doctor,
                            doctor.name
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDoctor(doctor.id)
                            doctorToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(text = stringResource(id = R.string.delete_confirm_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { doctorToDelete = null }) {
                        Text(text = stringResource(id = R.string.delete_confirm_no))
                    }
                }
            )
        }
    }
}

@Composable
fun DoctorRowItem(
    doctor: Doctor,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doctor.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = doctor.mobile,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Doctor Specialist",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Doctor Specialist",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
