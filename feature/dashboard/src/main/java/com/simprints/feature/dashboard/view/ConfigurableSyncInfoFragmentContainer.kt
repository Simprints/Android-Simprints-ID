package com.simprints.feature.dashboard.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoFragmentConfig

class ConfigurableSyncInfoFragmentContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val syncInfoFragmentConfig: SyncInfoFragmentConfig? = attrs?.let {
        var config: SyncInfoFragmentConfig? = null
        context.withStyledAttributes(attrs, R.styleable.FragmentContainerView) {
            config = SyncInfoFragmentConfig(
                isSyncInfoToolbarVisible = getBoolean(R.styleable.FragmentContainerView_isSyncInfoToolbarVisible, true),
                isSyncInfoStatusHeaderVisible = getBoolean(R.styleable.FragmentContainerView_isSyncInfoStatusHeaderVisible, false),
                isSyncInfoStatusHeaderSettingsButtonVisible = getBoolean(R.styleable.FragmentContainerView_isSyncInfoStatusHeaderSettingsButtonVisible, false),
                areSyncInfoSectionHeadersVisible = getBoolean(R.styleable.FragmentContainerView_areSyncInfoSectionHeadersVisible, true),
                isSyncInfoImageSyncVisible = getBoolean(R.styleable.FragmentContainerView_isSyncInfoImageSyncVisible, true),
                isSyncInfoRecordsImagesCombined = getBoolean(R.styleable.FragmentContainerView_isSyncInfoRecordsImagesCombined, false),
                isSyncInfoLogoutOnComplete = getBoolean(R.styleable.FragmentContainerView_isSyncInfoLogoutOnComplete, false),
                isSyncInfoModuleListVisible = getBoolean(R.styleable.FragmentContainerView_isSyncInfoModuleListVisible, true)
            )
        }
        config
    }

}
