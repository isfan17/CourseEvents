package com.example.courseevents.ui.auth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.example.courseevents.databinding.ActivityRegisterBinding
import com.example.courseevents.ui.main.MainActivity

class RegisterActivity : AppCompatActivity() {
    private var _activityRegisterBinding: ActivityRegisterBinding? = null
    private val binding get() = _activityRegisterBinding

    private var progressDialog: ProgressDialog? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _activityRegisterBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.hide();

        auth = Firebase.auth
        progressDialog = ProgressDialog(this@RegisterActivity)
        progressDialog?.setTitle("Loading")
        progressDialog?.setMessage("Please Wait ...")
        progressDialog?.setCancelable(false)

        binding?.btnLogin?.setOnClickListener {
            finish()
        }

        binding?.btnRegister?.setOnClickListener {
            val name = binding?.edtName?.text.toString().trim()
            val email = binding?.edtEmail?.text.toString().trim()
            val password = binding?.edtPassword?.text.toString().trim()
            val confirmPassword = binding?.edtConfirmPassword?.text.toString().trim()
            when
            {
                name.isEmpty() -> {
                    binding?.edtName?.error = "Field can not be blank"
                }
                email.isEmpty() -> {
                    binding?.edtEmail?.error = "Field can not be blank"
                }
                password.isEmpty() -> {
                    binding?.edtPassword?.error = "Field can not be blank"
                }
                confirmPassword.isEmpty() -> {
                    binding?.edtConfirmPassword?.error = "Field can not be blank"
                }
                else -> {
                    if (password != confirmPassword)
                    {
                        binding?.edtConfirmPassword?.error = "Please input the same password!"
                    }
                    else
                    {
                        register(name, email, password)
                    }
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            reload();
        }
    }

    private fun register(name: String, email: String, password: String) {
        progressDialog?.show()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null)
                {
                    val user = task.result.user
                    if (user != null)
                    {
                        val profileUpdates = userProfileChangeRequest {
                            displayName = name
                        }
                        user.updateProfile(profileUpdates).addOnCompleteListener {
                            reload()
                        }
                        progressDialog?.hide()
                    }
                    else
                    {
                        Toast.makeText(applicationContext, "Register Failed", Toast.LENGTH_SHORT).show()
                        progressDialog?.hide()
                    }
                }
                else
                {
                    Toast.makeText(applicationContext, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                    progressDialog?.hide()
                }
            }
    }

    private fun reload() {
        startActivity(Intent(applicationContext, MainActivity::class.java))
    }
}
