package com.example.bookapp

class ModelPdf {

    //variables
    var categoryId:String = ""
    var description:String = ""
    var downloadsCount:Long = 0
    var id = ""
    var timestamp:Long = 0
    var title:String = ""
    var uid = ""
    var url:String = ""
    var viewsCount:Long = 0

    //empty constructor (required by firebase)
    constructor()

    //parameterized constructor
    constructor(
        categoryId: String,
        description: String,
        downloadsCount: Long,
        id: String,
        timestamp: Long,
        title: String,
        uid: String,
        url: String,
        viewsCount: Long,
    ) {
        this.categoryId = categoryId
        this.description = description
        this.downloadsCount = downloadsCount
        this.id = id
        this.timestamp = timestamp
        this.title = title
        this.uid = uid
        this.url = url
        this.viewsCount = viewsCount
    }
}