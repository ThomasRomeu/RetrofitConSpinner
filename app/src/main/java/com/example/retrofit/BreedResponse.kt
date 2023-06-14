package com.example.retrofit

import com.google.gson.annotations.SerializedName

data class BreedResponse (
    @SerializedName("message") val breed: Map<String, List<String>>,
    val status: String
    )