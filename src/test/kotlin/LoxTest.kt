import io.kotest.core.spec.style.StringSpec
import lox.Lox

class LoxTest :
    StringSpec({
        "execute lox" {
            val lox = Lox

            lox.main(arrayOf("./test.lox"))
        }
    })
