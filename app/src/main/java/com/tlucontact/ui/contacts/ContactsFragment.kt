package com.tlucontact.ui.contacts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.tlucontact.R
import com.tlucontact.databinding.FragmentContactsBinding

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by viewModels()
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Kiểm tra trạng thái đăng nhập
        if (auth.currentUser == null) {
            Toast.makeText(context, "Vui lòng đăng nhập để xem danh bạ", Toast.LENGTH_LONG).show()
            return
        }

        // Setup UI
        setupRecyclerView()
        setupSearchView()
        setupFab()
        observeViewModel()

        // Hiển thị trạng thái loading
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE

        // Load danh sách liên hệ
        viewModel.loadContacts()
    }

    override fun onResume() {
        super.onResume()
        // Tải lại dữ liệu mỗi khi fragment được hiển thị lại
        // để cập nhật mọi thay đổi từ máy chủ hoặc các fragment khác
        if (auth.currentUser != null) {
            viewModel.loadContacts()
        }
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter { contact ->
            // Xử lý sự kiện click vào liên hệ
            val bundle = Bundle().apply {
                putString("contactId", contact.id)
            }
            findNavController().navigate(R.id.action_contacts_to_contact_detail, bundle)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contactAdapter
            setHasFixedSize(true) // Tối ưu hóa hiệu suất
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
                        viewModel.loadContacts()
                    } else {
                        viewModel.searchContacts(it)
                    }
                }
                return true
            }
        })

        // Clear focus để không hiển thị bàn phím khi vào fragment
        binding.searchView.clearFocus()
    }

    private fun setupFab() {
        binding.fabAddContact.setOnClickListener {
            // Chuyển đến màn hình thêm liên hệ mới
            findNavController().navigate(R.id.action_contacts_to_add_contact)
        }
    }

    private fun observeViewModel() {
        viewModel.contactList.observe(viewLifecycleOwner) { contactList ->
            // Cập nhật adapter với dữ liệu mới
            contactAdapter.submitList(contactList)

            // Log để debug
            Log.d("ContactsFragment", "Received ${contactList.size} contacts from ViewModel")

            // Xử lý hiển thị trạng thái trống nếu không có liên hệ
            if (contactList.isEmpty()) {
                binding.emptyView.text = "Chưa có liên hệ nào"
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        // Quan sát trạng thái loading
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

            // Ẩn recyclerview khi đang loading để tránh hiển thị dữ liệu cũ
            if (isLoading) {
                binding.recyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                // Hiển thị thông báo lỗi chỉ khi có lỗi thực sự
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()

                // Trạng thái trống sẽ được xử lý bởi observer cho contactList
                // Không làm ẩn recyclerView ở đây nếu có dữ liệu
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}