package com.example.harmonycare.data

import java.time.LocalDateTime

data class Record(
    var recordId: Int = 0,
    var recordTask: String,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
    var description: String
)