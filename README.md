# SwiftCodeParser

SwiftCodeParser to aplikacja do zarządzania danymi dotyczącymi SWIFT code. Program umożliwia wykonywanie operacji CRUD (Create, Read, Update, Delete) na danych SWIFT code, umożliwiając:
- Pobieranie danych na podstawie kodu SWIFT,
- Pobieranie danych na podstawie kodu kraju ISO2,
- Dodawanie nowych danych SWIFT code do bazy,
- Usuwanie danych na podstawie SWIFT code.

## Wymagania

Aby uruchomić projekt, potrzebujesz:

- **Java 17+** (zalecane użycie JDK 17)
- **Baza danych MySQL**
- **Maven** do zarządzania zależnościami i kompilowaniem projektu
- Opcjonalnie **JUnit 5** i **Mockito** do uruchamiania testów jednostkowych (jeśli chcesz uruchomić testy)

## Instalacja

### 1. Skonfiguruj środowisko Java

Jeśli nie masz zainstalowanego JDK 17 lub wyższego, pobierz go stąd: [https://adoptopenjdk.net/](https://adoptopenjdk.net/).

### 2. Pobierz projekt

Pobierz lub sklonuj repozytorium

### 3. Skonfiguruj swoje ustawienia

**csv.filepath=D:\\SwiftCodeParser\\Main\\Data\\dane.csv** -- lokalizacja pliku csv z danymi SWIFT

**log.filepath=D:\\SwiftCodeParser\\logs\\app.log**   -- lokalizacja w której mają być umieszczone logi

**db.url=jdbc:mysql://localhost:3306/swift_db**  -- url do połączenia z Twoją bazą

**db.user=root**  -- login użytkownika

**db.password=admin**  -- hasło użytkownika

### 4.Zastosowanie
**Endpoint 1: Pobierz SWIFT code na podstawie kodu kraju ISO2**
GET: /v1/swift-codes/{iso2-code}

Przykład:

GET http://localhost:8080/v1/swift-codes/PL

**Endpoint 2: Pobierz dane na podstawie SWIFT code**
GET: /v1/swift-codes/{swift-code}

Przykład:

GET http://localhost:8080/v1/swift-codes/AMNMBGS1XXX

**Endpoint 3: Dodaj nowy SWIFT code**
POST: /v1/swift-codes

Body:

{

    "address": "New address",
    
    "bankName": "New Bank",
    
    "countryISO2": "PL",
    
    "countryName": "Poland",
    
    "isHeadquarter": true,
    
    "swiftCode": "NEWCODE123"
    
}

**Endpoint 4: Usuń SWIFT code**
DELETE: /v1/swift-codes/{swift-code}

Przykład:

DELETE http://localhost:8080/v1/swift-codes/NEWCODE123
