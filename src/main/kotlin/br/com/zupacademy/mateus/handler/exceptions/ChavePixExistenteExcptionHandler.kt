package br.com.zupacademy.mateus.handler.exceptions

import br.com.zupacademy.mateus.handler.ExceptionHandler
import br.com.zupacademy.mateus.handler.ExceptionHandler.StatusWithDetails
import javax.inject.Singleton
import io.grpc.Status

@Singleton
class ChavePixExistenteExceptionHandler : ExceptionHandler<ChavePixExistenteException> {

    override fun handle(e: ChavePixExistenteException):StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.ALREADY_EXISTS
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixExistenteException
    }
}