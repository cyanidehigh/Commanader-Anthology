package com.commanderanalyst.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.commanderanalyst.data.DeckRepository
import com.commanderanalyst.domain.model.Deck
import com.commanderanalyst.domain.model.DeckSection
import com.commanderanalyst.domain.model.DeckSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeckViewModel(
    private val repository: DeckRepository
) : ViewModel() {
    val decks: StateFlow<List<Deck>> = repository.decks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val selectedDeckId = MutableStateFlow<String?>(null)

    val selectedSlots: StateFlow<List<DeckSlot>> = selectedDeckId
        .flatMapLatest { deckId ->
            if (deckId == null) emptyFlow() else repository.observeSlots(deckId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDeck(deckId: String?) {
        selectedDeckId.value = deckId
    }

    fun createDeck(name: String, commanderName: String?) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return

        viewModelScope.launch {
            repository.createDeck(cleanName, commanderName.cleanOptional())
        }
    }

    fun updateDeck(deckId: String, name: String, commanderName: String?) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return

        viewModelScope.launch {
            repository.updateDeck(deckId, cleanName, commanderName.cleanOptional())
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            repository.deleteDeck(deckId)
            if (selectedDeckId.value == deckId) {
                selectedDeckId.value = null
            }
        }
    }

    fun addSlot(deckId: String, cardName: String, quantity: Int, section: DeckSection) {
        val cleanName = cardName.trim()
        if (cleanName.isEmpty() || quantity < 1) return

        viewModelScope.launch {
            repository.addSlot(deckId, cleanName, quantity, section)
        }
    }

    fun updateSlot(slotId: String, cardName: String, quantity: Int, section: DeckSection) {
        val cleanName = cardName.trim()
        if (cleanName.isEmpty() || quantity < 1) return

        viewModelScope.launch {
            repository.updateSlot(slotId, cleanName, quantity, section)
        }
    }

    fun deleteSlot(slotId: String) {
        viewModelScope.launch {
            repository.deleteSlot(slotId)
        }
    }
}

class DeckViewModelFactory(
    private val repository: DeckRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeckViewModel::class.java)) {
            return DeckViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun String?.cleanOptional(): String? {
    return this?.trim()?.takeIf { it.isNotEmpty() }
}

