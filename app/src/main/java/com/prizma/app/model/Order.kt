package com.prizma.app.model

data class Order(
    val userId : String = "",
    val userEmail : String = "",
    val id : String = "",
    val title : String = "",
    val price : Int = 0,
    val status : Int = 0,
    val response : String = "",
    val giftId : String = "",
    val imageUrl : String = ""
)
