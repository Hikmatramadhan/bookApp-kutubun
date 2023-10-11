@file:Suppress("DEPRECATION")

package com.example.bookapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding:ActivityLoginBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progres dialog
    private lateinit var progresDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progres dialog, akan ditampilkan saat pengguna login
        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Loading...")
        progresDialog.setCanceledOnTouchOutside(false)

        //tangani klik, tidak punya akun, buka layar pendaftaran
        binding.noAccountTv.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //click back
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        //tangani klik, mulai login
        binding.loginBtn.setOnClickListener {

            /*
            LANGKAH

            1.masukan data
            2.memvalidasi data
            3.login - autentikasi pangkalan api
            4.periksa jenis pengguna - firebase auth
                * jika pengguna - pindah ke dasbor pengguna
                * jika admin - pindah ke dashboard admin
            */

            validateData()
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        //1.input data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        //2.memvalidasi data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Format email salah...", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(this, "Atur Password...", Toast.LENGTH_SHORT).show()
        }
        else{
            loginUser()
        }
    }

    private fun loginUser() {
        //3.login - firebase auth

        //show progres
        progresDialog.setMessage("Login...")
        progresDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //login success
                checkUser()
            }
            .addOnFailureListener{e->
                //failed login
                progresDialog.dismiss()
                Toast.makeText(this, "Gagal masuk karena ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        /*
        4.periksa jenis pengguna - firebase auth
            * jika pengguna - pindah ke dasbor pengguna
            * jika admin - pindah ke dashboard admin
         */
        progresDialog.setMessage("Memeriksa Pengguna...")
        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object  : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    progresDialog.dismiss()
                    //get user type e.g. user or admin
                    val  userType = snapshot.child("userType").value
                    if (userType == "user"){
                        //it simple user, buka dasbor pengguna
                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                        finish()
                    }
                    else if (userType == "admin"){
                        //its admin, open admin dashboard
                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                        finish()
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}