-- =============================================
-- Лабораторная работа №5: Партиционирование в PostgreSQL
-- =============================================

-- 1. Создание партиционированной таблицы orders
-- Разбиение по диапазону дат (order_date)
CREATE TABLE orders (
    id SERIAL,
    order_date DATE NOT NULL,
    customer_id INT,
    amount DECIMAL(10,2)
) PARTITION BY RANGE (order_date);

-- 2. Создание начальных партиций (январь и февраль 2024)
CREATE TABLE orders_2024_01 PARTITION OF orders
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE orders_2024_02 PARTITION OF orders
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

-- 3. Вставка тестовых данных
INSERT INTO orders (order_date, customer_id, amount) VALUES
('2024-01-15', 1, 100.50),
('2024-01-20', 2, 250.00),
('2024-02-05', 1, 75.20),
('2024-02-18', 3, 320.00);

-- Проверка распределения данных по партициям
SELECT * FROM orders_2024_01;  -- должно быть 2 строки
SELECT * FROM orders_2024_02;  -- должно быть 2 строки

-- 4. Добавление новой партиции (март 2024)
CREATE TABLE orders_2024_03 PARTITION OF orders
    FOR VALUES FROM ('2024-03-01') TO ('2024-04-01');

-- 5. Удаление партиции (январь)
DROP TABLE orders_2024_01;  -- удаляет и структуру, и данные

-- 6. Создание индексов
-- Локальный индекс по полю order_date
CREATE INDEX idx_orders_order_date ON orders (order_date);

-- Составной индекс (глобальный по смыслу)
CREATE INDEX idx_orders_customer_date ON orders (customer_id, order_date);

-- 7. Анализ запроса с использованием индекса (при малом объёме данных)
-- Сначала обычный план (может быть Seq Scan из-за маленького объёма)
EXPLAIN ANALYZE
SELECT * FROM orders
WHERE order_date BETWEEN '2024-02-01' AND '2024-02-28';

-- Принудительное использование индекса (отключаем последовательное сканирование)
SET enable_seqscan = OFF;
EXPLAIN ANALYZE
SELECT * FROM orders
WHERE order_date BETWEEN '2024-02-01' AND '2024-02-28';
SET enable_seqscan = ON;

-- 8. Демонстрация отсечения партиций (partition pruning)
EXPLAIN ANALYZE
SELECT * FROM orders
WHERE order_date = '2024-02-15';
-- В плане видно, что сканируется только партиция orders_2024_02

-- 9. Использование составного индекса
EXPLAIN ANALYZE
SELECT * FROM orders
WHERE customer_id = 1 AND order_date >= '2024-02-01';
-- Должен использоваться индекс idx_orders_customer_date

-- 10. Просмотр списка партиций (оставшихся после удаления)
SELECT
    nmsp_parent.nspname AS parent_schema,
    parent.relname AS parent_table,
    nmsp_child.nspname AS child_schema,
    child.relname AS child_table
FROM pg_inherits
JOIN pg_class parent ON pg_inherits.inhparent = parent.oid
JOIN pg_class child ON pg_inherits.inhrelid = child.oid
JOIN pg_namespace nmsp_parent ON nmsp_parent.oid = parent.relnamespace
JOIN pg_namespace nmsp_child ON nmsp_child.oid = child.relnamespace
WHERE parent.relname = 'orders';

-- 11. Дополнительная демонстрация добавления и удаления партиции
CREATE TABLE orders_2024_04 PARTITION OF orders
    FOR VALUES FROM ('2024-04-01') TO ('2024-05-01');

-- Удаление партиции (через DROP)
DROP TABLE orders_2024_04;

-- Или через DETACH (отсоединение, данные остаются в отдельной таблице)
-- CREATE TABLE orders_2024_04 PARTITION OF orders
--     FOR VALUES FROM ('2024-04-01') TO ('2024-05-01');
-- ALTER TABLE orders DETACH PARTITION orders_2024_04;

-- 12. Статистика по строкам в партициях
SELECT 'orders_2024_02' AS partition, COUNT(*) FROM orders_2024_02
UNION ALL
SELECT 'orders_2024_03', COUNT(*) FROM orders_2024_03;

-- =============================================
-- Конец скрипта
-- =============================================