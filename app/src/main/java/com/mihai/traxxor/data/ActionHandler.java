package com.mihai.traxxor.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ActionHandler {

    private long timeAtWorkMs;
    private long getUpFrequencyMs;

    private boolean timeAtWorkCallbackFired = false;
    @NonNull private final List<Long> pastGetUpEvents = new ArrayList<>();

    @NonNull private final RequestQueue volleyQueue;
    @Nullable private final Callback callback;

    interface Callback {
        void onTimeAtWorkCompleted();

        void onGetUpEventRequired();
    }

    public ActionHandler(@NonNull Context context, @Nullable Callback callback,
                         float timeAtWorkHours, int getUpFrequencyMinutes) {
        volleyQueue = Volley.newRequestQueue(context);
        this.callback = callback;
        setTimeAtWork(timeAtWorkHours);
        setGetUpFrequency(getUpFrequencyMinutes);
    }

    public void setTimeAtWork(float timeAtWorkHours) {
        timeAtWorkMs = (long) (timeAtWorkHours * 60 * 60 * 1000);
    }

    public void setGetUpFrequency(int getUpFrequencyMinutes) {
        getUpFrequencyMs = getUpFrequencyMinutes * 60 * 1000;
    }

    public void reset() {
        timeAtWorkCallbackFired = false;
        pastGetUpEvents.clear();
    }

    public long getTimeAtWorkRemainingMs(long totalDurationMs) {
        return timeAtWorkMs - totalDurationMs;
    }

    public long getNextGetUpEventTimeMs() {
        long lastGetUpEventMs = pastGetUpEvents.isEmpty()
                ? 0
                : pastGetUpEvents.get(pastGetUpEvents.size() - 1);

        return lastGetUpEventMs + getUpFrequencyMs;
    }

    // TODO add button for manually triggering this
    public void recordGetUpEvent(long totalDurationMs) {
        pastGetUpEvents.add(totalDurationMs);
    }

    public void handleActionsIfNeeded(long totalDurationMs) {
        handleTimeAtWorkCompletedIfNeeded(totalDurationMs);

        handleGetUpEventRequiredIfNeeded(totalDurationMs);
    }

    public void handleTimeAtWorkCompletedIfNeeded(long totalDurationMs) {
        if (getTimeAtWorkRemainingMs(totalDurationMs) > 0 || timeAtWorkCallbackFired) {
            return;
        }

        timeAtWorkCallbackFired = true;
        if (callback != null) {
            callback.onTimeAtWorkCompleted();
        }

        Log.d("bulic", "LEAVE!");
        makeIftttMakerRequest("traxxor_leaveWork");
    }

    public void handleGetUpEventRequiredIfNeeded(long totalDurationMs) {
        // TODO can add: getTimeAtWorkRemainingMs(totalDurationMs) < TimeUnit.MINUTES.toMillis(30)
        // to not have to get up within last 30 min?  Only add this if I'm good about leaving on time.
        if (getNextGetUpEventTimeMs() > totalDurationMs) {
            return;
        }

        recordGetUpEvent(totalDurationMs);
        if (callback != null) {
            callback.onGetUpEventRequired();
        }

        Log.d("bulic", "GET UP!");
//        makeIftttMakerRequest("traxxor_getUp");
    }

    private void makeIftttMakerRequest(String eventName) {
        String url = String.format("https://maker.ifttt.com/trigger/%s/with/key/dOevSBBSwD8hjSRLsHDd2e", eventName);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // no-op on success
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Traxxor", "Error making ifttt request", error);
                    }
                }
        );

        // Add the request to the RequestQueue.
        volleyQueue.add(stringRequest);
    }
}
