package com.example.utc2datingapp.auth


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.utc2datingapp.MainActivity
import com.example.utc2datingapp.R
import com.example.utc2datingapp.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    val auth = FirebaseAuth.getInstance()
    private var verificationId:String? =null

    private lateinit var dialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = AlertDialog.Builder(this).setView(R.layout.loading_layout)
            .setCancelable(false)
            .create()



        binding.btnSendotp.setOnClickListener {
            if (binding.usernumber.text!!.isEmpty()) {
                binding.usernumber.error = "Please enter your number"

            } else {
                sendOtp(binding.usernumber.text.toString())
            }

        }

        binding.btnVerfyOTP.setOnClickListener {
            if (binding.userOTP.text!!.isEmpty()) {
                binding.userOTP.error = "Please enter OTP"
            } else {
                verfyOtp(binding.userOTP.text.toString())
            }

        }

    }

    private fun verfyOtp(otp: String) {
        dialog.show()
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        signInWithPhoneAuthCredential(credential)


    }

    private fun sendOtp(number: String) {
        dialog.show()
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@LoginActivity.verificationId= verificationId
                dialog.dismiss()
                //                binding.btnSendotp.showNormalButton()

                binding.numberlayout.visibility= View.GONE
                binding.otplayout.visibility= View.VISIBLE

            }
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+84$number") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                //                  binding.btnSendotp.showNormalButton()
                if (task.isSuccessful) {

                    checkUserExist(binding.usernumber.text.toString())
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
                } else {
                    dialog.dismiss()
                    Toast.makeText(this,task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserExist(number: String) {
        // Kiểm tra nếu số điện thoại không bắt đầu bằng "0", thêm dấu "+" và định dạng "+84" vào trước số điện thoại
        val processedNumber = if (!number.startsWith("0")) {
            "+84$number"
        } else {
            "+${number.substring(1)}"
        }

        FirebaseDatabase.getInstance().getReference("users").child(processedNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    dialog.dismiss()
                    Toast.makeText(this@LoginActivity, p0.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    Log.d("FirebaseData", p0.value.toString())

                    if (p0.exists()) {
                        dialog.dismiss()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                    }
                }
            })
    }



}