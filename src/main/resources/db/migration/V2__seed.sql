INSERT INTO categories (name) VALUES
  ('Food'),
  ('Transport'),
  ('Entertainment'),
  ('Healthcare'),
  ('Salary'),
  ('Utilities'),
  ('Shopping');

INSERT INTO accounts (id, name, balance, created_at) VALUES
  (1, 'Jan Kowalski',   5000.00, '2024-01-01 10:00:00'),
  (2, 'Anna Nowak',    12350.75, '2024-01-15 09:30:00'),
  (3, 'Piotr Wiśniewski', 850.50, '2024-02-01 14:00:00');

INSERT INTO transactions (account_id, category_id, type, amount, description, date) VALUES
  (1, 5, 'INCOME',  6500.00, 'Monthly salary',        '2024-03-01 08:00:00'),
  (1, 1, 'EXPENSE',   45.50, 'Grocery shopping',      '2024-03-02 12:30:00'),
  (1, 2, 'EXPENSE',   15.00, 'Bus ticket monthly',    '2024-03-03 07:45:00'),
  (1, 3, 'EXPENSE',  120.00, 'Cinema and dinner',     '2024-03-05 19:00:00'),
  (1, 6, 'EXPENSE',  200.00, 'Electricity bill',      '2024-03-10 10:00:00'),

  (2, 5, 'INCOME',  15000.00, 'Monthly salary',       '2024-03-01 08:00:00'),
  (2, 7, 'EXPENSE',  350.00, 'Clothes shopping',      '2024-03-04 15:00:00'),
  (2, 4, 'EXPENSE',  250.00, 'Dentist appointment',   '2024-03-06 11:00:00'),
  (2, 1, 'EXPENSE',   89.99, 'Restaurant dinner',     '2024-03-08 20:00:00'),
  (2, 3, 'EXPENSE',   49.99, 'Spotify + Netflix',     '2024-03-09 09:00:00'),

  (3, 5, 'INCOME',   3500.00, 'Monthly salary',       '2024-03-01 08:00:00'),
  (3, 2, 'EXPENSE',    55.00, 'Fuel',                 '2024-03-03 16:00:00'),
  (3, 6, 'EXPENSE',   180.00, 'Internet + phone',     '2024-03-07 10:00:00'),
  (3, 1, 'EXPENSE',    32.50, 'Supermarket',          '2024-03-11 18:30:00');