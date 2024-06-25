package com.firebase.pushnotification.validation

import com.firebase.pushnotification.models.UserDetails


fun UserDetails.isValidData(): Boolean {
    return (name.isNotEmpty() && email.isNotEmpty() && profileImagePath.isNotEmpty())
}

fun UserDetails.isValidUpdatedData(): Boolean {
    return (name.isNotEmpty() && email.isNotEmpty())
}