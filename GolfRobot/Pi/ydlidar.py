import serial
import time
import math
import struct

class Lidar:


	def __init__(self, port, baudrate, timeout, debug):
		self.open_serial(port, baudrate, timeout)
		self.debug = debug
		self.restart()


	## SERIAL
	# Method for closing serial connection
	def open_serial(self, port, baudrate, timeout):
		self.__sp = serial.Serial(port, baudrate=baudrate, timeout=timeout)

	def close_serial(self):
		if __sp.is_open:
			__sp.close()


	## COMMANDS (TX)
	# Writes command to serial port
	def __write_command(self, cmd):
		result = bytearray.fromhex('a5'+cmd)
		if self.debug:
			print('|'.join(format(x,'02x') for x in result))
		self.__sp.write(result)

	# Will soft restart lidar
	def restart(self):
		self.__write_command('40')
		time.sleep(3)
		self.set_scan_freq()
		self.set_range_freq()

	# Will start scanning mode
	def start_scan(self):
		self.__write_command('60')

	# Will stop scanning mode
	def stop_scan(self):
		self.__write_command('65')

	# Will toggle ranging frequency (4,8 or 9 KHz)
	def set_range_freq(self):
		self.__write_command('D0')

	# Will get the current ranging frequency
	def get_range_freq(self):
		self.__write_command('D1')

	# Will set frequency
	def set_scan_freq(self):
		self.__write_command('0C')
		self.__write_command('0C')
		self.__write_command('0A')

	"""
	def calc_scan_freq(self):
		self.__get_scan_freq()
		response = bytearray()
		for i in range(11):									# Response from Lidar is 11 bytes
			response.append(self.read_output(1))
		freq = ((response[8] << 8) + response[7])/10		# Only bytes 7+8 is the data, in little endian mode, in deciHz
		return freq
	"""

	# Will get the current scan frequency
	def __get_scan_freq(self):
		self.__write_command('0D')

	## READING (RX)
	# Directly prints the port input
	def write_output(self, number):
		self.__pretty_print((self.__sp.read(number)))

	def read_output(self, number):
		return (self.__sp.read(number))

	# Reads the serialport and finds a packet
	def read_packet(self):
		packet = bytearray()
		#self.__sp.reset_input_buffer()

		# Find Package Header
		if self.debug:
			print("Looking for packet...")
		packet1 = self.__sp.read(1)
		packet2 = self.__sp.read(1)
		while self.__to_int(packet1) != 0xaa and self.__to_int(packet2) != 0x55:
			packet1 = packet2
			packet2 = self.__sp.read(1)
			if self.debug:
				self.__pretty_print(packet2)

		print('out of while')
		if self.debug:
			print("Found packet...")

		packet += self.__sp.read(1)
		sample_quantity = self.__sp.read(1)
		packet += sample_quantity
		packet += self.__sp.read(6+(self.__to_int(sample_quantity)*2))

		#if self.debug:
		return packet


	##UTILITY
	#Can print bytes
	def __pretty_print(self, value):
		print(hex(self.__to_int(value)))

	#Converts input to int by asumming its encoded as hex
	def __to_int(self, input):
		return int.from_bytes(input, "big")

###### TESTING ######
#lidar = Lidar("/dev/ttyAMA0", 230400, 3.0, True)
# UNCOMMENT LINE ABOVE, AND THEN TEST FUNCTIONS BELOW
#lidar.start_scan()
#lidar.stop_scan()
#lidar.restart()
#time.sleep(1)
#lidar.set_scan_freq()
#time.sleep(1)
#freq = (float)(lidar.calc_scan_freq())/10
#print(freq)
#time.sleep(1)
#lidar.read_packet()
#data = bytearray()
#for i in range(11):
#	lidar.pretty_print(lidar.read_output(1))
#print("\n")
#lidar.read_packet()
#lidar.read_packet()
#lidar.read_packet()
#time.sleep(1)
#lidar.stop_scan()
