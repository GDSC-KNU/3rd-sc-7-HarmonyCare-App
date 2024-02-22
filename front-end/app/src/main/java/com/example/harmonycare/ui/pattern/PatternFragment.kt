package com.example.harmonycare.ui.pattern

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.harmonycare.R
import com.example.harmonycare.databinding.FragmentPatternBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import com.example.harmonycare.databinding.FragmentChecklistBinding
import java.util.Calendar


class PatternFragment : Fragment() {

    private var _binding: FragmentPatternBinding? = null
    private val binding get() = _binding!!
    private lateinit var selectedDate: Calendar
    private lateinit var sharedPreferences: SharedPreferences

    companion object{
        fun newInstance(): Fragment{
            return PatternFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatternBinding.inflate(inflater, container, false)
        val root: View = binding.root
        //sharedPreference 초기화
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        // SharedPreferences에서 authcode 가져오기
        val accessToken = sharedPreferences.getString("accessToken", null)
        setXMLToggle(true)
        selectedDate = Calendar.getInstance()
        /*updateSelectedDateButtonText()

        if (accessToken != null) {
            fetchRecordsForSelectedDate(accessToken)
        }*/
        /*binding.button.setOnClickListener {
            showDatePickerDialog()
            if (accessToken != null) {
                fetchRecordsForSelectedDate(accessToken)
            }
        }*/

        // Begin a transaction to add the PieChartFragment to the container FrameLayout
        val transaction = childFragmentManager.beginTransaction()

// Initial transaction to add the PieChartFragment
        val pieChartFragment = PieChartFragment()
        transaction.replace(R.id.frag, pieChartFragment)
        transaction.addToBackStack(null) // Optional: Add the transaction to the back stack
        transaction.commit()

// Daily 텍스트뷰 클릭 리스너 설정
        binding.daily.setOnClickListener {
            setXMLToggle(true)
            // Begin a transaction to replace the current fragment with the PieChartFragment
            val pieTransaction = childFragmentManager.beginTransaction()
            pieTransaction.replace(R.id.frag, pieChartFragment)
            pieTransaction.addToBackStack(null) // Optional: Add the transaction to the back stack
            pieTransaction.commit()
        }

// Weekly 텍스트뷰 클릭 리스너 설정
        binding.Weekly.setOnClickListener {
            setXMLToggle(false)
            // Begin a transaction to replace the current fragment with the BarChartFragment
            val barTransaction = childFragmentManager.beginTransaction()
            val barChartFragment = BarChartFragment()
            barTransaction.replace(R.id.frag, barChartFragment)
            barTransaction.addToBackStack(null) // Optional: Add the transaction to the back stack
            barTransaction.commit()
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        if (_binding == null) {
            _binding = FragmentPatternBinding.inflate(layoutInflater)
        }
    }

    /*private fun showDatePickerDialog() {
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
    }*/
    /*private fun updateSelectedDateButtonText() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.time)
        binding.button.text = formattedDate
    }
    private fun fetchRecordsForSelectedDate(authToken: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.time)

        val apiService = RetrofitClient.createService(ApiService::class.java)
        val call = apiService.getRecordsForDay(formattedDate, 1, "Bearer $authToken")

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

    private fun displayRecordsOnPieChart(recordResponse: List<RecordGetRequest>) {
        val mpPieChart: PieChart = _binding!!.MPPieChart

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
            val percentage = duration.toFloat() / totalMinutesInDay * 100
            entries.add(PieEntry(percentage, record.recordTask))
            colors.add(color)
        }

        // Find the empty time slots and add them as gray entries
        val emptyTimeSlots = findEmptyTimeSlots(occupiedTimeSlots, totalMinutesInDay)
        for (emptySlot in emptyTimeSlots) {
            val duration = emptySlot.last - emptySlot.first
            val percentage = duration.toFloat() / totalMinutesInDay * 100
            entries.add(PieEntry(percentage, "Empty"))
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

    *//*private fun displayRecordsOnPieChart(recordResponse: List<RecordGetRequest>) {
        val mpPieChart: PieChart = _binding!!.MPPieChart

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
        mpPieChart.centerText = "Day + 3\n성장지표"
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
        // Check if the record response is empty
        if (recordResponse.isEmpty()) {
            // If the record response is empty, add an entry for the entire day as gray
            entries.add(PieEntry(100f, "Empty"))
            colors.add(Color.GRAY)
        } else {
            // Iterate over the records
            var totalRecordedTime = 0
            for (record in recordResponse) {
                val startTime = getMinutesFromTimeString(record.startTime)
                val endTime = getMinutesFromTimeString(record.endTime)
                val duration = endTime - startTime

                // Calculate the percentage of the day covered by this record
                val percentage = duration.toFloat() / totalMinutesInDay * 100

                // Get color based on recordTask
                val colorResId = taskColorMap[record.recordTask] ?: R.color.dark_gray // Default color if not found
                val color = ContextCompat.getColor(requireContext(), colorResId)

                // Add the entry for this record with corresponding color
                entries.add(PieEntry(percentage, record.recordTask))
                colors.add(color)

                // Update the total recorded time
                totalRecordedTime += duration
            }

            // Calculate the percentage of empty time slots in the day
            val emptyPercentage =
                (totalMinutesInDay - totalRecordedTime).toFloat() / totalMinutesInDay * 100

            // Add an entry for the empty time slots
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
    }*//*

    // Helper function to convert time string to minutes
    private fun getMinutesFromTimeString(timeString: String): Int {
        val parts = timeString.split(" ")[1].split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }*/
    private fun setXMLToggle(isViewClicked: Boolean) {
        if (!isViewClicked) {
            binding.daily.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
            binding.daily.setBackgroundResource(0)
            binding.Weekly.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.Weekly.setBackgroundResource(R.drawable.item_bg_on)
        } else {
            binding.daily.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.daily.setBackgroundResource(R.drawable.item_bg_on)
            binding.Weekly.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
            binding.Weekly.setBackgroundResource(0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView?.visibility = View.VISIBLE

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // 뒤로가기를 누르면 액티비티를 종료
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
