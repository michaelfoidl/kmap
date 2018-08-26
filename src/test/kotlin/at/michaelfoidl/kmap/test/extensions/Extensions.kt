package at.michaelfoidl.kmap.test.extensions

import at.michaelfoidl.kmap.mapper.ConcreteMapper

inline fun <SourceT: Any, reified TargetT : Any> ConcreteMapper<SourceT, TargetT>.map(source: SourceT): TargetT {
    val result = convert<TargetT>(source)
    execute(result)
    return result.value!!
}