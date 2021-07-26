package br.com.zupacademy.mateus.handler.exceptions

import br.com.zupacademy.mateus.handler.ExceptionHandler
import br.com.zupacademy.mateus.handler.ExceptionHandler.*
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixNaoEncontradaExceptionHandler : ExceptionHandler<ChavePixNaoEncontradaException> {

    override fun handle(e: ChavePixNaoEncontradaException): StatusWithDetails {
       return StatusWithDetails(Status.NOT_FOUND
           .withDescription(e.message)
           .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixNaoEncontradaException
    }


}