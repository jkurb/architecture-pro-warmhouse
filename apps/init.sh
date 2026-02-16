#!/bin/bash

# Exit on any error
set -e

echo "Starting the Smart Home Sensor API..."
echo "Building and starting containers..."
docker-compose up --build -d

echo "Waiting for services to be ready..."
# Wait for PostgreSQL to be ready
for i in {1..30}; do
  if docker exec smarthome-postgres pg_isready -U postgres > /dev/null 2>&1; then
    echo "PostgreSQL is ready!"
    break
  fi
  echo "Waiting for PostgreSQL to start... ($i/30)"
  sleep 1
done

# Check if PostgreSQL is ready
if ! docker exec smarthome-postgres pg_isready -U postgres > /dev/null 2>&1; then
  echo "Error: PostgreSQL did not start within the expected time."
  exit 1
fi

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
for i in {1..30}; do
  if docker exec smarthome-kafka kafka-topics --bootstrap-server kafka:9092 --list > /dev/null 2>&1; then
    echo "Kafka is ready!"
    break
  fi
  echo "Waiting for Kafka to start... ($i/30)"
  sleep 2
done

echo ""
echo "All services are up and running!"
echo ""
echo "Available services:"
echo "  - Smart Home API (monolith):  http://localhost:8080"
echo "  - Temperature API:            http://localhost:8081"
echo "  - Device Registry:            http://localhost:8082"
echo "  - Telemetry Service:          http://localhost:8083"
echo "  - Kafka:                      localhost:9092"
echo "  - PostgreSQL:                 localhost:5432"
echo ""
echo "To view logs, run: docker-compose logs -f"
echo "To stop the services, run: docker-compose down"
