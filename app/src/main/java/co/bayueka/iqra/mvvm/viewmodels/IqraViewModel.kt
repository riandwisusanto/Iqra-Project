package co.bayueka.iqra.mvvm.viewmodels

import androidx.lifecycle.*
import co.bayueka.iqra.mvvm.repositories.IqraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IqraViewModel @Inject constructor(
    private val repository: IqraRepository
): ViewModel() {

    val iqra = repository.iqras

    fun listIqra(iqra: Int) = viewModelScope.launch {
        repository.listIqra(iqra)
    }
}