package com.ifpr.androidapptemplate.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.ifpr.androidapptemplate.R
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.drawToBitmap
import com.bumptech.glide.Glide
import com.google.firebase.ai.type.content

class AiLogicFragment : Fragment() {

    private lateinit var promptInput: EditText
    private lateinit var resultText: TextView
    private lateinit var generateButton: Button
    private lateinit var model: GenerativeModel

    private lateinit var imageButton: Button
    private var imageUri: Uri? = null
    private lateinit var itemImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_ai_logic, container, false)

        promptInput = view.findViewById(R.id.prompt_input)
        resultText = view.findViewById(R.id.result_text)
        generateButton = view.findViewById(R.id.btn_generate)

        // Inicialização correta do modelo
        model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash")

        imageButton = view.findViewById(R.id.btn_select_image)
        itemImageView = view.findViewById(R.id.bitmapImageView)

        // Launcher para selecionar imagem
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                Glide.with(this).load(imageUri).into(itemImageView)
                resultText.text = "Imagem selecionada. Pronto para gerar."
            } else {
                imageUri = null // Garantir que a Uri seja limpa se a seleção falhar
                itemImageView.setImageDrawable(null) // Limpar ImageView
                resultText.text = "Nenhuma imagem selecionada."
            }
        }

        imageButton.setOnClickListener {
            pickImage.launch("image/*")
        }


        generateButton.setOnClickListener {
            val prompt = promptInput.text.toString().trim()
            if (prompt.isNotEmpty()) {
                resultText.text = "Aguardando resposta..."
                // Verifica se uma imagem foi carregada na ImageView
                if (itemImageView.drawable != null && imageUri != null) {
                    try {
                        // Converte o drawable da ImageView para Bitmap
                        val bitmap = itemImageView.drawToBitmap()
                        generateFromPrompt(prompt, bitmap)
                    } catch (e: Exception) {
                        resultText.text = "Erro ao processar imagem: ${e.message}"
                    }
                } else {
                    // Chama a versão sem imagem
                    generateFromPrompt(prompt)
                }
            } else {
                resultText.text = "Digite um **prompt** para continuar."
            }
        }

        return view
    }

    // --- Funções generateFromPrompt movidas para fora do onCreateView/setOnClickListener ---

    private fun generateFromPrompt(prompt: String, bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                // Conteúdo que inclui imagem e texto
                val promptImage = content {
                    image(bitmap)
                    text(prompt)
                }
                val response = model.generateContent(promptImage)
                resultText.text = response.text ?: "**Nenhuma resposta recebida**."
            } catch (e: Exception) {
                resultText.text = "Erro ao gerar resposta com imagem: ${e.message}"
            }
        }
    }

    private fun generateFromPrompt(prompt: String) {
        lifecycleScope.launch {
            try {
                // Conteúdo apenas com texto
                val response = model.generateContent(prompt)
                resultText.text = response.text ?: "**Nenhuma resposta recebida**."
            } catch (e: Exception) {
                resultText.text = "Erro ao gerar resposta apenas com texto: ${e.message}"
            }
        }
    }
}