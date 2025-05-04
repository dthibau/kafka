DROP TABLE IF EXISTS coursier;


CREATE TABLE coursier (
    coursierId INT NOT NULL,
    kafkaOffset INT NOT NULL,
    PRIMARY KEY (coursierId, kafkaOffset)
);

