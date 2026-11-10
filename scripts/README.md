# 10,000-employee seed data

These H2 SQL scripts create 10,000 employees with codes `P00001` through `P10000`.
Each employee gets one open-ended INR salary (effective from the date the seed is
run). No leave balances, leaves, payments, or payslips are created.

Employees created by the seed use the unique email prefix
`seed-10000-...@example.test`. The delete script uses that marker, not the
employee-code range, so it only targets seeded employees and their salary rows.
Do not add payments or other dependent records to these employees if you plan to
remove the seed later.

## Run outside Docker

Stop the application first, then run the commands from the repository root using
the H2 JAR already present in the local Maven repository. Replace `<version>`
with the installed H2 version:

```powershell
java -cp "$env:USERPROFILE\.m2\repository\com\h2database\h2\<version>\h2-<version>.jar" `
  org.h2.tools.RunScript `
  -url "jdbc:h2:file:./data/salarydb" `
  -user sa `
  -script scripts/seed-10000-employees.sql
```

To remove the seeded employees and their salary rows:

```powershell
java -cp "$env:USERPROFILE\.m2\repository\com\h2database\h2\<version>\h2-<version>.jar" `
  org.h2.tools.RunScript `
  -url "jdbc:h2:file:./data/salarydb" `
  -user sa `
  -script scripts/delete-10000-employees.sql
```

The database URL matches `src/main/resources/application.yml`. If the application
uses a different database path, update the URL accordingly.

## Run in Docker

The Docker image contains these scripts and the matching H2 command-line JAR.
They are not run automatically. With the app container running, execute the
commands from the repository root:

```sh
docker exec salary-management java -cp /app/h2.jar org.h2.tools.RunScript -url 'jdbc:h2:file:/app/data/salarydb;AUTO_SERVER=TRUE' -user sa -script /app/scripts/seed-10000-employees.sql
```

To delete the seeded employees and salaries:

```sh
docker exec salary-management java -cp /app/h2.jar org.h2.tools.RunScript \
  -url 'jdbc:h2:file:/app/data/salarydb;AUTO_SERVER=TRUE' \
  -user sa \
  -script /app/scripts/delete-10000-employees.sql
```

The URL assumes the named `salary-data` volume is mounted at `/app/data`. Avoid
deleting the seeded employees if they have payments or other dependent records.