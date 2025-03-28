package com.tlucontact.ui.students

import android.content.Intent
import android.net.Uri
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
import com.tlucontact.databinding.FragmentStudentDetailBinding

class StudentDetailFragment : Fragment() {

    private var _binding: FragmentStudentDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val studentId = arguments?.getString("studentId")
        if (studentId != null) {
            viewModel.getStudentDetail(studentId)
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Thiết lập chức năng gọi điện
        binding.btnCall.setOnClickListener {
            val phoneNumber = viewModel.selectedStudent.value?.phone
            if (!phoneNumber.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                startActivity(intent)
            } else {
                Toast.makeText(context, "Không có số điện thoại", Toast.LENGTH_SHORT).show()
            }
        }

        // Thiết lập chức năng gửi email
        binding.btnEmail.setOnClickListener {
            val emailAddress = viewModel.selectedStudent.value?.email
            if (!emailAddress.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$emailAddress")
                    putExtra(Intent.EXTRA_SUBJECT, "Liên hệ từ TLUContact")
                }
                startActivity(intent)
            } else {
                Toast.makeText(context, "Không có địa chỉ email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.selectedStudent.observe(viewLifecycleOwner) { student ->
            binding.tvStudentName.text = student.name
            binding.tvStudentId.text = student.studentId
            binding.tvClass.text = student.className
            binding.tvMajor.text = student.major
            binding.tvPhone.text = student.phone
            binding.tvEmail.text = student.email
            binding.tvAddress.text = student.address
            binding.tvDob.text = student.dateOfBirth

            // Load avatar
            if (!student.avatarUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(student.avatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.ivAvatar)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}