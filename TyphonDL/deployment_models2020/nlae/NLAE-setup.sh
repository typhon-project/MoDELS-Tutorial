#!/bin/sh

echo ""

echo ""

echo "Deploying Typhon NLAE Stack on Docker Swarm"

echo "------------------------------------------"

echo ""



docker stack deploy --compose-file nlae-compose.yml Typhon-NLAE


echo ""

echo ""

echo "Typhon NLAE Stack Deployed Successfully"

echo "---------------------------------------"



echo ""

echo ""

echo "Starting Typhon-NLAE"

echo "--------------------"

sleep 120s

docker exec -it $(docker ps --filter name=Typhon-NLAE_jobmanager -l --format={{.ID}}) /bin/bash /opt/flink/bin/flink run -c typhon.nlae.jobs.manager.JobManager /tmp/typhon-nlae-job-manager.jar
