package com.firebase.pushnotification.listener

import com.firebase.pushnotification.models.UserDetails

interface HandleButtonAction {
    fun onClickUpdate(userDetails: UserDetails)
    fun onClickDelete(uid: UserDetails)
}