package com.flexplayer.app.ui.share

import androidx.lifecycle.ViewModel
import com.flexplayer.app.sharing.SmartShareManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareViewModelHolder @Inject constructor(
    val smartShare: SmartShareManager
) : ViewModel()
