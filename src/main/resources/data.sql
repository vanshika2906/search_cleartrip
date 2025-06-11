-- Insert sample airports
INSERT INTO airport (id, name, code, city, country) VALUES
(1, 'John F. Kennedy International', 'JFK', 'New York', 'USA'),
(2, 'Los Angeles International', 'LAX', 'Los Angeles', 'USA'),
(3, 'O''Hare International', 'ORD', 'Chicago', 'USA'),
(4, 'Dallas/Fort Worth International', 'DFW', 'Dallas', 'USA'),
(5, 'Miami International', 'MIA', 'Miami', 'USA');

-- Insert sample flights for direct route 1->2 (JFK to LAX)
INSERT INTO flight (id, flight_number, price, departure_time, arrival_time, flight_date,
                   source_airport_id, destination_airport_id, available_seats, total_seats, status) VALUES
('FL001', 'AA100', 299.99, '2024-04-01 08:00:00', '2024-04-01 11:30:00', '2024-04-01', 1, 2, 150, 180, 'SCHEDULED');

-- Insert sample flights for multi-leg route 1->2->4->5 (JFK->LAX->DFW->MIA)
INSERT INTO flight (id, flight_number, price, departure_time, arrival_time, flight_date,
                   source_airport_id, destination_airport_id, available_seats, total_seats, status) VALUES
-- First leg: JFK to LAX
('FL002', 'AA200', 299.99, '2024-04-01 08:00:00', '2024-04-01 11:30:00', '2024-04-01', 1, 2, 150, 180, 'SCHEDULED'),
-- Second leg: LAX to DFW
('FL003', 'AA201', 199.99, '2024-04-01 13:00:00', '2024-04-01 16:30:00', '2024-04-01', 2, 4, 150, 180, 'SCHEDULED'),
-- Third leg: DFW to MIA
('FL004', 'AA202', 249.99, '2024-04-01 18:00:00', '2024-04-01 21:30:00', '2024-04-01', 4, 5, 150, 180, 'SCHEDULED');