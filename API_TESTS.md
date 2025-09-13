# Health
curl -s http://localhost:8081/api/health

# Products
curl -s http://localhost:8081/api/products | jq .

# Orders summary
curl -s http://localhost:8081/api/orders | jq .

# Get order by id
curl -s http://localhost:8081/api/orders/1 | jq .

# Create order
curl -s -X POST http://localhost:8081/api/orders \
-H 'Content-Type: application/json' \
-d '{"items":[{"productId":1,"quantity":1},{"productId":2,"quantity":2}]}' | jq .
