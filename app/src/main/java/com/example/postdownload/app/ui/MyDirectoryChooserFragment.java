package com.example.postdownload.app.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class MyDirectoryChooserFragment extends DirectoryChooserFragment
{
    private final PublishSubject<String> mDirectorySelectedSubject;
    private CreateFolderDialog mCreateFolderDialog;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public MyDirectoryChooserFragment()
    {
        mDirectorySelectedSubject = PublishSubject.create();
    }

    public static MyDirectoryChooserFragment newInstance(String initialDirectory)
    {
        MyDirectoryChooserFragment fragment = new MyDirectoryChooserFragment();
        Bundle args = new Bundle();
        args.putString(DirectoryChooserFragment.ARG_NEW_DIRECTORY_NAME, "");
        args.putString(DirectoryChooserFragment.ARG_INITIAL_DIRECTORY, initialDirectory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        mCreateFolderDialog = new CreateFolderDialog();
        mCompositeSubscription.add(
            mCreateFolderDialog
                .observe()
                .subscribe(new Action1<String>()
                {
                    @Override
                    public void call(String name)
                    {
                        mNewDirectoryName = name;
                        int msg = createFolder();
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    }
                })
        );

        mListener = new OnFragmentInteractionListener()
        {
            @Override
            public void onSelectDirectory(@NonNull String path)
            {
                mDirectorySelectedSubject.onNext(path);
            }

            @Override
            public void onCancelChooser()
            {

            }
        };
    }

    @Override
    public void onPause()
    {
        super.onPause();

        mCompositeSubscription.unsubscribe();
    }

    @Override
    protected void openNewFolderDialog()
    {
        mCreateFolderDialog
            .show(getFragmentManager(), "CreateFolderDialog");
    }

    public Observable<String> observeDirectorySelected()
    {
        return mDirectorySelectedSubject.asObservable();
    }
}
