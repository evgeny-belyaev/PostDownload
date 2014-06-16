package com.example.postdownload.app.lib;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import java.util.ArrayList;

public class SubscriptionHelper
{
    private ArrayList<Subscription> mManagedSubscriptions = new ArrayList<Subscription>();
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public void manage(Subscription subscription)
    {
        mCompositeSubscription.add(subscription);
        //        mManagedSubscriptions.add(subscription);
    }

    public void unsubscribe()
    {
        if (!mCompositeSubscription.isUnsubscribed())
        {
            mCompositeSubscription.unsubscribe();
        }

        mCompositeSubscription.clear();

//        for (Subscription subscription : mManagedSubscriptions)
//        {
//            if (!subscription.isUnsubscribed())
//            {
//                subscription.unsubscribe();
//            }
//        }
//
//        mManagedSubscriptions.clear();
    }
}
