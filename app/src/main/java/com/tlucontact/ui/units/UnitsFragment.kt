package com.tlucontact.ui.units

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tlucontact.R
import com.tlucontact.databinding.FragmentUnitsBinding

class UnitsFragment : Fragment() {

    private var _binding: FragmentUnitsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UnitsViewModel by viewModels()
    private lateinit var adapter: UnitsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupFilterChips()
        observeViewModel()

        viewModel.loadUnits()
    }

    private fun setupRecyclerView() {
        adapter = UnitsAdapter { unit ->
            // Use a simpler navigation method until SafeArgs is properly set up
            val bundle = Bundle().apply {
                putString("unitId", unit.id)
            }
            findNavController().navigate(R.id.action_units_to_unit_detail, bundle)

            // Once SafeArgs is properly set up, you can use:
            // val action = UnitsFragmentDirections.actionUnitsToUnitDetail(unit.id)
            // findNavController().navigate(action)
        }

        binding.rvUnits.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchUnits(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.loadUnits()
                }
                return true
            }
        })
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener {
            viewModel.loadUnits()
            updateChipSelection(binding.chipAll)
        }

        binding.chipFaculty.setOnClickListener {
            viewModel.loadUnits("Khoa")
            updateChipSelection(binding.chipFaculty)
        }

        binding.chipDepartment.setOnClickListener {
            viewModel.loadUnits("Phòng")
            updateChipSelection(binding.chipDepartment)
        }

        binding.chipCenter.setOnClickListener {
            viewModel.loadUnits("Trung tâm")
            updateChipSelection(binding.chipCenter)
        }
    }

    private fun updateChipSelection(selectedChip: View) {
        binding.chipAll.isChecked = selectedChip == binding.chipAll
        binding.chipFaculty.isChecked = selectedChip == binding.chipFaculty
        binding.chipDepartment.isChecked = selectedChip == binding.chipDepartment
        binding.chipCenter.isChecked = selectedChip == binding.chipCenter
    }

    private fun observeViewModel() {
        viewModel.units.observe(viewLifecycleOwner) { units ->
            adapter.submitList(units)
            binding.tvNoData.visibility = if (units.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}