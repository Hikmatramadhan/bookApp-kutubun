package com.example.bookapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.example.bookapp.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class CategoryAddActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityCategoryAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Harap tunggu...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, go back
        binding.submitBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, begin upload category
        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private var category = ""
    private fun validateData() {

        //get data
        category = binding.categoryEt.text.toString().trim()

        //validate data
        if (category.isEmpty()){
            Toast.makeText(this, "Masukan Kategori...", Toast.LENGTH_SHORT).show()
        }
        else{
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        //show progress
        progressDialog.show()

        //get timestamp
        val timestamp = System.currentTimeMillis()

        //setup data to add in firebase db
        val hashMap = HashMap<String, Any>()    //param kedua adalah Any; karena nilai de bisa naik jenis apa pun
        hashMap["id"] = "$timestamp"    //masukkan tanda kutip string karena stempel waktunya ganda, kita perlu string untuk id
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        //add to  firebase db: database root > categories > categoryId > category info
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                //added succesfuly
                progressDialog.dismiss()
                Toast.makeText(this, "Berhasil di tambahkan...", Toast.LENGTH_SHORT).show()


            }
            .addOnFailureListener{ e->
                //faild to add
                progressDialog.dismiss()
                Toast.makeText(this, "Faild to add due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}