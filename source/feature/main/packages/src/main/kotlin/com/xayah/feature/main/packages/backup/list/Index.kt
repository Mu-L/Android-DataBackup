package com.xayah.feature.main.packages.backup.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Rule
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.core.datastore.readLoadSystemApps
import com.xayah.core.datastore.saveLoadSystemApps
import com.xayah.core.model.SortType
import com.xayah.core.ui.component.BodyLargeText
import com.xayah.core.ui.component.Divider
import com.xayah.core.ui.component.IconButton
import com.xayah.core.ui.component.InnerBottomSpacer
import com.xayah.core.ui.component.LocalSlotScope
import com.xayah.core.ui.component.ModalActionDropdownMenu
import com.xayah.core.ui.component.ModalBottomSheet
import com.xayah.core.ui.component.PackageItem
import com.xayah.core.ui.component.SearchBar
import com.xayah.core.ui.component.SetOnResume
import com.xayah.core.ui.component.TitleLargeText
import com.xayah.core.ui.component.confirm
import com.xayah.core.ui.component.paddingHorizontal
import com.xayah.core.ui.component.paddingStart
import com.xayah.core.ui.component.paddingVertical
import com.xayah.core.ui.material3.pullrefresh.PullRefreshIndicator
import com.xayah.core.ui.material3.pullrefresh.pullRefresh
import com.xayah.core.ui.material3.pullrefresh.rememberPullRefreshState
import com.xayah.core.ui.model.ActionMenuItem
import com.xayah.core.ui.route.MainRoutes
import com.xayah.core.ui.token.SizeTokens
import com.xayah.core.ui.util.LocalNavController
import com.xayah.core.util.navigateSingle
import com.xayah.feature.main.packages.DotLottieView
import com.xayah.feature.main.packages.ListScaffold
import com.xayah.feature.main.packages.R
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalLayoutApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun PagePackagesBackupList() {
    val context = LocalContext.current
    val navController = LocalNavController.current!!
    val dialogState = LocalSlotScope.current!!.dialogSlot
    val viewModel = hiltViewModel<IndexViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val refreshState by viewModel.refreshState.collectAsStateWithLifecycle()
    val displayPackagesState by viewModel.displayPackagesState.collectAsStateWithLifecycle()
    val packagesState by viewModel.packagesState.collectAsStateWithLifecycle()
    val packagesSelectedState by viewModel.packagesSelectedState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(canScroll = { false })
    val scrollState = rememberLazyListState()
    val srcPackagesEmptyState by viewModel.srcPackagesEmptyState.collectAsStateWithLifecycle()
    val isRefreshing = uiState.isRefreshing
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = { viewModel.emitIntentOnIO(IndexUiIntent.OnRefresh) })
    var fabHeight: Float by remember { mutableFloatStateOf(0F) }
    val loadSystemApps by context.readLoadSystemApps().collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(null) {
        viewModel.emitIntentOnIO(IndexUiIntent.GetUsers)
    }

    SetOnResume {
        viewModel.emitIntentOnIO(IndexUiIntent.OnFastRefresh)
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    showBottomSheet = false
                }
            },
            sheetState = sheetState
        ) {
            var apkSelected by remember { mutableStateOf(true) }
            var userSelected by remember { mutableStateOf(true) }
            var userDeSelected by remember { mutableStateOf(true) }
            var dataSelected by remember { mutableStateOf(true) }
            var obbSelected by remember { mutableStateOf(true) }
            var mediaSelected by remember { mutableStateOf(true) }

            TitleLargeText(
                modifier = Modifier
                    .paddingHorizontal(SizeTokens.Level24)
                    .paddingVertical(SizeTokens.Level12),
                text = stringResource(id = R.string.filters)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = loadSystemApps,
                        onValueChange = {
                            scope.launch {
                                context.saveLoadSystemApps(loadSystemApps.not())
                            }
                        },
                        role = Role.Checkbox
                    )
                    .paddingHorizontal(SizeTokens.Level24)
                    .paddingVertical(SizeTokens.Level12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = loadSystemApps, onCheckedChange = null)
                BodyLargeText(modifier = Modifier.paddingStart(SizeTokens.Level16), text = stringResource(id = R.string.load_system_apps))
            }


            val sortIndexState by viewModel.sortIndexState.collectAsStateWithLifecycle()
            val sortTypeState by viewModel.sortTypeState.collectAsStateWithLifecycle()
            Row(
                modifier = Modifier
                    .paddingHorizontal(SizeTokens.Level24)
                    .paddingVertical(SizeTokens.Level12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TitleLargeText(text = stringResource(id = R.string.sort))
                IconButton(
                    icon = when (sortTypeState) {
                        SortType.ASCENDING -> Icons.Outlined.ArrowDropUp
                        SortType.DESCENDING -> Icons.Outlined.ArrowDropDown
                    }
                ) {
                    scope.launch {
                        scrollState.scrollToItem(0)
                        viewModel.emitIntentOnIO(IndexUiIntent.SortByType(type = sortTypeState))
                    }
                }
            }
            val radioOptions = stringArrayResource(id = R.array.backup_sort_type_items_apps).toList()
            Column(Modifier.selectableGroup()) {
                radioOptions.forEachIndexed { index, text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (index == sortIndexState),
                                onClick = {
                                    scope.launch {
                                        scrollState.scrollToItem(0)
                                        viewModel.emitIntentOnIO(IndexUiIntent.SortByIndex(index = index))
                                    }
                                },
                                role = Role.RadioButton
                            )
                            .paddingHorizontal(SizeTokens.Level24)
                            .paddingVertical(SizeTokens.Level12),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (index == sortIndexState), onClick = null)
                        BodyLargeText(modifier = Modifier.paddingStart(SizeTokens.Level16), text = text)
                    }
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                maxItemsInEachRow = 2
            ) {
//                PackageDataChip(
//                    modifier = Modifier.weight(1f),
//                    dataType = DataType.PACKAGE_APK,
//                    selected = apkSelected
//                ) {
//                    apkSelected = apkSelected.not()
//                }
//                PackageDataChip(
//                    modifier = Modifier.weight(1f),
//                    dataType = DataType.PACKAGE_USER,
//                    selected = userSelected
//                ) {
//                    userSelected = userSelected.not()
//                }
//                PackageDataChip(
//                    modifier = Modifier.weight(1f),
//                    dataType = DataType.PACKAGE_USER_DE,
//                    selected = userDeSelected
//                ) {
//                    userDeSelected = userDeSelected.not()
//                }
//                PackageDataChip(
//                    modifier = Modifier.weight(1f),
//                    dataType = DataType.PACKAGE_DATA,
//                    selected = dataSelected
//                ) {
//                    dataSelected = dataSelected.not()
//                }
//                PackageDataChip(
//                    modifier = Modifier.weight(1f),
//                    dataType = DataType.PACKAGE_OBB,
//                    selected = obbSelected
//                ) {
//                    obbSelected = obbSelected.not()
//                }
//                PackageDataChip(
//                    modifier = Modifier.weight(1f),
//                    dataType = DataType.MEDIA_MEDIA,
//                    selected = mediaSelected
//                ) {
//                    mediaSelected = mediaSelected.not()
//                }
            }
        }
    }

    ListScaffold(
        scrollBehavior = scrollBehavior,
        progress = if (uiState.isLoading) -1F else null,
        title = stringResource(id = R.string.select_apps),
        subtitle = if (packagesSelectedState != 0 && isRefreshing.not()) "(${packagesSelectedState}/${packagesState.size})" else null,
        actions = {
            if (isRefreshing.not() && srcPackagesEmptyState.not()) {
                IconButton(icon = Icons.Outlined.FilterList) {
                    showBottomSheet = true
                }

                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    IconButton(icon = Icons.Outlined.Checklist) {
                        expanded = true
//                        viewModel.emitIntentOnIO(IndexUiIntent.SelectAll(uiState.selectAll.not()))
//                        viewModel.emitStateOnMain(uiState.copy(selectAll = uiState.selectAll.not()))
                    }

                    ModalActionDropdownMenu(expanded = expanded, actionList = listOf(
                        ActionMenuItem(
                            title = stringResource(id = R.string.select_all),
                            icon = Icons.Rounded.CheckBox,
                            enabled = true,
                            secondaryMenu = listOf(),
                            onClick = {
                                viewModel.emitIntentOnIO(IndexUiIntent.SelectAll(true))
                                expanded = false
                            }
                        ),
                        ActionMenuItem(
                            title = "Unselect all",
                            icon = Icons.Rounded.CheckBoxOutlineBlank,
                            enabled = true,
                            secondaryMenu = listOf(),
                            onClick = {
                                viewModel.emitIntentOnIO(IndexUiIntent.SelectAll(false))
                                expanded = false
                            }
                        ),
                        ActionMenuItem(
                            title = "Reverse selection",
                            icon = Icons.Rounded.RestartAlt,
                            enabled = true,
                            secondaryMenu = listOf(),
                            onClick = {
                                viewModel.emitIntentOnIO(IndexUiIntent.Reverse)
                                expanded = false
                            }
                        ),
                        ActionMenuItem(
                            title = "For selected...",
                            icon = Icons.Rounded.MoreVert,
                            enabled = packagesSelectedState != 0,
                            secondaryMenu = listOf(
                                ActionMenuItem(
                                    title = "Block",
                                    icon = Icons.Rounded.Block,
                                    enabled = packagesSelectedState != 0,
                                    secondaryMenu = listOf(),
                                    onClick = {
                                        viewModel.launchOnIO {
                                            if (dialogState.confirm(
                                                    title = context.getString(R.string.prompt),
                                                    text = context.getString(R.string.confirm_add_to_blacklist)
                                                )
                                            ) {
                                                viewModel.emitIntentOnIO(IndexUiIntent.BlockSelected)
                                            }
                                        }
                                        expanded = false
                                    }
                                ),
                                ActionMenuItem(
                                    title = "Detailed data items",
                                    icon = Icons.Rounded.Rule,
                                    enabled = packagesSelectedState != 0,
                                    secondaryMenu = listOf(),
                                    onClick = {
                                        showBottomSheet = true
                                        expanded = false
                                    }
                                )
                            ),
                            onClick = {
                            }
                        )
                    ), onDismissRequest = { expanded = false })
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            AnimatedVisibility(visible = packagesSelectedState != 0 && isRefreshing.not(), enter = scaleIn(), exit = scaleOut()) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.onSizeChanged { fabHeight = it.height * 1.5f },
                    onClick = {
                        navController.navigateSingle(MainRoutes.PackagesBackupProcessingGraph.route)
                    },
                    icon = { Icon(Icons.Rounded.ChevronRight, null) },
                    text = { Text(text = stringResource(id = R.string._continue)) },
                )
            }
        }
    ) {
        if (isRefreshing || srcPackagesEmptyState) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState, uiState.isLoading.not()),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.paddingHorizontal(SizeTokens.Level16), horizontalAlignment = Alignment.CenterHorizontally) {
                        DotLottieView(isRefreshing = isRefreshing, refreshState = refreshState)
                    }
                }
                InnerBottomSpacer(innerPadding = it)
            }
            PullRefreshIndicator(refreshing = isRefreshing, state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
        } else {
            Column {
                val userListState by viewModel.userListState.collectAsStateWithLifecycle()
                val userIdIndexState by viewModel.userIdIndexState.collectAsStateWithLifecycle()
                val displayPackagesSelectedState by viewModel.displayPackagesSelectedState.collectAsStateWithLifecycle()
                Column {
                    SearchBar(
                        modifier = Modifier
                            .paddingHorizontal(SizeTokens.Level16)
                            .paddingVertical(SizeTokens.Level8),
                        enabled = true,
                        placeholder = stringResource(id = R.string.search_bar_hint_packages),
                        onTextChange = {
                            viewModel.emitIntentOnIO(IndexUiIntent.FilterByKey(key = it))
                        }
                    )

                    PrimaryScrollableTabRow(
                        selectedTabIndex = userIdIndexState,
                        edgePadding = SizeTokens.Level0,
                        indicator = @Composable { tabPositions ->
                            if (userIdIndexState < tabPositions.size) {
                                val width by animateDpAsState(targetValue = tabPositions[userIdIndexState].contentWidth)
                                TabRowDefaults.PrimaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[userIdIndexState]),
                                    width = width,
                                    shape = CircleShape
                                )
                            }
                        },
                        divider = {
                            Divider(modifier = Modifier.fillMaxWidth())
                        }
                    ) {
                        userListState.forEachIndexed { index, user ->
                            Tab(
                                selected = userIdIndexState == index,
                                onClick = {
                                    viewModel.emitIntentOnIO(IndexUiIntent.SetUserId(index))
                                },
                                text = {
                                    BadgedBox(
                                        modifier = Modifier.fillMaxSize(),
                                        badge = {
                                            val number = remember(displayPackagesSelectedState) {
                                                displayPackagesSelectedState[user.id]?.toString()
                                            }
                                            if (number != null) {
                                                Badge { Text(number) }
                                            }
                                        }
                                    ) {
                                        Text(text = "${user.name} (${user.id})", maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            )
                        }
                    }
                }

                Box {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .pullRefresh(pullRefreshState, uiState.isLoading.not()),
                        state = scrollState,
                    ) {
                        item(key = "-1") {
                            Spacer(modifier = Modifier.size(SizeTokens.Level1))
                        }

                        items(items = displayPackagesState, key = { "${uiState.uuid}-${it.id}" }) { item ->
                            Row(modifier = Modifier.animateItemPlacement()) {
                                PackageItem(
                                    item = item,
                                    onCheckedChange = { viewModel.emitIntentOnIO(IndexUiIntent.Select(item)) },
                                    onItemsIconClick = { flag ->
                                        viewModel.emitIntentOnIO(IndexUiIntent.ChangeFlag(flag, item))
                                    },
                                    onClick = {
                                        viewModel.emitIntentOnIO(IndexUiIntent.ToPageDetail(navController, item))
                                    }
                                )
                            }
                        }

                        if (packagesSelectedState != 0)
                            item {
                                with(LocalDensity.current) {
                                    Column {
                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(fabHeight.toDp())
                                        )
                                    }
                                }
                            }

                        item {
                            InnerBottomSpacer(innerPadding = it)
                        }
                    }
                    PullRefreshIndicator(refreshing = isRefreshing, state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
                }
            }
        }

        // TODO Issues of ScrollBar
        // ScrollBar(modifier = Modifier.align(Alignment.TopEnd), state = scrollState)
    }
}
