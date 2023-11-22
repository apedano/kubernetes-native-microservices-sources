#!/bin/bash

for i in {1..3}
 do
    echo "call number $i"
    curl -s -k 'GET' 'http://localhost:8082/resilience/do-delivery-bulkhead'
 done

