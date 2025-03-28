package com.tlucontact.ui.staff

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
import com.tlucontact.databinding.FragmentStaffDetailBinding

class StaffDetailFragment : Fragment() {

    private var _binding: FragmentStaffDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StaffViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val staffId = arguments?.getString("staffId")
        if (staffId != null) {
            viewModel.getStaffDetail(staffId)
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
            val phoneNumber = viewModel.selectedStaff.value?.phone
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
            val emailAddress = viewModel.selectedStaff.value?.email
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
        viewModel.selectedStaff.observe(viewLifecycleOwner) { staff ->
            binding.tvStaffName.text = staff.name
            binding.tvPosition.text = "${staff.title} - ${staff.position}"
            binding.tvPhone.text = staff.phone
            binding.tvEmail.text = staff.email
            binding.tvAddress.text = staff.address
            binding.tvBio.text = staff.bio

            // Load subjects
            val subjectsText = staff.subjects.joinToString(", ")
            binding.tvSubjects.text = if (subjectsText.isEmpty()) "Không có thông tin" else subjectsText

            // Load avatar
            if (!staff.avatarUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(staff.avatarUrl)
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