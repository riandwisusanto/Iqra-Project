package co.bayueka.iqra.mvvm.viewmodels

import androidx.lifecycle.*
import co.bayueka.iqra.mvvm.repositories.HijaiyahRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HijaiyahViewModel @Inject constructor(
    private val repository: HijaiyahRepository
): ViewModel() {

    val hijaiyah = repository.hijaiyah

    fun listHijaiyah(query: String) = viewModelScope.launch {
        repository.listHijaiyah(query)
    }
}