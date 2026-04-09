package com.example.appcomprayventa.Anuncios

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelo.ModeloAnuncio
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityDetalleAnuncioBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetalleAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleAnuncioBinding
    private var idAnuncio = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idAnuncio = intent.getStringExtra("idAnuncio") ?: ""

        cargarDetallesAnuncio()
        cargarImagenesAnuncio()
    }

    private fun cargarDetallesAnuncio() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val modeloAnuncio = snapshot.getValue(ModeloAnuncio::class.java)
                    if (modeloAnuncio != null) {
                        val titulo = modeloAnuncio.titulo
                        val descripcion = modeloAnuncio.descripcion
                        val precio = modeloAnuncio.precio
                        val marca = modeloAnuncio.marca
                        val categoria = modeloAnuncio.categoria
                        val condicion = modeloAnuncio.condicion

                        binding.tituloDetalle.text = titulo
                        binding.descripcionDetalle.text = descripcion
                        binding.precioDetalle.text = "$ $precio"
                        binding.marcaDetalle.text = "Marca: $marca"
                        binding.categoriaDetalle.text = "Categoría: $categoria"
                        binding.condicionDetalle.text = "Condición: $condicion"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun cargarImagenesAnuncio() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        try {
                            Glide.with(this@DetalleAnuncio)
                                .load(imagenUrl)
                                .placeholder(R.drawable.ic_imagen_perfil)
                                .into(binding.imagenDetalle)
                        } catch (e: Exception) {}
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
