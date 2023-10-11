package com.example.bookapp

import android.widget.Filter
import java.util.ArrayList

class FilterPdfUser: Filter {

    //daftar array yang ingin kita cari
    var filterList: ArrayList<ModelPdf>

    //adaptor di mana filter perlu diimplementasikan
    var adapterPdfUser: AdapterPdfUser

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint

        val results = FilterResults()
        //nilai yang akan dicari tidak boleh null dan tidak boleh kosong
        if (constraint != null && constraint.isNotEmpty()){
            //ubah ke huruf besar untuk menghilangkan sensitivitas huruf besar-kecil
            constraint = constraint.toString().uppercase()
            val filterModels = ArrayList<ModelPdf>()
            for (i in filterList.indices) {
                //validasi jika cocok
                if (filterList[i].title.uppercase().contains(constraint)) {
                    //searched value matched which title, add to list
                    filterModels.add(filterList[i])
                }
            }
            //return filtered list and size
            results.count = filterModels.size
            results.values = filterModels
        }
        else{
            //entah itu nol atau kosong
            //return original list and size
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter change
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfUser.notifyDataSetChanged()
    }

}