#!/bin/sh
git update
mvn clean compile exec:java -Dexec.mainClass=wikipedia.http.PageHistoryFetcher

