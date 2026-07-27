package com.example.myapplication


import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        window.statusBarColor = android.graphics.Color.WHITE

        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val fullName = findViewById<EditText>(R.id.full_name)
        val email = findViewById<EditText>(R.id.Enter_Email)
        val password = findViewById<EditText>(R.id.create_password)
        val confirmPassword = findViewById<EditText>(R.id.confirm_password)

        val createAccountBtn = findViewById<TextView>(R.id.create_account)

        val loginText = findViewById<TextView>(R.id.login_text)

        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        createAccountBtn.setOnClickListener {

            val nameText = fullName.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passText = password.text.toString().trim()
            val confirmPassText = confirmPassword.text.toString().trim()

            // Empty fields check
            if (nameText.isEmpty() ||
                emailText.isEmpty() ||
                passText.isEmpty() ||
                confirmPassText.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Password match check
            if (passText != confirmPassText) {

                Toast.makeText(
                    this,
                    "Passwords do not match",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }


            // Firebase Signup
            auth.createUserWithEmailAndPassword(emailText, passText)
                .addOnCompleteListener { task ->


                    if (task.isSuccessful) {

                        val userId = auth.currentUser?.uid

                        // User data map
                        val userMap = hashMapOf(
                            "fullName" to nameText,
                            "email" to emailText
                        )

                        // Save in Firestore
                        if (userId != null) {

                            db.collection("users")
                                .document(userId)
                                .set(userMap)
                                .addOnSuccessListener {

                                    Toast.makeText(
                                        this,
                                        "Account Created Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    startActivity(
                                        Intent(
                                            this,
                                            MainActivity::class.java
                                        )
                                    )

                                    finish()
                                }

                                .addOnFailureListener {

                                    Toast.makeText(
                                        this,
                                        "Database Error",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }

                    } else {

                        Toast.makeText(
                            this,
                            task.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}