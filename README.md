# Практическая работа №7: Room Database и CRUD операции

## 📚 Описание проекта

**База Данных Курсов** — Android-приложение, демонстрирующее локальное хранение данных с помощью **Room Database** (обертка над SQLite).

### Ключевые возможности:
- ✅ **CREATE** — добавление курсов в БД
- ✅ **READ** — чтение всех курсов с автоматическим обновлением
- ✅ **UPDATE** — редактирование существующих курсов
- ✅ **DELETE** — удаление курсов из БД
- ✅ **Статистика** — количество курсов, средняя цена, всего студентов
- ✅ **Сохранение после перезапуска** — данные не теряются
- ✅ **MVVM + Repository** — правильная архитектура

---

## 🎯 Что такое Room?

### Определение

**Room** — это библиотека от Google для работы с локальной базой данных SQLite в Android.

Room является **ORM (Object-Relational Mapping)** — переводит объекты Kotlin в таблицы SQL.

### Зачем нужен Room?

**БЕЗ Room (чистый SQLite):**
```kotlin
// ❌ ПЛОХО: Куча boilerplate кода
val db = dbHelper.writableDatabase
val values = ContentValues().apply {
    put("title", "Kotlin")
    put("price", 10000)
}
val id = db.insert("courses", null, values)

// Чтение
val cursor = db.query("courses", null, null, null, null, null, null)
while (cursor.moveToNext()) {
    val title = cursor.getString(cursor.getColumnIndex("title"))
    // ... много кода
}
```

**С Room:**
```kotlin
// ✅ ХОРОШО: Чистый и простой код
@Dao
interface CourseDao {
    @Insert
    suspend fun insert(course: Course): Long
    
    @Query("SELECT * FROM courses")
    fun getAllCourses(): LiveData<List<Course>>
}

// Использование
courseDao.insert(course)
courseDao.getAllCourses() // LiveData автоматически обновляется!
```

---

## 🏗️ Архитектура Room

Room состоит из **3 основных компонентов**:

```
┌─────────────────────────────────────┐
│         MainActivity (UI)           │
│                                     │
│  - Отображает данные                │
│  - Реагирует на действия             │
└──────────────┬──────────────────────┘
               │
               ↓ observe()
┌──────────────────────────────────────┐
│          ViewModel                   │
│                                      │
│  - LiveData<List<Course>>            │
│  - insert(), update(), delete()      │
└──────────────┬───────────────────────┘
               │
               ↓ вызывает методы
┌──────────────────────────────────────┐
│          Repository                  │
│                                      │
│  - Прослойка между ViewModel и DAO   │
│  - Может объединять БД + API         │
└──────────────┬───────────────────────┘
               │
               ↓ делегирует операции
┌──────────────────────────────────────┐
│      CourseDao (interface)           │  ← 1️⃣ DAO
│                                      │
│  @Query("SELECT * FROM courses")     │
│  fun getAllCourses(): LiveData<...>  │
│                                      │
│  @Insert suspend fun insert(...)     │
│  @Update suspend fun update(...)     │
│  @Delete suspend fun delete(...)     │
└──────────────┬───────────────────────┘
               │
               ↓ генерирует SQL
┌──────────────────────────────────────┐
│         AppDatabase                  │  ← 2️⃣ Database
│                                      │
│  @Database(entities = [Course::class])│
│  abstract class AppDatabase          │
│  abstract fun courseDao(): CourseDao │
└──────────────┬───────────────────────┘
               │
               ↓ работает с
┌──────────────────────────────────────┐
│      Course (@Entity)                │  ← 3️⃣ Entity
│                                      │
│  @PrimaryKey(autoGenerate = true)    │
│  val id: Long                        │
│  val title: String                   │
│  val price: Double                   │
└──────────────────────────────────────┘
               │
               ↓
┌──────────────────────────────────────┐
│       SQLite Database                │
│    (course_database.db)              │
│                                      │
│  Таблица: courses                    │
│  ┌────┬────────┬──────┬──────┐      │
│  │ id │ title  │price │ ...  │      │
│  ├────┼────────┼──────┼──────┤      │
│  │ 1  │ Kotlin │10000 │ ...  │      │
│  │ 2  │ Compose│15000 │ ...  │      │
│  └────┴────────┴──────┴──────┘      │
└──────────────────────────────────────┘
```

---

## 📋 1. Entity - Модель данных

**Файл:** `data/entity/Course.kt`

```kotlin
@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "price")
    val price: Double,
    
    // ... остальные поля
)
```

### Аннотации:

| Аннотация | Описание |
|-----------|----------|
| `@Entity` | Указывает, что это таблица БД |
| `@PrimaryKey` | Первичный ключ (уникальный ID) |
| `autoGenerate = true` | Room автоматически генерирует ID |
| `@ColumnInfo` | Настройка колонки (имя, тип) |

### Как это превращается в SQL?

```kotlin
@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    val price: Double
)
```

**Room генерирует:**
```sql
CREATE TABLE courses (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    title TEXT NOT NULL,
    price REAL NOT NULL
);
```

---

## 🔧 2. DAO - Data Access Object

**Файл:** `data/dao/CourseDao.kt`

DAO — это **интерфейс** с методами для работы с БД.

```kotlin
@Dao
interface CourseDao {
    
    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course): Long
    
    // READ
    @Query("SELECT * FROM courses ORDER BY created_at DESC")
    fun getAllCourses(): LiveData<List<Course>>
    
    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): Course?
    
    // UPDATE
    @Update
    suspend fun update(course: Course): Int
    
    @Query("UPDATE courses SET price = :newPrice WHERE id = :courseId")
    suspend fun updatePrice(courseId: Long, newPrice: Double)
    
    // DELETE
    @Delete
    suspend fun delete(course: Course): Int
    
    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteById(courseId: Long): Int
}
```

### Ключевые аннотации:

| Аннотация | SQL | Описание |
|-----------|-----|----------|
| `@Insert` | `INSERT INTO courses VALUES (...)` | Вставка записи |
| `@Update` | `UPDATE courses SET ... WHERE id = ?` | Обновление |
| `@Delete` | `DELETE FROM courses WHERE id = ?` | Удаление |
| `@Query` | Любой SQL | Кастомный запрос |

### suspend vs LiveData

**suspend функции:**
```kotlin
@Insert
suspend fun insert(course: Course): Long

// Использование (в корутине):
viewModelScope.launch {
    val id = courseDao.insert(course)  // Фоновый поток
}
```

**LiveData:**
```kotlin
@Query("SELECT * FROM courses")
fun getAllCourses(): LiveData<List<Course>>

// Использование:
courseDao.getAllCourses().observe(this) { courses ->
    // Автоматически обновляется при изменениях в БД!
}
```

---

## 🗄️ 3. Database - Главный класс БД

**Файл:** `data/database/AppDatabase.kt`

```kotlin
@Database(
    entities = [Course::class],  // Список таблиц
    version = 1,                 // Версия схемы БД
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun courseDao(): CourseDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "course_database"  // Имя файла БД
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### Singleton Pattern

**Зачем?** Гарантирует, что существует только **один экземпляр** БД.

```
1-й вызов: getDatabase() → создает БД → INSTANCE = db
2-й вызов: getDatabase() → INSTANCE уже есть → возвращает существующий
```

**Без Singleton:**
- Каждый раз создается новая БД
- Конфликты при записи
- Утечки памяти

---

## 📦 4. Repository - Прослойка между ViewModel и DAO

**Файл:** `data/repository/CourseRepository.kt`

```kotlin
class CourseRepository(private val courseDao: CourseDao) {
    
    val allCourses: LiveData<List<Course>> = courseDao.getAllCourses()
    
    suspend fun insert(course: Course): Long {
        return courseDao.insert(course)
    }
    
    suspend fun update(course: Course): Int {
        return courseDao.update(course)
    }
    
    suspend fun delete(course: Course): Int {
        return courseDao.delete(course)
    }
}
```

### Зачем нужен Repository?

**БЕЗ Repository:**
```
ViewModel → DAO → SQLite
```
Проблемы:
- ViewModel знает о Room
- Сложно добавить API
- Сложно тестировать

**С Repository:**
```
ViewModel → Repository → (DAO + API + Cache)
```
Преимущества:
- ViewModel не знает об источнике данных
- Легко добавить API: `Repository → DAO + Retrofit`
- Легко тестировать (mock Repository)
- Единое место для логики данных

### Пример с API + БД:

```kotlin
class CourseRepository(
    private val courseDao: CourseDao,
    private val api: CourseApi
) {
    // Сначала показываем данные из БД (быстро)
    val allCourses: LiveData<List<Course>> = courseDao.getAllCourses()
    
    // Затем обновляем из API (медленно, но актуально)
    suspend fun refreshCourses() {
        try {
            val freshCourses = api.getCourses()  // Из сети
            courseDao.insertAll(freshCourses)    // Сохраняем в БД
        } catch (e: Exception) {
            // Обработка ошибки
        }
    }
}
```

---

## 🎛️ 5. ViewModel - Управление данными и UI

**Файл:** `viewmodel/CourseViewModel.kt`

```kotlin
class CourseViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: CourseRepository
    val allCourses: LiveData<List<Course>>
    
    init {
        val database = AppDatabase.getDatabase(application)
        val courseDao = database.courseDao()
        repository = CourseRepository(courseDao)
        allCourses = repository.allCourses
    }
    
    fun insert(course: Course) {
        viewModelScope.launch {
            repository.insert(course)
        }
    }
    
    fun update(course: Course) {
        viewModelScope.launch {
            repository.update(course)
        }
    }
    
    fun delete(course: Course) {
        viewModelScope.launch {
            repository.delete(course)
        }
    }
}
```

### viewModelScope

**viewModelScope** — корутина, привязанная к ViewModel.

Автоматически отменяется при уничтожении ViewModel → **нет утечек памяти**.

```kotlin
fun insert(course: Course) {
    viewModelScope.launch {  // Запускается в фоновом потоке
        val id = repository.insert(course)
        println("Вставлен курс с ID: $id")
    }  // Автоматически отменяется при onCleared()
}
```

---

## 🔄 CRUD операции в действии

### CREATE - Добавление курса

**Поток данных:**
```
User → FAB click
  ↓
MainActivity → showAddCourseDialog()
  ↓
Dialog → Save button
  ↓
viewModel.insert(course)
  ↓
viewModelScope.launch {
  ↓
  repository.insert(course)
    ↓
    courseDao.insert(course)
      ↓
      Room → SQL: INSERT INTO courses VALUES (...)
        ↓
        SQLite Database ← Данные сохранены
      ↓
    LiveData автоматически уведомляет Observer
  ↓
}
  ↓
MainActivity.allCourses.observe { courses ->
  ↓
  adapter.submitList(courses)  // DiffUtil обновляет UI
    ↓
    RecyclerView ← Новый курс появляется
}
```

### READ - Чтение курсов

**Автоматическое обновление:**
```kotlin
// В MainActivity
viewModel.allCourses.observe(this) { courses ->
    courseAdapter.submitList(courses)
}

// LiveData подписывается на изменения в БД
// Любое изменение → автоматическое обновление UI!
```

### UPDATE - Обновление курса

```kotlin
// Пользователь редактирует курс
val updatedCourse = course.copy(price = 20000.0)
viewModel.update(updatedCourse)

// Room обновляет БД
// LiveData уведомляет
// DiffUtil обновляет только измененный элемент
```

### DELETE - Удаление курса

```kotlin
viewModel.delete(course)

// Room удаляет из БД
// LiveData уведомляет
// DiffUtil анимирует удаление
```

---

## 💾 Как данные сохраняются после перезапуска?

### Где хранится БД?

**SQLite файл:** `/data/data/com.example.coursedatabase/databases/course_database.db`

Этот файл **сохраняется на диске** и не удаляется при закрытии приложения.

### Жизненный цикл данных:

```
1. Первый запуск
   ↓
   AppDatabase создается
   ↓
   DatabaseCallback.onCreate() вызывается
   ↓
   Вставляются демо-курсы
   ↓
   Данные записываются в course_database.db

2. Закрытие приложения
   ↓
   Activity уничтожается
   ViewModel уничтожается
   LiveData отписывается
   ↓
   НО! course_database.db остается на диске

3. Повторный запуск
   ↓
   AppDatabase.getDatabase() → находит существующий файл
   ↓
   Читает данные из course_database.db
   ↓
   LiveData доставляет данные в UI
   ↓
   Все курсы восстановлены!
```

### Удаление БД

**Вручную:**
```kotlin
context.deleteDatabase("course_database")
```

**Автоматически:**
- Удаление приложения → БД удаляется
- Очистка данных в настройках → БД удаляется

---

## 🔀 Интеграция с DiffUtil

Room + ListAdapter + DiffUtil = **идеальная комбинация**!

```kotlin
// Room возвращает новый список
val newList = courseDao.getAllCourses()

// LiveData уведомляет
allCourses.observe { courses ->
    // submitList() → DiffUtil вычисляет изменения
    adapter.submitList(courses)
}

// DiffUtil сравнивает по ID из Room
class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
    override fun areItemsTheSame(old: Course, new: Course): Boolean {
        return old.id == new.id  // ID из @PrimaryKey
    }
    
    override fun areContentsTheSame(old: Course, new: Course): Boolean {
        return old == new  // Data class сравнивает все поля
    }
}
```

---

## 📊 Структура проекта

```
app/src/main/java/com/example/coursedatabase/
├── data/
│   ├── entity/
│   │   └── Course.kt                 # @Entity - таблица БД
│   ├── dao/
│   │   └── CourseDao.kt              # @Dao - SQL операции
│   ├── database/
│   │   └── AppDatabase.kt            # @Database - главный класс БД
│   └── repository/
│       └── CourseRepository.kt       # Repository - прослойка
├── viewmodel/
│   └── CourseViewModel.kt            # ViewModel - управление данными
├── adapter/
│   └── CourseAdapter.kt              # Adapter с DiffUtil
└── MainActivity.kt                   # UI

app/src/main/res/
├── layout/
│   ├── activity_main.xml             # Главный экран
│   ├── item_course.xml               # Элемент списка
│   └── dialog_add_course.xml         # Диалог добавления
└── values/
    ├── strings.xml
    ├── colors.xml
    └── themes.xml
```

---

## 🛠️ Технологии

- **Kotlin** 1.9.20
- **Room** 2.6.1 (SQLite ORM)
- **KSP** 1.9.20-1.0.14 (Kotlin Symbol Processing для Room)
- **Coroutines** 1.7.3 (Асинхронность)
- **LiveData** 2.6.2 (Реактивные данные)
- **ViewModel** 2.6.2 (MVVM)
- **ListAdapter** + **DiffUtil** (Оптимизация списков)
- **Material Design 3** 1.11.0

---

## 📦 Зависимости

```kotlin
dependencies {
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")  // Coroutines support
    ksp("androidx.room:room-compiler:2.6.1")        // Annotation processor
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
}
```

---

## 🧪 Тестирование CRUD

### 1. CREATE - Добавление

**Действия:**
1. Запустить приложение
2. Нажать FAB (+)
3. Заполнить форму
4. Нажать "Сохранить"

**Ожидаемый результат:**
- ✅ Курс появляется в списке
- ✅ Snackbar: "Курс добавлен в БД (ID: 8)"
- ✅ Статистика обновляется
- ✅ После перезапуска курс остается

### 2. READ - Чтение

**Действия:**
1. Перезапустить приложение
2. Просмотреть список

**Ожидаемый результат:**
- ✅ Все курсы отображаются
- ✅ Данные соответствуют БД
- ✅ ID курсов отображаются

### 3. UPDATE - Обновление

**Действия:**
1. Нажать иконку редактирования
2. Изменить цену
3. Нажать "Сохранить"

**Ожидаемый результат:**
- ✅ Цена обновляется в списке
- ✅ DiffUtil анимирует изменение
- ✅ После перезапуска новая цена сохранена

### 4. DELETE - Удаление

**Действия:**
1. Нажать иконку удаления
2. Подтвердить удаление

**Ожидаемый результат:**
- ✅ Курс исчезает из списка
- ✅ DiffUtil анимирует удаление
- ✅ Статистика обновляется
- ✅ После перезапуска курс отсутствует

### 5. Проверка сохранения

**Действия:**
1. Добавить курс
2. Закрыть приложение (kill process)
3. Открыть приложение снова

**Ожидаемый результат:**
- ✅ Добавленный курс присутствует
- ✅ Все данные восстановлены

---

## 📝 Ответы на контрольные вопросы

### 1. Что такое Room?

**Room** — это библиотека ORM (Object-Relational Mapping) от Google для работы с SQLite в Android.

Room **переводит объекты Kotlin в таблицы SQL** и наоборот.

**Компоненты:**
- **Entity** — таблица БД (Kotlin class → SQL table)
- **DAO** — методы для работы с БД (Kotlin functions → SQL queries)
- **Database** — главный класс БД (управляет подключением)

**Преимущества перед чистым SQLite:**
- Меньше boilerplate кода
- Проверка SQL на этапе компиляции
- Интеграция с LiveData и Coroutines
- Автоматические миграции

---

### 2. Что делает DAO?

**DAO (Data Access Object)** — интерфейс с методами для работы с БД.

Room **автоматически генерирует реализацию** DAO.

**Функции DAO:**

1. **CRUD операции:**
```kotlin
@Insert suspend fun insert(course: Course)
@Update suspend fun update(course: Course)
@Delete suspend fun delete(course: Course)
```

2. **Запросы к БД:**
```kotlin
@Query("SELECT * FROM courses")
fun getAllCourses(): LiveData<List<Course>>

@Query("SELECT * FROM courses WHERE id = :id")
suspend fun getCourseById(id: Long): Course?
```

3. **Кастомные обновления:**
```kotlin
@Query("UPDATE courses SET price = :newPrice WHERE id = :courseId")
suspend fun updatePrice(courseId: Long, newPrice: Double)
```

**Зачем интерфейс?** Room генерирует реализацию на этапе компиляции.

---

### 3. Что такое CRUD?

**CRUD** — акроним для 4 основных операций с данными:

| Операция | SQL | Room | Описание |
|----------|-----|------|----------|
| **C**reate | INSERT | @Insert | Создание новой записи |
| **R**ead | SELECT | @Query | Чтение данных |
| **U**pdate | UPDATE | @Update | Обновление записи |
| **D**elete | DELETE | @Delete | Удаление записи |

**Пример в проекте:**

**CREATE:**
```kotlin
@Insert suspend fun insert(course: Course): Long
// SQL: INSERT INTO courses VALUES (...)
```

**READ:**
```kotlin
@Query("SELECT * FROM courses")
fun getAllCourses(): LiveData<List<Course>>
// SQL: SELECT * FROM courses
```

**UPDATE:**
```kotlin
@Update suspend fun update(course: Course): Int
// SQL: UPDATE courses SET title=?, price=? WHERE id=?
```

**DELETE:**
```kotlin
@Delete suspend fun delete(course: Course): Int
// SQL: DELETE FROM courses WHERE id=?
```

---

### 4. Почему используется Repository?

**Repository** — прослойка между ViewModel и источниками данных.

**Архитектура БЕЗ Repository:**
```
ViewModel → DAO → SQLite
```

**Проблемы:**
1. ViewModel знает о Room
2. Нельзя добавить API
3. Сложно тестировать
4. Дублирование кода

**Архитектура С Repository:**
```
ViewModel → Repository → (DAO + API + Cache)
```

**Преимущества:**

1. **Абстракция источника данных:**
```kotlin
// ViewModel не знает откуда данные (БД? API? Cache?)
class CourseViewModel(repository: CourseRepository) {
    val courses = repository.allCourses  // Откуда? Не важно!
}
```

2. **Легко добавить API:**
```kotlin
class CourseRepository(
    private val dao: CourseDao,
    private val api: CourseApi  // Добавили API!
) {
    suspend fun refresh() {
        val courses = api.getCourses()  // Из сети
        dao.insertAll(courses)          // В БД
    }
}
```

3. **Легко тестировать:**
```kotlin
// Mock Repository для тестов
class FakeRepository : CourseRepository {
    override fun getAllCourses() = MutableLiveData(fakeData)
}
```

4. **Единое место для логики:**
```kotlin
suspend fun addCourseWithValidation(course: Course): Result<Long> {
    // Валидация
    if (course.price < 0) return Result.failure(...)
    
    // Вставка
    val id = dao.insert(course)
    return Result.success(id)
}
```

---

### 5. Как данные сохраняются после перезапуска?

**Механизм сохранения:**

1. **SQLite файл на диске:**
```
Путь: /data/data/com.example.coursedatabase/databases/course_database.db
```

2. **Первый запуск:**
```
AppDatabase создается
  ↓
DatabaseCallback.onCreate()
  ↓
Вставляются демо-курсы
  ↓
Записываются в course_database.db (на диске)
```

3. **Закрытие приложения:**
```
Activity → onDestroy()
ViewModel → onCleared()
LiveData → отписывается
БД → закрывается

НО! Файл course_database.db остается на диске
```

4. **Повторный запуск:**
```
AppDatabase.getDatabase(context)
  ↓
Room.databaseBuilder(...).build()
  ↓
Проверяет: существует ли course_database.db?
  ↓
ДА → открывает существующий файл
  ↓
Читает данные из БД
  ↓
LiveData доставляет в UI
  ↓
Данные восстановлены!
```

**Когда БД удаляется?**
- Удаление приложения
- Очистка данных в настройках
- `context.deleteDatabase("course_database")`

**Вывод:** Room сохраняет данные в **файл на диске**, который сохраняется между запусками приложения.

---

## 🎓 Дополнительные материалы

- [Официальная документация Room](https://developer.android.com/training/data-storage/room)
- [Coroutines with Room](https://developer.android.com/kotlin/coroutines)
- [Repository Pattern](https://developer.android.com/codelabs/android-room-with-a-view-kotlin)
- [MVVM Architecture](https://developer.android.com/topic/architecture)

---

## 👨‍💻 Автор

Практическая работа №7 — Room Database и CRUD

**Дата:** 2025
