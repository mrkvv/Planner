import random

import psycopg2
from datetime import datetime, timedelta
from typing import List
import time
import socket
from psycopg2 import OperationalError, InterfaceError

from ParserCal import GoogleCalendarParser, CalendarEvent
from ParserSched import SchedulerParser, Week, Lesson, Faculty, Group, SemesterSchedule


class DatabaseManager:
    def __init__(self):
        self.db_config = {
            'host': 'aws-1-eu-north-1.pooler.supabase.com',
            'port': 5432,
            'database': 'postgres',
            'user': 'postgres.pjcbyabqlgpjvkozojvc',
            'password': '*',
        }
        self.connection = None
        self.max_retries = 3
        self.retry_delay = 2

    def connect(self):
        try:
            ssl_config = {
                'sslmode': 'require',
            }
            full_config = {**self.db_config, **ssl_config}
            self.connection = psycopg2.connect(**full_config)
            self.connection.autocommit = False
            print("Успешное подключение к Supabase через session pooler")
            return True
        except Exception as e:
            print(f"Ошибка подключения к Supabase: {e}")
            return False

    def reconnect(self):
        """Переподключается к базе данных"""
        print("Попытка переподключения к базе данных...")
        self.disconnect()
        time.sleep(self.retry_delay)
        return self.connect()

    def ensure_connection(self):
        """Проверяет соединение и переподключается при необходимости"""
        try:
            if self.connection and not self.connection.closed:
                with self.connection.cursor() as cursor:
                    cursor.execute("SELECT 1")
                return True
            else:
                return self.reconnect()
        except (OperationalError, InterfaceError):
            return self.reconnect()

    def disconnect(self):
        if self.connection:
            try:
                self.connection.close()
                print("Отключение от Supabase")
            except:
                pass
            finally:
                self.connection = None

    def add_place_column_to_schedule(self):
        """Добавляет поле place в таблицу schedule, если его нет"""
        if not self.ensure_connection():
            print("Не удалось подключиться для добавления поля place")
            return False

        try:
            with self.connection.cursor() as cursor:
                # Проверяем, существует ли уже поле place
                cursor.execute("""
                    SELECT column_name
                    FROM information_schema.columns
                    WHERE table_name = 'schedule' AND column_name = 'place'
                """)
                if not cursor.fetchone():
                    # Добавляем поле place
                    cursor.execute("ALTER TABLE schedule ADD COLUMN place VARCHAR(100)")
                    self.connection.commit()
                    print("Поле place успешно добавлено в таблицу schedule")
                else:
                    print("Поле place уже существует в таблице schedule")
                return True
        except Exception as e:
            print(f"Ошибка добавления поля place: {e}")
            self.connection.rollback()
            return False

    def check_calendar_event_exists(self, event: CalendarEvent) -> bool:
        """Проверяет, существует ли уже такое событие в календаре"""
        if not self.ensure_connection():
            return False

        check_sql = """
        SELECT COUNT(*) FROM calendar_events
        WHERE title = %s AND date = %s AND start_time = %s AND end_time = %s AND calendar_name = %s
        """

        try:
            with self.connection.cursor() as cursor:
                cursor.execute(check_sql, (
                    event.title,
                    event.date,
                    event.start_time,
                    event.end_time,
                    event.calendar_name
                ))
                count = cursor.fetchone()[0]
                return count > 0
        except Exception as e:
            print(f"Ошибка проверки события календаря: {e}")
            return False

    def insert_calendar_events(self, events: List[CalendarEvent]):
        if not self.ensure_connection():
            print("Не удалось подключиться к БД для вставки событий календаря")
            return

        if not events:
            print("Нет событий для вставки в календарь")
            return

        # Используем UPSERT для обновления существующих записей
        upsert_sql = """
        INSERT INTO calendar_events
        (title, description, date, start_time, end_time, location, creator, calendar_name)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        ON CONFLICT (title, date, start_time, end_time, calendar_name)
        DO UPDATE SET
            description = EXCLUDED.description,
            location = EXCLUDED.location,
            creator = EXCLUDED.creator
        """

        try:
            with self.connection.cursor() as cursor:
                inserted_count = 0
                updated_count = 0

                for event in events:
                    # Проверяем, существует ли уже такое событие
                    cursor.execute("""
                        SELECT COUNT(*) FROM calendar_events
                        WHERE title = %s AND date = %s AND start_time = %s AND end_time = %s AND calendar_name = %s
                    """, (
                        event.title,
                        event.date,
                        event.start_time,
                        event.end_time,
                        event.calendar_name
                    ))
                    exists = cursor.fetchone()[0] > 0

                    cursor.execute(upsert_sql, (
                        event.title,
                        event.description,
                        event.date,
                        event.start_time,
                        event.end_time,
                        event.location,
                        event.creator,
                        event.calendar_name
                    ))

                    if exists:
                        updated_count += 1
                    else:
                        inserted_count += 1

                self.connection.commit()
                print(f"Календарь: добавлено {inserted_count} новых событий, обновлено {updated_count} существующих")

        except Exception as e:
            print(f"Ошибка вставки событий календаря: {e}")
            self.connection.rollback()

    def insert_semester_schedule(self, semester_schedule: SemesterSchedule, group_id: int):
        """
        Вставляет или обновляет расписание на весь семестр в базу данных
        """
        if not self.ensure_connection():
            print(f"❌ Не удалось подключиться к БД для группы {group_id}")
            return 0

        if not semester_schedule.weeks:
            print(f"⚠️ Для группы {group_id} нет данных за семестр")
            return 0

        upsert_sql = """
        INSERT INTO schedule
        (group_id, date, weekday, subject, type, start_time, end_time, teacher, audithory, place)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        ON CONFLICT (group_id, date, start_time, end_time, subject)
        DO UPDATE SET
            weekday = EXCLUDED.weekday,
            type = EXCLUDED.type,
            teacher = EXCLUDED.teacher,
            audithory = EXCLUDED.audithory,
            place = EXCLUDED.place
        """

        for attempt in range(self.max_retries):
            try:
                with self.connection.cursor() as cursor:
                    inserted_count = 0
                    updated_count = 0
                    batch_count = 0

                    for week in semester_schedule.weeks:
                        for day in week.days:
                            for lesson in day.lessons:
                                building = getattr(lesson, 'building', '') or ''

                                # Проверяем, существует ли уже такое занятие
                                cursor.execute("""
                                    SELECT COUNT(*) FROM schedule
                                    WHERE group_id = %s AND date = %s AND start_time = %s AND end_time = %s AND subject = %s
                                """, (
                                    group_id,
                                    day.date,
                                    lesson.time_start,
                                    lesson.time_end,
                                    lesson.subject
                                ))
                                exists = cursor.fetchone()[0] > 0

                                # Выполняем UPSERT
                                cursor.execute(upsert_sql, (
                                    group_id,
                                    day.date,
                                    day.weekday,
                                    lesson.subject,
                                    lesson.type,
                                    lesson.time_start,
                                    lesson.time_end,
                                    lesson.teacher,
                                    lesson.auditory,
                                    building
                                ))

                                if exists:
                                    updated_count += 1
                                else:
                                    inserted_count += 1

                                batch_count += 1

                                # Коммитим каждые 50 записей
                                if batch_count >= 50:
                                    self.connection.commit()
                                    batch_count = 0
                                    if not self.ensure_connection():
                                        return inserted_count + updated_count

                    # Финальный коммит
                    self.connection.commit()
                    return inserted_count + updated_count

            except (OperationalError, InterfaceError) as e:
                print(f"🔄 Попытка {attempt + 1}/{self.max_retries} для группы {group_id} не удалась: {e}")
                if attempt < self.max_retries - 1:
                    time.sleep(self.retry_delay)
                    if not self.ensure_connection():
                        continue
                else:
                    print(f"💥 Все попытки для группы {group_id} не удались")
                    return 0
            except Exception as e:
                print(f"💥 Неожиданная ошибка для группы {group_id}: {e}")
                self.connection.rollback()
                return 0

        return 0

    def create_unique_constraints(self):
        """Создает уникальные ограничения в таблицах для предотвращения дубликатов"""
        if not self.ensure_connection():
            print("Не удалось подключиться для создания ограничений")
            return False

        try:
            with self.connection.cursor() as cursor:
                # Создаем уникальное ограничение для calendar_events
                cursor.execute("""
                    DO $$
                    BEGIN
                        IF NOT EXISTS (
                            SELECT 1 FROM pg_constraint
                            WHERE conname = 'unique_calendar_event'
                        ) THEN
                            ALTER TABLE calendar_events
                            ADD CONSTRAINT unique_calendar_event
                            UNIQUE (title, date, start_time, end_time, calendar_name);
                        END IF;
                    END $$;
                """)

                # Создаем уникальное ограничение для schedule
                cursor.execute("""
                    DO $$
                    BEGIN
                        IF NOT EXISTS (
                            SELECT 1 FROM pg_constraint
                            WHERE conname = 'unique_schedule_lesson'
                        ) THEN
                            ALTER TABLE schedule
                            ADD CONSTRAINT unique_schedule_lesson
                            UNIQUE (group_id, date, start_time, end_time, subject);
                        END IF;
                    END $$;
                """)

                self.connection.commit()
                print("Уникальные ограничения созданы или уже существуют")
                return True

        except Exception as e:
            print(f"Ошибка создания уникальных ограничений: {e}")
            self.connection.rollback()
            return False

    def cleanup_duplicate_schedule_entries(self):
        """Очищает дубликаты в таблице расписания (на всякий случай)"""
        if not self.ensure_connection():
            print("Не удалось подключиться для очистки дубликатов")
            return

        cleanup_sql = """
        DELETE FROM schedule
        WHERE id IN (
            SELECT id FROM (
                SELECT id, ROW_NUMBER() OVER (
                    PARTITION BY group_id, date, start_time, end_time, subject
                    ORDER BY id
                ) as rn
                FROM schedule
            ) t
            WHERE t.rn > 1
        )
        """

        try:
            with self.connection.cursor() as cursor:
                cursor.execute("SELECT COUNT(*) FROM schedule")
                before_count = cursor.fetchone()[0]

                cursor.execute(cleanup_sql)
                deleted_count = cursor.rowcount

                self.connection.commit()

                cursor.execute("SELECT COUNT(*) FROM schedule")
                after_count = cursor.fetchone()[0]

                print(f"Очистка дубликатов расписания: удалено {deleted_count} записей")
                print(f"Было: {before_count}, стало: {after_count} записей")

        except Exception as e:
            print(f"Ошибка очистки дубликатов расписания: {e}")
            self.connection.rollback()

    def cleanup_duplicate_calendar_events(self):
        """Очищает дубликаты в таблице событий календаря (на всякий случай)"""
        if not self.ensure_connection():
            print("Не удалось подключиться для очистки дубликатов календаря")
            return

        cleanup_sql = """
        DELETE FROM calendar_events
        WHERE id IN (
            SELECT id FROM (
                SELECT id, ROW_NUMBER() OVER (
                    PARTITION BY title, date, start_time, end_time, calendar_name
                    ORDER BY id
                ) as rn
                FROM calendar_events
            ) t
            WHERE t.rn > 1
        )
        """

        try:
            with self.connection.cursor() as cursor:
                cursor.execute("SELECT COUNT(*) FROM calendar_events")
                before_count = cursor.fetchone()[0]

                cursor.execute(cleanup_sql)
                deleted_count = cursor.rowcount

                self.connection.commit()

                cursor.execute("SELECT COUNT(*) FROM calendar_events")
                after_count = cursor.fetchone()[0]

                print(f"Очистка дубликатов календаря: удалено {deleted_count} записей")
                print(f"Было: {before_count}, стало: {after_count} записей")

        except Exception as e:
            print(f"Ошибка очистки дубликатов календаря: {e}")
            self.connection.rollback()

    def insert_faculties_and_groups(self, scheduler_parser: SchedulerParser):
        if not self.ensure_connection():
            print("Не удалось подключиться к БД для вставки факультетов и групп")
            return []

        try:
            faculties = scheduler_parser.get_faculties()
            faculty_sql = """
            INSERT INTO faculties (id, name, abbr)
            VALUES (%s, %s, %s)
            ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, abbr = EXCLUDED.abbr
            """

            with self.connection.cursor() as cursor:
                for faculty in faculties:
                    cursor.execute(faculty_sql, (faculty.id, faculty.name, faculty.abbr))

                group_sql = """
                INSERT INTO groups (id, name, faculty_id)
                VALUES (%s, %s, %s)
                ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, faculty_id = EXCLUDED.faculty_id
                """

                all_groups = []
                for faculty in faculties:
                    try:
                        groups = scheduler_parser.get_groups_by_faculty(faculty.id)
                        all_groups.extend(groups)
                        for group in groups:
                            cursor.execute(group_sql, (group.id, group.name, faculty.id))
                        print(f"Факультет {faculty.name}: {len(groups)} групп")

                        self.connection.commit()

                    except Exception as e:
                        print(f"Ошибка получения групп для факультета {faculty.name}: {e}")
                        self.connection.rollback()

                print(f"Вставлено {len(faculties)} факультетов и {len(all_groups)} групп в Supabase")
                return all_groups

        except Exception as e:
            print(f"Ошибка вставки факультетов и групп: {e}")
            self.connection.rollback()
            return []

    def get_all_group_ids(self):
        if not self.ensure_connection():
            print("Не удалось подключиться к БД для получения групп")
            return []

        try:
            with self.connection.cursor() as cursor:
                cursor.execute("SELECT id FROM groups ORDER BY id")
                group_ids = [row[0] for row in cursor.fetchall()]
                return group_ids
        except Exception as e:
            print(f"Ошибка получения групп из базы: {e}")
            return []

    def check_tables_exist(self):
        if not self.ensure_connection():
            print("Не удалось подключиться к БД для проверки таблиц")
            return False

        tables = ['calendar_events', 'faculties', 'groups', 'schedule']
        missing_tables = []

        try:
            with self.connection.cursor() as cursor:
                for table in tables:
                    cursor.execute("""
                        SELECT EXISTS (
                            SELECT FROM information_schema.tables
                            WHERE table_name = %s
                        )
                    """, (table,))
                    exists = cursor.fetchone()[0]
                    if not exists:
                        missing_tables.append(table)

                if missing_tables:
                    print(f"Отсутствуют таблицы: {missing_tables}")
                    return False
                else:
                    print("Все необходимые таблицы существуют")
                    return True
        except Exception as e:
            print(f"Ошибка проверки таблиц: {e}")
            return False


def verify_connection():
    host = "aws-1-eu-north-1.pooler.supabase.com"
    try:
        ip = socket.gethostbyname(host)
        print(f"DNS разрешен (session pooler): {ip}")

        conn = psycopg2.connect(
            host=host,
            port=5432,
            database="postgres",
            user="postgres.pjcbyabqlgpjvkozojvc",
            password="yebkiSQDpswd",
            sslmode='require'
        )
        print("Подключение к БД через session pooler успешно!")
        conn.close()
        return True

    except socket.gaierror as e:
        print(f"Ошибка DNS (session pooler): {e}")
        return False
    except psycopg2.OperationalError as e:
        print(f"Ошибка подключения к БД (session pooler): {e}")
        return False
    except Exception as e:
        print(f"Другая ошибка (session pooler): {e}")
        return False


def get_all_groups_semester_schedule(scheduler_parser: SchedulerParser, db_manager: DatabaseManager,
                                     max_groups: int = None, delay: float = 5.0):
    print("Получаем расписание с ТЕКУЩЕЙ ДАТЫ для всех групп...")

    all_group_ids = db_manager.get_all_group_ids()

    if not all_group_ids:
        print("Не найдено групп в базе данных")
        return

    if max_groups:
        all_group_ids = all_group_ids[:max_groups]

    print(f"Всего групп для обработки: {len(all_group_ids)}")

    total_lessons = 0
    successful_groups = 0
    failed_groups = 0
    total_weeks = 0

    # Используем текущую дату
    current_date = datetime.now().strftime('%Y-%m-%d')

    for i, group_id in enumerate(all_group_ids, 1):
        try:
            # Получаем расписание с ТЕКУЩЕЙ ДАТЫ
            semester_schedule = scheduler_parser.get_semester_schedule(
                group_id=group_id,
                start_date=current_date,
                max_weeks=6
            )

            if semester_schedule.weeks:
                lessons_count = db_manager.insert_semester_schedule(semester_schedule, group_id)

                if lessons_count > 0:
                    successful_groups += 1
                    total_lessons += lessons_count
                    total_weeks += len(semester_schedule.weeks)

                    # Подсчитываем общее количество занятий
                    total_semester_lessons = sum(
                        len(day.lessons) for week in semester_schedule.weeks for day in week.days)

                    print(f"✅ Группа {i}/{len(all_group_ids)} (ID: {group_id}): {lessons_count} занятий за {len(semester_schedule.weeks)} недель")
                else:
                    print(f"○ Группа {i}/{len(all_group_ids)} (ID: {group_id}): нет новых занятий")
            else:
                print(f"○ Группа {i}/{len(all_group_ids)} (ID: {group_id}): нет данных")

        except Exception as e:
            failed_groups += 1
            print(f"❌ Группа {i}/{len(all_group_ids)} (ID: {group_id}): ошибка - {e}")

        if i < len(all_group_ids):
            current_delay = random.uniform(3.0, 5.0)
            time.sleep(current_delay)

        if i % 20 == 0:
            db_manager.ensure_connection()

    print(f"\n🎯 ФИНАЛЬНАЯ СТАТИСТИКА:")
    print(f"   ✅ Успешно обработано: {successful_groups} групп")
    print(f"   ❌ С ошибками: {failed_groups} групп")
    print(f"   📅 Всего недель расписания: {total_weeks}")
    print(f"   📚 Всего занятий записано в БД: {total_lessons}")

def main():
    calendar_parser = GoogleCalendarParser()
    scheduler_parser = SchedulerParser()

    db_manager = DatabaseManager()

    try:
        if not db_manager.connect():
            print("Не удалось подключиться к базе данных")
            return

        if not db_manager.check_tables_exist():
            print("Не все таблицы существуют. Проверьте структуру БД.")
            return

        print("НАЧИНАЕМ СБОР ДАННЫХ В SUPABASE...")
        print("=" * 50)

        # ДОБАВЛЯЕМ ПОЛЕ place В ТАБЛИЦУ schedule
        print("Добавляем поле place в таблицу schedule...")
        if not db_manager.add_place_column_to_schedule():
            print("Не удалось добавить поле place. Продолжаем без него...")

        print("Создание уникальных ограничений для предотвращения дубликатов...")
        db_manager.create_unique_constraints()

        print("Очистка существующих дубликатов...")
        db_manager.cleanup_duplicate_calendar_events()
        db_manager.cleanup_duplicate_schedule_entries()

        print("\nПолучаем события из Google Calendar...")
        today = datetime.now()
        first_day = today.replace(day=1)
        if today.month == 12:
            last_day = today.replace(year=today.year + 1, month=1, day=1) - timedelta(days=1)
        else:
            last_day = today.replace(month=today.month + 1, day=1) - timedelta(days=1)

        time_min = first_day.strftime('%Y-%m-%dT00:00:00Z')
        time_max = last_day.strftime('%Y-%m-%dT23:59:59Z')

        calendar_events = []
        for calendar in calendar_parser.CALENDARS:
            events = calendar_parser.getEvents(
                calendar_id=calendar['id'],
                calendar_name=calendar['name'],
                time_min=time_min,
                time_max=time_max
            )
            calendar_events.extend(events)
            print(f"{calendar['name']}: {len(events)} событий")

        db_manager.insert_calendar_events(calendar_events)

        print("\nПолучаем факультеты и группы из СПбПУ...")
        all_groups = db_manager.insert_faculties_and_groups(scheduler_parser)

        if all_groups:
            get_all_groups_semester_schedule(scheduler_parser, db_manager)

        print("\nВСЕ ДАННЫЕ УСПЕШНО ЗАГРУЖЕНЫ В SUPABASE!")

        if db_manager.ensure_connection():
            with db_manager.connection.cursor() as cursor:
                cursor.execute("SELECT COUNT(*) FROM calendar_events")
                calendar_count = cursor.fetchone()[0]

                cursor.execute("SELECT COUNT(*) FROM schedule")
                schedule_count = cursor.fetchone()[0]

                cursor.execute("SELECT COUNT(*) FROM faculties")
                faculties_count = cursor.fetchone()[0]

                cursor.execute("SELECT COUNT(*) FROM groups")
                groups_count = cursor.fetchone()[0]

                cursor.execute("SELECT COUNT(DISTINCT group_id) FROM schedule")
                groups_with_schedule = cursor.fetchone()[0]

                print(f"\nСТАТИСТИКА SUPABASE:")
                print(f"   • Событий календаря: {calendar_count}")
                print(f"   • Занятий расписания (ВЕСЬ СЕМЕСТР): {schedule_count}")
                print(f"   • Факультетов: {faculties_count}")
                print(f"   • Групп: {groups_count}")
                print(f"   • Групп с расписанием: {groups_with_schedule}")

    except Exception as e:
        print(f"Критическая ошибка: {e}")
    finally:
        db_manager.disconnect()


if __name__ == "__main__":
    verify_connection()
    main()