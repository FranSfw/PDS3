package com.example.appcomprayventa.Chat

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.appcomprayventa.R

import com.example.appcomprayventa.Adaptadores.AdaptadorChat
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelos.Chat
import com.example.appcomprayventa.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private var uid = "" // ID del receptor

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var miUid = "" // ID del emisor (yo)

    private var chatRuta = ""
    private var imagenUri: Uri? = null

    // Variables para audio
    private var mediaRecorder: MediaRecorder? = null
    private var audioPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        miUid = firebaseAuth.uid!!
        uid = intent.getStringExtra("uid")!!
        chatRuta = Constantes.rutaChat(uid, miUid)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        // Botón Adjuntar Imagen
        binding.adjuntarFAB.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                imagenGaleria()
            } else {
                solicitarPermisoAlmacenamiento.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        // Botón Enviar Texto
        binding.enviarFAB.setOnClickListener {
            validarMensaje()
        }

        // --- LÓGICA DE AUDIO (BOTÓN MICRÓFONO) ---
        // Asumiendo que tienes un botón llamado micFAB en tu XML
        binding.micFAB.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (comprobarPermisoAudio()) {
                        iniciarGrabacion()
                    } else {
                        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    detenerGrabacion()
                    true
                }
                else -> false
            }
        }

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        cargarInfo()
        cargarMensajes()
    }

    private fun comprobarPermisoAudio(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun cargarMensajes() {
        val mensajesArrayList = ArrayList<Chat>()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatRuta).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mensajesArrayList.clear()
                for (ds: DataSnapshot in snapshot.children) {
                    try {
                        val chat = ds.getValue(Chat::class.java)
                        mensajesArrayList.add(chat!!)
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "Error al cargar: ${e.message}")
                    }
                }
                val adaptadorChat = AdaptadorChat(this@ChatActivity, mensajesArrayList)
                binding.chatsRV.adapter = adaptadorChat
                binding.chatsRV.scrollToPosition(mensajesArrayList.size - 1)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun cargarInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"

                    binding.TxtNombreUsuario.text = nombres

                    try {
                        Glide.with(applicationContext)
                            .load(imagen)
                            .placeholder(R.drawable.ic_imagen_perfil)
                            .into(binding.ToolbarIV)
                    } catch (e: Exception) {
                        Log.e("ChatActivity", "${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${error.message}")
                }
            })
    }

    private fun validarMensaje() {
        val mensaje = binding.EtMensajeChat.text.toString().trim()
        val tiempo = Constantes.ObtenerTiempoDis()
        if (mensaje.isEmpty()) {
            Toast.makeText(this, "Ingrese un mensaje", Toast.LENGTH_SHORT).show()
        } else {
            enviarMensaje(Constantes.MENSAJE_TIPO_TEXTO, mensaje, tiempo)
        }
    }

    // --- FUNCIÓN UNIFICADA DE ENVÍO ---
    private fun enviarMensaje(tipoMensaje: String, mensaje: String, tiempo: Long) {
        val refChat = FirebaseDatabase.getInstance().getReference("Chats")
        val keyId = refChat.push().key ?: "${System.currentTimeMillis()}"

        val hashMap = HashMap<String, Any>()
        hashMap["idMensaje"] = keyId
        hashMap["tipoMensaje"] = tipoMensaje
        hashMap["mensaje"] = mensaje
        hashMap["emisorUid"] = miUid
        hashMap["receptorUid"] = uid
        hashMap["tiempo"] = tiempo

        refChat.child(chatRuta).child(keyId).setValue(hashMap)
            .addOnSuccessListener {
                binding.EtMensajeChat.setText("")
                if (progressDialog.isShowing) progressDialog.dismiss()
            }
            .addOnFailureListener { e ->
                if (progressDialog.isShowing) progressDialog.dismiss()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- GRABACIÓN DE AUDIO ---
    private fun iniciarGrabacion() {
        audioPath = "${externalCacheDir?.absolutePath}/audio_enviado.m4a"
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }

        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioPath)
            try {
                prepare()
                start()
                Toast.makeText(this@ChatActivity, "Grabando...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("AudioError", "Error al iniciar: ${e.message}")
            }
        }
    }

    private fun detenerGrabacion() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            subirAudioStorage()
        } catch (e: Exception) {
            mediaRecorder = null
        }
    }

    private fun subirAudioStorage() {
        progressDialog.setMessage("Enviando nota de voz...")
        progressDialog.show()

        val tiempo = Constantes.ObtenerTiempoDis()
        val rutaArchivo = "AudiosChat/$tiempo.m4a"
        val storageRef = FirebaseStorage.getInstance().getReference(rutaArchivo)
        val uri = Uri.fromFile(File(audioPath!!))

        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    enviarMensaje(Constantes.MENSAJE_TIPO_AUDIO, downloadUri.toString(), tiempo)
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al subir audio: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- IMÁGENES ---
    private fun imagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleriaARL.launch(intent)
    }

    private val resultadoGaleriaARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            imagenUri = res.data?.data
            subirImgStorage()
        }
    }

    private val solicitarPermisoAlmacenamiento = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) imagenGaleria()
    }

    private fun subirImgStorage() {
        progressDialog.setMessage("Subiendo imagen...")
        progressDialog.show()
        val tiempo = Constantes.ObtenerTiempoDis()
        val storageRef = FirebaseStorage.getInstance().getReference("ImagenesChat/$tiempo")
        storageRef.putFile(imagenUri!!)
            .addOnSuccessListener { task ->
                task.storage.downloadUrl.addOnSuccessListener { uri ->
                    enviarMensaje(Constantes.MENSAJE_TIPO_IMAGEN, uri.toString(), tiempo)
                }
            }
    }
}
