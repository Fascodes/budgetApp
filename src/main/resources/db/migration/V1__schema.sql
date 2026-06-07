CREATE TYPE "transaction_type" AS ENUM (
  'EXPENSE',
  'INCOME'
);

CREATE TABLE "categories" (
  "id" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "name" varchar
);

CREATE TABLE "transactions" (
  "id" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "account_id" bigint NOT NULL,
  "category_id" bigint NOT NULL,
  "type" transaction_type,
  "amount" numeric(14,2) NOT NULL DEFAULT 0,
  "description" varchar,
  "date" timestamp
);

CREATE TABLE "accounts" (
  "id" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "name" varchar,
  "balance" numeric(14,2) NOT NULL DEFAULT 0,
  "created_at" timestamp DEFAULT now()
);

ALTER TABLE "transactions" ADD FOREIGN KEY ("account_id") REFERENCES "accounts" ("id");

ALTER TABLE "transactions" ADD FOREIGN KEY ("category_id") REFERENCES "categories" ("id");
