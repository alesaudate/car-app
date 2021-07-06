#!/bin/bash

# Verify system

if ! command -v docker-compose &> /dev/null
then
	echo "It seems you don't have docker-compose installed."
	echo "Please install it and then run this script again."
	exit 1
fi

# Verify arguments

if [ -z "$1" ]; then
	echo "Correct usage is:"
	echo "start.sh <Google API Key>"
	echo "The system is not running"
	exit 2
fi


# Prepare variables

google_api_key=$1
local_dir=$(pwd)
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"



# Boot dependencies
cd $SCRIPT_DIR/docker
docker-compose up -d 
cd ..

# Boot system

./gradlew bootRun -Pargs="--app.interfaces.outcoming.gmaps.appKey=$google_api_key"

# After the user stops the system, take down dependencies

cd docker
docker-compose stop 


# Navigate to the original folder

cd $local_dir
