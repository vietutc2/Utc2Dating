package com.example.utc2datingapp.auth

import android.app.DatePickerDialog
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.example.utc2datingapp.MainActivity
import com.example.utc2datingapp.R
import com.example.utc2datingapp.databinding.ActivityRegisterBinding
import com.example.utc2datingapp.model.UserModel
import com.example.utc2datingapp.utils.Config
import com.example.utc2datingapp.utils.Config.hideDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private var imageUri: Uri? = null

    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            binding.userImage.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.userImage.setOnClickListener {
            selectImage.launch("image/*")
        }

        binding.ageSelectionEditText.setOnClickListener {
            showDatePickerDialog()
        }

        binding.saveData.setOnClickListener {
            validateData()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                binding.ageSelectionEditText.setText(formattedDate)
            }, year, month, day)

        datePickerDialog.datePicker.calendarViewShown = false // Ẩn chế độ lịch
        datePickerDialog.show()
    }

    private fun validateData() {
        if (binding.userName.text.toString().isEmpty()
            || binding.userEmail.text.toString().isEmpty()
            || binding.userCity.text.toString().isEmpty()
            || binding.userEmail.text.toString().isEmpty()
            || binding.userJob.text.toString().isEmpty()
            || binding.userstatus.text.toString().isEmpty()
            || imageUri == null
            || binding.radiogrp.checkedRadioButtonId == -1
            || binding.ageSelectionEditText.text.toString().isEmpty()
        ) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
        } else if (!binding.termsCondition.isChecked) {
            Toast.makeText(this, "Please accept terms and conditions", Toast.LENGTH_SHORT).show()
        } else {
            uploadImage()
        }
    }

    private fun uploadImage() {
        Config.showDialog(this)

        val storageRef = FirebaseStorage.getInstance().getReference("profile")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).child("profile.jpg")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener { _ ->
                storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val phoneNumber = currentUser?.phoneNumber
                    val gender: String? = when (binding.radiogrp.checkedRadioButtonId) {
                        R.id.radiomale -> "Male"
                        R.id.radiofemale -> "Female"
                        else -> null
                    }

                    val selectedDate = binding.ageSelectionEditText.text.toString()

                    val age = calculateAge(selectedDate)

                    val data = UserModel(
                        name = binding.userName.text.toString(),
                        image = imageUrl.toString(),
                        email = binding.userEmail.text.toString(),
                        city = binding.userCity.text.toString(),
                        job = binding.userJob.text.toString(),
                        status = binding.userstatus.text.toString(),
                        number = phoneNumber,
                        gender = gender,
                        age = age,
                        birthday = selectedDate // Thêm trường birthday vào data object
                    )

                    storeData(data)
                }.addOnFailureListener {
                    hideDialog()
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateAge(selectedDate: String): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        val dateOfBirth = dateFormat.parse(selectedDate)

        val diff = currentDate.time - dateOfBirth.time
        val age = diff / (24 * 60 * 60 * 1000 * 365.25)

        return age.toInt().toString()
    }

    private fun storeData(data: UserModel) {
        FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser!!.phoneNumber!!)
            .setValue(data)
            .addOnCompleteListener { task ->
                hideDialog()

                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    Toast.makeText(this, "User register successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
}
