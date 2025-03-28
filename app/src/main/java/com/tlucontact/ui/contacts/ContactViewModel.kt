package com.tlucontact.ui.contacts

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlucontact.data.models.ContactModel
import com.tlucontact.data.repository.ContactRepository
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {
    private val repository = ContactRepository()

    private val _contactList = MutableLiveData<List<ContactModel>>(emptyList())
    val contactList: LiveData<List<ContactModel>> = _contactList

    private val _selectedContact = MutableLiveData<ContactModel?>(null)
    val selectedContact: LiveData<ContactModel?> = _selectedContact

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _operationSuccessful = MutableLiveData<Boolean>(false)
    val operationSuccessful: LiveData<Boolean> = _operationSuccessful

    // Tải danh sách liên hệ
    fun loadContacts() {
        _loading.value = true
        _error.value = null // Reset error state

        viewModelScope.launch {
            try {
                val contacts = repository.getUserContacts()
                _contactList.value = contacts


                _error.value = null

                // Log để debug
                Log.d("ContactViewModel", "Loaded ${contacts.size} contacts")
            } catch (e: Exception) {
                _error.value = "Không thể tải danh bạ: ${e.message}"
                Log.e("ContactViewModel", "Error loading contacts", e)
            } finally {
                _loading.value = false
            }
        }
    }

    // Tìm kiếm liên hệ
    fun searchContacts(query: String) {
        if (query.isBlank()) {
            loadContacts()
            return
        }

        _loading.value = true
        _error.value = null // Reset error state

        viewModelScope.launch {
            try {
                val contacts = repository.searchContacts(query)
                _contactList.value = contacts

                // Chỉ hiển thị thông báo không tìm thấy nếu thành công và danh sách rỗng
                if (contacts.isEmpty()) {
                    _error.value = "Không tìm thấy liên hệ nào phù hợp"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi tìm kiếm: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // THÊM: Tải thông tin chi tiết một liên hệ
    fun loadContact(contactId: String) {
        if (contactId.isEmpty()) {
            _error.value = "ID liên hệ không hợp lệ"
            return
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                val contact = repository.getContact(contactId)
                if (contact != null) {
                    _selectedContact.value = contact
                    _error.value = null
                } else {
                    _error.value = "Không tìm thấy liên hệ"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi tải dữ liệu: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Thêm liên hệ mới
    fun addContact(contact: ContactModel) {
        _loading.value = true
        _operationSuccessful.value = false

        viewModelScope.launch {
            try {
                val contactId = repository.addContact(contact)
                if (contactId.isNotEmpty()) {
                    _operationSuccessful.value = true
                    loadContacts() // Tải lại danh sách sau khi thêm
                } else {
                    _error.value = "Không thể thêm liên hệ"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi thêm liên hệ: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Cập nhật liên hệ
    fun updateContact(contact: ContactModel) {
        if (contact.id.isEmpty()) {
            _error.value = "ID liên hệ không hợp lệ"
            return
        }

        _loading.value = true
        _operationSuccessful.value = false

        viewModelScope.launch {
            try {
                val success = repository.updateContact(contact)
                if (success) {
                    _operationSuccessful.value = true
                    _selectedContact.value = contact // Cập nhật contact đã chọn
                    loadContacts() // Tải lại danh sách sau khi cập nhật
                } else {
                    _error.value = "Không thể cập nhật liên hệ"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi cập nhật liên hệ: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // THÊM: Xóa liên hệ
    fun deleteContact(contactId: String) {
        if (contactId.isEmpty()) {
            _error.value = "ID liên hệ không hợp lệ"
            return
        }

        _loading.value = true
        _operationSuccessful.value = false
        _error.value = null // Reset error

        viewModelScope.launch {
            try {
                // Thêm logging để debug
                Log.d("ContactViewModel", "Deleting contact with ID: $contactId")

                val success = repository.deleteContact(contactId)
                if (success) {
                    _operationSuccessful.value = true
                    _selectedContact.value = null

                    // Log thành công
                    Log.d("ContactViewModel", "Successfully deleted contact: $contactId")

                    // Tải lại danh sách sau khi xóa
                    val updatedList = _contactList.value?.filter { it.id != contactId } ?: emptyList()
                    _contactList.value = updatedList
                } else {
                    _error.value = "Không thể xóa liên hệ"
                    Log.e("ContactViewModel", "Failed to delete contact: $contactId")
                }
            } catch (e: Exception) {
                _error.value = "Lỗi xóa liên hệ: ${e.message}"
                Log.e("ContactViewModel", "Exception when deleting contact: ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearSelectedContact() {
        _selectedContact.value = null
    }

    // THÊM: Reset trạng thái thao tác
    fun resetOperationStatus() {
        _operationSuccessful.value = false
        _error.value = null
    }
}