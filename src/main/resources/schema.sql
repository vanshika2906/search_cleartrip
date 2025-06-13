CREATE TABLE airport (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    city VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL
);

-- Create Flight table
CREATE TABLE flight (
    id VARCHAR(255) PRIMARY KEY,
    flight_number VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    flight_date DATE NOT NULL,
    source_airport_id BIGINT NOT NULL,
    destination_airport_id BIGINT NOT NULL,
    available_seats INTEGER NOT NULL,
    total_seats INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    FOREIGN KEY (source_airport_id) REFERENCES airport(id),
    FOREIGN KEY (destination_airport_id) REFERENCES airport(id)
);

-- Create indexes

-- For filtering/searches
CREATE INDEX idx_flight_src_dest_date ON flight (source_airport_id, destination_airport_id, flight_date);

-- For flight detail lookup
CREATE INDEX idx_flight_flight_number ON flight (flight_number);

-- Optional if filtering/sorting by these
CREATE INDEX idx_flight_departure_time ON flight (departure_time);
CREATE INDEX idx_flight_price ON flight (price);

