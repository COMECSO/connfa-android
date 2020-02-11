package com.lemberg.connfa.app;

import android.content.Context;
import androidx.multidex.MultiDexApplication;

import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.AppDatabaseInfo;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.database.LAPIDBRegister;
import com.lemberg.drupal.http.base.BaseRequest;
import com.lemberg.drupal.util.image.DrupalImageView;

public class App extends MultiDexApplication {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();

        LAPIDBRegister.getInstance().register(mContext, new AppDatabaseInfo(mContext));
        Model.instance(mContext);
        DrupalImageView.setupSharedClient(new DrupalClient(null, Model.instance().createNewQueue(getApplicationContext()), BaseRequest.RequestFormat.JSON, null));
    }

    public static Context getContext() {
        return mContext;
    }
}
