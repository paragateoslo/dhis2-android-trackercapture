#!/bin/sh

# Definitions
gitPath=$(git rev-parse --show-toplevel)

# Generate last commit
mkdir -p ${gitPath}/app/src/main/res/raw/
sh ${gitPath}/generate_last_commit.sh

# Use tracker capture SDK branch
cd sdk
git checkout tracker-capture
cd -
