-- name: create-loc_records-table!
CREATE TABLE IF NOT EXISTS loc_records (
	id VARCHAR(128) PRIMARY KEY,
	geo GEOMETRY,
	update_time TIMESTAMP)

-- name: list-all
SELECT * FROM loc_records ORDER BY update_time DESC

-- name: cnt
SELECT count(*) AS cnt FROM loc_records

-- name: update-record!
UPDATE loc_records SET geo = GeomFromText(:point), update_time=now() WHERE id = :id

-- name: delete-record!
DELETE FROM loc_records WHERE id = :id
