@echo off

REM --drop        : Drop collection before importing
REM --mode=insert : Insert docs; error on duplicate unique indexes
REM 
REM --mode=upsert : Replace existing modified docs; insert new docs

"C:\Program Files\MongoDB\Tools\100\bin\mongoimport" ^
  --db=stackoverflow ^
  --collection=questions ^
  --type=json ^
  --mode=upsert ^
  --file=so_questions.json

pause
