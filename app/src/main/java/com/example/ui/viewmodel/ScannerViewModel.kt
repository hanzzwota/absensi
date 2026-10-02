package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.StudentEntity
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.AuditLogRepository
import com.example.data.repository.ScanResult
import com.example.data.repository.StudentRepository
import com.example.ui.components.SoundFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Scanning : ScannerUiState()
    data class Success(val result: ScanResult.Success) : ScannerUiState()
    data class Duplicate(val result: ScanResult.AlreadyScanned) : ScannerUiState()
    data class NotFound(val code: String) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}

class ScannerViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val studentRepository: StudentRepository,
    private val auditLogRepository: AuditLogRepository
) : ViewModel() {

    val allStudents: StateFlow<List<StudentEntity>> = studentRepository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _scanState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val scanState: StateFlow<ScannerUiState> = _scanState

    private val _lastScannedStudent = MutableStateFlow<StudentEntity?>(null)
    val lastScannedStudent: StateFlow<StudentEntity?> = _lastScannedStudent

    fun onBarcodeScanned(code: String, staffName: String) {
        if (_scanState.value is ScannerUiState.Scanning) return

        _scanState.value = ScannerUiState.Scanning

        viewModelScope.launch {
            val result = attendanceRepository.processBarcodeScan(code, staffName)

            when (result) {
                is ScanResult.Success -> {
                    _scanState.value = ScannerUiState.Success(result)
                    _lastScannedStudent.value = result.student
                    SoundFeedback.playSuccessBeep()

                    auditLogRepository.logAction(
                        actorName = staffName,
                        actorRole = "STAFF",
                        action = "ATTENDANCE_CHECKIN",
                        details = "Marked ${result.student.name} (${result.student.className}) as ${result.attendance.status}. Notification: ${result.notificationStatus}"
                    )
                }
                is ScanResult.AlreadyScanned -> {
                    _scanState.value = ScannerUiState.Duplicate(result)
                    _lastScannedStudent.value = result.student
                    SoundFeedback.playErrorBeep()
                }
                is ScanResult.NotFound -> {
                    _scanState.value = ScannerUiState.NotFound(result.scannedCode)
                    SoundFeedback.playErrorBeep()
                }
                is ScanResult.Error -> {
                    _scanState.value = ScannerUiState.Error(result.message)
                    SoundFeedback.playErrorBeep()
                }
            }
        }
    }

    fun resetScanState() {
        _scanState.value = ScannerUiState.Idle
    }
}
