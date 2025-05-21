package com.appsmoviles.capitales
import android.content.ContentValues
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

    data class Ciudad(val nombre: String, val pais: String, val poblacion: Int)

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
                ciudades.add(Ciudad(nombreCiudad, nombrePais, poblacion))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return ciudades
    }

    fun obtenerCiudadPorNombre(nombre: String): Ciudad? {
        val db = readableDatabase
        val query = """
        SELECT ciudades.nombre, ciudades.poblacion, paises.nombre
        FROM ciudades
        JOIN paises ON ciudades.id_pais = paises.id
        WHERE ciudades.nombre = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(nombre))
        var ciudad: Ciudad? = null

        if (cursor.moveToFirst()) {
            val nombreCiudad = cursor.getString(0)
            val poblacion = cursor.getInt(1)
            val nombrePais = cursor.getString(2)
            ciudad = Ciudad(nombreCiudad, nombrePais, poblacion)
        }

        cursor.close()
        db.close()
        return ciudad
    }


    fun eliminarCiudadPorNombre(nombre: String): Boolean {
        val db = this.writableDatabase
        val resultado = db.delete("ciudades", "nombre = ?", arrayOf(nombre))
        db.close()
        return resultado > 0
    }

    fun modificarPoblacion(nombreCiudad: String, nuevaPoblacion: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("poblacion", nuevaPoblacion)
        }

        val filasAfectadas = db.update(
            "ciudades",
            values,
            "nombre = ?",
            arrayOf(nombreCiudad)
        )

        db.close()
        return filasAfectadas > 0
    }

    fun insertarCiudad(pais: String, ciudad: String, poblacion: Int) {
        val db = writableDatabase

        val cursor = db.rawQuery("SELECT id FROM paises WHERE nombre = ?", arrayOf(pais))
        if (cursor.moveToFirst()) {
            val idPais = cursor.getInt(0)
            val valores = ContentValues().apply {
                put("nombre", ciudad)
                put("poblacion", poblacion)
                put("id_pais", idPais)
            }
            db.insert("ciudades", null, valores)
        }
        cursor.close()
    }

    fun eliminarCiudadesPorPais(pais: String) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT id FROM paises WHERE nombre = ?", arrayOf(pais))
        if (cursor.moveToFirst()) {
            val idPais = cursor.getInt(0)
            db.delete("ciudades", "id_pais = ?", arrayOf(idPais.toString()))
        }
        cursor.close()
    }

    fun actualizarPoblacionCiudad(nombreCiudad: String, nuevaPoblacion: Int) {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("poblacion", nuevaPoblacion)
        }
        db.update("ciudades", valores, "nombre = ?", arrayOf(nombreCiudad))
    }

    fun eliminarCiudad(nombreCiudad: String) {
        val db = writableDatabase
        db.delete("ciudades", "nombre = ?", arrayOf(nombreCiudad))
    }

}
