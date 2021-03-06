package com.ugene.postdownload.app2.ui.picker;

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

    public static MyDirectoryChooserFragment newInstance(String initialDirectory, String newDirectoryName)
    {
        MyDirectoryChooserFragment fragment = new MyDirectoryChooserFragment();
        Bundle args = new Bundle();
        args.putString(DirectoryChooserFragment.ARG_NEW_DIRECTORY_NAME, newDirectoryName);
        args.putString(DirectoryChooserFragment.ARG_INITIAL_DIRECTORY, initialDirectory);

        fragment.setArguments(args);
        return fragment;
    }

    public void updateInitialDirectory(String initialDirectory, String newDirectoryName)
    {
        mInitialDirectory = initialDirectory;
        mNewDirectoryName = newDirectoryName;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        mCreateFolderDialog = CreateFolderDialog.create(mNewDirectoryName);

        mCompositeSubscription.add(
            mCreateFolderDialog
                .observe()
                .subscribe(
                    new Action1<String>()
                    {
                        @Override
                        public void call(String name)
                        {
                            mNewDirectoryName = name;
                            int msg = createFolder();
                            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Action1<Throwable>()
                    {
                        @Override
                        public void call(Throwable throwable)
                        {
                            int i = 45;
                        }
                    }
                )
        );

        mListener = new OnFragmentInteractionListener()
        {
            @Override
            public void onSelectDirectory(@NonNull String path)
            {
                mDirectorySelectedSubject.onNext(path);
                dismiss();
            }

            @Override
            public void onCancelChooser()
            {
                dismiss();
            }
        };
    }

    @Override
    public void onPause()
    {
        super.onPause();

        mCreateFolderDialog = null;
        mCompositeSubscription.unsubscribe();
        mCompositeSubscription = new CompositeSubscription();
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
