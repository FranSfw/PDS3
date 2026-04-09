package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Modelo.ModeloImagenSeleccionada
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ItemImagenSliderBinding

class AdaptadorImagenSlider(
    private val context: Context,
    private val imagenArrayList: ArrayList<ModeloImagenSeleccionada>
) : RecyclerView.Adapter<AdaptadorImagenSlider.HolderImagenSlider>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSlider {
        val binding = ItemImagenSliderBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagenSlider(binding.root)
    }

    override fun onBindViewHolder(holder: HolderImagenSlider, position: Int) {
        val modelo = imagenArrayList[position]
        val imagenUrl = modelo.imagenUrl

        Glide.with(context)
            .load(imagenUrl)
            .placeholder(R.drawable.ic_imagen_perfil)
            .into(holder.imagenIv)
    }

    override fun getItemCount(): Int = imagenArrayList.size

    inner class HolderImagenSlider(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenIv = ItemImagenSliderBinding.bind(itemView).imagenSlider
    }
}