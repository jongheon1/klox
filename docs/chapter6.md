# 6장 · Parsing Expressions (표현식 파싱)

파서의 일: **토큰 나열 → 문법 규칙에 맞는 AST(추상 구문 트리)**.
스캐너가 문자를 토큰으로 묶었다면, 파서는 그 토큰을 문법 구조로 묶는다.

---

## 6.1 모호한 문법과 그 해결

### 모호함(Ambiguity) 문제

5장에서 정의한 문법은 사실 **모호**했다:

```
expression → literal | unary | binary | grouping ;
binary     → expression operator expression ;
```

`6 / 3 - 1`을 이 문법으로 파싱하면 **서로 다른 두 트리**가 모두 가능하다:

- `(6 / 3) - 1` = 1
- `6 / (3 - 1)` = 3

같은 문자열인데 트리가 다르면 **결과가 달라진다**. 파서가 둘 중 하나를 결정론적으로 고를 수 있어야 한다. 그 기준이 수학에서 쓰던 두 규칙이다.

### 우선순위와 결합 법칙

- **우선순위(precedence)**: 서로 *다른* 연산자 중 무엇을 먼저 묶는가. `*`가 `+`보다 우선순위가 높다("더 단단히 묶인다").
- **결합 법칙(associativity)**: *같은* 우선순위의 연산자가 연달아 나올 때 어느 쪽부터 묶는가.
  - 좌결합(left): `5 - 3 - 1` = `(5 - 3) - 1` — 대부분의 산술 연산
  - 우결합(right): `a = b = c` = `a = (b = c)` — 할당

### Lox 우선순위 표 (C와 동일, 낮은 것 → 높은 것)

| 이름 | 연산자 | 결합 |
|---|---|---|
| Equality | `==` `!=` | 좌 |
| Comparison | `>` `>=` `<` `<=` | 좌 |
| Term | `-` `+` | 좌 |
| Factor | `/` `*` | 좌 |
| Unary | `!` `-` | 우 |

### 해법: 우선순위별로 문법을 계층화

우선순위 단계마다 규칙을 하나씩 만들고, 각 규칙은 자기 단계와 그보다 높은 단계만 매칭하게 한다:

```
expression → equality ;
equality   → comparison ( ( "!=" | "==" ) comparison )* ;
comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term       → factor ( ( "-" | "+" ) factor )* ;
factor     → unary ( ( "/" | "*" ) unary )* ;
unary      → ( "!" | "-" ) unary
           | primary ;
primary    → NUMBER | STRING | "true" | "false" | "nil"
           | "(" expression ")" ;
```

- 낮은 우선순위가 위, 높은 우선순위가 아래. 트리에서 더 깊이 들어간(=우선순위 높은) 노드가 먼저 평가된다.
- `( ... )*` 반복 패턴이 같은 단계 연산자를 좌결합 시퀀스로 묶는다.
- 이 문법은 더 이상 모호하지 않다. 각 입력에 대해 트리가 유일하게 결정된다.

---

## 6.2 재귀 하향 파싱 (Recursive Descent)

- **하향식(top-down)**: 최상위 규칙(`expression`)에서 출발해 트리의 잎으로 내려간다.
- 핵심 아이디어: **문법 규칙을 코드로 직역**한다.

| 문법 표기 | 코드 |
|---|---|
| 터미널 | 토큰 매칭/소비 |
| 논터미널 | 그 규칙의 함수 호출 |
| `\|` | `if` / `switch` |
| `*` `+` | `while` / `for` 루프 |
| `?` | `if` |

### 파서 클래스 구조 — 스캐너와 판박이

| | 스캐너 (3장) | 파서 (6장) |
|---|---|---|
| 입력 | 문자(char) 나열 | 토큰 나열 |
| 위치 추적 | `current` (문자 인덱스) | `current` (토큰 인덱스) |
| 헬퍼 | `advance` `peek` `match` | `advance` `peek` `match` `check` `consume` |
| 출력 | 토큰 리스트 | AST |

```java
class Parser {
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }
}
```

### 헬퍼 메서드

```java
private boolean match(TokenType... types) {
  for (TokenType type : types) {
    if (check(type)) {
      advance();
      return true;
    }
  }
  return false;
}

private boolean check(TokenType type) {
  if (isAtEnd()) return false;
  return peek().type == type;
}

private Token advance() {
  if (!isAtEnd()) current++;
  return previous();
}

private boolean isAtEnd() {
  return peek().type == EOF;
}

private Token peek() {
  return tokens.get(current);
}

private Token previous() {
  return tokens.get(current - 1);
}
```

`match`는 현재 토큰이 주어진 종류 중 하나면 **소비하고 true**, 아니면 위치를 그대로 둔 채 false를 돌려준다. `check`는 소비 없이 종류만 확인한다. 이 구분이 루프 조건과 분기를 만든다.

### 핵심 코드 패턴 — `equality()`

```java
private Expr expression() {
  return equality();
}

private Expr equality() {
  Expr expr = comparison();              // 한 단계 높은 우선순위 먼저

  while (match(BANG_EQUAL, EQUAL_EQUAL)) {  // ( ... )* → while
    Token operator = previous();
    Expr right = comparison();
    expr = new Expr.Binary(expr, operator, right);  // 좌결합으로 누적
  }

  return expr;
}
```

`a == b == c`를 파싱하면 루프를 돌 때마다 지금까지 만든 트리를 새 노드의 **왼쪽 자식**으로 넣는다. 그 결과 `((a == b) == c)`라는 왼쪽으로 치우친 트리가 만들어진다 — 이게 좌결합이다.

`comparison()`, `term()`, `factor()`는 **연산자 종류만 다를 뿐 완전히 같은 패턴**이다:

```java
private Expr comparison() {
  Expr expr = term();
  while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
    Token operator = previous();
    Expr right = term();
    expr = new Expr.Binary(expr, operator, right);
  }
  return expr;
}

private Expr term() {
  Expr expr = factor();
  while (match(MINUS, PLUS)) {
    Token operator = previous();
    Expr right = factor();
    expr = new Expr.Binary(expr, operator, right);
  }
  return expr;
}

private Expr factor() {
  Expr expr = unary();
  while (match(SLASH, STAR)) {
    Token operator = previous();
    Expr right = unary();
    expr = new Expr.Binary(expr, operator, right);
  }
  return expr;
}
```

### `unary()`만 다른 이유 — 우결합

단항 연산자는 우결합이라 루프가 아니라 **자기 자신을 재귀 호출**한다:

```java
private Expr unary() {
  if (match(BANG, MINUS)) {
    Token operator = previous();
    Expr right = unary();          // 우결합 → 재귀
    return new Expr.Unary(operator, right);
  }
  return primary();
}
```

### `primary()` — 트리의 잎

가장 높은 우선순위, 즉 리터럴과 괄호 그룹:

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

  throw error(peek(), "Expect expression.");
}
```

괄호 안에서 다시 최상위 `expression()`을 호출하는 데 주목하라. 이 재귀가 우선순위 계단을 한 바퀴 다시 돌게 해준다.

---

## 6.3 구문 에러

파서의 일은 사실 **두 가지**다:

1. 유효한 토큰 나열 → AST 생성
2. 무효한 토큰 나열 → **에러를 감지하고 알려주기**

좋은 파서는 에러를 만나도 죽지 않고, 가능한 한 많은 에러를 한 번에 보고하며, 잘못된 입력에 매달려 무한 루프에 빠지지 않아야 한다.

### `consume`과 에러 보고

```java
private Token consume(TokenType type, String message) {
  if (check(type)) return advance();
  throw error(peek(), message);
}

private ParseError error(Token token, String message) {
  Lox.error(token, message);
  return new ParseError();
}
```

`error`는 `ParseError`를 **반환**하지만, 던질지는 호출하는 쪽이 정한다. 이 유연함이 에러 복구의 핵심이다.

### 패닉 모드와 동기화

- 에러 발견 → **패닉 모드(panic mode)** 진입 → 정신을 차릴 수 있는 지점까지 토큰을 버린다 = **동기화(synchronization)**.
- 동기화 지점은 전통적으로 **문장(statement) 경계**:
  - `;` 바로 뒤 = 한 문장이 끝났을 가능성이 높다.
  - `class` `fun` `var` `for` `if` `while` `print` `return` = 새 문장이 시작될 가능성이 높다.

```java
private void synchronize() {
  advance();

  while (!isAtEnd()) {
    if (previous().type == SEMICOLON) return;

    switch (peek().type) {
      case CLASS:
      case FUN:
      case VAR:
      case FOR:
      case IF:
      case WHILE:
      case PRINT:
      case RETURN:
        return;
    }

    advance();
  }
}
```

- 구현은 Java **예외**로 콜스택을 되감는다. `ParseError`를 throw하면 문장 경계에서 catch한다.
- 재귀 하향 파서의 상태는 곧 콜스택 그 자체다. 따라서 상태를 리셋한다는 것은 스택을 되감는 것과 같다.
- 아직 문장 문법이 없어서 이번 장에서는 기계만 만들어두고, 실전 투입은 8장에서 한다.

---

## 6.4 결과

```java
Expr parse() {
  try {
    return expression();
  } catch (ParseError error) {
    return null;
  }
}
```

- `parse()`를 호출하면 표현식 하나를 파싱해 AST를 반환한다.
- 에러가 나면 `null`을 반환한다(`Lox.hadError`가 set되어 다음 단계가 스킵된다).
- 5장의 `AstPrinter`에 연결하면 파싱 결과를 눈으로 확인할 수 있다.

**200줄도 안 되는 코드로 우선순위와 결합 법칙까지 정확히 처리하는 파서가 완성된다.** 다음 장에서는 이 AST를 실제로 **평가**한다.
