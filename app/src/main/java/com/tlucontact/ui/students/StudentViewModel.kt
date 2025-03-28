package com.tlucontact.ui.students

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.tlucontact.data.models.StudentModel

class StudentViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _studentList = MutableLiveData<List<StudentModel>>()
    val studentList: LiveData<List<StudentModel>> = _studentList

    private val _selectedStudent = MutableLiveData<StudentModel>()
    val selectedStudent: LiveData<StudentModel> = _selectedStudent

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadStudents() {
        _loading.value = true

        db.collection("students")
            .get()
            .addOnSuccessListener { documents ->
                val studentList = documents.mapNotNull { doc ->
                    doc.toObject(StudentModel::class.java).apply { id = doc.id }
                }
                _studentList.value = studentList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
                _error.value = "Không thể tải danh sách sinh viên"
            }
    }

    fun loadStudentsByUnit(unitId: String) {
        _loading.value = true

        db.collection("students")
            .whereEqualTo("unitId", unitId)
            .get()
            .addOnSuccessListener { documents ->
                val studentList = documents.mapNotNull { doc ->
                    doc.toObject(StudentModel::class.java).apply { id = doc.id }
                }
                _studentList.value = studentList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
                _error.value = "Không thể tải danh sách sinh viên của đơn vị"
            }
    }

    fun getStudentDetail(studentId: String) {
        _loading.value = true

        db.collection("students").document(studentId)
            .get()
            .addOnSuccessListener { document ->
                document.toObject(StudentModel::class.java)?.apply { id = document.id }?.let { student ->
                    _selectedStudent.value = student
                }
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
                _error.value = "Không thể tải thông tin chi tiết sinh viên"
            }
    }

    fun searchStudents(query: String) {
        _loading.value = true

        db.collection("students")
            .get()
            .addOnSuccessListener { documents ->
                val studentList = documents.mapNotNull { doc ->
                    doc.toObject(StudentModel::class.java).apply { id = doc.id }
                }.filter { student ->
                    student.name.contains(query, ignoreCase = true) ||
                            student.studentId.contains(query, ignoreCase = true) ||
                            student.className.contains(query, ignoreCase = true)  // Đã thay đổi từ student.`class`
                }
                _studentList.value = studentList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
                _error.value = "Không thể tìm kiếm sinh viên"
            }
    }
}