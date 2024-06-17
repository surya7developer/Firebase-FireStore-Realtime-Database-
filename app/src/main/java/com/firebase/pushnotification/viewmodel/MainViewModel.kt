package com.firebase.pushnotification.viewmodel

import androidx.lifecycle.ViewModel
import com.firebase.pushnotification.constant.USERS
import com.firebase.pushnotification.models.UserDetails
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    private var database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<CreateUserDataState>(CreateUserDataState.None)
    private val uiState: StateFlow<CreateUserDataState> = _uiState.asStateFlow()

    fun getUiState() = uiState

    fun createUserData(userData: UserDetails) {
        _uiState.value = CreateUserDataState.Loading

        database.collection(USERS).document().set(userData).addOnCompleteListener { task ->
            when {
                task.isSuccessful -> _uiState.value = CreateUserDataState.Success
                else -> _uiState.value = CreateUserDataState.Error
            }
        }
    }

    sealed class CreateUserDataState {
        data object None:CreateUserDataState()
        data object Loading:CreateUserDataState()
        data object Success:CreateUserDataState()
        data object Error:CreateUserDataState()
    }
}