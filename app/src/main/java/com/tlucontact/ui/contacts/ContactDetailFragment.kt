package com.tlucontact.ui.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
    private var phoneNumber: String? = null

    // Launcher cho permission request
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Quyền được cấp, thực hiện cuộc gọi
                phoneNumber?.let { makePhoneCall(it) }
            } else {
                // Quyền bị từ chối
                Toast.makeText(
                    context,
                    "Cần cấp quyền gọi điện để sử dụng tính năng này",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactDetailBinding.inflate(inflater, container, false)
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

        // Nút gọi điện - Cập nhật xử lý sự kiện
        binding.btnCall.setOnClickListener {
            phoneNumber?.let { number ->
                if (number.isNotEmpty()) {
                    // Kiểm tra và yêu cầu quyền nếu cần
                    if (checkCallPhonePermission()) {
                        makePhoneCall(number)
                    } else {
                        requestCallPhonePermission()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Không tìm thấy số điện thoại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } ?: run {
                Toast.makeText(
                    context,
                    "Không tìm thấy số điện thoại",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Nút nhắn tin
        binding.btnMessage.setOnClickListener {
            phoneNumber?.let { number ->
                if (number.isNotEmpty()) {
                    sendSms(number)
                } else {
                    Toast.makeText(
                        context,
                        "Không tìm thấy số điện thoại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } ?: run {
                Toast.makeText(
                    context,
                    "Không tìm thấy số điện thoại",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Nút email
        binding.btnEmail.setOnClickListener {
            val email = binding.tvEmail.text.toString()
            if (email.isNotEmpty() && email != "Không có") {
                sendEmail(email)
            } else {
                Toast.makeText(
                    context,
                    "Không tìm thấy địa chỉ email",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Nút sửa và xóa giữ nguyên...
        binding.btnEdit.setOnClickListener {
            contactId?.let { id ->
                val bundle = Bundle().apply {
                    putString("contactId", id)
                }
                findNavController().navigate(R.id.action_contact_detail_to_edit_contact, bundle)
            }
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    // Thêm các phương thức mới
    private fun checkCallPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCallPhonePermission() {
        requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
    }

    private fun makePhoneCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        try {
            startActivity(callIntent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Không thể thực hiện cuộc gọi: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendSms(phoneNumber: String) {
        val smsIntent = Intent(Intent.ACTION_VIEW)
        smsIntent.data = Uri.parse("sms:$phoneNumber")
        try {
            startActivity(smsIntent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Không thể mở ứng dụng nhắn tin: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendEmail(email: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:$email")
        try {
            startActivity(Intent.createChooser(emailIntent, "Gửi email"))
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Không thể mở ứng dụng email: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val contactId = contactId ?: return // Thêm kiểm tra null

        // Sử dụng MaterialAlertDialogBuilder thay vì AlertDialog.Builder thông thường
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xóa liên hệ")
            .setMessage("Bạn có chắc chắn muốn xóa liên hệ này?")
            .setPositiveButton("Xóa") { _, _ ->
                binding.progressBar.visibility = View.VISIBLE // Hiển thị loading
                viewModel.deleteContact(contactId)
            }
            .setNegativeButton("Hủy", null)
            .setCancelable(false) // Ngăn việc bấm ngoài dialog để đóng
            .create()

        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
            if (contact != null) {
                binding.tvName.text = contact.name
                binding.tvPhone.text = contact.phone
                // Lưu số điện thoại để sử dụng khi gọi
                phoneNumber = contact.phone

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

        // Giữ nguyên các observers khác
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