package whatever

import fx._


infix type errors[R, E] = Effect[E] ?=> R

def program: Int errors String = {
    val r = shift[String, Int]("Boom") + Right(1).bind + Right(2).bind
    println("Hello!")
    r
}

def program2: Int errors String = 
    shift[String, Int]("Boom") + Right(1).bind + Right(2).bind

def program3: List[Int] errors String = {
    val effects = List(Right(1), Right(2))
    for {
      v <- effects
      z = v.bind + 1
    } yield z
}


@main def hello() = {
    val result = cont {
        program + program
    }.toEither

    val result2 = fold(
     program + program, 
     identity,
     identity
    )

    val result3 = cont {
        program3
    }.fold(identity, identity)
    println(result)
    println(result2)
    println(result3)
}
// [info] running whatever.hello 
// Left(Boom)
// Boom

/**
get
set
update
raise
env
log
trace
use
transact
emit
collect
schedule



*/