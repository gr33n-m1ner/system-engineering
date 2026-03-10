import os
import time
import random
from kafka import KafkaProducer

bootstrap_servers = os.environ.get('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9092')
topic = os.environ.get('KAFKA_TOPIC', 'numbers')

producer = KafkaProducer(
    bootstrap_servers=bootstrap_servers,
    value_serializer=lambda v: str(v).encode('utf-8')
)

print(f"Producing to {bootstrap_servers} topic {topic}")

while True:
    number = random.randint(-100, 100)
    producer.send(topic, value=number)
    print(f"Sent: {number}")
    time.sleep(1)