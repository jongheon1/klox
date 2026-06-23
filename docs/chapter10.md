# 10장 · Functions (함수)

9장까지의 Lox는 튜링 완전하지만, 코드를 **재사용 단위로 묶을** 방법이 없다. 같은 로직을 쓰려면 매번 복붙해야 한다. 이 장에서 함수 호출, 함수 선언, 인자, 반환값, 그리고 **클로저**까지 붙여 Lox를 진짜 프로그래밍 언어답게 만든다.

---

## 10.1 함수 호출 (Function Calls)

`average(1, 2)` 같은 호출 문법부터 본다. 호출은 "피호출자(callee) 뒤에 괄호로 묶인 인자 목록"이다.

```
unary     → ( "!" | "-" ) unary | call ;
call      → primary ( "(" arguments? ")" )* ;
arguments → expression ( "," expression )* ;
```

`call`이 `primary`보다 우선순위가 높다(`unary` 아래에 끼운다). `( ... )*` 반복은 `fn(1)(2)(3)`처럼 **연쇄 호출**을 좌결합으로 받기 위함이다 — 호출의 결과가 또 호출 가능한 값일 수 있다.

```java
private Expr call() {
  Expr expr = primary();

  while (true) {
    if (match(LEFT_PAREN)) {
      expr = finishCall(expr);     // 괄호를 만날 때마다 한 겹 더 감싼다
    } else {
      break;
    }
  }
  return expr;
}

private Expr finishCall(Expr callee) {
  List<Expr> arguments = new ArrayList<>();
  if (!check(RIGHT_PAREN)) {
    do {
      if (arguments.size() >= 255) {           // 인자 개수 상한
        error(peek(), "Can't have more than 255 arguments.");
      }
      arguments.add(expression());
    } while (match(COMMA));
  }

  Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
  return new Expr.Call(callee, paren, arguments);
}
```

`paren`(닫는 괄호 토큰)을 노드에 담아 둔다 — 런타임 에러가 났을 때 보고할 위치로 쓰기 위해서다.

새 AST 노드:

```java
"Call : Expr callee, Token paren, List<Expr> arguments",
```

> 인자 목록의 콤마는 6장에서 추가한 **콤마 연산자**와 충돌하지 않는다. 인자는 `expression()`이 아니라 `assignment()`(콤마보다 한 단계 위)로 파싱해야 `f(1, 2)`의 콤마가 인자 구분자로 읽힌다. 이 레포의 `finishCall`도 `assignment()`를 호출한다.

---

## 10.2 호출 가능한 것의 인터페이스 (LoxCallable)

`(callee)(arguments)`를 평가하려면 "이 값이 호출 가능한가"를 통일된 방식으로 물어야 한다. 함수든, 나중에(12장) 추가할 클래스든 똑같이 다루기 위해 인터페이스를 둔다.

```java
interface LoxCallable {
  int arity();                                       // 기대하는 인자 개수
  Object call(Interpreter interpreter, List<Object> arguments);
}
```

호출 평가는 두 가지를 검사한다 — **호출 가능한 값인가**, 그리고 **인자 개수가 맞는가**.

```java
@Override
public Object visitCallExpr(Expr.Call expr) {
  Object callee = evaluate(expr.callee);

  List<Object> arguments = new ArrayList<>();
  for (Expr argument : expr.arguments) {
    arguments.add(evaluate(argument));               // 인자는 왼→오 순서로 평가
  }

  if (!(callee instanceof LoxCallable)) {
    throw new RuntimeError(expr.paren,
        "Can only call functions and classes.");     // "totally not a function"() 같은 것 차단
  }

  LoxCallable function = (LoxCallable) callee;
  if (arguments.size() != function.arity()) {
    throw new RuntimeError(expr.paren, "Expected " +
        function.arity() + " arguments but got " +
        arguments.size() + ".");                      // 개수 불일치도 런타임 에러
  }

  return function.call(this, arguments);
}
```

`arity` 검사를 **런타임에 매 호출마다** 한다는 점에 주목하라 — 이 비용이 10장 챌린지 1번의 소재다.

---

## 10.3 네이티브 함수 (Native Functions)

언어가 바깥 세상과 닿는 통로다. 시간을 재는 `clock`을 인터프리터가 부팅할 때 전역에 심는다. 네이티브 함수는 `LoxCallable`을 자바 쪽에서 직접 구현한 객체일 뿐이다.

```java
globals.define("clock", new LoxCallable() {
  @Override public int arity() { return 0; }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    return (double) System.currentTimeMillis() / 1000.0;
  }

  @Override public String toString() { return "<native fn>"; }
});
```

여기서 **전역 환경**(`globals`)이 등장한다. 인터프리터의 `environment` 필드는 현재 스코프를 가리키며 들고나지만, `globals`는 항상 맨 바깥을 고정으로 가리킨다 — 네이티브 함수와 최상위 선언이 사는 곳이다.

---

## 10.4 함수 선언 (Function Declarations)

```
declaration → funDecl | varDecl | statement ;
funDecl     → "fun" function ;
function    → IDENTIFIER "(" parameters? ")" block ;
parameters  → IDENTIFIER ( "," IDENTIFIER )* ;
```

`function`을 따로 뺀 이유는 12장에서 클래스 메서드가 같은 규칙을 재사용하기 때문이다(그래서 파서 함수가 `kind` 문자열을 받는다 — 에러 메시지를 "function"/"method"로 바꿔 끼우려고).

```java
private Stmt function(String kind) {
  Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
  consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");

  List<Token> parameters = new ArrayList<>();
  if (!check(RIGHT_PAREN)) {
    do {
      if (parameters.size() >= 255) {
        error(peek(), "Can't have more than 255 parameters.");
      }
      parameters.add(consume(IDENTIFIER, "Expect parameter name."));
    } while (match(COMMA));
  }
  consume(RIGHT_PAREN, "Expect ')' after parameters.");

  consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
  List<Stmt> body = block();                 // block()은 '{'가 이미 소비됐다고 가정
  return new Stmt.Function(name, parameters, body);
}
```

새 AST 노드:

```java
"Function : Token name, List<Token> params, List<Stmt> body",
```

---

## 10.5 함수 객체 (LoxFunction)

`Stmt.Function`은 **선언 구문(syntax)**이다. 실행 가능한 **런타임 값**으로 감싸는 것이 `LoxFunction`이다 — `LoxCallable`을 구현해 인터프리터가 호출할 수 있게 한다.

```java
class LoxFunction implements LoxCallable {
  private final Stmt.Function declaration;
  private final Environment closure;              // 10.6에서 의미가 생긴다

  LoxFunction(Stmt.Function declaration, Environment closure) {
    this.declaration = declaration;
    this.closure = closure;
  }

  @Override public int arity() { return declaration.params.size(); }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment environment = new Environment(closure);     // 호출마다 새 환경
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme,  // 매개변수 ← 인자 바인딩
          arguments.get(i));
    }

    interpreter.executeBlock(declaration.body, environment);
    return null;                                            // return 없으면 nil
  }

  @Override public String toString() { return "<fn " + declaration.name.lexeme + ">"; }
}
```

핵심은 **호출마다 새 `Environment`를 만든다**는 것이다. 그래야 재귀가 자기 자신을 여러 번 호출해도 각 호출의 지역 변수가 섞이지 않는다. 그 새 환경에 매개변수를 인자 값으로 정의한 뒤, 9장의 `executeBlock`으로 본문을 실행한다.

함수 선언 자체를 실행하는 일은 "함수 객체를 만들어 이름에 묶는" 것뿐이다.

```java
@Override
public Void visitFunctionStmt(Stmt.Function stmt) {
  LoxFunction function = new LoxFunction(stmt, environment);   // 선언된 자리의 환경을 closure로
  environment.define(stmt.name.lexeme, function);
  return null;
}
```

---

## 10.6 반환문 (Return Statements)

```
statement  → exprStmt | forStmt | ifStmt | printStmt
           | returnStmt | whileStmt | block ;
returnStmt → "return" expression? ";" ;
```

```java
private Stmt returnStatement() {
  Token keyword = previous();
  Expr value = null;
  if (!check(SEMICOLON)) {        // return; 은 nil 반환
    value = expression();
  }
  consume(SEMICOLON, "Expect ';' after return value.");
  return new Stmt.Return(keyword, value);
}
```

문제는 실행이다. `return`은 함수 본문 한복판, 여러 겹의 블록·루프·if 안에서 튀어나올 수 있다. 트리 워킹의 자바 호출 스택을 한 번에 풀고 `call`까지 돌아가야 한다 — 9장 `break`와 똑같이 **예외로 스택을 되감는다**.

```java
class Return extends RuntimeException {
  final Object value;
  Return(Object value) {
    super(null, null, false, false);   // 스택 트레이스 비활성화 — 제어 흐름용이라 비용 절감
    this.value = value;
  }
}

@Override
public Void visitReturnStmt(Stmt.Return stmt) {
  Object value = null;
  if (stmt.value != null) value = evaluate(stmt.value);
  throw new Return(value);
}
```

`LoxFunction.call`에서 이 예외를 잡아 반환값으로 바꾼다.

```java
try {
  interpreter.executeBlock(declaration.body, environment);
} catch (Return returnValue) {
  return returnValue.value;        // return 문이 던진 값을 호출자에게
}
return null;                       // 예외가 안 났으면 함수 끝까지 간 것 → nil
```

---

## 10.7 지역 함수와 클로저 (Closures)

함수가 1급 값이고 다른 함수 안에서 선언될 수 있으니, **자기를 둘러싼 환경의 변수를 기억**해야 한다.

```
fun makeCounter() {
  var i = 0;
  fun count() {
    i = i + 1;
    print i;
  }
  return count;
}

var counter = makeCounter();
counter();   // 1
counter();   // 2
```

`makeCounter`는 이미 반환됐는데, `count`는 여전히 `i`를 읽고 쓴다. 이게 가능한 이유가 `LoxFunction`이 생성 시점의 `environment`를 **`closure`로 붙잡아 두기** 때문이다.

```java
LoxFunction function = new LoxFunction(stmt, environment);
//                                           └ 선언된 순간의 환경을 포획
```

`count`를 호출할 때 새 환경의 부모(enclosing)가 바로 이 `closure`다. 그래서 `makeCounter`의 지역 환경(그 안의 `i`)이 함수가 살아 있는 한 같이 살아남는다. 클로저가 환경 체인을 붙잡아 GC되지 않게 하는 것이다.

이 메커니즘은 멋지지만 **미묘한 버그**를 품고 있다 — 클로저가 "선언 시점"이 아니라 "호출 시점"의 변수를 보게 되는 경우다. 그 문제와 해결(정적 해소, static resolution)이 11장의 주제다.

---

**이제 Lox에는 함수, 재귀, 클로저가 있다.** 단 한 가지, 클로저의 변수 바인딩이 항상 옳지는 않다. 다음 장에서 **Resolver**를 한 패스 더 끼워 그 구멍을 메운다.
