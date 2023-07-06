@echo off

cd 

"C:\Program Files\MongoDB\Tools\100\bin\mongoexport" ^
  --db=stackoverflow ^
  --collection=questions ^
  --type=json ^
  --jsonFormat=canonical ^
  --out=so_questions.json

pause
