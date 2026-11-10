-- Adds exactly 10,000 seeded employees (P00001-P10000) and one open-ended INR salary each.
-- Run against an unused code range. The seed email prefix identifies records for cleanup.
INSERT INTO EMPLOYEE (
    EMPLOYEE_CODE,
    FIRST_NAME,
    LAST_NAME,
    DEPARTMENT,
    JOB_TITLE,
    ADDRESS,
    PHONE,
    EMAIL,
    COUNTRY,
    ACTIVE
)
SELECT
    'P' || LPAD(CAST(N AS VARCHAR), 5, '0'),
    'Employee' || LPAD(CAST(N AS VARCHAR), 5, '0'),
    'Seed' || LPAD(CAST(N AS VARCHAR), 5, '0'),
    CASE MOD(N, 5)
        WHEN 0 THEN 'Engineering'
        WHEN 1 THEN 'Finance'
        WHEN 2 THEN 'Human Resources'
        WHEN 3 THEN 'Operations'
        ELSE 'Sales'
    END,
    CASE MOD(N, 4)
        WHEN 0 THEN 'Analyst'
        WHEN 1 THEN 'Associate'
        WHEN 2 THEN 'Specialist'
        ELSE 'Manager'
    END,
    'Seed address ' || CAST(N AS VARCHAR),
    CAST(7000000000 + N AS VARCHAR),
    'seed-10000-' || 'P' || LPAD(CAST(N AS VARCHAR), 5, '0') || '@example.test',
    'India',
    TRUE
FROM SYSTEM_RANGE(1, 10000) AS NUMBERS(N);

INSERT INTO SALARY (
    EMPLOYEE_CODE,
    EFFECTIVE_FROM,
    EFFECTIVE_TILL,
    CURRENCY,
    BASIC_SALARY,
    HRA,
    DA,
    TAX,
    EPF,
    NET_SALARY
)
SELECT
    EMPLOYEE_CODE,
    CURRENT_DATE,
    NULL,
    'INR',
    50000.00,
    10000.00,
    5000.00,
    5000.00,
    2000.00,
    58000.00
FROM EMPLOYEE
WHERE EMAIL LIKE 'seed-10000-%@example.test';