# 12장 · Classes (클래스)

11장까지 Lox는 함수와 클로저를 갖춘 절차적 언어다. 이 장에서 **클래스**를 올려 객체지향 언어로 만든다. 클래스 선언, 인스턴스 생성, 필드(프로퍼티), 메서드, `this`, 생성자(`init`)까지 — 상속만 빼고(13장) OOP의 뼈대를 다 세운다.

---

## 12.1 왜 클래스인가

객체에 동작을 붙이는 방식은 크게 셋이다.

- **클래스** (C++, Java, C#, …): 인스턴스는 상태를, 클래스는 동작(메서드)을 가진다. 메서드 호출은 인스턴스의 클래스에서 메서드를 찾아 디스패치한다.
- **프로토타입** (JavaScript, Lua, Self): 클래스가 없고 객체가 직접 상태와 동작을 들고, 다른 객체에 위임(delegate)한다.
- **멀티메서드** (CLOS, Julia): 디스패치를 한 객체가 아니라 인자 전체로 한다.

Lox는 가장 익숙한 **클래스**를 택한다. 단순한 클래스 기반 OOP를 트리 워킹으로 어떻게 구현하는지 보는 게 목표다.

---

## 12.2 클래스 선언 (Class Declarations)

```
declaration → classDecl | funDecl | varDecl | statement ;
classDecl   → "class" IDENTIFIER "{" function* "}" ;
```

클래스 본문은 메서드(이름·매개변수·본문이 있지만 `fun` 키워드는 없는 함수)의 나열이다. 10장의 `function` 규칙을 그대로 재사용한다.

```java
private Stmt classDeclaration() {
  Token name = consume(IDENTIFIER, "Expect class name.");
  consume(LEFT_BRACE, "Expect '{' before class body.");

  List<Stmt.Function> methods = new ArrayList<>();
  while (!check(RIGHT_BRACE) && !isAtEnd()) {
    methods.add(function("method"));     // 메서드는 'fun' 없는 함수
  }

  consume(RIGHT_BRACE, "Expect '}' after class body.");
  return new Stmt.Class(name, methods);
}
```

새 AST 노드:

```java
"Class : Token name, List<Stmt.Function> methods",
```

런타임에서 클래스 선언을 실행하면 **런타임 표현인 `LoxClass`**를 만들어 이름에 묶는다. 이름을 **먼저 정의(null)했다가 나중에 할당**하는 2단계가 핵심이다 — 그래야 클래스 본문(메서드) 안에서 자기 클래스 이름을 참조할 수 있다.

```java
@Override
public Void visitClassStmt(Stmt.Class stmt) {
  environment.define(stmt.name.lexeme, null);   // 1) 먼저 이름만 등록
  // ... 메서드들로 LoxClass 생성 ...
  LoxClass klass = new LoxClass(stmt.name.lexeme, methods);
  environment.assign(stmt.name, klass);         // 2) 완성된 클래스를 할당
  return null;
}
```

---

## 12.3 인스턴스 생성 (Instances)

클래스 이름을 함수처럼 **호출**하면 인스턴스가 생긴다: `Bagel()`. 그러려면 `LoxClass`가 `LoxCallable`이어야 한다.

```java
class LoxClass implements LoxCallable {
  final String name;
  LoxClass(String name) { this.name = name; }

  @Override public String toString() { return name; }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    LoxInstance instance = new LoxInstance(this);   // 호출 = 인스턴스 생성
    return instance;
  }

  @Override public int arity() { return 0; }        // 생성자 없으면 인자 0개
}
```

인스턴스는 자기 클래스를 가리키고, 상태(필드)를 맵으로 들고 있다.

```java
class LoxInstance {
  private LoxClass klass;
  private final Map<String, Object> fields = new HashMap<>();

  LoxInstance(LoxClass klass) { this.klass = klass; }

  @Override public String toString() { return klass.name + " instance"; }
}
```

이로써 `print Bagel;`은 `Bagel`, `print Bagel();`은 `Bagel instance`를 찍는다.

---

## 12.4 프로퍼티 (Properties)

`instance.field` 문법이다. 점(`.`)은 호출 `()`과 같은 우선순위로 묶이며 **좌결합**으로 연쇄된다(`a.b.c`, `a.b().c`).

```
call → primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
```

```java
private Expr call() {
  Expr expr = primary();
  while (true) {
    if (match(LEFT_PAREN)) {
      expr = finishCall(expr);
    } else if (match(DOT)) {
      Token name = consume(IDENTIFIER, "Expect property name after '.'.");
      expr = new Expr.Get(expr, name);          // 새 노드: Get
    } else {
      break;
    }
  }
  return expr;
}
```

### 읽기 — Expr.Get

```java
@Override
public Object visitGetExpr(Expr.Get expr) {
  Object object = evaluate(expr.object);
  if (object instanceof LoxInstance) {
    return ((LoxInstance) object).get(expr.name);
  }
  throw new RuntimeError(expr.name, "Only instances have properties.");
}
```

### 쓰기 — Expr.Set

`a.b = c`는 할당이다. 할당 파싱에서 좌변이 `Expr.Get`이면 `Expr.Set`으로 변환한다(8장에서 `Expr.Variable`을 `Expr.Assign`으로 바꾼 것과 같은 수법).

```java
private Expr assignment() {
  Expr expr = or();
  if (match(EQUAL)) {
    Token equals = previous();
    Expr value = assignment();
    if (expr instanceof Expr.Variable) {
      return new Expr.Assign(((Expr.Variable) expr).name, value);
    } else if (expr instanceof Expr.Get) {            // 추가
      Expr.Get get = (Expr.Get) expr;
      return new Expr.Set(get.object, get.name, value);
    }
    error(equals, "Invalid assignment target.");
  }
  return expr;
}
```

Set의 평가 순서에 주의: **객체를 먼저, 값을 나중에** 평가한다.

```java
@Override
public Object visitSetExpr(Expr.Set expr) {
  Object object = evaluate(expr.object);
  if (!(object instanceof LoxInstance)) {
    throw new RuntimeError(expr.name, "Only instances have fields.");
  }
  Object value = evaluate(expr.value);
  ((LoxInstance) object).set(expr.name, value);
  return value;
}
```

필드 저장·조회는 인스턴스의 맵에 그대로 들어간다. 없는 필드를 읽으면 런타임 에러다(이건 곧 메서드 조회와 합쳐진다).

```java
Object get(Token name) {
  if (fields.containsKey(name.lexeme)) {
    return fields.get(name.lexeme);
  }
  throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
}

void set(Token name, Object value) {
  fields.put(name.lexeme, value);     // Lox는 새 필드를 자유롭게 추가 가능
}
```

---

## 12.5 메서드 (Methods)

메서드는 클래스에 저장되고, 인스턴스를 통해 접근한다. `LoxClass`가 메서드 맵을 들고, 인스턴스의 `get`이 **필드 먼저, 없으면 메서드**를 찾는다(필드가 메서드를 가린다).

```java
// LoxClass
private final Map<String, LoxFunction> methods;
LoxFunction findMethod(String name) {
  if (methods.containsKey(name)) return methods.get(name);
  return null;
}

// LoxInstance.get
Object get(Token name) {
  if (fields.containsKey(name.lexeme)) return fields.get(name.lexeme);

  LoxFunction method = klass.findMethod(name.lexeme);
  if (method != null) return method.bind(this);     // this 바인딩 (12.6)

  throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
}
```

인터프리터의 `visitClassStmt`는 메서드 선언들을 `LoxFunction`으로 감싸 맵으로 만든다.

```java
Map<String, LoxFunction> methods = new HashMap<>();
for (Stmt.Function method : stmt.methods) {
  LoxFunction function = new LoxFunction(method, environment, /*isInitializer*/ false);
  methods.put(method.name.lexeme, function);
}
LoxClass klass = new LoxClass(stmt.name.lexeme, methods);
```

`cake.taste`는 **그 자체로 값**(바인딩된 메서드)이다. `cake.taste()`는 그 값을 호출하는 것이다 — 둘은 분리돼 있다.

---

## 12.6 this

메서드 본문에서 자기 인스턴스를 가리키는 `this`다. 핵심 아이디어: **메서드를 인스턴스에 "바인딩"한다** — `this`를 담은 환경으로 메서드를 한 겹 감싼 새 클로저를 만든다.

```java
// LoxFunction
LoxFunction bind(LoxInstance instance) {
  Environment environment = new Environment(closure);
  environment.define("this", instance);       // this를 클로저에 주입
  return new LoxFunction(declaration, environment, isInitializer);
}
```

`instance.method`로 메서드를 꺼낼 때마다 `bind`가 호출돼, 그 인스턴스에 `this`가 묶인 새 `LoxFunction`이 반환된다. 메서드 본문 안의 `this`는 이 환경에서 찾는다.

파서: `this`는 `primary`에서 `Expr.This`로 받는다. 평가는 변수 조회와 똑같다(이름이 `this`일 뿐).

```java
@Override
public Object visitThisExpr(Expr.This expr) {
  return lookUpVariable(expr.keyword, expr);
}
```

### Resolver가 this를 위한 스코프를 깐다

`this`가 올바른 거리로 해소되도록, Resolver는 클래스의 메서드들을 감싸는 **가짜 스코프**를 만들어 `this`를 선언해 둔다. 또 `this`가 클래스 밖에서 쓰이면 정적 에러로 잡는다.

```java
private enum ClassType { NONE, CLASS }
private ClassType currentClass = ClassType.NONE;

@Override
public Void visitClassStmt(Stmt.Class stmt) {
  ClassType enclosingClass = currentClass;
  currentClass = ClassType.CLASS;

  declare(stmt.name); define(stmt.name);

  beginScope();
  scopes.peek().put("this", true);        // this 를 스코프에 심는다

  for (Stmt.Function method : stmt.methods) {
    resolveFunction(method, FunctionType.METHOD);
  }

  endScope();
  currentClass = enclosingClass;
  return null;
}

@Override
public Void visitThisExpr(Expr.This expr) {
  if (currentClass == ClassType.NONE) {
    Lox.error(expr.keyword, "Can't use 'this' outside of a class.");
    return null;
  }
  resolveLocal(expr, expr.keyword);
  return null;
}
```

런타임에서 메서드를 만들 때의 `closure` 환경이 바로 이 `this` 스코프에 대응한다.

---

## 12.7 생성자와 초기화 (init)

`init`이라는 이름의 메서드가 생성자다. 인스턴스를 만들 때 자동으로 불리고, 인자를 받는다.

```java
// LoxClass.call
public Object call(Interpreter interpreter, List<Object> arguments) {
  LoxInstance instance = new LoxInstance(this);
  LoxFunction initializer = findMethod("init");
  if (initializer != null) {
    initializer.bind(instance).call(interpreter, arguments);   // 생성자 실행
  }
  return instance;
}

public int arity() {
  LoxFunction initializer = findMethod("init");
  return initializer == null ? 0 : initializer.arity();        // 인자 수 = init의 인자 수
}
```

`init`에는 두 가지 특수 규칙이 있다.

**1) `init`은 항상 `this`를 반환한다.** 명시적 `return` 없이 끝나도, 심지어 본문에서 `return;`을 해도 인스턴스를 돌려준다. 그래서 `LoxFunction`에 `isInitializer` 플래그를 둔다.

```java
public Object call(Interpreter interpreter, List<Object> arguments) {
  Environment environment = new Environment(closure);
  // ... 매개변수 바인딩 ...
  try {
    interpreter.executeBlock(declaration.body, environment);
  } catch (Return returnValue) {
    if (isInitializer) return closure.getAt(0, "this");   // return; 도 this
    return returnValue.value;
  }
  if (isInitializer) return closure.getAt(0, "this");     // 끝까지 가도 this
  return null;
}
```

**2) `init` 안에서 값을 반환할 수 없다.** `return 1;`은 Resolver가 정적 에러로 막는다.

```java
private enum FunctionType { NONE, FUNCTION, INITIALIZER, METHOD }

// resolveFunction에서 init이면 INITIALIZER로 표시
// visitReturnStmt에서:
if (stmt.value != null) {
  if (currentFunction == FunctionType.INITIALIZER) {
    Lox.error(stmt.keyword, "Can't return a value from an initializer.");
  }
  resolve(stmt.value);
}
```

---

**이제 Lox는 클래스·인스턴스·필드·메서드·`this`·생성자를 갖췄다.** 남은 한 조각은 클래스끼리 동작을 물려받는 **상속**이다(13장).
