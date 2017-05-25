package bloomfilter.util

import sun.misc.{Unsafe => JUnsafe}

import scala.language.postfixOps
import scala.util.Try

object Unsafe {
  val unsafe: JUnsafe = Try {
    classOf[JUnsafe]
      .getDeclaredFields
      .find { field =>
        field.getType == classOf[JUnsafe]
      }
      .map { field =>
        field.setAccessible(true)
        field.get(null).asInstanceOf[JUnsafe]
      }
      .getOrElse(throw new IllegalStateException("Can't find instance of sun.misc.Unsafe"))
  } recover {
    case th: Throwable => throw new ExceptionInInitializerError(th)
  } get

}
