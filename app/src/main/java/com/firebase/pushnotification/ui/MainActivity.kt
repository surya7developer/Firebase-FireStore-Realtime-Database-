package com.firebase.pushnotification.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
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
import com.firebase.pushnotification.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), OnClickListener, HandleButtonAction {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var userListAdapter: UsersListAdapter
    private lateinit var listener: HandleButtonAction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initialization()
        setAdapter()
        setClickListener()
        createUserObserver()
        getAllUsersDataObserver()
        fetchAllData()
    }

    private fun initialization() {
        listener = this
    }

    private fun fetchAllData() {
        mainViewModel.fetchAllUsersData()
    }

    private fun setAdapter() {
        userListAdapter = UsersListAdapter(arrayListOf(), listener)
        binding.rvUsersList.adapter = userListAdapter
    }

    private fun setClickListener() {
        binding.btnAddToFirebase.setOnClickListener(this)
    }

    private fun createUserObserver() {
        lifecycleScope.launch {
            mainViewModel.getUiState().collect { result ->
                when (result) {
                    is MainViewModel.CreateUserDataState.Success -> {
                        hideShowLoader(false)
                        showToastMessage("User data sent successfully")
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
        }
    }

    private fun addUserData() {
        binding.apply {
            mainViewModel.createUserData(
                UserDetails(
                    edtName.text.toString(), edtEmail.text.toString()
                )
            )

            edtName.setText("")
            edtEmail.setText("")
        }
    }

    override fun onClickUpdate(userDetails: UserDetails) {
        showBottomSheetDialog(userDetails) { updatedData ->
            mainViewModel.updateUserDetails(updatedData)
            showToastMessage("Update data successfully")
        }
    }

    override fun onClickDelete(uid: String) {
        showDeleteDialog {
            mainViewModel.deleteUser(uid)
            showToastMessage("Delete successfully")
        }
    }
}