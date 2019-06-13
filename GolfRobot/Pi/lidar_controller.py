import ydlidar as yd
import socket

host = "172.20.10.2"
port = 5000

## NETWORK
print("Enabling Communication...")
client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

## LIDAR
print("Starting Lidar...")
lidar = yd.Lidar("/dev/ttyS0", 230400, 3.0, False)  
lidar.start_scan()

## SENDING
print("Sending UDP-Packets...")
while(True):
    msg = lidar.read_packet()
    client.sendto(bytes(msg), (host, port))

## STOPPING
print("Stopping Lidar...")
lidar.stop_scan()


