#!/bin/bash
ls | awk '{print "wc -l "$1}' | bash | awk '{print $1}' | sed '2,$ s/.*/+ &/' | xargs expr

