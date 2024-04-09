package com.dastanapps.appwidget

import java.io.Serializable

/* loaded from: classes.dex */
class Timer(
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val description: String?,
    val isSilent: Boolean
) : Serializable {
    override fun hashCode(): Int {
        val i = 1 * 31
        val result = (if (this.description == null) 0 else description.hashCode()) + 31
        return (((((result * 31) + this.hours) * 31) + this.minutes) * 31) + this.seconds
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj != null && javaClass == obj.javaClass) {
            val other = obj as Timer
            if (this.description == null) {
                if (other.description != null) {
                    return false
                }
            } else if (description != other.description) {
                return false
            }
            if (this.hours == other.hours && (this.minutes == other.minutes) && (this.seconds == other.seconds)) {
                return true
            }
            return false
        }
        return false
    }

    override fun toString(): String {
        return if (this.description != null) String.format(
            "%02d:%02d:%02d (%s)",
            this.hours,
            this.minutes,
            this.seconds,
            this.description
        ) else String.format("%02d:%02d:%02d", this.hours, this.minutes, this.seconds)
    }

    companion object {
        private const val serialVersionUID = 9045684051965285109L
    }
}