$json = '{ "username": "root", "password": "1234" }'
curl.exe -X POST "http://localhost:8080/login" -H "Content-Type: application/json" -d $json
