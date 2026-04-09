package com.example.appcomprayventa.Anuncios

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.appcomprayventa.Adaptadores.AdaptadorImagenSeleccionada
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelo.ModeloAnuncio
import com.example.appcomprayventa.Modelo.ModeloImagenSeleccionada
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityCrearAnuncioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class CrearAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityCrearAnuncioBinding
    private var imageUri: Uri? = null

    private lateinit var imagenSelecArrayList: ArrayList<ModeloImagenSeleccionada>
    private lateinit var adaptadorImagenSeleccionada: AdaptadorImagenSeleccionada

    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        imagenSelecArrayList = ArrayList()

        configurarRecyclerView()

        // Adaptadores para Spinners (AutoCompleteTextView)
        val adaptadorCat = ArrayAdapter(this, R.layout.item_categoria, Constantes.categorias)
        binding.Categoria.setAdapter(adaptadorCat)

        val adaptadorCon = ArrayAdapter(this, R.layout.item_condicion, Constantes.condiciones)
        binding.Condicion.setAdapter(adaptadorCon)

        // Al hacer clic en el ImageView "agregarImg"
        binding.agregarImg.setOnClickListener {
            seleccionarImagenDe()
        }

        // Al hacer clic en el botón "Crear Anuncio"
        binding.BtnCrearAnuncio.setOnClickListener {
            validarDatos()
        }
    }

    private var marca = ""
    private var categoria = ""
    private var condicion = ""
    private var precio = ""
    private var titulo = ""
    private var descripcion = ""

    private fun validarDatos() {
        marca = binding.EtMarca.text.toString().trim()
        categoria = binding.Categoria.text.toString().trim()
        condicion = binding.Condicion.text.toString().trim()
        precio = binding.EtPrecio.text.toString().trim()
        titulo = binding.EtTitulo.text.toString().trim()
        descripcion = binding.EtDescripcion.text.toString().trim()

        if (marca.isEmpty()) {
            binding.EtMarca.error = "Ingrese marca"
            binding.EtMarca.requestFocus()
        } else if (categoria.isEmpty()) {
            binding.Categoria.error = "Seleccione categoría"
            binding.Categoria.requestFocus()
        } else if (condicion.isEmpty()) {
            binding.Condicion.error = "Seleccione condición"
            binding.Condicion.requestFocus()
        } else if (precio.isEmpty()) {
            binding.EtPrecio.error = "Ingrese precio"
            binding.EtPrecio.requestFocus()
        } else if (titulo.isEmpty()) {
            binding.EtTitulo.error = "Ingrese título"
            binding.EtTitulo.requestFocus()
        } else if (descripcion.isEmpty()) {
            binding.EtDescripcion.error = "Ingrese descripción"
            binding.EtDescripcion.requestFocus()
        } else if (imagenSelecArrayList.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos una imagen", Toast.LENGTH_SHORT).show()
        } else {
            subirImagenesStorage()
        }
    }

    private fun subirImagenesStorage() {
        progressDialog.setMessage("Subiendo imágenes...")
        progressDialog.show()

        val urlsImagenes = ArrayList<String>()

        for (i in imagenSelecArrayList.indices) {
            val modeloImagen = imagenSelecArrayList[i]
            val nombreImagen = modeloImagen.id
            val rutaArchivo = "Anuncios/$nombreImagen"

            val storageReference = FirebaseStorage.getInstance().getReference(rutaArchivo)
            storageReference.putFile(modeloImagen.imagenUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    val uriTask = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val downloadUri = uriTask.result.toString()

                    if (uriTask.isSuccessful) {
                        urlsImagenes.add(downloadUri)
                    }

                    if (urlsImagenes.size == imagenSelecArrayList.size) {
                        subirAnuncioBD(urlsImagenes)
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun subirAnuncioBD(urlsImagenes: ArrayList<String>) {
        progressDialog.setMessage("Subiendo anuncio...")

        val tiempo = Constantes.ObtenerTiempoDis()
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        val keyId = ref.push().key

        val modeloAnuncio = ModeloAnuncio(
            "" + keyId,
            "" + firebaseAuth.uid,
            marca,
            categoria,
            condicion,
            precio,
            titulo,
            descripcion,
            Constantes.anuncio_disponible,
            tiempo,
            0.0,
            0.0
        )

        ref.child(keyId!!).setValue(modeloAnuncio)
            .addOnSuccessListener {
                // Una vez guardado el anuncio, guardamos las URLs de las imágenes en un nodo hijo
                val totalImg = urlsImagenes.size
                for (i in 0 until totalImg) {
                    val map = HashMap<String, Any>()
                    map["id"] = "${System.currentTimeMillis() + i}"
                    map["imagenUrl"] = urlsImagenes[i]

                    ref.child(keyId).child("Imagenes").child(map["id"] as String).setValue(map)
                }
                progressDialog.dismiss()
                Toast.makeText(this, "Anuncio publicado", Toast.LENGTH_SHORT).show()
                limpiarCampos()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarCampos() {
        binding.EtMarca.setText("")
        binding.Categoria.setText("")
        binding.Condicion.setText("")
        binding.EtPrecio.setText("")
        binding.EtTitulo.setText("")
        binding.EtDescripcion.setText("")
        imagenSelecArrayList.clear()
        adaptadorImagenSeleccionada.notifyDataSetChanged()
    }

    private fun configurarRecyclerView() {
        adaptadorImagenSeleccionada = AdaptadorImagenSeleccionada(this, imagenSelecArrayList)
        binding.RVImagenes.adapter = adaptadorImagenSeleccionada
    }

    private fun seleccionarImagenDe() {
        val popupMenu = PopupMenu(this, binding.agregarImg)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galería")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        concederPermisosCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                    } else {
                        concederPermisosCamara.launch(arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ))
                    }
                }
                2 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        imagenGaleria()
                    } else {
                        concederPermisosAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            }
            true
        }
    }

    private val concederPermisosCamara = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val todosConcedidos = resultado.values.all { it }
        if (todosConcedidos) {
            imagenCamara()
        } else {
            Toast.makeText(this, "Permisos de cámara denegados", Toast.LENGTH_SHORT).show()
        }
    }

    private fun imagenCamara() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Anuncio")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Imagen_Anuncio_Desc")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        resultadoCamara_ARL.launch(intent)
    }

    private val resultadoCamara_ARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            agregarImagenAlaLista()
        } else {
            Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    private val concederPermisosAlmacenamiento = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { esConcedido ->
        if (esConcedido) imagenGaleria()
        else Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
    }

    private fun imagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }

    private val resultadoGaleria_ARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            imageUri = resultado.data?.data
            agregarImagenAlaLista()
        }
    }

    private fun agregarImagenAlaLista() {
        val id = "${System.currentTimeMillis()}"
        val modeloImagenSeleccionada = ModeloImagenSeleccionada(id, imageUri!!, null, false)
        imagenSelecArrayList.add(modeloImagenSeleccionada)
        adaptadorImagenSeleccionada.notifyItemInserted(imagenSelecArrayList.size - 1)
    }
}