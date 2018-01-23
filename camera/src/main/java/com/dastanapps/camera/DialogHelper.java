package com.dastanapps.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by dastaniqbal on 17/01/2018.
 * dastanIqbal@marvelmedia.com
 * 17/01/2018 10:16
 */

public class DialogHelper {
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    public static class FeatureListDialog extends DialogFragment {

        interface ISelectItem {
            void onSelectItem(int which);
        }

        private ArrayAdapter<String> arrayAdapter;
        private List<String> features;
        private ISelectItem listener;

        public static FeatureListDialog newInstance() {
            FeatureListDialog dialog = new FeatureListDialog();
            Bundle args = new Bundle();
            return dialog;
        }

        public FeatureListDialog setFeatures(List<String> features, ISelectItem iSelectItem) {
            this.features = features;
            this.listener = iSelectItem;
            return this;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, features);
            return new AlertDialog.Builder(getActivity())
                    .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            listener.onSelectItem(which);
                        }
                    })
                    .create();
        }

    }
}
