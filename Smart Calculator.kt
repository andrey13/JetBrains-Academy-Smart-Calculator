package calculator

val memory = mutableMapOf<String, Int>()

fun countMinus(s: String): Int {
    var m = 0
    for (c in s) {
        if (c == '-') ++m
    }
    return m
}

fun reduceSign(s: String): MutableList<String> {
    val a = s.split("").toMutableList()
    var b = mutableListOf<String>()
    a.removeAt(a.size-1)
    a.removeAt(0)
    var old = "symbol"
    var new = "symbol"
    var factor = 1
    var operand = ""
    for (i in a) {
        new = if (i.matches(Regex("[-+]"))) "sign" else "operand"
        new = if (i.matches(Regex("[()/*]"))) "operator" else new

        if (old != "operand" && new == "operand") operand  = i
        if (old == "operand" && new == "operand") operand += i
        if (old == "operand" && new != "operand") b.add(operand)

        if (old != "sign"    && new == "sign")    factor  = if (i == "+") 1 else -1
        if (old == "sign"    && new == "sign")    factor *= if (i == "+") 1 else -1
        if (old == "sign"    && new != "sign")    b.add(if (factor == 1) "+" else "-")

        if (new == "operator") b.add(i)

        old = new
    }
    if (new == "operand") b.add(operand)
    if (b[0] == "-" || b[0] == "+") b = (mutableListOf("0") + b) as MutableList<String>

    return b
}

fun typeOfitem(i: String): String {
    return when {
        i.matches(Regex("[a-zA-Z]+")) -> "variable"
        i.matches(Regex("[0-9]+"))    -> "operand"
        i.matches(Regex("[-+]"))      -> "operator"
        i.matches(Regex("[/*]"))      -> "operator"
        i == "(" -> "left"
        i == ")" -> "rigth"
        else -> "???"
    }
}

fun priorityOfitem(i: String): Int {
    return when {
        i.matches(Regex("[-+]"))      -> 1
        i.matches(Regex("[/*]"))      -> 2
        else -> 0
    }
}


fun infix2postfix(a: MutableList<String>): MutableList<String> {
    val b = mutableListOf<String>()   // result
    val s = mutableListOf<String>()   // stack

    while (!a.isEmpty()) {
        val i = a[0]
        var j = if (s.isEmpty()) "" else s[s.size-1]
        val ti = typeOfitem(i)
        var tj = typeOfitem(j)
        val pi = priorityOfitem(i)
        var pj = priorityOfitem(j)

        when {
            (ti == "operand") || (ti == "variable") -> b.add(i)  //1
            (ti == "operator") && (s.isEmpty() || tj == "left") -> s.add(i)  //2
            (ti == "operator") && (pi > pj) -> s.add(i)  //3
            (ti == "operator") && (pi <= pj) -> {  //4
                while (!((pi > pj) || (tj == "left"))) {
                    b.add(j)
                    s.removeAt(s.size-1)
                    //println("$a ------ $b ------ $s")
                    if (s.size == 0) break
                    j = s[s.size-1]
                    tj = typeOfitem(j)
                    pj = priorityOfitem(j)
                }
                s.add(i)
            }
            ti == "left" -> s.add(i)  //5
            ti == "rigth" -> {  //6
                while (tj != "left") {
                    b.add(j)
                    s.removeAt(s.size-1)
                    j = s[s.size-1]
                    tj = typeOfitem(j)
                    pj = priorityOfitem(j)
                }
                s.removeAt(s.size-1)
            }
        }
        //println("$a ------ $b ------ $s")
        a.removeAt(0)
    }

    while (!s.isEmpty()) { //7
        b.add(s[s.size-1])
        s.removeAt(s.size-1)
    }

    return b
}

fun str2int(s: String): Int {
    var n = 0
    if (s.matches(Regex("[-]?[0-9]+"))) n = s.toInt()
    if (s.matches(Regex("[a-zA-Z]+"))) n = memory[s] ?: 0
    return n
}

fun calcPostfix(a: MutableList<String>): Int {
    //println(a)
    val s = mutableListOf<String>()   // stack
    for (i in a) {
        val ti = typeOfitem(i)
        when(ti) {
            "operand" -> s.add(i)
            "variable" -> s.add(i)
            "operator" -> {
                val y = str2int(s[s.size-1])
                val x = str2int(s[s.size-2])
                s.removeAt(s.size-1)
                s.removeAt(s.size-1)
                val res= when(i) {
                    "+" -> {
                        //println("$x $i $y")
                        x + y
                    }
                    "-" -> {
                        //println("$x $i $y ${x - y} ${(x-y).toString()}")
                        x - y
                    }
                    "*" -> {
                        //println("$x $i $y")
                        x * y
                    }
                    "/" -> {
                        //println("$x $i $y")
                        x / y
                    }
                    else -> {
                        //println("$x $i $y")
                        0
                    }
                }
                //println("res = $res")
                s.add(res.toString())

            }
        }
    }
    //println(s)
    //print("res=")
    //println(str2int(s[s.size-1]))
    return str2int(s[s.size-1])
}

fun calcMathExpression(s: String): String {
    val str1 = s.replace(" ","")
    val str2 = reduceSign(str1)
    val rcode = testMathExpression(str2)
    if (rcode != "OK") return rcode
    val str3 = infix2postfix(str2)
    //println(str3)
    val result = calcPostfix(str3)
    return result.toString()
}

fun testMathExpression(a: MutableList<String>): String {
    val s = a.joinToString("")
    when {
        Regex("[^-+0-9a-zA-Z*/()]").find(s) != null -> return "Invalid expression"
        Regex("[*/]{2,}").find(s) != null -> return "Invalid expression"
        Regex("[(]").findAll(s).count() != Regex("[)]").findAll(s).count()  -> return "Invalid expression"
        Regex("\\d[a-zA-Z]|[a-zA-Z]\\d").find(s) != null -> return "Invalid assignment"
    }
    for (i in a) {
        if (typeOfitem(i) == "variable")
            if (memory[i] == null) return "Unknown variable"
    }
    return "OK"
}

fun calc(s: String) {
    val a = s.trim().split("=").map {item -> item.trim()}
    // string does not contain an equals sign -------------------------------------------
    if (a.size == 1) {
        println(calcMathExpression(s))
        return
    }
    // string contains an equals sign ---------------------------------------------------
    if (a.size == 2) {
        if (a[0].matches(Regex("[a-zA-Z]+"))) {
            val sum = calcMathExpression(a[1])
            if (sum.matches(Regex("[-]?[0-9]+"))) {
                memory[a[0]] = sum.toInt()
            } else {
                println(sum)
            }
            return
        } else {
            println("Invalid identifier")
            return
        }
    }
    println("Invalid assignment")
}

fun main() {
    while (true) {
        val s = readLine()!!
        // empty input string -----------------------------------------------------------
        if (s == "") continue

        // input string is not command --------------------------------------------------
        if (!s.matches(Regex("[/]\\w*"))) {
            calc(s)
            continue
        }

        // input string is command ------------------------------------------------------
        when (s) {
            "/help" -> {
                println("The program calculates the sum of numbers")
                continue
            }
            "/exit" -> {
                println("Bye!")
                break
            }
        }

        // input string is invalid command ----------------------------------------------
        println("Unknown command")
    }
}
