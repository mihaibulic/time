#!/bin/bash

find ../res -type d -name "layout-*" -exec cp $1 {}/. \;
