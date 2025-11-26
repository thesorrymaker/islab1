# Лабораторная работа №1 — Разработка защищённого REST API

## Описание проекта

Это внутренний код, написанный на Java с использованием Maven для создания проекта.

---
## API — доступные эндпоинты

### `POST /auth/register`

```json
{
  "username": "yh1",
  "password": "yh123456"
  "nickname": "sandman",
  "email": "123456@example.com"
}
```

**Пример ответа (201 Created):**

```text
User registered successfully
```

### `POST /auth/login`
**Описание:** вход пользователя и получение JWT-токена.
**Пример запроса:**
```json
{
  "username": "yh1",
  "password": "yh123456"
}
```

**Пример ответа (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
}
```
### `POST /api/notes`

**Описание:** Загрузить заметки.
**Заголовок:**
Authorization: Bearer <access_token>
Content-Type: application/json
```json
{
  "content": "nice to meet u all",
}
```
**Пример ответа:**
```text
Post created with id: 1
```

### `POST /api/data?username=test`

**Описание:** возвращает защищённые данные для авторизованных пользователей.
**Заголовок:**
Authorization: Bearer <access_token>

**Пример ответа:**
```json
[
  {
    "id": 1,
    "content": "nice to meet u all",
    "profile": "sandman"
  },
  {
    "id": 2,
    "content": "I am yeheng.",
    "profile": "sandman"
  },
  {
    "id": 3,
    "content": "nice to meet u all",
    "profile": "sandman"
  }
]
```
## Реализованные меры защиты

### Защита от SQL Injection

- Мы используем Spring Data JPA, который автоматически генерирует SQL-код.
- используется ORM (Hibernate/JPA) и параметризованные запросы, что исключает возможность внедрения SQL-кода.
- Ни один SQL-запрос не формируется вручную через конкатенацию строк.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

### Защита от XSS (Cross-Site Scripting)

- сервер возвращает данные только в формате JSON, а не HTML
- Spring Security добавляет защитные HTTP-заголовки.
- API реализован как JSON-only (@RestController), сервер не рендерит HTML страницы. Все ответы сериализуются Jackson’ом в JSON, а не вставляются в HTML-шаблоны;

```java
@RestController
@RequestMapping("/api")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/data")
    public ResponseEntity<List<NoteRequest>> getPosts(@RequestParam String username) {
        return ResponseEntity.ok(noteService.getNotes(username));
    }
```
### Безопасная аутентификация и хранение паролей

- реализованы с помощью JWT-токенов
- Каждый запрос проверяется фильтром на наличие и корректность токена; неавторизованные запросы блокируются.
- сохраняются в базе данных только в хешированном виде (с помощью BCrypt).

```java
    public Note createNotes(String token, String content) {
        String username = jwtService.extractUsername(token);
        Profile profile = profileRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        Note note = Note.builder().content(content).profile(profile).build();
        return noteRepository.save(note);
    }
```

- Bean PasswordEncoder создаёт BCrypt-хешер паролей для безопасного хранения и проверки.
- Пароли не хранятся в открытом виде, а проверяются через matches.

```java
      public void register(String username, String password, String nickname, String email) {

        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder().username(username).password(passwordEncoder.encode(password)).build();
        userRepository.save(user);

        Profile profile = Profile.builder().nickname(nickname).email(email).user(user).build();
        profileRepository.save(profile);
    }
```
