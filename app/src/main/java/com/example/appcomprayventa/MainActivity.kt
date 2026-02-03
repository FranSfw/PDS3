package com.example.appcomprayventa


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appcomprayventa.Fragmentos.FragmentInicio
import com.example.appcomprayventa.Fragmentos.FragmentCuenta
import com.example.appcomprayventa.Fragmentos.FragmentMisAnuncios
import com.example.appcomprayventa.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        verFragmentInicio()
        binding.BottomNV.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.Item_Inicio->{
                    true
                }
                R.id.Item_Chats->{
                    true
                }
                R.id.Item_Mis_Anuncios->{
                    true
                }
                R.id.Item_Cuenta->{
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun verFragmentInicio(){
        binding.TituloRL.text = "Inicio"
        val fragment = FragmentInicio()
        val fragmenteTransition = supportFragmentManager.beginTransaction()
        fragmenteTransition.replace(binding.FragmentL1.id, fragment, "FragmentInicio")
        fragmenteTransition.commit()
    }
    private fun verFragmentChats(){
        binding.TituloRL.text = "Chats"
        val fragment = FragmentCuenta()
        val fragmenteTransition = supportFragmentManager.beginTransaction()
        fragmenteTransition.replace(binding.FragmentL1.id, fragment, "FragmentChats")
        fragmenteTransition.commit()
    }
    private fun verFragmentMisAnuncios(){
        binding.TituloRL.text = "Mis Anuncios"
        val fragment = FragmentMisAnuncios()
        val fragmenteTransition = supportFragmentManager.beginTransaction()
        fragmenteTransition.replace(binding.FragmentL1.id, fragment, "FragmentMisAnuncios")
        fragmenteTransition.commit()
    }
    private fun verFragmentCuenta(){
        binding.TituloRL.text = "Cuenta"
        val fragment = FragmentCuenta()
        val fragmenteTransition = supportFragmentManager.beginTransaction()
        fragmenteTransition.replace(binding.FragmentL1.id, fragment, "FragmentCuenta")
        fragmenteTransition.commit()
    }
}