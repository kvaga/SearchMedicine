#!/bin/sh
# crontab -e # it sets the cron
# 0 */4 * * * exec.sh # sets the cron for each 4 hours

TELEGRAM_SEND_MESSAGE_JAR=/home/opc/telegramsendmessage-1.2.jar
cd /home/opc/SearchMedicine/ && java -cp $TELEGRAM_SEND_MESSAGE_JAR:bin/ Exec
