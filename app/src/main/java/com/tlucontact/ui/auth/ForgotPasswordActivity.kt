package com.tlucontact.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.tlucontact.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Setup back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Xử lý yêu cầu đặt lại mật khẩu
        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resetPassword(email)
        }
    }

    private fun resetPassword(email: String) {
        binding.progressBar.visibility = View.VISIBLE

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Hướng dẫn đặt lại mật khẩu đã được gửi đến email của bạn",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Lỗi: ${task.exception?.message ?: "Không thể đặt lại mật khẩu"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}