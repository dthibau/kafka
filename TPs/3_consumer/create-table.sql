CREATE TABLE courier (
    courierId INT NOT NULL,
    kafkaOffset INT NOT NULL,
    PRIMARY KEY (courierId, kafkaOffset)
);

