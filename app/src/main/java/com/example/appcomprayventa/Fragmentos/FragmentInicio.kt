package com.example.appcomprayventa.Fragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appcomprayventa.Adaptadores.AdaptadorAnuncio
import com.example.appcomprayventa.Modelo.ModeloAnuncio
import com.example.appcomprayventa.databinding.FragmentInicioBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentInicio : Fragment() {

    private lateinit var binding: FragmentInicioBinding
    private lateinit var anuncioArrayList: ArrayList<ModeloAnuncio>
    private lateinit var adaptadorAnuncio: AdaptadorAnuncio

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarAnuncios()
    }

    private fun cargarAnuncios() {
        anuncioArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                anuncioArrayList.clear()
                for (ds in snapshot.children) {
                    val modeloAnuncio = ds.getValue(ModeloAnuncio::class.java)
                    if (modeloAnuncio != null) {
                        anuncioArrayList.add(modeloAnuncio)
                    }
                }
                // Usamos el mismo adaptador que ya creamos, ya tiene la lógica de click y de imagen
                adaptadorAnuncio = AdaptadorAnuncio(requireContext(), anuncioArrayList)
                binding.anunciosRV.adapter = adaptadorAnuncio
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}
