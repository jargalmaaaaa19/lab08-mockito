# Лаборатори №8: Mock объектууд — Mockito-гоор хамаарлыг орлуулж тестлэх

**Оюутан:** З.Жаргалмаа
**Оюутны код:** B232270005
**Хичээл:** F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест (2026)

## Орчин

`java -version`: 17.0.20.1
`mvn -version` : Apache Maven 3.8.7


## Төслийн тухай

Тестийн бай нь `CourseRegistrationService` класс бөгөөд хоёр хамааралтай: `StudentRepository` (өгөгдлийн сан) ба `EmailSender` (и-мэйл илгээгч). Хоёр интерфейст бодит хэрэгжүүлэлт зориуд байхгүй. Mockito-гоор хамаарлуудыг орлуулж, үйлчилгээний логикийг бүрэн тусгаарлагдсан нэгжийн түвшинд тестэлсэн.

Ашигласан хэрэгслүүд: OpenJDK 17, Apache Maven, JUnit 5 (Jupiter), Mockito 5.

## Хэрхэн ажиллуулах

```bash
mvn test
```

Тэмдэглэл: starter-ийн `pom.xml` дахь `maven-compiler-plugin`-ийн анхдагч хувилбар (3.1) нь `maven.compiler.release` тохиргоог танихгүй тул Ubuntu-н Maven дээр "Source option 5 is no longer supported" алдаа өгч байсан. Иймд `maven-compiler-plugin`-ийг 3.11.0 болгож `pom.xml`-д тодорхой зааж өгсөн.

## Хэсэг A: Найман тест

Тестүүд `src/test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java` файлд байна.

| № | Тест | Ашигласан хэрэгсэл |
|---|---|---|
| A1 | Идэвхтэй оюутан бүртгүүлэхэд "OK" буцаж, `saveRegistration` дуудагдаж, и-мэйл илгээгдэнэ | `when` + `verify` |
| A2 | Байхгүй оюутанд "ERROR_NO_STUDENT"; сан руу хадгалаагүй, и-мэйл яваагүй | `verify(..., never())` |
| A3 | Идэвхгүй оюутанд "ERROR_INACTIVE_STUDENT"; и-мэйлтэй огт харьцаагүй | `verifyNoInteractions` |
| A4 | Өмнөх холбоо хичээл дутуу бол "ERROR_PREREQUISITES"; хадгалаагүй | `when` + `never` |
| A5 | И-мэйл илгээгч exception шидсэн ч бүртгэл "OK", хадгалалт хийгдсэн | `doThrow(...).when(...)` |
| A6 | null оролтод `IllegalArgumentException`; хамаарлуудтай огт харьцаагүй | `assertThrows` |
| A7 | Илгээгдсэн и-мэйлийн биед хичээлийн код орсныг шалгах | `ArgumentCaptor` |
| A8 | Гурван алдааны ангийг нэг параметржүүлсэн тестээр | `@ParameterizedTest` + `@CsvSource` |

Тест бүрийн нэр `юуг_ямарНөхцөлд_юуХүлээгдэнэ` хэв маягтай. Нэг дуудалтад matcher ашигласан бол бүх аргументад matcher ашигласан (жишээ нь `verify(emailSender).send(eq("bold@must.edu.mn"), anyString(), contains("CS101"))`).

## Тестийн тоо

`results/mvn-test.txt`-ийн сүүлийн мөрийн дагуу: **Tests run: 10, Failures: 0, Errors: 0, Skipped: 0**, **BUILD SUCCESS**.

10 тест = 7 энгийн тест (A1–A7) + A8-ийн параметржүүлсэн 3 мөр (мөр бүр тусдаа тест болж тоологдоно).

## A8: `lenient()`-ийн тайлбар

A8-д нэг тестийн метод гурван мөрөөр (NO_STUDENT, INACTIVE, PREREQUISITES) ажилладаг бөгөөд мөр бүрд `register()` өөр шалгалтын цэг дээр эрт буцдаг. Тиймээс `courseExists`, `prerequisites` зэрэг stub зарим мөрд огт дуудагдахгүй, MockitoExtension-ий strict stubs горим үүнийг `UnnecessaryStubbingException` гэж тооцож тестийг унагадаг. Иймд эдгээр stub-ыг `lenient().when(...)` хэлбэрээр бичиж "заавал ашиглагдах албагүй" гэж тэмдэглэсэн.

## Хэсэг B: Мини мутаци

Кодыг зориуд эвдэж, тестүүд үнэхээр алдааг барьж байгааг шалгав. Мутант бүрийн дараа эх кодыг `git checkout src/main`-ээр сэргээсэн.

| № | Мутаци (`CourseRegistrationService.java`) | Унасан тестүүд | Failures / Errors | Файл |
|---|---|---|---|---|
| 1 | Идэвхгүй оюутны шалгалт `if (!student.active()) ...`-ийг комментлосон | `register_inactiveStudent_returnsErrorInactiveStudent` (A3), `register_errorScenario_returnsExpectedErrorCode[2]` (A8, INACTIVE мөр) | 2 / 0 | `results/mvn-test-mutant-1.txt` |
| 2 | `repository.saveRegistration(...)` дуудалтыг комментлосон | `register_activeStudent_returnsOkSavesAndSendsEmail` (A1), `register_emailSenderThrows_stillReturnsOkAndSaves` (A5) | 2 / 0 | `results/mvn-test-mutant-2.txt` |
| 3 | Эхний `if`-ээс `\|\| courseId == null`-ийг устгасан | `register_nullArguments_throwsIllegalArgumentException` (A6) | 1 / 0 | `results/mvn-test-mutant-3.txt` |

Гурван мутант бүрд `BUILD FAILURE` гарсан бөгөөд хүснэгт нь `results/mvn-test-mutant-1..3.txt` файлуудтай таарч байна.

### Мутант бүрийн тайлбар

- **Мутант 1:** Идэвхгүй оюутны шалгалтыг устгахад `register()` дараагийн шалгалтууд руу үргэлжилнэ. A3 нь `ERROR_INACTIVE_STUDENT` хүлээсэн ч `ERROR_NO_COURSE` гарч унав (`courseExists` stub хийгдээгүй тул false). A8-ийн INACTIVE мөр `ERROR_PREREQUISITES` гарч унав.
- **Мутант 2:** `saveRegistration` дуудагдахаа больсон тул `verify(repository).saveRegistration(...)` хийдэг A1 ба A5 унав. Харин үр дүн "OK" хэвээр буцаж байсан тул `assertEquals("OK", ...)` дангаараа энэ эвдрэлийг барьж чадахгүй, зөвхөн `verify` барьсан. Энэ нь гадны нөлөөг (хадгалалт) шалгахад `verify` зайлшгүй хэрэгтэйг харуулж байна.
- **Мутант 3:** `courseId == null` шалгалтыг устгахад `register("S123", null)` exception шидэхгүй болно. A6 нь `studentId` ба `courseId` хоёрын null-ийг тус тусад нь `assertThrows`-ээр шалгадаг тул мутантыг барьсан.

### Мутацийн дүгнэлт

Гурван мутант бүгд ядаж нэг тестээр баригдсан. Мутант 3-ыг A6 анхнаасаа барьсан тул тестийн цоорхой илрээгүй бөгөөд A6-д нэмэлт засвар хийх шаардлагагүй болсон. Мутацийн дараа `UnnecessaryStubbingException` илрээгүй, бүх унасан тест `Failures` төрөлд орсон. Хүснэгт нь `results/` дахь файлуудын гаралттай таарч байгаа.

## Ерөнхий дүгнэлт

Mock нь Mockito-гоор динамикаар үүсгэсэн орлуулагч объект бөгөөд тухайн тестэд хэрэгтэй дуудалтыг урьдчилан зааж өгч, дараа нь хэрхэн дуудагдсаныг шалгах боломж олгодог. Гараар бичсэн fake нь бодитоор ажилладаг хөнгөн хэрэгжүүлэлт (жишээ нь санах ойд хадгалдаг репозитори) бөгөөд төлөв хадгалж олон тестэд дахин ашиглагдах боловч өөрийг нь бичиж, засварлах шаардлагатай. Mock нь "аль метод ямар аргументтай дуудагдсан бэ" гэсэн харилцааг, fake нь "эцсийн төлөв зөв үү" гэдгийг шалгахад илүү тохиромжтой. Энгийн утгын объект (жишээ нь `Student` record), String, List зэрэг стандарт төрлийг mock хийх ёсгүй, учир нь тэдгээр нь хурдан, урьдчилан таамаглахуйц, гадаад хамааралгүй тул mock хийх нь тестийг утгагүй төвөгтэй болгоно. Мөн тестлэж буй классаа өөрийг нь mock хийвэл бодит логик тестлэгдэхгүй. `verify`-г хэтрүүлбэл тест нь үр дүнгийн оронд кодын дотоод хэрэгжүүлэлт (аль метод ямар дарааллаар дуудагдсан) дээр хэт уяагдаж, логик өөрчлөгдөөгүй refactoring хийхэд ч унадаг эмзэг тест болдог. Иймд хадгалалт, и-мэйл илгээлт зэрэг гадны нөлөө чухал үед л `verify` хэрэглэж, бусад тохиолдолд буцаасан утгыг `assertEquals`-ээр шалгах нь зөв. Мутацийн шалгалт нь тест ногоон байх нь тест сайн гэсэн үг биш, кодыг зориуд эвдэхэд тест унадаг байх ёстойг харуулсан.

## Репозиторийн бүтэц
lab08-mockito/
├── pom.xml
├── .gitignore
├── README.md
├── results/
│ ├── mvn-test.txt
│ ├── mvn-test-mutant-1.txt
│ ├── mvn-test-mutant-2.txt
│ └── mvn-test-mutant-3.txt
└── src/
├── main/java/mn/edu/must/sqat/ (Student, StudentRepository, EmailSender, CourseRegistrationService)
└── test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java


