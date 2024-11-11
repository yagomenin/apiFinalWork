package com.example.myapitest.service

import com.example.myapitest.model.Car
import com.example.myapitest.model.OneCarValues
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("/car")
    suspend fun getCars(): List<Car>

    @GET("/car/{id}")
    suspend fun getCar(@Path("id")id: String): OneCarValues

    @PATCH("/car/{id}")
    suspend fun updateCar(@Path("id")id: String, @Body updateCar: Car): Car

    @DELETE("/car/{id}")
    suspend fun deleleteCar(@Path("id")id: String)

    @POST("/car")
    suspend fun addCar(@Body car: Car): Car

}