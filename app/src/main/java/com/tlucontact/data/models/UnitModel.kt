package com.tlucontact.data.models

data class UnitModel(
    var id: String = "",
    val code: String = "",
    val name: String = "",
    val address: String = "",
    val logoURL: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val fax: String? = null,
    val parentUnitId: String? = null,
    val type: String = ""
)