# 7장 · Evaluating Expressions (표현식 평가)

6장에서 만든 AST를 실제로 **실행**한다. 트리를 순회하며 각 노드의 값을 계산하는 트리 워킹 인터프리터(tree-walk interpreter)를 만든다.

---

## 7.1 값을 어떻게 표현하나

Lox는 **동적 타입**, Java는 **정적 타입**이다. 이 간극을 어떻게 메우나? 답은 **`java.lang.Object`** 하나다.

| Lox 타입 | Java 표현 |
|---|---|
| 아무 Lox 값 | `Object` |
| `nil` | `null` |
| Boolean | `Boolean` |
| number | `Double` |
| string | `String` |

- 변수에는 `Object`를 담고, 런타임에 실제 타입을 `instanceof`로 확인한다.
- 메모리 관리는 **JVM의 GC가 공짜로** 해준다. 1차 인터프리터를 Java로 작성하는 핵심 이유다.

---

## 7.2 평가 = Visitor 구현

5장의 `AstPrinter`가 트리를 순회하며 **문자열**을 만들었다면, `Interpreter`는 똑같은 방식으로 순회하며 **값**을 만든다.

```java
class Interpreter implements Expr.Visitor<Object> {
  void interpret(Expr expression) { ... }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }
}
```

`evaluate`는 하위 표현식을 다시 인터프리터로 보내는 작은 헬퍼다. 모든 노드 평가가 이걸로 재귀한다.

### 노드별 평가 — 쉬운 것부터

**1) 리터럴** — 스캐너가 만들고 파서가 노드에 담아둔 값을 그대로 꺼낸다.

```java
@Override
public Object visitLiteralExpr(Expr.Literal expr) {
  return expr.value;
}
```

**2) 그룹핑** — 괄호 안 표현식을 재귀 평가한다.

```java
@Override
public Object visitGroupingExpr(Expr.Grouping expr) {
  return evaluate(expr.expression);
}
```

**3) 단항** — 피연산자를 먼저 평가하고, 그다음 연산자를 적용한다.

```java
@Override
public Object visitUnaryExpr(Expr.Unary expr) {
  Object right = evaluate(expr.right);

  switch (expr.operator.type) {
    case BANG:
      return !isTruthy(right);
    case MINUS:
      checkNumberOperand(expr.operator, right);
      return -(double)right;
  }

  return null;
}
```

`-(double)right`의 **런타임 캐스트**가 동적 타입의 정체다. 컴파일 시점에는 `right`가 숫자인지 알 수 없으므로, 실행 중에 캐스트한다.

**4) 이항** — 양쪽을 평가한 뒤 연산자를 적용한다.

```java
@Override
public Object visitBinaryExpr(Expr.Binary expr) {
  Object left = evaluate(expr.left);
  Object right = evaluate(expr.right);

  switch (expr.operator.type) {
    case GREATER:
      checkNumberOperands(expr.operator, left, right);
      return (double)left > (double)right;
    case GREATER_EQUAL:
      checkNumberOperands(expr.operator, left, right);
      return (double)left >= (double)right;
    case LESS:
      checkNumberOperands(expr.operator, left, right);
      return (double)left < (double)right;
    case LESS_EQUAL:
      checkNumberOperands(expr.operator, left, right);
      return (double)left <= (double)right;
    case MINUS:
      checkNumberOperands(expr.operator, left, right);
      return (double)left - (double)right;
    case SLASH:
      checkNumberOperands(expr.operator, left, right);
      return (double)left / (double)right;
    case STAR:
      checkNumberOperands(expr.operator, left, right);
      return (double)left * (double)right;
    case PLUS:
      if (left instanceof Double && right instanceof Double) {
        return (double)left + (double)right;
      }
      if (left instanceof String && right instanceof String) {
        return (String)left + (String)right;
      }
      throw new RuntimeError(expr.operator,
          "Operands must be two numbers or two strings.");
    case BANG_EQUAL:  return !isEqual(left, right);
    case EQUAL_EQUAL: return isEqual(left, right);
  }

  return null;
}
```

두 가지 포인트:

- **평가 순서는 왼쪽 → 오른쪽**으로 고정한다. 피연산자에 부수 효과가 있으면 이 순서가 사용자에게 보이므로, 언어 의미론의 일부다.
- **`+`만 오버로딩**되어 있다. 숫자 덧셈과 문자열 연결을 모두 처리하므로, `instanceof`로 동적 분기한다.

### Truthiness — "참 같은 값"

Boolean이 아닌 값이 조건 자리에 오면 어떻게 되나? 언어마다 규칙이 제각각이다.

| 언어 | 규칙 |
|---|---|
| JavaScript | `0`은 falsey인데 `"0"`은 truthy, 빈 배열은 truthy |
| Python | 빈 문자열·빈 시퀀스가 falsey |
| Lox (Ruby를 따름) | **`false`와 `nil`만 falsey, 나머지는 전부 truthy** |

```java
private boolean isTruthy(Object object) {
  if (object == null) return false;
  if (object instanceof Boolean) return (boolean)object;
  return true;
}
```

### 동등성

- `==`는 타입이 달라도 비교할 수 있다. 단, 다른 타입이면 무조건 `false`다(암묵적 변환 없음).
- 구현은 Java의 `equals()`에 위임하되 `null`을 특별 처리한다.

```java
private boolean isEqual(Object a, Object b) {
  if (a == null && b == null) return true;
  if (a == null) return false;
  return a.equals(b);
}
```

미묘한 함정: IEEE 754에서 `NaN`은 자기 자신과도 같지 않아야 한다(`NaN == NaN`은 false). 하지만 Java의 `Double.equals()`는 true를 반환한다. 그래서 이 구현의 Lox는 그 지점에서 IEEE를 따르지 않게 된다. 이런 미묘한 비호환을 다루는 것이 언어 구현자의 일상이다.

---

## 7.3 런타임 에러

`2 * (3 / -"muffin")` — 문자열은 부정(negate)할 수 없다.

- 지금까지의 에러는 **정적 에러**(실행 전에 감지)였다. 이제부터는 **런타임 에러**(실행 중에 감지)다.
- 그대로 두면 Java의 `ClassCastException`과 Java 스택트레이스가 노출된다. Lox가 Java로 구현됐다는 것은 사용자가 몰라야 할 디테일이다.
- 해법: 캐스트 직전에 직접 타입을 검사하고, 실패하면 **Lox 전용 `RuntimeError`**를 던진다(에러 위치 토큰을 함께 담는다).

```java
private void checkNumberOperand(Token operator, Object operand) {
  if (operand instanceof Double) return;
  throw new RuntimeError(operator, "Operand must be a number.");
}

private void checkNumberOperands(Token operator, Object left, Object right) {
  if (left instanceof Double && right instanceof Double) return;
  throw new RuntimeError(operator, "Operands must be numbers.");
}
```

- 런타임 에러는 표현식 평가를 중단시키되, **인터프리터(REPL 세션)는 죽이면 안 된다.**
- `interpret()`에서 잡아 메시지와 줄 번호를 출력하고 세션을 계속한다.

---

## 7.4 연결

```java
void interpret(Expr expression) {
  try {
    Object value = evaluate(expression);
    System.out.println(stringify(value));
  } catch (RuntimeError error) {
    Lox.runtimeError(error);
  }
}

private String stringify(Object object) {
  if (object == null) return "nil";

  if (object instanceof Double) {
    String text = object.toString();
    if (text.endsWith(".0")) {
      text = text.substring(0, text.length() - 2);
    }
    return text;
  }

  return object.toString();
}
```

- `stringify()`: 값을 사용자에게 보여줄 문자열로 바꾼다. `nil`을 처리하고, 정수 값 `Double`의 `.0`을 잘라낸다(`5.0` → `5`).
- REPL에서는 인터프리터 인스턴스를 **static으로 재사용**한다. 나중에 전역 변수가 세션 내내 유지되게 하기 위한 포석이다.
- 종료 코드: 구문 에러는 65, 런타임 에러는 70(UNIX `sysexits` 관례).

**스캐닝 → 파싱 → 평가로 이어지는 전체 파이프라인이 완성된다.** 지금은 계산기 수준이지만, 이 Visitor 뼈대 위에 다음 장들이 변수·함수·클래스를 채워 넣는다.
