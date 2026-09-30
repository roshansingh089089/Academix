CREATE TABLE students (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  student_code VARCHAR(50) UNIQUE, first_name VARCHAR(100), middle_name VARCHAR(100), last_name VARCHAR(100),
  full_name VARCHAR(255), phone VARCHAR(30), alternate_phone VARCHAR(30), email VARCHAR(255), gender VARCHAR(30),
  date_of_birth DATE, age INTEGER, father_name VARCHAR(255), mother_name VARCHAR(255), guardian_name VARCHAR(255),
  guardian_phone VARCHAR(30), address_line1 VARCHAR(500), address_line2 VARCHAR(500), city VARCHAR(120),
  district VARCHAR(120), state VARCHAR(120), pin_code VARCHAR(15), school_name VARCHAR(255), college_name VARCHAR(255),
  class_name VARCHAR(80), course VARCHAR(120), batch VARCHAR(120), stream VARCHAR(120), academic_year VARCHAR(30),
  admission_date DATE, status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', source VARCHAR(120), remarks TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_students_full_name_lower ON students(lower(full_name));
CREATE INDEX idx_students_phone ON students(phone);
CREATE INDEX idx_students_email ON students(lower(email));
CREATE INDEX idx_students_city_status ON students(city, status);
CREATE INDEX idx_students_course_batch ON students(course, batch);
CREATE INDEX idx_students_state ON students(state);
CREATE INDEX idx_students_class ON students(class_name);
CREATE INDEX idx_students_status ON students(status);

CREATE TABLE import_jobs (
  id INTEGER PRIMARY KEY AUTOINCREMENT, file_name VARCHAR(255) NOT NULL, total_rows BIGINT NOT NULL DEFAULT 0,
  processed_rows BIGINT NOT NULL DEFAULT 0, success_rows BIGINT NOT NULL DEFAULT 0, updated_rows BIGINT NOT NULL DEFAULT 0,
  failed_rows BIGINT NOT NULL DEFAULT 0, duplicate_rows BIGINT NOT NULL DEFAULT 0, status VARCHAR(40) NOT NULL,
  started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, completed_at TIMESTAMP
);
