package com.example.appcomprayventa.Adaptadores

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelos.Chat
import com.example.appcomprayventa.R
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdaptadorChat : RecyclerView.Adapter<AdaptadorChat.HolderChat> {

    private val context : Context
    private val chatArray : ArrayList<Chat>
    private val firebaseAuth : FirebaseAuth
    private var chatRuta = ""
    private var mediaPlayer: MediaPlayer? = null // Para gestionar la reproducción

    companion object {
        private const val MENSAJE_IZQUIERDO = 0
        private const val MENSAJE_DERECHO = 1
    }

    constructor(context: Context, chatArray: ArrayList<Chat>) {
        this.context = context
        this.chatArray = chatArray
        firebaseAuth = FirebaseAuth.getInstance()
    }

    inner class HolderChat(itemView : View) : RecyclerView.ViewHolder(itemView) {
        var Tv_mensaje : TextView = itemView.findViewById(R.id.Tv_mensaje)
        var Iv_mensaje : ShapeableImageView = itemView.findViewById(R.id.Iv_mensaje)
        var Tv_tiempo_mensaje : TextView = itemView.findViewById(R.id.Tv_tiempo_mensaje)
        // Nuevas vistas para el audio
        var Layout_audio : View = itemView.findViewById(R.id.Layout_audio)
        var Btn_play_audio : MaterialButton = itemView.findViewById(R.id.Btn_play_audio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChat {
        val layout = if (viewType == MENSAJE_DERECHO) R.layout.item_chat_derecho else R.layout.item_chat_izquierdo
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return HolderChat(view)
    }

    override fun getItemCount(): Int = chatArray.size

    override fun onBindViewHolder(holder: HolderChat, position: Int) {
        val modeloChat = chatArray[position]
        val mensaje = modeloChat.mensaje
        val tipoMensaje = modeloChat.tipoMensaje
        val tiempo = modeloChat.tiempo

        holder.Tv_tiempo_mensaje.text = Constantes.obtenerFechaHora(tiempo)

        // Reseteamos visibilidades para evitar errores al reciclar vistas
        holder.Tv_mensaje.visibility = View.GONE
        holder.Iv_mensaje.visibility = View.GONE
        holder.Layout_audio.visibility = View.GONE

        // Lógica según tipo de mensaje
        when (tipoMensaje) {
            Constantes.MENSAJE_TIPO_TEXTO -> {
                holder.Tv_mensaje.visibility = View.VISIBLE
                holder.Tv_mensaje.text = mensaje
                configurarClickEliminar(holder, position, modeloChat, "mensaje")
            }
            Constantes.MENSAJE_TIPO_IMAGEN -> {
                holder.Iv_mensaje.visibility = View.VISIBLE
                try {
                    Glide.with(context).load(mensaje).placeholder(R.drawable.img_enviada).into(holder.Iv_mensaje)
                } catch (e: Exception) { }
                configurarClickImagen(holder, position, modeloChat)
            }
            Constantes.MENSAJE_TIPO_AUDIO -> {
                holder.Layout_audio.visibility = View.VISIBLE
                holder.Btn_play_audio.setOnClickListener {
                    reproducirAudio(mensaje)
                }
                configurarClickEliminar(holder, position, modeloChat, "audio")
            }
        }
    }

    private fun reproducirAudio(audioUrl: String) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnCompletionListener { release() }
            }
        } catch (e: Exception) {
            Log.e("AudioError", "Error al reproducir: ${e.message}")
        }
    }

    private fun configurarClickEliminar(holder: HolderChat, position: Int, modeloChat: Chat, nombreTipo: String) {
        if (modeloChat.emisorUid == firebaseAuth.uid) {
            holder.itemView.setOnClickListener {
                val opciones = arrayOf<CharSequence>("Eliminar $nombreTipo", "Cancelar")
                AlertDialog.Builder(context)
                    .setTitle("¿Qué deseas realizar?")
                    .setItems(opciones) { _, which ->
                        if (which == 0) eliminarMensaje(position, holder, modeloChat)
                    }.show()
            }
        }
    }

    private fun configurarClickImagen(holder: HolderChat, position: Int, modeloChat: Chat) {
        holder.itemView.setOnClickListener {
            val esMio = modeloChat.emisorUid == firebaseAuth.uid
            val opciones = if (esMio) arrayOf("Eliminar imagen", "Ver imagen", "Cancelar") else arrayOf("Ver imagen", "Cancelar")

            AlertDialog.Builder(context)
                .setTitle("¿Qué desea realizar?")
                .setItems(opciones) { _, which ->
                    when (opciones[which]) {
                        "Eliminar imagen" -> eliminarMensaje(position, holder, modeloChat)
                        "Ver imagen" -> visualizadorImagen(modeloChat.mensaje)
                    }
                }.show()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatArray[position].emisorUid == firebaseAuth.uid) MENSAJE_DERECHO else MENSAJE_IZQUIERDO
    }

    private fun eliminarMensaje(position: Int, holder : HolderChat, modeloChat : Chat) {
        chatRuta = Constantes.rutaChat(modeloChat.receptorUid, modeloChat.emisorUid)
        FirebaseDatabase.getInstance().reference.child("Chats").child(chatRuta)
            .child(modeloChat.idMensaje).removeValue()
            .addOnSuccessListener { Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show() }
    }

    private fun visualizadorImagen(imagen : String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.visualizador_img)
        val Pv : PhotoView = dialog.findViewById(R.id.PV_img)
        val btnCerrar : MaterialButton = dialog.findViewById(R.id.BtnCerrarVisualizador)
        Glide.with(context).load(imagen).placeholder(R.drawable.img_enviada).into(Pv)
        btnCerrar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}