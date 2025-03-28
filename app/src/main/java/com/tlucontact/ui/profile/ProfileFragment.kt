package com.tlucontact.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.tlucontact.auth.LoginActivity
import com.tlucontact.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Setup UI elements and observe viewModel
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Set user information
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.tvUsername.text = currentUser.displayName ?: "Người dùng TLU"
            binding.tvEmail.text = currentUser.email ?: "sinhvien@e.tlu.edu.vn"
        } else {
            binding.tvUsername.text = "Người dùng TLU"
            binding.tvEmail.text = "sinhvien@e.tlu.edu.vn"
        }

        // Setup theme selector
        binding.layoutTheme.setOnClickListener {
            // To be implemented - Theme change functionality
            Toast.makeText(context, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        }

        // Setup logout button
        binding.layoutLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Setup About app
        binding.layoutAbout.setOnClickListener {
            Toast.makeText(context, "TLUContact - Phiên bản 1.0", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.username.observe(viewLifecycleOwner) { username ->
            // Only set if current value is default
            if (binding.tvUsername.text == "Người dùng TLU") {
                binding.tvUsername.text = username
            }
        }

        viewModel.email.observe(viewLifecycleOwner) { email ->
            // Only set if current value is default
            if (binding.tvEmail.text == "sinhvien@e.tlu.edu.vn") {
                binding.tvEmail.text = email
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng?")
            .setPositiveButton("Đăng xuất") { _, _ ->
                logoutUser()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun logoutUser() {
        // Sign out from Firebase
        auth.signOut()


        // Show success message
        Toast.makeText(context, "Đăng xuất thành công", Toast.LENGTH_SHORT).show()

        // Navigate to Login screen
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}