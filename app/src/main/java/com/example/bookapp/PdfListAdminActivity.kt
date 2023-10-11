package com.example.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.bookapp.databinding.ActivityPdfListAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityPdfListAdminBinding

    private companion object{
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    //category id title
    private var categoryId = ""
    private var category = ""

    //daftar array untuk menyimpan buku
    private lateinit var pdfArrayList:ArrayList<ModelPdf>

    //adapter
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //dapatkan dari intent, dari adaptor
        val intent = intent
        //panggil deklarasi private var categoryId = "" dan private var category = ""."categoryId","category" harus sama dengan data firebase
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

            //atur kategori pdf
            binding.subTitleTv.text = category

            //memuat buku pdf
            loadPdfList()

            //search
            binding.searchEt.addTextChangedListener(object: TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //filter data
                    try {
                        adapterPdfAdmin.filter.filter(s)
                    }
                    catch (e: Exception){
                        Log.d(TAG,"onTextChanged: ${e.message}")
                    }
                }

                override fun afterTextChanged(p0: Editable?) {

                }
            })
        //handle click, go back
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

    }

    private fun loadPdfList() {
        //init array list
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list before start adding data into it
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)
                        //Tambahkan ke list
                        if (model != null){
                            pdfArrayList.add(model)
                            Log.d(TAG,"onDataChange ${model.title} ${model.categoryId}")
                        }
                    }
                    // Inisialisasi adapterPdfAdmin sebelum penggunaan
                    adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity, pdfArrayList)
                    binding.booksTv.adapter = adapterPdfAdmin
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}
