package com.simprints.id.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.simprints.id.R;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;
import com.simprints.libcommon.RefusalForm;

import static com.simprints.libcommon.RefusalForm.Reason;
import static com.simprints.libcommon.RefusalForm.Reason.OTHER;
import static com.simprints.libcommon.RefusalForm.Reason.REFUSED;
import static com.simprints.libcommon.RefusalForm.Reason.SCANNER_NOT_HERE;
import static com.simprints.libcommon.RefusalForm.Reason.SCANNER_NOT_WORKING;
import static com.simprints.libcommon.RefusalForm.Reason.UNABLE_TO_CAPTURE_GOOD_SCAN;
import static com.simprints.libcommon.RefusalForm.Reason.UNABLE_TO_GIVE_PRINTS;

public class RefusalActivity extends AppCompatActivity {
    private Button submit;
    private Reason reason;
    private EditText otherText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refusal);
        submit = (Button) findViewById(R.id.bt_submit_refusal_form);
        otherText = (EditText) findViewById(R.id.et_other_refusal_text);

        otherText.clearFocus();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefusalForm refusalForm = new RefusalForm();
                refusalForm.setReason(reason);
                refusalForm.setOtherText(otherText.getText().toString());
                AppState.getInstance().setRefusalForm(refusalForm);

                //We are really bailing out of Simprints ID so the result is 'cancelled'
                setResult(RESULT_CANCELED);
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

    public void onRadioButtonClicked(View view) {
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
                    reason = SCANNER_NOT_HERE;
                break;
            case R.id.rb_scannerNotWorking:
                if (checked)
                    reason = SCANNER_NOT_WORKING;
                break;
            case R.id.rb_unableToCapture:
                if (checked)
                    reason = UNABLE_TO_CAPTURE_GOOD_SCAN;
                break;
            case R.id.rb_unableToGive:
                if (checked)
                    reason = UNABLE_TO_GIVE_PRINTS;
                break;
            case R.id.rb_refused:
                if (checked)
                    reason = REFUSED;
                break;
            case R.id.rb_other:
                if (checked)
                    reason = OTHER;
                break;
        }
    }

}
