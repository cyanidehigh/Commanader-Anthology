package com.commanderanalyst.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.commanderanalyst.data.ContainerRepository
import com.commanderanalyst.domain.model.Container
import com.commanderanalyst.domain.model.ContainerType
import com.commanderanalyst.domain.model.ManualInventoryCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val repository: ContainerRepository
) : ViewModel() {
    val containers: StateFlow<List<Container>> = repository.containers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cardCountsByContainer: StateFlow<Map<String, Int>> = repository.cardCountsByContainer
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val selectedContainerId = MutableStateFlow<String?>(null)

    val selectedCards: StateFlow<List<ManualInventoryCard>> = selectedContainerId
        .flatMapLatest { containerId ->
            if (containerId == null) emptyFlow() else repository.observeCards(containerId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createContainer(name: String, type: ContainerType) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return

        viewModelScope.launch {
            repository.createContainer(cleanName, type)
        }
    }

    fun updateContainer(containerId: String, name: String, type: ContainerType) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return

        viewModelScope.launch {
            repository.updateContainer(containerId, cleanName, type)
        }
    }

    fun deleteContainer(containerId: String) {
        viewModelScope.launch {
            repository.deleteContainer(containerId)
            if (selectedContainerId.value == containerId) {
                selectedContainerId.value = null
            }
        }
    }

    fun selectContainer(containerId: String?) {
        selectedContainerId.value = containerId
    }

    fun addManualCard(containerId: String, cardName: String, quantity: Int) {
        val cleanName = cardName.trim()
        if (cleanName.isEmpty() || quantity < 1) return

        viewModelScope.launch {
            repository.addManualCard(containerId, cleanName, quantity)
        }
    }

    fun updateManualCard(cardId: String, cardName: String, quantity: Int) {
        val cleanName = cardName.trim()
        if (cleanName.isEmpty() || quantity < 1) return

        viewModelScope.launch {
            repository.updateManualCard(cardId, cleanName, quantity)
        }
    }

    fun deleteManualCard(cardId: String) {
        viewModelScope.launch {
            repository.deleteManualCard(cardId)
        }
    }
}

class CollectionViewModelFactory(
    private val repository: ContainerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollectionViewModel::class.java)) {
            return CollectionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
