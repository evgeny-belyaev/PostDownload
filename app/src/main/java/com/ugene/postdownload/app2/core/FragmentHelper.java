package com.ugene.postdownload.app2.core;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import rx.functions.Func0;

@SuppressWarnings("UnusedDeclaration")
public class FragmentHelper
{

    public static <T extends Fragment> T replaceOrRestore(FragmentManager fragmentManager, int containerViewId, String tag, Func0<T> fragmentFactory)
    {
        T fragment = (T)fragmentManager.findFragmentByTag(tag);

        if (fragment == null)
        {
            fragment = fragmentFactory.call();

            fragmentManager.beginTransaction().replace(containerViewId, fragment, tag).commit();
        }

        return fragment;
    }

    public static <T extends Fragment> T createOrRestore(FragmentManager fragmentManager, int containerViewId, String tag, Func0<T> fragmentFactory)
    {
        T fragment = (T)fragmentManager.findFragmentByTag(tag);

        if (fragment == null)
        {
            fragment = fragmentFactory.call();

            fragmentManager.beginTransaction().add(containerViewId, fragment, tag).commit();
        }

        return fragment;
    }

    public static <T extends Fragment> T createOrRestore(FragmentManager fragmentManager, String tag, Func0<T> fragmentFactory)
    {
        T fragment = (T)fragmentManager.findFragmentByTag(tag);

        if (fragment == null)
        {
            fragment = fragmentFactory.call();

            fragmentManager.beginTransaction().add(fragment, tag).commit();
        }

        return fragment;
    }

    public static <T extends DialogFragment> T createOrRestoreDialog(FragmentManager fragmentManager, String tag, Func0<T> fragmentFactory)
    {
        T fragment = (T)fragmentManager.findFragmentByTag(tag);

        if (fragment == null)
        {
            fragment = fragmentFactory.call();
        }

        return fragment;
    }
}
