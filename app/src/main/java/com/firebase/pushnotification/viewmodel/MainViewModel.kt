package com.firebase.pushnotification.viewmodel

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.firebase.pushnotification.constant.PHOTOS
import com.firebase.pushnotification.constant.USERS
import com.firebase.pushnotification.models.UserDetails
import com.firebase.pushnotification.validation.isValidData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class MainViewModel : ViewModel() {

    private var database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storageReference: StorageReference = FirebaseStorage.getInstance().getReference()
    private val firebaseStorageReference: FirebaseStorage = FirebaseStorage.getInstance()

    var profileImagePath: Uri = "".toUri()

    //Trigger when we add new user details
    private val _uiState = MutableStateFlow<CreateUserDataState>(CreateUserDataState.None)
    private val uiState: StateFlow<CreateUserDataState> = _uiState.asStateFlow()

    //Trigger when we get all the users data
    private val _fetchAllUsersData = MutableStateFlow<CreateUserDataState>(CreateUserDataState.None)
    private val fetchAllUsersData: StateFlow<CreateUserDataState> = _fetchAllUsersData.asStateFlow()

    fun getUiState() = uiState
    fun getAllUsersData() = fetchAllUsersData

    fun createUserData(userData: UserDetails) {
        if (userData.isValidData()) {

            _uiState.value = CreateUserDataState.Loading
            userData.uid = database.collection(USERS).document().id

            database.collection(USERS).document(userData.uid).set(userData)
                .addOnCompleteListener { task ->
                    when {
                        task.isSuccessful -> _uiState.value =
                            CreateUserDataState.Success(mutableListOf())

                        else -> _uiState.value =
                            CreateUserDataState.Error(task.exception?.message.toString())
                    }
                }

            uploadProfileImage(userData)

        } else {
            _uiState.value = CreateUserDataState.ValidationMessage
        }
    }

    private fun uploadProfileImage(userData: UserDetails) {

        val fileReference = storageReference.child(PHOTOS).child(userData.uid)

        fileReference.putFile(profileImagePath).addOnSuccessListener {
                fileReference.getDownloadUrl().addOnSuccessListener { uri ->
                    val imageUrl: String = uri.toString()
                    userData.profileImagePath = imageUrl
                    updateUserProfileURL(userData)
                }
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
                _fetchAllUsersData.value =
                    CreateUserDataState.Error(message = error.message.toString())
            }
        }
    }

    fun updateUserDetails(updatedData: UserDetails) {
        database.collection(USERS).document(updatedData.uid).update(
            "name", updatedData.name, "email", updatedData.email
        ).addOnCompleteListener { task ->
            when {
                task.isSuccessful -> _uiState.value = CreateUserDataState.Update
                else -> _uiState.value =
                    CreateUserDataState.Error(task.exception?.message.toString())
            }
        }
    }

    private fun updateUserProfileURL(updatedData: UserDetails) {
        database.collection(USERS).document(updatedData.uid)
            .update("profileImagePath", updatedData.profileImagePath)
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> {
                        Log.d("createUserData", "update url successfully")
                        _uiState.value = CreateUserDataState.Update
                    }

                    else -> _uiState.value =
                        CreateUserDataState.Error(task.exception?.message.toString())
                }
            }
    }

    fun deleteUser(userDetails: UserDetails) {
        database.collection(USERS).document(userDetails.uid).delete()
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> _uiState.value = CreateUserDataState.Delete
                    else -> _uiState.value =
                        CreateUserDataState.Error(task.exception?.message.toString())
                }
            }
        firebaseStorageReference.getReferenceFromUrl(userDetails.profileImagePath).delete()
    }

    sealed class CreateUserDataState {
        data object None : CreateUserDataState()
        data object Loading : CreateUserDataState()
        data object ValidationMessage : CreateUserDataState()
        data object Update : CreateUserDataState()
        data object Delete : CreateUserDataState()
        data class Success(val data: MutableList<UserDetails>) : CreateUserDataState()
        data class Error(val message: String) : CreateUserDataState()
    }
}