package br.com.zupacademy.mateus.client

import br.com.zupacademy.mateus.model.response.DadosDaContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.contas.url}")
interface ContasDeClientesNoItauClient {

    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscaContaPorTipo(@PathVariable clienteId: String?, @QueryValue tipo: String) : HttpResponse<DadosDaContaResponse>

}