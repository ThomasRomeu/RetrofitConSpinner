package com.example.retrofit

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.FillEventHistory
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BreedsAdapter
    private var imagesByBreedList = mutableListOf<String>()
    private lateinit var searchView: SearchView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler)
        searchView = findViewById(R.id.searchview)

        searchView.setOnQueryTextListener(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BreedsAdapter(imagesByBreedList)
        recyclerView.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getImageBy(breed: String?) {

        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(ApiService::class.java).getImagesByBreeds("breed/$breed/images")
            val response = call.body()

            runOnUiThread {
                if(call.isSuccessful){
                    val images = response?.images ?: emptyList()
                    imagesByBreedList.clear()
                    imagesByBreedList.addAll(images)
                    adapter.notifyDataSetChanged()
                }else{
                    showError()
                }
                hideKeyBoard()
            }
        }
    }

    private fun hideKeyBoard() {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        var view: View? = this.currentFocus
        if (view == null){
            view = View(this);
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0);
    }

    private fun showError(){
        Toast.makeText(this, "Fallo en la llamada", Toast.LENGTH_SHORT).show()
    }
    private fun getRetrofit(): Retrofit{
        return Retrofit.Builder()
            .baseUrl(URL_DOGS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    override fun onQueryTextSubmit(breed: String?): Boolean {
        if (!breed.isNullOrBlank()) {
            getImageBy(breed)
        }
        return true
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        return false
    }

    companion object {
        const val URL_DOGS = "https://dog.ceo/api/"
    }
}