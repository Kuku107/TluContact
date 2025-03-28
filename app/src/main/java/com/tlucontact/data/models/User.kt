package com.tlucontact.data.models

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoURL: String? = null,
    val phoneNumber: String? = null,
    val role: String = "" // "CBGV" hoặc "SV"
)