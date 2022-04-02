package co.bayueka.iqra.mvvm.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IqraModel(
    val halaman: String,
    var img: Int
): Parcelable