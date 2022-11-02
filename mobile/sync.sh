#!/usr/bin/env bash

rsync -r ../public/js/ public/js ; npx cap sync ios
