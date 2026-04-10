package com.example.appcomprayventa.Modelo

class ModeloComentario {
    var id = ""
    var uid = ""
    var nombre = ""
    var comentario = ""
    var tiempo: Long = 0
    var esRespuesta = false
    var idPadre = ""

    constructor()

    constructor(id: String, uid: String, nombre: String, comentario: String, tiempo: Long, esRespuesta: Boolean = false, idPadre: String = "") {
        this.id = id
        this.uid = uid
        this.nombre = nombre
        this.comentario = comentario
        this.tiempo = tiempo
        this.esRespuesta = esRespuesta
        this.idPadre = idPadre
    }
}