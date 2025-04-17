package si.uni_lj.fri.pbd.location_sensing

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import si.uni_lj.fri.pbd.location_sensing.databinding.ActivityMapsBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import android.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    companion object {
        private const val REQUEST_ID_LOCATION_PERMISSIONS = 101
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            showLastKnownLocation()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_ID_LOCATION_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showLastKnownLocation()
            }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    @SuppressLint("SetTextI18n")
    fun showLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale (this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(
                    binding.root,
                    R.string.permission_location_rationale,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.ok) {
                    ActivityCompat.requestPermissions(
                        this@MapsActivity, arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        REQUEST_ID_LOCATION_PERMISSIONS
                    )
                }.show()
            }
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude

                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(lat, lng))
                            .title(
                                LocalDateTime.now().format(
                                    DateTimeFormatter.ofLocalizedDateTime(
                                        FormatStyle.SHORT, FormatStyle.SHORT)))
                    )

                    val zoom_level = 15.0F
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoom_level))

                    binding.textView.text = "Latitude: $lat"
                    binding.textView2.text = "Longitude: $lng"
                }
            }
    }
}