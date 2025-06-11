-- Disable foreign key checks temporarily to avoid order issues during insertion
SET FOREIGN_KEY_CHECKS=0;

-- Clear existing data from relevant tables (optional, use with caution)
-- DELETE FROM expense;
-- DELETE FROM team_member;
-- DELETE FROM team;
-- DELETE FROM budget;
-- DELETE FROM member;

-- Re-enable foreign key checks if you cleared tables and want to ensure integrity for subsequent operations
-- SET FOREIGN_KEY_CHECKS=1;
-- Then disable again for inserts
-- SET FOREIGN_KEY_CHECKS=0;

-- 1. Insert Member
-- Assuming 'id' is auto-increment and will be 1 for the first user.
-- Assuming created_at and updated_at are handled by BaseEntity/JPA or DB defaults.
INSERT INTO member (email, password, nickname, created_at, updated_at)
VALUES ('test@test.com', '12345678', 'user', '2025-05-16 00:00:00', '2025-05-16 00:00:00');
INSERT INTO member (email, password, nickname, created_at, updated_at)
VALUES ('test1@test.com', '12345678', 'user2', '2025-05-16 00:00:00', '2025-05-16 00:00:00');
INSERT INTO member (email, password, nickname, created_at, updated_at)
VALUES ('test2@test.com', '12345678', 'user3', '2025-05-16 00:00:00', '2025-05-16 00:00:00');
-- 2. Insert Budget
-- Assuming 'budget_id' is auto-increment and will be 1.
-- 'setBy' will be the ID of the member created above (assumed to be 1).
-- 'foreignBalance' and 'avgExchangeRate' are set to 0 or NULL.
INSERT INTO budget (total_amount, set_by, balance, foreign_balance, foreign_currency, avg_exchange_rate, created_at, updated_at)
VALUES (0.00, 1, 0.00, 0.00, 'USD', 0.00, '2025-05-16 00:00:00', '2025-05-16 00:00:00');

-- 3. Insert Team
-- Assuming 'team_id' (from @AttributeOverride(name = "id", column = @Column(name = "team_id"))) is auto-increment and will be 1.
-- 'leader_id' is the ID of the member (1).
-- 'budget_id' is the ID of the budget created above (1).
INSERT INTO team (name, team_code, team_password, leader_id, budget_id, created_at, updated_at)
VALUES ('test', 'TEAM001', 'password123', 1, 1, '2025-05-16 00:00:00', '2025-05-16 00:00:00');

-- 4. Insert TeamMember
-- Links the created member (ID 1) to the created team (ID 1).
-- Assuming 'team_member_id' is auto-increment.
INSERT INTO team_member (team_id, member_id, created_at, updated_at)
VALUES (1, 1, '2025-05-16 00:00:00', '2025-05-16 00:00:00');
INSERT INTO team_member (team_id, member_id, created_at, updated_at)
VALUES (1, 2, '2025-05-16 00:00:00', '2025-05-16 00:00:00');
INSERT INTO team_member (team_id, member_id, created_at, updated_at)
VALUES (1, 3, '2025-05-16 00:00:00', '2025-05-16 00:00:00');


-- 5. Insert Expenses (5 records)
-- 'payer_id' is the member ID (1).
-- 'team_id' is the team ID (1).
-- 'amount' is 10000 for each.
-- 'payment_method' is 'CASH' for all.
-- 'created_at' and 'updated_at' are set to the specified date.

INSERT INTO expense (description, amount, category, payment_method, payer_id, team_id, created_at, updated_at)
VALUES ('test1', 10000.00, 'MEAL', 'CASH', 1, 1, '2025-05-16 00:00:00', '2025-05-16 00:00:00');

INSERT INTO expense (description, amount, category, payment_method, payer_id, team_id, created_at, updated_at)
VALUES ('test2', 10000.00, 'SNACK', 'CASH', 1, 1, '2025-05-16 00:00:00', '2025-05-16 00:00:00');

INSERT INTO expense (description, amount, category, payment_method, payer_id, team_id, created_at, updated_at)
VALUES ('test3', 10000.00, 'TRANSPORT', 'CASH', 1, 1, '2025-05-16 00:00:00', '2025-05-16 00:00:00');

INSERT INTO expense (description, amount, category, payment_method, payer_id, team_id, created_at, updated_at)
VALUES ('test4', 10000.00, 'ACCOMMODATION', 'CASH', 1, 1, '2025-05-16 00:00:00', '2025-05-16 00:00:00');

INSERT INTO expense (description, amount, category, payment_method, payer_id, team_id, created_at, updated_at)
VALUES ('test5', 10000.00, 'MISCELLANEOUS', 'CASH', 1, 1, '2025-05-16 00:00:00', '2025-05-16 00:00:00');

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS=1;
