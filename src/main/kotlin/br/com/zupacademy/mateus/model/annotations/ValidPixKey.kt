package br.com.zupacademy.mateus.model.annotations

import br.com.zupacademy.mateus.model.dto.NovaChavePix
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass


@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator:: class])
annotation class ValidPixKey(
    val message: String = "chave Pix inválida (\${validatedValue.tipo})",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)

@Singleton
class ValidPixKeyValidator : ConstraintValidator<ValidPixKey, NovaChavePix>{

    override fun isValid(
        value: NovaChavePix?,
        context: ConstraintValidatorContext?
    ): Boolean {

        if(value?.tipo == null) {
            return false
        }

        return value.tipo.valida(value.chave)
    }

}
