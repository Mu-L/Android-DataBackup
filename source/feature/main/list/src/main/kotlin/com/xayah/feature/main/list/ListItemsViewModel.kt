package com.xayah.feature.main.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xayah.core.data.repository.AppsRepo
import com.xayah.core.model.DataState
import com.xayah.core.model.database.PackageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListItemsViewModel @Inject constructor(
    private val appsRepo: AppsRepo,
) : ViewModel() {
    fun onSelectedChanged(id: Long, selected: Boolean) {
        viewModelScope.launch {
            appsRepo.selectApp(id, selected)
        }
    }

    fun onChangeFlag(id: Long, flag: Int) {
        viewModelScope.launch {
            when (flag) {
                PackageEntity.FLAG_APK -> {
                    appsRepo.selectDataItems(
                        id = id,
                        apk = DataState.NotSelected,
                        user = DataState.Selected,
                        userDe = DataState.Selected,
                        data = DataState.Selected,
                        obb = DataState.Selected,
                        media = DataState.Selected,
                    )
                }

                PackageEntity.FLAG_ALL -> {
                    appsRepo.selectDataItems(
                        id = id,
                        apk = DataState.Selected,
                        user = DataState.NotSelected,
                        userDe = DataState.NotSelected,
                        data = DataState.NotSelected,
                        obb = DataState.NotSelected,
                        media = DataState.NotSelected,
                    )
                }

                else -> {
                    appsRepo.selectDataItems(
                        id = id,
                        apk = DataState.Selected,
                        user = DataState.Selected,
                        userDe = DataState.Selected,
                        data = DataState.Selected,
                        obb = DataState.Selected,
                        media = DataState.Selected,
                    )
                }
            }
        }
    }
}

