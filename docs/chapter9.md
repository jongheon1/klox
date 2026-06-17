# 9장 · Control Flow (제어 흐름)

8장의 Lox는 위에서 아래로 한 번 흐르면 끝이다. 같은 코드를 조건에 따라 건너뛰거나 반복할 수 없다. 이 장에서 `if`, 논리 연산자 `and`/`or`, `while`, `for`를 붙여 **제어 흐름**을 완성한다. 이것으로 Lox는 튜링 완전(Turing-complete)해진다.

---

## 9.1 튜링 기계 (잠깐의 이론)

"이 언어로 계산 가능한 건 다 짤 수 있나?"라는 질문의 답이 **튜링 완전성**이다. 어떤 언어가 튜링 기계를 흉내 낼 수 있으면 튜링 완전하다. 그러기 위해 필요한 최소 도구는 놀랍도록 적다.

- 산술
- 약간의 제어 흐름(분기와 반복)
- 임의 크기의 메모리

Lox는 이미 산술과 변수를 갖췄다. 이 장에서 **제어 흐름**만 채우면 문턱을 넘는다.

---

## 9.2 조건부 실행 (Conditional Execution)

`if` 문이다. 조건이 참이면 한 가지를, 거짓이면(있다면) 다른 가지를 실행한다.

```
statement → exprStmt
          | ifStmt
          | printStmt
          | block ;
ifStmt    → "if" "(" expression ")" statement
            ( "else" statement )? ;
```

### 매달린 else (dangling else)

```
if (first) if (second) whenTrue(); else whenFalse();
```

이 `else`는 어느 `if`에 붙나? 문법만으로는 모호하다. 관례는 **가장 가까운 `if`에 붙인다**. 코드로는 따로 처리할 게 없다 — `else`를 만나면 *그 자리에서 바로* 소비하므로, 자연히 가장 안쪽 `if`가 가져간다.

```java
private Stmt ifStatement() {
  consume(LEFT_PAREN, "Expect '(' after 'if'.");
  Expr condition = expression();
  consume(RIGHT_PAREN, "Expect ')' after if condition.");

  Stmt thenBranch = statement();
  Stmt elseBranch = null;
  if (match(ELSE)) {            // else는 보이는 즉시 가장 가까운 if가 가져간다
    elseBranch = statement();
  }

  return new Stmt.If(condition, thenBranch, elseBranch);
}
```

실행은 truthiness로 가지를 고른다.

```java
@Override
public Void visitIfStmt(Stmt.If stmt) {
  if (isTruthy(evaluate(stmt.condition))) {
    execute(stmt.thenBranch);
  } else if (stmt.elseBranch != null) {
    execute(stmt.elseBranch);
  }
  return null;
}
```

자바의 `if`로 Lox의 `if`를 구현한다 — 메타 순환(meta-circular)처럼 보이지만, 트리 워킹 인터프리터의 본질이 그렇다.

---

## 9.3 논리 연산자 (Logical Operators)

`and`와 `or`다. 이 둘이 `if`와 따로 떨어진 절에 있는 이유는 **단락 평가(short-circuit)** 때문이다.

- `false and sideEffect()` — 왼쪽이 거짓이면 오른쪽을 **평가하지 않는다**.
- `true or sideEffect()` — 왼쪽이 참이면 오른쪽을 평가하지 않는다.

즉 두 피연산자를 항상 평가하는 `Expr.Binary`와 동작이 다르다. 그래서 **별도 노드 `Expr.Logical`**을 둔다.

우선순위는 할당보다 높고 `equality`보다 낮다. `or`가 `and`보다 낮다.

```
expression → assignment ;
assignment → IDENTIFIER "=" assignment
           | logic_or ;
logic_or   → logic_and ( "or" logic_and )* ;
logic_and  → equality ( "and" equality )* ;
```

```java
private Expr assignment() {
  Expr expr = or();                    // equality() 였던 자리

  if (match(EQUAL)) { /* ... 8장과 동일 ... */ }
  return expr;
}

private Expr or() {
  Expr expr = and();
  while (match(OR)) {
    Token operator = previous();
    Expr right = and();
    expr = new Expr.Logical(expr, operator, right);
  }
  return expr;
}

private Expr and() {
  Expr expr = equality();
  while (match(AND)) {
    Token operator = previous();
    Expr right = equality();
    expr = new Expr.Logical(expr, operator, right);
  }
  return expr;
}
```

### 값을 반환하는 단락 평가

Lox의 `and`/`or`은 `true`/`false`가 아니라 **피연산자 값 자체**를 돌려준다(Python·Lua처럼). `"hi" or 2`는 `"hi"`, `nil or "default"`는 `"default"`.

```java
@Override
public Object visitLogicalExpr(Expr.Logical expr) {
  Object left = evaluate(expr.left);

  if (expr.operator.type == OR) {
    if (isTruthy(left)) return left;    // or: 왼쪽이 참이면 그걸로 끝
  } else {
    if (!isTruthy(left)) return left;   // and: 왼쪽이 거짓이면 그걸로 끝
  }

  return evaluate(expr.right);          // 여기 닿아야 비로소 오른쪽 평가
}
```

왼쪽으로 결판이 나면 오른쪽 `evaluate`를 **아예 호출하지 않는다**. 이 한 곳에 단락 평가가 응축돼 있다.

---

## 9.4 while 반복

```
statement → exprStmt | ifStmt | printStmt | whileStmt | block ;
whileStmt → "while" "(" expression ")" statement ;
```

```java
private Stmt whileStatement() {
  consume(LEFT_PAREN, "Expect '(' after 'while'.");
  Expr condition = expression();
  consume(RIGHT_PAREN, "Expect ')' after condition.");
  Stmt body = statement();

  return new Stmt.While(condition, body);
}

@Override
public Void visitWhileStmt(Stmt.While stmt) {
  while (isTruthy(evaluate(stmt.condition))) {
    execute(stmt.body);
  }
  return null;
}
```

여기서도 자바의 `while`이 Lox의 `while`을 그대로 돌린다.

---

## 9.5 for 반복 — 디슈가링 (Desugaring)

C 스타일 `for`다.

```
forStmt → "for" "(" ( varDecl | exprStmt | ";" )
                    expression? ";"
                    expression? ")" statement ;
```

세 칸(초기화·조건·증감)이 각각 생략 가능하다. 그런데 `for`는 **새로운 능력이 없다** — `while`로 할 수 있는 걸 더 편하게 쓰게 해줄 뿐이다. 그래서 새 AST 노드를 만들지 않고, 파서가 `for`를 **`while` 트리로 변환**한다. 이걸 디슈가링(desugaring)이라 한다.

```
for (var i = 0; i < 10; i = i + 1) print i;
```

는 다음과 정확히 같은 트리로 바뀐다.

```
{
  var i = 0;
  while (i < 10) {
    print i;
    i = i + 1;
  }
}
```

규칙은 셋이다.

- **증감식**은 본문 *뒤*에 붙인다 → `{ body; increment; }`
- **조건**은 `while`의 조건으로. 생략되면 `true`(무한 루프).
- **초기화식**은 `while` 전체를 감싸는 블록 맨 앞에 둔다 → 루프 변수가 루프 안에서만 살게.

```java
private Stmt forStatement() {
  consume(LEFT_PAREN, "Expect '(' after 'for'.");

  Stmt initializer;                              // 1) 초기화
  if (match(SEMICOLON)) {
    initializer = null;
  } else if (match(VAR)) {
    initializer = varDeclaration();
  } else {
    initializer = expressionStatement();
  }

  Expr condition = null;                         // 2) 조건
  if (!check(SEMICOLON)) {
    condition = expression();
  }
  consume(SEMICOLON, "Expect ';' after loop condition.");

  Expr increment = null;                         // 3) 증감
  if (!check(RIGHT_PAREN)) {
    increment = expression();
  }
  consume(RIGHT_PAREN, "Expect ')' after for clauses.");

  Stmt body = statement();

  // --- 여기부터 디슈가링: 뒤에서 앞으로 감싼다 ---

  if (increment != null) {                       // 본문 뒤에 증감식
    body = new Stmt.Block(Arrays.asList(
        body, new Stmt.Expression(increment)));
  }

  if (condition == null) condition = new Expr.Literal(true);
  body = new Stmt.While(condition, body);        // while로 감싸기

  if (initializer != null) {                     // 초기화로 한 번 더 감싸기
    body = new Stmt.Block(Arrays.asList(initializer, body));
  }

  return body;
}
```

`visitForStmt` 같은 건 **없다**. 인터프리터는 자기가 실행하는 트리가 원래 `for`였는지 `while`이었는지 전혀 모른다. 복잡함을 파서가 흡수하고, 인터프리터는 단순하게 유지된다 — 디슈가링의 핵심 이득이다.

### 종합 예시 — for가 while로 펼쳐져 실행되기까지

```
for (var i = 0; i < 3; i = i + 1) print i;
```

파서가 만드는 최종 트리(블록 → 초기화 + while):

```
Block
├ Var(i = 0)                                  ← 초기화
└ While( i < 3 )
   └ Block
      ├ Print(i)                              ← 원래 본문
      └ Expression( i = i + 1 )               ← 증감식이 뒤에 붙음
```

인터프리터가 이 `while` 트리를 도는 모습:

```
i=0 ┐ 조건 0<3 참 → print 0 → 증감 i=1
i=1 ┤ 조건 1<3 참 → print 1 → 증감 i=2
i=2 ┤ 조건 2<3 참 → print 2 → 증감 i=3
i=3 ┘ 조건 3<3 거짓 → 루프 종료
```

출력:

```
$ klox
0
1
2
```

`i`는 가장 바깥 `Block`이 만든 환경에 선언되므로 루프가 끝나면 사라진다. `for`가 평범한 블록과 while의 조합으로 완전히 환원된 것이다.

---

**이제 Lox는 튜링 완전하다.** 분기와 반복이 생겼으니, 원리상 계산 가능한 모든 것을 표현할 수 있다. 다음 단계는 코드를 재사용 단위로 묶는 **함수**(10장)다.
