import requests
from typing import List, Optional, Dict, Any
from dataclasses import dataclass

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
                    subject = lesson['subject'],
                    time_start = lesson['time_start'],
                    time_end = lesson['time_end'],
                    type = lesson['typeObj']['name'],
                    teacher = lesson.get('teachers', [{}])[0].get('full_name', '')
                        if lesson.get('teachers')
                        else '',
                    group = lesson.get('groups', [{}])[0].get('name', '')
                        if lesson.get('groups')
                        else data['group']['name'],
                    auditory = lesson.get('auditories', [{}])[0].get('name', '')
                        if lesson.get('auditories')
                        else '',
                    building = lesson.get('auditories', [{}])[0].get('building', {}).get('name', '')
                        if lesson.get('auditories')
                        else ''
                )
                    for lesson in day_data.get('lessons', [])
            ]

            days.append(Day(
                date = day_data['date'],
                weekday = day_data['weekday'],
                lessons = lessons
            ))

        return Week(
            group = data['group'],
            week = data['week'],
            days = days
        )

    def get_faculties(self) -> List[Faculty]:
        data = self.__make_request("faculties")
        return [
            Faculty(
                id = faculty['id'],
                name = faculty['name'],
                abbr = faculty['abbr']
            )
                for faculty in data.get('faculties', [])
        ]

    def get_groups_by_faculty(self, faculty_id: int) -> List[Group]:
        data = self.__make_request(f"faculties/{faculty_id}/groups")
        return [
            Group(
                id = group['id'],
                name = group['name']
            )
                for group in data.get('groups', [])
        ]

    def get_week_schedule_by_group(self, group_id: int) -> Week:
        data = self.__make_request(f"scheduler/{group_id}")
        return self.__parse_schedule(data)

    def get_week_schedule_by_group_and_date(self, group_id: int, schedule_date: str) ->  Week:
        data = self.__make_request(f"scheduler/{group_id}/?date={schedule_date}")
        return self.__parse_schedule(data)