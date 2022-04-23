import socket
import select
import errno
import sys

HEADER_LENGTH = 10
IP = '127.0.0.1'
PORT = 1234

user_name = input("Username: ")
client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect((IP, PORT))
#disable blocking
client.setblocking(False)
username = user_name.encode('utf-8')
username_header = f"{len(username):<{HEADER_LENGTH}}".encode('utf-8')
client.send(username_header + username)

while True:
    message = input(f"{user_name} > ")
    if message:
        message = message.encode('utf-8')
        message_header = f"{len(message) }:<{HEADER_LENGTH}".encode('utf-8')
        client.send(message_header + message)
    try:
        # Receive
        while True:
            username_header = client.recv(HEADER_LENGTH)
            if not len(username_header):
                print("connection closed by the server")
                sys.exit()
            username_length = int(username_header.decode('uft-8'))
            # Receive exactly the length of header amout of data
            username = client.recv(username_length).decode('utf-8')
            # Receive the data header
            message_header = client.recv(HEADER_LENGTH)
            message_length = int(message_header.decode('utf-8'))
            # Receive the content of message
            message = client.recv(message_length).decode('utf-8')
            print(f"{username} > {message}")
    except IOError as e:
        # ignore the following errors
        if e.errno != errno.EAGAIN and e.errno != errno.EWOULDBLOCK:
            print('Reading error', str(e))
            sys.exit()
        continue
    except Exception as e:
        print('Error', str(e))
        sys.exit()
