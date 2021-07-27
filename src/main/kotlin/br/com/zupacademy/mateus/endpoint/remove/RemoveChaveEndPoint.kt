package br.com.zupacademy.mateus.endpoint.remove

import br.com.zupacademy.mateus.KeymanagerRemoveGrpcServiceGrpc
import br.com.zupacademy.mateus.RemoveChavePixRequest
import br.com.zupacademy.mateus.RemoveChavePixResponse
import br.com.zupacademy.mateus.handler.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveChaveEndPoint(@Inject private val service: RemoveChaveService)
    : KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceImplBase(){

    override fun remove(
        request: RemoveChavePixRequest,
        responseObserver: StreamObserver<RemoveChavePixResponse>
    ) {
        service.remove(clienteId = request.clienteId, pixId = request.pixId)

        responseObserver.onNext(RemoveChavePixResponse.newBuilder()
                                    .setClienteId(request.clienteId)
                                    .setPixId(request.pixId)
                                    .build())
        responseObserver.onCompleted()
    }
}