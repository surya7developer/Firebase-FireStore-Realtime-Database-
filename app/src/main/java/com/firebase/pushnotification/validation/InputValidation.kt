package com.firebase.pushnotification.validation

import com.firebase.pushnotification.models.UserDetails


fun UserDetails.isValidData(): Boolean {
    return (name.isNotEmpty() && email.isNotEmpty())
}