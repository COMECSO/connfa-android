package com.lemberg.connfa.model;

import android.os.AsyncTask;
import android.text.TextUtils;
import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.R;
import com.lemberg.connfa.app.App;
import com.lemberg.connfa.model.data.UpdateDate;
import com.lemberg.connfa.model.database.ILAPIDBFacade;
import com.lemberg.connfa.model.managers.SynchronousItemManager;
import com.lemberg.drupal.http.base.BaseRequest;
import com.lemberg.drupal.http.base.RequestConfig;
import com.lemberg.drupal.http.base.ResponseData;
import com.lemberg.drupal.util.ObserverHolder;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class UpdatesManager {

    private DrupalClient mClient;
    private ObserverHolder<DataUpdatedListener> mUpdateListeners;

    public static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";
    private static final String LAST_MODIFIED_HEADER = "Last-Modified";

    public UpdatesManager(@NotNull DrupalClient client) {
        mUpdateListeners = new ObserverHolder<>();
        mClient = client;
    }

    public static void startLoading(final UpdateCallback callback, final UpdatesManager manager) {
        new AsyncTask<Void, Void, List<UpdateRequest>>() {
            @Override
            protected List<UpdateRequest> doInBackground(Void... params) {
                return manager.doPerformLoading();
            }

            @Override
            protected void onPostExecute(final List<UpdateRequest> result) {
                if (result != null) {
                    manager.mUpdateListeners.notifyAllObservers(new ObserverHolder.ObserverNotifier<DataUpdatedListener>() {
                        @Override
                        public void onNotify(DataUpdatedListener observer) {
                            observer.onDataUpdated(result);
                        }
                    });
                }

                if (result != null) {
                    if (callback != null) {
                        callback.onDownloadSuccess();
                    }
                    manager.mUpdateListeners.notifyAllObservers(new ObserverHolder.ObserverNotifier<DataUpdatedListener>() {
                        @Override
                        public void onNotify(DataUpdatedListener observer) {
                            observer.onDataUpdated(result);
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.onDownloadError();
                    }
                }
            }
        }.execute();
    }

    public void registerUpdateListener(DataUpdatedListener listener) {
        this.mUpdateListeners.registerObserver(listener);
    }

    public void unregisterUpdateListener(DataUpdatedListener listener) {
        this.mUpdateListeners.unregisterObserver(listener);
    }

    /**
     * @return return updated request id's list in case of success or null in case of failure
     */

    private List<UpdateRequest> doPerformLoading() {
        RequestConfig config = new RequestConfig();
        config.setResponseFormat(BaseRequest.ResponseFormat.JSON);
        config.setRequestFormat(BaseRequest.RequestFormat.JSON);
        config.setResponseClassSpecifier(UpdateDate.class);

        String baseURL = App.getContext().getString(R.string.api_value_base_url);
        BaseRequest checkForUpdatesRequest = new BaseRequest(BaseRequest.RequestMethod.GET, baseURL + "checkUpdates", config);

        String lastDate = PreferencesManager.getInstance().getLastUpdateDate();
        checkForUpdatesRequest.addRequestHeader(IF_MODIFIED_SINCE_HEADER, lastDate);
        ResponseData updatesData = mClient.performRequest(checkForUpdatesRequest, true);

        int statusCode = updatesData.getStatusCode();
        if (statusCode > 0 && statusCode < 400) {
            UpdateDate updateDate = (UpdateDate) updatesData.getData();
            if (updateDate == null) {
                return new LinkedList<>();
            }
            updateDate.setTime(updatesData.getHeaders().get(LAST_MODIFIED_HEADER));
            return loadData(updateDate);

        } else {
            return null;
        }
    }

    private List<UpdateRequest> loadData(UpdateDate updateDate) {

        ILAPIDBFacade facade = Model.instance().getFacade();
        try {
            facade.open();
            facade.beginTransactions();
            boolean success = true;
            List<UpdateRequest> updateList = updateDate.getIdsForUpdate();
            for (UpdateRequest update : updateList) {
                success = sendRequestById(update);
                if (!success) {
                    break;
                }
            }
            if (success) {
                facade.setTransactionSuccesfull();
                if (!TextUtils.isEmpty(updateDate.getTime())) {
                    PreferencesManager.getInstance().saveLastUpdateDate(updateDate.getTime());
                }
            }
            return success ? updateList : null;
        } finally {
            facade.endTransactions();
            facade.close();
        }


    }

    private boolean sendRequestById(UpdateRequest update) {

        SynchronousItemManager manager;
        switch (update) {
            case SETTINGS:
                manager = Model.instance().getSettingsManager();
                break;

            case TYPES:
                manager = Model.instance().getTypesManager();
                break;

            case LEVELS:
                manager = Model.instance().getLevelsManager();
                break;

            case TRACKS:
                manager = Model.instance().getTracksManager();
                break;

            case SPEAKERS:
                manager = Model.instance().getSpeakerManager();
                break;

            case LOCATIONS:
                manager = Model.instance().getLocationManager();
                break;

            case PROGRAMS:
                manager = Model.instance().getProgramManager();
                break;

            case BOFS:
                manager = Model.instance().getBofsManager();
                break;

            case SOCIALS:
                manager = Model.instance().getSocialManager();
                break;

            case POIS:
                manager = Model.instance().getPoisManager();
                break;

            case INFO:
                manager = Model.instance().getInfoManager();
                break;

            case FLOOR_PLANS:
                manager = Model.instance().getFloorPlansManager();
                break;

            case SCHEDULES:
                manager = Model.instance().getScheduleManager();
                break;

            default:
                return true;
        }

        if (manager != null) {
            return manager.fetchData();
        }

        return false;
    }

    public interface DataUpdatedListener {

        void onDataUpdated(List<UpdateRequest> requests);
    }

    public void checkForDatabaseUpdate() {
        ILAPIDBFacade facade = Model.instance().getFacade();
        facade.open();
        facade.close();
    }
}
