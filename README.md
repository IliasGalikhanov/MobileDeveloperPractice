# Практическая работа №6: DiffUtil и ListAdapter

## 📚 Описание проекта

**Менеджер Курсов** — Android-приложение, демонстрирующее оптимизацию списков с помощью `DiffUtil` и `ListAdapter`.

### Ключевые возможности:
- ✅ **Добавление курсов** — создание новых элементов списка
- ✅ **Удаление курсов** — удаление с подтверждением
- ✅ **Редактирование курсов** — обновление данных существующих элементов
- ✅ **Фильтрация** — по рейтингу (4.5+, 4.7+, 4.8+)
- ✅ **Сортировка** — по цене (возрастание/убывание)
- ✅ **DiffUtil** — автоматическое вычисление изменений
- ✅ **ListAdapter** — оптимизированный адаптер
- ✅ **Анимации** — плавные переходы при изменениях

---

## 🎯 Что такое DiffUtil?

### Определение

`DiffUtil` — это утилита от Android, которая вычисляет **минимальное количество изменений** между двумя списками и обновляет только измененные элементы.

### Проблема без DiffUtil

**БЕЗ DiffUtil (старый подход):**

```kotlin
// Старый список: [A, B, C, D, E]
// Новый список:  [A, B, X, D, E]  // Изменился только элемент C → X

// ❌ ПЛОХО: notifyDataSetChanged()
adapter.notifyDataSetChanged()

// Что происходит:
// 1. Перерисовываются ВСЕ элементы (A, B, C, D, E)
// 2. Нет анимаций
// 3. Мерцание экрана
// 4. Потеря состояния (например, позиция прокрутки)
// 5. Низкая производительность
```

**С DiffUtil (современный подход):**

```kotlin
// Старый список: [A, B, C, D, E]
// Новый список:  [A, B, X, D, E]  // Изменился только элемент C → X

// ✅ ХОРОШО: submitList()
adapter.submitList(newList)

// Что происходит:
// 1. DiffUtil сравнивает списки
// 2. Определяет: только элемент #2 изменился
// 3. Перерисовывается ТОЛЬКО элемент #2
// 4. Применяется анимация изменения
// 5. Остальные элементы остаются нетронутыми
```

### Как работает DiffUtil

```
Старый список          Новый список
┌─────────────┐       ┌─────────────┐
│ Course(1)   │       │ Course(1)   │  → areItemsTheSame(1,1)=true
│ price=10000 │       │ price=10000 │     areContentsTheSame=true
└─────────────┘       └─────────────┘     ✅ НЕ ОБНОВЛЯЕМ

┌─────────────┐       ┌─────────────┐
│ Course(2)   │       │ Course(2)   │  → areItemsTheSame(2,2)=true
│ price=15000 │       │ price=20000 │     areContentsTheSame=false
└─────────────┘       └─────────────┘     🔄 ОБНОВЛЯЕМ!

┌─────────────┐       
│ Course(3)   │       (удален)         → 🗑️ УДАЛЯЕМ
│ price=5000  │       
└─────────────┘       

                      ┌─────────────┐
                      │ Course(4)   │  → ➕ ДОБАВЛЯЕМ
                      │ price=8000  │
                      └─────────────┘
```

---

## 🏗️ Архитектура проекта

### Структура файлов

```
app/src/main/java/com/example/coursemanager/
├── model/
│   └── Course.kt                    # Модель данных (data class)
├── viewmodel/
│   └── CourseViewModel.kt           # ViewModel с LiveData
├── adapter/
│   └── CourseAdapter.kt             # ListAdapter + DiffUtil
└── MainActivity.kt                  # UI и взаимодействие

app/src/main/res/
├── layout/
│   ├── activity_main.xml            # Главный экран
│   ├── item_course.xml              # Элемент списка
│   └── dialog_add_course.xml        # Диалог добавления/редактирования
├── values/
│   ├── colors.xml                   # Material Design 3 цвета
│   ├── strings.xml                  # Строковые ресурсы
│   └── themes.xml                   # Светлая тема
└── values-night/
    └── themes.xml                   # Темная тема
```

---

## 🔧 Реализация DiffUtil

### 1. DiffUtil.ItemCallback в CourseAdapter

**Файл:** `adapter/CourseAdapter.kt`

```kotlin
private class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
    
    /**
     * Проверка: это один и тот же элемент?
     * Сравнивается по уникальному ID
     */
    override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Проверка: изменилось ли содержимое?
     * Вызывается только если areItemsTheSame = true
     * 
     * Data class автоматически генерирует equals()
     */
    override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem == newItem
    }
}
```

### Как data class помогает DiffUtil?

```kotlin
data class Course(
    val id: String,
    val title: String,
    val price: Double,
    val rating: Float
)

// Data class автоматически генерирует:
fun equals(other: Any?): Boolean {
    if (other !is Course) return false
    return id == other.id &&
           title == other.title &&
           price == other.price &&
           rating == other.rating
}
```

**Пример работы:**

```kotlin
val old = Course(id="1", title="Kotlin", price=10000.0, rating=4.5f)
val new = Course(id="1", title="Kotlin", price=15000.0, rating=4.5f)

areItemsTheSame(old, new)     // true  (тот же id)
areContentsTheSame(old, new)  // false (цена изменилась!)
// Результат: элемент будет обновлен
```

---

### 2. ListAdapter

**Файл:** `adapter/CourseAdapter.kt`

```kotlin
class CourseAdapter(
    private val onCourseClick: (Course) -> Unit,
    private val onCourseEdit: (Course) -> Unit,
    private val onCourseDelete: (Course) -> Unit
) : ListAdapter<Course, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {
    //  ↑                ↑                                    ↑
    //  Наследуем    ViewHolder                      DiffUtil callback
    //  ListAdapter

    // ViewHolder (хранит view элементы)
    inner class CourseViewHolder(
        private val binding: ItemCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(course: Course) {
            binding.tvCourseTitle.text = course.title
            binding.tvPrice.text = "${course.price.toInt()} ₽"
            // ... остальные поля
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        // Создается редко - только при первой прокрутке
        val binding = ItemCourseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        // Вызывается каждый раз при отображении элемента
        val course = getItem(position)  // ListAdapter.getItem()
        holder.bind(course)
    }
}
```

---

### 3. ViewModel с правильным обновлением

**Файл:** `viewmodel/CourseViewModel.kt`

⚠️ **КРИТИЧЕСКИ ВАЖНО:** При работе с `DiffUtil` нужно создавать **НОВЫЙ** список!

```kotlin
class CourseViewModel : ViewModel() {
    
    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> = _courses

    // ✅ ПРАВИЛЬНО: Добавление курса
    fun addCourse(course: Course) {
        val currentList = _courses.value.orEmpty()
        val newList = currentList + course  // Создаем НОВЫЙ список
        _courses.value = newList            // submitList() обнаружит изменения
    }

    // ✅ ПРАВИЛЬНО: Удаление курса
    fun deleteCourse(courseId: String) {
        val currentList = _courses.value.orEmpty()
        val newList = currentList.filter { it.id != courseId }  // НОВЫЙ список
        _courses.value = newList
    }

    // ✅ ПРАВИЛЬНО: Обновление курса
    fun updateCourse(updatedCourse: Course) {
        val currentList = _courses.value.orEmpty()
        val newList = currentList.map { course ->
            if (course.id == updatedCourse.id) updatedCourse else course
        }
        _courses.value = newList
    }

    // ❌ НЕПРАВИЛЬНО: Изменение существующего списка
    fun addCourseWrong(course: Course) {
        val list = _courses.value?.toMutableList() ?: mutableListOf()
        list.add(course)
        _courses.value = list  // Это тот же объект! DiffUtil не сработает
    }
}
```

**Почему важно создавать новый список?**

```kotlin
// Ситуация 1: ❌ НЕПРАВИЛЬНО
val list = mutableListOf(course1, course2)
_courses.value = list

list.add(course3)        // Изменяем тот же список
_courses.value = list    // Тот же объект!

// submitList() получает:
// oldList = [course1, course2, course3]  // ссылка на list
// newList = [course1, course2, course3]  // та же ссылка!
// oldList === newList -> НИЧЕГО НЕ ОБНОВИТСЯ!

// Ситуация 2: ✅ ПРАВИЛЬНО
val list1 = listOf(course1, course2)
_courses.value = list1

val list2 = list1 + course3  // Создаем НОВЫЙ список
_courses.value = list2

// submitList() получает:
// oldList = [course1, course2]        // ссылка на list1
// newList = [course1, course2, course3]  // ссылка на list2
// oldList !== newList -> DiffUtil РАБОТАЕТ!
```

---

### 4. MainActivity: submitList()

**Файл:** `MainActivity.kt`

```kotlin
class MainActivity : AppCompatActivity() {
    
    private val viewModel: CourseViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter

    private fun setupObservers() {
        viewModel.courses.observe(this) { courses ->
            // submitList() — ключевой метод ListAdapter
            // Внутри вызывается DiffUtil для вычисления изменений
            courseAdapter.submitList(courses)
            
            // DiffUtil работает в фоновом потоке!
            // После вычисления изменений обновляет UI в главном потоке
        }
    }

    private fun showAddCourseDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Добавить курс")
            .setView(dialogBinding.root)
            .setPositiveButton("Сохранить") { _, _ ->
                val course = createCourseFromDialog(dialogBinding)
                viewModel.addCourse(course)  // ViewModel создаст новый список
            }
            .show()
    }
}
```

---

## 📊 Сравнение: RecyclerView.Adapter vs ListAdapter

| Характеристика | RecyclerView.Adapter | ListAdapter |
|----------------|----------------------|-------------|
| **Обновление списка** | `notifyDataSetChanged()` | `submitList(newList)` |
| **DiffUtil** | Нужно реализовывать вручную | Встроен |
| **Производительность** | Низкая (перерисовка всего) | Высокая (только изменения) |
| **Анимации** | Нужно настраивать | Автоматические |
| **Код** | Больше boilerplate | Меньше кода |
| **Многопоточность** | Ручная | Автоматическая |

### Пример кода

**RecyclerView.Adapter:**
```kotlin
class OldCourseAdapter : RecyclerView.Adapter<ViewHolder>() {
    private var courses: List<Course> = emptyList()

    fun updateCourses(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()  // ❌ Перерисовка всего списка
    }

    override fun getItemCount() = courses.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(courses[position])
    }
}
```

**ListAdapter:**
```kotlin
class NewCourseAdapter : ListAdapter<Course, ViewHolder>(CourseDiffCallback()) {
    
    // Метод updateCourses() не нужен!
    // Просто вызываем submitList()
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))  // getItem() из ListAdapter
    }
}

// Использование:
adapter.submitList(newCourses)  // ✅ DiffUtil работает автоматически
```

---

## 🎬 Жизненный цикл обновления с DiffUtil

```
1. ViewModel изменяет данные
   ↓
   viewModel.addCourse(course)
   
2. ViewModel создает НОВЫЙ список
   ↓
   val newList = currentList + course
   _courses.value = newList
   
3. LiveData уведомляет Observer
   ↓
   courses.observe { courses ->
   
4. Вызывается submitList()
   ↓
   adapter.submitList(courses)
   
5. ListAdapter запускает DiffUtil в фоновом потоке
   ↓
   DiffUtil.calculateDiff(oldList, newList)
   
6. DiffUtil вызывает callback методы
   ↓
   areItemsTheSame(oldItem, newItem)
   areContentsTheSame(oldItem, newItem)
   
7. DiffUtil вычисляет изменения
   ↓
   Результат: добавлен элемент #5
   
8. Обновление UI в главном потоке
   ↓
   notifyItemInserted(5)
   
9. RecyclerView применяет анимацию
   ↓
   Элемент #5 плавно появляется
```

---

## 🚀 Преимущества DiffUtil

### 1. **Производительность**
- Обновляются только измененные элементы
- Вычисления в фоновом потоке
- Нет лишних перерисовок

### 2. **Анимации**
- Автоматические плавные переходы
- `notifyItemInserted()` — элемент появляется
- `notifyItemRemoved()` — элемент исчезает
- `notifyItemChanged()` — элемент обновляется

### 3. **Меньше кода**
```kotlin
// БЕЗ DiffUtil: ~50 строк кода
// С DiffUtil: ~10 строк кода
```

### 4. **Стабильность**
- Сохранение состояния (позиция прокрутки, выделение)
- Нет мерцания
- Корректная работа с клавиатурой

---

## 🧪 Тестирование DiffUtil

### Сценарии тестирования:

1. **Добавление элемента**
   - Нажать FAB → Заполнить форму → Сохранить
   - ✅ Элемент появляется внизу списка с анимацией

2. **Удаление элемента**
   - Нажать иконку удаления → Подтвердить
   - ✅ Элемент исчезает с анимацией

3. **Редактирование элемента**
   - Нажать иконку редактирования → Изменить цену → Сохранить
   - ✅ Только цена обновляется без перерисовки всего элемента

4. **Сортировка**
   - Нажать "Цена" → Выбрать "По возрастанию"
   - ✅ Элементы перестраиваются с анимацией

5. **Фильтрация**
   - Нажать "Рейтинг" → Выбрать "4.8 и выше"
   - ✅ Ненужные элементы исчезают, нужные остаются

---

## 🛠️ Технологии

- **Kotlin** 1.9.20
- **Android SDK** 34 (minSdk 24)
- **Gradle** 8.2.0 (Kotlin DSL)
- **RecyclerView** 1.3.2
- **ListAdapter** + **DiffUtil** (встроенные в RecyclerView)
- **Material Design 3** 1.11.0
- **ViewModel & LiveData** 2.6.2
- **ViewBinding** (включен в проекте)

---

## 📦 Установка и запуск

1. **Клонировать репозиторий:**
```bash
git clone <repository-url>
cd practice6-diffutil
```

2. **Открыть в Android Studio:**
```bash
open -a "Android Studio" .
```

3. **Gradle Sync:**
- Android Studio автоматически синхронизирует зависимости

4. **Запустить:**
- Выбрать эмулятор или физическое устройство
- Run → Run 'app'

---

## 📝 Git workflow

```bash
# 1. Создать ветку
git checkout -b feature/practice6-diffutil

# 2. Коммиты
git add .
git commit -m "Practice6: Initial project setup"
git commit -m "Practice6: Add Course model and ViewModel"
git commit -m "Practice6: Implement ListAdapter with DiffUtil"
git commit -m "Practice6: Add MainActivity with CRUD operations"

# 3. Отправить в репозиторий
git push origin feature/practice6-diffutil
```

---

## 📚 Ответы на контрольные вопросы

### 1. Зачем нужен DiffUtil?

**DiffUtil** нужен для эффективного обновления RecyclerView. Он:
- Вычисляет минимальное количество изменений между двумя списками
- Обновляет только измененные элементы вместо всего списка
- Добавляет автоматические анимации
- Работает в фоновом потоке, не блокируя UI

**Без DiffUtil:**
```kotlin
notifyDataSetChanged()  // ❌ Перерисовка всех элементов
```

**С DiffUtil:**
```kotlin
submitList(newList)  // ✅ Обновление только изменений
```

---

### 2. В чем отличие RecyclerView.Adapter от ListAdapter?

| Аспект | RecyclerView.Adapter | ListAdapter |
|--------|----------------------|-------------|
| **Хранение данных** | Вручную управляем списком | ListAdapter хранит список внутри |
| **Обновление** | `notifyDataSetChanged()` | `submitList()` |
| **DiffUtil** | Нужно реализовывать отдельно | Встроен |
| **getItemCount()** | Нужно переопределять | Автоматически |
| **getItem()** | Нужно получать из списка вручную | Метод `getItem(position)` |

**RecyclerView.Adapter:**
```kotlin
class OldAdapter : RecyclerView.Adapter<ViewHolder>() {
    private var list: List<Course> = emptyList()
    
    fun updateList(newList: List<Course>) {
        list = newList
        notifyDataSetChanged()
    }
    
    override fun getItemCount() = list.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}
```

**ListAdapter:**
```kotlin
class NewAdapter : ListAdapter<Course, ViewHolder>(DiffCallback()) {
    // list хранится внутри ListAdapter
    // getItemCount() автоматически
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))  // getItem() из ListAdapter
    }
}

// Использование:
adapter.submitList(newList)  // DiffUtil автоматически
```

---

### 3. Почему нельзя использовать notifyDataSetChanged()?

`notifyDataSetChanged()` **можно** использовать, но это **неэффективно**:

**Проблемы:**
1. **Перерисовка всего списка** — даже если изменился 1 элемент
2. **Нет анимаций** — элементы просто мгновенно меняются
3. **Мерцание** — экран "прыгает"
4. **Потеря состояния** — может сбиться позиция прокрутки
5. **Низкая производительность** — тормоза при больших списках

**Пример:**
```kotlin
// Список из 1000 элементов
val list = List(1000) { Course(...) }

// Изменяем ОДИН элемент
list[500] = updatedCourse

// ❌ ПЛОХО
adapter.notifyDataSetChanged()
// Результат: перерисовка ВСЕХ 1000 элементов!

// ✅ ХОРОШО
adapter.submitList(newList)
// Результат: обновление ТОЛЬКО элемента #500
```

---

### 4. Как DiffUtil определяет изменения в списке?

DiffUtil использует **два метода**:

**Шаг 1: `areItemsTheSame()`** — это один и тот же объект?
```kotlin
override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
    return oldItem.id == newItem.id  // Сравниваем по уникальному ID
}
```

**Шаг 2: `areContentsTheSame()`** — изменилось ли содержимое?
```kotlin
override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
    return oldItem == newItem  // Data class автоматически сравнивает все поля
}
```

**Алгоритм работы:**

```
Старый список: [Course(id=1, price=100), Course(id=2, price=200)]
Новый список:  [Course(id=1, price=150), Course(id=2, price=200)]

Для каждого элемента:
1. areItemsTheSame(old[0], new[0])
   → id=1 == id=1  ✅ true

2. areContentsTheSame(old[0], new[0])
   → Course(1,100) == Course(1,150)  ❌ false
   → Вывод: ОБНОВИТЬ элемент #0

3. areItemsTheSame(old[1], new[1])
   → id=2 == id=2  ✅ true

4. areContentsTheSame(old[1], new[1])
   → Course(2,200) == Course(2,200)  ✅ true
   → Вывод: НЕ ТРОГАТЬ элемент #1

Результат:
- Элемент #0: notifyItemChanged(0)
- Элемент #1: ничего не делаем
```

---

### 5. Какие проблемы производительности решает DiffUtil?

1. **Избыточная перерисовка**
   - Без DiffUtil: перерисовка всего списка
   - С DiffUtil: только измененные элементы

2. **Блокировка UI потока**
   - Без DiffUtil: все вычисления в главном потоке
   - С DiffUtil: вычисления в фоновом потоке

3. **Отсутствие анимаций**
   - Без DiffUtil: мгновенные изменения, мерцание
   - С DiffUtil: плавные анимации

4. **Проблемы с памятью**
   - Без DiffUtil: создание лишних ViewHolder
   - С DiffUtil: переиспользование существующих

5. **Потеря состояния**
   - Без DiffUtil: сброс позиции прокрутки, выделения
   - С DiffUtil: сохранение всего состояния

**Производительность на практике:**

```
Список из 100 элементов, изменен 1 элемент:

БЕЗ DiffUtil (notifyDataSetChanged):
- Вызовов onBindViewHolder: 100
- Время обновления: ~50ms
- Анимации: нет

С DiffUtil (submitList):
- Вызовов onBindViewHolder: 1
- Время обновления: ~5ms (вычисления в фоне)
- Анимации: да
```

---

## 📖 Дополнительные материалы

- [Официальная документация DiffUtil](https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil)
- [Официальная документация ListAdapter](https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter)
- [Android Developers: RecyclerView](https://developer.android.com/guide/topics/ui/layout/recyclerview)

---

## 👨‍💻 Автор

Практическая работа №6 — DiffUtil и ListAdapter

**Дата:** 2025
