package com.tlucontact.data.models

data class Staff(
    var id: String = "",
    val staffId: String = "",
    val fullName: String = "",
    val position: String = "",
    val phone: String? = null,
    val email: String = "",
    val photoURL: String? = null,
    val unitId: String = "",
    val userId: String = ""
)