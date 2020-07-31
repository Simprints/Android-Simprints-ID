package com.simprints.id.activities.settings.fingerselection

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.id.R
import kotlinx.android.synthetic.main.activity_finger_selection.*

class FingerSelectionActivity : BaseSplitActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finger_selection)

        fingerSelectionRecyclerView.layoutManager = LinearLayoutManager(this)
        fingerSelectionRecyclerView.adapter = FingerSelectionItemAdapter(this)
    }
}
