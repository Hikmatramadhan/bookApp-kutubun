package com.example.bookapp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.ActivityPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityPdfAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //category array list
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private var pdfUri: Uri? = null

    private val TAG = "PDF_ADD_TAG"

    private var title = ""
    private var description = ""
    private var category = ""
    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        //setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Proses Berlangsung")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, show category pick dialog
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        //handle click, pick pdf intent
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        //handle click, start uploadding pdf/book
        binding.submitBtn.setOnClickListener {
            validateData()
            // steps 1 validate data
            // steps 2 uploading pdf to firebase
            // steps 3
            // steps 4
        }
    }

    private fun validateData() {
        Log.d(TAG, "validateData: Validating data")

        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        //validate data
        if (title.isEmpty()) {
            Toast.makeText(this, "Masukkan Judul...", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Masukkan Deskripsi...", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Pilih Kategori...", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pilih PDF...", Toast.LENGTH_SHORT).show()
        } else {
            //data validate begin upload
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        //steps 2 up;oad pdf to firebase storage
        Log.d(TAG, "uploadPdfToStorage: Uploading to storage...")

        //show progress dialod
        progressDialog.setMessage("Mengunggah PDF...")
        progressDialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()


        //path of pdf in firebase storage
        val filePathName = "Books/$timestamp"
        //storage reverence
        val storageReference = FirebaseStorage.getInstance().getReference(filePathName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: PDF terunggah, mendapatkan URL...")

                //steps 3 get url of uploaded pdf
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadedPdfInfoToDb(uploadedPdfUrl, timestamp)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "UploadPdfStorage: Gagal mengunggah karena ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal mengunggah karena ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadedPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "uploadPdfInfoToDb: Mengunggah ke database...")
        progressDialog.setMessage("Mengunggah info PDF...")

        //uid of current user
        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        //db reference db > Books > bookId > (book info)
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: Terunggah ke database")
                progressDialog.dismiss()
                Toast.makeText(this, "Terunggah...", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "UploadPdfInfoToDb: Gagal mengunggah karena ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal mengunggah karena ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Memuat kategori PDF")
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error loading categories: ${error.message}")
            }
        })
    }

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Menampilkan dialog pemilihan kategori PDF")

        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Kategori")
            .setItems(categoriesArray) { dialog, which ->
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: Memulai intent pemilihan PDF")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    private val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF Dipilih")
                pdfUri = result.data?.data
            } else {
                Log.d(TAG, "Pemilihan PDF dibatalkan")
                Toast.makeText(this, "Dibatalkan", Toast.LENGTH_SHORT).show()
            }
        }
    )
}
