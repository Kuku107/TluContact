package com.tlucontact.ui.staff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tlucontact.R
import com.tlucontact.databinding.FragmentStaffBinding

class StaffFragment : Fragment() {

    private var _binding: FragmentStaffBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StaffViewModel by viewModels()
    private lateinit var staffAdapter: StaffAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        // Load data
        viewModel.loadStaff()
    }

    private fun setupRecyclerView() {
        staffAdapter = StaffAdapter { staffMember ->
            val bundle = Bundle().apply {
                putString("staffId", staffMember.id)
            }
            findNavController().navigate(R.id.action_staff_to_staff_detail, bundle)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = staffAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.isEmpty()) {
                        viewModel.loadStaff()
                    } else {
                        viewModel.searchStaff(it)
                    }
                }
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.staffList.observe(viewLifecycleOwner) { staffList ->
            staffAdapter.submitList(staffList)

            // Show empty view if list is empty
            binding.emptyView.visibility = if (staffList.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (staffList.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                binding.emptyView.text = errorMessage
                binding.emptyView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}