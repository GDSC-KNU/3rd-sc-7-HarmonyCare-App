package com.example.harmonycare.ui.pattern

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PatternViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is pattern Fragment"
    }
    val text: LiveData<String> = _text


    //dataSet 만들기
    data class CommitData(val date: String, val commitNum: Int)
    val dataList: List<CommitData> = listOf(
        CommitData("08-28",3),
        CommitData("08-29",2),
        CommitData("08-30",5),
        CommitData("08-31",2),
        CommitData("09-01",3),
        CommitData("09-02",6),
        CommitData("09-03",7),
        CommitData("09-04",1),
        CommitData("09-05",3),
        CommitData("09-06",2)
    )

}


