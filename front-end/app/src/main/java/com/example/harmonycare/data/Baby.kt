package com.example.harmonycare.data

import com.google.gson.annotations.SerializedName

data class Baby(
    @SerializedName("name") val name: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("birthDate") val birthDate: String,
    @SerializedName("birthWeight") val birthWeight: Float
)