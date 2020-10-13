# MoDELS-Tutorial

Welcome to MoDELS Tutorial "[T7] Big Data Polystore Management with TYPHON". Here you will find all the needed information to install the necessary tools to follow the tutorial.

## Easy Setup Guide
TYPHON Polystores are deloyed using Docker. Thus, if you only want to experiment with the already defined Polystore that will be used as a base for the tutorial you need to install Docker Desktop. Installation guides can be found [here](https://www.docker.com/get-started).

After you have Docker install yuo need to checkout this repository (or simply download the code) and run the `docker-compose up` command in the folder `/TyphonDL_noNLAE/test` where the `docker-compose.yaml` file is located.

## Create you own Polystores
TYPHON consists of a number of Domain-specific Languages (DSLs) that allow the definition of Polystore schemas and deployment details. These DSLs (namely the TyphonML and TyphonDL languaages) come as Eclipse plugins. Thus, one needs to firstly install Eclipse and then install the necessry plugins.

### Install Eclipse and Plugins
You need to install Eclipse with support for Epsilon and modelling tools. Installation instruction can be found [here](https://www.eclipse.org/epsilon/download/). After having Eclipse with Epsilon installed, you need to install the TyphonML, TyphonDL, TyphonQL aand Evolution plugins as described below. _NB: do not use an Eclipse newer than Eclipse 2020-6_.

* Navigate to Help -> Install new software ... in your Eclipse as shown below

![Install new software](https://github.com/typhon-project/MoDELS-Tutorial/blob/master/images/eclipse_new_software.png)

* Enter in the "Work with" filed the following updatesite: [http://typhon.clmsuk.com:8082/](http://typhon.clmsuk.com:8082/) and select all the available plugins as shown below

![Install plugins](https://github.com/typhon-project/MoDELS-Tutorial/blob/master/images/eclipse_update_site.png)

Click next, accept the license agreeement and when prompted restart Eclipse. TYPHON plugins are now succesfully installed in your Eclipse.

* Install Docker by following the instructions [here](https://www.docker.com/get-started).

You will now be able to create your own Polystores, generate the deployment script and deploy them using Docker.
  
