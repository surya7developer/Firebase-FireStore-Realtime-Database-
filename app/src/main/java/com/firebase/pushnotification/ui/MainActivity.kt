package com.firebase.pushnotification.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.firebase.pushnotification.R
import com.firebase.pushnotification.databinding.ActivityMainBinding
import com.firebase.pushnotification.extension.gone
import com.firebase.pushnotification.extension.showToastMessage
import com.firebase.pushnotification.extension.visible
import com.firebase.pushnotification.models.UserDetails
import com.firebase.pushnotification.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        setClickListener()
        observer()
    }

    private fun setClickListener() {
        binding.btnAddToFirebase.setOnClickListener(this)
    }

    private fun observer() {
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
                        showToastMessage("Failed, Please try again")
                    }

                    else -> {}
                }
            }
        }
    }

    private fun hideShowLoader(showLoader: Boolean) {
        binding.loader.apply {
            if (showLoader) visible() else gone()
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
                    edtName.text.toString(),
                    edtEmail.text.toString()
                )
            )

            edtName.setText("")
            edtEmail.setText("")
        }
    }
}