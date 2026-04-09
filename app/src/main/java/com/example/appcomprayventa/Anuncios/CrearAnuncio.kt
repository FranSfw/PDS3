package com.example.appcomprayventa.Anuncios

import android.app.Activity
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
import com.example.appcomprayventa.Modelo.ModeloImagenSeleccionada
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityCrearAnuncioBinding

class CrearAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityCrearAnuncioBinding
    private var imageUri: Uri? = null

    private lateinit var imagenSelecArrayList: ArrayList<ModeloImagenSeleccionada>
    private lateinit var adaptadorImagenSeleccionada: AdaptadorImagenSeleccionada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagenSelecArrayList = ArrayList()

        configurarRecyclerView()

        // Al hacer clic en el ImageView "agregarImg"
        binding.agregarImg.setOnClickListener {
            seleccionarImagenDe()
        }

        val adaptadorCat = ArrayAdapter(this, R.layout.item_categoria, Constantes.categorias)
        binding.Categoria.setAdapter(adaptadorCat)

        val adaptadorCon = ArrayAdapter(this, R.layout.item_condicion, Constantes.condiciones)
        binding.Condicion.setAdapter(adaptadorCon)



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

    // --- Lógica de Cámara ---

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
            // binding.agregarImg.setImageURI(imageUri) // Comentado para que no cambie el icono
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
            // binding.agregarImg.setImageURI(imageUri) // Comentado para que no cambie el icono
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