package com.mihai.traxxor.data;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

public class EventHandler implements Parcelable {

    private static final long IFTTT_REQUEST_DELAY_MS = 1000 * 60 * 5; // 5 minutes
    private long timeToLeaveMs;
    private long timeToGetUpMs;

    private boolean timeToLeaveEventFired = false;
    private boolean timeToGetUpEventFired = false;
    @NonNull
    private final List<Long> pastGetUpEvents = new ArrayList<>();

    @NonNull
    private RequestQueue volleyQueue;

    @Nullable
    private Listener listener;

    @NonNull
    private final Handler delayedTextMessageHandler = new Handler();

    public interface Listener {
        void onTimeToLeaveEvent();
        void onTimeToGetUpEvent();
    }

    /**
     * For recreating the {@link EventHandler} via a Parcel
     */
    private EventHandler(
            long timeToLeaveMs,
            long timeToGetUpMs,
            boolean timeToLeaveEventFired,
            boolean timeToGetUpEventFired,
            @NonNull List<Long> pastGetUpEvents) {
        this.timeToLeaveMs = timeToLeaveMs;
        this.timeToGetUpMs = timeToGetUpMs;
        this.timeToLeaveEventFired = timeToLeaveEventFired;
        this.timeToGetUpEventFired = timeToGetUpEventFired;
        this.pastGetUpEvents.addAll(pastGetUpEvents);
    }

    public EventHandler(@NonNull Context context, @Nullable Listener listener,
                        float timeToLeaveHours, int timeToGetUpMinutes) {
        volleyQueue = Volley.newRequestQueue(context);
        this.listener = listener;
        setTimeToLeave(timeToLeaveHours);
        setTimeToGetUp(timeToGetUpMinutes);
    }

    void initOnParcelRestore(@NonNull Context context, @Nullable Listener listener) {
        volleyQueue = Volley.newRequestQueue(context);
        this.listener = listener;
    }

    public void setTimeToLeave(float timeToLeaveHours) {
        timeToLeaveMs = (long) (timeToLeaveHours * 60 * 60 * 1000);
    }

    public void setTimeToGetUp(int getUpFrequencyMinutes) {
        timeToGetUpMs = getUpFrequencyMinutes * 60 * 1000;
    }

    public void reset() {
        timeToLeaveEventFired = false;
        pastGetUpEvents.clear();
    }

    public long getTimeToLeaveRemainingMs(long totalDurationMs) {
        return timeToLeaveMs - totalDurationMs;
    }

    public long getTimeToGetUpRemainingMs(long totalDurationMs) {
        long lastGetUpEventMs = pastGetUpEvents.isEmpty()
                                ? 0
                                : pastGetUpEvents.get(pastGetUpEvents.size() - 1);

        return timeToGetUpMs - (totalDurationMs - lastGetUpEventMs);
    }

    /** Call when user is likely leaving for the day. */
    public void onActionRequiredAcknowledged() {
        // User has acknowledged they need to act, so there is no need to send a text messages.
        delayedTextMessageHandler.removeCallbacksAndMessages(null);
    }

    public void onGetUpEvent(long totalDurationMs) {
        pastGetUpEvents.add(totalDurationMs);
        timeToGetUpEventFired = false;
    }

    public void handleEventsIfNeeded(long totalDurationMs) {
        handleTimeToLeaveEventIfNeeded(totalDurationMs);
        handleTimeToGetUpEventIfNeeded(totalDurationMs);
    }

    private void handleTimeToLeaveEventIfNeeded(long totalDurationMs) {
        if (getTimeToLeaveRemainingMs(totalDurationMs) > 0 || timeToLeaveEventFired) {
            return;
        }

        timeToLeaveEventFired = true;
        if (listener != null) {
            listener.onTimeToLeaveEvent();
        }

        initDelayedTextMessageRequest("traxxor_leaveWork");
    }

    private void handleTimeToGetUpEventIfNeeded(long totalDurationMs) {
        // TODO(P3) can add: getTimeToLeaveRemainingMs(totalDurationMs) < TimeUnit.MINUTES.toMillis(30)
        // to not have to get up within last 30 min?  Only add this if I'm good about leaving on time.
        if (getTimeToGetUpRemainingMs(totalDurationMs) > 0 || timeToGetUpEventFired) {
            return;
        }

        timeToGetUpEventFired = true;
        if (listener != null) {
            listener.onTimeToGetUpEvent();
        }

        initDelayedTextMessageRequest("traxxor_getUp");

    }

    /** Make a delayed request to IFTTT to send a text message.  This is delayed to give user a
     * chance to take care of the event before sending a text (limited to only 100/month). */
    private void initDelayedTextMessageRequest(final String eventName) {
        delayedTextMessageHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String url = String.format(
                        "https://maker.ifttt.com/trigger/%s/with/key/dOevSBBSwD8hjSRLsHDd2e",
                        eventName);

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
        }, IFTTT_REQUEST_DELAY_MS);
    }


    // ***************************************************************************
    // Parcelable related code only below this.
    // ***************************************************************************

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(timeToLeaveMs);
        out.writeLong(timeToGetUpMs);
        out.writeInt(timeToLeaveEventFired ? 1 : 0);
        out.writeInt(timeToGetUpEventFired ? 1 : 0);
        out.writeList(pastGetUpEvents);
    }

    public static final Parcelable.Creator<EventHandler> CREATOR =
            new Parcelable.Creator<EventHandler>() {
                public EventHandler createFromParcel(Parcel in) {
                    long timeToLeaveMs = in.readLong();
                    long timeToGetUpMs = in.readLong();
                    boolean timeToLeaveEventFired = in.readInt() == 1;
                    boolean timeToGetUpEventFired = in.readInt() == 1;
                    ArrayList<Long> pastGetUpEvents = new ArrayList<>();
                    in.readList(pastGetUpEvents, Long.class.getClassLoader());

                    return new EventHandler(
                            timeToLeaveMs,
                            timeToGetUpMs,
                            timeToLeaveEventFired,
                            timeToGetUpEventFired,
                            pastGetUpEvents
                    );
                }

                public EventHandler[] newArray(int size) {
                    return new EventHandler[size];
                }
            };
}
