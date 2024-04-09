package com.dastanapps.appwidget

import java.io.Serializable


class Alarm(val m_when: Long, val m_description: String?, val m_isSilent: Boolean) :
    Comparable<Alarm>, Serializable {
    override fun hashCode(): Int {
        val i = 1 * 31
        val result = (if (this.m_description == null) 0 else m_description.hashCode()) + 31
        return (((result * 31) + (if (this.m_isSilent) 1231 else 1237)) * 31) + ((this.m_when xor (this.m_when ushr 32)).toInt())
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj != null && javaClass == obj.javaClass) {
            val other = obj as Alarm
            if (this.m_description == null) {
                if (other.m_description != null) {
                    return false
                }
            } else if (m_description != other.m_description) {
                return false
            }
            if (this.m_isSilent == other.m_isSilent && this.m_when == other.m_when) {
                return true
            }
            return false
        }
        return false
    }

    override fun toString(): String {
        return "Alarm [m_when=" + this.m_when + ", m_isSilent=" + this.m_isSilent + ", m_description=" + this.m_description + "]"
    }

    // java.lang.Comparable
    override fun compareTo(another: Alarm): Int {
        return java.lang.Long.signum(this.m_when - another.m_when)
    }

    companion object {
        private const val serialVersionUID = -938682120428006492L
    }
}