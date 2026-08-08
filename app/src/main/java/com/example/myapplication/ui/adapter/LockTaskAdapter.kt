package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.Task

class LockTaskAdapter(
    private val onToggleCompletion: (Task) -> Unit = {}
) : RecyclerView.Adapter<LockTaskAdapter.LockViewHolder>() {

    private var tasks: List<Task> = emptyList()

    fun submitList(list: List<Task>) {
        this.tasks = list.filter { it.title.isNotBlank() }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = tasks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lock_task, parent, false)
        return LockViewHolder(view)
    }

    override fun onBindViewHolder(holder: LockViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(position, task)
    }

    inner class LockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskNumber: TextView = itemView.findViewById(R.id.taskNumber)
        private val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        private val taskTime: TextView = itemView.findViewById(R.id.taskTime)

        fun bind(position: Int, task: Task) {
            taskNumber.text = "${position + 1}."
            taskTitle.text = task.title

            // Show reminder time on the right side if set (No fake time when unset)
            if (!task.alarmTime.isNullOrBlank()) {
                taskTime.text = task.alarmTime
                taskTime.visibility = View.VISIBLE
            } else {
                taskTime.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onToggleCompletion(task)
            }
        }
    }
}
