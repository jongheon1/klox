# 11장 · Resolving and Binding (해소와 바인딩)

10장에서 클로저가 생겼다. 그런데 클로저의 변수 바인딩이 **항상 옳지는 않다**. 이 장은 그 버그를 드러내고, 인터프리터와 파서 사이에 **의미 분석(semantic analysis) 패스**를 하나 끼워 고친다. 이 Resolver는 12장 이후 클래스·메서드·`this`·`super`의 토대가 된다.

---

## 11.1 정적 스코프와 버그 (The Bug)

Lox는 **정적 스코프(lexical scope)**를 표방한다. 즉 변수가 어느 선언을 가리키는지는 **소스 코드의 위치만으로** 결정돼야 한다 — 프로그램이 어떻게 실행되든 변하지 않아야 한다. 다음 코드를 보자.

```
var a = "global";
{
  fun showA() {
    print a;
  }

  showA();            // 기대: global
  var a = "block";
  showA();            // 기대: global  ← 그러나 10장 인터프리터는 "block"을 찍는다
}
```

`showA`는 선언될 때 둘러싼 스코프에 `a`가 `"global"` 하나뿐이었다. 그러니 **두 번 다 `global`**이어야 정적 스코프다. 그런데 10장 구현은 두 번째에 `block`을 출력한다.

원인은 환경 체인이 **동적으로 자란다**는 데 있다. `var a = "block";`이 실행되면서 `showA`의 클로저 환경(블록 환경)에 새 `a`가 끼어든다. 두 번째 `showA()`가 `a`를 찾을 때, 같은 환경에 더 가까운 `a`("block")가 생겨 버린 것이다. 클로저는 "환경을 가리키는 참조"인데, 그 환경의 **내용물이 시간에 따라 바뀌었다**.

---

## 11.2 의미 분석 — 한 번만 해소하기

문제의 본질: 같은 `a` 표현식이 실행 시점마다 다른 변수로 해소될 수 있다는 것. 해결: **각 변수 사용이 어느 선언을 가리키는지 단 한 번 계산해 고정**한다.

"각 변수는 둘러싼 스코프를 몇 개나 거슬러 올라가야 찾는가"를 세는 일은 코드를 보기만 해도 알 수 있다 — 실행이 필요 없다. 그래서 파싱 후, 인터프리트 전에 AST를 한 번 훑는 **Resolver** 패스를 넣는다.

```
                 11장에서 추가되는 단계
                          │
  Scanner → Parser → [ Resolver ] → Interpreter
   (토큰)    (AST)    (변수 해소)     (실행)
```

Resolver가 하는 일은 딱 하나: 각 변수 사용 `Expr`에 대해 **"몇 단계(hops) 바깥에서 찾으면 되는지"** 그 거리(depth)를 계산해 인터프리터에 알려준다. 인터프리터는 실행 때 그 거리만큼 정확히 올라가 변수를 찾으므로, 나중에 같은 스코프에 끼어든 변수에 흔들리지 않는다.

---

## 11.3 Resolver 클래스

Resolver는 인터프리터처럼 `Expr.Visitor`/`Stmt.Visitor`를 구현하지만 **값을 만들지 않는다**(`Void`). 부수 효과 없이 트리를 걸으며 스코프만 추적한다. 대부분의 노드는 자식으로 재귀할 뿐이고, **실제로 의미가 있는 노드는 넷**이다.

- **블록**: 스코프를 만들고 닫는다.
- **변수 선언**: 현재 스코프에 이름을 추가한다.
- **변수 사용 / 할당**: 그 이름을 해소한다(거리 계산).
- **함수 선언**: 새 스코프를 만들고 매개변수를 추가한다.

스코프 스택은 "블록 스코프 → 이름이 정의 완료됐는지(Boolean)"의 맵을 쌓은 것이다. 전역은 일부러 추적하지 않는다(전역은 더 동적이므로).

```java
private final Stack<Map<String, Boolean>> scopes = new Stack<>();

private void beginScope() { scopes.push(new HashMap<>()); }
private void endScope()   { scopes.pop(); }
```

### 두 단계: 선언(declare)과 정의(define)

핵심 트릭은 변수 선언을 **두 단계**로 쪼개는 것이다. `var a = a;`처럼 초기화식이 자기 자신을 참조하는 경우를 잡기 위해서다.

```java
private void declare(Token name) {       // 1) "이름이 존재한다, 그러나 아직 못 쓴다"
  if (scopes.isEmpty()) return;
  Map<String, Boolean> scope = scopes.peek();
  if (scope.containsKey(name.lexeme)) {  // 같은 스코프 중복 선언도 여기서 차단
    Lox.error(name, "Already a variable with this name in this scope.");
  }
  scope.put(name.lexeme, false);         // false = 선언됐지만 초기화 전
}

private void define(Token name) {        // 2) "이제 초기화 끝, 사용 가능"
  if (scopes.isEmpty()) return;
  scopes.peek().put(name.lexeme, true);  // true = 준비됨
}
```

변수 선언을 해소할 때 `declare` → (초기화식 해소) → `define` 순서로 한다.

```java
@Override
public Void visitVarStmt(Stmt.Var stmt) {
  declare(stmt.name);
  if (stmt.initializer != null) {
    resolve(stmt.initializer);     // 이 사이에 변수는 false 상태
  }
  define(stmt.name);
  return null;
}
```

그래서 초기화식 안에서 같은 이름을 쓰면(`var a = a;`), 그 `a`는 "선언됐지만 false" 상태로 발견돼 에러가 된다.

```java
@Override
public Void visitVariableExpr(Expr.Variable expr) {
  if (!scopes.isEmpty() &&
      scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
    Lox.error(expr.name, "Can't read local variable in its own initializer.");
  }
  resolveLocal(expr, expr.name);
  return null;
}
```

### 거리 계산 (resolveLocal)

이름이 발견되는 가장 가까운 스코프까지 **몇 단계** 올라가는지 센다. 그 거리를 인터프리터에 넘긴다.

```java
private void resolveLocal(Expr expr, Token name) {
  for (int i = scopes.size() - 1; i >= 0; i--) {   // 안쪽 스코프부터 바깥으로
    if (scopes.get(i).containsKey(name.lexeme)) {
      interpreter.resolve(expr, scopes.size() - 1 - i);   // 거리 = 몇 칸 올라갔나
      return;
    }
  }
  // 어디서도 못 찾으면 전역으로 간주 — resolve 호출 안 함
}
```

### 함수 해소

함수는 스코프를 만들고 매개변수를 그 안에 declare+define한 뒤 본문을 해소한다. 선언과 본문을 **즉시** 해소한다는 점이 인터프리터(나중에 호출될 때 실행)와 다르다.

```java
@Override
public Void visitFunctionStmt(Stmt.Function stmt) {
  declare(stmt.name);
  define(stmt.name);          // 이름을 먼저 define — 재귀를 위해(챌린지 11-1)
  resolveFunction(stmt);
  return null;
}

private void resolveFunction(Stmt.Function function) {
  beginScope();
  for (Token param : function.params) {
    declare(param);
    define(param);
  }
  resolve(function.body);     // 매개변수와 같은 스코프에서 본문 해소
  endScope();
}
```

함수 이름을 본문 해소 *전에* `define`하는 것에 주목하라. 변수와 달리 함수는 자기 이름을 본문에서 참조해도 안전하다(재귀). 그 이유가 챌린지 11-1의 답이다.

---

## 11.4 인터프리터와 결합

Resolver가 계산한 거리를 인터프리터가 저장하고 쓴다. 저장소는 표현식 → 거리 맵이다.

```java
private final Map<Expr, Integer> locals = new HashMap<>();

void resolve(Expr expr, int depth) {     // Resolver가 호출
  locals.put(expr, depth);
}
```

변수를 읽을 때, 거리가 있으면 그만큼 환경 체인을 올라가 **이름으로**(여전히 맵 조회) 찾는다. 거리가 없으면 전역이다.

```java
private Object lookUpVariable(Token name, Expr expr) {
  Integer distance = locals.get(expr);
  if (distance != null) {
    return environment.getAt(distance, name.lexeme);   // 정확히 distance칸 위
  } else {
    return globals.get(name);
  }
}
```

`getAt`은 부모를 distance번 따라 올라간 뒤 그 환경에서 바로 꺼낸다 — 체인을 헤매지 않는다.

```java
Environment ancestor(int distance) {
  Environment environment = this;
  for (int i = 0; i < distance; i++) {
    environment = environment.enclosing;
  }
  return environment;
}

Object getAt(int distance, String name) {
  return ancestor(distance).values.get(name);
}
```

할당도 같은 방식(`assignAt`)이다. 이렇게 거리를 고정해 두면, 11.1의 `showA`는 선언 당시 계산된 거리로 **항상 같은 `a`(global)**를 찾는다. 버그가 사라진다.

```
var a = "global";
{
  fun showA() { print a; }   // 해소 시점: a는 2칸 바깥(전역). 거리 = 2 로 고정
  showA();                   // global
  var a = "block";           // 블록에 a가 끼어들어도...
  showA();                   // 거리 2를 그대로 따라가 → 여전히 global
}
```

---

## 11.5 해소 에러 (Resolution Errors)

Resolver는 실행 전에 트리를 다 보므로, 실행해 보지 않고도 잡을 수 있는 **정적 에러**를 보고하기 좋은 자리다. 이 패스에서 추가로 잡는 것들:

- **자기 초기화**: `var a = a;` → *"Can't read local variable in its own initializer."*
- **같은 스코프 중복 선언**: 지역에서 `var a; var a;` → *"Already a variable with this name in this scope."* (전역은 REPL 편의상 허용)
- **최상위 `return`**: 함수 밖의 `return;`을 막으려면 "지금 함수 안인가"를 추적한다.

```java
private enum FunctionType { NONE, FUNCTION }
private FunctionType currentFunction = FunctionType.NONE;

@Override
public Void visitReturnStmt(Stmt.Return stmt) {
  if (currentFunction == FunctionType.NONE) {
    Lox.error(stmt.keyword, "Can't return from top-level code.");
  }
  if (stmt.value != null) resolve(stmt.value);
  return null;
}
```

`resolveFunction`이 `currentFunction`을 저장·복원하며 중첩 함수를 정확히 추적한다.

---

**Resolver 한 패스가 두 가지를 동시에 해결했다** — 클로저의 정적 스코프 버그를 고치고(거리 고정), 실행 전에 여러 종류의 변수 오류를 잡는 정적 분석 지점을 마련했다. 그 인프라 위에서 12·13장이 클래스와 상속을 올린다.
