package com.firebase.pushnotification.models

data class UserDetails(
    val name: String = "",
    val email: String = "",
) {
    fun isValidData(): Boolean {
        return (name.isNotEmpty() && email.isNotEmpty())
    }
}
