package com.tlucontact.ui.staff

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.tlucontact.data.models.StaffModel

class StaffViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _staffList = MutableLiveData<List<StaffModel>>()
    val staffList: LiveData<List<StaffModel>> = _staffList

    private val _selectedStaff = MutableLiveData<StaffModel>()
    val selectedStaff: LiveData<StaffModel> = _selectedStaff

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadStaff() {
        _loading.value = true

        db.collection("staff")
            .get()
            .addOnSuccessListener { documents ->
                val staffList = documents.mapNotNull { doc ->
                    doc.toObject(StaffModel::class.java).apply { id = doc.id }
                }
                _staffList.value = staffList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
                _error.value = "Không thể tải danh sách cán bộ/giảng viên"
            }
    }

    fun loadStaffByUnit(unitId: String) {
        _loading.value = true

        db.collection("staff")
            .whereEqualTo("unitId", unitId)
            .get()
            .addOnSuccessListener { documents ->
                val staffList = documents.mapNotNull { doc ->
                    doc.toObject(StaffModel::class.java).apply { id = doc.id }
                }
                _staffList.value = staffList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
                _error.value = "Không thể tải danh sách cán bộ/giảng viên của đơn vị"
            }
    }

    fun getStaffDetail(staffId: String) {
        _loading.value = true

        db.collection("staff").document(staffId)
            .get()
            .addOnSuccessListener { document ->
                document.toObject(StaffModel::class.java)?.apply { id = document.id }?.let { staff ->
                    _selectedStaff.value = staff
                }
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
                _error.value = "Không thể tải thông tin chi tiết cán bộ/giảng viên"
            }
    }

    fun searchStaff(query: String) {
        _loading.value = true

        // Client-side search
        db.collection("staff")
            .get()
            .addOnSuccessListener { documents ->
                val staffList = documents.mapNotNull { doc ->
                    doc.toObject(StaffModel::class.java).apply { id = doc.id }
                }.filter { staff ->
                    staff.name.contains(query, ignoreCase = true) ||
                            staff.position.contains(query, ignoreCase = true)
                }
                _staffList.value = staffList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
                _error.value = "Không thể tìm kiếm cán bộ/giảng viên"
            }
    }
}