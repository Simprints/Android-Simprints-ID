package com.simprints.infra.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.simprints.infra.uibase.R

class PermissionRequestView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val title: TextView
    private val body: TextView
    private val actionButton: Button

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_camera_permission_request, this, true)

        title = findViewById(R.id.permissionTitle)
        body = findViewById(R.id.permissionBody)
        actionButton = findViewById(R.id.permissionAction)
    }

    fun init(title: String, body: String, buttonText: String, onClickListener: OnClickListener) {
        this.title.text = title
        this.body.text = body
        this.actionButton.text = buttonText
        this.actionButton.setOnClickListener(onClickListener)
    }

    fun init(@StringRes title: Int, @StringRes body: Int, @StringRes buttonText: Int, onClickListener: OnClickListener) {
        this.title.setText(title)
        this.body.setText(body)
        this.actionButton.setText(buttonText)
        this.actionButton.setOnClickListener(onClickListener)
    }
}
