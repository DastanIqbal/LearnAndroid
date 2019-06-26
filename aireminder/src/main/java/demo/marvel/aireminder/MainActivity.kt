package demo.marvel.aireminder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val taskList = ArrayList<RemindTaskB>()
                .apply {
                    add(RemindTaskB(1010, "Drink Water", 30))
                    add(RemindTaskB(1020, "Eat breakfast", 30))
                    add(RemindTaskB(1030, "Pick Laundry", 30))
                }

        rv.adapter = TaskAdapter(taskList)
    }
}
