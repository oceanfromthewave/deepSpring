CREATE TABLE account (
    name VARCHAR(50) PRIMARY KEY,
    balance INT NOT NULL
);

INSERT INTO account (name, balance) VALUES ('A', 1000);
INSERT INTO account (name, balance) VALUES ('B', 500);
INSERT INTO account (name, balance) VALUES ('C', 1000);
INSERT INTO account (name, balance) VALUES ('broken', 500);
INSERT INTO account (name, balance) VALUES ('D', 1000);
INSERT INTO account (name, balance) VALUES ('E', 1000);
INSERT INTO account (name, balance) VALUES ('F', 1000);
INSERT INTO account (name, balance) VALUES ('G', 1000);
