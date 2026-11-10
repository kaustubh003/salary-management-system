# Getting Started

### Run with Docker

Build the image from the repository root:

```sh
docker build -t salary-management:local .
```

Run the app with persistent named volumes for the H2 database and generated
payslips. Set a strong HR password before deployment:

```sh
docker run --name salary-management -p 8080:8080 -e TZ="Asia/Kolkata" -e HR_USERNAME=hr.manager -e HR_PASSWORD=change-me -v salary-data:/app/data -v salary-docs:/app/docs salary-management:local
```

The app is then available at `http://localhost:8080`. Keep the `salary-data` and
`salary-docs` volumes when replacing or restarting the container to retain the
database and generated payslips.

After rebuilding the image, recreate the container to use the new image. A
running container continues using the image it was originally created from:

```sh
docker stop salary-management
docker rm salary-management
```

Then run the `docker run` command above again with the same volume mounts and
environment values.

The optional seed scripts are included in the image but are not run
automatically. To add the 10,000 sample employees and their salaries, run:

```sh
docker exec salary-management java -cp /app/h2.jar org.h2.tools.RunScript \
  -url 'jdbc:h2:file:/app/data/salarydb;AUTO_SERVER=TRUE' \
  -user sa \
  -script /app/scripts/seed-10000-employees.sql
```

To delete only those seeded employees and salary rows, run:

```sh
docker exec salary-management java -cp /app/h2.jar org.h2.tools.RunScript \
  -url 'jdbc:h2:file:/app/data/salarydb;AUTO_SERVER=TRUE' \
  -user sa \
  -script /app/scripts/delete-10000-employees.sql
```

The H2 URL must match the database used by the running app. The seed requires
that employee-code range to be unused. Do not delete the sample employees if
they have payments or other dependent records.

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.2/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.2/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.1.2/reference/web/servlet.html)
* [SpringDoc OpenAPI](https://springdoc.org/)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.1.2/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/4.1.2/reference/actuator/index.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.1.2/reference/using/devtools.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [SpringDoc OpenAPI](https://github.com/springdoc/springdoc-openapi-demos/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.
