package com.xayah.databackup.feature.restore.messages

import androidx.lifecycle.viewModelScope
import com.xayah.databackup.data.restore.RestoreSession
import com.xayah.databackup.util.BaseViewModel
import com.xayah.databackup.util.filterMms
import com.xayah.databackup.util.filterSms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class UiState(val selectedIndex: Int = 0)

class MessagesViewModel(
    private val session: RestoreSession,
) : BaseViewModel() {
    private val sharingStarted = SharingStarted.WhileSubscribed(5_000)
    val state = session.state
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    val sms = combine(state, searchText) { state, query ->
        state.inventory?.sms.orEmpty().filterSms(query)
    }.stateIn(viewModelScope, sharingStarted, state.value.inventory?.sms.orEmpty())
    val mms = combine(state, searchText) { state, query ->
        state.inventory?.mms.orEmpty().filterMms(query)
    }.stateIn(viewModelScope, sharingStarted, state.value.inventory?.mms.orEmpty())
    val items = combine(sms, mms) { sms, mms -> (sms.keys + mms.keys).toList() }
        .stateIn(viewModelScope, sharingStarted, emptyList())
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    val selectedIndex = uiState.map { it.selectedIndex }.stateIn(viewModelScope, sharingStarted, 0)
    val visibleItems = combine(sms, mms, selectedIndex) { sms, mms, index ->
        (if (index == 0) sms.keys else mms.keys).toList()
    }.stateIn(viewModelScope, sharingStarted, emptyList())

    fun selectTab(index: Int) {
        withLock(Dispatchers.Default) {
            _uiState.update { it.copy(selectedIndex = index) }
        }
    }
    fun selectAllMessages() {
        withLock(Dispatchers.Default) {
            val messages = visibleItems.value
            session.selectItems(messages.toSet(), messages.any { it !in state.value.selected })
        }
    }

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
}
