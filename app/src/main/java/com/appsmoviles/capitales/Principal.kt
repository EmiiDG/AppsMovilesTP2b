package com.appsmoviles.capitales

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.SearchView
import android.widget.Toast
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
        val db = dbHelper.writableDatabase
        var pais = ""

        buscarCiudad.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                findViewById<LinearLayout>(R.id.ciudadLayout).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.paisLayout).visibility = View.GONE

                if (!query.isNullOrEmpty()) {
                    val ciudad = dbHelper.obtenerCiudadPorNombre(query)

                    if (ciudad != null){
                    findViewById<TextView>(R.id.ciudad).text = ciudad.nombre

                    findViewById<TextView>(R.id.ciudadPais).text = ciudad.pais

                    findViewById<EditText>(R.id.poblacion).setText(ciudad.poblacion.toString())
                }}
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
                pais = query.toString()

                if (!query.isNullOrEmpty()) {
                    Log.d("SearchView", "Texto ingresado: $query")
                    val ciudades = dbHelper.obtenerCiudadesPorPais(query)
                    val tabla = findViewById<TableLayout>(R.id.tablaPais)
                    tabla.removeViews(1, tabla.childCount - 1)
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

                        val botonEditar = Button(this@Principal)
                        botonEditar.text = "✏️"
                        botonEditar.setOnClickListener {
                            val input = EditText(this@Principal)
                            input.inputType = InputType.TYPE_CLASS_NUMBER
                            input.hint = "Nueva población"

                            AlertDialog.Builder(this@Principal)
                                .setTitle("Editar población de ${ciudad.nombre}")
                                .setView(input)
                                .setPositiveButton("Guardar") { _, _ ->
                                    val nuevaPoblacion = input.text.toString().toIntOrNull()
                                    if (nuevaPoblacion != null) {
                                        dbHelper.actualizarPoblacionCiudad(ciudad.nombre, nuevaPoblacion)
                                        poblacion.text = nuevaPoblacion.toString()
                                    }
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        }

                        val botonEliminar = Button(this@Principal)
                        botonEliminar.text = "🗑️"
                        botonEliminar.setOnClickListener {
                            AlertDialog.Builder(this@Principal)
                                .setTitle("Eliminar ciudad")
                                .setMessage("¿Deseás eliminar ${ciudad.nombre}?")
                                .setPositiveButton("Sí") { _, _ ->
                                    dbHelper.eliminarCiudad(ciudad.nombre)
                                    tabla.removeView(tableRow)
                                }
                                .setNegativeButton("No", null)
                                .show()
                        }
                        val contenedorBotones = LinearLayout(this@Principal)
                        contenedorBotones.orientation = LinearLayout.HORIZONTAL

                        botonEditar.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        botonEliminar.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                        contenedorBotones.addView(botonEditar)
                        contenedorBotones.addView(botonEliminar)

                        tableRow.addView(contenedorBotones)

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

        val eliminar = findViewById<Button>(R.id.eliminarCiudad)
        val modificar = findViewById<Button>(R.id.modificarCiudad)

        eliminar.setOnClickListener(){
            dbHelper.eliminarCiudadPorNombre(findViewById<TextView>(R.id.ciudad).text.toString())
        }

        val poblacion = findViewById<EditText>(R.id.poblacion)
        val cancelar = findViewById<Button>(R.id.dejarDeModificar)

        modificar.setOnClickListener(){
            poblacion.isEnabled = true
            poblacion.isFocusable = true
            poblacion.isFocusableInTouchMode = true
            poblacion.isCursorVisible = true
            poblacion.requestFocus()

            findViewById<LinearLayout>(R.id.botonesModificar).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.botonesVer).visibility = View.GONE
        }


        val aceptar = findViewById<Button>(R.id.aceptarModificacion)

        aceptar.setOnClickListener(){
            val nuevaPoblacion = poblacion.text.toString().toIntOrNull()
            if (nuevaPoblacion != null) {
                dbHelper.modificarPoblacion(findViewById<TextView>(R.id.ciudad).text.toString(), nuevaPoblacion)}
        }

        cancelar.setOnClickListener(){
            poblacion.isEnabled = false
            poblacion.isFocusable = false
            poblacion.isFocusableInTouchMode = false
            poblacion.isCursorVisible = false

            findViewById<LinearLayout>(R.id.botonesModificar).visibility = View.GONE
            findViewById<LinearLayout>(R.id.botonesVer).visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.agregarCiudad).setOnClickListener {
            val ciudad = findViewById<EditText>(R.id.editTextCiudad).text.toString()
            val poblacionStr = findViewById<EditText>(R.id.editTextPoblacion).text.toString()
            val poblacion = poblacionStr.toIntOrNull()

            if(pais.isNotBlank() && ciudad.isNotBlank() && poblacion != null) {
                val dbHelper = DBHelper(this)
                dbHelper.insertarCiudad(pais, ciudad, poblacion)
            }
        }

        findViewById<Button>(R.id.ePais).setOnClickListener{
            dbHelper.eliminarCiudadesPorPais(pais)
        }
    }

}