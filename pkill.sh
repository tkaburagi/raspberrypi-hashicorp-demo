#!/bin/sh

pkill consul
pkill vault
pkill java
pkill prometheus
brew services stop  grafana
