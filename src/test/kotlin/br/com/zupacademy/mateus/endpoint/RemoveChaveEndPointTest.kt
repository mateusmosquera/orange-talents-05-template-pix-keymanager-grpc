package br.com.zupacademy.mateus.endpoint

import br.com.zupacademy.mateus.KeymanagerRemoveGrpcServiceGrpc
import br.com.zupacademy.mateus.RemoveChavePixRequest
import br.com.zupacademy.mateus.model.ChavePix
import br.com.zupacademy.mateus.model.ContaAssociada
import br.com.zupacademy.mateus.model.TipoDeChave
import br.com.zupacademy.mateus.model.TipoDeConta
import br.com.zupacademy.mateus.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class RemoveChaveEndPointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub
){

    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setup(){
        CHAVE_EXISTENTE = repository.save(chave(
            tipo = TipoDeChave.EMAIL,
            chave = "mateus@gmail.com",
            clienteId = UUID.randomUUID()
        ))
    }

    @AfterEach
    fun cleanUp(){
        repository.deleteAll()
    }

    @Test
    fun `deve remover chave pix`(){

        val response = grpcClient.remove(RemoveChavePixRequest.newBuilder()
                                                .setPixId(CHAVE_EXISTENTE.id.toString())
                                                .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                                                .build())

        assertEquals(CHAVE_EXISTENTE.id.toString(), response.pixId)
        assertEquals(CHAVE_EXISTENTE.clienteId.toString(), response.clienteId)

    }

    @Test
    fun `nao deve remover chave pix quando chave inexistente`(){

        val pixIdNaoExistente = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setPixId(pixIdNaoExistente)
                .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                .build())
        }

        with(thrown){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix quando chave existe mas pertence a outro cliente`(){
        val outroClientId = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .setClienteId(outroClientId)
                .build())
        }

        with(thrown){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada ou não pertence ao cliente", status.description)
        }
    }


    @Factory
    class ClientsRemoveChavePix  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub? {
            return KeymanagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipo: br.com.zupacademy.mateus.model.TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipo = tipo,
            chave = chave,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rogerio Silva",
                cpfDoTitular = "18458928019",
                agencia = "1234",
                numeroDaConta = "345678"
            )
        )
    }
}