package com.example.myapitest.model

data class Car (
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Place
)

data class OneCarValues (
    val id: String,
    val value: Car
)

data class Place (
    val lat: Float,
    val long: Float
)
//    id: "001"
//    *         imageUrl: "https://image"
//    *         year: "2020/2020"
//    *         name: "Gaspar"
//    *         licence: "ABC-1234"
//    *         place:
//    *           lat: 0
//    *           long: 0
