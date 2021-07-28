package br.com.zupacademy.mateus.endpoint.lista

import br.com.zupacademy.mateus.*
import br.com.zupacademy.mateus.handler.ErrorHandler
import br.com.zupacademy.mateus.repository.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChaveEndPoint(@Inject private val repository: ChavePixRepository)
    : KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceImplBase(){

    override fun lista(
        request: ListaChavesPixRequest,
        responseObserver: StreamObserver<ListaChavesPixResponse>
    ) {
       if(request.clienteId.isNullOrBlank()){
           throw IllegalArgumentException("Cliente ID não pode ser nulo ou vazio")
       }

       val clienteId = UUID.fromString(request.clienteId)
       val chaves = repository.findAllByClienteId(clienteId).map {
           ListaChavesPixResponse.ChavePix.newBuilder()
               .setPixId(it.id.toString())
               .setTipo(TipoDeChave.valueOf(it.tipo.name))
               .setChave(it.chave)
               .setTipoDeConta(TipoDeConta.valueOf(it.tipoDeConta.name))
               .setCriadaEm(it.criadaEm.let {
                   val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                   Timestamp.newBuilder()
                       .setSeconds(createdAt.epochSecond)
                       .setNanos(createdAt.nano)
                       .build()
               })
               .build()
       }

       responseObserver.onNext(ListaChavesPixResponse.newBuilder()
           .setClienteId(clienteId.toString())
           .addAllChaves(chaves)
           .build())

       responseObserver.onCompleted()
    }
}