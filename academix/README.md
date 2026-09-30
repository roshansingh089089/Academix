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

Open `http://localhost:5173`. No PostgreSQL service, username, password, or database creation is needed. Backend/network failures are shown explicitly rather than being converted into empty or demo results.

During development the browser calls relative `/api/*` URLs. Vite proxies those requests to `http://localhost:8080`, so the frontend uses the same URL shape locally and in production.

## Vercel frontend and backend deployment

The production request path is:

```text
Browser -> Vercel React frontend -> /api/* rewrite -> public Spring Boot backend -> SQLite
```

The frontend must be deployed with `frontend/` as the Vercel project root. Before deploying, replace this placeholder in `frontend/vercel.json`:

```text
https://replace-with-backend-host.example.com
```

with the real HTTPS origin of the deployed Spring Boot service. Keep `/api/:path*` on both sides of the rewrite. The API rewrite is listed before the SPA fallback so API calls never resolve to `index.html`. `VITE_API_BASE_URL` should normally remain unset: relative `/api` requests are required for the single-public-URL architecture.

The backend currently uses SQLite and has not been migrated to a managed database. A production host must provide a persistent mounted disk and set:

```bash
SPRING_PROFILES_ACTIVE=production
ACADEMIX_DB_PATH=/mounted/persistent/path/academix.db
SERVER_PORT=8080 # omit when the provider supplies PORT
MAX_UPLOAD_SIZE=100MB
```

`ALLOWED_ORIGINS` accepts a comma-separated list for intentional direct browser-to-backend access. It is not needed for normal same-origin requests through the Vercel rewrite. Do not use `*`.

SQLite is safe only for a single backend instance sharing one persistent disk. Ephemeral filesystems will lose data on restart or redeployment, and multiple independently scaled instances must not use separate SQLite files. A later move to PostgreSQL would require restoring its JDBC driver, dialect, migrations, and provider-supplied datasource credentials; this deployment-preparation change does not perform that database migration.

### File upload constraint

The browser continues sending `FormData` without manually setting `Content-Type`, preserving the multipart boundary and upload progress. Spring accepts up to `MAX_UPLOAD_SIZE`, but the Vercel external rewrite and backend provider can enforce lower request-size or timeout limits. Vercel documents a maximum 120-second external rewrite response time. Validate representative production Excel files through the deployed rewrite; if the chosen path rejects large bodies, direct-to-storage/background import architecture will be required rather than increasing only Spring's limit.

## API overview

- `GET /api/students` — paginated search, filtering, and sorting
- `GET/POST /api/students/{id}` — student detail and updates
- `POST /api/students/bulk-update` and `/bulk-delete`
- `GET /api/students/duplicates` — duplicate phone groups
- `POST /api/imports` — batched CSV/XLS/XLSX import
- `GET /api/imports` — import history
- `GET /api/dashboard/stats` — efficient aggregate dashboard data
- `GET /api/health` — minimal backend availability check

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
