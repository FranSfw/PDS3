package com.example.appcomprayventa.Anuncios

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.appcomprayventa.Adaptadores.AdaptadorComentario
import com.example.appcomprayventa.Adaptadores.AdaptadorImagenSlider
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelo.ModeloAnuncio
import com.example.appcomprayventa.Modelo.ModeloComentario
import com.example.appcomprayventa.Modelo.ModeloImagenSeleccionada
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityDetalleAnuncioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetalleAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleAnuncioBinding
    private var idAnuncio = ""
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var imagenSliderArrayList: ArrayList<ModeloImagenSeleccionada>
    private lateinit var adaptadorImagenSlider: AdaptadorImagenSlider

    private lateinit var comentarioArrayList: ArrayList<ModeloComentario>
    private lateinit var adaptadorComentario: AdaptadorComentario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idAnuncio = intent.getStringExtra("idAnuncio") ?: ""
        firebaseAuth = FirebaseAuth.getInstance()

        cargarDetallesAnuncio()
        cargarImagenesAnuncio()
        cargarComentarios()

        verificarLikes()
        verificarDislikes()

        binding.btnLike.setOnClickListener {
            darLike()
        }

        binding.btnDislike.setOnClickListener {
            darDislike()
        }

        binding.btnEnviarComentario.setOnClickListener {
            validarComentario()
        }
    }

    private fun cargarDetallesAnuncio() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val modeloAnuncio = snapshot.getValue(ModeloAnuncio::class.java)
                    if (modeloAnuncio != null) {
                        binding.tituloDetalle.text = modeloAnuncio.titulo
                        binding.descripcionDetalle.text = modeloAnuncio.descripcion
                        binding.precioDetalle.text = "$ ${modeloAnuncio.precio}"
                        binding.marcaDetalle.text = "Marca: ${modeloAnuncio.marca}"
                        binding.categoriaDetalle.text = "Categoría: ${modeloAnuncio.categoria}"
                        binding.condicionDetalle.text = "Condición: ${modeloAnuncio.condicion}"
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun cargarImagenesAnuncio() {
        imagenSliderArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    imagenSliderArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelo = ds.getValue(ModeloImagenSeleccionada::class.java)
                        if (modelo != null) imagenSliderArrayList.add(modelo)
                    }
                    adaptadorImagenSlider = AdaptadorImagenSlider(this@DetalleAnuncio, imagenSliderArrayList)
                    binding.viewPagerImagenes.adapter = adaptadorImagenSlider
                    binding.tvContadorImagen.text = "1/${imagenSliderArrayList.size}"
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        binding.viewPagerImagenes.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tvContadorImagen.text = "${position + 1}/${imagenSliderArrayList.size}"
            }
        })
    }

    // --- LÓGICA DE LIKES ---

    private fun verificarLikes() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Likes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val numLikes = snapshot.childrenCount
                    binding.tvLikes.text = "$numLikes"

                    if (snapshot.hasChild(firebaseAuth.uid!!)) {
                        binding.btnLike.setImageResource(R.drawable.ic_like_active)
                    } else {
                        binding.btnLike.setImageResource(R.drawable.ic_like)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun darLike() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(idAnuncio)
        ref.child("Likes").child(firebaseAuth.uid!!).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Si ya existe, lo quitamos
                ref.child("Likes").child(firebaseAuth.uid!!).removeValue()
            } else {
                // Si no existe, lo agregamos y quitamos el dislike por si acaso
                ref.child("Likes").child(firebaseAuth.uid!!).setValue(true)
                ref.child("Dislikes").child(firebaseAuth.uid!!).removeValue()
            }
        }
    }

    // --- LÓGICA DE DISLIKES ---

    private fun verificarDislikes() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Dislikes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val numDislikes = snapshot.childrenCount
                    binding.tvDislikes.text = "$numDislikes"

                    if (snapshot.hasChild(firebaseAuth.uid!!)) {
                        binding.btnDislike.setImageResource(R.drawable.ic_dislike_active)
                    } else {
                        binding.btnDislike.setImageResource(R.drawable.ic_dislike)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun darDislike() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(idAnuncio)
        ref.child("Dislikes").child(firebaseAuth.uid!!).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                ref.child("Dislikes").child(firebaseAuth.uid!!).removeValue()
            } else {
                ref.child("Dislikes").child(firebaseAuth.uid!!).setValue(true)
                ref.child("Likes").child(firebaseAuth.uid!!).removeValue()
            }
        }
    }

    // --- LÓGICA DE COMENTARIOS ---

    private fun cargarComentarios() {
        comentarioArrayList = ArrayList()
        // Creamos el adaptador una sola vez
        adaptadorComentario = AdaptadorComentario(this, comentarioArrayList)
        binding.rvComentarios.adapter = adaptadorComentario

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Comentarios")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                comentarioArrayList.clear() // Limpiamos para no duplicar
                for (ds in snapshot.children) {
                    val modelo = ds.getValue(ModeloComentario::class.java)
                    if (modelo != null) {
                        comentarioArrayList.add(modelo)
                    }
                }
                // ESTA LÍNEA es la que avisa al RecyclerView que ya tiene los 3 comentarios
                adaptadorComentario.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun validarComentario() {
        val comentario = binding.etComentario.text.toString().trim()
        if (comentario.isEmpty()) {
            Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show()
        } else {
            agregarComentario(comentario)
        }
    }

    private fun agregarComentario(comentario: String) {
        val refUser = FirebaseDatabase.getInstance().getReference("Usuarios").child(firebaseAuth.uid!!)
        refUser.get().addOnSuccessListener { snapshot ->
            val nombre = "${snapshot.child("nombres").value}"
            val tiempo = Constantes.ObtenerTiempoDis()
            val idComentario = "${System.currentTimeMillis()}"

            val modeloComentario = ModeloComentario(idComentario, firebaseAuth.uid!!, nombre, comentario, tiempo)

            val refAnuncio = FirebaseDatabase.getInstance().getReference("Anuncios")
            refAnuncio.child(idAnuncio).child("Comentarios").child(idComentario).setValue(modeloComentario)
                .addOnSuccessListener {
                    binding.etComentario.setText("")
                    Toast.makeText(this, "Comentario enviado", Toast.LENGTH_SHORT).show()
                }
        }
    }
}