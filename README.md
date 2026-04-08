# YBVAutoRestart

Плагин для `Paper`, который автоматически перезапускает сервер по расписанию и выполняет действия по таймеру через систему `actions`.

По умолчанию текстовые сообщения используют `MiniMessage`. При необходимости можно переключиться на `legacy` формат.

## Что умеет

- Перезапускать сервер по расписанию.
- Выполнять действия на нужной секунде до рестарта.
- Показывать статус следующего рестарта.
- Поддерживает команды `now`, `delay`, `stop`, `reload`.

## Команды

- `/ybvautorestart status`
  Показывает, когда будет следующий рестарт.
- `/ybvautorestart reload`
  Перезагружает конфигурацию.
- `/ybvautorestart now`
  Запускает принудительный рестарт.
- `/ybvautorestart delay <секунды>`
  Откладывает активный рестарт.
- `/ybvautorestart stop`
  Отменяет активный рестарт.

## Конфиг

Главный файл: `src/main/resources/config.yml`

Основные разделы:

- `formatting.serializer`
  Глобальный форматтер текста: `MINIMESSAGE` или `LEGACY`.
- `schedule.restarts`
  Расписание рестартов.
- `actions`
  Список действий, которые выполняются на определённом времени до рестарта.
- `admin.now-countdown-seconds`
  Через сколько секунд делать рестарт для команды `now`.

## Форматирование сообщений

Глобальный форматтер задаётся так:

```yml
formatting:
  serializer: MINIMESSAGE
```

Поддерживаются значения:

- `MINIMESSAGE`
- `LEGACY`

### MiniMessage

Это режим по умолчанию.

Примеры:

```yml
messages:
  prefix: '<light_purple><bold>YBVAutoRestart</bold></light_purple>'
```

```yml
- '[time:60] message {PREFIX} <white>Рестарт через <light_purple>{TIME}</light_purple>.</white>'
```

### Legacy

Если нужен старый формат с `&`-кодами:

```yml
formatting:
  serializer: LEGACY
```

Примеры:

```yml
messages:
  prefix: '&d&lYBVAutoRestart'
```

```yml
- '[time:60] message {PREFIX} &fРестарт через &d{TIME}&f.'
```

Важно:

- в режиме `minimessage` legacy-строки автоматически не конвертируются,
- `sound` actions не зависят от выбранного режима форматирования.

## Формат расписания

Примеры:

```yml
schedule:
  restarts:
    - 'DAILY;06:00'
    - 'MONDAY;12:00'
    - 'FRIDAY;18;30'
```

Поддерживаются форматы:

- `DAILY;HH:MM`
- `DAY;HH:MM`
- `DAY;HH;MM`

Где `DAY` это:

- `MONDAY`
- `TUESDAY`
- `WEDNESDAY`
- `THURSDAY`
- `FRIDAY`
- `SATURDAY`
- `SUNDAY`

## Формат actions

Каждое действие задаётся одной строкой:

```yml
- '[time:N] <type> <payload>'
```

Где:

- `N` — количество секунд до рестарта
- `type` — тип действия
- `payload` — данные действия

### Поддерживаемые типы

#### `message`

Отправляет сообщение в чат всем игрокам.

```yml
- '[time:60] message {PREFIX} <white>Рестарт через <light_purple>{TIME}</light_purple>.</white>'
```

#### `actionbar`

Отправляет сообщение в action bar всем игрокам.

```yml
- '[time:10] actionbar {PREFIX} <white>Рестарт через <light_purple>{TIME}</light_purple>.</white>'
```

#### `sound`

Проигрывает звук всем игрокам.

Формат:

```yml
- '[time:60] sound <SOUND> <volume> <pitch>'
```

Пример:

```yml
- '[time:60] sound ENTITY_PLAYER_LEVELUP 1.0 1.0'
```

#### `command`

Выполняет серверную команду от имени консоли.

```yml
- '[time:0] command save-all'
- '[time:0] command restart'
```

## Плейсхолдеры

В `message` и `actionbar` доступны:

- `{PREFIX}` — префикс из `messages.yml`
- `{TIME}` — форматированное оставшееся время
- `{SECONDS}` — оставшиеся секунды

## Пример

```yml
formatting:
  serializer: MINIMESSAGE

schedule:
  restarts:
    - 'DAILY;06:00'

actions:
  - '[time:1800] message {PREFIX} <white>Рестарт сервера через <light_purple>{TIME}</light_purple>.</white>'
  - '[time:1800] sound ENTITY_PLAYER_LEVELUP 1.0 1.0'
  - '[time:60] actionbar {PREFIX} <white>Рестарт через <light_purple>{TIME}</light_purple>.</white>'
  - '[time:10] message {PREFIX} <white>Рестарт сервера через <light_purple>{TIME}</light_purple>.</white>'
  - '[time:0] message {PREFIX} <white>Сервер перезапускается.</white>'
  - '[time:0] command save-all'
  - '[time:0] command restart'

admin:
  now-countdown-seconds: 10
```
