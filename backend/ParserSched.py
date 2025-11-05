import requests
from typing import List, Optional, Dict, Any
from dataclasses import dataclass
from datetime import datetime, timedelta
import time
import random


@dataclass
class Faculty:
    id: int
    name: str
    abbr: str


@dataclass
class Group:
    id: int
    name: str


@dataclass
class Lesson:
    subject: str
    time_start: str
    time_end: str
    type: str
    teacher: str
    group: str
    auditory: str
    building: str


@dataclass
class Day:
    date: str
    weekday: str
    lessons: List[Lesson]


@dataclass
class Week:
    group: Dict[str, Any]
    week: Dict[str, Any]
    days: List[Day]


@dataclass
class SemesterSchedule:
    group: Dict[str, Any]
    weeks: List[Week]
    start_date: str
    end_date: str


class SchedulerParser:
    BASE_URL = "https://ruz.spbstu.ru/api/v1/ruz/"

    def __init__(self):
        self.session = requests.Session()
        # Увеличиваем таймауты и добавляем повторные попытки
        self.session.mount('https://', requests.adapters.HTTPAdapter(
            max_retries=3,
            pool_connections=10,
            pool_maxsize=30
        ))

    def __make_request(self, endpoint: str, params: Optional[Dict] = None, max_retries: int = 3) -> Dict[str, Any]:
        url = f"{self.BASE_URL}{endpoint}"

        for attempt in range(max_retries):
            try:
                response = self.session.get(
                    url,
                    params=params,
                    timeout=30,  # Увеличиваем таймаут
                    headers={
                        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
                    }
                )
                response.raise_for_status()
                return response.json()

            except requests.exceptions.RequestException as e:
                print(f"Попытка {attempt + 1}/{max_retries} не удалась для {url}: {e}")
                if attempt < max_retries - 1:
                    # Увеличиваем задержку с каждой попыткой
                    sleep_time = (2 ** attempt) + random.uniform(0.1, 0.5)
                    print(f"Ожидание {sleep_time:.2f} секунд перед повторной попыткой...")
                    time.sleep(sleep_time)
                else:
                    print(f"Все попытки не удались для {url}")
                    raise

    def __parse_schedule(self, data: Dict[str, Any]) -> Week:
        days = []

        for day_data in data.get('days', []):
            lessons = []

            for lesson in day_data.get('lessons', []):
                # Извлекаем информацию о здании
                building_name = ''
                if lesson.get('auditories'):
                    building_data = lesson['auditories'][0].get('building', {})
                    building_name = building_data.get('abbr', '')

                # Извлекаем информацию о преподавателе
                teacher_name = ''
                if lesson.get('teachers'):
                    teacher_name = lesson['teachers'][0].get('full_name', '')

                # Извлекаем информацию о группе
                group_name = ''
                if lesson.get('groups'):
                    group_name = lesson['groups'][0].get('name', '')
                else:
                    group_name = data['group'].get('name', '')

                # Извлекаем информацию об аудитории
                auditory_name = ''
                if lesson.get('auditories'):
                    auditory_name = lesson['auditories'][0].get('name', '')

                lesson_obj = Lesson(
                    subject=lesson['subject'],
                    time_start=lesson['time_start'],
                    time_end=lesson['time_end'],
                    type=lesson['typeObj']['name'],
                    teacher=teacher_name,
                    group=group_name,
                    auditory=auditory_name,
                    building=building_name
                )
                lessons.append(lesson_obj)

            days.append(Day(
                date=day_data['date'],
                weekday=day_data['weekday'],
                lessons=lessons
            ))

        return Week(
            group=data['group'],
            week=data['week'],
            days=days
        )

    def get_faculties(self) -> List[Faculty]:
        data = self.__make_request("faculties")
        return [
            Faculty(
                id=faculty['id'],
                name=faculty['name'],
                abbr=faculty['abbr']
            )
            for faculty in data.get('faculties', [])
        ]

    def get_groups_by_faculty(self, faculty_id: int) -> List[Group]:
        data = self.__make_request(f"faculties/{faculty_id}/groups")
        return [
            Group(
                id=group['id'],
                name=group['name']
            )
            for group in data.get('groups', [])
        ]

    def get_week_schedule_by_group(self, group_id: int) -> Optional[Week]:
        try:
            data = self.__make_request(f"scheduler/{group_id}")
            return self.__parse_schedule(data)
        except Exception as e:
            print(f"Ошибка получения расписания для группы {group_id}: {e}")
            return None

    def get_week_schedule_by_group_and_date(self, group_id: int, schedule_date: str) -> Optional[Week]:
        try:
            data = self.__make_request(f"scheduler/{group_id}", params={'date': schedule_date})
            return self.__parse_schedule(data)
        except Exception as e:
            print(f"Ошибка получения расписания для группы {group_id} на дату {schedule_date}: {e}")
            return None

    def get_semester_schedule(self, group_id: int, start_date: Optional[str] = None,
                              end_date: Optional[str] = None, max_weeks: int = 6) -> SemesterSchedule:
        """
        Получает расписание на указанный период
        """
        weeks = []

        # Используем дату начала или текущую дату
        current_date = start_date or datetime.now().strftime('%Y-%m-%d')

        for week_num in range(max_weeks):
            try:
                week_schedule = self.get_week_schedule_by_group_and_date(group_id, current_date)

                if not week_schedule:
                    break

                weeks.append(week_schedule)

                # Получаем дату начала следующей недели
                current_week_end = week_schedule.week.get('date_end', current_date)
                next_date = self.__get_next_week_date(current_week_end)
                current_date = next_date

                # Задержка между запросами
                sleep_time = random.uniform(1.0, 2.0)
                time.sleep(sleep_time)

            except Exception as e:
                # Продолжаем со следующей недели (добавляем 7 дней)
                try:
                    current_date_obj = datetime.strptime(current_date, '%Y-%m-%d')
                    next_date = current_date_obj + timedelta(days=7)
                    current_date = next_date.strftime('%Y-%m-%d')
                except:
                    break

        start_date = weeks[0].week['date_start'] if weeks else current_date
        end_date = weeks[-1].week['date_end'] if weeks else current_date

        return SemesterSchedule(
            group=weeks[0].group if weeks else {},
            weeks=weeks,
            start_date=start_date,
            end_date=end_date
        )

    def __get_next_week_date(self, current_week_end: str) -> str:
        """Получает дату начала следующей недели (понедельник)"""
        try:
            # Заменяем точки на дефисы, если нужно
            date_str = current_week_end.replace('.', '-')

            # Парсим дату
            end_date = datetime.strptime(date_str, '%Y-%m-%d')
            next_week_start = end_date + timedelta(days=1)
            return next_week_start.strftime('%Y-%m-%d')

        except Exception:
            # Если что-то пошло не так, добавляем 7 дней к текущей дате
            current_date = datetime.now()
            next_week = current_date + timedelta(days=7)
            return next_week.strftime('%Y-%m-%d')