package com.example.appcomprayventa

import android.text.format.DateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Locale


object Constantes {

    const val MENSAJE_TIPO_TEXTO = "TEXTO"
    const val MENSAJE_TIPO_IMAGEN = "IMAGEN"

    const val anuncio_disponible = "Disponible"
    const val anuncio_vendido = "Vendido"

    val categorias = arrayOf(
        "Celulares",
        "PCs/Laptops",
        "Electrónica y electrodomésticos",
        "Automóviles",
        "Consolas y videojuegos",
        "Hogar y muebles",
        "Belleza y cuidado personal",
        "Libros",
        "Deportes"
    )

    val condiciones = arrayOf(
        "Nuevo",
        "Usado",
        "Renovado"
    )


    fun ObtenerTiempoDis() : Long{
        return System.currentTimeMillis()
    }

    fun ObtenerFecha(tiempo: Long) : String{
        val calendario = Calendar.getInstance(Locale.ENGLISH)
        calendario.timeInMillis = tiempo

        return DateFormat.format("dd/MM/yyyy", calendario).toString()
    }

    fun obtenerFechaHora(tiempo: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = tiempo
        return DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString()

    }

//    fun rutaChat(receptorUid: String, emisorUid: String): String {
//        val arrayUid = arrayOf(receptorUid, emisorUid)
//        Arrays.sort(arrayUid)
//        return "${arrayUid[0]}_${arrayUid[1]}"
//    }
    fun rutaChat(receptorUid: String, emisorUid: String): String {
        val arrayUid = arrayOf(receptorUid, emisorUid)
        arrayUid.sort() // Función de extensión de Kotlin, más limpia
        return "${arrayUid[0]}_${arrayUid[1]}"
    }
}

