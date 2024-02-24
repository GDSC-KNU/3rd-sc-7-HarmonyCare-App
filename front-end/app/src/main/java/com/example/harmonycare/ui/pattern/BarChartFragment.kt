package com.example.harmonycare.ui.pattern

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.harmonycare.R
import com.example.harmonycare.data.SharedPreferencesManager
import com.example.harmonycare.retrofit.ApiService
import com.example.harmonycare.retrofit.RecordGetRequest
import com.example.harmonycare.retrofit.RecordGetResponse
import com.example.harmonycare.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.harmonycare.databinding.FragmentBarChartBinding
import com.example.harmonycare.databinding.FragmentPatternBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Date
import java.util.concurrent.TimeUnit

class BarChartFragment : Fragment() {

    private var _binding: FragmentBarChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var selectedDate: Calendar

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarChartBinding.inflate(inflater, container, false)
        val root: View = binding.root
        //sharedPreference 초기화
        val accessToken = SharedPreferencesManager.getAccessToken()

        selectedDate = Calendar.getInstance()
        updateSelectedDateButtonText()

        if (accessToken != null) {
            fetchRecordsForSelectedDate(accessToken)
        }

        binding.button.setOnClickListener {
            showDatePickerDialog()
            if (accessToken != null) {
                fetchRecordsForSelectedDate(accessToken)
            }
        }

        return root
        // Customize your PieChart here if needed

    }

    override fun onResume() {
        super.onResume()
        if (_binding == null) {
            _binding = FragmentBarChartBinding.inflate(layoutInflater)
        }
    }
    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateSelectedDateButtonText()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateSelectedDateButtonText() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.time)
        binding.button.text = formattedDate
    }

    private fun fetchRecordsForSelectedDate(authToken: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.time)
        val sixDaysAgo = Calendar.getInstance()
        sixDaysAgo.time = selectedDate.time
        sixDaysAgo.add(Calendar.DAY_OF_MONTH, -6) // 선택한 날짜에서 6일 전의 날짜를 얻음
        val weeklystart = dateFormat.format(sixDaysAgo.time)

        val apiService = RetrofitClient.createService(ApiService::class.java)
        val call = apiService.getRecordsForDay(formattedDate, 6, "Bearer $authToken")

        call.enqueue(object : Callback<RecordGetResponse> {
            override fun onResponse(
                call: Call<RecordGetResponse>,
                response: Response<RecordGetResponse>
            ) {
                if (response.isSuccessful) {
                    val recordResponse = response.body()
                    if (recordResponse != null) {
                        displayRecordsOnBarChart(recordResponse.response, weeklystart)
                    } else {
                        // Handle null response
                    }
                } else {
                    // Handle unsuccessful response
                }
            }

            override fun onFailure(call: Call<RecordGetResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }


    private fun displayRecordsOnBarChart(recordResponse: List<RecordGetRequest>, weeklystart: String) {
        val mpBarChart: BarChart = _binding!!.barChart

        // Clear any existing entries
        mpBarChart.clear()
        mpBarChart.setDrawBarShadow(false)
        mpBarChart.setDrawValueAboveBar(true)
        mpBarChart.description.isEnabled = false
        mpBarChart.setPinchZoom(false)
        mpBarChart.setDrawGridBackground(false)
        mpBarChart.legend.isEnabled = false
        mpBarChart.xAxis.isEnabled = false
        mpBarChart.axisLeft.isEnabled = false
        mpBarChart.axisRight.isEnabled = false
        mpBarChart.animateXY(1000, 1000)

        val description = Description()
        description.text = "Unit: minutes"
        description.textSize = 12f
        description.textColor = Color.BLACK
        description.setPosition(300f,30f)
        mpBarChart.description = description

        // Initialize lists to hold entry data
        val entries = ArrayList<BarEntry>()
        val colors = ArrayList<Int>()

        // recordTask에 따른 색상 맵
        val taskColorMap = mapOf(
            "SLEEP" to R.color.sleep_blue,
            "MEAL" to R.color.meal_green,
            "PLAY" to R.color.play_purple,
            "DIAPER" to R.color.diaper_yellow,
            "BATH" to R.color.bath_orange
        )

        // Initialize a map to hold duration data for each task and date
        val taskDurationMap = mutableMapOf<Pair<String, String>, Int>()

        // Iterate over the records to calculate duration for each task and date
        for (record in recordResponse) {
            val key = Pair(record.startTime.split(" ")[0], record.recordTask) // Date and task as key
            val duration = getDuration(record.startTime, record.endTime)
            val currentDuration = taskDurationMap.getOrDefault(key, 0)
            taskDurationMap[key] = currentDuration + duration
        }

        // Iterate over the dates from weeklystart to 6 days later and add entries for each date and task
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormat.parse(weeklystart) ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        for (i in 0..6) {
            val currentDate = dateFormat.format(calendar.time)
            for ((task, _) in taskColorMap) {
                val key = Pair(currentDate, task)
                val duration = taskDurationMap[key] ?: 0
                val colorResId = taskColorMap[task] ?: R.color.dark_gray
                val color = ContextCompat.getColor(requireContext(), colorResId)
                entries.add(BarEntry(i.toFloat(), duration.toFloat()))
                colors.add(color)
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Configure the data set
        val dataSet = BarDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = Color.BLACK
        dataSet.barBorderWidth = 1.0f


        // Create the BarData object and set it to the chart
        val data = BarData(dataSet)
        data.barWidth = 0.9f // Adjust the width of bars to 90% of available space
        mpBarChart.data = data


        // Refresh the chart
        mpBarChart.invalidate()
    }

    private fun getDaysDifference(startDate: String, endDate: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val start = dateFormat.parse(startDate) ?: Date()
        val end = dateFormat.parse(endDate) ?: Date()
        val diff = end.time - start.time
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
    }

    private fun getDuration(startTime: String, endTime: String): Int {
        val startParts = startTime.split(" ")[1].split(":")
        val endParts = endTime.split(" ")[1].split(":")
        val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
        val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
        return endMinutes - startMinutes
    }

    private fun getMinutesFromTimeString(timeString: String): Int {
        val parts = timeString.split(" ")[1].split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

}