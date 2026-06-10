# 연습문제 풀이 — 6장 & 7장

*Crafting Interpreters*의 6장(Parsing Expressions)과 7장(Evaluating Expressions) 끝에 있는 챌린지 풀이.
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
1 = 2, 3 * 4     →  (1 + 2)를 평가해 버리고, (3 * 4) = 12 를 반환
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
true ? "yes" : "no"      →  "yes"
1 < 2 ? "작다" : "크다"     →  "작다"   (비교가 ? 보다 먼저 묶인다)
1 ? 2 : 3 ? 4 : 5        →  우결합 1 ? 2 : (3 ? 4 : 5) → 2
true ? 1 + 1 : 9         →  가운데엔 어떤 표현식이든 올 수 있다 → 2
```

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
