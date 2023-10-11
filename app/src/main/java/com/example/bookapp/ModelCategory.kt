package com.example.bookapp

class ModelCategory {

    //variabel, harus cocok seperti di Firebase
    var id: String = ""
    var category: String = ""
    var timestamp: Long = 0
    var uid: String = ""

    // empty constructor, required by Firebase
    constructor()

    //parameterized constructor
    constructor(id: String, category: String, timestamp: Long, uid: String) {
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.uid = uid
    }
}
