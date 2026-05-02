package annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Greeting(val message: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
// porque o que vai mudar é o regex dependendo do que quisermos apanhar, daí ser o parâmetro
annotation class Extract(val regex: String)