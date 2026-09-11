package com.xayah.databackup.data.restore

import com.xayah.databackup.data.rustic.RusticSourceCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RestoreSession(initialState: RestoreSessionState = RestoreSessionState()) {
    private val mutableState = MutableStateFlow(initialState)
    val state = mutableState.asStateFlow()

    internal fun updateState(state: RestoreSessionState) {
        mutableState.value = state
    }

    fun selectCategory(category: RestoreCategory, checked: Boolean) {
        mutableState.update { it.selectCategory(category, checked) }
    }

    fun selectItem(id: String, checked: Boolean) {
        mutableState.update { it.selectItem(id, checked) }
    }

    fun selectItems(ids: Set<String>, checked: Boolean) {
        mutableState.update { it.selectItems(ids, checked) }
    }

    fun selectAppPart(id: String, part: RusticSourceCategory, checked: Boolean) {
        mutableState.update { it.selectAppPart(id, part, checked) }
    }

    fun selectAppParts(ids: Set<String>, parts: Set<RusticSourceCategory>, checked: Boolean) {
        mutableState.update { it.selectAppParts(ids, parts, checked) }
    }
}
