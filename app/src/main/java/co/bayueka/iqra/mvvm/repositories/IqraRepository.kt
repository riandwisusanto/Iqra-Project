package co.bayueka.iqra.mvvm.repositories

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import co.bayueka.iqra.R
import co.bayueka.iqra.mvvm.models.IqraModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class IqraRepository @Inject constructor(
    private val context: Context
): CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    val iqras: MediatorLiveData<MutableList<IqraModel>> = MediatorLiveData()

    suspend fun listIqra(iqra: Int) {
        val imgArray = when (iqra) {
            1 -> {
                context.resources.obtainTypedArray(R.array.iqra1)
            }
            2 -> {
                context.resources.obtainTypedArray(R.array.iqra2)
            }
            3 -> {
                context.resources.obtainTypedArray(R.array.iqra3)
            }
            4 -> {
                context.resources.obtainTypedArray(R.array.iqra4)
            }
            5 -> {
                context.resources.obtainTypedArray(R.array.iqra5)
            }
            else -> {
                context.resources.obtainTypedArray(R.array.iqra6)
            }
        }

        val dataIqra: MutableList<IqraModel> = mutableListOf()
        dataIqra.add(IqraModel("bismillah", -1))
        for (i in 0 until imgArray.length()) {
            val hal = i + 1
            val convertHalaman = if (hal < 10) {
                "0$hal"
            } else {
                hal.toString()
            }
            dataIqra.add(IqraModel(convertHalaman, imgArray.getResourceId(imgArray.getIndex(i), -1)))
        }

        imgArray.recycle()

        iqras.postValue(dataIqra)
    }


}