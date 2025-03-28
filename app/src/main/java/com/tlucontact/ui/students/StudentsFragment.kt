package com.tlucontact.ui.students

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
import com.tlucontact.databinding.FragmentStudentsBinding

class StudentsFragment : Fragment() {

    private var _binding: FragmentStudentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentViewModel by viewModels()
    private lateinit var studentAdapter: StudentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        // Load data
        viewModel.loadStudents()
    }

    private fun setupRecyclerView() {
        studentAdapter = StudentAdapter { student ->
            val bundle = Bundle().apply {
                putString("studentId", student.id)
            }
            findNavController().navigate(R.id.action_students_to_student_detail, bundle)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = studentAdapter
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
                        viewModel.loadStudents()
                    } else {
                        viewModel.searchStudents(it)
                    }
                }
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.studentList.observe(viewLifecycleOwner) { studentList ->
            studentAdapter.submitList(studentList)

            // Show empty view if list is empty
            binding.emptyView.visibility = if (studentList.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (studentList.isEmpty()) View.GONE else View.VISIBLE
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