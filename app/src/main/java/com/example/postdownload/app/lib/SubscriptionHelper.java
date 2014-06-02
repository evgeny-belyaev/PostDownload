package com.example.postdownload.app.lib;

import rx.Subscription;

import java.util.ArrayList;

public class SubscriptionHelper
{
    private ArrayList<Subscription> mManagedSubscriptions = new ArrayList<Subscription>();

    public void manage(Subscription subscription)
    {
        mManagedSubscriptions.add(subscription);
    }

    public void unsubscribe()
    {
        for (Subscription subscription : mManagedSubscriptions)
        {
            if (!subscription.isUnsubscribed())
            {
                subscription.unsubscribe();
            }
        }

        mManagedSubscriptions.clear();
    }
}
