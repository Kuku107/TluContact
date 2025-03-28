package com.tlucontact.data.models

data class Student(
    var id: String = "",
    val studentId: String = "",
    val fullName: String = "",
    val photoURL: String? = null,
    val phone: String? = null,
    val email: String = "",
    val address: String? = null,
    val className: String = "",
    val userId: String = ""
)