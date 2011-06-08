#!/bin/sh
cd ..
git pull
mvn clean compile exec:java -Dexec.mainClass=wikipedia.http.PageHistoryFetcher

