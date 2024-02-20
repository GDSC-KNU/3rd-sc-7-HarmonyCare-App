package com.example.harmonycare.data

import java.time.LocalDateTime

data class Checklist (
    var checklistId: Int,
    var title: String,
    var days: List<String>,
    var checkTime: LocalDateTime,
    var isCheck: Boolean
)