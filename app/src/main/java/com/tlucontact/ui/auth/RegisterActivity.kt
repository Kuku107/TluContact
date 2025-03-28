package com.tlucontact.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tlucontact.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // For now, just show a message that this functionality is coming soon
        binding.btnRegister.setOnClickListener {
            Toast.makeText(this, "Tính năng đăng ký sẽ sớm được phát triển", Toast.LENGTH_SHORT).show()
        }
    }
}