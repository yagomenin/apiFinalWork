package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapitest.databinding.ActivityAddCarBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class AddCarActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAddCarBinding

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var imageUri: Uri
    private var imageFile: File? = null


    private var photoUrl: String = "https://localstorage.com"
    private var lat: Float = 0.0F
    private var long: Float = 0.0F
    private val CAMERA_REQUEST_CODE = 1

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: File? = imageFile
            displayImage(data)
            Toast.makeText(this@AddCarActivity, "Imagem Obtida", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getCurrentLocation()
        mMap.setOnMapClickListener { latLng ->
            addMarker(latLng)
            saveLocation(latLng)
        }
    }

    private fun displayImage(data: File?) {
        data?.let {
            val bitmap = BitmapFactory.decodeFile(it.absolutePath)
            binding.imgViewCar.setImageBitmap(bitmap)
        }
    }


    private fun uploadImageToFirebase() {
        val storageRef = FirebaseStorage.getInstance().reference

        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        val imageBitmap = BitmapFactory.decodeFile(imageFile!!.path)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imagesRef.putBytes(data)
            .addOnFailureListener {
                Toast.makeText(this, "Falha ao realizar o upload", Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    photoUrl = uri.toString()
                    Toast.makeText(this, "Upload feito com sucesso", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveLocation(location: LatLng) {
        lat = location.latitude.toFloat()
        long  = location.longitude.toFloat()
    }


    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    addMarker(currentLatLng)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }
    }

    private fun addMarker(location: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(location).title("Sua localização"))

    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        return FileProvider.getUriForFile(
            this,
            "com.example.minhaprimeiraapi.fileprovider",
            imageFile!!
        )
    }

    private fun requestCameraPermission() {
        requestPermissions(
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    private fun takePicture() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun setupView() {

        binding.btTakePhoto.setOnClickListener {
            takePicture()
        }



        // TopAbbBar
        binding.topAppBar.setOnMenuItemClickListener() { menuItem ->
            when (menuItem.itemId) {
                R.id.saveItem -> {
                    save()
                    true
                }

                else -> false
            }
        }
        // Maps
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun save() {
        uploadImageToFirebase()
        if(verifyIsNull()) {
            CoroutineScope(Dispatchers.IO).launch {
                val place = Place(
                    lat = lat,
                    long = long
                )
                val id = SecureRandom().nextInt().toString()
                val car = Car(
                    id,
                    photoUrl,
                    binding.carYear.text.toString(),
                    binding.carName.text.toString(),
                    binding.carLicense.text.toString(),
                    place
                )
                val result = safeApiCall {
                    RetrofitClient.apiService.addCar(car)
                }
                withContext(Dispatchers.Main) {
                    when(result) {
                        is Result.Error -> Toast.makeText(
                            this@AddCarActivity,
                            "Erro ao criar novo Carro",
                            Toast.LENGTH_SHORT
                        ).show()

                        is Result.Success -> { Toast.makeText(
                            this@AddCarActivity,
                            "Carro ${binding.carName.text.toString()} criado com sucesso!!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                        }
                    }
                }
            }
        }
    }

    private fun verifyIsNull(): Boolean {
        if (binding.carName.text.toString().isEmpty()) {
            binding.carName.requestFocus()
            Toast.makeText(this@AddCarActivity,
                "Campo do nome não pode estar vazio",
                Toast.LENGTH_LONG).show()
            return false
        } else if (binding.carYear.text.toString().isEmpty()) {
            binding.carYear.requestFocus()
            Toast.makeText(this@AddCarActivity,
                "Campo do ano não pode estar vazio",
                Toast.LENGTH_LONG).show()
            return false
        } else if (binding.carLicense.text.toString().isEmpty()) {
            binding.carLicense.requestFocus()
            Toast.makeText(this@AddCarActivity,
                "Campo de licensa não pode estar vazio",
                Toast.LENGTH_LONG).show()
            return false
        } else
            return true
    }



























    companion object {
        fun newIntent(context: Context) = Intent(context, AddCarActivity::class.java)
    }
}