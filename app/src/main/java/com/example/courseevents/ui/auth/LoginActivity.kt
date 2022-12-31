package com.example.courseevents.ui.auth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.courseevents.databinding.ActivityLoginBinding
import com.example.courseevents.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private var _activityLoginBinding: ActivityLoginBinding? = null
    private val binding get() = _activityLoginBinding

    private var progressDialog: ProgressDialog? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _activityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.hide();

        auth = Firebase.auth
        progressDialog = ProgressDialog(this@LoginActivity)
        progressDialog?.setTitle("Loading")
        progressDialog?.setMessage("Please Wait ...")
        progressDialog?.setCancelable(false)

        binding?.btnRegister?.setOnClickListener {
            startActivity(Intent(applicationContext, RegisterActivity::class.java))
        }

        binding?.btnLogin?.setOnClickListener {
            val email = binding?.edtEmail?.text.toString().trim()
            val password = binding?.edtPassword?.text.toString().trim()
            when
            {
                email.isEmpty() -> {
                    binding?.edtEmail?.error = "Field can not be blank"
                }
                password.isEmpty() -> {
                    binding?.edtPassword?.error = "Field can not be blank"
                }
                else -> {
                    login(email, password)
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

    private fun login(email: String, password: String) {
        progressDialog?.show()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null)
            {
                if (task.result.user != null)
                {
                    reload()
                    progressDialog?.hide()
                }
                else
                {
                    Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                    progressDialog?.hide()
                }
            }
            else
            {
                Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                progressDialog?.hide()
            }
        }
    }

    private fun reload() {
        startActivity(Intent(applicationContext, MainActivity::class.java))
    }
}
