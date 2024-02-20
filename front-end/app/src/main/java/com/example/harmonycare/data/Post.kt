package com.example.harmonycare.data

import java.io.Serializable

data class Post (
    var communityId: Int,
    var title: String,
    var content: String
) : Serializable