package com.example.myapplication.ui.adapter

import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.Task
import java.util.Calendar
import java.util.Locale

class TaskAdapter(
    private val isReadOnly: Boolean = false,
    private val onSaveTask: (slotIndex: Int, existingTask: Task?, title: String) -> Unit,
    private val onDeleteTask: (task: Task) -> Unit,
    private val onSetAlarmTime: (task: Task, alarmTimeStr: String) -> Unit,
    private val onNextSlot: (currentSlotIndex: Int) -> Unit = {},
    private val onEditStarted: () -> Unit = {}
) : RecyclerView.Adapter<TaskAdapter.SlotViewHolder>() {

    private val slotTasks = arrayOfNulls<Task>(7)
    var inlineEditingSlotIndex: Int = -1

    fun updateTasks(tasks: List<Task>) {
        for (i in 0 until 7) {
            slotTasks[i] = null
        }
        tasks.forEachIndexed { index, task ->
            val slot = if (task.slotIndex in 0..6) task.slotIndex else index
            if (slot in 0..6 && slotTasks[slot] == null) {
                slotTasks[slot] = task
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = 7

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val task = slotTasks[position]
        holder.bind(position, task)
    }

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewModeContainer: View = itemView.findViewById(R.id.viewModeContainer)
        private val slotNumber: TextView = itemView.findViewById(R.id.slotNumber)
        private val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        private val taskTimeText: TextView = itemView.findViewById(R.id.taskTimeText)
        private val alarmBtn: ImageButton = itemView.findViewById(R.id.alarmBtn)
        private val menuBtn: ImageButton = itemView.findViewById(R.id.menuBtn)

        private val inlineEditContainer: View = itemView.findViewById(R.id.inlineEditContainer)
        private val editSlotNumber: TextView = itemView.findViewById(R.id.editSlotNumber)
        private val editTaskTitle: EditText = itemView.findViewById(R.id.editTaskTitle)
        private val saveInlineBtn: ImageButton = itemView.findViewById(R.id.saveInlineBtn)

        fun bind(slotIndex: Int, task: Task?) {
            val slotNumStr = "${slotIndex + 1}."
            slotNumber.text = slotNumStr
            editSlotNumber.text = slotNumStr

            val isEditing = (!isReadOnly && inlineEditingSlotIndex == slotIndex)

            if (isEditing) {
                viewModeContainer.visibility = View.GONE
                inlineEditContainer.visibility = View.VISIBLE

                if (task != null && task.title.isNotBlank()) {
                    editTaskTitle.setText(task.title)
                } else {
                    editTaskTitle.setText("")
                }

                editTaskTitle.requestFocus()
                editTaskTitle.setSelection(editTaskTitle.text.length)

                // Keyboard Next / Enter Action -> Auto move to next task row
                editTaskTitle.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                        val titleStr = editTaskTitle.text.toString().trim()
                        if (titleStr.isNotEmpty()) {
                            onSaveTask(slotIndex, task, titleStr)
                        }
                        if (slotIndex < 6) {
                            onNextSlot(slotIndex)
                        } else {
                            inlineEditingSlotIndex = -1
                            notifyDataSetChanged()
                        }
                        true
                    } else {
                        false
                    }
                }

                saveInlineBtn.setOnClickListener {
                    val titleStr = editTaskTitle.text.toString().trim()
                    if (titleStr.isNotEmpty()) {
                        onSaveTask(slotIndex, task, titleStr)
                        inlineEditingSlotIndex = -1
                        notifyDataSetChanged()
                    } else {
                        editTaskTitle.error = "Title cannot be empty"
                    }
                }

            } else {
                viewModeContainer.visibility = View.VISIBLE
                inlineEditContainer.visibility = View.GONE

                if (task != null && task.title.isNotBlank()) {
                    taskTitle.text = task.title

                    // Alarm Time Display (No fake time when unset)
                    if (!task.alarmTime.isNullOrBlank()) {
                        taskTimeText.text = task.alarmTime
                        taskTimeText.visibility = View.VISIBLE
                        alarmBtn.setColorFilter(0xFF3F5AA9.toInt())
                    } else {
                        taskTimeText.visibility = View.GONE
                        alarmBtn.setColorFilter(0xFF9E9E9E.toInt())
                    }
                } else {
                    // Empty Slot -> Visually Blank (No "Add Task" text)
                    taskTitle.text = ""
                    taskTimeText.visibility = View.GONE
                    alarmBtn.setColorFilter(0xFF9E9E9E.toInt())
                }

                if (isReadOnly) {
                    alarmBtn.visibility = View.GONE
                    menuBtn.visibility = View.GONE
                    itemView.setOnClickListener(null)
                } else {
                    alarmBtn.visibility = View.VISIBLE
                    menuBtn.visibility = View.VISIBLE

                    // Tapping row opens inline editing
                    itemView.setOnClickListener {
                        inlineEditingSlotIndex = slotIndex
                        onEditStarted()
                        notifyDataSetChanged()
                    }

                    // Alarm Icon Click Handler
                    alarmBtn.setOnClickListener {
                        if (task == null || task.title.isBlank()) {
                            inlineEditingSlotIndex = slotIndex
                            onEditStarted()
                            notifyDataSetChanged()
                            Toast.makeText(itemView.context, "Type task title first", Toast.LENGTH_SHORT).show()
                        } else {
                            showTimePicker(itemView.context, task)
                        }
                    }

                    // Three-dot Menu Click Handler (Edit & Delete only)
                    menuBtn.setOnClickListener { view ->
                        val popup = PopupMenu(itemView.context, view)
                        popup.menu.add("Edit")
                        if (task != null && task.title.isNotBlank()) {
                            popup.menu.add("Delete")
                        }
                        popup.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.title) {
                                "Edit" -> {
                                    inlineEditingSlotIndex = slotIndex
                                    onEditStarted()
                                    notifyDataSetChanged()
                                    true
                                }
                                "Delete" -> {
                                    if (task != null) {
                                        onDeleteTask(task)
                                    }
                                    true
                                }
                                else -> false
                            }
                        }
                        popup.show()
                    }
                }
            }
        }

        private fun showTimePicker(context: Context, task: Task) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    val amPm = if (selectedHour >= 12) "PM" else "AM"
                    val hour12 = when {
                        selectedHour == 0 -> 12
                        selectedHour > 12 -> selectedHour - 12
                        else -> selectedHour
                    }
                    val formattedTime = String.format(Locale.getDefault(), "%d:%02d %s", hour12, selectedMinute, amPm)
                    onSetAlarmTime(task, formattedTime)
                },
                hour,
                minute,
                false
            )
            timePickerDialog.show()
        }
    }
}
