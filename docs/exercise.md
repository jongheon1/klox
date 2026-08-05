# 연습문제 풀이 — 6 ~ 13장

*Crafting Interpreters*의 6장(Parsing Expressions), 7장(Evaluating Expressions), 8장(Statements and State), 9장(Control Flow), 10장(Functions), 11장(Resolving and Binding), 12장(Classes), 13장(Inheritance) 끝에 있는 챌린지 풀이.
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

### 10장 · Functions

1. **인자 개수 검사 비용** — 우리 인터프리터는 호출마다 인자 개수가 매개변수 개수와 맞는지 런타임에 검사한다. 이 비용이 매 호출에 든다. Smalltalk 구현에는 이 문제가 없다. 왜인가?

2. **익명 함수(람다)** — 함수 선언은 "함수를 만든다 + 이름에 묶는다" 두 일을 한다. 함수형 코드에서는 이름 없이 곧장 넘기거나 반환하고 싶을 때가 많다. 익명 함수 문법을 추가하라. 표현식 문장 자리에 익명 함수가 오는 까다로운 경우(`fun () {};`)는 어떻게 처리하는가?

3. **매개변수와 지역 변수의 스코프** — `fun scope(a) { var a = "local"; }`는 유효한가? 즉 매개변수와 지역 변수는 같은 스코프인가, 바깥 스코프인가? Lox는 어떻게 하는가? 다른 언어는? 어떻게 해야 한다고 보는가?

### 11장 · Resolving and Binding

1. **함수 이름의 즉시 정의** — 다른 변수는 초기화가 끝나기 전엔 못 쓰게 하면서, 함수 이름은 왜 즉시(eager) 정의해도 안전한가?

2. **초기화식의 자기 참조** — `var a = "outer"; { var a = a; }` 같은 코드를 다른 언어들은 어떻게 다루는가(런타임 에러? 컴파일 에러? 허용?)? 전역과 지역을 다르게 취급하는가? 그 선택에 동의하는가?

3. **사용되지 않은 지역 변수 경고** — 선언만 되고 한 번도 쓰이지 않는 지역 변수를 Resolver가 에러로 보고하도록 확장하라.

4. **인덱스 기반 환경** — Resolver가 변수의 스코프 거리는 계산하지만, 그 안에서는 여전히 이름으로(맵) 찾는다. 각 지역 변수에 슬롯 인덱스를 부여하고, 인터프리터가 배열 인덱스로 빠르게 접근하게 만들어 성능을 측정하라.

### 12장 · Classes

1. **정적 메서드** — 인스턴스가 아니라 클래스 객체에서 바로 부르는 정적(static) 메서드를 추가하라. 메서드 앞에 `class` 키워드를 붙여 표시한다. (힌트: 메타클래스)

2. **게터(getter)** — 매개변수 목록 없이 선언하고, 그 이름의 프로퍼티에 *접근*하는 순간 본문이 실행되는 게터 메서드를 추가하라.

3. **캡슐화** — Python·JS는 객체 필드를 밖에서 자유롭게 접근하게 하고, Ruby·Smalltalk는 상태를 캡슐화한다. Lox는 어느 쪽이며, 만약 바꾼다면 필드와 메서드 이름 충돌을 어떻게 다루겠는가? 트레이드오프와 견해를 논하라.

### 13장 · Inheritance

1. **다른 재사용 메커니즘** — Lox는 단일 상속뿐이다. 믹스인·트레이트·다중 상속 등 다른 재사용 방식 중 하나를 고른다면 무엇을, 왜 택하겠는가? (용감하다면 구현까지)

2. **BETA의 `inner`** — Lox(와 대부분 OOP)는 메서드 탐색을 하위에서 시작해 위로 간다(`super`로 위를 부른다). BETA는 반대로 위에서 시작해 아래로 내려가며, 상위 메서드가 `inner`로 하위를 불러들인다. Lox에 `inner`를 정의·구현하려면 무엇이 필요한가?

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

```
var a;
var b = null;
```

"선언됐지만 값이 없음"과 "할당돼서 값이 null 로 있음"을 구분해야 한다. 가장 깔끔한 방법은 **미할당을 나타내는 센티넬(sentinel) 객체**를 두는 것이다(`null`은 Lox의 `nil`로 이미 쓰이므로 못 쓴다).

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

> 이 레포(klox)에는 이 동작이 실제로 구현돼 있다. `Environment.UNINITIALIZED` 센티넬을 두고, `var a;`는 그걸로 정의한 뒤 `lookUpVariable`에서 읽을 때 센티넬이면 *"Unassigned variable 'a'."* 런타임 에러를 던진다. `var a; a = 1; print a;`처럼 한 번이라도 할당하면 센티넬이 덮여 정상 동작한다.

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

### 다른 언어들은 어떤 결과를 내나

같은 모양의 코드 — "안쪽에서 바깥과 같은 이름을 선언하면서, 그 초기화식 안에 같은 이름을 쓴다" — 가 언어마다 다른 결과를 낸다. 갈림길은 단 하나, **초기화식 안의 이름이 바깥 변수를 보느냐, 지금 선언 중인 안쪽 변수를 보느냐**다.

| 언어 | `var/let a = a + 2;`(안쪽)의 결과 | 이유 |
|------|------------------------------------|------|
| **Lox (jlox 8장)** | **`3`** | 초기화식이 평가될 때 안쪽 `a`가 아직 등록 전이라 바깥 `a`(1)를 본다 |
| **Rust** | **`3`** | `let a = a + 2;`는 섀도잉. RHS는 *이전* 바인딩(바깥 `a`)을 본다 — 의도된 설계 |
| **Scheme `let`** | **`3`** | `(let ((a (+ a 2))) ...)`의 초기화식은 *바깥* 스코프에서 평가된다 (`let*`였다면 안쪽을 봐서 달라진다) |
| **C** | **미정의 동작(쓰레기값)** | 선언자 이후부터 이름이 스코프에 들어와, `int a = a + 2;`의 `a`는 초기화 안 된 *자기 자신* |
| **Java** | **컴파일 에러** | 안쪽 `a`가 자기 초기화식에서 보이지만 "초기화되지 않았을 수 있음"으로 거부 |
| **JavaScript (`let`)** | **ReferenceError** | TDZ(Temporal Dead Zone): 안쪽 `a`는 선언 전 접근 금지라 RHS의 `a`가 던진다 |
| **Python** | 대응 안 됨(블록 스코프 없음) | 같은 스코프의 `a = a + 2`는 현재 `a`를 읽어 재대입. 단 함수 안에서 대입하면 전체가 지역이 돼 `UnboundLocalError` |

핵심은, 이 코드의 결과가 **"이름을 언제 스코프에 등록하느냐"라는 구현 결정**에 통째로 달려 있다는 것이다. 그래서 Lox도 11장(Resolving and Binding)에서 이 자기참조 초기화를 아예 **정적(컴파일) 에러**로 막아, jlox 8장의 `3`과 결별한다.

> 이 레포는 이미 11장의 `Resolver`를 갖췄다. `visitVariableExpr`에서 `scopes.peek()[name] == false`(선언됐지만 정의 전)이면 *"Can't read local variable in its own initializer."*를 보고한다. 즉 이 레포에서 위 프로그램은 `3`이 아니라 **컴파일 에러**다.

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

### Smalltalk 클래스 라이브러리의 실제 구현

`ifTrue:ifFalse:`는 컴파일러 키워드가 아니라 **`Boolean`의 메서드**다. `[ ... ]`는 블록(클로저, 1급 객체)이고, 거기에 `value` 메시지를 보내면 실행된다. 분기는 오직 "어느 서브클래스에 메시지가 도착했는가"로 결정된다.

```smalltalk
"True 클래스의 메서드"
ifTrue: trueBlock ifFalse: falseBlock
    ^trueBlock value      "참이면 then 블록만 평가해서 그 값을 반환"

"False 클래스의 메서드"
ifTrue: trueBlock ifFalse: falseBlock
    ^falseBlock value     "거짓이면 else 블록만 평가"
```

`true`는 `True`의 (유일한) 인스턴스, `false`는 `False`의 인스턴스다. 같은 `ifTrue:ifFalse:` 메시지라도 receiver의 클래스가 달라 **동적 디스패치**가 다른 메서드 본문으로 보낸다. `if` 문이 하던 일을 메서드 룩업이 대신하는 것이다.

### 실제로 쓰는 모습

조건 분기, 한쪽 가지, 값 반환, 단락 평가, 심지어 반복까지 전부 "객체에 블록을 넘기는 메시지"다.

```smalltalk
"기본 if/else — 그런데 문법이 아니라 메시지"
(n > 0)
    ifTrue:  [ Transcript show: 'positive' ]
    ifFalse: [ Transcript show: 'non-positive' ].
```

### 무엇이 동적 디스패치되는가 (인터페이스로 보기)

```java
interface Cond { Object select(Supplier<Object> thenFn, Supplier<Object> elseFn); }

Cond TRUE  = (t, e) -> t.get();   // 구현체 1: 첫째 선택
Cond FALSE = (t, e) -> e.get();   // 구현체 2: 둘째 선택

cond.select(thenFn, elseFn);      // cond 의 런타임 타입(TRUE/FALSE)이 분기를 결정
```

`cond`가 `TRUE`면 `t.get()`, `FALSE`면 `e.get()`이 불린다 — `if` 없이, 순전히 `cond`에 대한 가상 메서드 룩업만으로. Smalltalk의 `condition ifTrue: [..] ifFalse: [..]`에 그대로 대응한다: **receiver `condition` = 이 `cond`**, **두 블록 = `thenFn`/`elseFn`**, **`True`/`False` 클래스 = 두 구현체**.

- **언어 예시**: **Smalltalk**(메시지 `ifTrue:ifFalse:`). Lambda Calculus의 처치 불리언(Church boolean)도 같은 원리다(`true = λa.λb.a`, `false = λa.λb.b`) — 위 `makeTrue`/`makeFalse`가 바로 이 람다를 Lox로 옮긴 것이다.

---

## 9-2. 함수만으로 반복하기

> 같은 도구(1급 함수)로 반복도 만들 수 있는데, 그러려면 인터프리터가 중요한 최적화 하나를 지원해야 한다. 그게 무엇이고 왜 필요한가? 이 방식으로 반복하는 언어를 하나 대라.

### 답: 꼬리 호출 최적화 (Tail Call Optimization)

반복을 **재귀**로 표현한다. 그런데 "재귀로 짠다"고 다 같은 게 아니다. 같은 계산(팩토리얼)을 세 가지로 짜 보면 차이가 드러난다.

```kotlin
// ── ① while 루프 (명시적 반복) ──
fun factLoop(n: Int): Long {
    var acc = 1L
    var i = n
    while (i > 1) { acc *= i; i-- }
    return acc
}

// ── ② 일반 재귀 (재귀적 프로세스) ──
tailrec fun factRec(n: Int): Long =
    if (n <= 1) 1L
    else n * factRec(n - 1)          // 곱셈이 호출 "뒤"에 남는다

// ── ③ 꼬리 재귀 (반복적 프로세스) ──
tailrec fun factIter(n: Int, acc: Long = 1L): Long =
    if (n <= 1) acc
    else factIter(n - 1, acc * n)    // 호출이 함수의 "마지막 동작"
```

### 결정적 차이: "호출이 끝난 뒤에 할 일이 남았는가"

이게 꼬리 재귀의 전부다. **꼬리 위치(tail position)**의 정의:

> 호출의 반환값이 그대로 함수의 반환값이 되고, 그 사이에 추가 계산이 없으면 그 호출은 꼬리 위치에 있다.

②와 ③을 이 잣대로 보자.

```
② return n * factRec(n - 1)
            └─ 최상위 연산이 "*". factRec가 값을 돌려준 "뒤에"
               아직 n을 곱해야 한다 → 할 일 남음 → 꼬리 아님

③ return factIter(n - 1, acc * n)
          └─ 최상위 연산이 "호출" 그 자체. 반환값을 그대로 반환.
             acc * n은 인자라서 호출 "전에" 미리 계산됨 → 할 일 없음 → 꼬리
```

미묘하지만 핵심인 지점: ③의 `acc * n`은 인자 평가라 재귀 호출보다 먼저 끝난다. 그래서 호출하는 순간엔 이미 곱셈이 완료돼 있고, 호출이 돌아온 뒤 할 일이 0이다. ②는 반대로 `factRec(n-1)`이 돌아와야 비로소 곱셈을 할 수 있어서, 곱할 `n`을 기억하려고 프레임을 스택에 붙들고 있어야 한다.

### 스택에서 실제로 벌어지는 일 (프로세스의 모양)

② 일반 재귀 — "나중에 곱할 것"이 쌓인다(재귀적 프로세스):

```
factRec(5)
└ 5 * factRec(4)              ← "5 *" 보류
       └ 4 * factRec(3)       ← "4 *" 보류
              └ 3 * factRec(2)
                     └ 2 * factRec(1)
                            └ 1
   여기까지 펼쳐진 뒤(expand), 거꾸로 곱하며 줄어든다(contract)
   스택 프레임 5개 = O(n) 메모리
```

③ 꼬리 재귀 — 상태가 인자 `(n, acc)`에 다 들어있다(반복적 프로세스):

```
factIter(5,   1)
factIter(4,   5)     ← 이전 프레임은 할 일이 없으니 버려도 됨
factIter(3,  20)
factIter(2,  60)
factIter(1, 120)  → 120
   평평하다. 펼침/줄어듦 없음. 매 단계 (n, acc)만 갱신.
   프레임을 재사용하면 O(1) 메모리
```

③의 모양은 ①의 while 루프와 글자 그대로 똑같다 — `(n, acc)`가 ①의 변수 `(i, acc)`이고, 한 줄 내려가는 게 루프 한 바퀴다. 그래서 **꼬리 호출 최적화(TCO)란 ③을 기계적으로 ①로 바꾸는 변환**일 뿐이다.

### 런타임 현실: 그래서 진짜로 O(1)이 되나

여기서 챌린지의 "중요한 최적화"가 갈린다. ③이 O(1)인 건 **런타임이 TCO를 해줄 때만**이다.

- **Kotlin** — `tailrec` 키워드가 컴파일 시점에 ③을 ①(루프)로 바꿔준다. 그래서 `factIter(1_000_000)`도 멀쩡히 돈다. 보너스로 ②에 `tailrec`을 붙이면 컴파일러가 "꼬리 호출이 없다"고 경고하고 일반 재귀로 컴파일한다 — 즉 컴파일러가 "이게 진짜 꼬리 재귀냐"를 검증해준다.

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

> 이 레포(klox)에 위 설계를 그대로 구현했다. `break` 키워드 토큰(`BREAK`)과 `Stmt.Break` 노드를 추가하고, 파서는 `loopDepth`로 루프 밖 `break`를 파싱 단계에서 막는다. 인터프리터는 `BreakException`을 던지고 `visitWhileStmt`가 잡는다. **함수**가 있는 레포라서 한 가지를 더 처리했다 — 함수 본문을 파싱할 때 `loopDepth`를 0으로 리셋(끝나면 복원)한다. 그래야 루프 안에 정의된 함수 본문의 `break`(그 함수 입장에선 루프 밖)가 제대로 에러가 된다.

> `continue`도 같은 틀이다. `ContinueException`을 던지고, 루프 본문 실행을 감싼 안쪽에서 잡아 *증감식으로 건너뛰면* 된다(`for`라면 증감식을 반드시 실행해야 하므로 본문만 감싸는 위치가 중요하다).

---

# 10장 챌린지

## 10-1. 인자 개수 검사 비용 — Smalltalk엔 왜 없나

> 우리 인터프리터는 함수에 넘어온 인자 개수가 매개변수 개수와 맞는지 꼼꼼히 검사한다. 이 검사가 매 호출마다 런타임에 일어나므로 실제 성능 비용이 있다. Smalltalk 구현에는 이 문제가 없다. 왜인가?

### 우리 구현의 비용

`visitCallExpr`는 모든 호출에서 다음을 돈다.

```java
if (arguments.size() != function.arity()) {
  throw new RuntimeError(expr.paren, "Expected " +
      function.arity() + " arguments but got " + arguments.size() + ".");
}
```

호출이 일어날 때마다 정수 비교 한 번. 작지만 **런타임에, 매번** 치르는 비용이다.

### Smalltalk가 이 비용을 안 내는 이유

핵심은 **인자 개수가 메서드 이름(셀렉터, selector) 자체에 박혀 있다**는 데 있다. Smalltalk의 키워드 메시지는 콜론마다 인자 자리가 하나씩 대응한다.

```smalltalk
dict at: key put: value      "셀렉터 = at:put:  → 인자 정확히 2개"
collection do: aBlock         "셀렉터 = do:      → 인자 정확히 1개"
```

`at:put:`이라는 셀렉터는 **문법적으로 인자 두 개를 동반할 때만 만들어진다**. 인자를 하나만 적으면 그건 셀렉터 `at:put:`가 아니라 다른(혹은 불완전한) 메시지라 애초에 파싱 단계에서 갈린다. 즉 "셀렉터가 곧 arity"라서, **개수가 맞는지는 컴파일 시점에 이미 결정**된다.

런타임에 할 일은 "이 receiver의 클래스에 이 셀렉터에 해당하는 메서드가 있나"라는 메서드 룩업뿐이고, 셀렉터를 찾았다는 건 곧 개수가 맞았다는 뜻이다. 개수 불일치는 별도 검사가 아니라 **"메시지를 이해 못 함(`doesNotUnderstand:`)"**으로 자연히 흡수된다. Lox는 인자 목록과 함수 이름이 분리돼 있어(`f(1, 2)`의 `f`와 `2`가 따로) 런타임에 따로 맞춰 봐야 하는 것과 대조된다.

> 정리: Smalltalk는 arity를 **이름에 인코딩**해 정적으로 보장한다. 우리 Lox는 이름과 인자 개수가 독립이라 런타임 검사가 불가피하다. (C/Java 같은 정적 타입 언어도 같은 검사를 *컴파일 시점*으로 옮겨 런타임 비용을 없앤다.)

---

## 10-2. 익명 함수(람다) 추가

> 함수 선언은 "함수를 만든다"와 "이름에 묶는다" 두 가지를 한다. 함수형 스타일에선 이름 없이 만들어 곧장 넘기고 싶을 때가 많다. 익명 함수 문법을 추가해 다음이 되게 하라. 그리고 표현식 문장 자리에 익명 함수가 오는 까다로운 경우를 어떻게 다룰지 설명하라.

```
fun thrice(fn) {
  for (var i = 1; i <= 3; i = i + 1) {
    fn(i);
  }
}

fun foo(a) {
    print a;
};

thrice(foo);

thrice(fun (a) {       // 이름 없는 함수를 인자로
  print a;
});
// 1 2 3
```

### 풀이

익명 함수는 **이름이 없는 함수**, 즉 값을 만드는 **표현식**이다. 선언문(`Stmt.Function`)과 달리 식이 필요하다. 두 가지 접근이 있다.

**접근 A — `Stmt.Function`을 재사용하고 본문 파싱만 분리.** 책이 권하는 방식은, `function()`에서 "이름 읽기"와 "매개변수+본문 읽기"를 분리해 이름 없는 함수 본체 파서 `functionBody(kind)`를 만들고, 
그게 `Expr.Function`(또는 `Stmt.Function`을 감싼 식)을 반환하게 하는 것이다. 여기서는 새 식 노드를 두는 쪽으로 명확히 보인다.

```java
"Lambda : Token keyword, List<Token> params, List<Stmt> body",  // Expr 노드
```

`primary()`(혹은 그 근처)에서 `fun`을 만나면 익명 함수 식으로 파싱한다.

```java
private Expr primary() {
  // ...
  if (match(FUN)) return lambda();
  // ...
}

private Expr lambda() {
  Token keyword = previous();                 // 에러 위치용 'fun' 토큰
  consume(LEFT_PAREN, "Expect '(' after 'fun'.");

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
  consume(LEFT_BRACE, "Expect '{' before lambda body.");
  List<Stmt> body = block();
  return new Expr.Lambda(keyword, parameters, body);
}
```

런타임에서는 이미 있는 `LoxFunction`을 거의 그대로 쓴다 — 선언문이 아니라 식에서 함수 객체를 만들어 **반환**할 뿐이고, 이름 바인딩 단계가 없다.

```java
@Override
public Object visitLambdaExpr(Expr.Lambda expr) {
  // 이름만 없는 함수. 현재 환경을 클로저로 포획해 함수 객체를 만들어 값으로 돌려준다.
  return new LoxFunction(expr.params, expr.body, environment);
}
```

(이러려면 `LoxFunction`이 `Stmt.Function` 대신 `params`/`body`를 직접 받도록 살짝 일반화하면 깔끔하다. `toString`은 `<fn>` 정도로.)

### 까다로운 경우: `fun () {};` (표현식 문장)

문제는 **선언문 `fun`과 표현식 `fun`이 같은 토큰으로 시작**한다는 점이다. `declaration()`이 `fun`을 보면 곧장 *명명* 함수 선언으로 파싱하려다, 이름이 없으니 *"Expect function name."* 에러를 낸다 — 익명 함수를 식으로 보기도 전에.

해결은 **한 토큰 더 내다보는 것**이다. `fun` 다음이 식별자면 선언문, `(`면 표현식(익명 함수)이다.

```java
private Stmt declaration() {
  // fun 뒤에 이름이 오면 선언문, 아니면(= '(') 표현식 문장으로 흘려보낸다
  if (check(FUN) && checkNext(IDENTIFIER)) {
    advance();                       // consume 'fun'
    return function("function");
  }
  if (match(VAR)) return varDeclaration();
  return statement();                // 여기로 오면 fun(...){...} 은 expressionStatement에서 lambda로 파싱됨
}
```

`checkNext`는 `tokens.get(current + 1)`을 들여다보는 한 칸짜리 룩어헤드다. 이렇게 하면 `fun () {};`는 표현식 문장으로 가서 `lambda()`가 파싱하고, 만들어진 함수 객체는 아무 데도 안 묶인 채 버려진다 — 부수 효과도 없으니 사실상 무의미한 문장이지만 **문법적으로는 합법**이 된다(자바스크립트의 `(function(){})();` 직전 상태와 비슷한 처지).

> 이 레포(klox)에는 익명 함수가 아직 없다. 위 풀이는 책 기준의 설계이며, 도입한다면 `Expr.Lambda` 노드 추가, `declaration()`의 1토큰 룩어헤드, `LoxFunction`을 `params`/`body`로 일반화하는 세 군데가 손볼 지점이다.

---

## 10-3. 매개변수와 지역 변수의 스코프

> 다음은 유효한 프로그램인가? 즉 함수의 매개변수는 지역 변수와 같은 스코프에 있는가, 바깥 스코프에 있는가?
>
> ```
> fun scope(a) {
>   var a = "local";
> }
> ```

### Lox의 답: 같은 스코프 — 그래서 갈린다

`LoxFunction.call`을 보면, 호출 시 **환경 하나**를 만들어 거기에 매개변수를 정의하고, 그 **같은 환경**에서 본문을 실행한다(본문 블록이 환경을 또 만들지 않는다).

```java
Environment environment = new Environment(closure);
// 매개변수를 이 환경에 define
for (...) environment.define(param.lexeme, argument);
// 본문도 이 환경에서 실행 — 별도 중첩 없음
interpreter.executeBlock(declaration.body, environment);
```

따라서 매개변수 `a`와 본문의 `var a`는 **한 스코프**에 놓인다. 결과는 인터프리터 단독이냐, Resolver가 끼었느냐로 갈린다.

- **10장(인터프리터 단독)**: `var a = "local"`은 같은 환경에서 `a`를 **덮어쓴다**. 에러 없이 유효 — `a`가 `"local"`이 된다.
- **11장(Resolver 도입 이후, 이 레포)**: 같은 스코프에서 같은 이름을 두 번 선언하는 셈이라 `declare`가 *"Already a variable with this name in this scope."* **컴파일 에러**를 낸다.

이 레포는 Resolver를 갖췄으므로 위 프로그램은 **컴파일 에러**다. `resolveFunction`이 매개변수를 declare/define한 바로 그 스코프에서 본문을 해소하기 때문이다.

```java
private void resolveFunction(Stmt.Function function) {
  beginScope();
  for (Token param : function.params) { declare(param); define(param); }
  resolve(function.body);     // ← 같은 스코프. 여기서 var a 가 또 declare → 중복 에러
  endScope();
}
```

### 다른 언어들

- **C / Java**: 매개변수와 함수 최상위 지역 변수는 **같은 스코프**로 보아, 같은 이름 재선언을 **컴파일 에러**로 막는다. (Lox 11장과 같은 입장.)
- **JavaScript**: `function f(a) { var a = 1; }`는 **허용**된다 — `var`가 함수 스코프라 매개변수와 합쳐져 그냥 같은 변수의 재대입이 된다. 단 `let a`로 쓰면 재선언 에러.
- **Scheme**: 매개변수도 `let` 바인딩과 같은 종류의 지역 바인딩이라, 안쪽 `(let ((a ...)))`로 새로 묶으면 **섀도잉**(별개 스코프)이 된다.

### 어떻게 해야 하나(의견)

매개변수를 본문 지역과 **같은 스코프**로 두고 **재선언을 에러로 막는 쪽**(C/Java/Lox-11장)이 가장 덜 놀랍다. 매개변수를 실수로 가리는 버그를 컴파일 시점에 잡아 주기 때문이다. "조용히 덮어쓰기"(JS의 `var`)는 편하지만 의도치 않은 가림을 숨긴다.

---

# 11장 챌린지

## 11-1. 함수 이름은 왜 즉시 정의해도 안전한가

> 다른 변수는 초기화가 끝나기 전엔 사용을 막으면서(`var a = a;`를 에러로), 함수 이름은 본문 해소 *전에* 즉시 define한다. 왜 함수만 그래도 안전한가?

### 답: 초기화식이 "지금" 실행되지 않기 때문

차이는 **이름이 참조되는 시점**에 있다.

- 일반 변수 `var a = <식>;`의 초기화식 `<식>`은 **선언을 해소(그리고 실행)하는 바로 그 자리에서** 평가된다. 그 안에서 `a`를 읽으면 *아직 값이 없는* 자신을 읽는 것이라 위험하다. 그래서 Resolver는 `a`를 `false`(선언됐으나 미초기화)로 두고, 초기화식 안의 `a`를 에러로 잡는다.

- 함수 `fun f() { ... f() ... }`의 "초기화식"은 함수 본문이지만, 본문은 **선언 시점에 실행되지 않는다**. 한참 뒤 `f`가 **호출될 때** 비로소 실행된다. 그때쯤이면 `f`라는 이름은 이미 완전히 바인딩돼 있다. 즉 본문이 `f`를 보더라도, 보는 순간(호출 시점)엔 `f`가 멀쩡히 정의돼 있다.

그래서 함수 이름을 본문 해소 전에 `define`해 두는 것이다 — 그래야 본문 안의 재귀 호출 `f(...)`가 자기 자신으로 올바르게 **해소**된다(거리 계산이 된다). 만약 변수처럼 `false`로 묶어 두면, 본문 안의 재귀 참조가 "자기 초기화에서 자신을 읽음" 에러로 오인된다 — 재귀가 불가능해진다.

```java
@Override
public Void visitFunctionStmt(Stmt.Function stmt) {
  declare(stmt.name);
  define(stmt.name);      // ← 본문 해소 전에 define: 재귀 참조를 허용
  resolveFunction(stmt);
  return null;
}
```

> 한 줄 요약: 변수 초기화식은 **즉시 평가**되므로 자기참조가 위험하지만, 함수 본문은 **나중에 호출될 때** 평가되므로 이름을 먼저 묶어도 안전하고, 오히려 그래야 재귀가 된다.

---

## 11-2. 초기화식의 자기 참조 — 언어별 비교

> `var a = "outer"; { var a = a; }` 같은, 안쪽 초기화식이 자기 이름을 참조하는 코드를 다른 언어들은 어떻게 다루는가? 런타임 에러? 컴파일 에러? 허용? 전역과 지역을 다르게 보는가? 그 선택에 동의하는가?
> `var a = a` 는 런타임에러, `{ var a = a; }` 는 컴파일에러

이건 8-3과 한 뿌리이되, 11장의 초점은 **"안쪽 `a`의 초기화식 속 `a`가 바깥을 보느냐, 자기 자신을 보느냐"**다. 갈림은 언어가 "이름을 언제 스코프에 넣느냐"에 달려 있다.

| 언어                        | 지역 `var a = a;`(안쪽 블록)                                           | 전역은?                                     |
|---------------------------|------------------------------------------------------------------|------------------------------------------|
| **Lox (jlox 11장 / 이 레포)** | **컴파일 에러** *"Can't read local variable in its own initializer."* | 전역은 허용(추적 안 함) — 바깥 `a`를 읽거나 미정의면 런타임 처리 |
| **Rust**                  | 허용 — RHS의 `a`는 *바깥* `a`(섀도잉)                                     | 동일                                       |
| **Java**                  | **컴파일 에러** ("variable a might not have been initialized")        | 필드는 규칙이 다름                               |
| **JavaScript (`let`)**    | **런타임 ReferenceError** (TDZ)                                     | `let` 전역도 동일하게 TDZ                       |
| **JavaScript (`var`)**    | 허용 — `a`는 `undefined`로 호이스팅돼 RHS가 `undefined`                    | 동일                                       |
| **C**                     | 허용하나 **미정의 동작** (초기화 안 된 자기 값)                                   | 동일                                       |
| **Scheme `let`**          | RHS가 *바깥* 스코프에서 평가 → 바깥 `a` (`let*`면 안쪽)                         | —                                        |

전역을 다르게 보는 이유: jlox는 전역 스코프를 Resolver가 추적하지 않는다(REPL에서 한 줄씩 재선언·전방 참조를 허용하려는 실용적 타협). 그래서 **같은 코드라도 지역은 컴파일 에러, 전역은 통과**하는 비대칭이 생긴다.

### 이 레포의 실제 동작

```
var a = "outer";
{
  var a = a;        // 컴파일 에러: Can't read local variable in its own initializer.
}
```

`Resolver.visitVarStmt`가 `declare`(=false) → 초기화식 해소 → `define`(=true) 순서라, 초기화식 안의 `a`가 `false` 상태로 발견돼 막힌다(`Resolver.kt`의 `visitVariableExpr`).

### 동의하는가

**동의한다.** 지역에서 자기 초기화는 거의 항상 실수(섀도잉하려다 오타, 또는 바깥 값을 쓰려던 의도)다. 의도가 "바깥 값 복사"라면 이름을 다르게(`var a2 = a;`) 쓰면 명확해진다. Rust처럼 "RHS는 바깥을 본다"도 일관되고 좋은 선택이지만, **명시적 에러로 막는 쪽**이 모호함을 가장 확실히 없앤다. C의 미정의 동작이 최악이다.

---

## 11-3. 사용되지 않은 지역 변수 보고

> 선언만 되고 한 번도 읽히지 않는 지역 변수를 Resolver가 에러(경고)로 보고하도록 확장하라.

### 풀이

Resolver는 스코프가 닫힐 때 그 스코프의 모든 이름을 알고 있다. 그러니 **"선언됐는가"에 더해 "쓰였는가"를 추적**하고, 스코프를 `endScope`할 때 안 쓰인 것을 보고하면 된다. `Boolean` 대신 작은 상태 객체로 바꾼다.

```java
private static class Variable {
  final Token name;
  enum State { DECLARED, DEFINED, READ }
  State state;
  Variable(Token name, State state) { this.name = name; this.state = state; }
}

private final Stack<Map<String, Variable>> scopes = new Stack<>();
```

`declare`/`define`은 상태를 갱신하고, **변수를 읽을 때 `READ`로 표시**한다.

```java
private void resolveLocal(Expr expr, Token name, boolean isRead) {
  for (int i = scopes.size() - 1; i >= 0; i--) {
    if (scopes.get(i).containsKey(name.lexeme)) {
      interpreter.resolve(expr, scopes.size() - 1 - i);
      if (isRead) {
        scopes.get(i).get(name.lexeme).state = Variable.State.READ;  // 사용됨 표시
      }
      return;
    }
  }
}
```

`visitVariableExpr`은 읽기(`isRead = true`)로, `visitAssignExpr`은 쓰기(`isRead = false`)로 부른다 — "할당만 하고 읽지 않은" 변수도 잡고 싶다면 할당은 사용으로 치지 않는다.

스코프를 닫을 때 점검한다.

```java
private void endScope() {
  Map<String, Variable> scope = scopes.pop();
  for (Map.Entry<String, Variable> entry : scope.entrySet()) {
    if (entry.getValue().state != Variable.State.READ) {
      Lox.error(entry.getValue().name, "Local variable is never used.");
    }
  }
}
```

```
fun f() {
  var unused = 123;     // Local variable is never used.
  var used = 1;
  print used;           // used 는 READ → OK
}
```

주의: 전역은 스코프 스택에 없으니 검사 대상이 아니다(REPL에서 흔히 선언만 하므로 적절). 매개변수까지 잡으면 인터페이스를 맞추려 안 쓰는 매개변수가 많아 과하게 시끄러우니, 보통 매개변수는 예외로 둔다.

> 이 레포에는 이 기능이 없다(Resolver는 `Boolean` 맵만 쓴다). 위는 책 기준 확장 설계다.

---

## 11-4. 인덱스 기반 환경 (성능)

> Resolver가 변수의 스코프 거리는 계산하지만, 그 스코프 안에서는 여전히 이름으로(해시맵) 찾는다. 각 지역 변수에 **슬롯 인덱스**를 부여하고, 인터프리터가 **배열 인덱스**로 접근하게 만들어 성능 향상을 측정하라.

### 아이디어

현재 변수 접근은 **(거리, 이름)** → `getAt(distance, name)`이고, 마지막 단계가 `HashMap.get(name)`이다. 해시 계산·충돌 처리 비용이 매 접근에 든다. 이를 **(거리, 인덱스)** → `array[index]`로 바꾸면 상수 시간의 배열 접근이 된다.

### Resolver 쪽: 슬롯 번호 부여

각 스코프에서 변수를 declare할 때 **0부터 증가하는 슬롯 번호**를 매긴다. 변수 사용을 해소할 때 거리뿐 아니라 그 슬롯 번호도 함께 인터프리터에 넘긴다.

```java
// 스코프: 이름 → 슬롯 인덱스 (선언 순서대로 0,1,2,...)
private void declare(Token name) {
  Map<String, Integer> scope = scopes.peek();
  scope.put(name.lexeme, scope.size());      // size() 가 다음 슬롯 번호
}

private void resolveLocal(Expr expr, Token name) {
  for (int i = scopes.size() - 1; i >= 0; i--) {
    Integer slot = scopes.get(i).get(name.lexeme);
    if (slot != null) {
      interpreter.resolve(expr, scopes.size() - 1 - i, slot);   // 거리 + 슬롯
      return;
    }
  }
}
```

### 인터프리터 쪽: 맵 대신 배열

`Environment`의 `HashMap<String, Object>`를 `Object[]`(혹은 `ArrayList<Object>`)로 바꾼다. 변수는 이름이 아니라 슬롯 번호로 들고 난다.

```java
class Environment {
  final Environment enclosing;
  private final List<Object> values = new ArrayList<>();   // 이름 대신 인덱스

  Object getAt(int distance, int slot) {
    return ancestor(distance).values.get(slot);            // 해시 없음, 배열 접근
  }
  void setAt(int distance, int slot, Object value) {
    ancestor(distance).values.set(slot, value);
  }
}
```

`locals` 맵도 `Map<Expr, Integer>`(거리) 외에 슬롯을 함께 저장하도록 확장한다(거리·슬롯 쌍을 담는 작은 레코드, 또는 두 맵).

### 측정

- 변수 접근이 잦은 벤치마크(깊은 재귀 피보나치, 루프 누적 등)를 `clock()`으로 전후 시간 측정한다.
- 기대: 해시 비용이 사라져 변수 접근이 빨라진다. 다만 **전역은 여전히 맵**(슬롯을 미리 못 정함)이라, 전역 변수 위주 코드에선 차이가 작다.
- 트레이드오프: 슬롯은 선언 순서에 의존하므로, Resolver와 인터프리터의 슬롯 부여 규칙이 **정확히 일치**해야 한다(둘 중 하나라도 순서가 어긋나면 엉뚱한 변수를 읽는 치명적 버그). 이름 디버깅 정보가 사라지는 비용도 있다.

> 이 레포는 이름 기반 `HashMap` 환경을 쓴다(`Environment.kt`). 위는 clox(2부)가 택하는 방향에 가까운, jlox에서의 인덱스화 설계다.

---

# 12장 챌린지

> 이 레포(klox)에는 12장(클래스)이 실제 구현돼 있다 — `LoxClass`, `LoxInstance`, `Expr.Get/Set/This`, `Stmt.Class`, 메서드 바인딩(`LoxFunction.bind`), `init` 생성자. 아래 풀이들은 그 위에 얹는 확장이며, 별도 표시가 없으면 레포엔 아직 없는 추가 기능이다.

## 12-1. 정적 메서드 (static methods)

> 인스턴스 메서드는 있지만, 클래스 객체에서 바로 부르는 정적 메서드가 없다. 메서드 선언 앞에 `class` 키워드를 붙여 정적 메서드를 표시하고, 클래스 객체에 직접 매달리게 하라.

```
class Math {
  class square(n) { return n * n; }    // 정적 메서드
}
print Math.square(3);   // 9   — 인스턴스 없이 클래스에서 바로 호출
```

### 풀이: 메타클래스 (metaclass)

Smalltalk의 **메타클래스**가 깔끔하다. 핵심: "정적 메서드를 클래스에서 부른다"는 건 "클래스를 *인스턴스로 보는* 메타클래스의 인스턴스 메서드를 부른다"는 것. 그래서 `LoxClass`가 **그 자체로 `LoxInstance`이기도** 하게 만들면, 12장의 프로퍼티 조회(`LoxInstance.get`)를 고치지 않고 그대로 재사용한다.

- 파서: 메서드 앞에 `class`가 오면 **정적 메서드 목록**으로 분리한다.

```java
List<Stmt.Function> methods = new ArrayList<>();
List<Stmt.Function> classMethods = new ArrayList<>();
while (!check(RIGHT_BRACE) && !isAtEnd()) {
  boolean isStatic = match(CLASS);                 // 'class' 접두사
  (isStatic ? classMethods : methods).add(function("method"));
}
```

- `LoxClass`: `LoxInstance`를 상속하고, 생성자에서 **klass = 메타클래스**로 둔다(`LoxInstance(LoxClass)` 생성자 하나만 열어 주면 된다).

```java
class LoxClass extends LoxInstance implements LoxCallable {
  LoxClass(LoxClass metaclass, String name, LoxClass superclass,
           Map<String, LoxFunction> methods) {
    super(metaclass);          // LoxInstance의 klass = 메타클래스
    // name, superclass, methods 대입 …
  }
}
```

- 인터프리터: 정적 메서드로 **메타클래스를 먼저** 만들고, 그걸 klass로 갖는 실제 클래스를 만든다.

```java
LoxClass metaclass = new LoxClass(null, name.lexeme + " metaclass", null, classMethods);
LoxClass klass     = new LoxClass(metaclass, name.lexeme, superclass, methods);
```

조회는 새로 짤 게 없다: `Math.square`는 `LoxClass`(=`LoxInstance`)의 `get` → `klass`(=메타클래스)의 `findMethod`로 `square`를 찾아 **클래스 자신에 bind**한다. 그래서 정적 메서드 안의 `this`는 클래스 객체가 된다. 12장 코드를 한 겹 더 쓰는 셈이다.

---

## 12-2. 게터 (getter)

> 매개변수 목록 없이 선언하고, 그 이름의 프로퍼티에 *접근하는 순간* 본문이 실행되는 게터 메서드를 추가하라.

```
class Circle {
  init(radius) { this.radius = radius; }
  area {                                  // 괄호 없음 = 게터
    return 3.141592653 * this.radius * this.radius;
  }
}
var c = Circle(4);
print c.area;     // 50.265...   — c.area() 가 아니라 c.area
```

### 풀이

게터는 ① 매개변수 목록이 없고, ② `.area`로 **접근(Get)하는 순간 곧바로 호출**된다는 점만 다르다. 나머지는 메서드와 같다(`bind`로 `this`가 묶인다).

- 파서: `(`가 안 오면 게터. 매개변수 파싱을 건너뛰고 `params`를 `null`로 둬 표시한다.

```java
private Stmt.Function function(String kind) {
  Token name = consume(IDENTIFIER, "Expect " + kind + " name.");

  List<Token> parameters = null;                        // null = 게터
  if (!kind.equals("method") || check(LEFT_PAREN)) {    // 함수거나 '(' 가 보이면 일반 메서드
    consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
    parameters = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do { parameters.add(consume(IDENTIFIER, "Expect parameter name.")); }
      while (match(COMMA));
    }
    consume(RIGHT_PAREN, "Expect ')' after parameters.");
  }
  consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
  return new Stmt.Function(name, parameters, block());   // parameters == null 이면 게터
}
```

- `LoxFunction`: `isGetter() = (params == null)`. (전용 플래그 불필요.)

- Resolver: `resolveFunction`에서 `params`가 null이면 매개변수 선언을 건너뛴다(가드 안 넣으면 NPE). 게터도 그냥 `METHOD`로 해소하면 `this`가 맞게 잡힌다.

```java
beginScope();
if (function.params != null) {                 // 게터면 건너뜀
  for (Token param : function.params) { declare(param); define(param); }
}
resolve(function.body);
endScope();
```

- 인터프리터: `visitGetExpr`에서 꺼낸 멤버가 게터면 **즉시 `call`**, 아니면 12장처럼 값으로 돌려준다.

```
class Circle {
  init(radius) { this.radius = radius; }
  area {                                  // 괄호 없음 = 게터
    return 3.141592653 * this.radius * this.radius;
  }
}
var c = Circle(4);
print c.area;     // 50.265...   — c.area() 가 아니라 c.area
```

```java
@Override
public Object visitGetExpr(Expr.Get expr) {
  Object object = evaluate(expr.object);
  if (object instanceof LoxInstance) {
    Object result = ((LoxInstance) object).get(expr.name);  // 필드 or bind된 LoxFunction
    if (result instanceof LoxFunction && ((LoxFunction) result).isGetter()) {
      return ((LoxFunction) result).call(this, Collections.emptyList());  // 게터 → 즉시 실행
    }
    return result;
  }
  throw new RuntimeError(expr.name, "Only instances have properties.");
}
```

`c.area`는 인자 0개로 호출되고 `this`가 bind돼 있어 `this.radius`가 동작한다. (`c.area()`라고 쓰면 게터 결과(숫자)를 또 호출하려다 런타임 에러.)

---

## 12-3. 캡슐화 — 누가 필드에 접근할 수 있나 (논술)

> Python·JS는 객체의 필드를 밖에서 자유롭게 읽고 쓰게 한다. Ruby·Smalltalk는 인스턴스 상태를 캡슐화해 메서드를 통해서만 접근하게 한다. Lox는 어느 쪽이며, 캡슐화한다면 필드·메서드 이름 충돌은 어떻게 다루겠는가? 트레이드오프와 견해를 논하라.

### Lox의 현재 입장

Lox는 **Python·JS 쪽**이다. `instance.field`로 밖에서 자유롭게 읽고, `instance.field = x`로 새 필드까지 추가할 수 있다(`LoxInstance.set`은 무조건 맵에 넣는다). 캡슐화가 없다.

### 캡슐화한다면 — 이름 충돌 문제

Ruby/Smalltalk처럼 "필드는 메서드 본문(=`this`를 통해서만)에서만 접근, 밖에서는 메서드로만"으로 가면 한 가지가 꼬인다: **`this.x` 같은 필드 접근과 `obj.method()` 같은 멤버 접근이 같은 `.` 문법을 공유**한다는 점. 밖에서의 `obj.x`는 필드면 막아야 하고 메서드(혹은 게터)면 허용해야 하니, **이름공간을 나눌지** 결정해야 한다.

- **Ruby**: 필드(`@x`)와 메서드를 **다른 문법(`@` 접두사)**으로 구분한다. 그래서 같은 이름의 필드 `@x`와 메서드 `x`가 공존할 수 있고, 밖에서는 메서드만 보인다. 충돌이 원천적으로 없다.
- **Smalltalk**: 인스턴스 변수는 메서드 안에서만 보이고, 밖에서는 오직 메시지(메서드). 접근자(`x`, `x:`)를 직접 정의해 노출한다.

Lox에 도입한다면 Ruby식이 깔끔하다 — `this.x`(필드, 내부 전용)와 `obj.x`(메서드, 외부 노출)를 의미상 분리하고, 필드는 밖에서 못 읽게 한다. 그러면 필드와 같은 이름의 게터를 둬도 충돌하지 않는다(밖에선 게터, 안에선 필드).

### 강제한다면 — 어디에 검사를 넣나

흥미로운 점은, Lox 구조에서 "필드는 `this`로만"을 강제하는 게 **구문 정보만으로 가능**하다는 것이다. 필드 접근의 진입점은 `visitGetExpr`/`visitSetExpr` 둘뿐이고, 거기서 **접근 대상 표현식이 `Expr.This`인지** 보면 "안에서(this) 접근"과 "밖에서(obj) 접근"을 가른다.

```java
@Override
public Object visitGetExpr(Expr.Get expr) {
  Object object = evaluate(expr.object);
  if (object instanceof LoxInstance) {
    LoxInstance instance = (LoxInstance) object;
    boolean fromThis = expr.object instanceof Expr.This;   // this.x 인가, obj.x 인가
    return instance.get(expr.name, fromThis);              // 필드는 fromThis 일 때만 허용
  }
  throw new RuntimeError(expr.name, "Only instances have properties.");
}
```

```java
// LoxInstance.get — 외부에서 온 필드 접근이면 메서드만 허용
Object get(Token name, boolean fromThis) {
  if (fromThis && fields.containsKey(name.lexeme)) {       // 필드는 내부에서만
    return fields.get(name.lexeme);
  }
  LoxFunction method = klass.findMethod(name.lexeme);      // 밖에서는 메서드/게터만
  if (method != null) return method.bind(this);
  throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
}
```

이러면 같은 이름의 필드 `x`와 게터 `x`가 공존해도, **밖에선 게터가, 안(`this.x`)에선 필드가** 잡혀 충돌이 사라진다. `set`도 같은 방식으로 막으면 외부의 `obj.x = ...` 필드 추가가 차단된다. 비용은 거의 없다(분기 하나) — Lox가 안 하는 건 능력이 없어서가 아니라 **단순함을 택한 설계**임을 보여준다.

### 트레이드오프와 견해

- **개방형(현재 Lox/Python/JS)**: 단순하고 유연하다. 메타프로그래밍·직렬화·테스트가 쉽다. 대신 불변식을 강제할 수 없고, 외부가 내부 표현에 결합된다.
- **캡슐화(Ruby/Smalltalk)**: 불변식 보장·리팩터링 안전성이 크다. 대신 문법(접근자, `@`)이 늘고 보일러플레이트가 생긴다.

견해: **교육용·작은 동적 언어인 Lox라면 현재의 개방형이 적절**하다. 규모가 커지고 라이브러리 경계가 생기면 "관례적 비공개"(파이썬의 `_name`)만으로도 실무엔 충분하다. 강제 캡슐화는 정적 검사(접근 제어자)와 함께 갈 때 가치가 가장 크다.

---

# 13장 챌린지

> 이 레포에는 13장(단일 상속·`super`)도 구현돼 있다(`LoxClass.superclass`/`findMethod`, `Expr.Super`, Resolver의 `SUBCLASS`). 아래는 그 너머의 확장이다.

## 13-1. 다른 재사용 메커니즘 — 무엇을, 왜 (논술 + 스케치)

> Lox는 단일 상속뿐이다. 믹스인·트레이트·다중 상속 등 다른 재사용 방식 중 하나를 고른다면?

후보 비교:

- **다중 상속**: 강력하지만 **다이아몬드 문제**(공통 조상의 중복)와 메서드 충돌 해소(C3 선형화 등)가 복잡하다.
- **믹스인(mixin)**: 클래스에 메서드 묶음을 "섞어 넣는다". 선형화로 충돌을 순서로 푼다(Ruby `include`, Python MRO). 단일 상속에 얹기 쉽다.
- **트레이트(trait)**: 상태 없는 메서드 묶음. 충돌을 **명시적으로** 해소(이름 변경·배제)하게 강제해 다이아몬드 모호성을 피한다(Scala, Rust의 트레이트와 결이 비슷).

**선택: 믹스인**. 이유 — Lox의 기존 단일 상속 체인을 거의 건드리지 않고 얹을 수 있고, "동작 재사용"이라는 실용 목표에 충분하다. 상태 충돌 위험은 트레이트(상태 없음)가 더 안전하지만, Lox 메서드는 본문만이라 믹스인으로도 단순하게 간다.

### 핵심 개념 — "선형화(MRO)"로 충돌을 순서로 바꾼다

단일 상속에서 메서드 탐색은 쉽다. 내 클래스에 없으면 **부모로 한 칸 위**, 또 없으면 그 위로 — 상속이 **한 줄(체인)**이라 "다음에 볼 곳"이 늘 하나뿐이다. `super`도 "그 한 줄에서 바로 위"를 뜻한다.

믹스인은 이 한 줄에 **곁가지를 붙인다.** `class C < Base with M1, M2`면 메서드 출처가 C·M1·M2·Base 넷이다. 이제 `c.foo()`를 부르면 **넷 중 누구의 `foo`가 이기나?** 곁가지가 생기면서 "다음에 볼 곳"이 여러 개가 돼버렸다.

해법은 **곁가지를 다시 한 줄로 펴는 것** — 이게 선형화(MRO, *Method Resolution Order*)다. C·M1·M2·Base를 **정해진 규칙으로 한 줄로 늘어세우면**, 탐색은 다시 "그 줄을 앞에서부터 훑기"로 단순해진다. 즉 충돌이 **순서 문제**로 바뀐다.

줄 세우는 규칙(이 스케치): **자기 → 믹스인(뒤에 쓴 게 앞) → 상위의 MRO**.

```
class C < Base with M1, M2

     C            곁가지(믹스인)를 한 줄로 펴면:
   / | \
 M2 M1 Base  →   [C] → [M2] → [M1] → [Base]
                 자기   뒤 믹스인  앞 믹스인   상위
```

- `c.foo()` → 이 줄을 앞에서부터 훑어 **처음 만난 `foo`**가 이긴다(C에 있으면 C, 없으면 M2, …).
- **뒤에 쓴 믹스인이 앞**: `with M1, M2`에서 M2가 M1보다 우선(나중에 섞은 게 덮어쓴다는 직관).
- **상위(Base)는 맨 뒤**: 내 것·믹스인에 다 없을 때 최후로 본다.

다이아몬드(공통 조상)도 이 줄에서 자동으로 풀린다. M1·M2가 둘 다 Base를 상속해도, 선형화가 **Base를 한 번만** 남기므로(중복 제거) "Base를 두 번 거치나?" 같은 모호함이 없다. 다중 상속의 다이아몬드 문제를 **한 줄로 펴고 중복을 지우는 것**으로 회피하는 셈이다.

그러면 `super`는 자연스럽게 **"이 줄에서 내 다음 칸"**으로 일반화된다. 단일 상속에선 다음 칸이 곧 부모 하나였지만, MRO에선 다음 칸이 믹스인일 수도 상위일 수도 있다 — 의미는 똑같이 "현재 정의 **바로 다음**에 볼 곳". 그래서 아래 스케치는 `super` 환경에 단일 클래스 대신 **MRO의 꼬리 목록**을 담는다.

### 스케치

`class C < Base with M1, M2 { ... }` 문법을 두고, 클래스마다 **선형화된 탐색 목록(MRO)** 을 만들어 `findMethod`가 그 순서로 훑게 한다.

- 파서/AST: `with` 뒤 믹스인 이름들을 `Expr.Variable`로 모아 `Stmt.Class`에 넣는다(상위클래스와 같은 이유로 변수다).

```java
List<Expr> mixins = new ArrayList<>();
if (match(WITH)) {
  do { consume(IDENTIFIER, "Expect mixin name."); mixins.add(new Expr.Variable(previous())); }
  while (match(COMMA));
}
return new Stmt.Class(name, superclass, mixins, methods);
```

- 인터프리터: 믹스인들을 평가해 `LoxClass` 목록을 얻어 `LoxClass`에 넘긴다(각각 클래스인지 런타임 검사).

- `LoxClass`: MRO = **자기 → 믹스인 역순(뒤에 쓴 게 우선) → 상위의 MRO**. `findMethod`는 MRO를 순서대로 훑는다.

```java
List<LoxClass> linear = new ArrayList<>();
linear.add(this);                                      // 1) 나 자신
for (int i = mixins.size() - 1; i >= 0; i--)           // 2) 믹스인 역순
  linear.add(mixins.get(i));
if (superclass != null) linear.addAll(superclass.mro); // 3) 상위 MRO 이어붙임
this.mro = dedup(linear);                              // 중복 제거(정석은 C3 선형화)

LoxFunction findMethod(String name) {
  for (LoxClass k : mro) { LoxFunction m = k.methodsLocal(name); if (m != null) return m; }
  return null;
}
```

- `super`: "상위클래스 하나" 대신 **"MRO에서 정의 클래스의 다음"** 으로 일반화한다 — 바인딩 시 super 환경에 단일 클래스 대신 *MRO의 꼬리 목록*을 담으면 된다.

`class C < Base with M1, M2` → MRO = `[C, M2, M1, Base]`. 다중 상속과 달리 공통 조상 Base가 **한 번만** 들어가 충돌이 순서로 풀린다. 믹스인은 상태(필드)가 없어 인스턴스 구조엔 영향이 없고, 손대는 곳은 **MRO와 super의 의미**뿐이다.

---

## 13-2. BETA의 `inner`

> Lox(와 대부분 OOP)는 메서드 탐색을 **하위에서 위로** 한다(하위 오버라이드가 이기고, `super`로 위를 부른다). BETA는 반대로 **위에서 아래로** 간다 — 가장 상위 메서드가 먼저 실행되고, 그 안에서 `inner`로 하위 버전을 불러들인다. Lox에 `inner`를 구현하려면?

### 개념 비교

```
super: 하위가 주도. 하위 메서드 안에서 super.x() 로 상위를 호출. (안→밖으로 부른다)
inner: 상위가 주도. 상위 메서드가 inner; 로 하위를 호출.        (밖→안으로 부른다)
```

`inner`는 상위 메서드가 "여기에 하위가 끼어들 자리"를 표시하는 것이다. 하위 오버라이드가 없으면 `inner`는 아무것도 안 한다(no-op).

```
class Doughnut {
  cook() {
    print "Fry until golden brown.";
    inner;                                   // 하위가 있으면 여기서 실행
    print "Place on a rack to cool.";
  }
}
class BostonCream < Doughnut {
  cook() { print "Pipe full of custard."; }
}
BostonCream().cook();
// Fry until golden brown.
// Pipe full of custard.            ← inner 자리에서 하위 cook 실행
// Place on a rack to cool.
```

`super:sub = 1:n`(상위 하나에 하위 여럿)이라 "`inner`가 그중 뭘?"이 헷갈리지만, 메서드는 **클래스가 아니라 객체에 대고** 부른다. `BostonCream()` 객체는 실제 클래스가 하나로 확정돼 혈통이 딱 한 줄이라, 형제(`Glazed`)는 후보조차 아니고 `inner`는 그 줄을 한 칸 내려갈 뿐이다. `super`가 부모 하나로 위가 정해지듯, `inner`는 실제 클래스로 아래가 정해진다(같은 혈통 한 줄을 방향만 반대로).

### 왜 이렇게 하나 — Template Method 패턴

`inner`는 **Template Method 패턴을 언어 기능으로 굳힌 것**이다. 부모가 전체 흐름(뼈대)을 한 메서드에 박아두고, 바뀌는 부분만 **별도의 훅 메서드로 빼** 자식이 오버라이드하게 하는 패턴이다.

```java
// 부모: 흐름을 쥔다. 바뀔 부분은 fill() 로 위임.
class Doughnut {
  final void cook() {       // ← 템플릿 메서드 (뼈대, 못 건드리게 final)
    fry();
    fill();                 // ← 훅. 여기만 자식이 채운다
    cool();
  }
  void fry()  { print "튀김"; }
  void fill() { }           // 기본은 비어있음 (자식이 오버라이드)
  void cool() { print "식힘"; }
}
class BostonCream extends Doughnut {
  @Override void fill() { print "커스터드"; }   // 구멍만 채움
}
```

핵심 세 가지:
1. **부모가 흐름을 쥔다** — `cook()`이 `fry→fill→cool` 순서를 강제. 자식은 순서를 못 바꿈.
2. **자식은 지정된 구멍(`fill`)만 채운다.**
3. **앞뒤(`fry`, `cool`)는 무조건 실행된다** — 자식이 건드릴 수 없으니 보장됨.

`inner` = **이걸 언어가 자동으로 해주는 것.** Template Method는 훅 메서드를 손으로 따로 빼야 한다(`cook`과 `fill`을 분리). BETA의 `inner`는 그 분리 없이, 같은 이름 메서드 안에 `inner` 키워드 하나로 "여기가 구멍"이라 표시만 하면 된다.

```
class Doughnut {
  cook() { print "튀김"; inner; print "식힘"; }   // inner = fill() 자리
}
class BostonCream < Doughnut {
  cook() { print "커스터드"; }                     // 이 cook 이 그 구멍에 들어감
}
```

`super`와 뒤집힌 지점이 핵심이다.

```
super: 자식이 주도. super.cook() 안 부르면 부모 앞뒤 코드가 통째로 실종 가능.
inner: 부모가 주도. 자식은 뚫린 구멍에만 들어가, 부모 앞뒤(fry/cool)는 항상 실행 보장.
```

`super`는 흐름 보장이 자식의 "매너"에 달렸지만, `inner`는 부모가 운전대를 쥐고 순서·전후 처리를 **구조적으로 강제**한다.

### 구현에 필요한 것

`super`의 거울상이다. (A) 호출이 항상 **최상위 정의에서 시작**하고, (B) `inner`가 **하위 버전**을 가리킨다.

- 디스패치: `instance.cook()`은 수신자 클래스에서 위로 올라가며 같은 이름 정의를 모아 **상위→하위 체인**으로 엮고, 머리(최상위)부터 호출한다. 각 단계의 `inner` = 다음(하위) 단계. (`super`를 클로저에 담던 자리에 방향만 반대로.)

```java
private LoxFunction bindChain(List<LoxFunction> defs, LoxInstance inst) {
  LoxFunction next = null;                        // 맨 아래의 inner = 없음
  for (int i = defs.size() - 1; i >= 0; i--)      // 아래→위로 엮어 올라간다
    next = defs.get(i).bindWithInner(inst, next); // 환경에 this 와 inner(=next) 를 정의
  return next;                                    // 머리(최상위) 반환 → 이걸 call
}
```

- `inner` 평가(`Expr.Inner`): 클로저에서 꺼내 호출, 없으면 no-op.

```java
@Override
public Object visitInnerExpr(Expr.Inner expr) {
  int distance = locals.get(expr);
  LoxFunction inner = (LoxFunction) environment.getAt(distance, "inner");
  if (inner == null) return null;                 // 하위 오버라이드 없으면 아무것도 안 함
  return inner.call(this, Collections.emptyList());
}
```

- Resolver: `inner`도 `this`/`super`처럼 가짜 스코프로 해소하고 클래스 밖 사용을 정적 에러로 막는다(13.4와 같은 틀).

`BostonCream().cook()`이면 체인은 `[Doughnut.cook → BostonCream.cook]`. Doughnut.cook이 먼저 돌다 `inner`에서 BostonCream.cook이 끼어들고, 다시 Doughnut.cook으로 돌아와 마무리한다 — `inner`는 **방향만 반대인 `super`**라, 클로저에 담아 거리로 꺼내는 그 기계를 뒤집어 재사용하는 게 전부다.

