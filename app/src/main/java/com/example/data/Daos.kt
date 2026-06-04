package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorDao {
    @Query("SELECT * FROM doctors ORDER BY name ASC")
    fun getAllDoctorsFlow(): Flow<List<Doctor>>

    @Query("SELECT * FROM doctors ORDER BY name ASC")
    suspend fun getAllDoctors(): List<Doctor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: Doctor)

    @Query("DELETE FROM doctors WHERE id = :id")
    suspend fun deleteDoctorById(id: Long)
}

@Dao
interface AttenderDao {
    @Query("SELECT * FROM attenders ORDER BY name ASC")
    fun getAllAttendersFlow(): Flow<List<Attender>>

    @Query("SELECT * FROM attenders ORDER BY name ASC")
    suspend fun getAllAttenders(): List<Attender>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttender(attender: Attender)

    @Query("DELETE FROM attenders WHERE id = :id")
    suspend fun deleteAttenderById(id: Long)
}

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY date DESC, id DESC")
    fun getAllPatientsFlow(): Flow<List<Patient>>

    @Query("SELECT * FROM patients ORDER BY date DESC, id DESC")
    suspend fun getAllPatients(): List<Patient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatients(patients: List<Patient>)

    @Query("DELETE FROM patients WHERE id = :id")
    suspend fun deletePatientById(id: Long)

    @Query("DELETE FROM patients")
    suspend fun deleteAllPatients()

    @Query("DELETE FROM doctors")
    suspend fun deleteAllDoctors()

    @Query("DELETE FROM attenders")
    suspend fun deleteAllAttenders()
}
