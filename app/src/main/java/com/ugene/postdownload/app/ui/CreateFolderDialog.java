package com.ugene.postdownload.app.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.ugene.postdownload.app.R;
import rx.Observable;
import rx.subjects.PublishSubject;

public class CreateFolderDialog extends DialogFragment
{
    private final PublishSubject<String> mSubject;

    public CreateFolderDialog()
    {
        mSubject = PublishSubject.create();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.create_dir_dialog, null);
        final EditText name = (EditText)view.findViewById(R.id.create_dir_dialog_name);

        builder
            .setView(view)
            .setTitle(R.string.create_dir_title)
            .setPositiveButton(R.string.create_dir_confirm, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    mSubject.onNext(name.getText().toString());
                    dismiss();
                }
            })
            .setNegativeButton(R.string.create_dir_cancel, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    // User cancelled the dialog
                    dismiss();
                }
            });

        return builder.create();
    }

    public Observable<String> observe()
    {
        return mSubject.asObservable();
    }
}
