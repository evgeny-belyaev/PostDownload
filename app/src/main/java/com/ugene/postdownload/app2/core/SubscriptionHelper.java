package com.ugene.postdownload.app2.core;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class SubscriptionHelper
{
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public void manage(Subscription subscription)
    {
        mCompositeSubscription.add(subscription);
    }

    public void unsubscribe()
    {
        mCompositeSubscription.unsubscribe();
        mCompositeSubscription = new CompositeSubscription();
    }
}
