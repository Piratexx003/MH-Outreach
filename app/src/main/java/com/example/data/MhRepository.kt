package com.example.data

import kotlinx.coroutines.flow.Flow

class MhRepository(private val db: AppDatabase) {
    private val doctorDao = db.doctorDao()
    private val attenderDao = db.attenderDao()
    private val patientDao = db.patientDao()

    // Doctors
    val allDoctors: Flow<List<Doctor>> = doctorDao.getAllDoctorsFlow()
    suspend fun getDoctorsList(): List<Doctor> = doctorDao.getAllDoctors()
    suspend fun insertDoctor(doctor: Doctor) = doctorDao.insertDoctor(doctor)
    suspend fun deleteDoctorById(id: Long) = doctorDao.deleteDoctorById(id)

    // Attenders
    val allAttenders: Flow<List<Attender>> = attenderDao.getAllAttendersFlow()
    suspend fun getAttendersList(): List<Attender> = attenderDao.getAllAttenders()
    suspend fun insertAttender(attender: Attender) = attenderDao.insertAttender(attender)
    suspend fun deleteAttenderById(id: Long) = attenderDao.deleteAttenderById(id)

    // Patients
    val allPatients: Flow<List<Patient>> = patientDao.getAllPatientsFlow()
    suspend fun getPatientsList(): List<Patient> = patientDao.getAllPatients()
    suspend fun insertPatient(patient: Patient) = patientDao.insertPatient(patient)
    suspend fun insertPatients(patients: List<Patient>) = patientDao.insertPatients(patients)
    suspend fun deletePatientById(id: Long) = patientDao.deletePatientById(id)

    // Clear and restore tables for backup
    suspend fun restoreDatabase(doctors: List<Doctor>, attenders: List<Attender>, patients: List<Patient>) {
        db.runInTransaction {
            // Room does not support suspending inside runInTransaction natively, 
            // but we can execute direct blocking calls or run on dispatcher.
            // Since this runs on dispatcher we can clear tables first
        }
    }
    
    suspend fun clearAllData() {
        patientDao.deleteAllPatients()
        patientDao.deleteAllDoctors()
        patientDao.deleteAllAttenders()
    }
}
