package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private var locationEnabled = false

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private lateinit var map: GoogleMap

    private val GEOFENCE_RADIUS_METERS = 200f


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)

        mapFragment!!.getMapAsync(this)

        binding.saveLocationBtn.setOnClickListener(View.OnClickListener {

            onLocationSelected()
        })

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        return binding.root
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        map = p0
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            @SuppressLint("MissingPermission")
            override fun onLocationChanged(location: Location) {
                Log.i(this.javaClass.simpleName, "Location has changed:" + location.latitude + "," + location.longitude)
                map.setMyLocationEnabled(true)

                val userLocation = LatLng(location.latitude, location.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLng(userLocation))
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), 15f
                    )
                )
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // Nothing to do
            }

            override fun onProviderEnabled(provider: String?) {
                // Nothing to do
            }

            override fun onProviderDisabled(provider: String?) {
                // Nothing to do
            }


        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.setMyLocationEnabled(true)
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )

            lastKnownLocation =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                map.setMyLocationEnabled(true)
                val userLocation =
                    LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLng(userLocation))
            }

        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }


        setMapStyle(map)

        map.setOnPoiClickListener { poi ->

            handlePoiClick(poi)
        }

        map.setOnMapLongClickListener { latLng ->

            handleLongClick(latLng)
        }

        if (locationEnabled) {
            map.isMyLocationEnabled = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> {
                // act accordingly
                Log.i(this.javaClass.simpleName, "after requesting foregroung permissions")
                zoomOnLocation()
            }
            REQUEST_TURN_DEVICE_LOCATION_ON -> {
                zoomOnLocation()
            }
        }
    }

    // result from requesting permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                zoomOnLocation()
            }
        } else {
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.settings) {
                    startActivityForResult(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", requireContext().packageName, null)
                    }, REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE)
                }.show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun zoomOnLocation() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        this.javaClass.simpleName,
                        "Error getting location settings resolution: " + sendEx.message
                    )
                }
            } else {
                map.isMyLocationEnabled = true
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                if(isForegroundPermissionApproved()) {
                    map.isMyLocationEnabled = true
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0f,
                        locationListener
                    )

                    lastKnownLocation =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastKnownLocation != null) {
                        map.isMyLocationEnabled = true
                        val userLocation =
                            LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLng(userLocation))
                    }
                } else {
                    Toast.makeText(requireContext(), "For a better app experience, please enable location in settings", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "For a better app experience, please enable location in settings", Toast.LENGTH_SHORT).show()
            }

        }

        if(isForegroundPermissionApproved()){

            Log.i(TAG, "Permissions GRanted on Listener, setting location layer to true")
            map.setMyLocationEnabled(true)
            locationEnabled = true
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )
        }
    }

    private fun isForegroundPermissionApproved(): Boolean {

        return (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                )
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun handlePoiClick(poi: PointOfInterest) {
        map.clear()
        addMarkerPOI(poi)
        addCircle(poi.latLng, GEOFENCE_RADIUS_METERS)
        binding.viewModel!!.latitude.value = poi.latLng.latitude
        binding.viewModel!!.longitude.value = poi.latLng.longitude
        binding.viewModel!!.reminderSelectedLocationStr.value = poi.name
    }

    private fun handleLongClick(latLng: LatLng) {
        map.clear()
        addMarkerDroppedPin(latLng)
        addCircle(latLng, GEOFENCE_RADIUS_METERS)
        binding.viewModel!!.latitude.value = latLng.latitude
        binding.viewModel!!.longitude.value = latLng.longitude
        binding.viewModel!!.reminderSelectedLocationStr.value = "Dropped Pin"
    }

    private fun onLocationSelected() {
        requireActivity().onBackPressed()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }

        else -> super.onOptionsItemSelected(item)
    }


    private fun addMarkerPOI(poi: PointOfInterest) {
        val poiMarker = map.addMarker(
            MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
        )
        poiMarker.showInfoWindow()

    }

    private fun addMarkerDroppedPin(latLng: LatLng) {
        val poiMarker = map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Dropped Pin")
        )
        poiMarker.showInfoWindow()

    }

    private fun addCircle(latLng: LatLng, radius: Float) {
        val circleOptions = CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(radius.toDouble())
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
        circleOptions.fillColor(Color.argb(64, 255, 0, 0))
        circleOptions.strokeWidth(4f)
        map.addCircle(circleOptions)
    }

}

private const val TAG = "SelectLocationFragment"

private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

