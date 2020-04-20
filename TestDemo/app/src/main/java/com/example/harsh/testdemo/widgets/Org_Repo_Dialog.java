package com.example.harsh.testdemo.widgets;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.harsh.testdemo.R;

/**
 * Created by harsh on 1/21/2017.
 */

public class Org_Repo_Dialog extends DialogFragment  {

    Button save, cancel;
    EditText input_org, input_repo;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.view_org_repo_dialog);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        save = (Button) dialog.findViewById(R.id.button_save);
        cancel = (Button) dialog.findViewById(R.id.button_cancel);
        input_org= (EditText)dialog.findViewById(R.id.input_org);
        input_repo=(EditText)dialog.findViewById(R.id.input_repo);

        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return dialog;
    }
}