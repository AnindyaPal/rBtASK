package com.example.shantunu.redbusassgn.apiModels

class FullModel {
    val inventory : MutableList<Inventory> = mutableListOf()
    val busType : LinkedHashMap<String , String> = LinkedHashMap()
    val travels : LinkedHashMap<String , String> = LinkedHashMap()
}