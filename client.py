import socket
import threading
import json
import sys

# Connecting To Server
# def connect(address, port):
#     client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
#     client.connect(address, port)
#     return client
# client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# client.connect(('127.0.0.1', 55555))
def toJsonString(name, input):
    arguments = input.split(';')
    dictionary ={}
    dictionary[0] = name
    for i in range(len(arguments)):
        dictionary[i+1] = arguments[i]
    json_object = json.dumps(dictionary, 4)
    return json_object
# Listening to Server and Sending Nickname
def receive(client, name):
    while True:
        try:
            # Receive Message From Server
            # If 'NICK' Send Nickname
            message = client.recv(1024).decode('ascii')
            if message == 'NICK':
                client.send(name.encode('ascii'))
            else:
                print(message)
        except:
            # Close Connection When Error
            print("An error occured!")
            client.close()
            break
# Sending Messages To Server
# def write(client, name):
#     while True:
#         try:
#             command = input('')
#             message = '{}: {}'.format(name, toJsonString(name, command))
#             client.send(message.encode('ascii'))
        # except:
        #     # Close Connection When Error
        #     print("An error occured!")
        #     client.close()
        #     break    
        # message =  input(f'{name} > ')
        # if message:
        #     message = message.encode('utf-8')
        #     message_header = f"{len(message):<{10}}".encode('utf-8')
        #     client.send(message_header + message)
# Starting Threads For Listening And Writing

username = input("Give a username: ")
command = input("connect to server:")

arguments = command.split()
if arguments[0] == 'connect':
    if len(arguments)!=3:
        print("Connect needs two arguments: address, port ")
        sys.exit()
    # connect to the client
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect((arguments[1], int(arguments[2])))
    # Starting Threads For Listening And Writing
    receive_thread = threading.Thread(target=receive(client_socket,username))
    receive_thread.start()
    print('gggggg')
    while True:
        command = input('')
        message = '{}: {}'.format(username, toJsonString(username, command))
        client_socket.send(message.encode('ascii'))
        # write_thread = threading.Thread(target=write(client_socket, username))
        # write_thread.start()
else:
    print('Error: Need to connect to the server first!')
    sys.exit()

