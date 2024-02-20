package com.example.harmonycare.ui.checklist

import android.os.Build
import com.example.harmonycare.databinding.ChecklistItemBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.harmonycare.R
import com.example.harmonycare.data.Checklist

class ChecklistAdapter(
    private val dataList: List<Checklist>,
    private val onItemClick: (Checklist) -> Unit,
    private val onDeleteClick: (Checklist) -> Unit,
    private val onCheckClick: (Checklist) -> Unit
) : RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder>() {
    inner class ChecklistViewHolder(private val binding: ChecklistItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val checklist = dataList[position]
                    onItemClick(checklist)
                }
            }
            binding.buttonCheckbox.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val checklist = dataList[position]
                    onCheckClick(checklist)
                }
            }
            binding.buttonDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val checklist = dataList[position]
                    onDeleteClick(checklist)
                }
            }
        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(checklist: Checklist) {
            binding.textTitle.text = checklist.title
            var additionalText: String
            if (checklist.checkTime.hour < 1) additionalText = "${checklist.checkTime.minute} m"
            else additionalText = "${checklist.checkTime.hour}h ${checklist.checkTime.minute}m"
            binding.textCaption.text = "${checklist.days.joinToString(", ") { it.take(3).toLowerCase().capitalize() }}, $additionalText"
            if (checklist.isCheck) binding.buttonCheckbox.setImageResource(R.drawable.icon_checkbox_orange)
            else binding.buttonCheckbox.setImageResource(R.drawable.icon_checkbox_gray)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistViewHolder {
        val binding = ChecklistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChecklistViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ChecklistViewHolder, position: Int) {
        val checklist = dataList[position]
        holder.bind(checklist)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}