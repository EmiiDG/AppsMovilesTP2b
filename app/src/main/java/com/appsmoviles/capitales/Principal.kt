package com.appsmoviles.capitales

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Principal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buscarCiudad = findViewById<SearchView>(R.id.buscarCiudad)
        val buscarPais = findViewById<SearchView>(R.id.buscarPais)
        val dbHelper = DBHelper(this)
        val db =dbHelper.writableDatabase

        buscarCiudad.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                findViewById<LinearLayout>(R.id.ciudadLayout).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.paisLayout).visibility = View.GONE

                // Acá podrías hacer una búsqueda en tu base de datos también
                // buscarCiudad(query)

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        buscarPais.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                findViewById<LinearLayout>(R.id.paisLayout).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.ciudadLayout).visibility = View.GONE

                if (!query.isNullOrEmpty()) {
                    Log.d("SearchView", "Texto ingresado: $query")
                    val ciudades = dbHelper.obtenerCiudadesPorPais(query)
                    val tabla = findViewById<TableLayout>(R.id.tablaPais)
                    for (ciudad in ciudades) {
                        val tableRow = TableRow(this@Principal)

                        val nombreCiudad = TextView(this@Principal).apply {
                            text = ciudad.nombre
                            layoutParams = TableRow.LayoutParams(0, WRAP_CONTENT, 4f)
                            setPadding(8, 8, 8, 8)
                        }

                        val poblacion = TextView(this@Principal).apply {
                            text = ciudad.poblacion.toString()
                            layoutParams = TableRow.LayoutParams(0, WRAP_CONTENT, 2f)
                            setPadding(8, 8, 8, 8)
                        }

                        val opciones = TextView(this@Principal).apply{
                            text = "Ejemplo"
                            layoutParams = TableRow.LayoutParams(0, WRAP_CONTENT, 2f)
                            setPadding(8, 8, 8, 8)
                        }

                        tableRow.addView(nombreCiudad)
                        tableRow.addView(poblacion)
                        tableRow.addView(opciones)

                        tabla.addView(tableRow)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        findViewById<ImageButton>(R.id.agregarPais).setOnClickListener(){
            val nombrePais = findViewById<EditText>(R.id.editTextPais).text.toString()
            val valoresPais = ContentValues().apply {
                put("nombre", nombrePais)
            }
            db.insert("paises", null, valoresPais)
        }
    }
}