package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.utils.ProfileImageHelper
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImg: ImageView

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val success = ProfileImageHelper.saveProfileImage(this, it)
            if (success) {
                ProfileImageHelper.loadProfileImage(this, profileImg)
                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to save profile picture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        window.statusBarColor = Color.WHITE
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val arrow = findViewById<View>(R.id.profile_arrow)
        profileImg = findViewById(R.id.profile_img)
        val profileNameView = findViewById<TextView>(R.id.profile_name)
        val editProfileBox = findViewById<View>(R.id.box2)

        // Populate User Name from Firebase Auth
        val currentUser = FirebaseAuth.getInstance().currentUser
        val displayName = currentUser?.displayName ?: currentUser?.email?.substringBefore('@') ?: "Your Name"
        profileNameView.text = displayName

        arrow.setOnClickListener {
            finish()
        }

        // Click to change profile image
        profileImg.setOnClickListener {
            openGallery()
        }

        editProfileBox?.setOnClickListener {
            openGallery()
        }

        ProfileImageHelper.loadProfileImage(this, profileImg)
    }

    override fun onResume() {
        super.onResume()
        if (::profileImg.isInitialized) {
            ProfileImageHelper.loadProfileImage(this, profileImg)
        }
    }

    private fun openGallery() {
        selectImageLauncher.launch("image/*")
    }
}
