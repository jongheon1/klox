# 8장 · Statements and State (문장과 상태)

7장까지의 인터프리터는 표현식 하나를 계산해 값을 내는 **계산기**였다. 이제 언어다워지려면 두 가지가 필요하다.

- **문장(statement)**: 값을 내는 게 아니라 **효과(effect)**를 내는 단위. 화면 출력, 변수 선언 등.
- **상태(state)**: 프로그램이 기억하는 데이터. 변수에 값을 담고, 나중에 꺼내 쓴다.

이 장에서 `print` 문, 변수 선언, 할당, 그리고 블록 스코프를 차례로 붙인다.

---

## 8.1 문장 (Statements)

표현식과 문장은 문법적으로 **다른 종류**다. 그래서 `Expr`와 별개로 `Stmt` 트리를 새로 만든다. 처음 추가하는 문장은 두 가지다.

- **표현식 문장(expression statement)**: 표현식 뒤에 `;`. 부수 효과만 쓰고 값은 버린다. (`eat();` 같은 함수 호출이 전형적)
- **print 문(print statement)**: `print` 뒤 표현식을 평가해 출력. (원래는 라이브러리 함수여야 하지만, 함수가 아직 없으니 임시로 문장으로 박아둔다.)

```
program   → statement* EOF ;
statement → exprStmt
          | printStmt ;
exprStmt  → expression ";" ;
printStmt → "print" expression ";" ;
```

`program`은 새로운 시작 규칙이다. 프로그램은 **문장의 나열**이고 파일 끝(`EOF`)에서 끝난다.

### Stmt 트리

`Expr`와 똑같이 GenerateAst로 찍어내되, 별도의 Visitor를 둔다.

```java
abstract class Stmt {
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitPrintStmt(Print stmt);
  }

  static class Expression extends Stmt {
    final Expr expression;
    Expression(Expr expression) { this.expression = expression; }
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
  }

  static class Print extends Stmt {
    final Expr expression;
    Print(Expr expression) { this.expression = expression; }
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }
  }

  abstract <R> R accept(Visitor<R> visitor);
}
```

### 문장 파싱

`parse()`가 이제 표현식 하나가 아니라 **문장 리스트**를 돌려준다.

```java
List<Stmt> parse() {
  List<Stmt> statements = new ArrayList<>();
  while (!isAtEnd()) {
    statements.add(statement());
  }
  return statements;
}

private Stmt statement() {
  if (match(PRINT)) return printStatement();
  return expressionStatement();
}

private Stmt printStatement() {
  Expr value = expression();
  consume(SEMICOLON, "Expect ';' after value.");
  return new Stmt.Print(value);
}

private Stmt expressionStatement() {
  Expr expr = expression();
  consume(SEMICOLON, "Expect ';' after expression.");
  return new Stmt.Expression(expr);
}
```

`statement()`는 첫 토큰을 보고 어떤 문장인지 고른다. `print`면 print 문, 아니면 표현식 문장으로 떨어진다. 둘 다 끝에 `;`를 강제한다.

### 문장 실행

인터프리터가 `Stmt.Visitor<Void>`를 추가로 구현한다. 문장은 값을 내지 않으므로 반환 타입이 `Void`다.

```java
void interpret(List<Stmt> statements) {
  try {
    for (Stmt statement : statements) {
      execute(statement);
    }
  } catch (RuntimeError error) {
    Lox.runtimeError(error);
  }
}

private void execute(Stmt stmt) {
  stmt.accept(this);
}

@Override
public Void visitExpressionStmt(Stmt.Expression stmt) {
  evaluate(stmt.expression);    // 평가만 하고 값은 버린다
  return null;
}

@Override
public Void visitPrintStmt(Stmt.Print stmt) {
  Object value = evaluate(stmt.expression);
  System.out.println(stringify(value));
  return null;
}
```

`execute()`는 표현식의 `evaluate()`에 대응하는 문장용 헬퍼다. 이제 인터프리터는 표현식을 평가하는 `evaluate`와 문장을 실행하는 `execute`, 두 갈래를 갖는다.

---

## 8.2 전역 변수 (Global Variables)

변수를 쓰려면 두 가지가 필요하다.

- **변수 선언문**: `var name = expr;` — 새 이름을 만들어 값에 묶는다.
- **변수 표현식**: `name` — 그 이름의 값을 꺼낸다.

### 선언문은 아무 데나 못 온다

`if (mood) var beverage = "tea";` 같은 코드는 헷갈린다. `beverage`의 스코프가 어디까지인가? 그래서 선언문을 일반 문장과 분리하고, **선언이 허용되는 자리**를 따로 정한다. 새 규칙 `declaration`이 프로그램의 시작점이 된다.

```
program     → declaration* EOF ;
declaration → varDecl
            | statement ;
varDecl     → "var" IDENTIFIER ( "=" expression )? ";" ;
statement   → exprStmt
            | printStmt ;
```

`declaration`은 선언이거나 그냥 문장이다. 블록·if 본문 등 "한 문장"만 오는 자리에는 `statement`만 허용되므로, `if`의 본문에 곧바로 `var`를 둘 수 없다.

초기화식은 선택(`( "=" expression )?`)이다. `var a;`처럼 생략하면 `nil`로 초기화된다.

변수 표현식은 `primary`에 한 줄 추가한다.

```
primary → "true" | "false" | "nil"
        | NUMBER | STRING
        | "(" expression ")"
        | IDENTIFIER ;
```

### 파싱

`parse()`의 루프가 `statement()` 대신 `declaration()`을 부른다. `declaration()`은 파서의 **동기화 지점**이기도 하다. 문장 파싱 중 에러가 나면 여기서 잡아 `synchronize()`를 부르고 다음 선언으로 넘어간다.

```java
private Stmt declaration() {
  try {
    if (match(VAR)) return varDeclaration();
    return statement();
  } catch (ParseError error) {
    synchronize();
    return null;
  }
}

private Stmt varDeclaration() {
  Token name = consume(IDENTIFIER, "Expect variable name.");

  Expr initializer = null;
  if (match(EQUAL)) {
    initializer = expression();
  }

  consume(SEMICOLON, "Expect ';' after variable declaration.");
  return new Stmt.Var(name, initializer);
}

private Expr primary() {
  // ... 기존 리터럴·그룹핑 ...
  if (match(IDENTIFIER)) {
    return new Expr.Variable(previous());
  }
  // ...
}
```

### Environment — 상태를 담는 곳

변수 이름 → 값의 매핑이 곧 상태다. 이걸 `Environment` 클래스에 담는다.

```java
class Environment {
  private final Map<String, Object> values = new HashMap<>();

  void define(String name, Object value) {
    values.put(name, value);
  }

  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }
    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }
}
```

설계 결정 두 가지를 눈여겨보자.

- `define`은 **이미 있는 이름이어도 그냥 덮어쓴다**. 즉 `var a = 1; var a = 2;`를 전역에서 허용한다. (REPL 편의를 위한 선택.)
- `get`은 **없는 변수를 런타임 에러**로 처리한다. 컴파일 에러로 하면 재귀 함수를 정의하기 어려워지므로(아직 정의 안 된 자기 이름을 본문에서 참조), 참조 자체는 허용하되 *실행*해서 닿았을 때만 에러로 친다.

인터프리터는 `Environment` 필드를 하나 들고, 두 노드를 처리한다.

```java
private Environment environment = new Environment();

@Override
public Void visitVarStmt(Stmt.Var stmt) {
  Object value = null;
  if (stmt.initializer != null) {
    value = evaluate(stmt.initializer);
  }
  environment.define(stmt.name.lexeme, value);
  return null;
}

@Override
public Object visitVariableExpr(Expr.Variable expr) {
  return environment.get(expr.name);
}
```

---

## 8.3 할당 (Assignment)

`a = 3;`처럼 이미 있는 변수에 새 값을 넣는다. 할당은 **표현식**이다(C 계열 전통). 따라서 값을 내며, `a = b = 3`처럼 이어 쓸 수 있다.

```
expression → assignment ;
assignment → IDENTIFIER "=" assignment
           | equality ;
```

할당은 **우결합**이라 오른쪽에서 다시 `assignment`를 부른다.

### 파싱의 묘수 — l-value

문제는 파서가 `=`를 보기 전까지는 왼쪽이 할당 대상인지 모른다는 것이다. `a = 1`과 `a + 1`은 첫 토큰이 같다. 게다가 `a.b.c = 1`처럼 왼쪽이 길 수도 있다.

해법: **일단 왼쪽을 보통 표현식으로 파싱**한 뒤, `=`가 나오면 그 결과가 유효한 할당 대상(`Expr.Variable`)인지 검사한다.

```java
private Expr assignment() {
  Expr expr = equality();              // 왼쪽을 우선 표현식으로 파싱

  if (match(EQUAL)) {
    Token equals = previous();
    Expr value = assignment();         // 우결합 → 재귀

    if (expr instanceof Expr.Variable) {
      Token name = ((Expr.Variable)expr).name;
      return new Expr.Assign(name, value);
    }

    error(equals, "Invalid assignment target.");   // 1 = 2 같은 경우
  }

  return expr;
}
```

`a + b = c`에서는 왼쪽이 `Expr.Binary`라 `instanceof` 검사에 걸려 에러를 보고한다. 단, 여기서 에러는 **던지지 않고 보고만** 한다 — 패닉 모드로 동기화할 만큼 심각하지 않기 때문이다.

### 실행

```java
@Override
public Object visitAssignExpr(Expr.Assign expr) {
  Object value = evaluate(expr.value);
  environment.assign(expr.name, value);
  return value;                        // 할당식 자체의 값
}
```

`Environment`에는 `assign`을 추가한다. `define`과 달리 **없는 변수면 에러**다 — 할당은 새 변수를 만들지 않는다.

```java
void assign(Token name, Object value) {
  if (values.containsKey(name.lexeme)) {
    values.put(name.lexeme, value);
    return;
  }
  throw new RuntimeError(name,
      "Undefined variable '" + name.lexeme + "'.");
}
```

---

## 8.4 스코프 (Scope)

블록 `{ ... }`은 자기만의 스코프를 연다. 안에서 선언한 변수는 블록이 끝나면 사라지고, 같은 이름이 바깥에 있으면 **가린다(shadowing)**.

```
statement → exprStmt
          | printStmt
          | block ;
block     → "{" declaration* "}" ;
```

```java
private Stmt statement() {
  if (match(PRINT)) return printStatement();
  if (match(LEFT_BRACE)) return new Stmt.Block(block());
  return expressionStatement();
}

private List<Stmt> block() {
  List<Stmt> statements = new ArrayList<>();
  while (!check(RIGHT_BRACE) && !isAtEnd()) {
    statements.add(declaration());
  }
  consume(RIGHT_BRACE, "Expect '}' after block.");
  return statements;
}
```

### 환경 체인 (Cactus Stack)

스코프 중첩을 표현하려고 `Environment`마다 **바깥 환경(enclosing)**을 가리키게 한다. 변수를 찾을 때 현재 환경에 없으면 바깥으로, 또 없으면 더 바깥으로 거슬러 올라간다.

```java
class Environment {
  final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();

  Environment() {                       // 전역: 바깥이 없음
    enclosing = null;
  }
  Environment(Environment enclosing) {  // 블록: 바깥을 품음
    this.enclosing = enclosing;
  }

  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }
    if (enclosing != null) return enclosing.get(name);   // 바깥으로 위임

    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }

  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }
    if (enclosing != null) {
      enclosing.assign(name, value);     // 바깥으로 위임
      return;
    }
    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }
}
```

`get`/`assign`은 바깥으로 위임하지만, `define`은 **항상 현재 환경**에 새로 넣는다. 그래서 안쪽 블록의 `var a`가 바깥 `a`를 덮지 않고 가리는(shadow) 것이다.

### 블록 실행

블록에 들어가면 새 환경을 만들어 잠시 끼워 넣고, 빠져나오면 이전 환경으로 되돌린다. `finally`로 되돌려야 런타임 에러가 나도 환경이 새지 않는다.

```java
@Override
public Void visitBlockStmt(Stmt.Block stmt) {
  executeBlock(stmt.statements, new Environment(environment));
  return null;
}

void executeBlock(List<Stmt> statements, Environment environment) {
  Environment previous = this.environment;
  try {
    this.environment = environment;
    for (Stmt statement : statements) {
      execute(statement);
    }
  } finally {
    this.environment = previous;        // 반드시 원복
  }
}
```

### 종합 예시 — 가림(shadowing)이 동작하는 모습

```
var a = "global";
{
  var a = "block";
  print a;     // block
}
print a;       // global
```

환경 체인의 변화를 따라가 보자.

```
[전역]  a = "global"

블록 진입: new Environment(전역)
  [블록]   a = "block"   →  define는 현재(블록) 환경에 새로 넣는다
  [전역]   a = "global"  (그대로 살아 있음, 가려졌을 뿐)

  print a → 블록 환경에서 a 발견 → "block"

블록 탈출: environment = 전역 으로 원복
  [블록] 환경은 통째로 버려진다

print a → 전역에서 a 발견 → "global"
```

핵심은 `define`이 항상 **가장 안쪽 환경**에 쓰고, `get`은 **안쪽부터** 찾는다는 비대칭이다. 이 비대칭 하나가 가림과 스코프 복원을 동시에 만든다.

---

**이제 Lox는 데이터를 기억한다.** 변수·할당·블록 스코프까지 갖췄지만 아직 일직선으로만 실행된다. 9장에서 분기와 반복을 붙여 진짜 프로그래밍 언어로 만든다.
