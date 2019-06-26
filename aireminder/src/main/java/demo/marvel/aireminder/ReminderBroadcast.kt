package demo.marvel.aireminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dastanapps.dastanlib.NotificationB
import com.dastanapps.dastanlib.utils.CommonUtils

/**
 * Created by dastaniqbal on 20/06/2019.
 * 20/06/2019 10:38
 */
class ReminderBroadcast : BroadcastReceiver() {
    private val TAG = this::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.getBundleExtra("task")?.run {
            val name = this.getString("name", "no title")
            val id = this.getInt("id")

            val notificationB = NotificationB()
                    .id(id)
                    .cancelable(true)
                    .title(name)
                    .desc(name)
            CommonUtils.openNotification2(context!!, notificationB)
        }
    }
}