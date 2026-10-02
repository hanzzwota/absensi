package com.example.data.repository

import com.example.data.local.StudentDao
import com.example.data.model.ClassEntity
import com.example.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow

class StudentRepository(private val studentDao: StudentDao) {

    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    val allClasses: Flow<List<ClassEntity>> = studentDao.getAllClasses()
    val studentCount: Flow<Int> = studentDao.getStudentCount()

    suspend fun getStudentByNis(nis: String): StudentEntity? = studentDao.getStudentByNis(nis)

    suspend fun getStudentByBarcode(barcodeId: String): StudentEntity? = studentDao.getStudentByBarcode(barcodeId)

    fun getStudentsByClass(className: String): Flow<List<StudentEntity>> = studentDao.getStudentsByClass(className)

    suspend fun insertStudent(student: StudentEntity) = studentDao.insertStudent(student)

    suspend fun updateStudent(student: StudentEntity) = studentDao.updateStudent(student)

    suspend fun deleteStudent(nis: String) = studentDao.deleteStudentByNis(nis)

    suspend fun insertClass(classEntity: ClassEntity) = studentDao.insertClass(classEntity)

    suspend fun deleteClass(id: String) = studentDao.deleteClassById(id)
}
