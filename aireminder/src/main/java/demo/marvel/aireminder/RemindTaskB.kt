package demo.marvel.aireminder

import android.os.Bundle
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by dastaniqbal on 20/06/2019.
 * 20/06/2019 10:27
 */
@Parcelize
data class RemindTaskB(var id:Int,var name:String,var time:Long) : Parcelable{

    fun bundle(): Bundle {
        val bundle = Bundle()
        bundle.putString("name", name)
        bundle.putLong("time", time)
        bundle.putInt("id", id)
        return bundle
    }
}