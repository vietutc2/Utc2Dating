package com.example.utc2datingapp

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.utc2datingapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.navigationView.setNavigationItemSelectedListener(this)

        val navController = findNavController(R.id.fragment)
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)

        // Yêu cầu quyền truy cập vị trí sau khi đăng nhập và tạo người dùng thành công
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        Dexter.withActivity(this)
            .withPermission(ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    // Quyền truy cập vị trí đã được cấp, tiến hành hiển thị card và sử dụng vị trí
                    // Gọi hàm hiển thị card và sử dụng vị trí ở đây
                    showToast("Location permission granted")

                    // Bật vị trí nếu chưa được bật
                    enableLocation()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    // Người dùng từ chối cấp quyền truy cập vị trí
                    // Xử lý theo yêu cầu của bạn, ví dụ: thông báo cho người dùng về việc cần quyền truy cập vị trí để hiển thị card
                    showToast("Location permission denied")
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    // Hiển thị lời giải thích cho người dùng về việc cần quyền truy cập vị trí
                    // Gọi hàm token.continuePermissionRequest() để tiếp tục yêu cầu quyền truy cập vị trí sau khi người dùng đã hiểu
                    // Ví dụ: dialog.show() và token.continuePermissionRequest()
                    showToast("Location permission rationale should be shown")
                    token?.continuePermissionRequest()
                }
            })
            .check()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.rateUs -> {
                showToast("Rate Us")
            }
            R.id.filter -> {
                showToast("Filter")
            }
            R.id.shareApp -> {
                showToast("Share App")
            }
            R.id.termsCondition -> {
                showToast("Terms & Conditions")
            }
            R.id.privacyPolity -> {
                showToast("Privacy Policy")
            }
            R.id.developer -> {
                showToast("Developer")
            }
        }
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else
            super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else super.onBackPressed()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun enableLocation() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Vị trí chưa được bật, mở cài đặt để bật vị trí
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Vị trí đã được bật
            showToast("Location enabled")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Vị trí đã được bật từ cài đặt
                showToast("Location enabled")
            } else {
                // Người dùng không bật vị trí từ cài đặt
                showToast("Location disabled")
            }
        }
    }
}
