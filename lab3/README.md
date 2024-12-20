# Описание

Веб-приложение, принимает координаты начала и конца линий, выполняет отрисовку линий с помощью:

<ul>
    <li>пошагового метода</li>
    <li>метода ЦДА</li>
    <li>методов Брезенхема для линий или окружностей</li>
</ul>

### Цель работы

Разработать веб-приложение для иллюстрации работы базовых растровых алгоритмов.

### Задачи работы

1. Реализовать пошаговый алгоритм.
2. Разработать алгоритм Брезенхема для построения отрезка.
3. Разработать алгоритм Брезенхема для построения окружности.
4. Реализовать алгоритм ЦДА для построения отрезка.
5. Разработать пользовательский интерфейс, включающий масштабирование, вывод системы координат, осей, линий сетки и их
   подписей.
6. Произвести логирование и временные замеры работы алгоритмов.

### Использованные средства разработки

<ul>
    <li>Kotlin react + js</li>
</ul>

### Результаты

- Реализованы перечисленные выше алгоритмы.
- Реализован пользовательский интерфейс, удовлетворяющий всем требованиям.
- Оформлен отчёт.
- Проведено ручное тестирование
- Реализовано логирование пошагового метода в консоль

### Вывод

Временные результаты:

- Среднее время выполнения для пошагового метода: 8.192 милисекунды
- Среднее время выполнения для Брезенхема: 0.34 микросекунд
- Среднее время выполнения для ЦДА метода: 0.143 милисекунды
- Среднее время выполнения для отрисовки окружности: 0.62 микросекунд