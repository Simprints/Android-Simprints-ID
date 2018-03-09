package com.simprints.id.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.data.DataManager;
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.tools.AlertLauncher;
import com.simprints.id.tools.InternalConstants;
import com.simprints.id.libdata.models.enums.REFUSAL_FORM_REASON;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.RefusalForm;

public class RefusalActivity extends AppCompatActivity {

    private Button submit;
    private REFUSAL_FORM_REASON reason;
    private EditText otherText;
    private AlertLauncher alertLauncher;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refusal);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Application app = ((Application) getApplication());
        dataManager = app.getDataManager();

        alertLauncher = new AlertLauncher(this);
        submit = findViewById(R.id.bt_submit_refusal_form);
        otherText = findViewById(R.id.et_other_refusal_text);

        otherText.clearFocus();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent resultData = new Intent();

                if (reason != null) {
                    RefusalForm refusalForm = new RefusalForm(reason.toString(), otherText.getText().toString());
                    try {
                        dataManager.saveRefusalForm(refusalForm);
                    } catch (UninitializedDataManagerError error) {
                        dataManager.logError(error);
                        alertLauncher.launch(ALERT_TYPE.UNEXPECTED_ERROR, 0);
                        return;
                    }
                    resultData.putExtra(Constants.SIMPRINTS_REFUSAL_FORM, refusalForm);
                }

                //We are really bailing out of Simprints ID so the result is 'cancelled'
                setResult(RESULT_CANCELED, resultData);
                finish();
            }
        });

        findViewById(R.id.bt_back_to_simprints).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(InternalConstants.RESULT_TRY_AGAIN);
                finish();
            }
        });

        otherText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!otherText.getText().toString().isEmpty()) {
                    submit.setEnabled(true);
                }
            }
        });

        otherText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submit.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    public void onRefusalSelectionClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        if (checked) {
            otherText.setEnabled(true);

            if (view.getId() == R.id.rb_other && !otherText.getText().toString().isEmpty())
                submit.setEnabled(true);
            else
                submit.setEnabled(false);

            if (view.getId() != R.id.rb_other)
                submit.setEnabled(true);
        }

        switch (view.getId()) {
            case R.id.rb_scannerNotHere:
                if (checked)
                    reason = REFUSAL_FORM_REASON.SCANNER_NOT_HERE;
                break;
            case R.id.rb_scannerNotWorking:
                if (checked)
                    reason = REFUSAL_FORM_REASON.SCANNER_NOT_WORKING;
                break;
            case R.id.rb_unableToCapture:
                if (checked)
                    reason = REFUSAL_FORM_REASON.UNABLE_TO_CAPTURE_GOOD_SCAN;
                break;
            case R.id.rb_unableToGive:
                if (checked)
                    reason = REFUSAL_FORM_REASON.UNABLE_TO_GIVE_PRINTS;
                break;
            case R.id.rb_refused:
                if (checked)
                    reason = REFUSAL_FORM_REASON.REFUSED;
                break;
            case R.id.rb_other:
                if (checked)
                    reason = REFUSAL_FORM_REASON.OTHER;
                break;
        }
    }

}
