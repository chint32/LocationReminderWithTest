package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_select_location.*
import org.koin.android.ext.android.inject


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

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

        return binding.root
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        map = p0
        setMapStyle(map)

        // User should have granted permission in previous fragment.
        // Tried to ask for permission in this fragment but the user
        // would have to re-open the map for the permissions to take effect.
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        }

        map.setOnPoiClickListener { poi ->

            handlePoiClick(poi)
        }

        map.setOnMapLongClickListener { latLng ->

            handleLongClick(latLng)
        }
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


