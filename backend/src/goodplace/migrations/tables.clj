(ns goodplace.migrations.tables
  (:require [next.jdbc :as jdbc]))

(defn create-users-table!
  [db]
  (jdbc/execute!
   db
   [(str "CREATE TABLE IF NOT EXISTS users (\n"
         "  id SERIAL PRIMARY KEY,\n"
         "  first_name TEXT NOT NULL,\n"
         "  last_name TEXT NOT NULL,\n"
         "  username TEXT NOT NULL UNIQUE,\n"
         "  email TEXT NOT NULL UNIQUE,\n"
         "  password TEXT NOT NULL UNIQUE,\n"
         "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
         "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
         "  deleted_at TIMESTAMP\n"
         ");")]))

(defn destroy-users-table!
  [db]
  (jdbc/execute!
   db
   ["DROP TABLE users;"]))

(defn create-notes-table!
  [db]
  (jdbc/execute!
   db
   [(str "CREATE TABLE IF NOT EXISTS notes (\n"
         "  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),\n"
         "  user_id INTEGER NOT NULL,\n"
         "  title TEXT NOT NULL,\n"
         "  contents TEXT NOT NULL,\n"
         "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
         "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
         "  deleted_at TIMESTAMP,\n"
         "  CONSTRAINT fk_user_id FOREIGN KEY(user_id) REFERENCES users(id)"
         ");")]))

(defn destroy-notes-table!
  [db]
  (jdbc/execute!
   db
   ["DROP TABLE notes;"]))
