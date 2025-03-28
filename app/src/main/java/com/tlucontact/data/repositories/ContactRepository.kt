package com.tlucontact.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tlucontact.data.models.ContactModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ContactRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val contactsCollection = db.collection("contacts")

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""


    suspend fun getUserContacts(): List<ContactModel> = withContext(Dispatchers.IO) {
        if (currentUserId.isEmpty()) return@withContext emptyList<ContactModel>()

        try {
            val snapshot = contactsCollection
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .await()

            val result = snapshot.documents.mapNotNull { doc ->
                val contact = doc.toObject(ContactModel::class.java)
                contact?.id = doc.id
                contact
            }


            Log.d("ContactRepository", "Fetched ${result.size} contacts for user $currentUserId")

            result
        } catch (e: Exception) {
            Log.e("ContactRepository", "Error fetching contacts: ${e.message}", e)
            emptyList()
        }
    }

    // Tìm kiếm liên hệ
    suspend fun searchContacts(query: String): List<ContactModel> = withContext(Dispatchers.IO) {
        if (currentUserId.isEmpty() || query.isBlank()) return@withContext emptyList<ContactModel>()

        val lowerQuery = query.lowercase()

        try {
            val allUserContacts = contactsCollection
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .await()

            allUserContacts.documents.mapNotNull { doc ->
                val contact = doc.toObject(ContactModel::class.java)
                contact?.id = doc.id
                contact
            }.filter {
                it.name.lowercase().contains(lowerQuery) ||
                        it.phone.contains(query) ||
                        it.email.lowercase().contains(lowerQuery)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Lấy chi tiết một liên hệ
    suspend fun getContact(contactId: String): ContactModel? = withContext(Dispatchers.IO) {
        if (currentUserId.isEmpty() || contactId.isEmpty()) return@withContext null

        try {
            val doc = contactsCollection.document(contactId).get().await()
            val contact = doc.toObject(ContactModel::class.java)

            // Kiểm tra xem liên hệ này có thuộc về người dùng hiện tại không
            if (contact?.ownerId == currentUserId) {
                contact.id = doc.id
                contact
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Thêm liên hệ mới
    suspend fun addContact(contact: ContactModel): String = withContext(Dispatchers.IO) {
        if (currentUserId.isEmpty()) return@withContext ""

        // Đảm bảo contact được gán cho người dùng hiện tại
        val newContact = contact.copy(
            ownerId = currentUserId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        try {
            val ref = contactsCollection.add(newContact).await()
            ref.id
        } catch (e: Exception) {
            ""
        }
    }

    // Cập nhật liên hệ
    suspend fun updateContact(contact: ContactModel): Boolean = withContext(Dispatchers.IO) {
        if (currentUserId.isEmpty() || contact.id.isEmpty()) return@withContext false

        try {
            // Kiểm tra quyền sở hữu trước khi cập nhật
            val doc = contactsCollection.document(contact.id).get().await()
            val existingContact = doc.toObject(ContactModel::class.java)

            if (existingContact?.ownerId != currentUserId) {
                return@withContext false
            }

            // Cập nhật với thời gian hiện tại
            val updatedContact = contact.copy(
                ownerId = currentUserId, // Đảm bảo không thay đổi owner
                updatedAt = System.currentTimeMillis()
            )

            contactsCollection.document(contact.id).set(updatedContact).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Xóa liên hệ
    suspend fun deleteContact(contactId: String): Boolean = withContext(Dispatchers.IO) {
        if (currentUserId.isEmpty() || contactId.isEmpty()) {
            Log.e("ContactRepository", "Invalid parameters: userId=$currentUserId, contactId=$contactId")
            return@withContext false
        }

        try {
            // Kiểm tra quyền sở hữu trước khi xóa
            val doc = contactsCollection.document(contactId).get().await()
            val existingContact = doc.toObject(ContactModel::class.java)

            if (existingContact == null) {
                Log.e("ContactRepository", "Contact not found: $contactId")
                return@withContext false
            }

            if (existingContact.ownerId != currentUserId) {
                Log.e("ContactRepository", "Permission denied: contact owner=${existingContact.ownerId}, currentUser=$currentUserId")
                return@withContext false
            }

            // Thực hiện xóa với await() để đảm bảo hoàn thành
            contactsCollection.document(contactId).delete().await()
            Log.d("ContactRepository", "Contact deleted successfully: $contactId")

            return@withContext true
        } catch (e: Exception) {
            Log.e("ContactRepository", "Error deleting contact: ${e.message}", e)
            return@withContext false
        }
    }
}