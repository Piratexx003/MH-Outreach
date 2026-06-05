package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

enum class Screen {
    Dashboard,
    Doctors,
    Attenders,
    PatientEntry,
    PatientList,
    Reports,
    Backup,
    Schedule
}

class MhViewModel(private val repository: MhRepository) : ViewModel() {

    // Toast/Message state
    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    // Screen State
    private val _currentScreen = MutableStateFlow(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Db collections
    val doctors = repository.allDoctors.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val attenders = repository.allAttenders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val patients = repository.allPatients.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val schedules = repository.allSchedules.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Doctor Entry Input
    var doctorName = MutableStateFlow("")
    var doctorMobile = MutableStateFlow("")
    val editingDoctorId = MutableStateFlow<Long?>(null)

    fun startEditingDoctor(doctor: Doctor) {
        doctorName.value = doctor.name
        doctorMobile.value = doctor.mobile
        editingDoctorId.value = doctor.id
    }

    fun cancelEditingDoctor() {
        doctorName.value = ""
        doctorMobile.value = ""
        editingDoctorId.value = null
    }

    fun saveDoctor() {
        val name = doctorName.value.trim()
        val mobile = doctorMobile.value.trim()
        if (name.isEmpty() || mobile.isEmpty()) {
            triggerMessage("Please fill all doctor fields")
            return
        }
        viewModelScope.launch {
            repository.insertDoctor(Doctor(id = editingDoctorId.value ?: 0L, name = name, mobile = mobile))
            val isEdit = editingDoctorId.value != null
            doctorName.value = ""
            doctorMobile.value = ""
            editingDoctorId.value = null
            if (isEdit) {
                triggerMessage("Specialist details updated successfully!")
            } else {
                triggerMessage("Doctor registered successfully!")
            }
        }
    }

    fun deleteDoctor(id: Long) {
        viewModelScope.launch {
            repository.deleteDoctorById(id)
            triggerMessage("Doctor deleted")
        }
    }

    // Attender Entry Input
    var attenderName = MutableStateFlow("")
    var attenderMobile = MutableStateFlow("")
    val editingAttenderId = MutableStateFlow<Long?>(null)

    fun startEditingAttender(attender: Attender) {
        attenderName.value = attender.name
        attenderMobile.value = attender.mobile
        editingAttenderId.value = attender.id
    }

    fun cancelEditingAttender() {
        attenderName.value = ""
        attenderMobile.value = ""
        editingAttenderId.value = null
    }

    fun saveAttender() {
        val name = attenderName.value.trim()
        val mobile = attenderMobile.value.trim()
        if (name.isEmpty() || mobile.isEmpty()) {
            triggerMessage("Please fill all attender fields")
            return
        }
        viewModelScope.launch {
            repository.insertAttender(Attender(id = editingAttenderId.value ?: 0L, name = name, mobile = mobile))
            val isEdit = editingAttenderId.value != null
            attenderName.value = ""
            attenderMobile.value = ""
            editingAttenderId.value = null
            if (isEdit) {
                triggerMessage("Staff details updated successfully!")
            } else {
                triggerMessage("Attender registered successfully!")
            }
        }
    }

    fun deleteAttender(id: Long) {
        viewModelScope.launch {
            repository.deleteAttenderById(id)
            triggerMessage("Attender deleted")
        }
    }

    // Schedule Input Form States
    val scheduleDate = MutableStateFlow(DateUtils.getTodayIndianDate())
    val scheduleDoctor = MutableStateFlow("")
    val scheduleAttender = MutableStateFlow("")
    val scheduleLocation = MutableStateFlow("")
    val scheduleTargetMonth = MutableStateFlow("") 
    val editingScheduleId = MutableStateFlow<Long?>(null)

    fun startEditingSchedule(schedule: OutreachSchedule) {
        scheduleDate.value = DateUtils.formatToIndianDate(schedule.dateString)
        scheduleDoctor.value = schedule.doctorName
        scheduleAttender.value = schedule.attenderName
        scheduleLocation.value = schedule.location
        scheduleTargetMonth.value = schedule.targetMonth
        editingScheduleId.value = schedule.id
    }

    fun cancelEditingSchedule() {
        scheduleDate.value = DateUtils.getTodayIndianDate()
        scheduleDoctor.value = ""
        scheduleAttender.value = ""
        scheduleLocation.value = ""
        scheduleTargetMonth.value = ""
        editingScheduleId.value = null
    }

    fun saveSchedule() {
        val dateVal = scheduleDate.value.trim()
        val docName = scheduleDoctor.value.trim()
        val attName = scheduleAttender.value.trim()
        val loc = scheduleLocation.value.trim()
        
        if (dateVal.isEmpty() || loc.isEmpty()) {
            triggerMessage("Please fill camp date and location details")
            return
        }

        // Calculate Target Month and Creation Month
        var calcTargetMonth = scheduleTargetMonth.value.trim()
        val parsedDate = DateUtils.parseDate(dateVal)

        val cal = Calendar.getInstance().apply { time = parsedDate }
        val creationMonthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

        if (calcTargetMonth.isEmpty()) {
            // Rule: Default to the Month and Year of the allocated camp date itself
            calcTargetMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(parsedDate)
        }

        viewModelScope.launch {
            repository.insertSchedule(
                OutreachSchedule(
                    id = editingScheduleId.value ?: 0L,
                    dateString = DateUtils.formatToIndianDate(dateVal),
                    targetMonth = calcTargetMonth,
                    creationMonth = creationMonthName,
                    doctorName = docName,
                    attenderName = attName,
                    location = loc
                )
            )
            val isEdit = editingScheduleId.value != null
            cancelEditingSchedule()
            if (isEdit) {
                triggerMessage("Outreach schedule updated successfully!")
            } else {
                triggerMessage("Outreach schedule added successfully!")
            }
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            repository.deleteScheduleById(id)
            triggerMessage("Outreach camp schedule entry deleted")
        }
    }

    // Patient Form States
    val editingPatientId = MutableStateFlow<Long?>(null)
    val patientDate = MutableStateFlow(DateUtils.getTodayIndianDate())
    val outreachCentre = MutableStateFlow("")
    val attendingDoctor = MutableStateFlow("")
    val attenderNameSelected = MutableStateFlow("")
    val patientType = MutableStateFlow("New") // "New" or "Old"
    val patientRegNo = MutableStateFlow("")
    val patientName = MutableStateFlow("")
    val patientAge = MutableStateFlow("")
    val patientSex = MutableStateFlow("Male")
    val diagnosisSelect = MutableStateFlow("")
    val diagnosisManual = MutableStateFlow("")
    val selectedCategories = MutableStateFlow(setOf<String>())

    // Old Patient Auto-Suggestions based on Typing Patient Name
    val oldPatientsSuggestions = combine(patientName, patients) { typedValue, allPats ->
        if (patientType.value != "Old" || typedValue.isBlank() || typedValue.contains("(ID:")) {
            emptyList<Patient>()
        } else {
            // Find distinct patients matching prefix name
            allPats.filter { it.patientName.contains(typedValue, ignoreCase = true) }
                .distinctBy { it.regNo }
                .take(5)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Trigger patientRegNo updates when outreachCentre or patientType changes
        combine(outreachCentre, patientType, patients) { centre, type, pats ->
            if (type == "New" && centre.isNotBlank()) {
                generateNextId(centre, pats)
            } else {
                ""
            }
        }.onEach { generatedId ->
            if (editingPatientId.value == null && patientType.value == "New" && generatedId.isNotEmpty()) {
                patientRegNo.value = generatedId
            }
        }.launchIn(viewModelScope)
    }

    private fun generateNextId(outreach: String, pats: List<Patient>): String {
        val prefix = when (outreach) {
            "Gosanimari RH" -> "DMHP/GM/"
            "Bamanhat RH" -> "DMHP/BHT/"
            "Sitai RH" -> "DMHP/STI/"
            "Ashokbari BPHC" -> "DMHP/MTB-I/"
            "Ghoksadanga RH" -> "DMHP/GKD/"
            "Sitalkuchi RH" -> "DMHP/SLK/"
            "Changrabandha BPHC" -> "DMHP/CBD/"
            "Haldibari RH" -> "DMHP/HLD/"
            else -> ""
        }
        if (prefix.isEmpty()) return ""

        var maxSeq = 0
        pats.forEach { p ->
            if (p.regNo.startsWith(prefix)) {
                val numStr = p.regNo.substringAfterLast("/")
                val num = numStr.toIntOrNull()
                if (num != null && num > maxSeq) {
                    maxSeq = num
                }
            }
        }
        val nextSeqStr = (maxSeq + 1).toString().padStart(2, '0')
        return prefix + nextSeqStr
    }

    fun selectOldPatient(patient: Patient) {
        patientRegNo.value = patient.regNo
        patientName.value = patient.patientName
        patientAge.value = patient.age.toString()
        patientSex.value = patient.sex
        
        val commonDiagnoses = listOf(
            "Moderate Depression with Somatic Symptoms", "F20", "OCD", "GAD", 
            "BPAD", "ADHD", "IDD", "Mental & Behaviour Disorder", 
            "Seizure Disorder", "Conduct Disorder", "Psychosis NOS"
        )
        if (commonDiagnoses.contains(patient.diagnosis)) {
            diagnosisSelect.value = patient.diagnosis
            diagnosisManual.value = ""
        } else {
            diagnosisSelect.value = "Manual"
            diagnosisManual.value = patient.diagnosis
        }
        selectedCategories.value = patient.getCategoryList().toSet()
    }

    fun toggleCategory(category: String) {
        val currentSet = selectedCategories.value.toMutableSet()
        if (currentSet.contains(category)) {
            currentSet.remove(category)
        } else {
            currentSet.add(category)
        }
        selectedCategories.value = currentSet
    }

    fun clearPatientForm() {
        patientDate.value = DateUtils.getTodayIndianDate()
        // Retain outreachCentre, attendingDoctor, and attenderNameSelected as constant/persistent states across consecutive entries.
        patientType.value = "New"
        patientRegNo.value = ""
        patientName.value = ""
        patientAge.value = ""
        patientSex.value = "Male"
        diagnosisSelect.value = ""
        diagnosisManual.value = ""
        selectedCategories.value = emptySet()
        editingPatientId.value = null
    }

    fun startEditingPatient(patient: Patient) {
        editingPatientId.value = patient.id
        patientDate.value = DateUtils.formatToIndianDate(patient.date)
        outreachCentre.value = patient.outreach
        attendingDoctor.value = patient.doctorName
        attenderNameSelected.value = patient.attenderName
        patientType.value = patient.type
        patientRegNo.value = patient.regNo
        patientName.value = patient.patientName
        patientAge.value = patient.age.toString()
        patientSex.value = patient.sex
        
        val commonDiagnoses = listOf(
            "Moderate Depression with Somatic Symptoms", "F20", "OCD", "GAD", 
            "BPAD", "ADHD", "IDD", "Mental & Behaviour Disorder", 
            "Seizure Disorder", "Conduct Disorder", "Psychosis NOS"
        )
        if (commonDiagnoses.contains(patient.diagnosis)) {
            diagnosisSelect.value = patient.diagnosis
            diagnosisManual.value = ""
        } else {
            diagnosisSelect.value = "Manual"
            diagnosisManual.value = patient.diagnosis
        }
        selectedCategories.value = patient.getCategoryList().toSet()
        _currentScreen.value = Screen.PatientEntry
    }

    fun savePatient() {
        val dateValue = patientDate.value
        val outreachValue = outreachCentre.value
        val doctorValue = attendingDoctor.value
        val attenderValue = attenderNameSelected.value
        val typeValue = patientType.value
        val regVal = patientRegNo.value.trim()
        val nameValue = patientName.value.trim()
        val ageVal = patientAge.value.trim()
        val sexValue = patientSex.value
        val diagSelectVal = diagnosisSelect.value
        val diagManualVal = diagnosisManual.value.trim()

        if (dateValue.isEmpty() || outreachValue.isEmpty() || doctorValue.isEmpty() || regVal.isEmpty() || nameValue.isEmpty() || ageVal.isEmpty()) {
            triggerMessage("Please fill all required patient details!")
            return
        }

        val ageInt = ageVal.toIntOrNull()
        if (ageInt == null || ageInt < 0) {
            triggerMessage("Please provide a valid patient age!")
            return
        }

        val finalDiagnosis = if (diagSelectVal == "Manual") diagManualVal else diagSelectVal
        if (finalDiagnosis.isEmpty()) {
            triggerMessage("Diagnosis details are required!")
            return
        }

        val catsString = selectedCategories.value.joinToString(",")

        viewModelScope.launch {
            val isEdit = editingPatientId.value != null
            val patient = Patient(
                id = editingPatientId.value ?: 0L,
                regNo = regVal,
                date = DateUtils.formatToIndianDate(dateValue),
                outreach = outreachValue,
                doctorName = doctorValue,
                attenderName = attenderValue,
                patientName = nameValue,
                age = ageInt,
                sex = sexValue,
                type = typeValue,
                diagnosis = finalDiagnosis,
                categoriesString = catsString
            )
            repository.insertPatient(patient)
            if (isEdit) {
                triggerMessage("Patient ${patient.patientName} record updated successfully!")
            } else {
                triggerMessage("Patient ${patient.patientName} registered (ID: ${patient.regNo})!")
            }
            clearPatientForm()
            _currentScreen.value = Screen.Dashboard
        }
    }

    fun deletePatient(id: Long) {
        viewModelScope.launch {
            repository.deletePatientById(id)
            triggerMessage("Patient record removed")
        }
    }

    // Patient Search and Filters
    var searchQuery = MutableStateFlow("")
    val filteredPatients = combine(searchQuery, patients) { query, allPats ->
        if (query.isBlank()) {
            allPats
        } else {
            allPats.filter {
                it.patientName.contains(query, ignoreCase = true) ||
                        it.regNo.contains(query, ignoreCase = true) ||
                        it.outreach.contains(query, ignoreCase = true) ||
                        it.diagnosis.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reports Date Filter States
    var reportFromDate = MutableStateFlow("")
    var reportToDate = MutableStateFlow("")



    fun triggerMessage(msg: String) {
        viewModelScope.launch {
            _message.emit(msg)
        }
    }

    // Export System Backup JSON
    fun getBackupJson(): String {
        val docList = doctors.value
        val attList = attenders.value
        val patList = patients.value

        val root = JSONObject()

        val docsArray = JSONArray()
        docList.forEach { d ->
            val obj = JSONObject()
            obj.put("name", d.name)
            obj.put("mobile", d.mobile)
            docsArray.put(obj)
        }

        val attsArray = JSONArray()
        attList.forEach { a ->
            val obj = JSONObject()
            obj.put("name", a.name)
            obj.put("mobile", a.mobile)
            attsArray.put(obj)
        }

        val patsArray = JSONArray()
        patList.forEach { p ->
            val obj = JSONObject()
            obj.put("regNo", p.regNo)
            obj.put("date", p.date)
            obj.put("outreach", p.outreach)
            obj.put("doctorName", p.doctorName)
            obj.put("attenderName", p.attenderName)
            obj.put("patientName", p.patientName)
            obj.put("age", p.age)
            obj.put("sex", p.sex)
            obj.put("type", p.type)
            obj.put("diagnosis", p.diagnosis)
            obj.put("categoriesString", p.categoriesString)
            patsArray.put(obj)
        }

        root.put("doctors", docsArray)
        root.put("attenders", attsArray)
        root.put("patients", patsArray)

        return root.toString(2)
    }

    // Import Restore JSON
    fun restoreDatabaseFromJson(context: Context, uri: Uri, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: throw java.lang.Exception("File not found")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                inputStream.close()

                val root = JSONObject(stringBuilder.toString())

                val doctorsList = mutableListOf<Doctor>()
                if (root.has("doctors")) {
                    val arr = root.getJSONArray("doctors")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        doctorsList.add(Doctor(name = obj.getString("name"), mobile = obj.getString("mobile")))
                    }
                }

                val attendersList = mutableListOf<Attender>()
                if (root.has("attenders")) {
                    val arr = root.getJSONArray("attenders")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        attendersList.add(Attender(name = obj.getString("name"), mobile = obj.getString("mobile")))
                    }
                }

                val patientsList = mutableListOf<Patient>()
                if (root.has("patients")) {
                    val arr = root.getJSONArray("patients")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        patientsList.add(Patient(
                            regNo = obj.getString("regNo"),
                            date = obj.getString("date"),
                            outreach = obj.getString("outreach"),
                            doctorName = obj.getString("doctorName"),
                            attenderName = obj.optString("attenderName", ""),
                            patientName = obj.getString("patientName"),
                            age = obj.getInt("age"),
                            sex = obj.getString("sex"),
                            type = obj.getString("type"),
                            diagnosis = obj.getString("diagnosis"),
                            categoriesString = obj.getString("categoriesString")
                        ))
                    }
                }

                val existingDoctors = repository.getDoctorsList()
                val existingAttenders = repository.getAttendersList()
                val existingPatients = repository.getPatientsList()

                // Filter out already existing doctors (case-insensitive name check)
                val newDoctors = doctorsList.filter { newDoc ->
                    existingDoctors.none { extDoc -> extDoc.name.trim().equals(newDoc.name.trim(), ignoreCase = true) }
                }

                // Filter out already existing attenders (case-insensitive name check)
                val newAttenders = attendersList.filter { newAtt ->
                    existingAttenders.none { extAtt -> extAtt.name.trim().equals(newAtt.name.trim(), ignoreCase = true) }
                }

                // Filter out already existing patients (exact unique visit check: regNo + date + outreach + patientName)
                val newPatients = patientsList.filter { newPat ->
                    existingPatients.none { extPat ->
                        extPat.regNo.trim().equals(newPat.regNo.trim(), ignoreCase = true) &&
                        extPat.date == newPat.date &&
                        extPat.outreach.trim().equals(newPat.outreach.trim(), ignoreCase = true) &&
                        extPat.patientName.trim().equals(newPat.patientName.trim(), ignoreCase = true)
                    }
                }

                newDoctors.forEach { repository.insertDoctor(it) }
                newAttenders.forEach { repository.insertAttender(it) }
                repository.insertPatients(newPatients)

                triggerMessage("Data merged successfully! Added ${newPatients.size} new visits, ${newDoctors.size} new doctors, and ${newAttenders.size} new staff.")
                onSuccess()
            } catch (e: Exception) {
                triggerMessage("Restore error: ${e.message}")
            }
        }
    }
}
