package demo.marvel.aireminder

import android.app.PendingIntent
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dastanapps.dastanlib.utils.AlarmUtils
import com.dastanapps.dastanlib.utils.ViewUtils
import java.util.*

/**
 * Created by dastaniqbal on 20/06/2019.
 * 20/06/2019 10:28
 */
class TaskAdapter(val taskList: ArrayList<RemindTaskB>) : RecyclerView.Adapter<TaskAdapter.VHolder>() {
    private val TAG = this::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHolder {
        return VHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false))
    }

    override fun getItemCount(): Int = taskList.size

    override fun onBindViewHolder(holder: VHolder, position: Int) {
        val taskB = taskList[position]
        holder.bind(taskB)
    }

    inner class VHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(taskB: RemindTaskB) {
            val tvTask = view.findViewById<TextView>(R.id.tv_task)
            val tvRemind = view.findViewById<TextView>(R.id.btn_remind)
            val tvCancel = view.findViewById<TextView>(R.id.btn_cancel)
            val context = view.context

            tvTask.text = taskB.name
            val intent = Intent(context, ReminderBroadcast::class.java)
            intent.putExtra("task", taskB.bundle())
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)

            val pendingIntent = PendingIntent.getBroadcast(context, taskB.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)


            tvRemind.setOnClickListener {
                AlarmUtils.scheduleExactRepeatingEvent(view.context, taskB.time, pendingIntent)
                ViewUtils.showToast(it.context, "${taskB.name} Reminder Set")
            }

            tvCancel.setOnClickListener {
                AlarmUtils.cancelEvent(view.context, pendingIntent)
                ViewUtils.showToast(it.context, "${taskB.name} Cancel")
            }

        }
    }
}