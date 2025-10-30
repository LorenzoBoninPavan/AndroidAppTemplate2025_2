package com.ifpr.androidapptemplate.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.ItemMarketplace
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding
import com.ifpr.androidapptemplate.ui.adapter.ItemMarketplaceAdapter
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.android.gms.location.FusedLocationProviderClient // Importa o serviço de localização
import com.google.android.gms.location.LocationServices // Importa a classe do serviço

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Localização
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // Firebase e Adapter (código mantido)
    private lateinit var database: DatabaseReference
    private lateinit var itemMarketplaceAdapter: ItemMarketplaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializa o serviço de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 2. Inicializa o Firebase e Adapter (lógica de clique para o mapa)
        database = Firebase.database.getReference("itens_marketplace")
        itemMarketplaceAdapter = ItemMarketplaceAdapter { item ->
            val bundle = Bundle().apply {
                // IDs de argumento corretas do seu mobile_navigation.xml
                putFloat("item_latitude", item.latitude.toFloat())
                putFloat("item_longitude", item.longitude.toFloat())
                putString("title_key", item.nome)
            }
            try {
                // ID de navegação corrigida
                findNavController().navigate(R.id.nav_mapa_item, bundle)
            } catch (e: Exception) {
                Log.e("NAV_ERROR", "Erro ao navegar para o mapa: ${e.message}", e)
            }
        }

        // 3. Configura o RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = itemMarketplaceAdapter
        }

        // 4. Configura o clique no novo botão de localização
        binding.btnGetLocation.setOnClickListener {
            checkLocationPermission()
        }

        // 5. Carrega os dados do Firebase
        loadItemsFromFirebase()
    }

    // Método que verifica as permissões antes de chamar o GPS
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicita permissão se ainda não tiver
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Se a permissão já existe, chama a função de obter localização
            getCurrentLocation()
        }
    }

    // O método principal que obtém a localização
    private fun getCurrentLocation() {
        // Verifica novamente (pode ser redundante, mas seguro)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val lng = location.longitude
                        Toast.makeText(context, "Localização: Lat: $lat, Lng: $lng", Toast.LENGTH_LONG).show()

                        // Opcional: Você pode passar esta localização para um mapa,
                        // ou usá-la para filtrar os itens da lista, etc.

                    } else {
                        Toast.makeText(context, "Localização não disponível. Tente novamente.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("GPS_ERROR", "Falha ao obter localização", e)
                    Toast.makeText(context, "Erro ao obter localização: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida: tenta obter a localização
                getCurrentLocation()
            } else {
                // Permissão negada: exibe um aviso
                Toast.makeText(context, "Permissão de localização negada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ... (loadItemsFromFirebase e onDestroyView permanecem iguais)

    private fun loadItemsFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itemsList = mutableListOf<ItemMarketplace>()
                for (itemSnapshot in snapshot.children) {
                    try {
                        val item = itemSnapshot.getValue(ItemMarketplace::class.java)
                        if (item != null) {
                            item.id = itemSnapshot.key
                            itemsList.add(item)
                        }
                    } catch (e: DatabaseException) {
                        Log.e("FIREBASE_ERROR", "Erro ao desserializar item.", e)
                    }
                }
                itemMarketplaceAdapter.submitList(itemsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FIREBASE_ERROR", "Falha ao ler valor.", error.toException())
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}