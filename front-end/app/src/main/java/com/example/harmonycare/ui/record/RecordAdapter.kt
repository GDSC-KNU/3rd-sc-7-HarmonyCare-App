package com.example.harmonycare.ui.record

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.harmonycare.R
import com.example.harmonycare.databinding.RecordItemBinding
import com.example.harmonycare.data.Record

class RecordAdapter(
    val context: Context,
    private val dataList: List<Record>,
    private val onItemClick: (Record) -> Unit,
    private val onDeleteClick: (Record) -> Unit
) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {
    inner class RecordViewHolder(private val binding: RecordItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val record = dataList[position]
                    onItemClick(record)
                }
            }

            binding.buttonDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val record = dataList[position]
                    onDeleteClick(record)
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(record: Record) {
            when (record.recordTask) {
                "SLEEP"-> {
                    binding.textTitle.text = "Sleep"
                    binding.textTitle.setTextColor(ContextCompat.getColor(context, R.color.sleep_blue))
                    binding.textCircle.setTextColor(ContextCompat.getColor(context, R.color.sleep_blue))
                }
                "MEAL" -> {
                    binding.textTitle.text = "Meal"
                    binding.textTitle.setTextColor(ContextCompat.getColor(context, R.color.meal_green))
                    binding.textCircle.setTextColor(ContextCompat.getColor(context, R.color.meal_green))
                }
                "PLAY" -> {
                    binding.textTitle.text = "Play"
                    binding.textTitle.setTextColor(ContextCompat.getColor(context, R.color.play_purple))
                    binding.textCircle.setTextColor(ContextCompat.getColor(context, R.color.play_purple))
                }
                "DIAPER" -> {
                    binding.textTitle.text = "Diaper"
                    binding.textTitle.setTextColor(ContextCompat.getColor(context, R.color.diaper_yellow))
                    binding.textCircle.setTextColor(ContextCompat.getColor(context, R.color.diaper_yellow))
                }
                "BATH" -> {
                    binding.textTitle.text = "Bath"
                    binding.textTitle.setTextColor(ContextCompat.getColor(context, R.color.bath_orange))
                    binding.textCircle.setTextColor(ContextCompat.getColor(context, R.color.bath_orange))
                }
            }
            binding.textCaption.text = record.description
            var hour = record.startTime.hour
            if (hour < 12) binding.textAmpm.text = "AM" else binding.textAmpm.text = "PM"
            if (record.startTime.hour % 12 == 0) hour = 12 else hour = record.startTime.hour % 12
            binding.textTime.text = "${hour.toString().padStart(2, '0')}:${record.startTime.minute.toString().padStart(2, '0')}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = RecordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = dataList[position]
        holder.bind(record)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun getDataList(): List<Record> {
        return dataList
    }
}
