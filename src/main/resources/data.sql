-- Insert sample airports
INSERT INTO airport (name, code, city, country) VALUES
('Kempegowda International Airport', 'BLR', 'Bangalore', 'India'),
('Indira Gandhi International Airport', 'DEL', 'Delhi', 'India'),
('Chhatrapati Shivaji Maharaj International Airport', 'BOM', 'Mumbai', 'India'),
('Chennai International Airport', 'MAA', 'Chennai', 'India'),
('Netaji Subhas Chandra Bose International Airport', 'CCU', 'Kolkata', 'India')
ON CONFLICT (code) DO NOTHING;

-- Insert sample flights (these will be updated by the Catalogue Service)
INSERT INTO flight (id, flight_number, price, departure_time, arrival_time, flight_date, 
                   source_airport_id, destination_airport_id, available_seats, total_seats, stops)
SELECT 
    gen_random_uuid()::text,
    'AI' || LPAD(floor(random() * 1000)::text, 3, '0'),
    (random() * 10000 + 1000)::decimal(10,2),
    CURRENT_TIMESTAMP + (random() * interval '24 hours'),
    CURRENT_TIMESTAMP + (random() * interval '24 hours') + interval '2 hours',
    CURRENT_DATE + (random() * 30)::integer,
    (SELECT id FROM airport WHERE code = 'BLR'),
    (SELECT id FROM airport WHERE code = 'DEL'),
    100 + (random() * 50)::integer,
    150,
    0
FROM generate_series(1, 10)
ON CONFLICT (id) DO NOTHING; 