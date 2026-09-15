# EC Site starter

This is a minimal Java Servlet/JSP web application structure. It does not yet
contain e-commerce features or a database implementation.

## Requirements

- JDK 17
- Maven
- Apache Tomcat 10.1

## Build

Run `mvn clean package`. Maven creates `target/ec-site.war`, which can be
deployed to Tomcat.

## Main locations

- `src/main/java`: Java source code
- `src/main/resources`: application configuration
- `src/main/webapp`: JSP, HTML, CSS, and other web files
- `src/main/webapp/WEB-INF/views`: JSP pages that should be opened by Servlets
- `database.sql`: database schema and starter data (currently empty)
