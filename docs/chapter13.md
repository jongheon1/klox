# 13장 · Inheritance (상속)

12장에서 클래스를 세웠다. 이제 클래스가 다른 클래스의 동작을 **물려받게** 한다. 상위클래스 지정(`<`), 메서드 상속, 그리고 가장 까다로운 `super` 호출까지가 이 장의 내용이다. 이것으로 jlox가 완성된다.

---

## 13.1 상위클래스 (Superclasses)

Ruby 스타일로 `<` 뒤에 상위클래스를 적는다.

```
classDecl → "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}" ;
```

```java
private Stmt classDeclaration() {
  Token name = consume(IDENTIFIER, "Expect class name.");

  Expr.Variable superclass = null;
  if (match(LESS)) {
    consume(IDENTIFIER, "Expect superclass name.");
    superclass = new Expr.Variable(previous());     // 상위클래스는 변수 참조
  }

  consume(LEFT_BRACE, "Expect '{' before class body.");
  // ... 메서드 파싱 ...
  return new Stmt.Class(name, superclass, methods);
}
```

상위클래스를 **`Expr.Variable`**로 둔 게 포인트다. 클래스 이름은 런타임에 변수로 평가되므로, 상속도 "그 변수를 평가해 클래스 객체를 얻는" 일이 된다. 평가 결과가 진짜 클래스인지 런타임에 확인한다.

```java
@Override
public Void visitClassStmt(Stmt.Class stmt) {
  Object superclass = null;
  if (stmt.superclass != null) {
    superclass = evaluate(stmt.superclass);
    if (!(superclass instanceof LoxClass)) {
      throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
    }
  }
  // ...
}
```

`class Oops < Oops {}`처럼 자기 자신을 상속하는 건 Resolver가 정적으로 막는다.

```java
if (stmt.superclass != null &&
    stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
  Lox.error(stmt.superclass.name, "A class can't inherit from itself.");
}
```

---

## 13.2 메서드 상속 (Inheriting Methods)

`LoxClass`에 상위클래스 참조를 더하면 끝이다. 메서드를 못 찾으면 위로 거슬러 올라간다.

```java
class LoxClass implements LoxCallable {
  final String name;
  final LoxClass superclass;                    // 추가
  private final Map<String, LoxFunction> methods;

  LoxFunction findMethod(String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }
    if (superclass != null) {
      return superclass.findMethod(name);       // 상위로 위임
    }
    return null;
  }
}
```

이 한 줄짜리 재귀로 메서드 상속, 오버라이드(하위가 먼저 발견되므로 우선), 상속된 `init`(상위의 생성자가 `findMethod("init")`로 잡힘)까지 전부 처리된다.

---

## 13.3 super

오버라이드한 메서드 안에서 **상위클래스의** 메서드를 부르는 `super.method()`다. 여기엔 미묘한 함정이 있다.

```
class A {           method() { print "A"; } }
class B < A { method() { print "B"; } test() { super.method(); } }
class C < B {}

C().test();         // 무엇이 출력되나?
```

`C().test()`에서 `test`는 B에 정의돼 있다. `super.method()`의 `super`는 **`test`가 정의된 클래스(B)의 상위클래스(A)**를 가리켜야 한다 → `A`. 만약 "인스턴스(C)의 상위클래스"로 잘못 구현하면 B가 나와 무한 루프·오작동이 된다. 즉 **`super`는 수신자(C)가 아니라 메서드가 적힌 클래스(B)를 기준**으로 한다.

### 해결: super를 클로저에 담는다

`this`와 똑같은 수법이다. 클래스의 메서드들을 만들 때, **상위클래스를 담은 환경**으로 한 겹 더 감싼다. 그러면 메서드 본문은 자기가 정의된 클래스의 상위클래스를 클로저로 붙잡는다.

```java
@Override
public Void visitClassStmt(Stmt.Class stmt) {
  Object superclass = /* ... 평가 + 검사 ... */;

  environment.define(stmt.name.lexeme, null);

  if (stmt.superclass != null) {
    environment = new Environment(environment);
    environment.define("super", superclass);     // super 스코프
  }

  Map<String, LoxFunction> methods = new HashMap<>();
  for (Stmt.Function method : stmt.methods) {     // 메서드들이 이 환경을 클로저로 가짐
    LoxFunction function = new LoxFunction(method, environment,
        method.name.lexeme.equals("init"));
    methods.put(method.name.lexeme, function);
  }

  LoxClass klass = new LoxClass(stmt.name.lexeme, (LoxClass) superclass, methods);

  if (stmt.superclass != null) {
    environment = environment.enclosing;          // super 스코프 닫기
  }

  environment.assign(stmt.name, klass);
  return null;
}
```

환경이 두 겹이 된다: 바깥에 `super`, 그 안에 (메서드 호출 시 `bind`로 생기는) `this`. 그래서 메서드 본문에서 **`this`는 거리 d, `super`는 거리 d−1** 위치에 있다(아래 그림).

```
메서드 본문 환경
   │  (지역 변수)
   ▼
this 환경         ← bind가 만든, "this" 보유   (super보다 1 가까움)
   │
   ▼
super 환경        ← "super" = 상위클래스        (this보다 1 멈)
   │
   ▼
클래스가 선언된 환경 …
```

### super 평가

`Expr.Super`를 평가할 때, Resolver가 계산한 거리로 `super`(상위클래스)와 `this`(인스턴스)를 꺼내, 상위클래스에서 메서드를 찾아 **그 인스턴스에 바인딩**한다.

```java
@Override
public Object visitSuperExpr(Expr.Super expr) {
  int distance = locals.get(expr);
  LoxClass superclass = (LoxClass) environment.getAt(distance, "super");
  LoxInstance object = (LoxInstance) environment.getAt(distance - 1, "this");
  //                                                   └ this는 super보다 한 칸 가깝다

  LoxFunction method = superclass.findMethod(expr.method.lexeme);
  if (method == null) {
    throw new RuntimeError(expr.method,
        "Undefined property '" + expr.method.lexeme + "'.");
  }
  return method.bind(object);     // 상위 메서드를, 원래 인스턴스(this)에 바인딩
}
```

`object`를 `this`로 다시 묶는 게 중요하다. 상위클래스의 메서드를 부르되 `this`는 여전히 **원래 인스턴스**여야 하기 때문이다.

파서에서 `super`는 항상 `super.method` 꼴이어야 한다(`super` 단독은 의미 없음).

```java
if (match(SUPER)) {
  Token keyword = previous();
  consume(DOT, "Expect '.' after 'super'.");
  Token method = consume(IDENTIFIER, "Expect superclass method name.");
  return new Expr.Super(keyword, method);
}
```

---

## 13.4 super의 정적 검사

`super`도 Resolver가 거리를 해소하고, 잘못된 위치를 정적으로 막는다. `ClassType`에 `SUBCLASS`를 더해 "상위클래스가 있는 클래스 안인지"를 추적한다.

```java
private enum ClassType { NONE, CLASS, SUBCLASS }

// visitClassStmt에서 상위클래스가 있으면:
if (stmt.superclass != null) {
  currentClass = ClassType.SUBCLASS;
  resolve(stmt.superclass);
  beginScope();
  scopes.peek().put("super", true);    // super 스코프 (런타임의 super 환경에 대응)
}
// ... this 스코프, 메서드 해소 ...
// 끝나면 super 스코프도 endScope

@Override
public Void visitSuperExpr(Expr.Super expr) {
  if (currentClass == ClassType.NONE) {
    Lox.error(expr.keyword, "Can't use 'super' outside of a class.");
  } else if (currentClass != ClassType.SUBCLASS) {
    Lox.error(expr.keyword, "Can't use 'super' in a class with no superclass.");
  }
  resolveLocal(expr, expr.keyword);
  return null;
}
```

Resolver가 까는 `super`/`this` 스코프 두 겹이, 런타임에서 인터프리터가 만드는 `super`/`this` 환경 두 겹과 정확히 1:1로 대응한다 — 그래서 거리 계산이 들어맞는다.

---

**이것으로 jlox(트리 워킹 인터프리터)가 완성됐다.** 스캐너·파서·Resolver·인터프리터를 거쳐, 변수·함수·클로저·클래스·상속까지 도는 완전한 동적 언어다. 2부(clox)는 같은 언어를 바이트코드 VM으로 다시 구현해 속도를 끌어올린다.
