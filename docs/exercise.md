# 챕터 6 챌린지 — 콤마 연산자 (Comma Operator)

> *Crafting Interpreters* 6장 "Parsing Expressions"의 챌린지 1번 풀이.
> 책 6장까지의 코드(표현식 파서)를 기준으로 설명하고, 마지막에 이 저장소(klox)의 실제 구현으로 연결한다.

## 문제

> C에서 블록(block)은 하나의 문장이 필요한 자리에 여러 문장을 묶어 넣을 수 있게 해주는 문장 형태다. **콤마 연산자**는 표현식에 대한 그 유사물이다. 하나의 표현식이 기대되는 자리에(단, 함수 호출의 인자 목록 안은 제외) 콤마로 구분된 일련의 표현식을 넣을 수 있다.
> 런타임에 콤마 연산자는 **왼쪽 피연산자를 평가하고 그 결과를 버린다. 그런 다음 오른쪽 피연산자를 평가해 반환한다.**

예시 (이 저장소 기준):

```
$ cat program.lox
var a; var b; var c;
a = 1, b = 2, c = 3;
print a;
print b;
print c;
$ klox program.lox
1
2
3
```

`a = 1, b = 2, c = 3` 한 줄이 세 개의 할당을 차례로 수행하고, 표현식 전체의 값은 마지막 `c = 3`인 `3`이 된다(여기서는 버려진다).

## 핵심 개념 세 가지

1. **콤마는 이항 연산자처럼 동작한다.** `왼쪽 , 오른쪽` 구조이고, 다른 연산자들과 똑같이 우선순위(precedence)와 결합성(associativity)을 가진다.
2. **우선순위는 가장 낮다.** `1 + 2, 3 * 4`는 `(1 + 2) , (3 * 4)`로 묶여야 한다. 즉 콤마는 다른 모든 연산자보다 **늦게** 묶인다 → 문법에서 가장 바깥(가장 위) 규칙이 된다.
3. **결합성은 왼쪽(left-associative)이다.** `a, b, c`는 `(a, b), c`. C의 정의와 일치하며, 어차피 왼쪽 값들은 버려지므로 평가 순서(왼→오)만 지켜지면 결과는 같다.

## 문법 (책 6장 기준)

6장 끝 시점의 표현식 문법은 다음과 같다:

```
expression  → equality ;
equality    → comparison ( ( "!=" | "==" ) comparison )* ;
comparison  → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term        → factor ( ( "-" | "+" ) factor )* ;
factor      → unary ( ( "/" | "*" ) unary )* ;
unary       → ( "!" | "-" ) unary | primary ;
primary     → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
```

콤마는 **가장 낮은 우선순위**여야 하므로, 기존 최상위였던 `equality` 위에 새 규칙을 끼워 넣고 `expression`이 그것을 가리키게 한다:

```
expression  → comma ;
comma       → equality ( "," equality )* ;
```

규칙 읽는 법: "콤마 표현식은 하나의 equality이며, 그 뒤에 `, equality`가 0개 이상 따라붙는다." 반복(`*`)이 왼쪽 결합을 만든다.

> 📌 6장에는 아직 할당(`=`)·삼항(`?:`)·문장(statement)이 없다. 그래서 콤마 바로 아래 단계가 `equality`다. 이후 챕터에서 할당이 생기면 콤마 아래 단계는 `assignment`로 바뀐다(아래 "이 저장소에서의 구현" 참고).

## 파싱 코드 (책의 Java, 6장 스타일)

문법 규칙 하나가 함수 하나로 내려오는 **재귀 하향 파서(recursive descent)** 패턴 그대로다. `term`/`factor`와 똑같은 모양이라는 점에 주목:

```java
private Expr expression() {
  return comma();
}

private Expr comma() {
  Expr expr = equality();

  while (match(COMMA)) {
    Token operator = previous();
    Expr right = equality();
    expr = new Expr.Binary(expr, operator, right);   // 기존 Binary 노드 재사용
  }

  return expr;
}
```

### 왜 새 AST 노드를 만들지 않는가?

콤마는 "왼쪽, 오른쪽" 두 피연산자에 연산자 토큰을 가진다 — 이건 정확히 `Expr.Binary`의 형태다. 따라서 `Comma`라는 새 노드를 추가할 필요 없이 `Expr.Binary(left, commaToken, right)`로 표현하면 된다. 비지터(`visitBinaryExpr`)에서 연산자 종류로 분기만 추가하면 끝.

## 런타임 의미 (인터프리터, 7장에서 채워지는 부분)

6장은 파싱까지만 다루지만, 문제에서 요구한 런타임 동작은 인터프리터의 이항 연산 처리에 분기 하나로 들어간다. 이항 평가는 보통 **양쪽을 먼저 평가**한 뒤 연산자를 적용하는데, 콤마는 그 구조에 자연스럽게 들어맞는다:

```java
// visitBinaryExpr 내부 — left, right는 이미 평가됨
case COMMA:
  return right;   // left는 이미 평가되어 부수효과를 냈고, 값만 버린다
```

`left`를 평가하는 줄이 이미 실행됐기 때문에(할당 같은 부수효과가 일어남) "버린다"는 건 그냥 **그 값을 쓰지 않고 `right`를 반환**한다는 뜻이다.

## 함정: 함수 호출 인자 목록 예외

문제 문장의 괄호 친 단서 — **"단, 함수 호출의 인자 목록 안은 제외"** — 가 핵심 함정이다.

`f(1, 2)`에서 콤마는 **인자 구분자**이지 콤마 연산자가 아니다. 만약 인자 파싱이 `expression()`(= 이제 콤마 규칙)을 호출하면 `1, 2`가 콤마 연산자로 묶여 인자가 **하나(`2`)**가 되어 버린다.

해결책: 인자 목록은 콤마보다 **한 단계 위(우선순위가 높은)** 규칙으로 각 인자를 파싱한다. 6장엔 호출이 없지만, 호출이 생기는 시점(책 10장)의 `arguments` 파싱은 `expression()`이 아니라 그 위 단계를 불러야 한다.

```java
// 잘못: arguments.add(expression());  → "1, 2"가 인자 하나로 합쳐짐
// 올바름: 콤마보다 위 단계로 인자 하나씩
do {
  arguments.add(assignment());   // (책에서는 해당 챕터의 "콤마 바로 위" 규칙)
} while (match(COMMA));
```

같은 이유로, 콤마를 **허용해야 하는** 자리(삼항의 가운데 피연산자 `a ? b, c : d`나 그냥 표현식 문장)는 `expression()`을 부르고, **금지해야 하는** 자리(인자 목록)는 그 위 단계를 부른다. "어느 자리에서 어느 규칙을 호출하느냐"로 콤마 허용 여부를 제어하는 게 이 문제의 본질이다.

## 이 저장소(klox)에서의 구현

klox는 이미 할당·삼항·함수 호출이 있는 상태(책 10장 수준)라, 콤마 아래 단계가 `equality`가 아니라 **`assignment`**다. 문법:

```
expression  → comma ;
comma       → assignment ( "," assignment )* ;
```

코드 매핑:

| 위치 | 파일 | 내용 |
|---|---|---|
| 진입점 변경 | `Parser.kt:214` | `expression()` 이 `comma()` 호출 |
| 콤마 규칙 | `Parser.kt:216` | `assignment ( "," assignment )*`, `Expr.Binary(COMMA)` 생성 |
| 인자 목록 예외 | `Parser.kt:363` | `finishCall`에서 `expression()` 대신 `assignment()`로 인자 파싱 |
| 런타임 분기 | `Interpreter.kt:119` | `visitBinaryExpr`의 `TokenType.COMMA -> right` |

```kotlin
// Parser.kt
private fun expression(): Expr = comma()

private fun comma(): Expr {
    var expr = assignment()
    while (match(TokenType.COMMA)) {
        val operator = previous()
        val right = assignment()
        expr = Expr.Binary(expr, operator, right)
    }
    return expr
}
```

```kotlin
// Interpreter.kt — visitBinaryExpr 내부 (left, right 이미 평가됨)
TokenType.COMMA -> {
    right
}
```

`Expr.Binary`를 재사용하므로 Resolver는 수정이 필요 없다(기존 `visitBinaryExpr`가 left·right를 모두 해석함).

### 삼항과의 상호작용

klox의 삼항 가운데 피연산자는 `expression()`(= 콤마)을 호출하므로 `true ? 1, 2 : 3` 같은 코드가 허용되고 `2`로 평가된다. 이는 C와도 일치한다(삼항의 가운데는 완전한 표현식).

## 테스트

`src/test/kotlin/CommaOperatorTest.kt`에 end-to-end 테스트 5종이 있고, 각 테스트는 실행 시 셸 세션처럼 소스와 결과를 출력한다.

| 케이스 | 입력 | 기대 결과 |
|---|---|---|
| 최우측 반환 | `print 1, 2, 3;` | `3` |
| 연쇄 할당 | `a = 1, b = 2, c = 3;` | `1` / `2` / `3` |
| 부수효과 후 우측 반환 | `var b = (a = 1, a + 4);` | a=`1`, b=`5` |
| 인자 목록은 콤마 연산자 아님 | `add(1, 2)` | `3` |
| 삼항 가운데 피연산자 | `true ? 1, 2 : 3` | `2` |

```
./gradlew test --tests CommaOperatorTest
```

## 한 줄 요약

콤마는 **가장 낮은 우선순위의 좌결합 이항 연산자**다. 문법 최상위에 `comma → assignment ( "," assignment )*`를 끼우고 `Expr.Binary`로 표현하며, 런타임에선 왼쪽을 평가(부수효과)하고 버린 뒤 오른쪽을 반환한다. 유일한 예외는 함수 인자 목록 — 거기서는 콤마보다 한 단계 위 규칙으로 인자를 파싱해 콤마가 구분자로 남게 한다.
