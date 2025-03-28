package com.tlucontact.data.models

data class StudentModel(
    var id: String = "",
    val studentId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val className: String = "",
    val major: String = "",
    val unitId: String = "",
    val avatarUrl: String? = null,
    val address: String = "",
    val dateOfBirth: String = ""
)