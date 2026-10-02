package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.StudentEntity
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.NotificationRepository
import com.example.data.repository.StudentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ParentViewModel(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _selectedNis = MutableStateFlow<String>("STD2025001")
    val selectedNis: StateFlow<String> = _selectedNis

    val childStudent: StateFlow<StudentEntity?> = _selectedNis
        .flatMapLatest { nis ->
            flow { emit(studentRepository.getStudentByNis(nis)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val childAttendanceHistory: StateFlow<List<AttendanceEntity>> = _selectedNis
        .flatMapLatest { nis ->
            attendanceRepository.getAttendanceByStudent(nis)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val childNotificationLogs: StateFlow<List<NotificationLogEntity>> = _selectedNis
        .flatMapLatest { nis ->
            notificationRepository.getLogsByStudent(nis)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedChildNis(nis: String) {
        _selectedNis.value = nis
    }
}
