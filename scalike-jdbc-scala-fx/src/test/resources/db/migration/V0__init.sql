CREATE TABLE emp(
id SERIAL PRIMARY KEY,
name VARCHAR,
department VARCHAR
);

INSERT INTO emp(name, department) VALUES('Ana', 'IT');
INSERT INTO emp(name, department) VALUES('Mike', 'Marketing');