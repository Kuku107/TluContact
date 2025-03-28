package com.tlucontact.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.tlucontact.R
import com.tlucontact.data.models.ContactModel
import com.tlucontact.databinding.FragmentContactEditBinding

class ContactEditFragment : Fragment() {

    private var _binding: FragmentContactEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by viewModels()
    private var contactId: String? = null
    private var isEditMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Kiểm tra xem đang ở chế độ thêm mới hay cập nhật
        contactId = arguments?.getString("contactId")
        isEditMode = contactId != null

        // Cập nhật tiêu đề
        binding.tvTitle.text = if (isEditMode) "Sửa liên hệ" else "Thêm liên hệ mới"

        setupClickListeners()
        observeViewModel()

        // Nếu ở chế độ sửa, tải dữ liệu liên hệ hiện tại
        if (isEditMode) {
            contactId?.let { id ->
                viewModel.loadContact(id)
            }
        }
    }

    private fun setupClickListeners() {
        // Nút quay lại
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Nút lưu
        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                saveContact()
            }
        }
    }

    private fun validateInput(): Boolean {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        // Kiểm tra tên
        if (name.isEmpty()) {
            binding.tilName.error = "Vui lòng nhập tên"
            return false
        } else {
            binding.tilName.error = null
        }

        // Kiểm tra số điện thoại
        if (phone.isEmpty()) {
            binding.tilPhone.error = "Vui lòng nhập số điện thoại"
            return false
        } else {
            binding.tilPhone.error = null
        }

        return true
    }

    private fun saveContact() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        val contact = if (isEditMode) {
            ContactModel(
                id = contactId ?: "",
                name = name,
                phone = phone,
                email = email,
                address = address,
                note = note,
                // Các trường khác sẽ được giữ nguyên bởi repository
                avatarUrl = viewModel.selectedContact.value?.avatarUrl,
                ownerId = viewModel.selectedContact.value?.ownerId ?: ""
            )
        } else {
            ContactModel(
                name = name,
                phone = phone,
                email = email,
                address = address,
                note = note
                // ownerId sẽ được thiết lập trong repository
            )
        }

        if (isEditMode) {
            viewModel.updateContact(contact)
        } else {
            viewModel.addContact(contact)
        }
    }

    private fun observeViewModel() {
        viewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
            if (contact != null && isEditMode) {
                binding.etName.setText(contact.name)
                binding.etPhone.setText(contact.phone)
                binding.etEmail.setText(contact.email)
                binding.etAddress.setText(contact.address)
                binding.etNote.setText(contact.note)

                // Load avatar nếu có
                if (!contact.avatarUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(contact.avatarUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(binding.ivAvatar)
                }
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.formLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.operationSuccessful.observe(viewLifecycleOwner) { success ->
            if (success) {
                val message = if (isEditMode) "Cập nhật liên hệ thành công" else "Thêm liên hệ thành công"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
                viewModel.resetOperationStatus()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}