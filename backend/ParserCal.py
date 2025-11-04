import requests
from typing import List, Dict, Any
from dataclasses import dataclass
from datetime import datetime
import time


@dataclass
class CalendarEvent:
    title: str
    description: str
    date: str
    start_time: str
    end_time: str
    location: str
    creator: str
    calendar_name: str


class GoogleCalendarParser:
    BASE_URL = "https://www.googleapis.com/calendar/v3/calendars/"
    API_KEY = "*"

    CALENDARS = [
        {"id": "event@profunion.pro", "name": "ПРОФ.event"},
        {"id": "e65c064f7156dee361f77505e6c87b320d3b1bb8eaf95168e71f69b2894e4554@group.calendar.google.com",
         "name": "ОИ 'Адаптеры'"},
        {"id": "701e3f8ccf62f723720dd9ea713a974a680b561723328bfc5ac0a6da98bbb96f@group.calendar.google.com",
         "name": "ПРОФ.ГИ"},
        {"id": "b434418e8164fc8411abcad66f7ba590612aa167202a8c470abe9437a9bf346a@group.calendar.google.com",
         "name": "ПРОФ.ИБСиБ"},
        {"id": "f2a04cc46a39dd0d645532a2376bd386e42ea7f71cd68e6ab2ff84f1396454a0@group.calendar.google.com",
         "name": "ПРОФ.ИКНК"},
        {"id": "dab1bde56f260f63acaf32d57c366bad501f2d5dbd2381490801d7f51f48f762@group.calendar.google.com",
         "name": "ПРОФ.ИММиТ"},
        {"id": "f2d5e7ff86160640828dfa17cae44ace3df3e66acb0c34c70c31fb113fe45ffa@group.calendar.google.com",
         "name": "ПРОФ.ИПМЭиТ"},
        {"id": "e995d854e7deb1b6cb3662d31683efafa5ec7d014ad84e3ed53b4022e414b6b1@group.calendar.google.com",
         "name": "ПРОФ.ИСИ"},
        {"id": "f13dcab3456fc00690ef6b363077856241268d7641ae6d5d4733a7229ef00ee7@group.calendar.google.com",
         "name": "ПРОФ.ИСПО"},
        {"id": "8cc29ba3ab96034a67fdac57b52f8a968c9593ba5c64f0f47c27957986072263@group.calendar.google.com",
         "name": "ПРОФ.ИЭ"},
        {"id": "c8e31c9f2b9bf0d009664a6fbd179a203b0da1044a00a9698756aa0dd58ad8e7@group.calendar.google.com",
         "name": "ПРОФ.ИЭиТ"},
        {"id": "30353cdb176b181d631aff30f191f1770d60a069b676e2c7f34345d8705a0849@group.calendar.google.com",
         "name": "ПРОФ.ФизМех"},
        {"id": "f96940bfa37036d579912a36b347b6e3e309c90838d6dd20a130bbe2cef78d43@group.calendar.google.com",
         "name": "ПРОФ.hh"},
        {"id": "518e8b273e83a0a1256303081c3c9768180e08e29077ca78d2078a18d3d54be1@group.calendar.google.com",
         "name": "ПРОФ.life"},
        {"id": "6fb4cd295ed9df148c6cb43795ab6b9fc85ac7036f5d8ef75acac7a25a252f45@group.calendar.google.com",
         "name": "Прочие"},
        {"id": "2d4b68f83d02d70648d8903ad218d523adf43070ca79c723360f2f9c47d23029@group.calendar.google.com",
         "name": "СуперКульторги"},
    ]

    def __init__(self):
        self.session = requests.Session()

    def getEvents(self, calendar_id: str, calendar_name: str,
                  time_min: str, time_max: str,
                  max_results: int = 2500) -> List[CalendarEvent]:
        params = {
            'key': self.API_KEY,
            'maxResults': max_results,
            'singleEvents': True,
            'orderBy': 'startTime',
            'timeMin': time_min,
            'timeMax': time_max
        }

        url = f"{self.BASE_URL}{calendar_id}/events"

        try:
            response = self.session.get(url, params=params)
            if response.status_code == 200:
                data = response.json()
                return self.parseEvents(data, calendar_name)
            else:
                print(f"Ошибка {response.status_code} для календаря {calendar_name}")
                return []

        except Exception as e:
            print(f"Ошибка при получении событий из {calendar_name}: {e}")
            return []

    def parseEvents(self, data: Dict[str, Any], calendar_name: str) -> List[CalendarEvent]:
        events = []

        for event_data in data.get('items', []):
            if event_data.get('status') == 'cancelled':
                continue

            start_data = event_data.get('start', {})
            end_data = event_data.get('end', {})

            if 'dateTime' in start_data:
                try:
                    start_datetime = datetime.fromisoformat(start_data['dateTime'].replace('Z', '+00:00'))
                    end_datetime = datetime.fromisoformat(end_data['dateTime'].replace('Z', '+00:00'))
                    date = start_datetime.strftime('%Y-%m-%d')
                    start_time = start_datetime.strftime('%H:%M')
                    end_time = end_datetime.strftime('%H:%M')
                except:
                    date = start_data.get('date', '')
                    start_time = '00:00'
                    end_time = '23:59'
            else:
                date = start_data.get('date', '')
                start_time = '00:00'
                end_time = '23:59'

            events.append(CalendarEvent(
                title=event_data.get('summary', 'Без названия'),
                description=event_data.get('description', ''),
                date=date,
                start_time=start_time,
                end_time=end_time,
                location=event_data.get('location', ''),
                creator=event_data.get('creator', {}).get('email', 'Неизвестно'),
                calendar_name=calendar_name
            ))

        return events

    def getAllEvents(self, time_min: str, time_max: str) -> List[CalendarEvent]:
        all_events = []

        for calendar in self.CALENDARS:
            # ИСПРАВЛЕНИЕ: вызываем getEvents вместо get_events_from_calendar
            events = self.getEvents(
                calendar_id=calendar['id'],
                calendar_name=calendar['name'],
                time_min=time_min,
                time_max=time_max
            )
            all_events.extend(events)
            time.sleep(0.1)

        all_events.sort(key=lambda x: (x.date, x.start_time))

        return all_events
