package br.com.zupacademy.mateus.endpoint.carrega

import br.com.zupacademy.mateus.CarregaChavePixRequest
import br.com.zupacademy.mateus.CarregaChavePixResponse
import br.com.zupacademy.mateus.KeymanagerCarregaGrpcServiceGrpc
import br.com.zupacademy.mateus.client.BancoCentralClient
import br.com.zupacademy.mateus.handler.ErrorHandler
import br.com.zupacademy.mateus.repository.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class CarregaChaveEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BancoCentralClient,
    @Inject private val validator: Validator,
) : KeymanagerCarregaGrpcServiceGrpc.KeymanagerCarregaGrpcServiceImplBase() {

    override fun carrega(request: CarregaChavePixRequest,
                         responseObserver: StreamObserver<CarregaChavePixResponse>
    ) {
        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(CarregaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }

}