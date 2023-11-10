package com.yeslab.sirus.model

data class Order(
    val userId : String = "",
    val id : String = "",
    val title : String = "",
    val price : Int = 0,
    val status : Int = 0,
    val response : String = "",
    val giftId : String = "",
    val imageUrl : String = ""
)
