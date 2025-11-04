import requests
from typing import List, Optional, Dict, Any
from dataclasses import dataclass
from datetime import datetime, timedelta
import time


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

    def __make_request(self, endpoint: str, params: Optional[Dict] = None) -> Dict[str, Any]:
        url = f"{self.BASE_URL}{endpoint}"
        response = requests.get(url, params=params)
        response.raise_for_status()
        return response.json()

    def __parse_schedule(self, data: Dict[str, Any]) -> Week:
        days = []

        for day_data in data.get('days', []):
            lessons = [
                Lesson(
                    subject=lesson['subject'],
                    time_start=lesson['time_start'],
                    time_end=lesson['time_end'],
                    type=lesson['typeObj']['name'],
                    teacher=lesson.get('teachers', [{}])[0].get('full_name', '')
                    if lesson.get('teachers')
                    else '',
                    group=lesson.get('groups', [{}])[0].get('name', '')
                    if lesson.get('groups')
                    else data['group']['name'],
                    auditory=lesson.get('auditories', [{}])[0].get('name', '')
                    if lesson.get('auditories')
                    else '',
                    building=lesson.get('auditories', [{}])[0].get('building', {}).get('name', '')
                    if lesson.get('auditories')
                    else ''
                )
                for lesson in day_data.get('lessons', [])
            ]

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

    def get_week_schedule_by_group(self, group_id: int) -> Week:
        data = self.__make_request(f"scheduler/{group_id}")
        return self.__parse_schedule(data)

    def get_week_schedule_by_group_and_date(self, group_id: int, schedule_date: str) -> Week:
        data = self.__make_request(f"scheduler/{group_id}/?date={schedule_date}")
        return self.__parse_schedule(data)

    def get_semester_schedule(self, group_id: int, start_date: Optional[str] = None,
                              end_date: Optional[str] = None, max_weeks: int = 30) -> SemesterSchedule:
        """
        Получает расписание на весь семестр

        Args:
            group_id: ID группы
            start_date: Начальная дата в формате YYYY-MM-DD (если None - начнет с текущей недели)
            end_date: Конечная дата в формате YYYY-MM-DD (если None - будет искать max_weeks недель)
            max_weeks: Максимальное количество недель для поиска (по умолчанию 30 - типичная длина семестра)
        """
        weeks = []
        processed_weeks = set()

        # Начинаем с указанной даты или с текущей
        current_date = start_date or datetime.now().strftime('%Y-%m-%d')

        for week_num in range(max_weeks):
            try:
                # Получаем расписание на неделю
                week_schedule = self.get_week_schedule_by_group_and_date(group_id, current_date)

                # Проверяем, не обрабатывали ли мы уже эту неделю
                week_key = week_schedule.week.get('date_start', '')
                if week_key in processed_weeks:
                    break

                processed_weeks.add(week_key)
                weeks.append(week_schedule)

                # Переходим к следующей неделе
                current_date = self.__get_next_week_date(week_schedule.week.get('date_end', current_date))

                # Небольшая задержка чтобы не перегружать API
                time.sleep(0.1)

            except Exception as e:
                print(f"Ошибка при получении расписания на неделю {week_num + 1}: {e}")
                break

        # Определяем даты начала и конца семестра
        start_date = weeks[0].week['date_start'] if weeks else current_date
        end_date = weeks[-1].week['date_end'] if weeks else current_date

        return SemesterSchedule(
            group=weeks[0].group if weeks else {},
            weeks=weeks,
            start_date=start_date,
            end_date=end_date
        )

    def __get_next_week_date(self, current_week_end: str) -> str:
        """Получает дату начала следующей недели"""
        try:
            end_date = datetime.strptime(current_week_end, '%Y-%m-%d')
            next_week_start = end_date + timedelta(days=1)
            return next_week_start.strftime('%Y-%m-%d')
        except:
            # Если что-то пошло не так, добавляем 7 дней к текущей дате
            current_date = datetime.now()
            next_week = current_date + timedelta(days=7)
            return next_week.strftime('%Y-%m-%d')

    def get_complete_academic_year(self, group_id: int) -> List[SemesterSchedule]:
        """
        Получает расписание на весь учебный год (оба семестра)
        """
        # Первый семестр обычно начинается в сентябре
        fall_semester_start = f"{datetime.now().year}-09-01"

        # Второй семестр обычно начинается в феврале
        spring_semester_start = f"{datetime.now().year}-02-07"

        fall_semester = self.get_semester_schedule(group_id, fall_semester_start)
        spring_semester = self.get_semester_schedule(group_id, spring_semester_start)

        return [fall_semester, spring_semester]