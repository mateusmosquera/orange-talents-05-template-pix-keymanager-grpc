package br.com.zupacademy.mateus.endpoint

import br.com.zupacademy.mateus.RegistraChavePixRequest
import br.com.zupacademy.mateus.TipoDeChave.*
import br.com.zupacademy.mateus.TipoDeConta.*
import br.com.zupacademy.mateus.model.TipoDeChave
import br.com.zupacademy.mateus.model.TipoDeConta
import br.com.zupacademy.mateus.model.dto.NovaChavePix

fun RegistraChavePixRequest.toModel(): NovaChavePix {
    return NovaChavePix(
        clienteId = clienteId,
        tipo = when (tipoDeChave){
            UNKNOWN_TIPO_CHAVE -> null
            else -> TipoDeChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when(tipoDeConta){
            UNKNOWN_TIPO_CONTA -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name)
        }

    )
}