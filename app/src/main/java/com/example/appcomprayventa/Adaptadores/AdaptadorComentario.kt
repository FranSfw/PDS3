package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelo.ModeloComentario
import com.example.appcomprayventa.databinding.ItemComentarioBinding

class AdaptadorComentario(
    private val context: Context,
    private val comentarioArrayList: ArrayList<ModeloComentario>
) : RecyclerView.Adapter<AdaptadorComentario.HolderComentario>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComentario {
        val binding = ItemComentarioBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComentario(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComentario, position: Int) {
        val modelo = comentarioArrayList[position]
        val nombre = modelo.nombre
        val comentario = modelo.comentario
        val tiempo = modelo.tiempo

        val fecha = Constantes.ObtenerFecha(tiempo)

        holder.nombreTv.text = nombre
        holder.comentarioTv.text = comentario
        holder.fechaTv.text = fecha
    }

    override fun getItemCount(): Int = comentarioArrayList.size

    inner class HolderComentario(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemComentarioBinding.bind(itemView)
        val nombreTv = binding.nombreTv
        val fechaTv = binding.fechaTv
        val comentarioTv = binding.comentarioTv
    }
}
