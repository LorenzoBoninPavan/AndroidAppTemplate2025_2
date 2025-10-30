package com.ifpr.androidapptemplate.ui.adicionar

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // Para rodar o Geocoding assíncrono
import com.ifpr.androidapptemplate.ItemMarketplace // Sua classe de dados
import com.ifpr.androidapptemplate.databinding.FragmentAdicionarItemBinding // O Binding do seu layout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers // Para threads em segundo plano
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class AdicionarItemFragment : Fragment() {

    private var _binding: FragmentAdicionarItemBinding? = null
    // Usa o View Binding para acessar os elementos do layout
    private val binding get() = _binding!!

    // Referência ao nó 'itens_marketplace' no Firebase
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdicionarItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa a referência ao Firebase Realtime Database
        database = FirebaseDatabase.getInstance().getReference("itens_marketplace")

        // Define o Listener para o botão Salvar
        binding.botaoSalvar.setOnClickListener {
            salvarItem()
        }
    }

    /**
     * Função principal: Obtém dados, faz o Geocoding e salva no Firebase.
     */
    private fun salvarItem() {
        // 1. Coleta e Valida Dados
        val nome = binding.inputNome.text.toString().trim()
        val endereco = binding.inputEndereco.text.toString().trim()
        val precoStr = binding.inputPreco.text.toString().trim()
        val descricao = binding.inputDescricao.text.toString().trim()

        if (nome.isEmpty() || endereco.isEmpty() || precoStr.isEmpty()) {
            Toast.makeText(context, "Preencha Nome, Endereço e Preço.", Toast.LENGTH_SHORT).show()
            return
        }
        val preco = precoStr.toDoubleOrNull() ?: 0.0

        // 2. Inicia Coroutine para Geocoding (tarefa em segundo plano)
        lifecycleScope.launch {
            Toast.makeText(context, "Buscando coordenadas, aguarde...", Toast.LENGTH_SHORT).show()

            // 💡 Converte o endereço para Lat/Lng
            val (latitude, longitude) = buscarCoordenadas(endereco)

            if (latitude != 0.0 || longitude != 0.0) {
                // 3. Monta o ItemMarketplace com as coordenadas
                val novoItem = ItemMarketplace(
                    id = null, // O Firebase gera o ID com .push()
                    nome = nome,
                    endereco = endereco,
                    descricao = descricao,
                    preco = preco,
                    latitude = latitude,
                    longitude = longitude
                )

                // 4. Salva no Firebase
                salvarNoDatabase(novoItem)
            } else {
                Toast.makeText(context, "Endereço não encontrado. Tente um endereço mais específico.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Converte o endereço de String para coordenadas (Latitude e Longitude).
     * Esta função é suspensa e roda na thread de I/O.
     */
    private suspend fun buscarCoordenadas(endereco: String): Pair<Double, Double> =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())

                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(endereco, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    return@withContext Pair(address.latitude, address.longitude)
                }
            } catch (e: IOException) {
                // Captura erros de rede ou serviço indisponível
                Log.e("GEOCODING", "Serviço de Geocoding indisponível: ${e.message}")
            }
            // Retorna 0.0, 0.0 em caso de falha
            return@withContext Pair(0.0, 0.0)
        }

    /**
     * Salva o objeto ItemMarketplace no Firebase.
     */
    private fun salvarNoDatabase(item: ItemMarketplace) {
        // .push() gera uma chave única (ID) para o item e o salva
        database.push().setValue(item)
            .addOnSuccessListener {
                Toast.makeText(context, "Item cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                // Limpa os campos
                binding.inputNome.setText("")
                binding.inputEndereco.setText("")
                binding.inputPreco.setText("")
                binding.inputDescricao.setText("")
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FIREBASE", "Erro ao salvar item", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}