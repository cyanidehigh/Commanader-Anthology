package com.commanderanalyst.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.commanderanalyst.core.model.Container
import com.commanderanalyst.core.model.ContainerType
import com.commanderanalyst.core.model.Deck
import com.commanderanalyst.core.model.DeckSection
import com.commanderanalyst.core.model.DeckSlot
import com.commanderanalyst.core.model.InventoryEntry
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Commander Analyst"
    ) {
        CommanderAnalystDesktopApp()
    }
}

@Composable
@Preview
fun CommanderAnalystDesktopApp() {
    val state = remember { DesktopAppState() }
    LaunchedEffect(Unit) { state.loadOrSeed() }

    MaterialTheme(colorScheme = CynfulDesktopColors) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            DesktopShell(state)
        }
    }
}

@Composable
private fun DesktopShell(state: DesktopAppState) {
    var tab by remember { mutableStateOf(DesktopTab.Decks) }

    Row(Modifier.fillMaxSize()) {
        Sidebar(selected = tab, onSelect = { tab = it })
        Box(Modifier.fillMaxSize().padding(20.dp)) {
            when (tab) {
                DesktopTab.Decks -> DeckWorkspace(state)
                DesktopTab.Collection -> CollectionWorkspace(state)
                DesktopTab.Search -> ComingSoon("Commander-aware search")
                DesktopTab.Sync -> SyncWorkspace()
            }
        }
    }
}

@Composable
private fun Sidebar(selected: DesktopTab, onSelect: (DesktopTab) -> Unit) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(Color(0xFF090806))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource("cynful_logo.png"),
                contentDescription = "Cynful Studio",
                modifier = Modifier.size(56.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column {
                Text("Commander Analyst", fontWeight = FontWeight.Bold)
                Text("Desktop brewer", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        DesktopTab.entries.forEach { tab ->
            val selectedColor = if (selected == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            Text(
                text = tab.label,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp),
                color = selectedColor,
                fontWeight = if (selected == tab) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun DeckWorkspace(state: DesktopAppState) {
    var editingDeck by remember { mutableStateOf<Deck?>(null) }
    var deletingDeck by remember { mutableStateOf<Deck?>(null) }
    var showNewDeck by remember { mutableStateOf(false) }
    var editingSlot by remember { mutableStateOf<DeckSlot?>(null) }
    var deletingSlot by remember { mutableStateOf<DeckSlot?>(null) }
    var assigningSlot by remember { mutableStateOf<DeckSlot?>(null) }
    var showNewSlot by remember { mutableStateOf(false) }
    var showImportDeck by remember { mutableStateOf(false) }
    var viewingCard by remember { mutableStateOf<CardDetailsRequest?>(null) }

    val visibleDecks = state.decks.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name.trim() })
    val selectedDeck = visibleDecks.firstOrNull { it.id == state.selectedDeckId } ?: visibleDecks.firstOrNull()
    if (state.selectedDeckId == null && selectedDeck != null) state.selectedDeckId = selectedDeck.id
    val slots = selectedDeck?.let { deck -> state.deckSlots.filter { it.deckId == deck.id } }.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HeaderRow(
            title = "Deck Builder",
            subtitle = "Desktop-first intent editing. Ownership matching comes next.",
            action = "New deck",
            onAction = { showNewDeck = true }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            ListPanel(title = "Decks", modifier = Modifier.width(340.dp)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visibleDecks, key = { it.id }) { deck ->
                        DeckListRow(
                            deck = deck,
                            selected = deck.id == selectedDeck?.id,
                            onSelect = { state.selectedDeckId = deck.id },
                            onEdit = { editingDeck = deck },
                            onDelete = { deletingDeck = deck }
                        )
                    }
                }
            }
            DetailPanel(modifier = Modifier.weight(1f)) {
                if (selectedDeck == null) {
                    EmptyState("No deck selected")
                } else {
                    DeckDetail(
                        deck = selectedDeck,
                        slots = slots,
                        assignedQuantity = state::assignedQuantityFor,
                        assignedEntries = state::assignedEntriesFor,
                        assignmentSourceName = state::assignmentSourceNameFor,
                        availableEntries = state::availableEntriesFor,
                        containerName = state::containerName,
                        onAddSlot = { showNewSlot = true },
                        onImportDeck = { showImportDeck = true },
                        onEditDeck = { editingDeck = selectedDeck },
                        onDeleteDeck = { deletingDeck = selectedDeck },
                        onAssignSlot = { slot -> assigningSlot = slot },
                        onUnassignSlot = state::unassignOne,
                        onViewSlot = { slot ->
                            val assignedEntry = state.assignedEntriesFor(slot.id).firstOrNull()
                            viewingCard = CardDetailsRequest(
                                displayName = assignedEntry?.let { it.printingName ?: it.oracleName ?: it.cardName }
                                    ?: slot.preferredPrintingName
                                    ?: slot.oracleName
                                    ?: slot.cardName,
                                scryfallCardId = assignedEntry?.scryfallCardId ?: slot.preferredScryfallCardId,
                                isFoil = assignedEntry?.isFoil ?: false
                            )
                        },
                        onEditSlot = { editingSlot = it },
                        onDeleteSlot = { deletingSlot = it }
                    )
                }
            }
        }
    }

    if (showNewDeck) {
        DeckDialog(
            title = "New deck",
            confirmLabel = "Create",
            onDismiss = { showNewDeck = false },
            onConfirm = { name, commander ->
                state.createDeck(name, commander)
                showNewDeck = false
            }
        )
    }

    editingDeck?.let { deck ->
        DeckDialog(
            title = "Edit deck",
            confirmLabel = "Save",
            initialName = deck.name,
            initialCommander = deck.commanderName.orEmpty(),
            onDismiss = { editingDeck = null },
            onConfirm = { name, commander ->
                state.updateDeck(deck.id, name, commander)
                editingDeck = null
            }
        )
    }

    deletingDeck?.let { deck ->
        ConfirmDeleteDialog(
            title = "Delete deck",
            body = "Delete ${deck.name} and its deck rows?",
            onDismiss = { deletingDeck = null },
            onDelete = {
                state.deleteDeck(deck.id)
                deletingDeck = null
            }
        )
    }

    if (showNewSlot && selectedDeck != null) {
        DeckSlotDialog(
            title = "Add deck card",
            confirmLabel = "Add",
            onDismiss = { showNewSlot = false },
            onConfirm = { cardName, quantity, section, selection ->
                state.addDeckSlot(selectedDeck.id, cardName, quantity, section, selection)
                showNewSlot = false
            }
        )
    }

    if (showImportDeck && selectedDeck != null) {
        ImportDeckDialog(
            deckName = selectedDeck.name,
            onDismiss = { showImportDeck = false },
            onImport = { rows ->
                state.addImportedDeckSlots(selectedDeck.id, rows)
                showImportDeck = false
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
            initialSelection = slot.toSelectionOrNull(),
            onConfirm = { cardName, quantity, section, selection ->
                state.updateDeckSlot(slot.id, cardName, quantity, section, selection)
                editingSlot = null
            }
        )
    }

    deletingSlot?.let { slot ->
        ConfirmDeleteDialog(
            title = "Delete deck card",
            body = "Remove ${slot.cardName} from this deck?",
            onDismiss = { deletingSlot = null },
            onDelete = {
                state.deleteDeckSlot(slot.id)
                deletingSlot = null
            }
        )
    }

    viewingCard?.let { request ->
        CardDetailsDialog(request = request, onDismiss = { viewingCard = null })
    }

    assigningSlot?.let { slot ->
        AssignInventoryDialog(
            slot = slot,
            availableEntries = state.availableEntriesFor(slot).sortedWith(collectionEntryComparator()),
            containerName = state::containerName,
            onDismiss = { assigningSlot = null },
            onAssign = { entry ->
                state.assignInventoryEntry(slot, entry)
                assigningSlot = null
            }
        )
    }
}

@Composable
private fun CollectionWorkspace(state: DesktopAppState) {
    var editingContainer by remember { mutableStateOf<Container?>(null) }
    var deletingContainer by remember { mutableStateOf<Container?>(null) }
    var showNewContainer by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<InventoryEntry?>(null) }
    var movingEntry by remember { mutableStateOf<InventoryEntry?>(null) }
    var deletingEntry by remember { mutableStateOf<InventoryEntry?>(null) }
    var showNewEntry by remember { mutableStateOf(false) }
    var showImportCollection by remember { mutableStateOf(false) }
    var viewingCard by remember { mutableStateOf<CardDetailsRequest?>(null) }

    val visibleContainers = state.containers
        .filter { it.type != ContainerType.Deck }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name.trim() })
    val selectedContainer = visibleContainers.firstOrNull { it.id == state.selectedContainerId } ?: visibleContainers.firstOrNull()
    if (state.selectedContainerId == null && selectedContainer != null) state.selectedContainerId = selectedContainer.id
    val entries = selectedContainer
        ?.let { container -> state.inventoryEntries.filter { it.containerId == container.id } }
        .orEmpty()
        .sortedWith(collectionEntryComparator())

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HeaderRow(
            title = "Collection",
            subtitle = "Physical places first: binders, boxes, decks, piles.",
            action = "New container",
            onAction = { showNewContainer = true }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            ListPanel(title = "Containers", modifier = Modifier.width(340.dp)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visibleContainers, key = { it.id }) { container ->
                        ContainerListRow(
                            container = container,
                            count = state.inventoryEntries.filter { it.containerId == container.id }.sumOf { it.quantity },
                            selected = container.id == selectedContainer?.id,
                            onSelect = { state.selectedContainerId = container.id },
                            onEdit = { editingContainer = container },
                            onDelete = { deletingContainer = container }
                        )
                    }
                }
            }
            DetailPanel(modifier = Modifier.weight(1f)) {
                if (selectedContainer == null) {
                    EmptyState("No container selected")
                } else {
                    CollectionDetail(
                        container = selectedContainer,
                        entries = entries,
                        onAddEntry = { showNewEntry = true },
                        onImportEntries = { showImportCollection = true },
                        onEditContainer = { editingContainer = selectedContainer },
                        onDeleteContainer = { deletingContainer = selectedContainer },
                        onViewEntry = { entry ->
                            viewingCard = CardDetailsRequest(
                                displayName = entry.printingName ?: entry.oracleName ?: entry.cardName,
                                scryfallCardId = entry.scryfallCardId,
                                isFoil = entry.isFoil
                            )
                        },
                        onEditEntry = { editingEntry = it },
                        onMoveEntry = { movingEntry = it },
                        onDeleteEntry = { deletingEntry = it }
                    )
                }
            }
        }
    }

    if (showNewContainer) {
        ContainerDialog(
            title = "New container",
            confirmLabel = "Create",
            onDismiss = { showNewContainer = false },
            onConfirm = { name, type ->
                state.createContainer(name, type)
                showNewContainer = false
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
                state.updateContainer(container.id, name, type)
                editingContainer = null
            }
        )
    }

    deletingContainer?.let { container ->
        ConfirmDeleteDialog(
            title = "Delete container",
            body = "Delete ${container.name}? Inventory rows inside it will be removed.",
            onDismiss = { deletingContainer = null },
            onDelete = {
                state.deleteContainer(container.id)
                deletingContainer = null
            }
        )
    }

    if (showNewEntry && selectedContainer != null) {
        InventoryDialog(
            title = "Add collection card",
            confirmLabel = "Add",
            onDismiss = { showNewEntry = false },
            onConfirm = { cardName, quantity, isFoil, selection ->
                state.addInventoryEntry(selectedContainer.id, cardName, quantity, isFoil, selection)
                showNewEntry = false
            }
        )
    }

    if (showImportCollection && selectedContainer != null) {
        ImportCollectionDialog(
            containerName = selectedContainer.name,
            onDismiss = { showImportCollection = false },
            onImport = { rows ->
                state.addImportedInventoryEntries(selectedContainer.id, rows)
                showImportCollection = false
            }
        )
    }

    editingEntry?.let { entry ->
        InventoryDialog(
            title = "Edit collection card",
            confirmLabel = "Save",
            initialName = entry.cardName,
            initialQuantity = entry.quantity,
            initialFoil = entry.isFoil,
            onDismiss = { editingEntry = null },
            initialSelection = entry.toSelectionOrNull(),
            onConfirm = { cardName, quantity, isFoil, selection ->
                state.updateInventoryEntry(entry.id, cardName, quantity, isFoil, selection)
                editingEntry = null
            }
        )
    }

    movingEntry?.let { entry ->
        MoveInventoryDialog(
            entry = entry,
            containers = visibleContainers,
            currentContainerId = entry.containerId,
            onDismiss = { movingEntry = null },
            onMove = { targetContainer ->
                state.moveInventoryEntry(entry.id, targetContainer.id)
                movingEntry = null
            }
        )
    }

    deletingEntry?.let { entry ->
        ConfirmDeleteDialog(
            title = "Delete collection card",
            body = "Remove ${entry.cardName} from this container?",
            onDismiss = { deletingEntry = null },
            onDelete = {
                state.deleteInventoryEntry(entry.id)
                deletingEntry = null
            }
        )
    }

    viewingCard?.let { request ->
        CardDetailsDialog(request = request, onDismiss = { viewingCard = null })
    }
}

@Composable
private fun SyncWorkspace() {
    val bulkDataClient = remember { ScryfallBulkDataClient() }
    var statuses by remember { mutableStateOf<List<BulkDataStatus>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Bulk data is not checked yet.") }
    var installProgress by remember { mutableStateOf<BulkInstallProgress?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Sync", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Planned as a local sync bundle written through user-owned cloud storage.")
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("No maintained server", fontWeight = FontWeight.Bold)
                Text("The desktop app will export/import a bundle that can live in Google Drive, OneDrive, Dropbox, or a normal folder.")
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Scryfall Bulk Data", fontWeight = FontWeight.Bold)
                Text("Local cache: ${bulkDataClient.cachePath()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        enabled = !busy,
                        onClick = {
                            busy = true
                            installProgress = null
                            message = "Checking Scryfall bulk metadata..."
                            Thread {
                                bulkDataClient.checkForUpdates()
                                    .onSuccess { result ->
                                        statuses = result
                                        val updates = result.count { it.updateAvailable }
                                        message = if (updates == 0) "Bulk data is up to date." else "$updates bulk files need install/update."
                                    }
                                    .onFailure { error ->
                                        message = error.message ?: "Bulk data check failed."
                                    }
                                busy = false
                            }.start()
                        }
                    ) { Text("Check for updates") }
                    OutlinedButton(
                        enabled = !busy,
                        onClick = {
                            busy = true
                            installProgress = null
                            message = "Installing Scryfall bulk data..."
                            Thread {
                                bulkDataClient.installAll { progress ->
                                    installProgress = progress
                                    message = progress.label()
                                }
                                    .onSuccess { result ->
                                        statuses = result
                                        message = "Bulk data installed. ${result.size} files tracked."
                                    }
                                    .onFailure { error ->
                                        message = error.message ?: "Bulk data install failed."
                                    }
                                busy = false
                            }.start()
                        }
                    ) { Text(if (statuses.any { it.installed }) "Update all" else "Install all") }
                    OutlinedButton(
                        enabled = !busy,
                        onClick = {
                            busy = true
                            installProgress = null
                            message = "Building SQLite card cache..."
                            Thread {
                                ScryfallClient().prepareBulkLookupIndex { progress -> message = progress }
                                    .onSuccess {
                                        message = "SQLite card cache is ready."
                                    }
                                    .onFailure { error ->
                                        message = error.message ?: "SQLite card cache build failed."
                                    }
                                busy = false
                            }.start()
                        }
                    ) { Text("Build card cache") }
                }
                installProgress?.let { progress ->
                    LinearProgressIndicator(
                        progress = { progress.overallFraction },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "${(progress.overallFraction * 100).toInt()}% - ${progress.label()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (busy && installProgress == null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(320.dp)) {
                    items(statuses) { status ->
                        BulkDataStatusRow(status)
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkDataStatusRow(status: BulkDataStatus) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151310))) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(status.name, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text(status.type, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val state = when {
                !status.installed -> "Not installed"
                status.updateAvailable -> "Update available"
                else -> "Installed"
            }
            Text(
                "$state - remote ${status.remoteUpdatedAt} - local ${status.localUpdatedAt ?: "none"} - ${formatBytes(status.size)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "unknown size"
    val mib = bytes / 1024.0 / 1024.0
    return "%.1f MB".format(mib)
}

@Composable
private fun HeaderRow(title: String, subtitle: String, action: String, onAction: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(onClick = onAction) { Text(action) }
    }
}

@Composable
private fun ListPanel(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxHeight(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
        }
    }
}

@Composable
private fun DetailPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier = modifier.fillMaxHeight(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Box(Modifier.fillMaxSize().padding(18.dp)) { content() }
    }
}

@Composable
private fun DeckListRow(deck: Deck, selected: Boolean, onSelect: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    SelectableRowCard(selected = selected, onSelect = onSelect) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(deck.name, fontWeight = FontWeight.Bold)
            Text(deck.commanderName ?: "Commander not set", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(onClick = onEdit) { Text("Edit") }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

@Composable
private fun ContainerListRow(container: Container, count: Int, selected: Boolean, onSelect: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    SelectableRowCard(selected = selected, onSelect = onSelect) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(container.name, fontWeight = FontWeight.Bold)
            Text("${container.type.displayName()} - $count cards", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(onClick = onEdit) { Text("Edit") }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

@Composable
private fun SelectableRowCard(selected: Boolean, onSelect: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF2A1E0D) else Color(0xFF151310))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
private fun DeckDetail(
    deck: Deck,
    slots: List<DeckSlot>,
    assignedQuantity: (String) -> Int,
    assignedEntries: (String) -> List<InventoryEntry>,
    assignmentSourceName: (String, String) -> String?,
    availableEntries: (DeckSlot) -> List<InventoryEntry>,
    containerName: (String) -> String,
    onAddSlot: () -> Unit,
    onImportDeck: () -> Unit,
    onEditDeck: () -> Unit,
    onDeleteDeck: () -> Unit,
    onAssignSlot: (DeckSlot) -> Unit,
    onUnassignSlot: (DeckSlot) -> Unit,
    onViewSlot: (DeckSlot) -> Unit,
    onEditSlot: (DeckSlot) -> Unit,
    onDeleteSlot: (DeckSlot) -> Unit
) {
    val assignedCount = slots.sumOf { assignedQuantity(it.id) }
    val desiredCount = slots.sumOf { it.desiredQuantity }
    val availableCount = slots.sumOf { slot ->
        (slot.desiredQuantity - assignedQuantity(slot.id)).coerceAtLeast(0)
            .coerceAtMost(availableEntries(slot).sumOf { it.quantity })
    }
    val missingCount = (desiredCount - assignedCount - availableCount).coerceAtLeast(0)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(deck.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(deck.commanderName ?: "Commander not set", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddSlot) { Text("Add card") }
                OutlinedButton(onClick = onImportDeck) { Text("Import") }
                OutlinedButton(onClick = onEditDeck) { Text("Edit deck") }
                TextButton(onClick = onDeleteDeck) { Text("Delete") }
            }
        }
        StatRow(listOf("$desiredCount wanted", "$assignedCount assigned", "$availableCount available", "$missingCount missing"))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(slots, key = { it.id }) { slot ->
                val assigned = assignedQuantity(slot.id)
                val assignedPhysicalEntries = assignedEntries(slot.id).sortedWith(collectionEntryComparator())
                val available = availableEntries(slot)
                DeckSlotDataRow(
                    countText = "x${slot.desiredQuantity}",
                    title = slot.oracleName ?: slot.cardName,
                    subtitle = deckSlotSubtitle(slot, assigned, available, containerName, assignedPhysicalEntries, assignmentSourceName),
                    badges = deckSlotBadges(slot, assignedPhysicalEntries),
                    onTitleClick = { onViewSlot(slot) },
                    symbol = deckSlotSymbol(slot, assigned, available),
                    symbolColor = deckSlotSymbolColor(assigned, available),
                    onSymbolClick = {
                        if (available.isNotEmpty() && assigned < slot.desiredQuantity) {
                            onAssignSlot(slot)
                        } else if (assigned > 0) {
                            onUnassignSlot(slot)
                        }
                    },
                    onEdit = { onEditSlot(slot) },
                    onDelete = { onDeleteSlot(slot) }
                )
            }
        }
    }
}

@Composable
private fun DeckSlotDataRow(
    countText: String,
    title: String,
    subtitle: String,
    badges: List<RowBadge>,
    onTitleClick: () -> Unit,
    symbol: String,
    symbolColor: Color,
    onSymbolClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151310))) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF211A10))
                    .clickable(onClick = onSymbolClick),
                contentAlignment = Alignment.Center
            ) {
                Text(symbol, color = symbolColor, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(countText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    Text(
                        title,
                        modifier = Modifier.clickable(onClick = onTitleClick),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    RowBadges(badges)
                }
                if (subtitle.isNotBlank()) {
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            TextButton(onClick = onEdit) { Text("Edit") }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

@Composable
private fun AssignInventoryDialog(
    slot: DeckSlot,
    availableEntries: List<InventoryEntry>,
    containerName: (String) -> String,
    onDismiss: () -> Unit,
    onAssign: (InventoryEntry) -> Unit
) {
    AlertDialog(
        modifier = Modifier.width(900.dp),
        onDismissRequest = onDismiss,
        title = { Text("Choose copy for ${slot.oracleName ?: slot.cardName}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "${availableEntries.sumOf { it.quantity }} available across ${availableEntries.size} version rows",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (availableEntries.isEmpty()) {
                    Text("No available collection copies.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(560.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableEntries, key = { it.id }) { entry ->
                            AssignInventoryOption(
                                entry = entry,
                                sourceName = containerName(entry.containerId),
                                onAssign = { onAssign(entry) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AssignInventoryOption(entry: InventoryEntry, sourceName: String, onAssign: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAssign),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151310))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssignmentCardArt(entry)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("x${entry.quantity}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    Text(entry.printingName ?: entry.cardName, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    RowBadges(collectionEntryBadges(entry))
                }
                entry.oracleName
                    ?.takeIf { it != (entry.printingName ?: entry.cardName) }
                    ?.let { Text("Oracle: $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text("From: $sourceName", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onAssign) { Text("Use this copy") }
            }
        }
    }
}

@Composable
private fun AssignmentCardArt(entry: InventoryEntry) {
    var image by remember(entry.scryfallCardId) { mutableStateOf<ImageBitmap?>(null) }
    val displayName = entry.printingName ?: entry.cardName
    val scryfallClient = remember { ScryfallClient() }

    LaunchedEffect(entry.scryfallCardId) {
        val cardId = entry.scryfallCardId ?: return@LaunchedEffect
        Thread {
            scryfallClient.cardDetails(cardId)
                .getOrNull()
                ?.imageUrl
                ?.let { imageUrl -> image = loadCardImage(imageUrl).getOrNull() }
        }.start()
    }

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0C0A))) {
        Box(Modifier.width(92.dp).height(128.dp).padding(4.dp), contentAlignment = Alignment.Center) {
            if (image == null) {
                Text(
                    entry.setCode?.let { set -> "$set #${entry.collectorNumber.orEmpty()}" } ?: displayName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Image(
                    bitmap = image!!,
                    contentDescription = displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun CollectionDetail(
    container: Container,
    entries: List<InventoryEntry>,
    onAddEntry: () -> Unit,
    onImportEntries: () -> Unit,
    onEditContainer: () -> Unit,
    onDeleteContainer: () -> Unit,
    onViewEntry: (InventoryEntry) -> Unit,
    onEditEntry: (InventoryEntry) -> Unit,
    onMoveEntry: (InventoryEntry) -> Unit,
    onDeleteEntry: (InventoryEntry) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(container.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(container.type.displayName(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddEntry) { Text("Add card") }
                OutlinedButton(onClick = onImportEntries) { Text("Import") }
                OutlinedButton(onClick = onEditContainer) { Text("Edit container") }
                TextButton(onClick = onDeleteContainer) { Text("Delete") }
            }
        }
        StatRow(listOf("${entries.sumOf { it.quantity }} cards", "${entries.size} rows"))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(entries, key = { it.id }) { entry ->
                EditableDataRow(
                    countText = "x${entry.quantity}",
                    title = entry.printingName ?: entry.cardName,
                    subtitle = collectionEntrySubtitle(entry),
                    badges = collectionEntryBadges(entry),
                    onTitleClick = { onViewEntry(entry) },
                    onEdit = { onEditEntry(entry) },
                    onMove = { onMoveEntry(entry) },
                    onDelete = { onDeleteEntry(entry) }
                )
            }
        }
    }
}

private fun deckSlotSubtitle(
    slot: DeckSlot,
    assigned: Int,
    available: List<InventoryEntry>,
    containerName: (String) -> String,
    assignedEntries: List<InventoryEntry>,
    assignmentSourceName: (String, String) -> String?
): String {
    val availableQuantity = available.sumOf { it.quantity }
    val assignmentText = assignedEntries.firstOrNull()?.let { entry ->
        val printing = entry.printingName ?: entry.cardName
        val set = entry.setCode?.takeIf { it.isNotBlank() }
        val collector = entry.collectorNumber?.takeIf { it.isNotBlank() }
        val printingRef = listOfNotNull(set, collector?.let { "#$it" }).joinToString(" ")
        val from = assignmentSourceName(slot.id, entry.id) ?: containerName(entry.containerId)
        " - assigned $printing${printingRef.takeIf { it.isNotBlank() }?.let { " ($it)" }.orEmpty()} from $from"
    }
    val sourceText = assignmentText ?: available.firstOrNull()?.let { entry ->
        " - ${entry.cardName} in ${containerName(entry.containerId)}"
    }.orEmpty()
    return "${slot.section.displayName()} - $assigned assigned - $availableQuantity available$sourceText"
}

private fun collectionEntrySubtitle(entry: InventoryEntry): String {
    val oracleText = entry.oracleName
        ?.takeIf { it != (entry.printingName ?: entry.cardName) }
        ?.let { "Oracle: $it" }
        .orEmpty()
    return oracleText
}

private fun collectionEntryComparator(): Comparator<InventoryEntry> {
    return compareBy<InventoryEntry> { it.setCode.isNullOrBlank() }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.setCode.orEmpty() }
        .thenBy { it.collectorNumber.collectorSortNumber() }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.collectorNumber.collectorSortSuffix() }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.printingName ?: it.cardName }
}

private fun String?.collectorSortNumber(): Int {
    val value = this?.trim().orEmpty()
    return Regex("""\d+""").find(value)?.value?.toIntOrNull() ?: Int.MAX_VALUE
}

private fun String?.collectorSortSuffix(): String {
    val value = this?.trim().orEmpty()
    return value.replace(Regex("""\d+"""), "").trim()
}

private fun deckSlotBadges(slot: DeckSlot, assignedEntries: List<InventoryEntry>): List<RowBadge> {
    val identityBadge = if (slot.oracleId.isNullOrBlank()) {
        RowBadge("Manual", Color(0xFF2A2420), Color(0xFFE8D5B0))
    } else {
        RowBadge("Resolved intent", Color(0xFF26321F), Color(0xFFC8E6A0))
    }
    val assignedEntry = assignedEntries.firstOrNull()
    val assignedBadge = assignedEntry?.let {
        RowBadge("Assigned copy", Color(0xFF3A2608), Color(0xFFE0A52F))
    }
    val foilBadge = assignedEntry?.takeIf { it.isFoil }?.let {
        RowBadge("Foil", Color(0xFF2A2234), Color(0xFFDCC9FF))
    }
    val printingBadge = assignedEntry
        ?.let { printingBadge(it.setCode, it.collectorNumber) }
        ?: printingBadge(slot.preferredSetCode, slot.preferredCollectorNumber)
    return listOfNotNull(identityBadge, assignedBadge, foilBadge, printingBadge)
}

private fun collectionEntryBadges(entry: InventoryEntry): List<RowBadge> {
    val identityBadge = if (entry.scryfallCardId.isNullOrBlank()) {
        RowBadge("Manual", Color(0xFF2A2420), Color(0xFFE8D5B0))
    } else {
        RowBadge("Resolved printing", Color(0xFF26321F), Color(0xFFC8E6A0))
    }
    val printingBadge = printingBadge(entry.setCode, entry.collectorNumber)
    val foilBadge = if (entry.isFoil) RowBadge("Foil", Color(0xFF2A2234), Color(0xFFDCC9FF)) else null
    return listOfNotNull(identityBadge, foilBadge, printingBadge)
}

private fun importRowBadges(row: ImportedDeckRow): List<RowBadge> {
    val identityBadge = if (row.cardSelection == null) {
        RowBadge("Unresolved", Color(0xFF2A2420), Color(0xFFE8D5B0))
    } else {
        RowBadge("Resolved", Color(0xFF26321F), Color(0xFFC8E6A0))
    }
    val printingBadge = row.cardSelection?.let { printingBadge(it.setCode, it.collectorNumber) }
    return listOfNotNull(
        RowBadge(row.section.displayName(), Color(0xFF211A10), Color(0xFFD8C49A)),
        identityBadge,
        printingBadge
    )
}

@Composable
private fun ImportReviewRow(row: ImportedDeckRow) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151310))) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("x${row.quantity}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                Text(row.cardName, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Text(importRowSubtitle(row), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun importRowSubtitle(row: ImportedDeckRow): String {
    val identity = row.cardSelection?.let { selection ->
        "${selection.setCode} #${selection.collectorNumber}"
    } ?: "Unresolved"
    return "${row.section.displayName()} - $identity"
}

@Composable
private fun CollectionImportReviewRow(row: ImportedCollectionRow, onSelect: (ScryfallCardSelection) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151310))) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("x${row.quantity}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                Text(row.cardName, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                RowBadges(collectionImportBadges(row))
            }
            Text(collectionImportSubtitle(row), color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (row.cardSelection == null && row.printingOptions.isNotEmpty()) {
                PrintingOptions(options = row.printingOptions, selected = null, onSelect = onSelect)
            }
        }
    }
}

private fun collectionImportBadges(row: ImportedCollectionRow): List<RowBadge> {
    val statusBadge = when {
        row.cardSelection != null -> RowBadge("Resolved", Color(0xFF26321F), Color(0xFFC8E6A0))
        row.printingOptions.isNotEmpty() -> RowBadge("Choose printing", Color(0xFF332713), Color(0xFFE0A52F))
        else -> RowBadge("Manual", Color(0xFF2A2420), Color(0xFFE8D5B0))
    }
    val foilBadge = if (row.isFoil) RowBadge("Foil", Color(0xFF2A2234), Color(0xFFDCC9FF)) else null
    val printingBadge = row.cardSelection
        ?.let { printingBadge(it.setCode, it.collectorNumber) }
        ?: printingBadge(row.setCode, row.collectorNumber)
    return listOfNotNull(statusBadge, foilBadge, printingBadge)
}

private fun collectionImportSubtitle(row: ImportedCollectionRow): String {
    val identity = row.cardSelection?.let { selection ->
        "${selection.printingName} - ${selection.setCode} #${selection.collectorNumber}"
    } ?: row.setCode?.let { set ->
        val collector = row.collectorNumber?.let { " #$it" }.orEmpty()
        "Requested: $set$collector"
    } ?: "Name-only import row"
    return identity
}

private fun printingBadge(setCode: String?, collectorNumber: String?): RowBadge? {
    val set = setCode?.takeIf { it.isNotBlank() } ?: return null
    val collector = collectorNumber?.takeIf { it.isNotBlank() }
    val label = if (collector == null) set else "$set #$collector"
    return RowBadge(label, Color(0xFF332713), Color(0xFFE0A52F))
}

private fun ScryfallCardSelection.summary(prefix: String): String {
    val oracleText = if (oracleName != printingName) " / $oracleName" else ""
    return "$prefix: $printingName$oracleText · $setCode #$collectorNumber"
}

private fun DeckSlot.toSelectionOrNull(): ScryfallCardSelection? {
    val scryfallId = preferredScryfallCardId ?: return null
    return ScryfallCardSelection(
        scryfallCardId = scryfallId,
        oracleId = oracleId,
        oracleName = oracleName ?: cardName,
        printingName = preferredPrintingName ?: cardName,
        setCode = preferredSetCode.orEmpty(),
        collectorNumber = preferredCollectorNumber.orEmpty()
    )
}

private fun InventoryEntry.toSelectionOrNull(): ScryfallCardSelection? {
    val scryfallId = scryfallCardId ?: return null
    return ScryfallCardSelection(
        scryfallCardId = scryfallId,
        oracleId = oracleId,
        oracleName = oracleName ?: cardName,
        printingName = printingName ?: cardName,
        setCode = setCode.orEmpty(),
        collectorNumber = collectorNumber.orEmpty()
    )
}

private data class CardDetailsRequest(
    val displayName: String,
    val scryfallCardId: String?,
    val isFoil: Boolean = false
)

private fun deckSlotSymbol(slot: DeckSlot, assigned: Int, available: List<InventoryEntry>): String {
    return when {
        assigned >= slot.desiredQuantity -> "✓"
        assigned > 0 -> "◐"
        available.isNotEmpty() -> "○"
        else -> "●"
    }
}

private fun deckSlotSymbolColor(assigned: Int, available: List<InventoryEntry>): Color {
    return when {
        assigned > 0 -> Color(0xFFE0A52F)
        available.isNotEmpty() -> Color(0xFFB8B0A0)
        else -> Color(0xFF0A0A0A)
    }
}


private data class RowBadge(
    val label: String,
    val backgroundColor: Color,
    val contentColor: Color
)

@Composable
private fun RowBadges(badges: List<RowBadge>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        badges.forEach { badge ->
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badge.backgroundColor)
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(badge.label, color = badge.contentColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PrintingOptions(
    options: List<ScryfallCardSelection>,
    selected: ScryfallCardSelection?,
    onSelect: (ScryfallCardSelection) -> Unit
) {
    if (options.size <= 1) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Choose printing", style = MaterialTheme.typography.labelLarge)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (options.size > 5) 220.dp else (options.size * 44).dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(options) { option ->
                val isSelected = selected?.scryfallCardId == option.scryfallCardId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(option) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF4E4264) else Color(0xFF151310)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            option.optionLabel(),
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private fun ScryfallCardSelection.optionLabel(): String {
    val oracleText = if (printingName != oracleName) " / $oracleName" else ""
    return "$setCode #$collectorNumber - $printingName$oracleText"
}

@Composable
private fun CardDetailsDialog(request: CardDetailsRequest, onDismiss: () -> Unit) {
    var details by remember(request) { mutableStateOf<ScryfallCardDetails?>(null) }
    var image by remember(request) { mutableStateOf<ImageBitmap?>(null) }
    var message by remember(request) {
        mutableStateOf(if (request.scryfallCardId == null) "Manual or unresolved card." else "Loading card details...")
    }
    val scryfallClient = remember { ScryfallClient() }

    LaunchedEffect(request) {
        val cardId = request.scryfallCardId ?: return@LaunchedEffect
        Thread {
            scryfallClient.cardDetails(cardId)
                .onSuccess { loadedDetails ->
                    details = loadedDetails
                    message = ""
                    loadedDetails.imageUrl?.let { imageUrl ->
                        Thread {
                            image = loadCardImage(imageUrl).getOrNull()
                        }.start()
                    }
                }
                .onFailure { error ->
                    message = error.message ?: "Unable to load card details."
                }
        }.start()
    }

    AlertDialog(
        modifier = Modifier.width(980.dp),
        onDismissRequest = onDismiss,
        title = { Text(details?.name ?: request.displayName, fontWeight = FontWeight.Bold) },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(620.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                CardPreview(image = image, displayName = details?.name ?: request.displayName)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (details == null) {
                        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        CardDetailsBody(details = details!!, isFoil = request.isFoil)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun CardPreview(image: ImageBitmap?, displayName: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0C0A))) {
        Box(Modifier.width(300.dp).height(420.dp).padding(10.dp), contentAlignment = Alignment.Center) {
            if (image == null) {
                Text(displayName, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Image(
                    bitmap = image,
                    contentDescription = displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun CardDetailsBody(details: ScryfallCardDetails, isFoil: Boolean) {
    Text(details.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    if (details.oracleName != details.name) {
        Text("Oracle: ${details.oracleName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    details.manaCost?.takeIf { it.isNotBlank() }?.let {
        Text("Mana cost: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    details.typeLine?.takeIf { it.isNotBlank() }?.let {
        Text(it, fontWeight = FontWeight.SemiBold)
    }
    details.oracleText?.takeIf { it.isNotBlank() }?.let {
        Text(it, color = MaterialTheme.colorScheme.onSurface)
    }
    val stats = listOfNotNull(details.power, details.toughness).takeIf { it.size == 2 }?.joinToString("/")
    stats?.let { Text(it, fontWeight = FontWeight.Bold) }

    RowBadges(
        listOfNotNull(
            RowBadge("${details.setCode} #${details.collectorNumber}", Color(0xFF332713), Color(0xFFE0A52F)),
            if (isFoil) RowBadge("Foil", Color(0xFF2A2234), Color(0xFFDCC9FF)) else null,
            details.rarity?.let { RowBadge(it.replaceFirstChar { char -> char.uppercase() }, Color(0xFF211A10), Color(0xFFE8D5B0)) }
        )
    )
    details.setName?.takeIf { it.isNotBlank() }?.let {
        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    CardDetailsSection("Price") {
        Text(selectedPrice(details.prices, isFoil), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    CardDetailsSection("Legality") {
        RowBadges(listOf(commanderLegalityBadge(details.legalities["commander"])))
    }
}

private fun selectedPrice(prices: Map<String, String?>, isFoil: Boolean): String {
    val preferredPrices = if (isFoil) {
        listOf(
            "usd_foil" to "USD foil",
            "usd_etched" to "USD etched",
            "gbp" to "GBP",
            "eur" to "EUR",
            "usd" to "USD",
            "tix" to "MTGO tix"
        )
    } else {
        listOf(
            "gbp" to "GBP",
            "eur" to "EUR",
            "usd" to "USD",
            "usd_foil" to "USD foil",
            "usd_etched" to "USD etched",
            "tix" to "MTGO tix"
        )
    }
    val price = preferredPrices.firstNotNullOfOrNull { (key, label) ->
        prices[key]?.takeIf { it.isNotBlank() }?.let { "$label: $it" }
    }
    return price ?: "No price data."
}

private fun commanderLegalityBadge(status: String?): RowBadge {
    val cleanStatus = status ?: "unknown"
    val isLegal = cleanStatus == "legal"
    return RowBadge(
        "Commander: ${cleanStatus.uppercase()}",
        if (isLegal) Color(0xFF163620) else Color(0xFF2A2420),
        if (isLegal) Color(0xFF76E09A) else Color(0xFFE8D5B0)
    )
}

@Composable
private fun CardDetailsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontWeight = FontWeight.Bold)
        content()
    }
}

private fun loadCardImage(url: String): Result<ImageBitmap> {
    return runCatching {
        val connection = URL(url).openConnection()
        connection.connectTimeout = 5_000
        connection.readTimeout = 8_000
        connection.getInputStream().use { input ->
            ImageIO.read(input).toComposeImageBitmap()
        }
    }
}

@Composable
private fun EditableDataRow(
    countText: String,
    title: String,
    subtitle: String,
    badges: List<RowBadge>,
    onTitleClick: () -> Unit,
    onEdit: () -> Unit,
    onMove: (() -> Unit)? = null,
    onDelete: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151310))) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(countText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    Text(
                        title,
                        modifier = Modifier.clickable(onClick = onTitleClick),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    RowBadges(badges)
                }
                if (subtitle.isNotBlank()) {
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            onMove?.let { move ->
                TextButton(onClick = move) { Text("Move") }
            }
            TextButton(onClick = onEdit) { Text("Edit") }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

@Composable
private fun MoveInventoryDialog(
    entry: InventoryEntry,
    containers: List<Container>,
    currentContainerId: String,
    onDismiss: () -> Unit,
    onMove: (Container) -> Unit
) {
    val targetContainers = containers.filter { it.id != currentContainerId }

    AlertDialog(
        modifier = Modifier.width(640.dp),
        onDismissRequest = onDismiss,
        title = { Text("Move ${entry.printingName ?: entry.cardName}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Choose the container this physical row should live in.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (targetContainers.isEmpty()) {
                    Text("Create another container first.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(targetContainers, key = { it.id }) { container ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onMove(container) },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF151310))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(container.name, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(container.type.displayName(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("Move here", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DeckDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    initialCommander: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var commander by rememberSaveable(initialCommander) { mutableStateOf(initialCommander) }
    val cleanName = name.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Deck name") }, singleLine = true)
                OutlinedTextField(value = commander, onValueChange = { commander = it }, label = { Text("Commander") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(enabled = cleanName.isNotEmpty(), onClick = { onConfirm(cleanName, commander.trim().takeIf { it.isNotEmpty() }) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ImportDeckDialog(
    deckName: String,
    onDismiss: () -> Unit,
    onImport: (List<ImportedDeckRow>) -> Unit
) {
    var rawText by rememberSaveable { mutableStateOf("") }
    val result = remember(rawText) { parseDeckImport(rawText) }
    var resolvedRows by remember(rawText) { mutableStateOf(result.rows) }
    var resolveBusy by remember { mutableStateOf(false) }
    var resolveMessage by remember { mutableStateOf<String?>(null) }
    var resolveFailures by remember(rawText) { mutableStateOf<List<String>>(emptyList()) }
    val scryfallClient = remember { ScryfallClient() }
    val resolvedCount = resolvedRows.count { it.cardSelection != null }
    val unresolvedCount = resolvedRows.size - resolvedCount

    AlertDialog(
        modifier = Modifier.width(900.dp),
        onDismissRequest = onDismiss,
        title = { Text("Import into $deckName") },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth().height(560.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Paste decklist", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        modifier = Modifier.fillMaxWidth().height(500.dp),
                        label = { Text("1 Card Name, section headers supported") }
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Review", fontWeight = FontWeight.Bold)
                    Text(
                        "${resolvedRows.sumOf { it.quantity }} cards - ${resolvedRows.size} rows - $resolvedCount resolved - $unresolvedCount unresolved",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            enabled = resolvedRows.isNotEmpty() && !resolveBusy,
                            onClick = {
                                resolveBusy = true
                                resolveMessage = "Resolving imported rows..."
                                resolveFailures = emptyList()
                                Thread {
                                    val failures = mutableListOf<String>()
                                    val importLookupCache = mutableMapOf<String, Result<ScryfallCardSelection>>()
                                    val resolved = resolvedRows.mapIndexed { index, row ->
                                        val existing = row.cardSelection
                                        if (existing != null) {
                                            row
                                        } else {
                                            val cacheKey = row.cardName.importLookupKey()
                                            val result = importLookupCache.getOrPut(cacheKey) {
                                                scryfallClient.lookupImportedDeckCard(row.cardName)
                                            }
                                            val selection = result.getOrNull()
                                            resolveMessage = "Resolved ${index + 1} / ${resolvedRows.size}"
                                            Thread.sleep(180)
                                            if (selection == null) {
                                                failures += "${row.cardName}: ${result.exceptionOrNull()?.message ?: "No match"}"
                                                row
                                            } else {
                                                row.copy(
                                                    cardName = selection.oracleName,
                                                    section = sectionFromTypeLine(selection.typeLine, row.section),
                                                    cardSelection = selection
                                                )
                                            }
                                        }
                                    }
                                    resolvedRows = resolved
                                    resolveFailures = failures
                                    val failed = resolved.count { it.cardSelection == null }
                                    resolveMessage = if (failed == 0) "All imported rows resolved." else "$failed rows still unresolved."
                                    resolveBusy = false
                                }.start()
                            }
                        ) {
                            Text(if (resolveBusy) "Resolving" else "Resolve rows")
                        }
                        Text(resolveMessage ?: "Resolve before import for ownership matching.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                        items(resolvedRows) { row ->
                            ImportReviewRow(row)
                        }
                    }
                    if (resolveFailures.isNotEmpty()) {
                        Text("Unresolved", fontWeight = FontWeight.Bold)
                        resolveFailures.take(4).forEach { failure ->
                            Text(failure, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (result.warnings.isNotEmpty()) {
                        Text("Skipped", fontWeight = FontWeight.Bold)
                        result.warnings.take(4).forEach { warning ->
                            Text(warning, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = resolvedRows.isNotEmpty() && !resolveBusy, onClick = { onImport(resolvedRows) }) {
                Text("Import ${resolvedRows.size} rows")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ImportCollectionDialog(
    containerName: String,
    onDismiss: () -> Unit,
    onImport: (List<ImportedCollectionRow>) -> Unit
) {
    var rawText by rememberSaveable { mutableStateOf("") }
    val result = remember(rawText) { parseCollectionImport(rawText) }
    var resolvedRows by remember(rawText) { mutableStateOf(result.rows) }
    var resolveBusy by remember { mutableStateOf(false) }
    var resolveMessage by remember { mutableStateOf<String?>(null) }
    var resolveFailures by remember(rawText) { mutableStateOf<List<String>>(emptyList()) }
    val scryfallClient = remember { ScryfallClient() }
    val resolvedCount = resolvedRows.count { it.cardSelection != null }
    val ambiguousCount = resolvedRows.count { it.cardSelection == null && it.printingOptions.isNotEmpty() }
    val unresolvedCount = resolvedRows.size - resolvedCount - ambiguousCount

    AlertDialog(
        modifier = Modifier.width(980.dp),
        onDismissRequest = onDismiss,
        title = { Text("Import collection into $containerName") },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth().height(620.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Paste owned cards", fontWeight = FontWeight.Bold)
                        OutlinedButton(
                            enabled = !resolveBusy,
                            onClick = {
                                chooseCsvFile()?.let { file ->
                                    rawText = file.readText()
                                }
                            }
                        ) {
                            Text("Load CSV")
                        }
                    }
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        modifier = Modifier.fillMaxWidth().height(560.dp),
                        label = { Text("CSV export or 1 Card Name (SET) #123 foil") }
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Review", fontWeight = FontWeight.Bold)
                    Text(
                        "${resolvedRows.sumOf { it.quantity }} cards - ${resolvedRows.size} rows - $resolvedCount resolved - $ambiguousCount choices - $unresolvedCount unresolved",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            enabled = resolvedRows.isNotEmpty() && !resolveBusy,
                            onClick = {
                                resolveBusy = true
                                resolveMessage = "Resolving collection rows..."
                                resolveFailures = emptyList()
                                Thread {
                                    val failures = mutableListOf<String>()
                                    val resolved = resolvedRows.mapIndexed { index, row ->
                                        if (row.cardSelection != null) {
                                            row
                                        } else {
                                            resolveMessage = "Resolved ${index + 1} / ${resolvedRows.size}"
                                            val idSelection = row.scryfallCardId
                                                ?.takeIf { it.isNotBlank() }
                                                ?.let { scryfallClient.lookupCardById(it).getOrNull() }
                                            if (idSelection != null) {
                                                row.copy(
                                                    cardName = idSelection.printingName,
                                                    setCode = idSelection.setCode,
                                                    collectorNumber = idSelection.collectorNumber,
                                                    cardSelection = idSelection,
                                                    printingOptions = listOf(idSelection)
                                                )
                                            } else {
                                                val result = scryfallClient.lookupCardOptions(row.cardName, row.setCode)
                                                result.fold(
                                                    onSuccess = { options ->
                                                        val matchingCollector = row.collectorNumber?.let { collector ->
                                                            options.firstOrNull { it.collectorNumber.equals(collector, ignoreCase = true) }
                                                        }
                                                        val selection = matchingCollector ?: options.singleOrNull()
                                                        if (selection != null) {
                                                            row.copy(
                                                                cardName = selection.printingName,
                                                                setCode = selection.setCode,
                                                                collectorNumber = selection.collectorNumber,
                                                                cardSelection = selection,
                                                                printingOptions = options
                                                            )
                                                        } else {
                                                            row.copy(printingOptions = options)
                                                        }
                                                    },
                                                    onFailure = { error ->
                                                        failures += "${row.cardName}: ${error.message ?: "No match"}"
                                                        row
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    resolvedRows = resolved
                                    resolveFailures = failures
                                    val stillUnresolved = resolved.count { it.cardSelection == null && it.printingOptions.isEmpty() }
                                    val choices = resolved.count { it.cardSelection == null && it.printingOptions.isNotEmpty() }
                                    resolveMessage = when {
                                        stillUnresolved == 0 && choices == 0 -> "All collection rows resolved."
                                        choices > 0 -> "$choices rows need a printing choice."
                                        else -> "$stillUnresolved rows still unresolved."
                                    }
                                    resolveBusy = false
                                }.start()
                            }
                        ) {
                            Text(if (resolveBusy) "Resolving" else "Resolve rows")
                        }
                        Text(resolveMessage ?: "Resolve before import when printings matter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                        items(resolvedRows) { row ->
                            CollectionImportReviewRow(
                                row = row,
                                onSelect = { selection ->
                                    resolvedRows = resolvedRows.map {
                                        if (it === row) {
                                            it.copy(
                                                cardName = selection.printingName,
                                                setCode = selection.setCode,
                                                collectorNumber = selection.collectorNumber,
                                                cardSelection = selection
                                            )
                                        } else {
                                            it
                                        }
                                    }
                                }
                            )
                        }
                    }
                    if (resolveFailures.isNotEmpty()) {
                        Text("Unresolved", fontWeight = FontWeight.Bold)
                        resolveFailures.take(4).forEach { failure ->
                            Text(failure, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (result.warnings.isNotEmpty()) {
                        Text("Skipped", fontWeight = FontWeight.Bold)
                        result.warnings.take(4).forEach { warning ->
                            Text(warning, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = resolvedRows.isNotEmpty() && !resolveBusy, onClick = { onImport(resolvedRows) }) {
                Text("Import ${resolvedRows.size} rows")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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
    initialSelection: ScryfallCardSelection? = null,
    onConfirm: (String, Int, DeckSection, ScryfallCardSelection?) -> Unit
) {
    var cardName by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var quantityText by rememberSaveable(initialQuantity) { mutableStateOf(initialQuantity.toString()) }
    var selectedSection by rememberSaveable(initialSection) { mutableStateOf(initialSection) }
    var lookupResult by remember { mutableStateOf(initialSelection) }
    var lookupMessage by remember { mutableStateOf(initialSelection?.summary("Resolved")) }
    var lookupBusy by remember { mutableStateOf(false) }
    var printingOptions by remember { mutableStateOf<List<ScryfallCardSelection>>(emptyList()) }
    var setCode by rememberSaveable(initialSelection?.setCode.orEmpty()) { mutableStateOf(initialSelection?.setCode.orEmpty()) }
    val scryfallClient = remember { ScryfallClient() }
    val quantity = quantityText.toIntOrNull() ?: 0
    val cleanName = cardName.trim()
    val cleanSetCode = setCode.trim().takeIf { it.isNotEmpty() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = cardName, onValueChange = { cardName = it }, label = { Text("Card name") }, singleLine = true)
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { value -> quantityText = value.filter { it.isDigit() }.take(3) },
                    label = { Text("Quantity") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = setCode,
                    onValueChange = { value -> setCode = value.uppercase().filter { it.isLetterOrDigit() }.take(8) },
                    label = { Text("Set code, optional") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        enabled = cleanName.isNotEmpty() && !lookupBusy,
                        onClick = {
                            lookupBusy = true
                            lookupMessage = "Looking up..."
                            printingOptions = emptyList()
                            Thread {
                                val result = scryfallClient.lookupCardOptions(cleanName, cleanSetCode)
                                result
                                    .onSuccess { options ->
                                        val selection = options.first()
                                        lookupResult = selection
                                        cardName = selection.oracleName
                                        setCode = selection.setCode
                                        selectedSection = sectionFromTypeLine(selection.typeLine, selectedSection)
                                        printingOptions = options
                                        lookupMessage = if (options.size > 1) {
                                            "Choose printing: ${options.size} matches"
                                        } else {
                                            selection.summary("Resolved")
                                        }
                                    }
                                    .onFailure { error ->
                                        lookupMessage = error.message ?: "Lookup failed"
                                    }
                                lookupBusy = false
                            }.start()
                        }
                    ) {
                        Text("Lookup")
                    }
                    Text(lookupMessage ?: "Unresolved", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                PrintingOptions(
                    options = printingOptions,
                    selected = lookupResult,
                    onSelect = { selection ->
                        lookupResult = selection
                        cardName = selection.oracleName
                        setCode = selection.setCode
                        selectedSection = sectionFromTypeLine(selection.typeLine, selectedSection)
                        lookupMessage = selection.summary("Resolved")
                    }
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
            TextButton(enabled = cleanName.isNotEmpty() && quantity > 0, onClick = { onConfirm(cleanName, quantity, selectedSection, lookupResult) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                Text("Type", style = MaterialTheme.typography.labelLarge)
                containerTypeRows().forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type.displayName()) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = cleanName.isNotEmpty(), onClick = { onConfirm(cleanName, selectedType) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun InventoryDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    initialQuantity: Int = 1,
    initialFoil: Boolean = false,
    onDismiss: () -> Unit,
    initialSelection: ScryfallCardSelection? = null,
    onConfirm: (String, Int, Boolean, ScryfallCardSelection?) -> Unit
) {
    var cardName by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var quantityText by rememberSaveable(initialQuantity) { mutableStateOf(initialQuantity.toString()) }
    var isFoil by rememberSaveable(initialFoil) { mutableStateOf(initialFoil) }
    var lookupResult by remember { mutableStateOf(initialSelection) }
    var lookupMessage by remember { mutableStateOf(initialSelection?.summary("Resolved")) }
    var lookupBusy by remember { mutableStateOf(false) }
    var printingOptions by remember { mutableStateOf<List<ScryfallCardSelection>>(emptyList()) }
    var setCode by rememberSaveable(initialSelection?.setCode.orEmpty()) { mutableStateOf(initialSelection?.setCode.orEmpty()) }
    val scryfallClient = remember { ScryfallClient() }
    val quantity = quantityText.toIntOrNull() ?: 0
    val cleanName = cardName.trim()
    val cleanSetCode = setCode.trim().takeIf { it.isNotEmpty() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = cardName, onValueChange = { cardName = it }, label = { Text("Card name") }, singleLine = true)
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { value -> quantityText = value.filter { it.isDigit() }.take(3) },
                    label = { Text("Quantity") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = setCode,
                    onValueChange = { value -> setCode = value.uppercase().filter { it.isLetterOrDigit() }.take(8) },
                    label = { Text("Set code, optional") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = isFoil,
                        onClick = { isFoil = !isFoil },
                        label = { Text("Foil") }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        enabled = cleanName.isNotEmpty() && !lookupBusy,
                        onClick = {
                            lookupBusy = true
                            lookupMessage = "Looking up..."
                            printingOptions = emptyList()
                            Thread {
                                val result = scryfallClient.lookupCardOptions(cleanName, cleanSetCode)
                                result
                                    .onSuccess { options ->
                                        val selection = options.first()
                                        lookupResult = selection
                                        cardName = selection.printingName
                                        setCode = selection.setCode
                                        printingOptions = options
                                        lookupMessage = if (options.size > 1) {
                                            "Choose printing: ${options.size} matches"
                                        } else {
                                            selection.summary("Resolved")
                                        }
                                    }
                                    .onFailure { error ->
                                        lookupMessage = error.message ?: "Lookup failed"
                                    }
                                lookupBusy = false
                            }.start()
                        }
                    ) {
                        Text("Lookup")
                    }
                    Text(lookupMessage ?: "Unresolved", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                PrintingOptions(
                    options = printingOptions,
                    selected = lookupResult,
                    onSelect = { selection ->
                        lookupResult = selection
                        cardName = selection.printingName
                        setCode = selection.setCode
                        lookupMessage = selection.summary("Resolved")
                    }
                )
            }
        },
        confirmButton = {
            TextButton(enabled = cleanName.isNotEmpty() && quantity > 0, onClick = { onConfirm(cleanName, quantity, isFoil, lookupResult) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ConfirmDeleteDialog(title: String, body: String, onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onDelete) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun StatRow(values: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { value ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF211A10))) {
                Text(value, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ComingSoon(title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("This panel is reserved for the desktop-first workflow.")
    }
}

private fun deckSectionRows(): List<List<DeckSection>> {
    return listOf(
        listOf(DeckSection.Commander, DeckSection.Creature, DeckSection.Artifact),
        listOf(DeckSection.Enchantment, DeckSection.Instant, DeckSection.Sorcery),
        listOf(DeckSection.Planeswalker, DeckSection.Battle, DeckSection.Land),
        listOf(DeckSection.Other)
    )
}

private fun containerTypeRows(): List<List<ContainerType>> {
    return listOf(
        listOf(ContainerType.Binder, ContainerType.Box, ContainerType.Set),
        listOf(ContainerType.Deck, ContainerType.Ordered, ContainerType.Proxy),
        listOf(ContainerType.Other)
    )
}

private data class DeckImportParseResult(
    val rows: List<ImportedDeckRow>,
    val warnings: List<String>
)

private data class CollectionImportParseResult(
    val rows: List<ImportedCollectionRow>,
    val warnings: List<String>
)

private fun parseDeckImport(rawText: String): DeckImportParseResult {
    val rows = mutableListOf<ImportedDeckRow>()
    val warnings = mutableListOf<String>()
    var currentSection = DeckSection.Other
    var ignoringSection = false

    rawText.lineSequence().forEachIndexed { index, rawLine ->
        val lineNumber = index + 1
        val line = rawLine.trim()
        if (line.isBlank() || line.startsWith("//") || line.startsWith("#")) return@forEachIndexed

        val section = sectionFromHeader(line)
        if (section != null) {
            currentSection = section
            ignoringSection = false
            return@forEachIndexed
        }
        if (isIgnoredImportHeader(line)) {
            ignoringSection = true
            warnings += "Line $lineNumber: ignored section '$line'"
            return@forEachIndexed
        }
        if (ignoringSection) return@forEachIndexed

        val commander = parseNamedCommanderLine(line)
        if (commander != null) {
            rows += ImportedDeckRow(quantity = 1, cardName = commander, section = DeckSection.Commander)
            return@forEachIndexed
        }

        val parsed = parseDeckCardLine(line)
        if (parsed == null) {
            warnings += "Line $lineNumber: could not parse '$line'"
        } else {
            rows += ImportedDeckRow(quantity = parsed.quantity, cardName = parsed.cardName, section = currentSection)
        }
    }

    return DeckImportParseResult(rows, warnings)
}

private fun parseCollectionImport(rawText: String): CollectionImportParseResult {
    if (looksLikeCollectionCsv(rawText)) {
        return parseCollectionCsvImport(rawText)
    }

    val rows = mutableListOf<ImportedCollectionRow>()
    val warnings = mutableListOf<String>()

    rawText.lineSequence().forEachIndexed { index, rawLine ->
        val lineNumber = index + 1
        val line = rawLine.trim()
        if (line.isBlank() || line.startsWith("//") || line.startsWith("#")) return@forEachIndexed
        if (looksLikeCollectionHeader(line)) return@forEachIndexed

        val parsed = parseCollectionCardLine(line)
        if (parsed == null) {
            warnings += "Line $lineNumber: could not parse '$line'"
        } else {
            rows += ImportedCollectionRow(
                quantity = parsed.quantity,
                cardName = parsed.cardName,
                setCode = parsed.setCode,
                collectorNumber = parsed.collectorNumber,
                isFoil = parsed.isFoil
            )
        }
    }

    return CollectionImportParseResult(rows, warnings)
}

private fun parseCollectionCsvImport(rawText: String): CollectionImportParseResult {
    val records = parseCsvRecords(rawText)
    if (records.isEmpty()) return CollectionImportParseResult(emptyList(), emptyList())

    val headers = records.first().map { it.trim() }
    val headerIndex = headers
        .mapIndexed { index, header -> header.normalizedCsvHeader() to index }
        .toMap()
    val rows = mutableListOf<ImportedCollectionRow>()
    val warnings = mutableListOf<String>()

    records.drop(1).forEachIndexed { index, record ->
        val lineNumber = index + 2
        fun field(name: String): String? {
            val fieldIndex = headerIndex[name.normalizedCsvHeader()] ?: return null
            return record.getOrNull(fieldIndex)?.trim()?.takeIf { it.isNotBlank() }
        }

        val cardName = field("Card Name")
        val quantity = field("Quantity")?.toIntOrNull()
        if (cardName == null || quantity == null || quantity <= 0) {
            warnings += "Line $lineNumber: missing card name or quantity"
            return@forEachIndexed
        }

        rows += ImportedCollectionRow(
            quantity = quantity,
            cardName = cardName,
            setCode = field("Set Code")?.uppercase(),
            collectorNumber = field("Collector Number"),
            scryfallCardId = field("Scryfall ID"),
            isFoil = field("Finish").isFoilFinish()
        )
    }

    return CollectionImportParseResult(rows, warnings)
}

private data class ParsedDeckLine(
    val quantity: Int,
    val cardName: String
)

private data class ParsedCollectionLine(
    val quantity: Int,
    val cardName: String,
    val setCode: String?,
    val collectorNumber: String?,
    val isFoil: Boolean
)

private fun parseDeckCardLine(line: String): ParsedDeckLine? {
    val match = Regex("""^(\d+)\s*x?\s+(.+)$""", RegexOption.IGNORE_CASE).find(line) ?: return null
    val quantity = match.groupValues[1].toIntOrNull()?.takeIf { it > 0 } ?: return null
    val cardName = cleanupImportedCardName(match.groupValues[2])
    if (cardName.isBlank()) return null
    return ParsedDeckLine(quantity, cardName)
}

private fun parseCollectionCardLine(line: String): ParsedCollectionLine? {
    val match = Regex("""^(\d+)\s*x?\s+(.+)$""", RegexOption.IGNORE_CASE).find(line) ?: return null
    val quantity = match.groupValues[1].toIntOrNull()?.takeIf { it > 0 } ?: return null
    var body = match.groupValues[2].trim()

    val foilResult = stripFoilMarker(body)
    body = foilResult.first
    val isFoil = foilResult.second

    val collectorMatch = Regex("""(?:^|\s)#([^\s]+)\s*$""").find(body)
    val collectorNumber = collectorMatch?.groupValues?.get(1)
    if (collectorMatch != null) {
        body = body.removeRange(collectorMatch.range).trim()
    }

    val setMatch = Regex("""\(([A-Za-z0-9]{2,8})\)\s*$""").find(body)
    val setCode = setMatch?.groupValues?.get(1)?.uppercase()
    if (setMatch != null) {
        body = body.removeRange(setMatch.range).trim()
    }

    val cardName = body.trim()
    if (cardName.isBlank()) return null
    return ParsedCollectionLine(
        quantity = quantity,
        cardName = cardName,
        setCode = setCode,
        collectorNumber = collectorNumber,
        isFoil = isFoil
    )
}

private fun stripFoilMarker(value: String): Pair<String, Boolean> {
    val patterns = listOf(
        Regex("""\s*\*F\*\s*$""", RegexOption.IGNORE_CASE),
        Regex("""\s*\[F\]\s*$""", RegexOption.IGNORE_CASE),
        Regex("""\s*\(F\)\s*$""", RegexOption.IGNORE_CASE),
        Regex("""\s+\bfoil\b\s*$""", RegexOption.IGNORE_CASE),
        Regex("""^\s*\bfoil\b\s+""", RegexOption.IGNORE_CASE)
    )
    var stripped = value
    var isFoil = false
    patterns.forEach { pattern ->
        if (pattern.containsMatchIn(stripped)) {
            stripped = stripped.replace(pattern, " ").trim()
            isFoil = true
        }
    }
    return stripped to isFoil
}

private fun looksLikeCollectionHeader(line: String): Boolean {
    val normalized = line.trim().trimEnd(':').lowercase()
    return normalized in setOf("cards", "collection", "inventory", "main", "owned")
}

private fun looksLikeCollectionCsv(rawText: String): Boolean {
    val firstLine = rawText.lineSequence().firstOrNull { it.isNotBlank() } ?: return false
    val headers = parseCsvRecord(firstLine).map { it.normalizedCsvHeader() }.toSet()
    return "cardname" in headers && "quantity" in headers
}

private fun parseCsvRecords(rawText: String): List<List<String>> {
    val records = mutableListOf<List<String>>()
    val field = StringBuilder()
    val record = mutableListOf<String>()
    var inQuotes = false
    var index = 0

    fun endField() {
        record += field.toString()
        field.clear()
    }

    fun endRecord() {
        endField()
        if (record.any { it.isNotBlank() }) records += record.toList()
        record.clear()
    }

    while (index < rawText.length) {
        val char = rawText[index]
        when {
            char == '"' && inQuotes && rawText.getOrNull(index + 1) == '"' -> {
                field.append('"')
                index++
            }
            char == '"' -> inQuotes = !inQuotes
            char == ',' && !inQuotes -> endField()
            (char == '\n' || char == '\r') && !inQuotes -> {
                if (char == '\r' && rawText.getOrNull(index + 1) == '\n') index++
                endRecord()
            }
            else -> field.append(char)
        }
        index++
    }
    if (field.isNotEmpty() || record.isNotEmpty()) endRecord()

    return records
}

private fun parseCsvRecord(line: String): List<String> {
    return parseCsvRecords(line).firstOrNull().orEmpty()
}

private fun String.normalizedCsvHeader(): String {
    return lowercase().replace(Regex("""[^a-z0-9]"""), "")
}

private fun String?.isFoilFinish(): Boolean {
    val finish = this?.trim()?.lowercase().orEmpty()
    return finish == "foil" || finish == "etched" || finish == "foil etched"
}

private fun chooseCsvFile(): File? {
    val dialog = FileDialog(null as Frame?, "Import collection CSV", FileDialog.LOAD)
    dialog.file = "*.csv"
    dialog.isVisible = true
    val directory = dialog.directory ?: return null
    val file = dialog.file ?: return null
    return File(directory, file)
}

private fun parseNamedCommanderLine(line: String): String? {
    val match = Regex("""^commander\s*:\s*(.+)$""", RegexOption.IGNORE_CASE).find(line) ?: return null
    return cleanupImportedCardName(match.groupValues[1]).takeIf { it.isNotBlank() }
}

private fun cleanupImportedCardName(value: String): String {
    return value
        .replace(Regex("""\s*\[[^\]]+]"""), "")
        .replace(Regex("""\s*\([^)]+\)"""), "")
        .trim()
}

private fun sectionFromHeader(line: String): DeckSection? {
    val normalized = line
        .trim()
        .trimEnd(':')
        .lowercase()
        .replace(Regex("""\s*\(\d+\)$"""), "")
        .trim()

    return when (normalized) {
        "commander", "commanders" -> DeckSection.Commander
        "creature", "creatures" -> DeckSection.Creature
        "artifact", "artifacts" -> DeckSection.Artifact
        "enchantment", "enchantments" -> DeckSection.Enchantment
        "instant", "instants" -> DeckSection.Instant
        "sorcery", "sorceries" -> DeckSection.Sorcery
        "planeswalker", "planeswalkers" -> DeckSection.Planeswalker
        "battle", "battles" -> DeckSection.Battle
        "land", "lands" -> DeckSection.Land
        "other", "misc" -> DeckSection.Other
        else -> null
    }
}

private fun sectionFromTypeLine(typeLine: String?, currentSection: DeckSection): DeckSection {
    if (currentSection == DeckSection.Commander) return DeckSection.Commander
    val normalized = typeLine?.lowercase().orEmpty()
    return when {
        "land" in normalized -> DeckSection.Land
        "creature" in normalized -> DeckSection.Creature
        "instant" in normalized -> DeckSection.Instant
        "sorcery" in normalized -> DeckSection.Sorcery
        "planeswalker" in normalized -> DeckSection.Planeswalker
        "battle" in normalized -> DeckSection.Battle
        "artifact" in normalized -> DeckSection.Artifact
        "enchantment" in normalized -> DeckSection.Enchantment
        else -> currentSection
    }
}

private fun isIgnoredImportHeader(line: String): Boolean {
    val normalized = line.trim().trimEnd(':').lowercase()
    return normalized in setOf("sideboard", "maybeboard", "considering", "tokens")
}

private fun String.importLookupKey(): String {
    return trim().lowercase().replace(Regex("\\s+"), " ")
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

private enum class DesktopTab(val label: String) {
    Decks("Decks"),
    Collection("Collection"),
    Search("Search"),
    Sync("Sync")
}

private val CynfulDesktopColors = darkColorScheme(
    primary = Color(0xFFE0A52F),
    secondary = Color(0xFFD2B06D),
    tertiary = Color(0xFFC12A1D),
    background = Color(0xFF050505),
    surface = Color(0xFF111111),
    surfaceVariant = Color(0xFF1C1712),
    onPrimary = Color(0xFF140D02),
    onSecondary = Color(0xFF140D02),
    onTertiary = Color.White,
    onBackground = Color(0xFFF4E8D0),
    onSurface = Color(0xFFF4E8D0),
    onSurfaceVariant = Color(0xFFD8C49A),
    outline = Color(0xFF7B6340)
)
