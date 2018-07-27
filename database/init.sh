#!/bin/bash

# MONGODB USER AND DB CREATION
(
echo "setup mongodb auth"
clickBaitDb_user="if (!db.getUser('clickBaitService')) { db.createUser({ user: 'clickBaitService', pwd: 'clickBaitServicePassword', roles: [ {role:'readWrite', db:'clickBaitServiceDatabase'} ]}) }"
contentExtractionDb_user="if (!db.getUser('contentExtractionService')) { db.createUser({ user: 'contentExtractionService', pwd: 'contentExtractionServicePassword', roles: [ {role:'readWrite', db:'contentExtractionServiceDatabase'} ]}) }"
until mongo contentExtractionServiceDatabase --eval "$contentExtractionDb_user" && mongo clickBaitServiceDatabase --eval "$clickBaitDb_user"; do sleep 5; done
killall mongod
sleep 1
killall -9 mongod
) &

echo "start mongodb without auth"
chown -R mongodb /data/db
gosu mongodb mongod --config /mongod.conf "$@"

echo "restarting with auth on"
sleep 5
exec gosu mongodb mongod --auth --config /mongod.conf "$@"