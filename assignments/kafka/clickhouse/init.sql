CREATE TABLE IF NOT EXISTS numbers_queue (
    raw_message String
) ENGINE = Kafka
SETTINGS
    kafka_broker_list = 'kafka:9092',
    kafka_topic_list = 'numbers',
    kafka_group_name = 'clickhouse',
    kafka_format = 'RawBLOB',
    kafka_num_consumers = 1;

CREATE TABLE IF NOT EXISTS numbers (
    value Int64,
    sign Enum8('positive' = 1, 'negative' = -1),
    ts DateTime DEFAULT now()
) ENGINE = MergeTree()
ORDER BY ts;

CREATE TABLE IF NOT EXISTS numbers_dlq (
    raw_message String,
    error String,
    ts DateTime DEFAULT now()
) ENGINE = MergeTree()
ORDER BY ts;

CREATE MATERIALIZED VIEW IF NOT EXISTS numbers_success_mv TO numbers AS
SELECT
    toInt64(raw_message) AS value,
    if(value > 0, 'positive', 'negative') AS sign,
    now() AS ts
FROM numbers_queue
WHERE toInt64OrNull(raw_message) IS NOT NULL;

CREATE MATERIALIZED VIEW IF NOT EXISTS numbers_dlq_mv TO numbers_dlq AS
SELECT
    raw_message,
    'Cannot parse as Int64' AS error,
    now() AS ts
FROM numbers_queue
WHERE toInt64OrNull(raw_message) IS NULL;

CREATE TABLE IF NOT EXISTS sums (
    dummy UInt8,
    positive_sum AggregateFunction(sum, Int64),
    negative_sum AggregateFunction(sum, Int64)
) ENGINE = AggregatingMergeTree()
ORDER BY dummy;

CREATE MATERIALIZED VIEW IF NOT EXISTS sums_mv TO sums AS
SELECT
    1 AS dummy,
    sumState(if(sign = 'positive', value, 0)) AS positive_sum,
    sumState(if(sign = 'negative', value, 0)) AS negative_sum
FROM numbers
GROUP BY dummy;