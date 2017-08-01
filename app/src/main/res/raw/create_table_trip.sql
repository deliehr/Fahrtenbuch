CREATE TABLE IF NOT EXISTS 'Trip' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_driver_id' INTEGER NULL,
  'fk_car_id' INTEGER NULL,
  'uuid' TEXT NOT NULL UNIQUE,
  'description' TEXT NULL,
  'privateTrip' INTEGER NULL,
  'startAddress' TEXT NULL,
  'startPlace' TEXT NULL,
  'startMileage' REAL NULL,
  'startPointOfTime' TEXT NULL,
  'endPointOfTime' TEXT NULL,
  'endAddress' TEXT NULL,
  'endPlace' TEXT NULL,
  'distance' REAL NULL,
  CONSTRAINT 'fk_driver_id'
    FOREIGN KEY ('fk_driver_id')
    REFERENCES 'Driver' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT 'fk_car_id'
    FOREIGN KEY ('fk_car_id')
    REFERENCES 'Car' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE
);