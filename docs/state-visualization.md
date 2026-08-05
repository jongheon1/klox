# super / this 상태 추적 — 5가지 예제

클래스 정의부터 호출까지 한 스텝씩 따라가며 **환경 체인·인스턴스 필드·콜스택·출력**이 어떻게 바뀌는지 본다.
(`super-visualizer.html`의 글 버전.)

> 핵심 하나: **`super`는 메서드를 "찾는 출발점"만 위로 올린다 — `this`가 가리키는 객체는 끝까지 원래 인스턴스다.**

표기: `#1` = 힙의 인스턴스, `→ #1` = 그 인스턴스를 가리킴, 환경은 **위 = 안쪽(실행 중)** / 아래 = 바깥(전역).

---

## 예제 1 — `this`는 환경에 든 변수, 중첩 함수가 클로저로 붙잡는다 (12.6)

**보고 싶은 것:** 메서드가 끝났는데도 콜백 안의 `this.name`이 살아있는 이유.

```lox
class Thing {
  getCallback() {
    fun localFunction() { print this.name; }
    return localFunction;
  }
}

var t = Thing();
t.name = "thing-name";
t.getCallback()();
```

| 단계        | 상태 변화                                                                            |
|-----------|----------------------------------------------------------------------------------|
| 정의        | 전역에 `Thing`. 상위 없음 → 메서드 클로저는 전역.                                                |
| 생성        | `Thing()` → 빈 인스턴스 `#1`, `t = #1`.                                               |
| 필드 설정     | `t.name = "thing-name"` → `#1`에 필드 `name` 박힘.                                    |
| 호출        | `t.getCallback()` → bind가 만든 **this 환경(this=#1)**이 전역 위에 깔림.                     |
| 클로저       | `fun localFunction` 선언 → 이 함수가 **현재 환경 전체(지역 → this 환경 → 전역)를 클로저로 붙잡음.**        |
| 반환        | `return localFunction` → getCallback은 콜스택에서 빠지지만, 반환된 함수가 this 환경을 붙잡아 **살아남음.** |
| 두 번째 `()` | 반환된 함수 호출 → 클로저가 복원돼 this 환경(this=#1)이 되살아남.                                     |
| 출력        | `print this.name` → 체인을 거슬러 this=#1 찾고 `#1.name` 읽음 → **`thing-name`**           |

**요점.** `this`는 bind할 때 만든 **this 환경**에 담긴 평범한 변수다. 메서드 안에서 선언한 `localFunction`이 그 환경을 클로저로 붙잡으면, 메서드가 반환돼 사라진 뒤에도 `this(=#1)`는 살아남는다.

---

## 예제 2 — `init`: 클래스 호출이 인스턴스를 만들고 항상 this를 반환 (12.7)

**보고 싶은 것:** `Circle(4)`가 어떻게 인스턴스를 만들고 `radius`를 초기화하는가.

```lox
class Circle {
  init(radius) { this.radius = radius; }
  area() { return 3.14159 * this.radius * this.radius; }
}
print Circle(4).area();
```

| 단계   | 상태 변화                                                                                   |
|------|-----------------------------------------------------------------------------------------|
| 정의   | 전역에 `Circle`. `init`도 그냥 메서드 — 이름이 init일 뿐.                                             |
| 생성   | `Circle(4)` → ① 빈 인스턴스 `#1` ② `findMethod("init")`을 #1에 bind (arity = init 파라미터 수 = 1). |
| init | this 환경(this=#1) 위에 인자 `radius=4`.                                                      |
| init | `this.radius = radius` → `#1`에 필드 `radius = 4` 박힘.                                      |
| 반환   | init이 끝나면 **반환값을 무시하고 무조건 this(#1)를 반환**(isInitializer) → `Circle(4)` = #1.             |
| 호출   | `.area()` → #1에 bind, `3.14159 * this.radius * this.radius` 평가.                         |
| 출력   | `this.radius = 4` → `3.14159 × 4 × 4` = **`50.26544`**                                  |

**요점.** 클래스 호출은 ① 빈 인스턴스 생성 ② `init`을 그 인스턴스에 bind해 실행 ③ **무조건 그 인스턴스(this) 반환**. `arity()`도 init의 파라미터 수에서 온다.

---

## 예제 3 — 상위 메서드가 `this`를 읽는다 (Animal · Dog)

**보고 싶은 것:** `super.describe()` 안의 `this.name`이 `"Rex"`가 나오는 이유.

```lox
class Animal {
  init(name) { this.name = name; }
  describe() {
    return this.name + " makes a sound";
  }
}

class Dog < Animal {
  describe() {
    return super.describe() + " (woof)";
  }
}

var d = Dog("Rex");
print d.describe();
```

| 단계        | 상태 변화                                                                                 |
|-----------|---------------------------------------------------------------------------------------|
| 정의 Animal | 전역에 `Animal`. 상위 없음 → 클로저는 전역.                                                        |
| 정의 Dog    | `class Dog < Animal` — 상위클래스 `Animal`은 **변수**. 평가해 클래스 객체를 얻음.                        |
|           | 메서드 만들기 직전 **super 환경(super=Animal)**을 한 겹 끼움. `Dog.describe`가 이 환경을 클로저로 붙잡음.        |
| 생성        | `Dog("Rex")` → 빈 인스턴스 `#1`. init은 Dog에 없음 → **findMethod가 Animal로 올라가** 발견, #1에 bind. |
| init      | this 환경(this=#1) 위에 `name="Rex"` → `this.name = name`으로 **#1의 필드 name="Rex"**.        |
| 반환        | 생성자가 #1 반환 → `d = #1`.                                                                |
| 호출        | `d.describe()` → describe는 **Dog에서 발견**(오버라이드), #1에 bind.                             |
|           | 환경: this 환경(this=#1) → super 환경(super=Animal).                                        |
| super 평가  | `super.describe()`: **super는 거리 d**에서 Animal, **this는 한 칸 안쪽 거리 d−1**에서 #1.           |
| 재바인딩      | Animal.describe를 찾아 **`bind(#1)`** — 상위 메서드지만 **this는 여전히 #1**.                       |
|           | Animal 본문 `this.name + " makes a sound"` → this=#1 → `"Rex makes a sound"`.           |
| 복귀        | Dog로 복귀: `+ " (woof)"` → **`Rex makes a sound (woof)`**                               |

**요점.** `super.describe()`는 메서드 검색을 `Animal`로 올렸을 뿐, `bind(#1)`로 **this는 다시 #1(Rex)**에 묶었다. 그래서 Animal 본문의 `this.name`이 정확히 "Rex"를 읽는다. bind를 빼면 this가 엉뚱해져 출력이 깨진다.

---

## 예제 4 — super를 타도 `this.who()`는 다시 하위로 (동적 디스패치)

**보고 싶은 것:** `super.greet()` 안의 `this.who()`가 `B.who`로 디스패치되는 이유.

```lox
class A {
  greet() { return "Hi, I am " + this.who(); }
  who()   { return "an A"; }
}
class B < A {
  greet() { return super.greet(); }
  who()   { return "a B"; }
}
print B().greet();
```

| 단계       | 상태 변화                                                                            |
|----------|----------------------------------------------------------------------------------|
| 정의       | `A`(greet, who). `B < A`는 super 환경(super=A)을 끼우고 greet/who가 클로저로 잡음.             |
| 생성       | `B()` → 빈 인스턴스 `#1`(클래스 B).                                                      |
| 호출       | `.greet()` → B에서 발견, `B.greet`를 #1에 bind.                                        |
|          | 환경: this 환경(this=#1) → super 환경(super=A). 본문은 그냥 `super.greet()`.                |
| super 평가 | super(d)=A, this(d−1)=#1. **A.greet를 #1에 다시 bind** — this는 여전히 #1!               |
| 디스패치     | A.greet 본문 `"Hi, I am " + this.who()`. this=#1의 **클래스는 B** → who 탐색을 **B부터** 시작. |
|          | B에 who 있음(오버라이드) → **B.who 호출**(A.who 아님!) → `"a B"`.                            |
| 복귀       | A.greet: `"Hi, I am " + "a B"` → **`Hi, I am a B`**                              |

**요점.** super는 검색 시작점만 A로 올렸다. this는 #1(B 인스턴스) 그대로라서, A 본문에서 부른 `this.who()`는 다시 **#1의 클래스 B부터 탐색**해 `B.who`(오버라이드)를 잡는다. 이게 동적 디스패치(가상 메서드)다.

---

## 예제 5 — 책 본문: `C().test()`가 "A method"를 찍는 이유

**보고 싶은 것:** super는 수신자(C)가 아니라 **메서드가 정의된 클래스(B)의 상위(A)**를 가리킨다.

```lox
class A { method() { print "A method"; } }
class B < A {
  method() { print "B method"; }
  test()   { super.method(); }
}
class C < B {}
C().test();
```

| 단계 | 상태 변화 |
|---|---|
| 정의 | `A`. `B < A` — super 환경(**super=A**)을 끼우고 method·test가 클로저로 잡음. ← test의 super는 이 시점에 **A로 고정**. |
| | `C < B` — super 환경(super=B)을 끼우지만 **C엔 메서드가 없음.** |
| 생성 | `C()` → 빈 인스턴스 `#1`(클래스 C). |
| 호출 | `.test()` → C엔 없음 → **B에서 test 발견**, #1에 bind. |
| | B.test 실행. 클로저가 **B의 super 환경(super=A)** → this 환경(this=#1) 아래에 **super=A**. 수신자가 C여도 super는 A! |
| super 평가 | `super.method()`: super(d)=**A**, this(d−1)=#1 → **A.method**를 찾음(B.method 아님!). |
| 출력 | A.method를 #1에 bind해 실행 → **`A method`** |

**요점.** `test`는 **B에 정의**됐다. 그래서 test 클로저의 super 환경은 **B의 상위 = A**다. 수신자가 C든 뭐든 상관없다. 만약 "인스턴스(C)의 상위"로 잘못 구현하면 super가 B를 가리켜 `B.method`가 다시 불려 **무한 루프**가 된다.
