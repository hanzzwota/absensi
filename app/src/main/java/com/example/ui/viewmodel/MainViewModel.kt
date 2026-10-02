package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.AuditLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val auditLogRepository: AuditLogRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserEntity?>(
        UserEntity("usr_admin", "Principal Sarah Connor", "admin@school.edu", UserRole.ADMIN)
    )
    val currentUser: StateFlow<UserEntity?> = _currentUser

    fun loginAs(user: UserEntity) {
        _currentUser.value = user
        viewModelScope.launch {
            auditLogRepository.logAction(
                actorName = user.name,
                actorRole = user.role.name,
                action = "USER_LOGIN",
                details = "User logged in with role ${user.role.name}"
            )
        }
    }

    fun logout() {
        _currentUser.value = null
    }
}
