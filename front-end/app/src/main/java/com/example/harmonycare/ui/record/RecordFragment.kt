package com.example.harmonycare.ui.record

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.harmonycare.R
import com.example.harmonycare.databinding.FragmentRecordBinding
import com.example.harmonycare.data.Record
import com.example.harmonycare.data.SharedPreferencesManager
import com.example.harmonycare.databinding.RecordDialogBinding
import com.example.harmonycare.retrofit.ApiManager
import com.example.harmonycare.retrofit.ApiService
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.harmonycare.retrofit.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class RecordFragment : Fragment() {

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!
    private var isFabOpen = false
    private lateinit var adapter: RecordAdapter
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataListAndSetAdapter()

        binding.dateTextView.text = LocalDateTime.now().toLocalDate().toString()
        binding.fabAdd.setOnClickListener {
            toggleFab()
        }
        binding.fabBath.setOnClickListener {
            saveRecord("BATH", dateTimeToString(LocalDateTime.now()), dateTimeToString(LocalDateTime.now().plusMinutes(1)), "description")
        }
        binding.fabDiaper.setOnClickListener {
            saveRecord("DIAPER", dateTimeToString(LocalDateTime.now()), dateTimeToString(LocalDateTime.now()), "description")
        }
        binding.fabMeal.setOnClickListener {
            saveRecord("MEAL", dateTimeToString(LocalDateTime.now()), dateTimeToString(LocalDateTime.now()), "description")
        }
        binding.fabPlay.setOnClickListener {
            saveRecord("PLAY", dateTimeToString(LocalDateTime.now()), dateTimeToString(LocalDateTime.now()), "description")
        }
        binding.fabSleep.setOnClickListener {
            saveRecord("SLEEP", dateTimeToString(LocalDateTime.now()), dateTimeToString(LocalDateTime.now()), "description")
        }

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView?.visibility = View.VISIBLE

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // 뒤로가기를 누르면 액티비티를 종료
            requireActivity().finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDataList(onDataLoaded: (List<Record>) -> Unit) {
        val accessToken = SharedPreferencesManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
            val apiManager = ApiManager(apiService)


            apiManager.getRecordsForDay(LocalDateTime.now().toLocalDate().toString(), 0, "Bearer $accessToken",
                { response ->
                    if (response != null) {
                        onDataLoaded(response)
                    }
                },
                { _ ->
                    makeToast(requireContext(), "data load failed")
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDataListAndSetAdapter() {
        getDataList { recordList ->
            adapter = RecordAdapter(requireContext(), recordList,
                onItemClick = { record ->
                    showDetailDialog(record)
                },
                onDeleteClick = { record ->
                    showDeleteConfirmationDialog(requireContext()) {
                        // 예 버튼을 클릭했을 때의 동작
                        deleteRecord(record)
                    }
                }
            )
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = adapter

            startUpdateTimeRunnable()
        }
    }


    private fun toggleFab() {
        // 플로팅 액션 버튼 닫기 - 열려있는 플로팅 버튼 집어넣는 애니메이션 세팅
        if (isFabOpen) {
            ObjectAnimator.ofFloat(binding.fabSleep, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabMeal, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabPlay, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabDiaper, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabBath, "translationY", 0f).apply { start() }
            binding.fabAdd.setImageResource(R.drawable.icon_add)

            // 플로팅 액션 버튼 열기 - 닫혀있는 플로팅 버튼 꺼내는 애니메이션 세팅
        } else {
            ObjectAnimator.ofFloat(binding.fabSleep, "translationY", -200f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabMeal, "translationY", -400f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabPlay, "translationY", -600f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabDiaper, "translationY", -800f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabBath, "translationY", -1000f).apply { start() }
            binding.fabAdd.setImageResource(R.drawable.icon_close)
        }

        isFabOpen = !isFabOpen

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDetailDialog(record: Record) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val dialogBinding = RecordDialogBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(dialogBinding.root)

        when (record.recordTask) {
            "SLEEP" -> {
                dialogBinding.titleTextView.text = getString(R.string.sleep)
                dialogBinding.topBar.setBackgroundColor(resources.getColor(R.color.sleep_blue))
            }
            "MEAL" -> {
                dialogBinding.titleTextView.text = getString(R.string.meal)
                dialogBinding.topBar.setBackgroundColor(resources.getColor(R.color.meal_green))
            }
            "PLAY" -> {
                dialogBinding.titleTextView.text = getString(R.string.play)
                dialogBinding.topBar.setBackgroundColor(resources.getColor(R.color.play_purple))
            }
            "DIAPER" -> {
                dialogBinding.titleTextView.text = getString(R.string.diaper)
                dialogBinding.topBar.setBackgroundColor(resources.getColor(R.color.diaper_yellow))
            }
            "BATH" -> {
                dialogBinding.titleTextView.text = getString(R.string.bath)
                dialogBinding.topBar.setBackgroundColor(resources.getColor(R.color.bath_orange))
            }
        }
        dialogBinding.editText.setText(record.description)
        dialogBinding.startTimePicker.hour = record.startTime.hour
        dialogBinding.startTimePicker.minute = record.startTime.minute
        dialogBinding.endTimePicker.hour = record.endTime.hour
        dialogBinding.endTimePicker.minute = record.endTime.minute

        dialogBinding.buttonClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.buttonSave.setOnClickListener {
            val recordTask = dialogBinding.titleTextView.text.toString().toUpperCase()
            val startTime = hourToString(dialogBinding.startTimePicker.hour, dialogBinding.startTimePicker.minute)
            val endTime = hourToString(dialogBinding.endTimePicker.hour, dialogBinding.endTimePicker.minute)
            val description = dialogBinding.editText.text.toString()

            val accessToken = SharedPreferencesManager.getAccessToken()

            if (!accessToken.isNullOrEmpty()) {
                val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
                val apiManager = ApiManager(apiService)

                apiManager.updateRecord(record.recordId, recordTask, startTime, endTime, description, accessToken, onResponse =
                {   _ ->
                    getDataListAndSetAdapter()
                },
                {
                    _ ->
                    makeToast(requireContext(), getString(R.string.update_failed))
                })
            }
            else {
                makeToast(requireContext(), getString(R.string.access_token_error))
            }
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun hourToString(hour: Int, minute: Int): String {
        val currentTime = LocalDateTime.now()
        val selectedTime = currentTime.withHour(hour).withMinute(minute).withSecond(0).withNano(0)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss")
        return selectedTime.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dateTimeToString(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss")
        return dateTime.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveRecord(recordTask: String, startTime: String, endTime: String, description: String) {
        val accessToken = SharedPreferencesManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
            val apiManager = ApiManager(apiService)

            apiManager.saveRecord(accessToken, recordTask, startTime, endTime, description, { response ->
                // HTTP 응답 코드가 201이면 성공으로 간주합니다.
                if (response == 201) {
                    getDataListAndSetAdapter()
                } else {
                    makeToast(requireContext(), getString(R.string.save_failed))
                }
            }, {
                makeToast(requireContext(), getString(R.string.save_failed))
            })

        }
    }

    private fun showDeleteConfirmationDialog(context: Context, onDeleteConfirmed: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Delete Confirmation")
            .setMessage("Are you sure you want to delete this record?")
            .setPositiveButton("Delete") { _, _ ->
                onDeleteConfirmed()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun deleteRecord(record: Record) {
        val accessToken = SharedPreferencesManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
            val apiManager = ApiManager(apiService)

            apiManager.deleteRecord(record.recordId, accessToken) { response ->
                if (response) {
                    getDataListAndSetAdapter()
                } else {
                    makeToast(requireContext(), getString(R.string.delete_failed))
                }
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        if (_binding == null) {
            _binding = FragmentRecordBinding.inflate(layoutInflater)
        }
        activity?.let { fragmentActivity ->
            val accessToken = SharedPreferencesManager.getAccessToken()

            if (!accessToken.isNullOrEmpty()) {
                val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
                val apiManager = ApiManager(apiService)

                apiManager.getProfile(accessToken) { profile ->
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val specificDate = LocalDateTime.parse(profile.babyBirthDate, formatter)

                    val currentDate = LocalDateTime.now()

                    // 특정 날짜로부터 현재까지의 시간 간격을 계산합니다.
                    val daysPassed = ChronoUnit.DAYS.between(specificDate, currentDate)
                    (fragmentActivity as AppCompatActivity).supportActionBar?.title =
                        "${LocalDate.now()} (D+$daysPassed)"
                }

            } else {
                (fragmentActivity as AppCompatActivity).supportActionBar?.title = LocalDate.now().toString()
            }
        }
    }

    private fun startUpdateTimeRunnable() {
        handler.removeCallbacksAndMessages(null)

        handler.post(object : Runnable {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                updateLastUpdateTime()
                handler.postDelayed(this, 60000) // 1분마다 갱신
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLastUpdateTime() {
        val dataList = adapter.getDataList()

        val diaperRecords = dataList.filter { it.recordTask == "DIAPER" }
        val mealRecords = dataList.filter { it.recordTask == "MEAL" }
        val sleepRecords = dataList.filter { it.recordTask == "SLEEP" }

        if (_binding != null) {
            val binding = _binding!!
            val recentDiaperTime = findRecentTime(diaperRecords)
            val recentMealTime = findRecentTime(mealRecords)
            val recentSleepTime = findRecentTime(sleepRecords)

            binding.recentDiaperTextview.text = recentDiaperTime
            binding.recentMealTextview.text = recentMealTime
            binding.recentSleepTextview.text = recentSleepTime
        } else {
            Log.e(TAG, "Binding is null when trying to update last update time.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun findRecentTime(records: List<Record>): String {
        if (records.isEmpty()) return getString(R.string.no_record)

        val currentTime = LocalDateTime.now()
        val recentRecord = records.maxByOrNull { it.startTime } ?: return getString(R.string.no_record)
        val difference = Duration.between(recentRecord.startTime, currentTime)
        val differenceMinutes = difference.toMinutes()

        return if (differenceMinutes < 60) {
            "● $differenceMinutes${getString(R.string.m_ago)}"
        } else if (differenceMinutes < 1440) {
            val differenceHours = difference.toHours()
            val remainingMinutes = differenceMinutes % 60
            if (remainingMinutes == 0L) {
                "● $differenceHours${getString(R.string.h_ago)}"
            } else {
                "● $differenceHours${getString(R.string.h)} $remainingMinutes${getString(R.string.m_ago)}"
            }
        } else {
            getString(R.string.long_time_ago)
        }
    }

    private fun makeToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
}
