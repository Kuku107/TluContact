package com.tlucontact.data.models

data class StaffModel(
    var id: String = "",
    val name: String = "",
    val title: String = "",
    val email: String = "",
    val phone: String = "",
    val position: String = "",
    val avatarUrl: String? = null,
    val unitId: String = "",
    val address: String = "",
    val bio: String = "",
    val subjects: List<String> = emptyList()
)