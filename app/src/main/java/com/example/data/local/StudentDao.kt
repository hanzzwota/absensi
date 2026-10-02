package com.example.data.local

import androidx.room.*
import com.example.data.model.ClassEntity
import com.example.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE nis = :nis LIMIT 1")
    suspend fun getStudentByNis(nis: String): StudentEntity?

    @Query("SELECT * FROM students WHERE barcodeId = :barcodeId LIMIT 1")
    suspend fun getStudentByBarcode(barcodeId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE className = :className ORDER BY name ASC")
    fun getStudentsByClass(className: String): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE nis = :nis")
    suspend fun deleteStudentByNis(nis: String)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    // Class DAO methods
    @Query("SELECT * FROM classes ORDER BY name ASC")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<ClassEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity)

    @Query("DELETE FROM classes WHERE id = :id")
    suspend fun deleteClassById(id: String)
}
