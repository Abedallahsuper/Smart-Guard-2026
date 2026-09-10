# Rodent Guard

Android companion app for a Bluetooth PIR motion sensor used to watch for rodent activity in restaurants, kitchens, and food warehouses.

The sensor sits low on the floor. The phone connects over classic Bluetooth (HC-05 / HC-06), stores every Motion / Quiet event on the device, and estimates the next hour when movement is most likely.

## Features

- Connect to a paired HC Bluetooth module (SPP)
- Live status: online, motion, quiet
- Local event log (Room) with clear-all
- Charts for mix, hourly activity, and last 7 days
- Next-motion hour from past events
- Optional weekly report (notification + share), Fridays 8pm

No cloud. No extra sensors. One real HC device.

## Hardware

- Android 7.0+ (API 24)
- Paired HC-05 or HC-06
- Serial messages: `MOTION_DETECTED` / `NO_MOTION`

## Built with

- Java
- Material 3
- Room
- WorkManager
