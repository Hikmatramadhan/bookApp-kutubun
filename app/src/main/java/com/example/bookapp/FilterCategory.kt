package com.example.bookapp

import android.widget.Filter
import java.util.ArrayList

class FilterCategory(private val filterList: ArrayList<ModelCategory>, private val adapterCategory: AdapterCategory) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraintText = constraint
        val results = FilterResults()

        // Nilai tidak boleh null dan tidak boleh kosong
        if (constraintText != null && constraintText.isNotEmpty()) {
            // Nilai yang dicari tidak null dan tidak kosong

            // Ubah menjadi huruf besar, atau huruf kecil untuk menghindari sensitivitas huruf besar-kecil
            constraintText = constraintText.toString().uppercase()
            val filteredModels = ArrayList<ModelCategory>()
            for (i in filterList.indices) {
                // Validasi
                if (filterList[i].category.uppercase().contains(constraintText)) {
                    // Tambahkan ke daftar yang difilter
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            // Nilai pencarian entah null atau kosong
            results.count = filterList.size
            results.values = filterList
        }
        return results // Jangan lupa mengembalikannya
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        // Terapkan perubahan filter
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        // Memberi tahu perubahan
        adapterCategory.notifyDataSetChanged()
    }
}

