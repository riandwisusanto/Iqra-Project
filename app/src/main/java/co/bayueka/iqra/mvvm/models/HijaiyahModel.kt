package co.bayueka.iqra.mvvm.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HijaiyahModel(
    val img: Int,
    val title: String,
    val sound: Int = -1
): Parcelable {
    companion object {
        val NO_SOUND = -1
    }
}