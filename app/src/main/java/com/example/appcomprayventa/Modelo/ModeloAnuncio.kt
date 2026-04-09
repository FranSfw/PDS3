package com.example.appcomprayventa.Modelo

class ModeloAnuncio {
    var id: String = ""
    var uid: String = ""
    var marca: String = ""
    var categoria: String = ""
    var condicion: String = ""
    var precio: String = ""
    var titulo: String = ""
    var descripcion: String = ""
    var estado: String = ""
    var tiempo: Long = 0L
    var latitud: Double = 0.0
    var longitud: Double = 0.0

    constructor()

    constructor(
        id: String,
        uid: String,
        marca: String,
        categoria: String,
        condicion: String,
        precio: String,
        titulo: String,
        descripcion: String,
        estado: String,
        tiempo: Long,
        latitud: Double,
        longitud: Double
    ) {
        this.id = id
        this.uid = uid
        this.marca = marca
        this.categoria = categoria
        this.condicion = condicion
        this.precio = precio
        this.titulo = titulo
        this.descripcion = descripcion
        this.estado = estado
        this.tiempo = tiempo
        this.latitud = latitud
        this.longitud = longitud
    }
}
