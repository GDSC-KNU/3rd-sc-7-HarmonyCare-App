package com.example.harmonycare.ui.checklist

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
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
import com.example.harmonycare.data.Checklist
import com.example.harmonycare.data.SharedPreferencesManager
import com.example.harmonycare.databinding.ChecklistDialogBinding
import com.example.harmonycare.databinding.DialogTipBinding
import com.example.harmonycare.databinding.FragmentChecklistBinding
import com.example.harmonycare.retrofit.ApiManager
import com.example.harmonycare.retrofit.ApiService
import com.example.harmonycare.retrofit.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

class ChecklistFragment : Fragment() {

    private var _binding: FragmentChecklistBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChecklistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChecklistBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataListAndSetAdapter()

        binding.fabTip.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomDialogBinding = DialogTipBinding.inflate(layoutInflater)
            bottomSheetDialog.setContentView(bottomDialogBinding.root)

            val accessToken = SharedPreferencesManager.getAccessToken()

            if (!accessToken.isNullOrEmpty()) {
                val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
                val apiManager = ApiManager(apiService)

                apiManager.getProfile(accessToken) {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss")
                    val specificDate = LocalDateTime.parse(it.babyBirthDate, formatter)

                    val currentDate = LocalDateTime.now()

                    val period = Period.between(specificDate.toLocalDate(), currentDate.toLocalDate())
                    var months = period.toTotalMonths().toInt()
                    if (months != 0) months -= 1

                    val heightArray = resources.getStringArray(R.array.height_array)
                    val weightArray = resources.getStringArray(R.array.weight_array)
                    val growthArray = resources.getStringArray(R.array.growth_array)

                    bottomDialogBinding.textviewMonth.text = "${months+1} ${getString(R.string.months_old)}"
                    bottomDialogBinding.textviewHeight.text = heightArray[months]
                    bottomDialogBinding.textviewWeight.text = weightArray[months]
                    bottomDialogBinding.textviewGrowth.text = growthArray[months]
                }

                val today = LocalDateTime.now().toLocalDate().toString()

                apiManager.getTip(accessToken, today) {
                    bottomDialogBinding.textviewTip.text = it
                }
            }

            bottomSheetDialog.show()
        }

        binding.fab.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomDialogBinding = ChecklistDialogBinding.inflate(layoutInflater)
            bottomSheetDialog.setContentView(bottomDialogBinding.root)
            bottomDialogBinding.timePicker.setIs24HourView(true)
            bottomDialogBinding.timePicker.hour = 1
            bottomDialogBinding.timePicker.minute = 30

            bottomDialogBinding.buttonSave.setOnClickListener {
                val title = bottomDialogBinding.editText.text.toString()
                val days = mutableListOf<String>()

                val toggleButtons = listOf(
                    bottomDialogBinding.toggleButtonMon,
                    bottomDialogBinding.toggleButtonTue,
                    bottomDialogBinding.toggleButtonWed,
                    bottomDialogBinding.toggleButtonThu,
                    bottomDialogBinding.toggleButtonFri,
                    bottomDialogBinding.toggleButtonSat,
                    bottomDialogBinding.toggleButtonSun
                )

                toggleButtons.forEachIndexed { index, toggleButton ->
                    if (toggleButton.isChecked) {
                        val stringArray = resources.getStringArray(R.array.weeks)
                        days.add(stringArray[index])
                    }
                }

                if (title.isBlank()) {
                    makeToast(requireContext(), getString(R.string.please_input_title))
                    return@setOnClickListener
                }
                if (days.isEmpty()) {
                    makeToast(requireContext(), getString(R.string.please_select_days))
                    return@setOnClickListener
                }
                val accessToken = SharedPreferencesManager.getAccessToken()

                if (!accessToken.isNullOrEmpty()) {
                    val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
                    val apiManager = ApiManager(apiService)

                    val checkTime = hourToString(bottomDialogBinding.timePicker.hour, bottomDialogBinding.timePicker.minute)
                    apiManager.saveChecklist(accessToken, title, days, checkTime) {
                        if (it) getDataListAndSetAdapter()
                        else makeToast(requireContext(), getString(R.string.save_failed))
                    }
                }
                bottomSheetDialog.dismiss()
            }

            bottomDialogBinding.buttonClose.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView?.visibility = View.VISIBLE

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // 뒤로가기를 누르면 액티비티를 종료
            requireActivity().finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        if (_binding == null) {
            _binding = FragmentChecklistBinding.inflate(layoutInflater)
        }
        activity?.let {
            (it as AppCompatActivity).supportActionBar?.title = LocalDate.now().toString()
        }
        getDataListAndSetAdapter()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDataList(onDataLoaded: (List<Checklist>) -> Unit) {
        val accessToken = SharedPreferencesManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
            val apiManager = ApiManager(apiService)

            val today = LocalDateTime.now().toLocalDate().toString()

            apiManager.getChecklist(accessToken, today
            ) { checklistData ->
                onDataLoaded(checklistData)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDataListAndSetAdapter() {
        getDataList { checklistData ->
            adapter = ChecklistAdapter(requireContext(), checklistData,
                onItemClick = { checklist ->
                    showDetailDialog(checklist)
                },
                onDeleteClick = { checklist ->
                    showDeleteConfirmationDialog(requireContext()) {
                        // 예 버튼을 클릭했을 때의 동작
                        deleteChecklist(checklist)
                    }
                },
                onCheckClick = { checklist ->
                    toggleCheckList(checklist)
                }
            )
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = adapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun deleteChecklist(checklist: Checklist) {
        val accessToken = SharedPreferencesManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
            val apiManager = ApiManager(apiService)

            apiManager.deleteChecklist(checklist.checklistId, accessToken) { response ->
                if (response) getDataListAndSetAdapter()
                else makeToast(requireContext(), getString(R.string.delete_failed))
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDetailDialog(checklist: Checklist) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val dialogBinding = ChecklistDialogBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(dialogBinding.root)

        dialogBinding.editText.setText(checklist.title)
        dialogBinding.timePicker.setIs24HourView(true)
        dialogBinding.timePicker.hour = checklist.checkTime.hour
        dialogBinding.timePicker.minute = checklist.checkTime.minute
        val stringArray = resources.getStringArray(R.array.weeks)
        val toggleButtons = listOf(
            dialogBinding.toggleButtonMon,
            dialogBinding.toggleButtonTue,
            dialogBinding.toggleButtonWed,
            dialogBinding.toggleButtonThu,
            dialogBinding.toggleButtonFri,
            dialogBinding.toggleButtonSat,
            dialogBinding.toggleButtonSun,
        )

        toggleButtons.forEachIndexed { index, toggleButton ->
            if (checklist.days.contains(stringArray[index]))
                toggleButton.isChecked = true
        }

        dialogBinding.buttonClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.buttonSave.setOnClickListener {
            val title = dialogBinding.editText.text.toString()
            val days = mutableListOf<String>()

            toggleButtons.forEachIndexed { index, toggleButton ->
                if (toggleButton.isChecked) {
                    days.add(stringArray[index])
                }
            }

            if (title.isBlank()) {
                makeToast(requireContext(), getString(R.string.please_input_title))
                return@setOnClickListener
            }
            if (days.isEmpty()) {
                makeToast(requireContext(), getString(R.string.please_select_days))
                return@setOnClickListener
            }
            val accessToken = SharedPreferencesManager.getAccessToken()

            if (!accessToken.isNullOrEmpty()) {
                val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
                val apiManager = ApiManager(apiService)


                val checkTime =
                    hourToString(dialogBinding.timePicker.hour, dialogBinding.timePicker.minute)
                apiManager.updateChecklist(
                    checklist.checklistId, accessToken, title, days, checkTime
                ) {
                    if (it) getDataListAndSetAdapter()
                    else makeToast(requireContext(), getString(R.string.update_failed))
                }
            }
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun showDeleteConfirmationDialog(context: Context, onDeleteConfirmed: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.delete_dialog_title))
            .setMessage(getString(R.string.delete_dialog_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                onDeleteConfirmed()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hourToString(hour: Int, minute: Int): String {
        val currentTime = LocalDateTime.now()
        val selectedTime = currentTime.withHour(hour).withMinute(minute).withSecond(0).withNano(0)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss")
        return selectedTime.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toggleCheckList(checklist: Checklist) {
        val accessToken = SharedPreferencesManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
            val apiManager = ApiManager(apiService)

            apiManager.toggleChecklist(checklist.checklistId, accessToken) { response ->
                if (response) getDataListAndSetAdapter()
                else makeToast(requireContext(), getString(R.string.check_failed))
            }
        }
    }

    private fun makeToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
}