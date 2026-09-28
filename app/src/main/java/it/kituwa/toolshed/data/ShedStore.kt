package it.kituwa.toolshed.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.shedDataStore: DataStore<Preferences> by preferencesDataStore(name = "toolshed")

/** What the person has told the app about their own shed. */
data class ShedState(
    val shortlist: Set<String> = emptySet(),
    val running: Set<String> = emptySet(),
    val machineId: String = DEFAULT_MACHINE,
) {
    val isEmpty: Boolean get() = shortlist.isEmpty() && running.isEmpty()
}

const val DEFAULT_MACHINE = "minipc"

/**
 * Local-only persistence. Three keys, no networking, no account. Everything
 * here is a convenience for planning; losing it costs nothing but the marks.
 */
class ShedStore(context: Context) {

    private val store = context.applicationContext.shedDataStore

    /**
     * DataStore throws [IOException] when the backing file is unreadable or was
     * written by a different app. Left unhandled that surfaces as a crash on
     * the first composition, so a corrupt preferences file falls back to empty
     * rather than taking the app down. The marks are convenience state and are
     * cheap to re-enter.
     */
    val state: Flow<ShedState> = store.data
        .catch { cause ->
            if (cause is IOException) emit(emptyPreferences()) else throw cause
        }
        .map { prefs ->
            ShedState(
                shortlist = prefs[SHORTLIST].orEmpty(),
                running = prefs[RUNNING].orEmpty(),
                machineId = prefs[MACHINE] ?: DEFAULT_MACHINE,
            )
        }

    suspend fun toggleShortlist(id: String) = mutate { it.toggle(SHORTLIST, id) }

    suspend fun toggleRunning(id: String) = mutate { it.toggle(RUNNING, id) }

    suspend fun setMachine(id: String) = mutate { it[MACHINE] = id }

    suspend fun clearMarks() = mutate {
        it[SHORTLIST] = emptySet()
        it[RUNNING] = emptySet()
    }

    private suspend fun mutate(block: (MutablePreferences) -> Unit) {
        try {
            store.edit(block)
        } catch (cause: IOException) {
            // Nothing useful to do: the only way to hit this is a disk problem
            // we cannot fix from here, and the state is not important enough to
            // crash the app over.
        }
    }

    private fun MutablePreferences.toggle(key: Preferences.Key<Set<String>>, id: String) {
        this[key] = this[key].orEmpty().let { if (id in it) it - id else it + id }
    }

    private companion object {
        val SHORTLIST = stringSetPreferencesKey("shortlist")
        val RUNNING = stringSetPreferencesKey("running")
        val MACHINE = stringPreferencesKey("machine")
    }
}
