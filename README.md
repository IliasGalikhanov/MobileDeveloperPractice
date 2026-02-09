# Практическая работа №5: RecyclerView и Adapter

## 📱 Описание проекта

Android-приложение "Course Catalog" - каталог онлайн-курсов с использованием:
- **RecyclerView** для отображения списков
- **ListAdapter** с **DiffUtil** для эффективных обновлений
- **ViewHolder** паттерн для переиспользования view
- **MVVM** архитектура
- **Material Design 3**

## ✨ Функциональность

- ✅ Список из 12 курсов в RecyclerView
- ✅ Каждый курс: название, описание, преподаватель, цена, рейтинг
- ✅ Клик по курсу → детальная информация в диалоге
- ✅ Фильтрация по рейтингу (4.5+, 4.7+, 4.8+)
- ✅ Сортировка по цене (возрастание/убывание)
- ✅ Счётчик найденных курсов
- ✅ Material Design 3 UI
- ✅ Dark/Light theme поддержка

## 🏗️ Архитектура RecyclerView

### Компоненты:
1. **RecyclerView** - контейнер для отображения списка
2. **LayoutManager** - управляет расположением элементов (LinearLayoutManager)
3. **Adapter** - связывает данные с View
4. **ViewHolder** - хранит ссылки на View элементы
5. **DiffUtil** - вычисляет изменения между списками

### Схема работы:
```
ViewModel → LiveData<List<Course>> → CourseAdapter → ViewHolder → RecyclerView
```

## 📂 Структура проекта

```
app/src/main/
├── java/com/example/coursecatalog/
│   ├── MainActivity.kt              # RecyclerView setup
│   ├── model/
│   │   └── Course.kt                # Data class
│   ├── viewmodel/
│   │   └── CourseViewModel.kt       # State management
│   └── adapter/
│       └── CourseAdapter.kt         # Adapter + ViewHolder
└── res/
    └── layout/
        ├── activity_main.xml        # RecyclerView container
        └── item_course.xml          # Item layout
```

## 🔧 RecyclerView реализация

### 1. Setup в Activity:
```kotlin
recyclerView.apply {
    layoutManager = LinearLayoutManager(context)
    adapter = courseAdapter
    setHasFixedSize(true)
}
```

### 2. Adapter с ViewHolder:
```kotlin
class CourseAdapter : ListAdapter<Course, CourseViewHolder>(DiffCallback()) {
    inner class CourseViewHolder(binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root)
    
    override fun onCreateViewHolder() { /* создание ViewHolder */ }
    override fun onBindViewHolder() { /* привязка данных */ }
}
```

### 3. DiffUtil для оптимизации:
```kotlin
class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
    override fun areItemsTheSame() = oldItem.id == newItem.id
    override fun areContentsTheSame() = oldItem == newItem
}
```

## 🎯 Обработка кликов

Клик передаётся через lambda в конструктор Adapter:
```kotlin
CourseAdapter { course ->
    viewModel.selectCourse(course)
}
```

## 📊 Технологии

- Kotlin 1.9.20
- RecyclerView 1.3.2
- ViewModel & LiveData 2.6.2
- Material Design 3
- ViewBinding
- Kotlin DSL (build.gradle.kts)

## 🚀 Установка

1. Открыть в Android Studio
2. Gradle Sync
3. Run на эмуляторе/устройстве

## 📝 Git

```bash
git checkout -b feature/practice5-recyclerview
git commit -m "feat: setup RecyclerView with Adapter"
git commit -m "feat: implement ViewHolder pattern"
git commit -m "feat: add click handling and filters"
git push origin feature/practice5-recyclerview
```

## 📖 Ответы на контрольные вопросы

### 1. Для чего используется RecyclerView?

RecyclerView - современный компонент для отображения больших списков данных с высокой производительностью через переиспользование view элементов.

### 2. Чем RecyclerView отличается от ListView?

- ViewHolder обязателен (в ListView опционален)
- LayoutManager для гибкого расположения
- ItemDecoration для кастомизации
- ItemAnimator для анимаций
- Лучшая производительность
- Обязательное переиспользование view

### 3. Какова роль Adapter в RecyclerView?

Adapter - мост между данными и RecyclerView:
- Создаёт ViewHolder (onCreateViewHolder)
- Привязывает данные к view (onBindViewHolder)
- Сообщает количество элементов (getItemCount)
- Обрабатывает обновления через DiffUtil

### 4. Зачем используется ViewHolder?

ViewHolder хранит ссылки на view элементы, избегая повторных findViewById. Это значительно улучшает производительность при прокрутке больших списков.

### 5. Как обрабатываются события клика?

Через lambda callback в конструкторе Adapter или setOnClickListener в ViewHolder. Клик передаётся в ViewModel для обработки бизнес-логики.
