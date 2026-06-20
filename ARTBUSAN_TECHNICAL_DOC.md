# ArtBusan — AR 기반 부산 미술관·박물관 전시 안내 Android 앱

### 한 줄 소개

> QR 코드 스캔과 AR 카메라를 결합하여 부산 미술관·박물관의 작품 정보를 실시간으로 제공하고, 4개국어 지원과 오프라인 우선(Offline-First) 설계로 외국인 관람객까지 아우르는 Android 전시 안내 플랫폼

- **대회 참여 :** 해당 없음
- **수상 여부 :** 해당 없음
- **발표 자료 :** *(별도 제공)*
- **깃허브 링크 :** https://github.com/ghktnqns321/ArtBusan

**작성자 :** 조서현  
**개발 기간 :** 2026.04.06 ~ 현재 (진행 중)  
**담당 역할 :**

| 팀원 | 담당 영역 | 주요 기여 |
|:---|:---|:---|
| 조서현 (본인) | Android 전체 | 앱 초기 구조 설계, 홈 화면 리디자인 (4탭→단일 홈+FAB), 다크 테마 / Glow Blue 디자인 시스템 구축, Room DB + MVVM 아키텍처 도입 (kapt→KSP 전환 포함), 드로어 메뉴 5종 화면 구현, 4개국어 런타임 전환 |
| 팀원 B (ghktnqns321) | AR / 네트워크 | AR QR 스캔 뷰어(`ArViewerActivity`) 전체, CameraX + ML Kit 카메라 파이프라인, Retrofit 네트워크 레이어(Artar API), 코드 통합 및 PR 병합 |
| 팀원 C (jay020420) | 분석 | Firebase Analytics 이벤트 추적 시스템 (PR #2 — 검토 중) |

---

## 1. 서론 (Introduction)

### 1.1 연구 배경 및 필요성

부산은 해마다 수백만 명의 국내외 관광객이 방문하는 도시이며, 해운대·수영·영도 등 각 구에 독립적으로 운영되는 미술관·박물관이 산재해 있다. 그러나 기존 전시 안내 방식은 다음과 같은 구조적 한계를 갖는다.

**정보 단절 문제.** 각 미술관은 자체 웹사이트 또는 인쇄 팜플렛을 통해 전시 정보를 제공하지만, 관람 중 실시간으로 작품 정보를 조회할 수 있는 통합 수단이 없다. QR 코드가 부착된 전시도 있으나, 연결되는 URL이 미술관마다 달라 관람 경험이 파편화된다.

**외국인 관람객 접근성 문제.** 부산을 방문하는 일본·중국·영어권 관광객이 증가하고 있지만, 대부분의 전시 안내는 한국어 중심이다. 음성 안내기 대여는 별도 비용이 발생하며, 대여 가능한 기기 수가 제한적이다.

**AR 연동 전시 경험의 부재.** 스마트 기기를 활용한 AR 전시 안내는 국내외 주요 미술관에서 확산되고 있지만, 부산 지역 미술관에는 표준화된 AR 안내 시스템이 없다.

ArtBusan은 이러한 문제를 해소하기 위해 기획된 프로젝트로, **QR 스캔 한 번으로 작품의 요약·상세 설명을 제공하고 TTS로 음성 안내까지 이어지는 끊김 없는 전시 경험**을 목표로 한다. 오프라인 환경에서도 Room DB에 사전 시드된 미술관 정보를 즉시 제공하여, 인터넷 연결 없이도 앱의 핵심 기능이 동작한다.

---

### 1.2 연구 목표 및 내용

- **핵심 목표 1: QR → 작품 설명 파이프라인 구현.** 작품 옆에 부착된 QR 코드(`artar://work/{id}` 형식)를 스캔하면 요약 설명이 하단 시트에 즉시 표시되고, 상세보기를 펼치면 전체 설명과 TTS 음성 안내로 이어진다. 카메라는 설명을 보는 동안에도 켜진 상태를 유지하여 AR 전환 UX의 기반이 된다.

- **핵심 목표 2: 오프라인 우선(Offline-First) 데이터 전략.** 미술관 목록과 작품 기본 정보를 앱 번들 내 JSON 파일에 포함시키고, 최초 실행 시 Room DB에 시드한다. 네트워크 호출이 실패하면 로컬 DB 데이터로 자동 폴백하여, 인터넷이 불안정한 전시 현장에서도 끊김 없이 작동한다.

- **핵심 목표 3: 4개국어 런타임 전환.** 앱 재설치 없이 한국어·영어·일본어·중국어를 즉시 전환한다. 언어 코드를 SharedPreferences에 저장하고, `attachBaseContext`에서 `LocaleHelper`로 Context를 래핑하여 시스템 언어 설정과 독립적으로 동작한다. 언어 변경 시 Room DB를 비우고 해당 언어 JSON으로 재시드한 뒤 Activity를 재시작하여 전체 UI에 일관성을 보장한다.

---

## 2. 시스템 아키텍처 (System Architecture)

### 2.1 전체 시스템 구성도

ArtBusan은 단일 Android 앱으로 구성된 클라이언트-중심 아키텍처다. 별도 백엔드 서버 없이 작동 가능하며, 온라인 환경에서는 외부 Artar API를 통해 최신 작품 데이터를 가져온다.

```
┌──────────────────────────────────────────────────────────────────┐
│                      ArtBusan Android App                         │
│                                                                    │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                    UI Layer                                 │   │
│  │  MainActivity (Single Activity)                             │   │
│  │  ├── DrawerLayout (우측 메뉴 — 공지·언어·설정 3개 라우팅)  │   │
│  │  │   * AccessibilityFragment / GuideFragment 구현 완료,    │   │
│  │  │     nav_graph 미등록으로 현재 진입 불가                  │   │
│  │  ├── 커스텀 TopBar (검색·메뉴 버튼)                        │   │
│  │  └── NavHostFragment → nav_graph.xml                       │   │
│  │       ├── HomeFragment        (미술관 목록, 지역 칩 필터)   │   │
│  │       ├── ArtworkDetailFragment (상세 정보)                 │   │
│  │       ├── NoticeFragment      (공지사항)                    │   │
│  │       ├── LanguageFragment    (언어 선택)                   │   │
│  │       └── SettingsFragment    (설정)                        │   │
│  └────────────────────────────────────────────────────────────┘   │
│                                                                    │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                  ViewModel Layer                            │   │
│  │  MuseumViewModel (LiveData<List<Museum>>)                   │   │
│  │  └── MuseumViewModelFactory                                 │   │
│  └────────────────────────────────────────────────────────────┘   │
│                                                                    │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                  Data Layer                                 │   │
│  │  MuseumRepository                                           │   │
│  │  ├── MuseumDao (Room DB: museum.db)                         │   │
│  │  └── JSON Seed (assets/museums[_en|_ja|_zh].json)           │   │
│  │                                                             │   │
│  │  ArtworkExperienceRepository                                │   │
│  │  ├── ArtarApiClient (Retrofit → ARTAR_API_BASE_URL)         │   │
│  │  └── MuseumRepository (로컬 폴백)                           │   │
│  └────────────────────────────────────────────────────────────┘   │
│                                                                    │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │         AR / Camera Layer (팀원 B — ghktnqns321)            │   │
│  │  ArViewerActivity                                           │   │
│  │  ├── CameraX (Preview + ImageAnalysis)                      │   │
│  │  ├── ML Kit BarcodeScanning (QR 코드 → artar://work/{id})   │   │
│  │  ├── ArtworkExperienceRepository (작품 정보 로드)           │   │
│  │  └── TextToSpeech (작품 상세 설명 음성 안내)                │   │
│  └────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
                            │ Retrofit HTTP
                            ▼
              ┌─────────────────────────────┐
              │   Artar External API        │
              │   GET /works/{id}           │
              │   → ArtworkResponse (JSON)  │
              └─────────────────────────────┘
```

**개발 타임라인 (커밋 기반):**

| 날짜 | 커밋 | 내용 |
|:---|:---|:---|
| 2026.04.06 | `adbd715` | 초기 구조: 4탭(Home/Explore/Create/Profile) + DrawerLayout + 기본 레이아웃 |
| 2026.04.10 | `9b8a5c8` | 홈 화면 리디자인: 4탭 → 단일 홈 + 중앙 FAB. Explore/Create/Profile Fragment 삭제. ArtworkDetailFragment 추가 |
| 2026.04.10 | `db16936`~`00ea0dd` | 다크 테마 + Glow Blue 팔레트 (`#05070D` 배경 / `#2563EB` Primary / `#60A5FA` Glow) 적용. FAB·AR 버튼 HUD 스타일 전환. 상세 화면 FAB 숨김 처리 |
| 2026.04.11 | `7833bd1` | Room DB + MVVM 도입. kapt → KSP 전환. museums.json(8개 시설) 정의. data / viewmodel 레이어 분리 |
| 2026.04.29 | `ee9c480` | 드로어 메뉴 5종(공지·언어·설정·접근성·가이드) 화면 구현. 4개국어 JSON + 문자열 리소스 추가 |
| 2026.05.03 | `be00f8c` | 팀원 B(ghktnqns321) 작업 병합: ArViewerActivity, CameraX, ML Kit, Retrofit 네트워크 레이어 |
| 2026.05.06 | `11cf9c1`* | 팀원 C(jay020420) Firebase Analytics 이벤트 추적 추가 — *PR #2, main 미병합* |

---

### 2.2 적용 기술 스택 (Technology Stack)

#### 핵심 언어 및 플랫폼

| 항목 | 기술 | 선택 이유 |
|:---|:---|:---|
| Language | Kotlin | Android 공식 언어. 코루틴으로 비동기 DB·네트워크 호출을 자연스럽게 처리. `data class`, `object` 등으로 보일러플레이트 최소화 |
| Min SDK | API 26 (Android 8.0) | CameraX의 안정적인 지원 범위이자 국내 보급률이 높은 기준점. 한국 주요 미술관 방문객 기기 환경을 커버 |
| Target / Compile SDK | API 36 | 최신 Android API 활용 가능성 확보. Edge-to-Edge 디스플레이 대응 |
| Build System | Gradle (Version Catalog) | `gradle/libs.versions.toml`로 모든 의존성 버전을 중앙 관리. 팀 협업 시 버전 충돌 방지 |
| Annotation Processor | KSP (Kotlin Symbol Processing) | 초기 kapt로 시작했으나 AGP 9.0.1 내장 Kotlin과의 호환성 문제로 Room 도입 시 KSP로 전환 |

#### UI / 네비게이션

| 항목 | 기술 | 선택 이유 |
|:---|:---|:---|
| 네비게이션 | Jetpack Navigation Component | Fragment 간 전환을 `nav_graph.xml`로 선언적으로 관리. Back Stack 처리 자동화. Bundle 기반 안전한 인자 전달 |
| Activity 구조 | Single Activity | 화면별 Activity 분리보다 메모리 효율적이며, Fragment 간 상태 공유와 드로어 메뉴 관리가 단일 진입점에서 가능 |
| 레이아웃 | Custom XML (DrawerLayout, LinearLayout 기반) | Jetpack Compose 미사용. 기존 Android View 시스템을 완전히 활용하여 팀원 간 XML 레이아웃 작업 분담 용이 |
| 이미지 로딩 | Coil 2.6 | Kotlin 코루틴 기반 이미지 라이브러리. Glide 대비 Kotlin 친화적 API. placeholder/error 처리 내장 |
| RecyclerView | ListAdapter + DiffUtil | `DiffUtil.ItemCallback`으로 목록 변경 시 최소한의 뷰만 업데이트. 지역 필터 전환 시 애니메이션 자동 처리 |

#### 데이터 레이어

| 항목 | 기술 | 선택 이유 |
|:---|:---|:---|
| 로컬 DB | Room 2.7 (SQLite) | Android 공식 ORM. DAO 인터페이스로 쿼리를 타입 안전하게 선언. 싱글톤 `MuseumDatabase`로 DB 인스턴스 관리 |
| JSON 파싱 | Gson 2.10 | `assets/` JSON 시드 파일 파싱 및 Retrofit 응답 변환에 동일하게 사용. `TypeToken`으로 `List<Museum>` 제네릭 타입 안전 역직렬화 |
| 네트워크 | Retrofit 2.11 + Gson Converter | REST API 호출을 인터페이스 선언만으로 구현. `suspend fun`으로 코루틴 직접 연동 |
| 상태 관리 | ViewModel + LiveData | MVVM 패턴. 화면 회전 등 구성 변경(Configuration Change)에도 데이터 보존. `viewModelScope`로 코루틴 생명주기 관리 |
| 사용자 설정 | SharedPreferences | 언어 코드(`selected_language`) 단일 키 저장. DataStore 미사용 — 단순 설정값 1개에는 오버엔지니어링 |

#### AR / 카메라 레이어 (팀원 B 담당)

| 항목 | 기술 | 선택 이유 |
|:---|:---|:---|
| 카메라 | CameraX 1.4 (camera-core / camera2 / camera-lifecycle / camera-view) | Android 공식 카메라 라이브러리. 생명주기 자동 연동. `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST`로 QR 프레임 분석 성능 최적화 |
| QR 인식 | ML Kit Barcode Scanning 17.3 | Google 온디바이스 ML. 인터넷 없이도 동작. `FORMAT_QR_CODE`만 활성화하여 처리 범위 제한 → 스캔 속도 향상 |
| TTS | Android TextToSpeech API | 별도 라이브러리 없이 OS 내장 TTS 사용. `Locale.KOREAN` 설정. 앱 언어와 별개로 한국어 TTS 고정(작품 설명이 한국어이므로) |

---

### 2.3 주요 데이터 구조

#### Museum (Room Entity)

| 필드 | 타입 | 설명 |
|:---|:---|:---|
| `id` | Int (PK) | 미술관 고유 식별자. QR URL의 `{id}`와 일치 |
| `title` | String | 미술관명 (언어별 JSON에서 해당 언어로 제공) |
| `category` | String | 행정구역명 (해운대구, 수영구 등 10개 구·군). 칩 필터의 기준값. DB에는 항상 한국어로 저장 |
| `location` | String | 도로명 주소 |
| `hours` | String | 운영 시간 |
| `fee` | String | 입장료 정보 |
| `phone` | String | 대표 전화번호 |
| `description` | String | 미술관/전시 소개 텍스트 |
| `imageUrl` | String | 썸네일 이미지 URL (현재 JSON에서 대부분 빈 문자열 → Coil error placeholder 표시) |

#### ArtworkExperience (Network / 화면 전달 모델)

| 필드 | 타입 | 설명 |
|:---|:---|:---|
| `id` | Int | 작품 ID |
| `title` | String | 작품명 |
| `artist` | String | 작가명 |
| `summaryDescription` | String | 하단 시트 축소 상태에서 보이는 요약 설명 (1~2문장) |
| `detailDescription` | String | 상세보기 펼침 상태의 전체 설명 + TTS 낭독 대상 |
| `imageUrl` | String | 작품 이미지 URL |
| `arAssetUrl` | String? | AR 에셋 URL (현재 항상 `null` — AR 오버레이 렌더링 미구현) |

API 응답(`ArtworkResponse`)과 로컬 `Museum` 모델 모두 `toArtworkExperience()` 확장 함수로 이 단일 모델에 매핑되어, UI 레이어는 데이터 출처를 알 필요가 없다.

---

## 3. 핵심 기능 및 알고리즘 구현

### 3.1 초기 구조와 리디자인 결정

초기 커밋(`adbd715`, 2026.04.06)의 앱은 Home / Explore / Create / Profile 4탭 구조였다. 탐색·제작·프로필 Fragment가 각각 별도 레이아웃으로 분리된 전형적인 바텀 네비게이션 앱이었다.

2026.04.10 리디자인 커밋(`9b8a5c8`)에서 이 구조를 전면 개편했다. **4탭 → 단일 홈 + 중앙 FAB** 구조로 전환하고, Explore / Create / Profile Fragment와 해당 레이아웃을 삭제했다. 전환 이유는 부산 구·군별 미술관 목록과 지역 필터를 홈 화면 하나에 집중시키는 것이 사용자에게 더 직관적이라고 판단했기 때문이다. FAB(투어 제작)은 미구현 상태로 Toast만 표시한다.

같은 날 다수의 `design` 커밋(`db16936`~`00ea0dd`)을 통해 Glow Blue 팔레트를 확정했다.

```
Background : #05070D
Surface    : #0E1320
Primary    : #2563EB
Glow Blue  : #60A5FA (네온 그라디언트, HUD 테두리, 스캔 프레임)
```

---

### 3.2 QR 스캔 파이프라인 (팀원 B 구현)

`ArViewerActivity`(2026.05.03 병합)는 팀원 B(ghktnqns321)가 독립 브랜치에서 구현한 뒤 PR #1으로 병합됐다.

#### QR URL 파싱 전략

`ArViewerActivity.parseArtworkId()`는 다양한 QR URL 형식에 대응하기 위해 4단계 정규식 체인을 사용한다.

```
1순위: artar://work/102        → Regex("""^artar://work/(\d+)$""")
2순위: https://...?id=102      → Regex("""[?&]id=(\d+)""")
3순위: https://.../work/102    → Regex("""(?:work|artwork|venue)/(\d+)""")
4순위: 102 (순수 숫자)         → rawValue.toIntOrNull()
```

이 설계는 운영 초기에 QR URL 형식이 확정되지 않은 상황에서도 유연하게 대응할 수 있도록 한다. 파싱 실패 시 `INVALID QR` 상태를 UI에 표시하고 스캔을 계속 허용한다.

#### 스캔 상태 제어 — 이중 플래그 패턴

ML Kit의 `ImageAnalysis`는 매 프레임을 분석 콜백으로 전달한다. QR 인식 시 중복 처리를 방지하기 위해 `@Volatile` 플래그 2개를 조합한다.

```kotlin
@Volatile private var scanningEnabled = false   // 사용자가 스캔 버튼을 눌렀는지
@Volatile private var processingScan = false    // 현재 프레임 분석 진행 중인지
```

- `scanningEnabled = false`이면 모든 프레임을 즉시 버림 → 불필요한 ML Kit 호출 없음
- `processingScan = true`이면 해당 프레임 완료 전까지 다음 프레임 분석 차단
- `lastScannedValue`로 동일 QR 재처리를 추가로 방지

QR 인식 성공 → `scanningEnabled = false`로 자동 비활성화 → 재스캔은 "다시 스캔" 버튼으로만 재활성화.

#### 카메라 생명주기 관리

CameraX를 `Activity.lifecycle`에 바인딩(`bindToLifecycle`)하여 `onPause`에서 `unbindAll()`, `onResume`에서 프리뷰 재시작을 자동화한다. `cameraExecutor`는 별도 `ExecutorService`로 분리하여 이미지 분석이 메인 스레드를 블록하지 않도록 한다. `onDestroy`에서 ML Kit `scanner.close()`, `ExecutorService.shutdown()`, TTS `shutdown()`을 순서대로 정리한다.

---

### 3.3 오프라인 우선 데이터 전략

#### 시드(Seed) 메커니즘

`MuseumRepository.seedIfEmpty()`는 Room DB의 `museums` 테이블 row 수를 확인하고, 비어 있으면 `assets/` 디렉터리의 JSON을 읽어 삽입한다. 초기 데이터는 부산 8개 시설로 시작했다.

```
assets/museums.json       → 한국어 (기본)
assets/museums_en.json    → 영어
assets/museums_ja.json    → 일본어
assets/museums_zh.json    → 중국어
```

`seedIfEmpty()`는 DB 접근이므로 `withContext(Dispatchers.IO)`로 백그라운드 스레드에서 실행된다. 이 함수는 `getAll()`, `getByCategory()`, `getById()` 세 메서드 모두에서 호출되어, 어느 진입점으로 들어와도 시드가 보장된다.

#### API → 로컬 폴백 체인

`ArtworkExperienceRepository.getArtwork(id)`는 `runCatching`으로 API 호출을 감싸고, 실패 시 Room DB로 자동 폴백한다.

```kotlin
runCatching {
    ArtarApiClient.service.getArtwork(id).toArtworkExperience()
}.getOrElse {
    localRepository.getById(id)?.toArtworkExperience() ?: throw it
}
```

로컬에도 없으면 예외를 재발생시켜 UI에서 "작품 정보를 가져오지 못했습니다" 메시지를 표시하고 스캔 재활성화.

---

### 3.4 4개국어 런타임 전환

#### Context 래핑 방식

Android는 시스템 언어와 다른 언어로 앱을 실행하려면 `Context`를 교체해야 한다. `LocaleHelper.wrap()`은 `createConfigurationContext()`로 언어별 `Context`를 생성하며, `MainActivity.attachBaseContext()`에서 이를 호출한다.

```kotlin
override fun attachBaseContext(newBase: Context) {
    val lang = newBase.getSharedPreferences("artbusan_prefs", Context.MODE_PRIVATE)
        .getString("selected_language", "ko") ?: "ko"
    super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
}
```

`attachBaseContext`는 `onCreate` 이전에 호출되므로, Activity가 생성될 때부터 올바른 언어 Context가 적용된다. `LanguageFragment`에서 언어를 변경하면 `Activity.recreate()`로 재시작하여 `attachBaseContext`가 새 언어 코드를 읽는다.

#### DB 재시드 흐름

언어 변경 시 기존 DB 데이터는 이전 언어의 번역이므로 유효하지 않다. `LanguageFragment`는 언어 저장 후 아래 순서로 전환한다.

```
1. prefs.edit().putString("selected_language", code).apply()
2. withContext(Dispatchers.IO) { museumDao.deleteAll() }
3. requireActivity().recreate()
   → attachBaseContext에서 새 언어 코드 읽기
   → HomeFragment 진입 → seedIfEmpty() → 새 언어 JSON으로 재삽입
```

DB 비우기와 Activity 재시작을 순서대로 실행하지 않으면 이전 언어 데이터가 화면에 잔존할 수 있다. `deleteAll()` 완료를 `withContext`로 보장한 뒤 `recreate()`를 호출하는 이유가 이것이다.

#### 카테고리명 다국어 처리

미술관의 `category` 필드는 항상 한국어 구명(해운대구 등)을 기준값으로 저장한다. UI 표시 시에는 `CategoryUtils.translateCategory(context, category)`가 `context.getString(R.string.chip_xxx)` 리소스를 통해 현재 언어에 맞는 번역을 반환한다. DB 값이 언어에 독립적이므로 언어 전환 후 필터 로직이 그대로 작동한다.

---

### 3.5 하단 시트 (Bottom Sheet) 확장/축소 UX

`ArViewerActivity`는 Jetpack `BottomSheetBehavior` 대신 `ViewGroup.LayoutParams.height`를 직접 조작하는 방식으로 하단 시트의 확장/축소를 구현한다.

```kotlin
layoutParams.height = if (expanded) {
    resources.displayMetrics.heightPixels -
        resources.getDimensionPixelSize(R.dimen.bottom_sheet_expanded_top_offset)
} else {
    ViewGroup.LayoutParams.WRAP_CONTENT
}
bottomSheet.layoutParams = layoutParams
bottomSheet.requestLayout()
```

`BottomSheetBehavior`를 사용하지 않은 이유는 카메라 프리뷰(`PreviewView`)와 반투명 오버레이(`dimOverlay`)를 동시에 유지하면서 시트 높이를 동적으로 제어할 때 충돌이 발생할 수 있기 때문이다.

---

## 4. 구현 결과 (Screenshots & Flow)

---

- **[그림 1: 홈 화면]**
  - 부산 지역 구별 칩 필터(해운대구·수영구·남구 등 10개) + 미술관·박물관 2열 그리드 카드
  - 상단 AR 카메라 버튼 → `ArViewerActivity` 직접 진입
  - 핵심 기술: `MuseumViewModel` + `LiveData<List<Museum>>`, `ListAdapter` + `DiffUtil`, `GridLayoutManager(span=2)`

---

- **[그림 2: 작품 상세 화면]**
  - 미술관명, 카테고리(지역구), 주소, 운영 시간, 입장료, 전화번호, 설명 표시
  - "AR 체험" 버튼으로 `ArViewerActivity` 진입
  - 핵심 기술: Jetpack Navigation Bundle 전달, Room DB `getById()` 비동기 조회

---

- **[그림 3: AR QR 스캔 대기 화면]**
  - CameraX 프리뷰 위에 반투명 오버레이 + Glow Blue 스캔 프레임
  - "스캔 시작" 버튼으로 ML Kit QR 분석 활성화
  - QR 인식 전: 가이드 문구 표시, 카메라 프리뷰·스캔 프레임 숨김
  - 핵심 기술: CameraX `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST`, 이중 플래그(`scanningEnabled`, `processingScan`)

---

- **[그림 4: QR 스캔 후 작품 설명 시트 (축소)]**
  - QR 인식 성공 → 하단 시트에 작품명 + 1~2문장 요약 설명 표시
  - 카메라 프리뷰는 계속 작동 (dimOverlay alpha 0.22로 감광)
  - "상세보기" 버튼으로 시트 확장 전환
  - 핵심 기술: `ArtworkExperienceRepository` API↔로컬 폴백 체인, `lifecycleScope.launch` 비동기 로드

---

- **[그림 5: QR 스캔 후 작품 설명 시트 (확장)]**
  - 작품명, 작가명, 전체 상세 설명, TTS 음성 낭독 버튼, AR MODE 진입 버튼
  - 시트 높이 = 화면 높이 - `bottom_sheet_expanded_top_offset`
  - 핵심 기술: `TextToSpeech.speak()`, `ViewGroup.LayoutParams.height` 직접 조작

---

- **[그림 6: 언어 설정 화면]**
  - 한국어 / English / 日本語 / 中文 라디오 버튼 선택
  - 선택 즉시 SharedPreferences 저장 → Room DB 초기화 → Activity 재시작 → 전체 UI 언어 전환
  - 핵심 기술: `LocaleHelper.wrap()`, `MuseumDatabase.museumDao().deleteAll()`, `Activity.recreate()`

---

## 5. 결론 및 고찰

ArtBusan 프로젝트를 통해 QR 스캔 → 작품 설명 표시 → TTS 음성 안내로 이어지는 전시 안내 파이프라인과, 오프라인에서도 끊김 없이 동작하는 데이터 전략, 런타임 4개국어 전환을 단일 Android 앱에서 구현했다. 초기 4탭 구조를 과감히 단일 홈으로 개편한 리디자인 결정과 팀원 B의 AR 뷰어 작업이 병합되면서 앱의 핵심 UX가 완성됐다.

기술적으로 가장 의미 있었던 결정은 **`ArtworkExperience` 모델을 API와 로컬 DB의 공통 출력 타입으로 설계한 것**이다. `ArtworkResponse.toArtworkExperience()`와 `Museum.toArtworkExperience()` 두 확장 함수가 서로 다른 필드 구조를 동일한 타입으로 매핑하므로, `ArViewerActivity`는 데이터 출처에 관계없이 동일한 로직으로 UI를 구성한다. 이 설계 덕분에 Artar API 없이 로컬 시연만으로도 전체 QR 스캔 플로우를 검증할 수 있었다.

두 번째로 의미 있는 결정은 **Room 도입 시 kapt에서 KSP로 전환한 것**이다. AGP 9.0.1이 내장한 Kotlin 버전과 kapt의 호환성 문제가 빌드 실패를 야기했고, 이를 해결하기 위해 KSP로 전환했다. KSP는 kapt 대비 처리 속도가 빠르고 Kotlin 네이티브 API를 사용하므로, 결과적으로 더 나은 선택이 됐다.

아쉬운 점으로는 네 가지를 꼽을 수 있다. 첫째, `arAssetUrl`과 AR 오버레이 기능(`btnArMode`)이 UI까지 구현되어 있으나 실제 AR 렌더링(ARCore 등)은 미연결 상태다. "AR MODE" 버튼은 텍스트 배지만 바뀌는 데모 수준이다. 둘째, `AccessibilityFragment`와 `GuideFragment`가 코드와 레이아웃까지 구현됐지만 `nav_graph.xml`에 미등록되어 현재 진입 불가능하다. 드로어 메뉴에서 보여야 할 5개 화면 중 3개(공지·언어·설정)만 실제로 연결돼 있다. 셋째, Firebase Analytics 이벤트 추적(PR #2)이 main에 병합되지 않아 사용자 행동 데이터를 수집하지 못하는 상태다. 넷째, `ArtworkDetailFragment`에서 ViewModel 없이 `MuseumDatabase`를 직접 접근하는 부분이 남아 있어, 아키텍처 일관성 측면에서 ViewModel 레이어로 이동이 필요하다.

향후에는 nav_graph 미연결 화면 완성, PR #2 병합, ARCore 기반 작품 오버레이 렌더링, 스탬프 투어 기능(방문 기록 로컬 저장), FAB(투어 제작) 기능 구현을 통해 앱 완성도를 높이고자 한다.
