## Подключение 

docker-compose up <br>
docker exec -it kafka-clickhouse-1  clickhouse-client

## Запросы 

SELECT * FROM numbers ORDER BY ts DESC LIMIT 10;

SELECT * FROM numbers_dlq;

SELECT
    sumMerge(positive_sum) AS positive_sum,
    sumMerge(negative_sum) AS negative_sum
FROM sums;