package com.tlucontact.ui.units

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tlucontact.data.models.UnitModel  // Updated import

class UnitsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _units = MutableLiveData<List<UnitModel>>()  // Updated type
    val units: LiveData<List<UnitModel>> = _units  // Updated type

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _selectedUnit = MutableLiveData<UnitModel>()  // Updated type
    val selectedUnit: LiveData<UnitModel> = _selectedUnit  // Updated type

    fun loadUnits(filterType: String? = null) {
        _loading.value = true

        var query: Query = db.collection("units")
            .orderBy("name")

        if (filterType != null) {
            query = query.whereEqualTo("type", filterType)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val unitsList = documents.mapNotNull { doc ->
                    doc.toObject(UnitModel::class.java).apply { id = doc.id }  // Updated class
                }
                _units.value = unitsList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
                _units.value = emptyList()
            }
    }

    fun searchUnits(searchTerm: String) {
        if (searchTerm.isEmpty()) {
            loadUnits()
            return
        }

        _loading.value = true

        // Search using name and code fields (client-side filtering)
        db.collection("units")
            .orderBy("name")
            .get()
            .addOnSuccessListener { documents ->
                val unitsList = documents.mapNotNull { doc ->
                    doc.toObject(UnitModel::class.java).apply { id = doc.id }  // Updated class
                }.filter { unit ->
                    unit.name.contains(searchTerm, ignoreCase = true) ||
                            unit.code.contains(searchTerm, ignoreCase = true)
                }
                _units.value = unitsList
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
                _units.value = emptyList()
            }
    }

    fun getUnitDetails(unitId: String) {
        _loading.value = true

        db.collection("units").document(unitId)
            .get()
            .addOnSuccessListener { document ->
                document.toObject(UnitModel::class.java)?.apply { id = document.id }?.let { unit ->  // Updated class
                    _selectedUnit.value = unit
                }
                _loading.value = false
            }
            .addOnFailureListener {
                _loading.value = false
            }
    }
}