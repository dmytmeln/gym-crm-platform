INSERT INTO training_type (id, training_type_name)
VALUES (1, 'CARDIO'),
       (2, 'STRENGTH'),
       (3, 'YOGA');

INSERT INTO user (id, first_name, last_name, username, password, is_active)
VALUES (1, 'Liam', 'Miller', 'liam.miller', '$2a$10$TBu5GORg5TJMXR7sulyWP.jTe8jrnm9QESKPp6c/IvoejT06MbcOO', true),
       (2, 'Sophia', 'Wilson', 'sophia.wilson', '$2a$10$TBu5GORg5TJMXR7sulyWP.jTe8jrnm9QESKPp6c/IvoejT06MbcOO', true),
       (3, 'Bob', 'Wilson', 'bob.wilson', '$2a$10$TBu5GORg5TJMXR7sulyWP.jTe8jrnm9QESKPp6c/IvoejT06MbcOO', false),
       (4, 'Marcus', 'Stone', 'marcus.stone', '$2a$10$TBu5GORg5TJMXR7sulyWP.jTe8jrnm9QESKPp6c/IvoejT06MbcOO', true),
       (5, 'Sarah', 'Adams', 'sarah.adams', '$2a$10$TBu5GORg5TJMXR7sulyWP.jTe8jrnm9QESKPp6c/IvoejT06MbcOO', true),
       (6, 'Alex', 'Morgan', 'alex.morgan', '$2a$10$TBu5GORg5TJMXR7sulyWP.jTe8jrnm9QESKPp6c/IvoejT06MbcOO', true);

INSERT INTO trainee (id, date_of_birth, address, user_id)
VALUES (1, '1990-05-15', 'NYC', 1),
       (2, '1992-08-20', NULL, 2);

INSERT INTO trainer (id, specialization_id, user_id)
VALUES (1, 1, 4),
       (2, 2, 5),
       (3, 3, 6);

INSERT INTO trainee_trainer (trainee_id, trainer_id)
VALUES (1, 1);

INSERT INTO training (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
VALUES (1, 1, 1, 'Morning HIIT', 1, '2025-01-15', 60),
       (2, 2, 2, 'Evening Weights', 2, '2025-01-16', 90);