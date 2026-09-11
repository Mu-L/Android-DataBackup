package com.xayah.databackup.feature.backup.apps

import android.content.pm.UserInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.databackup.R
import com.xayah.databackup.rootservice.RemoteRootService
import com.xayah.databackup.ui.component.SearchTextField
import com.xayah.databackup.ui.component.rememberFadingEdgeState
import com.xayah.databackup.ui.component.selection.AppFilterSheetContent
import com.xayah.databackup.ui.component.selection.AppFilterUserOption
import com.xayah.databackup.ui.component.selection.AppListItem
import com.xayah.databackup.ui.component.selection.AppSelectionMenu
import com.xayah.databackup.ui.component.surfaceTopAppBarColors
import com.xayah.databackup.ui.component.verticalFadingEdges
import com.xayah.databackup.util.DefStorageSize
import com.xayah.databackup.util.FilterBackupUser
import com.xayah.databackup.util.FiltersSystemAppsBackup
import com.xayah.databackup.util.FiltersUserAppsBackup
import com.xayah.databackup.util.KeyFiltersSystemAppsBackup
import com.xayah.databackup.util.KeyFiltersUserAppsBackup
import com.xayah.databackup.util.KeySortsTypeBackup
import com.xayah.databackup.util.LaunchedEffect
import com.xayah.databackup.util.Navigator
import com.xayah.databackup.util.SortsSelectedFirstBackup
import com.xayah.databackup.util.SortsSequenceBackup
import com.xayah.databackup.util.SortsTypeBackup
import com.xayah.databackup.util.popBackStackSafely
import com.xayah.databackup.util.readBoolean
import com.xayah.databackup.util.readEnum
import com.xayah.databackup.util.readInt
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.compose.koinViewModel

@Composable
fun BackupAppsScreen(
    navigator: Navigator,
    viewModel: AppsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val searchScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val allSelected by viewModel.allSelected.collectAsStateWithLifecycle()
    val selectedBytes by viewModel.selectedBytes.collectAsStateWithLifecycle()
    val filterSheetState = rememberModalBottomSheetState()
    var showFilterSheet by remember { mutableStateOf(false) }
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    var onSearch by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val normalLazyListState = rememberLazyListState()
    val searchLazyListState = rememberLazyListState()
    val activeLazyListState = if (onSearch) searchLazyListState else normalLazyListState

    val fadingEdgeState = rememberFadingEdgeState(activeLazyListState, label = "backupApps")

    LaunchedEffect(onSearch) {
        if (onSearch) {
            searchLazyListState.scrollToItem(0)
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(if (onSearch) searchScrollBehavior.nestedScrollConnection else scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        topBar = {
            AnimatedContent(onSearch) { target ->
                if (target) {
                    TopAppBar(
                        title = {
                            SearchTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp)
                                    .focusRequester(focusRequester),
                                value = searchText,
                                onClose = {
                                    onSearch = false
                                    viewModel.changeSearchText("")
                                }
                            ) { viewModel.changeSearchText(it) }
                        },
                        actions = {
                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_funnel),
                                    contentDescription = stringResource(R.string.filters)
                                )
                            }
                            SelectIconButton(viewModel = viewModel)
                        },
                        scrollBehavior = searchScrollBehavior,
                    )
                } else {
                    LargeTopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = stringResource(R.string.select_apps),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (selectedBytes == DefStorageSize) {
                                        stringResource(R.string.items_selected, allSelected, apps.size)
                                    } else {
                                        stringResource(R.string.items_selected_and_size, allSelected, apps.size, selectedBytes)
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { navigator.popBackStackSafely() }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_left),
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { onSearch = true }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                                    contentDescription = stringResource(R.string.search)
                                )
                            }
                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_funnel),
                                    contentDescription = stringResource(R.string.filters)
                                )
                            }
                            SelectIconButton(viewModel = viewModel)
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.surfaceTopAppBarColors(),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier) {
            Spacer(modifier = Modifier.size(innerPadding.calculateTopPadding()))

            AnimatedContent(targetState = apps.isEmpty()) { isAppsEmpty ->
                if (isAppsEmpty) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            modifier = Modifier.size(300.dp),
                            imageVector = ImageVector.vectorResource(R.drawable.img_empty),
                            contentDescription = stringResource(R.string.it_is_empty)
                        )
                        Text(
                            text = stringResource(R.string.it_is_empty),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.verticalFadingEdges(fadingEdgeState),
                        state = activeLazyListState
                    ) {
                        items(items = apps, key = { it.pkgUserKey }) { app ->
                            AppListItem(
                                modifier = Modifier.animateItem(),
                                context = context,
                                app = app,
                                onSelectAll = { viewModel.selectAll(app.packageName, app.userId, app.toggleableState) },
                                onSelectApk = { viewModel.selectApk(app.packageName, app.userId, it) },
                                onSelectInternalData = { viewModel.selectInternalData(app.packageName, app.userId, it) },
                                onSelectExternalData = { viewModel.selectExternalData(app.packageName, app.userId, it) },
                                onSelectAdditionalData = { viewModel.selectAdditionalData(app.packageName, app.userId, it) },
                            )
                        }

                        item(key = "-1") {
                            Spacer(modifier = Modifier.size(innerPadding.calculateBottomPadding()))
                        }
                    }
                }
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showFilterSheet = false
                },
                sheetState = filterSheetState
            ) {
                val filterUser by context.readInt(FilterBackupUser).collectAsStateWithLifecycle(initialValue = FilterBackupUser.second)
                val sortsType by context.readEnum(SortsTypeBackup).collectAsStateWithLifecycle(initialValue = SortsTypeBackup.second)
                val sequenceBackup by context.readEnum(SortsSequenceBackup).collectAsStateWithLifecycle(initialValue = SortsSequenceBackup.second)
                val filtersUserApps by context.readBoolean(FiltersUserAppsBackup)
                    .collectAsStateWithLifecycle(initialValue = FiltersUserAppsBackup.second)
                val filtersSystemApps by context.readBoolean(FiltersSystemAppsBackup)
                    .collectAsStateWithLifecycle(initialValue = FiltersSystemAppsBackup.second)
                val selectedFirst by context.readBoolean(SortsSelectedFirstBackup)
                    .collectAsStateWithLifecycle(initialValue = SortsSelectedFirstBackup.second)
                var users by remember { mutableStateOf(listOf<UserInfo>()) }
                val filterUsers = remember(users) {
                    users.map { AppFilterUserOption(id = it.id, name = it.name) }
                }

                LaunchedEffect(context = Dispatchers.Default, null) {
                    users = RemoteRootService.getUsers()
                }

                AppFilterSheetContent(
                    modifier = Modifier.fillMaxWidth(),
                    users = filterUsers,
                    selectedUserId = filterUser,
                    sortsType = sortsType,
                    sortsSequence = sequenceBackup,
                    selectedFirst = selectedFirst,
                    filtersUserApps = filtersUserApps,
                    filtersSystemApps = filtersSystemApps,
                    onUserClick = { user ->
                        users.firstOrNull { it.id == user.id }?.let {
                            viewModel.changeUser(filterUser, it)
                        }
                    },
                    onSequenceClick = { viewModel.changeSequence(sequenceBackup) },
                    onSelectedFirstClick = { viewModel.changeSelectedFirst(selectedFirst.not()) },
                    onSortTypeClick = { type ->
                        viewModel.changeSort(sortsType == type, KeySortsTypeBackup, type)
                    },
                    onFilterUserAppsClick = {
                        if (filtersUserApps.not() || filtersSystemApps) {
                            viewModel.changeFilter(KeyFiltersUserAppsBackup, filtersUserApps.not())
                        }
                    },
                    onFilterSystemAppsClick = {
                        if (filtersSystemApps.not() || filtersUserApps) {
                            viewModel.changeFilter(KeyFiltersSystemAppsBackup, filtersSystemApps.not())
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SelectIconButton(viewModel: AppsViewModel) {
    val apkAllSelected by viewModel.apkAllSelected.collectAsStateWithLifecycle()
    val dataAllSelected by viewModel.dataAllSelected.collectAsStateWithLifecycle()
    val intDataAllSelected by viewModel.intDataAllSelected.collectAsStateWithLifecycle()
    val extDataAllSelected by viewModel.extDataAllSelected.collectAsStateWithLifecycle()
    val addlDataAllSelected by viewModel.addlDataAllSelected.collectAsStateWithLifecycle()

    AppSelectionMenu(
        apkAllSelected = apkAllSelected,
        dataAllSelected = dataAllSelected,
        intDataAllSelected = intDataAllSelected,
        extDataAllSelected = extDataAllSelected,
        addlDataAllSelected = addlDataAllSelected,
        onSelectAllApk = viewModel::selectAllApk,
        onSelectAllData = viewModel::selectAllData,
        onSelectAllIntData = viewModel::selectAllIntData,
        onSelectAllExtData = viewModel::selectAllExtData,
        onSelectAllAddlData = viewModel::selectAllAddlData,
    )
}
