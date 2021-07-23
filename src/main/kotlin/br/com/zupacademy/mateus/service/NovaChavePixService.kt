package br.com.zupacademy.mateus.service

import br.com.zupacademy.mateus.client.ContasDeClientesNoItauClient
import br.com.zupacademy.mateus.handler.exceptions.ChavePixExistenteException
import br.com.zupacademy.mateus.model.ChavePix
import br.com.zupacademy.mateus.model.dto.NovaChavePix
import br.com.zupacademy.mateus.repository.ChavePixRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(@Inject val repository: ChavePixRepository,
                          @Inject val itauClient: ContasDeClientesNoItauClient,) {

    //private val LOGGER = LoggerFactory.getLogger(this::class.java)

    fun registra(@Valid novaChave: NovaChavePix): ChavePix {

        if (repository.existsByChave(novaChave.chave)) // 1
            throw ChavePixExistenteException("Chave Pix '${novaChave.chave}' existente")

        val response = itauClient.buscaContaPorTipo(novaChave.clienteId, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        val chave = novaChave.toModel(conta)
        repository.save(chave)

        return chave

    }
}