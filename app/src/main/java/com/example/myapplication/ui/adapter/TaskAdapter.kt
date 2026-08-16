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
import com.example.myapplication.utils.TaskCategoryHelper
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

    private var activeTaskList: MutableList<Task> = mutableListOf()
    var inlineEditingSlotIndex: Int = -1

    fun updateTasks(tasks: List<Task>) {
        activeTaskList = tasks.filter { it.title.isNotBlank() }.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        val activeCount = activeTaskList.size
        return if (inlineEditingSlotIndex >= 0) {
            maxOf(activeCount, inlineEditingSlotIndex + 1)
        } else {
            activeCount
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val task = if (position < activeTaskList.size) activeTaskList[position] else null
        holder.bind(position, task)
    }

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewModeContainer: View = itemView.findViewById(R.id.viewModeContainer)
        private val slotNumber: TextView = itemView.findViewById(R.id.slotNumber)
        private val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        private val taskTimeText: TextView = itemView.findViewById(R.id.taskTimeText)
        private val taskEmojiIcon: TextView? = itemView.findViewById(R.id.taskEmojiIcon)
        private val statusDot: View? = itemView.findViewById(R.id.statusDot)
        private val alarmBtn: ImageButton? = itemView.findViewById(R.id.alarmBtn)
        private val menuBtn: ImageButton? = itemView.findViewById(R.id.menuBtn)

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

                editTaskTitle.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                        val titleStr = editTaskTitle.text.toString().trim()
                        if (titleStr.isNotEmpty()) {
                            onSaveTask(slotIndex, task, titleStr)
                        }
                        inlineEditingSlotIndex = -1
                        notifyDataSetChanged()
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
                    slotNumber.visibility = View.VISIBLE
                    taskTitle.text = task.title

                    // Intelligently assign smart category emoji icon
                    taskEmojiIcon?.text = TaskCategoryHelper.getCategoryIcon(task.title)
                    taskEmojiIcon?.visibility = View.VISIBLE
                    statusDot?.visibility = View.VISIBLE

                    if (!task.alarmTime.isNullOrBlank()) {
                        taskTimeText.text = task.alarmTime
                        taskTimeText.visibility = View.VISIBLE
                    } else {
                        taskTimeText.visibility = View.GONE
                    }
                } else {
                    slotNumber.visibility = View.GONE
                    taskTitle.text = ""
                    taskTimeText.visibility = View.GONE
                    taskEmojiIcon?.visibility = View.GONE
                    statusDot?.visibility = View.GONE
                }

                if (isReadOnly) {
                    alarmBtn?.visibility = View.GONE
                    menuBtn?.visibility = View.GONE
                    itemView.setOnClickListener(null)
                } else {
                    itemView.setOnClickListener {
                        inlineEditingSlotIndex = slotIndex
                        onEditStarted()
                        notifyDataSetChanged()
                    }

                    menuBtn?.setOnClickListener { view ->
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
    }
}
