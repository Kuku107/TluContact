package com.tlucontact.ui.contacts

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.tlucontact.R
import com.tlucontact.databinding.FragmentContactDetailBinding

class ContactDetailFragment : Fragment() {

    private var _binding: FragmentContactDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private var contactId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactDetailBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true) // Bật menu options
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Lấy contactId từ arguments
        contactId = arguments?.getString("contactId")

        if (contactId == null) {
            Toast.makeText(context, "Không tìm thấy thông tin liên hệ", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupClickListeners()
        observeViewModel()

        // Load thông tin chi tiết liên hệ
        viewModel.loadContact(contactId!!)
    }

    private fun setupClickListeners() {
        // Nút quay lại
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Nút gọi điện
        binding.btnCall.setOnClickListener {
            // Chức năng gọi điện - sẽ triển khai sau
            Toast.makeText(context, "Chức năng gọi điện đang được phát triển", Toast.LENGTH_SHORT).show()
        }

        // Nút nhắn tin
        binding.btnMessage.setOnClickListener {
            // Chức năng nhắn tin - sẽ triển khai sau
            Toast.makeText(context, "Chức năng nhắn tin đang được phát triển", Toast.LENGTH_SHORT).show()
        }

        // Nút email
        binding.btnEmail.setOnClickListener {
            // Chức năng gửi email - sẽ triển khai sau
            Toast.makeText(context, "Chức năng gửi email đang được phát triển", Toast.LENGTH_SHORT).show()
        }

        // Nút sửa
        binding.btnEdit.setOnClickListener {
            contactId?.let { id ->
                val bundle = Bundle().apply {
                    putString("contactId", id)
                }
                findNavController().navigate(R.id.action_contact_detail_to_edit_contact, bundle)
            }
        }

        // Nút xóa
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa liên hệ")
            .setMessage("Bạn có chắc chắn muốn xóa liên hệ này?")
            .setPositiveButton("Xóa") { _, _ ->
                contactId?.let { id ->
                    viewModel.deleteContact(id)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
            if (contact != null) {
                binding.tvName.text = contact.name
                binding.tvPhone.text = contact.phone
                binding.tvEmail.text = if (contact.email.isNotEmpty()) contact.email else "Không có"
                binding.tvAddress.text = if (contact.address.isNotEmpty()) contact.address else "Không có"

                if (contact.note.isNotEmpty()) {
                    binding.tvNote.text = contact.note
                    binding.noteLayout.visibility = View.VISIBLE
                } else {
                    binding.noteLayout.visibility = View.GONE
                }

                // Load avatar
                if (!contact.avatarUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(contact.avatarUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(binding.ivAvatar)
                } else {
                    binding.ivAvatar.setImageResource(R.drawable.ic_profile)
                }

                // Xử lý hiện/ẩn các nút
                val hasEmail = contact.email.isNotEmpty()
                binding.btnEmail.visibility = if (hasEmail) View.VISIBLE else View.GONE
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.operationSuccessful.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Xóa liên hệ thành công", Toast.LENGTH_SHORT).show()
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