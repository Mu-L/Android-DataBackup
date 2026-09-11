package com.xayah.databackup.data.restore

import com.xayah.databackup.data.rustic.RusticSourceCategory

internal fun RestoreSessionState.selectItem(id: String, checked: Boolean): RestoreSessionState {
    return selectItems(setOf(id), checked)
}

internal fun RestoreSessionState.selectCategory(category: RestoreCategory, checked: Boolean): RestoreSessionState =
    selectItems(inventory?.ids(category).orEmpty(), checked)

internal fun RestoreSessionState.selectAppPart(id: String, part: RusticSourceCategory, checked: Boolean): RestoreSessionState {
    val available = inventory?.availableAppParts?.get(id) ?: return this
    if (part !in available) return this
    val parts = appParts[id].orEmpty()
    val updated = if (checked) parts + part else parts - part
    val selected = if (updated.isEmpty()) selected - id else selected + id
    return copy(selected = selected, appParts = appParts + (id to updated))
}

internal fun RestoreSessionState.selectItems(ids: Set<String>, checked: Boolean): RestoreSessionState {
    val availableIds = ids.intersect(inventory?.allIds.orEmpty())
    val parts = inventory?.apps.orEmpty().keys.intersect(availableIds).associateWith {
        if (checked) inventory?.availableAppParts?.get(it).orEmpty() else emptySet()
    }
    return copy(selected = if (checked) selected + availableIds else selected - availableIds, appParts = appParts + parts)
}

internal fun RestoreSessionState.selectAppParts(ids: Set<String>, parts: Set<RusticSourceCategory>, checked: Boolean): RestoreSessionState {
    val updated = inventory?.apps.orEmpty().keys.intersect(ids).associateWith { id ->
        val available = inventory?.availableAppParts?.get(id).orEmpty().intersect(parts)
        val previous = appParts[id].orEmpty()
        if (checked) previous + available else previous - available
    }
    return copy(
        selected = selected - updated.keys + updated.filterValues { it.isNotEmpty() }.keys,
        appParts = appParts + updated,
    )
}
