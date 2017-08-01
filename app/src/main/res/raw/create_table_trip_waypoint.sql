CREATE TABLE IF NOT EXISTS 'TripWaypoint' (
  'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  'fk_trip_id' INTEGER NOT NULL,
  'latitude' REAL NULL,
  'longitude' REAL NULL,
  'accuracy' INTEGER NULL,
  'elevation' REAL NULL,
  'pointOfTime' TEXT NULL,
  CONSTRAINT 'fk_trip_id'
    FOREIGN KEY ('fk_trip_id')
    REFERENCES 'Trip' ('id')
    ON DELETE CASCADE
    ON UPDATE CASCADE);