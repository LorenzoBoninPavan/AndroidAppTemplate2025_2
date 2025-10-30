package com.ifpr.androidapptemplate


data class ItemMarketplace(
    // Campos necessários para o Adapter e Firebase
    var id: String? = null,
    var nome: String? = null,
    var endereco: String? = null,
    var descricao: String? = null,
    var preco: Double = 0.0,

    // CAMPOS DE LOCALIZAÇÃO
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
)