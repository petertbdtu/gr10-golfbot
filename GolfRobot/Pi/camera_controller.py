#!/usr/bin/python3

from picamera import PiCamera
from time import sleep
from datetime import datetime
from io import BytesIO
import base64
import socket


host = "172.20.10.7"
port = 6000

## NETWORK
print("Enabling Communication...")
client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

## CAMERA
print("Starting Camera...")
camera = PiCamera()
camera.resolution = (1280, 720)
camera.start_preview()
sleep(1)

## SENDING
print("Sending UDP-Packets...")
while(True):
	# Initialize stream
	stream = BytesIO()
	camera.capture(stream, format='jpeg')
	stream.seek(0)
	nbBytes = stream.getbuffer().nbytes
	
	# Send start packet
	s_packet = bytearray()
	s_packet.append(0xFF)
	s_packet += nbBytes.to_bytes(4, byteorder='big')
	#print(str(s_packet))
	#print(str(nbBytes))
	client.sendto(s_packet, (host, port))

	# Send picture in multiple packages
	for i in range(0, nbBytes, 1024):
		length = nbBytes-i if ((i + 1024) > nbBytes) else 1024
		client.sendto(stream.read(length), (host, port))
	
	# Close stream
	stream.close()
	#print("sover")
	sleep(0.2)

## STOPPING
print("Stopping Camera...")
camera.stop_preview()


