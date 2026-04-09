package com.example.appcomprayventa.Anuncios

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Adaptadores.AdaptadorImagenSlider
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelo.ModeloAnuncio
import com.example.appcomprayventa.Modelo.ModeloImagenSeleccionada
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

    // Dentro de DetalleAnuncio.kt
    private lateinit var imagenSliderArrayList: ArrayList<ModeloImagenSeleccionada>
    private lateinit var adaptadorImagenSlider: AdaptadorImagenSlider

    private fun cargarImagenesAnuncio() {
        imagenSliderArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    imagenSliderArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelo = ds.getValue(ModeloImagenSeleccionada::class.java)
                        if (modelo != null) {
                            imagenSliderArrayList.add(modelo)
                        }
                    }
                    adaptadorImagenSlider = AdaptadorImagenSlider(this@DetalleAnuncio, imagenSliderArrayList)
                    binding.viewPagerImagenes.adapter = adaptadorImagenSlider

                    // Actualizar contador
                    binding.tvContadorImagen.text = "1/${imagenSliderArrayList.size}"
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // Detectar cuando el usuario desliza la imagen para cambiar el contador
        binding.viewPagerImagenes.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tvContadorImagen.text = "${position + 1}/${imagenSliderArrayList.size}"
            }
        })
    }
}
