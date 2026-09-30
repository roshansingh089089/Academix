# Academix

Academix is a modular student-data management application for coaching institutes. It provides database-backed search and filtering, paginated student management, CSV/Excel imports, duplicate discovery, bulk changes, exports, import history, and a responsive SaaS dashboard.

## Architecture

- `frontend/` — React + Vite dashboard using Axios, Lucide icons, and Recharts
- `backend/` — Java 21 Spring Boot modular monolith with controller/service/repository layering
- SQLite — a local Flyway-managed database with focused indexes and no database server

The API always paginates student lists (50 records by default, 100 maximum). Search and filters are composed as SQL predicates; records are never loaded wholesale into the browser or filtered in Java.

## Requirements

- Java 21
- Node.js 20+
- No external database installation or account

## Database setup

No setup is required. On first startup, Academix creates `data/` if needed and Flyway initializes:

```text
academix/data/academix.db
```

The database uses SQLite WAL mode, foreign-key enforcement, a 5-second busy timeout, and a conservative single-connection Hikari pool for predictable single-user operation. The database path can optionally be overridden:

```bash
export ACADEMIX_DB_PATH=/absolute/path/to/academix.db
```

## Run locally

Backend:

```bash
cd backend
./gradlew bootRun
```

Frontend, in another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. No PostgreSQL service, username, password, or database creation is needed. The UI uses representative dashboard data if the API is offline; when the backend is available it automatically uses live statistics and search results.

## API overview

- `GET /api/students` — paginated search, filtering, and sorting
- `GET/POST /api/students/{id}` — student detail and updates
- `POST /api/students/bulk-update` and `/bulk-delete`
- `GET /api/students/duplicates` — duplicate phone groups
- `POST /api/imports` — batched CSV/XLS/XLSX import
- `GET /api/imports` — import history
- `GET /api/dashboard/stats` — efficient aggregate dashboard data

Student list filters include gender, age range, location, school, class, course, batch, stream, academic year, admission dates, status, and source. Use `q` for the global search and `page`, `size`, `sort`, and `direction` for result navigation.

Global search remains database-side and performs case-insensitive partial matching with `LOWER(column) LIKE ?` across full name, student ID, phone numbers, email, and guardian phone. Filtering, sorting, counts, and pagination are executed by SQLite—not in Java or React.

## Import workflow

Upload `.csv`, `.xls`, or `.xlsx` files from **Import Data**. Common source headings such as “Student Name”, “Mobile No”, “Town”, “Class”, and “Course Name” are normalized automatically. Records are persisted in 500-row batches, validation failures are counted, and the job is retained in import history. Maximum upload size defaults to 100 MB and can be changed with `MAX_UPLOAD_SIZE`.

## Security and operations

The local database files and WAL sidecars are gitignored. Parameters are bound through JPA, stack traces are omitted from API responses, and full personal records are not logged. Authentication is intentionally deferred, but web/API boundaries are separated so authorization can be added cleanly.

## Current limitations and next improvements

- Large Excel files currently use Apache POI's workbook reader; switch to an event/SAX reader for very large XLSX inputs.
- The first import iteration auto-maps common headings; a persisted, user-configurable mapping wizard is the next extension.
- Background jobs, downloadable rejected rows, merge review, and true streaming exports are natural production follow-ups.
- SQLite is ideal for the intended local, single-user deployment. A server database would still be preferable for concurrent multi-user or network deployments.
- Leading-wildcard partial search is database-side but cannot use a normal B-tree index as efficiently as prefix search; SQLite FTS5 is the natural future option if measured million-row search latency requires it.

## Verification

```bash
cd backend
./gradlew clean test
./gradlew build

cd ../frontend
npm run build
```

The backend integration suite covers CRUD, partial case-insensitive search, combined filters, pagination, sorting, CSV/XLSX import, duplicate detection, bulk update/delete, and the configured SQLite PRAGMAs.
