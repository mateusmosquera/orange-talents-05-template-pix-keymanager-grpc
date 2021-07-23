package br.com.zupacademy.mateus.endpoint

import br.com.zupacademy.mateus.KeymanagerRegistraGrpcServiceGrpc
import br.com.zupacademy.mateus.RegistraChavePixRequest
import br.com.zupacademy.mateus.TipoDeChave
import br.com.zupacademy.mateus.TipoDeConta.CONTA_CORRENTE
import br.com.zupacademy.mateus.client.ContasDeClientesNoItauClient
import br.com.zupacademy.mateus.model.ChavePix
import br.com.zupacademy.mateus.model.ContaAssociada
import br.com.zupacademy.mateus.model.TipoDeChave.*
import br.com.zupacademy.mateus.model.TipoDeConta
import br.com.zupacademy.mateus.model.response.DadosDaContaResponse
import br.com.zupacademy.mateus.model.response.InstituicaoResponse
import br.com.zupacademy.mateus.model.response.TitularResponse
import br.com.zupacademy.mateus.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerRegistraGrpcServiceGrpc.KeymanagerRegistraGrpcServiceBlockingStub
){

    @Inject
    lateinit var itauClient: ContasDeClientesNoItauClient;

    companion object{
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup(){
        repository.deleteAll()
    }

    @Test
    fun `deve registrar nova chave pix`(){
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        val response =  grpcClient.registra(RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("mateus@gmail.com")
            .setTipoDeConta(CONTA_CORRENTE)
            .build())

        with(response){
            assertEquals(CLIENT_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando chave existente`(){

        repository.save(chave(
            tipo = CPF,
            chave = "18458928019",
            clienteId = CLIENT_ID
        ))

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENT_ID.toString())
                .setTipoDeChave(TipoDeChave.CPF)
                .setChave("18458928019")
                .setTipoDeConta(CONTA_CORRENTE)
                .build())
        }

        with(thrown){
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '18458928019' existente", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao encontrar dados da conta cliente`(){
        `when`(itauClient.buscaContaPorTipo(
            clienteId = CLIENT_ID.toString(),
            tipo = "CONTA_CORRENTE")
        ).thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENT_ID.toString())
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setChave("mateus@gmail.com")
                .setTipoDeConta(CONTA_CORRENTE)
                .build())
        }

        with(thrown){
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itau", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos`(){

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder().build())
        }

        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }


    @MockBean(ContasDeClientesNoItauClient::class)
    fun itauClient(): ContasDeClientesNoItauClient? {
        return Mockito.mock(ContasDeClientesNoItauClient::class.java)
    }

    @Factory
    class Clients  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRegistraGrpcServiceGrpc.KeymanagerRegistraGrpcServiceBlockingStub? {
            return KeymanagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun dadosDaContaResponse(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", ContaAssociada.ITAU_UNIBANCO_ISPB),
            agencia = "1234",
            numero = "345678",
            titular = TitularResponse("Rogerio Silva", "18458928019")
        )
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