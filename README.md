OpenIoT : The Open Platform for the Internet of Things™
---------------------------------------------------------

#### OpenIoT is an open source platform for storing, processing, and analyzing M2M device data. ####

### Installing a Packaged Version

Most of the functionality provided by openiot is contained in a web archive (WAR) which is installed on a customized version of [Tomcat] (http://tomcat.apache.org/). To download the latest version of openiot server (including the latest WAR) check out the [downloads] (http://www.openiot.org/downloads) page on [openiot.org] (http://www.openiot.org/).

### Building from Source
If you want to customize openiot or otherwise have a need to build it from source code, use the following steps.

#### Required Tools #####
* [Apache Maven] (http://maven.apache.org/)
* A [GIT] (http://git-scm.com/) client

#### Clone and Build #####
Clone this repository locally using:

    git clone https://github.com/openiot/openiot.git
    
Navigate to the newly created directory and execute:

    mvn clean install

After the build completes, a file named **openiot.war** will have been created in the **deploy** 
folder. 

#### Building a Full Server #####
Once the **openiot.war** file has been generated, you can create the full server distribution by using:

	mvn -P builderServer clean install
	
This will download a copy of Tomcat, copy the WAR to the webapps folder, and copy the default 
configuration files to the correct location. A zipped archive is generated and may be used 
as the packaged version downloaded from the openiot.org website.

#### Copying into an Existing Tomcat Instance #####
Alternatively, the **openiot.war** archive can be copied to the **webapps** directory of an existing
Tomcat instance. The default openiot loader expects configuration files to be available in the **TOMCAT/conf/openiot/** 
folder. Copy the files from [here] (https://github.com/openiot/openiot/tree/openiot-1.0.2/openiot-core/config/openiot) 
as a starting point. For more details, see the 
[installation guide] (http://docs.openiot.org/current/userguide/installation.html#using-an-existing-tomcat-instance).


openiot Complete Install for Ubuntu
-------------------------------------

    sudo su
    apt-get update -y
    apt-get install unzip openjdk-7-jdk

###Install MongoDB

    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
    echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | sudo tee /etc/apt/sources.list.d/mongodb.list
    apt-get update
    apt-get install -y mongodb-org
    echo "mongodb-org hold" | sudo dpkg --set-selections
    echo "mongodb-org-server hold" | sudo dpkg --set-selections
    echo "mongodb-org-shell hold" | sudo dpkg --set-selections
    echo "mongodb-org-mongos hold" | sudo dpkg --set-selections
    echo "mongodb-org-tools hold" | sudo dpkg --set-selections
    service mongod start

###Install HiveMQ

    cd /opt
    wget --content-disposition http://www.hivemq.com/downloads/releases/latest
    unzip hivemq-2.2.1.zip
    cd hivemq-2.2.1/bin
    ./run.sh &

###Install Solr

> Optional if needed.

    cd ~
    wget http://archive.apache.org/dist/lucene/solr/4.7.2/solr-4.7.2.tgz
    tar -xvf solr-4.7.2.tgz
    cp -R solr-4.7.2/example /opt/solr
    export PWD=/opt/solr/webapps
    apt-get install rabbitmq-server -y
    cd /opt/openiot/bin
    ./startup.sh

###Install openiot Release Version

> Download a openiot server release from the openiot.org website

    cd /opt
    wget https://s3.amazonaws.com/openiot/openiot-server-1.0.1.tar.gz
    tar -zxvf openiot-server-1.0.1.tar.gz
    mv openiot-server-1.0.1 /opt/openiot
    sed -i -- 's/CATALINA_BASE/CATALINA_HOME/g' /opt/openiot/conf/openiot/openiot-server.xml
    export CATALINA_HOME=/opt/openiot
    cd /opt/openiot/bin
    sh startup.sh

> To build and install latest code from GitHub

    apt-get install maven git unzip -y
    apt-get install openjdk-7-jdk tomcat7 -y
    service tomcat7 stop
    git clone https://github.com/openiot/openiot.git
    cd openiot
    mvn clean install
    cp deploy/openiot $(YOUR_TOMCAT_PATH)/webapps/.
    cp -R openiot-core/config/* /var/lib/tomcat7/config
    service tomcat7 start

* * * *

Copyright (c) 2009-2015, [OpenIoT LLC](http://www.openiotplatform.com). All rights reserved.
