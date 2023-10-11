package com.example.bookapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bookapp.databinding.FragmentBookUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.Context

class BookUserFragment : Fragment {

    //view binding fragment_books_user.xml => FragmentBookUserBinding
    private lateinit var binding: FragmentBookUserBinding

    public companion object{
        private const val TAG = "BOOK_USER_TAG"

        //menerima data dari aktivitas memuat buku misalnya Id kategori, kategori, uid
        public fun newInstance(categoryId: String, category: String, uid: String): BookUserFragment{
            val fragment = BookUserFragment()
            //memasukkan data ke bundle intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId =""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //dapatkan argumen yang kami sampaikan dalam metode newInstance
        val args = arguments
        if (args != null) {
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Kembangkan tata letak untuk fragmen ini
        binding = FragmentBookUserBinding.inflate(LayoutInflater.from(context), container,false)
        //load pdf according to category, this fragment will have new instance to load each category
        Log.d(tag,"onCreateView: Category: $category")
        if (category == "Semua"){
            //memuat all
            loadAllBooks()
        }
        else if (category == "Populer"){
            //memuat buku yang paling banyak dilihat
            loadMostViewedDownloadedBooks("viewsCount")
        }
        else if (category == "Download terbanyak"){
            //memuat sebagian besar buku yang diunduh
            loadMostViewedDownloadedBooks("downloadsCount")
        }
        else{
            //memuat buku kategori yang dipilih
            loadCategorizedBooks()
        }

        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //filter data
                try {
                    adapterPdfUser.filter.filter(s)
                }
                catch (e: Exception){
                    Log.d(TAG,"onTextChanged: SEARCH EXCEPTION: ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        return binding.root
    }

    //load "ALL"
    private fun loadAllBooks() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear list
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    // Get data
                    val model = ds.getValue(ModelPdf::class.java)
                    // Add to list
                    model?.let { pdfArrayList.add(it) }
                }
                // Check if context is not null and pdfArrayList is not empty
                if (context != null && pdfArrayList.isNotEmpty()) {
                    // Setup adapter
                    adapterPdfUser = AdapterPdfUser(requireContext(), pdfArrayList) //java.lang.NullPointerException
                    // Set adapter to recycler view
                    binding.bookRv.adapter = adapterPdfUser
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    // => load "MostViewed & MostDownloaded"
    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        // Init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(2)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Clear list
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        // Get data
                        val model = ds.getValue(ModelPdf::class.java)
                        // Add to list
                        model?.let { pdfArrayList.add(it) }
                    }
                    // Check if context is not null and pdfArrayList is not empty
                    if (context != null && pdfArrayList.isNotEmpty()) {
                        // Setup adapter
                        adapterPdfUser = AdapterPdfUser(requireContext(), pdfArrayList) //java.lang.NullPointerException
                        // Set adapter to recycler view
                        binding.bookRv.adapter = adapterPdfUser
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }


    private fun loadCategorizedBooks() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Clear list
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        // Get data
                        val model = ds.getValue(ModelPdf::class.java)
                        // Add to list
                        model?.let { pdfArrayList.add(it) }
                    }
                    // Check if context is not null and pdfArrayList is not empty
                    if (context != null && pdfArrayList.isNotEmpty()) {
                        // Setup adapter
                        adapterPdfUser = AdapterPdfUser(requireContext(), pdfArrayList) //java.lang.NullPointerException
                        // Set adapter to recycler view
                        binding.bookRv.adapter = adapterPdfUser
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

}
