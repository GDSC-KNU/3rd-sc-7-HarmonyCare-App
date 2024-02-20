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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.harmonycare.R
import com.example.harmonycare.data.Checklist
import com.example.harmonycare.data.SharedPreferencesManager
import com.example.harmonycare.databinding.ChecklistDialogBinding
import com.example.harmonycare.databinding.FragmentChecklistBinding
import com.example.harmonycare.retrofit.ApiManager
import com.example.harmonycare.retrofit.ApiService
import com.example.harmonycare.retrofit.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChecklistFragment : Fragment() {

    private var _binding: FragmentChecklistBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChecklistAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChecklistBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataListAndSetAdapter()

        binding.fab.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomDialogBinding = ChecklistDialogBinding.inflate(layoutInflater)
            bottomSheetDialog.setContentView(bottomDialogBinding.root)
            bottomDialogBinding.timePicker.setIs24HourView(true)

            bottomDialogBinding.buttonSave.setOnClickListener {
                val accessToken = SharedPreferencesManager.getAccessToken()

                if (!accessToken.isNullOrEmpty()) {
                    val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
                    val apiManager = ApiManager(apiService)

                    val title = bottomDialogBinding.editText.text.toString()
                    val days = mutableListOf<String>()
                    if (bottomDialogBinding.toggleButtonMon.isChecked) days.add("MONDAY")
                    if (bottomDialogBinding.toggleButtonTue.isChecked) days.add("TUESDAY")
                    if (bottomDialogBinding.toggleButtonWed.isChecked) days.add("WEDNEDSDAY")
                    if (bottomDialogBinding.toggleButtonThu.isChecked) days.add("THURSDAY")
                    if (bottomDialogBinding.toggleButtonFri.isChecked) days.add("FRIDAY")
                    if (bottomDialogBinding.toggleButtonSat.isChecked) days.add("SATURDAY")
                    if (bottomDialogBinding.toggleButtonSun.isChecked) days.add("SUNDAY")
                    val checkTime = hourToString(bottomDialogBinding.timePicker.hour, bottomDialogBinding.timePicker.minute)
                    apiManager.saveChecklist(accessToken, title, days, checkTime, {
                        if (it == true) {
                            getDataListAndSetAdapter()
                        }
                        else {
                            makeToast(requireContext(), "checklist save failed")
                        }
                    })
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
    private fun getDataList(onDataLoaded: (List<Checklist>) -> Unit) {
        val accessToken = SharedPreferencesManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
            val apiManager = ApiManager(apiService)


            apiManager.getChecklist(accessToken,
                { checklistData ->
                    if (checklistData != null) {
                        onDataLoaded(checklistData)
                    }
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDataListAndSetAdapter() {
        getDataList { checklistData ->
            adapter = ChecklistAdapter(checklistData,
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

            apiManager.deleteChecklist(checklist.checklistId, accessToken, { response ->
                if (response == true) {
                    getDataListAndSetAdapter()
                } else {
                    makeToast(requireContext(), "Failed to delete checklist")
                }
            })

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
        if (checklist.days.contains("MONDAY")) dialogBinding.toggleButtonMon.isChecked = true
        if (checklist.days.contains("TUESDAY")) dialogBinding.toggleButtonTue.isChecked = true
        if (checklist.days.contains("WEDNEDSDAY")) dialogBinding.toggleButtonWed.isChecked = true
        if (checklist.days.contains("THURSDAY")) dialogBinding.toggleButtonThu.isChecked = true
        if (checklist.days.contains("FRIDAY")) dialogBinding.toggleButtonFri.isChecked = true
        if (checklist.days.contains("SATURDAY")) dialogBinding.toggleButtonSat.isChecked = true
        if (checklist.days.contains("SUNDAY")) dialogBinding.toggleButtonSun.isChecked = true

        dialogBinding.buttonClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.buttonSave.setOnClickListener {
            val accessToken = SharedPreferencesManager.getAccessToken()

            if (!accessToken.isNullOrEmpty()) {
                val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
                val apiManager = ApiManager(apiService)

                val title = dialogBinding.editText.text.toString()
                val days = mutableListOf<String>()
                if (dialogBinding.toggleButtonMon.isChecked) days.add("MONDAY")
                if (dialogBinding.toggleButtonTue.isChecked) days.add("TUESDAY")
                if (dialogBinding.toggleButtonWed.isChecked) days.add("WEDNEDSDAY")
                if (dialogBinding.toggleButtonThu.isChecked) days.add("THURSDAY")
                if (dialogBinding.toggleButtonFri.isChecked) days.add("FRIDAY")
                if (dialogBinding.toggleButtonSat.isChecked) days.add("SATURDAY")
                if (dialogBinding.toggleButtonSun.isChecked) days.add("SUNDAY")
                val checkTime = hourToString(dialogBinding.timePicker.hour, dialogBinding.timePicker.minute)
                apiManager.updateChecklist(
                    checklist.checklistId, accessToken, title, days, checkTime,
                    { response ->
                    if (response == true) {
                        getDataListAndSetAdapter()
                    } else {
                        makeToast(requireContext(), "Failed to update checklist")
                    }
                })

            }
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showDeleteConfirmationDialog(context: Context, onDeleteConfirmed: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Delete Confirmation")
            .setMessage("Are you sure you want to delete this checklist?")
            .setPositiveButton("Delete") { dialog, which ->
                onDeleteConfirmed()
            }
            .setNegativeButton("Cancel", null)
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

            apiManager.toggleChecklist(checklist.checklistId, accessToken, { response ->
                if (response == true) {
                    getDataListAndSetAdapter()
                } else {
                    makeToast(requireContext(), "Failed to toggle checklist")
                }
            })

        }
    }

    fun makeToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
}