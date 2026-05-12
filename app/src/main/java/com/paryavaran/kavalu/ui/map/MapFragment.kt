package com.paryavaran.kavalu.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.paryavaran.kavalu.MainActivity
import com.paryavaran.kavalu.R
import com.paryavaran.kavalu.data.AppViewModel
import com.paryavaran.kavalu.data.WasteReport
import com.paryavaran.kavalu.databinding.FragmentMapBinding
import com.paryavaran.kavalu.ui.report.ReportDetailBottomSheet

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppViewModel by activityViewModels()
    private var googleMap: GoogleMap? = null
    private val markerMap = mutableMapOf<Marker, String>() // marker → report id
    private lateinit var mapReportAdapter: MapReportListAdapter

    private val locationPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            enableMyLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.google_map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapReportAdapter = MapReportListAdapter(
            onItemClick = { report ->
                ReportDetailBottomSheet.newInstance(report.id)
                    .show(parentFragmentManager, "detail")
                // Center map on this report
                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(report.lat, report.lng), 16f)
                )
            },
            onMarkCleaned = { report ->
                viewModel.markCleaned(report.id)
                (activity as? MainActivity)?.showToast("🟢 Marked as Cleaned! +5 Eco-Karma")
            }
        )
        binding.rvMapReports.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mapReportAdapter
        }

        binding.btnMyLocation.setOnClickListener { centerOnMyLocation() }

        viewModel.reports.observe(viewLifecycleOwner) { reports ->
            updateStats(reports)
            updateMapMarkers(reports)
            mapReportAdapter.submitList(reports.reversed())
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = false
        }

        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Style the map with a natural/environmental look
        try {
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_green)
            )
        } catch (e: Exception) {
            // Use default style if custom style fails
        }

        // Default camera: Davangere, Karnataka
        val defaultLocation = LatLng(14.4644, 75.9218)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 13f))

        // Enable My Location if permission granted
        val hasFine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            enableMyLocation()
        } else {
            locationPermLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        map.setOnMarkerClickListener { marker ->
            val reportId = markerMap[marker]
            if (reportId != null) {
                ReportDetailBottomSheet.newInstance(reportId)
                    .show(parentFragmentManager, "detail")
                true
            } else false
        }

        // Render existing reports on map
        viewModel.reports.value?.let { updateMapMarkers(it) }
    }

    private fun enableMyLocation() {
        try {
            googleMap?.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    private fun centerOnMyLocation() {
        val hasFine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasFine) return

        try {
            com.google.android.gms.location.LocationServices
                .getFusedLocationProviderClient(requireActivity())
                .lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        googleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                        )
                    }
                }
        } catch (e: SecurityException) { /* ignore */ }
    }

    private fun updateStats(reports: List<WasteReport>) {
        val pending = reports.count { it.status == "pending" }
        val cleaned = reports.count { it.status == "cleaned" }
        binding.tvMapPending.text = pending.toString()
        binding.tvMapCleaned.text = cleaned.toString()
        binding.tvMapTotal.text = reports.size.toString()
    }

    private fun updateMapMarkers(reports: List<WasteReport>) {
        val map = googleMap ?: return
        map.clear()
        markerMap.clear()

        reports.forEach { report ->
            val position = LatLng(report.lat, report.lng)
            val isPending = report.status == "pending"

            val markerColor = if (isPending)
                BitmapDescriptorFactory.HUE_RED
            else
                BitmapDescriptorFactory.HUE_GREEN

            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(report.wasteType)
                    .snippet(if (isPending) "🔴 Pending Cleanup" else "🟢 Cleaned")
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
            )
            marker?.let { markerMap[it] = report.id }
        }

        // Auto-zoom to fit all markers if there are reports
        if (reports.isNotEmpty()) {
            val builder = LatLngBounds.builder()
            reports.forEach { builder.include(LatLng(it.lat, it.lng)) }
            try {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(builder.build(), 120)
                )
            } catch (e: Exception) { /* ignore */ }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
