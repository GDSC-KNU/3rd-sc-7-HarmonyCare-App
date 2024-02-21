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
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.harmonycare.databinding.FragmentPieChartBinding
import com.example.harmonycare.databinding.FragmentProfileBinding

class PieChartFragment : Fragment() {

    private var _binding: FragmentPieChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var selectedDate: Calendar

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPieChartBinding.inflate(inflater, container, false)
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
        val call = apiService.getRecordsForDay(formattedDate, 2, "Bearer $authToken")

        call.enqueue(object : Callback<RecordGetResponse> {
            override fun onResponse(call: Call<RecordGetResponse>, response: Response<RecordGetResponse>) {
                if (response.isSuccessful) {
                    val recordResponse = response.body()
                    if (recordResponse != null) {
                        displayRecordsOnPieChart(recordResponse.response)
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

    override fun onResume() {
        super.onResume()
        if (_binding == null) {
            _binding = FragmentPieChartBinding.inflate(layoutInflater)
        }
    }

    private fun displayRecordsOnPieChart(recordResponse: List<RecordGetRequest>) {
        val mpPieChart: PieChart = _binding!!.pieChart

        // Clear any existing entries
        mpPieChart.clear()
        mpPieChart.setUsePercentValues(false) // 퍼센트 값 사용 안 함
        mpPieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        mpPieChart.legend.isEnabled = false
        mpPieChart.description.isEnabled = false
        mpPieChart.isDrawHoleEnabled = true
        mpPieChart.setHoleColor(Color.WHITE)
        mpPieChart.transparentCircleRadius = 61f
        mpPieChart.animateY(1000, Easing.EaseInOutCubic)
        mpPieChart.centerText = "Day + 3"
        mpPieChart.setCenterTextSize(20f)
        mpPieChart.invalidate()

        // Define the total duration of a day (in minutes)
        val totalMinutesInDay = 24 * 60

        // Initialize lists to hold entry data
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        // recordTask에 따른 색상 맵
        val taskColorMap = mapOf(
            "SLEEP" to R.color.sleep_blue,
            "MEAL" to R.color.meal_green,
            "PLAY" to R.color.play_purple,
            "DIAPER" to R.color.diaper_yellow,
            "BATH" to R.color.bath_orange
        )

        // Create a list to store occupied time slots
        val occupiedTimeSlots = mutableListOf<IntRange>()

        // Iterate over the records
        var previousEndTime = 0 // 이전 endTime을 추적하기 위한 변수 추가
        for (record in recordResponse) {
            val startTime = getMinutesFromTimeString(record.startTime)
            val endTime = getMinutesFromTimeString(record.endTime)
            val timeSlot = startTime until endTime
            occupiedTimeSlots.add(timeSlot)

            // Get color based on recordTask
            val colorResId = taskColorMap[record.recordTask] ?: R.color.dark_gray // Default color if not found
            val color = ContextCompat.getColor(requireContext(), colorResId)

            // Add the entry for this record with corresponding color
            val duration = endTime - startTime
            if (previousEndTime < startTime) {
                val emptyDuration = startTime - previousEndTime
                val emptyPercentage = emptyDuration.toFloat() / totalMinutesInDay * 100
                entries.add(PieEntry(emptyPercentage, "Empty"))
                colors.add(Color.GRAY)
            }
            val percentage = duration.toFloat() / totalMinutesInDay * 100
            entries.add(PieEntry(percentage, record.recordTask))
            colors.add(color)

            // 빈 슬롯 처리


            previousEndTime = endTime // 이전 endTime 업데이트
        }

        // 마지막 endTime부터 23:59:59까지 빈 슬롯 추가
        if (previousEndTime < totalMinutesInDay) {
            val emptyDuration = totalMinutesInDay - previousEndTime
            val emptyPercentage = emptyDuration.toFloat() / totalMinutesInDay * 100
            entries.add(PieEntry(emptyPercentage, "Empty"))
            colors.add(Color.GRAY)
        }

        // Configure the data set
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = Color.BLACK

        // Create the PieData object and set it to the chart
        val data = PieData(dataSet)
        mpPieChart.data = data

        // Refresh the chart
        mpPieChart.invalidate()
    }

    private fun findEmptyTimeSlots(occupiedTimeSlots: List<IntRange>, totalMinutesInDay: Int): List<IntRange> {
        val emptyTimeSlots = mutableListOf<IntRange>()
        var previousEndTime = 0

        for (occupiedSlot in occupiedTimeSlots) {
            if (occupiedSlot.first > previousEndTime) {
                val emptySlot = previousEndTime until occupiedSlot.first
                emptyTimeSlots.add(emptySlot)
            }
            previousEndTime = occupiedSlot.last
        }

        if (previousEndTime < totalMinutesInDay) {
            val lastEmptySlot = previousEndTime until totalMinutesInDay
            emptyTimeSlots.add(lastEmptySlot)
        }

        return emptyTimeSlots
    }
    private fun getMinutesFromTimeString(timeString: String): Int {
        val parts = timeString.split(" ")[1].split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

}