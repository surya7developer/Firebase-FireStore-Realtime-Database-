package com.firebase.pushnotification.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.firebase.pushnotification.R
import com.firebase.pushnotification.databinding.UpdateUserDetailsDialogBinding
import com.firebase.pushnotification.extension.showToastMessage
import com.firebase.pushnotification.models.UserDetails
import com.firebase.pushnotification.validation.isValidData
import com.google.android.material.bottomsheet.BottomSheetDialog


fun Context.showBottomSheetDialog(userDetails: UserDetails, action: (UserDetails) -> Unit) {
    val bottomSheetDialog = BottomSheetDialog(this)
    val binding = UpdateUserDetailsDialogBinding.inflate(LayoutInflater.from(this))

    binding.apply {
        edtName.setText(userDetails.name)
        edtEmail.setText(userDetails.email)
        btnUpdate.setOnClickListener {
            val updatedData = UserDetails(
                uid = userDetails.uid,
                name = edtName.text.toString(),
                email = edtEmail.text.toString(),
            )
            if (updatedData.isValidData()){
                action.invoke(updatedData)
                bottomSheetDialog.dismiss()
            } else {
                showToastMessage(getString(R.string.please_enter_valid_details))
            }
        }
    }

    bottomSheetDialog.setContentView(binding.root)
    bottomSheetDialog.show()
}


fun Context.showDeleteDialog(action:()->Unit) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Alert!")
    builder.setMessage("Are you sure want to delete?")

    builder.setPositiveButton("Yes") { dialog, _ ->
        action.invoke()
        dialog.dismiss()
    }

    builder.setNegativeButton("Cancel") { dialog, _ ->
        dialog.dismiss()
    }
    builder.show()
}
