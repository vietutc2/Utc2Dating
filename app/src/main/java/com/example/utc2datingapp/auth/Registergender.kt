package com.example.utc2datingapp.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.utc2datingapp.MainActivity
import com.example.utc2datingapp.R
import com.example.utc2datingapp.databinding.ActivityRegistergenderBinding
import com.example.utc2datingapp.model.UserModel
import com.example.utc2datingapp.utils.Config.hideDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class Registergender : AppCompatActivity() {
    private lateinit var binding: ActivityRegistergenderBinding
    private var gender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistergenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.maleSelectionButton.setOnClickListener {
            gender = "MALE"
        }

        binding.femaleSelectionButton.setOnClickListener {
            gender = "FEMALE"
        }

        binding.preferenceContinueButton.setOnClickListener {
            saveDataToFirebase()
        }
    }

    private fun saveDataToFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val phoneNumber = currentUser?.phoneNumber
        val data = UserModel(

            gender = gender // Gán giá trị gender từ biến gender đã chọn

            // Các trường dữ liệu khác trong UserModel
        )

        FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser!!.phoneNumber!!)
            .setValue(data)
            .addOnCompleteListener {
                hideDialog()

                if (it.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    Toast.makeText(this, "User register successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, it.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
}