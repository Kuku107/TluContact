package com.tlucontact.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.tlucontact.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Setup back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Xử lý đăng ký
        binding.btnRegister.setOnClickListener {
            if (validateForm()) {
                registerUser()
            }
        }
    }

    private fun validateForm(): Boolean {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        var isValid = true

        // Kiểm tra tên
        if (name.isEmpty()) {
            binding.tilName.error = "Vui lòng nhập họ và tên"
            isValid = false
        } else {
            binding.tilName.error = null
        }

        // Kiểm tra email
        if (email.isEmpty()) {
            binding.tilEmail.error = "Vui lòng nhập email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Email không hợp lệ"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        // Kiểm tra mật khẩu
        if (password.isEmpty()) {
            binding.tilPassword.error = "Vui lòng nhập mật khẩu"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        // Kiểm tra xác nhận mật khẩu
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Vui lòng xác nhận mật khẩu"
            isValid = false
        } else if (confirmPassword != password) {
            binding.tilConfirmPassword.error = "Mật khẩu không khớp"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return isValid
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Hiển thị trạng thái loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        // Tạo tài khoản với Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Đăng ký thành công
                    val currentUser = auth.currentUser

                    // Cập nhật tên hiển thị
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    currentUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                // Lưu thêm thông tin vào Firestore
                                currentUser.uid.let { userId ->
                                    val userMap = hashMapOf(
                                        "name" to name,
                                        "email" to email,
                                        "createdAt" to System.currentTimeMillis()
                                    )

                                    db.collection("users").document(userId)
                                        .set(userMap)
                                        .addOnSuccessListener {
                                            // Đăng xuất người dùng (vì họ được tự động đăng nhập sau khi đăng ký)
                                            auth.signOut()

                                            // Hiển thị thông báo thành công
                                            Toast.makeText(
                                                this,
                                                "Đăng ký thành công! Vui lòng đăng nhập",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Quay lại màn hình đăng nhập
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            // Đã tạo account nhưng không lưu được thông tin user
                                            // Vẫn đăng xuất và quay lại màn hình đăng nhập
                                            auth.signOut()

                                            Toast.makeText(
                                                this,
                                                "Đăng ký tài khoản thành công! Vui lòng đăng nhập",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            // Quay lại màn hình đăng nhập
                                            finish()
                                        }
                                }
                            } else {
                                // Lỗi cập nhật profile
                                // Đăng xuất và quay lại màn hình đăng nhập
                                auth.signOut()

                                Toast.makeText(
                                    this,
                                    "Đăng ký thành công! Vui lòng đăng nhập",
                                    Toast.LENGTH_LONG
                                ).show()

                                // Quay lại màn hình đăng nhập
                                finish()
                            }

                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                        }
                } else {
                    // Đăng ký thất bại
                    val errorMessage = when {
                        task.exception?.message?.contains("email address is already") == true ->
                            "Email này đã được sử dụng"
                        task.exception?.message?.contains("network") == true ->
                            "Lỗi kết nối mạng"
                        else -> "Đăng ký thất bại: ${task.exception?.message}"
                    }

                    Toast.makeText(
                        baseContext,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()

                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                }
            }
    }
}