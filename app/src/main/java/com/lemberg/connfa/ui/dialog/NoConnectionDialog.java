package com.lemberg.connfa.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.lemberg.connfa.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NoConnectionDialog extends DialogFragment {

    public static final String TAG = "NoConnectionDialog";

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.Attention);
        alertDialog.setMessage(R.string.NoConnectionMessage);
        alertDialog.setPositiveButton(Objects.requireNonNull(getActivity()).getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        return alertDialog.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        finish();
    }

    private void finish() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }
}
