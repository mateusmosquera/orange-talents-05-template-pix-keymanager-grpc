package br.com.zupacademy.mateus.endpoint

import br.com.zupacademy.mateus.client.BancoCentralClient
import br.com.zupacademy.mateus.client.ContasDeClientesNoItauClient
import br.com.zupacademy.mateus.client.CreatePixKeyRequest
import br.com.zupacademy.mateus.handler.exceptions.ChavePixExistenteException
import br.com.zupacademy.mateus.model.ChavePix
import br.com.zupacademy.mateus.model.dto.NovaChavePix
import br.com.zupacademy.mateus.repository.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(@Inject val repository: ChavePixRepository,
                          @Inject val itauClient: ContasDeClientesNoItauClient,
                          @Inject val bcbClient: BancoCentralClient) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    fun registra(@Valid novaChave: NovaChavePix): ChavePix {

        if (repository.existsByChave(novaChave.chave)) // 1
            throw ChavePixExistenteException("Chave Pix '${novaChave.chave}' existente")

        val response = itauClient.buscaContaPorTipo(novaChave.clienteId, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        val chave = novaChave.toModel(conta)
        repository.save(chave)

        val bcbRequest = CreatePixKeyRequest.of(chave).also {
            LOGGER.info("Registrando chave Pix no Banco Central do Brasil (BCB): $it")
        }

        val bcbResponse = bcbClient.create(bcbRequest)
        if(bcbResponse.status != HttpStatus.CREATED)
            throw java.lang.IllegalStateException("Erro ao registrar chave pix no Banco Central do Brasil (BCB)")

        chave.atualiza(bcbResponse.body()!!.key)

        return chave

    }
}