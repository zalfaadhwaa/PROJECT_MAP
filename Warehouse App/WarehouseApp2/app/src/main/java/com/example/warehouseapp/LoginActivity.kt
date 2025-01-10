package com.example.warehouseapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var txtRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPreference = getSharedPreferences("app_preference", Context.MODE_PRIVATE)
        val id = sharedPreference.getString("id", "").toString()

        if (id.isNotBlank()) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnLogin = findViewById(R.id.btn_login)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        txtRegister = findViewById(R.id.text_page_register)

        txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            auth(email, password) { isValid ->
                if (!isValid) {
                    Toast.makeText(
                        applicationContext,
                        "Username atau password salah!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@auth
                }

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun auth(email: String, password: String, checkResult: (isValid: Boolean) -> Unit) {
        val db = Firebase.firestore
        db.collection("users").whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                var isValid = false

                for (document in documents) {
                    val pass = document.data["password"].toString()

                    if (!pass.equals(PasswordHelper.md5(password))) {
                        break
                    }

                    val sharedPreference = getSharedPreferences("app_preference", Context.MODE_PRIVATE)
                    val editor = sharedPreference.edit()
                    editor.putString("id", document.id)
                    editor.putString("name", document.data["name"].toString()) // Menyimpan nama
                    editor.putString("email", document.data["email"].toString())
                    editor.apply() // Menggunakan apply() untuk performa

                    isValid = true
                }

                checkResult.invoke(isValid)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal melakukan autentikasi.", Toast.LENGTH_SHORT).show()
                checkResult.invoke(false)
            }
    }
}