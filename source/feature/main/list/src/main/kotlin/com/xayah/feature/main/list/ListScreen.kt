package com.xayah.feature.main.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.core.ui.component.InnerTopSpacer
import com.xayah.core.ui.token.SizeTokens

@Composable
fun ListRoute(
    viewModel: ListViewModel = hiltViewModel(),
) {
    val uiState: ListUiState by viewModel.uiState.collectAsStateWithLifecycle()

    ListScreen(uiState = uiState, onSearchQueryChanged = viewModel::search, onTabClick = viewModel::setUser)
}

@Composable
internal fun ListScreen(
    uiState: ListUiState,
    onSearchQueryChanged: (String) -> Unit,
    onTabClick: (index: Int) -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    if (uiState is ListUiState.Success) {
        ListFilterSheet(
            target = uiState.target,
            opType = uiState.opType,
            isShow = showFilterSheet,
            onDismiss = {
                showFilterSheet = false
            }
        )
    }

    Scaffold(
        topBar = {
            ListTopBar(
                uiState = uiState,
                onFilter = {
                    showFilterSheet = true
                },
                onSearchQueryChanged = onSearchQueryChanged,
                onTabClick = onTabClick
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {

        },
    ) { innerPadding ->
        Column {
            InnerTopSpacer(innerPadding = innerPadding)

            Box(modifier = Modifier.weight(1f), content = {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = scrollState,
                ) {
                    item(key = "-1") {
                        Spacer(modifier = Modifier.size(SizeTokens.Level1))
                    }

                    listItems(uiState = uiState)
                }
            })
        }
    }
}
