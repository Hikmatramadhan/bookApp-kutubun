package com.example.bookapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList
import com.example.bookapp.databinding.RowPdfAdminBinding

class AdapterPdfAdmin :RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable{

    //view binding
    private lateinit var binding: RowPdfAdminBinding

    //context
    private val context: Context

    //array list to hold pdfs
    public var pdfArrayList: ArrayList<ModelPdf>

    //filter list
    public val filterList:ArrayList<ModelPdf>

    //filter object, just make it private, will work
    private var filter: FilterPdfAdmin? = null


    //constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        //bind inflate layout row_pdf_admin.xml
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        /*---get data, set data, handle clicks etc ---*/

        //get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        //mengubah timestamp to dd/MM/yyy format
        val formattedDate = MyApplication.formatTimestamp(timestamp)

        //mengatur data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        //category id
        MyApplication.loadCategory(categoryId, holder.categoryTv)

        //we don't need page number
        MyApplication.loadPdfFromUrlSinglePage(pdfUrl, title, holder.pdfView, holder.progressBar, null)

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        //handle click, show dialog with options 1) edit bok 2) delete book
        holder.moreBtn.setOnClickListener{
            moreOptionsDialog(model, holder)
        }

        //handle click, open book detail
        holder.itemView.setOnClickListener{
            //pass book id in intent, that be used to get pdf info
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", pdfId)//will by used to load book detail
            context.startActivity(intent)
        }

    }

    private fun moreOptionsDialog(model: ModelPdf, holder: AdapterPdfAdmin.HolderPdfAdmin) {
        //get id,url,title, of book
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //options to show in dialog
        val options = arrayOf("Edit", "Delete")

        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Chose Option")
            .setItems(options){dialod, position->
                //handle item click
                if (position == 0){
                    //edit is clicked
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId) //passed bookId, will be used to edit the book
                    context.startActivity(intent)
                }
                else if (position == 1){
                    //delete is clicked

                    //show confirmation
                    MyApplication.deleteBook(context,bookId,bookUrl,bookTitle)
                }
            }
            .show()
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size //items count
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }

    /*---viewHolder class for row_pdf_admin.xml---*/
    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ui views of row_pdf_admin.xml
        val pdfView = binding.pdfView
        var progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val categoryTv = binding.categoryTv
        val moreBtn = binding.moreBtn
    }
}
