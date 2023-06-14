package com.example.retrofit

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.FillEventHistory
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BreedsAdapter
    private lateinit var spinner: Spinner

    private var imagesByBreedList = mutableListOf<String>()
    private var breedsList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler)
        spinner = findViewById(R.id.spinner)


        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BreedsAdapter(imagesByBreedList)
        recyclerView.adapter = adapter

        getListOfBreeds()
    }

    private fun getListOfBreeds() {
        CoroutineScope(Dispatchers.IO).launch {
            val call =
                getRetrofit().create(ApiService::class.java).getListOfBreed("breeds/list/all")
            val response: BreedResponse? = call.body()

            runOnUiThread {
                if (call.isSuccessful) {

                    val breedsMap = response?.breed
                    if (breedsMap != null) {
                        for (breed in breedsMap.keys)
                            breedsList.add(breed)
                        setSpinner()

                    }
                } else {
                    showError()
                }
            }
        }
    }

    private fun setSpinner() {
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, breedsList)

        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                getImageBy(breedsList[p2])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
        private fun getImageBy(breed: String?) {

            CoroutineScope(Dispatchers.IO).launch {
                val call = getRetrofit().create(ApiService::class.java)
                    .getImagesByBreeds("breed/$breed/images")
                val response = call.body()

                runOnUiThread {
                    if (call.isSuccessful) {
                        val images = response?.images ?: emptyList()
                        imagesByBreedList.clear()
                        imagesByBreedList.addAll(images)
                        adapter.notifyDataSetChanged()
                    } else {
                        showError()
                    }
                }
            }
        }

        private fun showError() {
            Toast.makeText(this, "Fallo en la llamada", Toast.LENGTH_SHORT).show()
        }

        private fun getRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(URL_DOGS)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }


        companion object {
            const val URL_DOGS = "https://dog.ceo/api/"
        }

    }
