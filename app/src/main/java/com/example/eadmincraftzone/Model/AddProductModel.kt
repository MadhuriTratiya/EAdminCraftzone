package com.example.eadmincraftzone.Model

data class AddProductModel(
    val productName : String? = "",
    val productDescription : String? = "",
    val productCoverImage : String? = "",
    val productCategory : String? = "",
    val ProductId : String? = "",
    val productMrp : String? = "",
    val productSp : String? = "",
    val productsImage : ArrayList<String>?)
