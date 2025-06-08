-- Create Airport table
CREATE TABLE IF NOT EXISTS airport (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(3) NOT NULL UNIQUE,
    city VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Flight table
CREATE TABLE IF NOT EXISTS flight (
    id VARCHAR(36) PRIMARY KEY,
    flight_number VARCHAR(10) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    flight_date DATE NOT NULL,
    source_airport_id BIGINT NOT NULL,
    destination_airport_id BIGINT NOT NULL,
    available_seats INTEGER NOT NULL,
    total_seats INTEGER NOT NULL,
    stops INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_airport_id) REFERENCES airport(id),
    FOREIGN KEY (destination_airport_id) REFERENCES airport(id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_flight_source_dest ON flight(source_airport_id, destination_airport_id);
CREATE INDEX IF NOT EXISTS idx_flight_date ON flight(flight_date);
CREATE INDEX IF NOT EXISTS idx_flight_departure ON flight(departure_time);
CREATE INDEX IF NOT EXISTS idx_flight_arrival ON flight(arrival_time);
CREATE INDEX IF NOT EXISTS idx_airport_code ON airport(code);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_airport_updated_at
    BEFORE UPDATE ON airport
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_flight_updated_at
    BEFORE UPDATE ON flight
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column(); 