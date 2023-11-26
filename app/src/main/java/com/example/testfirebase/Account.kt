package com.example.testfirebase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Account(
    val uniqueIdentifier: String? = null,
    val balance: Int = 0
)