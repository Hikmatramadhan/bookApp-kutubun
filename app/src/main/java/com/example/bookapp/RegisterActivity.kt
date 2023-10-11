@file:Suppress("DEPRECATION")

package com.example.bookapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityRegisterBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progresDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog, akan ditampilkan saat membuat akun, saya akan mendaftarkan pengguna
        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Loading...")
        progresDialog.setCanceledOnTouchOutside(false)

        //menangani klik tombol kembali, pergi ke layar sebelumnya
        binding.backBtn.setOnClickListener {
            onBackPressed()//pergi ke layar sebelumnya
        }
        //menangani klik, mulai mendaftar
        binding.registerBtn.setOnClickListener {
            /* steps
            1.input data
            2.validate data
            3.create account - firebase auth
            4.save user info - firebase realtime database
             */
            validateData()
        }
    }

    private var name =""
    private var email =""
    private var password =""

    private fun validateData() {
        //1.input data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cpasswordEt.text.toString().trim()

        //2.validation data
        if (name.isEmpty()){
            //empty name...
            Toast.makeText(this, "Masukkan Nama Anda",Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //pola email tidak valid
            Toast.makeText(this, "Email Tidak Valid...",Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            //empty password
            Toast.makeText(this, "Masukan Password...",Toast.LENGTH_SHORT).show()
        }
        else if (cPassword.isEmpty()) {
            //empty password
            Toast.makeText(this, "Konfirmasi Password...", Toast.LENGTH_SHORT).show()
        }
        else if (password != cPassword){
            Toast.makeText(this, "Password Salah!", Toast.LENGTH_SHORT).show()
        }
        else{
            createUserAcount()
        }
    }

    private fun createUserAcount() {
        //3.create account firebase

        //show progress
        progresDialog.setMessage("Membuat Akun...")

        //create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //account created, now add user ifo in db
                updateUserInfo()

            }
            .addOnFailureListener { e->
                //failed creating account
                progresDialog.dismiss()
                Toast.makeText(this, "Gagal membuat akun karena ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        //4.save user info - firebase realtime database

        progresDialog.setMessage("Menyimpan informasi pengguna...")

        //timestamp
        val timestamp = System.currentTimeMillis()

        //get current user uid, since user is registered so we can get it now
        val uid = firebaseAuth.uid

        //setup data to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" //add empty, akan dilakukan dalam pengeditan profil
        hashMap["userType"] = "user" //nilai yang mungkin adalah pengguna atau admin, akan mengubah nilai menjadi admin secara manual di firebase dataBase
        hashMap["timestamp"] = timestamp

        //atur data ke database
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //info pengguna disimpan, buka dasbor pengguna
                progresDialog.dismiss()
                Toast.makeText(this, "akun berhasil dibuat...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener{e->
                //failed adding data to db
                progresDialog.dismiss()
                Toast.makeText(this, "Gagal menyimpan info pengguna karena ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }
}