package com.ugene.postdownload.app2.ui.picker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.ugene.postdownload.app2.R;
import rx.Observable;
import rx.subjects.PublishSubject;

public class CreateFolderDialog extends DialogFragment
{
    private static final String BUNDLE_KEY_FOLDER_NAME = "folderName";
    private final PublishSubject<String> mSubject;

    public static CreateFolderDialog create(String folderName)
    {
        Bundle b = new Bundle();
        b.putString(BUNDLE_KEY_FOLDER_NAME, folderName);

        CreateFolderDialog f = new CreateFolderDialog();
        f.setArguments(b);

        return f;
    }

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

        if (savedInstanceState == null)
        {
            name.setText(getArguments().getString(BUNDLE_KEY_FOLDER_NAME));
        }

        name.setSelection(name.getText().length());

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
