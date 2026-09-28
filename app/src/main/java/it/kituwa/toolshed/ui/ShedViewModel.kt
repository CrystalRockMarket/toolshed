package it.kituwa.toolshed.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.kituwa.toolshed.data.Catalog
import it.kituwa.toolshed.data.Category
import it.kituwa.toolshed.data.Fit
import it.kituwa.toolshed.data.Machine
import it.kituwa.toolshed.data.ResourceTotal
import it.kituwa.toolshed.data.ShedState
import it.kituwa.toolshed.data.ShedStore
import it.kituwa.toolshed.data.Service
import it.kituwa.toolshed.data.StarterStack
import it.kituwa.toolshed.data.evaluateFit
import it.kituwa.toolshed.data.totalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShedUiState(
    val query: String = "",
    val category: Category? = null,
    val shed: ShedState = ShedState(),
) {
    val visibleServices: List<Service> get() = Catalog.search(query, category)

    val machine: Machine get() = Machine.byId(shed.machineId)

    val shortlist: List<Service> get() = Catalog.byIds(shed.shortlist.toList())
    val running: List<Service> get() = Catalog.byIds(shed.running.toList())

    val shortlistTotal: ResourceTotal get() = totalOf(shortlist)
    val runningTotal: ResourceTotal get() = totalOf(running)

    val shortlistFit: Fit get() = evaluateFit(shortlistTotal, machine)
    val runningFit: Fit get() = evaluateFit(runningTotal, machine)
}

class ShedViewModel(application: Application) : AndroidViewModel(application) {

    private val store = ShedStore(application)

    private val local = MutableStateFlow(ShedUiState())

    val state: StateFlow<ShedUiState> = combine(local, store.state) { ui, shed ->
        ui.copy(shed = shed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShedUiState())

    fun setQuery(query: String) {
        local.value = local.value.copy(query = query)
    }

    fun setCategory(category: Category?) {
        local.value = local.value.copy(category = category)
    }

    fun toggleShortlist(id: String) {
        viewModelScope.launch { store.toggleShortlist(id) }
    }

    fun toggleRunning(id: String) {
        viewModelScope.launch { store.toggleRunning(id) }
    }

    fun setMachine(id: String) {
        viewModelScope.launch { store.setMachine(id) }
    }

    fun clearMarks() {
        viewModelScope.launch { store.clearMarks() }
    }

    fun service(id: String): Service? = Catalog.byId(id)

    fun stack(id: String): StarterStack? = Catalog.stacks.firstOrNull { it.id == id }
}
