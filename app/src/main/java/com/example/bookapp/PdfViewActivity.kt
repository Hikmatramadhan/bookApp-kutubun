package com.example.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.bookapp.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityPdfViewBinding

    //TAG
    private companion object{
        const val TAG = "PDF_VIEW_TAG"
    }

    //book id
    private var bookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get id from intent
        bookId = intent.getStringExtra("bookId")!!

        loadBooksDetails()

        //handle back
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }


    }

    private fun loadBooksDetails() {
        Log.d(TAG,"loadBookDetail: Get Pdf Url from db")
        //database reference
        //steps 1
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book url
                    val pdfUrl = snapshot.child("url").value
                    Log.d(TAG,"onDataChange: PDF_URL: $pdfUrl")

                    //steps 2 load
                    loadBookFromUrl("$pdfUrl")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        Log.d(TAG,"loadBookFromUrl: Get Pdf from firebase storage using url")

        val reverence = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reverence.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.d(TAG,"loadBookFromUrl: pdf get from url")

                //load pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)//set false to scroll vertical, set true to scroll horizontal
                    .onPageChange{page, pageCount->
                        //set current and total pages in toolbar subtitle
                        val currentPage = page+1
                        binding.toolbarSubTitleTv.text = "$currentPage/$pageCount"
                        Log.d(TAG,"loadBookFromUrl: $currentPage/$pageCount")
                    }
                    .onError{t->
                        Log.d(TAG,"loadBookFromUrl: ${t.message}")
                    }
                    .onPageError{page, t->
                        Log.d(TAG,"loadBookFromUrl: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e->
                Log.d(TAG,"loadBookFromUrl: Failed to get pdf due to ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
    }
}