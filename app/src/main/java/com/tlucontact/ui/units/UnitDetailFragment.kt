package com.tlucontact.ui.units

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
import com.tlucontact.databinding.FragmentUnitDetailBinding
import com.tlucontact.ui.staff.StaffAdapter
import com.tlucontact.ui.staff.StaffViewModel

class UnitDetailFragment : Fragment() {

    private var _binding: FragmentUnitDetailBinding? = null
    private val binding get() = _binding!!

    private val unitViewModel: UnitsViewModel by viewModels()
    private val staffViewModel: StaffViewModel by viewModels()
    private lateinit var staffAdapter: StaffAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnitDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thiết lập RecyclerView cho danh sách nhân viên
        setupStaffRecyclerView()

        // Lấy ID đơn vị từ arguments
        val unitId = arguments?.getString("unitId")
        if (unitId != null) {
            unitViewModel.getUnitDetails(unitId)
            staffViewModel.loadStaffByUnit(unitId)
        }

        setupUI()
        observeViewModels()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupStaffRecyclerView() {
        staffAdapter = StaffAdapter { staff ->
            val bundle = Bundle().apply {
                putString("staffId", staff.id)
            }
            findNavController().navigate(R.id.action_unit_detail_to_staff_detail, bundle)
        }
        binding.rvStaff.adapter = staffAdapter
    }

    private fun observeViewModels() {
        unitViewModel.selectedUnit.observe(viewLifecycleOwner) { unit ->
            binding.tvUnitName.text = unit.name
            binding.tvUnitCode.text = "Mã đơn vị: ${unit.code}"
            binding.tvAddress.text = unit.address
            binding.tvEmail.text = unit.email ?: "Không có thông tin"
            binding.tvPhone.text = unit.phone ?: "Không có thông tin"

            if (unit.fax.isNullOrEmpty()) {
                binding.layoutFax.visibility = View.GONE
            } else {
                binding.layoutFax.visibility = View.VISIBLE
                binding.tvFax.text = unit.fax
            }

            // Load logo nếu có
            if (!unit.logoURL.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(unit.logoURL)
                    .placeholder(R.drawable.ic_building)
                    .error(R.drawable.ic_building)
                    .into(binding.ivUnitLogo)
            }
        }

        staffViewModel.staffList.observe(viewLifecycleOwner) { staffList ->
            if (staffList.isEmpty()) {
                binding.rvStaff.visibility = View.GONE
                binding.tvNoStaff.visibility = View.VISIBLE
            } else {
                binding.rvStaff.visibility = View.VISIBLE
                binding.tvNoStaff.visibility = View.GONE
                staffAdapter.submitList(staffList)
            }
        }

        unitViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}