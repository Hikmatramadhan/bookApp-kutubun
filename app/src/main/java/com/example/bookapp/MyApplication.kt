package com.example.bookapp

import android.app.Application
import android.app.ProgressDialog
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.content.Context
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {

        // Membuat metode statis untuk mengonversi stempel waktu (sebagai string) ke format tanggal yang tepat, sehingga kita dapat menggunakannya di mana saja dalam proyek, tidak perlu menulis ulang lagi
        fun formatTimestamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            // Format dd/MM/yyyy
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        //mendapatkan ukuran pdf
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) { val TAG = "PDF_SIZE_TAG"

            //Dengan menggunakan url kita bisa mendapatkan file dan metadatanya dari penyimpanan firebase
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                    .addOnSuccessListener { storageMetaData ->
                        Log.d(TAG, "loadPdfSize: get metadata")
                        val bytes = storageMetaData.sizeBytes.toDouble()
                        Log.d(TAG, "loadFdfSize: size Bytes $bytes")

                        //convert bytes to KB/MB
                        val kb = bytes / 1024
                        val mb = kb / 1024
                        if (mb >= 1) {
                            sizeTv.text = "${String.format("%.2f", mb)} MB"
                        } else if (kb >= 1) {
                            sizeTv.text = "${String.format("%.2f", kb)} kb"
                        } else {
                            sizeTv.text = "${String.format("%.2f", bytes)} bytes"
                        }
                    }
                    .addOnFailureListener { e->
                        //failed to get meta data
                        Log.d(TAG, "loadPdfSize: Failed to get metadata due to${e.message}")
                    }
            }

            /*daripada membuat fungsi baru memuat pdfCount() untuk hanya memuat jumlah halaman jika akan lebih baik menggunakan fungsi yang sama yang sudah ada untuk melakukan itu
            * yaitu memuatPdfFromUrlSinglePage
            * kami akan menambahkan parameter lain bertipe TextView misalnya halamanTv
            * Setiap kali kita memanggil fungsi itu
            * 1) jika kami memerlukan nomor halaman, kami akan melewati halamanTv (TextView)
            * 2) jika kami tidak memerlukan nomor halaman, kami akan memberikan null
            * Tambahkan fungsi jika parameter pageTv (textView) bukan nol, atur jumlah nomor halaman*/

            fun loadPdfFromUrlSinglePage(
                pdfUrl: String,
                pdfTitle: String,
                pdfView: PDFView,
                progressBar: ProgressBar,
                pagesTv: TextView?
            ) {
                val TAG = "PDF_THUMBNAIL_TAG"

                //Using url we can get file and its metadata from firebase storage
                val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
                ref.getBytes(Constants.MAX_BYTES_PDF)
                    .addOnSuccessListener { bytes ->

                        Log.d(TAG, "loadFdfSize: size Bytes $bytes")

                        //set to PdfView
                        pdfView.fromBytes(bytes)
                            .pages(0)   //show first page only
                            .spacing(0)
                            .swipeHorizontal(false)
                            .enableSwipe(false)
                            .onError { t ->
                                progressBar.visibility = View.INVISIBLE
                                Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                            }
                            .onPageError { page, t ->
                                progressBar.visibility = View.INVISIBLE
                                Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                            }
                            .onLoad { nbPages ->
                                Log.d(TAG, "loadPdfFromUrlSinglePage: pages: $nbPages")
                                //pdfLoaded, kita dapat mengatur jumlah halaman, thumbnail pdf
                                progressBar.visibility = View.INVISIBLE

                                //jika parameter pageTv bukan nol, lalu tetapkan nomor halaman
                                if (pagesTv != null) {
                                    pagesTv.text = "$nbPages"
                                }
                            }
                            .load()
                    }
                    .addOnFailureListener { e ->
                        //failed to get meta data
                        Log.d(TAG, "loadPdfSize: Failed to get metadata due to${e.message}")
                    }
            }

        fun loadCategory(categoryId: String, categoryTv: TextView) {
            //memuat kategori menggunakan id kategori dari firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        val category = "${snapshot.child("category").value}"

                        //set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }

            fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String){

                val TAG = "DELETE_BOOK_TAG"

                Log.d(TAG,"deleteBook: Deleting...")

                //progress dialog
                val progressDialog = ProgressDialog(context)
                progressDialog.setTitle("Please wait")
                progressDialog.setMessage("Deleting $bookTitle...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                Log.d(TAG,"deleteBook: Deleting from storage...")

                val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
                storageReference.delete()
                    .addOnSuccessListener {
                        Log.d(TAG,"deleteBook: Delete from storage")
                        Log.d(TAG,"deleteBook: Delete from db now...")

                        val ref = FirebaseDatabase.getInstance().getReference("Books")
                        ref.child(bookId)
                            .removeValue()
                            .addOnSuccessListener {
                                progressDialog.dismiss()
                                Toast.makeText(context,"Successfully deleted",Toast.LENGTH_SHORT).show()
                                Log.d(TAG,"deleteBook: Deleted from db too...")

                            }
                            .addOnFailureListener {e->
                                progressDialog.dismiss()
                                Log.d(TAG,"deleteBook: Failed to delete from db due to ${e.message}")
                                Toast.makeText(context,"Failed to delete due to ${e.message}",Toast.LENGTH_SHORT).show()

                            }

                    }
                    .addOnFailureListener {e->
                        progressDialog.dismiss()
                        Log.d(TAG,"deleteBook: Failed to delete from storage due to ${e.message}")
                        Toast.makeText(context,"Failed to delete from storage due to ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            fun incrementBookViewCount(bookId: String){
                //1)get current
                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId)
                    .addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            //get views count
                            var viewsCount = "${snapshot.child("viewsCount").value}"

                            if (viewsCount==""){
                                viewsCount = "0"
                            }
                            //2 increment views count
                            val newViewsCount = viewsCount.toLong() + 1

                            //setup data to update to db
                            val hashMap = HashMap<String, Any>()
                            hashMap["viewsCount"] = newViewsCount

                            //set to db
                            val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                            dbRef.child(bookId)
                                .updateChildren(hashMap)
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }
        }

    }

