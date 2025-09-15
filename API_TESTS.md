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


## Recommendations
curl -s "http://localhost:8081/api/recommendations/1?limit=5" | jq .
curl -s -X POST "http://localhost:8081/api/recommendations/1/feedback" \
-H "Content-Type: application/json" \
-d '{"productId":3,"action":"added_to_cart"}'

## Feedback 
Record feedback on a recommended product.
Valid actions: viewed, ignored, added_to_cart, purchased.

curl -i -X POST "http://localhost:8081/recommendations/1/feedback" \
-H "Content-Type: application/json" \
-d '{"productId":3,"action":"viewed"}'

curl -i -X POST "http://localhost:8081/recommendations/1/feedback" \
-H "Content-Type: application/json" \
-d '{"productId":3,"action":"added_to_cart"}'

curl -i -X POST "http://localhost:8081/recommendations/1/feedback" \
-H "Content-Type: application/json" \
-d '{"productId":3,"action":"purchased"}'

curl -i -X POST "http://localhost:8081/recommendations/1/feedback" \
-H "Content-Type: application/json" \
-d '{"productId":3,"action":"ignored"}'

## Popular products 

# Get top 5 most popular products
curl -i "http://localhost:8081/popular?limit=5"

