package com.ifpr.androidapptemplate.ui.mapa

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ifpr.androidapptemplate.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapItemFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    // Variáveis que vão armazenar os argumentos
    private var itemLatitude: Double = 0.0
    private var itemLongitude: Double = 0.0
    private var itemTitle: String = "Local do Item"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout (o FrameLayout com o SupportMapFragment dentro)
        return inflater.inflate(R.layout.fragment_mapa_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            // Usa as chaves do XML e recebe como Float (o tipo que você definiu)
            itemLatitude = it.getFloat("item_latitude", 0.0f).toDouble()
            itemLongitude = it.getFloat("item_longitude", 0.0f).toDouble()

            // Continua usando 'title_key' que definimos no HomeFragment.kt
            itemTitle = it.getString("title_key", "Local do Item") ?: "Local do Item"
        }

        // 2. Localiza o mapa usando o ID do XML
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?

        // 3. Solicita o mapa assincronamente (chama onMapReady quando estiver pronto)
        mapFragment?.getMapAsync(this)
    }

    // Chamado quando o mapa do Google está pronto para ser usado
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Verifica se as coordenadas são válidas
        if (itemLatitude == 0.0 && itemLongitude == 0.0) {
            Toast.makeText(context, "Localização inválida ou não recebida.", Toast.LENGTH_LONG).show()
            // Centraliza em Curitiba (Paraná) se o dado for inválido
            val curitiba = LatLng(-25.4284, -49.2733)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curitiba, 10f))
            return
        }

        val localizacaoItem = LatLng(itemLatitude, itemLongitude)

        // Adiciona um marcador
        googleMap.addMarker(
            MarkerOptions()
                .position(localizacaoItem)
                .title(itemTitle)
        )

        // Move a câmera para o local com zoom
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(localizacaoItem, 15f))

        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    // ... Outras funções de Fragmento (ex: onDestroyView)
}