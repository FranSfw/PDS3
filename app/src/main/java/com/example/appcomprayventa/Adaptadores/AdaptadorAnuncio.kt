package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Anuncios.DetalleAnuncio
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelo.ModeloAnuncio
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ItemAnuncioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdaptadorAnuncio(
    private val context: Context,
    private val anuncioArrayList: ArrayList<ModeloAnuncio>
) : RecyclerView.Adapter<AdaptadorAnuncio.HolderAnuncio>() {

    private lateinit var binding: ItemAnuncioBinding
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAnuncio {
        binding = ItemAnuncioBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAnuncio(binding.root)
    }

    override fun onBindViewHolder(holder: HolderAnuncio, position: Int) {
        val modelo = anuncioArrayList[position]

        val titulo = modelo.titulo
        val descripcion = modelo.descripcion
        val precio = modelo.precio
        val tiempo = modelo.tiempo
        val idAnuncio = modelo.id
        val uidAnuncio = modelo.uid

        val fecha = Constantes.ObtenerFecha(tiempo)

        holder.tituloTv.text = titulo
        holder.descripcionTv.text = descripcion
        holder.precioTv.text = "$ $precio"
        holder.fechaTv.text = fecha

        cargarPrimeraImagen(modelo, holder)

        // Ver detalles al hacer clic en el item
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetalleAnuncio::class.java)
            intent.putExtra("idAnuncio", idAnuncio)
            context.startActivity(intent)
        }

        // Mostrar opciones solo si el anuncio es del usuario actual
        if (uidAnuncio == firebaseAuth.uid) {
            holder.opcionesBtn.visibility = View.VISIBLE
        } else {
            holder.opcionesBtn.visibility = View.GONE
        }

        holder.opcionesBtn.setOnClickListener {
            opcionesAnuncio(modelo, holder)
        }
    }

    private fun opcionesAnuncio(modelo: ModeloAnuncio, holder: HolderAnuncio) {
        val popupMenu = PopupMenu(context, holder.opcionesBtn)
        popupMenu.menu.add(0, 0, 0, "Editar")
        popupMenu.menu.add(0, 1, 1, "Eliminar")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val idItem = item.itemId
            if (idItem == 0) {
                // Lógica editar (pendiente)
            } else if (idItem == 1) {
                dialogEliminarAnuncio(modelo)
            }
            true
        }
    }

    private fun dialogEliminarAnuncio(modelo: ModeloAnuncio) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Eliminar Anuncio")
            .setMessage("¿Estás seguro de que quieres eliminar este anuncio?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarAnuncio(modelo)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun eliminarAnuncio(modelo: ModeloAnuncio) {
        val idAnuncio = modelo.id
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Anuncio eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarPrimeraImagen(modelo: ModeloAnuncio, holder: HolderAnuncio) {
        val idAnuncio = modelo.id
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        try {
                            Glide.with(context)
                                .load(imagenUrl)
                                .placeholder(R.drawable.ic_imagen_perfil)
                                .into(holder.imagenIv)
                        } catch (e: Exception) {}
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun getItemCount(): Int {
        return anuncioArrayList.size
    }

    inner class HolderAnuncio(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenIv = binding.imagenAnuncio
        val tituloTv = binding.tituloAnuncio
        val descripcionTv = binding.descripcionAnuncio
        val precioTv = binding.precioAnuncio
        val fechaTv = binding.fechaAnuncio
        val opcionesBtn = binding.opcionesAnuncio
    }
}
