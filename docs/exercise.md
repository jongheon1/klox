# 연습문제 풀이 — 6 ~ 9장

*Crafting Interpreters*의 6장(Parsing Expressions), 7장(Evaluating Expressions), 8장(Statements and State), 9장(Control Flow) 끝에 있는 챌린지 풀이.
모든 코드는 책의 jlox(Java) 기준이며, 각 장 진도까지의 코드 상태를 전제로 한다.

---

## 문제 목록

### 6장 · Parsing Expressions

1. **콤마 연산자** — C의 콤마 연산자를 추가하라. 하나의 표현식 자리에 콤마로 구분된 여러 표현식을 넣을 수 있다(함수 호출 인자 목록 안은 제외). 런타임에 왼쪽 피연산자를 평가해 버리고, 오른쪽 피연산자를 평가해 반환한다. C와 같은 우선순위·결합성으로 구현하라.

2. **조건(삼항) 연산자 `?:`** — C 스타일의 조건 연산자 `?:`를 추가하라. `?`와 `:` 사이에는 어떤 우선순위가 허용되는가? 연산자 전체는 좌결합인가 우결합인가?

3. **왼쪽 피연산자 누락 에러 처리** — 각 이항 연산자가 왼쪽 피연산자 없이 등장하는 경우를 처리하는 에러 프로덕션을 추가하라. 표현식 맨 앞의 이항 연산자를 감지해 에러로 보고하되, 오른쪽 피연산자는 적절한 우선순위로 파싱한 뒤 버려라.

### 7장 · Evaluating Expressions

1. **숫자 외 타입의 비교** — 숫자가 아닌 타입의 비교를 허용하겠는가? 허용한다면 어떤 타입 쌍을, 어떤 순서로 정의하겠는가? 선택을 정당화하고 다른 언어와 비교하라.

2. **문자열 `+` 변환** — 한쪽 피연산자가 문자열이면 다른 쪽을 문자열로 변환해 연결하도록 `+`를 확장하라. 예: `"scone" + 4` → `scone4`.

3. **0으로 나누기** — 지금 0으로 나누면 어떻게 되는가? 어떻게 되어야 하는가? 다른 언어들은 어떻게 처리하는가? `visitBinaryExpr()`에서 런타임 에러로 감지·보고하도록 고쳐라.

### 8장 · Statements and State

1. **REPL에서 표현식도 평가** — REPL이 문장과 표현식을 둘 다 받게 하라. 문장을 입력하면 실행하고, 표현식을 입력하면 평가해 그 결과값을 출력한다.

2. **초기화 안 된 변수 접근을 에러로** — 변수를 암묵적으로 `nil`로 초기화하는 대신, 초기화·할당된 적 없는 변수를 읽으면 런타임 에러가 나게 하라.

3. **가림 초기화의 동작** — 아래 프로그램은 무엇을 하는가? 무엇을 기대했는가? 왜 그렇게 동작하는가?
   ```
   var a = 1;
   {
     var a = a + 2;
     print a;
   }
   ```

### 9장 · Control Flow

1. **분기 없는 분기** — 1급 함수와 동적 디스패치만으로 조건부 실행을 구현할 수 있음을 보여라. 이 방식을 제어 흐름에 쓰는 언어를 하나 대라.

2. **함수만으로 반복** — 같은 도구로 반복도 구현할 수 있는데, 인터프리터의 어떤 최적화가 전제되어야 하는가? 왜 필요한가? 이 방식으로 반복하는 언어를 하나 대라.

3. **`break` 문 추가** — 대부분의 C 계열 언어에 있는 `break` 문을 루프 안에서 쓸 수 있게 추가하라.

---






















# 6장 챌린지

6장 끝의 표현식 문법은 다음과 같다(풀이의 출발점):

```
expression → equality ;
equality   → comparison ( ( "!=" | "==" ) comparison )* ;
comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term       → factor ( ( "-" | "+" ) factor )* ;
factor     → unary ( ( "/" | "*" ) unary )* ;
unary      → ( "!" | "-" ) unary | primary ;
primary    → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
```

## 6-1. 콤마 연산자

> C에서 블록이 하나의 문장 자리에 여러 문장을 넣게 해주듯, 콤마 연산자는 하나의 표현식 자리에 콤마로 구분된 여러 표현식을 넣게 해준다(함수 호출 인자 목록 안은 제외). 런타임에 왼쪽 피연산자를 평가해 버리고, 오른쪽 피연산자를 평가해 반환한다. C와 같은 우선순위·결합성으로 콤마 표현식을 지원하라.

### 콤마 연산자 예시

```
1, 2 == 3
1 == 2, 3 * 4     →  (1 + 2)를 평가해 버리고, (3 * 4) = 12 를 반환
"a", "b", "c"    →  앞의 둘을 버리고 "c" 를 반환
1, 2, 3          →  좌결합 ((1, 2), 3) → 최종 3
(1 + 2, 3) * 4   →  괄호 안 콤마는 3으로 평가 → 3 * 4 = 12
```

가장 바깥 자리에서 콤마는 "여러 표현식을 줄지어 평가하고 마지막 것만 값으로 쓴다".

### 풀이

콤마는 **가장 낮은 우선순위**이고 **좌결합**이다(C와 동일). 따라서 문법 최상위에 새 규칙을 끼우고 `expression`이 그것을 가리키게 한다. 6장 전체 문법에 한 줄(`comma`)이 추가되고 `expression`의 목적지가 바뀐 모습은 다음과 같다:

```
expression → comma ;
comma      → equality ( "," equality )* ;          ← 새로 추가
equality   → comparison ( ( "!=" | "==" ) comparison )* ;
comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term       → factor ( ( "-" | "+" ) factor )* ;
factor     → unary ( ( "/" | "*" ) unary )* ;
unary      → ( "!" | "-" ) unary | primary ;
primary    → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
```

`( "," equality )*` 반복이 좌결합 시퀀스를 만든다. 구조가 `왼쪽 , 오른쪽`이므로 새 AST 노드 없이 기존 `Expr.Binary`를 재사용할 수 있다.

```java
private Expr expression() {
  return comma();
}

private Expr comma() {
  Expr expr = equality();

  while (match(COMMA)) {
    Token operator = previous();
    Expr right = equality();
    expr = new Expr.Binary(expr, operator, right);
  }

  return expr;
}
```

런타임(7장에서 채워지는 부분)은 이항 평가 함수에 분기 하나만 추가한다. `visitBinaryExpr`는 이미 양쪽을 먼저 평가하므로, 콤마는 왼쪽 값을 그냥 쓰지 않고 오른쪽을 반환하면 된다(왼쪽의 부수 효과는 이미 일어났다):

```java
public Object visitBinaryExpr(Expr.Binary expr) {
  Object left  = evaluate(expr.left);    // 왼쪽 먼저 평가 (부수 효과 발생)
  Object right = evaluate(expr.right);   // 오른쪽 평가

  switch (expr.operator.type) {
    // ... 기존 산술·비교 연산자 분기들 ...
    case PLUS:  return (double)left + (double)right;
    case STAR:  return (double)left * (double)right;

    case COMMA:          // 새로 추가
      return right;      // left는 버리고 right 반환
  }
  return null;
}
```

---

## 6-2. 조건(삼항) 연산자 `?:`

> C 스타일의 조건 연산자 `?:`를 추가하라. `?`와 `:` 사이에는 어떤 우선순위가 허용되는가? 연산자 전체는 좌결합인가 우결합인가?

### 삼항 연산자 예시

```
true ? 1, 2 == 3 : "no"      →  "yes"
1 < 2 ? "작다" : "크다"     →  "작다"   (비교가 ? 보다 먼저 묶인다)
1 ? 2 : 3 ? 4 : 5        →  우결합 1 ? 2 : (3 ? 4 : 5) → 2
true ? 1 + 1 : 9         →  가운데엔 어떤 표현식이든 올 수 있다 → 2

1 ? 2 : 3 , 2
1 ? 2 , 4 : 3
1 ? 2 ? 3 : 4 : 5
```




1, 2 ? 3, 5 : 4
### 두 질문에 대한 답

- **`?`와 `:` 사이**: 어떤 표현식이든 올 수 있다. 마치 양쪽이 괄호로 묶인 것처럼 취급하므로, 가장 낮은 우선순위(`expression` 전체)까지 허용된다.
- **결합성**: **우결합**이다. `a ? b : c ? d : e`는 `a ? b : (c ? d : e)`로 묶인다.

### 풀이

콤마와 equality 사이에 `conditional` 단계를 끼운다. 6-1에서 만든 문법에 한 단계가 더 들어간 전체 모습은 다음과 같다:

```
expression  → comma ;
comma       → conditional ( "," conditional )* ;          ← comma의 목적지가 conditional로 바뀜
conditional → equality ( "?" expression ":" conditional )? ;   ← 새로 추가
equality    → comparison ( ( "!=" | "==" ) comparison )* ;
comparison  → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term        → factor ( ( "-" | "+" ) factor )* ;
factor      → unary ( ( "/" | "*" ) unary )* ;
unary       → ( "!" | "-" ) unary | primary ;
primary     → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
```

- 가운데 피연산자는 `expression`(전체 우선순위) — 첫 번째 질문의 답.
- else 가지는 `conditional`을 **재귀** 호출 — 우결합을 만든다(두 번째 질문의 답).

세 개의 피연산자를 담을 새 AST 노드가 필요하다. `tool/GenerateAst`에 다음을 추가한다.

```java
"Conditional : Expr condition, Expr thenBranch, Expr elseBranch",
```

파싱 코드:

```java
private Expr conditional() {
  Expr expr = equality();

  if (match(QUESTION)) {
    Expr thenBranch = expression();
    consume(COLON,
        "Expect ':' after then branch of conditional expression.");
    Expr elseBranch = conditional();          // 우결합 → 재귀
    expr = new Expr.Conditional(expr, thenBranch, elseBranch);
  }

  return expr;
}
```

스캐너에는 `?` → `QUESTION`, `:` → `COLON` 토큰을 추가해야 한다. 런타임 평가는 조건을 truthy로 판정해 한쪽 가지만 평가한다.

```java
@Override
public Object visitConditionalExpr(Expr.Conditional expr) {
  if (isTruthy(evaluate(expr.condition))) {
    return evaluate(expr.thenBranch);
  }
  return evaluate(expr.elseBranch);
}
```

---

## 6-3. 이항 연산자의 왼쪽 피연산자 누락 에러 처리

> 각 이항 연산자가 왼쪽 피연산자 없이 등장하는 경우를 다루는 **에러 프로덕션(error production)**을 추가하라. 즉, 표현식 맨 앞에 이항 연산자가 오는 것을 감지해 에러로 보고하되, 오른쪽 피연산자는 적절한 우선순위로 파싱한 뒤 버려라.

### 잡아내려는 입력 예시

```
<= 5      →  "Missing left-hand operand."   (<= 앞에 피연산자가 없음)
* 3       →  "Missing left-hand operand."   ("/" "*" 도 동일)
+ 4       →  "Missing left-hand operand."   ("+" 는 단항이 없으므로 항상 에러)
== 2      →  "Missing left-hand operand."
- 4       →  에러 아님!  "-" 는 단항 부정이 되어 -4 로 정상 파싱
```

마지막 줄이 핵심이다. `-`만은 단항 연산자로도 유효하므로 에러 프로덕션에서 제외한다.

### 풀이

`primary()`는 어떤 잎도 매칭하지 못하면 `Expect expression.` 에러를 던진다. 그 직전에, 잘못 등장한 이항 연산자를 잡아내는 프로덕션들을 추가한다. 각 프로덕션은 에러를 보고하고, 오른쪽 피연산자를 **해당 연산자보다 한 단계 위** 규칙으로 파싱해 버린 뒤 `null`을 반환한다.

```java
private Expr primary() {
  if (match(FALSE)) return new Expr.Literal(false);
  if (match(TRUE)) return new Expr.Literal(true);
  if (match(NIL)) return new Expr.Literal(null);

  if (match(NUMBER, STRING)) {
    return new Expr.Literal(previous().literal);
  }

  if (match(LEFT_PAREN)) {
    Expr expr = expression();
    consume(RIGHT_PAREN, "Expect ')' after expression.");
    return new Expr.Grouping(expr);
  }

  // 에러 프로덕션: 왼쪽 피연산자가 없는 이항 연산자
  if (match(BANG_EQUAL, EQUAL_EQUAL)) {
    error(previous(), "Missing left-hand operand.");
    equality();
    return null;
  }
  if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
    error(previous(), "Missing left-hand operand.");
    comparison();
    return null;
  }
  if (match(PLUS)) {
    error(previous(), "Missing left-hand operand.");
    term();
    return null;
  }
  if (match(SLASH, STAR)) {
    error(previous(), "Missing left-hand operand.");
    factor();
    return null;
  }

  throw error(peek(), "Expect expression.");
}
```

포인트:

- 여기서 `error(...)`는 보고만 시키고 던지지 않는다(`throw`를 빼고 호출). 오른쪽 피연산자를 마저 파싱해 버려야 하기 때문이다.
- 오른쪽 피연산자는 해당 연산자보다 **한 단계 높은** 규칙으로 파싱한다(`==`는 `equality`가 아니라 `comparison`을 부르는 게 더 정확하지만, 같은 단계를 불러도 동작한다 — 책 답안은 같은 단계를 호출한다).
- `MINUS`는 제외한다. `-`는 단항 부정으로도 쓰이므로(`-1`) 맨 앞에 와도 에러가 아니다. 반면 Lox에는 단항 `+`가 없으므로 맨 앞의 `+`는 항상 에러다.

---

# 7장 챌린지

## 7-1. 숫자 외 타입의 비교 연산

> 숫자가 아닌 타입에 비교 연산을 허용하면 유용할 수 있다. 문자열에는 합리적인 해석이 있고, `3 < "pancake"` 같은 혼합 타입 비교조차 이종 컬렉션 정렬 등에 쓸 수 있다. 아니면 그냥 버그와 혼란의 원천일 수도 있다. Lox를 확장해 다른 타입 비교를 지원하겠는가? 한다면 어떤 타입 쌍을 허용하고 순서를 어떻게 정의하겠는가? 선택을 정당화하고 다른 언어와 비교하라.

### 풀이 (논술형)

**제안: 문자열끼리(string vs string)의 비교만 허용하고, 혼합 타입은 런타임 에러로 막는다.**

근거:

- **문자열끼리**: 사전순(lexicographic) 정렬은 자연스럽고 널리 통용된다. Java의 `String.compareTo`로 그대로 구현할 수 있어 동작이 예측 가능하다.
- **혼합 타입(`3 < "pancake"`)**: 자연스러운 순서가 없다. 허용하면 의도치 않은 비교를 조용히 통과시켜 버그를 숨긴다. 막는 편이 안전하다.

다른 언어와 비교:

- **Python 3**: 혼합 타입 비교를 `TypeError`로 막는다. (Python 2는 임의 순서로 허용했는데, 거의 보편적으로 실수로 평가된다.)
- **JavaScript**: 피연산자를 강제 변환해 비교한다. `3 < "pancake"`는 `"pancake"`가 `NaN`이 되어 모든 비교가 `false` — 직관에 어긋나고 디버깅이 어렵다.
- 결론적으로 "엄격하게 막기"가 동적 언어에서도 버그를 줄이는 선택이다.

문자열 비교를 추가한다면 각 비교 분기를 다음처럼 일반화한다.

```java
case LESS:
  if (left instanceof Double && right instanceof Double) {
    return (double)left < (double)right;
  }
  if (left instanceof String && right instanceof String) {
    return ((String)left).compareTo((String)right) < 0;
  }
  throw new RuntimeError(expr.operator,
      "Operands must be two numbers or two strings.");
```

`>`, `>=`, `<=`도 같은 방식으로 `compareTo`의 부호만 바꿔 처리한다.

---

## 7-2. 한쪽이 문자열이면 `+`로 연결

> 많은 언어가 `+`의 한쪽이라도 문자열이면 다른 쪽을 문자열로 변환해 연결하도록 정의한다. 예를 들어 `"scone" + 4`는 `scone4`가 된다. `visitBinaryExpr()`을 확장해 이를 지원하라.

### 풀이

`PLUS` 분기에 "한쪽이라도 문자열이면 양쪽을 문자열로 변환해 연결"하는 경우를 추가한다. 숫자를 사람이 읽는 형태로 바꾸기 위해 `stringify`를 쓴다(그래야 `4`가 `4.0`이 아니라 `4`로 연결된다).

```java
case PLUS:
  if (left instanceof Double && right instanceof Double) {
    return (double)left + (double)right;
  }
  if (left instanceof String || right instanceof String) {
    return stringify(left) + stringify(right);
  }
  throw new RuntimeError(expr.operator,
      "Operands must be two numbers or two strings.");
```

이제 `"scone" + 4`는 `"scone4"`, `3 + " musketeers"`는 `"3 musketeers"`가 된다.

고려사항: `instanceof String ||`로 처리하면 `true + "!"`(→ `"true!"`)나 `nil + "?"`(→ `"nil?"`)처럼 Boolean·nil까지 연결된다. 이것이 편의인지 버그의 원천인지는 설계 판단이다. 숫자↔문자열만 허용하고 싶다면 조건을 더 좁히면 된다.

---

## 7-3. 0으로 나누기

> 지금 숫자를 0으로 나누면 어떻게 되는가? 어떻게 되어야 한다고 보는가? 정당화하라. 다른 언어들은 0으로 나누기를 어떻게 처리하며 왜 그렇게 하는가? `visitBinaryExpr()`을 고쳐 이 경우를 런타임 에러로 감지·보고하라.

### 현재 동작

피연산자가 `Double`이므로 `(double)left / (double)right`는 IEEE 754를 따른다. `1 / 0`은 `Infinity`, `0 / 0`은 `NaN`을 내며 **에러가 나지 않는다**. 사용자에게는 의미 불명의 결과가 조용히 흘러나온다.

### 다른 언어들

- **C**: 정수 0으로 나누기는 정의되지 않은 동작(undefined behavior).
- **Java**: 정수 나누기는 `ArithmeticException`을 던지지만, 부동소수점 나누기는 `Infinity`/`NaN`을 낸다.
- **Python**: `ZeroDivisionError`를 던진다 — 조용한 전파보다 명시적 실패를 택한다.

### 선택과 구현

Lox는 숫자가 하나뿐이라(부동소수점), 명시적인 **런타임 에러**로 보고하는 편이 가장 덜 놀랍다. `SLASH` 분기에서 0 검사를 추가한다.

```java
case SLASH:
  checkNumberOperands(expr.operator, left, right);
  if ((double)right == 0) {
    throw new RuntimeError(expr.operator, "Division by zero.");
  }
  return (double)left / (double)right;
```

`checkNumberOperands`가 먼저 양쪽이 숫자임을 보장하므로, 그 뒤의 캐스트와 0 비교는 안전하다.

---

# 8장 챌린지

## 8-1. REPL에서 문장과 표현식을 모두 받기

> 8장에서 인터프리터를 문장 기반으로 바꾸면서, REPL에 표현식 하나를 입력하면 그 값을 출력해 주던 기능이 사라졌다. 문장과 표현식을 둘 다 받도록 REPL을 고쳐라. 문장을 입력하면 실행하고, 표현식을 입력하면 평가해 결과값을 보여줘라.

### 입력 예시

```
> 1 + 2          ;  ← 표현식 (세미콜론 없음)  →  3 출력
> var a = 10;       ← 선언문                  →  출력 없음
> a * a          ;  ← 표현식                  →  100 출력
> print a;          ← print 문               →  10 출력
```

표현식을 입력하면 `print`를 치지 않아도 값이 보이는 게 목표다.

### 풀이

핵심은 **REPL 한 줄을 먼저 문장으로 파싱해 보고, 실패하면 표현식으로 다시 파싱**하는 것이다. 이를 위해 파서에 "표현식 하나만 파싱하는" 진입점을 하나 노출한다.

```java
// Parser
Object parseRepl() {
  allowExpression = true;            // 표현식 단독 허용 모드
  List<Stmt> statements = new ArrayList<>();
  while (!isAtEnd()) {
    statements.add(declaration());

    if (foundExpression) {           // 세미콜론 없는 표현식이었다면
      Stmt last = statements.get(statements.size() - 1);
      return ((Stmt.Expression) last).expression;
    }
    allowExpression = false;         // 두 번째 줄부터는 일반 문장만
  }
  return statements;
}
```

`expressionStatement()`에서, 세미콜론 없이 끝(파일 끝)에 닿았고 표현식 모드라면 문장 대신 "표현식이었다"고 표시한다.

```java
private Stmt expressionStatement() {
  Expr expr = expression();

  if (allowExpression && isAtEnd()) {
    foundExpression = true;          // ; 없이 끝 → 표현식으로 간주
  } else {
    consume(SEMICOLON, "Expect ';' after expression.");
  }

  return new Stmt.Expression(expr);
}
```

REPL 루프에서는 반환 타입으로 갈래를 탄다.

```java
private static void run(String source) {
  Object syntax = new Parser(scan(source)).parseRepl();
  if (hadError) return;

  if (syntax instanceof List) {
    interpreter.interpret((List<Stmt>) syntax);   // 문장들: 실행
  } else if (syntax instanceof Expr) {
    String result = interpreter.interpret((Expr) syntax);  // 표현식: 평가해 출력
    if (result != null) System.out.println("= " + result);
  }
}
```

요점: 문법을 바꾸지 않고 **파서의 동작 모드**만 토글한다. 파일을 실행할 때는 `allowExpression`이 꺼져 있으므로 세미콜론을 그대로 강제한다 — REPL에서만 느슨해진다.

---

## 8-2. 초기화되지 않은 변수 접근을 런타임 에러로

> Lox는 변수를 선언만 하면 암묵적으로 `nil`로 초기화한다. 이걸 더 엄격하게 바꿔, 초기화도 할당도 된 적 없는 변수를 읽으면 런타임 에러가 나게 하라.

### 동작 비교

```
var a;
print a;        // 현재: nil 출력      →  목표: 런타임 에러 "Unassigned variable 'a'."

var b;
b = 3;
print b;        // 할당했으므로 OK → 3

var c = 0;
print c;        // 초기화했으므로 OK → 0
```

### 풀이

"선언됐지만 값이 없음"과 "할당돼서 값이 있음"을 구분해야 한다. 가장 깔끔한 방법은 **미할당을 나타내는 센티넬(sentinel) 객체**를 두는 것이다(`null`은 Lox의 `nil`로 이미 쓰이므로 못 쓴다).

```java
class Environment {
  private static final Object UNINITIALIZED = new Object();
  private final Map<String, Object> values = new HashMap<>();

  // 초기화식 없는 var a; 는 UNINITIALIZED 로 정의
  void define(String name, Object value) {
    values.put(name, value);
  }

  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      Object value = values.get(name.lexeme);
      if (value == UNINITIALIZED) {
        throw new RuntimeError(name,
            "Unassigned variable '" + name.lexeme + "'.");
      }
      return value;
    }
    if (enclosing != null) return enclosing.get(name);
    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }
  // assign 은 그대로 — 값을 넣으면 더 이상 UNINITIALIZED 가 아니게 된다
}
```

인터프리터의 `visitVarStmt`는 초기화식이 없을 때 `nil`이 아니라 센티넬을 넣는다.

```java
@Override
public Void visitVarStmt(Stmt.Var stmt) {
  Object value = Environment.UNINITIALIZED;
  if (stmt.initializer != null) {
    value = evaluate(stmt.initializer);
  }
  environment.define(stmt.name.lexeme, value);
  return null;
}
```

이제 선언만 된 변수를 *읽으면* 에러, 한 번이라도 *할당하면* 정상이다. "정의되지 않음(Undefined)"과 "할당 안 됨(Unassigned)"을 다른 메시지로 구분한 점에 주목하라.

---

## 8-3. 가림 초기화 `var a = a + 2;`의 동작

> 다음 프로그램은 무엇을 하는가? 무엇을 기대했는가? 왜 그렇게 동작하는가?
>
> ```
> var a = 1;
> {
>   var a = a + 2;
>   print a;
> }
> ```

### 답: `3`을 출력한다

블록 안 `var a = a + 2;`는 두 단계로 처리된다.

1. **초기화식 `a + 2`를 먼저 평가**한다. 이 시점에는 안쪽 `a`가 아직 환경에 등록되지 않았으므로, 이름 `a`는 **바깥 스코프의 `a`(=1)**를 가리킨다. → `1 + 2 = 3`.
2. 그 결과 `3`을 **새 안쪽 변수 `a`**로 정의한다.

그래서 `print a`는 안쪽 `a`인 `3`을 출력한다.

```
[전역]  a = 1
블록 진입:
  var a = a + 2;
    1) RHS 평가:  a + 2  →  바깥 a(1) + 2  =  3
    2) 안쪽 a 정의:  a = 3
  print a  →  안쪽 a  →  3
```

### 무엇이 헷갈리는가

직관적으로 두 가지를 기대할 수 있다.

- **`3`** — RHS의 `a`가 바깥 변수를 본다(현재 jlox 8장 동작).
- **에러 또는 `nil`** — RHS의 `a`가 *지금 선언 중인* 안쪽 변수를 본다면, 그건 아직 값이 없으니 자기 자신을 참조하는 꼴이다.

문제는 이 동작이 **구현 디테일(언제 변수를 환경에 등록하는가)에 따라 달라진다**는 점이다. 그래서 책은 이 모호함을 11장(Resolving and Binding)에서 정면으로 다룬다. 거기서는 "초기화식 안에서 자기 자신을 참조하는 것"을 **정적(컴파일) 에러**로 막아, 어느 쪽이든 헷갈릴 여지를 아예 없앤다.

> 이 레포의 `Resolver`가 바로 그 장치다. `visitVariableExpr`에서 `scopes.peek()[name] == false`(선언됐지만 정의 전)이면 *"Can't read local variable in its own initializer."* 에러를 낸다.

---

# 9장 챌린지

## 9-1. 분기 없이 분기하기

> 몇 장 뒤에 Lox가 1급 함수와 동적 디스패치를 지원하게 되면, 분기문을 언어에 내장하지 않아도 된다. 그 도구들만으로 조건부 실행을 구현할 수 있음을 보여라. 이 기법을 제어 흐름에 쓰는 언어를 하나 대라.

### 아이디어

조건문을 **데이터에 대한 디스패치**로 바꾼다. 핵심은 "두 가지(참/거짓) 동작을 각각 함수로 싸 두고, 불리언 값이 그중 하나를 고르게" 하는 것이다.

Smalltalk의 방식이 정확히 이렇다. `Boolean`이 추상 클래스고, `True`와 `False`가 그 하위 클래스다. `ifTrue:ifFalse:`라는 메시지를 보내면 동적 디스패치가 알아서 갈래를 탄다.

```
True  >> ifTrue: t ifFalse: f   →  t value     (참이면 첫째 블록 실행)
False >> ifTrue: t ifFalse: f   →  f value     (거짓이면 둘째 블록 실행)
```

즉 `if`라는 **문법**이 없어도, `true`와 `false`가 서로 다른 객체이고 같은 메시지에 다르게 반응한다는 사실만으로 분기가 된다. 의사 Lox로 흉내 내면:

```
fun ifTrue(condition, thenFn, elseFn) {
  return condition(thenFn, elseFn);   // condition 자체가 둘 중 하나를 고름
}
```

- **언어 예시**: **Smalltalk**(메시지 `ifTrue:ifFalse:`). Lambda Calculus의 처치 불리언(Church boolean)도 같은 원리다(`true = λa.λb.a`, `false = λa.λb.b`).

---

## 9-2. 함수만으로 반복하기

> 같은 도구(1급 함수)로 반복도 만들 수 있는데, 그러려면 인터프리터가 중요한 최적화 하나를 지원해야 한다. 그게 무엇이고 왜 필요한가? 이 방식으로 반복하는 언어를 하나 대라.

### 답: 꼬리 호출 최적화 (Tail Call Optimization)

반복을 **재귀**로 표현한다. `while`을 자기 자신을 다시 부르는 함수로 바꾼다.

```
fun loop(i) {
  if (i < 10) {
    print i;
    loop(i + 1);     // 꼬리 위치에서 자기 자신 재귀
  }
}
loop(0);
```

문제는, 호출마다 스택 프레임이 쌓인다는 것이다. 10번이면 괜찮지만 백만 번 반복하면 **스택 오버플로**가 난다. 진짜 반복처럼 쓰려면 이 재귀가 스택을 늘리지 않아야 한다.

**꼬리 호출 최적화**가 그 해법이다. 함수의 **마지막 동작이 다른 호출**이면(꼬리 위치), 현재 프레임은 더 쓸 일이 없으니 새 프레임을 쌓는 대신 **현재 프레임을 재사용**한다. 그러면 재귀 깊이가 아무리 깊어도 스택이 일정하게 유지돼, 반복과 똑같이 동작한다.

- **왜 필요한가**: 이것이 없으면 재귀 기반 반복이 메모리를 선형으로 먹고 결국 터진다. 최적화가 있어야 비로소 재귀가 "공짜 반복"이 된다.
- **언어 예시**: **Scheme**(언어 표준이 꼬리 호출 최적화를 *의무화*한다). 그래서 Scheme에는 내장 반복문이 굳이 필요 없다.

---

## 9-3. `break` 문 추가

> 대부분의 C 계열 언어는 `break`와 `continue`로 루프를 빠져나간다. `break` 문을 추가하라.

### 잡아내려는 것

```
for (var i = 0; i < 10; i = i + 1) {
  if (i == 3) break;     // 3에서 루프 탈출
  print i;
}
// 출력: 0 1 2

break;                   // 루프 밖의 break → 컴파일 에러로 막아야 함
```

### 풀이

세 부분이다: 문법·파싱, 루프 밖 사용 차단, 그리고 실행 시 탈출.

**1) 문법과 파싱.** `break`는 키워드와 `;`뿐인 문장이다.

```
statement → exprStmt | forStmt | ifStmt | printStmt
          | whileStmt | block | breakStmt ;
breakStmt → "break" ";" ;
```

`break`가 루프 *안*에 있는지를 파싱 중에 추적한다. 루프를 파싱하는 동안만 켜지는 카운터를 둔다.

```java
private int loopDepth = 0;

private Stmt statement() {
  // ...
  if (match(BREAK)) return breakStatement();
  // ...
}

private Stmt breakStatement() {
  if (loopDepth == 0) {
    error(previous(), "Must be inside a loop to use 'break'.");
  }
  consume(SEMICOLON, "Expect ';' after 'break'.");
  return new Stmt.Break();
}
```

`while`/`for`를 파싱할 때 본문 파싱 전후로 `loopDepth`를 올렸다 내린다(에러가 나도 복구되게 `try/finally`).

```java
private Stmt whileStatement() {
  consume(LEFT_PAREN, "Expect '(' after 'while'.");
  Expr condition = expression();
  consume(RIGHT_PAREN, "Expect ')' after condition.");

  try {
    loopDepth++;
    Stmt body = statement();
    return new Stmt.While(condition, body);
  } finally {
    loopDepth--;
  }
}
```

이렇게 하면 루프 밖의 `break`는 **파싱 단계에서** 걸러진다.

**2) 실행 시 탈출.** 인터프리터에서 `break`를 만나면 여러 단계의 트리 워킹 호출을 한 번에 빠져나와야 한다. 이건 자바 **예외**로 스택을 되감는 게 가장 자연스럽다(6장 동기화와 같은 수법).

```java
private static class BreakException extends RuntimeException {}

@Override
public Void visitBreakStmt(Stmt.Break stmt) {
  throw new BreakException();
}
```

루프 실행부에서 이 예외를 잡아 루프만 끝낸다.

```java
@Override
public Void visitWhileStmt(Stmt.While stmt) {
  try {
    while (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.body);
    }
  } catch (BreakException ex) {
    // break: 이 루프를 정상 종료
  }
  return null;
}
```

`for`는 9.5의 디슈가링 덕분에 결국 `Stmt.While`이 되므로, **`while`의 catch 하나로 `for`의 `break`까지 공짜로 처리**된다. 중첩 루프에서도 예외는 가장 가까운(가장 안쪽) `while`의 catch에 잡히므로, "가장 안쪽 루프만 탈출"이라는 의미가 자동으로 지켜진다.

> `continue`도 같은 틀이다. `ContinueException`을 던지고, 루프 본문 실행을 감싼 안쪽에서 잡아 *증감식으로 건너뛰면* 된다(`for`라면 증감식을 반드시 실행해야 하므로 본문만 감싸는 위치가 중요하다).
