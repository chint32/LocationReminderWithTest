package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_SHORT).show();

        GeofenceTransitionsJobIntentService.enqueueWork(context, intent);
    }

}

