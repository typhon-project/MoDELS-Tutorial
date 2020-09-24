# NLAE Deployment

## Prerequisites:

1. Docker Swarm configured with (at least) 1 MANAGER node and 1 WORKER node.

2. Flink Job Manager runs on a Manager node and requires at least 12GB RAM

3. Flink Task Manager(s) run on Worker node(s) only and by default are configured to use 16GB RAM each. This allocation can be edited in the flink-conf.yaml file and the nlae-compose.yml file.

4. The remaining 3 services i.e. Elasticsearch, RabbitMQ and REST API are deployed on any available node. These service have the following minimum requirements:

    a. Elasticsearch: at least 16GB RAM and large storage capacity

    b. RabbitMQ: 4-8GB RAM and fast SSD storage

5. Create a *models* folder on the Master node and configure a shared file system (e.g. NFS) for the cluster. Share/mount */path/to/models* so that this folder is visible to all nodes in the cluster. This folder will include the custom trained classifier models.

## Configurable Entities:

The following values in the nlae-compose.yml can be edited to configure the job manager:

1. External Port for REST API. Since REST API is the only service visible outside the NLAE Docker Swarm, therefore the Ports for the rest of the services don’t need to be updated as these will only be visible within the NLAE cluster.

2. Flink Job Manager properties in /conf/flink-conf.yaml file.

    a. The heap size for the JobManager JVM <-- update according to RAM available/Job Manager

    b. The heap size for the TaskManager JVM <-- update according to RAM available/Task Manager

    c. taskmanager.numberOfTaskSlots <-- update according to number of Task Managers (replicas) deployed

    d. parallelism.default <-- update according to number of CORES available

    e. Update */path/to/models/* under volumes

3. Flink Task Manager properties in nlae-compose.yml: (can override properties set in flink-conf.yaml).

    a. “FLINK_PROPERTIES=prop1,prop2…”

      1. taskmanager.heap.size

      2. parallelism.default

    b. Flink Task Manager replicas

    c. Update */path/to/models/* under volumes

