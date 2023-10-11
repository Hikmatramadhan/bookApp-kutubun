package com.example.bookapp

import android.widget.Filter
import java.util.ArrayList

/*Digunakan untuk memfilter data dari recyclerview > mencari pdf dari daftar pdf di recyclerview*/
class FilterPdfAdmin : Filter{
    //array list di mana kita ingin mencari
    var filterList: ArrayList<ModelPdf>
    //adapter di mana filter perlu diterapkan
    var adapterPdfAdmin: AdapterPdfAdmin

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence? = constraint //value to search
        val results = FilterResults()
        //nilai yang akan dicari tidak boleh nol
        if (constraint != null && constraint.isNotEmpty()) {
            //ubah huruf besar
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices) {
                //validate if
                if (filterList[i].title.lowercase().contains(constraint)) {
                    //searched value
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            //searched value is either null or empty, return all data
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changes
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfAdmin.notifyDataSetChanged()
    }
}






