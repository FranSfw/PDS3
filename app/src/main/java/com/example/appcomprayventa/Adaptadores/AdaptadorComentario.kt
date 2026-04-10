package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.appcomprayventa.Anuncios.DetalleAnuncio
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelo.ModeloComentario
import com.example.appcomprayventa.databinding.ItemComentarioBinding

class AdaptadorComentario(
    private val context: Context,
    private val comentarioArrayList: ArrayList<ModeloComentario>
) : RecyclerView.Adapter<AdaptadorComentario.HolderComentario>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComentario {
        val binding = ItemComentarioBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComentario(binding)
    }

    override fun onBindViewHolder(holder: HolderComentario, position: Int) {
        val modelo = comentarioArrayList[position]

        holder.binding.nombreTv.text = modelo.nombre
        holder.binding.comentarioTv.text = modelo.comentario
        holder.binding.fechaTv.text = Constantes.ObtenerFecha(modelo.tiempo)

        // Lógica visual para respuestas
        if (modelo.esRespuesta) {
            // Si es una respuesta, le damos un margen a la izquierda para el efecto de hilo
            val params = holder.binding.cardComentario.layoutParams as LinearLayout.LayoutParams
            params.setMargins(60, 2, 2, 2)
            holder.binding.cardComentario.layoutParams = params
            holder.binding.cardComentario.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
            holder.binding.btnResponder.visibility = View.GONE // No permitimos responder a una respuesta (opcional)
        } else {
            // Si es un comentario principal
            val params = holder.binding.cardComentario.layoutParams as LinearLayout.LayoutParams
            params.setMargins(2, 2, 2, 2)
            holder.binding.cardComentario.layoutParams = params
            holder.binding.cardComentario.setCardBackgroundColor(Color.WHITE)
            holder.binding.btnResponder.visibility = View.VISIBLE
        }

        holder.binding.btnResponder.setOnClickListener {
            if (context is DetalleAnuncio) {
                context.prepararRespuesta(modelo.id, modelo.nombre)
            }
        }
    }

    override fun getItemCount(): Int = comentarioArrayList.size

    inner class HolderComentario(val binding: ItemComentarioBinding) : RecyclerView.ViewHolder(binding.root)
}
