package com.dastanapps.flexbox

import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass


/**
 *
 * Iqbal created on 19/05/2023
 */

data class Dummy(val id: Int)

@EpoxyModelClass
abstract class DummyEpoxyModel : BaseViewHolder<DummyEpoxyModel.DummyEpoxyHolder>() {

    @EpoxyAttribute
    var _id: Int = -1

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var onDragHandleTouchListener: View.OnTouchListener? = null

    override fun getDefaultLayout(): Int {
        return R.layout.home_epoxy_dummy
    }

    override fun bind(holder: DummyEpoxyHolder) {
        super.bind(holder)

        holder.text.text = _id.toString()
        holder.root.setOnTouchListener(onDragHandleTouchListener)
    }


    override fun unbind(holder: DummyEpoxyHolder) {
        with(holder) {
            root.setOnTouchListener(onDragHandleTouchListener)
        }
    }

    class DummyEpoxyHolder : BaseEpoxyHolder() {
        val root by bind<View>(R.id.fl)
        val text by bind<TextView>(R.id.tv)
    }

    companion object {
        fun mapTo(
            animate: Boolean = false,
            dummyList: List<Dummy>,
            callback: View.OnTouchListener
        ): MutableList<DummyEpoxyModel_> {
            return mutableListOf<DummyEpoxyModel_>().apply {
                dummyList.forEachIndexed { index, dummy ->
                    add(
                        DummyEpoxyModel_()
                            .id("dummy", dummy.id.toLong())
                            ._id(dummy.id)
                            .doAnimation(animate)
                            .onDragHandleTouchListener(callback)
                    )
                }
            }
        }
    }
}