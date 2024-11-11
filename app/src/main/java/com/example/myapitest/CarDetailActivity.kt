package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapitest.databinding.ActivityCarDetailBinding
import com.example.myapitest.model.Car
import com.example.myapitest.model.OneCarValues
import com.example.myapitest.model.Place
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.example.myapitest.ui.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarDetailBinding
    private lateinit var car: OneCarValues
    private lateinit var updateCar: Car


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        loadCar()
    }

    private fun setupView() {
       binding.topAppBar.setOnMenuItemClickListener() { menuItem ->
           when (menuItem.itemId) {
                R.id.editItem -> {
                   verifyIsNull()
                    true
                }
               R.id.deleteItem -> {
                   deleteCar()
                   true
               }

               else -> false
           }
       }
    }

    private fun deleteCar(){
        val carId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.deleleteCar(carId) }
            withContext(Dispatchers.Main) {
                when(result) {
                    is Result.Error -> Toast.makeText(this@CarDetailActivity,
                        "Erro ao deletar carro, tente mais tarde", Toast.LENGTH_LONG).show()
                    is Result.Success -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            "Carro ${car.value.name} foi removido com sucesso!!", Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun verifyIsNull() {
        if (binding.carName.text.toString().isEmpty()) {
            binding.carName.requestFocus()
            Toast.makeText(this@CarDetailActivity,
                "Campo do nome não pode estar vazio",
                Toast.LENGTH_LONG).show()
        } else if (binding.carYear.text.toString().isEmpty()) {
            binding.carYear.requestFocus()
            Toast.makeText(this@CarDetailActivity,
                "Campo do ano não pode estar vazio",
                Toast.LENGTH_LONG).show()
        } else if (binding.carLicense.text.toString().isEmpty()) {
            binding.carLicense.requestFocus()
            Toast.makeText(this@CarDetailActivity,
                "Campo de licensa não pode estar vazio",
                Toast.LENGTH_LONG).show()
        } else {
            editCar()
        }
    }

    private fun editCar() {
            val carId = intent.getStringExtra(ARG_ID) ?: ""

            CoroutineScope(Dispatchers.IO).launch {
                val result = safeApiCall { RetrofitClient.apiService.updateCar(
                    carId, car.value.copy(
                        name = binding.carName.text.toString(),
                        licence = binding.carLicense.text.toString(),
                        year = binding.carYear.text.toString(),
                    )
                ) }
                withContext(Dispatchers.Main) {
                    when(result) {
                        is Result.Error -> Toast.makeText(this@CarDetailActivity,
                            "Erro ao Atulizar o carro, favor tente novamente",
                            Toast.LENGTH_SHORT).show()
                        is Result.Success -> {
                            Toast.makeText(this@CarDetailActivity,
                                "Carro ${car.value.name} atulizado com sucesso!!",
                                Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }

    }

    private fun loadCar() {
        val carId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getCar(carId) }
            withContext(Dispatchers.Main) {
                when(result) {
                    is Result.Error -> {}
                    is Result.Success -> {
                        car = result.data
                        Log.d("CAR-RETURN", "Carro ->C $car")
                        returnSucess()
                    }
                }
            }
        }
    }

    private fun returnSucess() {
        binding.imgViewCar.loadUrl(car.value.imageUrl)
        binding.carName.setText(car.value.name)
        binding.carYear.setText(car.value.year)
        binding.carLicense.setText(car.value.licence)
        binding.topAppBar.title = car.value.name
    }

    private fun updateCarValues() {

    }









































    companion object {

        private const val ARG_ID = "ARG_ID"

        fun newIntent(context: Context, carId: String) =
            Intent(context, CarDetailActivity::class.java).apply {
                putExtra(ARG_ID, carId)
            }
        }
}