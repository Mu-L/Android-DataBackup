package com.xayah.databackup.feature.restore.contacts

import androidx.lifecycle.viewModelScope
import com.xayah.databackup.data.restore.RestoreSession
import com.xayah.databackup.util.BaseViewModel
import com.xayah.databackup.util.filterContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ContactsViewModel(
    private val session: RestoreSession,
) : BaseViewModel() {
    private val sharingStarted = SharingStarted.WhileSubscribed(5_000)
    val state = session.state
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    val items = combine(state, searchText) { state, query ->
        state.inventory?.contacts.orEmpty().filterContact(query)
    }.stateIn(viewModelScope, sharingStarted, state.value.inventory?.contacts.orEmpty())

    fun changeSearchText(text: String) {
        withLock(Dispatchers.Default) {
            _searchText.emit(text)
        }
    }

    fun selectItem(id: String, checked: Boolean) {
        withLock(Dispatchers.Default) {
            session.selectItem(id, checked)
        }
    }

    fun selectAll() {
        withLock(Dispatchers.Default) {
            val visibleItems = items.value
            session.selectItems(visibleItems.keys, visibleItems.keys.any { it !in state.value.selected })
        }
    }
}
