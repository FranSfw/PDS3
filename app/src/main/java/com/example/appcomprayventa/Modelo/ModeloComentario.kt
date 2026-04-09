package com.example.appcomprayventa.Modelo

class ModeloComentario {
    var id = ""
    var uid = ""
    var nombre = ""
    var comentario = ""
    var tiempo: Long = 0

    constructor()

    constructor(id: String, uid: String, nombre: String, comentario: String, tiempo: Long) {
        this.id = id
        this.uid = uid
        this.nombre = nombre
        this.comentario = comentario
        this.tiempo = tiempo
    }
}