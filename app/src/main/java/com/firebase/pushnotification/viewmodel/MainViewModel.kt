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

    private val _fetchAllUsersData = MutableStateFlow<CreateUserDataState>(CreateUserDataState.None)
    private val fetchAllUsersData: StateFlow<CreateUserDataState> = _fetchAllUsersData.asStateFlow()

    fun getUiState() = uiState
    fun getAllUsersData() = fetchAllUsersData

    fun createUserData(userData: UserDetails) {

        if (userData.isValidData()) {
            _uiState.value = CreateUserDataState.Loading

            database.collection(USERS).document().set(userData).addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> _uiState.value = CreateUserDataState.Success(mutableListOf())
                    else -> _uiState.value = CreateUserDataState.Error(task.exception?.message.toString())
                }
            }
        } else {
            _uiState.value = CreateUserDataState.ValidationMessage
        }
    }

    fun fetchAllUsersData() {
        database.collection(USERS).addSnapshotListener { value, error ->
            if (error == null) {
                value?.let {
                    val data = value.toObjects(UserDetails::class.java)
                    _fetchAllUsersData.value = CreateUserDataState.Success(data = data)
                } ?: emptyArray<UserDetails>()
            } else {
                _fetchAllUsersData.value = CreateUserDataState.Error(message = error.message.toString())
            }
        }
    }

    sealed class CreateUserDataState {
        data object None : CreateUserDataState()
        data object Loading : CreateUserDataState()
        data object ValidationMessage : CreateUserDataState()
        data class Success(val data: MutableList<UserDetails>) : CreateUserDataState()
        data class Error(val message: String) : CreateUserDataState()
    }
}