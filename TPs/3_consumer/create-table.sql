CREATE TABLE courier (
    courierId varchar(20) NOT NULL,
    kafkaOffset INT NOT NULL,
    PRIMARY KEY (courierId, kafkaOffset)
);

