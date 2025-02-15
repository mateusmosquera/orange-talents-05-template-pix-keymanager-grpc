package br.com.zupacademy.mateus.endpoint.remove

import br.com.zupacademy.mateus.CarregaChavePixRequest
import br.com.zupacademy.mateus.client.BancoCentralClient
import br.com.zupacademy.mateus.client.DeletePixKeyRequest
import br.com.zupacademy.mateus.handler.exceptions.ChavePixNaoEncontradaException
import br.com.zupacademy.mateus.model.annotations.ValidUUID
import br.com.zupacademy.mateus.repository.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChaveService (@Inject val repository: ChavePixRepository,
                          @Inject val bcbClient: BancoCentralClient){

    @Transactional
    fun remove(
        @NotBlank @ValidUUID(message = "cliente ID com formato inváludo") clienteId: String?,
        @NotBlank @ValidUUID(message = "pix ID com formato inválido") pixId: String?,
    ){
        val uuidPixId = UUID.fromString(pixId)
        val uuidClienteId = UUID.fromString(clienteId)

        val chave = repository.findByIdAndClienteId(uuidPixId, uuidClienteId)
            .orElseThrow { ChavePixNaoEncontradaException("Chave Pix não encontrada ou não pertence ao cliente") }

        repository.deleteById(uuidPixId)

        val request = DeletePixKeyRequest(chave.chave)

        val bcbResponse = bcbClient.delete(chave.chave,request)
        if(bcbResponse.status != HttpStatus.OK){
            throw IllegalStateException("Erro ao remover chave Pix no Banco Central do Bransil (BCB)")
        }

    }

}
