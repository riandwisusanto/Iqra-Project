package co.bayueka.iqra.mvvm.repositories

import androidx.lifecycle.MediatorLiveData
import co.bayueka.iqra.R
import co.bayueka.iqra.mvvm.models.HijaiyahModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class HijaiyahRepository @Inject constructor(): CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    val hijaiyah: MediatorLiveData<MutableList<HijaiyahModel>> = MediatorLiveData()

    suspend fun listHijaiyah(query: String) {
        val dataHijaiyah: MutableList<HijaiyahModel> = mutableListOf()
        dataHijaiyah.add(HijaiyahModel(R.drawable.jim, "Jim", R.raw.jim))
        dataHijaiyah.add(HijaiyahModel(R.drawable.tsa, "Tsa", R.raw.tsa))
        dataHijaiyah.add(HijaiyahModel(R.drawable.ta, "Ta", R.raw.ta))
        dataHijaiyah.add(HijaiyahModel(R.drawable.ba, "Ba", R.raw.ba))
        dataHijaiyah.add(HijaiyahModel(R.drawable.alif, "Alif", R.raw.alif))

        dataHijaiyah.add(HijaiyahModel(R.drawable.ra, "Ra", R.raw.ra))
        dataHijaiyah.add(HijaiyahModel(R.drawable.dzal, "Dzal", R.raw.dzal))
        dataHijaiyah.add(HijaiyahModel(R.drawable.dal, "Dal", R.raw.dal))
        dataHijaiyah.add(HijaiyahModel(R.drawable.kho, "Kho", R.raw.kho))
        dataHijaiyah.add(HijaiyahModel(R.drawable.kha, "Kha", R.raw.kha))

        dataHijaiyah.add(HijaiyahModel(R.drawable.dhod, "Dhod", R.raw.dhod))
        dataHijaiyah.add(HijaiyahModel(R.drawable.shod, "Shod", R.raw.shod))
        dataHijaiyah.add(HijaiyahModel(R.drawable.syin, "Syin", R.raw.syin))
        dataHijaiyah.add(HijaiyahModel(R.drawable.sin, "Sin", R.raw.sin))
        dataHijaiyah.add(HijaiyahModel(R.drawable.za, "Za", R.raw.za))

        dataHijaiyah.add(HijaiyahModel(R.drawable.fa, "Fa", R.raw.fa))
        dataHijaiyah.add(HijaiyahModel(R.drawable.ghoin, "Ghoin", R.raw.ghoin))
        dataHijaiyah.add(HijaiyahModel(R.drawable.ain, "Ain", R.raw.ain))
        dataHijaiyah.add(HijaiyahModel(R.drawable.dhlo, "Dhlo", R.raw.dhlo))
        dataHijaiyah.add(HijaiyahModel(R.drawable.tho, "Tho", R.raw.tho))

        dataHijaiyah.add(HijaiyahModel(R.drawable.nun, "Nun", R.raw.nun))
        dataHijaiyah.add(HijaiyahModel(R.drawable.mim, "Mim", R.raw.mim))
        dataHijaiyah.add(HijaiyahModel(R.drawable.lam, "Lam", R.raw.lam))
        dataHijaiyah.add(HijaiyahModel(R.drawable.kaf, "Kaf", R.raw.kaf))
        dataHijaiyah.add(HijaiyahModel(R.drawable.qof, "Qof", R.raw.qof))

        dataHijaiyah.add(HijaiyahModel(R.drawable.ya, "Ya", R.raw.ya))
        dataHijaiyah.add(HijaiyahModel(R.drawable.hamzah, "Hamzah", R.raw.hamzah))
        dataHijaiyah.add(HijaiyahModel(R.drawable.lam_alif, "Lam Alif", R.raw.lam_alif))
        dataHijaiyah.add(HijaiyahModel(R.drawable.ha, "Ha", R.raw.ha))
        dataHijaiyah.add(HijaiyahModel(R.drawable.wawu, "Wawu", R.raw.wawu))

        if (query.equals("")) {
            hijaiyah.postValue(dataHijaiyah)
        } else {
            val cpDataHijaiyah = dataHijaiyah
            dataHijaiyah.clear()
            cpDataHijaiyah.forEach {
                if (it.title.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))) {
                    dataHijaiyah.add(it)
                }
            }
            hijaiyah.postValue(dataHijaiyah)
        }
    }
}