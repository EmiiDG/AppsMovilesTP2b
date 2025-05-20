package com.appsmoviles.capitales

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "Paises.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val crearTablaPais = """
            CREATE TABLE paises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE
            )
        """.trimIndent()

        val crearTablaCiudad = """
            CREATE TABLE ciudades (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                poblacion INTEGER,
                id_pais INTEGER,
                FOREIGN KEY (id_pais) REFERENCES paises(id)
            )
        """.trimIndent()

        db.execSQL(crearTablaPais)
        db.execSQL(crearTablaCiudad)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ciudades")
        db.execSQL("DROP TABLE IF EXISTS paises")
        onCreate(db)
    }

    data class Ciudad(val nombre: String, val poblacion: Int)

    fun obtenerCiudadesPorPais(nombrePais: String): List<Ciudad> {
        val ciudades = mutableListOf<Ciudad>()
        val db = this.readableDatabase

        val query = """
        SELECT ciudades.nombre, ciudades.poblacion
        FROM ciudades
        JOIN paises ON ciudades.id_pais = paises.id
        WHERE paises.nombre = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(nombrePais))

        if (cursor.moveToFirst()) {
            do {
                val nombreCiudad = cursor.getString(0)
                val poblacion = cursor.getInt(1)
                ciudades.add(Ciudad(nombreCiudad, poblacion))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return ciudades
    }

}
