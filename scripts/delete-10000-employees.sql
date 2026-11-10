-- Deletes only employees created by seed-10000-employees.sql and their salary rows.
-- Do not run after adding payments or other dependent records for these employees.
DELETE FROM SALARY
WHERE EMPLOYEE_CODE IN (
    SELECT EMPLOYEE_CODE
    FROM EMPLOYEE
    WHERE EMAIL LIKE 'seed-10000-%@example.test'
);

DELETE FROM EMPLOYEE
WHERE EMAIL LIKE 'seed-10000-%@example.test';