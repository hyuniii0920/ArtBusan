# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# 디버그 빌드
./gradlew assembleDebug

# 릴리즈 빌드
./gradlew assembleRelease

# 유닛 테스트
./gradlew test

# 인스트루먼트 테스트 (에뮬레이터/기기 필요)
./gradlew connectedAndroidTest

# 단일 테스트 클래스 실행
./gradlew test --tests "com.example.artbusan.ExampleUnitTest"

# 린트
./gradlew lint
```

Android Studio에서 실행하거나 `adb` 연결 후 `./gradlew installDebug`로 기기에 직접 설치할 수 있다.

## 아키텍처

**Single Activity + Fragment Navigation** 구조.

- `MainActivity` — 앱의 유일한 Activity. 커스텀 바텀 네비게이션(LinearLayout 기반), 우측 DrawerLayout 메뉴, 상단 타이틀 바를 직접 관리한다. Jetpack Navigation의 바텀 네비게이션 컴포넌트를 쓰지 않고 `navController.navigate(destinationId)` 를 직접 호출하며, `OnDestinationChangedListener`로 탭 아이콘·텍스트 색상 상태를 수동 업데이트한다.
- `nav_graph.xml` — 4개 Fragment(Home / Explore / Create / Profile)를 정의. Fragment 간 전환 애니메이션은 별도 설정 없음.
- 각 Fragment는 View Binding 대신 `findViewById`를 사용한다.

## 주요 설정

- **minSdk 26**, **targetSdk / compileSdk 36** (API 36.1)
- **Java 11** 소스 호환성
- 의존성 버전은 `gradle/libs.versions.toml`에서 중앙 관리
- Proguard 비활성화 (`minifyEnabled false`)
