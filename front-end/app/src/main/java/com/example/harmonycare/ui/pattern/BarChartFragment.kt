package com.example.harmonycare.ui.pattern

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
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
import com.example.harmonycare.databinding.FragmentCommunityDetailBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

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

        val apiService = RetrofitClient.createService(ApiService::class.java)
        val call = apiService.getRecordsForDay(formattedDate, 7, "Bearer $authToken")

        call.enqueue(object : Callback<RecordGetResponse> {
            override fun onResponse(
                call: Call<RecordGetResponse>,
                response: Response<RecordGetResponse>
            ) {
                if (response.isSuccessful) {
                    val recordResponse = response.body()
                    if (recordResponse != null) {
                        displayRecordsOnBarChart(recordResponse.response)
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

    private fun displayRecordsOnBarChart(recordResponse: List<RecordGetRequest>) {
        val mpBarChart: BarChart = _binding!!.barChart

        // Clear any existing entries
        mpBarChart.clear()
        mpBarChart.setDrawBarShadow(false)
        mpBarChart.setDrawValueAboveBar(true)
        mpBarChart.description.isEnabled = false
        mpBarChart.setPinchZoom(false)
        mpBarChart.setDrawGridBackground(false)

        // Define the total duration of a day (in minutes)
        val totalMinutesInDay = 24 * 60

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

        // Iterate over the records
        var previousEndTime = 0 // 이전 endTime을 추적하기 위한 변수 추가
        for (record in recordResponse) {
            val startTime = getMinutesFromTimeString(record.startTime)
            val endTime = getMinutesFromTimeString(record.endTime)

            // Get color based on recordTask
            val colorResId = taskColorMap[record.recordTask] ?: R.color.dark_gray // Default color if not found
            val color = ContextCompat.getColor(requireContext(), colorResId)

            // Add the entry for this record with corresponding color
            val duration = endTime - startTime
            val percentage = duration.toFloat() / totalMinutesInDay * 100
            entries.add(BarEntry(startTime.toFloat(), percentage))
            colors.add(color)
        }

        // Configure the data set
        val dataSet = BarDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = Color.BLACK
        dataSet.barBorderWidth = 1.0f

        // Create the BarData object and set it to the chart
        val data = BarData(dataSet)
        mpBarChart.data = data

        // Refresh the chart
        mpBarChart.invalidate()
    }

    private fun getMinutesFromTimeString(timeString: String): Int {
        val parts = timeString.split(" ")[1].split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    override fun onResume() {
        super.onResume()
        if (_binding == null) {
            _binding = FragmentBarChartBinding.inflate(layoutInflater)
        }
    }

}