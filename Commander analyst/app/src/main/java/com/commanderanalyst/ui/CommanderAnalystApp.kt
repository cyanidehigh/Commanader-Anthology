package com.commanderanalyst.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.CircleShape
import com.commanderanalyst.R
import com.commanderanalyst.data.ContainerRepository
import com.commanderanalyst.data.DeckRepository
import com.commanderanalyst.data.local.CommanderAnalystDatabase
import com.commanderanalyst.domain.model.Container
import com.commanderanalyst.domain.model.ContainerType
import com.commanderanalyst.domain.model.Deck
import com.commanderanalyst.domain.model.DeckSection
import com.commanderanalyst.domain.model.DeckSlot
import com.commanderanalyst.domain.model.ManualInventoryCard

private enum class AppTab(val label: String) {
    Decks("Decks"),
    Collection("Collection"),
    Search("Search"),
    Settings("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommanderAnalystApp() {
    var currentTab by rememberSaveable { mutableStateOf(AppTab.Decks) }
    val context = LocalContext.current
    val collectionViewModel: CollectionViewModel = viewModel(
        factory = remember(context) {
            val database = CommanderAnalystDatabase.getInstance(context)
            CollectionViewModelFactory(
                ContainerRepository(
                    containerDao = database.containerDao(),
                    inventoryDao = database.inventoryDao()
                )
            )
        }
    )
    val deckViewModel: DeckViewModel = viewModel(
        factory = remember(context) {
            val database = CommanderAnalystDatabase.getInstance(context)
            DeckViewModelFactory(
                DeckRepository(
                    deckDao = database.deckDao(),
                    deckSlotDao = database.deckSlotDao()
                )
            )
        }
    )
    val containers by collectionViewModel.containers.collectAsState()
    val cardCountsByContainer by collectionViewModel.cardCountsByContainer.collectAsState()
    val selectedCards by collectionViewModel.selectedCards.collectAsState()
    val decks by deckViewModel.decks.collectAsState()
    val selectedDeckSlots by deckViewModel.selectedSlots.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.cynful_logo),
                                contentDescription = "Cynful Studio",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column {
                            Text("Commander Analyst", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Cynful deck and collection tooling",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {},
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (currentTab) {
            AppTab.Decks -> DecksScreen(
                padding = padding,
                decks = decks,
                selectedSlots = selectedDeckSlots,
                onCreateDeck = deckViewModel::createDeck,
                onUpdateDeck = deckViewModel::updateDeck,
                onDeleteDeck = deckViewModel::deleteDeck,
                onSelectDeck = deckViewModel::selectDeck,
                onAddSlot = deckViewModel::addSlot,
                onUpdateSlot = deckViewModel::updateSlot,
                onDeleteSlot = deckViewModel::deleteSlot
            )
            AppTab.Collection -> CollectionScreen(
                padding = padding,
                containers = containers,
                cardCountsByContainer = cardCountsByContainer,
                selectedCards = selectedCards,
                onCreateContainer = collectionViewModel::createContainer,
                onUpdateContainer = collectionViewModel::updateContainer,
                onDeleteContainer = collectionViewModel::deleteContainer,
                onSelectContainer = collectionViewModel::selectContainer,
                onAddManualCard = collectionViewModel::addManualCard,
                onUpdateManualCard = collectionViewModel::updateManualCard,
                onDeleteManualCard = collectionViewModel::deleteManualCard
            )
            AppTab.Search -> SearchScreen(padding)
            AppTab.Settings -> SettingsScreen(padding)
        }
    }
}

@Composable
private fun DecksScreen(
    padding: PaddingValues,
    decks: List<Deck>,
    selectedSlots: List<DeckSlot>,
    onCreateDeck: (String, String?) -> Unit,
    onUpdateDeck: (String, String, String?) -> Unit,
    onDeleteDeck: (String) -> Unit,
    onSelectDeck: (String?) -> Unit,
    onAddSlot: (String, String, Int, DeckSection) -> Unit,
    onUpdateSlot: (String, String, Int, DeckSection) -> Unit,
    onDeleteSlot: (String) -> Unit
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var selectedDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddSlotDialog by rememberSaveable { mutableStateOf(false) }
    var editingDeck by remember { mutableStateOf<Deck?>(null) }
    var deletingDeck by remember { mutableStateOf<Deck?>(null) }
    var editingSlot by remember { mutableStateOf<DeckSlot?>(null) }
    var deletingSlot by remember { mutableStateOf<DeckSlot?>(null) }
    val selectedDeck = decks.firstOrNull { it.id == selectedDeckId }

    if (selectedDeck == null) {
        ScreenColumn(padding) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Decks", style = MaterialTheme.typography.headlineMedium)
                    Text("Build intent first. Physical assignment comes after.")
                }
                Button(onClick = { showCreateDialog = true }) {
                    Text("New")
                }
            }
            Spacer(Modifier.height(12.dp))

            if (decks.isEmpty()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("No decks yet", style = MaterialTheme.typography.titleMedium)
                        Text("Create a deck shell, add the commander, then start adding intended cards.")
                        OutlinedButton(onClick = { showCreateDialog = true }) {
                            Text("Create first deck")
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(decks, key = { it.id }) { deck ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = deck.name,
                                    modifier = Modifier.clickable {
                                        selectedDeckId = deck.id
                                        onSelectDeck(deck.id)
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    deck.commanderName?.let { "Commander: $it" } ?: "Commander not set",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { editingDeck = deck }) {
                                        Text("Edit")
                                    }
                                    TextButton(onClick = { deletingDeck = deck }) {
                                        Text("Delete")
                                    }
                                    TextButton(
                                        onClick = {
                                            selectedDeckId = deck.id
                                            onSelectDeck(deck.id)
                                        }
                                    ) {
                                        Text("Open")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        DeckDetailScreen(
            padding = padding,
            deck = selectedDeck,
            slots = selectedSlots,
            onBack = {
                selectedDeckId = null
                onSelectDeck(null)
            },
            onAddSlot = { showAddSlotDialog = true },
            onEditDeck = { editingDeck = selectedDeck },
            onDeleteDeck = { deletingDeck = selectedDeck },
            onEditSlot = { editingSlot = it },
            onDeleteSlot = { deletingSlot = it }
        )
    }

    if (showCreateDialog) {
        DeckDialog(
            title = "New deck",
            confirmLabel = "Create",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, commanderName ->
                onCreateDeck(name, commanderName)
                showCreateDialog = false
            }
        )
    }

    editingDeck?.let { deck ->
        DeckDialog(
            title = "Edit deck",
            confirmLabel = "Save",
            initialName = deck.name,
            initialCommanderName = deck.commanderName.orEmpty(),
            onDismiss = { editingDeck = null },
            onConfirm = { name, commanderName ->
                onUpdateDeck(deck.id, name, commanderName)
                editingDeck = null
            }
        )
    }

    deletingDeck?.let { deck ->
        ConfirmDeleteDialog(
            title = "Delete deck",
            body = "Delete ${deck.name}? Intended deck rows will be removed too.",
            onDismiss = { deletingDeck = null },
            onDelete = {
                onDeleteDeck(deck.id)
                if (selectedDeckId == deck.id) {
                    selectedDeckId = null
                    onSelectDeck(null)
                }
                deletingDeck = null
            }
        )
    }

    if (showAddSlotDialog && selectedDeck != null) {
        DeckSlotDialog(
            title = "Add deck card",
            confirmLabel = "Add",
            onDismiss = { showAddSlotDialog = false },
            onConfirm = { cardName, quantity, section ->
                onAddSlot(selectedDeck.id, cardName, quantity, section)
                showAddSlotDialog = false
            }
        )
    }

    editingSlot?.let { slot ->
        DeckSlotDialog(
            title = "Edit deck card",
            confirmLabel = "Save",
            initialName = slot.cardName,
            initialQuantity = slot.desiredQuantity,
            initialSection = slot.section,
            onDismiss = { editingSlot = null },
            onConfirm = { cardName, quantity, section ->
                onUpdateSlot(slot.id, cardName, quantity, section)
                editingSlot = null
            }
        )
    }

    deletingSlot?.let { slot ->
        ConfirmDeleteDialog(
            title = "Delete deck card",
            body = "Remove ${slot.cardName} from this decklist?",
            onDismiss = { deletingSlot = null },
            onDelete = {
                onDeleteSlot(slot.id)
                deletingSlot = null
            }
        )
    }
}

@Composable
private fun DeckDetailScreen(
    padding: PaddingValues,
    deck: Deck,
    slots: List<DeckSlot>,
    onBack: () -> Unit,
    onAddSlot: () -> Unit,
    onEditDeck: () -> Unit,
    onDeleteDeck: () -> Unit,
    onEditSlot: (DeckSlot) -> Unit,
    onDeleteSlot: (DeckSlot) -> Unit
) {
    ScreenColumn(padding) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(deck.name, style = MaterialTheme.typography.headlineMedium)
                Text(deck.commanderName?.let { "Commander: $it" } ?: "Commander not set")
            }
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AssistChip(onClick = {}, label = { Text("${slots.sumOf { it.desiredQuantity }} cards") })
                Button(onClick = onAddSlot) {
                    Text("Add card")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onEditDeck) {
                    Text("Edit deck")
                }
                TextButton(onClick = onDeleteDeck) {
                    Text("Delete")
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (slots.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("No cards in this decklist", style = MaterialTheme.typography.titleMedium)
                    Text("Add intended cards manually first. Import and collection matching come after this loop.")
                    OutlinedButton(onClick = onAddSlot) {
                        Text("Add first card")
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(slots, key = { it.id }) { slot ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column {
                                Text(slot.cardName, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "x${slot.desiredQuantity} - ${slot.section.displayName()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { onEditSlot(slot) }) {
                                    Text("Edit")
                                }
                                TextButton(onClick = { onDeleteSlot(slot) }) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeckDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    initialCommanderName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var commanderName by rememberSaveable(initialCommanderName) { mutableStateOf(initialCommanderName) }
    val cleanName = name.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Deck name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = commanderName,
                    onValueChange = { commanderName = it },
                    label = { Text("Commander") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = cleanName.isNotEmpty(),
                onClick = { onConfirm(cleanName, commanderName.trim().takeIf { it.isNotEmpty() }) }
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DeckSlotDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    initialQuantity: Int = 1,
    initialSection: DeckSection = DeckSection.Other,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, DeckSection) -> Unit
) {
    var cardName by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var quantityText by rememberSaveable(initialQuantity) { mutableStateOf(initialQuantity.toString()) }
    var selectedSection by rememberSaveable(initialSection) { mutableStateOf(initialSection) }
    val cleanName = cardName.trim()
    val quantity = quantityText.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("Card name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { value -> quantityText = value.filter { it.isDigit() }.take(3) },
                    label = { Text("Quantity") },
                    singleLine = true
                )
                Text("Section", style = MaterialTheme.typography.labelLarge)
                deckSectionRows().forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { section ->
                            FilterChip(
                                selected = selectedSection == section,
                                onClick = { selectedSection = section },
                                label = { Text(section.displayName()) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = cleanName.isNotEmpty() && quantity > 0,
                onClick = { onConfirm(cleanName, quantity, selectedSection) }
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun deckSectionRows(): List<List<DeckSection>> {
    return listOf(
        listOf(DeckSection.Commander, DeckSection.Creature, DeckSection.Artifact),
        listOf(DeckSection.Enchantment, DeckSection.Instant, DeckSection.Sorcery),
        listOf(DeckSection.Planeswalker, DeckSection.Battle, DeckSection.Land),
        listOf(DeckSection.Other)
    )
}

private fun DeckSection.displayName(): String {
    return when (this) {
        DeckSection.Commander -> "Commander"
        DeckSection.Creature -> "Creature"
        DeckSection.Artifact -> "Artifact"
        DeckSection.Enchantment -> "Enchant"
        DeckSection.Instant -> "Instant"
        DeckSection.Sorcery -> "Sorcery"
        DeckSection.Planeswalker -> "Walker"
        DeckSection.Battle -> "Battle"
        DeckSection.Land -> "Land"
        DeckSection.Other -> "Other"
    }
}

@Composable
private fun CollectionScreen(
    padding: PaddingValues,
    containers: List<Container>,
    cardCountsByContainer: Map<String, Int>,
    selectedCards: List<ManualInventoryCard>,
    onCreateContainer: (String, ContainerType) -> Unit,
    onUpdateContainer: (String, String, ContainerType) -> Unit,
    onDeleteContainer: (String) -> Unit,
    onSelectContainer: (String?) -> Unit,
    onAddManualCard: (String, String, Int) -> Unit,
    onUpdateManualCard: (String, String, Int) -> Unit,
    onDeleteManualCard: (String) -> Unit
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var selectedContainerId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddCardDialog by rememberSaveable { mutableStateOf(false) }
    var editingContainer by remember { mutableStateOf<Container?>(null) }
    var deletingContainer by remember { mutableStateOf<Container?>(null) }
    var editingCard by remember { mutableStateOf<ManualInventoryCard?>(null) }
    var deletingCard by remember { mutableStateOf<ManualInventoryCard?>(null) }
    val selectedContainer = containers.firstOrNull { it.id == selectedContainerId }

    if (selectedContainer == null) {
        ScreenColumn(padding) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Collection", style = MaterialTheme.typography.headlineMedium)
                    Text("Create the places your cards actually live.")
                }
                Button(onClick = { showCreateDialog = true }) {
                    Text("New")
                }
            }
            Spacer(Modifier.height(12.dp))

            if (containers.isEmpty()) {
                EmptyCollectionCard(onCreate = { showCreateDialog = true })
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(containers, key = { it.id }) { container ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = container.name,
                                    modifier = Modifier.clickable {
                                        selectedContainerId = container.id
                                        onSelectContainer(container.id)
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(container.type.displayName(), style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${cardCountsByContainer[container.id] ?: 0} cards",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { editingContainer = container }) {
                                        Text("Edit")
                                    }
                                    TextButton(onClick = { deletingContainer = container }) {
                                        Text("Delete")
                                    }
                                    TextButton(
                                        onClick = {
                                            selectedContainerId = container.id
                                            onSelectContainer(container.id)
                                        }
                                    ) {
                                        Text("Open")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        ContainerDetailScreen(
            padding = padding,
            container = selectedContainer,
            cards = selectedCards,
            onBack = {
                selectedContainerId = null
                onSelectContainer(null)
            },
            onAddCard = { showAddCardDialog = true },
            onEditContainer = { editingContainer = selectedContainer },
            onDeleteContainer = { deletingContainer = selectedContainer },
            onEditCard = { editingCard = it },
            onDeleteCard = { deletingCard = it }
        )
    }

    if (showCreateDialog) {
        ContainerDialog(
            title = "New container",
            confirmLabel = "Create",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, type ->
                onCreateContainer(name, type)
                showCreateDialog = false
            }
        )
    }

    editingContainer?.let { container ->
        ContainerDialog(
            title = "Edit container",
            confirmLabel = "Save",
            initialName = container.name,
            initialType = container.type,
            onDismiss = { editingContainer = null },
            onConfirm = { name, type ->
                onUpdateContainer(container.id, name, type)
                editingContainer = null
            }
        )
    }

    deletingContainer?.let { container ->
        ConfirmDeleteDialog(
            title = "Delete container",
            body = "Delete ${container.name}? Cards inside this container will be removed too.",
            onDismiss = { deletingContainer = null },
            onDelete = {
                onDeleteContainer(container.id)
                if (selectedContainerId == container.id) {
                    selectedContainerId = null
                    onSelectContainer(null)
                }
                deletingContainer = null
            }
        )
    }

    if (showAddCardDialog && selectedContainer != null) {
        ManualCardDialog(
            title = "Add card",
            confirmLabel = "Add",
            containerName = selectedContainer.name,
            onDismiss = { showAddCardDialog = false },
            onConfirm = { cardName, quantity ->
                onAddManualCard(selectedContainer.id, cardName, quantity)
                showAddCardDialog = false
            }
        )
    }

    editingCard?.let { card ->
        ManualCardDialog(
            title = "Edit card",
            confirmLabel = "Save",
            containerName = selectedContainer?.name.orEmpty(),
            initialName = card.name,
            initialQuantity = card.quantity,
            onDismiss = { editingCard = null },
            onConfirm = { cardName, quantity ->
                onUpdateManualCard(card.id, cardName, quantity)
                editingCard = null
            }
        )
    }

    deletingCard?.let { card ->
        ConfirmDeleteDialog(
            title = "Delete card",
            body = "Remove ${card.name} from this container?",
            onDismiss = { deletingCard = null },
            onDelete = {
                onDeleteManualCard(card.id)
                deletingCard = null
            }
        )
    }
}

@Composable
private fun EmptyCollectionCard(onCreate: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("No containers yet", style = MaterialTheme.typography.titleMedium)
            Text("Make a binder, box, set folder, deck, proxy pile, or anything else you physically use.")
            OutlinedButton(onClick = onCreate) {
                Text("Create first container")
            }
        }
    }
}

@Composable
private fun ContainerDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    initialType: ContainerType = ContainerType.Box,
    onDismiss: () -> Unit,
    onConfirm: (String, ContainerType) -> Unit
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var selectedType by rememberSaveable(initialType) { mutableStateOf(initialType) }
    val cleanName = name.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                Text("Type", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ContainerType.Binder, ContainerType.Box, ContainerType.Set).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName()) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ContainerType.Deck, ContainerType.Proxy, ContainerType.Other).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = cleanName.isNotEmpty(),
                onClick = { onConfirm(cleanName, selectedType) }
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ContainerDetailScreen(
    padding: PaddingValues,
    container: Container,
    cards: List<ManualInventoryCard>,
    onBack: () -> Unit,
    onAddCard: () -> Unit,
    onEditContainer: () -> Unit,
    onDeleteContainer: () -> Unit,
    onEditCard: (ManualInventoryCard) -> Unit,
    onDeleteCard: (ManualInventoryCard) -> Unit
) {
    ScreenColumn(padding) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(container.name, style = MaterialTheme.typography.headlineMedium)
                Text(container.type.displayName())
            }
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AssistChip(onClick = {}, label = { Text("${cards.sumOf { it.quantity }} cards") })
                Button(onClick = onAddCard) {
                    Text("Add card")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onEditContainer) {
                    Text("Edit container")
                }
                TextButton(onClick = onDeleteContainer) {
                    Text("Delete")
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (cards.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("No cards in this container", style = MaterialTheme.typography.titleMedium)
                    Text("Add cards manually first. Scryfall matching comes after the inventory loop is solid.")
                    OutlinedButton(onClick = onAddCard) {
                        Text("Add first card")
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(cards, key = { it.id }) { card ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column {
                                Text(card.name, fontWeight = FontWeight.SemiBold)
                                Text("x${card.quantity}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { onEditCard(card) }) {
                                    Text("Edit")
                                }
                                TextButton(onClick = { onDeleteCard(card) }) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualCardDialog(
    title: String,
    confirmLabel: String,
    containerName: String,
    initialName: String = "",
    initialQuantity: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var cardName by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var quantityText by rememberSaveable(initialQuantity) { mutableStateOf(initialQuantity.toString()) }
    val cleanName = cardName.trim()
    val quantity = quantityText.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(containerName, style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("Card name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { value -> quantityText = value.filter { it.isDigit() }.take(3) },
                    label = { Text("Quantity") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = cleanName.isNotEmpty() && quantity > 0,
                onClick = { onConfirm(cleanName, quantity) }
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun ContainerType.displayName(): String {
    return when (this) {
        ContainerType.Set -> "Set"
        ContainerType.Binder -> "Binder"
        ContainerType.Box -> "Box"
        ContainerType.Deck -> "Deck"
        ContainerType.Ordered -> "Ordered"
        ContainerType.Proxy -> "Proxy"
        ContainerType.Other -> "Other"
    }
}

@Composable
private fun SearchScreen(padding: PaddingValues) {
    ScreenColumn(padding) {
        Text("Search", style = MaterialTheme.typography.headlineMedium)
        Text("Commander-aware selection context will live here.")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(selected = true, onClick = {}, label = { Text("Legal") })
            FilterChip(selected = false, onClick = {}, label = { Text("Available") })
            FilterChip(selected = false, onClick = {}, label = { Text("Suggestions") })
        }
    }
}

@Composable
private fun SettingsScreen(padding: PaddingValues) {
    ScreenColumn(padding) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Text("Scryfall cache, import defaults, bracket preferences, and AI provider settings will live here.")
    }
}

@Composable
private fun ScreenColumn(
    padding: PaddingValues,
    content: @Composable ColumnScopeWithSpacing.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ColumnScopeWithSpacing(this).content()
    }
}

private class ColumnScopeWithSpacing(private val columnScope: androidx.compose.foundation.layout.ColumnScope) :
    androidx.compose.foundation.layout.ColumnScope by columnScope
