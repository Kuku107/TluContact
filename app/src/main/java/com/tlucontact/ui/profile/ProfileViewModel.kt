package com.tlucontact.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    init {
        // Initialize with default values or load from preferences/repository
        _username.value = "Người dùng TLU"
        _email.value = "sinhvien@e.tlu.edu.vn"
    }
}