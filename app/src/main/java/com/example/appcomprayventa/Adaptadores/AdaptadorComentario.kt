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
        // CORRECCIÓN: Usar LayoutInflater directamente sin guardar el binding en la clase
        val binding = ItemComentarioBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComentario(binding)
    }

    override fun onBindViewHolder(holder: HolderComentario, position: Int) {
        val modelo = comentarioArrayList[position]
        
        holder.binding.nombreTv.text = modelo.nombre
        holder.binding.comentarioTv.text = modelo.comentario
        holder.binding.fechaTv.text = Constantes.ObtenerFecha(modelo.tiempo)
    }

    override fun getItemCount(): Int = comentarioArrayList.size

    inner class HolderComentario(val binding: ItemComentarioBinding) : RecyclerView.ViewHolder(binding.root)
}
