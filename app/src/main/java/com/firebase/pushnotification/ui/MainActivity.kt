package com.firebase.pushnotification.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.firebase.pushnotification.R
import com.firebase.pushnotification.adapter.UsersListAdapter
import com.firebase.pushnotification.databinding.ActivityMainBinding
import com.firebase.pushnotification.dialogs.showBottomSheetDialog
import com.firebase.pushnotification.dialogs.showDeleteDialog
import com.firebase.pushnotification.extension.gone
import com.firebase.pushnotification.extension.showToastMessage
import com.firebase.pushnotification.extension.visible
import com.firebase.pushnotification.listener.HandleButtonAction
import com.firebase.pushnotification.models.UserDetails
import com.firebase.pushnotification.util.permissionList
import com.firebase.pushnotification.util.requestPermissionResult
import com.firebase.pushnotification.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), OnClickListener, HandleButtonAction {
    private lateinit var resultCallBackLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestMultiplePermissions: ActivityResultLauncher<Array<String>>
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var userListAdapter: UsersListAdapter
    private lateinit var listener: HandleButtonAction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initialization()
        callBackResult()
        setAdapter()
        setClickListener()
        createUserObserver()
        getAllUsersDataObserver()
        fetchAllData()
    }

    private fun callBackResult() {
        requestMultiplePermissions = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                Log.d("resultLauncher", "${it.key} = ${it.value}")
                if (it.value) {
                    openGallery()
                }
            }
        }

        resultCallBackLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    val uri = data?.data
                    if (uri != null) {
                        mainViewModel.profileImagePath = uri
                        binding.profileImage.setImageURI(uri)
                    } else {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun initialization() {
        listener = this
    }

    private fun fetchAllData() {
        mainViewModel.fetchAllUsersData()
    }

    private fun setAdapter() {
        userListAdapter = UsersListAdapter(arrayListOf(), listener, this)
        binding.rvUsersList.adapter = userListAdapter
    }

    private fun setClickListener() {
        binding.btnAddToFirebase.setOnClickListener(this)
        binding.profileImage.setOnClickListener(this)
    }

    private fun createUserObserver() {
        lifecycleScope.launch {
            mainViewModel.getUiState().collect { result ->
                when (result) {
                    is MainViewModel.CreateUserDataState.Success -> {
                        hideShowLoader(false)
                        showToastMessage("User data sent successfully")
                        resetData()
                    }

                    is MainViewModel.CreateUserDataState.Loading -> {
                        hideShowLoader(true)
                    }

                    is MainViewModel.CreateUserDataState.Error -> {
                        showToastMessage(result.message)
                    }

                    is MainViewModel.CreateUserDataState.ValidationMessage -> {
                        showToastMessage(getString(R.string.please_enter_valid_details))
                    }

                    else -> {}
                }
            }
        }
    }

    private fun resetData() {
        mainViewModel.profileImagePath = "".toUri()
        binding.apply {
            edtName.setText("")
            edtEmail.setText("")
            profileImage.setImageResource(R.drawable.user)
        }
    }

    private fun getAllUsersDataObserver() {
        lifecycleScope.launch {
            mainViewModel.getAllUsersData().collect { result ->
                when (result) {
                    is MainViewModel.CreateUserDataState.Success -> {
                        userListAdapter.setData(result.data)
                    }

                    is MainViewModel.CreateUserDataState.Error -> {
                        showToastMessage(result.message)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun hideShowLoader(showLoader: Boolean) {
        binding.apply {
            if (showLoader) {
                btnAddToFirebase.text = ""
                loader.visible()
            } else {
                btnAddToFirebase.text = getString(R.string.add_to_firebase)
                loader.gone()
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnAddToFirebase -> {
                addUserData()
            }

            R.id.profileImage -> {
                requestPermissionResult { isGranted ->
                    if (isGranted) {
                        openGallery()
                    } else {
                        requestMultiplePermissions.launch(permissionList)
                    }
                }
            }
        }
    }

    private fun addUserData() {
        binding.apply {
            mainViewModel.createUserData(
                UserDetails(
                    edtName.text.toString(),
                    edtEmail.text.toString(),
                    profileImagePath = mainViewModel.profileImagePath.toString()
                )
            )
        }
    }

    override fun onClickUpdate(userDetails: UserDetails) {
        showBottomSheetDialog(userDetails) { updatedData ->
            mainViewModel.updateUserDetails(updatedData)
            showToastMessage("Update data successfully")
        }
    }

    override fun onClickDelete(userDetails: UserDetails) {
        showDeleteDialog {
            mainViewModel.deleteUser(userDetails)
            showToastMessage("Delete successfully")
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        resultCallBackLauncher.launch(intent)
    }
}