package com.paryavaran.kavalu.ui.report

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.paryavaran.kavalu.MainActivity
import com.paryavaran.kavalu.R
import com.paryavaran.kavalu.data.AppViewModel
import com.paryavaran.kavalu.data.WasteReport
import com.paryavaran.kavalu.databinding.FragmentReportBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppViewModel by activityViewModels()

    private var selectedWasteType: String? = null
    private var selectedSeverity: String = "Medium"
    private var capturedLat: Double? = null
    private var capturedLng: Double? = null
    private var photoUri: Uri? = null
    private var photoPath: String? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                binding.ivPhotoPreview.setImageURI(uri)
                binding.ivPhotoPreview.visibility = View.VISIBLE
                binding.layoutPhotoDrop.setBackgroundResource(R.drawable.bg_photo_drop_filled)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUri = it
            photoPath = it.toString()
            binding.ivPhotoPreview.setImageURI(it)
            binding.ivPhotoPreview.visibility = View.VISIBLE
            binding.layoutPhotoDrop.setBackgroundResource(R.drawable.bg_photo_drop_filled)
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            captureLocation()
        } else {
            useMockLocation()
        }
    }

    private val cameraPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera() else galleryLauncher.launch("image/*")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupPhotoUpload()
        setupWasteChips()
        setupSeverityButtons()
        setupGpsButton()
        setupSubmitButton()
    }

    private fun setupPhotoUpload() {
        binding.layoutPhotoDrop.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                showPhotoOptions()
            } else {
                cameraPermLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("📷 Take Photo", "🖼️ Choose from Gallery")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Photo Evidence")
            .setItems(options) { _, which ->
                if (which == 0) launchCamera() else galleryLauncher.launch("image/*")
            }.show()
    }

    private fun launchCamera() {
        val photoFile = createImageFile()
        photoPath = photoFile.absolutePath
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("WASTE_${timeStamp}_", ".jpg", storageDir)
    }

    private fun setupWasteChips() {
        val chips = listOf(
            binding.chipHousehold to "Household Waste",
            binding.chipPlastic to "Plastic Waste",
            binding.chipDebris to "Construction Debris",
            binding.chipBiomedical to "Bio-Medical Waste",
            binding.chipEwaste to "Electronic Waste",
            binding.chipMixed to "Mixed Waste"
        )
        chips.forEach { (chip, type) ->
            chip.setOnClickListener {
                selectedWasteType = type
                chips.forEach { (c, _) -> c.isSelected = false }
                chip.isSelected = true
            }
        }
    }

    private fun setupSeverityButtons() {
        val sevButtons = listOf(
            binding.btnSevLow to "Low",
            binding.btnSevMedium to "Medium",
            binding.btnSevHigh to "High"
        )
        // Default medium selected
        binding.btnSevMedium.isSelected = true

        sevButtons.forEach { (btn, sev) ->
            btn.setOnClickListener {
                selectedSeverity = sev
                sevButtons.forEach { (b, _) -> b.isSelected = false }
                btn.isSelected = true
            }
        }
    }

    private fun setupGpsButton() {
        binding.btnGetGps.setOnClickListener {
            val hasFineLocation = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFineLocation || hasCoarseLocation) {
                captureLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun captureLocation() {
        binding.btnGetGps.text = "Getting..."
        binding.btnGetGps.isEnabled = false

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        capturedLat = location.latitude
                        capturedLng = location.longitude
                        binding.tvLocCoords.text =
                            "${String.format("%.5f", location.latitude)}°N, ${String.format("%.5f", location.longitude)}°E"
                        binding.tvLocLabel.text = "Accuracy: ±${location.accuracy.toInt()}m"
                        binding.btnGetGps.text = "✅ Got"
                        binding.btnGetGps.isEnabled = false
                        (activity as? MainActivity)?.showToast("📍 Location captured!")
                    } else {
                        useMockLocation()
                    }
                }
                .addOnFailureListener { useMockLocation() }
        } catch (e: SecurityException) {
            useMockLocation()
        }
    }

    private fun useMockLocation() {
        // Davangere area fallback
        capturedLat = 14.4644 + (Math.random() - 0.5) * 0.01
        capturedLng = 75.9218 + (Math.random() - 0.5) * 0.01
        binding.tvLocCoords.text =
            "${String.format("%.5f", capturedLat!!)}°N, ${String.format("%.5f", capturedLng!!)}°E"
        binding.tvLocLabel.text = "Simulated GPS location"
        binding.btnGetGps.text = "✅ Got"
        binding.btnGetGps.isEnabled = false
        (activity as? MainActivity)?.showToast("📍 Location simulated (GPS unavailable)")
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            val activity = activity as? MainActivity ?: return@setOnClickListener

            if (photoUri == null) {
                activity.showToast("❌ Please add a photo first", true); return@setOnClickListener
            }
            if (selectedWasteType == null) {
                activity.showToast("❌ Please select waste type", true); return@setOnClickListener
            }
            if (capturedLat == null) {
                activity.showToast("❌ Please capture GPS location", true); return@setOnClickListener
            }

            binding.btnSubmit.isEnabled = false
            binding.tvSubmitText.text = "Submitting..."
            binding.progressSubmit.visibility = View.VISIBLE

            binding.root.postDelayed({
                val report = WasteReport(
                    id = "RPT_${System.currentTimeMillis()}",
                    wasteType = selectedWasteType!!,
                    severity = selectedSeverity,
                    lat = capturedLat!!,
                    lng = capturedLng!!,
                    photoPath = photoPath,
                    description = binding.etDescription.text.toString()
                )
                viewModel.addReport(report)

                val karmaEarned = when (selectedSeverity) {
                    "High" -> 20; "Medium" -> 15; else -> 10
                }

                // Show success overlay
                showSuccessOverlay(karmaEarned)
                resetForm()
            }, 1500)
        }
    }

    private fun showSuccessOverlay(karmaEarned: Int) {
        SuccessOverlayDialog.newInstance(karmaEarned) {
            (activity as? MainActivity)?.showTab("map")
        }.show(parentFragmentManager, "success")
    }

    private fun resetForm() {
        photoUri = null
        photoPath = null
        capturedLat = null
        capturedLng = null
        selectedWasteType = null
        selectedSeverity = "Medium"

        binding.ivPhotoPreview.visibility = View.GONE
        binding.layoutPhotoDrop.setBackgroundResource(R.drawable.bg_photo_drop)
        binding.tvLocCoords.text = "Not captured yet"
        binding.tvLocLabel.text = ""
        binding.btnGetGps.text = "Get GPS"
        binding.btnGetGps.isEnabled = true
        binding.etDescription.text?.clear()
        binding.progressSubmit.visibility = View.GONE
        binding.tvSubmitText.text = "📤 Submit Report"
        binding.btnSubmit.isEnabled = true

        listOf(
            binding.chipHousehold, binding.chipPlastic, binding.chipDebris,
            binding.chipBiomedical, binding.chipEwaste, binding.chipMixed
        ).forEach { it.isSelected = false }

        listOf(binding.btnSevLow, binding.btnSevMedium, binding.btnSevHigh).forEach {
            it.isSelected = false
        }
        binding.btnSevMedium.isSelected = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
