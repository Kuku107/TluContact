package com.tlucontact.data.models

data class ContactModel(
    var id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val address: String = "",
    val note: String = "",
    val ownerId: String = "", // ID của người dùng sở hữu liên hệ này
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)