package br.com.zupacademy.mateus.endpoint.lista

import br.com.zupacademy.mateus.KeymanagerListaGrpcServiceGrpc
import br.com.zupacademy.mateus.ListaChavesPixRequest
import br.com.zupacademy.mateus.model.ChavePix
import br.com.zupacademy.mateus.model.ContaAssociada
import br.com.zupacademy.mateus.model.TipoDeChave
import br.com.zupacademy.mateus.model.TipoDeConta
import br.com.zupacademy.mateus.repository.ChavePixRepository
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

/**
 * TIP: Necessario desabilitar o controle transacional (transactional=false) pois o gRPC Server
 * roda numa thread separada, caso contrário não será possível preparar cenário dentro do método @Test
 */
@MicronautTest(transactional = false)
internal class ListaChavesEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceBlockingStub,
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    /**
     * TIP: por padrão roda numa transação isolada
     */
    @BeforeEach
    fun setup() {
        repository.save(chave(tipo = TipoDeChave.EMAIL, chave = "mateus@gmail.com", clienteId = CLIENTE_ID))
        repository.save(chave(tipo = TipoDeChave.ALEATORIA, chave = "randomkey-2", clienteId = UUID.randomUUID()))
        repository.save(chave(tipo = TipoDeChave.ALEATORIA, chave = "randomkey-3", clienteId = CLIENTE_ID))
    }

    /**
     * TIP: por padrão roda numa transação isolada
     */
    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves do cliente`() {
        // cenário
        val clienteId = CLIENTE_ID.toString()

        // ação
        val response = grpcClient.lista(ListaChavesPixRequest.newBuilder()
            .setClienteId(clienteId)
            .build())

        // validação
        with (response.chavesList) {
            assertThat(this, hasSize(2))
            assertThat(
                this.map { Pair(it.tipo, it.chave) }.toList(),
                containsInAnyOrder(
                    Pair(br.com.zupacademy.mateus.TipoDeChave.ALEATORIA, "randomkey-3"),
                    Pair(br.com.zupacademy.mateus.TipoDeChave.EMAIL, " mateus@gmail.com.br")
                )
            )
        }
    }

    @Test
    fun `nao deve listar as chaves do cliente quando cliente nao possuir chaves`() {
        // cenário
        val clienteSemChaves = UUID.randomUUID().toString()

        // ação
        val response = grpcClient.lista(ListaChavesPixRequest.newBuilder()
            .setClienteId(clienteSemChaves)
            .build())

        // validação
        assertEquals(0, response.chavesCount)
    }

    @Test
    fun `nao deve listar todas as chaves do cliente quando clienteId for invalido`() {
        // cenário
        val clienteIdInvalido = ""

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.lista(
                ListaChavesPixRequest.newBuilder()
                .setClienteId(clienteIdInvalido)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente ID não pode ser nulo ou vazio", status.description)
        }
    }

    @Factory
    class ClientsListaChavePix  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceBlockingStub? {
            return KeymanagerListaGrpcServiceGrpc.newBlockingStub(channel)
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